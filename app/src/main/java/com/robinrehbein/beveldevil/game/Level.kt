package com.robinrehbein.beveldevil.game

import java.util.Locale
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** A piece of text in English and German. Chosen by device language. */
class T(val en: String, val de: String) {
    override fun toString(): String = if (Lang.german) de else en
}

object Lang {
    var german: Boolean = Locale.getDefault().language == "de"
}

enum class Dir { UP, DOWN, LEFT, RIGHT }

/**
 * What a map character turns into. Lowercase letters default to solid groups, uppercase to spike groups.
 * 'v' is reserved for ceiling spikes, 'P' for the spawn and 'D' for the door.
 */
data class Glyph(
    val spike: Boolean,
    val dir: Dir = Dir.UP,
    val hidden: Boolean = false,
    /** Hidden blocks that appear when the player hits them from below. */
    val bonk: Boolean = false,
)

sealed interface Trigger {
    /** Player center is right of [x]. */
    data class PastX(val x: Float) : Trigger
    /** Player center is left of [x]. */
    data class BeforeX(val x: Float) : Trigger
    /** Player center is inside the rectangle. */
    data class Zone(val x0: Float, val y0: Float, val x1: Float, val y1: Float) : Trigger
    /** Player stands on or touches a visible piece of the group. */
    data class Touch(val group: Char) : Trigger
    /** Seconds since the attempt started. */
    data class After(val seconds: Float) : Trigger
    /** No left/right/jump input for [seconds] in a row. */
    data class Idle(val seconds: Float) : Trigger
    /** The player shook the phone (or pressed the shake button). */
    data object Shaken : Trigger
}

sealed interface Action {
    data class Fall(val group: Char) : Action
    data class Show(val group: Char) : Action
    data class Hide(val group: Char) : Action
    data class Move(val group: Char, val dx: Float, val dy: Float, val speed: Float) : Action
    /** Door flies to tile ([col], [row]); standing on the tile below, or hanging from the tile above. */
    data class DoorTo(val col: Int, val row: Int, val speed: Float = 16f, val hanging: Boolean = false) : Action
    data class Gravity(val flipped: Boolean) : Action
    data class Swap(val on: Boolean) : Action
    data class Saw(val x: Float, val y: Float, val vx: Float, val vy: Float, val r: Float = 0.6f) : Action
    data class Say(val text: T) : Action
    data class Shake(val amount: Float) : Action
    /** Mephi plays a trap card: it flies into view, and dying before the attempt ends collects it. */
    data class Play(val card: Card) : Action

    /**
     * The group is solid for [on] seconds, then gone for [off], counted from when this runs; [phase] seconds
     * are skipped at the start. It flickers for [telegraph] seconds before vanishing, and waits while the player
     * stands where it would reappear.
     */
    data class Blink(val group: Char, val on: Float, val off: Float, val phase: Float = 0f) : Action {
        val period get() = on + off
        val telegraph get() = min(TELEGRAPH, on * 0.5f)
        /** Seconds into the cycle, [t] seconds after the blink started. */
        fun cycle(t: Float): Float = (((t + phase) % period) + period) % period
        fun solidAt(t: Float) = cycle(t) < on
        /** 0 → 1 through the flicker before vanishing, 0 otherwise. */
        fun warnAt(t: Float): Float {
            val c = cycle(t)
            val w = telegraph
            return if (c < on && c >= on - w) (c - (on - w)) / w else 0f
        }
        /** Gone, but back within [SOON] seconds. */
        fun soonAt(t: Float): Boolean = cycle(t).let { it >= on && it >= period - SOON }

        companion object {
            const val TELEGRAPH = 0.45f
            const val SOON = 0.35f
        }
    }

    /**
     * A saw that follows [points] (tile coordinates of its center) at [speed] tiles/s after waiting [delay] seconds:
     * back and forth, or round and round when [loop] (the last point joins the first).
     */
    data class PathSaw(val points: List<Pair<Float, Float>>, val speed: Float, val loop: Boolean = false, val delay: Float = 0f, val r: Float = 0.6f) : Action {
        constructor(speed: Float, vararg points: Pair<Float, Float>, loop: Boolean = false, delay: Float = 0f, r: Float = 0.6f) :
            this(points.toList(), speed, loop, delay, r)

        init { require(points.isNotEmpty() && speed >= 0f) }

        private val legs = (if (loop) points + points.first() else points).zipWithNext()
        private val length = legs.sumOf { (a, b) -> hypot(b.first - a.first, b.second - a.second).toDouble() }.toFloat()

        /** Center [t] seconds after the saw appeared. */
        fun at(t: Float): Pair<Float, Float> {
            if (length <= 0f) return points.first()
            val run = max(0f, t - delay) * speed
            var d = if (loop) run % length else (run % (2 * length)).let { if (it > length) 2 * length - it else it }
            for ((a, b) in legs) {
                val l = hypot(b.first - a.first, b.second - a.second)
                if (d <= l && l > 0f) return a.first + (b.first - a.first) * d / l to a.second + (b.second - a.second) * d / l
                d -= l
            }
            return legs.last().second
        }
    }

    /** Tilting the phone slides the group: fully left by [left] tiles, fully right by [right], at up to [speed] tiles/s. */
    data class Tilt(val group: Char, val left: Float, val right: Float = left, val speed: Float = 8f) : Action
    /** Tilting the phone pushes the player sideways by up to [speed] tiles/s, like a slope. 0 turns it off. */
    data class Slope(val speed: Float) : Action
}

class Trap(val trigger: Trigger, val actions: List<Action>, val delay: Float = 0f)

fun trap(trigger: Trigger, vararg actions: Action, delay: Float = 0f) = Trap(trigger, actions.toList(), delay)

/** Mutable char grid used to lay out a level. '#' solid, '^v<>' spikes, 'P' spawn, 'D' door. */
class MapBuilder(val cols: Int = 32, val rows: Int = 18) {
    val grid: Array<CharArray> = Array(rows) { CharArray(cols) { '.' } }

    fun put(x: Int, y: Int, c: Char) {
        require(x in 0 until cols && y in 0 until rows) { "($x,$y) outside map" }
        grid[y][x] = c
    }

    fun fill(xs: IntRange, ys: IntRange, c: Char = '#') {
        for (y in ys) for (x in xs) put(x, y, c)
    }

    /** Ceiling and side walls. */
    fun border() {
        fill(0 until cols, 0..0)
        fill(0..0, 0 until rows)
        fill(cols - 1 until cols, 0 until rows)
    }

    /** Standard ground: the player walks on row 14. */
    fun floor(top: Int = 15) = fill(0 until cols, top until rows)

    fun ascii(): String = grid.joinToString("\n") { String(it) }
}

class Level(
    val name: T,
    val intro: T,
    val legend: Map<Char, Glyph> = emptyMap(),
    val traps: List<Trap> = emptyList(),
    /** Actions that run as the attempt starts: blinking platforms, path saws, tilt. */
    val start: List<Action> = emptyList(),
    build: MapBuilder.() -> Unit,
) {
    val map: MapBuilder = MapBuilder().apply(build)
    val cols get() = map.cols
    val rows get() = map.rows

    private val actions get() = start + traps.flatMap { it.actions }
    /** Tilting the phone does something here. */
    val usesTilt: Boolean by lazy { actions.any { it is Action.Tilt || it is Action.Slope } }
    /** Shaking the phone does something here. */
    val usesShake: Boolean by lazy { traps.any { it.trigger == Trigger.Shaken } }
    val usesMotion get() = usesTilt || usesShake

    fun glyph(c: Char): Glyph? = when {
        legend.containsKey(c) -> legend[c]
        c == '^' -> Glyph(spike = true, dir = Dir.UP)
        c == 'v' -> Glyph(spike = true, dir = Dir.DOWN)
        c == '<' -> Glyph(spike = true, dir = Dir.LEFT)
        c == '>' -> Glyph(spike = true, dir = Dir.RIGHT)
        c == '#' || c in 'a'..'z' -> Glyph(spike = false)
        c in 'A'..'Z' && c != 'P' && c != 'D' -> Glyph(spike = true)
        else -> null
    }
}
