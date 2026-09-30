package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Ghost
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.PauseTrap
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Resumed
import com.robinrehbein.beveldevil.game.Trigger.Shaken
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 2, levels 33-48. Act 3, "Root": network mechanics combined, three meta twists in new disguises, one phone shake, and the exit to layer 3. */
object World2Part3 {
    private val hidden = Glyph(spike = true, hidden = true)
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)

    val levels: List<Level> = listOf(

        // 33 — EASTER EGG: sudo !! (repeat the last command, but as root)
        Level(
            name = T("sudo !!", "sudo !!"),
            intro = T("Not my day. Try again later.", "Nicht mein Tag. Versuch es später nochmal."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            traps = listOf(
                trap(PastX(9f), Play(Card.SPIKE_SEED), Show('A'), say("Command 1: one spike.", "Befehl 1: ein Spike.")),
                trap(PastX(17.5f), Show('B'), say("sudo !!  (three spikes, this time as root)", "sudo !!  (drei Spikes, diesmal als root)")),
                trap(PastX(24f), Fall('a'), say("sudo !!  (and the floor)", "sudo !!  (und der Boden)")),
            ),
        ) {
            border(); floor()
            put(12, 14, 'A')
            put(20, 14, 'B'); put(21, 14, 'B'); put(22, 14, 'B')
            fill(26..27, 15..17, 'a')
            spawn(); door(); bits(33)
        },

        // 34 — a floor portal drops you onto the ceiling, where gravity is now yours to flip
        Level(
            name = T("Reverse Proxy", "Reverse Proxy"),
            intro = T("Everything goes through me here. Everything.", "Hier läuft alles über mich. Alles."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Portal('1', 9 to 14, 16 to 1, twoWay = false),
                Portal('2', 26 to 1, 27 to 14, twoWay = false),
            ),
            traps = listOf(
                trap(Trigger.PastX(3.5f), Show('A'), say("Proxy cache: one spike, freshly served.", "Proxy-Cache: ein Spike, frisch serviert.")),
                trap(Zone(15.5f, 0.5f, 17.5f, 2.5f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Proxying your gravity.", "Deine Schwerkraft wird weitergeleitet.")),
                trap(Zone(26.5f, 12f, 29f, 15f), Gravity(false), say("Reverse, reverse.", "Rückwärts, rückwärts.")),
            ),
        ) {
            border(); floor()
            fill(12..13, 1..14)
            fill(19..20, 1..2); put(23, 1, 'v')
            put(6, 14, 'A')
            spawn(); door()
        },

        // 35 — a belt carries you through gates that open in a rhythm: you cannot stand and wait, only lean back against it
        Level(
            name = T("Pipeline", "Datenleitung"),
            intro = T("Line is clear. I checked.", "Leitung frei. Ich habe nachgesehen."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Belt('b', 5f),
                Laser('A', 9 to 1, 9 to 14, on = 1.2f, off = 1f),
                Laser('B', 15 to 1, 15 to 14, on = 1.2f, off = 1f, phase = 0.75f),
                Laser('C', 21 to 1, 21 to 14, on = 1.2f, off = 1f, phase = 1.5f),
            ),
            traps = listOf(
                trap(After(0.1f), Play(Card.CRUMBLE), say("Keep the packets moving.", "Halte die Pakete in Bewegung.")),
                trap(Trigger.PastX(15.8f), Belt('b', -6f), say("Backpressure: the line pushes back.", "Gegendruck: Die Leitung drückt zurück.")),
                trap(Trigger.PastX(21.8f), Show('A'), say("Last packet: a spike.", "Letztes Paket: ein Spike.")),
            ),
        ) {
            border(); floor()
            fill(1..27, 15..15, 'b')
            put(24, 14, 'A'); put(25, 14, 'A')
            spawn(); door()
        },

        // 36 — EASTER EGG: replay attack (your previous attempt is replayed: do not repeat yourself)
        Level(
            name = T("Access Log", "Zugriffsprotokoll"),
            intro = T("Nothing new here. Honestly.", "Nichts Neues hier. Ehrlich."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Laser('A', 10 to 1, 10 to 14, on = 2f, off = 2f),
                Laser('B', 20 to 1, 20 to 14, on = 2f, off = 2f, phase = 1.4f),
            ),
            traps = listOf(
                trap(PastX(6f), Play(Card.GHOST_BLOCK), Ghost(0.5f), say("Replay attack: I sent your last run again.", "Replay-Angriff: Ich habe deinen letzten Versuch nochmal gesendet.")),
                trap(PastX(22.2f), Show('A')),
            ),
        ) {
            border(); floor()
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door(); bits(36, x0 = 24, y = 1)
        },

        // 37 — EASTER EGG: two-factor authentication
        Level(
            name = T("Two-Factor Auth", "Zwei-Faktor-Login"),
            intro = T("Your code was sent by post. Arrival: soon.", "Dein Code wurde per Post verschickt. Ankunft: bald."),
            legend = mapOf('B' to hiddenSolid, 'A' to hidden),
            traps = listOf(
                trap(Touch('a'), Play(Card.GHOST_BLOCK), Show('B'), say("Factor 1 accepted. Now factor 2.", "Faktor 1 akzeptiert. Jetzt Faktor 2.")),
                trap(Touch('B'), Hide('w'), Saw(-1.5f, 14.4f, 6f, 0f), say("Code valid for 30 seconds. Actually 3.", "Code 30 Sekunden gültig. Eigentlich 3.")),
                trap(PastX(21f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(18..19, 1..14, 'w')
            fill(3..4, 13..14); fill(6..8, 11..11, 'a'); fill(10..12, 9..9, 'B')
            put(24, 14, 'A'); put(25, 14, 'A')
            spawn(); door(); bits(37, x0 = 24, y = 1)
        },

        // 38 — EASTER EGG: Bobby Tables (DROP TABLE floor: the hole has a wormhole at the bottom)
        Level(
            name = T("Bobby Tables", "Klein Bobby Tables"),
            intro = T("Welcome, Robert. Good to have you.", "Willkommen, Robert. Schön, dass du da bist."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Portal('1', 7 to 17, 24 to 3, twoWay = false),
                Portal('2', 8 to 17, 24 to 3, twoWay = false),
                Portal('3', 9 to 17, 24 to 3, twoWay = false),
            ),
            traps = listOf(
                trap(PastX(5.5f), Play(Card.COLLAPSE), Fall('a'), say("DROP TABLE floor; Did you sanitize your inputs?", "DROP TABLE floor; Hast du deine Eingaben bereinigt?")),
                trap(PastX(24.6f), Show('A')),
                trap(Landed(23f, 26f), Portal('4', 28 to 14, 5 to 14, twoWay = false), say("301: Bobby Tables moved. The door is behind a redirect.", "301: Bobby Tables ist umgezogen. Die Tür liegt hinter einer Weiterleitung.")),
            ),
        ) {
            border(); floor()
            fill(7..9, 15..16, 'a'); fill(7..9, 17..17, '.')
            leds(10..21)
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door()
        },

        // 39 — EASTER EGG: "Have you tried turning it off and on again?" (the firewall only goes down when you pause and resume)
        Level(
            name = T("Contingency Plan", "Notfallplan"),
            intro = T("The firewall is stuck. It'll open by itself any second now.", "Die Firewall klemmt. Die geht gleich von selbst auf."),
            start = listOf(Laser('L', 15 to 1, 15 to 14)),
            traps = listOf(
                trap(After(0.3f), Play(Card.DECOY), PauseTrap(PauseTrick.DODGE)),
                trap(Resumed(), Power('L', false), say("Session reset. The firewall forgot you.", "Sitzung zurückgesetzt. Die Firewall hat dich vergessen.")),
            ),
        ) {
            border(); floor()
            put(20, 14, '^'); put(21, 14, '^')
            spawn(); door(); bits(39)
        },

        // 40 — EASTER EGG: lag spike (the picture rolls while three gates run in a rhythm: a green wave, if you started on time)
        Level(
            name = T("Ping Pong", "Ping-Pong"),
            intro = T("Your ping is excellent. Truly.", "Dein Ping ist hervorragend. Wirklich."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Laser('A', 9 to 1, 9 to 14, on = 1.2f, off = 1.2f),
                Laser('B', 15 to 1, 15 to 14, on = 1.2f, off = 1.2f, phase = 1.6f),
                Laser('C', 21 to 1, 21 to 14, on = 1.2f, off = 1.2f, phase = 0.2f),
            ),
            traps = listOf(
                trap(PastX(6f), Play(Card.GHOST_BLOCK), Roll(3.5f, 2), say("Lag spike. Keep your eyes on the level.", "Lag-Spitze. Behalt das Level im Kopf.")),
                trap(PastX(22f), Show('A'), say("Pong: two spikes.", "Pong: zwei Spikes.")),
            ),
        ) {
            border(); floor()
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door(); bits(40, x0 = 24, y = 1)
        },

        // 41 — EASTER EGG: zero trust (never trust, always verify: every stone is a different lie)
        Level(
            name = T("Security Audit", "Sicherheitsaudit"),
            intro = T("Everything looks solid. I personally checked it all.", "Alles sieht solide aus. Ich habe persönlich nachgeprüft."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Stone 1: verified. Revoked.", "Stein 1: verifiziert. Widerrufen."), delay = 0.25f),
                trap(Touch('b'), Move('b', 0f, 12f, 9f), say("Stone 2: token expired.", "Stein 2: Token abgelaufen."), delay = 0.1f),
                trap(Touch('c'), Show('A'), say("Stone 3: certificate mismatch.", "Stein 3: Zertifikat passt nicht.")),
                trap(Touch('d'), Hide('d'), delay = 0.4f),
                trap(Touch('e'), Move('e', -3f, 0f, 6f), say("Stone 5: session hijacked.", "Stein 5: Sitzung entführt."), delay = 0.3f),
            ),
        ) {
            border()
            fill(0..5, 15..17); fill(27..31, 15..17)
            fill(7..8, 15..15, 'a'); fill(11..12, 14..14, 'b'); fill(15..17, 15..15, 'c'); fill(20..21, 15..15, 'd'); fill(24..25, 15..15, 'e')
            put(16, 14, 'A')
            spawn(); door(); bits(41)
        },

        // 42 — EASTER EGG: honeypot (the easy path is the trap)
        Level(
            name = T("Gold Mine", "Goldgrube"),
            intro = T("Take the easy way. You've earned it.", "Nimm den leichten Weg. Du hast ihn dir verdient."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(Touch('h'), Play(Card.SPIKE_SEED), Show('A'), say("Honeypot triggered. Intruder detected: you.", "Honeypot ausgelöst. Eindringling erkannt: du.")),
                trap(Touch('x'), Fall('x'), delay = 0.3f),
                trap(Touch('y'), Fall('y'), delay = 0.3f),
            ),
        ) {
            border(); floor()
            fill(10..11, 14..14, 'h')
            leds(13..22, c = 'A')
            fill(7..8, 13..14); fill(10..13, 11..11, 'x'); fill(16..19, 11..11, 'y')
            spawn(); door(); bits(42)
        },

        // 43 — EASTER EGG: percussive maintenance (shake the phone: the loose patch cable re-seats, the portal exit jumps
        // from the spike pit to the far side of the wall, and a laser guards the way to the door)
        Level(
            name = T("Workshop", "Werkstatt"),
            intro = T("I tried talking to it.", "Ich habe es mit Zureden versucht."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Portal('1', 10 to 14, 17 to 13, twoWay = false),
                Laser('G', 25 to 1, 25 to 14, on = 1f, off = 1.8f, phase = 1f),
            ),
            traps = listOf(
                trap(Shaken, Play(Card.HEADBUTT), Reroute('1', 21 to 14), Shake(1.2f), say("Works 90% of the time. Every time.", "Klappt in 90 % der Fälle. Jedes Mal.")),
                trap(Trigger.PastX(22f), Show('A'), say("Now it is screwed on properly.", "Jetzt ist es ordentlich festgeschraubt.")),
            ),
        ) {
            border(); floor()
            put(26, 14, 'A'); put(27, 14, 'A')
            fill(12..13, 1..14)
            fill(16..19, 14..14, '^')
            spawn(); door(29)
        },

        // 44 — EASTER EGG: merge conflict markers <<<<<<< ======= >>>>>>>
        Level(
            name = T("Rebase", "Rebase"),
            intro = T("<<<<<<< HEAD  ...  =======  ...  >>>>>>> feature", "<<<<<<< HEAD  ...  =======  ...  >>>>>>> feature"),
            traps = listOf(
                trap(PastX(16f), Play(Card.SINKING), Move('w', -7f, 0f, 6f), say("Automatic merge failed. Fix conflicts and try again.", "Automatischer Merge fehlgeschlagen. Konflikte lösen und nochmal versuchen.")),
                trap(PastX(24f), Fall('c'), say("Accept both changes? Bold.", "Beide Änderungen übernehmen? Mutig.")),
            ),
        ) {
            border(); floor()
            put(9, 14, '<'); put(9, 13, '<')
            rack(12, 2, 2)
            put(16, 14, '>'); put(16, 13, '>')
            fill(24..26, 13..14, 'w')
            fill(21..22, 4..5, 'c')
            spawn(); door(); bits(44)
        },

        // 45 — EASTER EGG: sandbox escape (the walls close in)
        Level(
            name = T("Playground", "Spielplatz"),
            intro = T("Nothing can happen to you in here, says the docs.", "Hier drin kann dir nichts passieren, sagt die Doku."),
            legend = mapOf('b' to ghost),
            traps = listOf(
                trap(After(2.2f), Play(Card.CRUMBLE), Move('l', 3f, 0f, 0.8f), Move('r', -3f, 0f, 0.8f), say("The sandbox is being garbage collected.", "Die Sandbox wird gerade aufgeräumt.")),
                trap(PastX(16f), Fall('c')),
            ),
        ) {
            border(); floor()
            fill(3..3, 12..14, 'l'); fill(10..10, 12..14, 'r'); put(6, 13, 'b')
            pit(14..15); fill(20..21, 3..4, 'c')
            spawn(6); door(); bits(45)
        },

        // 46 — EASTER EGG: privilege escalation (user, admin, root: a staircase of belts, each one faster and against you)
        Level(
            name = T("Privilege Escalation", "Rechteausweitung"),
            intro = T("I'm promoting you. All the way to the top.", "Ich befördere dich. Ganz nach oben."),
            start = listOf(Belt('a', -3f), Belt('b', -5f), Belt('c', -7f)),
            traps = listOf(
                trap(Touch('a'), Play(Card.CRUMBLE), say("user: permission denied.", "user: Zugriff verweigert.")),
                trap(Touch('b'), say("admin: sudo required.", "admin: sudo nötig.")),
                trap(Touch('c'), say("root: you shall not pass. (Jump.)", "root: Du kommst hier nicht durch. (Spring.)")),
            ),
        ) {
            border(); floor()
            fill(4..9, 14..14); fill(4..9, 13..13, 'a')
            fill(11..16, 12..14); fill(11..16, 11..11, 'b')
            fill(18..23, 10..14); fill(18..23, 9..9, 'c')
            fill(25..30, 7..14)
            put(10, 14, '^'); put(17, 14, '^'); put(24, 14, '^')
            spawn(); put(28, 6, 'D')
        },

        // 47 — EASTER EGG: integer overflow 127 + 1 (the LEDs wrap to 0000000) and kernel panic
        Level(
            name = T("Math Problem", "Rechenaufgabe"),
            intro = T("What's 127 plus 1? Take your time.", "Was ist 127 plus 1? Lass dir Zeit."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(3f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6.2f, 0f), say("Kernel panic! Attempted to kill init!", "Kernel Panic! Versuch, init zu beenden!")),
                trap(PastX(7.5f), Swap(true), say("127 + 1 = -128", "127 + 1 = -128")),
                trap(PastX(11f), Fall('a'), say("BUG: unable to handle kernel paging request", "BUG: Kernel-Paging-Anfrage nicht behandelbar")),
                trap(PastX(16f), Gravity(true), say("Call Trace: mephi_devil_flip+0x2a/0x80", "Call Trace: mephi_devil_flip+0x2a/0x80")),
                trap(PastX(22.5f), Swap(false)),
                trap(PastX(25.5f), Gravity(false), Show('A'), say("---[ end Kernel panic ]---", "---[ Ende Kernel Panic ]---")),
            ),
        ) {
            border(); floor()
            fill(12..14, 15..17, 'a')
            leds(17..24); put(19, 1, 'v'); put(21, 1, 'v')
            put(28, 14, 'A')
            spawn(); door(30); bits(128) // 127 + 1 wraps the LEDs to 0000000: nothing on the ceiling
        },

        // 48 — act and world finale: belt, tunnel, gate, swapped controls, and a door that moves down to layer 3
        Level(
            name = T("shutdown -h now", "shutdown -h now"),
            intro = T("Broadcast from mephi@hell: maintenance. Do not disturb.", "Rundruf von mephi@hoelle: Wartungsarbeiten. Bitte nicht stören."),
            start = listOf(
                Belt('a', -3f),
                Portal('1', 11 to 14, 15 to 14, twoWay = false),
                Laser('G', 18 to 1, 18 to 14, on = 1f, off = 1.8f, phase = 1f),
            ),
            traps = listOf(
                trap(PastX(3f), Play(Card.GRAND_FINALE), say("shutdown -h now. All services are stopping.", "shutdown -h now. Alle Dienste werden beendet.")),
                trap(PastX(20.5f), Swap(true), say("chown -R mephi /controls", "chown -R mephi /steuerung")),
                trap(PastX(25.5f), Swap(false), DoorTo(28, 16, speed = 20f), say("Layer 3: hardware. I'm moving out. Follow me if you dare.", "Schicht 3: Hardware. Ich ziehe aus. Komm nach, wenn du dich traust.")),
            ),
        ) {
            border(); floor()
            fill(3..10, 15..15, 'a')
            fill(12..13, 1..14)
            fill(27..29, 15..16, '.')
            put(7, 14, '^'); put(23, 14, '^'); put(24, 14, '^')
            spawn(); door(30)
        },
    )
}
