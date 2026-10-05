package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block B (levels 17-24, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first. They are
 * what an informed player does with the real physics, and they are shared by [World1DesignTest] (the guard rails),
 * [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsB {
    val solutions: Map<Int, List<Solution>> = mapOf(
        17 to listOf<Solution>(
            { hopR(5.8f).rightTo(15.0f).rightJump(0.35f).landRight().rightTo(19.9f).rightJump(0.35f).landRight()
                .rightJump(0.35f).landRight().rightTo(27.8f).leftTo(26.4f).leftJump(0.35f).landLeft().leftTo(2.0f) },
            { rightTo(9.6f).rightJump(0.35f).landRight().rightTo(15.0f).rightJump(0.35f).landRight().rightTo(19.9f).rightJump(0.35f).landRight()
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
                .rightKeyLeftTo(2.5f) },
        ),
        19 to listOf<Solution>(
            { hopR(7.9f).hopR(13.8f).rightTo(17.0f).rightJump(0.35f).landRight().rightTo(20.4f).rightJump(0.35f).landRight()
                .rightUntil { it.pads[0].down }.jump(0.14f).rightJump(0.3f).landRight()
                .waitFor { it.group('x').oy >= 9.9f }
                .leftUntil { it.group('y').oy > 0.5f }.waitFor { it.group('y').oy >= 7.9f }.leftJump(0.35f).landLeft().leftTo(2.5f) },
        ),
    )
}
