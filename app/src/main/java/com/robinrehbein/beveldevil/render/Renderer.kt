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
import com.robinrehbein.beveldevil.game.World
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
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
    private val settings = SettingsPainter(px)
    private val intro = IntroPainter(px, ui)
    private val glitch = Glitch(px)
    private val controls = ControlsPainter()
    private val motion = MotionPainter(px)
    private val twist = TwistPainter(px, ui)

    private var swirlPx = IntArray(0)
    private var swirlBmp: Bitmap? = null
    private var scanStep = -1
    private val scanPaint = Paint()
    private var vignetteFor = ""
    private val vignettePaint = Paint()

    // screen transitions: the last frame before a change, dissolved away with a dither pattern
    private var lastScreen: Screen? = null
    private var lastWorld: World? = null
    private var lastFake = false
    private var wipeT = -1f
    private var wipeP = 1f
    private var wipeIris = false
    private var wipeCx = 0f
    private var wipeCy = 0f
    private var wipeMax = 1f
    private var wipeW = 0
    private var wipeH = 0
    private var snapPx = IntArray(0)
    private var curPx = IntArray(0)

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
        val sameSize = px.lo.width == l.lw && px.lo.height == l.lh
        px.resize(l.lw, l.lh)
        transition(game, l, sameSize)
        px.texts.clear()
        swirl(game.time, game.heat, l)
        if (!l.overWorld(game.screen) && game.screen != Screen.PLAY) Fx.embers(px, l.lw, l.lh, game.time, 0f)
        when (game.screen) {
            Screen.TITLE -> { ui.title(game, l); settings.extras(game, l) }
            Screen.SETTINGS -> { ui.floorStrip(l); settings.screen(game, l) }
            Screen.SELECT -> ui.select(game, l)
            Screen.ALBUM -> ui.album(game, l)
            Screen.PLAY -> { level(game, l); if (!twist.fake(game, l)) { ui.hud(game, l); motion.hud(game, l) } }
            Screen.PAUSE -> { level(game, l); ui.pause(game, l); settings.extras(game, l) }
            Screen.CLEAR -> { level(game, l); ui.clear(game, l) }
            Screen.END -> { level(game, l); ui.end(game, l) }
            Screen.INTRO -> intro.intro(game, l)
            Screen.WORLD_INTRO -> intro.worldIntro(game, l)
        }
        if (game.glitch > 0f && game.screen == Screen.PLAY) glitch.apply(game.glitch / Game.GLITCH_TIME, game.time, l.lw, l.lh)
        wipe(game, l)

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
        for (i in 0 until px.texts.size) {
            val t = px.texts[i]
            if (wipeT >= 0f && !revealed(t.x.toInt(), t.y.toInt())) continue
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
        if (game.screen == Screen.PLAY && !game.fakeShown) controls.draw(canvas, l.controls)
    }

    /** The level with its meta twists: ghost, cracks, credit platforms, then the flipped or rolling picture. */
    private fun level(game: Game, l: Layout) {
        world.draw(game, l)
        twist.inWorld(game, l)
        twist.view(game, l)
    }

    // ---------- transitions ----------

    /** Notices a screen change or a new attempt and keeps the previous frame to dissolve from. */
    private fun transition(game: Game, l: Layout, sameSize: Boolean) {
        val s = game.screen
        val w = game.world
        val newWorld = s == Screen.PLAY && w !== lastWorld
        // a fake win dissolves in exactly like the real clear screen
        val fake = game.fakeShown && !lastFake
        if (lastScreen != null && sameSize && (s != lastScreen || newWorld || fake)) {
            val n = l.lw * l.lh
            if (snapPx.size != n) { snapPx = IntArray(n); curPx = IntArray(n) }
            px.lo.getPixels(snapPx, 0, l.lw, 0, 0, l.lw, l.lh)
            wipeT = game.time
            wipeW = l.lw; wipeH = l.lh
            wipeIris = newWorld && w != null
            if (wipeIris && w != null) {
                // a new attempt opens out of the dark, like a camera iris
                for (i in 0 until n) {
                    val c = snapPx[i]
                    snapPx[i] = Color.rgb((c shr 16 and 0xFF) / 5 + 6, (c shr 8 and 0xFF) / 6 + 2, (c and 0xFF) / 5 + 10)
                }
                wipeCx = l.fx + w.player.box.cx * TS
                wipeCy = l.fy + w.player.box.cy * TS
                wipeMax = hypot(max(wipeCx, l.lw - wipeCx), max(wipeCy, l.lh - wipeCy))
            }
        }
        lastScreen = s
        lastWorld = w
        lastFake = game.fakeShown
    }

    /** 0..1: where pixel ([x], [y]) sits in the wipe order, dithered in 2×2 blocks. */
    private fun order(x: Int, y: Int): Float {
        val v = if (wipeIris) hypot(x - wipeCx, y - wipeCy) / wipeMax else (x + (wipeH - y) * 0.7f) / (wipeW + wipeH * 0.7f)
        return v + bayer(x shr 1, y shr 1) * BAND
    }

    private fun revealed(x: Int, y: Int) = order(x, y) < wipeP * (1f + BAND)

    private fun wipe(game: Game, l: Layout) {
        if (wipeT < 0f) return
        wipeP = (game.time - wipeT) / (if (wipeIris) 0.5f else 0.36f)
        if (wipeP >= 1f || wipeW != l.lw || wipeH != l.lh) { wipeT = -1f; return }
        val w = l.lw
        val h = l.lh
        px.lo.getPixels(curPx, 0, w, 0, 0, w, h)
        val edge = wipeP * (1f + BAND)
        for (y in 0 until h) for (x in 0 until w) {
            val o = order(x, y)
            if (o >= edge) curPx[y * w + x] = snapPx[y * w + x]
        }
        px.lo.setPixels(curPx, 0, w, 0, 0, w, h)
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
        private const val BAND = 0.12f
        private val COOL = intArrayOf(0xFF160A22.toInt(), 0xFF240E30.toInt(), 0xFF34123E.toInt(), 0xFF22244E.toInt(), 0xFF1E405C.toInt(), 0xFF2C5C6E.toInt())
        private val HOT = intArrayOf(0xFF1E0818.toInt(), 0xFF380C22.toInt(), 0xFF5C1028.toInt(), 0xFF841A2C.toInt(), 0xFFAA3232.toInt(), 0xFFCC5A40.toInt())
    }
}
