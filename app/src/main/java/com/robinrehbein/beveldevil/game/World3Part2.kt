package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Heatsink
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.Heated
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch

/**
 * World 3, levels 17-32. Act 2, "Überhitzung": hot plates, chips under load, heatsinks, the overclocked floor and
 * melting plates, then combined with the circuits of act 1. The green board. The chains are all about temperature
 * going wrong at the wrong moment: a plate that heats faster once you trust it, a heatsink that warms up under you,
 * an overclocked landing, a gate that changes its rhythm, and one plate that glows but is perfectly cool.
 */
object World3Part2 {
    val levels: List<Level> = listOf(

        // 17 — a plate too long to cross in one go, with a heatsink in the middle; the second half heats faster once you touch the sink
        Level(
            name = T("Hot Plate", "Herdplatte"),
            intro = T("It's just a stove. A very long one.", "Ist nur ein Herd. Ein sehr langer."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(Touch('k'), Play(Card.THROTTLE), Heat('h', rise = 0.85f), say("Energy saving mode: off.", "Energiesparmodus: aus.")),
                trap(PastX(25.3f), HeatSpike('f', 0.9f), say("Cooled down? Good. Now warm up the tail.", "Abgekühlt? Gut. Dann wärm den Schluss auf.")),
            ),
        ) {
            border(); floor()
            fill(6..14, 15..15, 'h'); put(15, 15, 'k'); fill(16..21, 15..15, 'h')
            fill(27..28, 15..15, 'f')
            spawn(); door()
        },

        // 18 — a chip under load heats all the time: cool it on the heatsink first, but the sink warms up under you; once you
        // are down on the chip it runs hotter, so the last tiles are only safe in the air, and the landing after them is hot
        Level(
            name = T("Full Load", "Volllast"),
            intro = T("Chips get hot when they think. This one never stops.", "Chips werden heiß, wenn sie denken. Der hier hört nie auf."),
            start = listOf(Heat('c', rise = 2.2f, load = true), Heatsink('k', cools = "c")),
            traps = listOf(
                trap(Landed(4.8f, 7.2f), Play(Card.OVERCLOCKED), HeatSpike('k', 0.55f), say("The heatsink has a fever.", "Der Kühlkörper hat Fieber.")),
                trap(Landed(7f, 16f), Heat('c', rise = 1.6f, load = true), say("Turbo boost. For the chip, not for you.", "Turbo-Boost. Für den Chip, nicht für dich.")),
                trap(PastX(24.3f), HeatSpike('f', 1f), say("Bonus round: the landing is lava. Mildly.", "Bonusrunde: Die Landung ist Lava. Mild.")),
            ),
        ) {
            border(); floor()
            fill(5..6, 13..14, 'k')
            bridge(9..22, 'c')
            fill(26..28, 15..15, 'f')
            spawn(); door()
        },

        // 19 — melting stones: each one is gone for good if you stand on it too long; the next ones are already warm
        Level(
            name = T("Melt Fuse", "Schmelzsicherung"),
            intro = T("Lead-free solder. Also free of mercy.", "Bleifreies Lot. Und gnadenfrei."),
            start = listOf(Heat('m', rise = 0.9f, melt = true), Heat('n', rise = 0.9f, melt = true), Heat('o', rise = 0.9f, melt = true)),
            traps = listOf(
                trap(Touch('n'), Play(Card.GHOST_BLOCK), HeatSpike('o', 0.85f), say("The last stone was already warm. Sorry.", "Der letzte Stein war schon warm. Sorry.")),
                trap(Landed(14f, 18f), Heat('o', rise = 0.5f, melt = true), say("Solder with a lower melting point.", "Lot mit niedrigerem Schmelzpunkt.")),
                trap(Airborne(21.5f, 26.5f), HeatSpike('g', 0.7f), say("Dry land. Dry and warm.", "Festland. Trocken und warm.")),
            ),
        ) {
            border(); floor(); pit(7..25)
            fill(8..11, 15..15, 'm'); fill(14..17, 15..15, 'n'); fill(20..23, 15..15, 'o'); fill(26..27, 15..15, 'g')
            spawn(); door()
        },

        // 20 — the plate that glows is cool; the plain floor is what burns; as you land behind the last plate the door flies home
        // to the cool plate, and the floor on the way back is overclocked again: wait for it to cool
        Level(
            name = T("Cold Start", "Kaltstart"),
            intro = T("Nice and cool here. Take your time.", "Schön kühl hier. Lass dir Zeit."),
            start = listOf(Heat('h', rise = 14f, cool = 1f)),
            traps = listOf(
                trap(PastX(13.2f), Play(Card.OVERCLOCKED), HeatSpike('f', 0.7f), say("Overclocked! Factory settings: mine.", "Übertaktet! Werkseinstellung: meine.")),
                trap(Airborne(23.4f, 26f), HeatSpike('g', 0.75f), DoorTo(29, 12, speed = 20f), say("Twice! It's a feature.", "Nochmal! Ist ein Feature.")),
                // the door hovers within jumping reach until you land, then flies over your head to the glowing plate
                trap(
                    Landed(25.5f, 28.6f), DoorTo(7, 14, speed = 30f), HeatSpike('f', 1f), Heat('f', rise = 1.5f, cool = 3f),
                    say("The door prefers the cool plate. The way back is overclocked.", "Die Tür mag die kühle Platte. Der Rückweg ist übertaktet."),
                ),
            ),
        ) {
            border(); floor()
            fill(5..9, 15..15, 'h')
            fill(13..19, 15..15, 'f'); fill(23..25, 15..15, 'g')
            spawn(); door()
        },

        // 21 — three plates that share their heat and two sinks between them; the sinks make the plates nervous, the last leg
        // turns up as you leave the second sink (leap from the sink), and the floor after it is overclocked
        Level(
            name = T("Relay Race", "Staffellauf"),
            intro = T("Three plates, two heatsinks. Do the math.", "Drei Platten, zwei Kühlkörper. Rechne nach."),
            start = listOf(Heat('h', rise = 1f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(Touch('k'), Play(Card.THROTTLE), Heat('h', rise = 0.85f, cool = 1.4f), say("Plates are warmed up. So to speak.", "Platten sind warmgelaufen. Sozusagen.")),
                trap(PastX(19.2f), Heat('h', rise = 0.55f, cool = 1.4f), say("Last leg: sprint mode.", "Letzte Etappe: Sprintmodus.")),
                trap(PastX(26.2f), HeatSpike('f', 0.9f), say("Plate 4 is a plain floor. I counted.", "Platte 4 ist ein schlichter Boden. Ich hab mitgezählt.")),
            ),
        ) {
            border(); floor()
            fill(5..10, 15..15, 'h'); put(11, 15, 'k'); fill(12..17, 15..15, 'h'); put(18, 15, 'k'); fill(19..24, 15..15, 'h')
            fill(27..28, 15..15, 'f')
            spawn(); door()
        },

        // 22 — the chip warms up; linger on it and it goes to full load; a fan blade rolls in, so hop it without slowing down;
        // run on and the floor behind it is the problem
        Level(
            name = T("Warm-up", "Warmlaufen"),
            intro = T("A warm chip is a happy chip.", "Ein warmer Chip ist ein glücklicher Chip."),
            start = listOf(Heat('c', rise = 4f, load = true)),
            traps = listOf(
                trap(Heated('c', 0.7f), HeatSpike('c', 1f), say("Thermal throttling: the chip throttles YOU.", "Thermische Drosselung: der Chip drosselt DICH.")),
                trap(PastX(6f), Saw(33f, 14.4f, -9f, 0f), say("Fan blade. Stopping is not an option.", "Lüfterblatt. Anhalten ist keine Option.")),
                trap(PastX(23.4f), Play(Card.OVERCLOCKED), HeatSpike('f', 0.8f), say("Overclocked on the far side, too.", "Auch auf der anderen Seite übertaktet.")),
            ),
        ) {
            border(); floor()
            bridge(9..20, 'c')
            fill(24..27, 15..15, 'f')
            spawn(); door()
        },

        // 23 — the plate is lukewarm until you step on it; then Mephi turns it up, so waiting for the rail is not an option there
        Level(
            name = T("Waiting Room", "Wartezimmer"),
            intro = T("Take a seat. The plate is lukewarm.", "Setz dich ruhig. Die Platte ist lauwarm."),
            start = listOf(Heat('h', rise = 3f), Clock('a', on = 2.4f, off = 2f, phase = 2f)),
            traps = listOf(
                trap(PastX(8.3f), Play(Card.SINKING), Heat('h', rise = 0.8f), say("Set to Sauna. Sitting not recommended.", "Stufe Sauna. Sitzen nicht empfohlen.")),
                trap(Touch('a'), Clock('a', on = 2.2f, off = 2f), say("The rail got a new appointment schedule.", "Die Schiene hat einen neuen Terminplan.")),
                trap(Airborne(24.3f, 28.5f), HeatSpike('f', 0.7f), say("Next appointment: preheated.", "Nächster Termin: vorgeheizt.")),
            ),
        ) {
            border(); floor()
            fill(8..11, 15..15, 'h')
            bridge(12..22, 'a')
            fill(26..28, 15..15, 'f')
            spawn(); door()
        },

        // 24 — cooling fins: hot plates as a staircase up to the door; they heat faster after the first landing, the top ledge is overclocked
        Level(
            name = T("Cooling Fins", "Kühlrippen"),
            intro = T("Nice view from up there.", "Schöne Aussicht von da oben."),
            start = listOf(Heat('h', rise = 0.8f)),
            traps = listOf(
                trap(Landed(5f, 9f), Play(Card.THROTTLE), Heat('h', rise = 0.6f), say("Fins upgraded: now with extra heat.", "Rippen aufgerüstet: jetzt mit Extrawärme.")),
                trap(Airborne(24f, 28f), HeatSpike('g', 0.7f), say("The summit is warm.", "Der Gipfel ist warm.")),
                trap(Idle(1.3f), HeatSpike('h', 1f), say("Sitting on a plate. Bold.", "Auf einer Platte sitzen. Mutig.")),
            ),
        ) {
            border(); floor()
            fill(5..8, 13..13, 'h'); fill(11..14, 11..11, 'h'); fill(17..20, 9..9, 'h'); fill(23..26, 7..7, 'h')
            fill(28..30, 5..5, 'g')
            spawn(); door(29, 4)
        },

        // 25 — a hot floor and one cool spot, two tiles up; the cool spot warms up once you land on it; climb down and the floor
        // turns up (leap off the tower instead), and the exit is not load-bearing
        Level(
            name = T("Cooling Tower", "Kühlturm"),
            intro = T("The only cool spot is upstairs.", "Der einzige kühle Ort liegt oben."),
            start = listOf(Heat('h', rise = 1.4f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(Landed(12f, 16f), Play(Card.GHOST_BLOCK), HeatSpike('k', 0.55f), say("Upstairs is warming up. Hurry.", "Oben wird es warm. Beeil dich.")),
                trap(Landed(16f, 22f), Heat('h', rise = 0.55f), say("Ground floor: now with floor heating.", "Erdgeschoss: jetzt mit Fußbodenheizung.")),
                trap(PastX(24f), Fall('x'), say("Exit: load-bearing? No.", "Ausgang: tragend? Nein."), delay = 0.1f),
            ),
        ) {
            border(); floor()
            fill(4..24, 15..15, 'h')
            fill(13..15, 13..14, 'k')
            fill(27..28, 15..17, 'x')
            spawn(); door()
        },

        // 26 — a flickering live gate over a hot plate: wait on the sink for the gate; the gate changes rhythm when you touch the sink
        Level(
            name = T("Hot Wire", "Heißer Draht"),
            intro = T("Hot plate, live wire. Pick your favorite.", "Heiße Platte, Draht unter Strom. Such dir was aus."),
            start = listOf(Heat('h', rise = 1.3f), Heatsink('k', cools = "h"), Clock('Z', on = 1.2f, off = 2f)),
            traps = listOf(
                trap(Touch('k'), Play(Card.SHORT_CIRCUIT), Clock('Z', on = 0.8f, off = 1.8f), say("Gate firmware: also updated.", "Tor-Firmware: auch aktualisiert.")),
                trap(Airborne(23.5f, 27f), HeatSpike('f', 0.7f), say("Gate passed. Landing pad: warm.", "Tor passiert. Landeplatz: warm.")),
            ),
        ) {
            border(); floor()
            fill(6..12, 15..15, 'h'); put(13, 15, 'k'); fill(14..22, 15..15, 'h')
            wire(17, 'Z')
            fill(26..27, 15..15, 'f')
            spawn(); door()
        },

        // 27 — a pad powers the bridge; the floor behind it is overclocked, and a dead cable wakes up as you land
        Level(
            name = T("Wall Socket", "Steckdose"),
            intro = T("Surge protector: installed.", "Überspannungsschutz: eingebaut."),
            start = listOf(Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON), Circuit('Z', on = false)),
            traps = listOf(
                trap(PastX(16.3f), Play(Card.OVERCLOCKED), HeatSpike('f', 0.75f), say("Overvoltage! It was a feature request.", "Überspannung! Das war ein Feature-Wunsch.")),
                trap(Landed(17f, 22.5f), Power('Z', true), say("Undervoltage: no refunds.", "Unterspannung: keine Rückerstattung.")),
                trap(Idle(1.3f), Power('a', false), say("Idle? Power saving kicks in.", "Leerlauf? Der Stromsparmodus greift.")),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            fill(16..22, 15..15, 'f')
            fill(25..26, 14..14, 'Z')
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
