package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertTrue
import org.junit.Test

/** Guard rails (docs/LEVEL_DESIGN_V2.md §9) for World 3; the tests are in [DesignTestBase], the rules in [DesignRules]. */
class World3DesignTest : DesignTestBase() {
    override val world = 3
    override val levels get() = World3.levels
    override val design = DESIGN
    override val rebuilt = REBUILT
    override val solutions = SOLUTIONS
    /** The lead mechanics: heat in act 2, fans as transport in act 3 (exempt from "3 of 4", not from "twice in a row"). */
    override val lead = mapOf(2 to "R11", 3 to "R10")

    /** §8, World 3: the blocks of one mechanic are broken up, at the latest every third level brings another family. */
    @Test
    fun everyThirdLevelBringsAnotherFamily() {
        val v = DesignRules.otherFamilyViolations(design)
        assertTrue(v.joinToString("\n"), v.isEmpty())
    }

    companion object {
        /** §8, World 3 "Platine". A row without block ("–") and without ★ is a trap room. */
        val DESIGN: Map<Int, Design> = mapOf(
            // Act 1 "Stromkreise"
            1 to d("R1", "U17"),
            2 to d("–", "U6"),
            3 to d("R8", "U1"),
            4 to d("–", "U7"),
            5 to d("–", "U2"),
            6 to d("R1", "U15"),
            7 to d("R8", "U2"),
            8 to d("–", "U17", breather = true),
            9 to d("–", "U3"),
            10 to d("R8", "U4"),
            11 to d("R5", "U17"),
            12 to d("R7", "U14"),
            13 to d("R5", "U1"),
            14 to d("–", "U6"),
            15 to d("–", "U9"),
            16 to d("R1+R5+R8", "U17+U1+U18"),
            // Act 2 "Überhitzung"
            17 to d("R11", "U17"),
            18 to d("R1+R11", "U7"),
            19 to d("–", "U1"),
            20 to d("R6", "U4"),
            21 to d("R11+R5", "U8"),
            22 to d("–", "U3"),
            23 to d("–", "U2"),
            24 to d("–", "U17"),
            25 to d("R10", "U12"),
            26 to d("–", "U13"),
            27 to d("–", "U17", breather = true),
            28 to d("–", "U7"),
            29 to d("R2", "U15"),
            30 to d("–", "U14"),
            31 to d("R11+R5", "U3"),
            32 to d("R11+R1", "U17+U2+U18"),
            // Act 3 "Lüfter"
            33 to d("R10", "U1"),
            34 to d("R5", "U12"),
            35 to d("R4", "U7"),
            36 to d("–", "U6"),
            37 to d("–", "U3"),
            38 to d("–", "U15"),
            39 to d("R10", "U2"),
            40 to d("–", "U12"),
            41 to d("–", "U14"),
            42 to d("–", "U8"),
            43 to d("R1+R10", "U17"),
            44 to d("–", "U10", breather = true),
            45 to d("–", "U3"),
            46 to d("R5", "U16"),
            47 to d("R7+R6", "U14+U4+U18"),
            48 to d("R4+R10+R11", "U12+U10+U18"),
        )

        /** Levels that follow the V2 rules; the rollout adds each block here (see [DesignRules]). */
        val REBUILT: Set<Int> = (1..16).toSet()

        /** Level number → bot solution per round (round 1 first). */
        val SOLUTIONS: Map<Int, List<Solution>> = World3RoomsA.solutions + World3RoomsB.solutions

        /** Plays the registered solution of level [n], [round] 1-based, and expects the win (for [World3Test]). */
        fun play(n: Int, round: Int = 1) =
            DesignRules.play(World3.levels[n - 1], round - 1, SOLUTIONS.getValue(n)[round - 1]).expect(WorldState.WON)
    }
}
