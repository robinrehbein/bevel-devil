package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block F of World 3 (levels 41-48): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsF {
    val solutions: Map<Int, List<Solution>> = mapOf(
        41 to listOf<Solution>(
            { rightTo(4.6f).rightUntil { it.player.grounded && it.player.box.cx > 6.5f }
                .rightTo(8.4f).rightJump(0.1f).landRight()
                .rightTo(11.6f).rightJump(0.1f).landRight()
                .rightTo(15.6f).rightJump(0.1f).landRight()
                .rightTo(23.0f).waitFor { it.player.box.cy < 6.4f }.leftUntil { it.player.box.cy < 4.4f }
                .rightUntil { it.player.grounded && it.player.box.b < 5.5f }.rightTo(30f).right(1f) },
            // rematch: walk the bridge, do not hop it
            { rightTo(4.6f).rightUntil { it.player.grounded && it.player.box.cx > 6.5f }
                .rightTo(23.0f).waitFor { it.player.box.cy < 6.4f }.leftUntil { it.player.box.cy < 4.4f }
                .rightUntil { it.player.grounded && it.player.box.b < 5.5f }.rightTo(30f).right(1f) },
        ),
        42 to listOf<Solution>(
            { rightTo(15.0f).waitFor { it.player.box.cy < 6.8f }.rightUntil { it.player.grounded && it.player.box.b < 7.5f }
                .rightUntil { it.fans[1].wind < -2f }.leftUntil { it.player.box.cx < 26.3f }
                .leftUntil { it.player.grounded }.right(6f) },
            // rematch: keep hopping in the vent, standing still is loitering; the drain breathes in as before
            { rightTo(15.0f).fidgetUntil { it.player.box.cy < 6.8f }.rightUntil { it.player.grounded && it.player.box.b < 7.5f }
                .rightUntil { it.fans[1].wind < -2f }.leftUntil { it.player.box.cx < 26.3f }
                .leftUntil { it.player.grounded }.right(6f) },
        ),
        43 to listOf<Solution>(
            { rightTo(5.6f).rightTo(18.0f).rightUntil { it.player.box.cy < 7.4f }
                .leftUntil { it.player.grounded && it.player.box.cx < 16.8f }.leftTo(11.8f)
                .rightUntil { it.player.box.cx > 19.5f }.rightTo(23.5f).rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .right(3f) },
        ),
        44 to listOf<Solution>(
            { rightTo(9.5f).waitFor { it.player.box.cy < 6.4f }
                .waitFor { it.player.grounded }.rightTo(12.4f).rightJump(0.45f).landRight().rightTo(26f).right(1f) },
        ),
        45 to listOf<Solution>(
            { rightTo(14.5f).waitFor { it.player.box.cy < 9.5f }.leftUntil { it.player.box.cy < 4.6f }
                .rightUntil { it.player.grounded && it.player.box.b < 5.5f }.right(4f) },
        ),
        46 to listOf<Solution>(
            { rightTo(13.5f).waitFor { w -> w.fans[0].on && w.sprung.any { s -> s.trap.actions.any { a -> a is Action.Power && a.on } } }
                .rightUntil { it.player.box.cy < 9.4f }
                .leftUntil { it.player.grounded && it.player.box.cx < 11.5f }.leftTo(9.0f).leftJump(0.2f).leftTo(4.5f)
                .waitFor { it.player.box.cy < 3.8f }.rightUntil { it.player.grounded && it.player.box.b < 5.5f }
                .hopR(9.2f, 0.3f).rightUntil { w -> w.sprung.any { s -> s.trap.actions.any { a -> a is Action.Undo } } }
                .rightUntil { it.player.grounded && it.player.box.b < 5.5f }.hopR(9.2f, 0.3f).right(8f) },
        ),
        47 to listOf<Solution>(
            { rightTo(22.5f).waitFor { it.player.box.cy < 5.6f }.rightUntil { it.player.grounded && it.player.box.b < 6.5f }
                .rightUntil { it.player.box.cx > 27.0f }
                .leftUntil { it.player.box.cy > 8f }.waitFor { it.player.grounded }
                .rightTo(26.4f).right(1f) },
        ),
        48 to listOf<Solution>(
            {
                var turned = false
                rightTo(10.5f).waitFor { it.player.box.cy < 6.4f }.rightUntil { it.player.grounded && it.player.box.b < 7.5f }
                    .rightTo(20.0f).waitFor { it.player.box.cy < 2.6f }.rightUntil { it.player.grounded && it.player.box.b < 3.5f }
                    .rightTo(29.4f).waitFor { w -> w.cracks.any { it.fell } }
                    .rightTo(roomX(1, 5.5f)).rightUntil { w -> w.sprung.any { s -> s.trap.actions.any { a -> a is Action.Flip } } }
                    // while the picture is turned, left is right: keep pressing against the wall by pressing the other way
                    .leftUntil { w -> if (w.viewTurn() > 0.5f) turned = true; turned && w.viewTurn() < 0.05f }
                    .rightUntil { it.player.box.b > 11.0f }
                    .rightTo(roomX(1, 9.8f)).waitCooled('c', 0.2f).rightTo(roomX(1, 26f)).right(2f)
            },
        ),
    )
}
