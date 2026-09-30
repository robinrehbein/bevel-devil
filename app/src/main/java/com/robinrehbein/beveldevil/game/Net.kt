package com.robinrehbein.beveldevil.game

/** Portal pair [Action.Portal] at run time; [to] moves on [Action.Reroute]. */
class Link(val id: Char, val from: Pair<Int, Int>, var to: Pair<Int, Int>, val twoWay: Boolean) {
    var on = true
    /** [World.time] of the last hop, and the tiles it went from and to. */
    var hopTime = -9f
    var hopA = from
    var hopB = to
    /** [World.time] of the last reroute, and where the exit was before it. */
    var rerouteTime = -9f
    var oldTo = to

    /** Where walking into tile [c] leads, or null. */
    fun exit(c: Pair<Int, Int>): Pair<Int, Int>? = when {
        !on -> null
        c == from -> to
        twoWay && c == to -> from
        else -> null
    }
}

/** A laser at run time. Its clock started at [t0]; switched back on, it first warms up for [warm] seconds. */
class Beam(val laser: Action.Laser, var t0: Float) {
    var on = true
    var warm = 0f
    var lit = false
        private set
    /** 0 → 1 while the emitters glow before firing. */
    var warn = 0f
        private set

    fun update(time: Float) {
        val t = time - t0
        lit = on && t >= warm && laser.litAt(t - warm)
        warn = when {
            !on || lit -> 0f
            t < warm -> t / warm
            else -> laser.warnAt(t - warm)
        }
    }

    fun hits(b: Box) = lit && b.overlaps(laser.x0, laser.y0, laser.x1 - laser.x0, laser.y1 - laser.y0)
}

object Net {
    /** Seconds after a hop before any portal takes the player again. */
    const val COOLDOWN = 0.15f

    /** The tile the center of [b] is in. */
    fun cell(b: Box): Pair<Int, Int> = kotlin.math.floor(b.cx).toInt() to kotlin.math.floor(b.cy).toInt()

    /** Puts [b] into tile [to] where its center was in tile [from], kept fully inside the tile. */
    fun place(b: Box, from: Pair<Int, Int>, to: Pair<Int, Int>) {
        b.x = (to.first + b.cx - from.first - b.w / 2).coerceIn(to.first.toFloat(), to.first + 1 - b.w)
        b.y = (to.second + b.cy - from.second - b.h / 2).coerceIn(to.second.toFloat(), to.second + 1 - b.h)
    }
}
