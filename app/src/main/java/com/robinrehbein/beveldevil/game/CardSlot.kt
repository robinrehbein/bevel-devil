package com.robinrehbein.beveldevil.game

import kotlin.math.abs

/**
 * Where Mephi's flying trap card settles over the room in view: the stage center, or [SIDE] tiles to the left or right,
 * at the usual height, higher up or lower down. It keeps clear of the player and of what the trap acts on
 * ([World.footprint]), so the trap stays readable. All coordinates are tiles of the room in view.
 */
object CardSlot {
    /** [side] -1 left, 0 center, 1 right; [lift] tiles up (negative) or down from the usual height. */
    data class Slot(val side: Int, val lift: Float)

    /** Tiles between the center and a side slot (64 stage pixels). */
    const val SIDE = 8f
    /** Tiles the card moves up or down when all three slots at the usual height cover the trap. */
    const val HIGH = -3f
    const val LOW = 5f

    /** The card as drawn once settled (UiPainter: 35×48 stage pixels around (128, 52)), with a little room for its sway. */
    const val CX = 16f
    const val CY = 6.5f
    const val HALF_W = 2.5f
    const val HALF_H = 3.3f

    /**
     * The player must not be under this (the rule from before traps were taken into account, kept as it was): about
     * 5.5×7.5 tiles around (16, 8.25), and the player with [PLAYER_MX]×[PLAYER_MY] tiles of room to walk on.
     */
    const val GUARD_CY = 8.25f
    const val GUARD_HALF_W = 2.75f
    const val GUARD_HALF_H = 3.75f
    const val PLAYER_MX = 2f
    const val PLAYER_MY = 1f
    /** Covering the player counts this much more than covering the trap. */
    private const val PLAYER_WEIGHT = 4f

    /** The card in slot [s], as drawn. */
    fun area(s: Slot) = Area(CX + s.side * SIDE - HALF_W, CY + s.lift - HALF_H, CX + s.side * SIDE + HALF_W, CY + s.lift + HALF_H)

    /** What the player (grown by the room they walk on in) must stay out of, for slot [s]. */
    fun guard(s: Slot) = Area(CX + s.side * SIDE - GUARD_HALF_W, GUARD_CY + s.lift - GUARD_HALF_H, CX + s.side * SIDE + GUARD_HALF_W, GUARD_CY + s.lift + GUARD_HALF_H)

    /**
     * The slot for a card played while the player is at [player], over a trap acting on [traps]. Without traps it is
     * the old rule: the center, unless the player stands under it, then the side away from them. With traps it is the
     * first slot that covers neither (center, away from the player, toward the player; then the same higher up, then
     * lower down), or else the one that covers least.
     */
    fun choose(player: Area, traps: List<Area> = emptyList()): Slot {
        val away = if ((player.x0 + player.x1) / 2 > CX) -1 else 1
        if (traps.isEmpty()) return Slot(if (hits(player, Slot(0, 0f))) away else 0, 0f)
        val slots = listOf(0f, HIGH, LOW).flatMap { lift -> listOf(0, away, -away).map { Slot(it, lift) } }
        return slots.firstOrNull { cost(it, player, traps) == 0f } ?: slots.minBy { cost(it, player, traps) }
    }

    /** The player (with the room they walk on in) is under the card in slot [s]. */
    fun hits(player: Area, s: Slot): Boolean {
        val g = guard(s)
        val pcx = (player.x0 + player.x1) / 2
        val pcy = (player.y0 + player.y1) / 2
        return abs(pcx - (g.x0 + g.x1) / 2) < GUARD_HALF_W + PLAYER_MX + player.w / 2 &&
            abs(pcy - (g.y0 + g.y1) / 2) < GUARD_HALF_H + PLAYER_MY + player.h / 2
    }

    /** How much the card in slot [s] covers: the player (weighed more) and the traps, in square tiles. */
    fun cost(s: Slot, player: Area, traps: List<Area>): Float {
        val c = area(s)
        return guard(s).overlap(player.grow(PLAYER_MX, PLAYER_MY)) * PLAYER_WEIGHT + traps.sumOf { c.overlap(it).toDouble() }.toFloat()
    }
}
