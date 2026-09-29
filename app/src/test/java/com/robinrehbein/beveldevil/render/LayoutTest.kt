package com.robinrehbein.beveldevil.render

import com.robinrehbein.beveldevil.game.Ui
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutTest {
    private fun check(w: Int, h: Int, mode: HudMode, cutL: Int = 0) {
        val l = Layout().apply { update(w, h, 2.75f, cutL = cutL) }
        assertTrue("covers $w×$h", l.lw * l.sc >= w && l.lh * l.sc >= h)
        assertTrue("crops < 1 logical px", l.lw * l.sc - w < l.sc && l.lh * l.sc - h < l.sc)
        assertTrue(l.fx >= 0 && l.fy >= 0 && l.fx + Ui.W <= l.lw && l.fy + Ui.H <= l.lh)
        assertTrue(l.sx >= 0 && l.sy >= 0 && l.sx + Ui.W <= l.lw && l.sy + Ui.H <= l.lh)
        assertTrue("frame on screen", l.frame.x + l.frame.w < w / l.sc)
        assertEquals("$w×$h", mode, l.hud)
    }

    @Test fun sizes() {
        check(2400, 1080, HudMode.SIDE)
        check(2340, 1080, HudMode.SIDE)
        check(2400, 1080, HudMode.SIDE, cutL = 110)
        check(1920, 1080, HudMode.OVERLAY)
        check(2560, 1600, HudMode.OVERLAY)
        check(2048, 1536, HudMode.TOP)
        check(1600, 720, HudMode.SIDE)
        check(256, 144, HudMode.OVERLAY)
    }
}
