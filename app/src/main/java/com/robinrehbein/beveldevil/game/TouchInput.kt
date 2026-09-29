package com.robinrehbein.beveldevil.game

import kotlin.math.abs
import kotlin.math.max

enum class Scheme { BUTTONS, STICK }

/**
 * Pure pointer → action mapping (screen px in, held actions out). The screen splits in half: one half is
 * jump, the other movement (swapped when [leftHanded]). Movement is either two buttons split at [splitX]
 * or a floating horizontal stick. A finger that started on movement never becomes jump, and vice versa.
 */
class TouchInput {
    var width = 0f
    /** BUTTONS: x between the left and right button. */
    var splitX = 0f
    var density = 1f
    var scheme = Scheme.BUTTONS
    var leftHanded = false

    var left = false; private set
    var right = false; private set
    var jump = false; private set
    /** Set when a finger lands on the jump half; the consumer clears it. */
    var jumpPressed = false

    /** Stick of the newest movement finger, for drawing. */
    var stickActive = false; private set
    var stickX = 0f; private set
    var stickY = 0f; private set
    var knobX = 0f; private set

    val deadZone get() = DEAD_DP * density
    val stickRadius get() = RADIUS_DP * density

    private class P(val jump: Boolean, var x: Float, val baseY: Float, var baseX: Float, var seq: Int, var dir: Int)
    private val ps = LinkedHashMap<Int, P>()
    private var seq = 0

    private fun onJumpSide(x: Float) = (x >= width / 2f) != leftHanded

    private fun dirOf(p: P): Int =
        if (scheme == Scheme.BUTTONS) (if (p.x < splitX) -1 else 1)
        else {
            val dx = p.x - p.baseX
            if (abs(dx) <= deadZone) 0 else if (dx < 0) -1 else 1
        }

    fun down(id: Int, x: Float, y: Float) {
        val j = onJumpSide(x)
        val p = P(j, x, y, x, ++seq, 0)
        if (j) jumpPressed = true else p.dir = dirOf(p)
        ps[id] = p
        update()
    }

    fun move(id: Int, x: Float) {
        val p = ps[id] ?: return
        if (p.jump) return
        p.x = x
        if (scheme == Scheme.STICK) {
            val r = stickRadius
            if (x - p.baseX > r) p.baseX = x - r else if (x - p.baseX < -r) p.baseX = x + r
        }
        val d = dirOf(p)
        if (d != p.dir) { p.dir = d; p.seq = ++seq }
        update()
    }

    fun up(id: Int) { ps.remove(id); update() }

    fun cancel() { ps.clear(); jumpPressed = false; update() }

    private fun update() {
        var l = 0; var r = 0
        var top: P? = null
        jump = false
        for (p in ps.values) {
            if (p.jump) { jump = true; continue }
            if (top == null || p.seq > top.seq) top = p
            if (p.dir < 0) l = max(l, p.seq) else if (p.dir > 0) r = max(r, p.seq)
        }
        left = l > r
        right = r > l
        stickActive = scheme == Scheme.STICK && top != null
        if (top != null) { stickX = top.baseX; stickY = top.baseY; knobX = top.x.coerceIn(top.baseX - stickRadius, top.baseX + stickRadius) }
    }

    companion object {
        const val DEAD_DP = 8f
        const val RADIUS_DP = 44f
    }
}
