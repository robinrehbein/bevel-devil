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
                .right(6f) },
            // rematch: keep hopping in the vent, standing still is loitering
            { rightTo(15.0f).fidgetUntil { it.player.box.cy < 6.8f }.rightUntil { it.player.grounded && it.player.box.b < 7.5f }
                .right(6f) },
        ),
        43 to listOf<Solution>(
            { rightTo(5.6f).rightTo(18.0f).rightUntil { it.player.box.cy < 7.4f }
                .leftUntil { it.player.grounded && it.player.box.cx < 16.8f }.leftTo(11.8f)
                .rightUntil { it.player.box.cx > 19.5f }.rightTo(23.5f).rightUntil { it.player.grounded && it.player.box.b > 14.5f }
                .right(3f) },
        ),
        44 to listOf<Solution>(
            { rightTo(9.5f).waitFor { it.player.box.cy < 6.4f }
                .rightTo(16.4f).rightJump(0.25f).landRight().rightTo(29f).right(1f) },
        ),
    )
}
