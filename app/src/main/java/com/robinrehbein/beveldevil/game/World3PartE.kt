package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.*
import com.robinrehbein.beveldevil.game.Trigger.*


/**
 * World 3, act 3, block E (levels 33-40: Updraft, Tailwind, Headwind, Air Cushion, Lull, Silence, Downdraft, Air Castle), rebuilt under
 * the V2 level design (docs/LEVEL_DESIGN_V2.md, section 8). The bot solutions are in the test sources ([World3RoomsE]).
 */
object World3PartE {
    val levels: List<Level> = listOf(

        // 33 — Updraft (R10, U1, a puzzle room): you ride a draft up a cliff (stand in it against the wall, rise, step out onto the bridge), and up
        // there the floor goes: the plank ahead drops out the moment you land (jump the gap), the plank you land on drops out from under you
        // (keep moving), and over the second draft Mephi pauses the fan halfway up.
        Level(
            name = T("Updraft", "Aufwind"),
            intro = T("Boarding now. Mind the gap.", "Einsteigen bitte. Achtung, Lücke."),
            start = listOf(
                Fan('f', at = 8 to 17, dir = Dir.UP, reach = 11, speed = 9f, width = 4),
                Fan('g', at = 25 to 9, dir = Dir.UP, reach = 7, speed = 9f, width = 4),
            ),
            traps = listOf(
                trap(Landed(12.2f, 15.9f), Play(Card.CRUMBLE), Fall('b'), say("Gate change: your plank just left.", "Gate geändert: Deine Planke ist schon weg."), delay = 0.1f),
                trap(Landed(20f, 22.9f), Fall('c'), say("Seats are not reserved.", "Sitzplätze sind nicht reserviert."), delay = 0.4f),
                trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 0f), say("Brief pause. Please hold your altitude.", "Kurze Pause. Bitte Höhe halten.")),
                trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 9f), delay = 1.7f),
            ),
            hint = T("Stand in the draft and hold on to the wall. Planks only give way once you are on them.", "Stell dich in den Aufwind und halt dich an der Wand. Planken geben erst nach, wenn du drauf stehst."),
            // rematch: the planks hold now, and nothing waits for the second landing: but a stud has grown on the far plank, exactly where the
            // long leap from round one comes down. Hop it low instead
            rematch = listOf(
                Round(
                    T("Same ride. A different bridge.", "Gleiche Fahrt. Eine andere Brücke."),
                    traps = listOf(
                        trap(Landed(12.2f, 15.9f), Play(Card.SPIKE_SEED), Show('p'), say("Fresh from the factory: one plank, slightly pointy.", "Frisch ab Werk: eine Planke, leicht spitz.")),
                        trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 0f), say("Another pause. Our regular passengers know it.", "Wieder eine Pause. Stammgäste kennen das.")),
                        trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 9f), delay = 1.75f),
                    ),
                    legend = mapOf('p' to Glyph(spike = true, hidden = true)),
                ) { put(20, 7, 'p'); put(22, 5, '.') },
            ),
        ) {
            border(); floor(); pit(7..11)
            fill(8..11, 17..17)
            fill(12..12, 9..17)
            fill(12..15, 8..8); fill(16..19, 8..8, 'b'); fill(20..22, 8..8, 'c'); fill(23..24, 8..8)
            fill(22..24, 5..5, 'v')
            pit(13..28, 9)
            fill(25..28, 9..9)
            fill(29..30, 3..17)
            spawn(1, 14); door(29, 2)
        },

        // 34 — Tailwind (breather, U12 as gusts that come and go)
        Level(
            name = T("Tailwind", "Rückenwind"),
            intro = T("Go with the flow. Jump into it.", "Schwimm mit dem Strom. Spring hinein."),
            start = listOf(Fan('w', at = 31 to 6, dir = Dir.LEFT, reach = 22, speed = -12f, width = 7)),
            traps = listOf(
                trap(PastX(11f), FanSet('w', 0f), say("Lull. The wind is catching its breath.", "Flaute. Der Wind holt kurz Luft.")),
                trap(Zone(17f, 12f, 19.2f, 15f), FanSet('w', -12f), say("Here it comes again. Jump into it.", "Da kommt er wieder. Spring hinein."), delay = 0.15f),
                trap(Landed(25f, 28.5f), Play(Card.OVERCLOCKED), HeatSpike('h', 0.55f), say("The landing strip is preheated.", "Die Landebahn ist vorgeheizt.")),
                trap(Airborne(21f, 24f), FanSet('w', -6f), say("The gust loses breath mid-air.", "Der Böe geht mitten in der Luft die Luft aus.")),
            ),
        ) {
            border(); floor(); pit(19..24)
            fill(25..28, 15..15, 'h')
            fill(29..30, 12..12, 'v')
            spawn(1, 14); door(30, 14)
        },

        // 35 — Headwind (R4, U7)
        Level(
            name = T("Headwind", "Gegenwind"),
            intro = T("Breezy today.", "Heute ist es windig."),
            start = listOf(
                Fan('w', at = 31 to 9, dir = Dir.LEFT, reach = 22, speed = 6.6f, width = 6),
                Fan('g', at = 23 to 9, dir = Dir.LEFT, reach = 4, speed = 14f, width = 6),
                Pad('1', at = 15 to 14),
            ),
            traps = listOf(
                trap(PastX(4f), Saw(-1f, 14.4f, 11f, 0f), say("A blade for your back. The wind has no say there.", "Ein Blatt für den Rücken. Der Wind hat da nichts zu melden.")),
                trap(Landed(8f, 14f), Saw(33f, 14.4f, -10f, 0f), say("Fan blade, fresh off the shelf.", "Lüfterblatt, frisch aus dem Regal.")),
                trap(PastX(11f), FanSet('w', 7.1f), say("Breezy.", "Luftig.")),
                trap(PastX(13f), FanSet('w', 7.6f), say("Brisk.", "Frisch.")),
                trap(Pressed('1'), Play(Card.BACKDRAFT), Power('g', false), FanSet('w', -8f), say("Tailwind, as requested.", "Rückenwind, wie bestellt.")),
                trap(PastX(25f), Power('g', true), Saw(33f, 14.4f, -10f, 0f), say("The wall closes behind you. The blade does not.", "Die Wand schließt hinter dir. Das Blatt nicht.")),
            ),
            // rematch: the button moved before the wind and bites: it raises the wall and the gale and sends a blade. Hop it, then slog the whole
            // corridor, with the blades coming from the front
            rematch = listOf(
                Round(
                    T("Same wind. Better manners.", "Gleicher Wind. Bessere Manieren."),
                    start = listOf(
                        Fan('w', at = 31 to 9, dir = Dir.LEFT, reach = 22, speed = 6.6f, width = 6),
                        Fan('g', at = 23 to 9, dir = Dir.LEFT, reach = 4, speed = 14f, width = 6), Power('g', false),
                        Pad('1', at = 7 to 14),
                    ),
                    traps = listOf(
                        trap(PastX(8f), Play(Card.DEVIL_SAW), Saw(-1f, 14.4f, 11f, 0f), say("A blade for your back, as before.", "Ein Blatt für den Rücken, wie gehabt.")),
                        trap(Pressed('1'), Power('g', true), FanSet('w', 9.5f), Saw(33f, 14.4f, -9f, 0f), say("You pressed it! Wall up, wind up, blade out.", "Du hast gedrückt! Wand hoch, Wind hoch, Blatt raus.")),
                        trap(PastX(10.5f), FanSet('w', 7f), say("Breezy again.", "Wieder luftig.")),
                        trap(PastX(13f), Saw(33f, 14.4f, -10f, 0f), say("And the one from the shelf. Regulars get two.", "Und das aus dem Regal. Stammgäste bekommen zwei.")),
                        trap(PastX(15.5f), FanSet('w', 6.2f), say("A little rest. Do not get used to it.", "Ein bisschen Ruhe. Gewöhn dich nicht dran.")),
                        trap(PastX(18f), FanSet('w', 7f), say("Brisk. Remember brisk?", "Frisch. Weißt du noch, frisch?")),
                        trap(PastX(19.5f), Saw(33f, 14.4f, -11f, 0f), say("Another one, fresh off the shelf.", "Noch eins, frisch aus dem Regal.")),
                        trap(PastX(22f), FanSet('w', 5.4f), say("A lull, as a treat.", "Eine Flaute, als Leckerli.")),
                        trap(PastX(24.5f), FanSet('w', 7f), say("And it picks up again.", "Und er zieht wieder an.")),
                        trap(PastX(26.5f), Saw(33f, 14.4f, -12f, 0f), say("The last one. I promise. Roughly.", "Das letzte. Versprochen. Ungefähr.")),
                        trap(PastX(28.5f), FanSet('w', 6.4f), say("Last gust. Door is right there.", "Letzte Böe. Die Tür ist gleich da.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(23..23, 1..9)
            spawn(1, 14); door(30, 14)
        },

        // 36 — Air Cushion (trap room, U6)
        Level(
            name = T("Air Cushion", "Luftkissen"),
            intro = T("Soft landing guaranteed.", "Weiche Landung garantiert."),
            start = listOf(Fan('d', at = 8 to 0, dir = Dir.DOWN, reach = 15, speed = 3f, width = 6)),
            traps = listOf(
                trap(Zone(8f, 3f, 14f, 5.5f), Play(Card.COLLAPSE), Move('A', 3.5f, 0f, 5f), say("The walls have teeth. They are shy.", "Die Wände haben Zähne. Sie sind schüchtern.")),
                trap(Zone(8f, 7f, 14f, 8.5f), Move('B', -4f, 0f, 5f), say("Other side now. Mind the draft.", "Jetzt die andere Seite. Vorsicht, Zugluft.")),
                trap(Landed(8f, 13.9f), Move('C', -9f, 0f, 4.5f), say("The cushion ends here. The hospitality does not.", "Hier endet das Kissen. Die Gastfreundschaft nicht.")),
            ),
        ) {
            border(); floor()
            fill(0..7, 4..14)
            fill(14..17, 1..12)
            fill(5..7, 7..7, 'A')
            fill(14..17, 10..10, 'B')
            put(30, 14, 'C')
            spawn(1, 3); door(29, 14)
        },

        // 37 — Lull (trap room, U3)
        Level(
            name = T("Lull", "Flaute"),
            intro = T("Union rules apply to fans too.", "Für Lüfter gilt der Betriebsrat."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT)),
            start = listOf(
                Fan('w', at = 21 to 9, dir = Dir.LEFT, reach = 15, speed = 6f, width = 6, on = 2.0f, off = 1.5f),
                Fan('u', at = 22 to 17, dir = Dir.UP, reach = 12, speed = 4f, width = 5), Power('u', false),
            ),
            traps = listOf(
                trap(PastX(5f), Play(Card.SINKING), Move('W', 24f, 0f, 3.2f), say("Break time is over. Everyone out.", "Pause vorbei. Alle raus.")),
                trap(PastX(19f), Power('u', true), say("The lift only runs for passengers.", "Der Aufzug fährt nur für Fahrgäste.")),
            ),
        ) {
            border(); floor(); pit(15..18)
            fill(21..21, 1..9)
            fill(27..30, 6..17)
            fill(1..1, 10..14, 'W')
            fill(9..13, 12..12, 'v')
            spawn(3, 14); door(29, 5)
        },

        // 38 — Silence (trap room, U15)
        Level(
            name = T("Silence", "Stille"),
            intro = T("It's so quiet in here.", "Schön leise hier."),
            start = listOf(
                Fan('f', at = 20 to 17, dir = Dir.UP, reach = 12, speed = 4.5f, width = 4), Power('f', false),
                Clock('Z', on = 1.1f, off = 1.9f), Circuit('Y', on = false), Circuit('k'),
            ),
            traps = listOf(
                trap(Touch('k'), Play(Card.SHORT_CIRCUIT), Power('f', true), say("Fan restarted. Surprised? Me too.", "Lüfter neu gestartet. Überrascht? Ich auch.")),
                trap(PastX(15f), FanSet('f', 7.5f), say("Passenger detected. Full power.", "Fahrgast erkannt. Volle Leistung.")),
                trap(Zone(20f, 3f, 24f, 8.5f), Power('Y', true), say("Welcome to the top. Mind the cable.", "Willkommen oben. Vorsicht, Kabel.")),
            ),
        ) {
            border(); floor()
            fill(1..3, 15..15, 'k')
            fill(19..23, 9..9, 'Z')
            fill(24..30, 6..17)
            fill(27..28, 5..5, 'Y')
            spawn(10, 14); door(30, 5)
        },

        // 39 — Downdraft (R10, U2)
        Level(
            name = T("Downdraft", "Fallwind"),
            intro = T("Gravity, but with a motor.", "Schwerkraft, aber mit Motor."),
            start = listOf(
                Fan('u', at = 3 to 17, dir = Dir.UP, reach = 12, speed = 6f, width = 4),
                Fan('d', at = 13 to 0, dir = Dir.DOWN, reach = 15, speed = 2.2f, width = 4),
            ),
            traps = listOf(
                trap(Zone(6.8f, 4.5f, 7.9f, 6.2f), Play(Card.HEADBUTT), Fall('c'), say("Mind the ceiling. It minds you.", "Achtung, die Decke. Sie achtet auf dich."), delay = 0.3f),
                trap(Landed(11f, 12.9f), Fall('h'), say("And the next one, for good measure.", "Und die nächste, zur Sicherheit."), delay = 0.3f),
                trap(Zone(13f, 9f, 17f, 11f), FanSet('d', 3.4f), say("Downdraft, now with a turbo.", "Fallwind, jetzt mit Turbo.")),
                trap(PastX(18f), Fall('e'), say("Second floor. Closing.", "Zweiter Stock. Schließt gleich."), delay = 0.35f),
            ),
        ) {
            border(); floor()
            fill(7..12, 6..14)
            fill(17..18, 1..9)
            put(10, 5, '^')
            fill(7..7, 1..2, 'c'); fill(11..12, 1..2, 'h')
            fill(20..23, 1..2, 'e')
            fill(25..28, 12..12, 'v')
            spawn(1, 14); door(29, 14)
        },

        // 40 — Air Castle (trap room, U12)
        Level(
            name = T("Air Castle", "Luftschloss"),
            intro = T("Wind is free. Take as much as you like.", "Wind ist kostenlos. Nimm, so viel du willst."),
            legend = mapOf('t' to Glyph(spike = true, hidden = true)),
            start = listOf(
                Fan('f', at = 9 to 17, dir = Dir.UP, reach = 5, speed = 9f, width = 14),
                Fan('g', at = 22 to 9, dir = Dir.LEFT, reach = 7, speed = 7f, width = 6), Power('g', false),
            ),
            traps = listOf(
                trap(PastX(10f), Play(Card.THROTTLE), FanSet('f', -4f), say("Reverse thrust! The keep is your friend.", "Schubumkehr! Der Bergfried ist dein Freund.")),
                trap(Touch('s'), FanSet('f', 9f), say("Thrust forward. Try to keep up.", "Schub voraus. Halt dich fest."), delay = 0.6f),
                trap(PastX(17f), Power('g', true), say("Headwind, courtesy of the castle.", "Gegenwind, mit besten Grüßen vom Schloss.")),
                trap(Landed(23f, 27f), Show('t'), say("The welcome mat has opinions.", "Die Fußmatte hat Meinungen.")),
            ),
            // rematch: the controls are twisted as you float, and the mat has no opinions this time: left is right all the way across
            rematch = listOf(
                Round(
                    T("Same castle. Fresh perspective.", "Gleiches Schloss. Frische Perspektive."),
                    traps = listOf(
                        trap(PastX(8.5f), Play(Card.TWISTED), Swap(true), say("Left is right. Probably.", "Links ist rechts. Vermutlich.")),
                        trap(PastX(10f), FanSet('f', -4f), say("Reverse thrust, as advertised.", "Schubumkehr, wie angekündigt.")),
                        trap(Touch('s'), FanSet('f', 9f), say("Thrust forward. Or backward. Who can tell.", "Schub voraus. Oder zurück. Wer weiß das schon."), delay = 0.6f),
                        trap(PastX(17f), Power('g', true), say("Headwind again. Press the other way.", "Wieder Gegenwind. Drück in die andere Richtung.")),
                        trap(PastX(22.5f), Swap(false), say("Untwisted. Surprise: left is left again.", "Entdreht. Überraschung: Links ist wieder links.")),
                    ),
                ) { put(29, 12, '.') },
            ),
        ) {
            border(); floor(); pit(9..22)
            fill(9..22, 17..17); fill(9..22, 16..16, '^')
            fill(14..17, 13..15, 's')
            fill(22..22, 1..9)
            fill(23..30, 13..14)
            put(29, 12, 't')
            spawn(1, 14); door(30, 12)
        },
    )
}
