package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block A (levels 7-16, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first. They are
 * what an informed player does with the real physics, and they are shared by [World1DesignTest] (the guard rails),
 * [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsA {
    val solutions: Map<Int, List<Solution>> = mapOf(
        // 7: hop the panels, the bank, up the stairs, back along the upper floor over the panel that sinks
        7 to listOf<Solution>(
            { hopR(16.0f).rightTo(21.4f).rightJump(0.3f).landRight()
                .rightTo(26.2f).rightJump(0.3f).landRight().rightJump(0.3f).landRight().rightTo(30.5f)
                .leftJump(0.35f).landLeft().leftTo(1.5f) },
        ),
        // 8: hop the spike, let the room turn over, along the ceiling past the stalactites to the door above the start; rematch: the room is mirrored,
        // hop the spike running left, jump near the left wall, along the ceiling to the right
        8 to listOf<Solution>(
            { hopR(19.5f, 0.5f).rightTo(25.5f).landRight().leftTo(17.1f).leftJump(0.5f).landLeft().leftTo(1.5f)
                .rightUntil { it.player.grounded && it.gravity < 0f }.rightTo(2.8f) },
            { hopL(23.5f, 0.5f).leftTo(4.7f).leftJump(0.5f).landLeft().rightTo(18.9f).rightJump(0.5f).landRight().rightTo(30.5f)
                .leftUntil { it.player.grounded && it.gravity < 0f }.leftTo(29.2f) },
        ),
        // 9: across the deck past the pothole with swapped keys, onto the switch, home along the floor
        9 to listOf<Solution>(
            { hopR(11.2f, 0.5f).leftUntil { it.pads[0].presses >= 1 }
                .leftTo(22.4f).leftJump(0.5f).landLeft().leftTo(15.4f).leftJump(0.5f).landLeft().leftTo(1.0f) },
        ),
        // 10: along the floor ahead of the wall, up the stairs, along the upper floor over the step and the spike bed
        10 to listOf<Solution>(
            { leftTo(6.6f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().left(0.5f).rightJump(0.5f).landRight()
                .rightTo(8.2f).rightJump(0.5f).landRight().rightTo(12.6f).rightJump(0.5f).landRight().right(3f) },
        ),
        // 11: across the deck over the pothole, off its end, the creek on stones that crumble
        11 to listOf<Solution>(
            { leftTo(12.6f).leftJump(0.5f).landLeft().left(1.0f).landLeft()
                .rightTo(3.6f).rightJump(0.12f).landRight().rightTo(8.8f).rightJump(0.5f).landRight()
                .rightTo(13.8f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().right(3f) },
        ),
        // 12: hop the spike, then walk home with swapped keys; rematch: swapped from the first steps
        12 to listOf<Solution>(
            { leftTo(26.4f).leftJump(0.5f).landLeft().hopSL(14.4f, 0.5f).hopSL(8.6f, 0.5f).right(1.2f) },
            { leftUntil { it.swapped }.hopSL(14.4f, 0.5f).hopSL(8.6f, 0.5f).right(1.2f) },
        ),
        // 13: along the upper floor under the dropping ceiling, off its end, back along the ground floor to the door
        13 to listOf<Solution>(
            { leftTo(3.5f).rightTo(28.0f).right(1.5f) },
        ),
        // 14: ride the first lift to the upper floor and step off, hop the saw, ride the second lift down and step off
        14 to listOf<Solution>(
            { right(0.3f).waitFor { it.group('a').oy <= -5.6f }.rightJump(0.35f).landRight()
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightTo(25.6f)
                .waitFor { it.group('b').oy >= 2.6f }.leftTo(9.0f) },
        ),
        // 15: follow the door the long way round; rematch: it comes back, so drop off the end of the upper floor and walk to it
        15 to listOf<Solution>(
            { rightTo(24.6f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().right(0.4f)
                .leftJump(0.5f).landLeft().leftTo(1.5f) },
            { rightTo(24.6f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().right(0.4f)
                .leftJump(0.5f).landLeft().left(3.2f).landLeft().rightTo(12.0f) },
        ),
        // 16: up to the switch, down again, hop the saw to the door, through the breach, hop the pit and the second saw
        16 to listOf<Solution>(
            { leftTo(15.0f).leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().left(0.7f).landLeft()
                .rightJump(0.5f).landRight().rightTo(7.4f).rightJump(0.35f).landRight()
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightUntil(4f) { it.cracks.isNotEmpty() }
                .rightUntil(3f) { it.cracks.any { c -> c.fell } }.rightTo(roomX(1, 10.6f)).rightJump(0.5f).landRight()
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().right(5f) },
        ),
    )
}
