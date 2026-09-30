package com.robinrehbein.beveldevil.game

import kotlin.math.abs
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

enum class GroupMode { IDLE, FALL, MOVE }

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
    data class Played(val card: Card) : Event
}

enum class WorldState { PLAYING, DEAD, WON }

/** One attempt at a level. Pure game logic, no Android. */
class World(val level: Level) {
    val cols = level.cols
    val rows = level.rows
    val pieces = ArrayList<Piece>()
    val groups = LinkedHashMap<Char, Group>()
    val saws = ArrayList<Saw>()
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

    private class TrapState(val trap: Trap) {
        var fired = false
        var done = false
        var timer = 0f
    }

    private val traps = level.traps.map { TrapState(it) }

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
                    val group = if (c == '#' || c in "^v<>") null else groups.getOrPut(c) { Group(c, g.hidden, g.bonk) }
                    val p = Piece(g.spike, g.dir, x.toFloat(), y.toFloat(), group)
                    pieces += p
                    group?.pieces?.add(p)
                }
            }
        }
        val (sx, sy) = spawn ?: error("Level ${level.name.en} has no spawn")
        val (dx, dy) = doorAt ?: error("Level ${level.name.en} has no door")
        player = Player(sx + 0.5f, sy + 1f)
        door = Door(dx - 0.1f, dy + 1f - 1.6f)
        level.start.forEach(::run)
        updateBlinks()
    }

    fun group(id: Char): Group = groups[id] ?: error("Level ${level.name.en} has no group '$id'")

    fun step(dt: Float, input: Controls) {
        time += dt
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
        if (state != WorldState.PLAYING) {
            input.jumpPressed = false
            return
        }
        updateTraps(dt)
        updatePlayer(dt, input)
        input.jumpPressed = false
        checkHazards()
        if (state == WorldState.PLAYING && doorReached()) {
            state = WorldState.WON
            stateTime = time
            events += Event.Won
        }
    }

    // ---------- traps ----------

    private fun triggered(t: Trigger): Boolean {
        val b = player.box
        return when (t) {
            is Trigger.PastX -> b.cx > t.x
            is Trigger.BeforeX -> b.cx < t.x
            is Trigger.Zone -> b.cx in t.x0..t.x1 && b.cy in t.y0..t.y1
            is Trigger.After -> time >= t.seconds
            is Trigger.Idle -> idle >= t.seconds
            Trigger.Shaken -> shaken
            is Trigger.Touch -> {
                val g = groups[t.group] ?: return false
                g.visible && g.pieces.any { it.solid && b.overlaps(it.box.x - 0.06f, it.box.y - 0.06f, 1.12f, 1.12f) }
            }
        }
    }

    private fun updateTraps(dt: Float) {
        for (ts in traps) {
            if (ts.done) continue
            if (!ts.fired) {
                if (!triggered(ts.trap.trigger)) continue
                ts.fired = true
                ts.timer = ts.trap.delay
            }
            ts.timer -= dt
            if (ts.timer <= 0f) {
                ts.done = true
                ts.trap.actions.forEach(::run)
            }
        }
    }

    private fun run(a: Action) {
        when (a) {
            is Action.Fall -> group(a.group).apply { if (mode != GroupMode.FALL) { mode = GroupMode.FALL; vy = 0f } }
            is Action.Show -> group(a.group).visible = true
            is Action.Hide -> group(a.group).visible = false
            is Action.Move -> group(a.group).apply { tx = ox + a.dx; ty = oy + a.dy; speed = a.speed; mode = GroupMode.MOVE }
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
            is Action.Play -> {
                lastCard = a.card
                events += Event.Played(a.card)
            }
            is Action.Blink -> group(a.group).apply { blink = a; blinkT0 = time }
            is Action.PathSaw -> a.at(0f).let { (x, y) -> saws += Saw(x, y, 0f, 0f, a.r, a, time) }
            is Action.Tilt -> group(a.group).tilt = a
            is Action.Slope -> slope = a.speed
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
            }
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

        val rising = p.vy * gravity < 0f
        val g = Physics.GRAVITY * (if (rising && !input.jump) Physics.JUMP_CUT else 1f)
        p.vy += g * gravity * dt
        if (p.vy * gravity > Physics.MAX_FALL) p.vy = Physics.MAX_FALL * gravity

        moveX((p.vx + tilt * slope) * dt)
        moveY(p.vy * dt)
        p.squash += (1f - p.squash) * min(1f, dt * 12f)
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
        }
    }

    // ---------- outcomes ----------

    private fun checkHazards() {
        val b = player.box
        if (pieces.any { it.spike && it.visible && it.hurts(b) }) return die()
        for (s in saws) {
            val nx = s.x.coerceIn(b.x, b.r)
            val ny = s.y.coerceIn(b.y, b.b)
            if (hypot(s.x - nx, s.y - ny) < s.r * 0.85f) return die()
        }
        if (b.y > rows + 1 || b.b < -1) die()
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
}
