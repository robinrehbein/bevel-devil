package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import com.robinrehbein.beveldevil.game.Worlds

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
    /** Builds the far scenery for a canvas of the given width and height. */
    val build: (Int, Int) -> Backdrop,
    /** Doors of this world are USB-C sockets instead of the gold arch. */
    val usbDoor: Boolean = false,
) {
    /** The scenery behind the playfield, wider than it so it can slide for parallax. */
    val backdrop by lazy { build(PW + 2 * WorldPainter.PARALLAX, PH) }
}

object Themes {
    val HELL = Theme(GOLD_STONE, ROCK_STONE, BONE_SPIKE, intArrayOf(7, 3, 13), true, { w, h -> Backdrop(Fx.backdrop(w, h, 15 * TS)) })
    val DATA_CENTER = Theme(STEEL_STONE, CHASSIS_STONE, STEEL_SPIKE, intArrayOf(3, 5, 12), false, DataCenter::backdrop)
    /** World 3, acts 1 and 2: green solder mask. */
    val PCB_GREEN = Theme(CHIP_STONE, BOARD_GREEN_STONE, PIN_SPIKE, intArrayOf(2, 9, 6), false, { w, h -> Pcb.backdrop(w, h, Pcb.GREEN) }, true)
    /** World 3, act 3 (the BIOS finale): blue solder mask. */
    val PCB_BLUE = Theme(CHIP_STONE_BLUE, BOARD_BLUE_STONE, PIN_SPIKE, intArrayOf(2, 5, 16), false, { w, h -> Pcb.backdrop(w, h, Pcb.BLUE) }, true)

    /** First world-local level of World 3's last act. */
    const val BLUE_FROM = 33

    /** The theme of world [n] (1-based), for its first act. */
    fun of(n: Int) = of(n, 1)

    /** The theme of level [local] (1-based, within its world) of world [n]: World 3's act 3 is blue. */
    fun of(n: Int, local: Int) = when (n) {
        2 -> DATA_CENTER
        3 -> if (local >= BLUE_FROM) PCB_BLUE else PCB_GREEN
        else -> HELL
    }

    /** The theme of global level [i]. */
    fun forLevel(i: Int) = of(Worlds.of(i).number, Worlds.local(i))
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

/** Chip platforms: black IC packages with light bevels and copper pins. */
val CHIP_STONE = Stone(
    0xFF262B38.toInt(), 0xFF2C3242.toInt(), 0xFF20242F.toInt(), 0xFF1B1F29.toInt(),
    0xFF8792A8.toInt(), 0xFF4A5368.toInt(), 0xFF0B0D13.toInt(), 0xFF151821.toInt(),
    0xFF3A4254.toInt(), 0xFF12141B.toInt(), 0xFFDCE4F4.toInt(), true,
    pins = 0xFFE0A84A.toInt(), pinLo = 0xFF8A5E24.toInt(),
)

val CHIP_STONE_BLUE = Stone(
    0xFF232836.toInt(), 0xFF293040.toInt(), 0xFF1D212E.toInt(), 0xFF181C27.toInt(),
    0xFF8796B4.toInt(), 0xFF46516C.toInt(), 0xFF080B14.toInt(), 0xFF121624.toInt(),
    0xFF37415A.toInt(), 0xFF0F121C.toInt(), 0xFFE0ECFF.toInt(), true,
    pins = 0xFFCCD6E4.toInt(), pinLo = 0xFF727E94.toInt(),
)

/** The bare board around the playfield: darker than the backdrop, so the frame reads as a border. */
val BOARD_GREEN_STONE = Stone(
    0xFF050F0B.toInt(), 0xFF07130E.toInt(), 0xFF040C09.toInt(), 0xFF030A07.toInt(),
    0xFF153A2C.toInt(), 0xFF0D261D.toInt(), 0xFF020504.toInt(), 0xFF030806.toInt(),
    0xFF0A1C15.toInt(), 0xFF030806.toInt(), 0xFF153A2C.toInt(), false,
)

val BOARD_BLUE_STONE = Stone(
    0xFF040A17.toInt(), 0xFF060E1D.toInt(), 0xFF030812.toInt(), 0xFF02060E.toInt(),
    0xFF162F58.toInt(), 0xFF0C1C38.toInt(), 0xFF010309.toInt(), 0xFF02050C.toInt(),
    0xFF0A1830.toInt(), 0xFF02050C.toInt(), 0xFF162F58.toInt(), false,
)

/** Soldered leads: tin-silver with the same red danger tip as the data center's spikes. */
val PIN_SPIKE = Spike(0xFFF2F4F8.toInt(), 0xFF6E7888.toInt(), 0xFFC6CEDC.toInt(), 0xFF96A0B2.toInt(), 0xFF96A0B2.toInt(), 0xFF6A7486.toInt(), 0xFFFF6A70.toInt(), 0xFFC02840.toInt())
