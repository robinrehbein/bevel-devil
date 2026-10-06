package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block A of World 3 (levels 1-8): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsA {
    /** The flickering wall [id] has just gone dark (the first 0.35 s of its dark half): time enough to cross it. */
    private fun darkStart(w: World, id: Char): Boolean {
        val c = w.circuits[id] ?: return false
        val clock = c.clock ?: return false
        val t = clock.timing.cycle(w.time - c.clockT0)
        return t >= clock.on && t < clock.on + 0.35f
    }

    /** The path saw (pendulum) hanging in column [x]. */
    private fun pend(w: World, x: Float): Float = w.saws.firstOrNull { it.path != null && Math.abs(it.x - x) < 0.1f }?.y ?: 99f

    val solutions: Map<Int, List<Solution>> = mapOf(
        1 to listOf<Solution>(
            { hopL(18.5f).leftTo(2.6f).rightUntil { it.player.grounded && it.player.box.b > 14f }.rightTo(5.3f).rightJump(0.55f).landRight().rightTo(29.5f).right(1f) },
            // rematch: the plates moved, the landing wakes a stalker
            { leftTo(12.7f).leftJump(0.55f).landLeft().leftTo(2.6f).rightUntil { it.player.grounded && it.player.box.b > 14f }
                .rightTo(9.8f).rightJump(0.55f).landRight().rightTo(22.3f).rightJump(0.55f).landRight().rightTo(29.5f).right(1f) },
        ),
        2 to listOf<Solution>(
            { rightUntil { w -> w.group('K').let { it.homeX + it.ox - w.player.box.cx < 3.0f } }.rightJump(0.55f).landRight()
                .rightTo(7.3f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .rightTo(13.6f).rightJump(0.55f).landRight()
                .waitFor { it.group('Z').visible }.waitFor { !it.group('Z').visible }
                .rightUntil { w -> w.group('S').let { it.homeX + it.ox - w.player.box.cx < 3.0f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.box.cy > 10f }.landRight()
                .rightTo(27.4f).rightJump(0.55f).landRight().right(1f) },
        ),
        3 to listOf<Solution>(
            { hopR(4.0f).rightTo(9.2f).waitFor { it.group('p').visible }.rightTo(12.5f).rightJump(0.55f).landRight()
                .rightTo(17.5f).rightJump(0.55f).landRight().waitFor { it.group('m').visible }
                .rightTo(22.5f).rightJump(0.55f).landRight().rightTo(27.6f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1f) },
        ),
        4 to listOf<Solution>(
            { leftUntil { it.player.grounded && it.player.box.b > 14f }.leftTo(19.3f).waitFor { w -> pend(w, 17.5f) < 12.3f }
                .leftTo(15.8f).waitFor { w -> pend(w, 12.5f) < 12.0f }.leftTo(13.4f).leftJump(0.55f).landLeft().leftTo(9.6f)
                .leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().left(1f) },
        ),
        5 to listOf<Solution>(
            { rightTo(7.2f).waitFor { w -> w.group('c').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.rightJump(0.55f).landRight()
                .rightUntil { it.player.box.cx > 23.8f }.rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .leftTo(30.1f).leftJump(0.55f).landLeft().waitFor { w -> w.group('d').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }
                .leftJump(0.55f).landLeft().leftTo(2.4f) },
            // rematch: wait where you ran (the slab before the stairs, the last one on the shelf), run where you waited
            { rightTo(18.2f).waitFor { w -> w.group('g').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.rightJump(0.55f).landRight()
                .rightTo(24.4f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .leftTo(30.1f).leftJump(0.55f).landLeft().leftTo(17.0f).waitFor { w -> w.group('e').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }
                .leftJump(0.55f).landLeft().leftTo(2.4f) },
        ),
        6 to listOf<Solution>(
            { leftTo(7.2f).leftJump(0.55f).landLeft().rightUntil(0.3f) { it.pads[0].presses >= 1 || it.player.box.cx > 2.9f }.leftUntil(0.3f) { it.pads[0].presses >= 1 || it.player.box.cx < 2.1f }
                .rightTo(3.2f).rightJump(0.55f).landRight()
                .rightTo(9.4f).rightJump(0.55f).landRight().rightTo(16.0f).rightJump(0.55f).landRight()
                .rightTo(24.0f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1f) },
        ),
        7 to listOf<Solution>(
            { rightTo(7.2f).waitFor { w -> w.group('c').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.rightJump(0.55f).landRight()
                .rightTo(17.0f).waitFor { w -> darkStart(w, 'Z') }.rightTo(20.6f).rightTo(24.5f).waitFor { w -> darkStart(w, 'Y') }
                .rightTo(27.6f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1f) },
            // rematch: wait for the first slab (it lands in front now) and climb it, wait under the slab that stays up, hop the one that landed, wait out the seeded beam
            { var seenA = false; var seenB = false
              waitFor { w -> w.group('a').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.rightJump(0.55f).landRight()
                .rightTo(12.2f).waitFor { w -> w.group('b').let { it.mode == GroupMode.IDLE && it.oy > 1.5f } }.rightJump(0.4f).landRight()
                .waitFor { w -> if (w.beams.any { it.laser.id == 'A' && it.lit }) seenA = true; seenA && w.beams.none { it.laser.id == 'A' && (it.lit || it.warn > 0f) } }
                .rightTo(23.4f)
                .waitFor { w -> if (w.beams.any { it.laser.id == 'B' && it.lit }) seenB = true; seenB && w.beams.none { it.laser.id == 'B' && (it.lit || it.warn > 0f) } }
                .rightTo(27.6f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1f) },
        ),
        8 to listOf<Solution>(
            { rightTo(16.4f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().rightTo(29.5f).right(1f) },
        ),
    )
}
