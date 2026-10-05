package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Ghost
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.PauseTrap
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Resumed
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 2, levels 33-40 (block C of the V2 rollout, docs/LEVEL_DESIGN_V2.md §8 and §11). Act 3, "Root": the traps repeat,
 * flip, queue and sudo. Cards in this block: CRUMBLE 33, UPSIDE_DOWN 34, DECOY 34r2, COLLAPSE 35, DEVIL_SAW 36, HEADBUTT 37,
 * SINKING 38, SPIKE_SEED 39, STALKER 40, a bluff of CRUMBLE in 33r2.
 */
object World2Part3C {
    private val hidden = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(

        // 33 — sudo !! (a trap room: U14 the repeat is a lie, U1 the floor). Along the lane a hole opens in the floor (command 1), and the same
        // hole opens again as you land (sudo !!: louder). Up the stairs and back left along the deck, the repeat comes from above: a block drops
        // out of the ceiling as you pass, and as you hop it the spikes grow where the hop lands
        Level(
            name = T("sudo !!", "sudo !!"),
            intro = T("The last command is still warm.", "Der letzte Befehl ist noch warm."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.CRUMBLE), Fall('a'), say("Command: make hole. Done.", "Befehl: Loch machen. Erledigt.")),
                trap(PastX(11.0f), Fall('b'), say("sudo !!  Again. Louder.", "sudo !!  Nochmal. Lauter.")),
                trap(Zone(15.6f, 4f, 16.4f, 9f), Fall('c'), say("sudo !!  As root, this time.", "sudo !!  Diesmal als root.")),
                trap(Zone(11.5f, 3f, 14.5f, 7.2f), Show('A'), say("sudo sudo !!  Spikes are the new hole.", "sudo sudo !!  Spikes sind das neue Loch.")),
            ),
            // rematch: the first hole is a bluff, and the hop you learned lands in the LEDs. The echo is late now (the hole opens while you are over
            // the LEDs), and the block that dropped when you stopped now drops late: whoever stops to look is under it, the one who runs is not
            rematch = listOf(
                Round(
                    T("Same command. Check your privileges.", "Gleicher Befehl. Prüf deine Rechte."),
                    legend = mapOf('E' to hidden),
                    traps = listOf(
                        trap(PastX(4.5f), Bluff(Card.CRUMBLE), say("Command: make hole. (Not this time.)", "Befehl: Loch machen. (Diesmal nicht.)")),
                        trap(PastX(6.5f), Show('E'), say("Spikes grow where your hop lands.", "Spikes wachsen, wo dein Hüpfer landet.")),
                        trap(Landed(12.2f, 15f), Fall('b'), say("sudo !!  The echo is late.", "sudo !!  Das Echo kommt spät.")),
                        trap(Zone(22f, 4f, 22.8f, 9f), Fall('c'), say("Do not stop to look. I will not.", "Bleib nicht stehen. Ich tu es auch nicht."), delay = 0.35f),
                    ),
                ) {
                    put(11, 14, 'E')
                    fill(14..15, 15..17, '#'); fill(17..18, 15..17, 'b')
                    fill(12..13, 1..2, '#'); fill(21..22, 1..2, 'c')
                    fill(20..21, 13..14, '.'); fill(22..27, 11..14, '.')
                    fill(20..22, 9..9); fill(23..24, 13..14); fill(25..29, 11..14)
                    put(4, 8, '^'); put(5, 8, '^')
                },
            ),
        ) {
            border(); floor()
            fill(1..19, 9..9)
            fill(8..9, 15..17, 'a'); fill(14..15, 15..17, 'b')
            fill(12..13, 1..2, 'c')
            fill(20..21, 13..14); fill(22..27, 11..14)
            put(4, 8, 'A'); put(5, 8, 'A')
            spawn(2, 14); door(2, 8)
        },

        // 34 — reverse proxy (a puzzle room: R3 the portals, R5 the floors, U10 the world turns over). The lane runs right and the gravity turns over
        // before the middle of it: you fall up onto the ceiling, the only way on, and the obvious way along it is a wall of links at the right end,
        // which is the loopback and hands you back at the start of the ceiling. The way on is back to the left: a dark link in the top left comes
        // up as you pass the middle of the ceiling again, and leads to a ledge, where the gravity is yours again, and from there it is the lane
        // to the door
        Level(
            name = T("Reverse Proxy", "Reverse Proxy"),
            intro = T("Everything goes through me here. Everything.", "Hier läuft alles über mich. Alles."),
            start = listOf(
                Portal('2', 24 to 1, 14 to 1, twoWay = false), Portal('4', 24 to 2, 14 to 1, twoWay = false),
                Portal('5', 24 to 3, 14 to 1, twoWay = false), Portal('6', 24 to 4, 14 to 1, twoWay = false),
                Portal('3', 6 to 1, 3 to 10, twoWay = false), Power('3', false),
            ),
            traps = listOf(
                trap(Zone(12f, 10f, 13.5f, 15.5f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Proxying your gravity.", "Deine Schwerkraft wird weitergeleitet.")),
                trap(Zone(10.4f, 0.5f, 11.8f, 4f), Power('3', true), say("Link 3 is up. It always was. Mostly.", "Link 3 ist oben. War er immer. Meistens.")),
                trap(Zone(2.5f, 9f, 5f, 11f), Gravity(false), say("Reverse, reverse.", "Rückwärts, rückwärts.")),
            ),
            // rematch: the gravity turns over at the far end of the lane, behind the LEDs, and the ceiling run goes left; the wall of links is gone,
            // and the only link is stale (the loopback again) until its DNS entry is renewed, a moment after you pass it: hold the loop, wait
            rematch = listOf(
                Round(
                    T("Cache cleared. Please reload.", "Cache geleert. Bitte neu laden."),
                    start = listOf(Portal('3', 17 to 1, 22 to 1, twoWay = false)),
                    traps = listOf(
                        trap(Zone(24f, 10f, 25.4f, 15.5f), Gravity(true), say("Flipped at the other end now. Old habits, cached.", "Jetzt am anderen Ende gekippt. Alte Gewohnheiten, im Cache.")),
                        trap(Zone(19.5f, 0.5f, 20.9f, 4f), Play(Card.DECOY), Reroute('3', 10 to 10), say("Link 3 is renewed. Give it a second.", "Link 3 wird erneuert. Gib ihm eine Sekunde."), delay = 1f),
                        trap(Zone(9f, 9f, 11.5f, 11f), Gravity(false), say("Lane again. Same door, same rules.", "Wieder die Bahn. Gleiche Tür, gleiche Regeln.")),
                    ),
                ) {
                    put(17, 14, '.'); put(18, 14, '.'); put(20, 14, '^'); put(21, 14, '^')
                    put(27, 14, '.'); put(28, 14, 'D'); fill(7..12, 11..11)
                },
            ),
        ) {
            border(); floor()
            fill(1..6, 11..11)
            leds(17..18)
            spawn(2, 14); door(27, 14)
        },

        // 35 — pipeline (a trap room: U12 the belt turns around, U1 the floor). Along the lane (leftwards this time) the floor opens twice; up the
        // steps and onto the deck, where the way to the door is a conveyor in a duct, running your way: as you step on it it turns around, and the
        // floor behind you opens, so there is nothing to do but walk on, against it, with no room to hop. The second belt, in the open, turns
        // around the same way and does not care how you cross it, as long as you are not standing on it
        Level(
            name = T("Pipeline", "Datenleitung"),
            intro = T("Line is clear. I checked.", "Leitung frei. Ich habe nachgesehen."),
            start = listOf(Belt('d', 3f), Belt('e', 3f)),
            traps = listOf(
                trap(BeforeX(27.5f), Play(Card.COLLAPSE), Fall('a'), say("Keep the packets moving.", "Halte die Pakete in Bewegung.")),
                trap(BeforeX(20f), Fall('b'), say("Packet lost. Resending.", "Paket verloren. Wird neu gesendet.")),
                trap(Zone(14.2f, 6f, 15f, 9f), Belt('d', -4.5f), Fall('f'), say("Backpressure: the line pushes back.", "Gegendruck: Die Leitung drückt zurück.")),
                trap(Zone(24.2f, 5f, 25f, 9f), Belt('e', -7f), Fall('g'), say("Bandwidth throttled. Hop, if you can.", "Bandbreite gedrosselt. Hüpf, wenn du kannst.")),
            ),
        ) {
            border(); floor()
            fill(11..30, 9..9)
            fill(14..21, 9..9, 'd'); fill(24..28, 9..9, 'e')
            fill(11..13, 9..9, 'f'); fill(22..23, 9..9, 'g')
            fill(13..22, 7..7)
            fill(21..22, 15..17, 'a'); fill(15..16, 15..17, 'b')
            fill(9..10, 13..14); fill(3..8, 11..14)
            spawn(28, 14); door(29, 8)
        },

        // 36 — access log (a trap room: U16 the replay, with saws). Along the deck a saw rolls at you, and as you land from the hop a second, faster one;
        // off the end of the deck you drop onto the lane, a saw rolls in from the left, and a guard patrols in front of the door. Every attempt is
        // written to the log, and from the second attempt on the log is replayed against you: your last try walks the deck again as a ghost,
        // a second behind you
        Level(
            name = T("Access Log", "Zugriffsprotokoll"),
            intro = T("Nothing new here. Honestly.", "Nichts Neues hier. Ehrlich."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.DEVIL_SAW), Saw(32f, 8.4f, -6f, 0f), say("Login 1: it comes to you.", "Login 1: Er kommt zu dir.")),
                trap(Landed(18f, 27f), Saw(32f, 8.4f, -9f, 0f), say("Login 2: same, but in a hurry.", "Login 2: Genauso, aber eilig.")),
                trap(Landed(27.5f, 31f), Saw(-1.5f, 14.4f, 7f, 0f), say("Login 3: from the other side.", "Login 3: Von der anderen Seite.")),
                trap(Landed(6f, 16f), Saw(-1.5f, 14.4f, 12f, 0f), say("Login 4: the guard has a bad temper.", "Login 4: Der Wächter hat schlechte Laune.")),
                trap(PastX(2.6f), Ghost(0.4f), say("Replay attack: I sent your last run again.", "Replay-Angriff: Ich habe deinen letzten Versuch nochmal gesendet.")),
            ),
        ) {
            border(); floor()
            fill(1..27, 9..9)
            spawn(2, 8); door(2, 14)
        },

        // 37 — two-factor auth (a puzzle room: R1 the switches, U15 the help is the trap). The lane is locked by a copper wall, and the first
        // switch lies behind the start, at the far left: factor 1 accepted, the wall goes dark, and the ceiling over the switch comes down a moment
        // later. Past the wall a block drops out of the ceiling as you pass (once it has landed it is your first step), and up the steps on the roof
        // lies the second switch, which opens the second wall (down on the lane); the ceiling over it comes down on whoever waits there for the
        // result, so the way on is back along the roof and down to the wall
        Level(
            name = T("Two-Factor Auth", "Zwei-Faktor-Login"),
            intro = T("Your code was sent by post. Arrival: soon.", "Dein Code wurde per Post verschickt. Ankunft: bald."),
            start = listOf(
                Circuit('a'), Circuit('b'),
                Pad('1', at = 2 to 14, circuits = "a", mode = PadMode.OFF),
                Pad('2', at = 29 to 8, circuits = "b", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.HEADBUTT), Fall('e'), say("Factor 1 accepted. Do not get comfortable.", "Faktor 1 akzeptiert. Mach es dir nicht bequem."), delay = 0.5f),
                trap(PastX(16f), Fall('c'), say("Your code has arrived. It weighs a lot.", "Dein Code ist angekommen. Er wiegt einiges.")),
                trap(Pressed('2'), Fall('d'), say("Factor 2 accepted. Please wait here for the result.", "Faktor 2 akzeptiert. Bitte warte hier auf das Ergebnis."), delay = 0.4f),
            ),
        ) {
            border(); floor()
            fill(14..15, 1..14, 'a'); fill(27..28, 10..14, 'b')
            fill(19..20, 1..2, 'c'); fill(28..29, 1..2, 'd'); fill(2..3, 1..2, 'e')
            fill(21..23, 11..14); fill(26..30, 9..9)
            spawn(13, 14); door(30, 14)
        },

        // 38 — Bobby Tables (a puzzle room: R3 the portals, U1 the floor). DROP TABLE floor: the lane has two holes with a wormhole at the bottom of
        // each. The first leads back to the start, the second up to the roof, at the far right end, which is the way on: the door sits at the other
        // end of the roof, up two steps. The ground between the holes sinks as you cross it, and so do two stretches of the roof: keep moving
        Level(
            name = T("Bobby Tables", "Klein Bobby Tables"),
            intro = T("Welcome, Robert. Good to have you.", "Willkommen, Robert. Schön, dass du da bist."),
            start = listOf(
                Portal('1', 14 to 17, 3 to 14, twoWay = false), Portal('2', 15 to 17, 3 to 14, twoWay = false),
                Portal('3', 21 to 17, 28 to 7, twoWay = false), Portal('4', 22 to 17, 28 to 7, twoWay = false),
            ),
            traps = listOf(
                trap(Touch('i'), Play(Card.SINKING), Fall('i'), say("DROP TABLE floor; Did you sanitize your inputs?", "DROP TABLE floor; Hast du deine Eingaben bereinigt?"), delay = 0.5f),
                trap(Touch('j'), Fall('j'), say("DROP TABLE roof;", "DROP TABLE dach;"), delay = 0.45f),
                trap(Touch('k'), Fall('k'), say("DROP TABLE students;", "DROP TABLE schueler;"), delay = 0.45f),
            ),
        ) {
            border(); floor()
            pit(14..15); pit(21..22)
            fill(16..20, 15..17, 'i')
            fill(24..25, 9..14)
            fill(2..30, 8..8)
            fill(17..19, 8..8, 'j'); fill(9..11, 8..8, 'k')
            fill(3..5, 6..7); fill(1..2, 4..5)
            spawn(2, 14); door(1, 3)
        },

        // 39 — contingency plan (a trap room: U16 the pause, with lasers). Along the deck a rule is installed in front of you: a firewall flashes once
        // (wait until it is dark), and a trip wire at ankle height (hop it). Off the end of the deck, on the lane, another flash, and then the firewall
        // that is stuck: it is lit all the time and "opens by itself any second now". It does not. Turn it off and on again: pause, and resume
        Level(
            name = T("Contingency Plan", "Notfallplan"),
            intro = T("The firewall is stuck. It'll open by itself any second now.", "Die Firewall klemmt. Die geht gleich von selbst auf."),
            start = listOf(Laser('F', 11 to 10, 11 to 14)),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.SPIKE_SEED), Laser('A', 10 to 1, 10 to 8, on = 1f, off = 60f, delay = 0.55f), say("Rule 1: wait your turn.", "Regel 1: Warte, bis du dran bist.")),
                trap(PastX(12.5f), Laser('B', 17 to 7, 17 to 8, on = 3f, off = 0f), say("Rule 2: mind your ankles.", "Regel 2: Achte auf deine Knöchel.")),
                trap(Zone(19.5f, 12f, 20.5f, 15.5f), Laser('C', 15 to 10, 15 to 14, on = 0.8f, off = 60f, delay = 0.4f), say("Rule 3: the same, one floor down.", "Regel 3: Dasselbe, ein Stockwerk tiefer.")),
                trap(After(0.3f), PauseTrap(PauseTrick.DODGE)),
                trap(Resumed(), Power('F', false), say("Session reset. The firewall forgot you.", "Sitzung zurückgesetzt. Die Firewall hat dich vergessen.")),
            ),
            hint = T("Have you tried turning it off and on again?", "Hast du schon versucht, es aus- und wieder einzuschalten?"),
        ) {
            border(); floor()
            fill(1..20, 9..9)
            spawn(2, 8); door(7, 14)
        },

        // 40 — ping pong (a breather: U16 the lag roll, with a stalker). Short and mean: the door is at the far left, and as you pass the middle of the lane
        // a wall with spikes wakes up at the left end and starts for you, at the same moment the picture loses its vertical hold and rolls. Hop it as it
        // comes (ping), and it turns around and follows you to the door (pong)
        Level(
            name = T("Ping Pong", "Ping-Pong"),
            intro = T("Your ping is excellent. Truly.", "Dein Ping ist hervorragend. Wirklich."),
            legend = mapOf('S' to Glyph(spike = true, dir = Dir.RIGHT)),
            traps = listOf(
                trap(BeforeX(24f), Play(Card.STALKER), Chase('S', 3.5f, left = 0f, right = 22f), Roll(3.5f, 2), say("Lag spike. Keep your eyes on the level.", "Lag-Spitze. Behalt das Level im Kopf.")),
                trap(BeforeX(7f), say("Pong. Do keep going.", "Pong. Lauf ruhig weiter.")),
            ),
        ) {
            border(); floor()
            fill(1..1, 14..14, 'S')
            spawn(29, 14); door(2, 14)
        },
    )
}
