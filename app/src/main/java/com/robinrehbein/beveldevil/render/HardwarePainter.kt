package com.robinrehbein.beveldevil.render

import android.graphics.Color
import com.robinrehbein.beveldevil.game.Blower
import com.robinrehbein.beveldevil.game.Circuit
import com.robinrehbein.beveldevil.game.Dir
import com.robinrehbein.beveldevil.game.Group
import com.robinrehbein.beveldevil.game.Hardware
import com.robinrehbein.beveldevil.game.Heater
import com.robinrehbein.beveldevil.game.Sink
import com.robinrehbein.beveldevil.game.Switch
import com.robinrehbein.beveldevil.game.World
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The hardware mechanics of World 3: copper rails and live traces, pressure pads, hot plates, heatsinks and fans,
 * in the playfield's pixels. Copper, glow and air are bright on dark, so they read on a green and a blue board
 * alike; danger is red, as everywhere in the game.
 */
class HardwarePainter(px: Pixels) : Painter(px) {
    /** Drop shadows of the powered rails, under all tiles like the baked shadow layer. */
    fun shadow(w: World) {
        for (c in w.circuits.values) {
            if (c.trace || !c.powered) continue
            for (p in c.group.pieces) if (!p.spike) rect((p.box.x * TS).roundToInt() + 2, (p.box.y * TS).roundToInt() + 2, TS, TS, SHADOW)
        }
    }

    /** Over the tiles, under the hero. */
    fun under(w: World, t: Float) {
        var i = 0
        for (c in w.circuits.values) {
            val accent = CIRCUIT[i++ % CIRCUIT.size]
            if (c.trace) trace(c, w.time, t) else if (c.powered) rail(c, accent, w.time, t) else dark(c, accent, w.time)
        }
        for (s in w.sinks) sink(s, t)
        for (h in w.heaters.values) heat(h, t)
        for (s in w.pads) pad(s, w)
        for (k in 0 until w.fans.size) fan(w.fans[k], k)
    }

    private fun accentOf(w: World, id: Char): Int {
        var i = 0
        for (c in w.circuits.values) { if (c.id == id) return CIRCUIT[i % CIRCUIT.size]; i++ }
        return COPPER_HI
    }

    private fun has(g: Group, x: Int, y: Int) = g.pieces.any { it.hx.toInt() == x && it.hy.toInt() == y }

    // ---------- circuits ----------

    /** A powered rail: a bevelled copper bar with vias, current running along its top and a warm glow above. */
    private fun rail(c: Circuit, accent: Int, now: Float, t: Float) {
        val g = c.group
        // a clocked rail flickers red, ever faster, before the power drops
        val alarm = c.warn > 0f && floor(c.warn * (4f + 8f * c.warn)).toInt() and 1 == 1
        val edge = if (alarm) DANGER else INK
        val flash = now - c.flipTime
        val run = (t * 24f).toInt()
        val glow = alpha(COPPER_HI, 0.2f + 0.08f * sin(t * 3f))
        for (p in g.pieces) {
            if (p.spike) continue
            val hx = p.hx.toInt()
            val hy = p.hy.toInt()
            val x = (p.box.x * TS).roundToInt()
            val y = (p.box.y * TS).roundToInt()
            val l = !has(g, hx - 1, hy)
            val r = !has(g, hx + 1, hy)
            val u = !has(g, hx, hy - 1)
            val d = !has(g, hx, hy + 1)
            rect(x, y, TS, TS, COPPER)
            if (l) rect(x + 1, y, 1, TS, COPPER_HI)
            if (r) rect(x + TS - 2, y, 1, TS, COPPER_LO)
            if (u) rect(x, y + 1, TS, 1, COPPER_HI)
            if (d) rect(x, y + TS - 2, TS, 1, COPPER_LO)
            if (l) rect(x, y, 1, TS, edge)
            if (r) rect(x + TS - 1, y, 1, TS, edge)
            if (u) { rect(x, y, TS, 1, edge); rect(x, y - 1, TS, 1, glow) }
            if (d) rect(x, y + TS - 1, TS, 1, edge)
            when {
                // the ends carry the circuit's color, to match its pads
                l || r -> { rect(x + 2, y + 2, 4, 4, COPPER_LO); rect(x + 3, y + 3, 2, 2, accent) }
                (hx + hy) and 1 == 0 -> { rect(x + 2, y + 3, 4, 2, COPPER_LO); rect(x + 3, y + 2, 2, 4, COPPER_LO); rect(x + 3, y + 3, 2, 2, VIA) }
            }
            if (u) {
                val k = Math.floorMod(run - hx * TS, 16)
                if (k < TS) rect(x + k, y + 1, 1, 1, COPPER_SPARK)
            }
            if (flash in 0f..FLIP_FX) rect(x, y, TS, TS, alpha(WHITE, 0.6f * (1f - flash / FLIP_FX)))
        }
    }

    /** A dead rail: its outline in dashes, brighter just before the power returns. */
    private fun dark(c: Circuit, accent: Int, now: Float) {
        val g = c.group
        val flash = now - c.flipTime
        val color = when {
            flash in 0f..FLIP_FX -> if ((flash * 30f).toInt() and 1 == 0) WHITE else DANGER
            c.soon -> COPPER_HI
            else -> COPPER_DIM
        }
        for (p in g.pieces) {
            if (p.spike) continue
            val hx = p.hx.toInt()
            val hy = p.hy.toInt()
            val x = (p.box.x * TS).roundToInt()
            val y = (p.box.y * TS).roundToInt()
            for (k in 0 until TS) {
                if ((x + y + k) and 1 == 1) continue
                if (!has(g, hx, hy - 1)) rect(x + k, y, 1, 1, color)
                if (!has(g, hx, hy + 1)) rect(x + k, y + TS - 1, 1, 1, color)
                if (!has(g, hx - 1, hy)) rect(x, y + k, 1, 1, color)
                if (!has(g, hx + 1, hy)) rect(x + TS - 1, y + k, 1, 1, color)
            }
            if (!has(g, hx - 1, hy) || !has(g, hx + 1, hy)) rect(x + 3, y + 3, 2, 2, alpha(accent, 0.35f))
        }
    }

    /** An exposed trace: a copper line joining its tiles. Live, it glows red and crackles with sparks. */
    private fun trace(c: Circuit, now: Float, t: Float) {
        val g = c.group
        val live = c.powered
        val core = if (live) COPPER_HI else COPPER_LO
        val side = if (live) DANGER else COPPER_DK
        val tick = (t * 30f).toInt()
        val flash = now - c.flipTime
        for (p in g.pieces) {
            val hx = p.hx.toInt()
            val hy = p.hy.toInt()
            val x = (p.box.x * TS).roundToInt()
            val y = (p.box.y * TS).roundToInt()
            val l = has(g, hx - 1, hy)
            val r = has(g, hx + 1, hy)
            val u = has(g, hx, hy - 1)
            val d = has(g, hx, hy + 1)
            val across = l || r || !(u || d)
            if (across) {
                val x0 = if (l) x else x + 1
                val x1 = if (r) x + TS else x + TS - 1
                rect(x0, y + 2, x1 - x0, 1, side); rect(x0, y + 5, x1 - x0, 1, side)
                rect(x0, y + 3, x1 - x0, 2, core)
                if (live) { rect(x0, y + 1, x1 - x0, 1, alpha(DANGER, 0.3f)); rect(x0, y + 6, x1 - x0, 1, alpha(DANGER, 0.3f)) }
            }
            if (u || d) {
                val y0 = if (u) y else y + 1
                val y1 = if (d) y + TS else y + TS - 1
                rect(x + 2, y0, 1, y1 - y0, side); rect(x + 5, y0, 1, y1 - y0, side)
                rect(x + 3, y0, 2, y1 - y0, core)
                if (live) { rect(x + 1, y0, 1, y1 - y0, alpha(DANGER, 0.3f)); rect(x + 6, y0, 1, y1 - y0, alpha(DANGER, 0.3f)) }
            }
            // a solder pad where the trace ends
            if ((if (l) 1 else 0) + (if (r) 1 else 0) + (if (u) 1 else 0) + (if (d) 1 else 0) <= 1) {
                rect(x + 1, y + 1, 6, 6, side)
                rect(x + 2, y + 2, 4, 4, core)
                rect(x + 3, y + 3, 2, 2, VIA)
            }
            if (!live) continue
            // current crawling along, and now and then an arc jumping off
            val k = Math.floorMod(tick + hx * 3 + hy * 5, TS)
            if (across) rect(x + k, y + 3 + (k and 1), 1, 1, WHITE) else rect(x + 3 + (k and 1), y + k, 1, 1, WHITE)
            val h = Masonry.hash(hx * 7 + tick / 3, hy * 13)
            if (h and 7 == 0) {
                val ax = x + 1 + (h ushr 4 and 5)
                val ay = if (across) y else y + 1 + (h ushr 8 and 5)
                rect(ax, ay + 1, 1, 1, DANGER_HOT); rect(ax + 1, ay, 1, 1, WHITE); rect(ax + 2, ay + 1, 1, 1, DANGER_HOT)
            }
            if (flash in 0f..FLIP_FX) rect(x, y, TS, TS, alpha(WHITE, 0.5f * (1f - flash / FLIP_FX)))
        }
    }

    /** A pressure pad: a colored cap on a copper plate; it sinks while pressed. The LED shows its first circuit's power. */
    private fun pad(s: Switch, w: World) {
        val x = s.col * TS
        val y = s.row * TS
        val first = s.pad.circuits.firstOrNull()
        val cap = if (first == null) COPPER_HI else accentOf(w, first)
        val lit = first != null && w.circuits[first]?.powered == true
        rect(x + 3, y + TS - 1, TS - 1, 1, SHADOW)
        rect(x, y + 6, TS, 2, INK)
        rect(x + 1, y + 6, TS - 2, 1, COPPER_LO)
        rect(x + 1, y + 7, TS - 2, 1, COPPER_DK)
        rect(x + 1, y + 7, 1, 1, if (lit) LED_ON else LED_OFF)
        if (s.down) {
            rect(x + 1, y + 4, 6, 2, INK)
            rect(x + 2, y + 5, 4, 1, cap)
        } else {
            rect(x + 1, y + 2, 6, 4, INK)
            rect(x + 2, y + 3, 4, 3, cap)
            rect(x + 2, y + 3, 4, 1, mix(cap, WHITE, 0.5f))
            rect(x + 5, y + 4, 1, 2, mix(cap, INK, 0.35f))
        }
    }

    // ---------- heat ----------

    /**
     * A heated group: a heating coil etched into its tiles (dim copper while cold, so a declared hot plate is known
     * at a glance) that glows orange, yellow and white as it heats, tints the tiles, shimmers the air above and
     * flickers red when it is about to burn. A plate that is only ever spiked shows nothing until it gets warm.
     */
    private fun heat(h: Heater, t: Float) {
        val g = h.group
        if (h.melted || !g.visible) return
        val v = h.heat
        if (!h.declared && v <= 0f) return
        val line = if (v <= 0f) COPPER_DIM else ramp(v)
        val alarm = v >= Hardware.HOT && (t * 14f).toInt() and 1 == 1
        // a declared plate is a dark ceramic slab that glows through; a spiked floor keeps its look under a hot tint
        val body = if (h.declared) mix(PLATE, mix(EMBER_RED, HEAT_ORANGE, v), 0.6f * v) else alpha(mix(EMBER_RED, HEAT_ORANGE, v), 0.2f + 0.55f * v)
        for (p in g.pieces) {
            if (p.spike) continue
            val hx = p.hx.toInt()
            val hy = p.hy.toInt()
            val x = (p.box.x * TS).roundToInt()
            val y = (p.box.y * TS).roundToInt()
            if (h.declared) {
                rect(x, y, TS, TS, body)
                if (!has(g, hx, hy - 1)) { rect(x, y, TS, 1, INK); rect(x, y + 1, TS, 1, mix(body, WHITE, 0.2f)) }
                if (!has(g, hx, hy + 1)) rect(x, y + TS - 1, TS, 1, INK)
                if (!has(g, hx - 1, hy)) rect(x, y, 1, TS, INK)
                if (!has(g, hx + 1, hy)) rect(x + TS - 1, y, 1, TS, INK)
            } else if (v > 0.02f) rect(x, y, TS, TS, body)
            // a meander: two runs per tile, joined on alternating ends from tile to tile
            rect(x + 1, y + 2, TS - 2, 1, line)
            rect(x + 1, y + 5, TS - 2, 1, line)
            if (has(g, hx - 1, hy)) { rect(x, y + 2, 1, 1, line); rect(x, y + 5, 1, 1, line) }
            if (has(g, hx + 1, hy)) { rect(x + TS - 1, y + 2, 1, 1, line); rect(x + TS - 1, y + 5, 1, 1, line) }
            rect(if (hx and 1 == 0) x + TS - 2 else x + 1, y + 3, 1, 2, line)
            val top = !has(g, hx, hy - 1)
            if (!top) continue
            if (alarm) rect(x, y, TS, 1, DANGER)
            if (v > 0.3f) {
                // heat haze rising off the top
                for (k in 0..1) {
                    val ph = frac(t * (0.7f + 0.8f * v) + ((Masonry.hash(hx, k) ushr 3) and 255) / 256f)
                    val hx2 = x + 1 + Math.floorMod(hx * 3 + k * 4, 6) + (sin(t * 5f + k * 2f + hx) * 0.9f).roundToInt()
                    rect(hx2, y - 1 - (ph * 6f).toInt(), 1, 1, alpha(line, (1f - ph) * v))
                }
            }
            if (h.sinking && v > 0f) rect(x + Math.floorMod((t * 20f).toInt() + hx * 3, TS), y + 1, 1, 1, COOL)
        }
    }

    /** A heatsink: aluminium fins; stood on, cold sparkles rise from it. */
    private fun sink(s: Sink, t: Float) {
        val g = s.group
        if (!g.visible) return
        for (p in g.pieces) {
            if (p.spike) continue
            val hx = p.hx.toInt()
            val hy = p.hy.toInt()
            val x = (p.box.x * TS).roundToInt()
            val y = (p.box.y * TS).roundToInt()
            val top = !has(g, hx, hy - 1)
            rect(x, y, TS, TS, FIN_LO)
            for (k in 0 until TS step 2) {
                rect(x + k, y, 1, TS - 2, FIN)
                if (top) rect(x + k, y, 1, 1, FIN_HI)
            }
            rect(x, y + TS - 2, TS, 2, FIN)
            if (!has(g, hx, hy + 1)) rect(x, y + TS - 1, TS, 1, INK)
            if (!has(g, hx - 1, hy)) rect(x, y, 1, TS, INK)
            if (!has(g, hx + 1, hy)) rect(x + TS - 1, y, 1, TS, INK)
            if (top) rect(x, y, TS, 1, if (s.active) COOL else FIN_HI)
            if (s.active && top) for (k in 0..1) {
                val ph = frac(t * 1.4f + k * 0.5f + hx * 0.37f)
                rect(x + 1 + Math.floorMod(hx * 5 + k * 3, 6), y - 1 - (ph * 7f).toInt(), 1, 1, alpha(COOL, 1f - ph))
            }
        }
    }

    // ---------- fans ----------

    private fun fan(f: Blower, index: Int) {
        val a = f.fan
        for (i in 0 until a.width) {
            val col = a.at.first + if (a.vertical) i else 0
            val row = a.at.second + if (a.vertical) 0 else i
            rotor(col * TS, row * TS, f.angle + i * 0.9f, f.wind, a.dir)
        }
        air(f, index)
    }

    /** A square fan housing with screws, a grille on its blowing face and a three-bladed rotor that blurs at speed. */
    private fun rotor(x: Int, y: Int, angle: Float, wind: Float, dir: Dir) {
        rect(x, y, TS, TS, INK)
        rect(x + 1, y + 1, TS - 2, TS - 2, FAN_FRAME)
        rect(x + 1, y + 1, 1, 1, FAN_RIM); rect(x + TS - 2, y + 1, 1, 1, FAN_RIM)
        rect(x + 1, y + TS - 2, 1, 1, FAN_RIM); rect(x + TS - 2, y + TS - 2, 1, 1, FAN_RIM)
        val blur = abs(wind) > BLUR
        for (yy in 0 until 6) for (xx in 0 until 6) {
            val dx = xx - 2.5f
            val dy = yy - 2.5f
            val d = sqrt(dx * dx + dy * dy)
            if (d > 3.1f) continue
            val sector = floor((atan2(dy, dx) + angle) / (PI.toFloat() / 3f)).toInt() and 1 == 0
            val c = when {
                d < 1f -> FAN_HUB
                blur -> if (sector && d > 2f) BLADE else BLADE_LO
                sector -> BLADE
                else -> FAN_HOLE
            }
            rect(x + 1 + xx, y + 1 + yy, 1, 1, c)
        }
        // grille bars on the side the air leaves
        when (dir) {
            Dir.UP -> for (k in 1 until TS - 1 step 2) rect(x + k, y, 1, 1, FAN_RIM)
            Dir.DOWN -> for (k in 1 until TS - 1 step 2) rect(x + k, y + TS - 1, 1, 1, FAN_RIM)
            Dir.LEFT -> for (k in 1 until TS - 1 step 2) rect(x, y + k, 1, 1, FAN_RIM)
            Dir.RIGHT -> for (k in 1 until TS - 1 step 2) rect(x + TS - 1, y + k, 1, 1, FAN_RIM)
        }
    }

    /** Air streaks racing through the zone at the wind's speed, trailing behind; toward the fan when it sucks. */
    private fun air(f: Blower, index: Int) {
        val a = f.fan
        val wind = f.wind
        if (wind == 0f) return
        val strength = min(1f, abs(wind) / Hardware.FULL)
        val ux = when (a.dir) { Dir.LEFT -> -1; Dir.RIGHT -> 1; else -> 0 }
        val uy = when (a.dir) { Dir.UP -> -1; Dir.DOWN -> 1; else -> 0 }
        // the face the air leaves from, in tiles
        val fx = when (a.dir) { Dir.RIGHT -> a.at.first + 1f; Dir.LEFT -> a.at.first.toFloat(); else -> a.at.first.toFloat() }
        val fy = when (a.dir) { Dir.DOWN -> a.at.second + 1f; Dir.UP -> a.at.second.toFloat(); else -> a.at.second.toFloat() }
        val reach = a.reach.toFloat()
        val lanes = a.width * 3
        val per = maxOf(1, a.reach / 4)
        val tail = 2 + min(3, (abs(wind) / 4f).toInt())
        val back = if (wind > 0f) -1 else 1
        for (lane in 0 until lanes) for (k in 0 until per) {
            val h = Masonry.hash(index * 31 + lane, k)
            val s = frac(f.travel / reach + ((h ushr 4) and 1023) / 1024f) * reach
            val across = (lane + 0.2f + ((h ushr 14) and 7) / 12f) / 3f
            val fade = min(1f, min(s, reach - s) / 1.2f)
            val c = alpha(AIR, 0.55f * strength * fade)
            val px = (fx + ux * s + (if (a.vertical) across else 0f)) * TS
            val py = (fy + uy * s + (if (a.vertical) 0f else across)) * TS
            for (j in 0 until tail) rect(px + ux * back * j, py + uy * back * j, 1f, 1f, if (j == 0) c else alpha(AIR, 0.3f * strength * fade))
        }
    }

    private companion object {
        const val FLIP_FX = 0.18f
        /** Wind speed from which the rotor is a blur. */
        const val BLUR = 7f

        val CIRCUIT = intArrayOf(0xFF4FE3FF.toInt(), 0xFFFF5FD2.toInt(), 0xFFB6FF4F.toInt(), 0xFFB89CFF.toInt())
        const val COPPER = 0xFFC88A3A.toInt()
        const val COPPER_HI = 0xFFF2C06A.toInt()
        const val COPPER_LO = 0xFF8A5424.toInt()
        const val COPPER_DK = 0xFF4E2E14.toInt()
        const val COPPER_DIM = 0xFF9A6A38.toInt()
        const val COPPER_SPARK = 0xFFFFF1C4.toInt()
        const val VIA = 0xFF140C06.toInt()
        const val DANGER = 0xFFFF3A3A.toInt()
        const val DANGER_HOT = 0xFFFFA0A0.toInt()
        const val LED_ON = 0xFF7CFF9A.toInt()
        const val LED_OFF = 0xFF5A1A1A.toInt()
        const val EMBER_RED = 0xFFB0301C.toInt()
        const val PLATE = 0xFF33201A.toInt()
        const val HEAT_ORANGE = 0xFFFF9A2A.toInt()
        const val COOL = 0xFF8FE8FF.toInt()
        const val FIN = 0xFF8C9AAC.toInt()
        const val FIN_HI = 0xFFD6E0EA.toInt()
        const val FIN_LO = 0xFF4A5566.toInt()
        const val FAN_FRAME = 0xFF2B2F38.toInt()
        const val FAN_RIM = 0xFF6A7282.toInt()
        const val FAN_HUB = 0xFFD9DBE6.toInt()
        const val FAN_HOLE = 0xFF0B0D12.toInt()
        const val BLADE = 0xFFA8B2C4.toInt()
        const val BLADE_LO = 0xFF5C6578.toInt()
        const val AIR = 0xFFE6FAFF.toInt()

        /** Heat colors: copper, orange, yellow, white. */
        private val RAMP = intArrayOf(0xFFC88A3A.toInt(), 0xFFFF7A1E.toInt(), 0xFFFFC43A.toInt(), 0xFFFFF4D0.toInt())

        fun ramp(v: Float): Int {
            val f = v.coerceIn(0f, 1f) * (RAMP.size - 1)
            val i = min(RAMP.size - 2, f.toInt())
            return mix(RAMP[i], RAMP[i + 1], f - i)
        }

        fun frac(v: Float) = v - floor(v)

        fun alpha(c: Int, a: Float) = (c and 0xFFFFFF) or ((Color.alpha(c) * a.coerceIn(0f, 1f)).toInt() shl 24)

        fun mix(a: Int, b: Int, f: Float): Int = Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * f).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * f).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * f).toInt(),
        )
    }
}
