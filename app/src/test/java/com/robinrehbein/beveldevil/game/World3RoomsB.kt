package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block B of World 3 (levels 9-16): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsB {
    /** The x of the first piece of group [id] (home plus offset). */
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    /** Circuit [id] is lit and its clock came round to the lit half no more than [age] s ago (a window that has just opened). */
    private fun fresh(w: World, id: Char, age: Float = 0.5f): Boolean {
        val c = w.circuits[id] ?: return false
        val clock = c.clock ?: return c.powered
        return c.powered && clock.timing.cycle(w.time - c.clockT0) < age
    }

    val solutions: Map<Int, List<Solution>> = mapOf(
        9 to listOf<Solution>(
            { rightUntil { it.player.box.cy > 16f }.rightJump(0.55f).landRight()
                .leftUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'a') < 4f }.rightJump(0.55f).landRight()
                .rightUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'b') < 11f }.rightJump(0.55f).landRight()
                .rightTo(19.8f).rightJump(0.55f).landRight().right(1.5f) },
        ),
        10 to listOf<Solution>(
            { rightTo(4.0f).rightJump(0.55f).landRight()
                .rightTo(9.8f).waitFor { fresh(it, 'b') }.rightJump(0.55f).landRight()
                .rightTo(14.3f).waitFor { fresh(it, 'c') }.rightJump(0.55f).landRight().rightTo(19.2f).rightJump(0.55f).landRight()
                .rightJump(0.55f).landRight().rightTo(25.6f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1f) },
        ),
        11 to listOf<Solution>(
            { rightTo(9.4f).rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 9.5f }
                .leftTo(19.0f).leftJump(0.55f).landLeft().leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightTo(19.8f).rightJump(0.55f).landRight().rightTo(27.5f).right(1f) },
        ),
        12 to listOf<Solution>(
            { rightUntil { w -> w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= 3.2f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(17.8f)
                .waitFor { w -> w.group('c').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.wait(0.25f)
                .leftJump(0.35f).landLeft().leftJump(0.55f).landLeft()
                .waitFor { w -> w.saws.any { it.y > 12f } && w.saws.filter { it.y > 12f }.all { it.x > w.player.box.cx + 0.3f } }
                .leftUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(1.0f).left(1f) },
            // rematch: press the button under the slab, step out from under it, wait for the stair, then as before
            { rightUntil { w -> w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= 4.3f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(15.8f).rightTo(18.2f)
                .waitFor { w -> w.group('c').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.wait(0.25f)
                .leftJump(0.35f).landLeft().leftJump(0.55f).landLeft()
                .waitFor { w -> w.saws.any { it.y > 12f } && w.saws.filter { it.y > 12f }.all { it.x > w.player.box.cx + 0.3f } }
                .leftUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(1.0f).left(1f) },
        ),
        13 to listOf<Solution>(
            { rightTo(2.6f).rightJump(0.55f).landRight().rightTo(8.4f).rightJump(0.55f).landRight()
                .rightTo(13.2f).waitFor { fresh(it, 'r') }.rightTo(16.6f).rightJump(0.55f).landRight()
                .rightTo(22.9f)
                .waitFor { w -> w.circuits['Z']?.let { it.clock != null && !it.powered } == true }
                .rightTo(27.6f).rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .leftTo(28.4f).left(1f) },
        ),
        14 to listOf<Solution>(
            { rightTo(8.8f).rightUntil { it.player.grounded && it.player.box.b < 9.1f }
                .rightTo(18.4f).waitFor { w -> w.time > 3f && w.group('B').let { it.mode == GroupMode.IDLE && it.oy < 1.5f } }
                .rightTo(24.4f).rightJump(0.55f).landRight().rightTo(29.0f).right(1f) },
        ),
        15 to listOf<Solution>(
            { rightUntil { it.swapped }.leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightKeyLeftTo(24.8f).rightJump(0.55f).landRight()
                .rightUntil { w -> w.saws.any { it.y > 12f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 7.0f } }.rightJump(0.55f).landRight()
                .rightKeyLeftTo(10.9f).rightJump(0.55f).landRight()
                .rightUntil { !it.swapped }.leftTo(2.0f).left(1f) },
            // rematch: the plug on the floor restores the polarity before the spike, the one at the door reverses it again
            { rightUntil { it.swapped }.leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightKeyLeftTo(24.8f).rightJump(0.55f).landRight()
                .rightUntil { w -> w.saws.any { it.y > 12f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 7.0f } }.rightJump(0.55f).landRight()
                .rightUntil { !it.swapped }.leftTo(11.6f).leftJump(0.55f).landLeft()
                .leftUntil { it.swapped }.rightKeyLeftTo(2.0f).right(1f) },
        ),
    )
}
