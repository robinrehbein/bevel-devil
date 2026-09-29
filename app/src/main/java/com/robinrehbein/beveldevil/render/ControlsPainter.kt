package com.robinrehbein.beveldevil.render

import android.graphics.Canvas
import android.graphics.Paint
import com.robinrehbein.beveldevil.game.Dir

/** On-screen touch buttons, drawn in screen pixels at the positions [Layout] gives them. */
class ControlsPainter {
    private val fill = Paint()

    fun draw(canvas: Canvas, c: ControlLayout) {
        btn(canvas, c, c.leftX, c.y, Dir.LEFT, c.left)
        btn(canvas, c, c.rightX, c.y, Dir.RIGHT, c.right)
        btn(canvas, c, c.jumpX, c.y, Dir.UP, c.jump)
    }

    private fun btn(canvas: Canvas, c: ControlLayout, x: Float, y: Float, dir: Dir, pressed: Boolean) {
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
}
