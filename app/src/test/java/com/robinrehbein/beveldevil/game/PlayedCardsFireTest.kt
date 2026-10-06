package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every trap that plays a card ([Action.Play]) goes off on the registered clean run of its round: Mephi's card is the
 * point of the trap, so the solution path must see it. Runs over every level and round of all three worlds that has a
 * bot solution (the SOLUTIONS of the world design tests).
 */
class PlayedCardsFireTest {
    private val worlds = listOf(
        Triple(1, World1.levels, World1DesignTest.SOLUTIONS),
        Triple(2, World2.levels, World2DesignTest.SOLUTIONS),
        Triple(3, World3.levels, World3DesignTest.SOLUTIONS),
    )

    /**
     * Known content failures, "world-level-round" (round 1-based): the card sits on a trap the clean run is meant to
     * avoid, so only a mistake plays it. Level design, not engine; they stay listed here until the level is reworked.
     */
    private val expectedFailures = setOf(
        // Night Shift, rematch: Airborne(3, 8.5) plays SINKING on whoever hops in the first stretch; the clean run walks it
        "1-17-2",
        // Carpentry: Touch('b') plays GHOST_BLOCK on whoever jumps into the hidden knot; the clean run never bonks it
        "1-23-1",
        // Security Audit, rematch: Touch('a') plays SINKING on stone 1, which the clean run hops
        "2-41-2",
        // Privilege Escalation: Landed(27, 30) plays CRUMBLE after 0.9 s, but the clean run is through the door first
        // (World.sprung records a trap only once its actions have run)
        "2-46-1",
        // Relay Race, rematch: Touch('k') plays COLLAPSE on the heatsink, which the clean run hops
        "3-21-2",
    )

    private fun plays(t: Trap) = t.actions.flatMap { it.flat() }.any { it is Action.Play }

    /** "world-level-round: trigger" for every card trap of the round that did not go off on [solution]. */
    private fun missed(world: Int, n: Int, round: Int, level: Level, solution: Solution): List<String> {
        val stage = level.rounds[round]
        val bot = DesignRules.play(level, round, solution)
        val sprung = bot.world.sprung.map { it.trap }.toSet()
        return stage.traps.filter { plays(it) && it !in sprung }.map { "$world-$n-${round + 1}: ${it.trigger} (${stage.name.en}, run ends ${bot.world.state})" }
    }

    @Test
    fun everyCardTrapFiresOnTheSolutionPath() {
        val failing = LinkedHashMap<String, String>()
        var checked = 0
        for ((world, levels, solutions) in worlds) for ((n, list) in solutions.toSortedMap()) {
            val level = levels[n - 1]
            for ((r, s) in list.withIndex()) {
                if (r >= level.rounds.size) continue
                checked++
                missed(world, n, r, level, s).forEach { failing.putIfAbsent(it.substringBefore(':'), it) }
            }
        }
        failing.values.forEach { println("card trap not fired: $it") }
        val unexpected = failing.keys - expectedFailures
        // a listed round that passes now is only a warning: World 3 is being rebuilt on this branch, and its rooms may
        // lose their bait cards at any time
        (expectedFailures - failing.keys).forEach { println("expected card-trap failure $it passes now: drop it from the list") }
        assertTrue("checked no rounds", checked > 0)
        assertTrue(
            "card traps that never fire on the solution path:\n" + failing.filterKeys { it in unexpected }.values.joinToString("\n"),
            unexpected.isEmpty(),
        )
    }
}
