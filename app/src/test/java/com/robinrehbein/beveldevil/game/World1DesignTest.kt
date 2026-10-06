package com.robinrehbein.beveldevil.game

/** Guard rails (docs/LEVEL_DESIGN_V2.md §9) for World 1; the tests are in [DesignTestBase], the rules in [DesignRules]. */
class World1DesignTest : DesignTestBase() {
    override val world = 1
    override val levels get() = World1.levels
    override val design = DESIGN
    override val rebuilt = REBUILT
    override val solutions = SOLUTIONS
    override val allSolutions = ALL_SOLUTIONS

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
            43 to d("R2", "U7"),
            44 to d("–", "U16:Shake"),
            45 to d("–", "U1"),
            46 to d("R1", "U15"),
            47 to d("R12", "U3"),
            48 to d("R1+R5+R7", "U14+U18"),
        )

        /** Levels that follow the V2 rules; the rollout adds each block here (see [DesignRules]). */
        val REBUILT: Set<Int> = (7..48).toSet()

        /** Level number → bot solution per round (round 1 first). */
        val SOLUTIONS: Map<Int, List<Solution>> = World1RoomsA.solutions + World1RoomsB.solutions + World1RoomsC.solutions + World1RoomsD.solutions + World1RoomsE.solutions

        /**
         * The tutorial 1-6 (outside the V2 rollout, never in [REBUILT]): its clean runs, the same scripts as [World1Test]
         * and [World1DeckTest] play. The round rules A-C and E ([DesignRules.roundRules]) and the naive probe report need them.
         */
        val TUTORIAL_SOLUTIONS: Map<Int, List<Solution>> = mapOf(
            1 to listOf({ rightTo(17.6f).rightJump(0.35f).right(3f) }),
            2 to listOf({ rightTo(8f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().rightJump(0.35f).landRight().rightJump(0.35f).landRight().right(2f) }),
            3 to listOf({
                hopR(21.2f).rightTo(26.5f).leftTo(24.3f).leftJump(0.35f).landLeft().rightTo(21.3f).rightJump(0.35f).landRight()
                    .rightTo(25.6f).rightJump(0.35f).landRight().leftTo(23f).right(4f)
            }),
            4 to listOf(
                {
                    rightTo(13.12f).wait(0.5f).leftTo(10.2f).wait(0.5f).rightTo(11.0f).rightJump(0.35f).landRight()
                        .rightTo(17.3f).waitFor { it.player.grounded }.wait(0.7f)
                        .leftTo(17.6f).rightTo(19f).rightJump(0.35f).landRight().wait(0.5f)
                        .rightJump(0.35f).landRight().right(1f).left(2f)
                },
                { rightTo(18.3f).left(0.3f).wait(0.6f).rightTo(18.6f).rightJump(0.35f).landRight().right(3f) },
            ),
            5 to listOf({
                rightTo(18.5f).jump(0.3f).wait(0.5f).leftTo(16.8f).wait(0.2f).rightJump(0.35f).right(0.2f).rightJump(0.35f)
                    .rightTo(21.4f).wait(0.6f).rightTo(22.4f).rightJump(0.35f).landRight().rightJump(0.35f).right(2f)
            }),
            6 to listOf(
                { rightTo(12.8f).rightJump(0.3f).rightTo(18.6f).rightJump(0.35f).landRight().rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f) },
                { rightTo(12.8f).rightJump(0.3f).landRight().untilSaw(1.4f).jump(0.35f).right(3f) },
            ),
        )

        /** Every level's solutions, the tutorial included. */
        val ALL_SOLUTIONS: Map<Int, List<Solution>> = TUTORIAL_SOLUTIONS + SOLUTIONS

        /** Plays the registered solution of level [n], [round] 1-based, and expects the win (for [World1Test]). */
        fun play(n: Int, round: Int = 1) =
            DesignRules.play(World1.levels[n - 1], round - 1, SOLUTIONS.getValue(n)[round - 1]).expect(WorldState.WON)
    }
}
