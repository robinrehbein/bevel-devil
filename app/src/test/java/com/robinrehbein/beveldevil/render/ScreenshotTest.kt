package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import org.robolectric.RuntimeEnvironment
import com.robinrehbein.beveldevil.game.Audio
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Intro
import com.robinrehbein.beveldevil.game.Lang
import com.robinrehbein.beveldevil.game.Progress
import com.robinrehbein.beveldevil.game.Sound
import com.robinrehbein.beveldevil.game.Ui
import com.robinrehbein.beveldevil.game.WorldState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

class MemoryProgress : Progress {
    override var unlocked = 7
    override var sound = true
    override var stickScheme = false
    override var buttonSize = 1
    override var haptics = true
    override var leftHanded = false
    override var introSeen = true
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

    private class Size(val tag: String, val w: Int, val h: Int, val dp: Float, val cutL: Int = 0)
    private val sizes = listOf(
        Size("20x9", 2400, 1080, 2.75f),
        Size("16x9", 1920, 1080, 2.75f),
        Size("4x3", 2048, 1536, 2f),
        Size("19.5x9", 2340, 1080, 2.625f),
        Size("20x9-720p", 1600, 720, 2f),
    )

    private fun shoot(name: String, game: Game, only: List<Size> = sizes, ctl: ControlLayout.() -> Unit = {}) {
        for (s in only) shootAt(if (s === sizes[0]) name else "$name-${s.tag}", game, s, ctl)
    }

    private fun shootAt(name: String, game: Game, s: Size, ctl: ControlLayout.() -> Unit = {}) {
        val r = Renderer(RuntimeEnvironment.getApplication())
        val layout = Layout().apply { controls.ctl(); update(s.w, s.h, s.dp, cutL = s.cutL); controls.ctl() }
        val bmp = Bitmap.createBitmap(s.w, s.h, Bitmap.Config.ARGB_8888)
        r.draw(Canvas(bmp), game, layout)
        val dir = File("build/screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    /** Plays the game frame by frame through one renderer, so transitions see the frames before them. */
    private class Film(val game: Game, val s: Size) {
        val r = Renderer(RuntimeEnvironment.getApplication())
        val layout = Layout().apply { update(s.w, s.h, s.dp) }
        val bmp: Bitmap = Bitmap.createBitmap(s.w, s.h, Bitmap.Config.ARGB_8888)
        fun frame() = r.draw(Canvas(bmp), game, layout)
        /** Advances [seconds] at 120 Hz, rendering at 60 fps, or until [until] holds. */
        fun play(seconds: Float, until: () -> Boolean = { false }) {
            var t = 0f
            var n = 0
            while (t < seconds && !until()) {
                game.update(1f / 120f)
                if (n++ % 2 == 0) frame()
                t += 1f / 120f
            }
        }
        fun save(name: String) {
            frame()
            val dir = File("build/screenshots").apply { mkdirs() }
            File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    @Test
    fun animations() {
        Lang.german = true
        val big = sizes[0]
        // spawn pop-in and landing dust
        Film(Game(MemoryProgress(), silent), big).apply {
            game.startLevel(0)
            play(0.14f); save("16-spawn-pop")
            game.input.right = true
            play(0.5f)
            game.input.jumpPressed = true; game.input.jump = true
            play(0.1f)
            game.input.jump = false
            play(2f) { game.world!!.player.grounded }
            play(0.06f); save("17-land-dust")
        }
        // death shatter, then the iris wipe into the next attempt
        Film(Game(MemoryProgress(), silent), big).apply {
            game.startLevel(1)
            game.input.right = true
            play(5f) { game.world!!.state == WorldState.DEAD }
            play(0.1f); save("18-death-shatter")
            val dead = game.world
            play(2f) { game.world !== dead }
            play(0.22f); save("19-restart-iris")
        }
        // sucked into the door with a light burst
        Film(Game(MemoryProgress(), silent), big).apply {
            game.startLevel(0)
            game.input.right = true
            play(1.75f); game.input.jumpPressed = true; game.input.jump = true
            play(3f) { game.world!!.state == WorldState.WON }
            play(0.2f); save("20-win-suck")
            play(0.2f); save("21-win-burst")
        }
        // screen change: dither wipe from the title to the level select
        Film(Game(MemoryProgress(), silent), big).apply {
            play(0.5f)
            game.tap(Ui.titlePlay.x + 2f, Ui.titlePlay.y + 2f)
            play(0.16f); save("22-wipe")
        }
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
        val phone = Game(MemoryProgress(), silent); phone.startLevel(3); run(phone, 1.2f, right = true)
        shoot("13-level4", phone)
        shootAt("14-level4-cutout", phone, Size("cutout", 2400, 1080, 2.75f, cutL = 110))
        g.back(); g.startLevel(1); run(g, 1.0f, right = true); g.pause(); shoot("15-pause", g)
    }

    @Test
    fun story() {
        Lang.german = true
        val two = sizes.take(2)
        for (de in listOf(true, false)) {
            Lang.german = de
            val sfx = if (de) "" else "-en"
            val fresh = MemoryProgress().apply { introSeen = false }
            val g = Game(fresh, silent)
            for (page in 0 until 4) {
                // type it out fully, then show
                run(g, Intro.duration(page) + 0.1f)
                shoot("30-intro-${page + 1}$sfx", g, two)
                g.tap(100f, 100f)
            }
            run(g, 1.5f); shoot("34-world1$sfx", g, two)
        }
        Lang.german = true
        val g = Game(MemoryProgress(), silent)
        g.showWorldIntro(2); run(g, 2f); shoot("35-world2", g, two)
        g.showWorldIntro(3); run(g, 2f); shoot("36-world3", g, two)
        g.showWorldIntro(1); run(g, 0.25f); shoot("37-world1-typing", g, two)
        // mid-intro moments: terminal scrolling, the packet mid-flight
        val h = Game(MemoryProgress().apply { introSeen = false }, silent)
        run(h, 1.4f); shoot("38-terminal-mid", h, two)
        h.tap(100f, 100f); h.tap(100f, 100f); h.tap(100f, 100f); h.tap(100f, 100f); run(h, 1.6f); shoot("39-intro-packet-mid", h, two)
        // gameplay frame in the middle of a glitch
        for (s in two) {
            val f = Film(Game(MemoryProgress(), silent), s)
            f.game.startLevel(0)
            f.game.input.right = true
            f.play(5f) { f.game.card != null }
            f.play(0.07f); f.save("40-glitch-${s.tag}")
            f.play(0.1f); f.save("41-glitch-late-${s.tag}")
        }
    }

    @Test
    fun controls() {
        Lang.german = false
        val two = sizes.take(2)
        val g = Game(MemoryProgress(), silent)
        g.startLevel(1); run(g, 1.0f, right = true)
        shoot("20-controls-pressed", g, two) { right = true; jump = true }
        shoot("21-controls-idle", g, two)
        shoot("22-controls-lefty-large", g, two) { mirror = true; sizeScale = 1.25f; left = true }
        shoot("23-controls-small", g, two) { sizeScale = 0.8f }
        shoot("24-stick-touched", g, two) {
            stick = true; stickActive = true; stickX = 420f; stickY = 760f; knobX = 420f + 90f; stickR = 121f
        }
        shoot("25-stick-idle", g, two) { stick = true; stickR = 121f }
        val s = Game(MemoryProgress(), silent)
        run(s, 0.5f); s.tap(Ui.gear.x + 2f, Ui.gear.y + 2f); run(s, 0.3f)
        shoot("26-settings", s, two)
        s.setOption(0, 1); s.setOption(1, 2); s.setOption(4, 0); s.setOption(2, 1)
        Lang.german = true
        shoot("27-settings-de", s, two)
    }
}
