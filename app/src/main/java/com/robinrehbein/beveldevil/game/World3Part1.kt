package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed

/**
 * World 3, levels 1-16. Act 1, "Stromkreise": copper rails, pressure pads, clocks and live traces,
 * Mephi cutting the power under you, a bit flip. The first three levels just teach; the green board.
 */
object World3Part1 {
    val levels: List<Level> = listOf(

        // 1 — teaches the pad: step on it and the dark rail bridge wakes up
        Level(
            name = T("First Copper", "Erstes Kupfer"),
            intro = T("A button. A bridge. Even I can explain this one.", "Ein Knopf. Eine Brücke. Das kann sogar ich erklären."),
            start = listOf(Circuit('a', on = false), Pad('1', at = 6 to 14, circuits = "a")),
        ) {
            border(); floor()
            bridge(11..20, 'a')
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
        ) {
            border(); floor()
            wire(13, 'Z'); wire(21, 'Y')
            spawn(); door()
        },

        // 3 — teaches the clock: two rails over a pit take turns
        Level(
            name = T("Clock Cycle", "Taktgeber"),
            intro = T("Everything here runs on my schedule.", "Hier läuft alles nach meinem Zeitplan."),
            start = listOf(Clock('a', on = 1.8f, off = 1f), Clock('b', on = 1.8f, off = 1f, phase = 2.3f)),
        ) {
            border(); floor()
            bridge(9..15, 'a'); bridge(16..22, 'b')
            spawn(); door()
        },

        // 4 — the first betrayal: the rail bridge is cut as you step up to it, jump it anyway
        Level(
            name = T("Power Cut", "Stromausfall"),
            intro = T("Solid copper. Would I lie about copper?", "Massives Kupfer. Würde ich bei Kupfer lügen?"),
            start = listOf(Circuit('a')),
            traps = listOf(
                trap(PastX(11.6f), Play(Card.COLLAPSE), Power('a', false), say("Brownout. Sorry. (I'm not.)", "Spannungseinbruch. Tut mir leid. (Stimmt nicht.)")),
            ),
        ) {
            border(); floor()
            bridge(12..15, 'a')
            spawn(); door()
        },

        // 5 — a hold pad: the wall is only open while the button is down, so walk through without stopping
        Level(
            name = T("Turnstile", "Drehkreuz"),
            intro = T("Some buttons only work while you stand on them.", "Manche Knöpfe wirken nur, solange du draufstehst."),
            start = listOf(Circuit('w'), Pad('1', at = 11 to 14, circuits = "w", mode = PadMode.HOLD)),
        ) {
            border(); floor()
            wire(12, 'w'); wire(13, 'w')
            put(20, 14, '^'); put(21, 14, '^')
            spawn(); door()
        },

        // 6 — two identical pads, the second undoes the first: hop over it
        Level(
            name = T("Wrong Button", "Falscher Knopf"),
            intro = T("Two buttons. One of them is honest.", "Zwei Knöpfe. Einer davon ist ehrlich."),
            start = listOf(
                Circuit('a', on = false),
                Pad('1', at = 4 to 14, circuits = "a"),
                Pad('2', at = 6 to 14, circuits = "a"),
            ),
            traps = listOf(
                trap(Pressed('2'), Play(Card.DECOY), say("Button 2 undoes button 1. It's called a toggle.", "Knopf 2 macht Knopf 1 rückgängig. Nennt sich Toggle.")),
            ),
        ) {
            border(); floor()
            bridge(12..17, 'a')
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
        ) {
            border(); floor()
            wire(12, 'Z'); wire(20, 'Y')
            spawn(); door()
        },

        // 8 — a cosmic ray swaps two rails mid-bridge: be in the air when it hits
        Level(
            name = T("Bit Flip", "Bitkipper"),
            intro = T("A cosmic ray, completely random. (Completely.)", "Kosmische Strahlung, völlig zufällig. (Völlig.)"),
            start = listOf(Circuit('a'), Circuit('b', on = false)),
            traps = listOf(
                trap(PastX(14.5f), Play(Card.GHOST_BLOCK), BitFlip('a', 'b'), say("Bit flip! Purely cosmic. Nothing to do with me.", "Bitkipper! Rein kosmisch. Hat nichts mit mir zu tun.")),
            ),
        ) {
            border(); floor()
            bridge(12..17, 'a'); bridge(18..23, 'b')
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
        ) {
            border(); floor()
            bridge(8..13, 'a')
            wire(15, 'Z')
            spawn(); door()
        },

        // 10 — stepping stones on two clocks: hop over on the beat
        Level(
            name = T("Metronome", "Metronom"),
            intro = T("Step to the beat. Mine.", "Tritt im Takt. In meinem."),
            start = listOf(Clock('a', on = 2.4f, off = 1f), Clock('b', on = 2.4f, off = 1f, phase = 1.8f)),
        ) {
            border(); floor()
            pit(7..26)
            fill(8..12, 15..15, 'a'); fill(15..19, 15..15, 'b'); fill(22..26, 15..15, 'a')
            spawn(); door()
        },

        // 11 — a dark trace in the path goes live when you come near
        Level(
            name = T("Phantom Voltage", "Phantomspannung"),
            intro = T("That wire is dead. Totally. Go ahead.", "Der Draht ist tot. Komplett. Geh nur."),
            start = listOf(Circuit('Z', on = false)),
            traps = listOf(
                trap(PastX(12f), Play(Card.SPIKE_SEED), Power('Z', true), say("Who said it was dead? Oh right, I did.", "Wer sagt, dass er tot ist? Ach ja, ich.")),
            ),
        ) {
            border(); floor()
            fill(13..14, 14..14, 'Z')
            spawn(); door()
        },

        // 12 — do not press: the big button drops the ceiling
        Level(
            name = T("Do Not Press", "Nicht drücken"),
            intro = T("Please do not press the big button.", "Bitte nicht den großen Knopf drücken."),
            start = listOf(Pad('1', at = 14 to 14)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.HEADBUTT), Fall('c'), say("I asked you nicely.", "Ich hab doch höflich gefragt.")),
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
            start = listOf(Circuit('Z'), Pad('1', at = 11 to 11, circuits = "Z", mode = PadMode.OFF)),
        ) {
            border(); floor()
            chip(6, 2, 1); chip(8, 2, 2); chip(10, 4, 3); chip(14, 2, 2); chip(16, 2, 1)
            wire(22, 'Z')
            spawn(); door()
        },

        // 14 — the pad powers the bridge for a few seconds only
        Level(
            name = T("Timer Switch", "Zeitschaltuhr"),
            intro = T("Power is included. For a limited time.", "Strom inklusive. Nur für kurze Zeit."),
            start = listOf(Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SINKING), Power('a', false), say("Timer's up. Did you think it was a gift?", "Die Zeit ist um. Dachtest du, das sei ein Geschenk?"), delay = 3.2f),
            ),
        ) {
            border(); floor()
            put(8, 14, '^'); put(10, 14, '^')
            bridge(13..25, 'a')
            spawn(); door()
        },

        // 15 — a pad swaps the controls until the second pad swaps them back
        Level(
            name = T("Reverse Polarity", "Verpolt"),
            intro = T("Plus and minus. I mixed them up on purpose.", "Plus und Minus. Ich hab sie absichtlich vertauscht."),
            start = listOf(Pad('1', at = 4 to 14), Pad('2', at = 16 to 14)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Left is right. Again.", "Polung vertauscht. Links ist rechts. Schon wieder.")),
                trap(Pressed('2'), Swap(false), say("Polarity restored. Probably.", "Polung wiederhergestellt. Vermutlich.")),
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
