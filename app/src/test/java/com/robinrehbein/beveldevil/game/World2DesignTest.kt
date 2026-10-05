package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertTrue
import org.junit.Test

/** Guard rails (docs/LEVEL_DESIGN_V2.md §9) for World 2; the tests are in [DesignTestBase], the rules in [DesignRules]. */
class World2DesignTest : DesignTestBase() {
    override val world = 2
    override val levels get() = World2.levels
    override val design = DESIGN
    override val rebuilt = REBUILT
    override val solutions = SOLUTIONS

    /**
     * §8, World 2: at least 8 levels with real portal routing (R3, R4). Switches (R1, R2, R4) are capped at 3 per act by
     * the rotation rule H12 ([DesignRules.tableRotationViolations]).
     */
    @Test
    fun portalRoutingIsEverywhere() {
        val routing = DesignRules.withBlock(design, "R3", "R4")
        assertTrue("routing levels $routing", routing.size >= 8)
    }

    companion object {
        /** §8, World 2 "Höllen-Rechenzentrum". A row without block ("–") and without ★ is a trap room. */
        val DESIGN: Map<Int, Design> = mapOf(
            // Act 1 "Handshake"
            1 to d("–", "U1", breather = true),
            2 to d("R3", "U5"),
            3 to d("R6", "U4"),
            4 to d("–", "U6"),
            5 to d("–", "U7"),
            6 to d("R4", "U11"),
            7 to d("–", "U2"),
            8 to d("–", "U1", breather = true),
            9 to d("–", "U9"),
            10 to d("R3", "U1"),
            11 to d("R1", "U15"),
            12 to d("–", "U1"),
            13 to d("R5", "U10"),
            14 to d("R3", "U11"),
            15 to d("R1", "U3"),
            16 to d("R3+R5", "U9+U2"),
            // Act 2 "Traffic"
            17 to d("R10", "U12"),
            18 to d("R5", "U13"),
            19 to d("–", "U9+U3"),
            20 to d("R8", "U13"),
            21 to d("R3", "U11"),
            22 to d("–", "U7"),
            23 to d("–", "U12"),
            24 to d("–", "U2+U7"),
            25 to d("–", "U1"),
            26 to d("–", "U8"),
            27 to d("R10", "U2"),
            28 to d("R3", "U13"),
            29 to d("–", "U7"),
            30 to d("R3+R5", "U11+U18"),
            31 to d("R8", "U3"),
            32 to d("R4+R3", "U12+U11"),
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

        /** Levels that follow the V2 rules; the rollout adds each block here (see [DesignRules]). */
        val REBUILT: Set<Int> = (1..40).toSet()

        /** Level number → bot solution per round (round 1 first). */
        val SOLUTIONS: Map<Int, List<Solution>> = mapOf<Int, List<Solution>>(
            1 to listOf(
                { hopR(10.2f).hopR(18.0f).right(1.5f) },
                { rightTo(17.5f).hopR(18.6f).right(1.5f) },
            ),
            2 to listOf(
                { hopR(15.5f).hopR(21.9f).rightJump(0.4f).landRight()
                    .rightUntil { it.player.box.cy < 11.5f }.leftUntil { it.player.box.cx < 20.4f }
                    .waitFor { w -> w.links.first { it.id == 'g' }.on }.leftUntil { it.player.box.cx < 10.2f }
                    .leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().left(1f) },
            ),
            3 to listOf(
                { hopR(7.2f).hopR(12.8f).hopR(17.2f).rightUntil { it.player.box.cy > 12.5f }.leftUntil { World2Rooms.sawAheadLeft(it, 4.4f) }
                    .leftJump(0.35f).landLeft().left(3f) },
            ),
            4 to listOf(
                { rightTo(3.0f).rightJump(0.4f).landRight().rightJump(0.4f).landRight().rightJump(0.4f).landRight()
                    .rightUntil { it.player.box.cx > 11.7f }.waitFor { it.group('A').oy > 6.5f }.waitFor { it.group('A').oy < 6.0f }
                    .rightUntil { it.player.box.cx > 17.7f }.waitFor { it.group('B').oy > 6.5f }.waitFor { it.group('B').oy < 6.0f }
                    .rightUntil { it.player.box.cx > 27.5f }.leftUntil { w -> w.group('G').let { g -> w.player.box.cx - (g.homeX + g.ox) in 0f..4f } }
                    .leftJump(0.35f).landLeft().left(3f) },
                { rightTo(3.0f).rightJump(0.4f).landRight().rightJump(0.4f).landRight().rightJump(0.4f).landRight()
                    .rightUntil { it.player.box.cx > 11.7f }.waitFor { it.group('A').oy > 6.5f }.waitFor { it.group('A').oy < 6.0f }
                    .rightUntil { it.player.box.cx > 17.7f }.waitFor { it.group('B').oy > 6.5f }.waitFor { it.group('B').oy < 6.0f }
                    .rightUntil { it.player.box.cx > 27.5f }.leftUntil { it.player.box.cx < 26.4f }
                    .waitFor { it.group('C').oy > 3.0f }.waitFor { it.group('C').oy < 2.5f }.waitFor { it.group('C').oy > 3.0f }.waitFor { it.group('C').oy < 2.5f }
                    .leftUntil { w -> w.group('G').let { g -> w.player.box.cx - (g.homeX + g.ox) in 0f..4f } }
                    .leftJump(0.35f).landLeft().left(3f) },
            ),
            5 to listOf(
                { rightUntil { World2Rooms.sawAhead(it, 4.4f) }.rightJump(0.35f).landRight().rightUntil { it.player.box.cy > 7f }
                    .leftUntil { World2Rooms.sawAheadLeft(it, 4.0f) }.leftJump(0.35f).landLeft().leftUntil { it.player.box.cy > 11f }
                    .rightUntil { World2Rooms.sawAhead(it, 4.4f) }.rightJump(0.35f).landRight().rightUntil { it.player.box.cx > 27.6f }.right(1f) },
            ),
            6 to listOf(
                { leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft()
                    .leftUntil { it.player.box.cx < 2.5f }.rightUntil { it.player.box.cx > 15.3f }.waitFor { w -> w.links[0].on }.rightUntil { it.player.box.cx > 17.5f }
                    .rightJump(0.4f).landRight().rightJump(0.4f).landRight().rightTo(25.4f).rightJump(0.4f).landRight().right(1f) },
            ),
            7 to listOf(
                { leftTo(29.0f).leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft()
                    .waitFor { it.group('a').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
                    .leftJump(0.4f).landLeft()
                    .leftUntil { it.player.box.cx < 9.5f && it.player.grounded }.waitFor { it.group('d').let { g -> g.mode == GroupMode.IDLE && g.oy > 2f } }
                    .waitFor { !it.group('d').visible }.leftUntil { it.player.box.cx < 2.6f }.left(1f) },
                { leftTo(29.0f).leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft()
                    .leftUntil { it.player.box.cx < 9.5f && it.player.grounded }
                    .waitFor { it.group('e').let { g -> g.mode == GroupMode.IDLE && g.oy > 2f } }.waitFor { !it.group('e').visible }
                    .leftUntil { it.player.box.cx < 5.2f }
                    .waitFor { !it.group('d').visible }.leftUntil { it.player.box.cx < 2.6f }.left(1f) },
            ),
            8 to listOf(
                { rightUntil { it.player.box.cx > 15.3f }.waitFor { it.group('a').oy > 3f }.waitFor { it.group('a').oy < 0.3f }.rightUntil { it.player.box.cx > 25.6f }.rightJump(0.35f).landRight().right(1f) },
            ),
            9 to listOf(
                { waitFor { it.swapped }.leftUntil { World2Rooms.sawAhead(it, 4.4f) }.leftJump(0.35f).landLeft()
                    .leftUntil { !it.swapped && it.player.grounded }.leftUntil { it.player.box.cx < 12.5f }.waitFor { World2Rooms.sawAheadLeft(it, 4.4f) }.leftJump(0.35f).landLeft().leftUntil { it.player.box.cx < 2.6f }.left(1f) },
            ),
            10 to listOf(
                { leftTo(27.5f).leftJump(0.35f).landLeft()
                    .leftUntil { it.player.box.cx < 19.4f }.leftJump(0.35f).landLeft()
                    .leftUntil { it.player.box.cx < 15.2f }.waitFor { w -> w.links.first { it.id == 'b' }.on }.wait(0.35f).left(0.5f)
                    .waitFor { it.player.grounded }
                    .leftUntil { it.player.box.cx < 15.0f }.waitFor { w -> w.links.first { it.id == 'c' }.on }.wait(0.35f).left(0.4f)
                    .leftUntil { it.player.box.cx < 5.45f }.leftJump(0.35f).landLeft().left(1f) },
            ),
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
            16 to listOf(
                { hopR(5.6f, 0.5f).right(0.85f).waitFor { it.swapped }.wait(0.4f).right(1.9f)
                    .hopL(24.4f, 0.5f).hopL(17.4f, 0.5f).hopL(12.4f, 0.5f).left(2.3f)
                    .right(0.3f).hopR(23.6f, 0.5f).right(1.5f) },
            ),
            13 to listOf(
                { rightTo(8.3f).landRight().hopR(18.6f).rightTo(23.5f).rightTo(28.3f).landLeft().hopL(22.0f).leftTo(15.8f)
                    .waitFor { !it.group('f').visible }.waitFor { it.group('f').visible }.hopL(10.8f).left(2f) },
            ),
            33 to listOf({ World2Rooms.l33(this) }, { World2Rooms.l33r2(this) }),
            34 to listOf({ World2Rooms.l34(this) }, { World2Rooms.l34r2(this) }),
            35 to listOf({ World2Rooms.l35(this) }),
            36 to listOf({ World2Rooms.l36(this) }),
            37 to listOf({ World2Rooms.l37(this) }),
            38 to listOf({ World2Rooms.l38(this) }),
            39 to listOf({ World2Rooms.l39(this) }),
            40 to listOf({ World2Rooms.l40(this) }),
            25 to listOf({ World2Rooms.l25(this) }, { World2Rooms.l25r2(this) }),
            26 to listOf({ World2Rooms.l26(this) }, { World2Rooms.l26r2(this) }),
            27 to listOf({ World2Rooms.l27(this) }),
            28 to listOf({ World2Rooms.l28(this) }),
            29 to listOf({ World2Rooms.l29(this) }, { World2Rooms.l29r2(this) }),
            30 to listOf({ World2Rooms.l30(this) }),
            31 to listOf({ World2Rooms.l31(this) }),
            32 to listOf({ World2Rooms.l32(this) }),
        ) +
            World2Rooms.solutions.mapValues { (_, s) -> listOf<Solution>({ s(this) }) } +
            mapOf(
                18 to listOf<Solution>({ World2Rooms.l18(this) }, { World2Rooms.l18r2(this) }),
                20 to listOf<Solution>({ World2Rooms.l20(this) }, { World2Rooms.l20r2(this) }),
            )

        /** Plays the registered solution of level [n], [round] 1-based, and expects the win (for [World2Test]). */
        fun play(n: Int, round: Int = 1) =
            DesignRules.play(World2.levels[n - 1], round - 1, SOLUTIONS.getValue(n)[round - 1]).expect(WorldState.WON)
    }
}
