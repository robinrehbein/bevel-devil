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
            // ride the vent up, stop short of the hood, hop it, down the shaft; when the drain breathes in step out to the right
            { leftTo(17.0f).waitFor { it.player.box.cy < 6.8f }.leftUntil { it.player.grounded && it.player.box.b < 7.5f }
                .leftTo(12.8f).waitFor { it.group('h').oy > 3.9f }.leftJump(0.5f).landLeft()
                .leftUntil { it.player.box.cx < 4.2f }.leftUntil { it.fans[1].wind < -2f }.rightUntil { it.player.box.cx > 5.7f }
                .rightUntil { it.player.grounded }.left(6f) },
            // rematch: keep hopping in the vent, standing still is loitering; the rest as before
            { leftTo(17.0f).fidgetUntil { it.player.box.cy < 6.8f }.leftUntil { it.player.grounded && it.player.box.b < 7.5f }
                .leftTo(12.8f).waitFor { it.group('h').oy > 3.9f }.leftJump(0.5f).landLeft()
                .leftUntil { it.player.box.cx < 4.2f }.leftUntil { it.fans[1].wind < -2f }.rightUntil { it.player.box.cx > 5.7f }
                .rightUntil { it.player.grounded }.left(6f) },
        ),
        43 to listOf<Solution>(
            // (run right to left) press the first switch, run the powered bridge, up the lift, back right along the ledge to the
            // second switch, then left over the block and through where the wall was
            { leftTo(26.4f).leftTo(14.0f).leftUntil { it.player.box.cy < 7.4f }
                .rightUntil { it.player.grounded && it.player.box.cx > 15.2f }.rightTo(20.2f)
                .leftUntil { it.player.box.cx < 12.5f }.leftTo(8.5f).leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .left(3f) },
        ),
        44 to listOf<Solution>(
            // off the shelf to the lift, up and over onto the ceiling, hop the stud, along the ceiling to the door
            { leftTo(5.0f).waitFor { it.player.box.cy < 6.4f }
                .waitFor { it.player.grounded }.rightTo(8.4f).rightJump(0.45f).landRight()
.rightTo(26f).right(1f) },
        ),
        45 to listOf<Solution>(
            // off the rack into the shaft, hug its left wall on the way down (the spiked rack slides in from the right), slip into the gap
            // under the wall before touching down (the floor of the shaft sinks), hop the pin that slides at you, and the door is behind it
            { leftTo(23.0f).leftUntil { it.player.grounded }
                .leftUntil { w -> w.group('L').let { it.homeX + it.ox } > w.player.box.cx - 3.6f }.leftJump(0.5f).landLeft()
                .leftUntil { it.player.box.cx < 2.8f } },
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
