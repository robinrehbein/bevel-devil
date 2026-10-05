package com.robinrehbein.beveldevil.game

/**
 * Scripted clean runs of the rooms of World 2, block D (levels 41-48, act 3 "Root"), what an informed player does with the real
 * physics. Shared by [World2DesignTest] (the registered solutions) and [World2Test] / [World2DeckTest] (the rooms, their wrong
 * approaches, the rematches against round 1).
 */
object World2RoomsD {
    /** 41: over the first stone, hop the second, hop off the third, up the steps and back left onto the deck, hop the hole, hop the next one at once. */
    fun l41(b: Bot) = b.hopR(8.6f, 0.5f).hopR(14.4f, 0.5f).rightTo(24.3f).rightJump(0.45f).landRight().right(0.05f).rightJump(0.55f).landRight()
        .leftJump(0.55f).landLeft().hopL(18.4f, 0.4f).leftJump(0.5f).landLeft().left(3f)

    /** 41, round 2: hop the first stone (it drops at once), run over the honest second one, hop off the third, then as before. */
    fun l41r2(b: Bot) = b.hopR(5.2f, 0.5f).hopR(14.4f, 0.5f).rightTo(24.3f).rightJump(0.45f).landRight().right(0.05f).rightJump(0.55f).landRight()
        .leftJump(0.55f).landLeft().hopL(18.4f, 0.4f).leftJump(0.5f).landLeft().left(3f)

    /** The cart (a saw on the lane level) is past the player: it is to the right of them by [d] tiles. */
    private fun cartPast(w: World, d: Float) = w.saws.none { it.y > 13.5f && it.vx > 0f && it.x < w.player.box.cx + d }

    /** 42: hop the hole in the deck, hop cart 1, off the end of the deck, left over the pile (stand on it while cart 2 passes), run to the door. */
    fun l42(b: Bot) = b.hopR(6.0f, 0.5f).rightUntil { World2Rooms.sawAhead(it, 3.6f) }.rightJump(0.5f).landRight()
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
        .leftUntil { it.player.box.cx < 17.5f }.leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().waitFor { cartPast(it, 1.2f) }.leftTo(9.6f).left(3f)

    /** 42, round 2: as before, but the pile gives way under you: over it without a stop and hop cart 2 on the lane instead of standing on the gold. */
    fun l42r2(b: Bot) = b.rightUntil { World2Rooms.sawAhead(it, 3.6f) }.rightJump(0.5f).landRight()
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }
        .leftUntil { it.player.box.cx < 17.5f }.leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().leftJump(0.5f).landLeft()
        .leftUntil { World2Rooms.sawAheadLeft(it, 3.6f) }.leftJump(0.5f).landLeft().left(3f)

    /** 43: along the lane until the beam has flashed, up to the port, shake the cable, through the port onto the deck, wait out the two flashes, drop off the end to the door. */
    fun l43(b: Bot) = b.rightUntil { it.player.box.cx > 7.0f }.waitFor(cond = World2Rooms.clear('A')).rightUntil { it.player.box.cx > 9.4f }.shake(0.1f)
        .rightUntil { it.player.box.cy < 11f }.rightUntil { it.player.box.cx > 12.6f }.waitFor(cond = World2Rooms.clear('B'))
        .rightUntil { it.player.box.cx > 25.2f }.waitFor(cond = World2Rooms.clear('C')).right(3f)

    /** The ceiling slab (group [id]) has landed and is at rest. */
    private fun slabDown(w: World, id: Char) = w.group(id).let { it.mode == GroupMode.IDLE && it.oy > 1f }

    /** Mephi has hit undo (the undo trap has sprung). */
    private fun undone(w: World) = w.sprung.any { s -> s.trap.actions.any { it is Action.Undo } }

    /** 44: stop short of the slab, wait for it to land, hop onto the block and off its far side (the undo throws you back), hop the block again, stop for the second slab, hop it, to the door. */
    fun l44(b: Bot) = b.rightUntil { it.player.box.cx > 9.3f }.waitFor { slabDown(it, 'c') }.hopR(10.0f, 0.5f).rightUntil { undone(it) }
        .hopR(10.0f, 0.5f).rightUntil { it.player.box.cx > 19.3f }.waitFor { slabDown(it, 'd') }.hopR(21.0f, 0.5f).right(3f)

    /** 44, round 2: both slabs as before, and the undo comes right before the door: hop the second block again. */
    fun l44r2(b: Bot) = b.rightUntil { it.player.box.cx > 9.3f }.waitFor { slabDown(it, 'c') }.hopR(10.0f, 0.5f)
        .rightUntil { it.player.box.cx > 19.3f }.waitFor { slabDown(it, 'd') }.hopR(21.0f, 0.5f).rightUntil { undone(it) }.hopR(21.0f, 0.5f).right(3f)

    val solutions: Map<Int, List<Solution>> = mapOf(
        41 to listOf({ l41(this) }, { l41r2(this) }),
        42 to listOf({ l42(this) }, { l42r2(this) }),
        43 to listOf({ l43(this) }),
        44 to listOf({ l44(this) }, { l44r2(this) }),
    )
}
