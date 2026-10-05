package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.*
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Trigger.*

/**
 * World 1, levels 17-32. Act 2, "Neue Regeln": blinking platforms, path saws and the Idle trigger. Levels 17-24 are block B of the
 * V2 rebuild (World1Part2B), levels 25-32 block C (here): every room chains two to four traps, each of which punishes the
 * habit the one before taught. Levels with saws in `start` would show them in the plain-room check, so saws come with a trap.
 */
object World1Part2 {
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    /**
     * The door runs from you in several hops (up, across, down), so it never flies through the player. The hops are chained
     * with delays from their flight times; [first] runs with the first hop.
     */
    private fun flee(trigger: Trigger, fromCol: Int, fromRow: Int, first: List<Action>, vararg hops: DoorTo): List<Trap> {
        var x = fromCol - 0.1f
        var y = fromRow + 1f - 1.6f
        var t = 0f
        return hops.mapIndexed { i, h ->
            val trap = Trap(trigger, (if (i == 0) first else emptyList()) + h, delay = t)
            val nx = h.col - 0.1f
            val ny = h.row + 1f - 1.6f
            t += kotlin.math.hypot(nx - x, ny - y) / h.speed + 0.03f
            x = nx; y = ny
            trap
        }
    }

    val levels: List<Level> = World1Part2B.levels + listOf(
        // 25 — rush hour: the door waits on the far platform behind a bridge of stones over the void. Step on it and it starts to blink;
        // reach for the door and it leaves for the middle of the first platform; come back over the bridge and it leaves again, for the
        // far end behind a second bridge. The way back is the bridge, now with its rhythm
        // MECHANIC: Blink (two bridges) + the one fleeing door of the act
        Level(
            name = T("Rush Hour", "Stoßzeit"),
            intro = T("Hurry! The door closes soon.", "Schnell, schnell! Die Tür schließt gleich."),
            hint = T("The door is not where it was. The bridge is on a timetable.", "Die Tür ist nicht mehr da. Die Brücke hat einen Fahrplan."),
            traps = listOf(
                trap(Touch('a'), Blink('a', on = 1.2f, off = 1.0f), Blink('b', on = 1.2f, off = 1.0f), Say(T("The bridge runs on the hour. The hour is short.", "Die Brücke fährt zur vollen Stunde. Die Stunde ist kurz."))),
                trap(Touch('c'), Blink('c', on = 1.6f, off = 1.2f), Blink('d', on = 1.6f, off = 1.2f), Say(T("Second bridge, same timetable. Delays included.", "Zweite Brücke, gleicher Fahrplan. Verspätungen inklusive."))),
            ) + flee(PastX(26.2f), 29, 7, listOf(Play(Card.SHY_DOOR), Say(T("This door is out of service. Next stop: back there.", "Diese Tür ist außer Betrieb. Nächster Halt: da hinten."))),
                DoorTo(29, 3, speed = 14f), DoorTo(15, 3, speed = 14f), DoorTo(15, 7, speed = 14f)) +
                flee(Landed(25f, 27f), 15, 7, listOf(Say(T("Oh, you came back? I'll move on.", "Du kommst zurück? Dann ziehe ich weiter."))),
                    DoorTo(15, 3, speed = 14f), DoorTo(2, 3, speed = 14f), DoorTo(2, 7, speed = 14f)),
        ) {
            border()
            fill(1..4, 8..8); fill(9..22, 8..8); fill(27..30, 8..8)
            fill(7..8, 9..9, 'c'); fill(5..6, 9..9, 'd')
            fill(23..24, 9..9, 'a'); fill(25..26, 9..9, 'b')
            put(10, 7, 'P'); put(29, 7, 'D')
        },

    ) + World1Part2Old.levels.drop(9)
}
