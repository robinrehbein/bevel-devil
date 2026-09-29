package com.robinrehbein.beveldevil.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import com.robinrehbein.beveldevil.game.Game
import com.robinrehbein.beveldevil.game.Group
import com.robinrehbein.beveldevil.game.GroupMode
import com.robinrehbein.beveldevil.game.Level
import com.robinrehbein.beveldevil.game.Particle
import com.robinrehbein.beveldevil.game.World
import com.robinrehbein.beveldevil.game.WorldState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The level: hell rock around the playfield, a parallax backdrop, autotiled masonry, spikes, door,
 * saws, the hero and particles.
 *
 * Trap safety: every solid piece that rests at its home position (static or a group that has not
 * moved) is baked into one tile layer with one shadow layer, so a trap is pixel-identical to plain
 * level geometry until it fires. Only groups that are moving or displaced are drawn on their own.
 */
class WorldPainter(px: Pixels) : Painter(px) {
    private val tileBmp = Bitmap.createBitmap(PW, PH, Bitmap.Config.ARGB_8888)
    private val tilePx = IntArray(PW * PH)
    private val shadowBmp = Bitmap.createBitmap(PW + 2, PH + 2, Bitmap.Config.ARGB_8888)
    private val shadowC = Canvas(shadowBmp)
    private val frameShadow = Bitmap.createBitmap(PW + 2, PH + 2, Bitmap.Config.ARGB_8888)
    private val frameShadowC = Canvas(frameShadow)
    private val silhouette = Paint().apply { colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN) }
    private val shadowPaint = Paint().apply { alpha = Color.alpha(SHADOW) }
    private val glowPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD) }

    private var world: World? = null
    private var baked: World? = null
    private var groupsFor: Level? = null
    private val groups = ArrayList<Group>()
    private var groupBmp = arrayOfNulls<Bitmap>(0)
    private var groupCol = IntArray(0)
    private var groupRow = IntArray(0)
    private var resting = BooleanArray(0)
    private var cells = IntArray(0)

    private val backdrop by lazy { Fx.backdrop(PW + 2 * PARALLAX, PH, 15 * TS) }
    private val doorHalo by lazy { Fx.halo(26, 0xFFFF9A48.toInt(), 0x58, 0x30) }
    private val door by lazy { buildDoor() }
    private val doorFlipped by lazy { Bitmap.createBitmap(door, 0, 0, door.width, door.height, Matrix().apply { setScale(1f, -1f) }, false) }

    private var rockBmp: Bitmap? = null
    private var rockFor = IntArray(4)
    private val open = BooleanArray(2 * 32 + 2 * 18)
    private val rockOpen = BooleanArray(open.size)
    /** Glowing cracks in the rock: canvas x, y and phase per pixel. */
    private val cracks = IntArray(3 * 600)
    private var crackCount = 0

    fun draw(game: Game, l: Layout) {
        val w = game.world ?: return
        draw(w, game.time, game.heat, game.particles, l)
    }

    fun draw(w: World, t: Float, heat: Float, particles: List<Particle>, l: Layout) {
        surroundings(w, l, t, heat)
        px.at(l.fx, l.fy) { backdrop(w) }
        Fx.embers(px, l.lw, l.lh, t, heat)
        px.at(l.fx, l.fy) { drawWorld(w, t, particles) }
    }

    // ---------- surroundings ----------

    /** Rock everywhere outside the playfield, except where the level's edge is open: there the pit continues. */
    private fun surroundings(w: World, l: Layout, t: Float, heat: Float) {
        if (l.lw == PW && l.lh == PH) return
        val cols = w.cols
        val rows = w.rows
        open.fill(true)
        for (i in 0 until w.pieces.size) {
            val p = w.pieces[i]
            if (!p.visible) continue
            val cx = (p.box.x + 0.5f).toInt()
            val cy = (p.box.y + 0.5f).toInt()
            if (cx !in 0 until cols || cy !in 0 until rows) continue
            if (cy == 0) open[cx] = false
            if (cy == rows - 1) open[32 + cx] = false
            if (cx == 0) open[64 + cy] = false
            if (cx == cols - 1) open[82 + cy] = false
        }
        if (rockBmp == null || rockFor[0] != l.lw || rockFor[1] != l.lh || rockFor[2] != l.fx || rockFor[3] != l.fy || !open.contentEquals(rockOpen)) {
            buildRock(l, cols, rows)
            rockFor[0] = l.lw; rockFor[1] = l.lh; rockFor[2] = l.fx; rockFor[3] = l.fy
            open.copyInto(rockOpen)
        }
        lc.drawBitmap(rockBmp!!, 0f, 0f, null)
        for (i in 0 until crackCount) {
            val x = cracks[3 * i]
            val y = cracks[3 * i + 1]
            val v = sin(t * 2.3f + cracks[3 * i + 2] * 0.7f) + heat * 1.5f
            rect(x, y, 1, 1, if (v > 0.9f) CRACK_HOT else if (v > -0.2f) CRACK_WARM else EMBER)
        }
    }

    private fun isOpen(col: Int, row: Int, cols: Int, rows: Int): Boolean = when {
        row in 0 until rows && col < 0 -> open[64 + row]
        row in 0 until rows && col >= cols -> open[82 + row]
        col in 0 until cols && row < 0 -> open[col]
        col in 0 until cols && row >= rows -> open[32 + col]
        else -> false
    }

    private fun buildRock(l: Layout, cols: Int, rows: Int) {
        val bmp = rockBmp?.takeIf { it.width == l.lw && it.height == l.lh } ?: Bitmap.createBitmap(l.lw, l.lh, Bitmap.Config.ARGB_8888)
        val c0 = -((l.fx + TS - 1) / TS)
        val r0 = -((l.fy + TS - 1) / TS)
        val c1 = (l.lw - l.fx + TS - 1) / TS
        val r1 = (l.lh - l.fy + TS - 1) / TS
        val gw = c1 - c0
        val gh = r1 - r0
        val grid = IntArray(gw * gh)
        for (row in r0 until r1) for (col in c0 until c1) {
            grid[(row - r0) * gw + col - c0] = when {
                col in 0 until cols && row in 0 until rows -> 2
                isOpen(col, row, cols, rows) -> 0
                else -> 1
            }
        }
        val out = IntArray(l.lw * l.lh)
        val ox = l.fx + c0 * TS
        val oy = l.fy + r0 * TS
        Masonry.bake(out, l.lw, l.lh, ox, oy, grid, gw, gh, c0, r0, true, ROCK_STONE)
        crackCount = 0
        for (row in r0 until r1) for (col in c0 until c1) {
            val x = ox + (col - c0) * TS
            val y = oy + (row - r0) * TS
            val v = grid[(row - r0) * gw + col - c0]
            if (v == 0) {
                // the pit keeps going: the swirl shows through and darkens with depth
                val d = when {
                    col < 0 -> -col; col >= cols -> col - cols + 1
                    row < 0 -> -row; else -> row - rows + 1
                }
                val a = minOf(230, d * 70 - 20)
                for (yy in y until y + TS) for (xx in x until x + TS) {
                    if (xx in 0 until l.lw && yy in 0 until l.lh) out[yy * l.lw + xx] = Color.argb(if (bayer(xx, yy) < 0.5f) a else max(0, a - 40), 7, 3, 13)
                }
                continue
            }
            if (v != 1) continue
            val h = Masonry.hash(col * 5 + 11, row * 3 - 7)
            if (Math.floorMod(h, 10) != 0) continue
            // a jagged fissure; its pixels glow per frame
            var cx = x + 2 + (h ushr 4 and 3)
            var cy = y + 1
            val len = 4 + (h ushr 7 and 1)
            for (s in 0 until len) {
                if (cx in 0 until l.lw && cy in 0 until l.lh && crackCount < cracks.size / 3) {
                    out[cy * l.lw + cx] = ROCK_LO
                    cracks[3 * crackCount] = cx; cracks[3 * crackCount + 1] = cy; cracks[3 * crackCount + 2] = h ushr 9 and 15
                    crackCount++
                }
                cy++
                if ((h ushr (10 + s)) and 1 == 0) cx += if (h and 1 == 0) 1 else -1
            }
        }
        bmp.setPixels(out, 0, l.lw, 0, 0, l.lw, l.lh)
        rockBmp = bmp
    }

    // ---------- backdrop ----------

    private fun backdrop(w: World) {
        lc.save()
        lc.clipRect(0, 0, PW, PH)
        val shift = ((w.cols / 2f - w.player.box.cx) * 0.7f).roundToInt().coerceIn(-PARALLAX, PARALLAX)
        lc.drawBitmap(backdrop, (shift - PARALLAX).toFloat(), 0f, null)
        lc.restore()
    }

    // ---------- level layers ----------

    private fun rests(g: Group) = g.visible && g.mode == GroupMode.IDLE && g.ox == 0f && g.oy == 0f

    private fun newWorld(w: World) {
        world = w
        baked = null
        if (groupsFor === w.level && groups.size == w.groups.size) {
            // same level again (a restart): the group masses look the same, only the objects are new
            var i = 0
            for (g in w.groups.values) groups[i++] = g
        } else {
            groupsFor = w.level
            groups.clear()
            groups.addAll(w.groups.values)
            resting = BooleanArray(groups.size)
            groupCol = IntArray(groups.size)
            groupRow = IntArray(groups.size)
            groupBmp = Array(groups.size) { bakeGroup(it) }
        }
        if (cells.size != w.cols * w.rows) cells = IntArray(w.cols * w.rows)
    }

    /** A group on its own, as a mass of just its own pieces, for when it moves. */
    private fun bakeGroup(i: Int): Bitmap {
        val g = groups[i]
        var c0 = Int.MAX_VALUE; var r0 = Int.MAX_VALUE; var c1 = Int.MIN_VALUE; var r1 = Int.MIN_VALUE
        for (p in g.pieces) {
            c0 = min(c0, p.hx.toInt()); r0 = min(r0, p.hy.toInt()); c1 = max(c1, p.hx.toInt()); r1 = max(r1, p.hy.toInt())
        }
        val gw = c1 - c0 + 1
        val gh = r1 - r0 + 1
        val grid = IntArray(gw * gh)
        for (p in g.pieces) if (!p.spike) grid[(p.hy.toInt() - r0) * gw + p.hx.toInt() - c0] = 1
        val out = IntArray(gw * TS * gh * TS)
        Masonry.bake(out, gw * TS, gh * TS, 0, 0, grid, gw, gh, c0, r0, false, GOLD_STONE)
        for (p in g.pieces) if (p.spike) Masonry.spike(out, gw * TS, gh * TS, (p.hx.toInt() - c0) * TS, (p.hy.toInt() - r0) * TS, p.dir)
        groupCol[i] = c0
        groupRow[i] = r0
        return Bitmap.createBitmap(out, gw * TS, gh * TS, Bitmap.Config.ARGB_8888)
    }

    /** Bakes everything resting into the tile layer and its shadow. Runs on level start and when a trap fires. */
    private fun bakeLevel(w: World) {
        cells.fill(0)
        tilePx.fill(0)
        for (p in w.pieces) {
            if (p.spike || !restingPiece(p.group)) continue
            val c = p.hx.toInt()
            val r = p.hy.toInt()
            if (c in 0 until w.cols && r in 0 until w.rows) cells[r * w.cols + c] = 1
        }
        Masonry.bake(tilePx, PW, PH, 0, 0, cells, w.cols, w.rows, 0, 0, false, GOLD_STONE)
        for (p in w.pieces) if (p.spike && restingPiece(p.group)) Masonry.spike(tilePx, PW, PH, p.hx.toInt() * TS, p.hy.toInt() * TS, p.dir)
        tileBmp.setPixels(tilePx, 0, PW, 0, 0, PW, PH)
        shadowBmp.eraseColor(Color.TRANSPARENT)
        shadowC.drawBitmap(tileBmp, 2f, 2f, silhouette)
        baked = w
    }

    private fun restingPiece(g: Group?): Boolean = g == null || resting[groups.indexOf(g)]

    private fun groupX(i: Int, t: Float): Int {
        val g = groups[i]
        val jitter = if (g.mode == GroupMode.FALL) (sin(t * 60f) * 0.8f).roundToInt() else 0
        return ((groupCol[i] + g.ox) * TS).roundToInt() + jitter
    }

    private fun groupY(i: Int) = ((groupRow[i] + groups[i].oy) * TS).roundToInt()

    private fun drawWorld(w: World, t: Float, particles: List<Particle>) {
        if (world !== w) newWorld(w)
        var dirty = baked !== w
        var moving = false
        for (i in 0 until groups.size) {
            val r = rests(groups[i])
            if (r != resting[i]) { resting[i] = r; dirty = true }
            if (!r && groups[i].visible) moving = true
        }
        if (dirty) bakeLevel(w)

        lc.save()
        lc.clipRect(0, 0, PW, PH)
        drawHalo(w, t)
        lc.restore()
        // one shadow pass for everything solid, then all tiles on top
        val shadow = if (!moving) shadowBmp else {
            frameShadow.eraseColor(Color.TRANSPARENT)
            frameShadowC.drawBitmap(shadowBmp, 0f, 0f, null)
            for (i in 0 until groups.size) {
                if (resting[i] || !groups[i].visible) continue
                frameShadowC.drawBitmap(groupBmp[i]!!, groupX(i, t) + 2f, groupY(i) + 2f, silhouette)
            }
            frameShadow
        }
        lc.drawBitmap(shadow, 0f, 0f, shadowPaint)
        lc.save()
        lc.clipRect(0, 0, PW, PH)
        lc.drawBitmap(tileBmp, 0f, 0f, null)
        for (i in 0 until groups.size) {
            if (resting[i] || !groups[i].visible) continue
            lc.drawBitmap(groupBmp[i]!!, groupX(i, t).toFloat(), groupY(i).toFloat(), null)
        }
        spikeGleams(w, t)
        drawDoor(w, t)
        for (i in 0 until w.saws.size) { val s = w.saws[i]; drawSaw(s.x * TS, s.y * TS, s.r * TS, s.angle) }
        when (w.state) {
            WorldState.PLAYING -> drawPlayer(w)
            WorldState.DEAD -> deathFx(w)
            WorldState.WON -> { winFx(w); drawPlayer(w) }
        }
        drawParticles(particles)
        lc.restore()
    }

    /** A glint that sweeps along every visible spike, left to right. */
    private fun spikeGleams(w: World, t: Float) {
        for (i in 0 until w.pieces.size) {
            val p = w.pieces[i]
            if (!p.spike || !p.visible) continue
            val ph = frac(t * 0.4f - p.box.x * 0.03f - p.box.y * 0.011f)
            if (ph > 0.12f) continue
            val y = TS - 1 - (ph / 0.12f * 6.99f).toInt()
            val x = 4 - (y + 1) / 2 + 1
            val bx = (p.box.x * TS).roundToInt()
            val by = (p.box.y * TS).roundToInt()
            rect(bx + Masonry.spikeX(x, y, p.dir), by + Masonry.spikeY(x, y, p.dir), 1, 1, WHITE)
        }
    }

    // ---------- door ----------

    private fun buildDoor(): Bitmap {
        val w = 10
        val h = 13
        fun shape(x: Int, y: Int): Boolean {
            if (x !in 0 until w || y !in 0 until h) return false
            return !((y == 0 && (x <= 1 || x >= w - 2)) || (y == 1 && (x == 0 || x == w - 1)))
        }
        fun inside(x: Int, y: Int) = x in 2..7 && y in 3 until h && !(y == 3 && (x == 2 || x == 7))
        val out = IntArray((w + 2) * (h + 2))
        for (y in -1..h) for (x in -1..w) {
            val c = when {
                !shape(x, y) -> if (shape(x - 1, y) || shape(x + 1, y) || shape(x, y - 1) || shape(x, y + 1)) INK else 0
                inside(x, y) -> when {
                    x == 6 && y == 8 -> GOLD_HI
                    x == 6 && y == 9 -> GOLD_LO2
                    y == 3 || (y == 4 && (x == 2 || x == 7)) -> 0xFF120714.toInt()
                    else -> DOOR_DARK
                }
                x == 4 && y == 1 -> GOLD_SPARK
                x == 5 && y == 1 -> GOLD_HI
                !shape(x - 1, y) || !shape(x, y - 1) -> GOLD_HI
                !shape(x + 1, y) -> GOLD_LO2
                inside(x + 1, y) || inside(x, y + 1) -> GOLD_LO2
                inside(x - 1, y) -> GOLD_MID
                y == h - 1 -> GOLD_LO2
                else -> GOLD
            }
            out[(y + 1) * (w + 2) + x + 1] = c
        }
        return Bitmap.createBitmap(out, w + 2, h + 2, Bitmap.Config.ARGB_8888)
    }

    private fun doorX(w: World) = (w.door.box.x * TS).roundToInt() + 1
    private fun doorTop(w: World) = if (w.door.hanging) (w.door.box.y * TS).roundToInt() else (w.door.box.b * TS).roundToInt() - 13
    private fun winAge(w: World) = if (w.state == WorldState.WON) w.time - w.stateTime else -1f

    private fun drawHalo(w: World, t: Float) {
        val won = winAge(w)
        val a = if (won >= 0f) min(1f, 0.7f + won * 2f) else 0.62f + 0.18f * sin(t * 2.6f) + 0.06f * sin(t * 11f)
        glowPaint.alpha = (a * 255).toInt()
        val cx = doorX(w) + 5
        val cy = doorTop(w) + 7
        lc.drawBitmap(doorHalo, (cx - doorHalo.width / 2).toFloat(), (cy - doorHalo.height / 2).toFloat(), glowPaint)
    }

    private fun drawDoor(w: World, t: Float) {
        val flip = w.door.hanging
        val x = doorX(w)
        val top = doorTop(w)
        rect(x + 2, top + 2, 10, 13, SHADOW)
        lc.drawBitmap(if (flip) doorFlipped else door, x - 1f, top - 1f, null)
        val won = winAge(w)
        val glow = if (won >= 0f) min(1f, 0.5f + won * 3f) else 0.62f + 0.22f * sin(t * 5f)
        val warm = Color.argb((glow * 170).toInt(), 255, 150, 60)
        val hot = Color.argb((glow * 220).toInt(), 255, 214, 130)
        // light pools at the threshold and fades toward the arch
        val y0 = if (flip) top else top + 4
        rect(x + 2, y0, 6, 9, Color.argb((glow * 90).toInt(), 220, 80, 50))
        rect(x + 2, if (flip) top else top + 9, 6, 4, warm)
        rect(x + 3, if (flip) top else top + 11, 4, 2, hot)
        if (won < 0f) rect(x + 6, if (flip) top + 4 else top + 8, 1, 1, GOLD_HI)
    }

    // ---------- saws ----------

    private fun drawSaw(cx: Float, cy: Float, r: Float, angle: Float) {
        val ri = r.roundToInt() + 2
        for (y in -ri..ri) for (x in -ri..ri) {
            val d = sqrt((x * x + y * y).toFloat())
            if (d > r + 1f) continue
            val a = atan2(y.toFloat(), x.toFloat()) + angle
            val tooth = d > r - 1.6f && (floor(a / (PI.toFloat() / 5f)).toInt() and 1) == 0
            val color = when {
                d > r -> if (tooth) 0 else INK
                d < 1.5f -> RED_BTN
                tooth -> INK
                d > r - 1.6f -> STEEL_LO
                x + y < -1 && d < r - 2.5f -> WHITE
                else -> STEEL
            }
            if (color != 0) rect(cx + x, cy + y, 1f, 1f, color)
        }
    }

    // ---------- hero ----------

    private fun drawPlayer(w: World) {
        val p = w.player
        val flip = w.gravity < 0
        var cx = p.box.cx * TS
        var feet = (if (flip) p.box.y else p.box.b) * TS
        var k = if (w.time < POP) popScale(w.time / POP) else 1f
        val won = winAge(w)
        if (won >= 0f) {
            // sucked into the door
            val f = min(1f, won / SUCK)
            if (f >= 1f) return
            val e = f * f
            cx += ((doorX(w) + 5) - cx) * e
            feet += ((doorTop(w) + 7) + (if (flip) -3.5f else 3.5f) - feet) * e
            k = 1f - e
        }
        val playing = w.state == WorldState.PLAYING
        val pw = max(1, (6 / sqrt(p.squash) * k).roundToInt())
        val ph = max(1, (7 * p.squash * k).roundToInt())
        val running = playing && p.grounded && abs(p.vx) > 1.5f
        val bob = if (running && sin(w.time * 26f) > 0.2f) 1 else 0
        val x = (cx - pw / 2f).roundToInt()
        val y = if (flip) feet.roundToInt() + bob else feet.roundToInt() - ph - bob
        rect(x + 2, y + 2, pw, ph, SHADOW)
        if (playing && abs(p.vx) > 6f) {
            // speed lines trailing behind
            val back = if (p.vx > 0) x - 3 else x + pw + 2
            val step = if (p.vx > 0) -2 else 2
            rect(back, y + 1, 1, ph - 2, 0x806CF2C2.toInt())
            rect(back + step, y + 2, 1, max(1, ph - 4), 0x406CF2C2)
        }
        rect(x - 1, y, pw + 2, ph, HERO_OUT); rect(x, y - 1, pw, ph + 2, HERO_OUT)
        rect(x, y, pw, ph, MINT)
        rect(x, y, pw, 1, MINT_HI); rect(x, y, 1, ph, MINT_HI)
        rect(x, y + ph - 1, pw, 1, MINT_LO); rect(x + pw - 1, y + 1, 1, ph - 1, MINT_LO)
        if (pw > 4 && ph > 4) {
            if (ph > 5) rect(x, y, 1, 1, WHITE)
            val ey = if (flip) y + ph - 4 else y + 2
            val ex = x
            if (playing && (w.time + 1.3f) % 3.4f < 0.12f) {
                rect(ex + 1, ey + 1, 2, 1, HERO_OUT); rect(ex + pw - 3, ey + 1, 2, 1, HERO_OUT)
            } else {
                val look = if (p.facing > 0) 1 else 0
                val up = if (p.vy < -5f) 0 else 1
                rect(ex + 1, ey, 2, 2, WHITE); rect(ex + pw - 3, ey, 2, 2, WHITE)
                rect(ex + 1 + look, ey + up, 1, 1, DOOR_DARK); rect(ex + pw - 3 + look, ey + up, 1, 1, DOOR_DARK)
            }
        }
    }

    private fun deathFx(w: World) {
        val a = w.time - w.stateTime
        val b = w.player.box
        val cx = b.cx * TS
        val cy = b.cy * TS
        if (a < 0.07f) rect(b.x * TS - 1, b.y * TS - 1, b.w * TS + 2, b.h * TS + 2, WHITE)
        if (a < 0.34f) {
            val r = 3f + a * 58f
            for (i in 0 until 20) {
                if (a > 0.17f && i % 2 == 1) continue
                val ang = i * (PI.toFloat() / 10f) + a * 2f
                rect(cx + cos(ang) * r, cy + sin(ang) * r, 1f, 1f, if (a < 0.12f) WHITE else MINT_HI)
            }
        }
    }

    private fun winFx(w: World) {
        val a = winAge(w)
        val cx = doorX(w) + 5f
        val cy = doorTop(w) + 7f
        val burst = a - SUCK * 0.8f
        if (burst < 0f || burst > 0.42f) return
        val f = burst / 0.42f
        val len = 6f + 40f * sqrt(f)
        val start = 5f + 20f * f * f
        for (i in 0 until 12) {
            val ang = i * (PI.toFloat() / 6f) + 0.2f + burst * 0.8f
            val ca = cos(ang)
            val sa = sin(ang)
            var d = start
            while (d < len) {
                rect(cx + ca * d, cy + sa * d * 0.85f, 1f, 1f, if (d < len - 6f) GOLD_SPARK else GOLD_HI)
                d += if (i % 2 == 0) 2f else 3f
            }
        }
        if (burst < 0.2f) {
            val r = 3f + burst * 70f
            for (i in 0 until 24) {
                val ang = i * (PI.toFloat() / 12f)
                rect(cx + cos(ang) * r, cy + sin(ang) * r * 0.85f, 1f, 1f, WHITE)
            }
        }
    }

    private fun drawParticles(particles: List<Particle>) {
        for (i in 0 until particles.size) {
            val p = particles[i]
            val x = p.x * TS
            val y = p.y * TS
            when {
                p.dust -> {
                    val s = (p.size * p.life / p.life0 + 0.6f).toInt()
                    when {
                        s >= 4 -> rect(x - 1, y - 1, 3f, 3f, p.color)
                        s == 3 -> { rect(x - 1, y, 3f, 1f, p.color); rect(x, y - 1, 1f, 3f, p.color) }
                        s == 2 -> rect(x, y, 2f, 1f, p.color)
                        s == 1 -> rect(x, y, 1f, 1f, BONE_LO)
                    }
                }
                p.size >= 3 -> { rect(x - 1, y - 1, 4f, 4f, HERO_OUT); rect(x, y, 2f, 2f, p.color) }
                else -> rect(x, y, p.size.toFloat(), p.size.toFloat(), p.color)
            }
        }
    }

    companion object {
        /** How far the backdrop slides, in pixels. */
        const val PARALLAX = 20
        const val POP = 0.32f
        const val SUCK = 0.42f
        private const val CRACK_WARM = 0xFFC4462C.toInt()
        private const val CRACK_HOT = 0xFFFF9A4A.toInt()

        /** Grows from nothing with a little overshoot. */
        fun popScale(f: Float): Float {
            val u = f - 1f
            return 1f + 2.4f * u * u * u + 1.4f * u * u
        }

        private fun frac(v: Float) = v - floor(v)
    }
}
