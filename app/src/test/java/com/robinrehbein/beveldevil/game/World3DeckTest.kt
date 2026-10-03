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

    /** Round 1 (as in [World3Test]) of the levels that get a rematch. */
    private val first: Map<Int, (Bot) -> Bot> = mapOf(
        1 to { b -> b.rightTo(22f).rightJump(0.55f).landRight().right(3f) },
        2 to { b -> b.rightTo(23.5f).rightJump(0.55f).landRight().right(3f) },
        3 to { b -> b.rightTo(8.3f).waitPowered('a', false).waitPowered('a').rightTo(23.4f).rightJump(0.55f).landRight().right(3f) },
        4 to { b -> b.hopR(11.5f).rightJump(0.55f).landRight().right(3f) },
        5 to { b -> b.rightTo(14.3f).rightJump(0.55f).landRight().rightTo(20.8f).rightJump(0.55f).landRight().waitPowered('Z').waitPowered('Z', false).right(3f) },
        6 to { b -> b.rightTo(5.3f).rightJump(0.55f).landRight().rightTo(16.6f).rightJump(0.55f).landRight().right(3f) },
        8 to { b -> b.hopR(14f).hopR(18.9f).right(3f) },
        9 to { b -> b.rightTo(16.2f).rightJump(0.55f).landRight().right(3f) },
        11 to { b -> b.hopR(11.5f).rightJump(0.55f).landRight().right(3f) },
        12 to { b -> b.rightTo(18f).rightJump(0.55f).landRight().waitFor { (it.saws.firstOrNull()?.x ?: 99f) < 21.5f }.right(3f) },
        14 to { b -> b.rightTo(7.2f).rightJump(0.55f).landRight().rightTo(25f).rightJump(0.55f).landRight().right(3f) },
        15 to { b -> b.rightUntil { it.pads[0].presses >= 1 }.hopS(7.3f).leftUntil { it.pads[1].presses >= 1 }.hopR(20f).rightUntil { it.pads[2].presses >= 1 }.leftKeyRightTo(31f) },
        16 to { b -> b.rightTo(5f).rightTo(15.5f).waitPowered('Z', false).rightTo(21f).rightJump(0.55f).landRight().right(3f) },
        17 to { b -> b.rightTo(15.5f).waitCooled('h').rightTo(24.6f).rightJump(0.55f).landRight().right(3f) },
        21 to { b -> b.rightTo(11.5f).waitCooled('h').rightTo(18.5f).waitCooled('h').rightJump(0.55f).landRight().rightTo(24.6f).rightJump(0.55f).landRight().right(4f) },
        23 to { b -> b.rightTo(7f).waitPowered('a').rightTo(24.3f).rightJump(0.55f).landRight().right(4f) },
        28 to { b -> b.rightTo(16.5f).waitCooled('h').rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(4f) },
        30 to { b -> b.rightTo(6.5f).waitPowered('Z', false).rightTo(12f).right(4f).waitWhile { it.fake != null }
            .waitWhile(2f) { !it.player.grounded || it.door.moving }.leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().left(2f) },
        31 to { b -> b.rightTo(15.5f).waitCooled('h').rightTo(19f).rightJump(0.55f).landRight().rightTo(24.3f).rightJump(0.55f).landRight().right(4f) },
        32 to { b -> b.rightTo(5.5f).waitCooled('c').rightTo(25.5f).rightJump(0.55f).landRight().right(3f) },
        34 to { b -> b.hopR(8f).rightTo(21.6f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().right(1f).left(3f) },
        36 to { b -> b.hopR(8f).rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(1f).left(3f) },
        37 to { b -> b.rightTo(9f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().rightTo(22.4f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().right(1f).left(3f) },
        38 to { b -> b.rightTo(6f).rightTo(10f).waitFor { it.player.box.cy < 7f }.rightTo(16.5f).rightJump(0.55f).landRight().right(3f) },
        39 to { b -> b.rightTo(14.2f).leftTo(10.3f).waitFor { it.fans[0].wind == 0f }.rightTo(14.3f).rightJump(0.55f).landRight().rightTo(21.3f).rightJump(0.55f).landRight()
            .rightUntilSaw(3.2f).rightJump(0.55f).landRight().right(1f).left(3f) },
        41 to { b -> climb41(b).rightTo(21.2f).rightJump(0.55f).landRight().right(4f) },
        42 to { b -> b.rightTo(12.4f).waitFor { it.player.box.cy < 5.8f }.rightUntilSaw(4.3f).rightJump(0.55f).landRight().right(3f) },
    )

    private fun climb41(b: Bot) = b.rightTo(7f).waitFor { it.player.box.cy < 6.9f }.rightTo(14f).waitFor { it.player.box.cy < 3.3f }.rightTo(17.5f).landRight()

    /** Rematch rounds: level to the solution of each extra round. */
    private val rematch: Map<Int, List<(Bot) -> Bot>> = mapOf(
        1 to listOf { b -> b.rightTo(10.3f).waitPowered('a', false).waitPowered('a').rightTo(22f).rightJump(0.55f).landRight().right(3f) },
        2 to listOf(
            { b -> b.rightTo(10f).waitPowered('Z').leftTo(8.3f).rightTo(23.5f).rightJump(0.55f).landRight().right(3f) },
            { b -> b.rightTo(22f).rightJump(0.12f).landRight().rightTo(26.2f).rightJump(0.35f).landRight().right(0.5f).left(2f) },
        ),
        3 to listOf { b -> b.rightTo(8.3f).waitPowered('a', false).waitPowered('a').right(4f) },
        4 to listOf { b -> b.hopR(11.5f).right(3f) },
        5 to listOf { b -> b.rightTo(20.8f).rightJump(0.55f).landRight().waitPowered('Z').waitPowered('Z', false).right(3f) },
        6 to listOf { b -> b.rightTo(16.6f).rightJump(0.55f).landRight().right(3f) },
        8 to listOf { b -> b.rightTo(16f).rightJump(0.55f).landRight().right(3f) },
        9 to listOf { b -> b.rightTo(13.3f).waitPowered('Z').leftTo(10.4f).rightTo(16.2f).rightJump(0.55f).landRight().right(3f) },
        11 to listOf { b -> b.rightTo(16.5f).rightJump(0.55f).landRight().right(3f) },
        12 to listOf { b -> b.rightTo(21.5f).rightUntilSaw(3.2f).rightJump(0.55f).landRight().right(2f) },
        14 to listOf { b -> b.rightUntil { it.pads[0].presses >= 1 }.leftTo(3.2f).rightTo(7.2f).rightJump(0.55f).landRight().rightTo(25f).rightJump(0.55f).landRight().right(3f) },
        15 to listOf { b -> b.rightUntil { it.pads[0].presses >= 1 }.hopS(7.3f).leftUntil { it.pads[1].presses >= 1 }.hopS(20f).leftUntil { it.pads[2].presses >= 1 }.leftKeyRightTo(31f) },
        16 to listOf { b -> b.rightTo(5f).rightTo(15.5f).waitPowered('Z', false).rightTo(19.6f).rightJump(0.55f).landRight().right(3f) },
        17 to listOf { b -> b.rightTo(5f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(3f) },
        21 to listOf { b -> b.rightTo(11.5f).waitCooled('h').rightTo(18.5f).waitCooled('h').rightTo(24.6f).rightJump(0.55f).landRight().right(4f) },
        23 to listOf { b -> b.rightTo(3.2f).waitPowered('a', false).waitPowered('a').rightTo(24.3f).rightJump(0.55f).landRight().right(4f) },
        28 to listOf { b -> b.rightTo(16.5f).waitCooled('h').right(3f) },
        30 to listOf { b -> b.rightTo(8.5f).waitPowered('Z', false).rightTo(12f).right(4f).waitWhile { it.fake != null }
            .waitWhile(2f) { !it.player.grounded || it.door.moving }.leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().left(2f) },
        31 to listOf { b -> b.rightTo(13.4f).rightJump(0.35f).landRight().rightTo(20.5f).waitCooled('h').rightTo(24.3f).rightJump(0.55f).landRight().right(4f) },
        32 to listOf { b -> b.rightTo(5.5f).waitCooled('c').rightTo(21.6f).rightJump(0.55f).landRight().right(3f) },
        34 to listOf { b -> b.hopR(8f).waitFor { it.fans[0].wind > 3f }.rightTo(21.6f).rightJump(0.55f).landRight().right(1f).left(3f) },
        36 to listOf { b -> b.hopR(8f).rightTo(22f).rightJump(0.12f).landRight().rightTo(24.4f).rightJump(0.35f).landRight().right(1f).left(3f) },
        37 to listOf { b -> b.rightTo(9f).waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().waitFor { it.fans[0].wind > 9.5f }.rightJump(0.55f).landRight().right(1f).left(3f) },
        38 to listOf { b -> b.rightTo(6f).leftTo(4.3f).rightTo(10f).waitFor { it.player.box.cy < 7f }.rightTo(16.5f).right(3f) },
        39 to listOf { b -> b.rightTo(14.2f).waitFor { it.fans[0].wind == 0f }.rightTo(14.3f).rightJump(0.55f).landRight().rightTo(21.3f).rightJump(0.55f).landRight()
            .rightUntilSaw(3.2f).rightJump(0.55f).landRight().right(1f).left(3f) },
        41 to listOf { b -> climb41(b).rightTo(26f).right(4f) },
        42 to listOf { b -> b.rightTo(12.4f).waitFor { it.player.box.cy < 5.8f }.right(3f) },
    )

    // ---------- structure ----------

    @Test
    fun twentyLevelsGetARematchSpreadOverAllActs() {
        val levels = World3.levels.withIndex().filter { (_, l) -> l.rematch.isNotEmpty() }.map { it.index + 1 }
        assertTrue("rematch levels $levels", levels.size >= 20)
        for (act in 0..2) assertTrue("act ${act + 1}: $levels", levels.count { (it - 1) / 16 == act } >= 5)
        assertEquals(rematch.keys, levels.toSet())
        rematch.forEach { (n, rounds) -> assertEquals("level $n", rounds.size, level(n).rematch.size) }
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
        for ((n, rounds) in rematch) rounds.forEachIndexed { k, solve -> solve(bot(n, round = k + 1)).expect(WorldState.WON) }
    }

    /** A rematch subverts round 1: the move that won it loses the next round. */
    @Test
    fun theRoundOneSolutionLosesTheRematch() {
        for (n in rematch.keys) {
            first.getValue(n)(bot(n)).expect(WorldState.WON)
            val b = first.getValue(n)(bot(n, round = 1))
            assertNotEquals("level $n: round 1's solution still wins round 2", WorldState.WON, b.world.state)
        }
    }
}
