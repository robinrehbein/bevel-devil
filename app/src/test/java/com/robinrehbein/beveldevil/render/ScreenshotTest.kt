package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import org.robolectric.RuntimeEnvironment
import com.robinrehbein.beveldevil.game.Audio
import com.robinrehbein.beveldevil.game.Action
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Demo
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Intro
import com.robinrehbein.beveldevil.game.Lang
import com.robinrehbein.beveldevil.game.Level
import com.robinrehbein.beveldevil.game.PauseTrick
import com.robinrehbein.beveldevil.game.T
import com.robinrehbein.beveldevil.game.Trigger
import com.robinrehbein.beveldevil.game.TwistDemos
import com.robinrehbein.beveldevil.game.Twists
import com.robinrehbein.beveldevil.game.trap
import com.robinrehbein.beveldevil.game.Progress
import com.robinrehbein.beveldevil.game.Sound
import com.robinrehbein.beveldevil.game.Ui
import com.robinrehbein.beveldevil.game.WorldState
import com.robinrehbein.beveldevil.game.Worlds
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
    override var tiltSensor = true
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
        g.startLevel(7); run(g, 2.0f, right = true); shoot("07-level5-flip", g)
        g.startLevel(5); run(g, 1.7f, right = true); shoot("08-level7-saw", g)
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
    fun levelSelect() {
        Lang.german = true
        val two = sizes.take(2)
        fun cleared(n: Int) = MemoryProgress().apply { unlocked = n + 1; for (i in 0 until n) saveBest(i, (i * 7) % 23) }
        fun open(p: Progress) = Game(p, silent).also { run(it, 0.5f); it.tap(Ui.titlePlay.x + 2f, Ui.titlePlay.y + 2f); run(it, 0.6f) }
        shoot("50-select-w1-p1", open(MemoryProgress()), two)
        shoot("51-select-w1-mid", open(cleared(30)), two)
        val w2 = open(cleared(48 + 20))
        shoot("52-select-w2", w2, two)
        w2.page(-1); run(w2, 0.6f)
        w2.tap(Ui.worldTab(0, 3).x + 2f, Ui.worldTab(0, 3).y + 2f); run(w2, 0.6f)
        shoot("53-select-w1-last", w2, two)
        Lang.german = false
        shoot("54-select-w1-last-en", w2, two.take(1))
        Lang.german = true
        w2.startLevel(48 + 16); run(w2, 1.2f)
        shoot("55-hud-w2", w2, sizes)
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

    /** The meta twists, each on its test-only demo level. */
    @Test
    fun twists() {
        Lang.german = true
        for (s in sizes.take(2)) {
            val tag = if (s === sizes[0]) "" else "-${s.tag}"
            fun film(level: Level) = Film(Game(MemoryProgress(), silent).apply { sandbox = level; startLevel(0) }, s)
            fun Film.w() = game.world!!
            film(TwistDemos.credits).apply {
                play(0.6f)
                game.input.right = true
                play(6f) { w().fake != null }
                game.input.right = false
                play(Twists.FAKE_DELAY + 2.4f); save("60-fake-credits-mid$tag")
                play(12f) { w().fake == null }
                play(0.05f); save("61-fake-nope$tag")
                play(1.2f); save("62-credits-platforms$tag")
            }
            film(TwistDemos.clear).apply {
                game.input.right = true
                play(6f) { w().fake != null }
                game.input.right = false
                play(Twists.FAKE_DELAY + 1.2f); save("63-fake-clear$tag")
            }
            film(TwistDemos.crack).apply {
                play(0.4f)
                game.input.right = true
                play(3f) { w().cracks.isNotEmpty() }
                game.input.right = false
                play(0.55f); save("64-frame-crack$tag")
                play(0.62f); save("65-frame-falling$tag")
                play(1f); save("66-frame-landed$tag")
            }
            film(TwistDemos.ghost).apply {
                play(0.5f)
                game.input.right = true
                play(6f) { w().state == WorldState.DEAD }
                game.input.right = false
                val dead = w()
                play(3f) { game.world !== dead }
                play(0.4f)
                // run ahead and stop short of the spikes; the ghost comes after you
                game.input.right = true
                play(1.1f)
                game.input.right = false
                play(0.8f); save("67-ghost$tag")
            }
            film(TwistDemos.pause).apply {
                play(0.6f)
                game.tap(Ui.hudPause.x + 1f, Ui.hudPause.y + 1f)
                play(0.3f); save("68-pause-dodge$tag")
            }
            film(Level(T("Sharp Break", "Spitze Pause"), T("Relax.", "Entspann dich."), traps = listOf(trap(Trigger.After(0.2f), Action.PauseTrap(PauseTrick.SPIKE), Action.Say(T("Go on, take a break.", "Mach ruhig Pause."))))) {
                border(); floor(); put(2, 14, 'P'); put(29, 14, 'D')
            }).apply { play(1.5f); save("69-pause-spike$tag") }
            film(Level(T("Swap", "Tausch"), T("Need a break?", "Pause gefällig?"), traps = listOf(trap(Trigger.After(0f), Action.PauseTrap(PauseTrick.SWAP)))) {
                border(); floor(); put(2, 14, 'P'); put(29, 14, 'D')
            }).apply {
                play(0.8f); game.tap(Ui.hudPause.x + 1f, Ui.hudPause.y + 1f)
                play(0.38f); save("70-pause-swap-mid$tag")
                play(1f); save("71-pause-swapped$tag")
            }
            film(TwistDemos.flip).apply {
                game.input.right = true
                play(3f) { w().viewTurn() > 0f }
                game.input.right = false
                play(0.12f); save("72-flip-mid$tag")
                play(0.6f); save("73-flip-upside$tag")
            }
            film(Level(T("Vertical Hold", "Bildlauf"), T("Nice picture, right?", "Schönes Bild, oder?"), traps = listOf(trap(Trigger.PastX(5f), Action.Roll(1.4f)))) {
                border(); floor(); fill(12..14, 12..12); put(2, 14, 'P'); put(29, 14, 'D')
            }).apply {
                game.input.right = true
                play(3f) { w().viewRoll() > 0.3f }
                save("74-roll$tag")
            }
        }
    }

    @Test
    fun mechanics() {
        Lang.german = true
        val big = sizes[0]
        // blinking bridge: solid, flickering before it goes, gone (faint outline), about to return (bright)
        Film(Game(MemoryProgress(), silent), big).apply {
            game.startCustom(Demo.blink)
            val g = game.world!!.group('a')
            play(1.0f); save("60-blink-on")
            play(1f) { g.warn > 0.55f && g.warn < 0.8f }; save("61-blink-warn")
            play(1f) { !g.visible }; play(0.2f); save("62-blink-off")
            play(1f) { g.soon }; play(0.05f); save("63-blink-soon")
        }
        // saws on paths: a pendulum and one circling a block, with their tracks dotted in
        Film(Game(MemoryProgress(), silent), big).apply {
            game.startCustom(Demo.pathSaw)
            play(2.4f); save("64-path-saw")
        }
        // tilt HUD: the sensor version, then the button fallback with a latched tilt and a shake
        val both = Level(
            T("Spirit level", "Wasserwaage"), T("Tilt the world.", "Neig die Welt."),
            traps = listOf(trap(Trigger.Shaken, Action.Hide('b'))),
            start = listOf(Action.Tilt('a', left = 0f, right = 13f, speed = 6f)),
        ) {
            border(); floor(); put(2, 14, 'P'); put(29, 14, 'D')
            fill(8..23, 15..17, '.'); fill(8..10, 15..15, 'a'); fill(26..26, 1..14, 'b')
        }
        for (s in sizes.take(2)) Film(Game(MemoryProgress(), silent), s).apply {
            game.startCustom(both)
            game.input.right = true
            play(0.8f) { game.world!!.player.box.cx > 9f }
            game.input.right = false
            play(0.3f)
            game.input.tilt = 0.45f
            play(1.2f); save("65-tilt-sensor-${s.tag}")
            layout.controls.motionButtons = true
            layout.controls.tiltLatch = 1
            game.input.tilt = 1f
            play(0.6f)
            game.input.shake = true
            play(0.1f); save("66-tilt-buttons-${s.tag}")
        }
    }

    /** The new World 1: a look at levels of act 2 (new mechanics) and act 3 (meta twists). */
    @Test
    fun worldOneActs() {
        Lang.german = true
        for (s in sizes.take(2)) {
            val tag = if (s === sizes[0]) "" else "-${s.tag}"
            fun film(n: Int) = Film(Game(MemoryProgress(), silent).apply { startLevel(n - 1) }, s)
            fun Film.w() = game.world!!
            film(17).apply { play(1.2f); save("90-l17-blink-on$tag"); play(2f) { !w().group('a').visible }; play(0.2f); save("91-l17-blink-off$tag") }
            film(18).apply { play(2.6f); save("92-l18-pendulum$tag") }
            film(20).apply { play(1.4f); save("93-l20-lights$tag") }
            film(22).apply { play(2.0f); save("94-l22-airlock$tag") }
            film(26).apply { play(1.6f); save("95-l26-stairs$tag") }
            film(28).apply { play(0.8f); save("96-l28-rope$tag") }
            film(32).apply { play(0.9f); save("97-l32-beta$tag") }
            film(36).apply {
                game.input.right = true
                play(3f) { w().player.box.cx > 4.6f }
                game.input.right = false
                play(0.5f); save("80-l36-crack-warn$tag")
                play(1.0f); save("81-l36-crack-fell$tag")
            }
            film(38).apply {
                game.input.right = true
                play(3f) { w().player.box.cx > 6.3f }
                game.input.right = false
                play(0.6f); save("82-l38-flip$tag")
            }
            film(39).apply { play(0.6f); save("83-l39-tilt$tag") }
            film(41).apply {
                game.input.right = true
                play(3f) { w().player.box.cx > 10.3f }
                game.input.right = false
                play(0.9f); save("84-l41-roll$tag")
            }
            film(42).apply {
                game.input.right = true
                play(3f) { w().player.box.cx > 8f }
                play(0.5f); save("85-l42-panic$tag")
            }
            film(44).apply { play(0.6f); save("86-l44-shake$tag") }
            film(48).apply {
                game.input.right = true
                play(0.5f)
                play(6f) { w().fake != null }
                game.input.right = false
                play(Twists.FAKE_DELAY + 2.4f); save("87-l48-credits$tag")
                play(14f) { w().fake == null }
                play(1.5f); save("88-l48-stairs$tag")
            }
        }
    }

    @Test
    fun dataCenter() {
        Lang.german = true
        for (s in sizes.take(2)) {
            val tag = if (s === sizes[0]) "" else "-${s.tag}"
            fun film(n: Int) = Film(Game(MemoryProgress(), silent).apply { startLevel(Worlds.get(2).firstLevel + n - 1) }, s)
            film(1).apply { play(1.2f); save("110-w2-l1$tag") }
            film(5).apply { play(1.2f); save("111-w2-l5$tag") }
            film(13).apply { play(1.2f); save("112-w2-l13-long-name$tag") }
            film(10).apply {
                game.input.right = true
                play(4f) { game.world!!.player.box.cx > 6.6f }
                game.input.right = false
                play(0.45f); save("113-w2-l10-trap$tag")
            }
        }
        Film(Game(MemoryProgress(), silent).apply { startLevel(14) }, sizes[0]).apply { play(1.2f); save("114-w1-l15-compare") }
        // long names must fit the plate in every HUD mode
        val names = com.robinrehbein.beveldevil.game.Levels.all.withIndex()
        val sandwich = names.first { it.value.name.de.startsWith("sudo mach") }.index
        val w2 = Worlds.get(2)
        val longest = (w2.firstLevel until w2.firstLevel + w2.size).maxByOrNull { com.robinrehbein.beveldevil.game.Levels.all[it].name.de.length }!!
        for (s in sizes) {
            val sfx = if (s === sizes[0]) "" else "-${s.tag}"
            Film(Game(MemoryProgress(), silent).apply { startLevel(sandwich) }, s).apply { play(0.8f); save("115-hud-sandwich$sfx") }
            Film(Game(MemoryProgress(), silent).apply { startLevel(longest) }, s).apply { play(0.8f); save("117-hud-longest-w2$sfx") }
        }
        val g = Game(MemoryProgress().apply { unlocked = 60 }, silent)
        run(g, 0.5f); g.tap(Ui.titlePlay.x + 2f, Ui.titlePlay.y + 2f); run(g, 0.6f)
        shoot("116-select", g, sizes.take(2))
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
