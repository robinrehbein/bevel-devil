package com.robinrehbein.beveldevil.game

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

private const val EPS = 1e-4f

/** Tuning in tiles and seconds. */
object Physics {
    const val GRAVITY = 62f
    const val JUMP_SPEED = 17.5f
    const val JUMP_CUT = 2.4f          // extra gravity while rising without holding jump
    const val MAX_FALL = 26f
    const val RUN = 8.5f
    const val ACCEL_GROUND = 90f
    const val ACCEL_AIR = 55f
    const val FRICTION = 110f
    const val COYOTE = 0.09f
    const val BUFFER = 0.12f
    const val BLOCK_GRAVITY = 90f
    const val PLAYER_W = 0.72f
    const val PLAYER_H = 0.86f
}

class Box(var x: Float, var y: Float, var w: Float, var h: Float) {
    val r get() = x + w
    val b get() = y + h
    val cx get() = x + w / 2
    val cy get() = y + h / 2
    fun overlaps(o: Box) = x < o.r - EPS && r > o.x + EPS && y < o.b - EPS && b > o.y + EPS
    fun overlaps(ox: Float, oy: Float, ow: Float, oh: Float) =
        x < ox + ow - EPS && r > ox + EPS && y < oy + oh - EPS && b > oy + EPS
}

/** A rectangle in tiles, [x0]..[x1] × [y0]..[y1]. */
data class Area(val x0: Float, val y0: Float, val x1: Float, val y1: Float) {
    val w get() = x1 - x0
    val h get() = y1 - y0
    /** The smallest area around both. */
    operator fun plus(o: Area?): Area = if (o == null) this else Area(min(x0, o.x0), min(y0, o.y0), max(x1, o.x1), max(y1, o.y1))
    fun shift(dx: Float, dy: Float) = Area(x0 + dx, y0 + dy, x1 + dx, y1 + dy)
    fun grow(dx: Float, dy: Float) = Area(x0 - dx, y0 - dy, x1 + dx, y1 + dy)
    /** How much of the two overlaps, in square tiles (0 if they only touch). */
    fun overlap(o: Area): Float = max(0f, min(x1, o.x1) - max(x0, o.x0)) * max(0f, min(y1, o.y1) - max(y0, o.y0))

    companion object {
        fun of(b: Box) = Area(b.x, b.y, b.r, b.b)
        fun tile(c: Pair<Int, Int>) = Area(c.first.toFloat(), c.second.toFloat(), c.first + 1f, c.second + 1f)
        /** Around all of [areas], null if there are none. */
        fun union(areas: Iterable<Area?>): Area? = areas.fold(null as Area?) { acc, a -> if (a == null) acc else a + acc }
    }
}

enum class GroupMode { IDLE, FALL, MOVE, CHASE }

class Group(val id: Char, hidden: Boolean, val bonk: Boolean) {
    val pieces = ArrayList<Piece>()
    var visible = !hidden
    var ox = 0f
    var oy = 0f
    var vy = 0f
    var mode = GroupMode.IDLE
    var tx = 0f
    var ty = 0f
    var speed = 0f
    var blink: Action.Blink? = null
    var blinkT0 = 0f
    /** Blinking: 0 → 1 through the flicker before it vanishes. */
    var warn = 0f
    /** Blinking and gone, but about to return (or waiting for the player to step out). */
    var soon = false
    var tilt: Action.Tilt? = null
    /** Conveyor belt speed in tiles/s ([Action.Belt]), null if it is no belt. */
    var belt: Float? = null
    var beltOn = true
    /** How far the belt surface has run, for drawing it. */
    var beltRun = 0f
    val beltSpeed get() = if (beltOn && visible) belt ?: 0f else 0f
    /** The group is a circuit ([Action.Circuit]): drawn as copper, [visible] is its power. */
    var circuit: Circuit? = null
    /** Stalking the player ([Action.Chase]). */
    var chase: Action.Chase? = null
    /** Middle of the group as built, in tiles. */
    val homeX get() = if (pieces.isEmpty()) 0f else (pieces.minOf { it.hx } + pieces.maxOf { it.hx } + 1f) / 2f
}

class Piece(val spike: Boolean, val dir: Dir, val hx: Float, val hy: Float, val group: Group?) {
    val box = Box(hx, hy, 1f, 1f)
    val visible get() = group?.visible ?: true
    val solid get() = !spike && visible
    fun sync() {
        box.x = hx + (group?.ox ?: 0f)
        box.y = hy + (group?.oy ?: 0f)
    }

    /** Spikes kill only with their pointy part. */
    fun hurts(p: Box): Boolean {
        val x = box.x
        val y = box.y
        return when (dir) {
            Dir.UP -> p.overlaps(x + 0.18f, y + 0.45f, 0.64f, 0.55f)
            Dir.DOWN -> p.overlaps(x + 0.18f, y, 0.64f, 0.55f)
            Dir.LEFT -> p.overlaps(x + 0.45f, y + 0.18f, 0.55f, 0.64f)
            Dir.RIGHT -> p.overlaps(x, y + 0.18f, 0.55f, 0.64f)
        }
    }
}

class Door(x: Float, y: Float) {
    val box = Box(x, y, 1.2f, 1.6f)
    var tx = x
    var ty = y
    var speed = 0f
    var hanging = false
    val moving get() = box.x != tx || box.y != ty
}

class Saw(var x: Float, var y: Float, val vx: Float, val vy: Float, val r: Float, val path: Action.PathSaw? = null, val t0: Float = 0f) {
    var angle = 0f
}

class Player(cx: Float, bottom: Float) {
    val box = Box(cx - Physics.PLAYER_W / 2, bottom - Physics.PLAYER_H, Physics.PLAYER_W, Physics.PLAYER_H)
    var vx = 0f
    var vy = 0f
    var grounded = false
    var ground: Piece? = null
    var coyote = 0f
    var buffer = 0f
    var facing = 1
    /** 1 = normal, >1 stretched, <1 squashed. Eases back to 1. */
    var squash = 1f
}

class Controls {
    var left = false
    var right = false
    var jump = false
    var jumpPressed = false
    /** Phone roll, -1 (left edge down) .. 1 (right edge down). */
    var tilt = 0f
    /** Set on a shake; the world clears it after each step. */
    var shake = false
}

sealed interface Event {
    data object Jump : Event
    data object Land : Event
    data object Bonk : Event
    data object Flip : Event
    data object Crash : Event
    data class Died(val x: Float, val y: Float) : Event
    data object Won : Event
    data class Say(val text: T) : Event
    data class Shake(val amount: Float) : Event
    /** Mephi played [card]; [area] is what the trap that played it acts on (global tiles), so the card can keep clear of it. */
    data class Played(val card: Card, val bluff: Boolean = false, val area: Area? = null) : Event
    /** A trap without a card went off over [area] (global tiles): a card still flying should not cover it. */
    data class Target(val area: Area) : Event
    /** [Action.Hide] took these solid tiles away (their top-left corners, global tiles): they crumble and fall, for the look. */
    data class Crumble(val tiles: List<Pair<Float, Float>>) : Event
    /** A [Action.FakeWin] started: looks exactly like [Won] but grants nothing. */
    data object FakeWon : Event
    /** The fake win is over: Mephi glitches back in. */
    data object Unfaked : Event
    /** The player went through a portal. */
    data object Hop : Event
    /** Ctrl+Z: Bevel was put back ([Action.Undo]). */
    data object Rewind : Event
    /** The player stepped on a pressure pad. */
    data object Switch : Event
    /** A heated group got hot enough to hurt soon, or melted. */
    data object Sizzle : Event
    /** A fan spun up from a standstill. */
    data object Hum : Event
    /** Mephi breaks the room open ([Action.Extend]): the wall starts to crack. */
    data object Extended : Event
    /** The breach crumbled away, over the tile rectangle [x0]..[x1] × [y0]..[y1]. */
    data class Breach(val x0: Float, val y0: Float, val x1: Float, val y1: Float) : Event
    /** The camera started to pan one room over: [dir] +1 right, -1 left. */
    data class Pan(val dir: Int) : Event
}

enum class WorldState { PLAYING, DEAD, WON }

/** One attempt at a level (or at one of its rounds, see [Level.rounds]). Pure game logic, no Android. */
class World(val level: Level, private val past: Trail? = null) {
    val cols = level.cols
    val rows = level.rows
    val pieces = ArrayList<Piece>()
    val groups = LinkedHashMap<Char, Group>()
    val saws = ArrayList<Saw>()
    val links = ArrayList<Link>()
    val beams = ArrayList<Beam>()
    val circuits = LinkedHashMap<Char, Circuit>()
    val pads = ArrayList<Switch>()
    val heaters = LinkedHashMap<Char, Heater>()
    val sinks = ArrayList<Sink>()
    val fans = ArrayList<Blower>()
    val events = ArrayList<Event>()
    val player: Player
    val door: Door
    var gravity = 1f
        private set
    var swapped = false
        private set
    var time = 0f
        private set
    var state = WorldState.PLAYING
        private set
    /** [time] when [state] last changed (death or win), for the renderer's animations. */
    var stateTime = 0f
        private set
    /** The last trap card Mephi played in this attempt; collected if the player dies. */
    var lastCard: Card? = null
        private set
    /** Seconds without left/right/jump input. */
    var idle = 0f
        private set
    /** Tilt as last applied, for the HUD. */
    var tilt = 0f
        private set
    /** [time] of the last shake, for the HUD. */
    var shakeTime = -9f
        private set
    /** Tiles/s the player drifts at full tilt ([Action.Slope]). */
    var slope = 0f
        private set
    private var shaken = false
    /** The step the player last touched down in, and where ([Trigger.Landed]). */
    private var landTick = -9
    private var landX = 0f
    /** Just came out of this tile: it takes the player again only after they left it. */
    private var hopBlock: Pair<Int, Int>? = null
    private var hopReady = 0f

    private class TrapState(val trap: Trap) {
        var fired = false
        var done = false
        var timer = 0f
        /** [time] the trigger held. */
        var at = 0f
    }

    private val traps = level.traps.map { TrapState(it) }

    /**
     * A trap that went off in this attempt: its trigger held at [triggered], its actions ran at [time] (later by the
     * trap's delay). Read by the design guard rails (docs/LEVEL_DESIGN_V2.md §9), not by the game.
     */
    class Sprung(val trap: Trap, val triggered: Float, val time: Float)

    /** Every trap that went off in this attempt, in order. */
    val sprung = ArrayList<Sprung>()

    // ---------- meta twists (see Twists.kt) ----------

    /** Where Bevel stood in the last [HISTORY] seconds, one entry per step: what [Action.Undo] goes back to. */
    private val history = ArrayDeque<Long>()
    private val undoable = level.traps.any { t -> t.actions.any { it is Action.Undo } }

    /** This attempt's path, recorded only in levels with a ghost: the next attempt's ghost. */
    val trail = Trail()
    private val recording = level.traps.any { t -> t.actions.any { it is Action.Ghost } }
    /** Simulation steps so far. */
    var ticks = 0
        private set
    /** [time] when Bevel last popped in: the attempt start, or being spat out after a fake win. */
    var spawnTime = 0f
        private set
    var fake: Action.FakeWin? = null
        private set
    var fakeTime = 0f
        private set
    private var fakeLength = 0f
    private var doorLock = 0f
    /** The credits roll of the running (or last) [FakeEnd.CREDITS] fake. */
    var credits: List<CreditLine> = emptyList()
        private set
    /** The group the credits turned into, once they did: its platforms carry the lines. */
    var creditPlatforms: Char? = null
        private set
    /** The fake screen is up (after the door animation). */
    val fakeShown get() = fake != null && time - fakeTime >= Twists.FAKE_DELAY
    val fakeAge get() = time - fakeTime - Twists.FAKE_DELAY

    var pauseTrick = PauseTrick.HONEST
        private set
    var dodges = 0
        private set
    var dodgeTime = -99f
        private set
    var resumes = 0
        private set

    /** A cracking piece of wall: after [warn] seconds it falls into the level, or for an [extend] crumbles away. */
    class Crack(val group: Group, val time: Float, val warn: Float, val extend: Action.Extend? = null) {
        var fell = false
        val crumble get() = extend != null
    }
    val cracks = ArrayList<Crack>()
    /** Every action the level can run: its start, its traps, and what fake endings go on with. */
    private val allActions = (level.start + level.traps.flatMap { it.actions }).flatMap { it.flat() }
    private val crackRects = allActions.filterIsInstance<Action.FrameCrack>().distinct()
    private val crackGroups = HashMap<Action.FrameCrack, Group>()

    // ---------- rooms and camera ([Level.rooms], [Action.Extend]) ----------

    /** Rooms side by side; the camera shows exactly one of them, or pans between two. */
    val rooms = level.map.rooms
    private val breachRects = allActions.filterIsInstance<Action.Extend>()
        .onEach { require(it.into < rooms) { "Level ${level.name.en} extends into room ${it.into} but has only $rooms rooms" } }.map { Triple(it.into, it.top, it.bottom) }.distinct()
    private val breachGroups = HashMap<Triple<Int, Int, Int>, Group>()
    /** The room the camera shows (or pans to). */
    var room = 0
        private set
    /** Left edge of the view in tiles: [room] × [ROOM_COLS], or on the way there during a pan. */
    var camX = 0f
        private set
    private var panFrom = 0f
    /** Seconds into the current pan; [PAN] or more when the camera rests. */
    var panAge = PAN
        private set
    /** The camera is moving to another room: nothing else moves (see [step]). */
    val panning get() = panAge < PAN
    /** Global x of the room edge the camera last panned across (-1 before any): going back over it needs [ROOM_SLACK]. */
    private var lastSeam = -1
    /** +1 when the last pan went right, -1 left, 0 before any. */
    var panDir = 0
        private set
    /** [time] of the last pan (time stands still during one, so this is also when it ended). */
    var panTime = -99f
        private set

    /** The room that global x [x] lies in. */
    fun roomOf(x: Float): Int = floor(x / ROOM_COLS).toInt().coerceIn(0, rooms - 1)

    /** Shows room [r] at once, without a pan (tests, screenshots). */
    fun showRoom(r: Int) {
        room = r.coerceIn(0, rooms - 1)
        camX = (room * ROOM_COLS).toFloat()
        panAge = PAN
    }

    private var turnFrom = -99f
    private var turnUntil = -99f
    private var rollFrom = -99f
    private var rollUntil = -99f
    private var rollLaps = 0

    private var ghostStart = Int.MAX_VALUE
    /** The previous attempt's ghost, while it walks; null otherwise. */
    var ghost: Box? = null
        private set
    private val ghostBox = Box(0f, 0f, Physics.PLAYER_W, Physics.PLAYER_H)
    /** The ghost's frame in the previous attempt's [Trail], or -1. */
    var ghostFrame = -1
        private set
    private var spawnX = 0f

    init {
        var spawn: Pair<Int, Int>? = null
        var doorAt: Pair<Int, Int>? = null
        for (y in 0 until rows) for (x in 0 until cols) {
            val c = level.map.grid[y][x]
            when (c) {
                'P' -> spawn = x to y
                'D' -> doorAt = x to y
                '.' -> {}
                else -> {
                    val g = level.glyph(c) ?: error("Unknown map char '$c' in ${level.name.en}")
                    val crack = if (c == '#') crackRects.indexOfFirst { x in it.x0..it.x1 && y in it.y0..it.y1 } else -1
                    val breach = if (c == '#' && crack < 0) breachRects.indexOfFirst { (into, top, bottom) ->
                        (x == roomX(into, 0) - 1 || x == roomX(into, 0)) && y in top..bottom
                    } else -1
                    val group = when {
                        crack >= 0 -> groups.getOrPut(CRACK_ID + crack) { Group(CRACK_ID + crack, false, false) }.also { crackGroups[crackRects[crack]] = it }
                        breach >= 0 -> groups.getOrPut(BREACH_ID + breach) { Group(BREACH_ID + breach, false, false) }.also { breachGroups[breachRects[breach]] = it }
                        c == '#' || c in "^v<>" -> null
                        else -> groups.getOrPut(c) { Group(c, g.hidden, g.bonk) }
                    }
                    val p = Piece(g.spike, g.dir, x.toFloat(), y.toFloat(), group)
                    pieces += p
                    group?.pieces?.add(p)
                }
            }
        }
        val (sx, sy) = spawn ?: error("Level ${level.name.en} has no spawn")
        val (dx, dy) = doorAt ?: error("Level ${level.name.en} has no door")
        player = Player(sx + 0.5f, sy + 1f)
        spawnX = sx + 0.5f
        door = Door(dx - 0.1f, dy + 1f - 1.6f)
        // every attempt starts with the camera on the spawn's room
        showRoom(roomOf(spawnX))
        level.start.forEach(::run)
        updateBlinks()
        updateNet(0f)
        updateCircuits()
    }

    fun group(id: Char): Group = groups[id] ?: error("Level ${level.name.en} has no group '$id'")

    fun step(dt: Float, input: Controls) {
        // the camera moves to another room: like the rematch freeze, the whole world holds still meanwhile
        // (time included), so the new room is fully in view before anything in it moves. Input stays held.
        if (panning) {
            pan(dt)
            return
        }
        time += dt
        ticks++
        tilt = input.tilt.coerceIn(-1f, 1f)
        shaken = input.shake
        input.shake = false
        if (shaken) {
            shakeTime = time
            if (level.usesShake) events += Event.Shake(0.35f)
        }
        idle = if (input.left || input.right || input.jump || input.jumpPressed) 0f else idle + dt
        updateBlinks()
        updateGroups(dt)
        updateDoor(dt)
        updateSaws(dt)
        updateNet(dt)
        updateHardware(dt)
        if (state != WorldState.PLAYING) {
            input.jumpPressed = false
            return
        }
        if (recording) trail.add(player.box.x, player.box.y)
        if (undoable) {
            history.addLast((player.box.x.toRawBits().toLong() shl 32) or (player.box.y.toRawBits().toLong() and 0xFFFFFFFFL))
            while (history.size > HISTORY * Twists.HZ) history.removeFirst()
        }
        updateGhost()
        if (fake != null) {
            input.jumpPressed = false
            if (time - fakeTime >= fakeLength) unfake()
            return
        }
        updateCracks()
        updateTraps(dt)
        updatePlayer(dt, input)
        input.jumpPressed = false
        hop()
        checkHazards()
        if (state == WorldState.PLAYING && fake == null && time >= doorLock && doorReached()) {
            val atDoor = traps.firstOrNull { !it.fired && it.trap.trigger == Trigger.AtDoor }
            if (atDoor != null) {
                atDoor.fired = true
                atDoor.done = true
                sprung += Sprung(atDoor.trap, time, time)
                spring(atDoor.trap)
            } else {
                state = WorldState.WON
                stateTime = time
                events += Event.Won
            }
        }
        // also on the step the player died, so a death just past a room's edge is shown, not hidden off-screen
        if (fake == null && (state == WorldState.PLAYING || (state == WorldState.DEAD && stateTime == time))) followPlayer()
    }

    // ---------- camera ----------

    /**
     * Starts a pan as soon as the player's center is inside another room (walked, hopped or was put there).
     * Only going back over the edge just panned across needs [ROOM_SLACK], so standing in the doorway can't flicker.
     */
    private fun followPlayer() {
        if (rooms == 1) return
        val cx = player.box.cx
        val left = room * ROOM_COLS
        val right = (room + 1) * ROOM_COLS
        val lo = left - if (left == lastSeam) ROOM_SLACK else 0f
        val hi = right + if (right == lastSeam) ROOM_SLACK else 0f
        if (cx in lo..hi) return
        val to = roomOf(cx)
        if (to == room) return
        panDir = if (to > room) 1 else -1
        lastSeam = if (to > room) right else left
        panFrom = camX
        room = to
        panAge = 0f
        panTime = time
        events += Event.Pan(panDir)
    }

    private fun pan(dt: Float) {
        panAge = min(PAN, panAge + dt)
        val f = panAge / PAN
        val e = f * f * (3f - 2f * f)
        val target = (room * ROOM_COLS).toFloat()
        camX = if (panAge >= PAN) target else panFrom + (target - panFrom) * e
    }

    // ---------- meta twists ----------

    /** The HUD pause button was tapped. True if the game should really pause. */
    fun pausePressed(): Boolean = when (pauseTrick) {
        PauseTrick.HONEST, PauseTrick.SWAP -> true
        // during a pan the world is frozen: a real pause, no spike
        PauseTrick.SPIKE -> if (state == WorldState.PLAYING && fake == null && !panning) {
            events += Event.Say(SPIKE_LINE)
            die()
            false
        } else true
        PauseTrick.DODGE -> when {
            time - dodgeTime < Twists.DODGE_TIME -> false
            dodges >= Twists.DODGES -> true
            else -> {
                events += Event.Say(Twists.dodgeLines[dodges])
                dodgeTime = time
                dodges++
                false
            }
        }
    }

    /** The player came back from the pause screen. */
    fun resumed() {
        resumes++
    }

    /** 0 = upright, 1 = upside down; turns over [Twists.TURN] seconds each way. */
    fun viewTurn(): Float {
        val on = ((time - turnFrom) / Twists.TURN).coerceIn(0f, 1f)
        val off = (1f - (time - turnUntil) / Twists.TURN).coerceIn(0f, 1f)
        return min(on, off)
    }

    /** The picture is rolling ([Action.Roll]) right now. */
    val rolling get() = time >= rollFrom && time < rollUntil

    /**
     * How far the rolling picture is shifted up, as a fraction 0..1 of its height. A death catches the hold: the
     * picture settles on the nearest frame within [Twists.ROLL_SETTLE], so the player sees where they died.
     */
    fun viewRoll(): Float {
        if (state != WorldState.DEAD) return rollAt(time)
        val v = rollAt(stateTime)
        val k = ((time - stateTime) / Twists.ROLL_SETTLE).coerceIn(0f, 1f)
        if (k >= 1f) return 0f
        val r = v + (if (v < 0.5f) -v else 1f - v) * k * k * (3f - 2f * k)
        return r - floor(r)
    }

    private fun rollAt(t: Float): Float {
        if (t < rollFrom || t >= rollUntil) return 0f
        val f = (t - rollFrom) / (rollUntil - rollFrom)
        val e = f * f * (3f - 2f * f) * rollLaps
        return e - floor(e)
    }

    private fun updateCracks() {
        for (c in cracks) if (!c.fell && time >= c.time + c.warn && c.crumble) {
            // the breach: the wall crumbles away for good, a passage into the next room
            c.fell = true
            c.group.visible = false
            val b = c.group.pieces
            events += Event.Breach(b.minOf { it.hx }, b.minOf { it.hy }, b.maxOf { it.hx } + 1f, b.maxOf { it.hy } + 1f)
            events += Event.Crash
            events += Event.Shake(0.7f)
            c.extend?.door?.let { (col, row) ->
                // the door slips through the hole into the next room, out of reach until it lands
                door.tx = col - 0.1f
                door.ty = row + 1f - 1.6f
                door.speed = c.extend.doorSpeed
                door.hanging = false
                doorLock = max(doorLock, time + hypot(door.tx - door.box.x, door.ty - door.box.y) / door.speed + 0.05f)
            }
        } else if (!c.fell && time >= c.time + c.warn) {
            c.fell = true
            c.group.mode = GroupMode.FALL
            c.group.vy = 0f
            events += Event.Shake(0.4f)
        }
    }

    private fun updateGhost() {
        val p = past
        val i = ticks - ghostStart
        if (p == null || i !in 0 until p.size) { ghost = null; ghostFrame = -1; return }
        ghostFrame = i
        ghostBox.x = p.x(i)
        ghostBox.y = p.y(i)
        ghost = ghostBox
    }

    private fun unfake() {
        val f = fake ?: return
        fake = null
        f.platforms?.let { if (f.end == FakeEnd.CREDITS) { group(it).visible = true; creditPlatforms = it } }
        f.then.forEach(::run)
        // spat out of the door, back toward the start
        val d = door.box
        val b = player.box
        val dir = if (spawnX < d.cx) -1 else 1
        b.x = d.cx - b.w / 2
        b.y = if (door.hanging) d.y else d.b - b.h
        player.vx = dir * 9f
        player.vy = -12f * gravity
        player.facing = dir
        player.grounded = false
        spawnTime = time
        doorLock = time + Twists.DOOR_LOCK
        events += Event.Unfaked
    }

    // ---------- traps ----------

    private fun triggered(t: Trigger): Boolean {
        val b = player.box
        return when (t) {
            is Trigger.PastX -> b.cx > t.x
            is Trigger.BeforeX -> b.cx < t.x
            is Trigger.Zone -> b.cx in t.x0..t.x1 && b.cy in t.y0..t.y1
            is Trigger.Airborne -> ticks > 1 && !player.grounded && b.cx in t.x0..t.x1
            is Trigger.Landed -> ticks - landTick <= 1 && landX in t.x0..t.x1
            is Trigger.After -> time >= t.seconds
            is Trigger.Idle -> idle >= t.seconds
            Trigger.Shaken -> shaken
            is Trigger.Resumed -> resumes >= t.times
            Trigger.AtDoor -> false
            is Trigger.Touch -> groups[t.group]?.let(::touches) ?: false
            is Trigger.Pressed -> pads.any { it.pad.id == t.pad && it.presses >= t.times }
            is Trigger.Heated -> (heaters[t.group]?.heat ?: 0f) >= t.above
        }
    }

    private fun updateTraps(dt: Float) {
        for (ts in traps) {
            if (ts.done) continue
            if (!ts.fired) {
                if (!triggered(ts.trap.trigger)) continue
                ts.fired = true
                ts.timer = ts.trap.delay
                ts.at = time
            }
            ts.timer -= dt
            if (ts.timer <= 0f) {
                ts.done = true
                sprung += Sprung(ts.trap, ts.at, time)
                spring(ts.trap)
            }
        }
    }

    /** What the trap that is going off acts on, for its card ([Event.Played]); null outside [spring]. */
    private var springing: Area? = null

    /** Runs the actions of [trap], telling the card (or, without one, a card still in the air) what it acts on. */
    private fun spring(trap: Trap) {
        val area = Area.union(trap.actions.map(::footprint))
        springing = area
        try { trap.actions.forEach(::run) } finally { springing = null }
        if (area != null && trap.actions.none { it is Action.Play || it is Action.Bluff }) events += Event.Target(area)
    }

    /** Where [a] acts (global tiles), as it is about to run: what a card must not cover. Null for what has no place. */
    fun footprint(a: Action): Area? {
        fun tiles(id: Char): Area? = groups[id]?.pieces?.let { ps -> Area.union(ps.map { Area.of(it.box) }) }
        fun fan(id: Char): Area? = Area.union(fans.filter { it.id == id }.map { Area(it.fan.x0, it.fan.y0, it.fan.x1, it.fan.y1) })
        // things that move are followed for as long as a card flies
        val reach = CARD_REACH
        return when (a) {
            is Action.Fall -> tiles(a.group)?.let { it + it.shift(0f, 2f) }
            is Action.Show -> tiles(a.group)
            is Action.Hide -> tiles(a.group)
            is Action.Blink -> tiles(a.group)
            is Action.Tilt -> tiles(a.group)?.let { it.grow(max(a.left, a.right), 0f) }
            is Action.Belt -> tiles(a.group)?.let { it + it.shift(0f, -1f) }
            is Action.Move -> tiles(a.group)?.let { it + it.shift(a.dx, a.dy) }
            is Action.Chase -> tiles(a.group)?.let { t ->
                val far = a.speed * reach
                Area(t.x0 - min(far, a.left), t.y0, t.x1 + min(far, a.right), t.y1)
            }
            is Action.Saw -> {
                val start = Area(a.x - a.r, a.y - a.r, a.x + a.r, a.y + a.r)
                start + start.shift(a.vx * reach, a.vy * reach)
            }
            is Action.PathSaw -> Area.union(a.points.map { (x, y) -> Area(x - a.r, y - a.r, x + a.r, y + a.r) })
            is Action.Laser -> Area.tile(a.from) + Area.tile(a.to)
            is Action.Portal -> Area.tile(a.from) + Area.tile(a.to)
            is Action.Reroute -> Area.tile(a.to)
            is Action.DoorTo -> Area(a.col - 0.1f, if (a.hanging) a.row.toFloat() else a.row + 1f - 1.6f, a.col + 1.1f, if (a.hanging) a.row + 1.6f else a.row + 1f) + Area.of(door.box)
            is Action.Pad -> Area.tile(a.at)
            is Action.Fan -> Area(a.x0, a.y0, a.x1, a.y1) + Area.tile(a.at)
            is Action.FanSet -> fan(a.id)
            is Action.Power -> Area.union(
                links.filter { it.id == a.id }.flatMap { listOf(Area.tile(it.from), Area.tile(it.to)) } +
                    beams.filter { it.laser.id == a.id }.map { Area.tile(it.laser.from) + Area.tile(it.laser.to) } +
                    listOf(fan(a.id), groups[a.id]?.takeIf { it.belt != null || it.circuit != null }?.let { tiles(it.id) }),
            )
            is Action.Circuit -> tiles(a.group)
            is Action.Clock -> tiles(a.group)
            is Action.Toggle -> Area.union(a.groups.map { tiles(it) })
            is Action.BitFlip -> Area.union(listOf(tiles(a.a), tiles(a.b)))
            is Action.Heat -> tiles(a.group)
            is Action.Heatsink -> Area.union(listOf(tiles(a.group)) + a.cools.map { tiles(it) })
            is Action.HeatSpike -> tiles(a.group)
            is Action.FrameCrack -> Area(a.x0.toFloat(), a.y0.toFloat(), a.x1 + 1f, a.y1 + 1f).let { it + it.shift(0f, 2f) }
            is Action.Extend -> Area(a.x0.toFloat(), a.top.toFloat(), a.x1 + 1f, a.bottom + 1f)
            is Action.Gravity, is Action.Swap, is Action.Say, is Action.Shake, is Action.Play, is Action.Bluff, is Action.Undo,
            is Action.Slope, is Action.FakeWin, is Action.PauseTrap, is Action.Flip, is Action.Roll, is Action.Ghost -> null
        }
    }

    private fun run(a: Action) {
        when (a) {
            is Action.Fall -> group(a.group).apply { if (mode != GroupMode.FALL) { mode = GroupMode.FALL; vy = 0f } }
            is Action.Show -> group(a.group).visible = true
            is Action.Hide -> group(a.group).apply {
                // a floor that opens crumbles away (for the look only: the tiles are gone at once)
                if (visible) pieces.filter { it.solid }.takeIf { it.isNotEmpty() }?.let { ps -> events += Event.Crumble(ps.map { it.box.x to it.box.y }) }
                visible = false
            }
            is Action.Move -> group(a.group).apply { tx = ox + a.dx; ty = oy + a.dy; speed = a.speed; mode = GroupMode.MOVE }
            is Action.Chase -> group(a.group).apply { chase = a; mode = GroupMode.CHASE }
            is Action.Undo -> undo(a.seconds)
            is Action.DoorTo -> {
                door.tx = a.col - 0.1f
                door.ty = if (a.hanging) a.row.toFloat() else a.row + 1f - 1.6f
                door.speed = a.speed
                door.hanging = a.hanging
            }
            is Action.Gravity -> {
                val g = if (a.flipped) -1f else 1f
                if (g != gravity) {
                    gravity = g
                    player.grounded = false
                    player.coyote = 0f
                    events += Event.Flip
                }
            }
            is Action.Swap -> swapped = a.on
            is Action.Saw -> saws += Saw(a.x, a.y, a.vx, a.vy, a.r)
            is Action.Say -> events += Event.Say(a.text)
            is Action.Shake -> events += Event.Shake(a.amount)
            is Action.Bluff -> {
                lastCard = Card.BLUFF
                events += Event.Played(a.card, bluff = true, area = springing)
            }
            is Action.Play -> {
                lastCard = a.card
                events += Event.Played(a.card, area = springing)
            }
            is Action.Blink -> group(a.group).apply { blink = a; blinkT0 = time }
            is Action.PathSaw -> a.at(0f).let { (x, y) -> saws += Saw(x, y, 0f, 0f, a.r, a, time) }
            is Action.Tilt -> group(a.group).tilt = a
            is Action.Portal -> {
                requireFree(a.from); requireFree(a.to)
                links.removeAll { it.id == a.id }
                links += Link(a.id, a.from, a.to, a.twoWay)
            }
            is Action.Reroute -> link(a.id).apply {
                requireFree(a.to)
                if (hopBlock == to) hopBlock = null
                oldTo = to
                to = a.to
                rerouteTime = time
            }
            is Action.Belt -> group(a.group).belt = a.speed
            is Action.Laser -> {
                beams.removeAll { it.laser.id == a.id }
                beams += Beam(a, time).also { it.update(time) }
            }
            is Action.Power -> {
                var found = false
                links.filter { it.id == a.id }.forEach { it.on = a.on; found = true }
                beams.filter { it.laser.id == a.id }.forEach { b ->
                    if (a.on && !b.on) { b.t0 = time; b.warm = Action.Laser.TELEGRAPH }
                    b.on = a.on
                    b.update(time)
                    found = true
                }
                groups[a.id]?.takeIf { it.belt != null }?.let { it.beltOn = a.on; found = true }
                circuits[a.id]?.let { it.set(a.on); found = true }
                fans.filter { it.id == a.id }.forEach { it.on = a.on; found = true }
                require(found) { "Level ${level.name.en} has no portal, laser, belt, circuit or fan '${a.id}'" }
            }
            is Action.Circuit -> circuit(a.group, create = true).set(a.on)
            is Action.Clock -> circuit(a.group, create = true).apply { clock = a; clockT0 = time }
            is Action.Toggle -> for (id in a.groups) circuit(id).let { it.set(!it.wants(time)) }
            is Action.BitFlip -> {
                val x = circuit(a.a)
                val y = circuit(a.b)
                val v = x.wants(time)
                x.set(y.wants(time))
                y.set(v)
            }
            is Action.Pad -> {
                requireFree(a.at, "Pad")
                a.circuits.forEach { circuit(it) }
                pads.removeAll { it.pad.id == a.id }
                pads += Switch(a)
            }
            is Action.Heat -> heaters[a.group]?.let { it.spec = a } ?: heaters.set(a.group, Heater(group(a.group), a, declared = true))
            is Action.Heatsink -> sinks += Sink(group(a.group), a.cools)
            is Action.HeatSpike -> heaters.getOrPut(a.group) { Heater(group(a.group), Action.Heat(a.group), declared = false) }.heat = a.to.coerceIn(0f, 1f)
            is Action.Fan -> {
                fans.removeAll { it.id == a.id }
                // fans of the level start already run; later ones spin up
                fans += Blower(a, time).also { if (ticks == 0) it.wind = it.want(time) }
            }
            is Action.FanSet -> {
                val f = fans.filter { it.id == a.id }
                require(f.isNotEmpty()) { "Level ${level.name.en} has no fan '${a.id}'" }
                f.forEach { it.target = a.speed }
            }
            is Action.Slope -> slope = a.speed
            is Action.FakeWin -> if (fake == null) {
                fake = a
                fakeTime = time
                player.vx = 0f
                player.vy = 0f
                credits = if (a.end == FakeEnd.CREDITS) Twists.creditLines(pieces, a.platforms, cols) else emptyList()
                fakeLength = Twists.FAKE_DELAY + if (a.end == FakeEnd.CREDITS) Twists.creditsLength(credits, rows) else Twists.FAKE_CLEAR
                events += Event.FakeWon
            }
            is Action.PauseTrap -> pauseTrick = a.trick
            is Action.FrameCrack -> crackGroups[a]?.let { g ->
                if (cracks.none { it.group === g }) {
                    cracks += Crack(g, time, a.warn)
                    events += Event.Shake(0.25f)
                }
            }
            is Action.Extend -> breachGroups[Triple(a.into, a.top, a.bottom)]?.let { g ->
                if (cracks.none { it.group === g }) {
                    cracks += Crack(g, time, a.warn, a)
                    // no stepping through the door while the wall cracks
                    doorLock = max(doorLock, time + a.warn + 0.05f)
                    events += Event.Extended
                    events += Event.Shake(0.35f)
                    a.line?.let { events += Event.Say(it) }
                }
            } ?: error("Level ${level.name.en} has no wall to break open into room ${a.into} (rows ${a.top}..${a.bottom} must be '#')")
            is Action.Flip -> {
                val v = viewTurn()
                turnFrom = time - v * Twists.TURN
                turnUntil = time + a.seconds
            }
            is Action.Roll -> {
                rollFrom = time
                rollUntil = time + a.seconds
                rollLaps = a.laps
            }
            is Action.Ghost -> ghostStart = ticks + Math.round(a.delay * Twists.HZ)
        }
    }

    // ---------- network: portals, belts, lasers ----------

    private fun link(id: Char) = links.firstOrNull { it.id == id } ?: error("Level ${level.name.en} has no portal '$id'")

    private fun requireFree(c: Pair<Int, Int>, what: String = "Portal") {
        val (x, y) = c
        require(x in 0 until cols && y in 0 until rows && level.map.grid[y][x] == '.') { "$what tile $c in ${level.name.en} is not empty" }
    }

    private fun updateNet(dt: Float) {
        for (g in groups.values) if (g.belt != null) g.beltRun += g.beltSpeed * dt
        for (b in beams) b.update(time)
    }

    /** Walking into a portal tile pops the player out of the other end, same velocity, same spot within the tile. */
    private fun hop() {
        if (state != WorldState.PLAYING) return
        val b = player.box
        val here = Net.cell(b)
        if (hopBlock != null && here != hopBlock) hopBlock = null
        if (time < hopReady || here == hopBlock) return
        for (l in links) {
            val to = l.exit(here) ?: continue
            Net.place(b, here, to)
            l.hopTime = time
            l.hopA = here
            l.hopB = to
            hopBlock = to
            hopReady = time + Net.COOLDOWN
            player.grounded = false
            player.ground = null
            events += Event.Hop
            return
        }
    }

    // ---------- hardware: circuits, pads, heat, fans ----------

    private fun circuit(id: Char, create: Boolean = false): Circuit = circuits[id] ?: run {
        require(create) { "Level ${level.name.en} has no circuit '$id'" }
        val g = group(id)
        Circuit(g, g.visible).also { circuits[id] = it; g.circuit = it }
    }

    private fun touches(g: Group): Boolean {
        val b = player.box
        return g.visible && g.pieces.any { it.solid && b.overlaps(it.box.x - 0.06f, it.box.y - 0.06f, 1.12f, 1.12f) }
    }

    private fun standingOn(g: Group) = state == WorldState.PLAYING && player.grounded && player.ground?.group === g

    private fun updateHardware(dt: Float) {
        if (state == WorldState.PLAYING && fake == null) updatePads()
        updateCircuits()
        for (s in sinks) s.active = standingOn(s.group)
        for (h in heaters.values) {
            if (h.melted) continue
            h.standing = standingOn(h.group)
            h.sinking = sinks.any { it.active && h.group.id in it.cools }
            val before = h.heat
            h.update(dt)
            if (before < Hardware.HOT && h.heat >= Hardware.HOT) events += Event.Sizzle
            if (h.heat >= 1f && h.spec.melt) {
                h.melted = true
                h.group.visible = false
                events += Event.Sizzle
                events += Event.Shake(0.3f)
            }
        }
        for (f in fans) if (f.update(time, dt)) events += Event.Hum
    }

    /** Pads switch their circuits on the step onto them (and, holding, on the step off). */
    private fun updatePads() {
        for (s in pads) {
            val down = s.pressedBy(player.box)
            if (down == s.down) continue
            s.down = down
            s.time = time
            if (down) {
                s.presses++
                events += Event.Switch
            }
            for (id in s.pad.circuits) {
                val c = circuit(id)
                when (s.pad.mode) {
                    PadMode.TOGGLE -> if (down) c.set(!c.wants(time))
                    PadMode.HOLD -> c.set(!c.wants(time))
                    PadMode.ON -> if (down) c.set(true)
                    PadMode.OFF -> if (down) c.set(false)
                }
            }
        }
    }

    /** Circuits follow their switches and clocks, but a rail never comes back inside the player. */
    private fun updateCircuits() {
        for (c in circuits.values) {
            val g = c.group
            val want = c.wants(time)
            if (want && !g.visible) {
                val inside = !c.trace && state == WorldState.PLAYING && g.pieces.any { player.box.overlaps(it.box) }
                if (!inside) { g.visible = true; c.flipTime = time }
            } else if (!want && g.visible) {
                g.visible = false
                c.flipTime = time
            }
            val clock = c.clock
            c.warn = if (g.visible && clock != null) clock.timing.warnAt(time - c.clockT0) else 0f
            c.soon = !g.visible && (want || (clock != null && clock.timing.soonAt(time - c.clockT0)))
        }
    }

    // ---------- moving things ----------

    /** Blinking groups follow their clock, but never reappear inside the player. */
    private fun updateBlinks() {
        for (g in groups.values) {
            val b = g.blink ?: continue
            val t = time - g.blinkT0
            val on = b.solidAt(t)
            if (on && !g.visible) {
                val inside = state == WorldState.PLAYING && g.pieces.any { !it.spike && player.box.overlaps(it.box) }
                if (!inside) g.visible = true
            } else if (!on) g.visible = false
            g.warn = if (g.visible) b.warnAt(t) else 0f
            g.soon = !g.visible && (on || b.soonAt(t))
        }
    }

    private fun updateGroups(dt: Float) {
        for (g in groups.values) {
            when (g.mode) {
                GroupMode.IDLE -> g.tilt?.let { tiltGroup(g, it, dt) }
                GroupMode.FALL -> fall(g, dt)
                GroupMode.MOVE -> {
                    val dx = g.tx - g.ox
                    val dy = g.ty - g.oy
                    val dist = hypot(dx, dy)
                    val stepLen = g.speed * dt
                    val (mx, my) = if (dist <= stepLen) {
                        g.mode = GroupMode.IDLE
                        dx to dy
                    } else {
                        dx / dist * stepLen to dy / dist * stepLen
                    }
                    shift(g, mx, my)
                }
                GroupMode.CHASE -> {
                    val c = g.chase ?: continue
                    val target = (player.box.cx - g.homeX).coerceIn(-c.left, c.right)
                    val d = target - g.ox
                    if (abs(d) > EPS) shift(g, sign(d) * min(abs(d), c.speed * dt), 0f, soft = true)
                }
            }
        }
    }

    /** Puts Bevel back [seconds] into the [history], at the latest spot that is free now. */
    private fun undo(seconds: Float) {
        if (history.isEmpty()) return
        val back = (seconds * Twists.HZ).toInt().coerceIn(0, history.size - 1)
        var i = history.size - 1 - back
        while (i < history.size) {
            val v = history[i]
            val x = Float.fromBits((v ushr 32).toInt())
            val y = Float.fromBits(v.toInt())
            val probe = Box(x, y, player.box.w, player.box.h)
            if (pieces.none { it.solid && probe.overlaps(it.box) }) {
                player.box.x = x
                player.box.y = y
                player.vx = 0f
                player.vy = 0f
                history.clear()
                events += Event.Rewind
                events += Event.Shake(0.3f)
                return
            }
            i++
        }
    }

    private fun tiltGroup(g: Group, a: Action.Tilt, dt: Float) {
        val target = if (tilt < 0f) tilt * a.left else tilt * a.right
        val d = target - g.ox
        if (d != 0f) shift(g, sign(d) * min(abs(d), a.speed * dt), 0f, soft = true)
    }

    /**
     * Moves a group, carrying a player who stands on it and shoving one in its way. [soft]: a carried player
     * stops at walls and the group slides on underneath instead of crushing them.
     */
    private fun shift(g: Group, mx: Float, my: Float, soft: Boolean = false) {
        val carried = player.grounded && player.ground?.group === g
        g.ox += mx
        g.oy += my
        g.pieces.forEach { it.sync() }
        if (carried && state == WorldState.PLAYING) {
            if (soft) moveX(mx, skip = g) else player.box.x += mx
            player.box.y += my
        }
        pushPlayer(g, mx, my)
    }

    private fun fall(g: Group, dt: Float) {
        g.vy = min(g.vy + Physics.BLOCK_GRAVITY * dt, 40f)
        var dy = g.vy * dt
        var landed = false
        for (p in g.pieces) {
            if (p.spike) continue
            for (s in pieces) {
                if (s.group === g || !s.solid) continue
                val horizontal = p.box.x < s.box.r - EPS && p.box.r > s.box.x + EPS
                if (horizontal && s.box.y >= p.box.b - EPS && s.box.y - p.box.b < dy) {
                    dy = max(0f, s.box.y - p.box.b)
                    landed = true
                }
            }
        }
        g.oy += dy
        g.pieces.forEach { it.sync() }
        pushPlayer(g, 0f, dy)
        if (landed) {
            g.mode = GroupMode.IDLE
            g.vy = 0f
            events += Event.Crash
            events += Event.Shake(0.6f)
        } else if (g.oy > rows + 4) {
            g.mode = GroupMode.IDLE
            g.visible = false
        }
    }

    /** A moving group shoves the player out of its way; being shoved into something solid is fatal. */
    private fun pushPlayer(g: Group, dx: Float, dy: Float) {
        if (state != WorldState.PLAYING || !g.visible) return
        val b = player.box
        for (p in g.pieces) {
            if (!p.solid || !b.overlaps(p.box)) continue
            when {
                dy > 0f -> b.y = p.box.b
                dy < 0f -> b.y = p.box.y - b.h
                dx > 0f -> b.x = p.box.r
                dx < 0f -> b.x = p.box.x - b.w
            }
        }
        if (pieces.any { it.solid && b.overlaps(it.box) }) die()
    }

    private fun updateDoor(dt: Float) {
        if (!door.moving) return
        val dx = door.tx - door.box.x
        val dy = door.ty - door.box.y
        val dist = hypot(dx, dy)
        val s = door.speed * dt
        if (dist <= s) {
            door.box.x = door.tx
            door.box.y = door.ty
        } else {
            door.box.x += dx / dist * s
            door.box.y += dy / dist * s
        }
    }

    private fun updateSaws(dt: Float) {
        for (s in saws) {
            val path = s.path
            if (path == null) {
                s.x += s.vx * dt
                s.y += s.vy * dt
                s.angle += dt * 14f * (if (s.vx < 0) -1 else 1)
            } else {
                val (x, y) = path.at(time - s.t0)
                val dx = x - s.x
                s.angle += dt * 14f * (if (dx < 0f || (dx == 0f && y < s.y)) -1 else 1)
                s.x = x
                s.y = y
            }
        }
        saws.removeAll { it.path == null && (it.x < -4 || it.x > cols + 4 || it.y < -4 || it.y > rows + 4) }
    }

    // ---------- player ----------

    private fun updatePlayer(dt: Float, input: Controls) {
        val p = player
        var dir = (if (input.right) 1 else 0) - (if (input.left) 1 else 0)
        if (swapped) dir = -dir
        // upside down, left and right follow the screen
        if (viewTurn() >= 0.5f) dir = -dir
        if (dir != 0) p.facing = dir

        val target = dir * Physics.RUN
        val rate = when {
            dir == 0 && p.grounded -> Physics.FRICTION
            p.grounded -> Physics.ACCEL_GROUND
            else -> Physics.ACCEL_AIR
        }
        val diff = target - p.vx
        p.vx += sign(diff) * min(abs(diff), rate * dt)

        p.coyote = if (p.grounded) Physics.COYOTE else p.coyote - dt
        p.buffer = if (input.jumpPressed) Physics.BUFFER else p.buffer - dt
        if (p.buffer > 0f && p.coyote > 0f) {
            p.vy = -Physics.JUMP_SPEED * gravity
            p.buffer = 0f
            p.coyote = 0f
            p.grounded = false
            p.squash = 1.3f
            events += Event.Jump
        }

        // fans: sideways they drift, up or down they take over from gravity
        var drift = 0f
        var grip = 0f
        var lift = 0f
        for (f in fans) if (f.blows(p.box)) {
            drift += f.drift
            if (f.grip > grip) { grip = f.grip; lift = f.lift }
        }

        val rising = p.vy * gravity < 0f
        val g = Physics.GRAVITY * (if (rising && !input.jump) Physics.JUMP_CUT else 1f)
        p.vy += g * gravity * dt * (1f - grip)
        if (grip > 0f) p.vy += sign(lift - p.vy) * min(abs(lift - p.vy), Hardware.LIFT * grip * dt)
        if (p.vy * gravity > Physics.MAX_FALL) p.vy = Physics.MAX_FALL * gravity

        val belt = beltUnderfoot()
        moveX((p.vx + tilt * slope + belt + drift) * dt)
        moveY(p.vy * dt)
        p.squash += (1f - p.squash) * min(1f, dt * 12f)
    }

    /**
     * The speed of the belt the player stands on: of all belt tiles under their feet, the one under most of them. Any
     * part of the feet on a running belt is carried, from either side: [Player.ground] is just the first tile found
     * (the left one), which let a belt running left drop the player the moment they touched the tile beside it,
     * short of whatever waits at its end, while a belt running right carried them all the way off.
     */
    private fun beltUnderfoot(): Float {
        val p = player
        if (!p.grounded) return 0f
        val b = p.box
        val feet = if (gravity > 0f) b.b else b.y
        var speed = 0f
        var most = EPS
        for (g in groups.values) {
            if (g.belt == null) continue
            for (s in g.pieces) {
                if (!s.solid) continue
                val top = if (gravity > 0f) s.box.y else s.box.b
                if (abs(top - feet) > 0.01f) continue
                val over = min(b.r, s.box.r) - max(b.x, s.box.x)
                if (over > most) { most = over; speed = g.beltSpeed }
            }
        }
        return speed
    }

    private fun moveX(dx: Float, skip: Group? = null) {
        if (dx == 0f) return
        val b = player.box
        b.x += dx
        for (s in pieces) {
            if (!s.solid || (skip != null && s.group === skip) || !b.overlaps(s.box)) continue
            if (dx > 0f) b.x = s.box.x - b.w else b.x = s.box.r
            player.vx = 0f
        }
    }

    private fun moveY(dy: Float) {
        val p = player
        val b = p.box
        val wasGrounded = p.grounded
        p.grounded = false
        p.ground = null
        b.y += dy
        if (dy * gravity < 0f) {
            for (s in pieces) {
                val g = s.group ?: continue
                if (g.bonk && !g.visible && !s.spike && b.overlaps(s.box)) {
                    g.visible = true
                    events += Event.Bonk
                }
            }
        }
        for (s in pieces) {
            if (!s.solid || !b.overlaps(s.box)) continue
            if (dy > 0f) b.y = s.box.y - b.h else b.y = s.box.b
            if (dy * gravity > 0f) {
                p.grounded = true
                p.ground = s
            }
            p.vy = 0f
        }
        if (p.grounded && !wasGrounded) {
            p.squash = 0.72f
            events += Event.Land
            if (ticks > 1) { landTick = ticks; landX = b.cx }
        }
    }

    // ---------- outcomes ----------

    private fun checkHazards() {
        val b = player.box
        if (pieces.any { it.spike && it.visible && it.group?.circuit == null && it.hurts(b) }) return die()
        for (c in circuits.values) if (c.trace && c.powered && c.group.pieces.any { traceHits(it, b) }) return die()
        for (h in heaters.values) if (!h.melted && h.heat >= 1f && touches(h.group)) return die()
        ghost?.let { g -> if (b.overlaps(g.x + GHOST_INSET, g.y + GHOST_INSET, g.w - 2 * GHOST_INSET, g.h - 2 * GHOST_INSET)) return die() }
        for (l in beams) if (l.hits(b)) return die()
        for (s in saws) {
            val nx = s.x.coerceIn(b.x, b.r)
            val ny = s.y.coerceIn(b.y, b.b)
            if (hypot(s.x - nx, s.y - ny) < s.r * 0.85f) return die()
        }
        if (b.y > rows + 1 || b.b < -1) die()
    }

    private fun traceHits(p: Piece, b: Box): Boolean {
        val i = Hardware.TRACE_INSET
        return b.overlaps(p.box.x + i, p.box.y + i, 1f - 2 * i, 1f - 2 * i)
    }

    private fun doorReached(): Boolean {
        val d = door.box
        return player.box.overlaps(d.x + 0.3f, d.y + 0.3f, d.w - 0.6f, d.h - 0.3f)
    }

    private fun die() {
        if (state != WorldState.PLAYING) return
        state = WorldState.DEAD
        stateTime = time
        events += Event.Died(player.box.cx, player.box.cy)
        events += Event.Shake(1f)
    }

    companion object {
        /** Seconds a pan from one room to the next takes; the world is frozen meanwhile. */
        const val PAN = 0.5f
        /** How far (tiles) the player's center must be past a room's edge before the camera follows. */
        const val ROOM_SLACK = 0.15f
        /** Groups made for [Action.FrameCrack] pieces get ids from the private use area, clear of map letters. */
        private const val CRACK_ID = '\uE000'
        /** Groups made for [Action.Extend] breaches, likewise. */
        private const val BREACH_ID = '\uE100'
        private const val GHOST_INSET = 0.12f
        /** Seconds a moving trap is followed for its [footprint]: about as long as a card flies. */
        const val CARD_REACH = 2.2f
        /** Seconds of position history kept for [Action.Undo]. */
        private const val HISTORY = 4f
        private val SPIKE_LINE = T("Pause? Ours come with spikes.", "Pause? Gibt's hier nur mit Stacheln.")
    }
}
