package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Block B of act 1 (levels 9-16, rebuilt under V2; guarded by [World3DesignTest] and the rules in [DesignRules]): every level bites more than once.
 * For each lethal moment of a clean run, standing still or running straight on from the moment it fires must end in death. Levels 12 and 15 do the
 * same in their rematch, and holding right alone never wins any of them.
 */
class World3Act1TrapsTest {
    private fun level(n: Int) = World3.levels[n - 1]
    private fun sols(n: Int) = World3DesignTest.SOLUTIONS.getValue(n)

    /** Lethal moments of round [r] of level [n] that killed a probe: standing still (patient) or running straight on (reckless) from the moment the trap fired. */
    private fun killers(n: Int, r: Int): List<String> {
        val l = level(n)
        val sol = sols(n)[r]
        val clean = DesignRules.cleanRun(l, r, sol)
        assertEquals(WorldState.WON, clean.world.state)
        return DesignRules.counted(clean, l, r, sol).filter { it.weight == DesignRules.Weight.LETHAL }.mapNotNull { m ->
            val patient = DesignRules.patientProbe(l, r, sol, m.triggered)
            val reckless = DesignRules.recklessProbe(l, r, sol, m.triggered)
            if (patient.world.state == WorldState.DEAD || reckless.world.state == WorldState.DEAD) "${m.trigger}@${m.triggered}" else null
        }
    }

    private fun bites(n: Int, rounds: Int = 1, min: Int = 2) {
        for (r in 0 until rounds) {
            val k = killers(n, r)
            assertTrue("level $n round ${r + 1}: only ${k.size} moments bite: $k", k.size >= min)
        }
        assertTrue(Bot(level(n)).right(14f).world.state != WorldState.WON)
    }

    @Test fun level09Bites() = bites(9)
    @Test fun level10Bites() = bites(10)
    @Test fun level11Bites() = bites(11)
    @Test fun level12BitesInBothRounds() = bites(12, rounds = 2)
    @Test fun level13Bites() = bites(13)
    @Test fun level14Bites() = bites(14)
    @Test fun level15BitesInBothRounds() = bites(15, rounds = 2)
    @Test fun level16Bites() = bites(16)
}
