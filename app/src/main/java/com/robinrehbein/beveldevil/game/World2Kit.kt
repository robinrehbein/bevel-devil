package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Say

/** Small helpers for the tile art of world 2 (server racks, LED rows, binary level ids). */

internal fun say(en: String, de: String): Say = Say(T(en, de))

internal fun MapBuilder.spawn(x: Int = 2, y: Int = 14) = put(x, y, 'P')
internal fun MapBuilder.door(x: Int = 29, y: Int = 14) = put(x, y, 'D')

/** Cut a pit into the floor. */
internal fun MapBuilder.pit(xs: IntRange, top: Int = 15) = fill(xs, top..17, '.')

/** Server rack: a block column [w] wide and [h] tall standing on row [base]. */
internal fun MapBuilder.rack(x: Int, w: Int, h: Int, base: Int = 14, c: Char = '#') = fill(x until x + w, base - h + 1..base, c)

/** Blinking LED row: a run of static spikes. */
internal fun MapBuilder.leds(xs: IntRange, y: Int = 14, c: Char = '^') = fill(xs, y..y, c)

/** Ceiling spike row. */
internal fun MapBuilder.drip(xs: IntRange, y: Int = 1) = fill(xs, y..y, 'v')

/** The level number as 7 LEDs in binary on the ceiling (1 = block). Only fills empty cells. */
internal fun MapBuilder.bits(n: Int, x0: Int = 24, y: Int = 1) {
    for (i in 0 until 7) if ((n shr (6 - i)) and 1 == 1 && grid[y][x0 + i] == '.') put(x0 + i, y, '#')
}

/**
 * A door that flees in several hops (up, across the ceiling, down again) so it never
 * flies through the player. Hops are chained with delays computed from the flight time.
 * [first] actions run together with the first hop.
 */
internal fun doorTrail(
    trigger: Trigger,
    fromCol: Int,
    fromRow: Int,
    hops: List<Action.DoorTo>,
    first: List<Action> = emptyList(),
    startDelay: Float = 0f,
): List<Trap> {
    var x = fromCol - 0.1f
    var y = fromRow + 1f - 1.6f
    var t = startDelay
    return hops.mapIndexed { i, h ->
        val trap = Trap(trigger, (if (i == 0) first else emptyList()) + h, delay = t)
        val nx = h.col - 0.1f
        val ny = if (h.hanging) h.row.toFloat() else h.row + 1f - 1.6f
        t += kotlin.math.hypot(nx - x, ny - y) / h.speed + 0.03f
        x = nx; y = ny
        trap
    }
}

/** The opposite of [blink]: a hidden group that shows at [firstShow] for [on] seconds, every [period] seconds. */
internal fun blinkOn(g: Char, firstShow: Float, on: Float, period: Float, count: Int, first: List<Action> = emptyList()): List<Trap> =
    (0 until count).flatMap { i ->
        val t = firstShow + i * period
        listOf(
            Trap(Trigger.After(t), (if (i == 0) first else emptyList()) + Action.Show(g)),
            Trap(Trigger.After(t + on), listOf(Action.Hide(g))),
        )
    }

/** A group that moves back and forth on a timer: out at [t0], back at [t0 + period / 2]... */
internal fun shuttle(g: Char, t0: Float, period: Float, dy: Float, speed: Float, count: Int, dx: Float = 0f): List<Trap> =
    (0 until count).map { i ->
        val s = if (i % 2 == 0) 1f else -1f
        Trap(Trigger.After(t0 + i * period), listOf(Action.Move(g, dx * s, dy * s, speed)))
    }

/** A group that disappears at [firstHide] for [off] seconds, every [period] seconds, [count] times. */
internal fun blink(g: Char, firstHide: Float, off: Float, period: Float, count: Int, first: List<Action> = emptyList()): List<Trap> =
    (0 until count).flatMap { i ->
        val t = firstHide + i * period
        listOf(
            Trap(Trigger.After(t), (if (i == 0) first else emptyList()) + Action.Hide(g)),
            Trap(Trigger.After(t + off), listOf(Action.Show(g))),
        )
    }
