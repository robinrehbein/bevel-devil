package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block E (levels 41-48, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first.
 * Shared by [World1DesignTest] (the guard rails), [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsE {
    val solutions: Map<Int, List<Solution>> = mapOf(
        41 to listOf<Solution>(
            { leftTo(11.0f).waitFor { it.group('S').oy >= 12f }.leftTo(5.6f).jump(0.45f)
                .waitFor { it.group('T').oy >= 12f }.rightTo(20.0f).waitFor { it.group('U').oy >= 12f }
                .rightTo(26.0f).rightJump(0.5f).landRight().right(2f) },
        ),
    )
}
