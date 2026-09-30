package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

/** World 2's far scenery: a dim server room. Two rows of rack silhouettes, cable trays overhead, cold haze. */
object DataCenter {
    private val LED_COLORS = intArrayOf(0xFF49E8A0.toInt(), 0xFF49E8A0.toInt(), 0xFF3CC8E0.toInt(), 0xFF3CC8E0.toInt(), 0xFF9A7CF0.toInt(), 0xFFE8A040.toInt(), 0xFFE2344E.toInt())
    private const val MAX_LEDS = 28

    fun backdrop(w: Int, h: Int): Backdrop {
        val out = IntArray(w * h)
        for (y in 0 until h) {
            val f = y / h.toFloat()
            val c = Color.rgb((6 + 4 * f).toInt(), (8 + 6 * f).toInt(), (19 + 11 * f).toInt())
            for (x in 0 until w) out[y * w + x] = c
        }
        haze(out, w, h, w * 0.28f, h * 0.42f, 80f, 0x2A2048)
        haze(out, w, h, w * 0.78f, h * 0.68f, 90f, 0x0F3A4A)
        val leds = ArrayList<Int>()
        racks(out, w, h, 0, 16, 50, 70, 0xFF0E1526.toInt(), 0xFF18213A.toInt(), null)
        racks(out, w, h, 1, 26, 86, 104, 0xFF0A0F1E.toInt(), 0xFF1D2746.toInt(), leds)
        tray(out, w, 3, 0xFF1A2340.toInt(), 0xFF0E1426.toInt())
        for ((x0, len, sag) in listOf(Triple(14, 46, 13), Triple(88, 38, 10), Triple(150, 52, 15), Triple(236, 40, 11))) {
            cable(out, w, x0, len, sag, if (x0 % 2 == 0) 0xFF202C52.toInt() else 0xFF2A2250.toInt())
        }
        return Backdrop(Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888), leds.toIntArray())
    }

    private fun haze(out: IntArray, w: Int, h: Int, cx: Float, cy: Float, r: Float, rgb: Int) {
        for (y in 0 until h) for (x in 0 until w) {
            val dx = x - cx
            val dy = (y - cy) * 1.4f
            val v = 1f - sqrt(dx * dx + dy * dy) / r
            if (v > bayer(x shr 1, y shr 1) * 1.1f) out[y * w + x] = Fx.blend(out[y * w + x], (0x58 shl 24) or rgb)
        }
    }

    /** A row of cabinets standing on the floor, [minH]..[maxH] tall. The near row ([leds] given) carries status lights. */
    private fun racks(out: IntArray, w: Int, h: Int, layer: Int, cw: Int, minH: Int, maxH: Int, body: Int, rim: Int, leds: ArrayList<Int>?) {
        var x = -4 - layer * 9
        var i = layer * 50
        val unit = if (layer == 0) 4 else 6
        while (x < w) {
            val k = Masonry.hash(i++, 41 + layer)
            val rw = cw + (k and 3) * 2
            val top = h - (minH + Math.floorMod(k ushr 3, maxH - minH + 1))
            for (cx in maxOf(0, x) until minOf(w, x + rw)) {
                val edge = cx == x || cx == x + rw - 1
                for (y in top until h) {
                    var c = body
                    if (y == top) c = rim
                    else if (edge) c = if (cx == x) rim else 0xFF05070F.toInt()
                    else if ((y - top) % unit == 0) c = Fx.blend(body, 0x14FFFFFF)
                    out[y * w + cx] = c
                }
            }
            // status lights sit on the unit lines; some stay dark
            val n = if (layer == 0) 2 else 3 + (k ushr 9 and 3)
            for (j in 0 until n) {
                val kk = Masonry.hash(i * 7 + j, 77)
                val lx = x + 3 + Math.floorMod(kk, rw - 6)
                val ly = top + 3 + unit * Math.floorMod(kk ushr 8, (h - top - 6) / unit - 1)
                if (lx !in 0 until w || ly !in 0 until h) continue
                val col = LED_COLORS[Math.floorMod(kk ushr 16, LED_COLORS.size)]
                val lit = leds != null && kk ushr 20 and 3 != 0 && leds.size < 4 * MAX_LEDS
                out[ly * w + lx] = Fx.blend(body, ((if (lit) 0x60 else 0x34) shl 24) or (col and 0xFFFFFF))
                if (lit) { leds!!.add(lx); leds.add(ly); leds.add(col); leds.add(kk ushr 24 and 255) }
            }
            x += rw + 1 + (k ushr 5 and 3)
        }
    }

    private fun tray(out: IntArray, w: Int, y: Int, body: Int, dark: Int) {
        for (x in 0 until w) {
            out[y * w + x] = body
            out[(y + 1) * w + x] = if (x % 8 < 2) body else dark
            out[(y + 2) * w + x] = if (x % 8 < 2) body else dark
            out[(y + 3) * w + x] = 0xFF070A14.toInt()
        }
    }

    /** A cable swag hanging from the tray. */
    private fun cable(out: IntArray, w: Int, x0: Int, len: Int, sag: Int, c: Int) {
        for (dx in 0..len) {
            val x = x0 + dx
            if (x >= w) break
            val y = 7 + (sin(dx / len.toFloat() * PI) * sag).toInt()
            out[y * w + x] = c
            if (dx % 3 == 0) out[(y + 1) * w + x] = 0xFF0A0E1C.toInt()
        }
    }
}
