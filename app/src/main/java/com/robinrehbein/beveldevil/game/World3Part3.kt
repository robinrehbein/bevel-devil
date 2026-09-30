package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Fan
import com.robinrehbein.beveldevil.game.Action.FanSet
import com.robinrehbein.beveldevil.game.Action.Flip
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Heatsink
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 3, levels 33-48. Act 3, "Lüfter": fans (updrafts, crosswinds, reversals, fan traps), everything combined,
 * and the BIOS finale in the last three levels. The blue board. The wind is the trap here: gusts that change their
 * rhythm once you trust them, a draft that reverses mid-float, plates that warm up right where you land, and two
 * fans that look deadly but carry you. Nothing ever leaves the player stuck: a fan is never switched off for good.
 */
object World3Part3 {
    val levels: List<Level> = listOf(

        // 33 — teaches the updraft: stand in the draft until you are high enough, then step out onto the ledge; it is warm there, and breaks under the leap
        Level(
            name = T("Updraft", "Aufwind"),
            intro = T("Free ride. Hold on to your cube.", "Freifahrt. Halt deinen Würfel fest."),
            start = listOf(Fan('f', at = 12 to 15, dir = Dir.UP, reach = 11, speed = 9f, width = 2)),
            traps = listOf(
                trap(Landed(14.5f, 19f), HeatSpike('g', 0.75f), say("Landing gear: preheated.", "Fahrwerk: vorgeheizt.")),
                trap(Airborne(22f, 27f), Play(Card.SPIKE_SEED), Fall('x'), say("Landing fee for passengers.", "Landegebühr für Passagiere."), delay = 0.1f),
            ),
        ) {
            border(); floor()
            fill(15..30, 7..7); fill(16..17, 7..7, 'g'); fill(24..25, 7..7, 'x')
            spawn(); door(28, 6)
        },

        // 34 — teaches the crosswind: it only grabs you in the air; after the first landing the wind takes breaks
        Level(
            name = T("Tailwind", "Rückenwind"),
            intro = T("Go with the flow. Jump into it.", "Schwimm mit dem Strom. Spring hinein."),
            start = listOf(Fan('w', at = 0 to 8, dir = Dir.RIGHT, reach = 30, speed = 10f, width = 6)),
            traps = listOf(
                trap(Touch('e'), Play(Card.CRUMBLE), Fall('e'), say("Landing zone: crumbly.", "Landezone: bröselig."), delay = 0.45f),
                trap(Landed(17f, 19.6f), Fan('w', at = 0 to 8, dir = Dir.RIGHT, reach = 30, speed = 10f, width = 6, on = 1.6f, off = 2.2f), say("Wind break: the second gap needs a gust.", "Windpause: die zweite Lücke braucht eine Böe.")),
                trap(Airborne(23f, 28f), FanSet('w', 6f), say("The gust drops off mid-air.", "Die Böe lässt mitten in der Luft nach.")),
            ),
        ) {
            border(); floor(); pit(9..16)
            fill(17..18, 15..17, 'e')
            pit(22..27)
            spawn(); door()
        },

        // 35 — a headwind that takes breaks: no jump clears the spikes against it; it gusts harder once you are close, and the landing is warm
        Level(
            name = T("Headwind", "Gegenwind"),
            intro = T("Breezy today.", "Heute ist es windig."),
            start = listOf(Fan('w', at = 31 to 11, dir = Dir.LEFT, reach = 12, speed = 5.5f, width = 4, on = 2.2f, off = 2f)),
            traps = listOf(
                trap(PastX(18.5f), Play(Card.BACKDRAFT), FanSet('w', 7f), say("Gusts upgraded. No charge.", "Böen aufgerüstet. Kostenlos.")),
                trap(Airborne(23.5f, 27f), HeatSpike('g', 0.7f), say("Landing strip: warm.", "Landebahn: warm.")),
            ),
        ) {
            border(); floor()
            put(25, 14, '^'); put(26, 14, '^')
            fill(27..28, 15..15, 'g')
            spawn(); door()
        },

        // 36 — an updraft over a spike pit: looks deadly, carries you across; the far side is warm, and a fan blade rolls in
        Level(
            name = T("Air Cushion", "Luftkissen"),
            intro = T("Watch your step. Literally.", "Pass auf, wo du hintrittst. Wörtlich."),
            start = listOf(Fan('f', at = 9 to 17, dir = Dir.UP, reach = 7, speed = 8f, width = 12)),
            traps = listOf(
                trap(Landed(20.5f, 23.5f), HeatSpike('g', 0.65f), say("The pit was the safe part.", "Die Grube war der sichere Teil.")),
                trap(PastX(23.6f), Play(Card.GHOST_BLOCK), Saw(33f, 14.4f, -6.5f, 0f), say("Fan blade, rolling in.", "Lüfterblatt, rollt an.")),
            ),
        ) {
            border(); floor(); pit(9..20)
            fill(9..20, 17..17); fill(9..20, 16..16, '^')
            fill(21..22, 15..15, 'g')
            spawn(); door()
        },

        // 37 — the tailwind runs in shifts: jump when it is working; after the first landing the gusts get stronger
        Level(
            name = T("Lull", "Flaute"),
            intro = T("Union rules apply to fans too.", "Für Lüfter gilt der Betriebsrat."),
            start = listOf(Fan('w', at = 0 to 8, dir = Dir.RIGHT, reach = 30, speed = 10f, width = 6, on = 2.2f, off = 2.4f)),
            traps = listOf(
                trap(Touch('e'), Play(Card.SINKING), Fall('e'), say("Break time is over. For the floor, too.", "Pause vorbei. Für den Boden auch."), delay = 0.45f),
                trap(Landed(18f, 20.5f), FanSet('w', 14f), say("Overtime: the gusts are stronger now.", "Überstunden: die Böen sind jetzt stärker.")),
                trap(Airborne(24f, 29f), FanSet('w', -6f), say("And then it turns around.", "Und dann dreht er sich um.")),
            ),
        ) {
            border(); floor(); pit(10..17)
            fill(18..19, 15..17, 'e')
            pit(23..27)
            spawn(); door()
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

        // 39 — a downdraft over a short pit: you cannot jump against it, only between its gusts; the plates behind it are warm, and a blade rolls in
        Level(
            name = T("Downdraft", "Fallwind"),
            intro = T("Gravity, but with a motor.", "Schwerkraft, aber mit Motor."),
            start = listOf(Fan('d', at = 15 to 0, dir = Dir.DOWN, reach = 14, speed = 9f, width = 4, on = 2f, off = 2.4f)),
            traps = listOf(
                trap(Landed(18.5f, 21.5f), Play(Card.OVERCLOCKED), HeatSpike('g', 0.7f), say("Made it across. The plates are warm.", "Drüben. Die Platten sind warm.")),
                trap(Landed(25.5f, 28f), Saw(33f, 14.4f, -6f, 0f), say("And a fan blade for the road.", "Und ein Lüfterblatt für unterwegs.")),
            ),
        ) {
            border(); floor(); pit(15..18)
            fill(23..25, 15..15, 'g')
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

        // 41 — two updrafts, one above the other: ledge, ledge, door; the first ledge is warm, the top one breaks
        Level(
            name = T("Air Bridge", "Luftbrücke"),
            intro = T("Two fans, one door. The door is very high.", "Zwei Lüfter, eine Tür. Die Tür ist sehr hoch."),
            start = listOf(
                Fan('f', at = 6 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
                Fan('g', at = 13 to 8, dir = Dir.UP, reach = 6, speed = 9f, width = 2),
            ),
            traps = listOf(
                trap(Landed(8.5f, 11.5f), HeatSpike('p', 0.7f), say("Ledge one: freshly heated.", "Kante eins: frisch beheizt.")),
                trap(PastX(22f), Play(Card.GHOST_BLOCK), Fall('x'), say("Turbulence on the top floor.", "Turbulenzen im Obergeschoss."), delay = 0.1f),
            ),
        ) {
            border(); floor()
            fill(9..15, 8..8); fill(9..10, 8..8, 'p'); fill(16..30, 4..4); fill(23..24, 4..4, 'x')
            spawn(); door(28, 3)
        },

        // 42 — the floor is a chip under load: there is no waiting here, only the way up; a blade rolls in on the ledge, the ledge is warm
        Level(
            name = T("Exhaust", "Abluft"),
            intro = T("It's a bit warm in here.", "Ist ein bisschen warm hier."),
            start = listOf(
                Heat('c', rise = 3.5f, load = true),
                Fan('f', at = 12 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2),
            ),
            traps = listOf(
                trap(Landed(14.5f, 19f), HeatSpike('g', 0.7f), say("The ledge got the heat too.", "Die Kante hat die Hitze auch abbekommen.")),
                trap(PastX(20.5f), Play(Card.DEVIL_SAW), Saw(33f, 7.4f, -7f, 0f), say("Exhaust fan blade. Free-range.", "Abluft-Lüfterblatt. Freilaufend.")),
            ),
        ) {
            border(); floor()
            fill(3..30, 15..15, 'c'); put(12, 15, '#')
            fill(15..30, 8..8); fill(16..17, 8..8, 'g')
            spawn(); door(28, 7)
        },

        // 43 — the pad on the high ledge powers the bridge on a clock; the power is cut if you land on the floor instead of on the bridge
        Level(
            name = T("Wiring Diagram", "Schaltplan"),
            intro = T("Follow the wiring diagram.", "Folge dem Schaltplan."),
            start = listOf(
                Fan('f', at = 7 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
                Circuit('a', on = false), Pad('1', at = 12 to 7, circuits = "a"),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Clock('a', on = 3.2f, off = 2f), say("Wiring diagram, page 1: a timer.", "Schaltplan, Seite 1: ein Timer.")),
                trap(Landed(15.5f, 21.6f), Power('a', false), say("Wiring diagram, page 2: the floor is not on it.", "Schaltplan, Seite 2: der Boden steht nicht drin.")),
            ),
        ) {
            border(); floor()
            fill(10..15, 8..8)
            bridge(22..27, 'a')
            spawn(); door()
        },

        // 44 — the monitor is mounted upside down for a while, exactly when you ride the fan; then the picture rolls and the ledge warms up
        Level(
            name = T("Display", "Anzeige"),
            intro = T("I mounted the monitor myself.", "Den Monitor habe ich selbst montiert."),
            start = listOf(Fan('f', at = 14 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2)),
            traps = listOf(
                trap(PastX(6f), Play(Card.UPSIDE_DOWN), Flip(3.5f), say("Better view from down here. Or up. Whatever.", "Von hier unten hat man die bessere Aussicht. Oder oben.")),
                trap(Landed(17f, 22f), Roll(1.8f), say("Vertical hold lost. Horizontal, too.", "Bildlauf verloren. Horizontal auch.")),
                trap(PastX(22.6f), HeatSpike('g', 0.8f), say("Right side up again. Hot, too.", "Wieder richtig herum. Heiß auch.")),
            ),
        ) {
            border(); floor()
            fill(17..30, 7..7); fill(23..26, 7..7, 'g')
            spawn(); door(28, 6)
        },

        // 45 — the hot floor, the cool ledge: the updraft takes you to the heatsink, then sprint; the plates now heat faster
        Level(
            name = T("Cold Air", "Kaltluft"),
            intro = T("Plenty of hot air here. Mostly mine.", "Hier gibt es viel heiße Luft. Meist meine."),
            start = listOf(
                Heat('h', rise = 1.5f), Heatsink('k', cools = "h"),
                Fan('f', at = 8 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2),
            ),
            traps = listOf(
                trap(Landed(11f, 16f), Play(Card.THROTTLE), Heat('h', rise = 1.25f), say("Hot air, now with more heat.", "Heiße Luft, jetzt mit mehr Hitze.")),
                trap(Airborne(24.5f, 28.5f), HeatSpike('f', 0.7f), say("Cold air, hot plate, warm finish.", "Kalte Luft, heiße Platte, warmes Ende.")),
            ),
        ) {
            border(); floor()
            fill(3..26, 15..15, 'h'); put(8, 15, '#')
            fill(11..15, 8..8, 'k')
            fill(27..28, 15..15, 'f')
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
                trap(PastX(9f), Play(Card.BIOS), BitFlip('a', 'b'), say("Beep. Memory: one bit flipped. Acceptable.", "Piep. Speicher: ein Bit gekippt. Akzeptabel.")),
                trap(PastX(17f), Heat('c', rise = 2.1f, load = true), say("Beep beep. Processor: running hot. Normal.", "Piep piep. Prozessor: läuft heiß. Normal.")),
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
                trap(PastX(6f), Clock('Z', on = 1.0f, off = 1.6f), say("Boot delay: adjusted.", "Boot-Verzögerung: angepasst.")),
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
