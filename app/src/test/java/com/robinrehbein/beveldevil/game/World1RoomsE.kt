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
    fun World.ropeUp(x: Float, delay: Float, ahead: Float = 0f, from: Float = 0.1f, to: Float = 0.3f): Boolean = saws.firstOrNull { it.path?.points?.firstOrNull()?.first == x }
        ?.let { s -> ((time + ahead - s.t0 - delay) % 1.086f).let { it in from..to } } ?: false

    /** Seconds until laser [id] fires next, 0 while it is lit (or does not exist yet): wait for a long dark stretch before crossing a fence. */
    fun World.darkFor(id: Char): Float {
        val b = beams.firstOrNull { it.laser.id == id } ?: return 0f
        if (b.lit) return 0f
        var dt = 0f
        while (dt < 4f) { if (b.laser.litAt(time + dt - b.t0)) return dt; dt += 0.02f }
        return 4f
    }

    val solutions: Map<Int, List<Solution>> = mapOf(
        48 to listOf<Solution>(
            { hopR(2.5f, 0.5f).hopR(4.7f, 0.5f).rightTo(8.4f).waitFor { it.group('S').oy >= 7.5f }.rightTo(11.8f)
                .waitFor { it.ropeUp(14f, 0.9f) }.rightUntil { it.pads[0].down }.right(0.6f)
                .waitFor { it.group('V').oy >= 11.5f }.rightTo(28.6f).right(1.5f)
                .rightTo(37.5f).waitFor { it.ropeUp(41f, 0.9f) }.rightTo(45.0f).waitFor { it.darkFor('B') > 1.1f }.rightTo(51.5f)
                .waitFor { it.group('W').oy >= 11.5f }.rightTo(61.0f).right(2f) },
            { hopR(2.5f, 0.5f).hopR(4.7f, 0.5f).rightTo(8.4f).waitFor { it.group('S').oy >= 7.5f }.rightTo(11.8f)
                .waitFor { it.ropeUp(14f, 0.9f) }.rightUntil { it.pads[0].down }.right(0.6f)
                .waitFor { it.group('V').oy >= 11.5f }.rightTo(22.2f).rightJump(0.45f).landRight().rightTo(28.6f).right(1.5f)
                .rightTo(37.5f).waitFor { it.ropeUp(40f, 0.9f) }.rightTo(44.0f).waitFor { it.darkFor('B') > 1.1f }.rightTo(49.2f)
                .waitFor { it.group('W').oy >= 11.5f }.rightTo(55.0f).waitFor { it.darkFor('C') > 1.1f }.rightTo(61.0f).right(2f) },
        ),
        47 to listOf<Solution>(
            { leftTo(12.4f).leftJump(0.45f).landLeft().leftJump(0.45f).landLeft().leftJump(0.45f).landLeft().leftTo(5.9f).jump(0.4f)
                .rightTo(14.0f).waitFor { it.group('g').oy <= -1.2f }.rightTo(17.0f).rightJump(0.45f).landRight().rightJump(0.45f).landRight().rightTo(22.2f).jump(0.4f).waitFor { it.player.grounded }
                .rightTo(23.2f).waitFor { it.group('h').oy <= -1.2f }.rightTo(25.0f).right(2f) },
            { leftTo(12.4f).leftJump(0.45f).landLeft().leftJump(0.45f).landLeft().leftJump(0.45f).landLeft().leftTo(5.9f).jump(0.4f)
                .rightTo(14.0f).waitFor { it.group('g').oy <= -1.2f }.rightTo(17.0f).rightJump(0.45f).landRight().rightJump(0.45f).landRight().rightTo(21.2f).rightJump(0.45f).landRight()
                .waitFor { it.group('h').oy <= -1.2f }.rightTo(25.0f).right(2f) },
        ),
        46 to listOf<Solution>(
            { rightUntil { it.pads[0].down }.right(0.6f).waitFor { it.darkFor('A') > 1.1f }
                .leftUntil { w -> w.saws.any { it.path == null && it.x < w.player.box.cx && w.player.box.cx - it.x in 0f..5.7f } }
                .leftJump(0.5f).landLeft().leftTo(14.5f).waitFor { it.ropeUp(11f, 0.9f, from = 0.0f, to = 0.2f) }.leftTo(7.0f).waitFor { it.darkFor('B') > 1.1f }.left(5f) },
            { rightTo(22.2f).rightJump(0.45f).landRight().rightJump(0.45f).landRight().rightUntil { it.pads[0].down }.right(0.5f)
                .waitFor { it.darkFor('A') > 1.1f }.leftTo(14.5f).waitFor { it.darkFor('C') > 1.1f }.leftTo(7.0f).waitFor { it.darkFor('B') > 1.1f }.left(5f) },
        ),
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
