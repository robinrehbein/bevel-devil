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
    )
}
