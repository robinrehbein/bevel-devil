package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.CardSlot.Slot
import com.robinrehbein.beveldevil.game.Trigger.PastX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** A rolling picture (2-40) keeps the trap card out of the middle, and a death there settles the picture at once. */
class RollCardTest {
    private class Prog : Progress {
        override var unlocked = 1
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        override var tiltSensor = true
        val found = HashSet<Card>()
        override fun bestDeaths(level: Int): Int? = null
        override fun saveBest(level: Int, deaths: Int) {}
        override fun cardFound(card: Card) = card in found
        override fun findCard(card: Card) { found += card }
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
    }

    /** STALKER at x 4 with [traps]; a spike on the floor at x 12 to die on. */
    private fun room(vararg traps: Trap) = Level(T("Hold", "Bildlauf"), T("", ""), traps = traps.toList()) {
        border(); floor()
        put(12, 14, '^')
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    private fun game(l: Level) = Game(Prog(), object : Audio { override fun play(sound: Sound) {} }).apply { sandbox = l; startLevel(0) }

    @Test
    fun aCardPlayedWithARollKeepsToACornerBehindThePlayer() {
        val g = game(room(trap(PastX(4f), Play(Card.STALKER), Roll(3f, 2))))
        g.input.right = true
        while (g.card == null) g.update(Bot.DT)
        assertTrue(g.world!!.rolling)
        // running right: the card stays behind, on the left, up high
        assertEquals(Slot(-1, CardSlot.HIGH), Slot(g.cardSide, g.cardLift))
    }

    @Test
    fun aRollStartingUnderACardInTheAirMovesItOutOfTheMiddle() {
        val g = game(room(trap(PastX(4f), Play(Card.STALKER)), trap(PastX(6f), Roll(3f, 2))))
        g.input.right = true
        while (g.card == null) g.update(Bot.DT)
        assertEquals("no roll yet: the center", Slot(0, 0f), Slot(g.cardSide, g.cardLift))
        while (!g.world!!.rolling) g.update(Bot.DT)
        g.update(Bot.DT)
        assertTrue("the card went to a corner: ${g.cardSide}, ${g.cardLift}", Slot(g.cardSide, g.cardLift) in corners)
    }

    /** The two corners a card keeps to during a roll. */
    private val corners = setOf(Slot(-1, CardSlot.HIGH), Slot(1, CardSlot.LOW))

    /** Mephi's speech bubble in the overlay HUD (right edge 208, up to 118 wide, two lines from y 18), in tiles. */
    private val bubble = Area(90f / 8, 18f / 8, 208f / 8, 38f / 8)

    @Test
    fun duringARollTheCardNeverCoversMephisLine() {
        for (heading in -1..1) for (x in 2..30) {
            for (bottom in listOf(15f, 9f, 4f)) {
                val p = Area(x - Physics.PLAYER_W / 2, bottom - Physics.PLAYER_H, x + Physics.PLAYER_W / 2, bottom)
                val s = CardSlot.choose(p, edge = true, heading = heading)
                assertTrue("$s", s in corners)
                assertEquals("$s at x $x", 0f, CardSlot.area(s).overlap(bubble))
            }
        }
        // running left on the floor: the corner behind them, low on the right
        val runner = Area(8f - Physics.PLAYER_W / 2, 15f - Physics.PLAYER_H, 8f + Physics.PLAYER_W / 2, 15f)
        assertEquals(Slot(1, CardSlot.LOW), CardSlot.choose(runner, edge = true, heading = -1))
    }

    @Test
    fun aDeathDuringARollSettlesThePictureAndTheRetryStartsStill() {
        val g = game(room(trap(PastX(4f), Play(Card.STALKER), Roll(3f, 2))))
        g.input.right = true
        while (g.world!!.state == WorldState.PLAYING) g.update(Bot.DT)
        g.input.right = false
        val w = g.world!!
        assertTrue("still rolling at the death", w.rolling)
        var t = 0f
        while (t < Twists.ROLL_SETTLE + Bot.DT) { g.update(Bot.DT); t += Bot.DT }
        assertTrue(g.world === w && w.state == WorldState.DEAD)
        assertEquals("the picture caught its hold before the respawn", 0f, w.viewRoll(), 0f)
        while (g.world === w) g.update(Bot.DT)
        assertEquals(0f, g.world!!.viewRoll(), 0f)
        assertEquals(0f, g.glitch, 0f)
        assertEquals(null, g.card)
    }
}
