package com.robinrehbein.beveldevil.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Screen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * "Hell CRT" look: everything is drawn into a lo-res logical canvas (the [Layout]'s lw×lh, 8 px per tile),
 * scaled up by an integer factor without filtering, then text, the flying trap card and a scanline/vignette
 * pass are drawn at full resolution on top.
 */
class Renderer(context: Context) {
    private val px = Pixels(context)
    private val world = WorldPainter(px)
    private val ui = UiPainter(px)
    private val controls = ControlsPainter()

    private var swirlPx = IntArray(0)
    private var swirlBmp: Bitmap? = null
    private var scanStep = -1
    private val scanPaint = Paint()
    private var vignetteFor = ""
    private val vignettePaint = Paint()

    // ---------- background ----------

    /** Swirl at half resolution over the whole canvas, its grid anchored to the menu stage. */
    private fun swirl(t: Float, heat: Float, l: Layout) {
        val kx = (l.sx + 1) / 2
        val ky = (l.sy + 1) / 2
        val x0 = l.sx - 2 * kx
        val y0 = l.sy - 2 * ky
        val bw = (l.lw - x0 + 1) / 2
        val bh = (l.lh - y0 + 1) / 2
        val bmp = swirlBmp?.takeIf { it.width == bw && it.height == bh }
            ?: Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888).also { swirlBmp = it; swirlPx = IntArray(bw * bh) }
        val cx = BW / 2f
        val cy = BH / 2f
        val spin = t * 0.25f + heat * 1.2f
        for (by in 0 until bh) for (bx in 0 until bw) {
            val x = bx - kx
            val y = by - ky
            val dx = x - cx
            val dy = (y - cy) * 1.3f
            val r = sqrt(dx * dx + dy * dy)
            val a = atan2(dy, dx)
            val v = sin(a * 2 + r * 0.09f - spin * 2) * 0.8f + 0.5f * sin(x * 0.07f + t * 0.4f + sin(y * 0.1f + t * 0.6f)) + 0.35f * sin(r * 0.14f - t * 0.9f)
            val n = (v + 1.65f) / 3.3f + (BAYER[(y and 3) * 4 + (x and 3)] / 16f - 0.5f) * 0.16f
            val idx = floor(n * 6).toInt().coerceIn(0, 5)
            val hot = Math.floorMod(x * 7 + y * 13, 97) / 97f < heat
            swirlPx[by * bw + bx] = if (hot) HOT[idx] else COOL[idx]
        }
        bmp.setPixels(swirlPx, 0, bw, 0, 0, bw, bh)
        px.dst.set(x0.toFloat(), y0.toFloat(), (x0 + 2 * bw).toFloat(), (y0 + 2 * bh).toFloat())
        px.lc.drawBitmap(bmp, null, px.dst, px.blit)
    }

    // ---------- entry ----------

    fun draw(canvas: Canvas, game: Game, l: Layout) {
        if (l.w == 0) return
        px.resize(l.lw, l.lh)
        px.texts.clear()
        swirl(game.time, game.heat, l)
        when (game.screen) {
            Screen.TITLE -> ui.title(game, l)
            Screen.SELECT -> ui.select(game, l)
            Screen.ALBUM -> ui.album(game, l)
            Screen.PLAY -> { world.draw(game, l); ui.hud(game, l) }
            Screen.PAUSE -> { world.draw(game, l); ui.pause(l) }
            Screen.CLEAR -> { world.draw(game, l); ui.clear(game, l) }
            Screen.END -> { world.draw(game, l); ui.end(game, l) }
        }

        val sc = l.sc.toFloat()
        val jx = game.shake * sc * 2f * sin(game.time * 90f)
        val jy = game.shake * sc * 2f * cos(game.time * 77f)
        px.dst.set(0f, 0f, l.lw * sc, l.lh * sc)
        // while shaking, an unshaken copy underneath keeps the screen edges covered
        if (jx != 0f || jy != 0f) canvas.drawBitmap(px.lo, null, px.dst, px.blit)
        canvas.save()
        canvas.translate(jx, jy)
        canvas.drawBitmap(px.lo, null, px.dst, px.blit)
        val text = px.text
        for (t in px.texts) {
            text.textSize = t.size * sc
            text.textAlign = t.align
            val baseline = t.y * sc + t.size * sc * 0.36f
            if (t.shadow != 0) {
                text.color = t.shadow
                canvas.drawText(t.s, t.x * sc + sc * 0.8f, baseline + sc * 0.8f, text)
            }
            text.color = t.color
            canvas.drawText(t.s, t.x * sc, baseline, text)
        }
        if (game.screen == Screen.PLAY) game.card?.let { ui.flyingCard(canvas, game, it, l) }
        if (game.screen == Screen.ALBUM && game.albumSelection >= 0) ui.bigCard(canvas, game, Card.entries[game.albumSelection], l)
        canvas.restore()
        crt(canvas, l)
        if (game.screen == Screen.PLAY) controls.draw(canvas, l.controls)
    }

    private fun crt(canvas: Canvas, l: Layout) {
        val step = maxOf(2, l.sc)
        if (step != scanStep) {
            scanStep = step
            val bmp = Bitmap.createBitmap(1, step, Bitmap.Config.ARGB_8888)
            bmp.eraseColor(Color.TRANSPARENT)
            val lines = maxOf(1, Math.round(step * 0.38f))
            for (i in 0 until lines) bmp.setPixel(0, i, 0x2E000000)
            scanPaint.shader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
        val w = l.w.toFloat()
        val h = l.h.toFloat()
        canvas.drawRect(0f, 0f, w, h, scanPaint)
        val key = "${l.w}x${l.h}"
        if (vignetteFor != key) {
            vignetteFor = key
            vignettePaint.shader = RadialGradient(w / 2, h / 2, hypot(w, h) * 0.54f, intArrayOf(0, 0, 0x80000000.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w, h, vignettePaint)
    }

    companion object {
        private const val BW = 128
        private const val BH = 72
        private val BAYER = intArrayOf(0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5)
        private val COOL = intArrayOf(0xFF160A22.toInt(), 0xFF240E30.toInt(), 0xFF34123E.toInt(), 0xFF22244E.toInt(), 0xFF1E405C.toInt(), 0xFF2C5C6E.toInt())
        private val HOT = intArrayOf(0xFF1E0818.toInt(), 0xFF380C22.toInt(), 0xFF5C1028.toInt(), 0xFF841A2C.toInt(), 0xFFAA3232.toInt(), 0xFFCC5A40.toInt())
    }
}
