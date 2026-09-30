package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap

/** Colors of one circuit-board variant: the solder mask, its copper, and the parts on it. */
class PcbPalette(
    /** Solder mask, top to bottom (4 bands, dithered into each other). */
    val mask: IntArray,
    val trace: Int,
    val via: Int,
    val hole: Int,
    val pad: Int,
    val chip: Int,
    val chipRim: Int,
    val pin: Int,
    val cap: Int,
    val ledColors: IntArray,
    /** The far-back centerpiece: a CPU on the green board, a firmware flash chip and its battery on the blue one. */
    val firmware: Boolean,
)

/**
 * World 3's far scenery: a calm circuit board seen from above. A dark solder mask with faint routed buses,
 * vias, a few SMD parts and one big chip far back. Everything stays within a few steps of the mask color so
 * the playfield reads clearly in front of it; only the status LEDs are bright, and they blink slowly.
 */
object Pcb {
    val GREEN = PcbPalette(
        intArrayOf(0xFF0A1F19.toInt(), 0xFF0B231C.toInt(), 0xFF0D271F.toInt(), 0xFF0F2B22.toInt()),
        0xFF16392C.toInt(), 0xFF24584A.toInt(), 0xFF040F0A.toInt(), 0xFF6E5A30.toInt(),
        0xFF141C22.toInt(), 0xFF26313A.toInt(), 0xFF5A6258.toInt(), 0xFF2A3A52.toInt(),
        intArrayOf(0xFF49E8A0.toInt(), 0xFF49E8A0.toInt(), 0xFFE8B84A.toInt(), 0xFFE2344E.toInt()),
        false,
    )
    val BLUE = PcbPalette(
        intArrayOf(0xFF09152C.toInt(), 0xFF0A1932.toInt(), 0xFF0C1D39.toInt(), 0xFF0E2140.toInt()),
        0xFF17315C.toInt(), 0xFF27508A.toInt(), 0xFF030A1A.toInt(), 0xFF5A6070.toInt(),
        0xFF121A2A.toInt(), 0xFF26324A.toInt(), 0xFF6A7488.toInt(), 0xFF2A3A5A.toInt(),
        intArrayOf(0xFF6CE8FF.toInt(), 0xFF6CE8FF.toInt(), 0xFFB8F4FF.toInt(), 0xFFE2344E.toInt()),
        true,
    )

    private const val MAX_LEDS = 12

    fun backdrop(w: Int, h: Int, p: PcbPalette): Backdrop {
        val out = IntArray(w * h)
        val leds = ArrayList<Int>()
        mask(out, w, h, p)
        pour(out, w, h, w * 0.14f, h * 0.34f, 34, 20, p)
        pour(out, w, h, w * 0.70f, h * 0.22f, 40, 14, p)
        // routed buses: long runs with one 45° jog, far-back and faint
        bus(out, w, h, 0, 24, 3, 130, 6, p.trace)
        bus(out, w, h, 150, 58, 4, 140, -8, p.trace)
        bus(out, w, h, 60, 92, 3, 160, 7, p.trace)
        bus(out, w, h, -20, 112, 3, 120, -6, p.trace)
        busV(out, w, h, 30, 0, 3, 60, 6, p.trace)
        busV(out, w, h, 232, 40, 4, 70, -6, p.trace)
        // the centerpiece
        if (p.firmware) firmwareChip(out, w, h, 132, 30, p, leds) else cpu(out, w, h, 176, 26, p)
        // small parts
        for (i in 0 until 16) {
            val k = Masonry.hash(i, 303)
            val x = 6 + Math.floorMod(k, w - 14)
            val y = 10 + Math.floorMod(k ushr 9, h - 40)
            when (i % 4) {
                0, 1 -> resistor(out, w, h, x, y, i % 2 == 0, p)
                2 -> mlcc(out, w, h, x, y, p)
                else -> soic(out, w, h, x, y, 12, 8, p)
            }
        }
        can(out, w, h, 58, 38, 6, p)
        can(out, w, h, 214, 24, 5, p)
        qfp(out, w, h, 20, 62, 22, p)
        if (p.firmware) crystal(out, w, h, 186, 100, p) else soic(out, w, h, 196, 96, 16, 10, p)
        // vias, scattered on grid points
        for (i in 0 until 46) {
            val k = Masonry.hash(i, 505)
            via(out, w, h, 4 + Math.floorMod(k, (w - 8) / 6) * 6, 6 + Math.floorMod(k ushr 10, (h - 14) / 6) * 6, p)
        }
        // status lights next to parts
        val spots = listOf(40 to 14, 96 to 30, 246 to 60, 176 to 18, 12 to 100, 226 to 86, 100 to 72, 270 to 26)
        for ((i, s) in spots.withIndex()) {
            if (leds.size >= 4 * MAX_LEDS || s.first >= w) continue
            val k = Masonry.hash(i, 909)
            val col = p.ledColors[Math.floorMod(k, p.ledColors.size)]
            led(out, w, h, s.first, s.second, col, p.chip)
            leds.add(s.first); leds.add(s.second); leds.add(col); leds.add(k ushr 8 and 255)
        }
        return Backdrop(Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888), leds.toIntArray())
    }

    // ---------- helpers ----------

    private fun put(out: IntArray, w: Int, h: Int, x: Int, y: Int, c: Int) {
        if (x in 0 until w && y in 0 until h) out[y * w + x] = c
    }

    private fun fill(out: IntArray, w: Int, h: Int, x: Int, y: Int, rw: Int, rh: Int, c: Int) {
        for (yy in y until y + rh) for (xx in x until x + rw) put(out, w, h, xx, yy, c)
    }

    /** Solder mask: four bands dithered into one another, with the odd brighter speck. */
    private fun mask(out: IntArray, w: Int, h: Int, p: PcbPalette) {
        for (y in 0 until h) {
            val f = y / (h - 1f) * 3f
            val band = f.toInt().coerceIn(0, 2)
            val frac = f - band
            for (x in 0 until w) {
                val hi = frac > bayer(x, y)
                var c = p.mask[if (hi) band + 1 else band]
                if (Masonry.hash(x, y) and 63 == 0) c = Fx.blend(c, 0x10FFFFFF)
                out[y * w + x] = c
            }
        }
    }

    /** A ground-pour patch: checker hatch a hair lighter than the mask, soft at the rim. */
    private fun pour(out: IntArray, w: Int, h: Int, cx: Float, cy: Float, rx: Int, ry: Int, p: PcbPalette) {
        for (y in (cy - ry).toInt()..(cy + ry).toInt()) for (x in (cx - rx).toInt()..(cx + rx).toInt()) {
            if (x !in 0 until w || y !in 0 until h) continue
            val dx = (x - cx) / rx
            val dy = (y - cy) / ry
            if (dx * dx + dy * dy > 1f) continue
            if ((x + y) and 1 == 0) out[y * w + x] = Fx.blend(out[y * w + x], (0x10 shl 24) or (p.via and 0xFFFFFF))
        }
    }

    /** [n] parallel lanes [gap] px apart, running [len] px right from ([x0], [y0]), jogging [jog] px up/down by 45° mid-way. */
    private fun bus(out: IntArray, w: Int, h: Int, x0: Int, y0: Int, n: Int, len: Int, jog: Int, c: Int) {
        val a = len / 2 - Math.abs(jog) / 2
        for (lane in 0 until n) {
            val y = y0 + lane * 4
            var yy = y
            for (dx in 0..len + Math.abs(jog)) {
                val x = x0 + dx
                if (dx in a until a + Math.abs(jog)) yy += if (jog > 0) 1 else -1
                put(out, w, h, x, yy, c)
                if (dx in a until a + Math.abs(jog)) put(out, w, h, x, yy - 1, c)
            }
            // pad at the lane's end
            fill(out, w, h, x0 + len + Math.abs(jog), yy - 1, 2, 3, c)
        }
    }

    private fun busV(out: IntArray, w: Int, h: Int, x0: Int, y0: Int, n: Int, len: Int, jog: Int, c: Int) {
        val a = len / 2 - Math.abs(jog) / 2
        for (lane in 0 until n) {
            var xx = x0 + lane * 4
            for (dy in 0..len + Math.abs(jog)) {
                if (dy in a until a + Math.abs(jog)) xx += if (jog > 0) 1 else -1
                put(out, w, h, xx, y0 + dy, c)
            }
        }
    }

    private fun via(out: IntArray, w: Int, h: Int, x: Int, y: Int, p: PcbPalette) {
        for (dy in -1..1) for (dx in -1..1) if (dx != 0 || dy != 0) put(out, w, h, x + dx, y + dy, p.trace)
        put(out, w, h, x, y, p.hole)
    }

    private fun resistor(out: IntArray, w: Int, h: Int, x: Int, y: Int, horiz: Boolean, p: PcbPalette) {
        val bw = if (horiz) 6 else 3
        val bh = if (horiz) 3 else 6
        fill(out, w, h, x, y, bw, bh, p.chip)
        if (horiz) { fill(out, w, h, x, y, 1, 3, p.pad); fill(out, w, h, x + 5, y, 1, 3, p.pad) }
        else { fill(out, w, h, x, y, 3, 1, p.pad); fill(out, w, h, x, y + 5, 3, 1, p.pad) }
    }

    private fun mlcc(out: IntArray, w: Int, h: Int, x: Int, y: Int, p: PcbPalette) {
        fill(out, w, h, x, y, 4, 3, p.cap)
        fill(out, w, h, x, y, 1, 3, p.pad); fill(out, w, h, x + 3, y, 1, 3, p.pad)
    }

    /** A small IC seen from above: dark body, light rim, pin ticks on the long sides, pin-1 dot. */
    private fun soic(out: IntArray, w: Int, h: Int, x: Int, y: Int, bw: Int, bh: Int, p: PcbPalette) {
        for (px in x + 1 until x + bw - 1) if ((px - x) % 2 == 1) { put(out, w, h, px, y - 1, p.pad); put(out, w, h, px, y + bh, p.pad) }
        fill(out, w, h, x, y, bw, bh, p.chipRim)
        fill(out, w, h, x + 1, y + 1, bw - 2, bh - 2, p.chip)
        put(out, w, h, x + 2, y + 2, p.chipRim)
    }

    private fun qfp(out: IntArray, w: Int, h: Int, x: Int, y: Int, s: Int, p: PcbPalette) {
        for (i in 2 until s - 2 step 2) {
            for (e in listOf(-1, s)) {
                put(out, w, h, x + i, y + e, p.pad); put(out, w, h, x + e, y + i, p.pad)
            }
        }
        fill(out, w, h, x, y, s, s, p.chipRim)
        fill(out, w, h, x + 1, y + 1, s - 2, s - 2, p.chip)
        fill(out, w, h, x + 4, y + 4, 2, 2, p.chipRim)
    }

    /** Electrolytic can from above: a dark disc with a lighter cap ring and a polarity notch. */
    private fun can(out: IntArray, w: Int, h: Int, cx: Int, cy: Int, r: Int, p: PcbPalette) {
        for (dy in -r..r) for (dx in -r..r) {
            val d = dx * dx + dy * dy
            if (d > r * r + 1) continue
            put(out, w, h, cx + dx, cy + dy, if (d >= (r - 1) * (r - 1)) p.chipRim else p.chip)
        }
        fill(out, w, h, cx - 1, cy - 1, 3, 3, p.chipRim)
        for (dx in 0 until r) put(out, w, h, cx + r - dx - 1, cy + r - 1, p.cap)
    }

    private fun crystal(out: IntArray, w: Int, h: Int, x: Int, y: Int, p: PcbPalette) {
        fill(out, w, h, x + 1, y, 10, 5, p.chipRim)
        fill(out, w, h, x, y + 1, 12, 3, p.chipRim)
        fill(out, w, h, x + 2, y + 1, 8, 3, p.chip)
        fill(out, w, h, x + 1, y + 5, 2, 2, p.pad); fill(out, w, h, x + 9, y + 5, 2, 2, p.pad)
    }

    /** The big CPU far back: heat-spreader square, die window, pin rows all round. */
    private fun cpu(out: IntArray, w: Int, h: Int, x: Int, y: Int, p: PcbPalette) {
        val s = 64
        for (i in 2 until s - 1 step 3) {
            for (e in 1..2) {
                put(out, w, h, x + i, y - e, p.trace); put(out, w, h, x + i, y + s - 1 + e, p.trace)
                put(out, w, h, x - e, y + i, p.trace); put(out, w, h, x + s - 1 + e, y + i, p.trace)
            }
        }
        fill(out, w, h, x, y, s, s, p.chipRim)
        fill(out, w, h, x + 2, y + 2, s - 4, s - 4, p.chip)
        fill(out, w, h, x + 16, y + 16, 32, 32, p.chipRim)
        fill(out, w, h, x + 18, y + 18, 28, 28, p.mask[1])
        // die grid
        for (i in 0 until 28 step 7) { fill(out, w, h, x + 18 + i, y + 18, 1, 28, p.chip); fill(out, w, h, x + 18, y + 18 + i, 28, 1, p.chip) }
        fill(out, w, h, x + 4, y + 4, 3, 3, p.chipRim)
    }

    /** Blue board: a flash chip with a label sticker, and its coin cell beside it. */
    private fun firmwareChip(out: IntArray, w: Int, h: Int, x: Int, y: Int, p: PcbPalette, leds: ArrayList<Int>) {
        val bw = 48
        val bh = 30
        for (i in 3 until bw - 2 step 4) { fill(out, w, h, x + i, y - 3, 2, 3, p.pin); fill(out, w, h, x + i, y + bh, 2, 3, p.pin) }
        fill(out, w, h, x, y, bw, bh, p.chipRim)
        fill(out, w, h, x + 1, y + 1, bw - 2, bh - 2, p.chip)
        // label sticker with a few rows of "text"
        fill(out, w, h, x + 6, y + 7, bw - 12, bh - 14, p.chipRim)
        for (row in 0 until 3) fill(out, w, h, x + 9, y + 10 + row * 3, 14 + (row * 7) % 11, 1, p.chip)
        put(out, w, h, x + 3, y + 3, p.chipRim)
        put(out, w, h, x + 4, y + 3, p.chipRim)
        // coin cell
        val cx = x + bw + 26
        val cy = y + 14
        for (dy in -14..14) for (dx in -14..14) {
            val d = dx * dx + dy * dy
            if (d > 196) continue
            put(out, w, h, cx + dx, cy + dy, if (d >= 169) p.chipRim else if (d >= 140) p.chip else p.mask[3])
        }
        for (dy in -12..12) for (dx in -12..12) if (dx * dx + dy * dy <= 100 && (dx + dy) % 6 == 0) put(out, w, h, cx + dx, cy + dy, p.chipRim)
        fill(out, w, h, cx - 2, cy - 2, 5, 5, p.chipRim)
    }

    private fun led(out: IntArray, w: Int, h: Int, x: Int, y: Int, col: Int, body: Int) {
        // the dark package; the blinking light itself is drawn by the world painter
        put(out, w, h, x, y, Fx.blend(body, (0x70 shl 24) or (col and 0xFFFFFF)))
        put(out, w, h, x - 1, y, body); put(out, w, h, x + 1, y, body)
    }
}
