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

        // 28 — rest on the heatsink, and a fan blade rolls in along the floor; wait there for it and a second, faster blade comes
        // from behind; the plates behind the first blade warm up as you land
        Level(
            name = T("Break Time", "Pause"),
            intro = T("Take a seat on the heatsink. Relax.", "Setz dich auf den Kühlkörper. Entspann dich."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(PastX(16.5f), Play(Card.DEVIL_SAW), Saw(33f, 14.4f, -7f, 0f), say("Fan blade! It's rotating. On the floor.", "Lüfterblatt! Es dreht sich. Am Boden.")),
                trap(Touch('k'), Saw(-2f, 14.4f, 10f, 0f), say("Fans have more than one blade.", "Lüfter haben mehr als ein Blatt.")),
                trap(Landed(18f, 27f), HeatSpike('h', 0.6f), say("Sorry, the landing strip is warm.", "Sorry, die Landebahn ist warm.")),
            ),
            // rematch: the blade from the front now flies at head height: the hop that cleared it in round 1 runs into it,
            // walking passes under it (and the one from behind still punishes a long rest)
            rematch = listOf(
                Round(
                    T("Break's over. Second shift.", "Pause vorbei. Spätschicht."),
                    start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h")),
                    traps = listOf(
                        trap(PastX(16.5f), Play(Card.DEVIL_SAW), Saw(33f, 12.9f, -7f, 0f), say("Fan blade, mounted higher. Ergonomics.", "Lüfterblatt, höher montiert. Ergonomie.")),
                        trap(Touch('k'), Saw(-2f, 14.4f, 10f, 0f), say("The rear one is floor-mounted.", "Das hintere ist bodennah montiert.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(7..15, 15..15, 'h'); put(16, 15, 'k'); fill(17..25, 15..15, 'h')
            spawn(); door()
        },

        // 29 — the wall opens when the plate reaches 55 degrees: sit on it, then run; the floor behind the wall is overclocked,
        // and running on from the warm landing finds the floor before the door gone
        Level(
            name = T("Thermostat", "Thermostat"),
            intro = T("The wall has a thermostat. Somewhere.", "Die Wand hat ein Thermostat. Irgendwo."),
            start = listOf(Heat('h', rise = 2.4f)),
            traps = listOf(
                trap(Heated('h', 0.55f), Play(Card.GHOST_BLOCK), Hide('w'), say("Thermostat reached. Wall removed. Nothing suspicious.", "Thermostat erreicht. Wand entfernt. Nichts Verdächtiges.")),
                trap(PastX(17f), HeatSpike('g', 0.7f), say("Nothing suspicious. Except the floor.", "Nichts Verdächtiges. Außer dem Boden.")),
                trap(Landed(19f, 25f), Fall('x'), say("Solder joint: cold. Floor: gone.", "Lötstelle: kalt. Boden: weg.")),
            ),
        ) {
            border(); floor()
            fill(8..12, 15..15, 'h')
            wire(16, 'w')
            fill(20..23, 15..15, 'g')
            fill(26..27, 15..17, 'x')
            spawn(); door()
        },

        // 30 — burn-in test: the gate changes rhythm, the floor right before it burns in under whoever waits there, the door is
        // a fake, the floor around it is overclocked and the real door ran off
        Level(
            name = T("Burn-in Test", "Einbrenntest"),
            intro = T("Stress test: walk to the door. Easy.", "Stresstest: lauf zur Tür. Ganz leicht."),
            start = listOf(Clock('Z', on = 1.3f, off = 1.7f)),
            traps = listOf(
                trap(PastX(5.2f), Clock('Z', on = 0.9f, off = 1.5f), say("Test profile 2: shorter gaps.", "Testprofil 2: kürzere Pausen.")),
                trap(PastX(7.2f), HeatSpike('e', 0.6f), say("Test profile 3: the waiting area burns in.", "Testprofil 3: der Wartebereich brennt ein.")),
                trap(AtDoor, Play(Card.DECOY), FakeWin(FakeEnd.CLEAR, null, HeatSpike('f', 0.55f), DoorTo(17, 14), say("Stress test passed! ...Just kidding. Run.", "Stresstest bestanden! ...Scherz. Lauf."))),
            ),
            // rematch: the gate floor is cool now, the spot a step back (where round 1 was safe) burns in
            rematch = listOf(
                Round(
                    T("Burn-in, pass two. Different pixels.", "Einbrenntest, zweiter Lauf. Bildschirmschoner aus."),
                    start = listOf(Clock('Z', on = 1.3f, off = 1.7f)),
                    traps = listOf(
                        trap(PastX(5.2f), Clock('Z', on = 0.9f, off = 1.5f), say("Test profile 2 again.", "Wieder Testprofil 2.")),
                        trap(PastX(5.4f), HeatSpike('d', 0.6f), say("Test profile 3b: the back row burns in.", "Testprofil 3b: die hintere Reihe brennt ein.")),
                        trap(AtDoor, Play(Card.DECOY), FakeWin(FakeEnd.CLEAR, null, HeatSpike('f', 0.55f), DoorTo(17, 14), say("Passed! ...You know the drill.", "Bestanden! ...Du kennst das schon."))),
                    ),
                ) { fill(5..6, 15..15, 'd') },
            ),
        ) {
            border(); floor()
            fill(7..9, 15..15, 'e')
            wire(10, 'Z')
            fill(23..30, 15..15, 'f')
            spawn(); door()
        },

        // 31 — the second heatsink is hotter than advertised: hop over it; the landing plate and the end are warm too
        Level(
            name = T("Pit Stop", "Boxenstopp"),
            intro = T("Grab a heatsink. They're all trustworthy.", "Nimm ruhig einen Kühlkörper. Alle vertrauenswürdig."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h"), Heatsink('l', cools = "h")),
            traps = listOf(
                trap(Touch('l'), Play(Card.SINKING), HeatSpike('l', 1f), say("Heatsink 2 runs hotter than advertised.", "Kühlkörper 2 ist heißer als beworben.")),
                trap(Landed(21f, 25.5f), HeatSpike('h', 0.6f), say("Pit stop tires: preheated.", "Boxenstopp-Reifen: vorgeheizt.")),
                trap(PastX(26.2f), HeatSpike('f', 0.9f), say("Pit lane exit: hot.", "Boxenausfahrt: heiß.")),
            ),
        ) {
            border(); floor()
            fill(6..14, 15..15, 'h'); put(15, 15, 'k'); fill(16..19, 15..15, 'h'); put(20, 15, 'l'); fill(21..25, 15..15, 'h')
            fill(27..28, 15..15, 'f')
            spawn(); door()
        },

        // 32 — act finale: cool the chip, cut the live wall (it only flickers), hop the last overclocked tiles
        Level(
            name = T("Thermal Runaway", "Thermische Flucht"),
            intro = T("Act two finale. Everything is hot. Including me.", "Finale, Akt zwei. Alles ist heiß. Ich auch."),
            start = listOf(
                Heat('c', rise = 2.2f, load = true), Heatsink('k', cools = "c"),
                Circuit('Z'), Pad('1', at = 23 to 14, circuits = "Z", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(PastX(6.5f), Heat('c', rise = 1.9f, load = true), say("Core voltage: raised.", "Kernspannung: erhöht.")),
                trap(Pressed('1'), Clock('Z', on = 0.7f, off = 2.2f, phase = 0.7f), say("The breaker was a clock all along.", "Die Sicherung war die ganze Zeit ein Taktgeber.")),
                trap(PastX(25.8f), Play(Card.GRAND_FINALE), HeatSpike('f', 1f), say("Core temperature: yes.", "Kerntemperatur: ja.")),
            ),
            // rematch: the breaker now switches the wall ON; hop the button (or press it twice)
            rematch = listOf(
                Round(
                    T("Thermal reset. Everything is still hot.", "Thermischer Reset. Lüfter weiterhin optional."),
                    start = listOf(
                        Heat('c', rise = 2.2f, load = true), Heatsink('k', cools = "c"),
                        Circuit('Z', on = false), Pad('1', at = 23 to 14, circuits = "Z"),
                    ),
                    traps = listOf(
                        trap(PastX(6.5f), Heat('c', rise = 1.9f, load = true), say("Core voltage: raised. Tradition.", "Kernspannung: erhöht. Tradition.")),
                        trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Breaker firmware 2.0: ON means ON.", "Sicherungs-Firmware 2.0: AN heißt AN.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(4..5, 15..15, 'k')
            bridge(8..20, 'c')
            wire(26, 'Z')
            fill(27..28, 15..15, 'f')
            spawn(); door()
        },
    )
}
