package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import com.robinrehbein.beveldevil.game.Mood
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Mephi, the croupier of hell: a 30×30 pixel sprite built from shapes, with bevel shading
 * (light top-left, shadow bottom-right) and a face per mood.
 */
object Mephi {
    const val N = 30
    private const val OUT = 0xFF1A0610.toInt()
    private const val YEL = 0xFFFFD23F.toInt()
    private const val YEL_D = 0xFFD9982A.toInt()
    private const val WHITE = 0xFFFFF4E0.toInt()
    private const val MOUTH = 0xFF3A0612.toInt()
    private const val PINK = 0xFFFF8FA0.toInt()
    private const val GOLD = 0xFFFFD98A.toInt()
    private const val GOLD_D = 0xFFE8A84A.toInt()

    private val base: IntArray = buildBase()
    private val cache = HashMap<Int, Bitmap>()

    /** [look] is -1, 0 or 1 (pupil offset). */
    fun sprite(mood: Mood, blink: Boolean, look: Int): Bitmap {
        val key = mood.ordinal * 100 + (if (blink) 10 else 0) + (look + 1)
        return cache.getOrPut(key) {
            val px = base.copyOf()
            face(px, mood, blink, look)
            Bitmap.createBitmap(px, N, N, Bitmap.Config.ARGB_8888)
        }
    }

    private fun buildBase(): IntArray {
        val mask = IntArray(N * N) // 0 none, 1 skin, 2 horn, 3 suit, 4 tie, 5 shirt
        fun at(x: Int, y: Int) = if (x < 0 || y < 0 || x >= N || y >= N) 0 else mask[y * N + x]
        fun set(x: Float, y: Float, v: Int) {
            val xi = x.roundToInt(); val yi = y.roundToInt()
            if (xi in 0 until N && yi in 0 until N) mask[yi * N + xi] = v
        }
        val cx = 14.5f
        val cy = 14f
        for (y in 23 until N) for (x in 0 until N) if (abs(x - cx) <= 7 + (y - 23) * 1.7f) set(x.toFloat(), y.toFloat(), 3)
        for (y in 23 until 27) for (x in 0 until N) if (abs(x - cx) <= 3 - (y - 23) * 0.9f) set(x.toFloat(), y.toFloat(), 5)
        for (y in 0 until N) for (x in 0 until N) {
            val dx = (x - cx) / 8.6f
            val dy = (y - cy) / (if (y < cy) 8f else 7.6f)
            if (abs(dx).pow(2.4f) + abs(dy).pow(2.4f) <= 1f) set(x.toFloat(), y.toFloat(), 1)
        }
        for (y in 10..16) {
            val xmin = (if (y <= 12) 2 + (y - 10) * 0.5f else 3 + (y - 12) * 0.6f).roundToInt()
            for (x in xmin..6) { set(x.toFloat(), y.toFloat(), 1); set((29 - x).toFloat(), y.toFloat(), 1) }
        }
        for ((x, y) in listOf(14 to 22, 15 to 22, 14 to 23, 15 to 23, 14 to 24, 15 to 24, 15 to 25)) set(x.toFloat(), y.toFloat(), 1)
        var t = 0f
        while (t <= 1f) {
            val bx = (1 - t) * (1 - t) * 9 + 2 * (1 - t) * t * 3.5f + t * t * 4
            val by = (1 - t) * (1 - t) * 8 + 2 * (1 - t) * t * 5.5f
            val r = 1.7f * (1 - t) + 0.35f
            for (yy in -2..2) for (xx in -2..2) {
                if (xx * xx + yy * yy > r * r) continue
                if (at((bx + xx).roundToInt(), (by + yy).roundToInt()) != 1 || by + yy < 7) {
                    set(bx + xx, by + yy, 2); set(29 - (bx + xx), by + yy, 2)
                }
            }
            t += 0.02f
        }
        for ((x, y) in listOf(11 to 25, 12 to 25, 13 to 25, 16 to 25, 17 to 25, 18 to 25, 12 to 24, 17 to 24, 12 to 26, 17 to 26, 14 to 25, 15 to 25)) set(x.toFloat(), y.toFloat(), 4)

        val pal = mapOf(
            1 to intArrayOf(0xFFE2344E.toInt(), 0xFFFF6F82.toInt(), 0xFF9C1830.toInt()),
            2 to intArrayOf(0xFFF0D9A8.toInt(), 0xFFFFF6DC.toInt(), 0xFFB3905A.toInt()),
            3 to intArrayOf(0xFF2A1640.toInt(), 0xFF46295F.toInt(), 0xFF170A26.toInt()),
            4 to intArrayOf(0xFFE8A84A.toInt(), 0xFFFFD98A.toInt(), 0xFF9A6420.toInt()),
            5 to intArrayOf(0xFFEFE3CF.toInt(), 0xFFFFFFFF.toInt(), 0xFFC9B9A0.toInt()),
        )
        val out = IntArray(N * N)
        for (y in 0 until N) for (x in 0 until N) {
            val c = at(x, y)
            if (c == 0) continue
            val nb = intArrayOf(at(x - 1, y), at(x + 1, y), at(x, y - 1), at(x, y + 1))
            val outline = 0 in nb || (c == 3 && 1 in nb) || (c == 2 && 1 in nb) ||
                (c == 4 && (3 in nb || 5 in nb)) || (c == 5 && 3 in nb)
            out[y * N + x] = when {
                outline -> OUT
                at(x + 2, y + 2) != c -> pal.getValue(c)[2]
                at(x - 2, y - 2) != c -> pal.getValue(c)[1]
                else -> pal.getValue(c)[0]
            }
        }
        return out
    }

    private fun face(px: IntArray, mood: Mood, blink: Boolean, look: Int) {
        fun p(x: Int, y: Int, c: Int) { if (x in 0 until N && y in 0 until N) px[y * N + x] = c }
        fun line(x0: Int, y0: Int, x1: Int, y1: Int) {
            var x = x0; var y = y0
            val dx = abs(x1 - x0); val dy = -abs(y1 - y0)
            val sx = if (x0 < x1) 1 else -1; val sy = if (y0 < y1) 1 else -1
            var e = dx + dy
            while (true) {
                p(x, y, OUT)
                if (x == x1 && y == y1) break
                val e2 = 2 * e
                if (e2 >= dy) { e += dy; x += sx }
                if (e2 <= dx) { e += dx; y += sy }
            }
        }
        fun eye(ex: Int, ey: Int) {
            when {
                mood == Mood.LAUGH -> { p(ex - 1, ey, OUT); p(ex, ey - 1, OUT); p(ex + 1, ey, OUT) }
                blink -> for (i in -1..1) p(ex + i, ey, OUT)
                mood == Mood.SHOCK -> { for (i in -1..1) for (j in -2..0) p(ex + i, ey + j, WHITE); p(ex, ey - 1, OUT) }
                mood == Mood.SULK -> { for (i in -1..1) { p(ex + i, ey - 1, OUT); p(ex + i, ey, YEL) }; p(ex + look, ey, OUT) }
                else -> {
                    for (i in -1..1) { p(ex + i, ey - 1, YEL); p(ex + i, ey, if (i == 0) YEL else YEL_D); p(ex + i, ey - 2, OUT) }
                    p(ex + look, ey - 1, OUT); p(ex + look, ey, OUT)
                }
            }
        }
        eye(11, 15); eye(18, 15)
        when (mood) {
            Mood.GRIN -> {
                line(8, 11, 12, 12); line(17, 11, 20, 9)
                line(9, 17, 10, 18); for (x in 10..19) p(x, 18, OUT); line(19, 18, 21, 16)
                for (x in 11..18) p(x, 19, WHITE); for (x in 12..17) p(x, 20, MOUTH)
                p(12, 20, WHITE); p(17, 20, WHITE); p(10, 19, OUT); p(19, 19, OUT); p(11, 20, OUT); p(18, 20, OUT)
                for (x in 12..17) p(x, 21, OUT)
            }
            Mood.LAUGH -> {
                line(8, 11, 12, 11); line(17, 11, 21, 11)
                for (x in 10..19) { p(x, 17, OUT); p(x, 18, WHITE) }
                for (y in 19..20) for (x in 10..19) p(x, y, MOUTH)
                for (x in 13..16) p(x, 20, PINK); for (x in 11..18) p(x, 21, OUT)
                for (y in 18..20) { p(9, y, OUT); p(20, y, OUT) }
            }
            Mood.SULK -> {
                line(8, 11, 12, 13); line(21, 11, 17, 13)
                for (x in 12..17) p(x, 19, OUT); p(11, 20, OUT); p(18, 20, OUT)
            }
            Mood.SHOCK -> {
                line(8, 10, 12, 10); line(17, 10, 21, 10)
                for (x in 13..16) { p(x, 18, OUT); p(x, 21, OUT) }
                for (y in 19..20) { p(12, y, OUT); p(17, y, OUT); for (x in 13..16) p(x, y, MOUTH) }
            }
        }
        p(4, 17, OUT); p(4, 18, GOLD); p(4, 19, GOLD_D); p(3, 19, OUT); p(5, 19, OUT); p(4, 20, OUT)
    }
}
