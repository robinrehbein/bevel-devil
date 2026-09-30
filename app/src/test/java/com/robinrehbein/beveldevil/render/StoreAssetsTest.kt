package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.robinrehbein.beveldevil.game.Audio
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Lang
import com.robinrehbein.beveldevil.game.Mood
import com.robinrehbein.beveldevil.game.Sound
import com.robinrehbein.beveldevil.game.T
import com.robinrehbein.beveldevil.game.Twists
import com.robinrehbein.beveldevil.game.Ui
import com.robinrehbein.beveldevil.game.WorldState
import com.robinrehbein.beveldevil.game.Worlds
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.hypot

/**
 * Renders the Google Play store graphics from the game's own painters into docs/play:
 * feature graphic (1024x500), phone screenshots (1920x1080, plain and with a caption banner), German and English.
 *
 * Only runs with STORE_ASSETS=1 (or -DstoreAssets=1), so normal test runs never rewrite the docs:
 *
 *     STORE_ASSETS=1 gradle testDebugUnitTest --tests '*StoreAssetsTest' -q
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class StoreAssetsTest {
    companion object {
        fun prog(unlocked: Int = 12) = MemoryProgress().apply {
            this.unlocked = unlocked
            for (c in listOf(Card.DEVIL_SAW, Card.GHOST_BLOCK, Card.SINKING, Card.CRUMBLE, Card.DECOY)) findCard(c)
        }
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private val out = File("../docs/play")

    private fun enabled() = System.getenv("STORE_ASSETS") == "1" || System.getProperty("storeAssets") != null


    /** Plays the game frame by frame through one renderer, so animation state is real. */
    private class Film(val game: Game, val w: Int, val h: Int) {
        val r = Renderer(RuntimeEnvironment.getApplication())
        val layout = Layout().also { it.update(w, h, 2.75f) }
        val bmp: Bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        fun frame() = r.draw(Canvas(bmp), game, layout)
        fun play(seconds: Float, until: () -> Boolean = { false }) {
            var t = 0f
            var n = 0
            while (t < seconds && !until()) {
                game.update(1f / 120f)
                if (n++ % 2 == 0) frame()
                t += 1f / 120f
            }
        }
        fun world() = game.world!!
        fun runTo(x: Float, max: Float = 8f) { game.input.right = true; play(max) { world().player.box.cx > x }; game.input.right = false }
        fun jump() { game.input.jumpPressed = true; game.input.jump = true }
        fun tapStage(x: Int, y: Int) = game.tap(x + 2f, y + 2f)
    }

    private class Scene(val name: String, val caption: T, val progress: () -> MemoryProgress = { prog() }, val stage: (Film) -> Unit)

    private val scenes = listOf(
        Scene("01", T("The level is the enemy.", "Der Level ist der Gegner.")) { f ->
            f.play(1.3f)
        },
        Scene("02", T("Looks harmless. It is not.", "Sieht harmlos aus. Ist es nicht.")) { f ->
            f.game.startLevel(7)
            f.play(0.3f)
            f.runTo(7.0f)
            f.jump()
            f.game.input.right = true
            f.play(0.22f)
        },
        Scene("03", T("Every trap is a devil card.", "Jede Falle ist eine Teufelskarte.")) { f ->
            f.game.startLevel(2)
            f.game.input.right = true
            f.play(8f) { f.game.card != null }
            f.play(0.4f)
        },
        Scene("04", T("Collect them all.", "Sammle sie alle.")) { f ->
            f.play(0.4f)
            f.tapStage(Ui.titleAlbum.x, Ui.titleAlbum.y)
            f.play(0.4f)
            f.tapStage(Ui.albumCard(4).x, Ui.albumCard(4).y)
            f.play(0.5f)
        },
        Scene("05", T("Lasers, portals, server hell.", "Laser, Portale, Server-Hölle.")) { f ->
            f.game.startLevel(Worlds.get(2).firstLevel + 17)
            f.runTo(9.0f)
            f.play(8f) { f.world().beams.isNotEmpty() && f.world().beams[0].lit }
            f.play(0.12f)
        },
        Scene("06", T("Mephi cheats. Always.", "Mephi schummelt. Immer.")) { f ->
            f.game.startLevel(37)
            f.runTo(6.3f)
            f.play(0.3f)
        },
        Scene("07", T("96 levels, two layers of hell.", "96 Level, zwei Schichten Hölle."), { prog(69).apply { for (i in 0 until 68) saveBest(i, (i * 7) % 19) } }) { f ->
            f.play(0.5f)
            f.tapStage(Ui.titlePlay.x, Ui.titlePlay.y)
            f.play(0.8f)
        },
        Scene("08", T("Tilt the world. Literally.", "Neig die Welt. Wörtlich.")) { f ->
            f.game.startLevel(38)
            f.play(0.3f)
            f.runTo(6.5f)
            f.game.input.tilt = 0.8f
            f.play(1.4f)
        },
    )

    private fun save(bmp: Bitmap, file: File) {
        file.parentFile!!.mkdirs()
        bmp.setHasAlpha(false) // Play wants 24-bit PNGs without an alpha channel
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    /** Caption banner under the game picture: velvet bar, gold rim, pixel-font text with hard shadow. */
    private fun banner(c: Canvas, y: Int, w: Int, h: Int, text: String) {
        val p = Paint()
        p.shader = LinearGradient(0f, y.toFloat(), 0f, (y + h).toFloat(), 0xFF3B0F2A.toInt(), 0xFF14050F.toInt(), Shader.TileMode.CLAMP)
        c.drawRect(0f, y.toFloat(), w.toFloat(), (y + h).toFloat(), p)
        p.shader = null
        p.color = INK; c.drawRect(0f, y.toFloat(), w.toFloat(), y + 6f, p)
        p.color = GOLD; c.drawRect(0f, y + 6f, w.toFloat(), y + 18f, p)
        p.color = GOLD_HI; c.drawRect(0f, y + 6f, w.toFloat(), y + 9f, p)
        p.color = GOLD_LO2; c.drawRect(0f, y + 15f, w.toFloat(), y + 18f, p)
        val t = Pixels(RuntimeEnvironment.getApplication()).text
        t.textAlign = Paint.Align.CENTER
        var size = 76f
        t.textSize = size
        while (t.measureText(text) > w - 240f) { size -= 2f; t.textSize = size }
        val base = y + 18f + (h - 18f) / 2f + size * 0.36f
        t.color = 0xFF000000.toInt(); c.drawText(text, w / 2f + 5f, base + 5f, t)
        t.color = GOLD_HI; c.drawText(text, w / 2f, base, t)
    }

    private fun shoot(de: Boolean, scene: Scene, captioned: Boolean): Bitmap {
        Lang.german = de
        val game = Game(scene.progress(), silent)
        if (!captioned) {
            val f = Film(game, 1920, 1080)
            scene.stage(f)
            f.frame()
            return f.bmp
        }
        val f = Film(game, 1920, 900)
        scene.stage(f)
        f.frame()
        val full = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)
        val c = Canvas(full)
        c.drawBitmap(f.bmp, 0f, 0f, null)
        banner(c, 900, 1920, 180, scene.caption.toString())
        return full
    }

    @Test
    fun phoneScreenshots() {
        assumeTrue(enabled())
        for (de in listOf(true, false)) {
            val sfx = if (de) "" else "-en"
            for (s in scenes) {
                save(shoot(de, s, captioned = false), File(out, "screenshots/phone$sfx/${s.name}.png"))
                save(shoot(de, s, captioned = true), File(out, "screenshots/phone-captioned$sfx/${s.name}.png"))
            }
        }
        Lang.german = true
    }

    /** A static copy of the renderer's hell swirl (its own is private): dithered bands behind the level. */
    private fun swirl(px: Pixels) {
        val cool = intArrayOf(0xFF160A22.toInt(), 0xFF240E30.toInt(), 0xFF34123E.toInt(), 0xFF22244E.toInt(), 0xFF1E405C.toInt(), 0xFF2C5C6E.toInt())
        val hot = intArrayOf(0xFF1E0818.toInt(), 0xFF380C22.toInt(), 0xFF5C1028.toInt(), 0xFF841A2C.toInt(), 0xFFAA3232.toInt(), 0xFFCC5A40.toInt())
        val bayer = intArrayOf(0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5)
        val w = px.lo.width
        val h = px.lo.height
        val a = IntArray(w * h)
        for (y in 0 until h) for (x in 0 until w) {
            val dx = x - w * 0.62f
            val dy = (y - h * 0.5f) * 1.3f
            val r = kotlin.math.sqrt(dx * dx + dy * dy)
            val ang = kotlin.math.atan2(dy, dx)
            val v = kotlin.math.sin(ang * 2 + r * 0.09f - 1.6f) * 0.8f + 0.5f * kotlin.math.sin(x * 0.07f + 0.8f + kotlin.math.sin(y * 0.1f + 1.1f)) + 0.35f * kotlin.math.sin(r * 0.14f - 0.4f)
            val n = (v + 1.65f) / 3.3f + (bayer[(y and 3) * 4 + (x and 3)] / 16f - 0.5f) * 0.16f
            val idx = kotlin.math.floor(n * 6).toInt().coerceIn(0, 5)
            val isHot = Math.floorMod(x * 7 + y * 13, 97) / 97f < 0.55f
            a[y * w + x] = if (isHot) hot[idx] else cool[idx]
        }
        px.lo.setPixels(a, 0, w, 0, 0, w, h)
    }

    @Test
    fun featureGraphic() {
        assumeTrue(enabled())
        Lang.german = true
        val ctx = RuntimeEnvironment.getApplication()
        val px = Pixels(ctx)
        val layout = Layout().apply { update(1024, 576, 2.75f) } // 256x144 logical at 4x
        px.resize(layout.lw, layout.lh)
        val game = Game(prog(), silent)
        game.startLevel(7)
        game.input.right = true
        var t = 0f
        fun step() { game.update(1f / 120f); t += 1f / 120f }
        while (game.world!!.player.box.cx < 7.4f && t < 6f) step()
        game.input.jumpPressed = true; game.input.jump = true
        repeat(30) { step() }
        px.texts.clear()
        swirl(px)
        WorldPainter(px).draw(game, layout)
        px.texts.clear()
        val lc = px.lc
        val ui = UiPainter(px)
        // darken the left two thirds so the logo reads
        val shade = Paint()
        for (x in 0 until 190) {
            val a = (0xC0 * (1f - x / 190f).coerceIn(0f, 1f)).toInt()
            shade.color = Color.argb(a, 7, 3, 13)
            lc.drawRect(x.toFloat(), 0f, x + 1f, layout.lh.toFloat(), shade)
        }
        val mephi = Logo(px, "MEPHI", GOLD, GOLD_MID, GOLD_HI, GOLD_LO2, GOLD_LO, 0xFF5A2A10.toInt())
        val daemon = Logo(px, "DAEMON", DEVIL_RED, 0xFFFF5E74.toInt(), 0xFFFF9DAA.toInt(), 0xFFA8203A.toInt(), DEVIL_RED_LO, 0xFF420814.toInt())
        val top = 34f
        mephi.draw(lc, 18f, top, 0.9f)
        daemon.draw(lc, 18f, top + 27f, 1.6f)
        ui.devilFrame(172f, top - 6f, 64f, Mood.LAUGH, 0.3f, spriteScale = 2)

        val scale = 4
        val big = Bitmap.createScaledBitmap(px.lo, px.lo.width * scale, px.lo.height * scale, false)
        val cropY = 10 * scale
        val full = Bitmap.createBitmap(1024, 500, Bitmap.Config.ARGB_8888)
        val c = Canvas(full)
        c.drawBitmap(big, 0f, -cropY.toFloat(), null)
        // "THE" between the two words, crisp at full resolution
        val tp = px.text
        tp.textAlign = Paint.Align.LEFT
        tp.textSize = 6.5f * scale
        val by = (top + 26f + 6.5f * 0.36f) * scale - cropY
        tp.color = 0xFF000000.toInt(); c.drawText("THE", 18f * scale + 4f, by + 4f, tp)
        tp.color = CREAM; c.drawText("THE", 18f * scale, by, tp)
        // CRT: scanlines and vignette
        val sl = Paint().apply { color = 0x2E000000 }
        var y = 0
        while (y < 500) { c.drawRect(0f, y.toFloat(), 1024f, y + 1.5f, sl); y += 4 }
        val vp = Paint().apply {
            shader = RadialGradient(512f, 250f, hypot(1024f, 500f) * 0.54f, intArrayOf(0, 0, 0x80000000.toInt()), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, 1024f, 500f, vp)
        save(full, File(out, "feature-graphic.png"))
    }
}
