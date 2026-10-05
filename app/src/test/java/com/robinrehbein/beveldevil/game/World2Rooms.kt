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

    /** 18, round 2, up to the pad: the hole is two tiles further on (hop it late), wait for the gate, climb the rack and step onto the pad (the rack is the lift now). */
    fun l18r2ToPad(b: Bot) = b.hopR(17.8f).wait(0.05f).waitFor(cond = clear('G')).rightTo(23.1f).rightJump(0.24f).landRight().leftTo(26.3f)

    /** 18, round 2: ride the rack to the walkway (hold right against the tower), walk off it before the port scan, hop the tripwire and drop onto the ledge at the door. */
    fun l18r2(b: Bot) = l18r2ToPad(b).rightUntil { it.group('k').oy < -7.9f }.leftTo(13.4f).leftJump(0.35f).landLeft().left(2.5f)

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

    /** 20, round 2, up the stairs without a single wait (nothing to wait for, everything expires behind you). */
    fun l20r2Stairs(b: Bot) = b.hopR(4.1f, 0.3f).waitFor { it.circuits['w']?.powered == true }.hopR(7.6f).hopR(11.6f).hopR(15.6f).hopR(19.4f)

    /** 20, round 2: up the stairs, hop the tripwire on the ledge, off its end, and back left along the lane whose floor goes dark behind you, to the door. */
    fun l20r2(b: Bot) = l20r2Stairs(b).rightTo(23.8f).rightJump(0.35f).landRight().rightUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(15.5f).left(1f)

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

    /** 23: ride the on-ramp and step off onto the deck, hop the roadworks, ride the last lift only as far as the exit deck and jump off it. */
    fun l23(b: Bot) = b.hopR(5.3f).rightUntil { it.player.box.cx > 13f }.hopR(18.0f).rightUntil { it.group('c').oy < -3.9f }.leftTo(19f).left(1f)

    /** 24: along the top deck (wait for the stalactites to fall), off the end, back under the deck without stopping, hop the saw out of the back wall, to the door. */
    fun l24(b: Bot) = b.rightTo(16.9f).waitFor { it.group('V').oy > 6f }.rightUntil { it.player.box.cx > 28f }.rightUntil { it.player.grounded && it.player.box.b > 14.5f }
        .leftUntil { sawAheadLeft(it, 5.5f) }.leftJump(0.35f).landLeft().left(1.5f)

    // ---------- block B (25-32): the rebuilt rooms ----------

    /** 25, from the ground right of the pit: up the rack (three hops), back over the stones of the top floor, to the door on the platform. */
    fun l25Top(b: Bot) = b.rightTo(24.0f).rightJump(0.35f).landRight().rightJump(0.35f).landRight().leftJump(0.35f).landLeft()
        .leftTo(22.8f).leftJump(0.35f).landLeft().leftTo(16.2f).leftJump(0.35f).landLeft().left(1.5f)

    /** 25: over the three belts (hop off the end of each, off the third as you land), then [l25Top]. */
    fun l25(b: Bot) = l25Top(b.rightUntil { it.player.box.cx > 7.2f }.rightJump(0.35f).landRight().rightUntil { it.player.box.cx > 13.2f }.rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight())

    /** 25, round 2: the belts crumble faster and the second and third sit one tile further on, so the hops start one tile later. */
    fun l25r2(b: Bot) = l25Top(b.rightUntil { it.player.box.cx > 9.0f }.rightJump(0.35f).landRight().rightUntil { it.player.box.cx > 15.2f }.rightJump(0.35f).landRight()
        .rightJump(0.35f).landRight())

    /** 26: hop the piece that drops on the top deck, off its left end onto the middle deck, right along it (the queue follows), through the gap to the lane, left to the door (hop the low block). */
    fun l26(b: Bot) = b.leftTo(23.6f).leftJump(0.35f).landLeft().leftUntil { it.player.box.b > 8f }.rightUntil { it.player.grounded && it.player.box.b > 9.5f }
        .rightTo(18.2f).rightJump(0.35f).landRight().rightUntil { it.player.box.cx > 25.3f }.leftUntil { it.player.grounded && it.player.box.b > 14.5f }.leftTo(17.2f).leftJump(0.35f).landLeft().left(1.5f)

    /** 26, round 2: along the top deck and off its left end, right along the middle deck (hop the queue that comes toward you), through the gap, left along the lane (hop the second queue) to the door. */
    fun l26r2(b: Bot) = b.leftUntil { it.player.box.b > 8f }.rightUntil { wallOnTheRight(it, 'S', 2.55f) }.rightJump(0.5f).landRight()
        .rightUntil { it.player.box.cx > 25.3f }.leftUntil { it.player.grounded && it.player.box.b > 14.5f }.leftUntil { wallOnTheLeft(it, 'Q', 2.7f) }.leftJump(0.5f).landLeft().left(2.5f)

    /** 27: stop as the first packet starts to fall (the belt carries you back), hop up onto it, back off the edge, onto the second, onto the third and along the walkway. */
    fun l27(b: Bot) = b.rightUntil { it.group('c').mode == GroupMode.FALL }.waitFor { it.group('c').oy > 12.9f }.rightTo(4.8f).rightJump(0.3f).landRight()
        .waitFor { it.group('d').oy > 11.9f }.rightTo(9.5f).rightJump(0.3f).landRight()
        .leftTo(12.2f).waitFor { it.group('e').oy > 10.9f }.rightTo(12.6f).rightJump(0.3f).landRight()
        .rightUntil { it.group('f').mode == GroupMode.FALL }.waitFor { it.group('f').oy > 9.9f }.hopR(21.1f).right(3f)

    /** 28: into the tunnel, out of it in front of the first gate; wait for each gate to go dark, cross, on into the second tunnel, and back along the walkway to the door. */
    fun l28(b: Bot) = b.rightUntil { it.player.box.cx > 13.3f }.wait(0.05f).waitFor(cond = clear('M')).rightUntil { it.player.box.cx > 17.8f }
        .wait(0.05f).waitFor(cond = clear('N')).rightUntil { it.player.box.cx > 22.8f }.wait(0.05f).waitFor(cond = clear('P')).rightUntil { it.player.box.b < 9.5f }
        .leftUntil { it.player.box.cx < 19.6f }.wait(0.05f).waitFor(cond = clear('O')).left(3f)

    /** The pendulum saw at [x] stays clear of the lane (up, not coming down) for the next [from]..[to] seconds. */
    fun pendulumCalm(w: World, x: Float, from: Float = 0.1f, to: Float = 0.5f): Boolean {
        val s = w.saws.firstOrNull { kotlin.math.abs(it.x - x) < 0.2f && it.path != null } ?: return false
        return (0..8).all { k -> s.path!!.at(w.time - s.t0 + from + (to - from) * k / 8f).second < 12.2f }
    }

    /** 29: stop in front of each pendulum saw and slip under it when it is up. */
    fun l29(b: Bot) = b.rightTo(7.7f).waitFor { pendulumCalm(it, 10f) }.rightUntil { it.player.box.cx > 11.7f }
        .rightTo(13.7f).waitFor { pendulumCalm(it, 16f) }.rightUntil { it.player.box.cx > 17.7f }
        .rightTo(19.7f).waitFor { pendulumCalm(it, 22f) }.right(3f)

    /** 29, round 2: slip under the first pendulum as before, wait in front of the lane for the second saw to lap off into the upper corner, run the lane, and slip under the last pendulum. */
    fun l29r2(b: Bot) = b.rightTo(6.7f).waitFor { pendulumCalm(it, 10f, 0.1f, 0.65f) }.rightUntil { it.player.box.cx > 11.7f }
        .waitFor { w -> w.saws.any { it.path?.loop == true && it.x > 21f && it.y < 12.2f } }.rightUntil { it.player.box.cx > 21.9f }
        .waitFor { pendulumCalm(it, 25f) }.right(3f)

    /** 30: through the tunnel up to the ledge, hop the hole, to the door and through the breach, along the ledge and down onto the lane, through the second tunnel, hop the spikes. */
    fun l30(b: Bot) = b.rightUntil { it.player.box.b < 9.5f }.hopR(17.4f).rightUntil(4f) { it.cracks.isNotEmpty() }
        .rightUntil(3f) { it.cracks.any { c -> c.fell } }.rightUntil { it.player.box.b > 12f }.rightTo(roomX(1, 25.7f)).rightJump(0.35f).landRight().right(2f)

    /** Blinking group [id] is solid during the whole stretch from [from] to [to] seconds ahead. */
    fun stoneUp(w: World, id: Char, from: Float, to: Float): Boolean {
        val g = w.group(id)
        val b = g.blink ?: return false
        return (0..6).all { k -> b.solidAt(w.time - g.blinkT0 + from + (to - from) * k / 6f) }
    }

    /** 31: wait on the deck for the first stone, hop over the three stones as each one is up, off the end of the deck, and back left along the lane to the door. */
    fun l31(b: Bot) = b.rightTo(7.5f).waitFor { stoneUp(it, 's', 0.3f, 0.75f) }.rightJump(0.35f).landRight()
        .rightTo(12.5f).waitFor { stoneUp(it, 't', 0.3f, 0.75f) }.rightJump(0.35f).landRight()
        .rightTo(17.4f).waitFor { stoneUp(it, 'u', 0.3f, 0.75f) }.rightJump(0.35f).landRight()
        .rightUntil { it.player.box.cx > 27.4f }.leftUntil { it.player.grounded && it.player.box.b > 14.5f }.left(6f)

    /** 32: along the belt, hop the LED, through the tunnel, hop the belt that turns, up the tunnel to the deck, back left (hop the LED, hop the belt that turns up), onto the switch, down to the lane, hop the spikes, the same way again, and let the belt carry you into the second tunnel. */
    fun l32(b: Bot) = b.hopR(6.4f).rightUntil { it.player.box.cx > 16.9f }.rightJump(0.35f).landRight()
        .rightUntil { it.player.box.b < 9.5f }.hopL(26.6f)
        .leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().leftJump(0.35f).landLeft().leftTo(4.6f)
        .leftUntil { it.player.box.b > 12f }.landLeft().rightJump(0.35f).landRight()
        .hopR(6.4f).rightUntil { it.player.box.cx > 16.9f }.right(4f)

    // ---------- block C (33-40): the rebuilt rooms ----------

    /** 33: along the lane (hop the hole, hop it again), up the two steps and left onto the deck, stop when the block drops, climb it and hop the spikes to the door. */
    fun l33(b: Bot) = b.hopR(6.9f, 0.5f).hopR(12.8f, 0.5f).rightTo(18.3f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().hopL(23.2f, 0.5f)
        .leftUntil { it.group('c').mode == GroupMode.FALL }.waitFor { it.group('c').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .hopL(15.6f, 0.5f).hopL(8.2f, 0.5f).left(1.5f)

    /** 33, round 2: hop the LEDs where round 1 hopped the hole, hop the hole that opens under the echo, up the steps, along the deck without stopping (the block drops late), hop the spikes. */
    fun l33r2(b: Bot) = b.hopR(8.8f, 0.5f).hopR(15.5f, 0.5f).rightTo(21.3f).rightJump(0.5f).landRight().rightJump(0.5f).landRight().hopL(26.2f, 0.5f)
        .hopL(8.2f, 0.5f).left(1.5f)

    /** 34: along the lane until the gravity turns over, up onto the ceiling and back left along it, into the link that comes up, off the ledge, hop the LEDs and run to the door. */
    fun l34(b: Bot) = b.rightTo(14f).rightUntil { it.player.box.cy < 3f }.leftUntil { it.player.box.cy > 8f }
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.hopR(15.4f, 0.5f).right(3f)

    /** 34, round 2: hop the LEDs, on along the lane to the far end where the gravity turns over, left along the ceiling, wait for the stale link to be renewed and step in; drop off the ledge, hop the LEDs again and run to the door. */
    fun l34r2(b: Bot) = b.hopR(17.8f, 0.5f).rightTo(25f).rightUntil { it.player.box.cy < 3f }.leftUntil { it.player.box.cx < 18.2f }
        .waitFor { w -> w.links.first { it.id == '3' }.to == 10 to 10 }.leftUntil { it.player.box.cy > 8f }
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.hopR(17.8f, 0.5f).right(3.5f)

    /** 35: left along the lane (hop the two holes), up the two steps, onto the deck and right through the duct against its belt, hop along the second belt to the door. */
    fun l35(b: Bot) = b.hopL(24.0f, 0.5f).hopL(18.0f, 0.5f).leftTo(12.7f).leftJump(0.5f).landLeft().leftJump(0.5f).landLeft().hopR(7.8f, 0.5f)
        .rightUntil { it.player.box.cx > 23f }.hopR(24.4f, 0.5f).hopR(26.0f, 0.5f).right(2f)

    /** 36: along the deck (hop the saw that rolls at you, hop the second one as you land), off its end, left along the lane (hop the saw from the left, wait for the guard to turn, run past it) to the door. */
    fun l36(b: Bot) = b.rightUntil { sawAhead(it, 4.0f) }.rightJump(0.5f).landRight().rightUntil { sawAhead(it, 4.9f) }.rightJump(0.5f).landRight()
        .rightUntil { it.player.grounded && it.player.box.b > 14.5f }.leftUntil { sawAheadRight(it, 4.3f) }.leftJump(0.5f).landLeft()
        .leftUntil { sawAheadRight(it, 5.7f) }.leftJump(0.5f).landLeft().left(2.5f)

    /** On the ground, a saw rolls toward the player from the right... ahead on the left within [d] tiles. */
    fun sawAheadRight(w: World, d: Float) = w.player.grounded && w.saws.any { it.x < w.player.box.cx && w.player.box.cx - it.x <= d && kotlin.math.abs(it.y - w.player.box.cy) < 1.5f }

    /** 37: left onto the first switch, back right through the dark wall, stop where the block drops and climb it, up onto the steps, over to the roof and onto the second switch, run on off the roof and through the wall to the door. */
    fun l37(b: Bot) = b.leftTo(2.5f).rightTo(5.5f)
        .rightUntil { it.group('c').mode == GroupMode.FALL }.waitFor { it.group('c').let { g -> g.mode == GroupMode.IDLE && g.oy > 1f } }
        .hopR(17.2f, 0.5f).rightJump(0.5f).landRight().hopR(22.6f, 0.5f).rightTo(29.4f)
        .leftUntil { it.player.box.cx < 25.9f }.rightUntil { it.player.box.cx > 31f }.right(4f)

    /** 38: hop the first hole (it leads home), over the sinking ground and into the second hole, along the roof from the far end to the door, never standing on the planks. */
    fun l38(b: Bot) = b.hopR(12.8f, 0.5f).rightUntil { it.player.box.cx > 25f }.leftTo(7.0f).leftJump(0.5f).landLeft().hopL(4.4f, 0.5f).left(1f)

    val solutions: Map<Int, (Bot) -> Bot> = mapOf(
        17 to ::l17, 18 to ::l18, 19 to ::l19, 20 to ::l20, 21 to ::l21, 22 to ::l22, 23 to ::l23, 24 to ::l24,
    )
}
