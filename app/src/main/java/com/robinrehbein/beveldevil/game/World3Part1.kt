package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch

/**
 * World 3, levels 1-16. Act 1, "Stromkreise": copper rails, pressure pads, clocks and live traces, Mephi cutting the
 * power under you, a bit flip. The first three levels teach; from the fourth on the traps come in chains, each one
 * springing on the spot (mid-jump, at the landing, the moment you step on a rail) and each of a different kind.
 * The room looks calm: the danger is what the hardware does, not what is hidden in the floor.
 */
object World3Part1 {
    private val hidden = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(

        // 1 — teaches the pad: step on it and the dark rail bridge wakes up; but on a timer, so do not dawdle
        Level(
            name = T("First Copper", "Erstes Kupfer"),
            intro = T("A button. A bridge. Even I can explain this one.", "Ein Knopf. Eine Brücke. Das kann sogar ich erklären."),
            start = listOf(Circuit('a', on = false), Pad('1', at = 6 to 14, circuits = "a", mode = PadMode.ON)),
            traps = listOf(
                trap(Pressed('1'), Clock('a', on = 2.6f, off = 1.5f), say("Power saving mode. You're welcome.", "Stromsparmodus. Gern geschehen.")),
                trap(Airborne(23f, 27f), Play(Card.OVERCLOCKED), HeatSpike('g', 0.7f), say("That landing pad is warm.", "Der Landeplatz ist vorgewärmt.")),
            ),
        ) {
            border(); floor()
            bridge(11..20, 'a')
            pit(24..25)
            fill(26..27, 15..15, 'g')
            spawn(); door()
        },

        // 2 — teaches the live trace: two walls of exposed copper, each with a breaker pad in front; the second breaker resets
        Level(
            name = T("Live Wire", "Unter Strom"),
            intro = T("Don't touch the copper. Or do. I'm curious.", "Fass das Kupfer nicht an. Oder doch. Ich bin neugierig."),
            start = listOf(
                Circuit('Z'), Circuit('Y'),
                Pad('1', at = 8 to 14, circuits = "Z", mode = PadMode.OFF),
                Pad('2', at = 16 to 14, circuits = "Y", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(Pressed('2'), Play(Card.SHORT_CIRCUIT), Power('Y', true), say("Breaker 2 has a reset timer.", "Sicherung 2 hat einen Reset-Timer."), delay = 0.9f),
                trap(Airborne(24f, 28f), HeatSpike('g', 0.7f), say("The doormat is warm today.", "Die Fußmatte ist heute warm.")),
            ),
        ) {
            border(); floor()
            wire(13, 'Z'); wire(21, 'Y')
            put(25, 14, '^'); put(26, 14, '^')
            fill(27..28, 15..15, 'g')
            spawn(); door()
        },

        // 3 — teaches the clock: two rails over a pit take turns; the second one changes its rhythm when you step on it
        Level(
            name = T("Clock Cycle", "Taktgeber"),
            intro = T("Everything here runs on my schedule.", "Hier läuft alles nach meinem Zeitplan."),
            start = listOf(Clock('a', on = 1.8f, off = 1f), Clock('b', on = 1.8f, off = 1f, phase = 2.3f)),
            traps = listOf(
                trap(Touch('b'), Clock('b', on = 1.1f, off = 1.4f), say("New firmware: same rail, faster clock.", "Neue Firmware: gleiche Schiene, schnellerer Takt.")),
                trap(PastX(23.6f), Play(Card.OVERCLOCKED), HeatSpike('f', 0.8f), say("Overclocked! Free of charge.", "Übertaktet! Kostenlos.")),
            ),
        ) {
            border(); floor()
            bridge(9..15, 'a'); bridge(16..22, 'b')
            fill(24..27, 15..15, 'f')
            spawn(); door()
        },

        // 4 — the first betrayal: the rail bridge is cut as you step up to it; jump it, and the floor behind it runs hot
        Level(
            name = T("Solid Copper", "Massives Kupfer"),
            intro = T("Nothing can go wrong here. Nothing.", "Hier kann nichts schiefgehen. Gar nichts."),
            start = listOf(Circuit('a')),
            traps = listOf(
                trap(PastX(11.6f), Play(Card.COLLAPSE), Power('a', false), say("Brownout. Sorry. (I'm not.)", "Spannungseinbruch. Tut mir leid. (Stimmt nicht.)")),
                trap(Landed(15.6f, 18.5f), HeatSpike('f', 0.85f), say("Landing fee: thermal.", "Landegebühr: thermisch.")),
                trap(Idle(1.3f), Saw(-1.5f, 14.4f, 7f, 0f), say("Stop staring. Fan blade, from behind.", "Nicht rumstehen. Lüfterblatt, von hinten.")),
            ),
        ) {
            border(); floor()
            bridge(12..15, 'a')
            fill(17..20, 15..15, 'f')
            spawn(); door()
        },

        // 5 — a hold pad: the wall is open only while the button is down; pressing it also drops a ceiling slab in your way
        Level(
            name = T("Turnstile", "Drehkreuz"),
            intro = T("Revolving door. Very modern.", "Drehtür. Sehr modern."),
            start = listOf(Circuit('w'), Pad('1', at = 11 to 14, circuits = "w", mode = PadMode.HOLD)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.HEADBUTT), Fall('c'), say("Revolving door: now with ceiling.", "Drehtür: jetzt mit Decke.")),
                trap(Airborne(20f, 24.5f), HeatSpike('f', 0.7f), say("Landing gear: preheated.", "Fahrwerk: vorgeheizt.")),
            ),
        ) {
            border(); floor()
            wire(12, 'w'); wire(13, 'w')
            fill(16..17, 9..10, 'c')
            put(22, 14, '^'); put(23, 14, '^')
            fill(26..27, 15..15, 'f')
            spawn(); door()
        },

        // 6 — two identical pads, the second undoes the first: hop over it; then the bridge starts to flicker
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
                trap(Touch('a'), Clock('a', on = 1.0f, off = 1.0f), say("Loose contact. Keep walking.", "Wackelkontakt. Lauf weiter.")),
            ),
        ) {
            border(); floor()
            bridge(12..17, 'a')
            spawn(); door()
        },

        // 7 — flickering live traces: wait at the gate until it goes dark; waiting too long gets noticed, the second gate changes rhythm
        Level(
            name = T("Loose Contact", "Wackelkontakt"),
            intro = T("It's not a bug, it's a flicker.", "Das ist kein Fehler, das ist ein Flimmern."),
            start = listOf(
                Clock('Z', on = 1.5f, off = 1.5f),
                Clock('Y', on = 1.5f, off = 1.5f, phase = 0.75f),
            ),
            traps = listOf(
                trap(Idle(0.9f), Play(Card.COLLAPSE), Fall('e'), say("Observed. Collapsed.", "Beobachtet. Kollabiert.")),
                trap(PastX(12.6f), Clock('Y', on = 1.0f, off = 2.0f), say("Gate 2 runs on another schedule.", "Tor 2 hat einen anderen Fahrplan.")),
            ),
        ) {
            border(); floor()
            wire(12, 'Z'); wire(20, 'Y')
            fill(14..17, 15..17, 'e')
            spawn(); door()
        },

        // 8 — a cosmic ray swaps two rails mid-bridge: be in the air when it hits; and again as you land
        Level(
            name = T("Memory", "Arbeitsspeicher"),
            intro = T("ECC memory. Error-free, allegedly.", "ECC-Speicher. Angeblich fehlerfrei."),
            start = listOf(Circuit('a'), Circuit('b', on = false)),
            traps = listOf(
                trap(PastX(14.5f), Play(Card.BIT_FLIP), BitFlip('a', 'b'), say("Bit flip! Purely cosmic. Nothing to do with me.", "Bitkipper! Rein kosmisch. Hat nichts mit mir zu tun.")),
                trap(Landed(18f, 22.5f), BitFlip('a', 'b'), say("Second cosmic ray. They come in pairs.", "Zweiter kosmischer Strahl. Kommen paarweise."), delay = 0.2f),
                trap(Airborne(19.5f, 23.5f), HeatSpike('g', 0.7f), say("Memory: fully warmed up.", "Speicher: voll aufgewärmt.")),
            ),
        ) {
            border(); floor()
            bridge(12..17, 'a'); bridge(18..22, 'b')
            fill(23..24, 15..15, 'g')
            spawn(); door()
        },

        // 9 — the pad powers the bridge and the trace wall behind it; a second pad on the bridge cuts the wall, for a while
        Level(
            name = T("Side Effect", "Nebenwirkung"),
            intro = T("Ask your sysadmin or your devil about side effects.", "Zu Risiken und Nebenwirkungen fragen Sie Ihren Teufel."),
            start = listOf(
                Circuit('a', on = false), Circuit('Z', on = false),
                Pad('1', at = 4 to 14, circuits = "aZ"),
                Pad('2', at = 10 to 14, circuits = "Z", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(Pressed('2'), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Side effects include: resets.", "Nebenwirkungen: Neustarts."), delay = 0.9f),
                trap(PastX(16.5f), HeatSpike('f', 0.8f), say("Side effects include: warm feet.", "Nebenwirkungen: warme Füße.")),
                trap(Airborne(16.3f, 20.5f), HeatSpike('g', 0.7f), say("Also: warm landings.", "Außerdem: warme Landungen.")),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            wire(15, 'Z')
            fill(17..20, 15..15, 'f'); fill(21..22, 15..15, 'g')
            spawn(); door()
        },

        // 10 — stepping stones on clocks: hop over on the beat; the last stone speeds up once you stand on the middle one
        Level(
            name = T("Metronome", "Metronom"),
            intro = T("Step to the beat. Mine.", "Tritt im Takt. In meinem."),
            start = listOf(Clock('a', on = 2.4f, off = 1f), Clock('b', on = 2.4f, off = 1f, phase = 1.8f), Clock('c', on = 2.4f, off = 1f)),
            traps = listOf(
                trap(Landed(14f, 20.5f), Clock('c', on = 1.4f, off = 1.2f), say("Stone three went up-tempo.", "Stein drei ist auf Allegro umgestiegen.")),
                trap(Touch('e'), Play(Card.SINKING), Fall('e'), say("The last tile was on a timer too.", "Die letzte Kachel hatte auch einen Timer."), delay = 0.3f),
            ),
        ) {
            border(); floor()
            put(27, 15, 'e'); fill(27..27, 16..17, 'e')
            pit(7..26)
            fill(8..12, 15..15, 'a'); fill(15..19, 15..15, 'b'); fill(22..26, 15..15, 'c')
            spawn(); door()
        },

        // 11 — a dark cable in the path goes live when you come near; and the one behind the landing does too
        Level(
            name = T("Loose Cables", "Kabelsalat"),
            intro = T("Tidy cabling. I did it myself.", "Saubere Verkabelung. Hab ich selbst gemacht."),
            start = listOf(Circuit('Z', on = false), Circuit('Y', on = false)),
            traps = listOf(
                trap(PastX(12f), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Who said it was dead? Oh right, I did.", "Wer sagt, dass er tot ist? Ach ja, ich.")),
                trap(Landed(15.5f, 17.8f), Power('Y', true), say("And its sibling.", "Und sein Geschwisterkabel.")),
                trap(Airborne(18.5f, 23f), HeatSpike('g', 0.7f), say("The floor went and got warm.", "Der Boden ist auch warm geworden.")),
            ),
        ) {
            border(); floor()
            fill(13..14, 14..14, 'Z')
            fill(18..19, 14..14, 'Y'); fill(21..22, 15..15, 'g')
            spawn(); door()
        },

        // 12 — do not press: the button is a bluff; the ceiling drops when you run under it, and a fan blade rolls in behind the wall
        Level(
            name = T("The Button", "Der Knopf"),
            intro = T("A very ordinary button.", "Ein ganz gewöhnlicher Knopf."),
            start = listOf(Pad('1', at = 8 to 14)),
            traps = listOf(
                trap(Pressed('1'), say("Nothing happened. Suspicious, isn't it?", "Nichts passiert. Verdächtig, oder?")),
                trap(PastX(16.5f), Play(Card.HEADBUTT), Fall('c'), say("The button was a decoy. The ceiling is real.", "Der Knopf war Attrappe. Die Decke ist echt.")),
                trap(Airborne(19f, 23f), Saw(33f, 14.4f, -7f, 0f), say("Delivery! Fan blade, one piece, rolling.", "Lieferung! Lüfterblatt, ein Stück, rollend.")),
            ),
        ) {
            border(); floor()
            fill(20..22, 9..10, 'c')
            spawn(); door()
        },

        // 13 — the fuse box is on top of the hill: climb it to switch the live wall off, but it only slows the current
        Level(
            name = T("Fuse Box", "Sicherungskasten"),
            intro = T("The fuse box is upstairs. Naturally.", "Der Sicherungskasten ist oben. Natürlich."),
            start = listOf(Circuit('Z'), Pad('1', at = 11 to 11, circuits = "Z", mode = PadMode.OFF)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Clock('Z', on = 0.8f, off = 2.8f, phase = 0.8f), say("Fuse box: the fuse is a clock now.", "Sicherungskasten: die Sicherung ist jetzt ein Taktgeber.")),
                trap(PastX(22.6f), Fall('x'), say("Load-bearing floor is optional.", "Tragender Boden ist optional."), delay = 0.15f),
            ),
        ) {
            border(); floor()
            chip(6, 2, 1); chip(8, 2, 2); chip(10, 4, 3); chip(14, 2, 2); chip(16, 2, 1)
            wire(22, 'Z')
            fill(24..26, 15..17, 'x')
            spawn(); door()
        },

        // 14 — the pad powers the bridge for a few seconds only
        Level(
            name = T("Power Supply", "Netzteil"),
            intro = T("Unlimited power. Terms and conditions apply.", "Unbegrenzter Strom. Es gelten die AGB."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON), Circuit('Z')),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SINKING), Power('a', false), say("Timer's up. Did you think it was a gift?", "Die Zeit ist um. Dachtest du, das sei ein Geschenk?"), delay = 3.2f),
                trap(PastX(17f), Power('Z', false), say("The cable was never live. I just like the glow.", "Das Kabel war nie unter Strom. Ich mag nur das Glühen.")),
                trap(PastX(26.4f), Show('A'), say("Made it? Here, a spike. On the house.", "Geschafft? Hier, ein Spike. Aufs Haus.")),
            ),
        ) {
            border(); floor()
            put(8, 14, '^'); put(10, 14, '^')
            bridge(13..25, 'a')
            wire(19, 'Z')
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

        // 16 — act finale: a timed pad bridge, a flickering gate that changes rhythm, and a bit flip, in one go
        Level(
            name = T("Motherboard", "Hauptplatine"),
            intro = T("Act one finale. All the parts, all the ways to die.", "Finale, Akt eins. Alle Teile, alle Wege zu sterben."),
            start = listOf(
                Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON),
                Clock('Z', on = 1.2f, off = 1.8f),
                Circuit('b'), Circuit('c', on = false),
            ),
            traps = listOf(
                trap(Pressed('1'), Power('a', false), say("The bridge is on a timer. Again.", "Die Brücke hat einen Timer. Schon wieder."), delay = 2.8f),
                trap(PastX(14.2f), Clock('Z', on = 0.9f, off = 1.6f), say("Gate firmware updated.", "Tor-Firmware aktualisiert.")),
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
