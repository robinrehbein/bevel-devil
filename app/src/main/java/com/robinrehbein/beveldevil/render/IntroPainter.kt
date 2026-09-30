package com.robinrehbein.beveldevil.render

import android.graphics.Paint
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Intro
import com.robinrehbein.beveldevil.game.Mood
import com.robinrehbein.beveldevil.game.T
import com.robinrehbein.beveldevil.game.Txt
import com.robinrehbein.beveldevil.game.Ui
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** The story intro (boot log, Mephi, Bevel, the way down) and the per-world transition screen. */
class IntroPainter(px: Pixels, private val ui: UiPainter) : Painter(px) {
    private val dictHead = T("daemon", "Daemon, der")
    private val dict1 = T("1. background process", "1. Hintergrundprozess")
    private val dict2 = T("2. devil", "2. Dämon")
    private val dictSee = T("see: Mephi", "siehe: Mephi")
    private val firewall = T("FIREWALL", "FIREWALL")
    private val layerNames = listOf(T("USERSPACE", "USERSPACE"), T("NETWORK", "NETZWERK"), T("HARDWARE", "HARDWARE"), T("ROOT #", "ROOT #"))
    private val prompt = "root@hell:~# "

    // ---------- intro ----------

    fun intro(game: Game, l: Layout) {
        val page = Intro.pages[game.introPage]
        val age = game.introAge
        val t = game.time
        val done = age >= Intro.duration(game.introPage)
        if (page.kind == Intro.Kind.BOOT) rect(0, 0, l.lw, l.lh, TERM_BG)
        px.at(l.sx, l.sy) {
            when (page.kind) {
                Intro.Kind.BOOT -> terminal(age, t)
                Intro.Kind.DAEMON -> daemon(page, age, t)
                Intro.Kind.PACKET -> packet(page, age, t)
                Intro.Kind.ROOT -> root(page, age, t)
            }
            box(Ui.introSkip, PLUM, PLUM_HI)
            say(Txt.skip.toString(), Ui.introSkip.x + Ui.introSkip.w / 2f, Ui.introSkip.y + 6.3f, 4f, CREAM, Paint.Align.CENTER)
            for (i in Intro.pages.indices) rect(12 + i * 7, 137, 4, 4, if (i == game.introPage) GOLD else PLUM_HI)
            if (done && (t * 2).toInt() % 2 == 0) say(Txt.tapContinue.toString(), 244f, 139f, 4f, CREAM, Paint.Align.RIGHT)
        }
    }

    /** The ending: the kill typed into a terminal; a tap skips ahead to the final card. */
    fun ending(game: Game, l: Layout) {
        rect(0, 0, l.lw, l.lh, TERM_BG)
        px.at(l.sx, l.sy) {
            terminal(game.endAge, game.time, Intro.ending, "root@hell: ~", Intro.END_LINE_T)
            if (game.endAge >= Intro.endDuration - 0.4f && (game.time * 2).toInt() % 2 == 0) say(Txt.tapContinue.toString(), 244f, 139f, 4f, CREAM, Paint.Align.RIGHT)
        }
    }

    private fun terminal(age: Float, t: Float, lines: List<Intro.Line> = Intro.boot, title: String = "root@hell: /var/log/boot.log", lineT: Float = Intro.LINE_T) {
        val shown = min(lines.size, (age / lineT).toInt() + 1)
        val total = shown + if (age >= lines.size * lineT) 1 else 0
        val rows = 12
        val first = max(0, total - rows)
        say(title, 8f, 10f, 4f, TERM_DIM, shadow = 0)
        rect(4, 17, 186, 1, TERM_DIM2)
        val tagW = textWidth("[WARN]", 5f) + 5f
        for (row in first until total) {
            val y = 25f + (row - first) * 8.6f
            if (row < lines.size) {
                val ln = lines[row]
                val c = when (ln.tag) {
                    Intro.Tag.OK -> TERM_OK; Intro.Tag.FAIL -> TERM_FAIL; Intro.Tag.WARN -> TERM_WARN; else -> TERM_TEXT
                }
                if (ln.tag != Intro.Tag.NONE) say(ln.tag.label.trimEnd(), 8f, y, 5f, c, shadow = 0)
                say(ln.text.toString(), 8f + tagW, y, 5f, if (ln.tag == Intro.Tag.FAIL) 0xFFFFB0B8.toInt() else TERM_TEXT, shadow = 0)
            } else {
                say(prompt.trimEnd(), 8f, y, 5f, TERM_OK, shadow = 0)
                if ((t * 2.5f).toInt() % 2 == 0) rect(8f + textWidth(prompt.trimEnd(), 5f) + 4, y - 3, 4, 6, TERM_OK)
            }
        }
    }

    private fun daemon(page: Intro.Page, age: Float, t: Float) {
        ui.devilFrame(14f, 14f, 68f, if (age > 1.5f) Mood.LAUGH else page.mood, t, spriteScale = 2)
        val x = 98f
        val y = 14f
        box(x, y, 148f, 66f, CARD_CREAM, WHITE)
        rect(x + 3, y + 3, 142, 1, CARD_EDGE); rect(x + 3, y + 62, 142, 1, CARD_EDGE)
        say(dictHead.toString(), x + 8, y + 12, 8f, INK_TEXT, shadow = 0)
        rect(x + 8, y + 20, 132, 1, INK_TEXT)
        say(dict1.toString(), x + 8, y + 31, 5f, INK_TEXT, shadow = 0)
        if (age > 1.0f) say(dict2.toString(), x + 8, y + 42, 5f, DEVIL_RED_LO, shadow = 0)
        if (age > 1.5f) say(dictSee.toString(), x + 8, y + 55, 4f, LOCKED, shadow = 0)
        speech(page.text.toString(), age, 12f, 88f, 232f, 40f, tailUp = 48f)
    }

    private fun packet(page: Intro.Page, age: Float, t: Float) {
        val cy = 50
        val wallX = 140
        rect(8, cy + 8, 240, 3, 0xFF2A1A3A.toInt()); rect(8, cy + 8, 240, 1, 0xFF4A3A6A.toInt())
        for (yy in 20 until 64 step 2) {
            val hot = ((yy / 2 + (t * 9f).toInt()) % 2) == 0
            rect(wallX, yy, 4, 2, if (hot) RED_BTN_HI else DEVIL_RED_LO)
            if (hot) rect(wallX + 4, yy, 1, 2, RED_BTN)
        }
        say(firewall.toString(), wallX + 2f, 13f, 4f, RED_BTN_HI, Paint.Align.CENTER)
        val run = age * 90f
        for (i in 0 until 5) {
            val x = -10f + run - i * 16f
            if (x < wallX - 10) {
                if (x > -10) packetBox(x, cy.toFloat(), false)
            } else {
                // dropped at the wall: it tumbles off the cable
                val d = x - (wallX - 10)
                val y = cy + d * d * 0.05f
                if (y < 80) packetBox(wallX - 10 + d * 0.3f, y, true)
            }
        }
        // the last one strolls through the fire
        val bx = min(-10f + run - 5 * 16f, 196f)
        if (bx > -14) {
            val bob = if (bx < 196f && sin(t * 16f) > 0.2f) 1 else 0
            bevel(bx.roundToInt().toFloat(), cy - 7f - bob, 2, age)
            if (bx > wallX + 6) say("ACK", bx + 6, cy - 14f, 4f, MINT, Paint.Align.CENTER)
        }
        speech(page.text.toString(), age, 12f, 86f, 190f, 42f, tailRight = true)
        ui.devilFrame(208f, 88f, 36f, page.mood, t)
    }

    private fun packetBox(x: Float, y: Float, dropped: Boolean) {
        val xr = x.roundToInt().toFloat()
        val yr = y.roundToInt().toFloat()
        rect(xr - 1, yr, 10, 7, INK); rect(xr, yr - 1, 8, 9, INK)
        rect(xr, yr, 8, 7, if (dropped) DEVIL_RED_LO else PLUM_HI)
        rect(xr, yr, 8, 1, if (dropped) RED_BTN else STEEL_LO); rect(xr + 1, yr + 3, 6, 1, if (dropped) INK else PLUM)
    }

    private fun root(page: Intro.Page, age: Float, t: Float) {
        val x = 70
        val w = 116
        val cols = intArrayOf(GOLD, TEAL, 0xFF3FA05F.toInt(), RED_BTN)
        val his = intArrayOf(GOLD_HI, 0xFF7FD6D0.toInt(), 0xFF8FE0A0.toInt(), RED_BTN_HI)
        for (i in 0 until 4) {
            val y = 14 + i * 16
            rect(x + 2, y + 2, w, 12, SHADOW)
            rect(x - 1, y, w + 2, 12, INK); rect(x, y - 1, w, 14, INK)
            rect(x, y, w, 12, cols[i]); rect(x, y, w, 1, his[i]); rect(x, y + 11, w, 1, INK)
            say(layerNames[i].toString(), x + w / 2f, y + 6.3f, 5f, if (i == 3) CREAM else INK_TEXT, Paint.Align.CENTER, shadow = 0)
        }
        // dashed way down, with an arrow head
        for (yy in 16 until 66 step 4) rect(52, yy, 2, 2, GOLD_LO2)
        rect(49, 66, 8, 1, GOLD_LO2); rect(50, 67, 6, 1, GOLD_LO2); rect(51, 68, 4, 1, GOLD_LO2); rect(52, 69, 2, 1, GOLD_LO2)
        // Bevel hops down slab by slab
        val pos = min(3f, age * 1.4f)
        val hop = if (pos < 3f) sin((pos % 1f) * PI.toFloat()) * 5f else 0f
        bevel(196f, 14f + pos * 16f + 2f - hop, 1, age)
        if (pos >= 3f && (t * 3).toInt() % 2 == 0) rect(x - 4, 14 + 3 * 16 + 4, 2, 4, MINT)
        speech(page.text.toString(), age, 12f, 86f, 190f, 42f, tailRight = true)
        ui.devilFrame(208f, 88f, 36f, page.mood, t)
    }

    /** Bevel at logical scale [s]: the little mint cube with white eyes. */
    private fun bevel(x: Float, y: Float, s: Int, age: Float) {
        rect(x + 2 * s, y + 2 * s, 6 * s, 7 * s, SHADOW)
        rect(x - 1, y, 6 * s + 2, 7 * s, HERO_OUT); rect(x, y - 1, 6 * s, 7 * s + 2, HERO_OUT)
        rect(x, y, 6 * s, 7 * s, MINT); rect(x, y, 6 * s, s, MINT_HI); rect(x, y, s, 7 * s, MINT_HI)
        rect(x, y + 6 * s, 6 * s, s, MINT_LO); rect(x + 5 * s, y + s, s, 6 * s, MINT_LO)
        if ((age + 0.4f) % 2.9f < 0.12f) { rect(x + s, y + 3 * s, 2 * s, s, HERO_OUT); rect(x + 3 * s, y + 3 * s, 2 * s, s, HERO_OUT) }
        else {
            rect(x + s, y + 2 * s, 2 * s, 2 * s, WHITE); rect(x + 3 * s, y + 2 * s, 2 * s, 2 * s, WHITE)
            rect(x + 2 * s, y + 3 * s, s, s, DOOR_DARK); rect(x + 4 * s, y + 3 * s, s, s, DOOR_DARK)
        }
    }

    /** Cream speech box with typewriter text that shrinks to fit. */
    private fun speech(text: String, age: Float, x: Float, y: Float, w: Float, h: Float, tailUp: Float = -1f, tailRight: Boolean = false) {
        var size = 6f
        var lines: List<String>
        while (true) {
            lines = wrap(text, size, w - 12f)
            val fits = lines.maxOf { textWidth(it, size) } <= w - 12f && 8f + lines.size * size * 1.3f <= h
            if (fits || size <= 3.5f) break
            size -= 0.5f
        }
        box(x, y, w, h, CREAM, WHITE)
        if (tailUp >= 0f) { rect(tailUp - 1, y - 3, 3, 3, CREAM); rect(tailUp, y - 5, 2, 2, CREAM) }
        if (tailRight) { rect(x + w, y + 8, 3, 3, CREAM); rect(x + w + 3, y + 9, 2, 2, CREAM) }
        val lh = size * 1.3f
        val top = y + (h - lines.size * lh) / 2f + lh / 2f
        var left = (age * Intro.CPS).toInt()
        lines.forEachIndexed { i, ln ->
            if (left <= 0) return@forEachIndexed
            say(ln.take(left), x + 6, top + i * lh, size, INK_TEXT, shadow = 0)
            left -= ln.length + 1
        }
    }

    // ---------- world transition ----------

    fun worldIntro(game: Game, l: Layout) {
        val info = game.worldInfo
        val age = game.worldAge
        val t = game.time
        px.at(l.sx, l.sy) {
            // depth gauge: which layer we are on
            for (i in 0 until 3) {
                val on = i + 1 == info.number
                val y = 12 + i * 9
                rect(9, y + 1, 14, 6, SHADOW); rect(7, y - 1, 16, 8, INK)
                rect(8, y, 14, 6, if (on) GOLD else ROCK); rect(8, y, 14, 1, if (on) GOLD_HI else ROCK_HI)
            }
            say(Txt.worldNo.toString().replace("%d", info.number.toString()), 128f, 17f, 8f, GOLD, Paint.Align.CENTER, GOLD_LO)
            val name = info.name.toString().uppercase()
            var size = 13f
            while (size > 6f && textWidth(name, size) > 220f) size -= 0.5f
            say(name, 128f, 36f, size, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            val layer = info.layer.toString()
            val lw = textWidth(layer, 6f)
            say(layer, 128f, 53f, 6f, MINT, Paint.Align.CENTER, MINT_LO)
            rect(128f - lw / 2, 59f, lw.roundToInt(), 1, MINT_LO)
            ui.devilFrame(14f, 74f, 46f, if ((t % 4f) < 1.0f && age > 1.2f) Mood.LAUGH else Mood.GRIN, t)
            speech(info.line.toString(), max(0f, age - 0.3f), 68f, 78f, 178f, 38f)
            if (age > 0.5f && (t * 2).toInt() % 2 == 0) say(Txt.tapDescend.toString(), 128f, 135f, 4.5f, CREAM, Paint.Align.CENTER)
        }
    }

    private companion object {
        const val TERM_BG = 0xFF040A06.toInt()
        const val TERM_TEXT = 0xFFB4EBC4.toInt()
        const val TERM_DIM = 0xFF4E8A62.toInt()
        const val TERM_DIM2 = 0xFF1F4A2E.toInt()
        const val TERM_OK = 0xFF5CFF8A.toInt()
        const val TERM_FAIL = 0xFFFF4E5E.toInt()
        const val TERM_WARN = 0xFFFFB84A.toInt()
    }
}
