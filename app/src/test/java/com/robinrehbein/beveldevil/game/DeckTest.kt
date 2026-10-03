package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.PastX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** V2 "Mephi shuffles": rematch rounds in the same room ([Round]) and traps dealt by attempt ([Deal]). */
class DeckTest {
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
    private fun Game.run(seconds: Float) { var t = 0f; while (t < seconds) { update(Bot.DT); t += Bot.DT } }
    /** Holds right for up to [seconds], letting go as soon as the player dies. */
    private fun Game.hold(seconds: Float, right: Boolean = false) {
        input.right = right
        var t = 0f
        while (t < seconds && world?.state == WorldState.PLAYING) { update(Bot.DT); t += Bot.DT }
        input.right = false
    }
    private fun sandbox(level: Level, p: Progress = Prog()) = Game(p, silent).apply { sandbox = level; startLevel(0) }

    /** A pit that opens on odd attempts at x=9 and on even ones at x=20; a second round where the first pit stays shut. */
    private val demo = Level(
        name = T("Deck Demo", "Deck-Demo"),
        intro = T("Pick a card.", "Zieh eine Karte."),
        traps = listOf(
            trap(PastX(8f), Play(Card.COLLAPSE), Fall('a'), deal = Deal.odd),
            trap(PastX(19f), Play(Card.COLLAPSE), Fall('b'), deal = Deal.even),
        ),
        rematch = listOf(
            Round(
                T("Again. Same room.", "Nochmal. Gleicher Raum."),
                traps = listOf(trap(PastX(14f), Play(Card.SPIKE_SEED), Show('C'))),
                legend = mapOf('C' to Glyph(spike = true, hidden = true)),
            ) { put(16, 14, 'C') },
        ),
    ) {
        border(); floor()
        fill(9..11, 15..17, 'a')
        fill(20..22, 15..17, 'b')
        put(2, 14, 'P'); put(28, 14, 'D')
    }

    @Test
    fun dealCycles() {
        assertTrue(Deal.ALWAYS.dealt(1) && Deal.ALWAYS.dealt(7))
        assertEquals(listOf(true, false, true, false), (1..4).map { Deal.odd.dealt(it) })
        assertEquals(listOf(false, true, false, true), (1..4).map { Deal.even.dealt(it) })
        assertEquals(listOf(false, false, true, true), (1..4).map { Deal.from(3).dealt(it) })
        assertEquals(listOf(false, true, false, false, true), (1..5).map { Deal.cycle(3, 1).dealt(it) })
    }

    @Test
    fun roundsShareTheRoomButNotTheTraps() {
        assertEquals(2, demo.rounds.size)
        val r2 = demo.rounds[1]
        assertEquals('C', r2.map.grid[14][16])
        assertEquals('a', r2.map.grid[15][9])
        assertEquals(1, r2.traps.size)
        assertTrue(r2.rematch.isEmpty())
    }

    @Test
    fun theSameRunDiesOnDifferentPitsPerAttempt() {
        // attempt 1: the first pit opens under the player
        Bot(demo, attempt = 1).right(3f).expect(WorldState.DEAD)
        // attempt 2: the first pit holds, the player dies at the second one
        val b = Bot(demo, attempt = 2).right(1.2f)
        assertTrue(b.world.player.box.cx > 12f)
        b.right(3f).expect(WorldState.DEAD)
        assertTrue(b.world.player.box.cx > 19f)
        // each attempt is solvable once you know it
        Bot(demo, attempt = 1).hopR(7.6f).right(4f).expect(WorldState.WON)
        Bot(demo, attempt = 2).right(1.5f).hopR(18.6f).right(3f).expect(WorldState.WON)
    }

    @Test
    fun gameAdvancesAttemptsAndRounds() {
        val g = sandbox(demo)
        assertEquals(0, g.round); assertEquals(1, g.attempt); assertEquals("1/2", g.roundTag)
        g.hold(3f, right = true)
        assertEquals(1, g.deaths)
        g.run(1.2f)
        assertEquals(2, g.attempt)
        assertEquals(2, g.world!!.attempt)

        // play attempt 2 through the door: the rematch starts instead of the clear screen
        g.hold(1.5f, right = true)
        g.input.right = true
        while (g.world!!.player.box.cx < 18.2f) g.update(Bot.DT)
        g.input.jump = true; g.input.jumpPressed = true
        g.run(0.35f)
        g.input.jump = false
        var guard = 0f
        while (g.world!!.state == WorldState.PLAYING && guard < 3f) { g.update(Bot.DT); guard += Bot.DT }
        g.input.right = false
        assertEquals("x=${g.world!!.player.box.cx} y=${g.world!!.player.box.b}", WorldState.WON, g.world!!.state)
        g.run(1f)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(1, g.round)
        assertEquals(1, g.attempt)
        assertEquals("2/2", g.roundTag)
        assertTrue(g.rematchAge < 0.5f)
        assertEquals(1, g.deaths)

        // dying in round 2 restarts round 2, not the level
        g.hold(3f, right = true)
        assertEquals(2, g.deaths)
        g.run(1.2f)
        assertEquals(1, g.round)
        assertEquals(2, g.attempt)
    }

    @Test
    fun aLevelWithoutRematchHasNoRoundTag() {
        val g = sandbox(Level(T("x", "x"), T("x", "x")) { border(); floor(); put(2, 14, 'P'); put(28, 14, 'D') })
        assertEquals(null, g.roundTag)
        assertNotEquals(Screen.CLEAR, g.screen)
        assertFalse(g.rematchAge < 1f)
    }

    @Test
    fun aStuckPlayerGetsTheHintOnce() {
        val l = Level(T("x", "x"), T("Hi.", "Hi."), hint = T("Try the ceiling.", "Probier die Decke.")) {
            border(); floor(); fill(10..10, 5..14); put(2, 14, 'P'); put(28, 14, 'D')
        }
        val g = sandbox(l)
        g.run(Game.HINT_AFTER - 0.5f)
        assertNotEquals(l.hint.toString(), g.bubble)
        g.run(1f)
        assertEquals(l.hint.toString(), g.bubble)
    }

    @Test
    fun theClearScreenTakesATapAnywhereAfterABeat() {
        val l = Level(T("x", "x"), T("x", "x")) { border(); floor(); put(2, 14, 'P'); put(5, 14, 'D') }
        val g = sandbox(l)
        g.hold(2f, right = true)
        g.run(1.2f)
        assertEquals(Screen.CLEAR, g.screen)
        g.tap(4f, 4f)
        assertEquals(Screen.CLEAR, g.screen)
        g.run(Game.CLEAR_ANYWHERE + 0.1f)
        g.tap(4f, 4f)
        assertNotEquals(Screen.CLEAR, g.screen)
    }
}
