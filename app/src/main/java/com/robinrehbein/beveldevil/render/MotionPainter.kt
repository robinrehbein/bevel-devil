package com.robinrehbein.beveldevil.render

import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Hit
import com.robinrehbein.beveldevil.game.Screen
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * HUD for levels that read the phone's motion, at the bottom of the playfield: a spirit level whose bead
 * shows the tilt, and a shaking-phone icon. Without the sensor they come with tilt and shake buttons.
 */
class MotionPainter(px: Pixels) : Painter(px) {
    fun hud(game: Game, l: Layout) {
        val w = game.world ?: return
        if (game.screen != Screen.PLAY || !w.level.usesMotion) return
        val buttons = l.controls.motionButtons
        val t = game.time
        if (w.level.usesTilt) {
            vial(l.tiltVial, w.tilt)
            if (!buttons) rockingPhone(l.tiltL, t)
            else {
                slopeButton(l.tiltL, -1, l.controls.tiltLatch == -1)
                slopeButton(l.tiltR, 1, l.controls.tiltLatch == 1)
            }
        }
        if (w.level.usesShake) {
            val since = w.time - w.shakeTime
            shakeIcon(l.shakeHit(w.level.usesTilt), buttons, since in 0f..0.3f, t)
        }
    }

    /** Glass tube with a bead that rolls to the lower side, the way platforms and Bevel slide. */
    private fun vial(h: Hit, tilt: Float) {
        box(h, PLUM, PLUM_HI)
        val x = h.x + 3
        val y = h.y + 3
        val tw = h.w - 6
        rect(x - 1, y - 1, tw + 2, 7, INK)
        rect(x, y, tw, 5, TEAL)
        rect(x, y, tw, 1, 0xFF6FD6CC.toInt())
        rect(x, y + 4, tw, 1, 0xFF1F6E6A.toInt())
        val mid = x + tw / 2
        rect(mid - 4, y - 1, 1, 7, GOLD_LO2); rect(mid + 4, y - 1, 1, 7, GOLD_LO2)
        val bx = (mid - 2 + tilt * (tw / 2 - 4)).roundToInt()
        rect(bx, y + 1, 5, 3, MINT_HI)
        rect(bx + 1, y + 1, 2, 1, WHITE)
    }

    /** With the sensor: a landscape phone that rocks from side to side, saying "tilt me". */
    private fun rockingPhone(h: Hit, t: Float) {
        val rock = (sin(t * 3f) * 1.6f).roundToInt()
        val x = h.x + (h.w - 11) / 2
        val y = h.y + 3
        // two columns, each lifted by the rock, so the phone leans
        for (c in 0 until 11) {
            val dy = ((c - 5) * rock / 5f).roundToInt()
            val edge = c == 0 || c == 10
            rect(x + c, y - 1 + dy, 1, 7, INK)
            rect(x + c, y + dy, 1, 5, CREAM)
            if (!edge && c != 9) rect(x + c, y + 1 + dy, 1, 3, PLUM_HI)
        }
    }

    /** Button with a slope and a ball rolling down it: [dir] -1 lowers the left edge. */
    private fun slopeButton(h: Hit, dir: Int, on: Boolean) {
        box(h, if (on) RED_BTN else PLUM, if (on) RED_BTN_HI else PLUM_HI)
        val cx = h.x + h.w / 2
        val cy = h.y + h.h / 2
        for (i in -4..4) rect(cx + i, cy + 1 + (i * dir * 0.5f).roundToInt(), 1, 2, if (on) WHITE else CREAM)
        // the ball has rolled to the low end
        val bx = cx + dir * 3
        rect(bx - 1, cy - 1 + (3 * dir * dir * 0.5f).roundToInt() - 1, 3, 3, if (on) GOLD_HI else MINT)
    }

    /** A phone with shake marks; lights up for a moment after a shake. */
    private fun shakeIcon(h: Hit, button: Boolean, flash: Boolean, t: Float) {
        if (button || flash) box(h, if (flash) RED_BTN else PLUM, if (flash) RED_BTN_HI else PLUM_HI)
        val jig = if (flash) (sin(t * 70f) * 1.2f).roundToInt() else 0
        val x = h.x + (h.w - PHONE[0].length) / 2 + jig
        val y = h.y + (h.h - PHONE.size) / 2
        PHONE.forEachIndexed { r, row ->
            row.forEachIndexed { c, ch ->
                val color = when (ch) { '#' -> CREAM; 'o' -> INK; '~' -> if (flash) GOLD_HI else BONE_LO; else -> 0 }
                if (color != 0) rect(x + c, y + r, 1, 1, color)
            }
        }
    }

    private companion object {
        val PHONE = arrayOf(
            "....#####....",
            "..~.#ooo#.~..",
            ".~..#ooo#..~.",
            ".~..#ooo#..~.",
            "..~.#ooo#.~..",
            "....#ooo#....",
            "....#####....",
        )
    }
}
