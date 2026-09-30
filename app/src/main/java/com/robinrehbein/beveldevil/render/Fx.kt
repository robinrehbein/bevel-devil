package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.sin
import kotlin.math.sqrt

val BAYER = intArrayOf(0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5)

/** 4×4 ordered-dither threshold in 0..1 for pixel ([x], [y]). */
fun bayer(x: Int, y: Int) = (BAYER[(y and 3) * 4 + (x and 3)] + 0.5f) / 16f

/** Atmosphere that is a pure function of time: no state, no allocations per frame. */
object Fx {
    private val EMBERS = intArrayOf(0xFF5C1A22.toInt(), 0xFF8A2E24.toInt(), 0xFFC4462C.toInt(), 0xFFF07A3A.toInt(), 0xFFFFC46A.toInt())

    /** Sparks rising over a [w]×[h] area, hotter and denser while [heat] is up. */
    fun embers(px: Pixels, w: Int, h: Int, t: Float, heat: Float) {
        val n = w * h / 1400 + (heat * 14).toInt()
        val span = h + 16
        for (i in 0 until n) {
            val k = Masonry.hash(i, 911)
            val speed = 7f + (k and 15) * (1f + heat)
            val rise = (t * speed + (k ushr 4 and 1023)) % span
            val y = h + 8 - rise
            val x = Math.floorMod(k ushr 14, w) + sin(t * (0.7f + (k and 7) * 0.13f) + i) * 3f
            val life = rise / span
            val flicker = if (((t * 9f).toInt() + i) % 5 == 0) 1 else 0
            val idx = ((1f - life) * 4.2f).toInt() + flicker - (if (life > 0.8f) 1 else 0)
            val c = EMBERS[idx.coerceIn(0, 4)]
            px.rect(x, y, 1, 1, c)
            if (k and 3 == 0 && idx >= 2) px.rect(x, y + 1, 1, 1, EMBERS[idx - 2])
        }
    }

    /** Source-over of ARGB [src] onto [dst], both straight alpha. */
    private fun blend(dst: Int, src: Int): Int {
        val sa = src ushr 24
        val da = dst ushr 24
        val oa = sa + da * (255 - sa) / 255
        if (oa == 0) return 0
        fun ch(shift: Int): Int {
            val sc = src shr shift and 0xFF
            val dc = dst shr shift and 0xFF
            return (sc * sa + dc * da * (255 - sa) / 255) / oa
        }
        return (oa shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    /** A soft dithered halo of [color], [r] px radius, as a (2r)² bitmap. Few alpha steps keep it pixel-crisp. */
    fun halo(r: Int, color: Int, inner: Int, outer: Int): Bitmap {
        val d = 2 * r
        val out = IntArray(d * d)
        for (y in 0 until d) for (x in 0 until d) {
            val dx = x + 0.5f - r
            val dy = (y + 0.5f - r) * 1.15f
            val v = 1f - sqrt(dx * dx + dy * dy) / r
            if (v <= 0f) continue
            // concentric bands with thin dithered seams
            val q = v * 3f + (bayer(x, y) - 0.5f) * 0.35f
            val a = when {
                q > 2f -> inner
                q > 1f -> outer
                q > 0.15f -> outer / 2
                else -> 0
            }
            if (a > 0) out[y * d + x] = (a shl 24) or (color and 0xFFFFFF)
        }
        return Bitmap.createBitmap(out, d, d, Bitmap.Config.ARGB_8888)
    }

    /**
     * Distant hell behind the level: dithered lava glow, spires and broken pillars with a hot rim,
     * and chains hanging from the dark. [w] wider than the playfield so it can slide for parallax.
     */
    fun backdrop(w: Int, h: Int, horizon: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val out = IntArray(w * h)
        val glow = 0xFF6A1A2A.toInt()
        for (y in horizon - 34 until h) for (x in 0 until w) {
            val v = (y - (horizon - 34)) / 40f
            if (v > bayer(x shr 1, y shr 1) * 1.4f) out[y * w + x] = (0x48 shl 24) or (glow and 0xFFFFFF)
        }
        // two rows of skyline: pale far spires, then darker near ones
        val top = IntArray(w)
        for (layer in 0..1) {
            top.fill(h)
            var x = if (layer == 0) 3 else 0
            var i = layer * 100
            while (x < w) {
                val k = Masonry.hash(i++, 5)
                val sw = 7 + (k and 7) - layer * 2
                val tall = (if (layer == 0) 34 else 16) + (k ushr 3 and 15) + (if (k ushr 9 and 3 == 0) 18 - layer * 8 else 0)
                val base = horizon - tall
                val spire = k ushr 12 and 1 == 0
                for (c in 0 until sw) {
                    val cx = x + c
                    if (cx >= w) break
                    val edge = minOf(c, sw - 1 - c)
                    val t = if (spire) base + maxOf(0, (sw / 2 - edge) * 3 - 2) else base + (if ((c + (k ushr 14)) % 3 == 0 && edge > 0) 2 else 0)
                    top[cx] = minOf(top[cx], t)
                }
                x += sw + (k ushr 16 and 7) + (if (layer == 0) 2 else 6)
            }
            val sil = if (layer == 0) 0x5A1A0C24 else 0xA0100614.toInt()
            val rim = if (layer == 0) 0x70622036 else 0xB0782432.toInt()
            for (cx in 0 until w) for (y in top[cx] until h) {
                val lit = y == top[cx] || (y == top[cx] + 1 && (cx + y) % 2 == 0)
                out[y * w + cx] = blend(out[y * w + cx], if (lit) rim else sil)
            }
        }
        // lit windows
        for (j in 0 until w / 11) {
            val k = Masonry.hash(j, 17)
            val wx = Math.floorMod(k, w)
            val wy = top[wx] + 5 + (k ushr 8 and 15)
            if (wy < horizon - 3 && wx > 0 && wy + 1 < h && top[wx] < wy - 2 && top[wx - 1] < wy && wx + 1 < w && top[wx + 1] < wy) {
                out[wy * w + wx] = 0xFFE0703A.toInt(); out[(wy + 1) * w + wx] = 0xFFA83A2C.toInt()
            }
        }
        bmp.setPixels(out, 0, w, 0, 0, w, h)
        // chains from the ceiling
        val c = Canvas(bmp)
        val p = Paint()
        for ((cx, len) in listOf(38 to 34, 57 to 22, 151 to 46, 238 to 28, 270 to 40)) {
            if (cx >= w) continue
            var y = 8
            var link = 0
            while (y < 8 + len) {
                p.color = 0xD0301C3A.toInt()
                if (link % 2 == 0) {
                    c.drawRect(cx - 1f, y.toFloat(), cx + 2f, y + 4f, p)
                    p.color = 0xFF130819.toInt(); c.drawRect(cx.toFloat(), y + 1f, cx + 1f, y + 3f, p)
                    p.color = 0xB05A3A60.toInt(); c.drawRect(cx - 1f, y.toFloat(), cx.toFloat(), y + 2f, p)
                } else c.drawRect(cx.toFloat(), y.toFloat(), cx + 1f, y + 3f, p)
                y += 3; link++
            }
            p.color = 0xD0301C3A.toInt()
            c.drawRect(cx - 2f, y.toFloat(), cx + 3f, y + 2f, p)
            c.drawRect(cx - 2f, y + 2f, cx - 1f, y + 4f, p); c.drawRect(cx + 2f, y + 2f, cx + 3f, y + 4f, p)
        }
        return bmp
    }
}
