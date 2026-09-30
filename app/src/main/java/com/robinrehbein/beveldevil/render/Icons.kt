package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import com.robinrehbein.beveldevil.game.Card

/** 16×16 pixel icons for the trap cards. */
object Icons {
    private const val GOLD = 0xFFE8A84A.toInt()
    private const val GOLD_HI = 0xFFFFD98A.toInt()
    private const val GOLD_LO = 0xFF8F4F1F.toInt()
    private const val INK = 0xFF2A0710.toInt()
    private const val BONE = 0xFFFFF4EA.toInt()
    private const val MINT = 0xFF6CF2C2.toInt()
    private const val MINT_HI = 0xFFB8FFE6.toInt()
    private const val DARK = 0xFF1A0B1E.toInt()
    private const val RED = 0xFFE2344E.toInt()
    private const val STEEL = 0xFFD9DBE6.toInt()

    private val cache = HashMap<Any, Bitmap>()

    fun card(c: Card): Bitmap = cache.getOrPut(c) { make { draw(c) } }
    val back: Bitmap by lazy { make { horns() } }
    val skull: Bitmap by lazy {
        val rows = listOf(".####.", "######", "#..#..", "######", ".#.#.#")
        val px = IntArray(6 * 5)
        rows.forEachIndexed { y, r -> r.forEachIndexed { x, ch -> if (ch == '#') px[y * 6 + x] = BONE } }
        Bitmap.createBitmap(px, 6, 5, Bitmap.Config.ARGB_8888)
    }
    val lock: Bitmap by lazy {
        val rows = listOf("..##..", ".#..#.", ".#..#.", "######", "##..##", "######")
        val px = IntArray(6 * 6)
        rows.forEachIndexed { y, r -> r.forEachIndexed { x, ch -> if (ch == '#') px[y * 6 + x] = INK } }
        Bitmap.createBitmap(px, 6, 6, Bitmap.Config.ARGB_8888)
    }

    private class Pen {
        val px = IntArray(256)
        fun p(c: Int, x: Int, y: Int, w: Int = 1, h: Int = 1) {
            for (yy in y until y + h) for (xx in x until x + w) if (xx in 0..15 && yy in 0..15) px[yy * 16 + xx] = c
        }
        fun tile(x: Int, y: Int) { p(GOLD, x, y, 5, 5); p(GOLD_HI, x, y, 5, 1); p(GOLD_HI, x, y, 1, 5); p(GOLD_LO, x, y + 4, 5, 1); p(GOLD_LO, x + 4, y, 1, 5) }
        fun hero(x: Int, y: Int, flip: Boolean = false) {
            p(MINT, x, y, 6, 6); p(MINT_HI, x, y + if (flip) 5 else 0, 6, 1)
            val ey = if (flip) y + 3 else y + 1
            p(0xFFFFFFFF.toInt(), x + 1, ey, 2, 2); p(0xFFFFFFFF.toInt(), x + 4, ey, 2, 2); p(DARK, x + 2, ey + 1); p(DARK, x + 5, ey + 1)
        }
        fun spike(x: Int, y: Int) { p(BONE, x + 1, y, 1, 1); p(BONE, x + 1, y + 1, 2, 1); p(BONE, x, y + 2, 4, 2) }
        fun door(x: Int, y: Int) { p(GOLD, x, y, 7, 11); p(DARK, x + 1, y + 2, 5, 9); p(GOLD, x + 1, y, 5, 2); p(GOLD_HI, x + 4, y + 6) }
        /** Draws [rows] (16 wide at most) with one color per letter; '.' is empty. */
        fun art(rows: List<String>, colors: Map<Char, Int>, x0: Int = 0, y0: Int = 0) {
            rows.forEachIndexed { y, r -> r.forEachIndexed { x, ch -> colors[ch]?.let { p(it, x0 + x, y0 + y) } } }
        }
        fun horns() { p(GOLD, 3, 2, 2, 2); p(GOLD, 4, 4, 2, 2); p(GOLD, 5, 6, 2, 3); p(GOLD, 11, 2, 2, 2); p(GOLD, 10, 4, 2, 2); p(GOLD, 9, 6, 2, 3); p(GOLD, 6, 9, 4, 3); p(GOLD, 7, 12, 2, 2) }

        fun draw(c: Card) = when (c) {
            Card.COLLAPSE -> { tile(0, 11); tile(11, 11); tile(5, 13); p(INK, 6, 8, 1, 3); p(INK, 9, 7, 1, 4); p(INK, 7, 5, 1, 2) }
            Card.SPIKE_SEED -> { tile(0, 11); tile(5, 11); tile(10, 11); spike(0, 7); spike(6, 7); spike(11, 7) }
            Card.SHY_DOOR -> { door(7, 3); for (y in listOf(5, 8, 11)) p(INK, 1, y, 4, 1) }
            Card.HEADBUTT -> { p(GOLD, 3, 0, 10, 5); p(GOLD_HI, 3, 0, 10, 1); p(GOLD_LO, 3, 4, 10, 1); p(INK, 5, 6, 1, 2); p(INK, 10, 6, 1, 2); hero(5, 9) }
            Card.UPSIDE_DOWN -> { p(GOLD, 0, 0, 16, 3); hero(5, 3, flip = true); p(INK, 7, 11, 2, 4); p(INK, 6, 12, 4, 1); p(INK, 5, 13, 6, 1) }
            Card.TWISTED -> { p(INK, 2, 4, 12, 1); p(INK, 3, 3, 1, 3); p(INK, 2, 4); p(INK, 2, 11, 12, 1); p(INK, 12, 10, 1, 3); hero(5, 5) }
            Card.DEVIL_SAW -> { for (y in 0..15) for (x in 0..15) { val dx = x - 7.5f; val dy = y - 7.5f; val d = dx * dx + dy * dy; if (d < 42f) p(if ((x + y) % 4 == 0 && d > 28f) INK else STEEL, x, y) }; p(RED, 6, 6, 4, 4) }
            Card.GHOST_BLOCK -> { for (i in 2..13 step 2) { p(GOLD, i, 2); p(GOLD, i, 13); p(GOLD, 2, i); p(GOLD, 13, i) }; p(INK, 7, 5, 2, 1); p(INK, 9, 6, 1, 2); p(INK, 8, 8, 1, 1); p(INK, 8, 10, 1, 1) }
            Card.SINKING -> { tile(0, 3); tile(5, 3); tile(10, 3); p(INK, 7, 9, 2, 4); p(INK, 5, 11, 6, 1); p(INK, 6, 12, 4, 1); p(INK, 7, 13, 2, 1) }
            Card.DECOY -> { door(4, 2); p(RED, 2, 4, 2, 2); p(RED, 4, 6, 2, 2); p(RED, 6, 8, 2, 2); p(RED, 8, 10, 2, 2); p(RED, 10, 12, 2, 2); p(RED, 10, 4, 2, 2); p(RED, 4, 12, 2, 2) }
            Card.CRUMBLE -> { tile(0, 6); tile(5, 8); tile(10, 11); p(INK, 2, 7, 1, 3); p(INK, 7, 9, 1, 2); p(INK, 12, 12, 1, 2) }
            Card.SHORT_CIRCUIT -> {
                art(listOf("..........##....", ".........###....", "........###.....", ".......###......", "......#####.....", ".......#####....", ".........###....", "........###.....", ".......###......", ".......##......."), mapOf('#' to GOLD_HI), 0, 0)
                p(INK, 0, 14, 5, 1); p(INK, 11, 14, 5, 1); p(RED, 5, 13, 1, 2); p(RED, 10, 13, 1, 2); p(GOLD_HI, 7, 12, 2, 1); p(GOLD_HI, 6, 11); p(GOLD_HI, 9, 11)
            }
            Card.OVERCLOCKED -> {
                for (i in listOf(4, 7, 10)) { p(STEEL, i, 2, 2, 2); p(STEEL, i, 12, 2, 2); p(STEEL, 2, i, 2, 2); p(STEEL, 12, i, 2, 2) }
                p(INK, 3, 3, 10, 10); p(RED, 4, 4, 8, 8); p(GOLD, 5, 5, 6, 6); p(GOLD_HI, 6, 6, 4, 4); p(BONE, 7, 7, 2, 2)
            }
            Card.BIT_FLIP -> {
                art(listOf("###", "#.#", "#.#", "#.#", "###"), mapOf('#' to MINT), 1, 5)
                art(listOf(".#.", "##.", ".#.", ".#.", "###"), mapOf('#' to RED), 11, 5)
                p(INK, 6, 4, 4, 1); p(INK, 9, 3); p(INK, 9, 5); p(INK, 6, 11, 4, 1); p(INK, 6, 10); p(INK, 6, 12)
            }
            Card.BACKDRAFT -> {
                hero(1, 5)
                for (y in listOf(2, 7, 12)) { p(STEEL, 9, y, 7, 1); p(STEEL, 8, y - 1); p(STEEL, 8, y + 1); p(STEEL, 7, y) ; p(STEEL, 8, y) }
                p(INK, 8, 4, 1, 2); p(INK, 9, 9, 1, 2)
            }
            Card.THROTTLE -> {
                p(INK, 5, 1, 6, 10); p(BONE, 6, 2, 4, 9); p(INK, 3, 9, 10, 6); p(RED, 4, 10, 8, 4); p(RED, 7, 4, 2, 6); p(GOLD_HI, 4, 10, 2, 1)
                for (y in listOf(3, 5, 7)) p(INK, 11, y, 3, 1)
            }
            Card.BIOS -> {
                p(GOLD, 0, 1, 16, 11); p(GOLD_HI, 0, 1, 16, 1); p(DARK, 1, 2, 14, 9)
                p(MINT, 3, 4, 6, 1); p(MINT, 3, 6, 9, 1); p(MINT, 3, 8, 4, 1); p(MINT_HI, 8, 8, 2, 2)
                p(GOLD_LO, 6, 12, 4, 2); p(GOLD, 3, 14, 10, 1)
            }
            Card.GRAND_FINALE -> { horns(); p(RED, 6, 9, 4, 3); p(GOLD_HI, 7, 10, 2, 1) }
        }
    }

    private fun make(block: Pen.() -> Unit): Bitmap {
        val pen = Pen().apply(block)
        return Bitmap.createBitmap(pen.px, 16, 16, Bitmap.Config.ARGB_8888)
    }
}
