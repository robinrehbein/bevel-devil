package com.robinrehbein.beveldevil.game

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sign
import kotlin.math.sqrt

/** Phone motion as pure functions: sensor vectors in, tilt and shakes out. */
object Motion {
    /** Tilt below this many degrees counts as level. */
    const val DEAD_DEG = 3f
    /** Tilt at this many degrees is full. */
    const val FULL_DEG = 22f
    /** Surface.ROTATION_* values, mirrored so this stays free of Android. */
    const val ROT_0 = 0
    const val ROT_90 = 1
    const val ROT_180 = 2
    const val ROT_270 = 3

    /**
     * Sideways component of a gravity/accelerometer reading ([x], [y] in device axes, any unit) along the
     * screen's x axis for display [rotation]. Positive when the screen's right edge points up.
     */
    fun screenX(x: Float, y: Float, rotation: Int): Float = when (rotation) {
        ROT_90 -> -y
        ROT_180 -> -x
        ROT_270 -> y
        else -> x
    }

    /**
     * Roll of the phone as seen on screen: -1 (left edge down) .. 1 (right edge down), with a dead zone,
     * from a gravity reading ([x], [y], [z] in device axes) and the display [rotation].
     */
    fun tilt(x: Float, y: Float, z: Float, rotation: Int): Float {
        val g = sqrt(x * x + y * y + z * z)
        if (g < 1e-3f) return 0f
        // the sensor reports the push against gravity, so the lower edge reads negative
        val s = (-screenX(x, y, rotation) / g).coerceIn(-1f, 1f)
        val deg = Math.toDegrees(asin(s).toDouble()).toFloat()
        val m = ((abs(deg) - DEAD_DEG) / (FULL_DEG - DEAD_DEG)).coerceIn(0f, 1f)
        return sign(deg) * m
    }
}

/** Reports a shake on a short, hard jolt; a second jolt within [cooldown] seconds is the same shake. */
class ShakeDetector(private val threshold: Float = 2.2f, private val cooldown: Double = 0.8) {
    private var last = -1e9

    /** Feeds an accelerometer reading in m/s² at [seconds]; true once per shake. */
    fun feed(x: Float, y: Float, z: Float, seconds: Double): Boolean {
        val g = sqrt(x * x + y * y + z * z) / 9.81f
        if (g < threshold) return false
        if (seconds - last < cooldown) { last = seconds; return false }
        last = seconds
        return true
    }
}
