package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block D (levels 33-40, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first.
 * Shared by [World1DesignTest] (the guard rails), [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsD {
    /** The jump-rope saw at column [x] (swinging 3.8 tiles at 7 tiles/s after [delay] s) is on its way up, so the lane is open for a crossing. */
    fun World.ropeUp(x: Float, delay: Float): Boolean = saws.firstOrNull { it.path?.points?.firstOrNull()?.first == x }
        ?.let { s -> ((time - s.t0 - delay) % 1.086f).let { it in 0.1f..0.3f } } ?: false

    /** The [i]th piece of the frame that has cracked off has come down to rest [oy] tiles below where it hung. */
    fun World.pieceLanded(i: Int, oy: Float): Boolean = cracks.getOrNull(i)?.group?.oy?.let { it >= oy - 0.05f } == true

    val solutions: Map<Int, List<Solution>> = mapOf(
        // 33: hop the strip, hop the oncoming saw, through the breach, board the ferry and hop off it at once; rematch: the rope, the strips, the ferry
        33 to listOf<Solution>(
            { hopR(9.0f, 0.4f).rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightUntil(4f) { it.cracks.isNotEmpty() }
                .rightUntil(3f) { it.cracks.any { c -> c.fell } }.rightTo(roomX(1, 10.4f)).rightJump(0.4f).landRight()
                .rightJump(0.35f).landRight().right(3f) },
            { rightTo(14.0f).waitFor { it.ropeUp(17f, 1.1f) }.hopR(23.8f, 0.4f).rightUntil(4f) { it.cracks.any { c -> c.fell } }.hopR(roomX(1, 4.6f), 0.4f).rightTo(roomX(1, 10.4f)).rightJump(0.4f).landRight()
                .rightJump(0.35f).landRight().rightUntilSaw(4.5f).rightJump(0.5f).landRight().right(3f) },
        ),
        // 34: at the wall pause for real (the pit opens behind it), hop it and the second pit, up the stairs and back along the upper floor,
        // let the slab land and hop it
        34 to listOf<Solution>(
            { rightTo(7.0f).tapPause().pauseResume().hopR(8.6f, 0.4f).hopR(15.4f, 0.4f).rightTo(20.4f).rightJump(0.5f).landRight()
                .rightTo(25.0f).rightJump(0.5f).landRight().leftTo(26.8f).leftJump(0.5f).landLeft()
                .waitFor { it.group('s').oy >= 6.9f }.leftJump(0.4f).landLeft().left(3f) },
        ),
        // 35: up the three platforms (the door jumps up as you start, and is gone again when you land), off the left end and back along the ground
        35 to listOf<Solution>(
            { rightTo(15.6f).rightJump(0.5f).landRight().rightTo(19.9f).rightJump(0.5f).landRight().rightTo(23.2f).rightJump(0.5f).landRight()
                .left(3f) },
        ),
        // 36: let the first piece land, climb on it and hop off its far end over the spikes, up the stairs, back along the upper floor,
        // let the second piece land and hop it
        36 to listOf<Solution>(
            { rightTo(6.5f).waitFor { it.pieceLanded(0, 5f) }.rightTo(7.2f).rightJump(0.3f).landRight().rightTo(10.6f).rightJump(0.5f).landRight()
                .rightTo(19.3f).rightJump(0.5f).landRight().right(0.25f).rightJump(0.5f).landRight()
                .leftTo(25.8f).leftJump(0.5f).landLeft().waitFor { it.pieceLanded(1, 8f) }.leftTo(20.8f).leftJump(0.4f).landLeft()
                .leftTo(13.4f).leftJump(0.4f).landLeft().left(4f) },
        ),
        // 37: run ahead of the deleted floor, hop up the first step (it is deleted behind you) onto the second, back along the upper floor;
        // rematch: hop the stones over the pits that were opened ahead, without staying on any
        37 to listOf<Solution>(
            { rightTo(19.3f).rightJump(0.5f).landRight().right(0.25f).rightJump(0.5f).landRight()
                .leftTo(25.8f).leftJump(0.5f).landLeft().left(4f) },
            { rightTo(4.0f).rightJump(0.4f).landRight().rightTo(9.9f).rightJump(0.4f).landRight().rightTo(14.9f).rightJump(0.4f).landRight()
                .rightJump(0.5f).landRight().right(0.25f).rightJump(0.5f).landRight()
                .leftTo(25.8f).leftJump(0.5f).landLeft().left(4f) },
        ),
    )
}
