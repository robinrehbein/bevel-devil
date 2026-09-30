package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import com.robinrehbein.beveldevil.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Draws the real launcher layers under several launcher masks (circle, squircle, rounded square, teardrop) at
 * 48, 96 and 192 px, plus the themed monochrome layer, to app/build/screenshots/icon-*.png.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class IconScreenshotTest {
    private fun mask(kind: String, s: Float): Path {
        val p = Path()
        val r = RectF(0f, 0f, s, s)
        when (kind) {
            "circle" -> p.addOval(r, Path.Direction.CW)
            "squircle" -> p.addRoundRect(r, s * 0.34f, s * 0.34f, Path.Direction.CW)
            "rounded" -> p.addRoundRect(r, s * 0.16f, s * 0.16f, Path.Direction.CW)
            else -> p.addRoundRect(r, floatArrayOf(s / 2, s / 2, s / 2, s / 2, s / 2, s / 2, s * 0.08f, s * 0.08f), Path.Direction.CW)
        }
        return p
    }

    private fun render(kind: String, size: Int, mono: Boolean): Bitmap {
        val ctx = RuntimeEnvironment.getApplication()
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()
        c.clipPath(mask(kind, s))
        // the 108 dp layer canvas maps to 1.5x the visible mask box
        val full = (s * 1.5f).toInt()
        val off = ((s - full) / 2f).toInt()
        fun layer(id: Int, tint: Int? = null) {
            val d = ctx.getDrawable(id)!!.mutate()
            d.setBounds(off, off, off + full, off + full)
            if (tint != null) d.colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
            d.draw(c)
        }
        if (mono) {
            c.drawColor(Color.rgb(0xF3, 0xD9, 0xDE))
            layer(R.drawable.ic_launcher_monochrome, Color.rgb(0x5A, 0x1A, 0x2E))
        } else {
            layer(R.drawable.ic_launcher_background)
            layer(R.drawable.ic_launcher_foreground)
        }
        return bmp
    }

    @Test
    fun launcherIconUnderMasks() {
        val dir = File("build/screenshots").apply { mkdirs() }
        var painted = 0
        for (size in listOf(48, 96, 192)) for (kind in listOf("circle", "squircle", "rounded", "teardrop")) {
            val bmp = render(kind, size, mono = false)
            File(dir, "icon-$kind-$size.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            if (Color.alpha(bmp.getPixel(size / 2, size / 2)) == 255) painted++
        }
        for (size in listOf(48, 96, 192)) {
            val bmp = render("circle", size, mono = true)
            File(dir, "icon-mono-$size.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        assertEquals(12, painted)
    }
}
