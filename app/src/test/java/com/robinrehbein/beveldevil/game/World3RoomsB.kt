package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block B of World 3 (levels 9-16): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsB {
    /** The x of the first piece of group [id] (home plus offset). */
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    val solutions: Map<Int, List<Solution>> = mapOf(
        9 to listOf<Solution>(
            { rightUntil { it.player.box.cy > 16f }.rightJump(0.55f).landRight()
                .leftUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'a') < 4f }.rightJump(0.55f).landRight()
                .rightUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'b') < 11f }.rightJump(0.55f).landRight()
                .rightTo(19.8f).rightJump(0.55f).landRight().right(1.5f) },
        ),
    )
}
