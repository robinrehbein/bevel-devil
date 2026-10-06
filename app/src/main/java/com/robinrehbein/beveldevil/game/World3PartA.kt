package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed

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
    )
}
