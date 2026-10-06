package com.robinrehbein.beveldevil.game

/**
 * Clean runs of World 1, block A (levels 7-16, docs/LEVEL_DESIGN_V2.md §11), one [Solution] per round, round 1 first. They are
 * what an informed player does with the real physics, and they are shared by [World1DesignTest] (the guard rails),
 * [World1Test] (the levels) and [World1DeckTest] (round 1 scripts against the rematches).
 */
object World1RoomsA {
    val solutions: Map<Int, List<Solution>> = mapOf(
        // 7: run on under the piece that comes down, hop the panels, the bank, up the stairs, back along the upper floor over the panel that
        // sinks and over the hole the piece left
        7 to listOf<Solution>(
            { hopR(16.0f).rightTo(21.4f).rightJump(0.3f).landRight()
                .rightTo(26.2f).rightJump(0.3f).landRight().rightJump(0.3f).landRight().rightTo(30.5f)
                .leftJump(0.35f).landLeft().leftTo(11.2f).leftJump(0.35f).landLeft().leftTo(1.5f) },
        ),
        // 8: hop the spike, the room turns over in mid-hop: steer back left onto the ceiling, along it past the stalactites to the door
        // above the start; rematch: stop as the room turns, hop the ceiling spikes and the stalactites, drop back to the floor past the
        // spike, and let the room turn once more under the door
        8 to listOf<Solution>(
            { rightTo(19.5f).rightJump(0.25f).leftUntil { it.player.grounded }.leftTo(17.1f).leftJump(0.5f).landLeft().leftTo(1.5f)
                .rightUntil { it.player.grounded && it.gravity < 0f }.rightTo(2.8f) },
            { rightTo(5.0f).leftUntil { it.player.grounded && it.gravity < 0f }.rightTo(7.2f).rightJump(0.5f).landRight()
                .rightTo(14.2f).rightJump(0.5f).landRight().rightUntil { it.player.grounded && it.gravity > 0f }.right(3f) },
        ),
        // 9: across the deck past the pothole, down at its end, back left with swapped keys over the pothole on the ground and the pit
        // onto the switch, and the same road to the door with the keys back
        9 to listOf<Solution>(
            { hopR(11.2f, 0.5f).leftUntil { it.player.box.cx > 25f && it.player.grounded }
                .hopSL(22.4f, 0.5f).hopSL(15.4f, 0.5f).rightUntil { it.pads[0].presses >= 1 }.hopR(10.4f, 0.5f).hopR(17.2f, 0.5f).rightTo(30f) },
        ),
        // 10: along the floor ahead of the wall, up the stairs, along the upper floor over the step and the spike bed
        10 to listOf<Solution>(
            { leftTo(6.6f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().left(0.5f).rightJump(0.5f).landRight()
                .rightTo(8.2f).rightJump(0.5f).landRight().rightTo(12.6f).rightJump(0.5f).landRight().right(3f) },
        ),
        // 11: across the deck over the pothole, off its end, the creek on stones that crumble, onto the little bank with the door
        11 to listOf<Solution>(
            { leftTo(12.6f).leftJump(0.5f).landLeft().left(1.0f).landLeft()
                .rightTo(3.6f).rightJump(0.12f).landRight().rightTo(8.8f).rightJump(0.5f).landRight()
                .rightTo(13.8f).rightJump(0.5f).landRight().right(1.5f) },
        ),
        // 12: hop the spike, then walk home with swapped keys ahead of it; rematch: swapped from the first steps, hop the spike that slides
        // at you, normal again behind the pit
        12 to listOf<Solution>(
            { leftTo(26.4f).leftJump(0.5f).landLeft().hopSL(14.4f, 0.5f).hopSL(8.6f, 0.5f).left(1.2f) },
            { leftUntil { it.swapped }.rightUntil { w -> w.player.box.cx - (18.5f + w.group('M').ox) <= 4.0f }.rightJump(0.35f).landRight()
                .hopSL(14.4f, 0.5f).hopL(8.6f, 0.5f).left(1.2f) },
        ),
        // 13: wait for the first piece and hop it, run under the second, off the end of the upper floor, back along the ground floor;
        // wait for the plain piece and hop it to the door
        13 to listOf<Solution>(
            { leftTo(25.5f).wait(0.4f).leftJump(0.5f).landLeft().leftTo(3.8f).rightTo(10.1f).wait(0.6f).hopR(10.4f).rightTo(19f) },
        ),
        // 14: ride the first lift to the upper floor and step off, hop the saw, ride the second lift down and step off to the door
        14 to listOf<Solution>(
            { right(0.3f).waitFor { it.group('a').oy <= -5.6f }.rightJump(0.35f).landRight()
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightTo(25.6f)
                .waitFor { it.group('b').oy >= 2.6f }.leftTo(15.6f) },
        ),
        // 15: let the block slam and hop it, up the stairs, back along the upper floor to the door; rematch: the door drops back, so
        // down the hole in the upper floor and back to it
        15 to listOf<Solution>(
            { rightTo(19.1f).wait(0.75f).leftTo(16.5f).hopR(19.25f, 0.5f).rightTo(24.6f).rightJump(0.5f).landRight()
                .rightJump(0.5f).landRight().right(0.4f).leftJump(0.5f).landLeft().leftTo(13.5f) },
            { rightTo(19.1f).wait(0.75f).leftTo(16.5f).hopR(19.25f, 0.5f).rightTo(24.6f).rightJump(0.5f).landRight()
                .rightJump(0.5f).landRight().right(0.4f).leftJump(0.5f).landLeft().leftTo(9.6f).landLeft().rightTo(14.5f) },
        ),
        // 16: up the stairs ahead of the first saw to the switch (the door leaves through the breach), down again, hop the second saw
        // and the slab, through the breach, hop the third saw, up the stairs and back along the ledge to the door
        16 to listOf<Solution>(
            { leftTo(15.0f).leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().leftJump(0.35f).landLeft()
                .left(0.7f).landLeft().rightJump(0.5f).landRight().rightTo(7.4f).rightJump(0.35f).landRight()
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightTo(25.8f).wait(0.5f).rightJump(0.5f).landRight()
                .rightUntilSaw(4.5f).rightJump(0.5f).landRight().rightTo(roomX(1, 21.6f)).rightJump(0.4f).landRight()
                .rightJump(0.4f).landRight().rightTo(roomX(1, 28.6f)).leftTo(roomX(1, 26.6f)).leftJump(0.5f).landLeft().leftTo(roomX(1, 10.6f)) },
        ),
    )
}
