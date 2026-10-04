package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertTrue
import org.junit.Test

/** Guard rails (docs/LEVEL_DESIGN_V2.md §9) for World 2; the tests are in [DesignTestBase], the rules in [DesignRules]. */
class World2DesignTest : DesignTestBase() {
    override val world = 2
    override val levels get() = World2.levels
    override val design = DESIGN
    override val pending = PILOT
    override val rebuilt = REBUILT
    override val solutions = SOLUTIONS
    override val pilot = PILOT_V2
    override val pilotSolutions = PILOT_SOLUTIONS

    /**
     * §8, World 2: at least 8 levels with real portal routing (R3, R4). Switches (R1, R2, R4) are capped at 3 per act by
     * the rotation rule H12 ([DesignRules.tableRotationViolations]), the pilot included.
     */
    @Test
    fun portalRoutingIsEverywhere() {
        val routing = DesignRules.withBlock(design, "R3", "R4")
        assertTrue("routing levels $routing", routing.size >= 8)
    }

    companion object {
        /** The pilot (11-16) fixes its own rows; add them here once the pilot report is in. */
        val PILOT: Set<Int> = (11..16).toSet()

        /** §8, World 2 "Höllen-Rechenzentrum". A row without block ("–") and without ★ is a trap room. */
        val DESIGN: Map<Int, Design> = mapOf(
            // Act 1 "Handshake"
            1 to d("–", "U1", breather = true),
            2 to d("R3", "U5"),
            3 to d("R6", "U4"),
            4 to d("–", "U6"),
            5 to d("–", "U1"),
            6 to d("R4", "U11"),
            7 to d("–", "U2"),
            8 to d("–", "U1", breather = true),
            9 to d("–", "U9"),
            10 to d("R3", "U7"),
            // 11-16: pilot, see PILOT
            // Act 2 "Traffic"
            17 to d("R10", "U12"),
            18 to d("R5", "U13"),
            19 to d("–", "U9"),
            20 to d("R8", "U13"),
            21 to d("R3", "U11"),
            22 to d("–", "U2+U3"),
            23 to d("–", "U3"),
            24 to d("–", "U13+U1"),
            25 to d("–", "U1"),
            26 to d("–", "U8"),
            27 to d("R10", "U2"),
            28 to d("R3", "U13"),
            29 to d("–", "U7"),
            30 to d("R3+R5", "U11+U18"),
            31 to d("R8", "U3"),
            32 to d("R4+R3+R8", "U12+U13"),
            // Act 3 "Root"
            33 to d("–", "U14"),
            34 to d("R3+R5", "U10"),
            35 to d("–", "U12"),
            36 to d("–", "U16:Ghost"),
            37 to d("R1", "U15"),
            38 to d("R3", "U1"),
            39 to d("–", "U16:Pause"),
            40 to d("–", "U16:Roll", breather = true),
            41 to d("–", "U1"),
            42 to d("R5+R7", "U15"),
            43 to d("–", "U16:Shake"),
            44 to d("–", "U16:Undo"),
            45 to d("R2", "U3"),
            46 to d("R10", "U12"),
            47 to d("R5", "U9+U10"),
            48 to d("R4+R3", "U11+U9+U18"),
        )

        /**
         * The pilot rooms built before recipe v2. [DesignTestBase.pilotV2Report] checks them against every v2 rule and
         * writes the violations to build/reports/pilot-v2-violations.txt; a rebuilt pilot level moves to [REBUILT] (with
         * its row in [DESIGN], out of [PILOT]) and its solutions to [SOLUTIONS].
         */
        val PILOT_V2: Set<Int> = (11..16).toSet()

        /** The pilot's solutions, one per round (round 1 first), from [World2Test.rooms] and [World2Rooms]. */
        val PILOT_SOLUTIONS: Map<Int, List<Solution>> =
            (11..16).associateWith { n -> listOf<Solution>({ World2Test.rooms.getValue(n)(this) }) } +
                mapOf(
                    11 to listOf({ World2Test.rooms.getValue(11)(this) }, { World2Rooms.l11r2(this) }),
                    14 to listOf({ World2Test.rooms.getValue(14)(this) }, { World2Rooms.l14r2(this) }),
                )

        /** Levels that follow the V2 rules; the rollout adds each block here (see [DesignRules]). */
        val REBUILT: Set<Int> = (17..24).toSet()

        /** Level number → bot solution per round (round 1 first). */
        val SOLUTIONS: Map<Int, List<Solution>> =
            World2Rooms.solutions.mapValues { (_, s) -> listOf<Solution>({ s(this) }) } +
                mapOf(
                    18 to listOf({ World2Rooms.l18(this) }, { World2Rooms.l18r2(this) }),
                    20 to listOf({ World2Rooms.l20(this) }, { World2Rooms.l20r2(this) }),
                )

        /** Plays the registered solution of level [n], [round] 1-based, and expects the win (for [World2Test]). */
        fun play(n: Int, round: Int = 1) =
            DesignRules.play(World2.levels[n - 1], round - 1, SOLUTIONS.getValue(n)[round - 1]).expect(WorldState.WON)
    }
}
