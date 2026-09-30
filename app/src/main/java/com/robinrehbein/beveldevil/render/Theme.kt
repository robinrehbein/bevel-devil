package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap

/** A backdrop bitmap plus its blinking lights: [leds] holds x, y, color and phase (0..255) per light. */
class Backdrop(val bmp: Bitmap, val leds: IntArray = IntArray(0))

/**
 * The look of one world: what solid tiles, the surrounding rock and spikes are made of, and the far scenery.
 * Hell CRT frame, HUD, hero and door are shared by all worlds. A new world adds one [Theme] here.
 */
class Theme(
    val stone: Stone,
    val rock: Stone,
    val spike: Spike,
    /** RGB of the pit where a level's edge is open. */
    val pit: IntArray,
    /** Rising embers and glowing cracks in the rock: hell only. */
    val fire: Boolean,
    make: () -> Backdrop,
) {
    val backdrop by lazy(make)
}

object Themes {
    private fun scenery(make: (Int, Int) -> Backdrop) = { make(PW + 2 * WorldPainter.PARALLAX, PH) }

    val HELL = Theme(GOLD_STONE, ROCK_STONE, BONE_SPIKE, intArrayOf(7, 3, 13), true, scenery { w, h -> Backdrop(Fx.backdrop(w, h, 15 * TS)) })
    val DATA_CENTER = Theme(STEEL_STONE, CHASSIS_STONE, STEEL_SPIKE, intArrayOf(3, 5, 12), false, scenery(DataCenter::backdrop))

    /** The theme of world [n] (1-based). World 3 (Platine) has no levels yet and falls back to hell. */
    fun of(n: Int) = when (n) { 2 -> DATA_CENTER; else -> HELL }
}

val STEEL_STONE = Stone(
    0xFF566379.toInt(), 0xFF5F6D85.toInt(), 0xFF4D5A70.toInt(), 0xFF46526A.toInt(),
    0xFFBCCAE2.toInt(), 0xFF8898B4.toInt(), 0xFF0E1420.toInt(), 0xFF252F46.toInt(),
    0xFF6B7A94.toInt(), 0xFF333F57.toInt(), 0xFFE8F2FF.toInt(), true, true,
)

val CHASSIS_STONE = Stone(
    0xFF0F1626.toInt(), 0xFF131B2E.toInt(), 0xFF0C1220.toInt(), 0xFF0A101C.toInt(),
    0xFF27334C.toInt(), 0xFF1A2438.toInt(), 0xFF04060D.toInt(), 0xFF090E19.toInt(),
    0xFF18223A.toInt(), 0xFF090E19.toInt(), 0xFF27334C.toInt(), false,
)

val STEEL_SPIKE = Spike(0xFFEAF2FF.toInt(), 0xFF566379.toInt(), 0xFFB4C2D8.toInt(), 0xFF8898B4.toInt(), 0xFF8898B4.toInt(), 0xFF64728C.toInt(), 0xFFFF6A70.toInt(), 0xFFC02840.toInt())
