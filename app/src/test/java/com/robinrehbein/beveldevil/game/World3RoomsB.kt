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
            { leftUntil { it.player.box.cy > 16f }.leftJump(0.55f).landLeft()
                .rightUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'a') > 28.5f }.leftJump(0.55f).landLeft()
                .leftUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'b') > 21.5f }.leftJump(0.55f).landLeft()
                .leftTo(12.2f).leftJump(0.55f).landLeft().left(1.5f) },
        ),
        10 to listOf<Solution>(
            // step off the deck, wait on the island for the stone next to it, walk it, wait at its edge for the late one, hop over
            // and off it at once, run up the stairs after the door
            { rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .waitFor { fresh(it, 'b') }.rightUntil { it.player.box.cx > 13.4f }.waitFor { fresh(it, 'c', 0.9f) }.rightJump(0.04f).landRight()
                .rightTo(18.0f).rightJump(0.55f).landRight().rightUntil { it.player.box.cx > 22.7f }.rightJump(0.55f).landRight()
                .rightTo(25.0f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1f) },
        ),
        11 to listOf<Solution>(
            { rightTo(9.4f).rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 9.5f }
                .leftTo(19.0f).leftJump(0.55f).landLeft().leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightTo(19.8f).rightJump(0.55f).landRight().rightTo(27.5f).right(1f) },
        ),
        12 to listOf<Solution>(
            // over the decoy button, under the low slab at a run, stop next to the high slab, climb it, wait out the blade on the wall
            { leftTo(17.8f)
                .waitFor { w -> w.group('c').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.wait(0.25f)
                .leftJump(0.35f).landLeft().leftJump(0.55f).landLeft()
                .waitFor { w -> w.saws.any { it.y > 12f } && w.saws.filter { it.y > 12f }.all { it.x > w.player.box.cx + 0.3f } }
                .leftUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(5.0f).left(0.5f) },
            // rematch: press the button under the slab, step out from under it, wait for the stair, then as before
            { leftTo(15.8f).rightTo(18.2f)
                .waitFor { w -> w.group('c').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.wait(0.25f)
                .leftJump(0.35f).landLeft().leftJump(0.55f).landLeft()
                .waitFor { w -> w.saws.any { it.y > 12f } && w.saws.filter { it.y > 12f }.all { it.x > w.player.box.cx + 0.3f } }
                .leftUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(5.0f).left(0.5f) },
        ),
        13 to listOf<Solution>(
            // step off the cabinet onto the step, hop to the island, wait for the bridge (not too long), up the steps, up to the
            // sill, wait out the surge, hop over onto the last floor and off it at once
            { rightUntil { it.player.grounded && it.player.box.b > 10.5f }.rightUntil { it.player.box.cx > 10.6f }.rightJump(0.06f).landRight()
                .rightTo(15.6f).waitFor { fresh(it, 'r') }.rightTo(18.6f).rightJump(0.12f).landRight()
                .rightTo(20.6f).rightJump(0.2f).landRight()
                .rightUntil { it.player.box.cx > 24.5f }
                .waitFor { w -> w.circuits['Z']?.let { it.clock != null && !it.powered && w.time - it.clockT0 > 0.5f } == true }.wait(0.15f)
                .rightJump(0.3f).landRight().rightUntil { it.player.grounded && it.player.box.b > 14.5f }.right(1f) },
        ),
        14 to listOf<Solution>(
            // ride the lift, step off at the top, stop in front of the fin the plate slams down, hop the pin at the door
            { leftTo(22.2f).leftUntil { it.player.grounded && it.player.box.b < 10.1f && it.player.box.cx < 20.5f }
                .leftTo(13.6f).waitFor { w -> w.time > 3f && w.group('B').let { it.mode == GroupMode.IDLE && it.oy < 1.5f } }
                .leftTo(9.8f).leftJump(0.55f).landLeft().left(0.6f) },
        ),
        15 to listOf<Solution>(
            { rightUntil { it.swapped }.leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightKeyLeftTo(24.8f).rightJump(0.55f).landRight()
                .rightUntil { w -> w.saws.any { it.y > 12f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 7.0f } }.rightJump(0.55f).landRight()
                .waitFor { w -> w.saws.any { it.path != null && it.y < 12.2f } }
                .rightKeyLeftTo(9.2f).rightJump(0.55f).landRight()
                .rightUntil { !it.swapped }.leftTo(2.0f).left(1f) },
            // rematch: the plug on the floor restores the polarity before the spike, the one at the door reverses it again
            { rightUntil { it.swapped }.leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightKeyLeftTo(24.8f).rightJump(0.55f).landRight()
                .rightUntil { w -> w.saws.any { it.y > 12f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 7.0f } }.rightJump(0.55f).landRight()
                .rightUntil { !it.swapped }.leftTo(10.0f).leftJump(0.55f).landLeft()
                .leftUntil { it.swapped }.rightKeyLeftTo(2.0f).right(1f) },
        ),
        16 to listOf<Solution>(
            { rightTo(16.5f).rightTo(17.4f).rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .waitFor { w -> w.circuits['Z']?.let { it.clock != null && !it.powered && w.time - it.clockT0 > 0.5f } == true }.wait(0.4f)
                .rightTo(20.6f).rightJump(0.35f).landRight().rightJump(0.45f).landRight().rightJump(0.45f).landRight()
                .rightUntil(4f) { it.cracks.any { c -> c.fell } }.rightTo(roomX(1, 10.0f)).rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightTo(roomX(1, 19.3f)).waitFor { it.circuits['b']?.powered == true }.rightTo(roomX(1, 25.0f)).rightJump(0.45f).landRight().rightJump(0.45f).landRight().rightTo(roomX(1, 29.0f)).right(1f) },
        ),
    )
}
