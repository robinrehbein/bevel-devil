package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.PastX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mephi bluffs: a card without its trap, a tell while it flies, then it turns over to BLUFF. */
class BluffTest {
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
        val deaths = HashMap<Card, Int>()
        override fun bestDeaths(level: Int): Int? = null
        override fun saveBest(level: Int, deaths: Int) {}
        override fun cardFound(card: Card) = card in found
        override fun findCard(card: Card) { found += card }
        override fun cardDeaths(card: Card) = deaths[card] ?: 0
        override fun addCardDeath(card: Card) { deaths[card] = cardDeaths(card) + 1 }
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private fun Game.run(seconds: Float) { var t = 0f; while (t < seconds) { update(Bot.DT); t += Bot.DT } }

    /** "Collapse!" at x 6, but the floor holds; a jump on reflex lands in spikes at x 9..11. */
    private val demo = Level(
        name = T("Bluff Demo", "Bluff-Demo"),
        intro = T("x", "x"),
        legend = mapOf('B' to Glyph(spike = true, hidden = true)),
        traps = listOf(
            trap(PastX(6f), Bluff(Card.COLLAPSE)),
            trap(Airborne(6.5f, 12f), Show('B')),
        ),
    ) {
        border(); floor()
        fill(8..9, 15..17, 'a')
        for (x in 9..11) put(x, 14, 'B')
        put(2, 14, 'P'); put(28, 14, 'D')
    }

    private fun game(p: Prog) = Game(p, silent).apply { sandbox = demo; startLevel(0) }

    @Test
    fun walkingCalmlyThroughTheBluffWins() {
        // the hidden spikes only show under a jump; walking never jumps
        Bot(demo).right(4f).expect(WorldState.WON)
    }

    @Test
    fun survivingTheBluffFlipsTheCardAndMephiSulks() {
        val p = Prog()
        val g = game(p)
        g.input.right = true
        while (g.card == null) g.update(Bot.DT)
        g.input.right = false
        assertTrue("bluff flag", g.cardBluff)
        assertTrue("Mephi has a tell while the bluff flies", g.bluffTell)
        assertFalse("the bluffed card is not found", Card.COLLAPSE in p.found)
        g.run(Game.BLUFF_FLIP + 0.05f)
        assertFalse("tell over", g.bluffTell)
        assertTrue("found", Card.BLUFF in p.found)
        assertEquals(Mood.SULK, g.mood)
    }

    @Test
    fun fallingForItRevealsAtOnceAndCountsOnTheBluffCard() {
        val p = Prog()
        val g = game(p)
        g.input.right = true
        while (g.card == null) g.update(Bot.DT)
        // the reflex: jump
        g.input.jump = true; g.input.jumpPressed = true
        var t = 0f
        while (g.world!!.state == WorldState.PLAYING && t < 2f) { g.update(Bot.DT); t += Bot.DT }
        assertEquals(WorldState.DEAD, g.world!!.state)
        assertTrue(Card.BLUFF in p.found)
        assertEquals(1, p.cardDeaths(Card.BLUFF))
        assertEquals(0, p.cardDeaths(Card.COLLAPSE))
        assertFalse(g.bluffTell)
    }

    @Test
    fun theCardStatSaysAfterNotBy() {
        // the count goes to the card played last (a bluff that did nothing included), so it must not claim the card did it
        assertTrue(Txt.caught.en.contains("%d") && Txt.caught.de.contains("%d"))
        assertTrue(Txt.caught.en.contains("after") && Txt.caught.de.contains("danach"))
        assertFalse(Txt.caught.en.contains("crashed you"))
    }
}
