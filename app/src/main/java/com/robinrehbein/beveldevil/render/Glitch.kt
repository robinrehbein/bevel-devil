package com.robinrehbein.beveldevil.render

import android.graphics.Paint

/**
 * "Mephi is hacking the system": a short CRT glitch over the finished logical frame. Shifted scanline slices,
 * RGB-split and tinted bands, a few corrupted blocks and a flicker of garbage text. Reuses its buffers, so a
 * frame allocates nothing. Effects are re-rolled ~36 times a second and thin out as [k] falls from 1 to 0.
 */
class Glitch(private val px: Pixels) {
    private var src = IntArray(0)
    private var out = IntArray(0)
    private var row = IntArray(0)

    private fun rnd(seed: Int, i: Int, n: Int) = (Masonry.hash(seed, i) ushr 1) % n

    /** [k] is the remaining strength 1..0, [t] the game time. */
    fun apply(k: Float, t: Float, w: Int, h: Int) {
        val n = w * h
        if (src.size != n) { src = IntArray(n); out = IntArray(n); row = IntArray(w) }
        px.lo.getPixels(src, 0, w, 0, 0, w, h)
        System.arraycopy(src, 0, out, 0, n)
        val s = (t * 36f).toInt()

        // slices of scanlines slide sideways
        val slices = 2 + (k * 6f).toInt()
        val maxOff = 4 + (k * 14f).toInt()
        for (i in 0 until slices) {
            val y0 = rnd(s, i * 7, h)
            val sh = 2 + rnd(s, i * 7 + 1, 7)
            val off = rnd(s, i * 7 + 2, 2 * maxOff + 1) - maxOff
            for (y in y0 until minOf(h, y0 + sh)) {
                val b = y * w
                for (x in 0 until w) out[b + x] = src[b + Math.floorMod(x - off, w)]
            }
        }

        // RGB split: red and blue drift apart in a couple of bands
        val d = 2 + (k * 2f).toInt()
        for (i in 0 until 2) {
            val y0 = rnd(s, 100 + i, h)
            for (y in y0 until minOf(h, y0 + 8 + rnd(s, 110 + i, 14))) {
                val b = y * w
                System.arraycopy(out, b, row, 0, w)
                for (x in 0 until w) {
                    val r = row[minOf(w - 1, x + d)] and 0xFF0000
                    val bl = row[maxOf(0, x - d)] and 0xFF
                    out[b + x] = (row[x] and 0xFF00FF00.toInt()) or r or bl
                }
            }
        }

        // thin tinted bands
        for (i in 0 until 2 + (k * 2f).toInt()) {
            val tint = TINTS[rnd(s, 200 + i, TINTS.size)]
            val y0 = rnd(s, 210 + i, h)
            val tr = tint shr 16 and 0xFF
            val tg = tint shr 8 and 0xFF
            val tb = tint and 0xFF
            for (y in y0 until minOf(h, y0 + 2 + rnd(s, 220 + i, 4))) {
                val b = y * w
                for (x in 0 until w) {
                    val c = out[b + x]
                    val r = ((c shr 16 and 0xFF) * 5 + tr * 3) shr 3
                    val g = ((c shr 8 and 0xFF) * 5 + tg * 3) shr 3
                    val bl = ((c and 0xFF) * 5 + tb * 3) shr 3
                    out[b + x] = (c and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or bl
                }
            }
        }

        // corrupted blocks: displaced copies of the frame, some solid
        for (i in 0 until 3 + (k * 8f).toInt()) {
            val bw = 4 + rnd(s, 300 + i, 10)
            val bh = 2 + rnd(s, 320 + i, 4)
            val x0 = rnd(s, 340 + i, maxOf(1, w - bw))
            val y0 = rnd(s, 360 + i, maxOf(1, h - bh))
            val solid = rnd(s, 380 + i, 3) == 0
            val color = BLOCKS[rnd(s, 390 + i, BLOCKS.size)]
            val dx = (rnd(s, 400 + i, 33) - 16) * 2
            val dy = rnd(s, 420 + i, 9) - 4
            for (y in y0 until minOf(h, y0 + bh)) for (x in x0 until minOf(w, x0 + bw)) {
                out[y * w + x] = if (solid) color else src[Math.floorMod(y + dy, h) * w + Math.floorMod(x + dx, w)]
            }
        }

        px.lo.setPixels(out, 0, w, 0, 0, w, h)

        // a flicker of garbage text
        if (k > 0.2f && s % 3 != 0) {
            val y = 6f + rnd(s, 500, maxOf(1, h - 14))
            val x = rnd(s, 501, maxOf(1, w - 90)).toFloat()
            px.say(HEX[rnd(s, 502, HEX.size)], x, y, 4f, HEX_COLORS[rnd(s, 503, HEX_COLORS.size)], Paint.Align.LEFT, 0)
        }
    }

    private companion object {
        val TINTS = intArrayOf(0xFF00FF, 0x00FFFF, 0xFF2040)
        val BLOCKS = intArrayOf(0xFF6CF2C2.toInt(), 0xFFFFFFFF.toInt(), 0xFFFF2E9A.toInt(), 0xFFE2344E.toInt())
        val HEX = arrayOf("0x4D455048 MEPHI", "SEGFAULT @0x0000666", "rm -rf /floor", "0xDEADBEEF", "PID 666: hijack", "SIGKILL floor.exe", "sudo make trap")
        val HEX_COLORS = intArrayOf(0xFF6CF2C2.toInt(), 0xFFFF4E5E.toInt(), 0xFFFFB84A.toInt())
    }
}
