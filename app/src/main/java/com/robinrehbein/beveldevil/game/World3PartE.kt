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
                trap(Landed(12.2f, 15.9f), Play(Card.SINKING), Fall('b'), say("Gate change: your plank just left.", "Gate geändert: Deine Planke ist schon weg."), delay = 0.1f),
                trap(Landed(20f, 22.9f), Fall('c'), say("Seats are not reserved.", "Sitzplätze sind nicht reserviert."), delay = 0.4f),
                trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 0f), say("Brief pause. Please hold your altitude.", "Kurze Pause. Bitte Höhe halten.")),
                trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 9f), delay = 1.7f),
            ),
            hint = T("Stand in the draft and hold on to the wall. Planks only give way once you are on them.", "Stell dich in den Aufwind und halt dich an der Wand. Planken geben erst nach, wenn du drauf stehst."),
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

        // 34 — Tailwind (breather, U12 as gusts that come and go): scratch v13
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

        // 35 — Headwind (R4, U7): scratch v3
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
        ) {
            border(); floor()
            fill(23..23, 1..9)
            spawn(1, 14); door(30, 14)
        },

        // 36 — Air Cushion (trap room, U6): scratch v1
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

        // 37 — Lull (trap room, U3): scratch v2
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

        // 38 — the fan is dead until you step on the reset pad; leaving the draft, a dead cable wakes up, and the landing is warm
        Level(
            name = T("Silence", "Stille"),
            intro = T("It's so quiet in here.", "Schön leise hier."),
            start = listOf(
                Fan('f', at = 9 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2), Power('f', false),
                Pad('1', at = 5 to 14), Circuit('Z', on = false),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.DECOY), Power('f', true), say("Fan restarted. Surprised? Me too.", "Lüfter neu gestartet. Überrascht? Ich auch.")),
                trap(Landed(12f, 17f), Power('Z', true), say("Sorry, a cable on the ledge.", "Sorry, ein Kabel auf der Kante.")),
                trap(Airborne(17f, 22f), HeatSpike('g', 0.7f), say("Landing pad: toasty.", "Landeplatz: kuschelig.")),
            ),
        ) {
            border(); floor()
            fill(12..30, 8..8); fill(21..22, 8..8, 'g')
            fill(18..19, 7..7, 'Z')
            spawn(); door(28, 7)
        },

        // 39 — a downdraft over a short pit: you cannot jump against it, only between its gusts; the edge where you wait for them warms up (wait further back, sprint), the plates behind it are warm, and a blade rolls in
        Level(
            name = T("Downdraft", "Fallwind"),
            intro = T("Gravity, but with a motor.", "Schwerkraft, aber mit Motor."),
            start = listOf(Fan('d', at = 15 to 0, dir = Dir.DOWN, reach = 14, speed = 9f, width = 4, on = 2f, off = 2.4f)),
            traps = listOf(
                trap(Zone(13f, 13f, 15f, 15f), HeatSpike('w', 0.45f), say("The waiting room is heated. You're welcome.", "Der Warteraum ist beheizt. Gern geschehen.")),
                trap(Landed(18.5f, 21.5f), Play(Card.OVERCLOCKED), HeatSpike('g', 0.7f), say("Made it across. The plates are warm.", "Drüben. Die Platten sind warm.")),
                trap(Landed(25.5f, 28f), Saw(33f, 14.4f, -6f, 0f), say("And a fan blade for the road.", "Und ein Lüfterblatt für unterwegs.")),
            ),
            // rematch: the heating moved to the back rows, where round 1 was safe: wait at the edge now
            rematch = listOf(
                Round(
                    T("Downdraft, reheated.", "Fallwind, zweiter Aufguss."),
                    start = listOf(Fan('d', at = 15 to 0, dir = Dir.DOWN, reach = 14, speed = 9f, width = 4, on = 2f, off = 2.4f)),
                    traps = listOf(
                        trap(Zone(9f, 13f, 11.5f, 15f), HeatSpike('u', 0.7f), say("Heating moved to the back rows.", "Die Heizung ist nach hinten umgezogen."), delay = 0.9f),
                        trap(Landed(18.5f, 21.5f), Play(Card.OVERCLOCKED), HeatSpike('g', 0.7f), say("Across. The plates are warm, as usual.", "Drüben. Die Platten sind warm, wie üblich.")),
                        trap(Landed(25.5f, 28f), Saw(33f, 14.4f, -6f, 0f), say("The blade is a regular.", "Das Lüfterblatt ist Stammgast.")),
                    ),
                ) { fill(7..10, 15..15, 'u') },
            ),
        ) {
            border(); floor(); pit(15..18)
            fill(11..14, 15..15, 'w'); fill(23..25, 15..15, 'g')
            spawn(); door()
        },

        // 40 — the updraft over the spikes reverses while you float: settle on the stone, it comes back; the landing is warm
        Level(
            name = T("Air Castle", "Luftschloss"),
            intro = T("Wind is free. Take as much as you like.", "Wind ist kostenlos. Nimm, so viel du willst."),
            start = listOf(Fan('f', at = 9 to 17, dir = Dir.UP, reach = 7, speed = 8f, width = 16)),
            traps = listOf(
                trap(PastX(11.3f), Play(Card.BACKDRAFT), FanSet('f', -9f), say("Reverse thrust! (The stone is your friend.)", "Schubumkehr! (Der Stein ist dein Freund.)")),
                trap(Touch('s'), FanSet('f', 8f), say("Thrust forward. Try to keep up.", "Schub voraus. Halt dich fest.")),
                trap(Landed(25f, 31f), HeatSpike('g', 0.7f), say("Solid ground. Warm solid ground.", "Fester Boden. Warmer fester Boden.")),
            ),
        ) {
            border(); floor(); pit(9..24)
            fill(9..24, 17..17); fill(9..24, 16..16, '^')
            fill(11..13, 15..16, 's')
            fill(27..28, 15..15, 'g')
            spawn(); door()
        },

    )
}
