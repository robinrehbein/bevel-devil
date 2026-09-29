package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.ceil

/**
 * A word of the title logo, rasterized once in the pixel font at an integer pixel size and turned into a
 * chunky 3D block: beveled face with a two-band gradient, a diagonal extrude and an ink outline.
 */
class Logo(px: Pixels, word: String, face: Int, faceLi: Int, hi: Int, lo: Int, side: Int, sideLo: Int) {
    private val bmp: Bitmap
    private val faceMask: BooleanArray
    private val w: Int
    private val h: Int
    private val glint = Paint()
    /** First row with ink, so callers can place the letters rather than the bitmap. */
    private val top: Int

    init {
        val p = Paint().apply { typeface = px.text.typeface; textSize = 24f; isAntiAlias = false }
        val fm = p.fontMetrics
        w = ceil(p.measureText(word)).toInt() + DEPTH + 4
        h = ceil(fm.descent - fm.ascent).toInt() + DEPTH + 4
        val m = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        Canvas(m).drawText(word, 2f, 2f - fm.ascent, p)
        val a = IntArray(w * h)
        m.getPixels(a, 0, w, 0, 0, w, h)
        faceMask = BooleanArray(w * h) { Color.alpha(a[it]) > 127 }
        fun f(x: Int, y: Int) = x in 0 until w && y in 0 until h && faceMask[y * w + x]
        var y0 = h
        var y1 = 0
        for (y in 0 until h) for (x in 0 until w) if (f(x, y)) { y0 = minOf(y0, y); y1 = maxOf(y1, y) }
        top = y0 - 1
        val mid = (y0 + y1) / 2
        val out = IntArray(w * h)
        for (d in DEPTH downTo 1) for (y in 0 until h) for (x in 0 until w) {
            if (f(x - d, y - d)) out[y * w + x] = if (d == DEPTH) sideLo else side
        }
        for (y in 0 until h) for (x in 0 until w) {
            if (!f(x, y)) continue
            out[y * w + x] = when {
                !f(x, y - 1) || !f(x - 1, y) -> hi
                !f(x, y + 1) || !f(x + 1, y) -> lo
                y < mid -> faceLi
                else -> face
            }
        }
        val outlined = out.copyOf()
        for (y in 0 until h) for (x in 0 until w) {
            if (out[y * w + x] != 0) continue
            val near = (x > 0 && out[y * w + x - 1] != 0) || (x < w - 1 && out[y * w + x + 1] != 0) ||
                (y > 0 && out[(y - 1) * w + x] != 0) || (y < h - 1 && out[(y + 1) * w + x] != 0)
            if (near) outlined[y * w + x] = INK
        }
        bmp = Bitmap.createBitmap(outlined, w, h, Bitmap.Config.ARGB_8888)
    }

    /** Draws with the letters' top at [yTop]; a glint sweeps diagonally across the face every few seconds. */
    fun draw(c: Canvas, x: Float, yTop: Float, t: Float) {
        val y = yTop - top
        c.drawBitmap(bmp, x, y, null)
        val g = ((t * 70f) % (w + h + 160f)).toInt() - 20
        if (g < -2 || g > w + h) return
        glint.color = WHITE
        for (yy in 0 until h) for (k in 0..2) {
            val xx = g - yy + k
            if (xx !in 0 until w || !faceMask[yy * w + xx]) continue
            glint.color = if (k == 1) WHITE else GOLD_SPARK
            c.drawRect(x + xx, y + yy, x + xx + 1, y + yy + 1, glint)
        }
    }

    companion object {
        const val DEPTH = 4
    }
}
