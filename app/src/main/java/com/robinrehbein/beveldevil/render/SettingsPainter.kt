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
                box(Ui.titleStory, PLUM, PLUM_HI)
                say(Txt.story.toString(), Ui.titleStory.x + Ui.titleStory.w / 2f, Ui.titleStory.y + 6.3f, 4f, CREAM, Paint.Align.CENTER)
                box(Ui.gear, PLUM, PLUM_HI)
                gearIcon(Ui.gear.x + (Ui.gear.w - 9) / 2f, Ui.gear.y + 1.5f)
                if (game.noAdsOffered) noAdsButton(Ui.noAdsTitle)
            } else {
                button(Ui.pauseSettings, Txt.settings.toString(), false)
                if (game.skipOffered) {
                    val ready = game.skipReady
                    box(Ui.pauseSkip, if (ready) RED_BTN else PLUM, if (ready) RED_BTN_HI else PLUM_HI)
                    val label = when {
                        !game.skipNeedsAd -> Txt.skipLevel
                        ready -> Txt.skipAd
                        else -> Txt.adLoading
                    }
                    say(label.toString(), Ui.pauseSkip.x + Ui.pauseSkip.w / 2f, Ui.pauseSkip.y + 6.8f, 5f, if (ready) CREAM else 0xFFB9A6CF.toInt(), Paint.Align.CENTER)
                }
            }
        }
    }

    private fun noAdsButton(h: com.robinrehbein.beveldevil.game.Hit) {
        box(h, GOLD_LO2, GOLD)
        say(Txt.noAds.toString(), h.x + h.w / 2f, h.y + 6.5f, 4.5f, CREAM, Paint.Align.CENTER)
    }

    fun screen(game: Game, l: Layout) {
        rect(0, 0, l.lw, l.lh, 0x70100818)
        px.at(l.sx, l.sy) {
            button(Ui.back, "<", false)
            if (game.noAdsOffered) noAdsButton(Ui.noAdsSettings)
            say(Txt.settings.toString(), 128f, 14f, 7f, GOLD_HI, Paint.Align.CENTER, GOLD_LO)
            val labels = listOf(Txt.controls, Txt.btnSize, Txt.haptics, Txt.sound, Txt.music, Txt.leftHanded, Txt.tilt)
            val opts = listOf<List<T>>(
                listOf(Txt.schemeButtons, Txt.schemeStick), listOf(T("S", "S"), T("M", "M"), T("L", "L")),
                listOf(Txt.on, Txt.off), listOf(Txt.on, Txt.off), listOf(Txt.on, Txt.off), listOf(Txt.on, Txt.off), listOf(Txt.on, Txt.off),
            )
            for (row in 0 until Ui.SET_ROWS) {
                say(labels[row].toString(), 14f, Ui.SET_TOP + 7f + row * Ui.SET_STEP, 5f, CREAM)
                for (i in opts[row].indices) {
                    val h = Ui.setOpt(row, i, opts[row].size)
                    val on = game.optionOf(row) == i
                    box(h, if (on) RED_BTN else PLUM, if (on) RED_BTN_HI else PLUM_HI)
                    say(opts[row][i].toString(), h.x + h.w / 2f, h.y + 7f, 6f, if (on) CREAM else 0xFFB9A6CF.toInt(), Paint.Align.CENTER)
                }
            }
            if (game.adChoicesOffered) {
                box(Ui.adChoices, PLUM, PLUM_HI)
                say(Txt.adChoices.toString(), Ui.adChoices.x + Ui.adChoices.w / 2f, 135f, 5f, CREAM, Paint.Align.CENTER)
            }
            box(Ui.privacy, PLUM, PLUM_HI)
            say(T("PRIVACY", "DATENSCHUTZ").toString(), 128f, 135f, 5f, CREAM, Paint.Align.CENTER)
        }
    }
}
