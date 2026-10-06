package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block C of World 3 (levels 17-24): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsC {
    /** The x of the first piece of group [id] (home plus offset). */
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    /** Group [id] has fallen and lies still, [low] tiles or more below where it hung. */
    private fun landed(w: World, id: Char, low: Float = 1.5f): Boolean = w.group(id).let { it.mode == GroupMode.IDLE && it.oy > low }

    /** Plate group [id] is cool enough to be crossed in a few steps. */
    private fun cool(w: World, id: Char): Boolean = (w.heaters[id]?.heat ?: 0f) < 0.25f

    val solutions: Map<Int, List<Solution>> = mapOf(
        17 to listOf<Solution>(
            { rightTo(6.3f).waitCooled('g').rightTo(24.8f).landRight()
                .leftTo(19.9f).leftTo(17.8f).leftJump(0.55f).landLeft().leftTo(12.4f).left(0.5f) },
            // rematch: hop the hot sink and the quick plate, wait out the two lids and hop them
            { rightTo(4.4f).rightJump(0.5f).landRight().rightTo(9.4f).rightJump(0.5f).landRight().rightTo(16.7f)
                .waitFor { landed(it, 'd', 1f) }.rightJump(0.55f).landRight().rightTo(24.8f).landRight()
                .leftTo(22.8f).waitFor { landed(it, 'c', 1f) }.leftJump(0.55f).landLeft().leftTo(12.4f).left(0.5f) },
        ),
        18 to listOf<Solution>(
            { rightUntil { w -> w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= 5.5f } }.rightJump(0.55f).landRight()
                .rightTo(27.6f).leftUntil { w -> w.player.grounded && w.player.ground?.group?.id == 'm' }.waitCooled('c')
                .leftUntil { w -> w.saws.any { it.y > 12f && it.x < w.player.box.cx && w.player.box.cx - it.x <= 4.8f } }.leftJump(0.55f).landLeft()
                .leftTo(3.0f).left(1.5f) },
        ),
        19 to listOf<Solution>(
            // run onto the fuse, hop where it blows, run off before it melts
            { rightTo(11.9f).rightJump(0.45f).landRight().rightTo(29.5f).right(0.5f) },
        ),
        20 to listOf<Solution>(
            { rightTo(6.7f).rightJump(0.5f).landRight().rightTo(13.2f).rightJump(0.5f).landRight().rightTo(20.7f).rightJump(0.5f).landRight()
                .rightTo(28.0f).leftJump(0.5f).landLeft().leftTo(21.3f).leftJump(0.5f).landLeft().leftTo(14.3f).leftJump(0.5f).landLeft()
                .leftTo(7.3f).leftJump(0.5f).landLeft().leftTo(2.6f).left(1f) },
        ),
        21 to listOf<Solution>(
            { rightTo(10.7f).waitCooled('b').rightTo(19.5f).right(0.3f).rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .leftTo(25.2f).leftJump(0.55f).landLeft()
                .leftUntil { w -> gx(w, 'U') < w.player.box.cx && w.player.box.cx - gx(w, 'U') <= 3.8f }.leftJump(0.55f).landLeft().leftTo(4.6f).left(1f) },
            // rematch: the stove is on for good and the sink sends the wall at once: hop over it on the blocks
            { rightTo(4.6f).rightJump(0.4f).landRight().rightTo(9.4f).rightJump(0.4f).landRight().rightTo(16.2f).rightJump(0.4f).landRight()
                .rightTo(19.5f).right(0.3f).rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .leftTo(25.2f).leftJump(0.55f).landLeft()
                .leftUntil { w -> gx(w, 'U') < w.player.box.cx && w.player.box.cx - gx(w, 'U') <= 3.8f }.leftJump(0.55f).landLeft().leftTo(2.4f).left(1f) },
        ),
        22 to listOf<Solution>(
            { rightTo(10.4f).rightJump(0.55f).landRight().rightTo(21.5f).landRight()
                .leftUntil { w -> gx(w, 'F') < w.player.box.cx && w.player.box.cx - gx(w, 'F') <= 3.8f }.leftJump(0.55f).landLeft()
                .leftTo(9.5f).landLeft()
                .rightTo(10.8f).rightJump(0.55f).landRight()
                .rightUntil { w -> gx(w, 'G') > w.player.box.cx && gx(w, 'G') - w.player.box.cx <= 3.8f }.rightJump(0.55f).landRight().right(2f) },
        ),
        23 to listOf<Solution>(
            { rightTo(5.2f).waitFor { it.player.grounded }.rightTo(6.8f).waitFor { landed(it, 'a', 1f) }.rightJump(0.55f).landRight()
                .rightTo(24.0f).waitFor { landed(it, 'e', 1f) }.rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1.5f) },
        ),
        24 to listOf<Solution>(
            { leftTo(27.0f).landLeft().waitFor { it.player.grounded && cool(it, 'a') }.leftTo(15.4f).waitFor { cool(it, 'b') }
                .leftTo(9.4f).waitPowered('Z', true).waitPowered('Z', false).leftTo(5.6f).leftJump(0.55f).landLeft().leftJump(0.55f).landLeft().left(0.5f) },
        ),
    )
}
