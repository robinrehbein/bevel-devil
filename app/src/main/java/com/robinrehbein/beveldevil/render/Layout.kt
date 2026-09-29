package com.robinrehbein.beveldevil.render

import com.robinrehbein.beveldevil.game.Hit
import com.robinrehbein.beveldevil.game.Screen
import com.robinrehbein.beveldevil.game.Ui
import kotlin.math.max
import kotlin.math.min

/** On-screen touch buttons in screen pixels (placed by [Layout]), plus which ones are held. */
class ControlLayout {
    var r = 0f
    var leftX = 0f
    var rightX = 0f
    var jumpX = 0f
    var y = 0f
    var left = false
    var right = false
    var jump = false
    /** Settings, set by the view before [Layout.update]. */
    var stick = false
    var mirror = false
    var sizeScale = 1f
    var sc = 1
    /** Floating stick being touched (screen px). */
    var stickActive = false
    var stickX = 0f
    var stickY = 0f
    var knobX = 0f
    var stickR = 0f
}

/** Where the HUD goes: over the playfield, in the side columns (wide phones) or in a band above it (tablets). */
enum class HudMode { OVERLAY, SIDE, TOP }

/**
 * Maps the screen onto a logical pixel canvas of [lw]×[lh] (always ≥ 256×144) shown at integer scale [sc],
 * and places the 256×144 playfield, the 256×144 menu stage ([Ui] coordinates), HUD and touch buttons in it.
 * All positions are logical canvas pixels unless noted. Shared by touch input and rendering.
 */
class Layout {
    var w = 0; private set
    var h = 0; private set
    var sc = 1; private set
    var lw = Ui.W; private set
    var lh = Ui.H; private set
    /** Playfield (level) origin. */
    var fx = 0; private set
    var fy = 0; private set
    /** Menu stage origin. */
    var sx = 0; private set
    var sy = 0; private set
    var hud = HudMode.OVERLAY; private set
    var pause = Ui.hudPause; private set
    /** OVERLAY/TOP: top-left of the pill row. SIDE: the column the pills stack in. */
    var pills = Hit(24, 3, 0, 12); private set
    var frame = Ui.devilFrame; private set
    /** OVERLAY/TOP: bubble's right edge (with tail), top and max width. SIDE: the box the bubble must fit in. */
    var bubble = Hit(0, 18, 118, 0); private set
    val controls = ControlLayout()

    /** [w]×[h] surface in screen px, [density] px per dp, cutout insets in screen px. */
    fun update(w: Int, h: Int, density: Float, cutL: Int = 0, cutT: Int = 0, cutR: Int = 0, cutB: Int = 0) {
        this.w = w; this.h = h
        sc = max(1, min(w / Ui.W, h / Ui.H))
        lw = max(Ui.W, ceil(w, sc)); lh = max(Ui.H, ceil(h, sc))
        // fully visible part; the last partial logical pixel may be cropped
        val vw = max(Ui.W, w / sc); val vh = max(Ui.H, h / sc)
        val il = ceil(cutL, sc); val it = ceil(cutT, sc); val ir = ceil(cutR, sc); val ib = ceil(cutB, sc)
        sx = center(il, ir, Ui.W, vw); sy = center(it, ib, Ui.H, vh)
        fx = sx; fy = sy
        val left = fx - il
        val right = vw - ir - fx - Ui.W
        val band = min(vh - it - ib - Ui.H, 48)
        hud = when {
            left >= 28 && right >= 32 -> HudMode.SIDE
            band >= 40 -> HudMode.TOP
            else -> HudMode.OVERLAY
        }
        when (hud) {
            HudMode.OVERLAY -> {
                pause = Hit(fx + Ui.hudPause.x, fy + Ui.hudPause.y, Ui.hudPause.w, Ui.hudPause.h)
                pills = Hit(fx + 24, fy + 3, 0, 12)
                frame = Hit(fx + Ui.devilFrame.x, fy + Ui.devilFrame.y, Ui.devilFrame.w, Ui.devilFrame.h)
                bubble = Hit(frame.x - 4, fy + 18, 118, 0)
            }
            HudMode.TOP -> {
                fy = it + band
                pause = Hit(fx + 4, it + 4, 14, 12)
                pills = Hit(fx + 24, it + 4, 0, 12)
                val fs = min(38, band - 6)
                frame = Hit(fx + Ui.W - 4 - fs, it + (band - fs) / 2, fs, fs)
                bubble = Hit(frame.x - 4, it + 20, 118, 0)
            }
            HudMode.SIDE -> {
                pause = Hit(il + (left - 14) / 2, fy + 4, 14, 12)
                pills = Hit(il + 2, fy + 22, left - 4, 0)
                val fs = (right - 2).coerceIn(30, 38)
                frame = Hit(min(fx + Ui.W + (right - fs) / 2, vw - ir - fs - 1), fy + 4, fs, fs)
            }
        }
        placeControls(density, cutL, cutR, cutB)
        if (hud == HudMode.SIDE) {
            val top = frame.y + frame.h + 6
            val bottom = ((controls.y - controls.r) / sc).toInt() - 6
            bubble = Hit(fx + Ui.W + 2, top, vw - ir - 2 - (fx + Ui.W + 2), bottom - top)
        }
    }

    private fun placeControls(dp: Float, cutL: Int, cutR: Int, cutB: Int) {
        val c = controls
        val margin = 18f * dp
        val pad = 3f * sc
        var r = min(34f * dp * c.sizeScale, h * 0.14f)
        val pair = 2.3f
        val mv: Float // x of the left arrow
        val jx: Float
        if (hud == HudMode.SIDE) {
            val colL0 = cutL.toFloat(); val colL1 = fx * sc.toFloat()
            val colR0 = (fx + Ui.W) * sc.toFloat(); val colR1 = (w - cutR).toFloat()
            val (m0, m1) = if (c.mirror) colR0 to colR1 else colL0 to colL1
            val (j0, j1) = if (c.mirror) colL0 to colL1 else colR0 to colR1
            r = max(min(r, min((m1 - m0 - 2 * pad) / 4.3f, (j1 - j0 - 2 * pad) / 2f)), 26f * dp)
            // too narrow a column: the buttons may reach into the playfield, but stay on screen
            mv = if (c.mirror) min((m0 + m1) / 2 + 1.15f * r, m1 - r - pad) - pair * r else max((m0 + m1) / 2 - 1.15f * r, m0 + r + pad)
            jx = if (c.mirror) max((j0 + j1) / 2, j0 + r + pad) else min((j0 + j1) / 2, j1 - r - pad)
        } else {
            r = max(r, 26f * dp)
            mv = if (c.mirror) w - cutR - margin - r - pair * r else cutL + margin + r
            jx = if (c.mirror) cutL + margin + r else w - cutR - margin - r
        }
        c.r = r
        c.leftX = mv
        c.rightX = mv + pair * r
        c.jumpX = jx
        c.y = h - cutB - margin - r
        c.sc = sc
        c.stickR = 44f * dp
    }

    /** Menus shown over a level (pause, clear, end) sit on the playfield, the rest on the centered stage. */
    fun overWorld(s: Screen) = s == Screen.PAUSE || s == Screen.CLEAR || s == Screen.END
    fun stageX(s: Screen) = if (overWorld(s)) fx else sx
    fun stageY(s: Screen) = if (overWorld(s)) fy else sy

    /** Screen px → logical canvas px. */
    fun lx(x: Float) = x / sc
    fun ly(y: Float) = y / sc

    private fun ceil(a: Int, b: Int) = (a + b - 1) / b
    private fun center(lo: Int, hi: Int, size: Int, total: Int) = (lo + (total - lo - hi - size) / 2).coerceIn(0, total - size)
}
