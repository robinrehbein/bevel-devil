package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed

/** Test-only rooms for [DesignRulesTest]: one that follows every V2 rule, and the bad habits the rules exist for. */
object DesignDemos {
    private val hidden = Glyph(spike = true, hidden = true)

    /**
     * A one-screen puzzle room (R1 + R5, U1): the door is behind a copper wall. The switch that cuts the wall lies on
     * the upper floor, back at the far left; the stairs up are at the right, next to the wall. Pressing it drops a
     * piece of the upper floor behind you, so the way back is a hop (or the hole and the lower floor).
     */
    val puzzle = Level(
        name = T("Demo: Back Office", "Demo: Hinterzimmer"),
        intro = T("The door is right there. The switch is not.", "Die Tür ist gleich da. Der Schalter nicht."),
        start = listOf(Circuit('w'), Pad('1', at = 3 to 9, circuits = "w", mode = PadMode.OFF)),
        traps = listOf(
            trap(Pressed('1'), Play(Card.COLLAPSE), Fall('f'), Say(T("Floor plan changed.", "Grundriss geändert."))),
        ),
    ) {
        border(); floor()
        fill(1..22, 10..10)        // the upper floor
        fill(8..9, 10..10, 'f')    // the piece that falls
        fill(22..23, 13..14)       // step A
        fill(24..25, 11..14)       // step B
        fill(26..26, 1..14, 'w')   // the copper wall
        put(2, 14, 'P')
        put(29, 14, 'D')
    }

    /**
     * Round 1 of [puzzle]: up the stairs, back left along the upper floor to the switch, down the new hole, up the
     * stairs again and over the dark wall. Steps and walls stop the bot, so it never has to hit a spot to the pixel.
     */
    val puzzleSolution: Solution = {
        right(3f)                                   // along the lower floor, up against step A
        rightJump(0.35f).landRight()                // onto A
        rightJump(0.35f).landRight()                // onto B, against the wall
        leftJump(0.35f).landLeft()                  // over to the upper floor
        leftTo(2.6f)                                // the switch: wall off, floor gone
        right(3f)                                   // through the hole, along the lower floor to A
        rightJump(0.35f).landRight()
        rightJump(0.35f).landRight()
        right(1.5f)                                 // over where the wall was, into the door
    }

    /**
     * Everything V1 did too often: a straight corridor, two spike popups behind you, [card] played, run through in
     * three seconds. With [heat], the last trap overclocks the floor in front of the door.
     */
    fun corridor(card: Card = Card.SPIKE_SEED, heat: Boolean = false) = Level(
        name = T("Demo: Corridor", "Demo: Flur"),
        intro = T("Run.", "Lauf."),
        legend = mapOf('A' to hidden, 'B' to hidden),
        traps = listOf(
            trap(PastX(10f), Play(card), Show('A')),
            trap(PastX(12f), Show('B')),
        ) + if (heat) listOf(trap(PastX(27f), HeatSpike('g'))) else emptyList(),
    ) {
        border(); floor()
        put(4, 14, 'A'); put(6, 14, 'B')
        fill(27..28, 15..15, 'g')
        put(2, 14, 'P')
        put(29, 14, 'D')
    }

    /** Pixel-perfect: at 2 s spikes cover the floor except one tile, at 3 s they are gone again. */
    val needle = Level(
        name = T("Demo: Needle", "Demo: Nadelöhr"),
        intro = T("Stand exactly there.", "Stell dich genau dahin."),
        legend = mapOf('S' to hidden),
        traps = listOf(
            trap(After(2f), Play(Card.SPIKE_SEED), Show('S')),
            trap(After(3f), Hide('S')),
        ),
    ) {
        border(); floor()
        fill(5..9, 14..14, 'S'); fill(11..25, 14..14, 'S')
        put(2, 14, 'P')
        put(29, 14, 'D')
    }

    /** Door at the far left, spawn at the right; a heat spike at [heatAt], then a harmless last trap. */
    fun leftDoor(heatAt: Trigger) = Level(
        name = T("Demo: Back Door", "Demo: Hintertür"),
        intro = T("Go back.", "Geh zurück."),
        traps = listOf(
            trap(heatAt, HeatSpike('g')),
            trap(PastX(29f), Play(Card.SPIKE_SEED)),
        ),
    ) {
        border(); floor()
        fill(18..21, 15..15, 'g')
        put(28, 14, 'P')
        put(2, 14, 'D')
    }
}
