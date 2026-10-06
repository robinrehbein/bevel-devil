package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block A of World 3 (levels 1-8): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsA {
    val solutions: Map<Int, List<Solution>> = mapOf(
        1 to listOf<Solution>(
            { hopL(18.5f).leftTo(2.6f).rightUntil { it.player.grounded && it.player.box.b > 14f }.rightTo(5.3f).rightJump(0.55f).landRight().rightTo(29.5f).right(1f) },
        ),
    )
}
