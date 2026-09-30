package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Fan
import com.robinrehbein.beveldevil.game.Action.FanSet
import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Flip
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Heatsink
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 3, levels 33-48. Act 3, "Lüfter": fans (updrafts, crosswinds, reversals, fan traps), everything combined,
 * and the BIOS finale in the last three levels. The blue board.
 */
object World3Part3 {
    private val hidden = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(

        // 33 — teaches the updraft: stand in the draft until you are high enough, then step out onto the ledge
        Level(
            name = T("Updraft", "Aufwind"),
            intro = T("Free ride. Hold on to your cube.", "Freifahrt. Halt deinen Würfel fest."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            start = listOf(Fan('f', at = 12 to 15, dir = Dir.UP, reach = 11, speed = 9f, width = 2)),
            traps = listOf(
                trap(PastX(4.4f), Show('B'), say("Boarding pass, please. Spikes only.", "Bordkarte bitte. Nur Spikes.")),
                trap(PastX(22.3f), Play(Card.SPIKE_SEED), Show('A'), say("Landing fee for passengers.", "Landegebühr für Passagiere.")),
            ),
        ) {
            border(); floor()
            put(5, 14, 'B')
            fill(15..30, 7..7)
            put(23, 6, 'A'); put(24, 6, 'A')
            spawn(); door(28, 6)
        },

        // 34 — teaches the crosswind: it only grabs you in the air, and carries the jump over a pit too wide for it
        Level(
            name = T("Tailwind", "Rückenwind"),
            intro = T("Go with the flow. Jump into it.", "Schwimm mit dem Strom. Spring hinein."),
            start = listOf(Fan('w', at = 0 to 8, dir = Dir.RIGHT, reach = 30, speed = 10f, width = 6)),
            traps = listOf(
                trap(Touch('e'), Play(Card.CRUMBLE), Fall('e'), say("Landing zone: crumbly.", "Landezone: bröselig."), delay = 0.45f),
            ),
        ) {
            border(); floor(); pit(9..16)
            fill(17..18, 15..17, 'e')
            spawn(); door()
        },

        // 35 — a headwind that takes breaks: no jump clears the spikes against it
        Level(
            name = T("Headwind", "Gegenwind"),
            intro = T("Breezy today.", "Heute ist es windig."),
            start = listOf(Fan('w', at = 31 to 11, dir = Dir.LEFT, reach = 12, speed = 5.5f, width = 4, on = 2.2f, off = 2f)),
            traps = listOf(
                trap(Touch('x'), Play(Card.DECOY), Fall('x'), say("Wind-eroded. Sorry.", "Windgeschliffen. Sorry."), delay = 0.15f),
            ),
        ) {
            border(); floor()
            fill(4..5, 15..17, 'x')
            put(25, 14, '^'); put(26, 14, '^')
            spawn(); door()
        },

        // 36 — an updraft over a spike pit: looks deadly, carries you across
        Level(
            name = T("Air Cushion", "Luftkissen"),
            intro = T("Watch your step. Literally.", "Pass auf, wo du hintrittst. Wörtlich."),
            legend = mapOf('A' to hidden),
            start = listOf(Fan('f', at = 9 to 17, dir = Dir.UP, reach = 7, speed = 8f, width = 12)),
            traps = listOf(
                trap(PastX(3.4f), HeatSpike('g', 0.8f), say("Runway: heated.", "Startbahn: beheizt.")),
                trap(PastX(25.2f), Play(Card.GHOST_BLOCK), Show('A'), say("The pit was the safe part.", "Die Grube war der sichere Teil.")),
            ),
        ) {
            border(); floor(); pit(9..20)
            fill(4..6, 15..15, 'g')
            fill(9..20, 17..17); fill(9..20, 16..16, '^')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 37 — the tailwind runs in shifts: jump when it is working
        Level(
            name = T("Lull", "Flaute"),
            intro = T("Union rules apply to fans too.", "Für Lüfter gilt der Betriebsrat."),
            start = listOf(Fan('w', at = 0 to 8, dir = Dir.RIGHT, reach = 30, speed = 10f, width = 6, on = 2.2f, off = 2.4f)),
            traps = listOf(
                trap(Touch('e'), Play(Card.SINKING), Fall('e'), say("Break time is over. For the floor, too.", "Pause vorbei. Für den Boden auch."), delay = 0.45f),
            ),
        ) {
            border(); floor(); pit(10..17)
            fill(18..19, 15..17, 'e')
            spawn(); door()
        },

        // 38 — the fan is dead until you step on the reset pad
        Level(
            name = T("Silence", "Stille"),
            intro = T("It's so quiet in here.", "Schön leise hier."),
            start = listOf(
                Fan('f', at = 9 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2), Power('f', false),
                Pad('1', at = 5 to 14),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.DECOY), Power('f', true), say("Fan restarted. Surprised? Me too.", "Lüfter neu gestartet. Überrascht? Ich auch.")),
            ),
        ) {
            border(); floor()
            fill(12..30, 8..8)
            spawn(); door(28, 7)
        },

        // 39 — a downdraft over a short pit: you cannot jump against it, only between its gusts
        Level(
            name = T("Downdraft", "Fallwind"),
            intro = T("Gravity, but with a motor.", "Schwerkraft, aber mit Motor."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            start = listOf(Fan('d', at = 15 to 0, dir = Dir.DOWN, reach = 14, speed = 9f, width = 4, on = 2f, off = 2.4f)),
            traps = listOf(
                trap(PastX(4.4f), Show('B'), say("Ground crew says hello. With spikes.", "Bodenpersonal grüßt. Mit Spikes.")),
                trap(PastX(22.2f), Play(Card.SPIKE_SEED), Show('A'), say("Made it across. The next gap is spiky.", "Drüben. Die nächste Lücke ist spitz.")),
            ),
        ) {
            border(); floor(); pit(15..18)
            put(5, 14, 'B')
            put(23, 14, 'A'); put(24, 14, 'A')
            spawn(); door()
        },

        // 40 — the updraft over the spikes reverses while you float: settle on the stone, it comes back
        Level(
            name = T("Air Castle", "Luftschloss"),
            intro = T("Wind is free. Take as much as you like.", "Wind ist kostenlos. Nimm, so viel du willst."),
            start = listOf(Fan('f', at = 9 to 17, dir = Dir.UP, reach = 7, speed = 8f, width = 16)),
            traps = listOf(
                trap(PastX(11.3f), Play(Card.TWISTED), FanSet('f', -9f), say("Reverse thrust! (The stone is your friend.)", "Schubumkehr! (Der Stein ist dein Freund.)")),
                trap(Touch('s'), FanSet('f', 8f), say("Thrust forward. Try to keep up.", "Schub voraus. Halt dich fest.")),
            ),
        ) {
            border(); floor(); pit(9..24)
            fill(9..24, 17..17); fill(9..24, 16..16, '^')
            fill(11..13, 15..16, 's')
            spawn(); door()
        },

        // 41 — two updrafts, one above the other: ledge, ledge, door
        Level(
            name = T("Air Bridge", "Luftbrücke"),
            intro = T("Two fans, one door. The door is very high.", "Zwei Lüfter, eine Tür. Die Tür ist sehr hoch."),
            start = listOf(
                Fan('f', at = 6 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
                Fan('g', at = 13 to 8, dir = Dir.UP, reach = 6, speed = 9f, width = 2),
            ),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(23.3f), Play(Card.GHOST_BLOCK), Show('A'), say("Turbulence on the top floor.", "Turbulenzen im Obergeschoss.")),
            ),
        ) {
            border(); floor()
            fill(9..15, 8..8); fill(16..30, 4..4)
            put(23, 3, 'A')
            spawn(); door(28, 3)
        },

        // 42 — the floor is a chip under load: there is no waiting here, only the way up
        Level(
            name = T("Exhaust", "Abluft"),
            intro = T("It's a bit warm in here.", "Ist ein bisschen warm hier."),
            legend = mapOf('B' to hidden),
            start = listOf(
                Heat('c', rise = 3.5f, load = true),
                Fan('f', at = 12 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2),
            ),
            traps = listOf(
                trap(PastX(4.4f), Show('B'), say("Ventilation shaft: entry spike.", "Lüftungsschacht: Eingangsspike.")),
                trap(PastX(20.5f), Play(Card.DEVIL_SAW), Saw(33f, 7.4f, -7f, 0f), say("Exhaust fan blade. Free-range.", "Abluft-Lüfterblatt. Freilaufend.")),
            ),
        ) {
            border(); floor()
            put(5, 14, 'B')
            fill(3..30, 15..15, 'c'); put(12, 15, '#')
            fill(15..30, 8..8)
            spawn(); door(28, 7)
        },

        // 43 — the pad on the high ledge powers the bridge: fan, pad, bridge
        Level(
            name = T("Wiring Diagram", "Schaltplan"),
            intro = T("Follow the wiring diagram.", "Folge dem Schaltplan."),
            start = listOf(
                Fan('f', at = 7 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
                Circuit('a', on = false), Pad('1', at = 12 to 7, circuits = "a"),
            ),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(4.4f), Play(Card.SPIKE_SEED), Show('A'), say("Wiring diagram, page 1: spikes.", "Schaltplan, Seite 1: Spikes.")),
            ),
        ) {
            border(); floor()
            put(5, 14, 'A')
            fill(10..15, 8..8)
            bridge(20..27, 'a')
            spawn(); door()
        },

        // 44 — the monitor is mounted upside down for a while, exactly when you ride the fan
        Level(
            name = T("Display", "Anzeige"),
            intro = T("I mounted the monitor myself.", "Den Monitor habe ich selbst montiert."),
            start = listOf(Fan('f', at = 14 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2)),
            traps = listOf(
                trap(PastX(4.4f), Show('B'), say("Pixel error at column five.", "Pixelfehler in Spalte fünf.")),
                trap(PastX(6f), Play(Card.UPSIDE_DOWN), Flip(3.5f), say("Better view from down here. Or up. Whatever.", "Von hier unten hat man die bessere Aussicht. Oder oben.")),
                trap(PastX(23.3f), Show('A'), say("Right side up again. Spikes too.", "Wieder richtig herum. Spikes auch.")),
            ),
            legend = mapOf('A' to hidden, 'B' to hidden),
        ) {
            border(); floor()
            put(5, 14, 'B')
            fill(17..30, 7..7)
            put(24, 6, 'A'); put(25, 6, 'A')
            spawn(); door(28, 6)
        },

        // 45 — the hot floor, the cool ledge: the updraft takes you to the heatsink, then sprint
        Level(
            name = T("Cold Air", "Kaltluft"),
            intro = T("Plenty of hot air here. Mostly mine.", "Hier gibt es viel heiße Luft. Meist meine."),
            start = listOf(
                Heat('h', rise = 1.5f), Heatsink('k', cools = "h"),
                Fan('f', at = 8 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2),
            ),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(3.4f), HeatSpike('g', 0.8f), say("Hot start, cold finish. Or so I hoped.", "Heißer Start, kalter Schluss. Dachte ich.")),
                trap(PastX(26.2f), Play(Card.CRUMBLE), Show('A'), say("Cold air, hot plate, sharp finish.", "Kalte Luft, heiße Platte, spitzes Ende.")),
            ),
        ) {
            border(); floor()
            put(27, 14, 'A'); put(28, 14, 'A')
            fill(3..26, 15..15, 'h'); put(8, 15, '#'); fill(4..6, 15..15, 'g')
            fill(11..15, 8..8, 'k')
            spawn(); door()
        },

        // 46 — BIOS finale, stage 1: the power-on self-test. Memory, processor, fan: one beep each.
        Level(
            name = T("POST", "Selbsttest"),
            intro = T("Power-on self-test. Three checks. One beep each.", "Einschalt-Selbsttest. Drei Prüfungen. Je ein Piep."),
            start = listOf(
                Circuit('a'), Circuit('b', on = false),
                Heat('c', rise = 2.6f, load = true), Heatsink('k', cools = "c"),
                Fan('f', at = 26 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
            ),
            traps = listOf(
                trap(PastX(3f), say("POST: memory test... ", "POST: Speichertest... ")),
                trap(PastX(9f), Play(Card.GHOST_BLOCK), BitFlip('a', 'b'), say("Beep. Memory: one bit flipped. Acceptable.", "Piep. Speicher: ein Bit gekippt. Akzeptabel.")),
                trap(PastX(17f), say("Beep beep. Processor: running hot. Normal.", "Piep piep. Prozessor: läuft heiß. Normal.")),
                trap(PastX(25f), say("Fan: 0 RPM. Press F1 to continue.", "Lüfter: 0 U/min. F1 zum Fortfahren drücken.")),
            ),
        ) {
            border(); floor()
            bridge(7..10, 'a'); bridge(11..14, 'b')
            put(16, 15, 'k'); fill(18..24, 15..15, 'c')
            fill(28..30, 7..7)
            spawn(); door(29, 6)
        },

        // 47 — BIOS finale, stage 2: the boot order. USB and disk are fake doors, the network is real.
        Level(
            name = T("Boot Order", "Boot-Reihenfolge"),
            intro = T("Just walk to the door. Really.", "Geh einfach zur Tür. Wirklich."),
            start = listOf(
                Clock('Z', on = 1.3f, off = 1.7f),
                Fan('f', at = 16 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2), Power('f', false),
            ),
            traps = listOf(
                trap(AtDoor, Play(Card.DECOY), FakeWin(FakeEnd.CLEAR, null, HeatSpike('g', 0.6f), DoorTo(4, 14), say("Boot device 1: USB. No bootable medium.", "Bootgerät 1: USB. Kein bootfähiges Medium."))),
                trap(AtDoor, FakeWin(FakeEnd.CLEAR, null, Power('f', true), DoorTo(28, 5), say("Boot device 2: disk. Sector 0 unreadable.", "Bootgerät 2: Festplatte. Sektor 0 unlesbar."))),
            ),
        ) {
            border(); floor()
            wire(10, 'Z')
            fill(21..24, 15..15, 'g')
            fill(19..30, 6..6)
            spawn(); door()
        },

        // 48 — BIOS finale, stage 3: the setup. Gate, melt plates, the fan that turns on you, and the door.
        Level(
            name = T("BIOS Setup", "BIOS-Setup"),
            intro = T("Press DEL to enter setup. Everything else is my job.", "ENTF für das Setup. Alles andere ist mein Job."),
            start = listOf(
                Clock('Z', on = 1.2f, off = 1.8f),
                Heat('m', rise = 0.7f, melt = true), Heat('n', rise = 0.7f, melt = true),
                Fan('f', at = 20 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2),
                Circuit('w'), Pad('1', at = 25 to 5, circuits = "w", mode = PadMode.HOLD),
            ),
            traps = listOf(
                trap(PastX(3f), say("BIOS v6.66. Memory: 640K. Patience: 0K.", "BIOS v6.66. Speicher: 640K. Geduld: 0K.")),
                trap(PastX(17.5f), say("CPU: overclocked. Fan: optional.", "CPU: übertaktet. Lüfter: optional.")),
                trap(Zone(20f, 0f, 22f, 6.2f), Play(Card.GRAND_FINALE), FanSet('f', -9f), say("Fan mode: REVERSE. Load defaults? No.", "Lüftermodus: UMGEKEHRT. Standardwerte laden? Nein.")),
                trap(PastX(23.5f), Roll(2.5f), say("kill -9 mephi. ... Permission denied.", "kill -9 mephi. ... Zugriff verweigert.")),
            ),
        ) {
            border(); floor()
            wire(6, 'Z')
            pit(9..17)
            fill(11..13, 15..15, 'm'); fill(14..17, 15..15, 'n')
            put(20, 14, '^'); put(21, 14, '^')
            fill(22..30, 6..6)
            wire(26, 'w', top = 1, bottom = 5)
            spawn(); door(29, 5)
        },
    )
}
