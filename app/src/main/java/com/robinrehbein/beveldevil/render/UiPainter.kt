package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Hit
import com.robinrehbein.beveldevil.game.Mood
import com.robinrehbein.beveldevil.game.PauseTrick
import com.robinrehbein.beveldevil.game.Rarity
import com.robinrehbein.beveldevil.game.Screen
import com.robinrehbein.beveldevil.game.Twists
import com.robinrehbein.beveldevil.game.Txt
import com.robinrehbein.beveldevil.game.Ui
import com.robinrehbein.beveldevil.game.WorldInfo
import com.robinrehbein.beveldevil.game.Worlds
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Menus (drawn on the 256×144 [Ui] stage), the in-game HUD, Mephi's frame, the speech bubble and trap cards. */
class UiPainter(px: Pixels) : Painter(px) {
    private val fill get() = px.fill
    private val text get() = px.text

    // ---------- Mephi ----------

    fun devilFrame(x: Float, y: Float, size: Float, mood: Mood, t: Float, spriteScale: Int = 1) {
        rect(x + 2, y + 2, size, size, SHADOW)
        val inner = size - 6
        for (yy in 0 until inner.toInt()) {
            // velvet darkens downward, with a line-dithered seam
            val v = yy / inner
            val dark = v > 0.66f || (v > 0.46f && yy % 2 == 0)
            rect(x + 3, y + 3 + yy, inner, 1f, if (dark) VELVET_LO else VELVET)
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
        // small frames crop the suit, never the horns
        val top = if (inner < n) y + 3 else y + size - 3 - n + 2 * spriteScale
        dst.set(x + (size - n) / 2f + jit, top - bob * spriteScale, 0f, 0f)
        dst.right = dst.left + n; dst.bottom = dst.top + n
        lc.drawBitmap(sprite, null, dst, blit)
        lc.restore()
        rect(x - 1, y - 1, size + 2, 1, INK); rect(x - 1, y + size, size + 2, 1, INK); rect(x - 1, y, 1, size, INK); rect(x + size, y, 1, size, INK)
        rect(x, y, size, 3, GOLD); rect(x, y + size - 3, size, 3, GOLD); rect(x, y, 3, size, GOLD); rect(x + size - 3, y, 3, size, GOLD)
        rect(x, y, size, 1, GOLD_HI); rect(x, y, 1, size, GOLD_HI); rect(x, y + size - 1, size, 1, GOLD_LO2); rect(x + size - 1, y, 1, size, GOLD_LO2)
        rect(x + 2, y + 2, size - 4, 1, INK); rect(x + 2, y + 2, 1, size - 4, INK)
        // corner studs and a sparkle that runs around the frame
        for (i in 0 until 4) {
            val cx = if (i % 2 == 0) x - 1 else x + size - 4
            val cy = if (i < 2) y - 1 else y + size - 4
            rect(cx, cy, 5f, 5f, INK); rect(cx + 1, cy + 1, 3f, 3f, GOLD_MID); rect(cx + 1, cy + 1, 1f, 1f, GOLD_SPARK); rect(cx + 3, cy + 3, 1f, 1f, GOLD_LO)
        }
        val per = 4 * (size - 1)
        val d = (t * 22f) % (per * 2.5f)
        if (d < per) {
            val side = (d / (size - 1)).toInt()
            val o = d % (size - 1)
            val sx = when (side) { 0 -> x + o; 1 -> x + size - 1; 2 -> x + size - 1 - o; else -> x }
            val sy = when (side) { 0 -> y; 1 -> y + o; 2 -> y + size - 1; else -> y + size - 1 - o }
            rect(sx, sy, 1f, 1f, WHITE)
        }
    }

    /** Speech bubble whose right edge (with tail) ends at [right]. Wraps to two lines. */
    private fun bubble(text: String, age: Float, right: Float, top: Float, maxW: Float = 118f) {
        val size = 5.5f
        val lines = wrap(text, size, maxW - 10)
        val w = lines.maxOf { textWidth(it, size) } + 12
        val h = 6f + lines.size * 7f
        val x = right - 5 - w
        box(x, top, w, h, CREAM, WHITE)
        rect(x + w, top + 4, 3, 3, CREAM); rect(x + w + 3, top + 5, 2, 2, CREAM)
        bubbleText(lines, age, x + 5, top + 6.5f, size, 7f)
    }

    /** Narrow bubble hanging below Mephi's frame (tail up at [tailX]), shrinking its text to fit [area]. */
    private fun bubbleBelow(text: String, age: Float, tailX: Float, area: Hit) {
        var size = 5.5f
        var lines: List<String>
        while (true) {
            lines = wrap(text, size, area.w - 6f)
            val fits = lines.maxOf { textWidth(it, size) } <= area.w - 6f && 5f + lines.size * size * 1.27f <= area.h
            if (fits || size <= 3.5f) break
            size -= 0.5f
        }
        val lh = size * 1.27f
        val tw = lines.maxOf { textWidth(it, size) }
        val w = min(area.w.toFloat(), tw + 10)
        val h = (5f + lines.size * lh).roundToInt().toFloat()
        val x = (tailX - w / 2).coerceIn(area.x.toFloat(), area.x + area.w - w).roundToInt().toFloat()
        val top = area.y.toFloat()
        box(x, top, w, h, CREAM, WHITE)
        rect(tailX - 1, top - 3, 3, 3, CREAM); rect(tailX, top - 5, 2, 2, CREAM)
        bubbleText(lines, age, x + (w - tw) / 2, top + 2.5f + lh / 2, size, lh)
    }

    private fun bubbleText(lines: List<String>, age: Float, x: Float, y: Float, size: Float, lh: Float) {
        var left = (age * 30f).toInt()
        lines.forEachIndexed { i, l ->
            if (left <= 0) return@forEachIndexed
            say(l.take(left), x, y + i * lh, size, INK_TEXT, shadow = 0)
            left -= l.length + 1
        }
    }

    // ---------- HUD ----------

    fun hud(game: Game, l: Layout) {
        val t = game.time
        if (l.hud == HudMode.TOP) header(l)
        pauseButton(game, l)
        if (l.hud == HudMode.SIDE) sidePills(game, l.pills) else pillRow(game, l)
        val f = l.frame
        devilFrame(f.x.toFloat(), f.y.toFloat(), f.w.toFloat(), game.mood, t)
        game.bubble?.let {
            if (l.hud == HudMode.SIDE) bubbleBelow(it, game.bubbleAge, f.x + f.w / 2f, l.bubble)
            else bubble(it, game.bubbleAge, l.bubble.x.toFloat(), l.bubble.y.toFloat(), l.bubble.w.toFloat())
        }
    }

    /** The HUD pause button, unless a [PauseTrick] makes it dodge or grow spikes. */
    private fun pauseButton(game: Game, l: Layout) {
        val p = l.pause
        var x = p.x.toFloat()
        var y = p.y.toFloat()
        val w = game.world
        if (w != null && w.pauseTrick == PauseTrick.DODGE && w.dodges > 0) {
            val f = (w.time - w.dodgeTime) / Twists.DODGE_TIME
            if (f in 0f..1f) {
                // darts off, loiters, sneaks back
                val e = when { f < 0.18f -> f / 0.18f; f < 0.7f -> 1f; else -> (1f - f) / 0.3f }
                val (dx, dy) = DODGE_HOPS[(w.dodges - 1) % DODGE_HOPS.size]
                x = (x + dx * e).roundToInt().toFloat().coerceIn(2f, l.lw - p.w - 2f)
                y = (y + dy * e + sin(f * PI.toFloat()) * -6f).roundToInt().toFloat().coerceIn(2f, l.lh - p.h - 2f)
            }
        }
        if (w?.pauseTrick == PauseTrick.SPIKE) {
            box(x, y, p.w.toFloat(), p.h.toFloat(), DEVIL_RED_LO, RED_BTN)
            for (i in 0 until 3) {
                val sx = x + 2 + i * 4
                rect(sx + 1, y + 2, 1f, 2f, INK)
                rect(sx, y + 4, 3f, 3f, INK)
                rect(sx + 1, y + 3, 1f, 5f, BONE)
                rect(sx, y + 6, 1f, 3f, BONE); rect(sx + 2, y + 6, 1f, 3f, BONE_LO)
            }
            return
        }
        box(x, y, p.w.toFloat(), p.h.toFloat(), PLUM, PLUM_HI)
        rect(x + 4, y + 3, 2, 6, CREAM); rect(x + 8, y + 3, 2, 6, CREAM)
    }

    /** TOP/OVERLAY: level plate and death counter in one row; the name shrinks to fit left of the devil frame. */
    private fun pillRow(game: Game, l: Layout) {
        val x = l.pills.x.toFloat()
        val y = l.pills.y.toFloat()
        val name = "${game.levelLabel} · ${game.level.name.toString().uppercase()}"
        val dLabel = game.deaths.toString()
        val dw = textWidth(dLabel, 5f) + 18
        val maxW = l.frame.x - 4 - x - 6 - dw
        var size = 5f
        while (size > 2.5f && px.fineWidth(name, size) + 10 > maxW) size -= 0.5f
        val nw = px.fineWidth(name, size) + 10
        box(x, y, nw, 12f, PLUM, PLUM_HI)
        say(name, x + 5, y + 6.3f, size)
        val dx = x + nw + 6
        box(dx, y, dw, 12f, RED_BTN, RED_BTN_HI)
        lc.drawBitmap(Icons.skull, dx + 4, y + 3.5f, null)
        say(dLabel, dx + 13, y + 6.3f, 5f)
    }

    /** Level plaque and death counter stacked in a narrow side column. */
    private fun sidePills(game: Game, col: Hit) {
        val x = col.x.toFloat()
        val w = col.w.toFloat()
        val cx = x + w / 2
        val name = game.level.name.toString().uppercase()
        val maxW = w - 8
        var size = 4f
        while (size > 2.5f && name.split(' ').any { px.fineWidth(it, size) > maxW }) size -= 0.5f
        // a word too long even at the smallest size breaks with a hyphen
        val lines = wrapFine(name, size, maxW).flatMap { line -> if (px.fineWidth(line, size) <= maxW) listOf(line) else hyphenate(line, size, maxW) }
        val lh = size * 1.3f
        val bh = (17f + lines.size * lh + 3).roundToInt().toFloat()
        var y = col.y.toFloat()
        plaque(x, y, w, bh)
        // little horns on top of the plaque
        val hx = cx.roundToInt().toFloat()
        rect(hx - 6, y - 3, 2f, 3f, INK); rect(hx - 5, y - 4, 1f, 2f, INK); rect(hx - 5, y - 2, 1f, 2f, BONE)
        rect(hx + 4, y - 3, 2f, 3f, INK); rect(hx + 4, y - 4, 1f, 2f, INK); rect(hx + 4, y - 2, 1f, 2f, BONE)
        val label = game.levelLabel
        var ls = 9f
        while (ls > 6f && textWidth(label, ls) > w - 6) ls -= 0.5f
        say(label, cx, y + 8.5f, ls, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
        rect(x + 5, y + 15, w - 10, 1f, GOLD_LO2)
        lines.forEachIndexed { i, s -> say(s, cx, y + 17.5f + lh / 2 + i * lh, size, CREAM, Paint.Align.CENTER) }
        y += bh + 6
        val dLabel = game.deaths.toString()
        val cw = 9 + textWidth(dLabel, 5f)
        val dx = (cx - cw / 2).roundToInt().toFloat()
        box(x, y, w, 12f, RED_BTN, RED_BTN_HI)
        rect(x, y + 11, w, 1f, DEVIL_RED_LO)
        lc.drawBitmap(Icons.skull, dx, y + 3.5f, null)
        say(dLabel, dx + 9, y + 6.3f, 5f)
    }

    private fun wrapFine(text: String, size: Float, maxW: Float): List<String> {
        val out = ArrayList<String>()
        var line = ""
        for (word in text.split(' ')) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (px.fineWidth(candidate, size) > maxW && line.isNotEmpty()) { out += line; line = word } else line = candidate
        }
        if (line.isNotEmpty()) out += line
        return out
    }

    private fun hyphenate(word: String, size: Float, maxW: Float): List<String> {
        val out = ArrayList<String>()
        var rest = word
        while (px.fineWidth(rest, size) > maxW && rest.length > 2) {
            var n = rest.length - 1
            while (n > 1 && px.fineWidth(rest.take(n) + "-", size) > maxW) n--
            out += rest.take(n) + "-"
            rest = rest.drop(n)
        }
        out += rest
        return out
    }

    /** Velvet panel in a beveled gold frame. */
    private fun plaque(x: Float, y: Float, w: Float, h: Float) {
        rect(x + 2, y + 2, w, h, SHADOW)
        rect(x - 1, y, w + 2, h, INK); rect(x, y - 1, w, h + 2, INK)
        rect(x, y, w, h, GOLD); rect(x, y, w, 1f, GOLD_HI); rect(x, y, 1f, h, GOLD_HI)
        rect(x, y + h - 1, w, 1f, GOLD_LO); rect(x + w - 1, y, 1f, h, GOLD_LO)
        rect(x + 2, y + 2, w - 4, h - 4, VELVET)
        rect(x + 2, y + 2, w - 4, 1f, INK); rect(x + 2, y + 2, 1f, h - 4, INK)
        rect(x + 2, y + h * 0.6f, w - 4, h * 0.4f - 2, VELVET_LO)
        rect(x + 1, y + 1, 1f, 1f, GOLD_SPARK)
    }

    private var headerBmp: Bitmap? = null
    private var headerFor = 0L

    /** TOP mode: the band above the playfield is a croupier's table edge: velvet with a diamond weave and a gold rail. */
    private fun header(l: Layout) {
        val key = l.lw.toLong() shl 32 or l.fy.toLong()
        if (headerBmp == null || headerFor != key) {
            val w = l.lw
            val h = l.fy
            val out = IntArray(w * h)
            for (y in 0 until h) for (x in 0 until w) {
                val v = y / h.toFloat()
                var c = if (v * 1.3f > bayer(x shr 1, y shr 1)) VELVET else VELVET_LO
                val dx = Math.floorMod(x - l.fx, 12)
                val dy = Math.floorMod(y, 12)
                if (dx == dy || dx == 12 - dy) c = if (c == VELVET) 0xFF4A1636.toInt() else VELVET
                if (dx == 0 && dy == 0) c = GOLD_LO2
                val r = h - 1 - y
                c = when (r) {
                    0 -> INK; 1 -> GOLD_LO; 2 -> GOLD; 3 -> GOLD_HI; 4 -> INK
                    5, 6 -> if ((x + r) % 2 == 0) INK else c
                    else -> c
                }
                if (r in 1..3 && Math.floorMod(x - l.fx, 16) == 8) c = if (r == 3) GOLD_SPARK else if (r == 2) GOLD_HI else GOLD_LO2
                out[y * w + x] = c
            }
            headerBmp = Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888)
            headerFor = key
        }
        lc.drawBitmap(headerBmp!!, 0f, 0f, null)
    }

    // ---------- trap card (screen pixels) ----------

    fun flyingCard(canvas: Canvas, game: Game, card: Card, l: Layout) {
        val sc = l.sc.toFloat()
        val age = game.cardAge
        val f = min(1f, age / 0.55f)
        val out = age > Game.CARD_LIFE - 0.4f
        val fade = if (out) max(0f, (Game.CARD_LIFE - age) / 0.4f) else 1f
        val fr = l.frame
        val sx = fr.x + fr.w / 2f
        val sy = fr.y + fr.h / 2f
        val tx = l.fx + 128f
        val ty = l.fy + 66f
        val k = min(1f, f * 1.4f)
        val cx = (sx + (tx - sx) * k) * sc
        val cy = (sy + (ty - sy) * k - sin(k * PI.toFloat()) * 18f) * sc
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

    fun bigCard(canvas: Canvas, game: Game, card: Card, l: Layout) {
        val sc = l.sc.toFloat()
        fill.color = 0xB0000000.toInt()
        canvas.drawRect(0f, 0f, l.lw * sc, l.lh * sc, fill)
        canvas.save()
        canvas.translate((l.sx + 128f) * sc, (l.sy + 72f) * sc)
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

    // ---------- screens (stage coordinates) ----------

    private var stripBmp: Bitmap? = null
    private var stripTheme: Theme? = null
    private val stripFor = IntArray(4)

    /** Gold floor strip at the stage bottom, stretched across the canvas, with rock below it. */
    fun floorStrip(l: Layout, theme: Theme = Themes.HELL) {
        if (stripBmp == null || stripTheme !== theme || stripFor[0] != l.lw || stripFor[1] != l.lh || stripFor[2] != l.sx || stripFor[3] != l.sy) {
            val c0 = -((l.sx + TS - 1) / TS) - 1
            val gw = (l.lw - l.sx) / TS + 2 - c0
            val ox = l.sx + c0 * TS
            val y = l.sy + 136
            val out = IntArray(l.lw * l.lh)
            Masonry.bake(out, l.lw, l.lh, ox, y, IntArray(gw) { 1 }, gw, 1, c0, 17, false, theme.stone)
            val gh = (l.lh - y) / TS
            // a sliver of rock may remain below the last full tile: keep it from showing the swirl
            if (theme !== Themes.HELL) for (yy in y + TS until l.lh) for (xx in 0 until l.lw) out[yy * l.lw + xx] = theme.rock.face
            if (gh > 1) Masonry.bake(out, l.lw, l.lh, ox, y + TS, IntArray(gw * gh) { 1 }, gw, gh, c0, 18, true, theme.rock)
            stripTheme = theme
            stripBmp = Bitmap.createBitmap(out, l.lw, l.lh, Bitmap.Config.ARGB_8888)
            stripFor[0] = l.lw; stripFor[1] = l.lh; stripFor[2] = l.sx; stripFor[3] = l.sy
        }
        lc.drawBitmap(stripBmp!!, 0f, 0f, null)
    }

    private inline fun stage(l: Layout, s: Screen, block: () -> Unit) = px.at(l.stageX(s), l.stageY(s), block)

    private var backdrop: Bitmap? = null
    private val mephiLogo by lazy { Logo(px, "MEPHI", GOLD, GOLD_MID, GOLD_HI, GOLD_LO2, GOLD_LO, 0xFF5A2A10.toInt()) }
    private val daemonLogo by lazy { Logo(px, "DAEMON", DEVIL_RED, 0xFFFF5E74.toInt(), 0xFFFF9DAA.toInt(), 0xFFA8203A.toInt(), DEVIL_RED_LO, 0xFF420814.toInt()) }

    fun title(game: Game, l: Layout) {
        val t = game.time
        val bd = backdrop?.takeIf { it.width == l.lw + 2 * WorldPainter.PARALLAX }
            ?: Fx.backdrop(l.lw + 2 * WorldPainter.PARALLAX, Ui.H, 136).also { backdrop = it }
        lc.drawBitmap(bd, (-WorldPainter.PARALLAX + sin(t * 0.21f) * 12f).roundToInt().toFloat(), l.sy.toFloat(), null)
        floorStrip(l)
        stage(l, Screen.TITLE) {
            mephiLogo.draw(lc, 16f, 23f + (sin(t * 1.7f) * 0.7f).roundToInt(), t)
            say("THE", 18f, 49f, 4f, CREAM)
            daemonLogo.draw(lc, 16f, 50f + (sin(t * 1.7f + 1.4f) * 0.7f).roundToInt(), t - 0.6f)
            say(Txt.tap.toString(), 20f, 82f, 5.5f, if ((t * 2).toInt() % 2 == 0) CREAM else 0xFFB9A6CF.toInt())
            devilFrame(172f, 22f, 68f, if ((t % 6f) < 1.2f) Mood.LAUGH else Mood.GRIN, t, spriteScale = 2)
            button(Ui.titlePlay, Txt.play.toString(), true)
            button(Ui.titleAlbum, "${Txt.album} ${Card.entries.count { game.cardFound(it) }}/${Card.entries.size}", false)
            box(Ui.sound, PLUM, PLUM_HI)
            say(if (game.soundOn) Txt.soundOn.toString() else Txt.soundOff.toString(), Ui.sound.x + Ui.sound.w / 2f, Ui.sound.y + 6.3f, 4f, CREAM, Paint.Align.CENTER)
            // Bevel strolls along the floor, bobbing and blinking
            val bx = (40f + ((t * 20f) % 170f)).roundToInt().toFloat()
            val by = 129f - (if (sin(t * 16f) > 0.2f) 1 else 0)
            rect(bx + 2, by + 2, 6f, 7f, SHADOW)
            rect(bx - 1, by, 8f, 7f, HERO_OUT); rect(bx, by - 1, 6f, 9f, HERO_OUT)
            rect(bx, by, 6f, 7f, MINT); rect(bx, by, 6f, 1f, MINT_HI); rect(bx, by, 1f, 7f, MINT_HI)
            rect(bx, by + 6, 6f, 1f, MINT_LO); rect(bx + 5, by + 1, 1f, 6f, MINT_LO); rect(bx, by, 1f, 1f, WHITE)
            if ((t + 0.4f) % 2.9f < 0.12f) { rect(bx + 1, by + 3, 2f, 1f, HERO_OUT); rect(bx + 3, by + 3, 2f, 1f, HERO_OUT) }
            else {
                rect(bx + 1, by + 2, 2f, 2f, WHITE); rect(bx + 3, by + 2, 2f, 2f, WHITE)
                rect(bx + 2, by + 3, 1f, 1f, DOOR_DARK); rect(bx + 4, by + 3, 1f, 1f, DOOR_DARK)
            }
        }
    }

    private var selTheme: Theme? = null
    private var selBackdrop: Backdrop? = null

    /** The shown world's far scenery behind the level select, drifting like the title's; hell keeps the swirl. */
    private fun worldBackdrop(game: Game, l: Layout) {
        val theme = Themes.of(game.selWorld.number)
        if (theme === Themes.HELL) return
        val width = l.lw + 2 * WorldPainter.PARALLAX
        val bd = selBackdrop?.takeIf { selTheme === theme && it.bmp.width == width }
            ?: theme.build(width, Ui.H).also { selBackdrop = it; selTheme = theme }
        // above the stage (tall screens) the scenery's top row continues
        rect(0f, 0f, l.lw.toFloat(), l.sy.toFloat() + 1, bd.bmp.getPixel(0, 0))
        val dx = (-WorldPainter.PARALLAX + sin(game.time * 0.21f) * 12f).roundToInt()
        lc.drawBitmap(bd.bmp, dx.toFloat(), l.sy.toFloat(), null)
        val leds = bd.leds
        for (i in 0 until leds.size / 4) {
            if (((game.time * 0.22f + leds[4 * i + 3] / 256f) % 1f) > 0.66f) continue
            rect((leds[4 * i] + dx).toFloat(), (leds[4 * i + 1] + l.sy).toFloat(), 1f, 1f, leds[4 * i + 2])
        }
    }

    fun select(game: Game, l: Layout) {
        worldBackdrop(game, l)
        floorStrip(l, Themes.of(game.selWorld.number))
        stage(l, Screen.SELECT) {
            val t = game.time
            button(Ui.back, "<", false)
            val n = Worlds.all.size
            Worlds.all.forEachIndexed { k, w -> worldTab(game, w, Ui.worldTab(k, n), t) }
            val sw = game.selWorld
            val name = sw.name.toString().uppercase()
            var size = 7f
            while (size > 5f && textWidth(name, size) > 200f) size -= 0.5f
            say(name, 128f, 29f, size, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            for (slot in 0 until Ui.PAGE) {
                val i = game.selLevel(slot)
                if (i >= 0) levelTile(game, i, Ui.levelTile(slot), t)
            }
            val pages = game.pages()
            if (pages > 1) {
                pageArrow(Ui.pagePrev, -1, game.selPage > 0, t)
                pageArrow(Ui.pageNext, 1, game.selPage < pages - 1, t)
                for (p in 0 until pages) pip(game, sw, p, Ui.pageDot(p, pages))
            }
            button(Ui.selectAlbum, "${Txt.album} ${Card.entries.count { game.cardFound(it) }}/${Card.entries.size}", false)
        }
    }

    /** World tab: gold when shown, plum when open, rock with a lock until its first level unlocks. */
    private fun worldTab(game: Game, w: WorldInfo, h: Hit, t: Float) {
        val x = h.x.toFloat()
        val y = h.y.toFloat()
        val cx = x + h.w / 2f
        val label = Txt.worldNo.toString().replace("%d", w.number.toString())
        when {
            w === game.selWorld -> {
                bevelGold(x, y, h.w.toFloat(), h.h.toFloat())
                say(label, cx, y + 7f, 5.5f, INK_TEXT, Paint.Align.CENTER, GOLD_HI)
                // a notch points down at the page
                val bob = if ((t * 2f).toInt() % 2 == 0) 0f else 1f
                rect(cx - 3, y + h.h + 1 + bob, 7f, 1f, GOLD); rect(cx - 2, y + h.h + 2 + bob, 5f, 1f, GOLD); rect(cx - 1, y + h.h + 3 + bob, 3f, 1f, GOLD_LO)
            }
            game.worldOpen(w) -> {
                box(h, PLUM, PLUM_HI)
                say(label, cx, y + 7f, 5.5f, CREAM, Paint.Align.CENTER)
            }
            else -> {
                box(h, ROCK, ROCK_HI)
                rect(x, y + h.h - 1, h.w.toFloat(), 1f, ROCK_LO)
                val s = if (w.size == 0) Txt.soon.toString() else label
                val tw = textWidth(s, 5f)
                val lx = (cx - (tw + 11) / 2 + 2).roundToInt().toFloat()
                rect(lx - 2, y + 2, 10f, 10f, INK); rect(lx - 1, y + 3, 8f, 8f, LOCKED_HI); rect(lx - 1, y + 10, 8f, 1f, LOCKED)
                lc.drawBitmap(Icons.lock, lx, y + 4, null)
                say(s, lx + 11, y + 7f, 5f, LOCKED_HI, Paint.Align.LEFT, ROCK_LO)
            }
        }
    }

    /** Beveled gold slab, the face of open level tiles and the shown world's tab. */
    private fun bevelGold(x: Float, y: Float, w: Float, hh: Float) {
        rect(x + 2, y + 2, w, hh, SHADOW)
        rect(x - 1, y, w + 2, hh, INK); rect(x, y - 1, w, hh + 2, INK)
        rect(x, y, w, hh, GOLD)
        rect(x, y, w, 1f, GOLD_HI); rect(x, y, 1f, hh, GOLD_HI); rect(x + 1, y + 1, w - 2, 1f, GOLD_MID); rect(x + 1, y + 1, 1f, hh - 2, GOLD_MID)
        rect(x, y + hh - 1, w, 1f, GOLD_LO); rect(x + w - 1, y, 1f, hh, GOLD_LO); rect(x + 1, y + hh - 2, w - 2, 1f, GOLD_LO2); rect(x + w - 2, y + 1, 1f, hh - 2, GOLD_LO2)
        rect(x + 1, y + 1, 1f, 1f, GOLD_SPARK)
    }

    /** Tile of global level [i]: its number within the world, best deaths, locked or open. */
    private fun levelTile(game: Game, i: Int, h: Hit, t: Float) {
        val open = i < game.unlocked()
        val x = h.x.toFloat()
        val y = h.y.toFloat()
        val w = h.w.toFloat()
        val hh = h.h.toFloat()
        val num = Worlds.local(i).toString()
        var ns = 11f
        while (ns > 7f && textWidth(num, ns) > w - 10) ns -= 0.5f
        if (open) {
            bevelGold(x, y, w, hh)
            // engraved number plate
            rect(x + 3, y + 4, w - 6, 15f, 0xFFD9963F.toInt())
            rect(x + 3, y + 4, w - 6, 1f, GOLD_LO2); rect(x + 3, y + 4, 1f, 15f, GOLD_LO2)
            rect(x + 3, y + 18, w - 6, 1f, GOLD_MID); rect(x + w - 4, y + 4, 1f, 15f, GOLD_MID)
            say(num, x + w / 2f, y + 12f, ns, INK_TEXT, Paint.Align.CENTER, GOLD_HI)
            val best = game.bestDeaths(i)
            if (best == null) say(Txt.new.toString(), x + w / 2f, y + 25f, 4f, INK_TEXT, Paint.Align.CENTER, 0)
            else {
                lc.drawBitmap(Icons.skull, x + 3f, y + 23f, null)
                say(best.toString(), x + 16f, y + 25.5f, 4.5f, INK_TEXT, Paint.Align.CENTER, 0)
            }
            if (i == game.unlocked() - 1 && best == null) {
                val a = ((sin(t * 5f) + 1) * 0.5f * 255).toInt()
                val c = Color.argb(a, 108, 242, 194)
                rect(x - 3, y - 3, w + 6, 1f, c); rect(x - 3, y + hh + 2, w + 6, 1f, c)
                rect(x - 3, y - 3, 1f, hh + 6, c); rect(x + w + 2, y - 3, 1f, hh + 6, c)
                // a sparkle hops around the next level
                val k = ((t * 3f).toInt() % 4)
                rect(if (k % 2 == 0) x - 3 else x + w + 2, if (k < 2) y - 3 else y + hh + 2, 1f, 1f, WHITE)
            }
        } else {
            rect(x + 2, y + 2, w, hh, SHADOW)
            rect(x - 1, y, w + 2, hh, INK); rect(x, y - 1, w, hh + 2, INK)
            rect(x, y, w, hh, ROCK)
            rect(x, y, w, 1f, ROCK_HI); rect(x, y, 1f, hh, ROCK_HI)
            rect(x, y + hh - 1, w, 1f, ROCK_LO); rect(x + w - 1, y, 1f, hh, ROCK_LO)
            rect(x + 3, y + hh - 6, 1f, 2f, ROCK_LO); rect(x + 4, y + hh - 4, 1f, 2f, ROCK_LO)
            say(num, x + w / 2f, y + 12f, ns, LOCKED_HI, Paint.Align.CENTER, ROCK_LO)
            val lx = x + (w - 10) / 2
            rect(lx, y + 19, 10f, 10f, INK); rect(lx + 1, y + 20, 8f, 8f, LOCKED_HI); rect(lx + 1, y + 27, 8f, 1f, LOCKED)
            lc.drawBitmap(Icons.lock, lx + 2, y + 21f, null)
        }
    }

    /** Slim page button centered in the tappable strip [h]; [dir] -1 = back. Dimmed at the first/last page. */
    private fun pageArrow(h: Hit, dir: Int, enabled: Boolean, t: Float) {
        val bw = 12f
        val bh = 30f
        val x = (h.x + (h.w - bw) / 2).roundToInt().toFloat()
        val y = (h.y + (h.h - bh) / 2).roundToInt().toFloat()
        if (enabled) box(x, y, bw, bh, PLUM, PLUM_HI) else { box(x, y, bw, bh, ROCK_LO2, ROCK_MID); rect(x, y + bh - 1, bw, 1f, ROCK_LO) }
        val nudge = if (enabled && (t % 1.6f) < 0.2f) dir.toFloat() else 0f
        val c = if (enabled) CREAM else ROCK_HI
        val cx = x + 4 + nudge
        val cy = y + bh / 2 - 4
        // a chunky 4×7 chevron
        for (r in 0 until 7) {
            val len = 4 - abs(r - 3)
            rect(if (dir < 0) cx + 4 - len else cx, cy + r, len.toFloat(), 1f, c)
        }
        if (!enabled) return
        rect(if (dir < 0) cx + 3 else cx, cy, 1f, 1f, WHITE)
    }

    /** Page pip: gold for the shown page, mint when all its levels are cleared, plum when playable, rock when locked. */
    private fun pip(game: Game, w: WorldInfo, p: Int, h: Hit) {
        val first = w.firstLevel + p * Ui.PAGE
        val last = minOf(w.firstLevel + w.size, first + Ui.PAGE) - 1
        val cur = p == game.selPage
        val c = when {
            cur -> GOLD
            (first..last).all { game.bestDeaths(it) != null } -> MINT_LO
            first < game.unlocked() -> PLUM_HI
            else -> ROCK
        }
        val pw = if (cur) 8f else 6f
        val ph = if (cur) 6f else 4f
        val x = h.x + (h.w - pw) / 2
        val y = h.y + (h.h - ph) / 2
        rect(x + 1, y + 1, pw, ph, SHADOW)
        rect(x - 1, y, pw + 2, ph, INK); rect(x, y - 1, pw, ph + 2, INK)
        rect(x, y, pw, ph, c)
        rect(x, y, pw, 1f, if (cur) GOLD_HI else if (c == MINT_LO) MINT else if (c == PLUM_HI) 0xFF6A4A88.toInt() else ROCK_HI)
    }

    fun album(game: Game, l: Layout) {
        floorStrip(l)
        stage(l, Screen.ALBUM) {
            button(Ui.back, "<", false)
            val found = Card.entries.count { game.cardFound(it) }
            say("${Txt.albumTitle} $found/${Card.entries.size}", 128f, 14f, 7f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            val pages = game.albumPages()
            val first = game.albumPage * Ui.ALBUM_PAGE
            if (pages > 1) {
                pageArrow(Ui.pagePrev, -1, game.albumPage > 0, game.time)
                pageArrow(Ui.pageNext, 1, game.albumPage < pages - 1, game.time)
                for (p in 0 until pages) {
                    val cur = p == game.albumPage
                    rect(128f - pages * 5 + p * 10 + 2, 3f, 6f, 3f, if (cur) GOLD else ROCK)
                }
            }
            Card.entries.forEachIndexed { i, c ->
                if (i < first || i >= first + Ui.ALBUM_PAGE) return@forEachIndexed
                val h = Ui.albumCard(i)
                val bob = (sin(game.time * 2f + i) * 1.2f).roundToInt()
                val x = h.x.toFloat()
                val y = h.y.toFloat() + bob
                val w = h.w.toFloat()
                val hh = h.h.toFloat()
                rect(x + 2, y + 2 - bob, w, hh, SHADOW)
                rect(x - 1, y, w + 2, hh, INK); rect(x, y - 1, w, hh + 2, INK)
                if (game.cardFound(c)) {
                    rect(x, y, w, hh, if (c.rarity == Rarity.LEGENDARY) CARD_GOLD else CARD_CREAM)
                    rect(x, y, w, 1f, WHITE); rect(x, y + hh - 1, w, 1f, CARD_EDGE)
                    val edge = when (c.rarity) { Rarity.COMMON -> CARD_EDGE; Rarity.RARE -> TEAL; Rarity.LEGENDARY -> GOLD }
                    rect(x + 2, y + 2, w - 4, 1f, edge); rect(x + 2, y + hh - 3, w - 4, 1f, edge); rect(x + 2, y + 2, 1f, hh - 4, edge); rect(x + w - 3, y + 2, 1f, hh - 4, edge)
                    // rarity gem in the corner
                    if (c.rarity != Rarity.COMMON) {
                        rect(x + w - 7, y + 4, 3f, 1f, edge); rect(x + w - 6, y + 3, 1f, 3f, edge); rect(x + w - 6, y + 4, 1f, 1f, WHITE)
                    }
                    lc.drawBitmap(Icons.card(c), x + 7, y + 8, null)
                    say(c.title.toString().uppercase(), x + w / 2f, y + 31f, 3.2f, INK_TEXT, Paint.Align.CENTER, 0)
                    lc.drawBitmap(Icons.skull, x + 6, y + 36f, null)
                    say(game.cardDeaths(c).toString(), x + 19f, y + 38.5f, 3.6f, INK_TEXT, Paint.Align.CENTER, 0)
                    // a shine sweeps over the card now and then
                    val ph = (game.time * 0.35f + i * 0.17f) % 1f
                    if (ph < 0.25f) {
                        val d = (ph / 0.25f * (w + hh)).toInt()
                        for (yy in 1 until hh.toInt() - 1) {
                            val xx = d - yy / 2
                            if (xx in 1 until w.toInt() - 1) rect(x + xx, y + yy, 1f, 1f, 0x90FFFFFF.toInt())
                        }
                    }
                } else {
                    rect(x, y, w, hh, CARD_BACK)
                    for (yy in 4 until hh.toInt() - 4 step 2) for (xx in 4 until w.toInt() - 4 step 2) {
                        if ((xx / 2 + yy / 2) % 2 == 0) rect(x + xx, y + yy, 1f, 1f, 0xFF751424.toInt())
                    }
                    rect(x + 3, y + 3, w - 6, 1f, GOLD); rect(x + 3, y + hh - 4, w - 6, 1f, GOLD); rect(x + 3, y + 3, 1f, hh - 6, GOLD); rect(x + w - 4, y + 3, 1f, hh - 6, GOLD)
                    rect(x, y, w, 1f, 0xFFB8384A.toInt())
                    lc.drawBitmap(Icons.back, x + (w - 16) / 2, y + (hh - 16) / 2, null)
                }
            }
            say(Txt.tapCard.toString(), 128f, 136f - 3f, 4.5f, CREAM, Paint.Align.CENTER)
        }
    }

    private fun dim(l: Layout) = rect(0, 0, l.lw, l.lh, 0xB0100818.toInt())

    fun pause(game: Game, l: Layout) {
        dim(l)
        stage(l, Screen.PAUSE) {
            say(Txt.pause.toString(), 128f, 36f, 14f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            if (game.pauseSwapped) {
                // the two buttons trade places in front of you, swinging out to either side
                val f = ((game.time - game.pausedAt - 0.15f) / 0.5f).coerceIn(0f, 1f)
                val e = f * f * (3 - 2 * f)
                val dy = ((Ui.pauseLevels.y - Ui.pauseResume.y) * e).roundToInt()
                val dx = (sin(e * PI.toFloat()) * 46f).roundToInt()
                val r = Ui.pauseResume
                button(Hit(r.x - dx, r.y + dy, r.w, r.h), Txt.levels.toString(), false)
                button(Hit(r.x + dx, Ui.pauseLevels.y - dy, r.w, r.h), Txt.resume.toString(), true)
            } else {
                button(Ui.pauseResume, Txt.resume.toString(), true)
                button(Ui.pauseLevels, Txt.levels.toString(), false)
            }
        }
    }

    fun clear(game: Game, l: Layout) {
        dim(l)
        stage(l, Screen.CLEAR) {
            say(Txt.cleared.toString(), 128f, 22f, 16f, MINT, Paint.Align.CENTER, MINT_LO)
            val best = game.bestDeaths(game.levelIndex)
            say("${game.levelLabel}   ${Txt.deaths} ${game.deaths}   ${Txt.best} ${best ?: game.deaths}", 128f, 40f, 5.5f, CREAM, Paint.Align.CENTER)
            devilFrame(70f, 52f, 44f, Mood.SHOCK, game.time)
            game.bubble?.let { bubble(it, game.bubbleAge + 10f, 232f, 64f, 110f) }
            button(Ui.clearNext, Txt.next.toString(), true)
        }
    }

    fun end(game: Game, l: Layout) {
        dim(l)
        stage(l, Screen.END) {
            say(Txt.endTitle.toString(), 128f, 26f, 12f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            say(Txt.endSub.toString(), 128f, 42f, 7f, DEVIL_RED, Paint.Align.CENTER)
            devilFrame(109f, 52f, 38f, Mood.SULK, game.time)
            say(Txt.endQuote.toString(), 128f, 96f, 5f, DEVIL_RED, Paint.Align.CENTER)
            say(Txt.endDeaths.toString().replace("%d", game.totalBestDeaths().toString()), 128f, 105f, 5.5f, CREAM, Paint.Align.CENTER)
            button(Ui.endTitle, Txt.toTitle.toString(), true)
        }
    }

    private companion object {
        /** Where the dodging pause button hops to, in logical pixels from home. */
        val DODGE_HOPS = listOf(64f to 4f, 26f to 70f, 150f to 22f)
    }
}
