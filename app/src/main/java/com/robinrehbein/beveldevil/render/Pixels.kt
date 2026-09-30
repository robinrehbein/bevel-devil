package com.robinrehbein.beveldevil.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.robinrehbein.beveldevil.R
import com.robinrehbein.beveldevil.game.Hit
import com.robinrehbein.beveldevil.game.Ui
import kotlin.math.roundToInt

const val PW = Ui.W
const val PH = Ui.H
const val TS = 8

const val NIGHT = 0xFF07030D.toInt()
const val SHADOW = 0x8C000000.toInt()
const val INK = 0xFF1A0610.toInt()
const val INK_TEXT = 0xFF2A0710.toInt()
const val CREAM = 0xFFFFF4E0.toInt()
const val WHITE = 0xFFFFFFFF.toInt()
const val GOLD = 0xFFE8A84A.toInt()
const val GOLD_HI = 0xFFFFD98A.toInt()
const val GOLD_MID = 0xFFF6C46C.toInt()
const val GOLD_LO = 0xFF8F4F1F.toInt()
const val GOLD_LO2 = 0xFFB8702E.toInt()
const val GOLD_SPARK = 0xFFFFF1C4.toInt()
const val ROCK = 0xFF2E1426.toInt()
const val ROCK_HI = 0xFF4A2238.toInt()
const val ROCK_MID = 0xFF3A1A2E.toInt()
const val ROCK_LO = 0xFF12060E.toInt()
const val ROCK_LO2 = 0xFF200C1A.toInt()
const val EMBER = 0xFF8A2E24.toInt()
const val BONE = 0xFFFFF4EA.toInt()
const val BONE_LO = 0xFFCBB8D6.toInt()
const val MINT = 0xFF6CF2C2.toInt()
const val MINT_HI = 0xFFB8FFE6.toInt()
const val MINT_LO = 0xFF2AA97F.toInt()
const val HERO_OUT = 0xFF0D2A22.toInt()
const val DOOR_DARK = 0xFF1A0B1E.toInt()
const val PLUM = 0xFF2C1440.toInt()
const val PLUM_HI = 0xFF46295F.toInt()
const val RED_BTN = 0xFFB3321F.toInt()
const val RED_BTN_HI = 0xFFE0553F.toInt()
const val DEVIL_RED = 0xFFE2344E.toInt()
const val DEVIL_RED_LO = 0xFF7A1522.toInt()
const val VELVET = 0xFF3B0F2A.toInt()
const val VELVET_LO = 0xFF2A0A1E.toInt()
const val STEEL = 0xFFD9DBE6.toInt()
const val STEEL_LO = 0xFF9A9CB0.toInt()
const val LOCKED = 0xFF5A4050.toInt()
const val LOCKED_HI = 0xFF7A5A6A.toInt()
const val CARD_CREAM = 0xFFEFE3CF.toInt()
const val CARD_GOLD = 0xFFF6DDA4.toInt()
const val CARD_EDGE = 0xFFC9B9A0.toInt()
const val CARD_BACK = 0xFF8E1C2C.toInt()
const val TEAL = 0xFF3AA6A0.toInt()

/**
 * The lo-res logical canvas ([lo], sized by the [Layout]) plus a queue of text that the renderer
 * draws crisp at full resolution afterwards. [at] shifts both into a sub-area such as the playfield.
 */
class Pixels(context: Context) {
    var lo: Bitmap = Bitmap.createBitmap(PW, PH, Bitmap.Config.ARGB_8888)
        private set
    var lc = Canvas(lo)
        private set
    val fill = Paint()
    val blit = Paint().apply { isFilterBitmap = false; isDither = false }
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = context.resources.getFont(R.font.silkscreen) }
    private val measure = Paint().apply { typeface = text.typeface }
    val dst = RectF()

    class TextCmd {
        var s = ""; var x = 0f; var y = 0f; var size = 0f; var color = 0; var align = Paint.Align.LEFT; var shadow = 0
    }
    private val pool = ArrayList<TextCmd>()
    /** Text queued this frame; the commands are pooled, so a frame queues without allocating. */
    val texts = ArrayList<TextCmd>()
    @PublishedApi internal var ox = 0
    @PublishedApi internal var oy = 0

    fun resize(w: Int, h: Int) {
        if (lo.width == w && lo.height == h) return
        lo = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        lc = Canvas(lo)
    }

    /** Runs [block] with pixels and text shifted by ([x], [y]). */
    inline fun at(x: Int, y: Int, block: () -> Unit) {
        lc.save(); lc.translate(x.toFloat(), y.toFloat())
        ox += x; oy += y
        try { block() } finally { lc.restore(); ox -= x; oy -= y }
    }

    fun rect(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int) {
        fill.color = color
        val rx = x.roundToInt().toFloat()
        val ry = y.roundToInt().toFloat()
        c.drawRect(rx, ry, rx + w, ry + h, fill)
    }

    fun rect(x: Number, y: Number, w: Number, h: Number, color: Int) = rect(lc, x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color)
    // unboxed overloads for the common argument types, so per-frame drawing does not allocate
    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) = rect(lc, x, y, w, h, color)
    fun rect(x: Float, y: Float, w: Int, h: Int, color: Int) = rect(lc, x, y, w.toFloat(), h.toFloat(), color)
    fun rect(x: Int, y: Int, w: Int, h: Int, color: Int) = rect(lc, x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), color)

    fun say(s: String, x: Float, y: Float, size: Float, color: Int = CREAM, align: Paint.Align = Paint.Align.LEFT, shadow: Int = SHADOW) {
        val t = if (pool.size > texts.size) pool[texts.size] else TextCmd().also { pool += it }
        t.s = s; t.x = x + ox; t.y = y + oy; t.size = size; t.color = color; t.align = align; t.shadow = shadow
        texts += t
    }

    fun textWidth(s: String, size: Float): Float {
        measure.textSize = size
        return measure.measureText(s)
    }

    fun wrap(text: String, size: Float, maxW: Float): List<String> {
        val out = ArrayList<String>()
        var line = ""
        for (word in text.split(' ')) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (textWidth(candidate, size) > maxW && line.isNotEmpty()) { out += line; line = word } else line = candidate
        }
        if (line.isNotEmpty()) out += line
        return out
    }

    fun box(h: Hit, fillColor: Int, hi: Int, shadow: Boolean = true) = box(h.x.toFloat(), h.y.toFloat(), h.w.toFloat(), h.h.toFloat(), fillColor, hi, shadow)

    fun box(x: Float, y: Float, w: Float, h: Float, fillColor: Int, hi: Int, shadow: Boolean = true) {
        if (shadow) rect(x + 2, y + 2, w, h, SHADOW)
        rect(x - 1, y, w + 2, h, INK); rect(x, y - 1, w, h + 2, INK)
        rect(x, y, w, h, fillColor); rect(x, y, w, 1, hi)
    }

    fun button(h: Hit, label: String, primary: Boolean) {
        box(h, if (primary) RED_BTN else PLUM, if (primary) RED_BTN_HI else PLUM_HI)
        say(label, h.x + h.w / 2f, h.y + h.h / 2f + 0.5f, if (h.h >= 16) 7f else 6f, CREAM, Paint.Align.CENTER)
    }
}

/** Base for the painters: forwards the pixel primitives so drawing code reads like plain calls. */
abstract class Painter(protected val px: Pixels) {
    protected val lc get() = px.lc
    protected val dst get() = px.dst
    protected val blit get() = px.blit
    protected fun rect(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int) = px.rect(c, x, y, w, h, color)
    protected fun rect(x: Number, y: Number, w: Number, h: Number, color: Int) = px.rect(x, y, w, h, color)
    protected fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) = px.rect(x, y, w, h, color)
    protected fun rect(x: Float, y: Float, w: Int, h: Int, color: Int) = px.rect(x, y, w, h, color)
    protected fun rect(x: Int, y: Int, w: Int, h: Int, color: Int) = px.rect(x, y, w, h, color)
    protected fun say(s: String, x: Float, y: Float, size: Float, color: Int = CREAM, align: Paint.Align = Paint.Align.LEFT, shadow: Int = SHADOW) =
        px.say(s, x, y, size, color, align, shadow)
    protected fun textWidth(s: String, size: Float) = px.textWidth(s, size)
    protected fun wrap(text: String, size: Float, maxW: Float) = px.wrap(text, size, maxW)
    protected fun box(h: Hit, fillColor: Int, hi: Int, shadow: Boolean = true) = px.box(h, fillColor, hi, shadow)
    protected fun box(x: Float, y: Float, w: Float, h: Float, fillColor: Int, hi: Int, shadow: Boolean = true) = px.box(x, y, w, h, fillColor, hi, shadow)
    protected fun button(h: Hit, label: String, primary: Boolean) = px.button(h, label, primary)
}
