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

    val solutions: Map<Int, List<Solution>> = mapOf(
        41 to listOf({ l41(this) }, { l41r2(this) }),
    )
}
