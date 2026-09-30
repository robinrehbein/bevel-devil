package com.robinrehbein.beveldevil.render

import android.graphics.Canvas
import android.graphics.Paint
import com.robinrehbein.beveldevil.game.Dir
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** On-screen touch controls, drawn in screen pixels on a grid of logical pixels ([ControlLayout.sc]). */
class ControlsPainter {
    private val p = Paint()

    private fun rect(c: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, color: Int, alpha: Int = 255) {
        p.color = color; p.alpha = alpha
        c.drawRect(x0, y0, x1, y1, p)
    }

    fun draw(canvas: Canvas, c: ControlLayout) {
        if (c.stick) stick(canvas, c) else {
            btn(canvas, c, c.leftX, Dir.LEFT, c.left)
            btn(canvas, c, c.rightX, Dir.RIGHT, c.right)
        }
        btn(canvas, c, c.jumpX, Dir.UP, c.jump)
    }

    private fun btn(canvas: Canvas, c: ControlLayout, cx: Float, dir: Dir, pressed: Boolean) {
        val s = c.sc.toFloat()
        val r = (c.r / s).roundToInt() * s
        val x0 = (cx / s).roundToInt() * s - r
        val x1 = x0 + 2 * r
        val y0 = (c.y / s).roundToInt() * s - r + if (pressed) s else 0f
        val y1 = y0 + 2 * r
        val a = if (pressed) 240 else 135
        if (pressed) for (k in 3 downTo 1) rect(canvas, x0 - k * 2 * s, y0 - k * 2 * s, x1 + k * 2 * s, y1 + k * 2 * s, GOLD_HI, 16 + (4 - k) * 6)
        else rect(canvas, x0 + s, y0 + 2 * s, x1 + 2 * s, y1 + 2 * s, INK, 110)
        rect(canvas, x0 - s, y0 - s, x1 + s, y1 + s, INK, a)
        rect(canvas, x0, y0, x1, y1, GOLD, a)
        rect(canvas, x0, y0, x1, y0 + s, GOLD_HI, a); rect(canvas, x0, y0, x0 + s, y1, GOLD_HI, a)
        rect(canvas, x0, y1 - s, x1, y1, GOLD_LO, a); rect(canvas, x1 - s, y0, x1, y1, GOLD_LO, a)
        rect(canvas, x0 + 2 * s, y0 + 2 * s, x1 - 2 * s, y1 - 2 * s, INK, a)
        rect(canvas, x0 + 2 * s, y0 + 2 * s, x1 - 2 * s, y1 - 2 * s, if (pressed) PLUM_HI else PLUM, if (pressed) 245 else 150)
        rect(canvas, x0 + 2 * s, y0 + 2 * s, x1 - 2 * s, y0 + 3 * s, PLUM_HI, if (pressed) 0 else 200)
        arrow(canvas, (x0 + x1) / 2, (y0 + y1) / 2, r, dir, pressed)
    }

    /** 9×9 cell pixel arrow, apex first. */
    private fun arrow(canvas: Canvas, cx: Float, cy: Float, r: Float, dir: Dir, pressed: Boolean) {
        val cell = maxOf(1, (r * 1.3f / 9f).toInt()).toFloat()
        val o = cell * 4.5f
        val a = if (pressed) 255 else 215
        for (pass in 0..1) for (row in 0 until 9) for (col in 0 until 9) {
            if (!ARROW[row][col]) continue
            val (u, v) = when (dir) {
                Dir.UP -> col to row
                Dir.LEFT -> row to col
                Dir.RIGHT -> 8 - row to col
                Dir.DOWN -> col to 8 - row
            }
            val x = cx - o + u * cell + if (pass == 0) cell * 0.5f else 0f
            val y = cy - o + v * cell + if (pass == 0) cell * 0.5f else 0f
            if (pass == 0) rect(canvas, x, y, x + cell, y + cell, INK, 150)
            else rect(canvas, x, y, x + cell, y + cell, if (pressed) WHITE else CREAM, a)
        }
    }

    // ---------- floating stick ----------

    private fun disc(canvas: Canvas, cx: Float, cy: Float, rad: Float, s: Float, color: Int, alpha: Int) {
        val n = (rad / s).toInt()
        for (i in -n..n) {
            val hw = sqrt(maxOf(0f, rad * rad - (i * s) * (i * s)))
            val w = (hw / s).toInt() * s
            rect(canvas, cx - w, cy + i * s, cx + w, cy + (i + 1) * s, color, alpha)
        }
    }

    private fun stick(canvas: Canvas, c: ControlLayout) {
        val s = c.sc.toFloat()
        val active = c.stickActive
        val bx = if (active) c.stickX else (c.leftX + c.rightX) / 2
        val by = if (active) c.stickY else c.y
        val k = if (active) 1f else 0.55f
        val a = if (active) 150 else 55
        val rr = c.stickR
        val kr = c.r * 0.55f
        val h = kr * 1.25f
        // capsule track: ink outline, gold rim, plum fill
        fun cap(pad: Float, color: Int, alpha: Int) {
            rect(canvas, bx - rr - pad, by - h - pad, bx + rr + pad, by + h + pad, color, alpha)
            disc(canvas, bx - rr, by, h + pad, s, color, alpha); disc(canvas, bx + rr, by, h + pad, s, color, alpha)
        }
        if (active) cap(3 * s, GOLD_HI, 24)
        cap(2 * s, INK, (a * k + 40).toInt().coerceAtMost(200))
        cap(s, GOLD, (a * k + 60).toInt().coerceAtMost(220))
        cap(0f, if (active) PLUM else INK, if (active) 170 else 90)
        for (d in intArrayOf(-1, 1)) { // small direction chevrons
            val ax = bx + d * (rr + h * 0.35f)
            for (i in 0 until 4) rect(canvas, ax - d * i * s * 2 - s, by - (i + 1) * s, ax - d * i * s * 2, by + (i + 1) * s, CREAM, if (active) 200 else 90)
        }
        val kx = if (active) c.knobX else bx
        disc(canvas, kx + s, by + 2 * s, kr, s, INK, if (active) 120 else 50)
        disc(canvas, kx, by, kr + s, s, INK, if (active) 230 else 90)
        disc(canvas, kx, by, kr, s, GOLD, if (active) 255 else 110)
        disc(canvas, kx, by, kr - 2 * s, s, if (active) PLUM_HI else PLUM, if (active) 255 else 120)
        disc(canvas, kx - s, by - s, kr * 0.35f, s, GOLD_HI, if (active) 230 else 0)
    }

    companion object {
        private val ARROW = arrayOf(
            "....#....", "...###...", "..#####..", ".#######.", "#########",
            "...###...", "...###...", "...###...", "...###...",
        ).map { row -> BooleanArray(9) { row[it] == '#' } }
    }
}
