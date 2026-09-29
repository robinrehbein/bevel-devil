package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.World
import com.robinrehbein.beveldevil.game.WorldState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** The level: hell rock around the playfield, tiles, spikes, door, saws, the player and particles. */
class WorldPainter(px: Pixels) : Painter(px) {
    private val staticBmp = Bitmap.createBitmap(PW, PH, Bitmap.Config.ARGB_8888)
    private var staticLevel: Any? = null

    private var rockBmp: Bitmap? = null
    private var rockFor = IntArray(4)
    private val open = BooleanArray(2 * 32 + 2 * 18)
    private val rockOpen = BooleanArray(open.size)

    fun draw(game: Game, l: Layout) {
        val w = game.world ?: return
        surroundings(w, l)
        px.at(l.fx, l.fy) {
            lc.clipRect(0, 0, PW, PH)
            drawWorld(game, w)
        }
    }

    // ---------- surroundings ----------

    /** Rock everywhere outside the playfield, except where the level's edge is open: there the pit continues. */
    private fun surroundings(w: World, l: Layout) {
        if (l.lw == PW && l.lh == PH) return
        val cols = w.cols
        val rows = w.rows
        open.fill(true)
        for (p in w.pieces) {
            if (!p.visible) continue
            val cx = (p.box.x + 0.5f).toInt()
            val cy = (p.box.y + 0.5f).toInt()
            if (cx !in 0 until cols || cy !in 0 until rows) continue
            if (cy == 0) open[cx] = false
            if (cy == rows - 1) open[32 + cx] = false
            if (cx == 0) open[64 + cy] = false
            if (cx == cols - 1) open[82 + cy] = false
        }
        val key = intArrayOf(l.lw, l.lh, l.fx, l.fy)
        if (rockBmp == null || !key.contentEquals(rockFor) || !open.contentEquals(rockOpen)) {
            buildRock(l, cols, rows)
            rockFor = key
            open.copyInto(rockOpen)
        }
        lc.drawBitmap(rockBmp!!, 0f, 0f, null)
    }

    private fun isOpen(col: Int, row: Int, cols: Int, rows: Int): Boolean = when {
        row in 0 until rows && col < 0 -> open[64 + row]
        row in 0 until rows && col >= cols -> open[82 + row]
        col in 0 until cols && row < 0 -> open[col]
        col in 0 until cols && row >= rows -> open[32 + col]
        else -> false
    }

    private fun buildRock(l: Layout, cols: Int, rows: Int) {
        val bmp = rockBmp?.takeIf { it.width == l.lw && it.height == l.lh } ?: Bitmap.createBitmap(l.lw, l.lh, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.TRANSPARENT)
        val c = Canvas(bmp)
        val c0 = -((l.fx + TS - 1) / TS)
        val r0 = -((l.fy + TS - 1) / TS)
        val c1 = (l.lw - l.fx + TS - 1) / TS
        val r1 = (l.lh - l.fy + TS - 1) / TS
        for (row in r0 until r1) for (col in c0 until c1) {
            if (col in 0 until cols && row in 0 until rows) continue
            val x = (l.fx + col * TS).toFloat()
            val y = (l.fy + row * TS).toFloat()
            if (isOpen(col, row, cols, rows)) {
                // the pit keeps going: the swirl shows through and darkens with depth
                val d = when {
                    col < 0 -> -col; col >= cols -> col - cols + 1
                    row < 0 -> -row; else -> row - rows + 1
                }
                rect(c, x, y, 8f, 8f, Color.argb(minOf(230, d * 70 - 20), 7, 3, 13))
                continue
            }
            rockTile(c, x, y, col, row)
            if (isOpen(col, row + 1, cols, rows)) rect(c, x, y + 7, 8f, 1f, INK)
            if (isOpen(col, row - 1, cols, rows)) rect(c, x, y, 8f, 1f, INK)
            if (isOpen(col + 1, row, cols, rows)) rect(c, x + 7, y, 1f, 8f, INK)
            if (isOpen(col - 1, row, cols, rows)) rect(c, x, y, 1f, 8f, INK)
        }
        rockBmp = bmp
    }

    private fun rockTile(c: Canvas, x: Float, y: Float, col: Int, row: Int) {
        val h = (col * 73856093) xor (row * 19349663)
        px.rockTile(c, x, y, (h ushr 3) and 0xFFFF)
    }

    // ---------- level ----------

    private fun buildStatic(w: World) {
        staticBmp.eraseColor(Color.TRANSPARENT)
        val c = Canvas(staticBmp)
        val statics = w.pieces.filter { it.group == null }
        for (p in statics) if (!p.spike) rect(c, p.box.x * TS + 2, p.box.y * TS + 2, 8f, 8f, SHADOW)
        for (p in statics) if (!p.spike) tile(c, p.box.x * TS, p.box.y * TS)
        for (p in statics) if (p.spike) spike(c, p.box.x * TS, p.box.y * TS, p.dir)
        staticLevel = w.level
    }

    private fun drawWorld(game: Game, w: World) {
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
}
