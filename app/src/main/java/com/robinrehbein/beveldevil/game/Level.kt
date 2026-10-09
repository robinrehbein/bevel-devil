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
    /**
     * The player is in the air (mid-jump or falling) with the center between [x0] and [x1]. Fires once they are
     * committed: too late to turn back, in time to react.
     *
     *     trap(Airborne(9.3f, 10.9f), Show('A'))   // spikes sprout where the jump over the spike will land
     */
    data class Airborne(val x0: Float, val x1: Float) : Trigger
    /**
     * The player touched down (airborne one step, on the ground the next) with the center between [x0] and [x1].
     *
     *     trap(Landed(17f, 19.5f), Fall('d'), Say(T("Welcome back.", "Willkommen zurück.")))
     */
    data class Landed(val x0: Float, val x1: Float) : Trigger
    /** Seconds since the attempt started. */
    data class After(val seconds: Float) : Trigger
    /** No left/right/jump input for [seconds] in a row. */
    data class Idle(val seconds: Float) : Trigger
    /** The player shook the phone (or pressed the shake button). */
    data object Shaken : Trigger
    /** The player reaches the door. Fires instead of the win (its delay is ignored); pair it with [Action.FakeWin]. */
    data object AtDoor : Trigger
    /** The player paused and resumed [times] times in this attempt. */
    data class Resumed(val times: Int = 1) : Trigger

    /**
     * Pressure pad [pad] ([Action.Pad]) was stepped on [times] times in this attempt.
     *
     *     trap(Pressed('1'), Power('b', false), Say(T("Wrong button.", "Falscher Knopf.")))
     */
    data class Pressed(val pad: Char, val times: Int = 1) : Trigger

    /**
     * Heated group [group] ([Action.Heat]) reached heat [above] (0..1).
     *
     *     trap(Heated('h', 0.5f), Fall('x'))   // the plate gets warm, the ledge ahead drops
     */
    data class Heated(val group: Char, val above: Float) : Trigger
}

/** What a pressure pad ([Action.Pad]) does to its circuits. */
enum class PadMode {
    /** Every step flips them. */
    TOGGLE,
    /** Flips them while pressed, flips them back on release. */
    HOLD,
    /** Powers them up. */
    ON,
    /** Cuts them. */
    OFF,
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
    /**
     * Mephi says [text]; if [unless] already holds when the trap goes off, he says [otherwise] instead. For a trap
     * the player can reach in another order than intended: the trap stays the same, only the line owns up to it.
     *
     *     trap(Landed(14.5f, 18.5f), Swap(true), Say(T("Left is the new right.", "Links ist das neue Rechts."),
     *         unless = Pressed('1'), otherwise = T("Repair expired.", "Reparatur abgelaufen.")))
     */
    data class Say(val text: T, val unless: Trigger? = null, val otherwise: T? = null) : Action
    data class Shake(val amount: Float) : Action
    /** Mephi plays a trap card: it flies into view, and dying before the attempt ends collects it. */
    data class Play(val card: Card) : Action

    /**
     * Mephi bluffs: [card] flies in like a real trap, but nothing comes of it (the real trap, if any, is another
     * one). While it flies he has a tell (sweat, darting eyes); then the card flips over and shows BLUFF.
     * Use it sparingly: only in a rematch round where the same card was honest in round 1.
     *
     *     trap(PastX(9f), Bluff(Card.COLLAPSE))
     *     trap(Landed(10f, 14f), Show('B'))           // the real trap: spikes where the reflex jump lands
     */
    data class Bluff(val card: Card) : Action

    /**
     * Ctrl+Z: Bevel is put back where he was [seconds] ago (at most [World.HISTORY] seconds), standing still.
     *
     *     trap(PastX(24f), Play(Card.UNDO), Undo(2f), Say(T("Undo.", "Rückgängig.")))
     */
    data class Undo(val seconds: Float) : Action

    /**
     * The group starts stalking the player sideways at [speed] tiles/s: it slides so its middle stays under (or
     * over) Bevel, at most [left] tiles left and [right] tiles right of where it was built.
     *
     *     trap(PastX(8f), Play(Card.STALKER), Chase('S', speed = 4f, left = 3f, right = 18f))
     */
    data class Chase(val group: Char, val speed: Float, val left: Float = 32f, val right: Float = 32f) : Action

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

    // network mechanics (World 2), run-time state in Net.kt

    /**
     * Portal pair [id]: walking into tile [from] (col, row) pops the player out of tile [to] (and back, when [twoWay])
     * at the same spot within the tile and with the same velocity. Both tiles must be empty.
     */
    data class Portal(val id: Char, val from: Pair<Int, Int>, val to: Pair<Int, Int>, val twoWay: Boolean = true) : Action
    /** "DNS changed": portal [id] now comes out at tile [to]. */
    data class Reroute(val id: Char, val to: Pair<Int, Int>) : Action
    /** The group is a conveyor belt: standing on it moves the player [speed] tiles/s (negative: left). Run again to change it. */
    data class Belt(val group: Char, val speed: Float) : Action
    /**
     * Switches portal, laser, belt, circuit or fan [id] off or back on. A laser coming back on warms up (telegraphs)
     * first; a circuit's [Clock] stops; a fan spins down to a halt or back up to its speed.
     *
     *     trap(PastX(12f), Power('a', false))   // Mephi cuts the rail under you
     */
    data class Power(val id: Char, val on: Boolean) : Action

    /**
     * Laser [id] between emitters on tiles [from] and [to] (same column or row). The emitters sit on the far side of
     * their tiles, facing each other, and the beam kills. It is lit for [on] seconds, then dark for [off] (0: always
     * lit), after [delay] seconds, [phase] seconds into the cycle. The emitters glow for [telegraph] seconds before it fires.
     */
    data class Laser(
        val id: Char, val from: Pair<Int, Int>, val to: Pair<Int, Int>,
        val on: Float = 1f, val off: Float = 0f, val phase: Float = 0f, val delay: Float = 0f,
    ) : Action {
        init { require(from != to && (from.first == to.first || from.second == to.second)) { "laser $id must be straight" } }

        val vertical get() = from.first == to.first
        val period get() = on + off
        val telegraph get() = min(TELEGRAPH, off * 0.6f)
        private val lo get() = if (vertical) min(from.second, to.second) else min(from.first, to.first)
        private val hi get() = if (vertical) max(from.second, to.second) else max(from.first, to.first)
        /** The beam, lens to lens, in tiles. */
        val x0 get() = if (vertical) from.first + 0.5f - HALF else lo + LENS
        val x1 get() = if (vertical) from.first + 0.5f + HALF else hi + 1 - LENS
        val y0 get() = if (vertical) lo + LENS else from.second + 0.5f - HALF
        val y1 get() = if (vertical) hi + 1 - LENS else from.second + 0.5f + HALF

        fun cycle(t: Float): Float = (((t + phase) % period) + period) % period
        /** Firing, [t] seconds after it started. */
        fun litAt(t: Float) = t >= delay && (off <= 0f || cycle(t - delay) < on)
        /** 0 → 1 through the glow before it fires, 0 otherwise. */
        fun warnAt(t: Float): Float {
            if (t < delay) {
                val w = min(TELEGRAPH, delay)
                return if (t >= delay - w) (t - (delay - w)) / w else 0f
            }
            if (off <= 0f) return 0f
            val c = cycle(t - delay)
            val w = telegraph
            return if (c >= period - w) (c - (period - w)) / w else 0f
        }

        companion object {
            const val TELEGRAPH = 0.6f
            /** How far into its tile an emitter reaches. */
            const val LENS = 0.625f
            /** Half the beam's thickness. */
            const val HALF = 0.125f
        }
    }

    // hardware mechanics (World 3), run-time state in Hardware.kt

    /**
     * Group [group] is a circuit, powered from the start when [on]. A lowercase (block) group is a copper rail:
     * solid and glowing while powered, a dark outline you fall through while not. An uppercase (spike) group is an
     * exposed live trace: deadly to touch while powered, harmless while not, never solid. Switch it with a [Pad],
     * a [Clock], [Power], [Toggle] or [BitFlip]. A rail regaining power waits until the player is out of it.
     *
     *     start = listOf(Circuit('a'), Circuit('Z'), Pad('1', at = 4 to 14, circuits = "Z", mode = PadMode.OFF))
     *     trap(PastX(12f), Power('a', false))   // the rail under you goes dark
     */
    data class Circuit(val group: Char, val on: Boolean = true) : Action

    /**
     * Circuit [group] runs on a clock: powered for [on] seconds, dead for [off], [phase] seconds into the cycle,
     * counted from when this runs. Makes [group] a circuit if it is none yet. A rail flickers red before it drops.
     * [Power], [Toggle], [BitFlip] and pads stop the clock.
     *
     *     start = listOf(Clock('a', on = 1.6f, off = 1.2f), Clock('b', on = 1.6f, off = 1.2f, phase = 1.4f))
     */
    data class Clock(val group: Char, val on: Float, val off: Float, val phase: Float = 0f) : Action {
        /** The same timing as a blinking platform, telegraph included. */
        val timing get() = Blink(group, on, off, phase)
    }

    /**
     * Flips each circuit in [groups]: powered ones go dark, dark ones come on. Stops their clocks.
     *
     *     trap(Touch('x'), Toggle("ab"))
     */
    data class Toggle(val groups: String) : Action

    /**
     * "Bit flip": circuits [a] and [b] swap their power. Stops their clocks.
     *
     *     trap(PastX(15f), BitFlip('a', 'b'), Say(T("Cosmic ray.", "Kosmische Strahlung.")))
     */
    data class BitFlip(val a: Char, val b: Char) : Action

    /**
     * Pressure pad [id] lying on the floor of tile [at] (an empty tile above something solid). Stepping on it
     * switches the circuits in [circuits] as [mode] says; its cap has the color of the first circuit, and its LED
     * shows whether that one is powered. [Trigger.Pressed] lets traps hook onto it.
     *
     *     start = listOf(Circuit('a', on = false), Pad('1', at = 6 to 14, circuits = "a"))
     *     start = listOf(Circuit('w'), Pad('2', at = 9 to 14, circuits = "w", mode = PadMode.HOLD))  // hold to open
     */
    data class Pad(val id: Char, val at: Pair<Int, Int>, val circuits: String = "", val mode: PadMode = PadMode.TOGGLE) : Action

    /**
     * Group [group] heats up: standing on it takes it from cold to full heat in [rise] seconds; off it cools back
     * down in [cool] seconds. With [load] (a chip under load) it heats all the time, stood on or not, and only a
     * [Heatsink] or [HeatSpike] cools it. At full heat it burns on touch, or with [melt] melts away for good.
     * It glows copper → orange → yellow → white and flickers red near the end. Run again to change the rates.
     *
     *     start = listOf(Heat('h', rise = 1.2f), Heat('c', rise = 4f, load = true))
     */
    data class Heat(val group: Char, val rise: Float = 1.5f, val cool: Float = rise, val load: Boolean = false, val melt: Boolean = false) : Action {
        init { require(rise > 0f && cool > 0f) }
    }

    /**
     * Group [group] is a heatsink: while the player stands on it, the heated groups in [cools] cool down fast
     * (from full heat in [Hardware.SINK] seconds).
     *
     *     start = listOf(Heat('c', rise = 3f, load = true), Heatsink('k', cools = "c"))
     */
    data class Heatsink(val group: Char, val cools: String) : Action

    /**
     * "Overclocked": group [group] jumps to heat [to] (0..1) at once and cools as a hot plate from there. A plain
     * group that was never declared with [Heat] looks like any other floor until this runs (a hidden trap), and
     * then heats and cools like a default [Heat] plate.
     *
     *     trap(PastX(14f), HeatSpike('f'), Say(T("Overclocked!", "Übertaktet!")))
     */
    data class HeatSpike(val group: Char, val to: Float = 0.7f) : Action

    /**
     * Fan [id] in tile [at] (a floor or wall tile; its housing is drawn over it) blowing toward [dir] across
     * [reach] tiles, [width] tiles wide (to the right of [at] for UP and DOWN, below it for LEFT and RIGHT).
     * [speed] is the wind in tiles/s, negative sucks: sideways it drifts the player like a belt, walking or in
     * the air; up or down it replaces gravity, easing the player's vertical speed to the wind. A player whose
     * center is in the zone feels it. With [off] > 0 it runs for [on] seconds, then rests [off] ([phase] into
     * the cycle). It spins up and down at [Hardware.SPIN] tiles/s², so a change shows before it bites.
     *
     *     start = listOf(Fan('f', at = 12 to 15, dir = Dir.UP, reach = 9, speed = 12f, width = 2))
     *     start = listOf(Fan('w', at = 0 to 10, dir = Dir.RIGHT, reach = 20, speed = 5f, width = 4, on = 2f, off = 2f))
     */
    data class Fan(
        val id: Char, val at: Pair<Int, Int>, val dir: Dir, val reach: Int, val speed: Float = 10f, val width: Int = 1,
        val on: Float = 0f, val off: Float = 0f, val phase: Float = 0f,
    ) : Action {
        init { require(reach > 0 && width > 0) { "fan $id needs reach and width" } }

        val vertical get() = dir == Dir.UP || dir == Dir.DOWN
        /** The wind zone, in tiles. */
        val x0 get() = when (dir) { Dir.LEFT -> at.first - reach; Dir.RIGHT -> at.first + 1; else -> at.first }.toFloat()
        val x1 get() = when (dir) { Dir.LEFT -> at.first; Dir.RIGHT -> at.first + 1 + reach; else -> at.first + width }.toFloat()
        val y0 get() = when (dir) { Dir.UP -> at.second - reach; Dir.DOWN -> at.second + 1; else -> at.second }.toFloat()
        val y1 get() = when (dir) { Dir.UP -> at.second; Dir.DOWN -> at.second + 1 + reach; else -> at.second + width }.toFloat()

        /** Blowing (rather than resting) [t] seconds after it started. */
        fun runsAt(t: Float): Boolean = off <= 0f || ((((t + phase) % (on + off)) + on + off) % (on + off)) < on
    }

    /**
     * Fan [id] changes its wind to [speed] tiles/s (negative: reversed). It spins down, through zero and up again,
     * so a reversal is visible for a moment before it pulls.
     *
     *     trap(PastX(9f), FanSet('w', -8f), Say(T("Reverse thrust!", "Schubumkehr!")))
     */
    data class FanSet(val id: Char, val speed: Float) : Action

    /** Tilting the phone slides the group: fully left by [left] tiles, fully right by [right], at up to [speed] tiles/s. */
    data class Tilt(val group: Char, val left: Float, val right: Float = left, val speed: Float = 8f) : Action
    /** Tilting the phone pushes the player sideways by up to [speed] tiles/s, like a slope. 0 turns it off. */
    data class Slope(val speed: Float) : Action
    /**
     * Meta twists, see Twists.kt. A fake win: Bevel is sucked into the door, a [FakeEnd] screen shows, then Mephi
     * glitches in ("Nope.") and Bevel is spat back out. [then] runs at that moment. With [FakeEnd.CREDITS] and a
     * hidden [platforms] group, the last credit lines stop on that group's rows and turn into its platforms.
     */
    data class FakeWin(val end: FakeEnd, val platforms: Char?, val then: List<Action>) : Action {
        constructor(end: FakeEnd = FakeEnd.CLEAR, platforms: Char? = null, vararg then: Action) : this(end, platforms, then.toList())
    }
    /** From now on the HUD pause button plays [trick]. The back button always pauses for real. */
    data class PauseTrap(val trick: PauseTrick) : Action
    /** The golden frame cracks in the tile rectangle for [warn] seconds, then that piece of it falls into the level. */
    data class FrameCrack(val x0: Int, val y0: Int, val x1: Int = x0, val y1: Int = y0, val warn: Float = 0.8f) : Action
    /**
     * "Who says the room ends here?": the wall between room [into] - 1 and room [into] of a level with several
     * [Level.rooms] cracks open for [warn] seconds, then crumbles away in rows [top]..[bottom] (both its tiles, the
     * right wall of the one room and the left wall of the next; plain '#' only). Mephi laughs and says [line]. The
     * camera stays in the room the player is in: it pans to the next room (the game frozen, see [World.PAN]) only
     * once the player walks through the breach, so every room is fully in view before anything in it can be done.
     * The door cannot be entered while the wall cracks. With [door] (global tile, like [DoorTo]) the door then slips
     * through the breach to that tile, at [doorSpeed] tiles/s and out of reach until it lands: the classic is
     * [Trigger.AtDoor], so the door the player just reached "was never the end":
     *
     *     trap(Trigger.AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 28) to 14))
     */
    data class Extend(
        val into: Int = 1, val top: Int = 12, val bottom: Int = 14, val warn: Float = 0.6f,
        val door: Pair<Int, Int>? = null, val doorSpeed: Float = 14f, val line: T? = ROOM_LINE,
    ) : Action {
        init { require(into >= 1 && top <= bottom && warn >= 0f) }
        /** Global columns of the breach: the shared wall between the two rooms. */
        val x0 get() = roomX(into, 0) - 1
        val x1 get() = roomX(into, 0)

        companion object {
            val ROOM_LINE = T("Who says the room ends here?", "Wer sagt, dass der Raum hier aufhört?")
        }
    }
    /** The picture turns upside down for [seconds]. Left and right follow the screen, so the controls stay sane. */
    data class Flip(val seconds: Float) : Action
    /** The CRT loses vertical hold for [seconds]: the picture rolls [laps] times. Visual only. */
    data class Roll(val seconds: Float, val laps: Int = 2) : Action
    /** The previous attempt replays as a deadly ghost, starting [delay] seconds from now. */
    data class Ghost(val delay: Float = 1f) : Action
}

class Trap(val trigger: Trigger, val actions: List<Action>, val delay: Float = 0f)

fun trap(trigger: Trigger, vararg actions: Action, delay: Float = 0f) = Trap(trigger, actions.toList(), delay)

/**
 * A rematch: after the door, Mephi deals a new hand in the same room ("Revanche!"). The map is the level's own,
 * changed by [edit]; [legend] adds to the level's. [start] and [hint] default to the level's own (pass a list, even an
 * empty one, to replace the start actions). Dying restarts this round, not the whole level.
 *
 *     rematch = listOf(Round(T("Again. Same room.", "Nochmal. Gleicher Raum."), traps = listOf(…)) { fill(9..10, 15..17, 'x') })
 */
class Round(
    val intro: T,
    val traps: List<Trap> = emptyList(),
    val start: List<Action>? = null,
    val legend: Map<Char, Glyph> = emptyMap(),
    val hint: T? = null,
    val edit: MapBuilder.() -> Unit = {},
)

/** Columns of one room: one screen. A level is one room, or (rarely, see [Level.rooms]) several side by side. */
const val ROOM_COLS = 32
/** Rows of a room (and of every level). */
const val ROOM_ROWS = 18

/** Global tile column of column [x] of room [room]: rooms sit side by side, [ROOM_COLS] apart. */
fun roomX(room: Int, x: Int): Int = room * ROOM_COLS + x
/** Global x of [x] tiles into room [room], for triggers and saws: `PastX(roomX(1, 4f))`. */
fun roomX(room: Int, x: Float): Float = room * ROOM_COLS + x

/**
 * Mutable char grid used to lay out a level. '#' solid, '^v<>' spikes, 'P' spawn, 'D' door.
 *
 * A level of several rooms ([Level.rooms]) is one wide grid, [ROOM_COLS] columns per room. Inside [room] every call
 * takes that room's local coordinates (0..31), and [border] and [floor] cover just that room. Outside it they take
 * global coordinates, and [border] frames every room on its own, so neighbouring rooms are parted by a two-tile wall
 * ([Action.Extend] breaks it open).
 */
class MapBuilder(val cols: Int = ROOM_COLS, val rows: Int = ROOM_ROWS) {
    val grid: Array<CharArray> = Array(rows) { CharArray(cols) { '.' } }
    /** Rooms side by side. */
    val rooms get() = maxOf(1, cols / ROOM_COLS)
    private var ox = 0
    private var span = cols
    /** Width of what calls address now: one room inside [room], else the whole map. */
    val width get() = span

    /**
     * Runs [block] in room [i], in its local coordinates:
     *
     *     room(1) { fill(10..12, 12..14); door(28) }
     */
    fun room(i: Int, block: MapBuilder.() -> Unit) {
        require(i in 0 until rooms) { "room $i outside the map ($rooms rooms)" }
        val o = ox
        val s = span
        ox = i * ROOM_COLS
        span = ROOM_COLS
        try { block() } finally { ox = o; span = s }
    }

    fun put(x: Int, y: Int, c: Char) {
        require(x in 0 until span && y in 0 until rows) { "($x,$y) outside map" }
        grid[y][ox + x] = c
    }

    /** The char at ([x], [y]), in the same coordinates as [put]. */
    fun at(x: Int, y: Int): Char = grid[y][ox + x]

    fun fill(xs: IntRange, ys: IntRange, c: Char = '#') {
        for (y in ys) for (x in xs) put(x, y, c)
    }

    /** Ceiling and side walls; of every room on its own when the map has several and no [room] is chosen. */
    fun border() {
        if (span > ROOM_COLS) {
            for (r in 0 until rooms) room(r) { border() }
            return
        }
        fill(0 until span, 0..0)
        fill(0..0, 0 until rows)
        fill(span - 1 until span, 0 until rows)
    }

    /** Standard ground: the player walks on row 14. */
    fun floor(top: Int = 15) = fill(0 until span, top until rows)

    fun ascii(): String = grid.joinToString("\n") { String(it) }
}

class Level(
    val name: T,
    val intro: T,
    val legend: Map<Char, Glyph> = emptyMap(),
    val traps: List<Trap> = emptyList(),
    /** Actions that run as the attempt starts: blinking platforms, path saws, tilt. */
    val start: List<Action> = emptyList(),
    /** Further rounds in the same room, played after the door; see [Round]. */
    val rematch: List<Round> = emptyList(),
    /** What Mephi lets slip on the respawn after [Game.HINT_DEATHS] deaths in a round (once per round). */
    val hint: T? = null,
    /**
     * Rooms side by side, [ROOM_COLS] columns each. Hard rule: a level is one screen. The rare exception is Mephi's
     * surprise "who says the room ends here?" ([Action.Extend]): the camera always shows one whole room and pans,
     * with the game frozen, when the player walks into the next one. See docs/LEVEL_DESIGN_V2.md, U18.
     */
    val rooms: Int = 1,
    private val build: MapBuilder.() -> Unit,
) {
    init { require(rooms >= 1) { "a level has at least one room" } }

    val map: MapBuilder = MapBuilder(cols = ROOM_COLS * rooms).apply(build)

    /** This level and its rematches, each a level of its own: round 1 is this level. */
    val rounds: List<Level> by lazy {
        listOf(this) + rematch.map { r ->
            Level(name, r.intro, legend + r.legend, r.traps, r.start ?: start, hint = r.hint ?: hint, rooms = rooms) { build(); r.edit(this) }
        }
    }
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

/** This action and, for a [Action.FakeWin], everything it goes on with (nested). */
fun Action.flat(): List<Action> = if (this is Action.FakeWin) listOf(this) + then.flatMap { it.flat() } else listOf(this)
