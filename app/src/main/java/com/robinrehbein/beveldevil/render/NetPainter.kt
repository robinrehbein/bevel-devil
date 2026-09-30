package com.robinrehbein.beveldevil.render

import android.graphics.Color
import com.robinrehbein.beveldevil.game.Action
import com.robinrehbein.beveldevil.game.Beam
import com.robinrehbein.beveldevil.game.Dir
import com.robinrehbein.beveldevil.game.Group
import com.robinrehbein.beveldevil.game.Link
import com.robinrehbein.beveldevil.game.World
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/** The network mechanics of World 2: conveyor belts, firewall lasers and portals, in the playfield's pixels. */
class NetPainter(px: Pixels) : Painter(px) {
    /** Over the tiles, under the hero. */
    fun under(w: World, t: Float) {
        for (g in w.groups.values) if (g.belt != null && g.visible) belt(g)
        for (i in 0 until w.beams.size) beam(w.beams[i], t)
        for (i in 0 until w.links.size) portal(w.links[i], i, w.time, t)
    }

    /** Over the hero: the flash of a hop and a packet trail between the two ends. */
    fun over(w: World) {
        for (i in 0 until w.links.size) {
            val l = w.links[i]
            val a = w.time - l.hopTime
            if (a !in 0f..HOP_FX) continue
            val f = a / HOP_FX
            val c = PORTAL[i % PORTAL.size]
            val (ax, ay) = l.hopA
            val (bx, by) = l.hopB
            val x0 = ax * TS + 4f; val y0 = ay * TS + 3f; val x1 = bx * TS + 4f; val y1 = by * TS + 3f
            val n = max(1, (hypot(x1 - x0, y1 - y0) / 3f).toInt())
            // dashes race from the entry to the exit
            for (k in 0..n) {
                val s = k / n.toFloat()
                if (s > f * 1.6f || s < f * 1.6f - 0.5f) continue
                rect(x0 + (x1 - x0) * s, y0 + (y1 - y0) * s, 1f, 1f, alpha(if (k % 3 == 0) WHITE else c, 1f - f))
            }
            burst(ax, ay, f, c)
            burst(bx, by, f * 0.8f, c)
        }
    }

    // ---------- belts ----------

    private fun has(g: Group, x: Int, y: Int) = g.pieces.any { !it.spike && it.hx.toInt() == x && it.hy.toInt() == y }

    private fun belt(g: Group) {
        val speed = g.beltSpeed
        val run = (g.beltRun * TS).roundToInt()
        val led = if (speed == 0f) LED_DIM else LED
        for (p in g.pieces) {
            if (p.spike) continue
            val hx = p.hx.toInt()
            val hy = p.hy.toInt()
            val x = (p.box.x * TS).roundToInt()
            val y = (p.box.y * TS).roundToInt()
            val left = !has(g, hx - 1, hy)
            val right = !has(g, hx + 1, hy)
            if (has(g, hx, hy - 1)) { rect(x, y, TS, TS, BELT_FRAME); rect(x, y + 3, TS, 1, BELT_FRAME_LO); continue }
            // housing with two rollers below the band
            rect(x, y, TS, TS, BELT_FRAME)
            rect(x, y + TS - 1, TS, 1, INK)
            for (r in 0..1) {
                val rx = x + 1 + r * 4
                rect(rx, y + 4, 3, 3, BELT_FRAME_LO)
                rect(rx + 1, y + 4, 1, 3, STEEL_LO); rect(rx, y + 5, 3, 1, STEEL_LO)
                // a bolt that turns with the belt
                val k = Math.floorMod(run / 2, 4)
                rect(rx + ROLL_X[k], y + 4 + ROLL_Y[k], 1, 1, STEEL)
            }
            // the band: dark rubber with LED chevrons running along
            rect(x, y, TS, 4, BELT)
            rect(x, y, TS, 1, BELT_HI)
            for (i in -1..2) {
                val cx = x + i * 4 + Math.floorMod(run, 4)
                for (k in 0..2) {
                    val dx = if (speed < 0f) (if (k == 1) 0 else 1) else (if (k == 1) 1 else 0)
                    val xx = cx + dx
                    if (xx in x until x + TS) rect(xx, y + 1 + k, 1, 1, led)
                }
            }
            if (left) { rect(x, y, 1, TS, INK); rect(x + 1, y + 1, 1, 2, BELT_HI) }
            if (right) rect(x + TS - 1, y, 1, TS, INK)
        }
    }

    // ---------- lasers ----------

    private fun beam(b: Beam, t: Float) {
        val l = b.laser
        val warn = b.warn
        val flick = warn > 0f && floor(warn * (4f + 10f * warn)).toInt() and 1 == 1
        val lens = when {
            b.lit -> LASER_CORE
            flick -> LASER_HOT
            warn > 0f -> LASER
            else -> LASER_IDLE
        }
        val lo = if (l.vertical) l.from.second <= l.to.second else l.from.first <= l.to.first
        val (a, z) = if (lo) l.from to l.to else l.to to l.from
        val glow = if (b.lit) 1f else warn
        emitter(a, if (l.vertical) Dir.UP else Dir.LEFT, lens, glow)
        emitter(z, if (l.vertical) Dir.DOWN else Dir.RIGHT, lens, glow)
        val x0 = (l.x0 * TS).roundToInt()
        val y0 = (l.y0 * TS).roundToInt()
        val len = if (l.vertical) ((l.y1 - l.y0) * TS).roundToInt() else ((l.x1 - l.x0) * TS).roundToInt()
        fun dot(s: Int, across: Int, c: Int) = if (l.vertical) rect(x0 + across, y0 + s, 1, 1, c) else rect(x0 + s, y0 + across, 1, 1, c)
        when {
            b.lit -> {
                val move = (t * 40f).toInt()
                for (s in 0 until len) {
                    dot(s, -1, LASER_GLOW)
                    dot(s, 0, LASER)
                    dot(s, 1, if ((s + move) % 5 == 0) WHITE else LASER_CORE)
                    dot(s, 2, LASER)
                    dot(s, 3, LASER_GLOW)
                }
            }
            warn > 0f -> {
                // aiming: a dotted line that fills in as it charges
                val gap = if (warn > 0.66f) 2 else if (warn > 0.33f) 3 else 4
                val move = (t * 24f).toInt()
                for (s in 0 until len) if ((s + move) % gap == 0) dot(s, 1, alpha(LASER, 0.35f + 0.5f * warn))
            }
            else -> for (s in 2 until len - 1 step 4) dot(s, 1, LASER_TRACE)
        }
    }

    /** An emitter on the [side] of tile [c] (the wall it hangs on), lens facing the beam. */
    private fun emitter(c: Pair<Int, Int>, side: Dir, lens: Int, glow: Float) {
        val cx = c.first * TS
        val cy = c.second * TS
        // [a] counts from the wall into the tile, [b] across; [la] × [lb] in those axes
        fun r(a: Int, b: Int, la: Int, lb: Int, color: Int) = when (side) {
            Dir.UP -> rect(cx + b, cy + a, lb, la, color)
            Dir.DOWN -> rect(cx + b, cy + TS - a - la, lb, la, color)
            Dir.LEFT -> rect(cx + a, cy + b, la, lb, color)
            Dir.RIGHT -> rect(cx + TS - a - la, cy + b, la, lb, color)
        }
        r(0, 0, 1, 8, INK)
        r(0, 1, 1, 6, STEEL_LO)
        r(1, 0, 3, 8, INK)
        r(1, 1, 3, 6, EMITTER)
        r(1, 1, 1, 6, EMITTER_HI)
        r(2, 1, 1, 1, lens); r(2, 6, 1, 1, lens)
        r(4, 2, 1, 4, INK)
        r(4, 3, 1, 2, lens)
        if (glow > 0f) {
            // a hot halo around the lens that grows as it charges
            val a = 0.35f + 0.5f * glow
            r(5, 2, 1, 4, alpha(LASER, a * 0.6f))
            r(4, 1, 1, 1, alpha(LASER, a)); r(4, 6, 1, 1, alpha(LASER, a))
            if (glow > 0.5f) r(6, 3, 1, 2, alpha(LASER_HOT, a * 0.5f))
        }
    }

    // ---------- portals ----------

    private fun portal(l: Link, i: Int, now: Float, t: Float) {
        val c = PORTAL[i % PORTAL.size]
        val re = now - l.rerouteTime
        if (re in 0f..REROUTE_FX && l.oldTo != l.to) {
            // the old exit breaks up
            val (ox, oy) = l.oldTo
            ring(ox, oy, if ((re * 20f).toInt() and 1 == 0) alpha(c, 1f - re / REROUTE_FX) else 0, 0, t, dashed = true)
        }
        ring(l.from.first, l.from.second, if (l.on) c else PORTAL_OFF, if (l.on) 1 else 0, t, dashed = false)
        ring(l.to.first, l.to.second, if (l.on) c else PORTAL_OFF, if (!l.on) 0 else if (l.twoWay) 1 else -1, t, dashed = false)
        if (re in 0f..REROUTE_FX) burst(l.to.first, l.to.second, re / REROUTE_FX, c)
    }

    /** An upright oval gate standing in tile ([col], [row]), a bit wider and taller than the tile. [swirl]: 1 in, -1 out only, 0 dark. */
    private fun ring(col: Int, row: Int, color: Int, swirl: Int, t: Float, dashed: Boolean) {
        if (color == 0) return
        val x = col * TS - 1
        val y = row * TS + TS - RING_H
        val dark = mix(color, INK, 0.55f)
        for (yy in 0 until RING_H) for (xx in 0 until RING_W) {
            val d = RING[yy * RING_W + xx]
            if (d == 0) continue
            if (dashed && (xx + yy) and 1 == 0) continue
            val cc = when (d) {
                3 -> INK
                2 -> if (xx + yy < 9) mix(color, WHITE, 0.45f) else color
                else -> if (dashed) 0 else PORTAL_HOLE
            }
            if (cc != 0) rect(x + xx, y + yy, 1, 1, cc)
            if (d == 2 && xx >= 6 && yy >= 7) rect(x + xx, y + yy, 1, 1, dark)
        }
        if (dashed || swirl == 0) return
        // two sparks circle inside; an exit-only gate shows a steady glow instead
        if (swirl < 0) { rect(x + 4, y + 5, 2, 4, alpha(color, 0.45f)); return }
        for (k in 0..1) {
            val a = t * 5f + k * Math.PI.toFloat()
            rect(x + 4.5f + 1.8f * sin(a), y + 6.5f + 3.2f * sin(a + 1.57f), 1f, 1f, if (k == 0) WHITE else color)
        }
        rect(x + 4, y + 5, 2, 3, alpha(color, 0.25f + 0.2f * sin(t * 7f)))
    }

    /** A widening ring of light around a gate, [f] 0 → 1. */
    private fun burst(col: Int, row: Int, f: Float, color: Int) {
        if (f !in 0f..1f) return
        val cx = col * TS + 4f
        val cy = row * TS + 3f
        val r = 3f + 9f * f
        val c = alpha(if (f < 0.3f) WHITE else color, 1f - f)
        for (k in 0 until 14) {
            val a = k * (2 * Math.PI.toFloat() / 14f)
            rect(cx + kotlin.math.cos(a) * r, cy + sin(a) * r * 1.2f, 1f, 1f, c)
        }
        if (f < 0.25f) rect(col * TS + 1, row * TS + TS - RING_H + 2, TS - 2, RING_H - 4, alpha(WHITE, 0.6f * (1f - f / 0.25f)))
    }

    private companion object {
        const val HOP_FX = 0.32f
        const val REROUTE_FX = 0.6f
        const val RING_W = 10
        const val RING_H = 13

        /** Oval gate: 3 outline, 2 ring, 1 hole. */
        val RING: IntArray = IntArray(RING_H * RING_W).also { out ->
            for (y in 0 until RING_H) for (x in 0 until RING_W) {
                val dx = (x + 0.5f - RING_W / 2f) / (RING_W / 2f)
                val dy = (y + 0.5f - RING_H / 2f) / (RING_H / 2f)
                val d = dx * dx + dy * dy
                out[y * RING_W + x] = when {
                    d > 1.12f -> 0
                    d > 0.86f -> 3
                    d > 0.42f -> 2
                    else -> 1
                }
            }
        }

        val PORTAL = intArrayOf(0xFF4FE3FF.toInt(), 0xFFFF5FD2.toInt(), 0xFFB6FF4F.toInt(), 0xFFFFB84F.toInt())
        const val PORTAL_OFF = 0xFF5A5A6E.toInt()
        const val PORTAL_HOLE = 0xFF0A0614.toInt()
        const val BELT = 0xFF17141F.toInt()
        const val BELT_HI = 0xFF3A3548.toInt()
        const val BELT_FRAME = 0xFF3B4052.toInt()
        const val BELT_FRAME_LO = 0xFF22252F.toInt()
        const val LED = 0xFF6CFF8E.toInt()
        const val LED_DIM = 0xFF2E5A3C.toInt()
        const val EMITTER = 0xFF4A4F66.toInt()
        const val EMITTER_HI = 0xFF7A8098.toInt()
        const val LASER = 0xFFFF3A5A.toInt()
        const val LASER_HOT = 0xFFFFA0B0.toInt()
        const val LASER_CORE = 0xFFFFD6E0.toInt()
        const val LASER_GLOW = 0x60FF3A5A
        const val LASER_IDLE = 0xFF7A1A30.toInt()
        const val LASER_TRACE = 0x40FF3A5A
        val ROLL_X = intArrayOf(0, 2, 2, 0)
        val ROLL_Y = intArrayOf(0, 0, 2, 2)

        fun alpha(c: Int, a: Float) = (c and 0xFFFFFF) or ((Color.alpha(c) * a.coerceIn(0f, 1f)).toInt() shl 24)

        fun mix(a: Int, b: Int, f: Float): Int = Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * f).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * f).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * f).toInt(),
        )
    }
}
