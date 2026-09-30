package com.robinrehbein.beveldevil.game

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * A circuit ([Action.Circuit]) at run time. Its group's [Group.visible] is its power as it really is: a rail is
 * solid while visible, a trace live. [on] (or the [clock]) is what the switches want; a rail waits to come back
 * while the player stands in it.
 */
class Circuit(val group: Group, var on: Boolean) {
    val id get() = group.id
    /** An exposed trace (a spike group): deadly while powered, never solid. */
    val trace = group.pieces.all { it.spike }
    var clock: Action.Clock? = null
    var clockT0 = 0f
    val powered get() = group.visible
    /** Clocked and powered: 0 → 1 through the flicker before the power drops. */
    var warn = 0f
    /** Dark, but about to come back (or waiting for the player to step out). */
    var soon = false
    /** [World.time] of the last change of power, for sparks. */
    var flipTime = -9f

    /** What the switches want at [time]. */
    fun wants(time: Float): Boolean = clock?.let { it.timing.solidAt(time - clockT0) } ?: on

    /** Switched by hand: the clock stops. */
    fun set(v: Boolean) {
        clock = null
        on = v
    }
}

/** A pressure pad ([Action.Pad]) at run time. */
class Switch(val pad: Action.Pad) {
    var down = false
    var presses = 0
    /** [World.time] of the last press or release. */
    var time = -9f
    val col get() = pad.at.first
    val row get() = pad.at.second

    /** The player's feet on the pad: the bottom of its tile. */
    fun pressedBy(b: Box) = b.overlaps(col + 0.1f, row + 1f - Hardware.PAD_H, 0.8f, Hardware.PAD_H)
}

/** A heated group ([Action.Heat], or one hit by [Action.HeatSpike]) at run time. */
class Heater(val group: Group, var spec: Action.Heat, /** Declared as a hot plate, so it shows even while cold. */ val declared: Boolean) {
    /** 0 cold .. 1 burning. */
    var heat = 0f
    var melted = false
    /** Stood on or cooled by a sink in the last step, for the look. */
    var standing = false
    var sinking = false

    fun update(dt: Float) {
        heat = when {
            sinking -> max(0f, heat - dt / Hardware.SINK)
            spec.load || standing -> min(1f, heat + dt / spec.rise)
            else -> max(0f, heat - dt / spec.cool)
        }
    }
}

/** A heatsink ([Action.Heatsink]) at run time. */
class Sink(val group: Group, val cools: String) {
    /** The player stands on it. */
    var active = false
}

/** A fan ([Action.Fan]) at run time. Its clock started at [t0]. */
class Blower(val fan: Action.Fan, val t0: Float) {
    /** The wind it is set to (tiles/s, negative sucks), [Action.FanSet]. */
    var target = fan.speed
    var on = true
    /** The wind right now: follows [want] at [Hardware.SPIN] tiles/s². */
    var wind = 0f
    /** Rotor angle and how far the air has moved (tiles), for drawing. */
    var angle = 0f
    var travel = 0f
    val id get() = fan.id

    fun want(time: Float) = if (on && fan.runsAt(time - t0)) target else 0f

    /** Spins toward [want]; true when it just started up from a standstill. */
    fun update(time: Float, dt: Float): Boolean {
        val w = want(time)
        val d = w - wind
        val was = wind
        wind += sign(d) * min(abs(d), Hardware.SPIN * dt)
        angle += wind * dt * 2.2f
        travel += wind * dt
        return was == 0f && wind != 0f
    }

    /** The player's center is in the wind zone. */
    fun blows(b: Box) = wind != 0f && b.cx >= fan.x0 && b.cx <= fan.x1 && b.cy >= fan.y0 && b.cy <= fan.y1

    /** The drift sideways, tiles/s. */
    val drift get() = when (fan.dir) { Dir.RIGHT -> wind; Dir.LEFT -> -wind; else -> 0f }
    /** The vertical speed the wind eases the player to, tiles/s (down is positive). */
    val lift get() = when (fan.dir) { Dir.UP -> -wind; Dir.DOWN -> wind; else -> 0f }
    /** How much it takes over from gravity, 0 (still) .. 1 (at [Hardware.FULL] tiles/s or more). */
    val grip get() = if (fan.vertical) min(1f, abs(wind) / Hardware.FULL) else 0f
}

object Hardware {
    /** Seconds a heatsink needs to drain full heat. */
    const val SINK = 0.35f
    /** Heat from which a plate sizzles and flickers red. */
    const val HOT = 0.75f
    /** How fast fans change speed, tiles/s². */
    const val SPIN = 14f
    /** A vertical wind this strong fully replaces gravity. */
    const val FULL = 4f
    /** How fast a vertical wind brings the player to its speed, tiles/s². */
    const val LIFT = 70f
    /** Height of a pad's sensitive strip at the bottom of its tile. */
    const val PAD_H = 0.3f
    /** Inset of a live trace's deadly core within its tile. */
    const val TRACE_INSET = 0.18f
}
