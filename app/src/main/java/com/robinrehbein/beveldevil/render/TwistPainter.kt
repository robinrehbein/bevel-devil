package com.robinrehbein.beveldevil.render

import android.graphics.Paint
import com.robinrehbein.beveldevil.game.CreditLine
import com.robinrehbein.beveldevil.game.FakeEnd
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Twists
import com.robinrehbein.beveldevil.game.World
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Meta twists: the cracking frame, the ghost of the last attempt, credit lines on their platforms (all in
 * playfield pixels), the upside-down / rolling picture, and the fake clear screen or credits roll.
 */
class TwistPainter(px: Pixels, private val ui: UiPainter) : Painter(px) {
    private var src = IntArray(0)
    private var out = IntArray(0)

    /** Everything that lives in the level, drawn over it (playfield coordinates). */
    fun inWorld(game: Game, l: Layout) {
        val w = game.world ?: return
        // level pixels, shifted like the level itself (see [World.camX])
        val cam = (w.camX * TS).roundToInt()
        px.at(l.fx - cam, l.fy) {
            lc.save()
            lc.clipRect(cam, 0, cam + PW, PH)
            cracks(w)
            ghost(w)
            if (w.viewTurn() == 0f && w.viewRoll() == 0f) creditPlatforms(w, game.deaths)
            lc.restore()
        }
    }

    // ---------- frame crack ----------

    private fun cracks(w: World) {
        for (c in w.cracks) {
            if (c.fell) continue
            val f = ((w.time - c.time) / c.warn).coerceIn(0f, 1f)
            val g = c.group
            val x0 = (g.pieces.minOf { it.hx } * TS).toInt()
            val x1 = ((g.pieces.maxOf { it.hx } + 1) * TS).toInt()
            val y0 = (g.pieces.minOf { it.hy } * TS).toInt()
            val y1 = ((g.pieces.maxOf { it.hy } + 1) * TS).toInt()
            val hot = sin(w.time * 40f) > 0f
            // the break runs down both ends first, then zigzags along the middle from the center out
            val glow = if (hot) CRACK_HOT else CRACK_WARM
            val down = ((y1 - y0) * min(1f, f * 2f)).toInt()
            for (yy in 0 until down) {
                val y = y0 + yy
                val jl = Masonry.hash(x0, y) and 1
                val jr = Masonry.hash(x1, y) and 1
                // a dark gap with hell glowing through it
                rect(x0 - 1 + jl, y, 2, 1, INK); rect(x0 + 1 + jl, y, 1, 1, glow)
                rect(x1 - 1 - jr, y, 2, 1, INK); rect(x1 - 2 - jr, y, 1, 1, glow)
            }
            val mid = (x0 + x1) / 2
            val reach = ((x1 - x0) / 2f * ((f - 0.25f) / 0.6f).coerceIn(0f, 1f)).toInt()
            for (x in mid - reach until mid + reach) {
                val y = (y0 + y1) / 2 + (Masonry.hash(x shr 1, y0) ushr 3) % 3 - 1
                rect(x, y - 1, 1, 1, INK)
                rect(x, y, 1, 1, if ((x + (w.time * 12f).toInt()) % 3 == 0) GOLD_SPARK else glow)
                rect(x, y + 1, 1, 1, INK)
                // short branches off the seam
                if (Masonry.hash(x, 5) and 7 == 0) for (k in 1..2) rect(x + k * (if (x < mid) -1 else 1), y + 1 + k, 1, 1, INK)
            }
            // the loose piece sags a pixel at the end: a dark line under it and a lit edge on top
            if (f > 0.7f) {
                rect(x0, y1, x1 - x0, 1, INK)
                if (hot) rect(x0 + 2, y1 - 1, x1 - x0 - 4, 1, CRACK_WARM)
            }
            // grit trickles out of the break
            for (i in 0 until 3 + (f * 7).toInt()) {
                val h = Masonry.hash(i, 77)
                val x = x0 + 1 + Math.floorMod(h, maxOf(1, x1 - x0 - 2))
                val y = y1 + ((w.time - c.time) * 40f + (h ushr 8 and 31)) % 22f
                rect(x.toFloat(), y, 1f, 1f, if (i % 3 == 0) GOLD_HI else GOLD_LO2)
            }
        }
    }

    // ---------- ghost ----------

    /** The last attempt as a translucent, scanlined, RGB-split Bevel with its undo tag. */
    private fun ghost(w: World) {
        val g = w.ghost ?: return
        val t = w.time
        val x = (g.cx * TS - 3).roundToInt()
        val y = (g.b * TS - 7).roundToInt()
        val s = (t * 18f).toInt()
        val jit = if (s % 5 == 0) 2 else 0
        // echoes a moment behind, fading
        val back = if (g.cx > w.player.box.cx) 1 else -1
        rect(x + back * 5, y + 1, 6, 5, GHOST_ECHO)
        rect(x + back * 9, y + 2, 6, 3, GHOST_ECHO)
        // colour fringes drifting apart
        rect(x - 2, y, 6, 7, GHOST_RED)
        rect(x + 2, y + (s and 1), 6, 7, GHOST_BLUE)
        for (r in -1..7) {
            val row = y + r
            val shift = if (r == 2 || r == 3) jit * (if (s % 2 == 0) 1 else -1) else 0
            if (r == -1 || r == 7) { rect(x + shift, row, 6, 1, GHOST_EDGE); continue }
            val c = if ((row + (t * 30f).toInt()) % 2 == 0) GHOST else GHOST_DIM
            rect(x - 1 + shift, row, 8, 1, GHOST_EDGE)
            rect(x + shift, row, 6, 1, c)
        }
        // hollow eyes
        rect(x + 1, y + 2, 2, 2, GHOST_EYE); rect(x + 4, y + 2, 2, 2, GHOST_EYE)
        rect(x + 1, y + 3, 1, 1, GHOST_EDGE); rect(x + 4, y + 3, 1, 1, GHOST_EDGE)
        if (s % 9 != 0) say("CTRL+Z", x + 3f, y - 5f, 3.5f, GHOST_TAG, Paint.Align.CENTER, GHOST_EDGE)
    }

    // ---------- credits as platforms ----------

    private fun creditPlatforms(w: World, deaths: Int) {
        val id = w.creditPlatforms ?: return
        val g = w.group(id)
        if (!g.visible) return
        for (line in w.credits) {
            val stop = line.stop ?: continue
            val row = (stop - 0.5f).toInt()
            val n = g.pieces.count { it.hy.toInt() == row && !it.spike }
            val s = text(line, deaths)
            var size = 5f
            while (size > 3f && textWidth(s, size) > n * TS - 3) size -= 0.5f
            say(s, (line.x + g.ox) * TS, (stop + g.oy) * TS + 0.5f, size, INK_TEXT, Paint.Align.CENTER, GOLD_HI)
        }
    }

    private fun text(line: CreditLine, deaths: Int) =
        line.text.toString().replace("%d", deaths.toString())

    // ---------- the picture itself ----------

    /** Turns the playfield upside down (a vertical card flip) or lets it roll like a CRT losing hold. */
    fun view(game: Game, l: Layout) {
        val w = game.world ?: return
        val turn = w.viewTurn()
        val roll = w.viewRoll()
        if (turn == 0f && roll == 0f) return
        val n = PW * PH
        if (src.size != n) { src = IntArray(n); out = IntArray(n) }
        px.lo.getPixels(src, 0, PW, l.fx, l.fy, PW, PH)
        if (turn > 0f) {
            val s = cos(PI.toFloat() * turn)
            val mirror = s < 0f
            for (y in 0 until PH) {
                val sy = if (abs(s) < 0.03f) -1 else (PH / 2f + (y + 0.5f - PH / 2f) / s).toInt()
                val b = y * PW
                if (sy !in 0 until PH) {
                    for (x in 0 until PW) out[b + x] = if (bayer(x, y) < 0.5f) NIGHT else ROCK_LO
                    continue
                }
                val sb = sy * PW
                // the thinner the picture, the darker, like a card seen edge-on
                val dim = abs(s) < 0.4f && y % 2 == 0
                for (x in 0 until PW) {
                    val c = src[sb + if (mirror) PW - 1 - x else x]
                    out[b + x] = if (dim) (c ushr 1 and 0x7F7F7F) or (0xFF shl 24) else c
                }
            }
            System.arraycopy(out, 0, src, 0, n)
        }
        if (roll > 0f) {
            val off = (roll * PH).toInt()
            for (y in 0 until PH) {
                val sy = (y + off) % PH
                val b = y * PW
                val seam = PH - 1 - off
                val d = y - seam
                if (d in -1..2) {
                    // the black blanking bar between two frames
                    for (x in 0 until PW) out[b + x] = if (d == -1 && bayer(x, y) < 0.3f) 0xFF3A2A4A.toInt() else NIGHT
                } else {
                    System.arraycopy(src, sy * PW, out, b, PW)
                }
            }
        } else {
            System.arraycopy(src, 0, out, 0, n)
        }
        px.lo.setPixels(out, 0, PW, l.fx, l.fy, PW, PH)
    }

    // ---------- the fake ending ----------

    /** Draws the fake clear screen or credits roll; true while one is showing (the HUD stays hidden). */
    fun fake(game: Game, l: Layout): Boolean {
        val w = game.world ?: return false
        if (!game.fakeShown) return false
        val f = w.fake ?: return false
        if (f.end == FakeEnd.CLEAR) {
            ui.clear(game, l)
            return true
        }
        val age = w.fakeAge
        val len = Twists.creditsLength(w.credits, w.rows)
        // the dark comes in, then lifts as the last lines settle onto the level
        val fadeIn = min(1f, age / 0.4f)
        val fadeOut = ((len - age) / Twists.CREDIT_HOLD).coerceIn(0f, 1f)
        val a = min(fadeIn, fadeOut)
        px.at(l.fx, l.fy) {
            for (y in 0 until PH step 2) rect(0, y, PW, 2, ((a * 0xE8).toInt() shl 24) or (NIGHT and 0xFFFFFF))
            for (line in w.credits) {
                val cy = Twists.creditY(line, age, w.rows) * TS
                // text can't be clipped, so lines appear and vanish at the frame's inner edge
                if (cy < TS + 4f || cy > PH - TS - 4f) continue
                val s = text(line, game.deaths)
                val x = line.x * TS - w.camX * TS
                if (line.big) say(s, x, cy, 9f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
                else say(s, x, cy, 6f, if (line.stop != null) GOLD_MID else CREAM, Paint.Align.CENTER)
            }
        }
        return true
    }

    private companion object {
        const val GHOST = 0xC0D8C8FF.toInt()
        const val GHOST_DIM = 0x809078E0.toInt()
        const val GHOST_ECHO = 0x309070E0
        const val GHOST_TAG = 0xFFD8C8FF.toInt()
        const val GHOST_EDGE = 0x90201848.toInt()
        const val GHOST_EYE = 0xE0F0F4FF.toInt()
        const val GHOST_RED = 0x70FF3070
        const val GHOST_BLUE = 0x7030D0FF
        const val CRACK_WARM = 0xFFC4462C.toInt()
        const val CRACK_HOT = 0xFFFF9A4A.toInt()
    }
}
