package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Heatsink
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.Heated
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch

/**
 * World 3, levels 17-32. Act 2, "Überhitzung": hot plates, chips under load, heatsinks, the overclocked floor and
 * melting plates, then combined with the circuits of act 1. The green board.
 */
object World3Part2 {
    private val hidden = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(

        // 17 — a plate too long to cross in one go, with a heatsink in the middle: stop on it and let the plate cool
        Level(
            name = T("Hot Plate", "Herdplatte"),
            intro = T("It's just a stove. A very long one.", "Ist nur ein Herd. Ein sehr langer."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(PastX(4.4f), Show('B'), say("Warm-up round, on the house.", "Aufwärmrunde, aufs Haus.")),
                trap(PastX(25.3f), Play(Card.SPIKE_SEED), Show('A'), say("Cooled down? Good. Now cool off these spikes.", "Abgekühlt? Gut. Dann kühl dich an diesen Spikes.")),
            ),
        ) {
            border(); floor()
            put(5, 14, 'B')
            fill(6..14, 15..15, 'h'); put(15, 15, 'k'); fill(16..24, 15..15, 'h')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 18 — a chip under load heats all the time: climb onto the heatsink first, cool it, then cross at full speed
        Level(
            name = T("Full Load", "Volllast"),
            intro = T("Chips get hot when they think. This one never stops.", "Chips werden heiß, wenn sie denken. Der hier hört nie auf."),
            start = listOf(Heat('c', rise = 2.2f, load = true), Heatsink('k', cools = "c")),
            traps = listOf(
                trap(PastX(24.6f), Play(Card.CRUMBLE), HeatSpike('f', 0.8f), say("Bonus round: the floor is lava. Mildly.", "Bonusrunde: Der Boden ist Lava. Mild.")),
            ),
        ) {
            border(); floor()
            fill(5..6, 13..14, 'k')
            bridge(9..22, 'c')
            fill(25..27, 15..15, 'f')
            spawn(); door()
        },

        // 19 — melting stones: each one is gone for good if you stand on it too long
        Level(
            name = T("Melt Fuse", "Schmelzsicherung"),
            intro = T("Lead-free solder. Also free of mercy.", "Bleifreies Lot. Und gnadenfrei."),
            start = listOf(Heat('m', rise = 0.9f, melt = true), Heat('n', rise = 0.9f, melt = true), Heat('o', rise = 0.9f, melt = true)),
            traps = listOf(
                trap(Touch('n'), Play(Card.GHOST_BLOCK), HeatSpike('o', 0.85f), say("The last stone was already warm. Sorry.", "Der letzte Stein war schon warm. Sorry.")),
            ),
        ) {
            border(); floor(); pit(7..25)
            fill(8..11, 15..15, 'm'); fill(14..17, 15..15, 'n'); fill(20..23, 15..15, 'o')
            spawn(); door()
        },

        // 20 — the plain floor is overclocked when you step on it: never stop
        Level(
            name = T("Cold Start", "Kaltstart"),
            intro = T("Nice and cool here. Take your time.", "Schön kühl hier. Lass dir Zeit."),
            traps = listOf(
                trap(PastX(13.2f), Play(Card.CRUMBLE), HeatSpike('f', 0.7f), say("Overclocked! Factory settings: mine.", "Übertaktet! Werkseinstellung: meine.")),
                trap(PastX(22.7f), HeatSpike('g', 0.75f), say("Twice! It's a feature.", "Nochmal! Ist ein Feature.")),
            ),
        ) {
            border(); floor()
            fill(13..19, 15..15, 'f'); fill(23..25, 15..15, 'g')
            spawn(); door()
        },

        // 21 — three plates that share their heat and two sinks between them
        Level(
            name = T("Relay Race", "Staffellauf"),
            intro = T("Three plates, two heatsinks. Do the math.", "Drei Platten, zwei Kühlkörper. Rechne nach."),
            legend = mapOf('A' to hidden),
            start = listOf(Heat('h', rise = 1f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(PastX(2.6f), HeatSpike('g', 0.8f), say("Starting blocks: preheated.", "Startblöcke: vorgeheizt.")),
                trap(PastX(26.2f), Play(Card.SPIKE_SEED), Show('A'), say("Plate 4 is a spike plate. I counted.", "Platte 4 ist eine Spike-Platte. Ich hab mitgezählt.")),
            ),
        ) {
            border(); floor()
            fill(3..4, 15..15, 'g')
            fill(5..10, 15..15, 'h'); put(11, 15, 'k'); fill(12..17, 15..15, 'h'); put(18, 15, 'k'); fill(19..25, 15..15, 'h')
            put(27, 14, 'A'); put(28, 14, 'A')
            spawn(); door()
        },

        // 22 — the chip warms up, and when it does, spikes grow on the far side
        Level(
            name = T("Warm-up", "Warmlaufen"),
            intro = T("A warm chip is a happy chip.", "Ein warmer Chip ist ein glücklicher Chip."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            start = listOf(Heat('c', rise = 4f, load = true)),
            traps = listOf(
                trap(PastX(4.5f), Show('B'), say("Welcome. Mind the threshold.", "Willkommen. Vorsicht, Schwelle.")),
                trap(Heated('c', 0.5f), Play(Card.SPIKE_SEED), Show('A'), say("Thermal throttling: the spikes run at full speed.", "Thermische Drosselung: die Spikes laufen auf Vollgas.")),
            ),
        ) {
            border(); floor()
            bridge(9..20, 'c')
            put(25, 14, 'A'); put(26, 14, 'A'); put(5, 14, 'B')
            spawn(); door()
        },

        // 23 — the plate is lukewarm until you step on it; then Mephi turns it up, so waiting for the rail is not an option
        Level(
            name = T("Waiting Room", "Wartezimmer"),
            intro = T("Take a seat. The plate is lukewarm.", "Setz dich ruhig. Die Platte ist lauwarm."),
            legend = mapOf('A' to hidden),
            start = listOf(Heat('h', rise = 3f), Clock('a', on = 2.4f, off = 2f, phase = 2f)),
            traps = listOf(
                trap(PastX(8.3f), Play(Card.SINKING), Heat('h', rise = 0.8f), say("Set to Sauna. Sitting not recommended.", "Stufe Sauna. Sitzen nicht empfohlen.")),
                trap(PastX(25.2f), Show('A'), say("Next appointment: spikes.", "Nächster Termin: Spikes.")),
            ),
        ) {
            border(); floor()
            fill(8..11, 15..15, 'h')
            bridge(12..22, 'a')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 24 — cooling fins: hot plates as a staircase up to the door
        Level(
            name = T("Cooling Fins", "Kühlrippen"),
            intro = T("Nice view from up there.", "Schöne Aussicht von da oben."),
            legend = mapOf('A' to hidden),
            start = listOf(Heat('h', rise = 0.8f)),
            traps = listOf(
                trap(PastX(3.4f), Play(Card.SPIKE_SEED), Show('A'), say("Watch your step. Literally this time.", "Pass auf, wo du hintrittst. Diesmal wirklich.")),
            ),
        ) {
            border(); floor()
            put(6, 14, 'A')
            fill(5..8, 13..13, 'h'); fill(11..14, 11..11, 'h'); fill(17..20, 9..9, 'h'); fill(23..26, 7..7, 'h')
            fill(28..30, 5..5)
            spawn(); door(29, 4)
        },

        // 25 — a hot floor and one cool spot, two tiles up
        Level(
            name = T("Cooling Tower", "Kühlturm"),
            intro = T("The only cool spot is upstairs.", "Der einzige kühle Ort liegt oben."),
            legend = mapOf('A' to hidden),
            start = listOf(Heat('h', rise = 1.4f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(Touch('x'), Fall('x'), say("Entrance hall: load-bearing? No.", "Eingangshalle: tragend? Nein."), delay = 0.1f),
                trap(PastX(25.2f), Play(Card.GHOST_BLOCK), Show('A'), say("Upstairs was cool. Downstairs is spiky.", "Oben war es kühl. Unten ist es spitz.")),
            ),
        ) {
            border(); floor()
            fill(4..5, 15..17, 'x')
            fill(4..24, 15..15, 'h')
            fill(13..15, 13..14, 'k')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 26 — a flickering live gate over a hot plate: wait on the sink for the gate, not on the plate
        Level(
            name = T("Hot Wire", "Heißer Draht"),
            intro = T("Hot plate, live wire. Pick your favorite.", "Heiße Platte, Draht unter Strom. Such dir was aus."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h"), Clock('Z', on = 1.2f, off = 2f)),
            traps = listOf(
                trap(PastX(3.2f), Show('B'), say("Even the doormat bites.", "Sogar die Fußmatte beißt.")),
                trap(PastX(24.2f), Play(Card.SPIKE_SEED), Show('A'), say("Gate passed. Toll booth ahead.", "Tor passiert. Mautstelle voraus.")),
            ),
        ) {
            border(); floor()
            put(4, 14, 'B')
            fill(6..12, 15..15, 'h'); put(13, 15, 'k'); fill(14..22, 15..15, 'h')
            wire(17, 'Z')
            put(25, 14, 'A'); put(26, 14, 'A')
            spawn(); door()
        },

        // 27 — a pad powers the bridge; the floor behind it is overclocked when you come near
        Level(
            name = T("Wall Socket", "Steckdose"),
            intro = T("Surge protector: installed.", "Überspannungsschutz: eingebaut."),
            start = listOf(Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON)),
            traps = listOf(
                trap(PastX(16.3f), Play(Card.CRUMBLE), HeatSpike('f', 0.75f), say("Overvoltage! It was a feature request.", "Überspannung! Das war ein Feature-Wunsch.")),
                trap(PastX(25.2f), Show('A'), say("Undervoltage: no refunds.", "Unterspannung: keine Rückerstattung.")),
            ),
            legend = mapOf('A' to hidden),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            fill(16..22, 15..15, 'f')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 28 — rest on the heatsink, and a fan blade rolls in along the floor
        Level(
            name = T("Break Time", "Pause"),
            intro = T("Take a seat on the heatsink. Relax.", "Setz dich auf den Kühlkörper. Entspann dich."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(PastX(16.5f), Play(Card.DEVIL_SAW), Saw(33f, 14.4f, -7f, 0f), say("Fan blade! It's rotating. On the floor.", "Lüfterblatt! Es dreht sich. Am Boden.")),
                trap(PastX(4.4f), Show('A'), say("Mind the gap. The spike gap.", "Vorsicht, Lücke. Die Spike-Lücke.")),
            ),
            legend = mapOf('A' to hidden),
        ) {
            border(); floor()
            put(5, 14, 'A')
            fill(7..15, 15..15, 'h'); put(16, 15, 'k'); fill(17..25, 15..15, 'h')
            spawn(); door()
        },

        // 29 — the wall opens when the plate reaches 55 degrees: sit on it, then run
        Level(
            name = T("Thermostat", "Thermostat"),
            intro = T("The wall has a thermostat. Somewhere.", "Die Wand hat ein Thermostat. Irgendwo."),
            start = listOf(Heat('h', rise = 2.4f)),
            traps = listOf(
                trap(Heated('h', 0.55f), Play(Card.GHOST_BLOCK), Hide('w'), say("Thermostat reached. Wall removed. Nothing suspicious.", "Thermostat erreicht. Wand entfernt. Nichts Verdächtiges.")),
                trap(PastX(18.3f), Show('A'), say("Nothing suspicious. Except these.", "Nichts Verdächtiges. Außer die hier.")),
            ),
            legend = mapOf('A' to hidden),
        ) {
            border(); floor()
            fill(8..12, 15..15, 'h')
            wire(16, 'w')
            put(19, 14, 'A'); put(20, 14, 'A')
            spawn(); door()
        },

        // 30 — burn-in test: the door is a fake, the floor around it is overclocked, and the real door ran off
        Level(
            name = T("Burn-in Test", "Einbrenntest"),
            intro = T("Stress test: walk to the door. Easy.", "Stresstest: lauf zur Tür. Ganz leicht."),
            start = listOf(Clock('Z', on = 1.3f, off = 1.7f)),
            traps = listOf(
                trap(AtDoor, Play(Card.DECOY), FakeWin(FakeEnd.CLEAR, null, HeatSpike('f', 0.55f), DoorTo(17, 14), say("Stress test passed! ...Just kidding. Run.", "Stresstest bestanden! ...Scherz. Lauf."))),
            ),
        ) {
            border(); floor()
            wire(10, 'Z')
            fill(23..30, 15..15, 'f')
            spawn(); door()
        },

        // 31 — the second heatsink is hotter than advertised: hop over it
        Level(
            name = T("Pit Stop", "Boxenstopp"),
            intro = T("Grab a heatsink. They're all trustworthy.", "Nimm ruhig einen Kühlkörper. Alle vertrauenswürdig."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h"), Heatsink('l', cools = "h")),
            traps = listOf(
                trap(Touch('l'), Play(Card.SINKING), HeatSpike('l', 1f), say("Heatsink 2 runs hotter than advertised.", "Kühlkörper 2 ist heißer als beworben.")),
                trap(PastX(25.2f), Show('A'), say("Pit stop over. Tires: spiked.", "Boxenstopp vorbei. Reifen: bespikt.")),
            ),
            legend = mapOf('A' to hidden),
        ) {
            border(); floor()
            fill(6..14, 15..15, 'h'); put(15, 15, 'k'); fill(16..19, 15..15, 'h'); put(20, 15, 'l'); fill(21..25, 15..15, 'h')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 32 — act finale: cool the chip, cut the live wall, hop the last overclocked tiles
        Level(
            name = T("Thermal Runaway", "Thermische Flucht"),
            intro = T("Act two finale. Everything is hot. Including me.", "Finale, Akt zwei. Alles ist heiß. Ich auch."),
            start = listOf(
                Heat('c', rise = 2.2f, load = true), Heatsink('k', cools = "c"),
                Circuit('Z'), Pad('1', at = 23 to 14, circuits = "Z", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(PastX(25.8f), Play(Card.GRAND_FINALE), HeatSpike('f', 1f), say("Core temperature: yes.", "Kerntemperatur: ja.")),
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
