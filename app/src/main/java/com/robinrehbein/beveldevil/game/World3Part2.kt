package com.robinrehbein.beveldevil.game

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
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h")),
        ) {
            border(); floor()
            fill(6..14, 15..15, 'h'); put(15, 15, 'k'); fill(16..24, 15..15, 'h')
            spawn(); door()
        },

        // 18 — a chip under load heats all the time: climb onto the heatsink first, cool it, then cross at full speed
        Level(
            name = T("Full Load", "Volllast"),
            intro = T("Chips get hot when they think. This one never stops.", "Chips werden heiß, wenn sie denken. Der hier hört nie auf."),
            start = listOf(Heat('c', rise = 2.2f, load = true), Heatsink('k', cools = "c")),
        ) {
            border(); floor()
            fill(5..6, 13..14, 'k')
            bridge(9..22, 'c')
            spawn(); door()
        },

        // 19 — melting stones: each one is gone for good if you stand on it too long
        Level(
            name = T("Melt Fuse", "Schmelzsicherung"),
            intro = T("Lead-free solder. Also free of mercy.", "Bleifreies Lot. Und gnadenfrei."),
            start = listOf(Heat('m', rise = 0.9f, melt = true), Heat('n', rise = 0.9f, melt = true), Heat('o', rise = 0.9f, melt = true)),
        ) {
            border(); floor(); pit(7..25)
            fill(8..11, 15..15, 'm'); fill(14..17, 15..15, 'n'); fill(20..23, 15..15, 'o')
            spawn(); door()
        },

        // 20 — the plain floor is overclocked when you step on it: never stop
        Level(
            name = T("Overclocked", "Übertaktet"),
            intro = T("Nice and cool here. Take your time.", "Schön kühl hier. Lass dir Zeit."),
            traps = listOf(
                trap(PastX(13.2f), Play(Card.CRUMBLE), HeatSpike('f', 0.7f), say("Overclocked! Factory settings: mine.", "Übertaktet! Werkseinstellung: meine.")),
            ),
        ) {
            border(); floor()
            fill(13..19, 15..15, 'f')
            spawn(); door()
        },

        // 21 — three plates that share their heat and two sinks between them
        Level(
            name = T("Relay Race", "Staffellauf"),
            intro = T("Three plates, two heatsinks. Do the math.", "Drei Platten, zwei Kühlkörper. Rechne nach."),
            start = listOf(Heat('h', rise = 1f), Heatsink('k', cools = "h")),
        ) {
            border(); floor()
            fill(5..10, 15..15, 'h'); put(11, 15, 'k'); fill(12..17, 15..15, 'h'); put(18, 15, 'k'); fill(19..25, 15..15, 'h')
            spawn(); door()
        },

        // 22 — the chip warms up, and when it does, spikes grow on the far side
        Level(
            name = T("Heat Soak", "Hitzestau"),
            intro = T("A warm chip is a happy chip.", "Ein warmer Chip ist ein glücklicher Chip."),
            legend = mapOf('A' to hidden),
            start = listOf(Heat('c', rise = 4f, load = true)),
            traps = listOf(
                trap(Heated('c', 0.5f), Play(Card.SPIKE_SEED), Show('A'), say("Thermal throttling: the spikes run at full speed.", "Thermische Drosselung: die Spikes laufen auf Vollgas.")),
            ),
        ) {
            border(); floor()
            bridge(9..20, 'c')
            put(25, 14, 'A'); put(26, 14, 'A')
            spawn(); door()
        },

        // 23 — the plate is lukewarm until you step on it; then Mephi turns it up, so waiting for the rail is not an option
        Level(
            name = T("Turn Up the Heat", "Heizung aufgedreht"),
            intro = T("Take a seat. The plate is lukewarm.", "Setz dich ruhig. Die Platte ist lauwarm."),
            start = listOf(Heat('h', rise = 3f), Clock('a', on = 2.4f, off = 2f, phase = 2f)),
            traps = listOf(
                trap(PastX(8.3f), Play(Card.SINKING), Heat('h', rise = 0.8f), say("Set to Sauna. Sitting not recommended.", "Stufe Sauna. Sitzen nicht empfohlen.")),
            ),
        ) {
            border(); floor()
            fill(8..11, 15..15, 'h')
            bridge(12..22, 'a')
            spawn(); door()
        },

        // 24 — cooling fins: hot plates as a staircase up to the door
        Level(
            name = T("Cooling Fins", "Kühlrippen"),
            intro = T("Climb the fins. They only bite when you linger.", "Klettere die Rippen hoch. Sie beißen nur, wenn du trödelst."),
            start = listOf(Heat('h', rise = 0.8f)),
        ) {
            border(); floor()
            fill(5..8, 13..13, 'h'); fill(11..14, 11..11, 'h'); fill(17..20, 9..9, 'h'); fill(23..26, 7..7, 'h')
            fill(28..30, 5..5)
            spawn(); door(29, 4)
        },

        // 25 — a hot floor and one cool spot, two tiles up
        Level(
            name = T("Cooling Tower", "Kühlturm"),
            intro = T("The only cool spot is upstairs.", "Der einzige kühle Ort liegt oben."),
            start = listOf(Heat('h', rise = 1.4f), Heatsink('k', cools = "h")),
        ) {
            border(); floor()
            fill(4..24, 15..15, 'h')
            fill(13..15, 13..14, 'k')
            spawn(); door()
        },

        // 26 — a flickering live gate over a hot plate: wait on the sink for the gate, not on the plate
        Level(
            name = T("Hot Wire", "Heißer Draht"),
            intro = T("Hot plate, live wire. Pick your favorite.", "Heiße Platte, Draht unter Strom. Such dir was aus."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h"), Clock('Z', on = 1.2f, off = 2f)),
        ) {
            border(); floor()
            fill(6..12, 15..15, 'h'); put(13, 15, 'k'); fill(14..22, 15..15, 'h')
            wire(17, 'Z')
            spawn(); door()
        },

        // 27 — a pad powers the bridge; the floor behind it is overclocked when you come near
        Level(
            name = T("Overvoltage", "Überspannung"),
            intro = T("Surge protector: installed. Not working.", "Überspannungsschutz: eingebaut. Funktioniert nicht."),
            start = listOf(Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON)),
            traps = listOf(
                trap(PastX(16.3f), Play(Card.CRUMBLE), HeatSpike('f', 0.75f), say("Overvoltage! It was a feature request.", "Überspannung! Das war ein Feature-Wunsch.")),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            fill(16..22, 15..15, 'f')
            spawn(); door()
        },

        // 28 — rest on the heatsink, and a fan blade rolls in along the floor
        Level(
            name = T("Hot Seat", "Heißer Stuhl"),
            intro = T("Take a seat on the heatsink. Relax.", "Setz dich auf den Kühlkörper. Entspann dich."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(PastX(16.5f), Play(Card.DEVIL_SAW), Saw(33f, 14.4f, -7f, 0f), say("Fan blade! It's rotating. On the floor.", "Lüfterblatt! Es dreht sich. Am Boden.")),
            ),
        ) {
            border(); floor()
            fill(7..15, 15..15, 'h'); put(16, 15, 'k'); fill(17..25, 15..15, 'h')
            spawn(); door()
        },

        // 29 — the wall opens when the plate reaches 55 degrees: sit on it, then run
        Level(
            name = T("Thermostat", "Thermostat"),
            intro = T("Door opens at 55 degrees. Sit on the plate.", "Tür öffnet bei 55 Grad. Setz dich auf die Platte."),
            start = listOf(Heat('h', rise = 2.4f)),
            traps = listOf(
                trap(Heated('h', 0.55f), Play(Card.GHOST_BLOCK), Hide('w'), say("Thermostat reached. Wall removed. Nothing suspicious.", "Thermostat erreicht. Wand entfernt. Nichts Verdächtiges.")),
            ),
        ) {
            border(); floor()
            fill(8..12, 15..15, 'h')
            wire(16, 'w')
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
            name = T("Cooling Failure", "Kühlung defekt"),
            intro = T("Grab a heatsink. They're all trustworthy.", "Nimm ruhig einen Kühlkörper. Alle vertrauenswürdig."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h"), Heatsink('l', cools = "h")),
            traps = listOf(
                trap(Touch('l'), Play(Card.SINKING), HeatSpike('l', 1f), say("Heatsink 2 runs hotter than advertised.", "Kühlkörper 2 ist heißer als beworben.")),
            ),
        ) {
            border(); floor()
            fill(6..14, 15..15, 'h'); put(15, 15, 'k'); fill(16..19, 15..15, 'h'); put(20, 15, 'l'); fill(21..25, 15..15, 'h')
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
