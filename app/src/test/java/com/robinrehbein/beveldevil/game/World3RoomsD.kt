package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block D of World 3 (levels 25-32): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsD {
    /** The x of the first piece of group [id] (home plus offset). */
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    /** Group [id] has fallen and lies still, [low] tiles or more below where it hung. */
    private fun landed(w: World, id: Char, low: Float = 1.5f): Boolean = w.group(id).let { it.mode == GroupMode.IDLE && it.oy > low }

    private fun sawsAhead(w: World, x: Float, hi: Float = 0f) = w.saws.any { it.vx < 0f && it.x > x && it.y > hi }

    val solutions: Map<Int, List<Solution>> = mapOf(
        32 to listOf<Solution>(
            { rightTo(2.6f).rightJump(0.4f).landRight().rightTo(5.0f).rightJump(0.4f).landRight()
                .rightTo(12.6f).waitFor { landed(it, 'r') }.rightJump(0.5f).landRight().rightTo(28.6f)
                .rightTo(roomX(1, 14.8f)).waitFor { landed(it, 'q') }
                .rightJump(0.45f).landRight().rightTo(roomX(1, 28.6f))
                .rightTo(roomX(2, 8.9f)).rightJump(0.5f).landRight()
                .rightTo(roomX(2, 19.8f)).waitFor { landed(it, 'z') }.rightJump(0.5f).landRight()
                .rightTo(roomX(2, 29.0f)).right(1f) },
            { rightTo(8.6f).waitCooled('c', 0.05f).waitFor { landed(it, 'r') }.rightTo(15.6f).rightJump(0.5f).landRight()
                .rightTo(28.6f).rightTo(roomX(1, 4.9f)).rightJump(0.3f).landRight().rightTo(roomX(1, 9.4f)).waitFor { landed(it, 'q') }
                .rightJump(0.5f).landRight().rightTo(roomX(1, 28.6f))
                .rightTo(roomX(2, 8.9f)).rightJump(0.5f).landRight()
                .rightTo(roomX(2, 16.4f)).rightJump(0.5f).landRight()
                .rightTo(roomX(2, 29.0f)).right(1f) },
        ),
        31 to listOf<Solution>(
            { rightTo(9.8f).waitCooled('c', 0.05f)
                .rightTo(25.4f).rightJump(0.5f).landRight()
                .leftTo(27.8f).leftJump(0.5f).landLeft()
                .leftTo(24.0f).leftJump(0.5f).landLeft().waitCooled('d', 0.05f)
                .leftUntil { it.player.grounded && it.player.box.b > 10.5f }
                .leftTo(16.6f).leftJump(0.5f).landLeft().left(0.5f) },
        ),
        30 to listOf<Solution>(
            // climb on (the second step sinks), hop the pin on the shelf, let the test pattern sink (the pin behind is slow), walk to the door
            { rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight()
                .rightUntil { w -> w.group('Q').let { it.homeX + it.ox - w.player.box.cx < 3.2f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .leftTo(21.2f).waitFor { !it.group('S').visible }.leftTo(11.4f).left(0.5f) },
            // rematch: the first step sinks, hop the pin on the shelf, hop the real spikes at once (the floor before them heats up, the
            // pin behind does not wait)
            { rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight()
                .rightUntil { w -> w.group('Q').let { it.homeX + it.ox - w.player.box.cx < 3.2f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .leftTo(23.8f).leftJump(0.6f).landLeft().leftTo(11.4f).left(0.5f) },
        ),
        29 to listOf<Solution>(
            // over the floor heating to the hatch pad, straight up through the hatch, back left over the radiator, over the
            // thermostat pad into the wall it opens
            { rightTo(24.0f).right(0.7f).leftJump(0.3f).landLeft()
                .leftTo(2.4f).left(0.5f) },
        ),
        28 to listOf<Solution>(
            { rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.b > 12.5f }
                .rightTo(12.4f).rightUntil { it.player.grounded && it.player.box.b > 15.5f }
                .rightTo(16.2f).rightJump(0.3f).landRight()
                .waitFor { w -> w.saws.none { it.vx < 0f && it.x > 17f } }
                .rightJump(0.55f).landRight()
                .rightTo(20.2f).rightUntil { it.player.grounded && it.player.box.b > 15.5f }
                .rightTo(24.2f).rightJump(0.3f).landRight()
                .waitFor { w -> w.saws.none { it.vx < 0f && it.x > 25f } }
                .rightJump(0.55f).landRight().right(1.5f) },
            // rematch: no sitting down; hop the narrowed dips in the air, with each high blade let past first, then up the steps
            { rightTo(4.4f).rightUntil { it.player.grounded && it.player.box.b > 12.5f }
                .rightTo(10.4f).waitFor { w -> w.saws.none { it.y < 11.5f && it.x > w.player.box.cx - 2.5f } }
                .rightTo(12.0f).rightJump(0.55f).landRight()
                .rightTo(18.2f).waitFor { w -> w.saws.none { it.y < 11.5f && it.x > w.player.box.cx - 2.5f } }
                .rightTo(20.0f).rightJump(0.55f).landRight()
                .rightTo(24.6f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight().right(1.0f) },
        ),
        27 to listOf<Solution>(
            { rightTo(4.5f).waitFor { it.circuits['a']?.powered == true }
                .rightTo(21.9f).rightJump(0.55f).landRight().right(1.5f) },
        ),
        26 to listOf<Solution>(
            // hop the tripwire, run through the landing light, off the block before the scan, hop the hot step, wait out the cable
            { rightTo(10.4f).rightJump(0.55f).landRight()
                .wait(0.1f).waitFor { w -> w.beams.none { it.laser.id == 'b' && it.on } }
                .rightTo(23.4f).rightJump(0.5f).landRight()
                .leftTo(25.8f).leftJump(0.5f).landLeft()
                .leftJump(0.5f).landLeft()
                .leftTo(15.4f).waitFor { w -> w.time > 1f && !w.beams.first { it.laser.id == 'c' }.on }
                .leftTo(11.8f).left(0.5f) },
        ),
        25 to listOf<Solution>(
            { leftTo(22.5f).landLeft()
                .leftJump(0.55f).landLeft().leftJump(0.55f).landLeft()
                .leftUntil { it.player.grounded && it.player.box.b > 11.5f }
                .rightUntil { (it.group('c').belt ?: 0f) < -5f }.rightJump(0.55f).landRight()
                .rightUntil { (it.group('b').belt ?: 0f) < 0f }.rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().right(0.5f) },
        ),
    )
}
