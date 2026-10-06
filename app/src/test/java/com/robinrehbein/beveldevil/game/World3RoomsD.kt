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
                .rightTo(14.5f)
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.rightTo(28.6f)
                .rightTo(roomX(1, 12.0f)).waitFor { landed(it, 'q') }
                .rightJump(0.4f).landRight().rightTo(roomX(1, 28.6f))
                .rightTo(roomX(2, 8.9f)).rightJump(0.5f).landRight()
                .rightTo(roomX(2, 29.0f)).right(1f) },
            { rightTo(8.6f).waitCooled('c', 0.05f).rightTo(14.5f)
                .rightTo(28.6f).rightTo(roomX(1, 4.9f)).rightJump(0.5f).landRight().rightTo(roomX(1, 28.6f))
                .rightTo(roomX(2, 8.9f)).rightJump(0.5f).landRight()
                .rightTo(roomX(2, 16.4f)).rightJump(0.5f).landRight()
                .rightTo(roomX(2, 29.0f)).right(1f) },
        ),
        31 to listOf<Solution>(
            { rightTo(6.4f).rightJump(0.5f).landRight().waitCooled('c', 0.05f)
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightTo(25.4f).rightJump(0.5f).landRight()
                .leftTo(27.8f).leftJump(0.5f).landLeft()
                .leftTo(24.0f).leftJump(0.5f).landLeft().waitCooled('d', 0.05f)
                .leftUntil { it.player.grounded && it.player.box.b > 10.5f }
                .leftTo(16.6f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().left(0.8f) },
        ),
        30 to listOf<Solution>(
            { rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight()
                .rightTo(9.4f).rightTo(10.4f).rightJump(0.55f).landRight()
                .rightTo(16.6f).rightTo(17.4f).rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .leftTo(14.2f).leftJump(0.55f).landLeft().leftTo(8.6f).left(0.5f) },
            // rematch: same arch, but the spikes in the lane are real (hop them) and the shelf sprouts some of its own (hop them too); the plates come first
            { rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight()
                .rightTo(9.4f).rightTo(10.4f).rightJump(0.55f).landRight()
                .rightTo(18.1f).rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .leftTo(22.4f).leftJump(0.55f).landLeft().leftTo(14.2f).leftJump(0.55f).landLeft().leftTo(8.6f).left(0.5f) },
        ),
        29 to listOf<Solution>(
            { rightTo(24.0f).right(0.7f).leftJump(0.3f).landLeft().leftTo(21.2f)
                .waitFor { w -> landed(w, 't', 0.5f) }
                .leftTo(19.0f).leftJump(0.5f).landLeft()
                .leftTo(13.4f).leftJump(0.55f).landLeft()
                .leftTo(5.0f).left(1.0f) },
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
            { rightTo(10.4f).rightJump(0.55f).landRight()
                .rightTo(23.4f).rightJump(0.5f).landRight()
                .rightTo(29.0f).leftTo(25.8f).leftJump(0.5f).landLeft()
                .leftJump(0.5f).landLeft()
                .leftTo(12.8f).waitFor { w -> !w.beams.first { it.laser.id == 'c' }.on }
                .leftTo(9.6f).leftJump(0.55f).landLeft().leftTo(3.6f).left(0.5f) },
        ),
        25 to listOf<Solution>(
            { leftTo(22.5f).landLeft()
                .leftJump(0.55f).landLeft().leftJump(0.55f).landLeft()
                .leftUntil { it.player.grounded && it.player.box.b > 11.5f }
                .rightUntil { (it.group('c').belt ?: 0f) < 0f }.rightJump(0.55f).landRight()
                .rightUntil { (it.group('b').belt ?: 0f) < 0f }.rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().rightJump(0.04f).landRight().right(0.5f) },
        ),
    )
}
