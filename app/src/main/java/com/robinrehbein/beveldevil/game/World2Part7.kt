package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

private val hidden = Glyph(spike = true, hidden = true)
private val hiddenSolid = Glyph(spike = false, hidden = true)
private val ghost = Glyph(spike = false, hidden = true, bonk = true)

/** World 2, levels 97-112: "Security". */
internal val world2Part7: List<Level> = listOf(
    // 97 — EASTER EGG: phishing ("your door has expired, click here")
    Level(
        name = T("Phishing", "Phishing"),
        intro = T("Dear user, your door has expired. Click here to renew.", "Sehr geehrter Nutzer, Ihre Tür ist abgelaufen. Hier klicken zur Verlängerung."),
        traps = listOf(
            trap(PastX(22f), Fall('a'), Gravity(true), say("You clicked the link. The floor is gone.", "Du hast auf den Link geklickt. Der Boden ist weg.")),
        ) + doorTrail(
            PastX(22f), 28, 8,
            listOf(DoorTo(30, 4, 22f), DoorTo(3, 4, 22f), DoorTo(3, 1, 22f, hanging = true)),
            first = listOf(Play(Card.UPSIDE_DOWN)),
        ),
    ) {
        border()
        fill(1..19, 9..9); fill(20..30, 9..9, 'a')
        fill(1..30, 15..17); leds(8..16)
        put(12, 1, 'v'); put(18, 1, 'v')
        put(2, 8, 'P'); put(28, 8, 'D')
    },

    // 98 — EASTER EGG: two-factor authentication
    Level(
        name = T("Two-Factor Authentication", "Zwei-Faktor-Authentifizierung"),
        intro = T("Please enter the code we just sent to your other door.", "Bitte gib den Code ein, den wir an deine andere Tür gesendet haben."),
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
        spawn(); door(); bits(98, x0 = 24, y = 1)
    },

    // 99 — EASTER EGG: honeypot (the easy path is the trap)
    Level(
        name = T("Honeypot", "Honeypot"),
        intro = T("Look, a shiny block! Step on it. Please.", "Schau, ein glänzender Block! Tritt drauf. Bitte."),
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
        spawn(); door(); bits(99)
    },

    // 100 — EASTER EGG: level 100 = 0b1100100 (the ceiling shows it, and blocks fall for every 1)
    Level(
        name = T("0b1100100", "0b1100100"),
        intro = T("Level 100. In binary. Because I can.", "Level 100. Binär. Weil ich es kann."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(5.5f), Play(Card.GRAND_FINALE), Fall('c'), say("1", "1")),
            trap(PastX(9.5f), Fall('d'), say("1", "1")),
            trap(PastX(12.5f), Swap(true), say("0", "0")),
            trap(PastX(17f), say("0", "0")),
            trap(PastX(18.5f), Fall('e'), say("1", "1")),
            trap(PastX(23f), Swap(false), Show('A'), say("0 0. Done: 100.", "0 0. Fertig: 100.")),
        ),
    ) {
        border(); floor()
        fill(8..9, 3..4, 'c'); fill(12..13, 3..4, 'd'); fill(21..22, 3..4, 'e')
        pit(15..16); put(26, 14, 'A'); put(27, 14, 'A')
        spawn(); door(30); bits(100)
    },

    // 101 — EASTER EGG: password hunter2 (seven stars, seven spikes)
    Level(
        name = T("hunter2", "hunter2"),
        intro = T("Password: ******* (I see it clearly. It's hunter2.)", "Passwort: ******* (Ich sehe es klar. Es ist hunter2.)"),
        legend = ('H'..'N').associateWith { hidden },
        traps = ('H'..'N').mapIndexed { i, g ->
            val x = 8 + i * 3
            trap(PastX(x - 3.4f), *(if (i == 0) arrayOf(Play(Card.SPIKE_SEED), say("h  u  n  t  e  r  2", "h  u  n  t  e  r  2")) else emptyArray()), Show(g))
        },
    ) {
        border(); floor()
        for ((i, g) in ('H'..'N').withIndex()) put(8 + i * 3, 14, g)
        spawn(); door(30); bits(101, x0 = 24, y = 1)
    },

    // 102 — EASTER EGG: zero trust (never trust, always verify: every stone is a different lie)
    Level(
        name = T("Zero Trust", "Zero Trust"),
        intro = T("Never trust, always verify. Especially floors.", "Niemals vertrauen, immer prüfen. Vor allem Böden."),
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
        spawn(); door(); bits(102)
    },

    // 103 — EASTER EGG: Trojan horse (the horse is full of saws)
    Level(
        name = T("Trojan Horse", "Trojanisches Pferd"),
        intro = T("A gift! It's a very nice, very hollow horse.", "Ein Geschenk! Ein sehr schönes, sehr hohles Pferd."),
        traps = listOf(
            trap(Touch('h'), Play(Card.DEVIL_SAW), Saw(15f, 10.7f, -5f, 0f), say("Attention, Greeks: inside the horse.", "Achtung, Griechen: im Pferd.")),
            trap(Touch('h'), Saw(15f, 10.7f, 5f, 0f), delay = 0.1f),
            trap(Touch('h'), Fall('h'), delay = 1.5f),
        ),
    ) {
        border(); floor()
        fill(9..10, 13..14)
        fill(12..18, 11..12, 'h'); fill(13..13, 13..14, 'h'); fill(17..17, 13..14, 'h'); fill(19..20, 9..10, 'h')
        spawn(); door(); bits(103, x0 = 24, y = 1)
    },

    // 104 — EASTER EGG: rootkit (it hides the real door from the very first second)
    Level(
        name = T("Rootkit", "Rootkit"),
        intro = T("Nothing to see here. Especially not the door.", "Hier gibt es nichts zu sehen. Erst recht keine Tür."),
        traps = listOf(
            trap(Touch('a'), Fall('a'), delay = 0.3f),
        ) + doorTrail(
            After(0.4f), 29, 14,
            listOf(DoorTo(29, 1, 30f, hanging = true), DoorTo(5, 1, 30f, hanging = true), DoorTo(5, 6, 30f)),
            first = listOf(Play(Card.SHY_DOOR), say("rootkit: process 'door' hidden.", "rootkit: Prozess 'tuer' versteckt.")),
        ),
    ) {
        border(); floor()
        fill(11..12, 13..14); fill(9..10, 11..11, 'a'); fill(7..8, 9..9, 'c'); fill(4..6, 7..7)
        spawn(16); door()
    },

    // 105 — EASTER EGG: keylogger (the controls are being watched, and flip on a timer)
    Level(
        name = T("Keylogger", "Keylogger"),
        intro = T("I log your keys. And swap them, for fun.", "Ich protokolliere deine Tasten. Und tausche sie, zum Spaß."),
        legend = mapOf('b' to ghost, 'A' to hidden),
        traps = listOf(
            trap(After(2.4f), Play(Card.TWISTED), Swap(true), say("Keys logged: L, R, L, R, jump.", "Tasten protokolliert: L, R, L, R, Sprung.")),
            trap(After(3.6f), Swap(false)),
            trap(After(4.8f), Swap(true)),
            trap(After(6.0f), Swap(false)),
            trap(After(7.2f), Swap(true)),
            trap(After(8.4f), Swap(false)),
            trap(PastX(17f), Show('A')),
        ),
    ) {
        border(); floor()
        fill(8..9, 12..14); put(6, 13, 'b')
        pit(13..14); put(19, 14, 'A'); put(20, 14, 'A'); pit(23..24)
        spawn(); door(); bits(105)
    },

    // 106 — EASTER EGG: botnet (a million zombie machines)
    Level(
        name = T("Botnet", "Botnet"),
        intro = T("1,000,000 zombie machines. All of them like you.", "1.000.000 Zombie-Rechner. Alle mögen dich."),
        traps = listOf(
            trap(PastX(4f), Play(Card.DEVIL_SAW), Saw(33.5f, 14.4f, -6.5f, 0f), say("Bot 1 of 1,000,000 reporting.", "Bot 1 von 1.000.000 meldet sich.")),
            trap(PastX(4f), Saw(11f, -0.5f, 0f, 9f), delay = 0.5f),
            trap(PastX(4f), Saw(33.5f, 14.4f, -6.5f, 0f), delay = 1.4f),
            trap(PastX(4f), Saw(17f, -0.5f, 0f, 9f), delay = 1.2f),
            trap(PastX(4f), Saw(23f, -0.5f, 0f, 9f), delay = 2.0f),
            trap(PastX(4f), Saw(-1.5f, 14.4f, 6.5f, 0f), delay = 3.0f),
            trap(PastX(4f), Saw(28f, -0.5f, 0f, 9f), delay = 2.6f),
        ),
    ) {
        border(); floor()
        fill(9..13, 1..2); fill(15..19, 1..2); fill(21..25, 1..2); fill(26..30, 1..2)
        rack(14, 1, 2); rack(21, 1, 2)
        spawn(); door()
    },

    // 107 — EASTER EGG: backdoor (the way in is behind you)
    Level(
        name = T("Backdoor", "Hintertür"),
        intro = T("The front door is locked. The back door is behind you.", "Die Vordertür ist zu. Die Hintertür ist hinter dir."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('t'), Play(Card.GHOST_BLOCK), Hide('w'), Saw(33.5f, 14.4f, -6f, 0f), say("Backdoor opened. (It was always open.)", "Hintertür geöffnet. (War immer offen.)")),
            trap(PastX(18f), Show('A')),
        ),
    ) {
        border(); floor()
        put(1, 14, 't')
        fill(22..23, 11..14, 'w')
        put(26, 14, 'A'); put(27, 14, 'A')
        spawn(6); door(); bits(107)
    },

    // 108 — EASTER EGG: sandbox escape (the walls close in)
    Level(
        name = T("Sandbox Escape", "Sandbox-Ausbruch"),
        intro = T("You are safely isolated in a sandbox. Forever.", "Du bist sicher in einer Sandbox isoliert. Für immer."),
        legend = mapOf('b' to ghost),
        traps = listOf(
            trap(After(2.2f), Play(Card.SINKING), Move('l', 3f, 0f, 0.8f), Move('r', -3f, 0f, 0.8f), say("The sandbox is being garbage collected.", "Die Sandbox wird gerade aufgeräumt.")),
            trap(PastX(16f), Fall('c')),
        ),
    ) {
        border(); floor()
        fill(3..3, 12..14, 'l'); fill(10..10, 12..14, 'r'); put(6, 13, 'b')
        pit(14..15); fill(20..21, 3..4, 'c')
        spawn(6); door(); bits(108)
    },

    // 109 — EASTER EGG: ROT13 (the ferry moves 13 tiles)
    Level(
        name = T("ROT13", "ROT13"),
        intro = T("Apply twice to get back where you started.", "Zweimal anwenden, um wieder am Anfang zu sein."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.SINKING), Move('a', 13f, 0f, 5f), say("Rotating by 13 places.", "Rotiere um 13 Stellen."), delay = 0.3f),
            trap(After(0.3f), Saw(11.5f, -0.5f, 0f, 10f)),
            trap(After(1.15f), Saw(16f, -0.5f, 0f, 10f)),
            trap(PastX(21f), Show('A')),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(23..31, 15..17)
        fill(6..8, 15..15, 'a')
        put(25, 14, 'A')
        spawn(); door(); bits(109, x0 = 24, y = 1)
    },

    // 110 — EASTER EGG: Base64 (the padding is ==)
    Level(
        name = T("Base64 Padding ==", "Base64-Padding =="),
        intro = T("Encoded in 4 tiles per 3 bytes. Padded with ==.", "Kodiert in 4 Kacheln pro 3 Bytes. Aufgefüllt mit ==."),
        traps = listOf(
            trap(After(0.6f), Play(Card.SINKING), say("Decoding platform layout...", "Dekodiere Plattform-Layout...")),
        ) + shuttle('a', 0.6f, 3.2f, 0f, 2.2f, 8, dx = 4f) + shuttle('b', 2.0f, 3.2f, 0f, 1.6f, 8, dx = 3f),
    ) {
        border()
        fill(0..5, 15..17); fill(24..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(16..18, 15..15, 'b')
        rack(27, 2, 1)
        spawn(); door(30)
    },

    // 111 — EASTER EGG: hash collision (two traps with the same input)
    Level(
        name = T("Hash Collision", "Hash-Kollision"),
        intro = T("Two different inputs. Same trigger. Same disaster.", "Zwei verschiedene Eingaben. Gleicher Auslöser. Gleiche Katastrophe."),
        traps = listOf(
            trap(PastX(16f), Play(Card.UPSIDE_DOWN), Gravity(true), say("md5(input_a) == md5(input_b)", "md5(eingabe_a) == md5(eingabe_b)")),
            trap(PastX(16f), Swap(true)),
            trap(PastX(24f), Gravity(false), say("Collision resolved. Badly.", "Kollision aufgelöst. Schlecht.")),
            trap(PastX(24f), Swap(false)),
        ),
    ) {
        border(); floor()
        pit(10..11)
        leds(17..23); put(19, 1, 'v'); put(21, 1, 'v')
        spawn(); door(30)
    },

    // 112 — EASTER EGG: root access (sudo su; act finale)
    Level(
        name = T("Root Access", "Root-Zugriff"),
        intro = T("root@hell:~# whoami  ->  Mephi", "root@hoelle:~# whoami  ->  Mephi"),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(3f), Play(Card.GRAND_FINALE), Saw(-1.5f, 14.4f, 6.5f, 0f), say("sudo su. Password accepted (it was 'password').", "sudo su. Passwort akzeptiert (es war 'passwort').")),
            trap(PastX(8f), Swap(true), say("chown -R mephi:mephi /controls", "chown -R mephi:mephi /steuerung")),
            trap(PastX(12f), Fall('a')),
            trap(PastX(17f), Gravity(true), say("mount --bind /ceiling /floor", "mount --bind /decke /boden")),
            trap(PastX(23f), Swap(false)),
            trap(PastX(26f), Gravity(false), Show('A'), say("rm -rf /player", "rm -rf /spieler")),
        ),
    ) {
        border(); floor()
        fill(13..15, 15..17, 'a')
        leds(19..24); put(20, 1, 'v'); put(22, 1, 'v')
        put(28, 14, 'A')
        spawn(); door(30)
    },
)
