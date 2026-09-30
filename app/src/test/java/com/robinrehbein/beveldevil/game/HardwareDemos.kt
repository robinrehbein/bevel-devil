package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fan
import com.robinrehbein.beveldevil.game.Action.FanSet
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Heatsink
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Trigger.PastX

/** One tiny level per hardware mechanic of World 3, proving each with the Bot (not part of the game's levels). */
object HardwareDemos {
    private fun MapBuilder.ends() { border(); floor(); put(2, 14, 'P'); put(29, 14, 'D') }

    /** A pit bridged by a dead rail; the pad on the way powers it. */
    val circuit = Level(
        T("Circuit", "Stromkreis"), T("Close the circuit.", "Schließ den Stromkreis."),
        start = listOf(Circuit('a', on = false), Pad('1', at = 6 to 14, circuits = "a")),
    ) {
        ends()
        fill(11..20, 15..17, '.')
        fill(11..20, 15..15, 'a')
    }

    /** Two rails over a pit on clocks, out of step: wait for the power, hop across. */
    val clock = Level(
        T("Clock", "Takt"), T("Mind the clock.", "Achte auf den Takt."),
        start = listOf(Clock('a', on = 1.8f, off = 1f), Clock('b', on = 1.8f, off = 1f, phase = 2.3f)),
    ) {
        ends()
        fill(9..22, 15..17, '.')
        fill(9..15, 15..15, 'a')
        fill(16..22, 15..15, 'b')
    }

    /** A live trace from the ceiling to the floor; the pad in front of it cuts the power. */
    val live = Level(
        T("Live wire", "Unter Strom"), T("Don't touch the copper.", "Nicht das Kupfer anfassen."),
        start = listOf(Circuit('Z'), Pad('1', at = 10 to 14, circuits = "Z", mode = PadMode.OFF)),
    ) {
        ends()
        fill(16..16, 1..14, 'Z')
    }

    /** A rail bridge over a short pit; as you step on, Mephi cuts the power. Jump it anyway. */
    val cut = Level(
        T("Power cut", "Stromausfall"), T("Solid copper.", "Massives Kupfer."),
        start = listOf(Circuit('a')),
        traps = listOf(trap(PastX(11.6f), Power('a', false), Say(T("Oops. Power cut.", "Huch. Stromausfall.")))),
    ) {
        ends()
        fill(12..15, 15..17, '.')
        fill(12..15, 15..15, 'a')
    }

    /** A long hot plate with a one-tile heatsink in the middle: stop on it and let the plate cool. */
    val heat = Level(
        T("Hot plate", "Herdplatte"), T("Don't dawdle.", "Nicht trödeln."),
        start = listOf(Heat('h', rise = 1f), Heatsink('k', cools = "h")),
    ) {
        ends()
        fill(8..14, 15..15, 'h')
        fill(15..15, 15..15, 'k')
        fill(16..22, 15..15, 'h')
    }

    /**
     * A chip under load bridges a pit and heats from the start, stood on or not; a heatsink in its middle cools
     * it (and one at the start keeps it cool while you think). Stop on the middle one.
     */
    val chip = Level(
        T("Under load", "Volllast"), T("Everything's running hot.", "Alles läuft heiß."),
        start = listOf(Heat('c', rise = 1.4f, load = true), Heatsink('k', cools = "c")),
    ) {
        ends()
        fill(4..5, 15..15, 'k')
        fill(8..23, 15..17, '.')
        fill(8..14, 15..15, 'c')
        put(15, 15, 'k')
        fill(16..23, 15..15, 'c')
    }

    /** Floor that is just floor, until Mephi overclocks it under you. Keep moving. */
    val overclock = Level(
        T("Overclocked", "Übertaktet"), T("Nice and cool here.", "Schön kühl hier."),
        traps = listOf(trap(PastX(16.5f), HeatSpike('f', 0.6f), Say(T("Overclocked!", "Übertaktet!")))),
    ) {
        ends()
        fill(13..19, 15..15, 'f')
    }

    /** The door sits on a high ledge; a floor fan's updraft lifts you there. */
    val fan = Level(
        T("Updraft", "Aufwind"), T("Catch the draft.", "Nimm den Aufwind."),
        start = listOf(Fan('f', at = 12 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2)),
    ) {
        border(); floor(); put(2, 14, 'P'); put(28, 7, 'D')
        fill(16..30, 8..8)
    }

    /** A pit too wide to jump; a wall fan's wind carries the jump across, then reverses on the other side. */
    val wind = Level(
        T("Tailwind", "Rückenwind"), T("Go with the flow.", "Schwimm mit dem Strom."),
        start = listOf(Fan('w', at = 0 to 9, dir = Dir.RIGHT, reach = 30, speed = 8f, width = 4)),
        traps = listOf(trap(PastX(18f), FanSet('w', -8f), Say(T("Headwind.", "Gegenwind.")))),
    ) {
        ends()
        fill(9..15, 15..17, '.')
    }

    val all = listOf(circuit, clock, live, cut, heat, chip, overclock, fan, wind)
}
