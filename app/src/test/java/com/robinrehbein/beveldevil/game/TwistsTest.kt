package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.FrameCrack
import com.robinrehbein.beveldevil.game.Action.Flip
import com.robinrehbein.beveldevil.game.Action.Ghost
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.PauseTrap
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Resumed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tiny demo levels for the meta twists. Test-only: the real levels live in the World*Part* files. */
object TwistDemos {
    /** The door "ends the game": credits roll, then the last lines become the stairs to the real door. */
    val credits = Level(
        name = T("Roll Credits", "Abspann"),
        intro = T("Last level. Promise.", "Letztes Level. Versprochen."),
        legend = mapOf('c' to Glyph(spike = false, hidden = true)),
        traps = listOf(trap(AtDoor, FakeWin(FakeEnd.CREDITS, 'c', DoorTo(1, 8)))),
    ) {
        border(); floor()
        fill(1..13, 9..9, 'c')
        fill(15..19, 11..11, 'c')
        fill(22..26, 13..13, 'c')
        put(2, 14, 'P'); put(28, 14, 'D')
    }

    /** A fake clear screen; afterwards the floor is gone and the door went back to the start. */
    val clear = Level(
        name = T("Too Easy", "Zu einfach"),
        intro = T("Just walk. Honestly.", "Einfach laufen. Ehrlich."),
        traps = listOf(trap(AtDoor, FakeWin(FakeEnd.CLEAR, null, Hide('f'), DoorTo(3, 14)))),
    ) {
        border(); floor()
        fill(13..16, 15..17, 'f')
        put(8, 14, 'P'); put(28, 14, 'D')
    }

    /** The pause button dodges; pausing for real opens the wall, and takes the floor with it. */
    val pause = Level(
        name = T("Coffee Break", "Kaffeepause"),
        intro = T("You look tired. Take a break.", "Du wirkst müde. Mach mal Pause."),
        traps = listOf(
            trap(After(0.3f), PauseTrap(PauseTrick.DODGE)),
            trap(Resumed(), Hide('w'), Hide('a'), Say(T("Refreshed? The floor took a break too.", "Erholt? Der Boden macht jetzt auch Pause."))),
        ),
    ) {
        border(); floor()
        fill(22..22, 1..14, 'w')
        fill(12..14, 15..17, 'a')
        put(2, 14, 'P'); put(28, 14, 'D')
    }

    /** A piece of the golden frame breaks off the ceiling and crashes onto the path. */
    val crack = Level(
        name = T("Load-Bearing Frame", "Tragender Rahmen"),
        intro = T("Nice frame, isn't it?", "Schöner Rahmen, oder?"),
        traps = listOf(trap(PastX(4f), FrameCrack(16, 0, 19, 0, warn = 0.9f), Say(T("Crack.", "Knack.")))),
    ) {
        border(); floor()
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    /** The picture turns upside down over a spike pit, then the CRT loses its vertical hold. */
    val flip = Level(
        name = T("Headstand", "Kopfstand"),
        intro = T("Hold your phone properly.", "Halt dein Handy mal richtig."),
        traps = listOf(
            trap(PastX(6f), Flip(3f)),
            trap(PastX(21f), Roll(1.2f)),
        ),
    ) {
        border(); floor()
        fill(13..14, 14..14, '^')
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    /** Your last attempt walks again, one second behind you. */
    val ghost = Level(
        name = T("git blame", "git blame"),
        intro = T("Replaying your last attempt.", "Ich spiele deinen letzten Versuch ab."),
        traps = listOf(trap(After(0f), Ghost(1f))),
    ) {
        border(); floor()
        fill(15..16, 14..14, '^')
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    val all = listOf(credits, clear, pause, crack, flip, ghost)
}

class TwistsTest {
    private class Prog : Progress {
        override var unlocked = 1
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        override var tiltSensor = true
        val best = HashMap<Int, Int>()
        override fun bestDeaths(level: Int): Int? = best[level]
        override fun saveBest(level: Int, deaths: Int) { best[level] = deaths }
        override fun cardFound(card: Card) = false
        override fun findCard(card: Card) {}
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private fun Game.tapOn(h: Hit) = tap(h.x + 1f, h.y + 1f)
    private fun Game.run(seconds: Float) { var t = 0f; while (t < seconds) { update(DT); t += DT } }
    private fun sandbox(level: Level, p: Progress = Prog()) = Game(p, silent).apply { sandbox = level; startLevel(0) }

    // ---------- demo levels, solved ----------

    @Test fun demosBuild() = TwistDemos.all.forEach { World(it) }

    @Test
    fun credits() = Bot(TwistDemos.credits).right(4f)
        .also { assertNotNull(it.world.fake) }
        .waitWhile { it.fake != null }
        .also { assertTrue(it.world.group('c').visible); assertEquals('c', it.world.creditPlatforms) }
        .waitWhile(2f) { !it.player.grounded }
        .leftTo(27.6f).leftJump(0.35f).landLeft()
        .leftTo(22.4f).leftJump(0.35f).landLeft()
        .leftTo(15.4f).leftJump(0.35f).landLeft()
        .left(3f)
        .expect(WorldState.WON)

    @Test
    fun creditLinesStopOnThePlatformRows() {
        val w = World(TwistDemos.credits)
        val lines = Twists.creditLines(w.pieces, 'c', w.cols)
        assertEquals(Twists.credits.size, lines.size)
        assertEquals(listOf(9.5f, 11.5f, 13.5f), lines.mapNotNull { it.stop })
        assertEquals(listOf(null, null, null, null), lines.take(4).map { it.stop })
        // the top platform spans tiles 1..13
        assertEquals(7.5f, lines[4].x, 0.01f)
        val len = Twists.creditsLength(lines, w.rows)
        lines.forEach { l -> assertEquals(l.stop ?: -1f, Twists.creditY(l, len, w.rows).coerceAtLeast(-1f), 0.01f) }
    }

    @Test
    fun fakeClear() = Bot(TwistDemos.clear).right(4f)
        .also { assertEquals(FakeEnd.CLEAR, it.world.fake?.end) }
        .waitWhile { it.fake != null }
        .waitWhile(2f) { !it.player.grounded || it.door.moving }
        .leftTo(18f).hopL(17.4f).left(4f)
        .expect(WorldState.WON)

    @Test
    fun fakeClearNaiveStaysOnTheFloorAndDies() = Bot(TwistDemos.clear).right(4f).waitWhile { it.fake != null }.left(4f).expect(WorldState.DEAD)

    @Test
    fun pause() = Bot(TwistDemos.pause).wait(0.5f)
        .tapPause().also { assertEquals(1, it.world.dodges); assertEquals(0, it.world.resumes) }
        .right(1f).expect(WorldState.PLAYING)

    @Test
    fun pauseSolved() = Bot(TwistDemos.pause).wait(0.5f).pauseResume().wait(0.1f)
        .also { assertFalse(it.world.group('w').visible) }
        .hopR(10.6f).right(4f)
        .expect(WorldState.WON)

    @Test
    fun wallHoldsWithoutAPause() = Bot(TwistDemos.pause).right(4f).also { assertTrue(it.world.player.box.cx < 22f) }.expect(WorldState.PLAYING)

    @Test
    fun crack() = Bot(TwistDemos.crack).rightTo(10f).wait(1.6f)
        .also { assertTrue(it.world.cracks.single().fell) }
        .hopR(14.4f).right(4f)
        .expect(WorldState.WON)

    @Test
    fun crackCrushesTheNaiveRun() = Bot(TwistDemos.crack).right(4f).expect(WorldState.DEAD)

    @Test
    fun flip() = Bot(TwistDemos.flip).rightTo(6.5f)
        .waitWhile(1f) { it.viewTurn() < 1f }
        // upside down: the right key walks left on the level, so the left key goes on to the door
        .leftKeyRightTo(11.4f).leftJump(0.35f).landLeft()
        .waitWhile(5f) { it.viewTurn() > 0f }
        .right(4f)
        .expect(WorldState.WON)

    @Test
    fun ghost() = Bot(TwistDemos.ghost).right(4f).also { it.expect(WorldState.DEAD) }
        .retry().rightTo(13.6f).rightJump(0.35f).right(4f)
        .expect(WorldState.WON)

    // ---------- fake win ----------

    @Test
    fun fakeWinUnlocksNothingAndCountsNothing() {
        val p = Prog()
        val g = sandbox(TwistDemos.credits, p)
        g.input.right = true
        while (g.world!!.fake == null && g.time < 5f) g.update(DT)
        g.input.right = false
        assertEquals(WorldState.PLAYING, g.world!!.state)
        g.run(Twists.FAKE_DELAY + 0.05f)
        assertTrue(g.fakeShown)
        assertEquals(Mood.SHOCK, g.mood)
        while (g.world!!.fake != null && g.time < 20f) g.update(DT)
        g.update(DT)
        assertFalse(g.fakeShown)
        assertEquals(Twists.nope.toString(), g.bubble)
        assertTrue(g.glitch > 0f)
        g.run(3f)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(WorldState.PLAYING, g.world!!.state)
        assertEquals(1, p.unlocked)
        assertTrue(p.best.isEmpty())
        assertEquals(0, g.deaths)
    }

    @Test
    fun theRealDoorStillWinsAfterAFake() {
        val p = Prog()
        val g = sandbox(TwistDemos.clear, p)
        val w = g.world!!
        g.input.right = true
        while (w.fake == null) g.update(DT)
        g.input.right = false
        while (w.fake != null) g.update(DT)
        g.run(1.5f)
        // put Bevel into the relocated door
        repeat(300) {
            if (w.state == WorldState.PLAYING) { w.player.box.x = w.door.box.cx - w.player.box.w / 2; w.player.box.y = w.door.box.b - w.player.box.h }
            g.update(DT)
        }
        assertEquals(Screen.CLEAR, g.screen)
        assertEquals(2, p.unlocked)
        assertEquals(0, p.best[0])
    }

    @Test
    fun doorIsLockedWhileBevelIsSpatOut() {
        val w = World(Level(T("", ""), T("", ""), traps = listOf(trap(AtDoor, FakeWin()))) { border(); floor(); put(2, 14, 'P'); put(5, 14, 'D') })
        val c = Controls().apply { right = true }
        while (w.fake == null) w.step(DT, c)
        c.right = false
        while (w.fake != null) w.step(DT, c)
        repeat((Twists.DOOR_LOCK * Twists.HZ).toInt() - 2) { w.step(DT, c); assertEquals(WorldState.PLAYING, w.state) }
    }

    // ---------- pause traps ----------

    private fun trickLevel(trick: PauseTrick) = Level(T("", ""), T("", ""), traps = listOf(trap(After(0f), PauseTrap(trick)))) {
        border(); floor(); put(2, 14, 'P'); put(29, 14, 'D')
    }

    @Test
    fun everyPauseTrickLeavesARealPause() {
        for (trick in PauseTrick.entries) {
            val g = sandbox(trickLevel(trick))
            g.run(0.2f)
            assertEquals(trick, g.world!!.pauseTrick)
            // the back button always pauses, and a tap beside the buttons always resumes
            assertTrue(g.back()); assertEquals("$trick", Screen.PAUSE, g.screen)
            g.tap(20f, 130f); assertEquals("$trick", Screen.PLAY, g.screen)
            // the app going to the background pauses too
            g.pause(); assertEquals("$trick", Screen.PAUSE, g.screen)
            g.tap(20f, 130f); assertEquals(Screen.PLAY, g.screen)
            assertEquals(WorldState.PLAYING, g.world!!.state)
            assertEquals(2, g.world!!.resumes)
        }
    }

    @Test
    fun dodgingPauseGivesUpAfterThreeTaps() {
        val g = sandbox(trickLevel(PauseTrick.DODGE))
        g.run(0.2f)
        repeat(Twists.DODGES) {
            g.tapOn(Ui.hudPause); assertEquals(Screen.PLAY, g.screen)
            // taps while it is away don't count
            g.tapOn(Ui.hudPause); assertEquals(Screen.PLAY, g.screen)
            g.run(Twists.DODGE_TIME + 0.05f)
        }
        assertEquals(Twists.DODGES, g.world!!.dodges)
        g.tapOn(Ui.hudPause); assertEquals(Screen.PAUSE, g.screen)
    }

    @Test
    fun spikyPauseKillsButBackStillPauses() {
        val g = sandbox(trickLevel(PauseTrick.SPIKE))
        g.run(0.2f)
        g.tapOn(Ui.hudPause)
        assertEquals(Screen.PLAY, g.screen)
        g.run(0.05f)
        assertEquals(WorldState.DEAD, g.world!!.state)
        assertEquals(1, g.deaths)
        g.run(1.2f)
        assertEquals(WorldState.PLAYING, g.world!!.state)
        // a new attempt starts honest until the trap fires again, and back works regardless
        g.back(); assertEquals(Screen.PAUSE, g.screen)
    }

    @Test
    fun swappedPauseButtonsFollowTheirLabels() {
        val g = sandbox(trickLevel(PauseTrick.SWAP))
        g.run(0.2f)
        g.tapOn(Ui.hudPause); assertEquals(Screen.PAUSE, g.screen)
        assertTrue(g.pauseSwapped)
        // RESUME now sits where LEVELS was
        g.tapOn(Ui.pauseLevels); assertEquals(Screen.PLAY, g.screen)
        g.tapOn(Ui.hudPause); g.tapOn(Ui.pauseResume); assertEquals(Screen.SELECT, g.screen)
    }

    @Test
    fun restartFromPauseIsAnInstantCountedRetry() {
        val g = sandbox(Level(T("", ""), T("", ""), traps = listOf(trap(After(0.3f), Hide('w')))) {
            border(); floor(); put(2, 14, 'P'); put(29, 14, 'D'); put(10, 13, 'w')
        })
        g.input.right = true
        g.run(0.6f)
        g.input.right = false
        val old = g.world!!
        assertFalse(old.group('w').visible)
        assertTrue(old.player.box.cx > 4f)
        g.tapOn(Ui.hudPause); assertEquals(Screen.PAUSE, g.screen)
        g.tapOn(Ui.pauseRestart)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(1, g.deaths)
        assertNotSame(old, g.world)
        assertEquals(WorldState.PLAYING, g.world!!.state)
        assertTrue(g.world!!.group('w').visible)
        assertTrue(g.world!!.player.box.cx < 3f)
        assertEquals(0, g.world!!.resumes)
        // no death animation delay: Bevel moves right away
        g.input.right = true; g.run(0.2f)
        assertTrue(g.world!!.player.box.cx > 2.5f)
        g.input.right = false
        g.tapOn(Ui.hudPause); g.tapOn(Ui.pauseRestart)
        assertEquals(2, g.deaths)
    }

    @Test
    fun restartFromEveryPauseTrapPauseJustRestarts() {
        for (trick in PauseTrick.entries) {
            val lvl = Level(T("", ""), T("", ""), traps = listOf(trap(After(0f), PauseTrap(trick)), trap(Resumed(), Hide('w')))) {
                border(); floor(); put(2, 14, 'P'); put(29, 14, 'D'); put(10, 13, 'w')
            }
            val g = sandbox(lvl)
            g.run(0.2f)
            g.back(); assertEquals("$trick", Screen.PAUSE, g.screen)
            g.tapOn(Ui.pauseRestart)
            assertEquals("$trick", Screen.PLAY, g.screen)
            assertEquals("$trick", 1, g.deaths)
            assertEquals("$trick", 0, g.world!!.resumes)
            g.run(0.2f)
            assertTrue("$trick", g.world!!.group('w').visible)
            assertEquals("$trick", trick, g.world!!.pauseTrick)
        }
    }

    @Test
    fun resumingFiresResumedTraps() {
        val g = sandbox(TwistDemos.pause)
        g.run(0.5f)
        assertTrue(g.world!!.group('w').visible)
        g.back(); g.run(0.3f); g.tap(20f, 130f); g.run(0.05f)
        assertFalse(g.world!!.group('w').visible)
        assertFalse(g.world!!.group('a').visible)
    }

    // ---------- camera ----------

    @Test
    fun flipTurnsBackAndControlsFollowTheScreen() {
        val w = World(TwistDemos.flip)
        val c = Controls().apply { right = true }
        while (w.player.box.cx < 6.2f) w.step(DT, c)
        c.right = false
        repeat((Twists.TURN * Twists.HZ).toInt() + 2) { w.step(DT, c) }
        assertEquals(1f, w.viewTurn(), 0f)
        // pressing right walks left on the level: right on the upside-down screen
        val x = w.player.box.cx
        c.right = true
        repeat(30) { w.step(DT, c) }
        assertTrue(w.player.box.cx < x)
        c.right = false
        repeat(((3f + Twists.TURN) * Twists.HZ).toInt()) { w.step(DT, c) }
        assertEquals(0f, w.viewTurn(), 0f)
        val y = w.player.box.cx
        c.right = true
        repeat(30) { w.step(DT, c) }
        assertTrue(w.player.box.cx > y)
    }

    @Test
    fun rollRestores() {
        val w = World(TwistDemos.flip)
        val c = Controls()
        repeat(10) { w.step(DT, c) }
        assertEquals(0f, w.viewRoll(), 0f)
        val lvl = Level(T("", ""), T("", ""), traps = listOf(trap(After(0f), Roll(1f, 2)))) { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }
        val r = World(lvl)
        var moved = false
        repeat(Twists.HZ) { r.step(DT, c); if (r.viewRoll() > 0.1f) moved = true }
        assertTrue(moved)
        repeat(3) { r.step(DT, c) }
        assertEquals(0f, r.viewRoll(), 0f)
    }

    @Test
    fun crackedFrameIsPlainFrameUntilItBreaks() {
        val w = World(TwistDemos.crack)
        val g = w.groups.values.single()
        assertEquals(4, g.pieces.size)
        assertTrue(g.pieces.all { it.hy == 0f && it.hx in 16f..19f && !it.spike })
        assertTrue(g.visible)
    }

    // ---------- ghost ----------

    @Test
    fun ghostReplaysTheLastAttemptExactly() {
        val first = Bot(TwistDemos.ghost).right(0.6f).left(0.3f).rightJump(0.3f).right(4f)
        first.expect(WorldState.DEAD)
        val trail = first.world.trail
        assertTrue(trail.size > 100)
        val w = World(TwistDemos.ghost, trail)
        val c = Controls().apply { right = true }
        var seen = 0
        while (w.state == WorldState.PLAYING && w.time < 6f) {
            w.step(DT, c)
            if (w.player.box.cx > 12f) c.jumpPressed = true.also { c.jump = true }
            val i = w.ghostFrame
            if (i < 0) continue
            assertEquals(trail.x(i), w.ghost!!.x, 0f)
            assertEquals(trail.y(i), w.ghost!!.y, 0f)
            seen++
        }
        // the ghost starts one second in
        assertTrue(seen > 50)
        // and the first frame it showed was the old spawn
        val again = World(TwistDemos.ghost, trail)
        while (again.ghost == null) again.step(DT, Controls())
        assertEquals(Twists.HZ + 1, again.ticks)
        assertEquals(trail.x(0), again.ghost!!.x, 0f)
    }

    @Test
    fun ghostKillsOnContact() {
        val b = Bot(TwistDemos.ghost).wait(3f).retry()
        // the last attempt idled at the spawn: its ghost appears right on top of Bevel
        b.wait(0.9f).expect(WorldState.PLAYING)
        b.wait(0.3f).expect(WorldState.DEAD)
    }

    @Test
    fun noGhostOnTheFirstAttemptOrWithoutTheAction() {
        assertNull(Bot(TwistDemos.ghost).wait(2f).world.ghost)
        val w = World(TwistDemos.crack)
        repeat(100) { w.step(DT, Controls()) }
        assertEquals(0, w.trail.size)
    }

    @Test
    fun gameHandsTheTrailToTheNextAttempt() {
        val g = sandbox(TwistDemos.ghost)
        g.run(1.5f)
        val old = g.world!!.trail
        g.input.right = true
        while (g.world!!.state != WorldState.DEAD) g.update(DT)
        g.input.right = false
        val dead = g.world
        while (g.world === dead) g.update(DT)
        g.run(1.05f)
        assertNotNull(g.world!!.ghost)
        assertTrue(old.size > 0)
    }

    private companion object {
        const val DT = Bot.DT
    }
}
