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
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** Test-only rooms for [DesignRulesTest]: one that follows every V2 rule, and the bad habits the rules exist for. */
object DesignDemos {
    private val hidden = Glyph(spike = true, hidden = true)

    /**
     * A one-screen puzzle room (R1 + R5, U1) after recipe v2: a loop, no way walked twice. You start on the upper floor;
     * the door is right below you, behind a copper wall. The switch that cuts the wall lies at the far right of the upper
     * floor; from there the way down is the drop at the right edge and back left along the lower floor. Four real traps
     * in two families (spikes, drop): spikes sprout ahead of you, a hanging block drops on whoever runs under it, the
     * switch drops the block above it, and down below a piece of the floor crumbles under whoever stops on it.
     */
    val puzzle = Level(
        name = T("Demo: Back Office", "Demo: Hinterzimmer"),
        intro = T("The door is right there. The switch is not.", "Die Tür ist gleich da. Der Schalter nicht."),
        legend = mapOf('S' to hidden),
        start = listOf(Circuit('w'), Pad('1', at = 25 to 9, circuits = "w", mode = PadMode.OFF)),
        traps = listOf(
            trap(Zone(6f, 5f, 7.5f, 9.9f), Show('S'), Say(T("Mind your step.", "Pass auf, wo du hintrittst."))),
            trap(PastX(16f), Fall('b')),
            trap(Pressed('1'), Play(Card.COLLAPSE), Fall('c'), Say(T("Floor plan changed.", "Grundriss geändert.")), delay = 0.1f),
            trap(Zone(22f, 11f, 24f, 15.5f), Fall('d'), Say(T("That floor was on loan.", "Der Boden war geliehen.")), delay = 0.4f),
        ),
    ) {
        border(); floor()
        fill(1..28, 10..10)           // the upper floor; the drop is at the right edge
        put(10, 9, 'S')               // sprouts ahead of you
        fill(19..20, 2..2, 'b')       // hangs over the upper floor, drops on whoever runs under it
        fill(24..25, 4..4, 'c')       // hangs over the switch
        fill(22..23, 15..17, 'd')     // crumbles under whoever stops on it
        fill(5..5, 11..14, 'w')       // the copper wall in front of the door
        put(2, 9, 'P')
        put(2, 14, 'D')
    }

    /**
     * Round 1 of [puzzle]: hop the spike that sprouts, stop short of the hanging block and hop it once it lies, over the
     * switch (its block drops behind you) and off the edge, then left along the lower floor without stopping on the
     * crumbling piece, through where the wall was, into the door. Every hazard is one tile wide and every jump starts
     * well clear of it, so no spot has to be hit to the pixel.
     */
    val puzzleSolution: Solution = {
        hopR(8.2f, hold = 0.45f)                                                                  // over the spike
        rightTo(17.2f).waitFor { it.group('b').mode == GroupMode.IDLE && it.group('b').oy > 0f }   // the block lies
        hopR(17.6f)                                                                               // over it
        right(1.5f)                                                                               // over the switch, off the edge
        leftTo(1.5f)                                                                              // over the crumbling piece, into the door
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

    /**
     * Decoration: a block hangs far ahead and drops as soon as you start running. By the time you get there it lies on
     * the floor; whether you wait or run, you just hop it.
     */
    val toothless = Level(
        name = T("Demo: Decoration", "Demo: Deko"),
        intro = T("Something will happen. Eventually.", "Gleich passiert was. Irgendwann."),
        traps = listOf(trap(PastX(4f), Play(Card.HEADBUTT), Fall('b'))),
    ) {
        border(); floor()
        fill(20..21, 12..12, 'b')
        put(2, 14, 'P')
        put(29, 14, 'D')
    }

    /** Spikes right in front of the spawn: holding right dies at once, before any trap went off. */
    val spawnSpikes = Level(
        name = T("Demo: Doormat", "Demo: Fußmatte"),
        intro = T("Wipe your feet.", "Füße abtreten."),
        traps = listOf(trap(PastX(12f), Play(Card.COLLAPSE), Fall('a'))),
    ) {
        border(); floor()
        put(5, 14, '^')
        fill(14..15, 15..17, 'a')
        put(2, 14, 'P')
        put(29, 14, 'D')
    }

    /** A rematch that is the same room without the trap and with the door halfway: round 1's solution wins it, and it is shorter. */
    val lazyRematch = Level(
        name = T("Demo: Same Again", "Demo: Nochmal dasselbe"),
        intro = T("Again.", "Nochmal."),
        traps = listOf(trap(PastX(8f), Play(Card.COLLAPSE), Fall('a'))),
        rematch = listOf(Round(T("Again, but easier.", "Nochmal, aber leichter."), traps = emptyList()) { put(28, 14, '.'); put(16, 14, 'D') }),
    ) {
        border(); floor()
        fill(9..11, 15..17, 'a')
        put(2, 14, 'P')
        put(28, 14, 'D')
    }
}
