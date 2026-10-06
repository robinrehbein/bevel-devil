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

    private fun sawsAhead(w: World, x: Float, hi: Float = 0f) = w.saws.any { it.vx < 0f && it.x > x && it.y > hi }

    val solutions: Map<Int, List<Solution>> = mapOf(
        28 to listOf<Solution>(
            { rightTo(6.6f).at("ledge").rightUntil { it.player.grounded && it.player.box.b > 12.5f }.at("land")
                .rightTo(12.4f).at("edge1").rightUntil { it.player.grounded && it.player.box.b > 15.5f }.at("in1")
                .rightTo(16.2f).rightJump(0.3f).landRight().at("sit1")
                .waitFor { w -> w.saws.none { it.vx < 0f && it.x > 17f } }.at("go1")
                .rightJump(0.55f).landRight().at("out1")
                .rightTo(20.2f).at("edge2").rightUntil { it.player.grounded && it.player.box.b > 15.5f }.at("in2")
                .rightTo(24.2f).rightJump(0.3f).landRight().at("sit2")
                .waitFor { w -> w.saws.none { it.vx < 0f && it.x > 25f } }.at("go2")
                .rightJump(0.55f).landRight().at("out2").right(1.5f) },
            // rematch: no sitting down; hop the narrowed dips in the air, with each high blade let past first, then up the steps
            { rightTo(4.4f).at("ledge").rightUntil { it.player.grounded && it.player.box.b > 12.5f }.at("land")
                .rightTo(10.4f).at("g1").waitFor { w -> w.saws.none { it.y < 11.5f && it.x > w.player.box.cx - 2.5f } }.at("clear1")
                .rightTo(12.0f).rightJump(0.55f).landRight().at("over1")
                .rightTo(18.2f).at("g2").waitFor { w -> w.saws.none { it.y < 11.5f && it.x > w.player.box.cx - 2.5f } }.at("clear2")
                .rightTo(20.0f).rightJump(0.55f).landRight().at("over2")
                .rightTo(24.6f).rightJump(0.5f).landRight().at("s1").rightJump(0.5f).landRight().at("s2").rightJump(0.5f).landRight().at("s3").right(1.0f) },
        ),
        27 to listOf<Solution>(
            { rightTo(4.5f).at("edge").waitFor { it.circuits['a']?.powered == true }.at("lit")
                .rightTo(21.9f).at("isle").rightJump(0.55f).landRight().at("land").right(1.5f) },
        ),
        26 to listOf<Solution>(
            { rightTo(10.4f).rightJump(0.55f).landRight().at("w1")
                .rightTo(23.4f).at("pre").rightJump(0.5f).landRight().at("s1")
                .rightTo(29.0f).at("far").leftTo(25.8f).leftJump(0.5f).landLeft().at("s2")
                .leftJump(0.5f).landLeft().at("top")
                .leftTo(12.8f).at("gate").waitFor { w -> !w.beams.first { it.laser.id == 'c' }.on }.at("go")
                .leftTo(9.6f).at("w2").leftJump(0.55f).landLeft().at("end").leftTo(3.6f).left(0.5f) },
        ),
        25 to listOf<Solution>(
            { leftTo(22.5f).landLeft().at("B")
                .leftJump(0.55f).landLeft().at("h1").leftJump(0.55f).landLeft().at("h2")
                .leftUntil { it.player.grounded && it.player.box.b > 11.5f }.at("C")
                .rightUntil { (it.group('c').belt ?: 0f) < 0f }.at("T2").rightJump(0.55f).landRight().at("g1")
                .rightUntil { (it.group('b').belt ?: 0f) < 0f }.at("T3").rightJump(0.04f).landRight().at("g2").rightJump(0.04f).landRight().at("g3").rightJump(0.04f).landRight().at("g4").rightJump(0.04f).landRight().at("g5").rightJump(0.04f).landRight().at("g6").rightJump(0.04f).landRight().at("g7").rightJump(0.04f).landRight().at("g8").right(0.5f) },
        ),
    )
}
