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

/** V2 rematch rounds: after the door, the same room with a new hand of traps ([Round]). */
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
        val checkpoints = HashMap<Int, Pair<Int, Int>>()
        override fun checkpoint(level: Int) = checkpoints[level] ?: (0 to 0)
        override fun saveCheckpoint(level: Int, round: Int, deaths: Int) { if (round <= 0) checkpoints.remove(level) else checkpoints[level] = round to deaths }
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

    /** A pit that opens at x=9; a second round in the same room where the pit stays shut and spikes sprout instead. */
    private val demo = Level(
        name = T("Deck Demo", "Deck-Demo"),
        intro = T("Pick a card.", "Zieh eine Karte."),
        traps = listOf(
            trap(PastX(8f), Play(Card.COLLAPSE), Fall('a')),
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
    fun roundsShareTheRoomButNotTheTraps() {
        assertEquals(2, demo.rounds.size)
        val r2 = demo.rounds[1]
        assertEquals('C', r2.map.grid[14][16])
        assertEquals('a', r2.map.grid[15][9])
        assertEquals(1, r2.traps.size)
        assertTrue(r2.rematch.isEmpty())
    }

    @Test
    fun everyAttemptOfARoundIsTheSame() {
        // the room never changes between attempts: same run, same death
        repeat(3) { Bot(demo).right(3f).expect(WorldState.DEAD) }
        Bot(demo).hopR(7.6f).right(4f).expect(WorldState.WON)
        // round 2: the pit holds, the spikes at x=16 bite
        Bot(demo, round = 1).right(3f).expect(WorldState.DEAD)
        Bot(demo, round = 1).hopR(14.6f).right(4f).expect(WorldState.WON)
    }

    @Test
    fun gameAdvancesThroughTheRounds() {
        val g = sandbox(demo)
        assertEquals(0, g.round); assertEquals(null, g.roundTag)
        g.hold(3f, right = true)
        assertEquals(1, g.deaths)
        g.run(1.2f)
        assertEquals(0, g.round)

        // jump the pit and through the door: the rematch starts instead of the clear screen
        g.input.right = true
        while (g.world!!.player.box.cx < 7.6f) g.update(Bot.DT)
        g.input.jump = true; g.input.jumpPressed = true
        g.run(0.35f)
        g.input.jump = false
        var guard = 0f
        while (g.world!!.state == WorldState.PLAYING && guard < 4f) { g.update(Bot.DT); guard += Bot.DT }
        g.input.right = false
        assertEquals("x=${g.world!!.player.box.cx} y=${g.world!!.player.box.b}", WorldState.WON, g.world!!.state)
        g.run(1f)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(1, g.round)
        assertEquals("#2", g.roundTag)
        assertTrue(g.rematchAge < 0.5f)
        assertEquals(1, g.deaths)

        // dying in round 2 restarts round 2, not the level
        g.hold(3f, right = true)
        assertEquals(2, g.deaths)
        g.run(1.2f)
        assertEquals(1, g.round)
    }

    @Test
    fun aLevelWithoutRematchHasNoRoundTag() {
        val g = sandbox(Level(T("x", "x"), T("x", "x")) { border(); floor(); put(2, 14, 'P'); put(28, 14, 'D') })
        assertEquals(null, g.roundTag)
        assertNotEquals(Screen.CLEAR, g.screen)
        assertFalse(g.rematchAge < 1f)
    }

    /** Spikes three tiles right of the spawn (holding right dies), the door four tiles to the left. With a hint and a rematch. */
    private val spiky = Level(T("x", "x"), T("Hi.", "Hi."), hint = T("Try the ceiling.", "Probier die Decke."),
        rematch = listOf(Round(T("Again.", "Nochmal.")))) {
        border(); floor(); put(9, 14, '^'); put(6, 14, 'P'); put(2, 14, 'D')
    }

    /** Dies by holding right into the spikes, then waits out the respawn. */
    private fun Game.dieOnce() {
        hold(3f, right = true)
        run(Game.RESPAWN + 0.1f)
    }

    @Test
    fun theHintComesOnTheRespawnAfterTheSecondDeathAndOnlyOnce() {
        val g = sandbox(spiky)
        // alive and stuck for a long time: no hint any more (it used to come after 9 s)
        g.run(12f)
        assertNotEquals(spiky.hint.toString(), g.bubble)
        g.dieOnce()
        assertEquals(1, g.roundDeaths)
        assertNotEquals(spiky.hint.toString(), g.bubble)
        g.dieOnce()
        assertEquals(2, g.roundDeaths)
        assertEquals(spiky.hint.toString(), g.bubble)
        // once per round: the third respawn brings a taunt or nothing, not the hint again
        g.run(6f)
        g.dieOnce()
        assertNotEquals(spiky.hint.toString(), g.bubble)
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

    /** Through the door of round 1 (the pit is jumped), so round 2 starts; returns with the input still held right. */
    private fun Game.winRoundOne() {
        input.right = true
        while (world!!.player.box.cx < 7.6f) update(Bot.DT)
        input.jump = true; input.jumpPressed = true
        run(0.35f)
        input.jump = false
        var guard = 0f
        while (round == 0 && guard < 6f) { update(Bot.DT); guard += Bot.DT }
    }

    @Test
    fun aNewRoundStartsWithTheInputReleasedAndHoldsStillUnderTheBanner() {
        val g = sandbox(demo)
        g.winRoundOne()
        assertEquals(1, g.round)
        assertFalse("held input is released", g.input.right)
        // even pressing right again, nothing moves before the banner is gone
        val x = g.world!!.player.box.cx
        g.input.right = true
        g.run(Game.REMATCH_FREEZE - 0.1f)
        assertEquals(x, g.world!!.player.box.cx, 1e-4f)
        assertEquals(0f, g.world!!.time, 1e-4f)
        g.run(0.5f)
        assertTrue(g.world!!.player.box.cx > x)
    }

    @Test
    fun theLevelPicksUpAtTheRoundReached() {
        val p = Prog()
        val i = Levels.all.indexOfFirst { it.rematch.isNotEmpty() }
        p.checkpoints[i] = 1 to 5
        val g = Game(p, silent).apply { startLevel(i) }
        assertEquals(1, g.round)
        assertEquals(5, g.deaths)
        assertEquals("#2", g.roundTag)
        assertTrue("the banner shows what round this is", g.rematchAge < 0.1f)
        // a level without a checkpoint starts at round 1
        val h = Game(Prog(), silent).apply { startLevel(i) }
        assertEquals(0, h.round)
    }

    @Test
    fun aRoundInheritsTheLevelsStartAndHintUnlessItSaysOtherwise() {
        val l = Level(
            T("x", "x"), T("x", "x"),
            start = listOf(Action.Blink('a', on = 1f, off = 1f)),
            hint = T("Hint", "Tipp"),
            rematch = listOf(
                Round(T("r2", "r2")),
                Round(T("r3", "r3"), start = emptyList(), hint = T("Other", "Anders")),
            ),
        ) { border(); floor(); fill(10..12, 15..15, 'a'); put(2, 14, 'P'); put(28, 14, 'D') }
        assertEquals(l.start, l.rounds[1].start)
        assertEquals("Hint", l.rounds[1].hint?.en)
        assertTrue(l.rounds[2].start.isEmpty())
        assertEquals("Other", l.rounds[2].hint?.en)
    }

    /** Like [spiky], but the rematch has a hint of its own ([Round.hint]): the DSL parameter is named `hint`. */
    private val spikyOwnHint = Level(T("x", "x"), T("Hi.", "Hi."), hint = T("Try the ceiling.", "Probier die Decke."),
        rematch = listOf(Round(T("Again.", "Nochmal."), hint = T("The ceiling is closed now.", "Die Decke ist jetzt zu.")))) {
        border(); floor(); put(9, 14, '^'); put(6, 14, 'P'); put(2, 14, 'D')
    }

    @Test
    fun aRematchRoundShowsItsOwnHintNotRoundOnes() {
        assertEquals("Try the ceiling.", spikyOwnHint.rounds[0].hint?.en)
        assertEquals("The ceiling is closed now.", spikyOwnHint.rounds[1].hint?.en)
        val g = sandbox(spikyOwnHint)
        g.dieOnce(); g.dieOnce()
        assertEquals(spikyOwnHint.rounds[0].hint.toString(), g.bubble)
        g.hold(2f); g.input.left = true; g.run(1.5f); g.input.left = false
        g.run(1.2f)
        assertEquals(1, g.round)
        g.run(Game.REMATCH_FREEZE + 0.1f)
        g.dieOnce(); g.dieOnce()
        assertEquals(spikyOwnHint.rounds[1].hint.toString(), g.bubble)
        assertNotEquals(spikyOwnHint.rounds[0].hint.toString(), g.bubble)
    }

    @Test
    fun theHintComesAgainInTheNextRoundAfterTwoDeathsThere() {
        val g = sandbox(spiky)
        g.dieOnce(); g.dieOnce()
        assertEquals(spiky.hint.toString(), g.bubble)
        // through the door on the left: round 2 starts with its own count
        g.hold(2f); g.input.left = true; g.run(1.5f); g.input.left = false
        g.run(1.2f)
        assertEquals(1, g.round)
        assertEquals(0, g.roundDeaths)
        g.run(Game.REMATCH_FREEZE + 0.1f)
        g.dieOnce()
        assertNotEquals(spiky.hint.toString(), g.bubble)
        g.dieOnce()
        assertEquals(spiky.hint.toString(), g.bubble)
    }
}
