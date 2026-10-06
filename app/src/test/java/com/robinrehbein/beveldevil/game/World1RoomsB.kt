package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block B (levels 17-24, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first. They are
 * what an informed player does with the real physics, and they are shared by [World1DesignTest] (the guard rails),
 * [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
/** Seconds the blinking group [id] stays solid from now, 0 while it is gone. */
fun World.solidLeft(id: Char): Float = group(id).let { g ->
    val k = g.blink
    if (!g.visible) 0f else if (k == null) 9f else k.on - k.cycle(time - g.blinkT0)
}

/** Seconds until beam [id] fires again (0 while lit, 5 if it stays dark for good or is not there yet). */
fun World.darkLeft(id: Char): Float {
    val b = beams.firstOrNull { it.laser.id == id } ?: return 0f
    if (b.lit) return 0f
    var d = 0f
    while (d < 5f) { if (b.laser.litAt(time + d - b.t0 - b.warm)) return d; d += 0.02f }
    return 5f
}

/** Beam [id] is dark and stays dark for the time a crossing takes. */
fun World.gateOpen(id: Char, margin: Float = 0.9f): Boolean = beams.any { it.laser.id == id } && darkLeft(id) > margin

object World1RoomsB {
    val solutions: Map<Int, List<Solution>> = mapOf(
        17 to listOf<Solution>(
            { hopR(5.8f).rightTo(15.0f).rightJump(0.35f).landRight().rightTo(19.9f).rightJump(0.35f).landRight()
                .rightJump(0.35f).landRight().rightTo(27.8f).leftTo(26.4f).leftJump(0.35f).landLeft().leftTo(2.0f) },
            { rightTo(15.0f).rightJump(0.35f).landRight().rightTo(19.9f).rightJump(0.35f).landRight()
                .rightJump(0.35f).landRight().rightTo(27.8f).leftTo(26.4f).leftJump(0.35f).landLeft().leftTo(2.0f) },
        ),
        18 to listOf<Solution>(
            { rightUntilSaw(4.5f).rightJump(0.35f).landRight().rightUntilSaw(4.5f).rightJump(0.35f).landRight().landRight()
                .leftTo(24.6f).leftJump(0.35f).landLeft()
                .leftUntil { w -> w.saws.any { it.y > 12f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 4.5f } }.leftJump(0.35f).landLeft()
                .leftTo(9.0f).leftJump(0.35f).landLeft().leftTo(2.5f) },
            { rightUntil { w -> w.saws.any { it.y < 9f && it.vx < 0f && it.x > w.player.box.cx && it.x - w.player.box.cx <= 6.0f } }.rightJump(0.35f).landRight()
                .rightUntil { w -> w.saws.any { it.y < 9f && it.vx > 0f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 3.4f } }.rightJump(0.35f).landRight()
                .rightUntil { w -> w.swapped && w.saws.any { it.y > 12f && it.vx < -10f && it.x > w.player.box.cx && it.x - w.player.box.cx <= 3.4f } }.rightJump(0.35f).landRight()
                .rightUntil { w -> w.saws.any { it.y > 12f && it.vx > 0f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 4.5f } }.rightJump(0.35f).landRight()
                .rightUntil { !it.swapped }.leftTo(2.5f) },
        ),
        19 to listOf<Solution>(
            { hopR(7.9f).hopR(13.8f).rightTo(17.0f).rightJump(0.35f).landRight().rightTo(20.4f).rightJump(0.35f).landRight()
                .rightUntil { it.pads[0].down }.jump(0.14f).rightJump(0.3f).landRight()
                .waitFor { it.group('x').oy >= 9.9f }
                .leftUntil { it.group('y').oy > 0.5f }.waitFor { it.group('y').oy >= 7.9f }.leftJump(0.35f).landLeft().leftTo(2.5f) },
        ),
        20 to listOf<Solution>(
            { hopR(5.8f, 0.3f).hopR(10.8f, 0.3f).hopR(15.8f, 0.3f).hopR(20.8f, 0.3f)
                .rightJump(0.35f).landRight().rightJump(0.35f).landRight()
                .leftTo(28.6f).leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().leftJump(0.35f).landLeft()
                .leftTo(14.8f).leftJump(0.35f).landLeft().leftTo(5.5f) },
        ),
        21 to listOf<Solution>(
            { hopR(7.4f).hopR(13.6f).rightTo(20.2f).rightJump(0.35f).landRight().rightTo(25.6f).rightJump(0.3f).landRight()
                .waitFor { it.group('s').oy >= 11.5f }.leftTo(28.4f).leftJump(0.35f).landLeft().leftTo(22.0f).leftJump(0.35f).landLeft().leftTo(18.0f).leftJump(0.35f).landLeft().leftTo(11.6f) },
            { rightTo(25.6f).rightJump(0.3f).landRight()
                .waitFor { it.group('s').oy >= 11.5f }.leftJump(0.3f).landLeft().leftTo(22.0f).leftJump(0.35f).landLeft()
                .leftTo(18.0f).leftJump(0.35f).landLeft().leftTo(11.6f) },
        ),
        22 to listOf<Solution>(
            { rightTo(10.6f).waitFor { it.gateOpen('A') }.rightTo(25.5f).rightTo(28.0f).landRight()
                .leftTo(23.5f).waitFor { it.gateOpen('B') }.leftTo(12.6f) },
        ),
        23 to listOf<Solution>(
            { rightTo(5.0f).waitFor { w -> w.saws.any { it.path != null && it.x < 9f && it.y < 2.8f } }.rightTo(15.0f).landRight().landRight()
                .leftUntil { w -> w.saws.any { it.vx > 0f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 4.5f } }.leftJump(0.35f).landLeft().leftTo(5.0f).landLeft()
                .rightTo(6.0f).waitFor { w -> w.saws.any { it.path != null && it.x in 8.5f..9.5f && it.y < 11.0f } }.rightTo(13.5f)
                .waitFor { w -> w.saws.any { it.path != null && it.x > 15f && it.y < 11.0f } }.rightTo(26.0f) },
        ),
        24 to listOf<Solution>(
            { hopR(6.8f, 0.3f).rightTo(17.3f).wait(0.4f).hopR(17.6f, 0.3f).rightTo(24.4f).rightJump(0.35f).landRight().rightJump(0.35f).landRight()
                .leftTo(28.6f).leftJump(0.35f).landLeft()
                .leftUntil { w -> w.group('F').let { g -> g.homeX + g.ox < w.player.box.cx && w.player.box.cx - (g.homeX + g.ox) <= 3.4f } }.leftJump(0.35f).landLeft().leftTo(14.6f) },
            { hopR(6.8f, 0.3f).rightTo(17.3f).wait(0.4f).hopR(17.6f, 0.3f).rightTo(25.0f).rightJump(0.3f).landRight().waitFor { it.group('l').oy <= -4f }.leftJump(0.35f).landLeft()
                .leftUntil { w -> w.group('G').let { g -> g.homeX + g.ox < w.player.box.cx && w.player.box.cx - (g.homeX + g.ox) <= 3.4f } }.leftJump(0.35f).landLeft()
                .leftUntil { w -> w.group('F').let { g -> g.homeX + g.ox < w.player.box.cx && w.player.box.cx - (g.homeX + g.ox) <= 3.4f } }.leftJump(0.35f).landLeft().leftTo(14.6f) },
        ),
    )
}
