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

    /** §8, World 2: at least 12 levels with switches (R1, R2, R4), at least 8 with real portal routing (R3, R4). */
    @Test
    fun switchesAndPortalRoutingAreEverywhere() {
        val switches = DesignRules.withBlock(design, "R1", "R2", "R4")
        val routing = DesignRules.withBlock(design, "R3", "R4")
        assertTrue("switch levels $switches", switches.size >= 12)
        assertTrue("routing levels $routing", routing.size >= 8)
    }

    companion object {
        /** The pilot (11-24) fixes its own rows; add them here once the pilot report is in. */
        val PILOT: Set<Int> = (11..24).toSet()

        /** §8, World 2 "Höllen-Rechenzentrum". */
        val DESIGN: Map<Int, Design> = mapOf(
            // Act 1 "Handshake"
            1 to d("–", "U1", breather = true),
            2 to d("R3", "U5"),
            3 to d("R6", "U4"),
            4 to d("R8", "U6"),
            5 to d("R12", "U1"),
            6 to d("R4", "U11"),
            7 to d("R5", "U2"),
            8 to d("–", "U1", breather = true),
            9 to d("R1", "U9"),
            10 to d("R3", "U7"),
            // 11-24: pilot, see PILOT
            // Act 2 "Traffic"
            25 to d("R4", "U1"),
            26 to d("R2", "U8"),
            27 to d("R10", "U2"),
            28 to d("R3", "U13"),
            29 to d("R1", "U7"),
            30 to d("R3+R5", "U11"),
            31 to d("R8", "U3"),
            32 to d("R4+R3+R8", "U12+U13+U4"),
            // Act 3 "Root"
            33 to d("R7", "U14"),
            34 to d("R3+R5", "U10"),
            35 to d("R4", "U12"),
            36 to d("R5", "U16:Ghost"),
            37 to d("R1", "U15"),
            38 to d("R3", "U1"),
            39 to d("R1", "U16:Pause"),
            40 to d("–", "U16:Roll", breather = true),
            41 to d("R7", "U1"),
            42 to d("R5+R7", "U15"),
            43 to d("R4", "U16:Shake"),
            44 to d("R9", "U16:Undo"),
            45 to d("R2", "U3"),
            46 to d("R10+R1", "U12"),
            47 to d("R5", "U9+U10"),
            48 to d("R4+R3+R6", "U11+U9+U4"),
        )

        /** Levels that follow the V2 rules; the rollout adds each block here (see [DesignRules]). */
        val REBUILT: Set<Int> = emptySet()

        /** Level number → bot solution per round (round 1 first). */
        val SOLUTIONS: Map<Int, List<Solution>> = emptyMap()

        /** Plays the registered solution of level [n], [round] 1-based, and expects the win (for [World2Test]). */
        fun play(n: Int, round: Int = 1) =
            DesignRules.play(World2.levels[n - 1], round - 1, SOLUTIONS.getValue(n)[round - 1]).expect(WorldState.WON)
    }
}
