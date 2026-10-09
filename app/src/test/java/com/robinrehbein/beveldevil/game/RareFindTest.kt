package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Trigger.PastX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The first rare or legendary card in the album gets a line of its own from Mephi; common ones and repeats don't. */
class RareFindTest {
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

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private val trapLine = T("Trap line.", "Fallenzeile.")

    /** [card] plays at x 4 (with the level's own line when [line]); spikes on the floor at x 9 to die on. */
    private fun room(card: Card, line: Boolean = false) = Level(T("Find", "Fund"), T("", ""),
        traps = listOf(if (line) trap(PastX(4f), Play(card), Action.Say(trapLine)) else trap(PastX(4f), Play(card)))) {
        border(); floor()
        put(9, 14, '^')
        put(2, 14, 'P'); put(29, 14, 'D')
    }

    private fun bragsOf(c: Card) = (if (c.rarity == Rarity.LEGENDARY) Game.LEGENDARY_LINES else Game.RARE_LINES)
        .map { it.toString().replace("%s", c.title.toString()) }.toSet()

    /** Walks right until [card] is played, then [seconds] more standing still. */
    private fun play(p: Prog, card: Card, line: Boolean = false, seconds: Float = 0f): Game {
        val g = Game(p, silent).apply { sandbox = room(card, line); startLevel(0) }
        g.input.right = true
        while (g.card == null) g.update(Bot.DT)
        g.input.right = false
        var t = 0f
        while (t < seconds) { g.update(Bot.DT); t += Bot.DT }
        return g
    }

    @Test
    fun theFirstRareFindGetsABragAndAnExtraLaugh() {
        for (c in listOf(Card.TWISTED, Card.GRAND_FINALE)) {
            val p = Prog()
            val g = play(p, c)
            assertTrue("$c: ${g.bubble}", g.bubble in bragsOf(c))
            assertEquals(Mood.LAUGH, g.mood)
            assertTrue(c in p.found)
            // the second time it is just a card
            val again = play(p, c)
            assertFalse(again.bubble in bragsOf(c))
        }
    }

    @Test
    fun everyBragFitsLikeAQuip() {
        for (c in Card.entries.filter { it.rarity != Rarity.COMMON }) for (t in Game.RARE_LINES + Game.LEGENDARY_LINES) {
            for ((line, title) in listOf(t.en to c.title.en, t.de to c.title.de)) {
                val s = line.replace("%s", title)
                assertTrue(s, s.length <= DevilQuips.MAX_LEN)
            }
        }
    }

    @Test
    fun aCommonCardGetsNoBrag() {
        val g = play(Prog(), Card.STALKER)
        val all = Card.entries.flatMap { bragsOf(it) }.toSet()
        assertFalse(g.bubble in all)
    }

    @Test
    fun theLevelsOwnLineGoesFirstThenTheBrag() {
        val g = play(Prog(), Card.DECOY, line = true)
        assertEquals(trapLine.toString(), g.bubble)
        var t = 0f
        while (g.bubble == trapLine.toString() && t < 6f) { g.update(Bot.DT); t += Bot.DT }
        assertTrue(g.bubble in bragsOf(Card.DECOY))
    }

    @Test
    fun dyingRightAwayGetsTheBragInsteadOfATaunt() {
        val p = Prog()
        val g = Game(p, silent).apply { sandbox = room(Card.SHY_DOOR, line = true); startLevel(0) }
        g.input.right = true
        while (g.world!!.state == WorldState.PLAYING) g.update(Bot.DT)
        assertTrue(g.bubble in bragsOf(Card.SHY_DOOR))
        // once only: the next death is a plain taunt again
        while (g.world!!.state != WorldState.PLAYING) g.update(Bot.DT)
        while (g.world!!.state == WorldState.PLAYING) g.update(Bot.DT)
        assertFalse(g.bubble in bragsOf(Card.SHY_DOOR))
    }
}
