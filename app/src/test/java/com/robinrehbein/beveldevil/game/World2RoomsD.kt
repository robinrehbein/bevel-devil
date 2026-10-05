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

    val solutions: Map<Int, List<Solution>> = mapOf(
        41 to listOf({ l41(this) }, { l41r2(this) }),
        42 to listOf({ l42(this) }, { l42r2(this) }),
    )
}
