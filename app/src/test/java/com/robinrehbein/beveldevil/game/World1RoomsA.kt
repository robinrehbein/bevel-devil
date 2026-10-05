package com.robinrehbein.beveldevil.game

/** Bot solutions of World 1, block A (levels 7-16), one per round; shared by the design tests and the level tests. */
object World1RoomsA {
    val solutions: Map<Int, List<Solution>> = mapOf(
        7 to listOf<Solution>(
            { hopR(16.0f).rightTo(21.4f).rightJump(0.3f).landRight()
                .rightTo(26.2f).rightJump(0.3f).landRight().rightJump(0.3f).landRight().rightTo(30.5f)
                .leftJump(0.35f).landLeft().leftTo(1.5f) },
        ),
        8 to listOf<Solution>(
            { hopR(19.5f, 0.5f).rightTo(25.5f).landRight().leftTo(17.1f).leftJump(0.5f).landLeft().leftTo(1.5f)
                .rightUntil { it.player.grounded && it.gravity < 0f }.rightTo(2.8f) },
            { hopR(19.5f, 0.5f).rightTo(27.3f).rightJump(0.5f).landRight().leftTo(13.1f).leftJump(0.5f).landLeft().leftTo(1.5f)
                .rightUntil { it.player.grounded && it.gravity < 0f }.rightTo(2.8f) },
        ),
        9 to listOf<Solution>(
            { hopR(11.2f, 0.5f).leftUntil { it.pads[0].presses >= 1 }
                .leftTo(22.4f).leftJump(0.5f).landLeft().leftTo(15.4f).leftJump(0.5f).landLeft().leftTo(1.0f) },
        ),
        10 to listOf<Solution>(
            { leftTo(6.6f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().left(0.5f).rightJump(0.5f).landRight()
                .rightTo(8.2f).rightJump(0.5f).landRight().rightTo(12.6f).rightJump(0.5f).landRight().right(3f) },
        ),
    )
}
