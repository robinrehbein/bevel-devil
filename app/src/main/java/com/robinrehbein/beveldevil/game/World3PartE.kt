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
        // (keep moving), and over the second draft Mephi pauses the fan halfway up: its grille has teeth at the far end now, so drift back to the
        // bridge end as you drop, and ride again once it spins up.
        Level(
            name = T("Updraft", "Aufwind"),
            intro = T("Boarding now. Mind the gap.", "Einsteigen bitte. Achtung, Lücke."),
            start = listOf(
                Fan('f', at = 8 to 17, dir = Dir.UP, reach = 11, speed = 9f, width = 4),
                Fan('g', at = 25 to 9, dir = Dir.UP, reach = 7, speed = 9f, width = 4),
            ),
            traps = listOf(
                trap(Landed(12.2f, 15.9f), Play(Card.CRUMBLE), Fall('b'), say("Gate change: your plank just left.", "Gate geändert: Deine Planke ist schon weg."), delay = 0.1f),
                trap(Touch('c'), Fall('c'), say("Seats are not reserved. Not even for a second.", "Sitzplätze sind nicht reserviert. Nicht mal für eine Sekunde."), delay = 0.35f),
                trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 0f), say("Brief pause. Please hold your altitude.", "Kurze Pause. Bitte Höhe halten.")),
                trap(Zone(26f, 2.9f, 28.8f, 6.1f), FanSet('g', 9f), say("And up again. Mind the wall, not the view.", "Und wieder hoch. Achte auf die Wand, nicht auf die Aussicht."), delay = 1.7f),
            ),
            hint = T("Stand in the draft and hold on to the wall. Planks only give way once you are on them. When the second draft stops, drift back to the bridge.", "Stell dich in den Aufwind und halt dich an der Wand. Planken geben erst nach, wenn du drauf stehst. Bleibt der zweite Aufwind stehen, lass dich zur Brücke zurücktreiben."),
            // rematch: the planks hold now, and nothing waits for the second landing: but the first draft takes its lunch break while you
            // ride it (spikes on the floor of the pit: steer back to the edge as you fall, and ride again once it is back), and a stud has
            // grown on the far plank, exactly where the long leap from round one comes down. Hop it low instead
            rematch = listOf(
                Round(
                    T("Same ride. A different bridge.", "Gleiche Fahrt. Eine andere Brücke."),
                    traps = listOf(
                        trap(Zone(7f, 9.5f, 12f, 12.5f), FanSet('f', 0f), say("Lunch break. The draft is out. Mid-ride, yes.", "Mittagspause. Der Aufwind ist aus. Mitten in der Fahrt, ja.")),
                        trap(Landed(3.5f, 9.7f), FanSet('f', 9f), say("Back from lunch. Boarding again.", "Zurück aus der Pause. Wieder einsteigen."), delay = 0.4f),
                        trap(Landed(12.2f, 15.9f), Play(Card.SPIKE_SEED), Show('p'), say("Fresh from the factory: one plank, slightly pointy.", "Frisch ab Werk: eine Planke, leicht spitz.")),
                        trap(Zone(26f, 3f, 28.8f, 6f), FanSet('g', 0f), say("Another pause. Our regular passengers know it.", "Wieder eine Pause. Stammgäste kennen das.")),
                        trap(Zone(26f, 2.9f, 28.8f, 6.1f), FanSet('g', 9f), delay = 1.75f),
                    ),
                    legend = mapOf('p' to Glyph(spike = true, hidden = true)),
                ) { put(20, 7, 'p'); put(22, 5, '.'); fill(10..11, 16..16, '^') },
            ),
        ) {
            border(); floor(); pit(7..11)
            fill(8..11, 17..17)
            fill(12..12, 9..17)
            fill(12..15, 8..8); fill(16..19, 8..8, 'b'); fill(20..22, 8..8, 'c'); fill(23..24, 8..8)
            fill(22..24, 5..5, 'v')
            pit(13..28, 9)
            fill(25..28, 9..9); fill(27..28, 9..9, '^')
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
                trap(Landed(26f, 29.5f), Play(Card.OVERCLOCKED), HeatSpike('h', 0.55f), say("The landing strip is preheated.", "Die Landebahn ist vorgeheizt.")),
                trap(Airborne(22f, 25f), FanSet('w', -6f), say("The gust loses breath mid-air.", "Der Böe geht mitten in der Luft die Luft aus.")),
            ),
            hint = T("The gap is too wide for a run-up. Wait at the edge until the wind is back, then jump into it.", "Die Lücke ist zu breit für einen Anlauf. Warte am Rand, bis der Wind zurück ist, dann spring hinein."),
        ) {
            border(); floor(); pit(19..25)
            fill(26..28, 15..15, 'h')
            fill(29..30, 12..12, 'v')
            spawn(1, 14); door(30, 14)
        },

        // 35 — Headwind (R4, U7): a corridor against the wind, run from right to left this time (the start is at the right wall, the door at
        // the left), a blade from behind and one from the front. The button at the wall of wind does not open it right away: the tailwind carries
        // you into the gale and you stand there for a moment, with a blade on its way, until the gale is switched off. Round two is the other way
        // round: the gale is off, the button turns it on.
        Level(
            name = T("Headwind", "Gegenwind"),
            intro = T("Breezy today.", "Heute ist es windig."),
            start = listOf(
                Fan('w', at = 0 to 9, dir = Dir.RIGHT, reach = 22, speed = 6.6f, width = 6),
                Fan('g', at = 8 to 9, dir = Dir.RIGHT, reach = 4, speed = 17f, width = 6),
                Pad('1', at = 18 to 14),
            ),
            traps = listOf(
                trap(BeforeX(28f), Saw(33f, 14.4f, -11f, 0f), say("A blade for your back. The wind has no say there.", "Ein Blatt für den Rücken. Der Wind hat da nichts zu melden.")),
                trap(Landed(18f, 24f), Saw(-1f, 14.4f, 10f, 0f), say("A blade rolls down the corridor. Free of charge.", "Ein Blatt rollt durch den Flur. Kostenlos.")),
                trap(BeforeX(21f), FanSet('w', 7.6f), say("Breezy.", "Luftig.")),
                trap(Pressed('1'), Play(Card.BACKDRAFT), FanSet('w', -8f), say("Tailwind, as requested.", "Rückenwind, wie bestellt.")),
                trap(Pressed('1'), Power('g', false), say("The gale needs a moment to think it over.", "Der Sturm braucht einen Moment zum Nachdenken."), delay = 1.0f),
                trap(BeforeX(7f), Power('g', true), Saw(-1f, 14.4f, 10f, 0f), say("The wall closes behind you. The blade does not.", "Die Wand schließt hinter dir. Das Blatt nicht.")),
            ),
            hint = T("The gale will not let you through until the button has had a moment. Wait in front of it, and hop the blade that follows you through.", "Der Sturm lässt dich erst durch, wenn der Knopf ausgelöst hat. Warte davor, und hüpf über das Blatt, das dir durch folgt."),
            // rematch: the gale is down and the button puts it up: do not press it, hop it. The wind turns on its way across and two blades follow
            rematch = listOf(
                Round(
                    T("Same wind. Better manners.", "Gleicher Wind. Bessere Manieren."),
                    start = listOf(
                        Fan('w', at = 0 to 9, dir = Dir.RIGHT, reach = 22, speed = 6.6f, width = 6),
                        Fan('g', at = 8 to 9, dir = Dir.RIGHT, reach = 4, speed = 14f, width = 6), Power('g', false),
                        Pad('1', at = 24 to 14),
                    ),
                    traps = listOf(
                        trap(BeforeX(24f), Play(Card.DEVIL_SAW), Saw(33f, 14.4f, -11f, 0f), say("A blade for your back, as before.", "Ein Blatt für den Rücken, wie gehabt.")),
                        trap(Pressed('1'), Power('g', true), FanSet('w', 9.5f), Saw(-1f, 14.4f, 9f, 0f), say("You pressed it! Wall up, wind up, blade out.", "Du hast gedrückt! Wand hoch, Wind hoch, Blatt raus.")),
                        trap(BeforeX(21.5f), FanSet('w', 7f), say("Breezy again.", "Wieder luftig.")),
                        trap(BeforeX(19f), Saw(-1f, 14.4f, 10f, 0f), say("And the one from the shelf. Regulars get two.", "Und das aus dem Regal. Stammgäste bekommen zwei.")),
                        trap(BeforeX(16f), FanSet('w', 5.6f), say("A little rest. Do not get used to it.", "Ein bisschen Ruhe. Gewöhn dich nicht dran.")),
                        trap(BeforeX(12.5f), Saw(-1f, 14.4f, 11f, 0f), say("Blade number three. Do not count.", "Blatt Nummer drei. Nicht mitzählen.")),
                        trap(BeforeX(10f), FanSet('w', 6.2f), say("And it picks up again.", "Und er zieht wieder an.")),
                        trap(BeforeX(7.6f), Saw(-1f, 14.4f, 12f, 0f), say("The last one. I promise. Roughly.", "Das letzte. Versprochen. Ungefähr.")),
                        trap(BeforeX(5.5f), FanSet('w', 3f), say("Wind dropping. Suspicious, is it not?", "Der Wind lässt nach. Verdächtig, oder?")),
                        trap(BeforeX(4.5f), Saw(-1f, 14.4f, 12f, 0f), say("I lied about the last one.", "Beim letzten hab ich gelogen.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(8..8, 1..9)
            spawn(30, 14); door(1, 14)
        },

        // 36 — Air Cushion (trap room, U6): sink down the draft past bars that slide out of alternating walls, hop the pin along the floor,
        // and do not wait for it on the stretch of floor that is only on loan: it sinks under whoever stands on it.
        Level(
            name = T("Air Cushion", "Luftkissen"),
            intro = T("Soft landing guaranteed.", "Weiche Landung garantiert."),
            start = listOf(Fan('d', at = 8 to 0, dir = Dir.DOWN, reach = 15, speed = 3f, width = 6)),
            traps = listOf(
                trap(Zone(8f, 3f, 14f, 5.5f), Move('A', 3.5f, 0f, 5f), say("The walls have teeth. They are shy.", "Die Wände haben Zähne. Sie sind schüchtern.")),
                trap(Zone(8f, 7f, 14f, 8.5f), Move('B', -4f, 0f, 5f), say("Other side now. Mind the draft.", "Jetzt die andere Seite. Vorsicht, Zugluft.")),
                trap(Zone(8f, 11.9f, 14f, 12.6f), Move('E', 2.5f, 0f, 5f), say("And back to the first side. Teeth come in rows.", "Und zurück zur ersten Seite. Zähne kommen in Reihen.")),
                trap(Landed(8f, 16f), Move('C', -14f, 0f, 4.5f), say("The cushion ends here. The hospitality does not.", "Hier endet das Kissen. Die Gastfreundschaft nicht.")),
                trap(Touch('k'), Play(Card.COLLAPSE), Move('k', 0f, 6f, 3f), say("This bit of floor is on loan. No waiting for pins on it.", "Dieses Stück Boden ist geliehen. Hier wartet keiner auf Stifte."), delay = 0.2f),
            ),
            hint = T("The bars come from alternating sides. Keep moving across the shaft as you sink, and hop the last one along the floor.", "Die Balken kommen abwechselnd von beiden Seiten. Wechsle im Schacht die Seite, während du sinkst, und hüpf unten über den letzten."),
        ) {
            border(); floor()
            fill(0..7, 4..14)
            fill(14..17, 1..12)
            fill(5..7, 7..7, 'A')
            fill(14..17, 10..10, 'B'); fill(5..7, 13..13, 'E')
            put(30, 14, 'C')
            pit(20..22); fill(20..22, 15..15, 'k')
            spawn(1, 3); door(29, 14)
        },

        // 37 — Lull (trap room, U3)
        Level(
            name = T("Lull", "Flaute"),
            intro = T("Union rules apply to fans too.", "Für Lüfter gilt der Betriebsrat."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT)),
            start = listOf(
                Fan('w', at = 21 to 9, dir = Dir.LEFT, reach = 15, speed = 6f, width = 6, on = 3.0f, off = 1.5f),
                Fan('u', at = 22 to 17, dir = Dir.UP, reach = 12, speed = 4f, width = 5), Power('u', false),
            ),
            traps = listOf(
                trap(PastX(5f), Move('W', 24f, 0f, 3.2f), say("Break time is over. Everyone out.", "Pause vorbei. Alle raus.")),
                trap(PastX(9f), FanSet('w', 4.8f), say("Coffee break: the draft takes five.", "Kaffeepause: Der Zug macht fünf Minuten.")),
                trap(PastX(19f), Power('u', true), say("The lift only runs for passengers.", "Der Aufzug fährt nur für Fahrgäste.")),
                trap(Landed(19f, 21.9f), Play(Card.SINKING), Move('f', 0f, 3f, 6f), say("Do not linger at the lift. The floor has a schedule too.", "Nicht am Aufzug trödeln. Der Boden hat auch einen Fahrplan."), delay = 0.45f),
            ),
            hint = T("The headwind rests in rhythm. Sprint while it rests, and do not stop at the lift.", "Der Gegenwind ruht im Takt. Sprinte, solange er ruht, und trödle nicht am Aufzug."),
        ) {
            border(); floor(); pit(15..18)
            fill(21..21, 1..9)
            fill(27..30, 6..17)
            fill(19..21, 15..16, 'f')
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
                Clock('Z', on = 1.1f, off = 1.9f), Circuit('Y', on = false), Circuit('k'), Circuit('X', on = false),
            ),
            traps = listOf(
                trap(Touch('k'), Play(Card.SHORT_CIRCUIT), Power('f', true), say("Fan restarted. Surprised? Me too.", "Lüfter neu gestartet. Überrascht? Ich auch.")),
                trap(PastX(12.5f), Power('X', true), say("Floor cable live. Silence has a price.", "Bodenkabel unter Strom. Stille hat ihren Preis.")),
                trap(Zone(20f, 9.6f, 24f, 14.5f), FanSet('f', 7.5f), say("Passenger detected. Full power. All the way up.", "Fahrgast erkannt. Volle Leistung. Ganz nach oben.")),
                trap(Zone(20f, 3f, 24f, 8.5f), Power('Y', true), say("Welcome to the top. Mind the cable.", "Willkommen oben. Vorsicht, Kabel.")),
            ),
            hint = T("Touch the copper on the left first: it brings the fan back. Time the blinking floor, and step off the draft at the top before it pins you to the ceiling.", "Berühr zuerst das Kupfer links: Es holt den Lüfter zurück. Pass den blinkenden Boden ab, und tritt oben aus dem Luftstrom, bevor er dich an die Decke drückt."),
        ) {
            border(); floor()
            fill(1..3, 15..15, 'k')
            fill(19..23, 9..9, 'Z')
            fill(24..30, 6..17)
            fill(27..28, 5..5, 'Y')
            fill(20..23, 3..3, 'v'); fill(14..14, 14..14, 'X')
            spawn(10, 14); door(30, 5)
        },

        // 39 — Downdraft (R10, U2): you start up on the roof of the machine at the left. A loose piece of ceiling over the far end of the roof
        // drops as you head for it (stop short, let it land, hop it), then you are in the downdraft, which lowers you gently, until it kicks into
        // turbo halfway down. Down at the bottom the way to the door runs under a low ceiling, and its loose panel drops ahead of you as you come: stop short
        // of it, let it land, hop it. The bottom of the shaft is spiked: hold on to the wall, and step out under it before the turbo lands you.
        Level(
            name = T("Downdraft", "Fallwind"),
            intro = T("Gravity, but with a motor.", "Schwerkraft, aber mit Motor."),
            start = listOf(
                Fan('d', at = 13 to 0, dir = Dir.DOWN, reach = 15, speed = 2.2f, width = 4),
            ),
            traps = listOf(
                trap(PastX(8.3f), Play(Card.HEADBUTT), Fall('h'), say("Walking on the roof? The ceiling takes that personally.", "Auf dem Dach spazieren? Das nimmt die Decke persönlich."), delay = 0.1f),
                trap(Zone(13f, 7f, 17f, 9f), FanSet('d', 6f), say("Downdraft, now with a turbo.", "Fallwind, jetzt mit Turbo.")),
                trap(PastX(18f), Fall('e'), say("Ground floor. The ceiling comes down to say hello. In front of you.", "Erdgeschoss. Die Decke kommt runter und sagt Hallo. Vor dir.")),
            ),
            hint = T("Ceilings here come down in front of you. Stop short, let them land, then hop them.", "Decken kommen hier vor dir herunter. Bleib davor stehen, lass sie landen, dann spring drüber."),
        ) {
            border(); floor()
            fill(1..12, 6..14)
            fill(17..18, 1..9)
            fill(11..12, 1..2, 'h')
            fill(19..30, 1..8); fill(20..22, 9..10, 'e')
            fill(27..28, 9..9, 'v'); fill(13..16, 14..14, '^')
            spawn(3, 5); door(29, 14)
        },

        // 40 — Air Castle (trap room, U12): you start at the right and float a draft over a pit of spikes to the keep in the middle and on to
        // the ledge at the far left, where the door is. The draft reverses as you float (the keep is the one safe place), then spins up again,
        // and a headwind from the castle wall waits over the second half; the welcome mat in front of the door has opinions.
        Level(
            name = T("Air Castle", "Luftschloss"),
            intro = T("Wind is free. Take as much as you like.", "Wind ist kostenlos. Nimm, so viel du willst."),
            legend = mapOf('t' to Glyph(spike = true, hidden = true)),
            start = listOf(
                Fan('f', at = 9 to 17, dir = Dir.UP, reach = 5, speed = 9f, width = 14),
                Fan('g', at = 9 to 9, dir = Dir.RIGHT, reach = 7, speed = 7f, width = 6), Power('g', false),
            ),
            traps = listOf(
                trap(BeforeX(22f), Play(Card.THROTTLE), FanSet('f', -4f), say("Reverse thrust! The keep is your friend.", "Schubumkehr! Der Bergfried ist dein Freund.")),
                trap(Touch('s'), FanSet('f', 9f), say("Thrust forward. Try to keep up.", "Schub voraus. Halt dich fest."), delay = 0.8f),
                trap(BeforeX(15f), Power('g', true), say("Headwind, courtesy of the castle.", "Gegenwind, mit besten Grüßen vom Schloss.")),
                trap(Landed(5f, 9f), Show('t'), say("The welcome mat has opinions.", "Die Fußmatte hat Meinungen.")),
            ),
            hint = T("The reverse thrust throws you back, but the keep in the middle is a fine place to stand. The welcome mat is not.", "Die Schubumkehr wirft dich zurück, aber der Bergfried in der Mitte ist ein guter Standplatz. Die Fußmatte nicht."),
            // rematch: the controls are twisted as you float, untwisted in mid-flight under the headwind, and twisted once more as you
            // land on the far side (the mat has no opinions this time: the keys do)
            rematch = listOf(
                Round(
                    T("Same castle. Fresh perspective.", "Gleiches Schloss. Frische Perspektive."),
                    traps = listOf(
                        trap(BeforeX(23.5f), Play(Card.TWISTED), Swap(true), say("Left is right. Probably.", "Links ist rechts. Vermutlich.")),
                        trap(BeforeX(22f), FanSet('f', -4f), say("Reverse thrust, as advertised.", "Schubumkehr, wie angekündigt.")),
                        trap(Touch('s'), FanSet('f', 9f), say("Thrust forward. Or backward. Who can tell.", "Schub voraus. Oder zurück. Wer weiß das schon."), delay = 0.6f),
                        trap(BeforeX(15f), Power('g', true), say("Headwind again. Press the other way.", "Wieder Gegenwind. Drück in die andere Richtung.")),
                        trap(BeforeX(12.5f), Swap(false), say("Untwisted, mid-flight. Surprise: left is left again.", "Entdreht, mitten im Flug. Überraschung: Links ist wieder links.")),
                        trap(Landed(5f, 9f), Swap(true), say("And twisted again. I could not decide.", "Und wieder verdreht. Ich konnte mich nicht entscheiden.")),
                    ),
                ) { put(2, 12, '.') },
            ),
        ) {
            border(); floor(); pit(9..22)
            fill(9..22, 17..17); fill(9..22, 16..16, '^')
            fill(14..17, 13..15, 's')
            fill(9..9, 1..9)
            fill(1..8, 13..14)
            put(2, 12, 't')
            spawn(30, 14); door(1, 12)
        },
    )
}
