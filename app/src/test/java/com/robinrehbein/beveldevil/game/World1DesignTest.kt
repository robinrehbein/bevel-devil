package com.robinrehbein.beveldevil.game

/** Guard rails (docs/LEVEL_DESIGN_V2.md §9) for World 1; the tests are in [DesignTestBase], the rules in [DesignRules]. */
class World1DesignTest : DesignTestBase() {
    override val world = 1
    override val levels get() = World1.levels
    override val design = DESIGN
    override val rebuilt = REBUILT
    override val solutions = SOLUTIONS

    companion object {
        /** §8, World 1 "Höllenkeller". Levels 1-6 are the tutorial; the V2 rules start at 7. A row without block ("–") and without ★ is a trap room. */
        val DESIGN: Map<Int, Design> = mapOf(
            // Act 1 "Die Karten"
            1 to d("–", "U1"),
            2 to d("–", "U5"),
            3 to d("R5", "U4"),
            4 to d("–", "U2"),
            5 to d("R12", "U15"),
            6 to d("–", "U7"),
            7 to d("–", "U1"),
            8 to d("R5", "U10"),
            9 to d("R1", "U9"),
            10 to d("–", "U3"),
            11 to d("–", "U1"),
            12 to d("–", "U9", breather = true),
            13 to d("R7", "U14"),
            14 to d("–", "U12"),
            15 to d("R6", "U6"),
            16 to d("R1+R5", "U7+U18"),
            // Act 2 "Neue Regeln"
            17 to d("–", "U1"),
            18 to d("–", "U7"),
            19 to d("R2", "U2"),
            20 to d("–", "U6"),
            21 to d("R9", "U1"),
            22 to d("R1", "U13"),
            23 to d("–", "U7"),
            24 to d("R9", "U3"),
            25 to d("R6", "U15"),
            26 to d("–", "U8"),
            27 to d("–", "U1", breather = true),
            28 to d("–", "U7"),
            29 to d("R10", "U3"),
            30 to d("–", "U2"),
            31 to d("R7", "U7"),
            32 to d("R1+R5+R8", "U1+U7+U18"),
            // Act 3 "Mephi schummelt"
            33 to d("R7", "U14+U18"),
            34 to d("–", "U16:Pause"),
            35 to d("–", "U4", breather = true),
            36 to d("–", "U16:FrameCrack"),
            37 to d("–", "U1"),
            38 to d("R5", "U10"),
            39 to d("R10", "U16:Tilt"),
            40 to d("–", "U9"),
            41 to d("R12", "U2"),
            42 to d("–", "U8"),
            43 to d("R2", "U16:Ghost"),
            44 to d("–", "U16:Shake"),
            45 to d("–", "U1"),
            46 to d("R1", "U15"),
            47 to d("R12", "U3"),
            48 to d("R1+R5+R7", "U14+U18"),
        )

        /** Levels that follow the V2 rules; the rollout adds each block here (see [DesignRules]). */
        val REBUILT: Set<Int> = (7..32).toSet()

        /** Level number → bot solution per round (round 1 first). */
        val SOLUTIONS: Map<Int, List<Solution>> = World1RoomsA.solutions + World1RoomsB.solutions + World1RoomsC.solutions

        /** Plays the registered solution of level [n], [round] 1-based, and expects the win (for [World1Test]). */
        fun play(n: Int, round: Int = 1) =
            DesignRules.play(World1.levels[n - 1], round - 1, SOLUTIONS.getValue(n)[round - 1]).expect(WorldState.WON)
    }
}
