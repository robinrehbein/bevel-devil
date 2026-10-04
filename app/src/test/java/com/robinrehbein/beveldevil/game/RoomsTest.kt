package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Extend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** U18 "Who says the room ends here?": levels of several rooms, the breach, and the camera that follows room by room. */
class RoomsTest {
    private val dt = Bot.DT

    // ---------- DSL ----------

    @Test
    fun roomsSitSideBySideWithLocalCoordinates() {
        val l = Level(T("x", "x"), T("x", "x"), rooms = 3) {
            border(); floor()
            room(0) { spawn(); door() }
            room(1) { put(3, 4, 'x'); assertEquals(ROOM_COLS, width) }
            room(2) { fill(0..1, 2..2, 'y'); assertEquals('.', at(5, 5)) }
        }
        assertEquals(96, l.cols)
        assertEquals(18, l.rows)
        assertEquals(3, l.map.rooms)
        assertEquals('x', l.map.grid[4][35])
        assertEquals('y', l.map.grid[2][64])
        assertEquals('y', l.map.grid[2][65])
        // every room has its own frame: two-tile walls between rooms, the ceiling across all, floors everywhere
        for (x in listOf(0, 31, 32, 63, 64, 95)) assertEquals("wall at $x", '#', l.map.grid[7][x])
        assertEquals('.', l.map.grid[7][30])
        assertEquals('.', l.map.grid[7][33])
        assertEquals('#', l.map.grid[0][50])
        assertEquals('#', l.map.grid[16][70])
        assertEquals(35, roomX(1, 3))
        assertEquals(68.5f, roomX(2, 4.5f))
    }

    @Test
    fun aRoomOnlyTakesItsOwnColumns() {
        assertThrows(IllegalArgumentException::class.java) { Level(T("x", "x"), T("x", "x"), rooms = 2) { room(0) { put(32, 3, '#') } } }
        assertThrows(IllegalArgumentException::class.java) { Level(T("x", "x"), T("x", "x"), rooms = 2) { room(2) { } } }
        // outside room(), global coordinates reach every room
        Level(T("x", "x"), T("x", "x"), rooms = 2) { put(40, 3, '#'); border(); floor(); put(2, 14, 'P'); put(60, 14, 'D') }
    }

    @Test
    fun aRematchKeepsTheRooms() {
        val l = Level(T("x", "x"), T("x", "x"), rooms = 2, rematch = listOf(Round(T("y", "y")) { room(1) { put(5, 5, 'z') } })) {
            border(); floor(); room(0) { spawn(); door() }
        }
        assertEquals(64, l.rounds[1].cols)
        assertEquals('z', l.rounds[1].map.grid[5][37])
    }

    @Test
    fun oneRoomLevelsAreUnchanged() {
        for (l in Levels.all) {
            assertEquals(l.name.en, ROOM_COLS, l.cols)
            val w = World(l)
            assertEquals(1, w.rooms)
            assertEquals(0f, w.camX)
        }
    }

    @Test
    fun extendingIntoAMissingRoomFailsEarly() {
        val l = Level(T("x", "x"), T("x", "x"), traps = listOf(trap(Trigger.AtDoor, Extend(into = 1)))) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }
        assertThrows(IllegalArgumentException::class.java) { World(l) }
    }

    // ---------- the breach ----------

    @Test
    fun theDoorIsNotTheEnd() {
        val b = Bot(RoomDemos.annex).hopR(13f).rightUntil(3f) { it.cracks.isNotEmpty() }
        val w = b.world
        assertEquals(WorldState.PLAYING, w.state)
        assertEquals(Card.ANNEX, w.lastCard)
        // the wall still stands while it cracks; the door can't be taken meanwhile
        val breach = w.cracks.single()
        assertTrue(breach.crumble)
        assertTrue(breach.group.visible)
        assertEquals(setOf(31f, 32f), breach.group.pieces.map { it.hx }.toSet())
        assertEquals(setOf(12f, 13f, 14f), breach.group.pieces.map { it.hy }.toSet())
        b.wait(0.3f).expect(WorldState.PLAYING)
        assertTrue(breach.group.visible)
        // then it crumbles and the door slips through into room 2
        b.wait(0.4f)
        assertFalse(breach.group.visible)
        b.waitFor(4f) { !it.door.moving }.expect(WorldState.PLAYING)
        assertEquals(roomX(1, 28) - 0.1f, w.door.box.x, 1e-4f)
        // the camera stayed: the player has not crossed yet
        assertEquals(0, w.room)
        assertEquals(0f, w.camX)
    }

    @Test
    fun mephiSaysItAndLaughs() {
        val g = game(RoomDemos.annex)
        g.input.right = true
        var t = 0f
        while (t < 6f && g.world!!.cracks.isEmpty()) {
            hopBlock(g)
            g.update(dt); t += dt
        }
        repeat(3) { g.update(dt) }
        assertEquals(Extend.ROOM_LINE.toString(), g.bubble)
        assertEquals(Mood.LAUGH, g.mood)
        assertEquals(Card.ANNEX, g.card)
    }

    // ---------- camera ----------

    /** The annex with the wall broken and the door gone, the player standing in the breach. */
    private fun throughTheDoor(): Bot = Bot(RoomDemos.annex).hopR(13f).rightUntil(3f) { it.cracks.isNotEmpty() }.waitFor(4f) { !it.door.moving }

    @Test
    fun theCameraPansWhenThePlayerCrosses() {
        val b = throughTheDoor()
        val w = b.world
        b.rightUntil(3f) { it.panning }
        // as soon as the center is past the seam: the player is never mostly off-screen
        assertTrue(w.player.box.cx > 32f && w.player.box.cx < 32.1f)
        assertEquals(1, w.room)
        assertEquals(1, w.panDir)
        assertTrue(w.events.any { it is Event.Pan && it.dir == 1 })
        // frozen during the pan: time, player and saws stand still, although right is held
        val t = w.time
        val x = w.player.box.x
        val input = Controls().apply { right = true }
        var steps = 0
        val cams = ArrayList<Float>()
        while (w.panning) { w.step(dt, input); steps++; cams += w.camX }
        assertEquals(t, w.time)
        assertEquals(x, w.player.box.x)
        // about half a second, eased, ending exactly on room 2
        assertTrue("$steps steps", steps in (World.PAN / dt - 2).toInt()..(World.PAN / dt + 2).toInt())
        assertTrue(cams.zipWithNext().all { (a, c) -> c >= a })
        assertEquals(32f, w.camX)
        // then the game goes on
        w.step(dt, input)
        assertTrue(w.time > t)
        assertTrue(w.player.box.x > x)
    }

    @Test
    fun backtrackingPansBack() {
        val b = throughTheDoor()
        val w = b.world
        b.rightUntil(3f) { it.panning }.waitWhile(1f) { it.panning }
        assertEquals(32f, w.camX)
        b.right(0.3f)
        // back through the breach to room 1
        b.leftUntil(3f) { it.panning }
        assertEquals(0, w.room)
        assertEquals(-1, w.panDir)
        b.waitWhile(1f) { it.panning }
        assertEquals(0f, w.camX)
        assertTrue(w.player.box.cx < 32f)
        // standing in the gap doesn't flicker: a little back and forth across the seam stays put
        b.right(0.05f)
        assertFalse(w.panning)
    }

    @Test
    fun dyingInRoomTwoStartsOverInRoomOne() {
        val g = game(RoomDemos.annex)
        assertEquals(0, g.world!!.room)
        // through the door and the breach, then straight on into room 2's pit
        g.input.right = true
        var t = 0f
        while (t < 8f && g.world!!.room == 0) {
            hopBlock(g)
            g.update(dt); t += dt
        }
        assertEquals(1, g.world!!.room)
        val dying = g.world!!
        while (t < 16f && dying.state == WorldState.PLAYING) { g.update(dt); t += dt }
        assertEquals(WorldState.DEAD, dying.state)
        assertTrue("died in room 2", dying.player.box.cx > 32f)
        g.input.right = false
        while (t < 20f && g.world === dying) { g.update(dt); t += dt }
        val fresh = g.world!!
        assertNotEquals(dying, fresh)
        assertEquals(0, fresh.room)
        assertEquals(0f, fresh.camX)
        assertFalse(fresh.panning)
        assertEquals(1, g.deaths)
    }

    @Test
    fun theFakeClearGoesOnIntoTheNextRoom() {
        // the door is a fake end: the clear screen, then Mephi breaks the wall open behind it
        val l = Level(
            T("x", "x"), T("x", "x"), rooms = 2,
            traps = listOf(trap(Trigger.AtDoor, Action.FakeWin(FakeEnd.CLEAR, null, listOf(Extend(into = 1, door = roomX(1, 28) to 14))))),
        ) { border(); floor(); room(0) { spawn(); door() } }
        val b = Bot(l).rightUntil(6f) { it.fake != null }.waitFor(10f) { it.cracks.isNotEmpty() }
        b.waitFor(4f) { !it.door.moving && !it.cracks[0].group.visible }.expect(WorldState.PLAYING)
        assertEquals(roomX(1, 28) - 0.1f, b.world.door.box.x, 1e-4f)
        b.rightUntil(3f) { it.panning }.waitWhile(1f) { it.panning }.rightTo(roomX(1, 30f)).expect(WorldState.WON)
    }

    @Test
    fun aRoomCanStartOpen() {
        val l = Level(T("x", "x"), T("x", "x"), rooms = 2, start = listOf(Extend(into = 1, line = null))) {
            border(); floor(); room(0) { spawn() }; room(1) { door() }
        }
        val b = Bot(l).wait(1f)
        assertFalse(b.world.cracks.single().group.visible)
        b.rightUntil(4f) { it.panning }.waitWhile(1f) { it.panning }.rightTo(roomX(1, 30f)).expect(WorldState.WON)
    }

    @Test
    fun theSpikePauseDoesNotFireDuringAPan() {
        val l = Level(
            T("x", "x"), T("x", "x"), rooms = 2,
            start = listOf(Extend(into = 1, line = null), Action.PauseTrap(PauseTrick.SPIKE)),
        ) { border(); floor(); room(0) { spawn() }; room(1) { door() } }
        val b = Bot(l).rightUntil(4f) { it.panning }
        assertTrue(b.world.pausePressed())
        assertEquals(WorldState.PLAYING, b.world.state)
        b.waitWhile(1f) { it.panning }
        assertFalse(b.world.pausePressed())
        assertEquals(WorldState.DEAD, b.world.state)
    }

    @Test
    fun aRematchStartsInTheSpawnRoom() {
        val l = Level(
            T("x", "x"), T("x", "x"), rooms = 2,
            traps = listOf(trap(Trigger.AtDoor, Extend(into = 1, door = roomX(1, 6) to 14))),
            rematch = listOf(Round(T("again", "nochmal"))),
        ) { border(); floor(); room(0) { spawn(); door() } }
        val g = game(l)
        g.input.right = true
        var t = 0f
        while (t < 12f && g.round == 0) { g.update(dt); t += dt }
        assertEquals(1, g.round)
        assertEquals(0, g.world!!.room)
        assertEquals(0f, g.world!!.camX)
    }

    // ---------- the bot solves them ----------

    @Test
    fun twoRooms() {
        throughTheDoor()
            .rightUntil(3f) { it.panning }
            .hopR(roomX(1, 11.2f), hold = 0.5f).hopR(roomX(1, 20.5f)).rightTo(roomX(1, 30f)).expect(WorldState.WON)
    }

    @Test
    fun twoRoomsNaively() {
        // straight on after the door: the pit in room 2
        throughTheDoor().right(4f).expect(WorldState.DEAD)
    }

    @Test
    fun threeRooms() {
        val b = Bot(RoomDemos.sprawl).rightUntil(4f) { it.cracks.isNotEmpty() }.waitFor(4f) { !it.door.moving }
        b.rightUntil(3f) { it.panning }.waitWhile(1f) { it.panning }
        assertEquals(1, b.world.room)
        // the pit, then the saw: pass under it while it is up
        b.hopR(roomX(1, 6.5f)).rightTo(roomX(1, 16f))
            .waitFor(4f) { w -> w.saws[0].y < 9.2f }
            .rightUntil(4f) { it.cracks.size == 2 }
        assertEquals(2, b.world.cracks.size)
        b.waitFor(4f) { !it.door.moving && !it.cracks[1].group.visible }
        b.rightUntil(3f) { it.panning }.waitWhile(1f) { it.panning }
        assertEquals(2, b.world.room)
        assertEquals(64f, b.world.camX)
        // the belt carries; hop its spike
        b.hopR(roomX(2, 14.3f)).rightTo(roomX(2, 30f)).expect(WorldState.WON)
    }

    @Test
    fun theSawStillKillsInRoomTwo() {
        val b = Bot(RoomDemos.sprawl).rightUntil(4f) { it.cracks.isNotEmpty() }.waitFor(4f) { !it.door.moving }
        b.rightUntil(3f) { it.panning }.waitWhile(1f) { it.panning }
        b.hopR(roomX(1, 6.5f)).rightTo(roomX(1, 16f)).waitFor(4f) { w -> w.saws[0].y < 9.2f }.wait(0.45f).right(1f).expect(WorldState.DEAD)
    }

    /** Holding right in the game: jumps (held) over the annex's block in room 1. */
    private fun hopBlock(g: Game) {
        val p = g.world!!.player
        val x = p.box.cx
        g.input.jump = x in 12.3f..14.5f
        if (x in 12.3f..12.8f && p.grounded) g.input.jumpPressed = true
    }

    private fun game(l: Level): Game {
        val g = Game(object : Progress {
            override var unlocked = 1
            override var sound = false
            override var stickScheme = false
            override var buttonSize = 1
            override var haptics = false
            override var leftHanded = false
            override var introSeen = true
            override var tiltSensor = true
            override fun bestDeaths(level: Int): Int? = null
            override fun saveBest(level: Int, deaths: Int) {}
            override fun cardFound(card: Card) = false
            override fun findCard(card: Card) {}
            override fun cardDeaths(card: Card) = 0
            override fun addCardDeath(card: Card) {}
        }, object : Audio { override fun play(sound: Sound) {} })
        g.startCustom(l)
        return g
    }
}
