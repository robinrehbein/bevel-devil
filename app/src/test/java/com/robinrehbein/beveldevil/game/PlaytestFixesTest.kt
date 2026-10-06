package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.CardSlot.Slot
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Zone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fixes from the playtest of the internal build: belts at their ends, the card dodging the trap, crumbling floors. */
class PlaytestFixesTest {
    // ---------- belts carry from either side ----------

    /** A belt on tiles 17..20 with a spike on the floor tile left of it (2-32's lane), running at [speed]. */
    private fun beltRoom(speed: Float, spikeAt: Int, spawn: Int = 18) = Level(T("Belt", "Band"), T("", ""), start = listOf(Belt('b', speed))) {
        border(); floor()
        fill(17..20, 15..15, 'b')
        put(spikeAt, 14, '^')
        put(spawn, 14, 'P'); put(29, 14, 'D')
    }

    @Test
    fun aBeltRunningLeftCarriesIntoTheSpikeAtItsEnd() {
        // 2-32: it used to drop the player as soon as their edge touched the floor tile, 0.13 tiles short of the spike
        Bot(beltRoom(-9f, spikeAt = 16)).wait(3f).expect(WorldState.DEAD)
    }

    @Test
    fun aBeltRunningRightCarriesIntoTheSpikeAtItsEnd() {
        Bot(beltRoom(9f, spikeAt = 21)).wait(3f).expect(WorldState.DEAD)
    }

    @Test
    fun aBeltLetsGoOnceThePlayerIsOffIt() {
        // the spike one tile further on is out of reach on both sides: the belt hands the player over and stops carrying
        val left = Bot(beltRoom(-9f, spikeAt = 15)).wait(3f)
        left.expect(WorldState.PLAYING)
        assertTrue(left.world.player.box.r <= 17.01f)
        val right = Bot(beltRoom(9f, spikeAt = 22)).wait(3f)
        right.expect(WorldState.PLAYING)
        assertTrue(right.world.player.box.x >= 20.99f)
    }

    // ---------- where the card settles ----------

    private fun standing(cx: Float, bottom: Float) = Area(cx - Physics.PLAYER_W / 2, bottom - Physics.PLAYER_H, cx + Physics.PLAYER_W / 2, bottom)

    @Test
    fun withoutATrapTheCardDodgesOnlyThePlayer() {
        assertEquals(Slot(0, 0f), CardSlot.choose(standing(4f, 15f)))
        // on the floor right under the card: it keeps its slot, as it always did
        assertEquals(Slot(0, 0f), CardSlot.choose(standing(16f, 15f)))
        assertEquals(Slot(-1, 0f), CardSlot.choose(standing(18f, 9f)))
        assertEquals(Slot(1, 0f), CardSlot.choose(standing(14f, 9f)))
    }

    @Test
    fun theCardKeepsOffTheTrap() {
        // a hole opening in the top floor under the center (2-22's carpet), the player far off to the left: right
        val carpet = Area(16f, 7f, 18f, 8f)
        assertEquals(Slot(1, 0f), CardSlot.choose(standing(4f, 7f), listOf(carpet)))
        // the player on the right as well: left
        assertEquals(Slot(-1, 0f), CardSlot.choose(standing(27f, 7f), listOf(carpet)))
        // nothing in the way: the center, as before
        assertEquals(Slot(0, 0f), CardSlot.choose(standing(4f, 15f), listOf(Area(28f, 14f, 30f, 15f))))
    }

    @Test
    fun aTrapAcrossTheWholeRoomSendsTheCardUpOrDown() {
        // a beam right across the usual height: above it, if that is free
        val beam = Area(1f, 7.4f, 31f, 7.6f)
        assertEquals(Slot(0, CardSlot.HIGH), CardSlot.choose(standing(4f, 15f), listOf(beam)))
        // and the room above taken as well: below
        val top = Area(1f, 1f, 31f, 4f)
        assertEquals(Slot(0, CardSlot.LOW), CardSlot.choose(standing(4f, 9f), listOf(beam, top)))
        // no free slot at all: the one that covers least
        val all = Area(1f, 0f, 31f, 18f)
        val s = CardSlot.choose(standing(4f, 15f), listOf(all))
        assertTrue(CardSlot.cost(s, standing(4f, 15f), listOf(all)) <= CardSlot.cost(Slot(0, 0f), standing(4f, 15f), listOf(all)))
    }

    @Test
    fun theCardNeverSettlesOverThePlayer() {
        val carpet = Area(16f, 7f, 18f, 8f)
        for (x in listOf(3f, 9f, 13.5f, 16f, 19f, 24f, 29f)) for (y in listOf(7f, 10f, 15f)) {
            val p = standing(x, y)
            val s = CardSlot.choose(p, listOf(carpet))
            assertTrue("player at $x,$y under the card in $s", !CardSlot.hits(p, s))
        }
    }

    /** 2-22 in small: a card on the way in (a saw from the right), then a floor that drops out ahead. */
    private val bouncer = Level(T("Bouncer", "Türsteher"), T("", ""),
        traps = listOf(
            trap(PastX(8.5f), Play(Card.DEVIL_SAW), Saw(33.5f, 6.4f, -4.5f, 0f)),
            trap(Zone(12.5f, 3f, 14.5f, 7.4f), Hide('c')),
        ),
    ) {
        border(); floor()
        fill(1..28, 7..7); fill(16..17, 7..7, 'c')
        put(2, 6, 'P'); put(2, 14, 'D')
    }

    @Test
    fun theTrapsTellTheCardWhatTheyActOn() {
        val bot = Bot(bouncer)
        val seen = ArrayList<Event>()
        bot.rightUntil { w -> seen += w.events; w.events.clear(); w.player.box.cx > 13f }
        val played = seen.filterIsInstance<Event.Played>().single()
        assertNotNull(played.area)
        val saw = played.area!!
        assertTrue("the saw's way over the next seconds: $saw", saw.x0 < 25f && saw.x1 > 33f && saw.y0 < 6.4f && saw.y1 > 6.4f)
        val hole = seen.filterIsInstance<Event.Target>().single().area
        assertEquals(Area(16f, 7f, 18f, 8f), hole)
        // and the hole crumbles: one chunk of rubble per tile that went
        assertEquals(listOf(16f to 7f, 17f to 7f), seen.filterIsInstance<Event.Crumble>().single().tiles)
    }

    private class Prog : Progress {
        override var unlocked = 1
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        override var tiltSensor = true
        override fun bestDeaths(level: Int): Int? = null
        override fun saveBest(level: Int, deaths: Int) {}
        override fun cardFound(card: Card) = false
        override fun findCard(card: Card) {}
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
        override fun checkpoint(level: Int) = 0 to 0
        override fun saveCheckpoint(level: Int, round: Int, deaths: Int) {}
    }

    @Test
    fun aCardInTheAirMovesOffAFloorThatOpensUnderIt() {
        val g = Game(Prog(), object : Audio { override fun play(sound: Sound) {} }).apply { sandbox = bouncer; startLevel(0) }
        g.input.right = true
        while (g.card == null) g.update(Bot.DT)
        assertEquals("played far from anything: the center", Slot(0, 0f), Slot(g.cardSide, g.cardLift))
        assertEquals(0f to 0f, g.cardSlot())
        while (g.world!!.player.box.cx < 13f) g.update(Bot.DT)
        g.input.right = false
        g.update(Bot.DT)
        val s = Slot(g.cardSide, g.cardLift)
        assertTrue("the card left the center for the hole: $s", s != Slot(0, 0f))
        assertEquals(0f, CardSlot.area(s).overlap(Area(16f, 7f, 18f, 8f)))
        // it glides there
        repeat((Game.CARD_GLIDE / Bot.DT).toInt() + 2) { g.update(Bot.DT) }
        assertEquals(s.side.toFloat() to s.lift, g.cardSlot())
        // the rubble is in the air
        assertTrue(g.particles.any { it.y > 7f && !it.dust })
    }

    @Test
    fun hidingAnInvisibleOrSpikeGroupThrowsNoRubble() {
        val l = Level(T("Hide", "Weg"), T("", ""), legend = mapOf('h' to Glyph(spike = false, hidden = true)),
            traps = listOf(trap(PastX(3f), Hide('h'), Hide('S')))) {
            border(); floor(); fill(10..11, 12..12, 'h'); put(14, 14, 'S'); put(2, 14, 'P'); put(28, 14, 'D')
        }
        val bot = Bot(l)
        val seen = ArrayList<Event>()
        bot.rightUntil { w -> seen += w.events; w.events.clear(); w.player.box.cx > 4f }
        assertTrue(seen.none { it is Event.Crumble })
    }
}
