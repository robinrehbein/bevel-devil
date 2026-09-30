package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Slope
import com.robinrehbein.beveldevil.game.Action.Tilt
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Shaken

/** One tiny level per engine mechanic, proving each with the Bot (not part of the game's levels). */
object Demo {
    private fun MapBuilder.ends() { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }

    /** A bridge over a pit that is there 1.8 s, gone 1 s. */
    val blink = Level(T("Blink", "Blinken"), T("Now you see it.", "Mal da, mal nicht."), start = listOf(Blink('a', on = 1.8f, off = 1f))) {
        ends()
        fill(11..20, 15..17, '.')
        fill(11..20, 15..15, 'a')
    }

    /** A wall that blinks: stand in its gap and it waits for you to leave. */
    val blinkWall = Level(T("Blink wall", "Blinkwand"), T("Wait for it.", "Warte kurz."), start = listOf(Blink('a', on = 1f, off = 1f))) {
        ends()
        fill(10..10, 1..14, 'a')
    }

    /** A saw that bobs up and down across the path, then a looping one around a block. */
    val pathSaw = Level(
        T("Pendulum", "Pendel"), T("Mind the gap. The moving one.", "Achte auf die Lücke. Die bewegliche."),
        start = listOf(
            PathSaw(6f, 16f to 14.4f, 16f to 9f, delay = 1.8f),
            PathSaw(4f, 23f to 11f, 26f to 11f, 26f to 13f, 23f to 13f, loop = true),
        ),
    ) {
        ends()
        fill(24..25, 12..12)
    }

    /** Standing still for 1.2 s drops the floor under the spawn. */
    val idle = Level(
        T("Heisenbug", "Heisenbug"), T("Don't just stand there.", "Steh nicht nur rum."),
        traps = listOf(trap(Idle(1.2f), Fall('a'), Say(T("Observed. Collapsed.", "Beobachtet. Kollabiert.")))),
    ) {
        ends()
        fill(1..4, 15..17, 'a')
    }

    /** A pit too wide to jump; the platform on its left edge slides with the phone. */
    val tilt = Level(T("Spirit level", "Wasserwaage"), T("Tilt the world.", "Neig die Welt."), start = listOf(Tilt('a', left = 0f, right = 13f, speed = 6f))) {
        ends()
        fill(8..23, 15..17, '.')
        fill(8..10, 15..15, 'a')
    }

    /** A gap too wide to jump: tilt right and the slope carries the jump across. */
    val slope = Level(T("Downhill", "Bergab"), T("Lean into it.", "Leg dich rein."), start = listOf(Slope(8f))) {
        ends()
        fill(10..16, 15..17, '.')
    }

    /** A wall to the ceiling that only a shake brings down. */
    val shake = Level(
        T("Shake it", "Schüttel dich"), T("Have you tried shaking it?", "Schon mal geschüttelt?"),
        traps = listOf(trap(Shaken, Hide('a'), Say(T("Hey! Stop that!", "He! Lass das!")))),
    ) {
        ends()
        fill(20..20, 1..14, 'a')
    }

    val all = listOf(blink, blinkWall, pathSaw, idle, tilt, slope, shake)
}
