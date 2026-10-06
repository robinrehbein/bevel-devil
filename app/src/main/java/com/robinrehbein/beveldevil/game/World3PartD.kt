package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Extend
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Heatsink
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Toggle
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Heated
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 3, act 2, block D (levels 25-32: Cooling Tower, Hot Wire, Wall Socket, Break Time, Thermostat, Burn-in Test, Pit Stop,
 * Thermal Runaway), rebuilt under the V2 level design (docs/LEVEL_DESIGN_V2.md, section 8). The bot solutions are in the test sources
 * ([World3RoomsD]).
 */
object World3PartD {
    val levels: List<Level> = listOf(

        // 25 — a cooling tower in three floors, run down and back along belts (R10 transport, U12 the belts turn around). You start on the
        // roof on the right and run off its left end onto the second floor, whose plates are cooler than they look until you land: one flares,
        // so do not run on it, hop it. Off its left end you drop onto the ground belts, which wait idle at a walking pace and carry you to
        // the door; the first turns around as you reach its end and pushes you back into the spikes in the corner: hop onto the next belt, which
        // turns around too, under a ceiling of spikes you cannot hop high under. Low hops all the way to the door. Belts only push whoever stands on them.
        Level(
            name = T("Cooling Tower", "Kühlturm"),
            intro = T("Conveyor tour of the tower. Hold on to nothing.", "Förderband-Tour durch den Turm. Nichts festhalten."),
            start = listOf(Belt('c', 2f), Belt('b', 2f)),
            traps = listOf(
                trap(Landed(12f, 24f), Play(Card.OVERCLOCKED), HeatSpike('a', 0.8f), say("Floor two runs the heating. Cooling tower, I know.", "Etage zwei heizt. Ist ein Kühlturm, ich weiß.")),
                trap(Zone(9f, 9f, 10.4f, 12.3f), Belt('c', -12f), say("Belt one: change of plans. Yours.", "Band eins: Planänderung. Deine.")),
                trap(Zone(17f, 9f, 18.4f, 12.3f), Belt('b', -12f), say("Belt two read the memo.", "Band zwei hat das Memo gelesen.")),
            ),
            hint = T("Belts only push whoever stands on them. In the air you are your own boss, but mind the ceiling.", "Bänder schieben nur, wer auf ihnen steht. In der Luft bist du dein eigener Chef, aber Vorsicht, die Decke."),
        ) {
            border(); floor(12)
            fill(23..30, 2..3)
            fill(7..24, 6..7); fill(7..24, 6..6, 'a')
            fill(1..10, 12..12, 'c'); fill(11..30, 12..12, 'b')
            fill(1..1, 11..11, '^'); fill(16..24, 8..9, 'v')
            spawn(29, 1); door(29, 11)
        },

        // 26 — two lanes of live copper and one cable that has opinions (U13, a trap room). Lower lane: as you pass the first post a tripwire
        // arms itself ahead of you at ankle height (hop it), and the landing behind it gets a flash a moment after you touch down (keep
        // going). Up the hot steps at the end (the second one is hotter than it looks) and back along the upper lane towards the door above
        // the start: a cable hangs over the way, cold until you come up the stairs, then it warms up for a moment (wait before it, not under
        // it), and a last tripwire is strung in front of the door.
        Level(
            name = T("Hot Wire", "Heißer Draht"),
            intro = T("Mind the cable. It minds you.", "Achte auf das Kabel. Es achtet auf dich."),
            start = listOf(Heat('j', rise = 1.1f), Laser('c', 10 to 1, 10 to 8), Power('c', false)),
            traps = listOf(
                trap(PastX(6.5f), Play(Card.SPIKE_SEED), Laser('a', 12 to 14, 13 to 14, on = 99f, delay = 0.55f), say("Tripwire. Self-assembling.", "Stolperdraht. Selbstmontierend.")),
                trap(Landed(14.2f, 17.5f), Laser('b', 14 to 14, 19 to 14, on = 0.7f, off = 60f, delay = 0.65f), say("Landing light. It is on request. Yours.", "Landebeleuchtung. Auf Anfrage. Deine.")),
                trap(Landed(20f, 23.5f), HeatSpike('j', 0.75f), say("The second step is a hot seat.", "Die zweite Stufe ist ein heißer Stuhl.")),
                trap(Zone(14.5f, 7f, 17f, 9.2f), Power('c', true), say("The cable is warming up. Take a seat. Not under it.", "Das Kabel läuft warm. Setz dich. Nicht darunter.")),
                trap(Zone(14.5f, 7f, 17f, 9.2f), Power('c', false), delay = 1.8f),
                trap(Zone(10.6f, 7f, 12.1f, 9.2f), Laser('e', 6 to 8, 7 to 8, on = 99f, delay = 0.3f), say("Tripwire, mirrored. I have a drawer full.", "Stolperdraht, gespiegelt. Ich hab eine Schublade voll.")),
            ),
            hint = T("The wire only wakes up once you pass it. The cable on the upper lane takes a moment: let it.", "Der Draht wacht erst auf, wenn du vorbei bist. Das Kabel oben braucht einen Moment: gönn es ihm."),
        ) {
            border(); floor()
            fill(1..19, 9..10)
            fill(25..30, 13..14); fill(20..23, 11..11)
            fill(20..23, 11..11, 'j')
            spawn(); door(3, 8)
        },

        // 27 ★ — a breather with one gag: the floor is a power strip, and the strip has to boot. As you come up to it, Mephi pulls the plug: the strip
        // goes dark and powers up again piece by piece, left to right, like a runway lighting up (wait for the first piece, then run with the wave;
        // each piece goes dark again behind you), and the last plates before the door have been overclocked while you were busy looking down (hop them)
        Level(
            name = T("Wall Socket", "Steckdose"),
            intro = T("Surge protector: installed.", "Überspannungsschutz: eingebaut."),
            start = listOf(Heat('f', rise = 0.8f, cool = 6f)),
            traps = listOf(
                trap(PastX(19.5f), HeatSpike('f', 0.85f), say("Overvoltage! It was a feature request.", "Überspannung! Das war ein Feature-Wunsch.")),
                trap(
                    PastX(3.2f), Play(Card.SHORT_CIRCUIT),
                    Clock('a', on = 1.2f, off = 99f, phase = 99.4f), Clock('b', on = 1.2f, off = 99f, phase = 99.07f), Clock('c', on = 1.2f, off = 99f, phase = 98.74f),
                    Clock('d', on = 1.2f, off = 99f, phase = 98.41f), Clock('e', on = 1.2f, off = 99f, phase = 98.08f),
                    say("Power saving: the strip is off. Boot sequence: 1... 2... 3...", "Stromsparmodus: Die Leiste ist aus. Hochfahren: 1... 2... 3..."),
                ),
            ),
            hint = T("The strip boots from the left, one piece after the other. Start when the first one lights up.", "Die Leiste fährt von links hoch, Stück für Stück. Los, wenn das erste leuchtet."),
        ) {
            border(); floor()
            bridge(6..8, 'a'); bridge(9..11, 'b'); bridge(12..14, 'c'); bridge(15..17, 'd'); bridge(18..20, 'e')
            fill(23..25, 15..15, 'f')
            spawn(); door()
        },

        // 28 — the break room (U7, a trap room): you start on a shelf and drop to the floor, which has two deep dips, each a hot plate with a
        // heatsink on a step at its far end to sit on. As you land a fan blade comes in along the floor at ankle height; the dips are the one
        // place it cannot reach: sit on the heatsink while it passes overhead. The moment you sit down a second blade crawls out of the wall of
        // the dip at the height of the bench: climb out as soon as the first has gone by. The second bench is the same, with the blades coming
        // from both sides.
        // Rematch: the break is over. The benches are filled in, the dips are only three wide (hop them, a blade patrols the bottom of each)
        // and the blades from the front fly at head height now: waiting under one is fine, hopping into one is the end of you, so each hop waits
        // for the blade to be well past. The way out is a staircase of three steps instead of the floor.
        Level(
            name = T("Break Time", "Pause"),
            intro = T("Take a seat on the heatsink. Relax.", "Setz dich auf den Kühlkörper. Entspann dich."),
            start = listOf(Heat('h', rise = 1.3f), Heat('g', rise = 1.3f), Heatsink('k', cools = "hg"), Heatsink('l', cools = "hg")),
            traps = listOf(
                trap(Landed(8f, 12.9f), Play(Card.DEVIL_SAW), Saw(33f, 12.4f, -8f, 0f), say("Fan blade! It's rotating. On the floor.", "Lüfterblatt! Es dreht sich. Am Boden.")),
                trap(Zone(17f, 13.8f, 19f, 15.1f), PathSaw(3.2f, 11f to 14.4f, 18.4f to 14.4f), say("Fans have more than one blade.", "Lüfter haben mehr als ein Blatt.")),
                trap(Zone(25f, 13.8f, 27f, 15.1f), Saw(33f, 12.4f, -7.5f, 0f), PathSaw(2.6f, 20f to 14.4f, 26.4f to 14.4f), say("Second bench, second blades. I do the seating plan.", "Zweite Bank, zweite Blätter. Ich mache den Sitzplan.")),
            ),
            hint = T("The dips are the one place the blades cannot reach. Sit on the heatsink, and do not stay.", "Die Mulden sind der einzige Ort, den die Blätter nicht erreichen. Setz dich auf den Kühlkörper, aber bleib nicht."),
            rematch = listOf(
                Round(
                    T("Break is over. The benches are decoration now.", "Pause vorbei. Die Bänke sind jetzt Deko."),
                    hint = T("Do not sit down in there. Cross the dips in the air, and let each high blade go by first.", "Setz dich da unten nicht hin. Spring über die Mulden, und lass erst das hohe Blatt vorbei."),
                    start = listOf(Heat('h', rise = 1.3f), Heat('g', rise = 1.3f)),
                    traps = listOf(
                        trap(
                            Landed(6f, 12f), Play(Card.THROTTLE), Heat('h', rise = 0.5f), Heat('g', rise = 0.5f), Saw(18f, 10.9f, -8f, 0f),
                            PathSaw(3.2f, 13.2f to 14.4f, 15.4f to 14.4f),
                            say("Staff meeting in the break room. Blades only.", "Teambesprechung im Pausenraum. Nur Blätter."),
                        ),
                        trap(Landed(16f, 19f), Saw(28f, 10.9f, -8f, 0f), PathSaw(3.2f, 21.2f to 14.4f, 23.4f to 14.4f), say("Second shift, same height. Ergonomics is a lifestyle.", "Zweite Schicht, gleiche Höhe. Ergonomie ist ein Lebensstil.")),
                        trap(Landed(24f, 26.5f), Saw(33f, 12.4f, -8f, 0f), say("One more for the stairs. Hop, do not queue.", "Noch eins für die Treppe. Hüpf, nicht anstellen.")),
                    ),
                ) {
                    fill(5..6, 5..6, '.')
                    fill(16..18, 13..15); fill(24..26, 13..15)
                    fill(26..27, 11..12); fill(28..29, 9..12); fill(30..30, 7..12); door(30, 6)
                },
            ),
        ) {
            border(); floor(13)
            fill(1..6, 5..6)
            pit(13..18, 13); fill(13..18, 16..17); fill(13..16, 16..16, 'h'); fill(17..18, 15..15, 'k')
            pit(21..26, 13); fill(21..26, 16..17); fill(21..24, 16..16, 'g'); fill(25..26, 15..15, 'l')
            spawn(2, 4); door(29, 12)
        },

        // 29 — a puzzle room (R2 the hold switch, U15 the help is the trap). You start in a crawl space under a low roof, and the only way on is a hatch of
        // copper in the roof at the far end that is open only while you stand on the pad beneath it: stand on it, jump straight up through the hatch
        // (the copper cannot close on you in the gap). The moment you press, a slab above the hatch comes down through it: it is meant for whoever
        // stays on the pad. On the roof the way leads back left: a second slab falls on the path as you come (let it land, then hop it), the
        // plates before the far end are overclocked (hop them), and the door is behind a wall of copper at the end of the roof that a second pad opens for as long as you stand on it:
        // step on the pad and walk straight off it into the wall.
        Level(
            name = T("Thermostat", "Thermostat"),
            intro = T("The wall has a thermostat. Somewhere.", "Die Wand hat ein Thermostat. Irgendwo."),
            start = listOf(
                Circuit('w'), Pad('1', at = 27 to 14, circuits = "w", mode = PadMode.HOLD),
                Circuit('n'), Pad('2', at = 5 to 12, circuits = "n", mode = PadMode.HOLD),
                Heat('q', rise = 0.8f, cool = 6f),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.HEADBUTT), Fall('s'), say("Thermostat: set to 'crush'. Stand still to enjoy.", "Thermostat: auf 'zerquetschen' gestellt. Stehenbleiben zum Genießen."), delay = 0.7f),
                trap(Zone(20f, 11f, 21.5f, 13f), Fall('t'), say("Ceiling insulation. It comes off in one piece.", "Deckendämmung. Kommt in einem Stück runter."), delay = 0.15f),
                trap(Zone(12.4f, 11f, 13.6f, 13f), HeatSpike('q', 0.85f), say("The roof has a setting for 'warm'. This is the other one.", "Das Dach hat eine Stufe 'warm'. Das ist die andere.")),
            ),
            hint = T("The pad opens the hatch above it for as long as you stand on it. Jump straight up and do not stay.", "Der Knopf öffnet die Luke über ihm, solange du draufstehst. Spring senkrecht hoch und bleib nicht."),
        ) {
            border(); floor()
            fill(1..30, 13..13); fill(26..28, 13..13, 'w'); fill(9..11, 13..13, 'q')
            fill(3..4, 1..12, 'n'); fill(28..28, 14..14); fill(1..6, 14..14)
            fill(26..28, 1..1, 's'); fill(15..16, 1..1, 't')
            spawn(8, 14); door(2, 12)
        },

        // 30 — the burn-in test (U14, a trap room: the lie is the spikes). An arch: a staircase of hot steps up to a shelf (hop it, the steps flare as
        // you land on the first), along the shelf over two patches of overclocked plates (hop them) and off its end to the floor, where the way to the
        // door leads back left underneath it. Across the way lie a pair of spikes, and behind them hidden ones: the spikes are a test pattern, they
        // sink after you land, so whoever hops them lands where the hidden ones sprout for whoever is in the air. The plates in front of the door
        // flare as you turn into the lane (hop them).
        // Rematch: the test pattern is real this time. Round 1's wait for the spikes to sink is the end of you, one single spike stands in the
        // lane and has to be hopped, the plates come first; and the shelf has a second layer: hidden spikes sprout in front of whoever runs along it.
        Level(
            name = T("Burn-in Test", "Einbrenntest"),
            intro = T("Stress test: walk to the door. Easy.", "Stresstest: lauf zur Tür. Ganz leicht."),
            legend = mapOf('T' to Glyph(spike = true, hidden = true), 'U' to Glyph(spike = true, hidden = true)),
            start = listOf(Heat('f', rise = 0.8f, cool = 6f), Heat('l', rise = 0.8f, cool = 6f), Heat('m', rise = 0.8f, cool = 6f), Heat('b', rise = 0.8f, cool = 6f)),
            traps = listOf(
                trap(Landed(2.5f, 5.5f), HeatSpike('f', 0.4f), say("Every step is a pixel. Mine, all mine.", "Jede Stufe ist ein Pixel. Meine, alle meine.")),
                trap(Zone(8.5f, 7f, 10.2f, 9.2f), Play(Card.OVERCLOCKED), HeatSpike('l', 0.85f), say("Burn-in: please do not stand on the logo.", "Einbrennen: Bitte nicht aufs Logo stellen.")),
                trap(Zone(15.5f, 7f, 17f, 9.2f), HeatSpike('m', 0.85f), say("The logo is also on the other side. Branding.", "Das Logo ist auch auf der anderen Seite. Markenauftritt.")),
                trap(Zone(22.4f, 13.5f, 23.4f, 15.2f), HeatSpike('b', 1f), say("Last pixel before the door. Also mine.", "Letztes Pixel vor der Tür. Auch meins.")),
                trap(Zone(17.3f, 11.6f, 22.6f, 13.7f), Show('T'), say("Surprise! The test pattern has a second layer.", "Überraschung! Das Testbild hat eine zweite Ebene.")),
                trap(Landed(25f, 30f), Hide('S'), say("Test pattern: spikes. Just kidding. Mostly.", "Testbild: Stacheln. War ein Scherz. Fast."), delay = 0.4f),
            ),
            hint = T("The spikes in the way are only a test pattern: let them sink. Do not hop them.", "Die Stacheln im Weg sind nur ein Testbild: lass sie versinken. Spring nicht drüber."),
            rematch = listOf(
                Round(
                    T("The test pattern is real this time. Probably.", "Diesmal ist das Testbild echt. Wahrscheinlich."),
                    start = listOf(Heat('f', rise = 0.8f, cool = 6f), Heat('l', rise = 0.8f, cool = 6f), Heat('b', rise = 0.8f, cool = 6f)),
                    hint = T("The spikes stay. Hop them, and watch the shelf: it has a second layer now.", "Die Stacheln bleiben. Spring drüber, und achte aufs Regal: Das hat jetzt eine zweite Ebene."),
                    traps = listOf(
                        trap(Landed(2.5f, 5.5f), HeatSpike('f', 0.4f), say("Round two: every step is a pixel again.", "Runde zwei: Jede Stufe ist wieder ein Pixel.")),
                        trap(Zone(8.5f, 7f, 10.2f, 9.2f), Play(Card.THROTTLE), Heat('l', rise = 0.8f, cool = 6f), HeatSpike('l', 0.85f), say("Refresh rate: lowered. Temperature: not.", "Bildwiederholrate: gesenkt. Temperatur: nicht.")),
                        trap(Zone(15f, 7f, 16.4f, 9.2f), Show('U'), say("The shelf has a second layer, and it is sharp.", "Das Regal hat eine zweite Ebene, und die ist spitz."), delay = 0.3f),
                        trap(Zone(23.8f, 13.5f, 24.8f, 15.2f), HeatSpike('b', 1f), say("Last pixel before the door. You know the drill.", "Letztes Pixel vor der Tür. Du kennst das schon.")),
                    ),
                ) {
                    fill(18..20, 9..9, '#')
                    fill(20..20, 8..8, 'U')
                    fill(11..13, 15..15, '#'); fill(19..20, 15..15, 'b')
                    fill(19..20, 14..14, '.'); fill(11..11, 14..14, 'S')
                },
            ),
        ) {
            border(); floor()
            fill(3..4, 13..14); fill(5..6, 11..14); fill(7..24, 9..10)
            fill(3..4, 13..13, 'f'); fill(5..6, 11..11, 'f'); fill(11..13, 9..9, 'l'); fill(18..20, 9..9, 'm')
            fill(19..20, 14..14, 'S'); fill(16..17, 14..14, 'T')
            fill(11..13, 15..15, 'b')
            spawn(2, 14); door(8, 14)
        },

        // 31 — a puzzle room (R11 cool the chips, R5 two floors, U3 the crane walks with you). The floor is a chip under load that heats all the time;
        // the heatsink bench in front of it cools it only while you stand on it, and the moment you touch the bench the crane above it starts to walk
        // after you and comes down on whoever is still there a moment later (stand just long enough, then cross). Along the chip, up a step: a crane
        // comes down on the step as you land (keep going), back left onto a shelf, over a block that is the second heatsink (a second chip, a second crane)
        // and up two steps to the door.
        Level(
            name = T("Pit Stop", "Boxenstopp"),
            intro = T("Grab a heatsink. They're all trustworthy.", "Nimm ruhig einen Kühlkörper. Alle vertrauenswürdig."),
            start = listOf(
                Heat('c', rise = 1.3f, load = true), Heatsink('k', cools = "c"),
                Heat('d', rise = 1.0f, load = true), Heatsink('l', cools = "d"),
            ),
            traps = listOf(
                trap(Touch('k'), Play(Card.STALKER), Chase('s', 6f, left = 4f, right = 16f), HeatSpike('c', 1f), say("The crane is a stalker. Not a very good one.", "Der Kran ist ein Verfolger. Kein besonders guter.")),
                trap(Touch('k'), Move('s', 0f, 12.6f, 22f), delay = 0.9f),
                trap(Zone(12.6f, 13f, 13.6f, 15.2f), Move('y', 0f, 12.6f, 30f), say("Pit lane rule one: do not stand in the pit lane.", "Boxengassen-Regel eins: nicht in der Boxengasse stehen.")),
                trap(Landed(26f, 31f), Move('u', 0f, 11f, 22f), say("Pit crew: tyres changed, ceiling lowered.", "Boxencrew: Reifen gewechselt, Decke gesenkt."), delay = 0.9f),
                trap(Touch('l'), Move('t', 0f, 7f, 22f), say("Second heatsink. Second crane. Same warranty.", "Zweiter Kühlkörper. Zweiter Kran. Gleiche Garantie."), delay = 0.8f),
            ),
            hint = T("The heatsink cools the chip only while you stand on it. Stay just long enough: the crane follows you.", "Der Kühlkörper kühlt den Chip nur, solange du draufstehst. Bleib nur kurz: Der Kran folgt dir."),
        ) {
            border(); floor()
            fill(9..10, 15..15, 'k'); fill(12..20, 15..15, 'c')
            fill(14..24, 11..12); fill(19..22, 9..10); fill(19..22, 9..9, 'l'); fill(16..18, 11..11, 'd')
            fill(14..15, 9..10); fill(10..13, 7..8)
            fill(27..30, 13..14)
            fill(9..11, 1..1, 's'); fill(19..22, 1..1, 't'); fill(27..30, 1..1, 'u'); fill(12..13, 1..1, 'y')
            spawn(1, 14); door(10, 6)
        },

        // 32 — act finale (R11 cool the chip, R1 a pad; U17 heat, U2 the ceiling, U18 the room goes on, twice). Room one: a live wall of copper stands where the plateau
        // ends, and the way on is a chip floor. A staircase of three steps leads up to the plateau; the pad on its far edge cuts the wall, and the same press starts the
        // chip heating (a couple of seconds, then it is lethal): drop off the edge and run across at once. A slab hangs over the middle of the chip and falls on whoever
        // is still standing there. At the door the end turns out not to be one: the wall breaks open and the door slips into the next room. Room two: warm plates under
        // a ceiling that is the next to fall (they are the trigger: stop behind them, let the slab land, hop it), and at the door the wall breaks open again. Room three:
        // plates that are overclocked as you come (hop them), and the door.
        Level(
            name = T("Thermal Runaway", "Thermische Flucht"),
            intro = T("Act two finale. Everything is hot. Including me.", "Finale, Akt zwei. Alles ist heiß. Ich auch."),
            rooms = 3,
            start = listOf(
                Circuit('W'), Pad('1', at = 13 to 11, circuits = "W"),
                Heat('g', rise = 1.3f, cool = 6f), Heat('f', rise = 0.8f, cool = 6f),
            ),
            traps = listOf(
                trap(Pressed('1'), Heat('c', rise = 2f, load = true), say("Wall cut. Core on. Everything is fine. Run.", "Wand aus. Kern an. Alles in Ordnung. Lauf.")),
                trap(Zone(17.2f, 13f, 18.2f, 15.2f), Fall('r'), say("Insulation: on the house. Mind your head.", "Dämmung: aufs Haus. Kopf einziehen."), delay = 0.1f),
                trap(AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 29) to 14)),
                trap(Zone(roomX(1, 4.4f), 13f, roomX(1, 5.6f), 15.2f), Fall('q'), say("The plates are warm. The ceiling noticed.", "Die Platten sind warm. Die Decke hat es gemerkt."), delay = 0.75f),
                trap(AtDoor, Extend(into = 2, door = roomX(2, 29) to 14, line = T("Did you think the reactor was one room?", "Dachtest du, der Reaktor ist ein Raum?"))),
                trap(PastX(roomX(2, 7f)), HeatSpike('f', 0.85f), say("Core temperature: yes.", "Kerntemperatur: ja.")),
                trap(PastX(roomX(2, 25f)), say("The exit. Finally. It is warm, too.", "Der Ausgang. Endlich. Auch der ist warm.")),
            ),
            hint = T("The pad on the plateau cuts the wall and starts the chip: run across right after it. Beyond each door there is more room.", "Der Knopf auf dem Plateau schaltet die Wand ab und startet den Chip: lauf gleich danach drüber. Hinter jeder Tür ist noch Platz."),
            rematch = listOf(
                Round(
                    T("Emergency shutdown. The roof is the hot part now.", "Notabschaltung. Das Dach ist jetzt der heiße Teil."),
                    hint = T("The roof is the chip now. Go underneath, cool down on the heatsink and press the pad. Do not wait where the ceiling falls.", "Das Dach ist jetzt der Chip. Geh darunter durch, kühl dich auf dem Kühlkörper ab und drück den Knopf. Warte nicht, wo die Decke fällt."),
                    start = listOf(
                        Circuit('W'), Pad('1', at = 11 to 14, circuits = "W"),
                        Heat('c', rise = 1.6f, load = true), Heatsink('k', cools = "c"), Heat('p', rise = 1.1f, load = true),
                        Heat('g', rise = 1.3f, cool = 6f), Heat('f', rise = 0.8f, cool = 6f), Heat('e', rise = 0.8f, cool = 6f),
                    ),
                    traps = listOf(
                        trap(Zone(6.6f, 13f, 7.6f, 15.2f), HeatSpike('c', 1f), say("The chip is at 100 per cent. Cooling is at your discretion.", "Der Chip ist bei 100 Prozent. Kühlen liegt in deinem Ermessen.")),
                        trap(Zone(17.2f, 13f, 18.2f, 15.2f), Fall('r'), say("Same insulation. Different mood.", "Gleiche Dämmung. Andere Laune."), delay = 0.1f),
                        trap(AtDoor, Play(Card.GRAND_FINALE), Extend(into = 1, warn = 1.0f, door = roomX(1, 29) to 14, line = T("Everything at once. The room included.", "Alles auf einmal. Der Raum inklusive."))),
                        trap(PastX(roomX(1, 3.2f)), HeatSpike('g', 0.85f), say("The plates remember being stood on. They are hot about it.", "Die Platten erinnern sich ans Draufstehen. Sie sind sauer.")),
                        trap(Zone(roomX(1, 11.2f), 13f, roomX(1, 12.2f), 15.2f), Fall('q'), say("Do not stop here. The ceiling remembers.", "Bleib hier nicht stehen. Die Decke merkt sich das."), delay = 0.75f),
                        trap(AtDoor, Extend(into = 2, door = roomX(2, 29) to 14, line = T("Shutdown cancelled. The reactor wants an encore.", "Abschaltung abgebrochen. Der Reaktor will eine Zugabe."))),
                        trap(PastX(roomX(2, 7f)), HeatSpike('f', 0.85f), say("Residual heat: a figure of speech.", "Restwärme: eine Redewendung.")),
                        trap(PastX(roomX(2, 15.5f)), HeatSpike('e', 0.85f), say("And residual heat, part two.", "Und Restwärme, Teil zwei.")),
                        trap(PastX(roomX(2, 25f)), say("Door ahead. Please do not touch anything hot. Anything.", "Tür voraus. Bitte nichts Heißes anfassen. Gar nichts.")),
                    ),
                ) {
                    room(0) { fill(21..22, 15..15, '#'); fill(4..5, 14..14, '.'); fill(6..7, 13..14, '.'); fill(8..13, 13..14, '.'); fill(8..13, 12..12, 'p'); fill(8..9, 15..15, 'k') }
                    room(1) { fill(5..5, 15..15, '#'); fill(9..9, 15..15, '#'); fill(14..17, 1..1, '.'); fill(10..13, 1..1, 'q') }
                    room(2) { fill(18..20, 15..15, 'e') }
                },
            ),
        ) {
            border(); floor()
            room(0) {
                fill(4..5, 14..14); fill(6..7, 13..14); fill(8..13, 12..14)
                fill(14..14, 1..14, 'W')
                fill(15..22, 15..15, 'c')
                fill(17..19, 1..1, 'r')
                spawn(1, 14); door(29, 14)
            }
            room(1) {
                fill(5..9, 15..15, 'g'); fill(14..17, 1..1, 'q')
            }
            room(2) {
                fill(10..12, 15..15, 'f')
            }
        },

    )
}
