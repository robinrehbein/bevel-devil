package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Move
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
import com.robinrehbein.beveldevil.game.Trigger.Zone

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
            // rematch: round 1 taught "press and run"; now the bridge runs a short shift, so running at once drops you mid-way
            rematch = listOf(
                Round(
                    T("Rematch. Same bridge, new power plan.", "Revanche. Die Brücke hat jetzt Gleitzeit."),
                    start = listOf(Circuit('a', on = false), Pad('1', at = 6 to 14, circuits = "a", mode = PadMode.ON)),
                    traps = listOf(
                        trap(Pressed('1'), Play(Card.SINKING), Clock('a', on = 1.6f, off = 1.4f), say("Night tariff: short shifts only.", "Nachttarif: nur Kurzschichten.")),
                        trap(Airborne(23f, 27f), HeatSpike('g', 0.7f), say("The pad remembered you. Warmly.", "Der Landeplatz erinnert sich. Herzlich.")),
                    ),
                ),
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
            // rematch: the reset timer moved to breaker 1 (step on it twice); breaker 2 only plays the card
            rematch = listOf(
                Round(
                    T("Warm reboot. I rewired the fuse box.", "Warmstart. Ich hab den Sicherungskasten neu belegt."),
                    start = listOf(
                        Circuit('Z'), Circuit('Y'),
                        Pad('1', at = 8 to 14, circuits = "Z", mode = PadMode.OFF),
                        Pad('2', at = 16 to 14, circuits = "Y", mode = PadMode.OFF),
                    ),
                    traps = listOf(
                        trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Breaker 1 got the reset timer now.", "Jetzt hat Sicherung 1 den Reset-Timer."), delay = 0.3f),
                        trap(Pressed('2'), say("Breaker 2 behaves. For now.", "Sicherung 2 benimmt sich. Noch."), delay = 0.4f),
                        trap(Airborne(24f, 28f), HeatSpike('g', 0.7f), say("Doormat: still warm.", "Fußmatte: immer noch warm.")),
                    ),
                ),
                // round 3: the pins in front of the door slide onto the landing spot the moment you jump; provoke them with a
                // small hop first, then jump the spot they moved to
                Round(
                    T("Round three. I moved the furniture.", "Runde drei. Ich hab umgeräumt."),
                    start = listOf(
                        Circuit('Z'), Circuit('Y'),
                        Pad('1', at = 8 to 14, circuits = "Z", mode = PadMode.OFF),
                        Pad('2', at = 16 to 14, circuits = "Y", mode = PadMode.OFF),
                    ),
                    traps = listOf(
                        trap(Airborne(21.5f, 27f), Play(Card.SPIKE_SEED), Move('S', 2f, 0f, 16f), say("Pins: hot-pluggable.", "Pins: im laufenden Betrieb steckbar.")),
                    ),
                ) { put(25, 14, 'S'); put(26, 14, 'S') },
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
            // rematch: the end stays cool and gives way under whoever jumps it (walk it); without the firmware update on rail
            // two, the runner who never waits drops off it
            rematch = listOf(
                Round(
                    T("New schedule. Same tracks.", "Neuer Fahrplan. Die Bahn lässt grüßen."),
                    start = listOf(Clock('a', on = 1.8f, off = 1f), Clock('b', on = 1.8f, off = 1f, phase = 2.3f)),
                    traps = listOf(
                        trap(Airborne(23.5f, 28f), Play(Card.COLLAPSE), Fall('f'), say("Jumped? The landing went on strike.", "Gesprungen? Die Landung streikt.")),
                    ),
                ) { fill(24..28, 15..17, 'f') },
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
            // rematch: the brownout waits until you are on the rail (hop it as before), the floor is cool, and the second hop
            // that saved you in round 1 lands in a hole: walk on
            rematch = listOf(
                Round(
                    T("Again. I learn faster than you.", "Nochmal. Ich hab mitgeschrieben."),
                    start = listOf(Circuit('a')),
                    traps = listOf(
                        trap(Touch('a'), Play(Card.COLLAPSE), Power('a', false), say("Brownout, slightly delayed.", "Spannungseinbruch, leicht verspätet."), delay = 0.3f),
                        trap(Airborne(17f, 21.5f), Fall('y'), say("Hopping is so last round.", "Hüpfen war letzte Runde.")),
                        trap(Idle(1.3f), Saw(-1.5f, 14.4f, 7f, 0f), say("Still staring. Still a fan blade.", "Immer noch am Starren. Immer noch ein Lüfterblatt.")),
                    ),
                ) { fill(20..24, 15..17, 'y') },
            ),
        ) {
            border(); floor()
            bridge(12..15, 'a')
            fill(17..20, 15..15, 'f')
            spawn(); door()
        },

        // 5 — a hold pad: the wall is open only while the button is down; pressing it also drops a ceiling slab in your way;
        // the landing behind the spikes is hot, so you run on, and the dark trace before the door wakes up as you land
        Level(
            name = T("Turnstile", "Drehkreuz"),
            intro = T("Revolving door. Very modern.", "Drehtür. Sehr modern."),
            start = listOf(Circuit('w'), Pad('1', at = 11 to 14, circuits = "w", mode = PadMode.HOLD), Circuit('Z', on = false)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.HEADBUTT), Fall('c'), say("Revolving door: now with ceiling.", "Drehtür: jetzt mit Decke.")),
                trap(Airborne(20f, 24.5f), HeatSpike('f', 0.7f), say("Landing gear: preheated.", "Fahrwerk: vorgeheizt.")),
                // the hot plate says "keep running"; the doorway says "wait". Stop short of the plate, let the pulse pass
                trap(Landed(24f, 28f), Clock('Z', on = 0.9f, off = 1.6f), say("Door frame: now with doorbell.", "Türrahmen: jetzt mit Klingel.")),
            ),
            // rematch: the slab stays up at the button and comes down on whoever jumps under it: walk under it
            rematch = listOf(
                Round(
                    T("Turnstile, serviced. Mostly.", "Drehkreuz frisch gewartet. Von mir."),
                    start = listOf(Circuit('w'), Pad('1', at = 11 to 14, circuits = "w", mode = PadMode.HOLD), Circuit('Z', on = false)),
                    traps = listOf(
                        trap(Pressed('1'), say("Revolving door: ceiling on standby.", "Drehtür: Decke im Standby.")),
                        trap(Airborne(13.5f, 17.5f), Play(Card.HEADBUTT), Fall('c'), say("Standby over.", "Standby beendet.")),
                        trap(Airborne(20f, 24.5f), HeatSpike('f', 0.7f), say("Landing gear: preheated, again.", "Fahrwerk: wieder vorgeheizt.")),
                        trap(Landed(24f, 28f), Clock('Z', on = 0.9f, off = 1.6f), say("The doorbell stayed.", "Die Klingel ist geblieben.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            wire(12, 'w'); wire(13, 'w')
            fill(16..17, 9..10, 'c')
            put(22, 14, '^'); put(23, 14, '^')
            fill(26..27, 15..15, 'f')
            wire(28, 'Z')
            spawn(); door()
        },

        // 6 — two identical pads, the second undoes the first: hop over it; then the bridge starts to flicker, so you hurry
        // across, and the floor after it gives way under the hurry: leave the bridge with a jump
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
                trap(Touch('e'), Fall('e'), say("Walking was the right idea. Here, though?", "Weiterlaufen war richtig. Nur nicht hier."), delay = 0.08f),
            ),
            // rematch: the buttons swapped jobs (button 1 is the dud now), and behind the bridge the floor crumbles away right
            // behind you, tile pair by tile pair, all the way to the door: land and keep running
            rematch = listOf(
                Round(
                    T("Two buttons, new wiring. Run along.", "Zwei Knöpfe, neu verdrahtet. Lauf schon."),
                    start = listOf(
                        Circuit('a', on = false),
                        Pad('1', at = 4 to 14),
                        Pad('2', at = 6 to 14, circuits = "a"),
                    ),
                    traps = listOf(
                        trap(Pressed('1'), say("Button 1 is decorative now.", "Knopf 1 ist jetzt Deko.")),
                        trap(Touch('a'), Clock('a', on = 1.0f, off = 1.0f), say("Loose contact, still.", "Wackelkontakt, immer noch.")),
                        trap(Touch('e'), Fall('e'), say("Here, though? Still no.", "Hier? Immer noch nicht."), delay = 0.08f),
                        trap(Landed(19f, 24f), Play(Card.CRUMBLE), Fall('m'), say("Garbage collection. Behind you.", "Speicherbereinigung. Hinter dir."), delay = 0.3f),
                        trap(Landed(19f, 24f), Fall('n'), delay = 0.55f),
                        trap(Landed(19f, 24f), Fall('o'), delay = 0.8f),
                        trap(Landed(19f, 24f), Fall('q'), delay = 1.05f),
                    ),
                ) { fill(21..22, 15..17, 'm'); fill(23..24, 15..17, 'n'); fill(25..26, 15..17, 'o'); fill(27..28, 15..17, 'q') },
            ),
        ) {
            border(); floor()
            bridge(12..17, 'a')
            fill(18..20, 15..17, 'e')
            spawn(); door()
        },

        // 7 — flickering live traces: wait at the gate until it goes dark; waiting too long gets noticed, hopping on the spot
        // to look busy brings the slab above down (pace instead), and the second gate changes rhythm
        Level(
            name = T("Loose Contact", "Wackelkontakt"),
            intro = T("It's not a bug, it's a flicker.", "Das ist kein Fehler, das ist ein Flimmern."),
            start = listOf(
                Clock('Z', on = 1.5f, off = 1.5f),
                Clock('Y', on = 1.5f, off = 1.5f, phase = 0.75f),
            ),
            traps = listOf(
                trap(Idle(0.9f), Play(Card.COLLAPSE), Fall('e'), say("Observed. Collapsed.", "Beobachtet. Kollabiert.")),
                trap(Airborne(8.4f, 11.7f), Fall('c'), say("Hopping on the spot? The ceiling felt that.", "Auf der Stelle hüpfen? Die Decke hat's gespürt.")),
                trap(PastX(12.6f), Clock('Y', on = 1.0f, off = 2.0f), say("Gate 2 runs on another schedule.", "Tor 2 hat einen anderen Fahrplan.")),
            ),
        ) {
            border(); floor()
            wire(12, 'Z'); wire(20, 'Y')
            fill(9..11, 9..9, 'c')
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
            // rematch: no ray at 14.5, so the early jump lands on the dark rail; the ray comes at the end of the lit rail: jump late
            rematch = listOf(
                Round(
                    T("ECC patch installed. Errors now on schedule.", "ECC-Patch installiert. Fehler jetzt nach Plan."),
                    start = listOf(Circuit('a'), Circuit('b', on = false)),
                    traps = listOf(
                        trap(Zone(16.2f, 13.6f, 17.8f, 15f), Play(Card.BIT_FLIP), BitFlip('a', 'b'), say("Cosmic rays come later today.", "Die kosmische Strahlung kommt heute später.")),
                    ),
                ),
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
            // rematch: breaker 2 resets too fast to outrun now: step on it a second time
            rematch = listOf(
                Round(
                    T("Second opinion. Same symptoms.", "Zweitmeinung eingeholt. Bei mir."),
                    start = listOf(
                        Circuit('a', on = false), Circuit('Z', on = false),
                        Pad('1', at = 4 to 14, circuits = "aZ"),
                        Pad('2', at = 10 to 14, circuits = "Z", mode = PadMode.OFF),
                    ),
                    traps = listOf(
                        trap(Pressed('2'), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Side effects include: impatience.", "Nebenwirkungen: Ungeduld."), delay = 0.4f),
                        trap(PastX(16.5f), HeatSpike('f', 0.8f), say("Warm feet, still.", "Warme Füße, immer noch.")),
                        trap(Airborne(16.3f, 20.5f), HeatSpike('g', 0.7f), say("Warm landings, as prescribed.", "Warme Landungen, wie verschrieben.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            wire(15, 'Z')
            fill(17..20, 15..15, 'f'); fill(21..22, 15..15, 'g')
            spawn(); door()
        },

        // 10 — stepping stones on clocks: hop over on the beat; the first stone goes on a short shift once you land on it, so
        // waiting there for the next beat is out (watch it from the floor); the last stone speeds up once you stand on the middle one
        Level(
            name = T("Metronome", "Metronom"),
            intro = T("Step to the beat. Mine.", "Tritt im Takt. In meinem."),
            start = listOf(Clock('a', on = 2.4f, off = 1f), Clock('b', on = 2.4f, off = 1f, phase = 1.8f), Clock('c', on = 2.4f, off = 1f)),
            traps = listOf(
                trap(Landed(7.6f, 13f), Clock('a', on = 1.2f, off = 1.0f), say("Stone one: clocked in, short shift.", "Stein eins: eingestempelt, Kurzschicht.")),
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
            // rematch: the first cable stays dead and jumping it brings a slab down; the second one goes live earlier
            rematch = listOf(
                Round(
                    T("Round two. I tidied up. Sort of.", "Runde zwei. Kabelmanagement: kreativ."),
                    start = listOf(Circuit('Z', on = false), Circuit('Y', on = false)),
                    traps = listOf(
                        trap(PastX(12f), say("This one stays dead. Today.", "Das hier bleibt tot. Heute.")),
                        trap(Airborne(11.2f, 16f), Fall('c'), say("Cable tray, unmounted.", "Kabelkanal, abmontiert.")),
                        trap(PastX(16.8f), Play(Card.SHORT_CIRCUIT), Power('Y', true), say("That one is live for real.", "Das hier ist echt live.")),
                    ),
                ) { fill(13..15, 9..10, 'c') },
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
            // rematch: the ceiling card is a bluff, so there is no slab to stand on while the blade, now early, rolls through
            rematch = listOf(
                Round(
                    T("Same button. Different mood.", "Gleicher Knopf. Er hat heute Montag."),
                    start = listOf(Pad('1', at = 8 to 14)),
                    traps = listOf(
                        trap(PastX(16.5f), Bluff(Card.HEADBUTT)),
                        trap(PastX(21.5f), Saw(33f, 14.4f, -7f, 0f), say("Express delivery.", "Expresslieferung.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(20..22, 9..10, 'c')
            spawn(); door()
        },

        // 13 — the fuse box is on top of the hill: climb it to switch the live wall off, but it only slows the current;
        // waiting for the dark beat on the rail in front of the wall cuts the rail (wait on the hill instead), and the floor behind the wall drops
        Level(
            name = T("Fuse Box", "Sicherungskasten"),
            intro = T("The fuse box is upstairs. Naturally.", "Der Sicherungskasten ist oben. Natürlich."),
            start = listOf(Circuit('Z'), Pad('1', at = 11 to 11, circuits = "Z", mode = PadMode.OFF), Circuit('a')),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Clock('Z', on = 0.8f, off = 2.8f, phase = 0.8f), say("Fuse box: the fuse is a clock now.", "Sicherungskasten: die Sicherung ist jetzt ein Taktgeber.")),
                trap(Touch('a'), Power('a', false), say("Waiting room closed. Should have waited upstairs.", "Wartezimmer geschlossen. Oben warten wäre klüger gewesen."), delay = 0.8f),
                trap(PastX(22.6f), Fall('x'), say("Load-bearing floor is optional.", "Tragender Boden ist optional."), delay = 0.15f),
            ),
        ) {
            border(); floor()
            chip(6, 2, 1); chip(8, 2, 2); chip(10, 4, 3); chip(14, 2, 2); chip(16, 2, 1)
            bridge(18..21, 'a')
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
            // rematch: the bridge is live from the start and the button turns it off (twice turns it back on, or hop it)
            rematch = listOf(
                Round(
                    T("Firmware update: the button got opinions.", "Firmware-Update: Der Knopf hat jetzt eine Meinung."),
                    start = listOf(Circuit('a'), Pad('1', at = 4 to 14, circuits = "a"), Circuit('Z')),
                    traps = listOf(
                        trap(Pressed('1'), Play(Card.SINKING), Power('a', false), say("Pressing it was a habit, not a plan.", "Drücken war Gewohnheit, kein Plan.")),
                        trap(PastX(17f), Power('Z', false), say("The cable still only glows.", "Das Kabel glüht immer noch nur.")),
                        trap(PastX(26.4f), Show('A'), say("The spike is a regular.", "Der Spike ist Stammgast.")),
                    ),
                ),
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
            // rematch: plug 2 only claims to fix the polarity, so the controls stay swapped all the way
            rematch = listOf(
                Round(
                    T("Unplugged and plugged back in. Classic.", "Aus- und wieder eingesteckt. Hilft immer. Mir."),
                    start = listOf(Pad('1', at = 4 to 14), Pad('2', at = 16 to 14), Pad('3', at = 26 to 14)),
                    traps = listOf(
                        trap(Pressed('1'), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Old habit.", "Polung vertauscht. Alte Gewohnheit.")),
                        trap(Pressed('2'), say("Polarity restored. Probably not.", "Polung wiederhergestellt. Eher nicht.")),
                        trap(Pressed('3'), Swap(true), say("Plug 3: still reversed. Consistency.", "Stecker 3: immer noch verdreht. Konsequenz.")),
                    ),
                ),
            ),
        ) {
            border(); floor(); pit(8..11)
            put(22, 14, '^'); put(23, 14, '^')
            spawn(); door()
        },

        // 16 — act finale: a timed pad bridge, a flickering gate that changes rhythm, and a bit flip, in one go; the flip
        // comes back a moment after you land on the rail it saved you with, so do not stop on it
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
                trap(Landed(23.6f, 28f), BitFlip('b', 'c'), say("Component 4 of 3. I never could count.", "Bauteil 4 von 3. Zählen war nie meins."), delay = 0.5f),
            ),
            // rematch: the rails start the other way round, so the early jump from round 1 falls into the dark one
            rematch = listOf(
                Round(
                    T("Reboot, same motherboard. Different bugs.", "Reboot, gleiche Platine. Andere Bugs."),
                    start = listOf(
                        Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON),
                        Clock('Z', on = 1.2f, off = 1.8f),
                        Circuit('b', on = false), Circuit('c'),
                    ),
                    traps = listOf(
                        trap(Pressed('1'), Power('a', false), say("Bridge timer: still a thing.", "Brückentimer: gibt's immer noch."), delay = 2.8f),
                        trap(PastX(14.2f), Clock('Z', on = 0.9f, off = 1.6f), say("Gate firmware: rolled back. Then forward.", "Tor-Firmware: zurückgerollt. Dann wieder vor.")),
                        trap(Landed(24.9f, 28f), Play(Card.BIT_FLIP), BitFlip('b', 'c'), say("Bits swapped back. For balance.", "Bits zurückgetauscht. Fürs Gleichgewicht."), delay = 0.12f),
                    ),
                ),
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
