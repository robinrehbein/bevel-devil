package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block E (levels 41-48, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first.
 * Shared by [World1DesignTest] (the guard rails), [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsE {
    /** Seconds the blinking group [id] stays solid from now: 0 while it is gone (or does not blink yet). */
    fun World.solidLeft(id: Char): Float = group(id).let { g ->
        val k = g.blink ?: return 0f
        if (g.visible) k.on - k.cycle(time - g.blinkT0) else 0f
    }

    /** The jump-rope saw at column [x] (swinging 3.8 tiles at 7 tiles/s after [delay] s) is [ahead] s before the phase where it is on its way up. */
    fun World.ropeUp(x: Float, delay: Float, ahead: Float = 0f): Boolean = saws.firstOrNull { it.path?.points?.firstOrNull()?.first == x }
        ?.let { s -> ((time + ahead - s.t0 - delay) % 1.086f).let { it in 0.1f..0.3f } } ?: false

    val solutions: Map<Int, List<Solution>> = mapOf(
        45 to listOf<Solution>(
            { hopR(7.4f, 0.5f).hopR(12.4f, 0.5f).hopR(17.4f, 0.5f).rightJump(0.45f).landRight().rightTo(27.0f).rightJump(0.45f).landRight()
                .right(0.5f).leftJump(0.5f).landLeft().leftTo(18.0f).leftJump(0.5f).landLeft().leftTo(11.0f).leftJump(0.5f).landLeft().left(5f) },
        ),
        44 to listOf<Solution>(
            { rightTo(8.2f).waitFor { it.solidLeft('q') > 1.0f }
                .rightTo(13.2f).waitFor { it.solidLeft('r') > 1.0f }.rightTo(18.2f).waitFor { it.solidLeft('s') > 1.0f }.rightTo(23.0f).shake()
                .rightTo(25.2f).rightJump(0.5f).landRight().rightJump(0.45f).landRight().rightJump(0.45f).landRight().right(2f) },
        ),
        43 to listOf<Solution>(
            { rightTo(5.4f).waitFor { it.ropeUp(8f, 0.9f) }.rightTo(11.5f).rightUntil { it.pads[0].down }.rightTo(14.0f).waitFor { it.ropeUp(19f, 0.2f, 0.3f) }
                .rightTo(21.0f).rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightJump(0.45f).landRight().rightJump(0.45f).landRight().rightTo(30.4f).leftJump(0.5f).landLeft()
                .leftUntil { w -> w.saws.any { it.path == null && it.y in 7f..9.2f && it.x < w.player.box.cx && w.player.box.cx - it.x in 0f..4.2f } }
                .leftJump(0.5f).landLeft().left(4f) },
        ),
        41 to listOf<Solution>(
            { leftTo(11.0f).waitFor { it.group('S').oy >= 12f }.leftTo(5.6f).jump(0.45f)
                .waitFor { it.group('T').oy >= 12f }.rightTo(20.0f).waitFor { it.group('U').oy >= 12f }
                .rightTo(26.0f).rightJump(0.5f).landRight().right(2f) },
        ),
        42 to listOf<Solution>(
            { hopR(8.0f, 0.5f).hopR(14.6f, 0.35f).rightUntil { it.player.box.b > 7f }.waitFor { it.player.grounded }
                .hopL(21.6f, 0.35f).leftUntil { it.player.box.b > 12f }.waitFor { it.player.grounded }
                .hopR(11.0f, 0.5f).hopR(17.0f, 0.5f).hopR(22.4f, 0.35f).right(3f) },
            { hopR(8.0f, 0.5f).hopR(14.6f, 0.35f).rightUntil { it.player.box.b > 7f }.waitFor { it.player.grounded }
                .hopL(21.6f, 0.35f).leftUntil { it.player.box.b > 12f }.waitFor { it.player.grounded }
                .rightTo(9.0f).rightJump(0.45f).landRight().rightTo(15.4f).waitFor { it.solidLeft('q') > 1.0f }
                .rightJump(0.45f).landRight().waitFor { it.solidLeft('r') > 1.0f }
                .rightTo(21.0f).rightJump(0.45f).landRight().right(3f) },
        ),
    )
}
