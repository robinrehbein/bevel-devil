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
    fun sawAhead(w: World, d: Float) = w.player.grounded && w.saws.any { it.x > w.player.box.cx && it.x - w.player.box.cx <= d }

    /** Firewall gate [id] is lit or glowing. */
    fun busy(id: Char) = { w: World -> !clear(id)(w) }

    /** 17: over the duct, wait for the loose shelf piece, up the stairs, hop the hole, press the pad, ride the bus, jump the closed station. */
    fun l17(b: Bot) = b.rightJump(0.35f).landRight().rightJump(0.35f).landRight()
        .rightUntil { it.group('c').mode == GroupMode.FALL }.waitFor { it.group('c').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .rightJump(0.35f).landRight().rightTo(17.4f).rightJump(0.4f).landRight()
        .wait(0.12f).leftJump(0.4f).landLeft().hopL(15.2f)
        .leftTo(4.6f).leftTo(2.6f).rightUntil { it.player.grounded && it.player.box.b > 13.9f }
        .rightJump(0.35f).landRight().rightTo(23.5f).rightJump(0.35f).landRight().right(1.5f)

    /** 18, up to the pad on the rack: hop the low beam, wait for the gate, climb the rack, step back onto the pad. */
    fun l18ToPad(b: Bot) = b.hopR(16.4f).rightTo(23.2f).waitFor(cond = clear('G')).rightTo(25.6f).rightJump(0.35f).landRight().leftTo(28.6f)

    /** The stairs of 18, from the floor right of the first step up to the ledge: each jump starts where the next step is within reach. */
    private fun stairs18(b: Bot) = b.leftTo(17.0f).leftJump(0.35f).landLeft().leftTo(13.6f).leftJump(0.4f).landLeft()
        .leftTo(9.6f).leftJump(0.4f).landLeft().leftTo(5.6f).leftJump(0.4f)

    /** 18: press the pad, run back, hop the port scan onto the first step and up the stairs without stopping. */
    fun l18(b: Bot) = stairs18(l18ToPad(b).leftTo(23.4f).leftJump(0.35f).landLeft()).landLeft().left(1f)

    /** 18, round 2: climb first, press the pad where the door was (landing on it), run down for the door, hop the saw and wait for the last gate. */
    fun l18r2(b: Bot) = b.hopR(10.3f).wait(0.15f).leftTo(13.6f).leftJump(0.4f).landLeft().leftTo(9.6f).leftJump(0.4f).landLeft().leftTo(5.6f).leftJump(0.4f).waitFor { it.player.grounded }
        .rightUntil { sawAhead(it, 4.3f) }.rightJump(0.4f).landRight().rightTo(24.6f).waitFor(cond = clear('Z')).rightTo(28.6f).right(1f)

    /** 19, up to the moment the order is restored: hop the pit, the wall and the stairs with swapped hands. */
    fun l19ToShelf(b: Bot) = b.hopR(6.8f).hopS(11.6f).hopS(13.9f).hopS(19.6f).hopS(23.4f)

    /** 19: the whole room: stairs with swapped hands, the top floor with normal ones, wait for the packet from the ceiling and hop it. */
    fun l19(b: Bot) = l19ToShelf(b).leftTo(25.6f).leftJump(0.35f).landLeft()
        .leftUntil { it.group('c').mode == GroupMode.FALL }.waitFor { it.group('c').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .hopL(15.2f).hopL(9.8f).left(1.5f)

    /** 20, up to the second check: over the scanner on the way, onto the second pad (gate 3 shuts). */
    fun l20ToScanner(b: Bot) = b.rightTo(18.6f)

    /** 20: back to the scanner (gate 3 opens again, the queue forms), then down the lane: hop the low beam, wait for the gate. */
    fun l20(b: Bot) = l20ToScanner(b).leftTo(6.0f).rightTo(13.6f).rightJump(0.35f).landRight().rightTo(21.6f)
        .waitFor(cond = busy('Z')).waitFor(cond = clear('Z')).right(3f)

    /** 20, round 2: the same, turned around. */
    fun l20r2(b: Bot) = b.leftTo(13.4f).rightTo(26.0f).leftTo(18.4f).leftJump(0.35f).landLeft().leftTo(10.4f)
        .waitFor(cond = busy('Z')).waitFor(cond = clear('Z')).left(3f)

    /** 21, into the captive portal on the lane, up on the top floor at the far right. */
    fun l21ToCage(b: Bot) = b.rightUntil { it.links[0].hopTime > 0f }

    /** 21: back left along the top floor without a jump, down through the hole, hop the low beam and the portal in front of the door. */
    fun l21(b: Bot) = l21ToCage(b).leftTo(10.4f).leftTo(9.0f).waitFor { it.player.grounded }.hopR(11.4f).rightTo(20.2f).rightJump(0.12f).landRight().rightTo(25.6f).waitFor(cond = busy('G')).waitFor(cond = clear('G')).right(1.5f)

    /** The middle of the bouncer's wall (group w) in tiles. */
    fun bouncerX(w: World) = w.group('w').homeX + w.group('w').ox

    /** 22: along the top floor (wait for each packet to land, then hop it), drop down, hop the bouncer and the pit, to the door. */
    fun l22(b: Bot) = b.rightUntil { it.group('c').mode == GroupMode.FALL }.waitFor { it.group('c').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .rightJump(0.35f).landRight().rightUntil { it.group('d').mode == GroupMode.FALL }.waitFor { it.group('d').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .rightJump(0.35f).landRight().rightTo(29.5f).waitFor { it.player.grounded }
        .leftUntil { it.player.grounded && bouncerX(it) < it.player.box.cx && it.player.box.cx - bouncerX(it) <= 4.0f }.leftJump(0.35f).landLeft()
        .hopL(7.4f).left(1f)

    /** On the ground, wall group [id] rolls toward the player from the right within [d] tiles. */
    fun carAhead(w: World, id: Char, d: Float): Boolean {
        val x = w.group(id).homeX + w.group(id).ox - w.player.box.cx
        return w.player.grounded && x in 0f..d
    }

    /** 23: hop the first car, ride the on-ramp, hop the second and the third. */
    fun l23(b: Bot) = b.rightUntil { carAhead(it, 'a', 4.3f) }.rightJump(0.35f).landRight().rightUntil { it.links[0].hopTime > 0f }
        .rightUntil { carAhead(it, 'b', 4.3f) }.rightJump(0.35f).landRight().rightUntil { carAhead(it, 'c', 4.3f) }.rightJump(0.35f).landRight().right(3f)

    /** 24: along the first floor (wait for the packet, hop it), drop to the second (wait for the gate), walk into the hole, ride the piece down and hop the low beam. */
    fun l24(b: Bot) = b.rightTo(9.0f).waitFor { it.group('c').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .rightJump(0.35f).landRight().rightTo(19.2f).leftUntil { it.player.grounded }
        .leftTo(17.6f).waitFor(cond = busy('G')).waitFor(cond = clear('G')).leftTo(13.0f).landLeft()
        .hopR(17.4f).right(4f)

    /** 11, round 2: the switch behind the spawn, the one upstairs, then jump the raised floor in front of the door. */
    fun l11r2(b: Bot) = b.leftTo(1.6f).rightTo(14.2f).rightJump(0.3f).landRight().wait(0.2f).leftJump(0.45f).landLeft()
        .leftTo(2.5f).rightTo(14.5f).landRight().rightTo(24.6f).rightJump(0.5f).landRight().right(0.5f)

    /** 14, round 2: the floor link leads on now; wait in the top right, then outrun the firewall over the bridge. */
    fun l14r2(b: Bot) = b.right(1.6f).leftTo(9f).leftJump(0.3f).landLeft().leftUntil { it.player.box.cx > 20f }
        .leftTo(24.1f).wait(0.6f).leftJump(0.35f).landLeft().waitFor { it.group('k').visible }.waitFor { !it.group('k').visible }
        .leftUntil { it.player.box.cx > 25f }.leftTo(26.6f).leftJump(0.3f).landLeft().leftTo(22.6f).leftJump(0.35f).landLeft().left(0.5f)

    val solutions: Map<Int, (Bot) -> Bot> = mapOf(
        17 to ::l17, 18 to ::l18, 19 to ::l19, 20 to ::l20, 21 to ::l21, 22 to ::l22, 23 to ::l23, 24 to ::l24,
    )
}
