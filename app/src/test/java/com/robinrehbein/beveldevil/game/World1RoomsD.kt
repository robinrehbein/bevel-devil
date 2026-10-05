package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block D (levels 33-40, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first.
 * Shared by [World1DesignTest] (the guard rails), [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsD {
    /** The jump-rope saw at column [x] (swinging 3.8 tiles at 7 tiles/s after [delay] s) is on its way up, so the lane is open for a crossing. */
    fun World.ropeUp(x: Float, delay: Float): Boolean = saws.firstOrNull { it.path?.points?.firstOrNull()?.first == x }
        ?.let { s -> ((time - s.t0 - delay) % 1.086f).let { it in 0.1f..0.3f } } ?: false

    val solutions: Map<Int, List<Solution>> = mapOf(
        33 to listOf<Solution>(
            { hopR(9.0f, 0.4f).rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightUntil(4f) { it.cracks.isNotEmpty() }
                .rightUntil(3f) { it.cracks.any { c -> c.fell } }.rightTo(roomX(1, 10.4f)).rightJump(0.4f).landRight()
                .rightJump(0.35f).landRight().right(3f) },
            { rightTo(14.0f).waitFor { it.ropeUp(17f, 1.1f) }.hopR(23.8f, 0.4f).rightUntil(4f) { it.cracks.any { c -> c.fell } }.hopR(roomX(1, 4.6f), 0.4f).rightTo(roomX(1, 10.4f)).rightJump(0.4f).landRight()
                .rightJump(0.35f).landRight().rightUntilSaw(4.5f).rightJump(0.5f).landRight().right(3f) },
        ),
    )
}
