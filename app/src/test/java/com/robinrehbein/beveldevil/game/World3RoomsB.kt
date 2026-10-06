package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block B of World 3 (levels 9-16): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsB {
    /** The x of the first piece of group [id] (home plus offset). */
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    /** Circuit [id] is lit and its clock came round to the lit half no more than [age] s ago (a window that has just opened). */
    private fun fresh(w: World, id: Char, age: Float = 0.5f): Boolean {
        val c = w.circuits[id] ?: return false
        val clock = c.clock ?: return c.powered
        return c.powered && clock.timing.cycle(w.time - c.clockT0) < age
    }

    val solutions: Map<Int, List<Solution>> = mapOf(
        9 to listOf<Solution>(
            { rightUntil { it.player.box.cy > 16f }.rightJump(0.55f).landRight()
                .leftUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'a') < 4f }.rightJump(0.55f).landRight()
                .rightUntil { it.player.box.cy > 16f }.waitFor { w -> gx(w, 'b') < 11f }.rightJump(0.55f).landRight()
                .rightTo(19.8f).rightJump(0.55f).landRight().right(1.5f) },
        ),
        10 to listOf<Solution>(
            { rightTo(4.0f).rightJump(0.55f).landRight()
                .rightTo(9.8f).waitFor { fresh(it, 'b') }.rightJump(0.55f).landRight()
                .rightTo(14.3f).waitFor { fresh(it, 'c') }.rightJump(0.55f).landRight().rightTo(19.2f).rightJump(0.55f).landRight()
                .rightJump(0.55f).landRight().rightTo(25.6f).rightJump(0.55f).landRight().rightJump(0.55f).landRight().right(1f) },
        ),
        11 to listOf<Solution>(
            { rightTo(9.4f).rightJump(0.55f).landRight().rightJump(0.55f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 9.5f }
                .leftTo(19.0f).leftJump(0.55f).landLeft().leftUntil { it.player.grounded && it.player.box.b > 14.5f }
                .rightTo(19.8f).rightJump(0.55f).landRight().rightTo(27.5f).right(1f) },
        ),
    )
}
