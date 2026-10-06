package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block C of World 3 (levels 17-24): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsC {
    /** Group [id] has fallen and lies still, [low] tiles or more below where it hung. */
    private fun landed(w: World, id: Char, low: Float = 1.5f): Boolean = w.group(id).let { it.mode == GroupMode.IDLE && it.oy > low }

    val solutions: Map<Int, List<Solution>> = mapOf(
        17 to listOf<Solution>(
            { rightTo(6.3f).waitCooled('g').rightTo(24.8f).landRight()
                .leftTo(13.9f).leftTo(11.8f).leftJump(0.55f).landLeft().leftTo(3.4f).left(1f) },
            // rematch: hop the hot sink and the quick plate, wait out the two lids and hop them
            { rightTo(4.4f).rightJump(0.5f).landRight().rightTo(9.4f).rightJump(0.5f).landRight().rightTo(16.7f)
                .waitFor { landed(it, 'd', 1f) }.rightJump(0.55f).landRight().rightTo(24.8f).landRight()
                .leftTo(22.8f).waitFor { landed(it, 'c', 1f) }.leftJump(0.55f).landLeft().leftTo(3.4f).left(1f) },
        ),
    )
}
