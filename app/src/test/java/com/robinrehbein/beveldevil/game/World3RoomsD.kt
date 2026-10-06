package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block D of World 3 (levels 25-32): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsD {
    /** The x of the first piece of group [id] (home plus offset). */
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    /** Group [id] has fallen and lies still, [low] tiles or more below where it hung. */
    private fun landed(w: World, id: Char, low: Float = 1.5f): Boolean = w.group(id).let { it.mode == GroupMode.IDLE && it.oy > low }

    /** Debug hook of the scratch report (removed before the final commit). */
    var trace: ((String, Bot) -> Unit)? = null
    private fun Bot.at(tag: String): Bot { trace?.invoke(tag, this); return this }

    val solutions: Map<Int, List<Solution>> = mapOf(
        25 to listOf<Solution>(
            { leftTo(22.5f).landLeft().at("B")
                .leftJump(0.55f).landLeft().at("h1").leftJump(0.55f).landLeft().at("h2")
                .leftUntil { it.player.grounded && it.player.box.b > 11.5f }.at("C")
                .rightUntil { (it.group('c').belt ?: 0f) < 0f }.at("T2").rightJump(0.55f).landRight().at("g1")
                .rightUntil { (it.group('b').belt ?: 0f) < 0f }.at("T3").rightJump(0.04f).landRight().at("g2").rightJump(0.04f).landRight().at("g3").rightJump(0.04f).landRight().at("g4").rightJump(0.04f).landRight().at("g5").rightJump(0.04f).landRight().at("g6").rightJump(0.04f).landRight().at("g7").rightJump(0.04f).landRight().at("g8").right(0.5f) },
        ),
    )
}
