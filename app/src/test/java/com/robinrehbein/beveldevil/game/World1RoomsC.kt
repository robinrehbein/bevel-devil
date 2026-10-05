package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block C (levels 25-32, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first. They are
 * what an informed player does with the real physics, and they are shared by [World1DesignTest] (the guard rails),
 * [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsC {
    val solutions: Map<Int, List<Solution>> = mapOf(
        25 to listOf<Solution>(
            { rightTo(25.2f).rightJump(0.35f).landRight().rightTo(28.4f)
                .leftTo(27.3f).waitFor { it.solidLeft('a') > 0.9f && it.solidLeft('b') > 0.9f }
                .leftTo(23.6f).leftJump(0.35f).landLeft().leftTo(5.6f).leftJump(0.35f).landLeft().leftTo(2.5f) },
        ),
    )
}
