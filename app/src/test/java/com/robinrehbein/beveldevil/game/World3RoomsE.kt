package com.robinrehbein.beveldevil.game

/** Bot solutions of the rebuilt block E of World 3 (levels 33-40): one per round, round 1 first. Registered in [World3DesignTest]. */
object World3RoomsE {
    /** The x of the first piece of group [id] (home plus offset). */
    private fun gx(w: World, id: Char): Float = w.group(id).let { it.homeX + it.ox }

    val solutions: Map<Int, List<Solution>> = mapOf(
        33 to listOf<Solution>(
            { rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.cx > 12.3f }.rightTo(15.3f).rightJump(0.5f).landRight()
                .rightTo(24.6f).rightUntil { it.player.box.cx > 29.3f } },
            // rematch: hop the stud on the far plank low instead of the long leap
            { rightTo(6.6f).rightUntil { it.player.grounded && it.player.box.cx > 12.3f }.rightTo(18.2f).rightJump(0.1f).landRight()
                .rightTo(24.6f).rightUntil { it.player.box.cx > 29.3f } },
        ),
        34 to listOf<Solution>(
            { rightTo(18.6f).waitFor { it.fans[0].wind < -11.5f }.rightJump(0.5f).landRight().rightUntil { it.player.box.cx > 30.3f } },
        ),
        35 to listOf<Solution>(
            // hop the blade from behind and the one from the front, step on the button, stand at the wall (hop the blade on its way) until the gale is off
            { rightUntil { w -> w.saws.any { it.vx > 0f && w.player.box.cx - it.x < 3.0f } }.rightJump(0.5f).landRight()
                .rightUntil { w -> w.saws.any { it.vx < 0f && it.x > w.player.box.cx && it.x - w.player.box.cx <= 3.2f } }.rightJump(0.5f).landRight()
                .rightTo(13.6f)
                .waitFor { it.fans[1].wind == 0f }.rightUntil { it.player.box.cx > 24.6f }
                .rightUntil { w -> w.saws.any { it.vx < -9f && it.x > w.player.box.cx && it.x - w.player.box.cx <= 7.5f } }.rightJump(0.5f).landRight().rightUntil { it.player.box.cx > 30.3f } },
            // rematch: hop the button, then slog the whole corridor and hop each blade as it comes
            { rightTo(5.2f).rightJump(0.5f).landRight()
                .rightUntil { w -> w.saws.any { it.vx > 0f && w.player.box.cx - it.x < 3.0f } }.rightJump(0.5f).landRight()
                .rightUntil { w -> w.saws.any { it.vx < 0f && it.x > w.player.box.cx && it.x - w.player.box.cx <= 4.0f } }.rightJump(0.5f).landRight()
                .rightUntil { w -> w.saws.any { it.vx < -10.5f && it.vx > -11.5f && it.x > w.player.box.cx && it.x - w.player.box.cx <= 4.0f } }.rightJump(0.5f).landRight()
                .rightUntil { w -> w.saws.any { it.vx < -11.5f && it.x > w.player.box.cx && it.x - w.player.box.cx <= 4.0f } }.rightJump(0.5f).landRight()
                .rightUntil { it.player.box.cx > 30.3f } },
        ),
        36 to listOf<Solution>(
            { rightTo(7.4f).rightUntil { it.player.box.b > 8.2f }
                .leftUntil { it.player.box.cx < 9.2f }.leftUntil { it.player.grounded }
                .rightUntil { w -> w.player.box.cx > 18.5f }
                .rightUntil { w -> gx(w, 'C') > w.player.box.cx && gx(w, 'C') - w.player.box.cx < 3.5f }.rightJump(0.5f).landRight()
                .rightUntil { it.player.box.cx > 30f } },
        ),
        37 to listOf<Solution>(
            { rightTo(14.9f).waitFor { it.fans[0].wind == 0f }.rightTo(14.9f).rightJump(0.5f).landRight()
                .rightUntil { it.player.box.cx > 29.5f } },
        ),
        38 to listOf<Solution>(
            { leftTo(2.4f).rightTo(18.6f).waitFor { w -> w.circuits['Z']?.let { !it.powered && w.time - it.flipTime < 0.4f } == true }
                .rightUntil { it.player.box.cx > 25f }.rightTo(25.6f).rightJump(0.55f).landRight().rightUntil { it.player.box.cx > 30.3f } },
        ),
        39 to listOf<Solution>(
            { rightUntil { it.player.box.b < 6.3f }.rightTo(8.3f).rightJump(0.1f).landRight()
                .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.rightUntil { it.player.box.cx > 29.5f } },
        ),
        40 to listOf<Solution>(
            { rightTo(8.6f).rightUntil { it.player.grounded && it.player.box.cx > 14.4f }
                .waitFor { it.fans[0].wind > 4.5f }.rightUntil { it.player.box.cx > 23.2f }
                .rightUntil { it.player.grounded && it.player.box.cx > 23.4f }.rightTo(27.0f).rightJump(0.1f).landRight().rightUntil { it.player.box.cx > 30.3f } },
            // rematch: the controls are twisted: press left to float right
            { rightTo(8.6f).leftUntil { it.player.grounded && it.player.box.cx > 14.4f }
                .waitFor { it.fans[0].wind > 4.5f }.leftUntil { it.player.box.cx > 22.8f }
                .rightUntil { it.player.box.cx > 30.3f } },
        ),
    )
}
