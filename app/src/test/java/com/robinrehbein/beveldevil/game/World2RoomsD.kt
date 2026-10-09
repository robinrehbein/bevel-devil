package com.robinrehbein.beveldevil.game

/**
 * Scripted clean runs of the rooms of World 2, block D (levels 41-48, act 3 "Root"), what an informed player does with the real
 * physics. Shared by [World2DesignTest] (the registered solutions) and [World2Test] / [World2DeckTest] (the rooms, their wrong
 * approaches, the rematches against round 1).
 */
object World2RoomsD {
    /** 41: over the first stone, hop the second, hop off the third, up the steps and back left onto the deck, hop the hole, hop the next one at once. */
    fun l41(b: Bot) = b.hopR(8.6f, 0.5f).hopR(14.4f, 0.5f).rightTo(24.3f).rightJump(0.45f).landRight().right(0.05f).rightJump(0.55f).landRight()
        .leftJump(0.55f).landLeft().hopL(22.4f, 0.4f).leftJump(0.5f).landLeft().left(3f)

    /** 41, round 2: hop the first stone (it drops at once), run over the slowly sinking second one, hop off the third, then as before. */
    fun l41r2(b: Bot) = b.hopR(5.2f, 0.5f).hopR(14.4f, 0.5f).rightTo(24.3f).rightJump(0.45f).landRight().right(0.05f).rightJump(0.55f).landRight()
        .leftJump(0.55f).landLeft().hopL(22.4f, 0.4f).leftJump(0.5f).landLeft().left(3f)

    /** 41 (round [r], 1-based): over the stones as in the clean run and onto step 4, then stay on it: it is revoked under you and you are on the floor below the block. */
    fun l41MissTheClimb(b: Bot, r: Int = 1) = b.hopR(if (r == 1) 8.6f else 5.2f, 0.5f).hopR(14.4f, 0.5f).rightTo(24.3f).rightJump(0.45f).landRight().wait(1f)

    /** 41: below the block after a missed climb, wait until the renewed step has lifted you to where step 4 was. */
    fun l41Renewed(b: Bot) = b.waitFor(3f) { it.group('r').oy <= -1f }.also { it.expect(WorldState.PLAYING) }

    /** 41: from step 4 (or the renewed one) up onto the block and on to the door, as in the clean run. */
    fun l41FromTheStep(b: Bot) = b.right(0.05f).rightJump(0.55f).landRight().leftJump(0.55f).landLeft().hopL(22.4f, 0.4f).leftJump(0.5f).landLeft().left(3f)

    /** The cart (a saw on the lane level) is past the player: it is to the right of them by [d] tiles. */
    private fun cartPast(w: World, d: Float) = w.saws.none { it.y > 13.5f && it.vx > 0f && it.x < w.player.box.cx + d }

    /** 42: hop the hole in the deck, stop for the ore bucket on its rope and slip under it, off the end of the deck, left over the pile (stand on it while cart 2 passes), run to the door. */
    fun l42(b: Bot) = b.hopR(6.0f, 0.5f).rightTo(17.4f).waitFor { World2Rooms.pendulumCalm(it, 20f, 0.1f, 0.6f, 7.4f) }
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
        .leftUntil { it.player.box.cx < 26.0f }.leftJump(0.4f).landLeft().waitFor { cartPast(it, 1.2f) }.left(2.5f)

    /** 42, round 2: as before, but the pile gives way under you: over it without a stop and hop cart 2 on the lane instead of standing on the gold. */
    fun l42r2(b: Bot) = b.rightTo(17.4f).waitFor { World2Rooms.pendulumCalm(it, 20f, 0.1f, 0.6f, 7.4f) }
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
        .leftUntil { it.player.box.cx < 26.0f }.leftJump(0.4f).landLeft().leftJump(0.5f).landLeft()
        .leftUntil { World2Rooms.sawAheadLeft(it, 3.6f) }.leftJump(0.5f).landLeft().left(3f)

    /** 43: along the lane until the beam has flashed, up to the port, shake the cable, through the port onto the deck, wait out the two flashes, drop off the end to the door. */
    fun l43(b: Bot) = b.leftUntil { it.player.box.cx < 25.0f }.waitFor(cond = World2Rooms.clear('A')).leftUntil { it.player.box.cx < 22.6f }.shake(0.1f)
        .leftUntil { it.player.box.cy < 11f }.leftUntil { it.player.box.cx < 19.4f }.waitFor(cond = World2Rooms.clear('B'))
        .leftUntil { it.player.box.cx < 6.8f }.waitFor(cond = World2Rooms.clear('C')).left(3f)

    /** The ceiling slab (group [id]) has landed and is at rest. */
    private fun slabDown(w: World, id: Char) = w.group(id).let { it.mode == GroupMode.IDLE && it.oy > 1f }

    /** On the ground, the landed slab (group [id]) stands ahead with its left face within [d] tiles. */
    private fun faceAhead(w: World, id: Char, d: Float) = w.player.grounded && w.group(id).let { g ->
        g.mode == GroupMode.IDLE && g.oy > 1f && g.pieces.minOf { it.box.x } - w.player.box.cx in 0f..d
    }

    /** Mephi has hit undo (the undo trap has sprung). */
    private fun undone(w: World) = w.sprung.any { s -> s.trap.actions.any { it is Action.Undo } }

    /** 44: hop the wall, stop short of the slab (under the one that hangs over your waiting place), wait for it to land, hop the block and on; when the undo puts you back, run at once and hop the block again, stop for the second slab, hop it, to the door. */
    fun l44(b: Bot) = b.rightUntil { it.player.box.cx > 2.9f }.rightJump(0.5f).landRight().rightUntil { it.player.box.cx > 9.3f }.waitFor { slabDown(it, 'c') }
        .rightUntil { faceAhead(it, 'c', 2.0f) }.rightJump(0.5f).landRight().rightUntil { undone(it) }
        .rightUntil { faceAhead(it, 'c', 2.0f) }.rightJump(0.5f).landRight().rightUntil { it.player.box.cx > 21.4f }.waitFor { slabDown(it, 'd') }
        .rightUntil { faceAhead(it, 'd', 2.0f) }.rightJump(0.5f).landRight().right(3f)










    /** 44, round 2: the first slab drops further on, so stop later; the second slab follows, and the undo comes right before the door: it sends you back to your first waiting place, under the slab that hangs over it: run at once, hop both blocks again, to the door. */
    fun l44r2(b: Bot) = b.rightUntil { it.player.box.cx > 2.9f }.rightJump(0.5f).landRight().rightUntil { it.player.box.cx > 8.8f }.waitFor { slabDown(it, 'c') }
        .rightUntil { faceAhead(it, 'c', 2.0f) }.rightJump(0.5f).landRight().rightUntil { it.player.box.cx > 18.2f }.waitFor { slabDown(it, 'd') }
        .rightUntil { faceAhead(it, 'd', 2.0f) }.rightJump(0.5f).landRight().rightUntil { undone(it) }
        .rightUntil { faceAhead(it, 'c', 2.0f) }.rightJump(0.5f).landRight().rightUntil { faceAhead(it, 'd', 2.0f) }.rightJump(0.5f).landRight().right(3f)










    /** 45: crawl right to the pad at the far end, jump up through the hatch, along the roof, up the steps to the door. */
    fun l45(b: Bot) = b.rightUntil { it.player.box.cx > 26.5f }.leftJump(0.55f).landLeft().leftUntil { it.player.box.cx < 19.2f }.leftJump(0.5f).landLeft()
        .leftJump(0.4f).landLeft().left(1.5f)

    /** 46: up onto the first belt, run against it to its end, hop the LEDs onto the second, run against it onto the pillar, wait there for root's maintenance window, cross root under the lifted gate and hop onto the bridge, along it to the door before it sinks. */
    fun l46(b: Bot) = b.leftTo(28.7f).leftJump(0.3f).landLeft().leftUntil { it.player.box.cx < 23.6f }.leftJump(0.55f).landLeft()
        .leftUntil { it.player.box.cx < 15.4f }.waitFor { it.group('c').belt == 0f }.leftJump(0.45f).landLeft()
        .leftUntil { it.player.box.cx < 8.6f }.leftJump(0.55f).landLeft().left(3f)

    /** 46, round 2: ride the belts that carry you right and hop at their ends, then back left from the top belt onto the carpet that runs the other way, and hold left against it to the door in its middle. */
    fun l46r2(b: Bot) = b.leftTo(28.7f).leftJump(0.3f).landLeft().waitFor { it.player.box.cx < 23.6f }.leftJump(0.55f).landLeft()
        .waitFor { it.player.box.cx < 16.6f }.leftJump(0.55f).landLeft().rightUntil { it.player.box.cx > 14.1f }.rightJump(0.5f).landRight().right(1.5f)

    /** 47: hop the cart on the lane (the controls swap as you leave the ground, so go on with the other key), stop for the pendulum, slip under it, up the narrow steps with swapped hands; on the first step the controls are back to normal: to the deck and the door. */
    fun l47(b: Bot) = b.rightUntil { World2Rooms.sawAhead(it, 3.3f) }.rightJump(0.08f).leftJump(0.45f).landLeft()
        .leftKeyRightTo(16.0f).waitFor { World2Rooms.pendulumCalm(it, 18.5f, 0.1f, 0.6f, 10.0f) }
        .leftKeyRightTo(21.0f).leftJump(0.5f).landLeft().rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.5f).landRight().rightJump(0.4f).landRight()
        .wait(0.4f).leftJump(0.45f).landLeft().leftUntil { World2Rooms.sawAheadLeft(it, 3.2f) }.leftJump(0.5f).landLeft().left(4f)

















    /**
     * 48: left to the pad behind the start (the portal is re-pointed), into the portal onto the deck, wait for the firewall, hop the tripwire, to the door and through the
     * breach; in the second room the controls swap on the deck: drop off its end, hop the LEDs and the cart with swapped hands, and fall down the shaft to the door.
     */
    fun l48(b: Bot): Bot {
        var seenLit = false
        val flashedOut = { w: World ->
            if (w.beams.any { it.laser.id == 'K' && it.lit }) seenLit = true
            seenLit && w.beams.none { it.laser.id == 'K' && (it.lit || it.warn > 0f) }
        }
        return b.leftUntil { it.player.box.cx < 4.6f }.leftJump(0.3f).landLeft().leftUntil { it.player.box.cx < 2.9f }.rightUntil { it.player.box.cy < 11f }.rightUntil { it.player.box.cx > 18.9f }.waitFor(cond = World2Rooms.clear('G'))
            .rightUntil { it.player.box.cx > 23.6f }.rightJump(0.45f).landRight().leftUntil { it.cracks.isNotEmpty() }.leftUntil { it.cracks.any { c -> c.fell } }
            .leftUntil { it.player.grounded && it.player.box.b > 14.5f }
            .leftUntil { World2Rooms.sawAhead(it, 4.4f) }.leftJump(0.55f).landLeft()
            .waitFor(cond = flashedOut).left(4f)
    }

    val solutions: Map<Int, List<Solution>> = mapOf(
        41 to listOf({ l41(this) }, { l41r2(this) }),
        42 to listOf({ l42(this) }, { l42r2(this) }),
        43 to listOf({ l43(this) }),
        44 to listOf({ l44(this) }, { l44r2(this) }),
        45 to listOf({ l45(this) }),
        46 to listOf({ l46(this) }, { l46r2(this) }),
        47 to listOf({ l47(this) }),
        48 to listOf({ l48(this) }),
    )
}
