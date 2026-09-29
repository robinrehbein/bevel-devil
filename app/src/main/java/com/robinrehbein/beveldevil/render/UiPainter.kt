package com.robinrehbein.beveldevil.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Hit
import com.robinrehbein.beveldevil.game.Levels
import com.robinrehbein.beveldevil.game.Mood
import com.robinrehbein.beveldevil.game.Rarity
import com.robinrehbein.beveldevil.game.Screen
import com.robinrehbein.beveldevil.game.Txt
import com.robinrehbein.beveldevil.game.Ui
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
        val p = l.pause
        box(p, PLUM, PLUM_HI)
        rect(p.x + 4, p.y + 3, 2, 6, CREAM); rect(p.x + 8, p.y + 3, 2, 6, CREAM)
        if (l.hud == HudMode.SIDE) sidePills(game, l.pills) else pillRow(game, l.pills.x.toFloat(), l.pills.y.toFloat())
        val f = l.frame
        devilFrame(f.x.toFloat(), f.y.toFloat(), f.w.toFloat(), game.mood, t)
        game.bubble?.let {
            if (l.hud == HudMode.SIDE) bubbleBelow(it, game.bubbleAge, f.x + f.w / 2f, l.bubble)
            else bubble(it, game.bubbleAge, l.bubble.x.toFloat(), l.bubble.y.toFloat(), l.bubble.w.toFloat())
        }
    }

    private fun pillRow(game: Game, x: Float, y: Float) {
        val name = "${game.levelIndex + 1} · ${game.level.name.toString().uppercase()}"
        val nw = textWidth(name, 5f) + 10
        box(x, y, nw, 12f, PLUM, PLUM_HI)
        say(name, x + 5, y + 6.3f, 5f)
        val dx = x + nw + 6
        val dLabel = game.deaths.toString()
        val dw = textWidth(dLabel, 5f) + 18
        box(dx, y, dw, 12f, RED_BTN, RED_BTN_HI)
        lc.drawBitmap(Icons.skull, dx + 4, y + 3.5f, null)
        say(dLabel, dx + 13, y + 6.3f, 5f)
    }

    /** Level badge and death counter stacked in a narrow side column. */
    private fun sidePills(game: Game, col: Hit) {
        val x = col.x.toFloat()
        val w = col.w.toFloat()
        val cx = x + w / 2
        val name = game.level.name.toString().uppercase()
        var size = 4f
        while (size > 3f && name.split(' ').any { textWidth(it, size) > w - 4 }) size -= 0.5f
        val lines = wrap(name, size, w - 4)
        val lh = size * 1.3f
        val bh = (15f + lines.size * lh + 2).roundToInt().toFloat()
        var y = col.y.toFloat()
        box(x, y, w, bh, PLUM, PLUM_HI)
        say("${game.levelIndex + 1}", cx, y + 7f, 9f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
        lines.forEachIndexed { i, s -> say(s, cx, y + 15f + lh / 2 + i * lh, size, CREAM, Paint.Align.CENTER) }
        y += bh + 6
        val dLabel = game.deaths.toString()
        val cw = 9 + textWidth(dLabel, 5f)
        val dx = (cx - cw / 2).roundToInt().toFloat()
        box(x, y, w, 12f, RED_BTN, RED_BTN_HI)
        lc.drawBitmap(Icons.skull, dx, y + 3.5f, null)
        say(dLabel, dx + 9, y + 6.3f, 5f)
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

    /** Gold floor strip at the stage bottom, stretched across the canvas, with rock below it. */
    private fun floorStrip(l: Layout) {
        val y = l.sy + 136
        val x0 = l.sx % TS - TS
        for (x in x0 until l.lw step TS) tile(lc, x.toFloat(), y.toFloat())
        for (ry in y + TS until l.lh step TS) for (x in x0 until l.lw step TS) px.rockTile(lc, x.toFloat(), ry.toFloat(), (x * 31 + ry * 17) and 0xFFFF)
    }

    private fun stage(l: Layout, s: Screen, block: () -> Unit) = px.at(l.stageX(s), l.stageY(s), block)

    fun title(game: Game, l: Layout) {
        floorStrip(l)
        stage(l, Screen.TITLE) {
            val t = game.time
            say("BEVEL", 18f, 36f, 26f, GOLD, shadow = GOLD_LO)
            say("DEVIL", 18f, 64f, 26f, DEVIL_RED, shadow = DEVIL_RED_LO)
            say(Txt.tap.toString(), 20f, 82f, 5.5f, if ((t * 2).toInt() % 2 == 0) CREAM else 0xFFB9A6CF.toInt())
            devilFrame(172f, 22f, 68f, if ((t % 6f) < 1.2f) Mood.LAUGH else Mood.GRIN, t, spriteScale = 2)
            button(Ui.titlePlay, Txt.play.toString(), true)
            button(Ui.titleAlbum, "${Txt.album} ${Card.entries.count { game.cardFound(it) }}/${Card.entries.size}", false)
            box(Ui.sound, PLUM, PLUM_HI)
            say(if (game.soundOn) Txt.soundOn.toString() else Txt.soundOff.toString(), Ui.sound.x + Ui.sound.w / 2f, Ui.sound.y + 6.3f, 4f, CREAM, Paint.Align.CENTER)
            val bx = 40f + ((t * 20f) % 170f)
            rect(bx, 129f, 6, 7, MINT); rect(bx, 129f, 6, 1, MINT_HI); rect(bx + 1, 131f, 2, 2, WHITE); rect(bx + 4, 131f, 2, 2, WHITE)
            rect(bx + 2, 132f, 1, 1, DOOR_DARK); rect(bx + 5, 132f, 1, 1, DOOR_DARK)
        }
    }

    fun select(game: Game, l: Layout) {
        floorStrip(l)
        stage(l, Screen.SELECT) {
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
    }

    fun album(game: Game, l: Layout) {
        floorStrip(l)
        stage(l, Screen.ALBUM) {
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
    }

    private fun dim(l: Layout) = rect(0, 0, l.lw, l.lh, 0xB0100818.toInt())

    fun pause(l: Layout) {
        dim(l)
        stage(l, Screen.PAUSE) {
            say(Txt.pause.toString(), 128f, 36f, 14f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            button(Ui.pauseResume, Txt.resume.toString(), true)
            button(Ui.pauseLevels, Txt.levels.toString(), false)
        }
    }

    fun clear(game: Game, l: Layout) {
        dim(l)
        stage(l, Screen.CLEAR) {
            say(Txt.cleared.toString(), 128f, 22f, 16f, MINT, Paint.Align.CENTER, MINT_LO)
            val best = game.bestDeaths(game.levelIndex)
            say("${Txt.deaths} ${game.deaths}   ${Txt.best} ${best ?: game.deaths}", 128f, 40f, 5.5f, CREAM, Paint.Align.CENTER)
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
            say(Txt.endDeaths.toString().replace("%d", game.totalBestDeaths().toString()), 128f, 100f, 5.5f, CREAM, Paint.Align.CENTER)
            button(Ui.endTitle, Txt.toTitle.toString(), true)
        }
    }
}
