package com.robinrehbein.beveldevil.game

/**
 * Scripted clean runs of the puzzle rooms of World 2, act 2 (levels 17-24), keyed by level number: what an informed
 * player does, with the real physics. Shared by [World2Test] (the level, its wrong approaches, its length) and
 * [World2DeckTest] (round-1 scripts against the rematches).
 */
object World2Rooms {
    /** Firewall gate [id] is dark and not glowing: safe to cross now. */
    fun clear(id: Char) = { w: World -> w.beams.none { it.laser.id == id && (it.lit || it.warn > 0f) } }

    /** Firewall gate [id] is lit or glowing. */
    fun busy(id: Char) = { w: World -> !clear(id)(w) }

    /** 17: over the duct, wait for the loose shelf piece to drop, up the stairs, left along the shelf to the pad, ride the bus. */
    fun l17(b: Bot) = b.rightTo(1.9f).rightJump(0.15f).landRight().wait(0.1f).rightJump(0.4f).landRight()
        .rightTo(10.2f).wait(0.6f).hopR(11f, 0.2f)
        .rightTo(23.5f).rightJump(0.4f).landRight().wait(0.1f).leftJump(0.4f).landLeft()
        .leftTo(15.6f).leftJump(0.35f).landLeft().leftTo(2.2f).wait(0.8f)
        .rightTo(1.9f).rightJump(0.15f).landRight().right(3f)

    /** 18, up to the pad on the rack behind the gate. */
    fun l18ToPad(b: Bot) = b.hopR(10f).rightTo(14.5f).waitFor(cond = clear('L')).rightTo(23f).rightJump(0.4f).landRight().rightTo(26.3f)

    /** 18: through the gate, press the pad, sit out the port scan on the rack, back through the gate, climb without stopping. */
    fun l18(b: Bot) = l18ToPad(b).wait(0.1f).waitFor { w -> w.beams.any { it.laser.id == 'S' && it.lit } }.waitFor(cond = clear('S'))
        .leftTo(17.5f).waitFor(cond = clear('L')).leftTo(13.8f).leftJump(0.4f).landLeft()
        .leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().left(1f)

    /** 18, round 2: climb first, press the pad where the door was, stand on the lowest step while the saw passes below. */
    fun l18r2(b: Bot) = b.hopR(10f).wait(0.15f).leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftJump(0.4f).landLeft().leftTo(2.4f)
        .rightUntil { it.player.box.cx > 11.3f && it.player.grounded && it.player.box.b < 13.5f }
        .waitFor { w -> w.saws.isNotEmpty() && w.saws.all { it.x < 9f } }
        .rightTo(14.5f).waitFor(cond = clear('L')).rightTo(22.5f).rightJump(0.4f).landRight().right(2f)

    /** 19, up to the moment the shelf swaps the controls. */
    fun l19ToShelf(b: Bot) = b.hopR(5.6f).hopR(14.6f).rightTo(24.8f).rightJump(0.3f).landRight().rightJump(0.4f).landRight()
        .leftTo(28.5f).wait(0.15f).leftJump(0.4f).landLeft()

    /** 19, swapped: over the shelf spikes, up to the step and the top floor, over the gap. */
    fun l19Swapped(b: Bot) = l19ToShelf(b).rightKeyLeftTo(18.2f).rightJump(0.35f).landRight().rightKeyLeftTo(9.6f)
        .rightJump(0.3f).jump(0.1f).waitFor { it.player.grounded }.wait(0.1f).leftKeyRightTo(8.4f).leftJump(0.4f).landLeft()
        .leftKeyRightTo(14.9f).leftJump(0.35f).landLeft()

    /** 19: the whole snake; after the gap the controls are back to normal. */
    fun l19(b: Bot) = l19Swapped(b).right(3f)

    /** 20, up to the ID scanner on the island's rack. */
    fun l20ToScanner(b: Bot) = b.waitFor(cond = clear('L')).hopR(9.6f).rightJump(0.4f).landRight().rightTo(15.4f)

    /** 20: wait for gate 2 on the rack, then jump over the second check. */
    fun l20(b: Bot) = l20ToScanner(b).waitFor(cond = busy('M')).waitFor(cond = clear('M')).hopR(22.6f).right(2f)

    /** 20, round 2: the same, but walk over the second check (the jump is the trap now). */
    fun l20r2(b: Bot) = l20ToScanner(b).waitFor(cond = busy('M')).waitFor(cond = clear('M')).rightTo(30f)

    /** 21, into the captive portal and onto the terms. */
    fun l21ToCage(b: Bot) = b.hopR(6.9f).hopR(12.9f).hopR(18.9f).rightUntil { it.links[0].hopTime > 0f }.wait(0.1f).leftTo(2.6f)

    /** 21: wait for the cage to open, walk (no jumping) and fall through the hole after the door, hop the portal. */
    fun l21(b: Bot) = l21ToCage(b).waitFor { !it.group('w').visible }.rightTo(24.3f).wait(0.7f)
        .rightTo(25.7f).rightJump(0.35f).landRight().right(1f)

    /** 22, back left, knock on the hidden step, up onto the shelf. */
    fun l22ToShelf(b: Bot) = b.leftTo(4.6f).leftJump(0.4f).landLeft().wait(0.15f).rightJump(0.4f).landRight()
        .rightTo(6.7f).wait(0.1f).jump(0.15f).wait(0.5f).leftTo(5.5f).wait(0.15f).rightJump(0.2f).jump(0.25f).wait(0.4f)
        .rightTo(7.2f).rightJump(0.4f).landRight()

    /** 22: drop onto the bouncer and walk straight off his far side. */
    fun l22(b: Bot) = l22ToShelf(b).rightTo(21.9f).right(3f)

    /** 23, lanes 1 and 2 (the jam: walk against it, hop the spikes). */
    fun l23ToLane3(b: Bot) = b.hopR(10.2f).rightUntil { it.links[0].hopTime > 0f }.hopR(4.3f).rightTo(13.9f).rightJump(0.4f).landRight()
        .rightUntil { it.links[1].hopTime > 0f }

    /** 23: on lane 3, jump the closed section. */
    fun l23(b: Bot) = l23ToLane3(b).rightTo(13.6f).rightJump(0.4f).landRight().right(3f)

    /** 24, up to rack 2: through gate 1 on the first rack, jump. */
    fun l24ToRack2(b: Bot) = b.rightTo(3.0f).rightJump(0.3f).landRight().rightTo(6.2f).waitFor(cond = clear('1')).rightTo(8.3f).rightJump(0.4f).landRight()

    /** 24, up to the top: off the hot spot where you land, each gate when it is dark, each jump from the far end of a rack. */
    fun l24Up(b: Bot) = l24ToRack2(b).rightTo(11.2f).waitFor { clear('2')(it) && clear('W')(it) }.rightTo(13.3f).rightJump(0.4f).landRight()
        .rightTo(16.2f).waitFor(cond = clear('3')).rightTo(18.3f).rightJump(0.4f).landRight()
        .rightTo(21.2f).waitFor(cond = clear('4')).rightTo(23.3f).rightJump(0.4f).landRight().rightTo(27f)

    /** 24: the door sinks to the start; climb down, waiting on each rack until the gate below it is dark. */
    fun l24(b: Bot) = l24Up(b).wait(0.5f).leftTo(24.5f).waitFor(cond = clear('4')).leftTo(19.4f)
        .waitFor(cond = clear('3')).leftTo(14.4f).waitFor(cond = clear('2')).leftTo(9.4f).waitFor(cond = clear('1')).left(3f)

    val solutions: Map<Int, (Bot) -> Bot> = mapOf(
        17 to ::l17, 18 to ::l18, 19 to ::l19, 20 to ::l20, 21 to ::l21, 22 to ::l22, 23 to ::l23, 24 to ::l24,
    )
}
