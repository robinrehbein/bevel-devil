package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
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
import com.robinrehbein.beveldevil.game.Action.Move
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
    val levels: List<Level> = World3PartE.levels + listOf(

        // 41 — two updrafts, one above the other: ledge, ledge, door; the first ledge is warm, live ceiling wiring talks you out of jumping (it switches off when you do), and the top ledge breaks under a walk
        Level(
            name = T("Air Bridge", "Luftbrücke"),
            intro = T("Two fans, one door. The door is very high.", "Zwei Lüfter, eine Tür. Die Tür ist sehr hoch."),
            legend = mapOf('Z' to Glyph(spike = true, dir = Dir.DOWN)),
            start = listOf(
                Fan('f', at = 6 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
                Fan('g', at = 13 to 8, dir = Dir.UP, reach = 6, speed = 9f, width = 2),
                Circuit('Z'),
            ),
            traps = listOf(
                trap(Landed(8.5f, 11.5f), HeatSpike('p', 0.7f), say("Ledge one: freshly heated.", "Kante eins: frisch beheizt.")),
                trap(PastX(22f), Play(Card.GHOST_BLOCK), Fall('x'), say("Turbulence on the top floor.", "Turbulenzen im Obergeschoss."), delay = 0.1f),
                // the live ceiling wiring talks you out of jumping; it switches off the moment you do
                trap(Airborne(21.5f, 24f), Power('Z', false), say("Ceiling wiring: off. It was only there to scare you.", "Deckenleitung: aus. Die war nur zum Erschrecken da.")),
            ),
            // rematch: the turbulence is a bluff and the wiring stays live: the jump from round 1 hits it, walking under it is fine
            rematch = listOf(
                Round(
                    T("Air bridge, after inspection.", "Luftbrücke. TÜV bestanden. Angeblich."),
                    start = listOf(
                        Fan('f', at = 6 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
                        Fan('g', at = 13 to 8, dir = Dir.UP, reach = 6, speed = 9f, width = 2),
                        Circuit('Z'),
                    ),
                    traps = listOf(
                        trap(Landed(8.5f, 11.5f), HeatSpike('p', 0.7f), say("Ledge one: still heated.", "Kante eins: immer noch beheizt.")),
                        trap(PastX(22f), Bluff(Card.GHOST_BLOCK)),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(9..15, 8..8); fill(9..10, 8..8, 'p'); fill(16..30, 4..4); fill(23..24, 4..4, 'x')
            fill(22..25, 1..1, 'Z')
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
            // rematch: the blade flies at head height now: the hop that cleared it in round 1 runs into it, walking passes under
            rematch = listOf(
                Round(
                    T("Exhaust, round two. New blade mount.", "Abluft, Runde zwei. Frisch montiert."),
                    start = listOf(
                        Heat('c', rise = 3.5f, load = true),
                        Fan('f', at = 12 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2),
                    ),
                    traps = listOf(
                        trap(Landed(14.5f, 19f), HeatSpike('g', 0.7f), say("The ledge is warm. Some things never change.", "Die Kante ist warm. Manches ändert sich nie.")),
                        trap(PastX(20.5f), Play(Card.DEVIL_SAW), Saw(33f, 6f, -7f, 0f), say("Exhaust blade, ceiling-mounted.", "Abluft-Blatt, an der Decke montiert.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(3..30, 15..15, 'c'); put(12, 15, '#')
            fill(15..30, 8..8); fill(16..17, 8..8, 'g')
            spawn(); door(28, 7)
        },

        // 43 — the pad on the high ledge powers the bridge on a clock; the power is cut if you land on the floor instead of on the bridge, and a dead cable on the bridge goes live as you land on it
        Level(
            name = T("Wiring Diagram", "Schaltplan"),
            intro = T("Follow the wiring diagram.", "Folge dem Schaltplan."),
            start = listOf(
                Fan('f', at = 7 to 15, dir = Dir.UP, reach = 9, speed = 9f, width = 2),
                Circuit('a', on = false), Pad('1', at = 12 to 7, circuits = "a"),
                Circuit('Z', on = false),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Clock('a', on = 3.2f, off = 2f), say("Wiring diagram, page 1: a timer.", "Schaltplan, Seite 1: ein Timer.")),
                trap(Landed(15.5f, 21.6f), Power('a', false), say("Wiring diagram, page 2: the floor is not on it.", "Schaltplan, Seite 2: der Boden steht nicht drin.")),
                trap(Landed(21.6f, 28f), Power('Z', true), say("Page 3: this cable is live now.", "Seite 3: dieses Kabel ist jetzt live.")),
            ),
        ) {
            border(); floor()
            fill(10..15, 8..8)
            bridge(22..27, 'a'); put(26, 14, 'Z')
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

        // 45 — the hot floor, the cool ledge: the updraft takes you to the heatsink; the plates now heat faster, the floor is overclocked where you drop onto it (leap far or hop), and the landing is warm
        Level(
            name = T("Cold Air", "Kaltluft"),
            intro = T("Plenty of hot air here. Mostly mine.", "Hier gibt es viel heiße Luft. Meist meine."),
            start = listOf(
                Heat('h', rise = 1.5f), Heatsink('k', cools = "h"),
                Fan('f', at = 8 to 15, dir = Dir.UP, reach = 10, speed = 9f, width = 2),
            ),
            traps = listOf(
                trap(Landed(11f, 16f), Play(Card.THROTTLE), Heat('h', rise = 1.25f), say("Hot air, now with more heat.", "Heiße Luft, jetzt mit mehr Hitze.")),
                trap(Landed(16.2f, 24.6f), HeatSpike('h', 0.7f), say("Floor: overclocked on arrival.", "Boden: bei Ankunft übertaktet.")),
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
