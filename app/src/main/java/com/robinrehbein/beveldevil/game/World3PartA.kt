package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Toggle
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 3, levels 1-8 (block A of the V2 rebuild, docs/LEVEL_DESIGN_V2.md §8/§11): the first copper. Every room is a
 * different shape (a descent, a pillar, two lanes, a shaft ...), and the hardware always does something other than
 * what it just taught.
 */
object World3PartA {
    val levels: List<Level> = listOf(

        // 1 — start high, door low: a descent in two floors. The deck is warm in the middle (hop it), the pad at its far end cuts
        // the live wall on the floor below, but only on a timer, and the way down lands you on a plate that has just been
        // overclocked: hop that one too, leaving it with a jump.
        Level(
            name = T("First Copper", "Erstes Kupfer"),
            intro = T("A button, a wall of live copper, a timer. Even I can explain this.", "Ein Knopf, eine Wand unter Strom, ein Timer. Das kann sogar ich erklären."),
            start = listOf(Circuit('Z'), Pad('1', at = 5 to 5, circuits = "Z", mode = PadMode.OFF)),
            traps = listOf(
                trap(BeforeX(19.5f), HeatSpike('g', 0.95f), say("Complimentary preheating.", "Kostenlose Vorheizung.")),
                trap(Pressed('1'), say("Click. The breaker is open. Do hurry.", "Klick. Die Sicherung ist offen. Beeil dich.")),
                trap(Pressed('1'), Power('Z', true), say("Breaker closed. Hope you weren't attached.", "Sicherung zu. Hoffentlich warst du nicht dran."), delay = 3.4f),
                trap(Landed(2f, 8f), Play(Card.OVERCLOCKED), HeatSpike('h', 0.95f), say("Landing pad: warm, as promised.", "Landeplatz: warm, wie versprochen.")),
            ),
            hint = T("The deck bites the runner. The wire bites the dawdler.", "Das Deck beißt den Läufer. Der Draht beißt den Trödler."),
        ) {
            border(); floor()
            fill(3..30, 6..6)
            fill(15..17, 6..6, 'g')
            fill(6..8, 15..15, 'h')
            wire(15, 'Z', top = 7)
            spawn(30, 5); door(30, 14)
        },

        // 2 — a pillar in the middle of the room: up the stairs, over the top, down the other side. The overhead line above the
        // top lights up for a moment (wait for it at the foot of the line), a pin on the roof of the pillar walks towards
        // you the while (jump it), and the doormat in front of the door has been plugged in.
        Level(
            name = T("Live Wire", "Unter Strom"),
            intro = T("Mind the wires. They mind you.", "Achte auf die Kabel. Die achten auf dich."),
            start = listOf(Circuit('Z', on = false), Circuit('Y', on = false)),
            traps = listOf(
                trap(PastX(1.6f), Move('K', -3.5f, 0f, 2.5f), say("The welcome pin. It comes to you.", "Der Begrüßungsstift. Er kommt zu dir.")),
                trap(Landed(14f, 19f), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Overhead line: live from the second you look up.", "Oberleitung: unter Strom, sobald du hochschaust."), delay = 0.2f),
                trap(Landed(14f, 19f), Power('Z', false), delay = 1.45f),
                trap(Zone(14f, 5.6f, 19f, 7f), Move('S', -7f, 0f, 3f), say("A visiting pin. It followed you home.", "Ein Besucher-Stift. Er ist dir nachgelaufen."), delay = 0.9f),
                trap(Landed(24f, 29f), Power('Y', true), say("The doormat has been plugged in.", "Die Fußmatte wurde eingesteckt."), delay = 0.3f),
            ),
            hint = T("The wire above the pillar glows only for a moment. The pin does not stop.", "Der Draht über dem Pfeiler glüht nur kurz. Der Stift hört nicht auf."),
        ) {
            border(); floor()
            fill(8..9, 13..14); fill(10..11, 11..14); fill(12..13, 9..14)
            fill(14..22, 7..14)
            fill(19..21, 5..6, 'Z')
            put(22, 6, 'S'); put(5, 14, 'K')
            fill(28..29, 14..14, 'Y')
            spawn(1, 14); door(30, 14)
        },
    )
}
