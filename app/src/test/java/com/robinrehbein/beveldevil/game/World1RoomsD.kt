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
                .rightUntil(3f) { it.cracks.any { c -> c.fell } }.rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightTo(roomX(1, 9.9f)).rightJump(0.4f).landRight()
                .rightJump(0.35f).landRight().right(3f) },
            { rightTo(16.6f).waitFor { it.ropeUp(19.5f, 1.4f) }.hopR(23.8f, 0.4f).rightUntil(4f) { it.cracks.any { c -> c.fell } }.hopR(roomX(1, 4.6f), 0.4f).rightTo(roomX(1, 10.4f)).rightJump(0.4f).landRight()
                .rightJump(0.35f).landRight().rightUntilSaw(4.5f).rightJump(0.5f).landRight().right(3f) },
        ),
        // 34: at the wall pause for real (the pit opens behind it), hop it and the second pit, up the stairs and back along the upper floor,
        // let the slab land and hop it
        34 to listOf<Solution>(
            { rightTo(7.0f).tapPause().pauseResume().hopR(8.6f, 0.4f).rightJump(0.4f).landRight().rightTo(20.4f).rightJump(0.5f).landRight()
                .rightTo(25.0f).rightJump(0.5f).landRight().leftTo(26.8f).leftJump(0.5f).landLeft().rightTo(25.5f)
                .waitFor { it.pieceLanded(0, 8f) }.leftJump(0.35f).landLeft().left(3f) },
        ),
        // 35: up the three platforms (the door jumps up as you start, and is gone again when you land), off the left end and back along the ground
        35 to listOf<Solution>(
            { rightTo(15.6f).rightJump(0.5f).landRight().rightTo(19.9f).rightJump(0.5f).landRight().rightTo(23.2f).rightJump(0.5f).landRight()
                .left(3f) },
        ),
        // 36: let the rope come down and go up again, run under it, wait for the first piece to land and climb on it and up to the ledge, let the second piece land,
        // hop it; the curator puts you back a second, so hop it again
        36 to listOf<Solution>(
            { rightTo(4.2f).waitFor { World1RoomsE.run { it.ropeUp(7f, 0f, from = 0.643f, to = 0.843f) } }.rightTo(11.5f).waitFor { it.pieceLanded(0, 14f) }
                .hopR(12.2f, 0.4f).rightTo(15.4f).rightJump(0.5f).landRight().waitFor { it.pieceLanded(1, 11f) }
                .hopR(24.6f, 0.4f).rightUntil(2f) { it.player.box.cx < 24f }.hopR(24.6f, 0.4f).rightTo(29.6f) },
        ),
        // 40: the keys swap at the first steps: wait for the beam to go dark, cross, the keys come back at the end of the shelf, drop down the
        // shaft, run back along the ground floor; the keys swap again halfway (reboot) and the pit is hopped with them
        40 to listOf<Solution>(
            { rightUntil { it.swapped }.leftKeyRightTo(12.0f).waitFor { w -> !w.beams.any { it.laser.id == 'A' && it.lit } }.leftKeyRightTo(21.0f)
                .leftUntil(3f) { !it.swapped }.rightUntil(3f) { it.player.box.b > 11f }.landRight().leftUntil { it.swapped }
                .hopSL(18.6f, 0.4f).rightKeyLeftTo(12.6f).right(1f) },
        ),
        // 39: let the ceiling tile land and hop it, up the stairs to the upper floor, hop the piece that gives way, board the shelf, tilt and ride it
        // across, jump off its end over the bank that gives way onto the ledge with the door
        39 to listOf<Solution>(
            { rightTo(3.5f).waitFor { it.group('s').oy >= 12.9f }.rightTo(4.0f).rightJump(0.5f).landRight().rightJump(0.45f).landRight().rightJump(0.45f).landRight()
                .rightJump(0.45f).landRight().rightJump(0.45f).landRight()
                .rightUntil { it.player.box.cx > 17.0f }.waitFor { it.group('h').visible }.rightUntil { it.player.box.cx > 23.0f }
                .tilt(1f).waitFor { it.group('a').ox >= 2.9f }.rightUntil { it.player.box.cx > 26.0f }.rightJump(0.5f).landRight().right(3f) },
        ),
        // 38: hop the oncoming saw, let the picture turn, flee the second saw to the stairs with the mirrored keys, climb, the picture is
        // back: leave step 2 to the upper floor, let the rope go up, hop home
        38 to listOf<Solution>(
            { leftUntil { w -> w.saws.any { it.x < w.player.box.cx && w.player.box.cx - it.x <= 4.5f } }.leftJump(0.5f).landLeft().waitFor { it.viewTurn() >= 0.5f }
                .hopSL(9.6f, 0.5f).rightKeyLeftTo(7.0f).rightJump(0.5f).landRight()
                .waitFor { it.viewTurn() < 0.5f }.rightTo(2.1f).rightJump(0.5f).landRight().waitFor { it.ropeUp(18f, 0.35f) }.right(3f) },
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
