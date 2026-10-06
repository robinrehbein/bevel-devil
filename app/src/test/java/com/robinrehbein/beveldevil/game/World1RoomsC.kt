package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block C (levels 25-32, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first. They are
 * what an informed player does with the real physics, and they are shared by [World1DesignTest] (the guard rails),
 * [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsC {
    /** The middle of group [id] is within [d] tiles of the player (sideways). */
    fun World.near(id: Char, d: Float): Boolean = group(id).let { g -> kotlin.math.abs(g.homeX + g.ox - player.box.cx) <= d }

    /** The jump-rope saw at column [x] (swinging 3.8 tiles at 7 tiles/s after [delay] s) is on its way up, so the lane is open for a crossing. */
    fun World.ropeUp(x: Float, delay: Float): Boolean = saws.firstOrNull { it.path?.points?.firstOrNull()?.first == x }
        ?.let { s -> ((time - s.t0 - delay) % 1.086f).let { it in 0.1f..0.3f } } ?: false

    val solutions: Map<Int, List<Solution>> = mapOf(
        25 to listOf<Solution>(
            { rightTo(25.2f).rightJump(0.35f).landRight().rightTo(28.4f)
                .leftTo(27.3f).waitFor { it.solidLeft('a') > 0.9f && it.solidLeft('b') > 0.9f }
                .leftTo(23.6f).leftJump(0.35f).landLeft().leftTo(5.6f).leftJump(0.35f).landLeft().leftTo(2.5f) },
        ),
        26 to listOf<Solution>(
            { hopR(2.4f, 0.5f).hopR(8.4f, 0.5f).hopR(13.4f, 0.5f).hopR(18.2f, 0.5f)
                .rightTo(25.0f).rightJump(0.5f).landRight().right(0.5f).left(0.2f)
                .leftJump(0.5f).landLeft()
                .leftUntil { it.near('T', 3.4f) }.leftJump(0.5f).landLeft().leftTo(2.5f) },
        ),
        27 to listOf<Solution>(
            { hopR(7.4f, 0.5f).rightTo(16.6f).rightJump(0.4f).landRight().right(3f) },
        ),
        28 to listOf<Solution>(
            { rightTo(5.2f).rightJump(0.5f).landRight()
                .waitFor { w -> w.saws.any { it.path?.loop == true && it.x < 6.5f && it.y < 12f } }
                .rightJump(0.4f).landRight()
                .rightTo(14.0f).waitFor { it.ropeUp(17f, 0.9f) }.rightTo(19.5f).rightJump(0.5f).landRight()
                .rightTo(23.2f).rightJump(0.5f).landRight().right(0.5f).left(0.2f)
                .leftJump(0.5f).landLeft()
                .leftUntil { w -> w.saws.any { it.y in 7f..9.2f && w.player.box.cx - it.x in 0f..4.6f } }.leftJump(0.35f).landLeft()
                .leftTo(2.5f) },
            { rightTo(4.4f).waitFor { w -> w.saws.any { it.path?.loop == true && it.x > 9.5f } }
                .rightTo(5.4f).rightJump(0.5f).landRight()
                .waitFor { w -> w.saws.any { it.path?.loop == true && it.y > 14f && it.x < 9f } }
                .rightJump(0.4f).landRight()
                .rightTo(14.0f).waitFor { it.gateOpen('A') }.rightTo(19.5f).rightJump(0.5f).landRight()
                .rightTo(23.2f).rightJump(0.5f).landRight().right(0.5f).left(0.2f)
                .leftJump(0.5f).landLeft()
                .leftUntil { w -> w.saws.any { it.y in 7f..9.2f && w.player.box.cx - it.x in 0f..4.6f } }.leftJump(0.35f).landLeft()
                .leftTo(16.5f).leftTo(15.0f).leftJump(0.5f).landLeft()
                .leftUntil { w -> w.saws.any { it.y in 7f..9.2f && w.player.box.cx - it.x in 0f..4.6f } }.leftJump(0.35f).landLeft()
                .leftTo(2.5f) },
        ),
        29 to listOf<Solution>(
            { rightTo(18.8f).rightTo(19.4f).rightJump(0.5f).landRight().rightTo(25.2f).rightJump(0.5f).landRight().right(2f) },
        ),
        30 to listOf<Solution>(
            { rightTo(8.6f).waitFor { it.group('a').oy >= 11.9f }.rightJump(0.5f).landRight()
                .waitFor { it.group('c').oy >= 9.9f }.rightJump(0.5f).landRight()
                .waitFor { it.group('f').oy >= 7.9f }.rightJump(0.5f).landRight()
                .leftKeyRightTo(21.7f).leftJump(0.5f).landLeft().leftKeyRightTo(25.6f).leftJump(0.35f).landLeft().left(1.5f) },
        ),
        31 to listOf<Solution>(
            { leftTo(30.0f).leftJump(0.5f).landLeft().leftTo(27.6f).leftJump(0.5f).landLeft()
                .leftTo(25.6f).leftJump(0.5f).landLeft().leftTo(23.6f).leftJump(0.5f).landLeft()
                .leftTo(18.2f).waitFor { w -> w.saws.any { it.path?.points?.firstOrNull()?.first == 16.5f && it.y > 8.2f } }.leftJump(0.4f).landLeft()
                .leftTo(14.0f).leftJump(0.35f).landLeft()
                .leftTo(8.0f).waitFor { w -> w.saws.any { it.path?.points?.firstOrNull()?.first == 6f && it.y > 10.5f } }.left(3f) },
        ),
        32 to listOf<Solution>(
            { rightTo(12.4f).waitFor { it.solidLeft('b') > 1.0f }.rightTo(20.5f)
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightUntil(4f) { it.cracks.isNotEmpty() }
                .rightUntil(3f) { it.cracks.any { c -> c.fell } }.rightTo(roomX(1, 5.0f)).waitFor { it.solidLeft('d') > 1.0f }
                .rightTo(roomX(1, 13.6f)).waitFor { it.solidLeft('c') > 1.0f }
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().right(5f) },
        ),
    )
}
