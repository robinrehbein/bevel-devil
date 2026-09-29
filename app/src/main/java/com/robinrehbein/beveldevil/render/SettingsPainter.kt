package com.robinrehbein.beveldevil.render

import android.graphics.Paint
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Screen
import com.robinrehbein.beveldevil.game.T
import com.robinrehbein.beveldevil.game.Txt
import com.robinrehbein.beveldevil.game.Ui

/** Settings screen plus the gear buttons that lead to it (title, pause). */
class SettingsPainter(px: Pixels) : Painter(px) {
    private val gear = arrayOf(
        "...###...", ".#.###.#.", "..#####..", "###...###", "###...###", "###...###", "..#####..", ".#.###.#.", "...###...",
    )

    private fun gearIcon(x: Float, y: Float) {
        gear.forEachIndexed { r, row -> row.forEachIndexed { c, ch -> if (ch == '#') rect(x + c, y + r, 1, 1, CREAM) } }
    }

    /** Extra buttons on top of the existing title and pause screens. */
    fun extras(game: Game, l: Layout) {
        val s = game.screen
        if (s != Screen.TITLE && s != Screen.PAUSE) return
        px.at(l.stageX(s), l.stageY(s)) {
            if (s == Screen.TITLE) {
                box(Ui.gear, PLUM, PLUM_HI)
                gearIcon(Ui.gear.x + (Ui.gear.w - 9) / 2f, Ui.gear.y + 1.5f)
            } else button(Ui.pauseSettings, Txt.settings.toString(), false)
        }
    }

    fun screen(game: Game, l: Layout) {
        val y = l.sy + 136
        val x0 = l.sx % TS - TS
        for (x in x0 until l.lw step TS) tile(lc, x.toFloat(), y.toFloat())
        for (ry in y + TS until l.lh step TS) for (x in x0 until l.lw step TS) px.rockTile(lc, x.toFloat(), ry.toFloat(), (x * 31 + ry * 17) and 0xFFFF)
        rect(0, 0, l.lw, l.lh, 0x70100818)
        px.at(l.sx, l.sy) {
            button(Ui.back, "<", false)
            say(Txt.settings.toString(), 128f, 14f, 7f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            val labels = listOf(Txt.controls, Txt.btnSize, Txt.haptics, Txt.sound, Txt.leftHanded)
            val opts = listOf<List<T>>(
                listOf(Txt.schemeButtons, Txt.schemeStick), listOf(T("S", "S"), T("M", "M"), T("L", "L")),
                listOf(Txt.on, Txt.off), listOf(Txt.on, Txt.off), listOf(Txt.on, Txt.off),
            )
            for (row in 0 until Ui.SET_ROWS) {
                say(labels[row].toString(), 14f, 34.5f + row * 22, 5f, CREAM)
                for (i in opts[row].indices) {
                    val h = Ui.setOpt(row, i, opts[row].size)
                    val on = game.optionOf(row) == i
                    box(h, if (on) RED_BTN else PLUM, if (on) RED_BTN_HI else PLUM_HI)
                    say(opts[row][i].toString(), h.x + h.w / 2f, h.y + 8.5f, 6f, if (on) CREAM else 0xFFB9A6CF.toInt(), Paint.Align.CENTER)
                }
            }
        }
    }
}
