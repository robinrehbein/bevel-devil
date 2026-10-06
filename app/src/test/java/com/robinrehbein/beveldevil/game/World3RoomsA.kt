package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block A of World 3 (levels 1-8): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsA {
    val solutions: Map<Int, List<Solution>> = mapOf(
        1 to listOf<Solution>(
            { hopL(18.5f).leftTo(2.6f).rightUntil { it.player.grounded && it.player.box.b > 14f }.rightTo(5.3f).rightJump(0.55f).landRight().rightTo(29.5f).right(1f) },
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
    )
}
