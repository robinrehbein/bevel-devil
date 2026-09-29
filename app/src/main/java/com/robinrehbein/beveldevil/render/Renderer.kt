package com.robinrehbein.beveldevil.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import com.robinrehbein.beveldevil.R
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Dir
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Hit
import com.robinrehbein.beveldevil.game.Levels
import com.robinrehbein.beveldevil.game.Mood
import com.robinrehbein.beveldevil.game.Rarity
import com.robinrehbein.beveldevil.game.Screen
import com.robinrehbein.beveldevil.game.Txt
import com.robinrehbein.beveldevil.game.Ui
import com.robinrehbein.beveldevil.game.World
import com.robinrehbein.beveldevil.game.WorldState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** On-screen touch buttons in screen pixels; filled in by the view. */
class ControlLayout {
    var r = 0f
    var leftX = 0f
    var rightX = 0f
    var jumpX = 0f
    var y = 0f
    var left = false
    var right = false
    var jump = false
}

/**
 * "Hell CRT" look: everything is drawn into a 256×144 pixel buffer (8 px per tile),
 * scaled up without filtering, then text, the flying trap card and a scanline/vignette
 * pass are drawn at full resolution on top.
 */
class Renderer(context: Context) {
    private val lo = Bitmap.createBitmap(PW, PH, Bitmap.Config.ARGB_8888)
    private val lc = Canvas(lo)
    private val fill = Paint()
    private val blit = Paint().apply { isFilterBitmap = false; isDither = false }
    private val font = context.resources.getFont(R.font.silkscreen)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = font }
    private val measure = Paint().apply { typeface = font }

    private val swirlPx = IntArray(BW * BH)
    private val swirlBmp = Bitmap.createBitmap(BW, BH, Bitmap.Config.ARGB_8888)
    private val staticBmp = Bitmap.createBitmap(PW, PH, Bitmap.Config.ARGB_8888)
    private var staticLevel: Any? = null

    private class TextCmd(val s: String, val x: Float, val y: Float, val size: Float, val color: Int, val align: Paint.Align, val shadow: Int)
    private val texts = ArrayList<TextCmd>()

    private val src = Rect()
    private val dst = RectF()
    private var scanStep = -1
    private val scanPaint = Paint()
    private var vignetteFor = -1f
    private val vignettePaint = Paint()

    var scale = 1f
        private set
    var originX = 0f
        private set
    var originY = 0f
        private set

    fun layout(w: Int, h: Int) {
        scale = min(w.toFloat() / PW, h.toFloat() / PH)
        originX = (w - PW * scale) / 2f
        originY = (h - PH * scale) / 2f
    }

    // ---------- primitives (logical pixels) ----------

    private fun rect(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int) {
        fill.color = color
        val rx = x.roundToInt().toFloat()
        val ry = y.roundToInt().toFloat()
        c.drawRect(rx, ry, rx + w, ry + h, fill)
    }

    private fun rect(x: Number, y: Number, w: Number, h: Number, color: Int) = rect(lc, x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color)

    private fun say(s: String, x: Float, y: Float, size: Float, color: Int = CREAM, align: Paint.Align = Paint.Align.LEFT, shadow: Int = SHADOW) {
        texts += TextCmd(s, x, y, size, color, align, shadow)
    }

    private fun textWidth(s: String, size: Float): Float {
        measure.textSize = size
        return measure.measureText(s)
    }

    private fun tile(c: Canvas, x: Float, y: Float) {
        rect(c, x, y, 8f, 8f, GOLD); rect(c, x, y, 8f, 1f, GOLD_HI); rect(c, x, y, 1f, 8f, GOLD_HI)
        rect(c, x + 1, y + 1, 6f, 1f, GOLD_MID)
        rect(c, x, y + 7, 8f, 1f, GOLD_LO); rect(c, x + 7, y, 1f, 8f, GOLD_LO); rect(c, x + 1, y + 6, 6f, 1f, GOLD_LO2)
        rect(c, x, y, 1f, 1f, GOLD_SPARK)
    }

    private fun spike(c: Canvas, x: Float, y: Float, dir: Dir) {
        for (row in 1..7) {
            val half = (row + 1) / 2
            for (col in 4 - half until 4 + half) {
                val color = if (col < 4) BONE else BONE_LO
                val (px, py) = when (dir) {
                    Dir.UP -> col to row
                    Dir.DOWN -> col to 7 - row
                    Dir.LEFT -> row to col
                    Dir.RIGHT -> 7 - row to col
                }
                rect(c, x + px, y + py, 1f, 1f, color)
            }
        }
    }

    private fun box(h: Hit, fillColor: Int, hi: Int, shadow: Boolean = true) = box(h.x.toFloat(), h.y.toFloat(), h.w.toFloat(), h.h.toFloat(), fillColor, hi, shadow)

    private fun box(x: Float, y: Float, w: Float, h: Float, fillColor: Int, hi: Int, shadow: Boolean = true) {
        if (shadow) rect(x + 2, y + 2, w, h, SHADOW)
        rect(x - 1, y, w + 2, h, INK); rect(x, y - 1, w, h + 2, INK)
        rect(x, y, w, h, fillColor); rect(x, y, w, 1, hi)
    }

    private fun button(h: Hit, label: String, primary: Boolean) {
        box(h, if (primary) RED_BTN else PLUM, if (primary) RED_BTN_HI else PLUM_HI)
        say(label, h.x + h.w / 2f, h.y + h.h / 2f + 0.5f, if (h.h >= 16) 7f else 6f, CREAM, Paint.Align.CENTER)
    }

    // ---------- background ----------

    private fun swirl(t: Float, heat: Float) {
        val cx = BW / 2f
        val cy = BH / 2f
        val spin = t * 0.25f + heat * 1.2f
        for (y in 0 until BH) for (x in 0 until BW) {
            val dx = x - cx
            val dy = (y - cy) * 1.3f
            val r = sqrt(dx * dx + dy * dy)
            val a = atan2(dy, dx)
            val v = sin(a * 2 + r * 0.09f - spin * 2) * 0.8f + 0.5f * sin(x * 0.07f + t * 0.4f + sin(y * 0.1f + t * 0.6f)) + 0.35f * sin(r * 0.14f - t * 0.9f)
            val n = (v + 1.65f) / 3.3f + (BAYER[(y and 3) * 4 + (x and 3)] / 16f - 0.5f) * 0.16f
            val idx = floor(n * 6).toInt().coerceIn(0, 5)
            val hot = ((x * 7 + y * 13) % 97) / 97f < heat
            swirlPx[y * BW + x] = if (hot) HOT[idx] else COOL[idx]
        }
        swirlBmp.setPixels(swirlPx, 0, BW, 0, 0, BW, BH)
        src.set(0, 0, BW, BH)
        dst.set(0f, 0f, PW.toFloat(), PH.toFloat())
        lc.drawBitmap(swirlBmp, src, dst, blit)
    }

    // ---------- entry ----------

    fun draw(canvas: Canvas, game: Game, controls: ControlLayout) {
        texts.clear()
        swirl(game.time, game.heat)
        when (game.screen) {
            Screen.TITLE -> drawTitle(game)
            Screen.SELECT -> drawSelect(game)
            Screen.ALBUM -> drawAlbum(game)
            Screen.PLAY -> { drawWorld(game); drawHud(game) }
            Screen.PAUSE -> { drawWorld(game); drawPause() }
            Screen.CLEAR -> { drawWorld(game); drawClear(game) }
            Screen.END -> { drawWorld(game); drawEnd(game) }
        }

        canvas.drawColor(NIGHT)
        val sc = scale
        val jx = game.shake * sc * 2f * sin(game.time * 90f)
        val jy = game.shake * sc * 2f * cos(game.time * 77f)
        canvas.save()
        canvas.translate(originX + jx, originY + jy)
        dst.set(0f, 0f, PW * sc, PH * sc)
        canvas.drawBitmap(lo, null, dst, blit)
        for (t in texts) {
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
        if (game.screen == Screen.PLAY) game.card?.let { drawFlyingCard(canvas, game, it) }
        if (game.screen == Screen.ALBUM && game.albumSelection >= 0) drawBigCard(canvas, game, Card.entries[game.albumSelection])
        crt(canvas, sc)
        canvas.restore()
        if (game.screen == Screen.PLAY) drawControls(canvas, controls)
    }

    private fun crt(canvas: Canvas, sc: Float) {
        val step = max(2, sc.roundToInt())
        if (step != scanStep) {
            scanStep = step
            val bmp = Bitmap.createBitmap(1, step, Bitmap.Config.ARGB_8888)
            bmp.eraseColor(Color.TRANSPARENT)
            val lines = max(1, (step * 0.38f).roundToInt())
            for (i in 0 until lines) bmp.setPixel(0, i, 0x2E000000)
            scanPaint.shader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
        canvas.drawRect(0f, 0f, PW * sc, PH * sc, scanPaint)
        if (vignetteFor != sc) {
            vignetteFor = sc
            vignettePaint.shader = RadialGradient(PW * sc / 2, PH * sc / 2, PW * sc * 0.62f, intArrayOf(0, 0, 0x80000000.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, PW * sc, PH * sc, vignettePaint)
    }

    // ---------- world ----------

    private fun buildStatic(w: World) {
        staticBmp.eraseColor(Color.TRANSPARENT)
        val c = Canvas(staticBmp)
        val statics = w.pieces.filter { it.group == null }
        for (p in statics) if (!p.spike) rect(c, p.box.x * TS + 2, p.box.y * TS + 2, 8f, 8f, SHADOW)
        for (p in statics) if (!p.spike) tile(c, p.box.x * TS, p.box.y * TS)
        for (p in statics) if (p.spike) spike(c, p.box.x * TS, p.box.y * TS, p.dir)
        staticLevel = w.level
    }

    private fun drawWorld(game: Game) {
        val w = game.world ?: return
        if (staticLevel !== w.level) buildStatic(w)
        lc.drawBitmap(staticBmp, 0f, 0f, null)
        val t = game.time
        for (g in w.groups.values) {
            if (!g.visible) continue
            val jitter = if (g.mode.name == "FALL") (sin(t * 60f) * 0.8f).roundToInt().toFloat() else 0f
            for (p in g.pieces) if (!p.spike) rect(p.box.x * TS + 2 + jitter, p.box.y * TS + 2, 8, 8, SHADOW)
            for (p in g.pieces) {
                if (p.box.y > w.rows + 1) continue
                if (p.spike) spike(lc, p.box.x * TS, p.box.y * TS, p.dir) else tile(lc, p.box.x * TS + jitter, p.box.y * TS)
            }
        }
        drawDoor(w, t)
        for (s in w.saws) drawSaw(s.x * TS, s.y * TS, s.r * TS, s.angle)
        if (w.state != WorldState.DEAD) drawPlayer(w)
        for (p in game.particles) rect(p.x * TS, p.y * TS, 2, 2, p.color)
    }

    private fun drawDoor(w: World, t: Float) {
        val d = w.door
        val x = (d.box.x * TS).roundToInt().toFloat() + 1
        val flip = d.hanging
        val top = if (flip) (d.box.y * TS).roundToInt().toFloat() else (d.box.b * TS).roundToInt().toFloat() - 13
        val glow = 0.6f + 0.4f * sin(t * 5f)
        rect(x + 2, top + 2, 10, 13, SHADOW)
        rect(x - 1, top - 1, 12, 15, INK)
        rect(x, top, 10, 13, GOLD)
        rect(x, if (flip) top + 12 else top, 10, 1, GOLD_HI); rect(x, top, 1, 13, GOLD_HI)
        val innerTop = if (flip) top else top + 2
        rect(x + 2, innerTop, 6, 11, DOOR_DARK)
        rect(x + 2, innerTop, 6, 11, Color.argb((glow * 80).toInt(), 255, 217, 138))
        rect(x + 6, innerTop + 5, 1, 1, GOLD_HI)
    }

    private fun drawSaw(cx: Float, cy: Float, r: Float, angle: Float) {
        val ri = r.roundToInt() + 1
        for (y in -ri..ri) for (x in -ri..ri) {
            val d = sqrt((x * x + y * y).toFloat())
            if (d > r) continue
            val a = atan2(y.toFloat(), x.toFloat()) + angle
            val tooth = d > r - 1.6f && (floor(a / (PI.toFloat() / 5f)).toInt() and 1) == 0
            val color = when {
                d < 1.5f -> RED_BTN
                tooth -> INK
                d > r - 1.6f -> STEEL_LO
                else -> STEEL
            }
            rect(cx + x, cy + y, 1, 1, color)
        }
    }

    private fun drawPlayer(w: World) {
        val p = w.player
        val flip = w.gravity < 0
        val k = if (w.state == WorldState.WON) 0.8f else 1f
        val pw = max(2, (6 / sqrt(p.squash) * k).roundToInt())
        val ph = max(2, (7 * p.squash * k).roundToInt())
        val x = (p.box.cx * TS - pw / 2f).roundToInt().toFloat()
        val y = if (flip) (p.box.y * TS).roundToInt().toFloat() else (p.box.b * TS).roundToInt().toFloat() - ph
        rect(x + 2, y + 2, pw, ph, SHADOW)
        rect(x - 1, y, pw + 2, ph, HERO_OUT); rect(x, y - 1, pw, ph + 2, HERO_OUT)
        rect(x, y, pw, ph, MINT)
        rect(x, if (flip) y + ph - 1 else y, pw, 1, MINT_HI); rect(x, y, 1, ph, MINT_HI)
        rect(x, if (flip) y else y + ph - 1, pw, 1, MINT_LO); rect(x + pw - 1, y, 1, ph, MINT_LO)
        if (ph > 4 && pw > 4) {
            val ey = if (flip) y + ph - 4 else y + 2
            val look = if (p.facing > 0) 1 else 0
            rect(x + 1, ey, 2, 2, WHITE); rect(x + pw - 3, ey, 2, 2, WHITE)
            rect(x + 1 + look, ey + 1, 1, 1, DOOR_DARK); rect(x + pw - 3 + look, ey + 1, 1, 1, DOOR_DARK)
        }
    }

    // ---------- Mephi ----------

    private fun devilFrame(x: Float, y: Float, size: Float, mood: Mood, t: Float, spriteScale: Int = 1) {
        rect(x + 2, y + 2, size, size, SHADOW)
        for (yy in 0 until (size - 6).toInt()) {
            val dark = yy > (size - 6) * 0.55f
            rect(x + 3, y + 3 + yy, size - 6, 1, if (dark) VELVET_LO else VELVET)
        }
        val blink = mood != Mood.LAUGH && (t % 3.1f) < 0.13f
        val look = if (mood == Mood.GRIN) sin(t * 0.9f).roundToInt() else 0
        val bob = when (mood) {
            Mood.LAUGH -> (abs(sin(t * 14f)) * 2f).roundToInt()
            else -> (sin(t * 2.2f) * 0.6f).roundToInt()
        }
        val jit = if (mood == Mood.SHOCK) sin(t * 50f).roundToInt() else 0
        val sprite = Mephi.sprite(mood, blink, look)
        val n = Mephi.N * spriteScale
        lc.save()
        lc.clipRect(x + 3, y + 3, x + size - 3, y + size - 3)
        dst.set(x + (size - n) / 2f + jit, y + size - 3 - n + 2 * spriteScale - bob * spriteScale, 0f, 0f)
        dst.right = dst.left + n; dst.bottom = dst.top + n
        lc.drawBitmap(sprite, null, dst, blit)
        lc.restore()
        rect(x - 1, y - 1, size + 2, 1, INK); rect(x - 1, y + size, size + 2, 1, INK); rect(x - 1, y, 1, size, INK); rect(x + size, y, 1, size, INK)
        rect(x, y, size, 3, GOLD); rect(x, y + size - 3, size, 3, GOLD); rect(x, y, 3, size, GOLD); rect(x + size - 3, y, 3, size, GOLD)
        rect(x, y, size, 1, GOLD_HI); rect(x, y, 1, size, GOLD_HI); rect(x, y + size - 1, size, 1, GOLD_LO2); rect(x + size - 1, y, 1, size, GOLD_LO2)
        rect(x + 2, y + 2, size - 4, 1, INK); rect(x + 2, y + 2, 1, size - 4, INK)
    }

    /** Speech bubble whose right edge (with tail) ends at [right]. Wraps to two lines. */
    private fun bubble(text: String, age: Float, right: Float, top: Float, maxW: Float = 118f) {
        val size = 5.5f
        val lines = wrap(text, size, maxW - 10)
        val shown = (age * 30f).toInt()
        val w = lines.maxOf { textWidth(it, size) } + 12
        val h = 6f + lines.size * 7f
        val x = right - 5 - w
        box(x, top, w, h, CREAM, WHITE)
        rect(x + w, top + 4, 3, 3, CREAM); rect(x + w + 3, top + 5, 2, 2, CREAM)
        var left = shown
        lines.forEachIndexed { i, l ->
            if (left <= 0) return@forEachIndexed
            say(l.take(left), x + 5, top + 6.5f + i * 7f, size, INK_TEXT, shadow = 0)
            left -= l.length + 1
        }
    }

    private fun wrap(text: String, size: Float, maxW: Float): List<String> {
        val out = ArrayList<String>()
        var line = ""
        for (word in text.split(' ')) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (textWidth(candidate, size) > maxW && line.isNotEmpty()) { out += line; line = word } else line = candidate
        }
        if (line.isNotEmpty()) out += line
        return out
    }

    private fun drawHud(game: Game) {
        val t = game.time
        box(Ui.hudPause, PLUM, PLUM_HI)
        rect(Ui.hudPause.x + 4, Ui.hudPause.y + 3, 2, 6, CREAM); rect(Ui.hudPause.x + 8, Ui.hudPause.y + 3, 2, 6, CREAM)
        val name = "${game.levelIndex + 1} · ${game.level.name.toString().uppercase()}"
        val nw = textWidth(name, 5f) + 10
        box(24f, 3f, nw, 12f, PLUM, PLUM_HI)
        say(name, 29f, 9.3f, 5f)
        val dx = 24f + nw + 6
        val dLabel = game.deaths.toString()
        val dw = textWidth(dLabel, 5f) + 18
        box(dx, 3f, dw, 12f, RED_BTN, RED_BTN_HI)
        lc.drawBitmap(Icons.skull, dx + 4, 6.5f, null)
        say(dLabel, dx + 13, 9.3f, 5f)
        val f = Ui.devilFrame
        devilFrame(f.x.toFloat(), f.y.toFloat(), f.w.toFloat(), game.mood, t)
        game.bubble?.let { bubble(it, game.bubbleAge, f.x - 4f, 18f) }
    }

    // ---------- trap card ----------

    private fun drawFlyingCard(canvas: Canvas, game: Game, card: Card) {
        val sc = scale
        val age = game.cardAge
        val f = min(1f, age / 0.55f)
        val out = age > Game.CARD_LIFE - 0.4f
        val fade = if (out) max(0f, (Game.CARD_LIFE - age) / 0.4f) else 1f
        val fr = Ui.devilFrame
        val sx = fr.x + fr.w / 2f
        val sy = fr.y + fr.h / 2f
        val k = min(1f, f * 1.4f)
        val cx = (sx + (128f - sx) * k) * sc
        val cy = (sy + (66f - sy) * k - sin(k * PI.toFloat()) * 18f) * sc
        val angle = PI.toFloat() * (1 - f)
        val front = angle < PI.toFloat() / 2
        val s = if (out) 1f + (1f - fade) * 0.3f else 0.4f + 0.6f * k
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(((sin(game.time * 2.4f) * 0.05f - 0.05f + (1 - f) * 0.6f) * 180f / PI.toFloat()))
        canvas.scale(max(0.03f, abs(cos(angle))) * s, s)
        drawCardFace(canvas, sc, card, front, (fade * 255).toInt(), game.cardDeaths(card), compact = true)
        canvas.restore()
    }

    private fun drawBigCard(canvas: Canvas, game: Game, card: Card) {
        val sc = scale
        fill.color = 0xB0000000.toInt()
        canvas.drawRect(0f, 0f, PW * sc, PH * sc, fill)
        canvas.save()
        canvas.translate(128f * sc, 72f * sc)
        canvas.scale(1.9f, 1.9f)
        drawCardFace(canvas, sc, card, true, 255, game.cardDeaths(card), compact = false)
        canvas.restore()
    }

    /** Card centered at the origin, 44×60 logical pixels. */
    private fun drawCardFace(canvas: Canvas, sc: Float, card: Card, front: Boolean, alpha: Int, deaths: Int, compact: Boolean) {
        val cw = 44 * sc
        val ch = 60 * sc
        fun r(x: Float, y: Float, w: Float, h: Float, color: Int, rad: Float = 0f) {
            fill.color = color; fill.alpha = (Color.alpha(color) * alpha / 255)
            if (rad > 0) canvas.drawRoundRect(x, y, x + w, y + h, rad, rad, fill) else canvas.drawRect(x, y, x + w, y + h, fill)
        }
        r(-cw / 2 + 4 * sc, -ch / 2 + 5 * sc, cw, ch, 0x99000000.toInt(), 4 * sc)
        r(-cw / 2 - sc, -ch / 2 - sc, cw + 2 * sc, ch + 2 * sc, INK, 5 * sc)
        blit.alpha = alpha
        if (front) {
            r(-cw / 2, -ch / 2, cw, ch, if (card.rarity == Rarity.LEGENDARY) CARD_GOLD else CARD_CREAM, 4 * sc)
            val border = when (card.rarity) { Rarity.COMMON -> CARD_EDGE; Rarity.RARE -> TEAL; Rarity.LEGENDARY -> GOLD }
            fill.style = Paint.Style.STROKE; fill.strokeWidth = sc
            r(-cw / 2 + 3 * sc, -ch / 2 + 3 * sc, cw - 6 * sc, ch - 6 * sc, border)
            fill.style = Paint.Style.FILL
            r(-cw / 2 + 5 * sc, -ch / 2 + 5 * sc, cw - 10 * sc, 8 * sc, if (card.rarity == Rarity.LEGENDARY) RED_BTN else INK)
            text.textAlign = Paint.Align.CENTER
            text.color = CARD_CREAM; text.alpha = alpha; text.textSize = 4.2f * sc
            canvas.drawText(if (compact) Txt.trap.toString() else card.rarity.label.toString().uppercase(), 0f, -ch / 2 + 10.6f * sc, text)
            dst.set(-11 * sc, -15 * sc, 11 * sc, 7 * sc)
            canvas.drawBitmap(Icons.card(card), null, dst, blit)
            text.color = INK; text.alpha = alpha; text.textSize = 5f * sc
            canvas.drawText(card.title.toString().uppercase(), 0f, 13.5f * sc, text)
            text.textSize = 3.2f * sc
            if (!compact) {
                wrap(card.flavor.toString(), 3.2f, 36f).forEachIndexed { i, l -> canvas.drawText(l, 0f, (18f + i * 3.8f) * sc, text) }
            }
            text.alpha = (alpha * 0.7f).toInt()
            canvas.drawText(Txt.caught.toString().replace("%d", deaths.toString()), 0f, ch / 2 - (if (compact) 4.5f else 3.6f) * sc, text)
        } else {
            r(-cw / 2, -ch / 2, cw, ch, CARD_BACK, 4 * sc)
            fill.style = Paint.Style.STROKE; fill.strokeWidth = 2 * sc
            r(-cw / 2 + 4 * sc, -ch / 2 + 4 * sc, cw - 8 * sc, ch - 8 * sc, GOLD)
            fill.style = Paint.Style.FILL
            dst.set(-10 * sc, -10 * sc, 10 * sc, 10 * sc)
            canvas.drawBitmap(Icons.back, null, dst, blit)
        }
        blit.alpha = 255
        text.alpha = 255
    }

    // ---------- screens ----------

    private fun floorStrip() {
        for (x in 0 until 32) tile(lc, x * 8f, 136f)
    }

    private fun drawTitle(game: Game) {
        floorStrip()
        val t = game.time
        say("BEVEL", 18f, 36f, 26f, GOLD, shadow = GOLD_LO)
        say("DEVIL", 18f, 64f, 26f, DEVIL_RED, shadow = DEVIL_RED_LO)
        say(Txt.tap.toString(), 20f, 82f, 5.5f, if ((t * 2).toInt() % 2 == 0) CREAM else 0xFFB9A6CF.toInt())
        devilFrame(172f, 22f, 68f, if ((t % 6f) < 1.2f) Mood.LAUGH else Mood.GRIN, t, spriteScale = 2)
        button(Ui.titlePlay, Txt.play.toString(), true)
        button(Ui.titleAlbum, "${Txt.album} ${Card.entries.count { game.cardFound(it) }}/${Card.entries.size}", false)
        box(Ui.sound, PLUM, PLUM_HI)
        say(if (game.soundOn) Txt.soundOn.toString() else Txt.soundOff.toString(), Ui.sound.x + Ui.sound.w / 2f, Ui.sound.y + 6.3f, 4f, CREAM, Paint.Align.CENTER)
        val px = 40f + ((t * 20f) % 170f)
        rect(px, 129f, 6, 7, MINT); rect(px, 129f, 6, 1, MINT_HI); rect(px + 1, 131f, 2, 2, WHITE); rect(px + 4, 131f, 2, 2, WHITE)
        rect(px + 2, 132f, 1, 1, DOOR_DARK); rect(px + 5, 132f, 1, 1, DOOR_DARK)
    }

    private fun drawSelect(game: Game) {
        floorStrip()
        button(Ui.back, "<", false)
        say(Txt.world.toString(), 128f, 14f, 7f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
        for (i in Levels.all.indices) {
            val h = Ui.levelTile(i)
            val open = i < game.unlocked()
            rect(h.x + 2, h.y + 2, h.w, h.h, SHADOW)
            if (open) {
                rect(h.x, h.y, h.w, h.h, GOLD); rect(h.x, h.y, h.w, 2, GOLD_HI); rect(h.x, h.y, 2, h.h, GOLD_HI)
                rect(h.x, h.y + h.h - 2, h.w, 2, GOLD_LO); rect(h.x + h.w - 2, h.y, 2, h.h, GOLD_LO)
                say((i + 1).toString(), h.x + h.w / 2f, h.y + 12f, 11f, INK_TEXT, Paint.Align.CENTER, GOLD_HI)
                val best = game.bestDeaths(i)
                if (best == null) say(Txt.new.toString(), h.x + h.w / 2f, h.y + 25f, 4f, INK_TEXT, Paint.Align.CENTER, 0)
                else {
                    lc.drawBitmap(Icons.skull, h.x + 6f, h.y + 23f, null)
                    say(best.toString(), h.x + 20f, h.y + 25.5f, 4.5f, INK_TEXT, Paint.Align.CENTER, 0)
                }
                if (i == game.unlocked() - 1 && best == null) {
                    val a = ((sin(game.time * 5f) + 1) * 0.5f * 255).toInt()
                    rect(h.x - 3, h.y - 3, h.w + 6, 1, Color.argb(a, 108, 242, 194)); rect(h.x - 3, h.y + h.h + 2, h.w + 6, 1, Color.argb(a, 108, 242, 194))
                    rect(h.x - 3, h.y - 3, 1, h.h + 6, Color.argb(a, 108, 242, 194)); rect(h.x + h.w + 2, h.y - 3, 1, h.h + 6, Color.argb(a, 108, 242, 194))
                }
            } else {
                rect(h.x, h.y, h.w, h.h, LOCKED)
                rect(h.x, h.y, h.w, 1, LOCKED_HI)
                say((i + 1).toString(), h.x + h.w / 2f, h.y + 12f, 11f, 0x802A0710.toInt(), Paint.Align.CENTER, 0)
                lc.drawBitmap(Icons.lock, h.x + 12f, h.y + 21f, null)
            }
        }
        button(Ui.selectAlbum, "${Txt.album} ${Card.entries.count { game.cardFound(it) }}/${Card.entries.size}", false)
    }

    private fun drawAlbum(game: Game) {
        floorStrip()
        button(Ui.back, "<", false)
        val found = Card.entries.count { game.cardFound(it) }
        say("${Txt.albumTitle} $found/${Card.entries.size}", 128f, 14f, 7f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
        Card.entries.forEachIndexed { i, c ->
            val h = Ui.albumCard(i)
            val bob = (sin(game.time * 2f + i) * 1.2f).roundToInt()
            val x = h.x.toFloat()
            val y = h.y.toFloat() + bob
            rect(x + 2, y + 2, h.w, h.h, SHADOW)
            rect(x - 1, y, h.w + 2, h.h, INK); rect(x, y - 1, h.w, h.h + 2, INK)
            if (game.cardFound(c)) {
                rect(x, y, h.w, h.h, if (c.rarity == Rarity.LEGENDARY) CARD_GOLD else CARD_CREAM)
                val edge = when (c.rarity) { Rarity.COMMON -> CARD_EDGE; Rarity.RARE -> TEAL; Rarity.LEGENDARY -> GOLD }
                rect(x + 2, y + 2, h.w - 4, 1, edge); rect(x + 2, y + h.h - 3, h.w - 4, 1, edge); rect(x + 2, y + 2, 1, h.h - 4, edge); rect(x + h.w - 3, y + 2, 1, h.h - 4, edge)
                lc.drawBitmap(Icons.card(c), x + 7, y + 7, null)
                say(c.title.toString().uppercase(), x + h.w / 2f, y + 31f, 3.2f, INK_TEXT, Paint.Align.CENTER, 0)
                lc.drawBitmap(Icons.skull, x + 6, y + 36f, null)
                say(game.cardDeaths(c).toString(), x + 19f, y + 38.5f, 3.6f, INK_TEXT, Paint.Align.CENTER, 0)
            } else {
                rect(x, y, h.w, h.h, CARD_BACK)
                rect(x + 3, y + 3, h.w - 6, 1, GOLD); rect(x + 3, y + h.h - 4, h.w - 6, 1, GOLD); rect(x + 3, y + 3, 1, h.h - 6, GOLD); rect(x + h.w - 4, y + 3, 1, h.h - 6, GOLD)
                say("?", x + h.w / 2f, y + h.h / 2f, 10f, GOLD, Paint.Align.CENTER, INK)
            }
        }
        say(Txt.tapCard.toString(), 128f, 136f - 3f, 4.5f, CREAM, Paint.Align.CENTER)
    }

    private fun dim() = rect(0, 0, PW, PH, 0xB0100818.toInt())

    private fun drawPause() {
        dim()
        say(Txt.pause.toString(), 128f, 36f, 14f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
        button(Ui.pauseResume, Txt.resume.toString(), true)
        button(Ui.pauseLevels, Txt.levels.toString(), false)
    }

    private fun drawClear(game: Game) {
        dim()
        say(Txt.cleared.toString(), 128f, 22f, 16f, MINT, Paint.Align.CENTER, MINT_LO)
        val best = game.bestDeaths(game.levelIndex)
        say("${Txt.deaths} ${game.deaths}   ${Txt.best} ${best ?: game.deaths}", 128f, 40f, 5.5f, CREAM, Paint.Align.CENTER)
        devilFrame(70f, 52f, 44f, Mood.SHOCK, game.time)
        game.bubble?.let { bubble(it, game.bubbleAge + 10f, 232f, 64f, 110f) }
        button(Ui.clearNext, Txt.next.toString(), true)
    }

    private fun drawEnd(game: Game) {
        dim()
        say(Txt.endTitle.toString(), 128f, 26f, 12f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
        say(Txt.endSub.toString(), 128f, 42f, 7f, DEVIL_RED, Paint.Align.CENTER)
        devilFrame(109f, 52f, 38f, Mood.SULK, game.time)
        say(Txt.endDeaths.toString().replace("%d", game.totalBestDeaths().toString()), 128f, 100f, 5.5f, CREAM, Paint.Align.CENTER)
        button(Ui.endTitle, Txt.toTitle.toString(), true)
    }

    // ---------- touch controls (screen pixels) ----------

    private fun drawControls(canvas: Canvas, c: ControlLayout) {
        fun btn(x: Float, y: Float, dir: Dir, pressed: Boolean) {
            val r = c.r
            fill.color = INK; fill.alpha = 150
            canvas.drawRect(x - r - 3, y - r - 3, x + r + 3, y + r + 3, fill)
            fill.color = if (pressed) PLUM_HI else PLUM; fill.alpha = if (pressed) 200 else 120
            canvas.drawRect(x - r, y - r, x + r, y + r, fill)
            fill.color = GOLD; fill.alpha = 200
            canvas.drawRect(x - r, y - r, x + r, y - r + r * 0.1f, fill); canvas.drawRect(x - r, y - r, x - r + r * 0.1f, y + r, fill)
            // stepped pixel arrow
            val u = r / 7f
            fill.color = CREAM; fill.alpha = if (pressed) 255 else 210
            for (i in 0 until 4) {
                val len = (1 + i * 2) * u
                val off = (i - 1.5f) * u
                when (dir) {
                    Dir.UP -> canvas.drawRect(x - len / 2, y + off - u / 2, x + len / 2, y + off + u / 2, fill)
                    Dir.LEFT -> canvas.drawRect(x + off - u / 2, y - len / 2, x + off + u / 2, y + len / 2, fill)
                    Dir.RIGHT -> canvas.drawRect(x - off - u / 2, y - len / 2, x - off + u / 2, y + len / 2, fill)
                    Dir.DOWN -> {}
                }
            }
            fill.alpha = 255
        }
        btn(c.leftX, c.y, Dir.LEFT, c.left)
        btn(c.rightX, c.y, Dir.RIGHT, c.right)
        btn(c.jumpX, c.y, Dir.UP, c.jump)
    }

    companion object {
        const val PW = Ui.W
        const val PH = Ui.H
        const val TS = 8
        private const val BW = 128
        private const val BH = 72
        private val BAYER = intArrayOf(0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5)
        private val COOL = intArrayOf(0xFF160A22.toInt(), 0xFF240E30.toInt(), 0xFF34123E.toInt(), 0xFF22244E.toInt(), 0xFF1E405C.toInt(), 0xFF2C5C6E.toInt())
        private val HOT = intArrayOf(0xFF1E0818.toInt(), 0xFF380C22.toInt(), 0xFF5C1028.toInt(), 0xFF841A2C.toInt(), 0xFFAA3232.toInt(), 0xFFCC5A40.toInt())

        const val NIGHT = 0xFF07030D.toInt()
        const val SHADOW = 0x8C000000.toInt()
        const val INK = 0xFF1A0610.toInt()
        const val INK_TEXT = 0xFF2A0710.toInt()
        const val CREAM = 0xFFFFF4E0.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
        const val GOLD = 0xFFE8A84A.toInt()
        const val GOLD_HI = 0xFFFFD98A.toInt()
        const val GOLD_MID = 0xFFF6C46C.toInt()
        const val GOLD_LO = 0xFF8F4F1F.toInt()
        const val GOLD_LO2 = 0xFFB8702E.toInt()
        const val GOLD_SPARK = 0xFFFFF1C4.toInt()
        const val BONE = 0xFFFFF4EA.toInt()
        const val BONE_LO = 0xFFCBB8D6.toInt()
        const val MINT = 0xFF6CF2C2.toInt()
        const val MINT_HI = 0xFFB8FFE6.toInt()
        const val MINT_LO = 0xFF2AA97F.toInt()
        const val HERO_OUT = 0xFF0D2A22.toInt()
        const val DOOR_DARK = 0xFF1A0B1E.toInt()
        const val PLUM = 0xFF2C1440.toInt()
        const val PLUM_HI = 0xFF46295F.toInt()
        const val RED_BTN = 0xFFB3321F.toInt()
        const val RED_BTN_HI = 0xFFE0553F.toInt()
        const val DEVIL_RED = 0xFFE2344E.toInt()
        const val DEVIL_RED_LO = 0xFF7A1522.toInt()
        const val VELVET = 0xFF3B0F2A.toInt()
        const val VELVET_LO = 0xFF2A0A1E.toInt()
        const val STEEL = 0xFFD9DBE6.toInt()
        const val STEEL_LO = 0xFF9A9CB0.toInt()
        const val LOCKED = 0xFF5A4050.toInt()
        const val LOCKED_HI = 0xFF7A5A6A.toInt()
        const val CARD_CREAM = 0xFFEFE3CF.toInt()
        const val CARD_GOLD = 0xFFF6DDA4.toInt()
        const val CARD_EDGE = 0xFFC9B9A0.toInt()
        const val CARD_BACK = 0xFF8E1C2C.toInt()
        const val TEAL = 0xFF3AA6A0.toInt()
    }
}
