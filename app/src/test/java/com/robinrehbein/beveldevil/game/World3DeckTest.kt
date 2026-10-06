package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * World 3 under V2: rematch rounds in the same room ("Revanche!"). Every round has a solution played with the real
 * physics, every room is the same on every attempt, and the move that won round 1 is what Mephi punishes next.
 */
class World3DeckTest {
    private fun level(n: Int) = World3.levels[n - 1]
    private fun bot(n: Int, round: Int = 0) = Bot(level(n), round)

    /** A card played with nothing behind it: the real trap is somewhere else. Pads that switch by themselves do not count. */
    private fun bluff(t: Trap) = t.actions.any { it is Action.Bluff }

    // ---------- the solutions ----------

    /** The rebuilt levels with a rematch (blocks A to E): their solutions are the registered ones, round 1 first. */
    private val rebuilt: Map<Int, List<Solution>> = World3DesignTest.SOLUTIONS.filterValues { it.size > 1 }

    // ---------- structure ----------

    @Test
    fun aboutAThirdOfTheLevelsGetARematchSpreadOverAllActs() {
        val levels = World3.levels.withIndex().filter { (_, l) -> l.rematch.isNotEmpty() }.map { it.index + 1 }
        assertTrue("rematch levels $levels", levels.size in 12..18)
        for (act in 0..2) assertTrue("act ${act + 1}: $levels", levels.count { (it - 1) / 16 == act } in 4..7)
        assertEquals(rebuilt.keys, levels.toSet())
        rebuilt.forEach { (n, sols) -> assertEquals("level $n", sols.size - 1, level(n).rematch.size) }
        // blocks A to F keep exactly fifteen rematch levels (3 + 2 + 2 + 3 + 3 + 2: levels 17 and 21 in block C, 28, 30 and 32 in block D, 33, 35 and 40 in block E, 41 and 42 in block F)
        assertEquals(15, rebuilt.size)
    }

    /**
     * Bluffs are rare: at most one per act, only in a rematch, and only with a card that was honest in round 1 of the same
     * level (the player learned to fear it there).
     */
    @Test
    fun bluffCardsAreRareAndWereHonestInRoundOne() {
        val bluffs = World3.levels.withIndex().flatMap { (i, l) ->
            l.rounds.drop(1).flatMap { r -> r.traps.filter(::bluff).map { t -> Triple(i + 1, l, t.actions.filterIsInstance<Action.Bluff>().single().card) } }
        }
        assertTrue("bluffs in ${bluffs.map { it.first }}", bluffs.size in 1..3)
        for (act in 0..2) assertTrue("act ${act + 1}: ${bluffs.map { it.first }}", bluffs.count { (it.first - 1) / 16 == act } <= 1)
        for ((n, l, card) in bluffs) {
            val honest = l.traps.any { t -> t.actions.any { it is Action.Play && it.card == card } && !bluff(t) }
            assertTrue("level $n bluffs with $card, which round 1 never played for real", honest)
        }
    }

    @Test
    fun rematchIntrosAreShortBilingualAndNeverRepeat() {
        val banned = listOf("spike", "trap", "collapse", "falls", "drop", "cut", "flip", "reverse", "Falle", "Stachel", "Spike", "stürz", "kapp")
        val intros = World3.levels.flatMap { l -> l.rounds.drop(1).map { it.intro } }
        assertEquals(intros.size, intros.map { it.en }.toSet().size)
        assertEquals(intros.size, intros.map { it.de }.toSet().size)
        val levelIntros = World3.levels.map { it.intro.en }.toSet()
        for (t in intros) {
            assertTrue("$t too long", t.en.length <= 66 && t.de.length <= 66)
            assertTrue("${t.en}: same in both languages", t.en != t.de)
            assertTrue("${t.en}: repeats a level intro", t.en !in levelIntros)
            assertTrue("${t.en} gives away a trap", banned.none { t.en.contains(it, ignoreCase = true) || t.de.contains(it, ignoreCase = true) })
        }
    }

    // ---------- fairness: every round ----------

    @Test
    fun everyRoundParses() {
        World3.levels.forEach { l -> l.rounds.forEach { World(it) } }
    }

    @Test
    fun holdingRightAloneWinsNoRound() {
        val winners = World3.levels.withIndex().flatMap { (i, l) ->
            l.rounds.indices.filter { r -> Bot(l, r).right(14f).world.state == WorldState.WON }.map { r -> "${i + 1}/r${r + 1}" }
        }
        assertEquals(emptyList<String>(), winners)
    }

    @Test
    fun standingStillIsSafeInEveryRound() {
        World3.levels.forEachIndexed { i, l ->
            l.rounds.forEachIndexed { r, round ->
                if (round.traps.none { it.trigger is Trigger.Idle }) {
                    assertEquals("level ${i + 1} round ${r + 1}", WorldState.PLAYING, Bot(l, r).wait(2.2f).world.state)
                }
            }
        }
    }

    @Test
    fun noFanIsSwitchedOffForGoodInAnyRound() {
        World3.levels.forEachIndexed { i, l ->
            l.rounds.forEach { r ->
                val a = (r.start + r.traps.flatMap { it.actions }).flatMap { if (it is Action.FakeWin) listOf(it) + it.then else listOf(it) }
                for (off in a.filterIsInstance<Action.Power>().filter { !it.on }) {
                    if (a.none { it is Action.Fan && it.id == off.id }) continue
                    assertTrue("level ${i + 1} switches fan ${off.id} off for good", a.any { it is Action.Power && it.on && it.id == off.id })
                }
            }
        }
    }

    @Test
    fun everyRematchRoundIsSolvable() {
        for ((n, sols) in rebuilt) for (r in 1 until sols.size) DesignRules.play(level(n), r, sols[r]).expect(WorldState.WON)
    }

    /** A rematch subverts the round before: the move that won it never wins the next round (round 2's on round 3 too). */
    @Test
    fun theRoundOneSolutionLosesTheRematch() {
        for ((n, sols) in rebuilt) for (r in 1 until sols.size) {
            assertNotEquals("level $n: round 1's solution still wins round ${r + 1}", WorldState.WON, DesignRules.play(level(n), r, sols[0]).world.state)
        }
    }

    /** The card a round shows: its honest card, or the card it bluffs with. */
    private fun shown(r: Level) = r.traps.flatMap { it.actions }.firstNotNullOfOrNull { (it as? Action.Play)?.card ?: (it as? Action.Bluff)?.card }

    /** The card must not give the trap away: at least half the rematch rounds bluff or show a card the round before did not. */
    @Test
    fun atLeastHalfTheRematchRoundsChangeTheCard() {
        val rounds = World3.levels.flatMap { l -> l.rounds.zipWithNext() }
        val fresh = rounds.count { (a, b) -> b.traps.any(::bluff) || shown(b) != shown(a) }
        assertTrue("only $fresh of ${rounds.size} rematch rounds change the card", fresh * 2 >= rounds.size)
    }
}
