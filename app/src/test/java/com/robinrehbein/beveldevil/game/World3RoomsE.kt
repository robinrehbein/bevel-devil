package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block E of World 3 (levels 33-40): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsE {
    val solutions: Map<Int, List<Solution>> = mapOf(
        33 to listOf<Solution>(
            { rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.cx > 12.3f }.rightTo(15.3f).rightJump(0.5f).landRight()
                .rightTo(24.6f).rightUntil { it.player.box.cx > 29.3f } },
        ),
        34 to listOf<Solution>(
            { rightTo(18.6f).waitFor { it.fans[0].wind < -11.5f }.rightJump(0.5f).landRight().rightUntil { it.player.box.cx > 30.3f } },
        ),
    )
}
