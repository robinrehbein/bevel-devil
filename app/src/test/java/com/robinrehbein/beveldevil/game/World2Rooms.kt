package com.robinrehbein.beveldevil.game

/**
 * Scripted clean runs of the puzzle rooms of World 2, act 2 (levels 17-24, and the rematches of 11 and 14), keyed by level number: what an informed
 * player does, with the real physics. Shared by [World2Test] (the level, its wrong approaches, its length) and
 * [World2DeckTest] (round-1 scripts against the rematches).
 */
object World2Rooms {
    /** Firewall gate [id] is dark and not glowing: safe to cross now. */
    fun clear(id: Char) = { w: World -> w.beams.none { it.laser.id == id && (it.lit || it.warn > 0f) } }

    /** On the ground, a saw is ahead within [d] tiles. */
    fun sawAhead(w: World, d: Float) = w.player.grounded && w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= d && kotlin.math.abs(it.y - w.player.box.cy) < 1.5f }

    /** Firewall gate [id] is lit or glowing. */
    fun busy(id: Char) = { w: World -> !clear(id)(w) }

    /** The middle of the moving wall (group [id]) in tiles. */
    fun wallX(w: World, id: Char) = w.group(id).homeX + w.group(id).ox

    /** On the ground, wall group [id] is ahead on the left within [d] tiles, driving toward the player. */
    fun wallOnTheLeft(w: World, id: Char, d: Float): Boolean = w.player.grounded && (w.player.box.cx - wallX(w, id)) in 0f..d

    /** On the ground, wall group [id] is ahead on the right within [d] tiles, driving toward the player. */
    fun wallOnTheRight(w: World, id: Char, d: Float): Boolean = w.player.grounded && (wallX(w, id) - w.player.box.cx) in 0f..d

    /** 17: over the duct, wait for the loose shelf piece, up the stairs, hop the hole, touch the plate, ride the bus, jump the closed station. */
    fun l17(b: Bot) = b.rightJump(0.35f).landRight().rightJump(0.35f).landRight()
        .rightUntil { it.group('c').mode == GroupMode.FALL }.waitFor { it.group('c').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .rightJump(0.35f).landRight().rightTo(17.4f).rightJump(0.4f).landRight()
        .wait(0.12f).leftJump(0.4f).landLeft().hopL(15.2f)
        .leftTo(4.6f).leftTo(2.6f).rightUntil { it.player.grounded && it.player.box.b > 13.9f }
        .rightJump(0.35f).landRight().rightTo(23.5f).rightJump(0.35f).landRight().right(1.5f)

    /** 18, up to the pad on the rack: hop the hole, wait for the gate, climb the rack, step back onto the pad. */
    fun l18ToPad(b: Bot) = b.hopR(15.6f).rightTo(21.3f).wait(0.05f).waitFor(cond = clear('G')).rightTo(23.1f).rightJump(0.24f).landRight().leftTo(26.3f)

    /** The stairs of 18, from the floor right of the first step up to the ledge: each jump starts where the next step is within reach. */
    private fun stairs18(b: Bot) = b.leftTo(17.0f).leftJump(0.24f).landLeft().leftTo(13.6f).leftJump(0.24f).landLeft()
        .leftTo(9.6f).leftJump(0.24f).landLeft().leftTo(5.6f).leftJump(0.24f)

    /** 18: press the pad, leave the rack before the port scan, run back and climb the stairs without stopping. */
    fun l18(b: Bot) = stairs18(l18ToPad(b)).landLeft().left(1f)

    /** 18, round 2: climb first, press the pad where the door was (landing on it), run down for the door and hop the saw. */
    fun l18r2(b: Bot) = b.rightTo(7.4f).wait(0.05f).waitFor(cond = clear('G')).hopR(10.3f, 0.24f).wait(0.15f).leftTo(13.6f).leftJump(0.24f).landLeft().leftTo(9.6f).leftJump(0.24f).landLeft().leftTo(5.6f).leftJump(0.24f).waitFor { it.player.grounded }
        .rightUntil { sawAhead(it, 4.7f) }.rightJump(0.35f).landRight().rightTo(28.6f).right(1f)

    /** 19, up to the moment the order is restored: hop the pit, the wall and the stairs with swapped hands. */
    fun l19ToShelf(b: Bot) = b.hopR(6.8f).hopS(11.6f).hopS(13.9f).hopS(19.6f).hopS(23.4f)

    /** 19: the whole room: stairs with swapped hands, the top floor with normal ones, hop the wall that drives toward you and the block before the door. */
    fun l19(b: Bot) = l19ToShelf(b).leftTo(25.6f).leftJump(0.35f).landLeft()
        .leftUntil { wallOnTheLeft(it, 'w', 4.3f) }.leftJump(0.35f).landLeft()
        .hopL(11.4f).left(2.5f)

    /** 20, through the second check, the flashing gate and up the stairs to the ledge: landing on the ledge presses the scanner. */
    fun l20ToLedge(b: Bot) = b.hopR(4.1f, 0.24f).wait(0.05f).waitFor(cond = clear('M')).hopR(7.0f, 0.24f).hopR(10.0f, 0.24f).wait(0.05f).waitFor(cond = clear('N')).hopR(13.0f, 0.24f).hopR(16.0f, 0.24f).hopR(19.0f, 0.24f)

    /** 20, up to the scanner on the ledge (it is where you land). */
    fun l20ToScanner(b: Bot) = l20ToLedge(b)

    /** 20: scan, wait until the queue at the exit has passed, walk off the ledge (holding back against the drift) and run for the door. */
    fun l20(b: Bot) = l20ToScanner(b).wait(0.05f).waitFor(cond = clear('K')).rightUntil { it.player.box.b > 4f }.leftUntil { it.player.grounded }.rightTo(28.8f).right(1f)

    /** 20, round 2: the scanner is a bluff; jump the gap onto the far ledge and walk back to the door. */
    fun l20r2(b: Bot) = l20ToLedge(b).rightTo(24.4f).rightJump(0.5f).landRight().rightUntil { it.player.box.b > 4f }.leftUntil { it.player.grounded }.leftTo(28.0f).left(1f)

    /** 21, into the near portal: the closet between the walls (the port opens on the way in). */
    fun l21ToCloset(b: Bot) = b.rightUntil { it.player.box.cx > 14.2f }

    /** 21, out of the closet again (step off the portal tile and back onto it) and through the far portal: up on the shelf, where the treadmill starts. */
    fun l21Up(b: Bot) = l21ToCloset(b).leftTo(14.6f).rightUntil { it.player.box.cx < 12f }.rightUntil { it.player.box.cy < 9f }

    /** 21, up on the shelf and along the treadmill to the hole. */
    fun l21ToShelf(b: Bot) = l21Up(b).rightTo(12.5f)

    /** 21: along the shelf and down through the hole, hop the portal in front of the door. */
    fun l21(b: Bot) = l21ToShelf(b).rightUntil { it.player.box.b > 8.5f }.waitFor { it.player.grounded }.hopR(24.0f).right(2f)

    /** On the ground, a saw rolls toward the player from the left within [d] tiles. */
    fun sawAheadLeft(w: World, d: Float) = w.player.grounded && w.saws.any { it.x < w.player.box.cx && w.player.box.cx - it.x <= d && kotlin.math.abs(it.y - w.player.box.cy) < 1.5f }

    /** 22, up to the lane: along the top floor (hop the bouncer who rolls out of the wall, hop the hole) and down at the right-hand end. */
    fun l22ToLane(b: Bot) = b.hopR(15.0f).rightUntil { sawAhead(it, 4.4f) }.rightJump(0.35f).landRight().rightTo(29.5f).waitFor { it.player.grounded }

    /** 22: then hop the LEDs, hop the second bouncer who rolls out of the back door, hop the pit, to the door. */
    fun l22(b: Bot) = l22ToLane(b).hopL(22.6f).leftUntil { sawAheadLeft(it, 4.4f) }.leftJump(0.35f).landLeft().hopL(7.4f).left(1f)

    /** 23, lane 1: the traffic comes against you, hop the LED on the belt, hop the hole, into the on-ramp (and out on lane 2). */
    fun l23Lane1(b: Bot) = b.hopR(8.3f).rightTo(19.6f).hopR(19.8f).rightUntil { it.player.box.cy < 10f && it.player.box.cx > 4.0f }

    /** 23: lane 2: the express belt throws you at the LED (hop it), the wrong-way lane is hopped, not walked (hop after hop until it ends), hop the last LED. */
    fun l23(b: Bot): Bot {
        l23Lane1(b).hopR(7.0f)
        while (b.world.player.box.cx < 21.3f && b.world.state == WorldState.PLAYING) b.rightJump(0.35f).landRight()
        return b.hopR(22.0f).right(3f)
    }

    /** 24: along the top floor over the stone (keep moving), drop to the second floor, back left over the stepping stones, jump the LEDs under the first stone, to the door. */
    fun l24(b: Bot) = b.rightTo(27f).waitFor { it.player.grounded }.leftTo(14.0f).leftJump(0.35f).landLeft().left(3f)

    val solutions: Map<Int, (Bot) -> Bot> = mapOf(
        17 to ::l17, 18 to ::l18, 19 to ::l19, 20 to ::l20, 21 to ::l21, 22 to ::l22, 23 to ::l23, 24 to ::l24,
    )
}
