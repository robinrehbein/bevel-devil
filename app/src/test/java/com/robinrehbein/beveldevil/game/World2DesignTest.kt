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
        /** The pilot (11-24) fixes its own rows; add them here once the pilot report is in. */
        val PILOT: Set<Int> = (11..24).toSet()

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
            // 11-24: pilot, see PILOT
            // Act 2 "Traffic"
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
        val PILOT_V2: Set<Int> = (11..24).toSet()

        /** The pilot's solutions, one per round (round 1 first), from [World2Test.rooms] and [World2Rooms]. */
        val PILOT_SOLUTIONS: Map<Int, List<Solution>> by lazy {
            val old: Map<Int, List<Solution>> = (11..16).associateWith { n -> listOf<Solution>({ World2Test.rooms.getValue(n)(this) }) } +
                World2Rooms.solutions.mapValues { (_, s) -> listOf<Solution>({ s(this) }) } +
                mapOf<Int, List<Solution>>(
                    11 to listOf<Solution>({ World2Test.rooms.getValue(11)(this) }, { World2Rooms.l11r2(this) }),
                    14 to listOf<Solution>({ World2Test.rooms.getValue(14)(this) }, { World2Rooms.l14r2(this) }),
                    18 to listOf<Solution>({ World2Rooms.l18(this) }, { World2Rooms.l18r2(this) }),
                    20 to listOf<Solution>({ World2Rooms.l20(this) }, { World2Rooms.l20r2(this) }),
                )
            old + SOLUTIONS
        }

        /** Levels that follow the V2 rules; the rollout adds each block here (see [DesignRules]). */
        val REBUILT: Set<Int> = emptySet()

        /** Level number → bot solution per round (round 1 first). */
        val SOLUTIONS: Map<Int, List<Solution>> = mapOf(
            11 to listOf(
                { hopR(10.8f, 0.5f).leftTo(13f).leftJump(0.5f).landLeft()
                    .waitFor { w -> w.saws.any { it.y < 8.5f && it.x > 8f } }.leftTo(2.2f).left(1f)
                    .hopR(10.8f, 0.5f).hopR(15.2f, 0.5f).hopR(21.5f, 0.5f).right(1.5f) },
                { hopR(10.8f, 0.5f).leftTo(13f).leftJump(0.5f).landLeft()
                    .waitUntil(3.5f).leftTo(2.2f).waitUntil(5.4f).rightTo(12.6f)
                    .hopR(15.2f, 0.5f).hopR(21.5f, 0.5f).right(1.5f) },
            ),
            12 to listOf(
                { rightTo(4.8f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight()
                    .rightJump(0.5f).landRight().rightJump(0.5f).landRight().right(0.4f)
                    .leftTo(26.2f).hopL(26.0f, 0.5f).leftTo(19.3f)
                    .waitFor { it.group('f').oy > 5f }.waitFor { it.group('f').oy < 0.3f }
                    .hopL(18.8f, 0.5f).leftJump(0.5f).landLeft().left(2f) },
            ),
            14 to listOf(
                { rightTo(10.3f).rightJump(0.5f).landRight().hopL(9.2f, 0.5f).leftTo(1.5f)
                    .waitFor { it.links[2].on }.left(0.3f).leftTo(24.1f).wait(0.45f).hopL(23.5f, 0.5f)
                    .leftUntil { it.player.box.cy > 10f }.left(0.4f)
                    .leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().left(1f) },
                { right(1.65f).hopL(9.2f, 0.5f).waitFor { it.links[2].to.first == 30 }.left(0.65f)
                    .leftTo(24.1f).wait(0.45f).hopL(23.5f, 0.5f).leftUntil { it.player.box.cy > 10f }.left(0.4f)
                    .leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().right(0.5f) },
            ),
            15 to listOf(
                { hopL(10.2f, 0.5f).leftTo(4.2f).hopR(6.2f, 0.5f).rightTo(15.4f)
                    .waitFor { it.group('h').oy > 5f }.waitFor { it.group('h').oy < 0.3f }.rightTo(27.1f)
                    .waitFor { it.group('b').visible }.hopR(27.4f, 0.5f).rightJump(0.5f).landRight().right(1f) },
            ),
            13 to listOf(
                { rightTo(8.3f).landRight().hopR(17.6f).rightTo(23.5f).rightTo(28.3f).landLeft().hopL(21.0f).leftTo(15.8f)
                    .waitFor { !it.group('f').visible }.waitFor { it.group('f').visible }.hopL(10.8f).left(2f) },
            ),
        )

        /** Plays the registered solution of level [n], [round] 1-based, and expects the win (for [World2Test]). */
        fun play(n: Int, round: Int = 1) =
            DesignRules.play(World2.levels[n - 1], round - 1, SOLUTIONS.getValue(n)[round - 1]).expect(WorldState.WON)
    }
}
