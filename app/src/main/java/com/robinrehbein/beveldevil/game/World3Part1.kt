package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Pressed

/**
 * World 3, levels 1-16. Act 1, "Stromkreise": copper rails, pressure pads, clocks and live traces,
 * Mephi cutting the power under you, a bit flip. The first three levels just teach; the green board.
 */
object World3Part1 {
    private val hidden = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(

        // 1 — teaches the pad: step on it and the dark rail bridge wakes up
        Level(
            name = T("First Copper", "Erstes Kupfer"),
            intro = T("A button. A bridge. Even I can explain this one.", "Ein Knopf. Eine Brücke. Das kann sogar ich erklären."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('a', on = false), Pad('1', at = 6 to 14, circuits = "a")),
            traps = listOf(
                trap(PastX(23.3f), Play(Card.SPIKE_SEED), Show('A'), say("Spikes. Installed while you were busy.", "Spikes. Eingebaut, während du beschäftigt warst.")),
            ),
        ) {
            border(); floor()
            bridge(11..20, 'a')
            put(24, 14, 'A'); put(25, 14, 'A')
            spawn(); door()
        },

        // 2 — teaches the live trace: two walls of exposed copper, each with a breaker pad in front
        Level(
            name = T("Live Wire", "Unter Strom"),
            intro = T("Don't touch the copper. Or do. I'm curious.", "Fass das Kupfer nicht an. Oder doch. Ich bin neugierig."),
            start = listOf(
                Circuit('Z'), Circuit('Y'),
                Pad('1', at = 8 to 14, circuits = "Z", mode = PadMode.OFF),
                Pad('2', at = 16 to 14, circuits = "Y", mode = PadMode.OFF),
            ),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(25.2f), Play(Card.GHOST_BLOCK), Show('A'), say("Did I say the wire was the only danger?", "Hab ich gesagt, dass nur der Draht gefährlich ist?")),
            ),
        ) {
            border(); floor()
            wire(13, 'Z'); wire(21, 'Y')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 3 — teaches the clock: two rails over a pit take turns
        Level(
            name = T("Clock Cycle", "Taktgeber"),
            intro = T("Everything here runs on my schedule.", "Hier läuft alles nach meinem Zeitplan."),
            start = listOf(Clock('a', on = 1.8f, off = 1f), Clock('b', on = 1.8f, off = 1f, phase = 2.3f)),
            traps = listOf(
                trap(PastX(3.4f), HeatSpike('g', 0.8f), say("Overclocked already? I did it early.", "Schon übertaktet? Ich war früh dran.")),
                trap(PastX(23.6f), Play(Card.CRUMBLE), HeatSpike('f', 0.8f), say("Overclocked! Free of charge.", "Übertaktet! Kostenlos.")),
            ),
        ) {
            border(); floor()
            fill(4..6, 15..15, 'g')
            bridge(9..15, 'a'); bridge(16..22, 'b')
            fill(24..27, 15..15, 'f')
            spawn(); door()
        },

        // 4 — the first betrayal: the rail bridge is cut as you step up to it, jump it anyway
        Level(
            name = T("Solid Copper", "Massives Kupfer"),
            intro = T("Nothing can go wrong here. Nothing.", "Hier kann nichts schiefgehen. Gar nichts."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('a')),
            traps = listOf(
                trap(PastX(11.6f), Play(Card.COLLAPSE), Power('a', false), say("Brownout. Sorry. (I'm not.)", "Spannungseinbruch. Tut mir leid. (Stimmt nicht.)")),
                trap(PastX(16.6f), Show('A'), say("Jumped early? Landing fee.", "Früh gesprungen? Landegebühr.")),
            ),
        ) {
            border(); floor()
            bridge(12..15, 'a')
            put(17, 14, 'A'); put(18, 14, 'A')
            spawn(); door()
        },

        // 5 — a hold pad: the wall is only open while the button is down, so walk through without stopping
        Level(
            name = T("Turnstile", "Drehkreuz"),
            intro = T("Revolving door. Very modern.", "Drehtür. Sehr modern."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            start = listOf(Circuit('w'), Pad('1', at = 11 to 14, circuits = "w", mode = PadMode.HOLD)),
            traps = listOf(
                trap(PastX(4.4f), Show('B'), say("Welcome mat: spikes.", "Fußmatte: Spikes.")),
                trap(PastX(16.3f), Play(Card.SPIKE_SEED), Show('A'), say("One more spike. For the road.", "Noch ein Spike. Für unterwegs.")),
            ),
        ) {
            border(); floor()
            put(5, 14, 'B')
            wire(12, 'w'); wire(13, 'w')
            put(17, 14, 'A')
            put(23, 14, '^'); put(24, 14, '^')
            spawn(); door()
        },

        // 6 — two identical pads, the second undoes the first: hop over it
        Level(
            name = T("Two Buttons", "Zwei Knöpfe"),
            intro = T("Press whichever you like.", "Drück, welchen du willst."),
            start = listOf(
                Circuit('a', on = false),
                Pad('1', at = 4 to 14, circuits = "a"),
                Pad('2', at = 6 to 14, circuits = "a"),
            ),
            traps = listOf(
                trap(Pressed('2'), Play(Card.DECOY), say("Button 2 undoes button 1. It's called a toggle.", "Knopf 2 macht Knopf 1 rückgängig. Nennt sich Toggle.")),
                trap(PastX(21.3f), Show('A'), say("Dry land. Dry spikes.", "Festland. Feste Spikes.")),
            ),
            legend = mapOf('A' to hidden),
        ) {
            border(); floor()
            bridge(12..17, 'a')
            put(22, 14, 'A'); put(23, 14, 'A')
            spawn(); door()
        },

        // 7 — flickering live traces: wait at the gate until it goes dark
        Level(
            name = T("Loose Contact", "Wackelkontakt"),
            intro = T("It's not a bug, it's a flicker.", "Das ist kein Fehler, das ist ein Flimmern."),
            start = listOf(
                Clock('Z', on = 1.5f, off = 1.5f),
                Clock('Y', on = 1.5f, off = 1.5f, phase = 0.75f),
            ),
            traps = listOf(
                trap(Touch('x'), Fall('x'), say("Loose floor contact, too.", "Wackelkontakt im Boden auch."), delay = 0.15f),
                trap(Idle(0.9f), Play(Card.COLLAPSE), Fall('e'), say("Observed. Collapsed.", "Beobachtet. Kollabiert.")),
            ),
        ) {
            border(); floor()
            fill(4..5, 15..17, 'x')
            wire(12, 'Z'); wire(20, 'Y')
            fill(14..17, 15..17, 'e')
            spawn(); door()
        },

        // 8 — a cosmic ray swaps two rails mid-bridge: be in the air when it hits
        Level(
            name = T("Memory", "Arbeitsspeicher"),
            intro = T("ECC memory. Error-free, allegedly.", "ECC-Speicher. Angeblich fehlerfrei."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('a'), Circuit('b', on = false)),
            traps = listOf(
                trap(PastX(14.5f), Play(Card.GHOST_BLOCK), BitFlip('a', 'b'), say("Bit flip! Purely cosmic. Nothing to do with me.", "Bitkipper! Rein kosmisch. Hat nichts mit mir zu tun.")),
                trap(PastX(25f), Show('A'), say("Cosmic rays also make spikes. Ask NASA.", "Kosmische Strahlung macht auch Spikes. Frag die NASA.")),
            ),
        ) {
            border(); floor()
            bridge(12..17, 'a'); bridge(18..23, 'b')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 9 — the pad powers the bridge and the trace wall behind it; a second pad on the bridge cuts the wall again
        Level(
            name = T("Side Effect", "Nebenwirkung"),
            intro = T("Ask your sysadmin or your devil about side effects.", "Zu Risiken und Nebenwirkungen fragen Sie Ihren Teufel."),
            start = listOf(
                Circuit('a', on = false), Circuit('Z', on = false),
                Pad('1', at = 4 to 14, circuits = "aZ"),
                Pad('2', at = 10 to 14, circuits = "Z", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(PastX(16.5f), Play(Card.CRUMBLE), HeatSpike('f', 0.8f), say("Side effects include: warm feet.", "Nebenwirkungen: warme Füße.")),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            wire(15, 'Z')
            fill(17..20, 15..15, 'f')
            spawn(); door()
        },

        // 10 — stepping stones on two clocks: hop over on the beat
        Level(
            name = T("Metronome", "Metronom"),
            intro = T("Step to the beat. Mine.", "Tritt im Takt. In meinem."),
            start = listOf(Clock('a', on = 2.4f, off = 1f), Clock('b', on = 2.4f, off = 1f, phase = 1.8f)),
            traps = listOf(
                trap(Touch('e'), Play(Card.SINKING), Fall('e'), say("The last tile was on a timer too.", "Die letzte Kachel hatte auch einen Timer."), delay = 0.3f),
            ),
        ) {
            border(); floor()
            put(27, 15, 'e'); fill(27..27, 16..17, 'e')
            pit(7..26)
            fill(8..12, 15..15, 'a'); fill(15..19, 15..15, 'b'); fill(22..26, 15..15, 'a')
            spawn(); door()
        },

        // 11 — a dark trace in the path goes live when you come near
        Level(
            name = T("Loose Cables", "Kabelsalat"),
            intro = T("Tidy cabling. I did it myself.", "Saubere Verkabelung. Hab ich selbst gemacht."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('Z', on = false)),
            traps = listOf(
                trap(PastX(12f), Play(Card.SPIKE_SEED), Power('Z', true), say("Who said it was dead? Oh right, I did.", "Wer sagt, dass er tot ist? Ach ja, ich.")),
                trap(PastX(16.8f), Show('A'), say("Good jump. Shame about the landing.", "Schöner Sprung. Schade um die Landung.")),
            ),
        ) {
            border(); floor()
            fill(13..14, 14..14, 'Z')
            put(17, 14, 'A'); put(18, 14, 'A')
            spawn(); door()
        },

        // 12 — do not press: the big button drops the ceiling
        Level(
            name = T("The Button", "Der Knopf"),
            intro = T("A very ordinary button.", "Ein ganz gewöhnlicher Knopf."),
            start = listOf(Pad('1', at = 14 to 14)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.HEADBUTT), Fall('c'), say("I asked you nicely.", "Ich hab doch höflich gefragt.")),
                trap(PastX(18.5f), Saw(33f, 14.4f, -7f, 0f), say("Delivery! Fan blade, one piece, rolling.", "Lieferung! Lüfterblatt, ein Stück, rollend.")),
            ),
        ) {
            border(); floor()
            fill(14..17, 9..10, 'c')
            spawn(); door()
        },

        // 13 — the fuse box is on top of the hill: climb it to switch the live wall off
        Level(
            name = T("Fuse Box", "Sicherungskasten"),
            intro = T("The fuse box is upstairs. Naturally.", "Der Sicherungskasten ist oben. Natürlich."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('Z'), Pad('1', at = 11 to 11, circuits = "Z", mode = PadMode.OFF)),
            traps = listOf(
                trap(PastX(24.3f), Play(Card.SPIKE_SEED), Show('A'), say("Fuse box: 1. Spike box: 2.", "Sicherungskasten: 1. Spike-Kasten: 2.")),
            ),
        ) {
            border(); floor()
            chip(6, 2, 1); chip(8, 2, 2); chip(10, 4, 3); chip(14, 2, 2); chip(16, 2, 1)
            wire(22, 'Z')
            put(25, 14, 'A'); put(26, 14, 'A')
            spawn(); door()
        },

        // 14 — the pad powers the bridge for a few seconds only
        Level(
            name = T("Power Supply", "Netzteil"),
            intro = T("Unlimited power. Terms and conditions apply.", "Unbegrenzter Strom. Es gelten die AGB."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SINKING), Power('a', false), say("Timer's up. Did you think it was a gift?", "Die Zeit ist um. Dachtest du, das sei ein Geschenk?"), delay = 3.2f),
                trap(PastX(26.4f), Show('A'), say("Made it? Here, a spike. On the house.", "Geschafft? Hier, ein Spike. Aufs Haus.")),
            ),
        ) {
            border(); floor()
            put(8, 14, '^'); put(10, 14, '^')
            bridge(13..25, 'a')
            put(27, 14, 'A'); put(28, 14, 'A')
            spawn(); door()
        },

        // 15 — a pad swaps the controls until the second pad swaps them back
        Level(
            name = T("Connector", "Stecker"),
            intro = T("USB-C fits either way up. Everything does.", "USB-C passt andersrum. Alles passt andersrum."),
            start = listOf(Pad('1', at = 4 to 14), Pad('2', at = 16 to 14), Pad('3', at = 26 to 14)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Left is right. Again.", "Polung vertauscht. Links ist rechts. Schon wieder.")),
                trap(Pressed('2'), Swap(false), say("Polarity restored. Probably.", "Polung wiederhergestellt. Vermutlich.")),
                trap(Pressed('3'), Swap(true), say("Polarity reversed again. Third time lucky.", "Polung schon wieder vertauscht. Dreimal ist Steckerrecht.")),
            ),
        ) {
            border(); floor(); pit(8..11)
            put(22, 14, '^'); put(23, 14, '^')
            spawn(); door()
        },

        // 16 — act finale: a pad bridge, a flickering gate and a bit flip, in one go
        Level(
            name = T("Motherboard", "Hauptplatine"),
            intro = T("Act one finale. All the parts, all the ways to die.", "Finale, Akt eins. Alle Teile, alle Wege zu sterben."),
            start = listOf(
                Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON),
                Clock('Z', on = 1.2f, off = 1.8f),
                Circuit('b'), Circuit('c', on = false),
            ),
            traps = listOf(
                trap(PastX(21.5f), Play(Card.GRAND_FINALE), BitFlip('b', 'c'), say("Motherboard: 3 of 3 components hostile.", "Hauptplatine: 3 von 3 Bauteilen feindlich.")),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            wire(17, 'Z')
            bridge(20..23, 'b'); bridge(24..27, 'c')
            spawn(); door()
        },
    )
}
