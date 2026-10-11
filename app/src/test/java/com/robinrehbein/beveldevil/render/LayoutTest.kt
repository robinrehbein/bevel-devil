package com.robinrehbein.beveldevil.render

import com.robinrehbein.beveldevil.game.Action
import com.robinrehbein.beveldevil.game.CardSlot
import com.robinrehbein.beveldevil.game.Levels
import com.robinrehbein.beveldevil.game.ROOM_COLS
import com.robinrehbein.beveldevil.game.RoomDemos
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

    /** Is any touch button (with its outline) over tile ([col], [row]) of the playfield? A door is [tall] tiles tall, standing on the tile. */
    private fun covered(l: Layout, col: Int, row: Int, tall: Float): Boolean {
        val c = l.controls
        val pad = 3f * l.sc
        val xs = if (c.stick) listOf(c.jumpX) else listOf(c.leftX, c.rightX, c.jumpX)
        val x0 = (l.fx + col * TS) * l.sc.toFloat(); val x1 = x0 + TS * l.sc
        val y1 = (l.fy + (row + 1) * TS) * l.sc.toFloat(); val y0 = y1 - tall * TS * l.sc
        return xs.any { x0 < it + c.r + pad && x1 > it - c.r - pad && y0 < c.y + c.r + pad && y1 > c.y - c.r - pad }
    }

    /** No level's spawn or exit door (nor a place a door moves to above the ground; a door that sinks into the ground is meant to be half hidden) lies under a touch button on common phone shapes. */
    @Test fun spawnAndDoorsAreNeverUnderTheControls() {
        val offenders = ArrayList<String>()
        for ((w, h) in listOf(1920 to 1080, 2400 to 1080, 2340 to 1080, 2560 to 1080)) for (mirror in listOf(false, true)) {
            val l = Layout()
            l.controls.mirror = mirror
            l.update(w, h, 2.75f)
            (Levels.all + RoomDemos.all).forEachIndexed { i, base -> for (level in base.rounds) {
                val spots = ArrayList<Triple<String, Int, Int>>()
                for (y in 0 until level.rows) for (x in 0 until level.cols) when (level.map.grid[y][x]) {
                    'P' -> spots += Triple("spawn", x, y)
                    'D' -> spots += Triple("door", x, y)
                }
                val actions = level.traps.flatMap { it.actions }
                actions.filterIsInstance<Action.DoorTo>().filter { it.row <= 14 }.forEach { spots += Triple("door target", it.col, it.row) }
                actions.filterIsInstance<Action.Extend>().mapNotNull { it.door }.filter { it.second <= 14 }.forEach { spots += Triple("door target", it.first, it.second) }
                // a level of several rooms is seen one room at a time: what counts is the column within its room
                for ((what, cx, cy) in spots) {
                    if (covered(l, Math.floorMod(cx, ROOM_COLS), cy, if (what == "spawn") 1f else 1.6f)) offenders += "level ${i + 1} $what ($cx,$cy) at ${w}x$h mirror=$mirror"
                }
            } }
        }
        assertTrue("under the controls (${offenders.size}):\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }

    /** The corners a card keeps to while the picture rolls (2-40) are never under a touch button, at any button size, on phone and tablet shapes. */
    @Test fun theRollCornersAreNeverUnderTheControls() {
        val corners = listOf(CardSlot.Slot(-1, CardSlot.HIGH), CardSlot.Slot(1, CardSlot.LOW))
        val offenders = ArrayList<String>()
        for ((w, h, dp) in listOf(Triple(1920, 1080, 2.75f), Triple(2400, 1080, 2.75f), Triple(2340, 1080, 2.75f), Triple(2560, 1080, 2.75f), Triple(2560, 1600, 2f), Triple(1280, 720, 2f), Triple(1280, 720, 1.5f), Triple(1600, 900, 2f))) {
            for (mirror in listOf(false, true)) for (size in listOf(0.8f, 1f, 1.25f)) {
                val l = Layout()
                l.controls.mirror = mirror
                l.controls.sizeScale = size
                l.update(w, h, dp)
                val c = l.controls
                val pad = 3f * l.sc
                for (s in corners) {
                    // the card's area in tiles of the playfield, in screen px
                    val a = CardSlot.area(s)
                    val x0 = (l.fx + a.x0 * TS) * l.sc; val x1 = (l.fx + a.x1 * TS) * l.sc
                    val y0 = (l.fy + a.y0 * TS) * l.sc; val y1 = (l.fy + a.y1 * TS) * l.sc
                    val hit = listOf(c.leftX, c.rightX, c.jumpX).any { x0 < it + c.r + pad && x1 > it - c.r - pad && y0 < c.y + c.r + pad && y1 > c.y - c.r - pad }
                    if (hit) offenders += "$s at ${w}x$h dp=$dp mirror=$mirror size=$size"
                }
            }
        }
        assertTrue("under the controls:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
