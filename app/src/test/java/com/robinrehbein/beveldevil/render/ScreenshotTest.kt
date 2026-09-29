package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import org.robolectric.RuntimeEnvironment
import com.robinrehbein.beveldevil.game.Audio
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Lang
import com.robinrehbein.beveldevil.game.Progress
import com.robinrehbein.beveldevil.game.Sound
import com.robinrehbein.beveldevil.game.Ui
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

class MemoryProgress : Progress {
    override var unlocked = 7
    override var sound = true
    private val best = mutableMapOf(0 to 3, 1 to 7, 2 to 12, 3 to 5, 4 to 21, 5 to 9)
    private val found = mutableSetOf(Card.COLLAPSE, Card.SPIKE_SEED, Card.SHY_DOOR, Card.HEADBUTT, Card.UPSIDE_DOWN, Card.TWISTED)
    private val deaths = mutableMapOf<Card, Int>(Card.COLLAPSE to 12, Card.SPIKE_SEED to 8, Card.SHY_DOOR to 3)
    override fun bestDeaths(level: Int) = best[level]
    override fun saveBest(level: Int, deaths: Int) { best[level] = deaths }
    override fun cardFound(card: Card) = card in found
    override fun findCard(card: Card) { found += card }
    override fun cardDeaths(card: Card) = deaths[card] ?: 0
    override fun addCardDeath(card: Card) { deaths[card] = cardDeaths(card) + 1 }
}

/** Renders real screens to PNGs in app/build/screenshots, for eyeballing the look without a device. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ScreenshotTest {
    private val silent = object : Audio { override fun play(sound: Sound) {} }

    private fun shoot(name: String, game: Game, w: Int = 2400, h: Int = 1080) {
        val r = Renderer(RuntimeEnvironment.getApplication())
        r.layout(w, h)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = ControlLayout().apply { this.r = 90f; y = h - 140f; leftX = 140f; rightX = 140f + 3.3f * 90f; jumpX = w - 140f }
        r.draw(Canvas(bmp), game, c)
        val dir = File("build/screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun run(game: Game, seconds: Float, right: Boolean = false, jumpAt: Float = -1f) {
        var t = 0f
        game.input.right = right
        while (t < seconds) {
            if (jumpAt in t..(t + 1f / 120f)) { game.input.jumpPressed = true; game.input.jump = true }
            game.update(1f / 120f)
            t += 1f / 120f
        }
    }

    @Test
    fun screens() {
        Lang.german = true
        val g = Game(MemoryProgress(), silent)
        run(g, 1.3f); shoot("01-title", g)
        g.tap(Ui.titlePlay.x + 2f, Ui.titlePlay.y + 2f); run(g, 0.5f); shoot("02-select", g)
        g.startLevel(0); run(g, 0.8f); shoot("03-level1-intro", g)
        run(g, 1.6f, right = true); shoot("04-level1-trap", g)
        run(g, 0.45f, right = true); shoot("05-level1-dead", g)
        g.startLevel(2); run(g, 3.0f, right = true); shoot("06-level3-door", g)
        g.startLevel(4); run(g, 2.0f, right = true); shoot("07-level5-flip", g)
        g.startLevel(6); run(g, 1.7f, right = true); shoot("08-level7-saw", g)
        g.startLevel(10); run(g, 1.3f, right = true); shoot("09-level11-crumble", g)
        g.startLevel(0); run(g, 2.0f, right = true, jumpAt = 1.75f); run(g, 2.5f, right = true); shoot("10-clear", g)
        g.back(); g.back(); g.tap(Ui.titleAlbum.x + 2f, Ui.titleAlbum.y + 2f); run(g, 0.3f); shoot("11-album", g)
        g.tap(Ui.albumCard(0).x + 2f, Ui.albumCard(0).y + 2f); run(g, 0.1f); shoot("12-album-card", g)
        val phone16x9 = Game(MemoryProgress(), silent); phone16x9.startLevel(3); run(phone16x9, 1.2f, right = true)
        shoot("13-level4-16x9", phone16x9, 1920, 1080)
    }
}
