package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Undo
import com.robinrehbein.beveldevil.game.Action.Bluff
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
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Resumed
import com.robinrehbein.beveldevil.game.Trigger.Shaken
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 2, levels 33-48. Act 3, "Root": network mechanics combined, three meta twists in new disguises, one phone shake, and the exit to layer 3. Levels 33-40 are in [World2Part3C]. */
object World2Part3D {
    private val hidden = Glyph(spike = true, hidden = true)
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)

    val levels: List<Level> = listOf(

        // 41 — security audit (a trap room: U1 the floor, zero trust: every stone is a different lie). Three stones lie over the pit: the first
        // falls a moment after you step on it, the second drops at once (hop it), the third vanishes under whoever crosses it. On the far side the
        // steps lie as well, and the way to the door is back along a deck over the pit: a plank falls out of it ahead of you as you come, and as you
        // land behind the hole the next one is revoked. Rematch: the stones swap their lies (the first drops at once, the second is honest now)
        Level(
            name = T("Security Audit", "Sicherheitsaudit"),
            intro = T("Everything looks solid. I personally checked it all.", "Alles sieht solide aus. Ich habe persönlich nachgeprüft."),
            traps = listOf(
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Stone 1: verified. Revoked.", "Stein 1: verifiziert. Widerrufen."), delay = 0.55f),
                trap(Touch('b'), Move('b', 0f, 12f, 9f), say("Stone 2: token expired.", "Stein 2: Token abgelaufen."), delay = 0.08f),
                trap(Touch('c'), Hide('c'), say("Stone 3: certificate mismatch.", "Stein 3: Zertifikat passt nicht."), delay = 0.3f),
                trap(Landed(25f, 28f), Hide('s'), say("Step 4: not on the list.", "Stufe 4: nicht auf der Liste."), delay = 0.7f),
                trap(Zone(20.5f, 8f, 26.5f, 10.6f), Fall('p'), say("Plank 5: out of scope.", "Planke 5: außerhalb des Prüfbereichs.")),
                trap(Touch('t'), Hide('q'), say("Plank 6: who audits the auditors?", "Planke 6: Wer prüft die Prüfer?"), delay = 0.2f),
            ),
            // rematch: re-audit. The same stones, but the lies are shuffled: the first one drops at once now, so round 1's run over it ends in the
            // pit, and the second one is honest. Sixth lie: the deck's far end is a step that vanishes under you, so the ascent goes over it
            rematch = listOf(
                Round(
                    T("Audit failed. Re-audit. Same stones.", "Audit durchgefallen. Nachprüfung. Gleiche Steine."),
                    traps = listOf(
                        trap(Touch('a'), Move('a', 0f, 12f, 9f), say("Stone 1: revoked in advance.", "Stein 1: vorab widerrufen."), delay = 0.05f),
                        trap(Touch('c'), Hide('c'), say("Stone 3: still not trusted.", "Stein 3: weiterhin nicht vertrauenswürdig."), delay = 0.3f),
                        trap(Landed(25f, 28f), Hide('s'), say("Step 4: audit trail missing.", "Stufe 4: Prüfpfad fehlt."), delay = 0.7f),
                        trap(Zone(20.5f, 8f, 26.5f, 10.6f), Fall('p'), say("Plank 5: re-scoped.", "Planke 5: neu zugeschnitten.")),
                        trap(Touch('t'), Hide('q'), say("Plank 6: the audit was the exploit.", "Planke 6: Das Audit war der Exploit."), delay = 0.2f),
                    ),
                ),
            ),
        ) {
            border()
            fill(0..5, 15..17); fill(17..30, 15..17)
            fill(6..8, 15..15, 'a'); fill(9..11, 15..15, 'b'); fill(12..14, 15..15, 'c')
            fill(1..27, 10..10); fill(15..17, 10..10, 'p'); fill(10..12, 10..10, 'q'); fill(13..14, 10..10, 't')
            fill(25..27, 14..14, 's'); fill(28..30, 12..14)
            spawn(2, 14); door(2, 9); bits(41)
        },

        // 42 — gold mine (a puzzle room: R5 two floors, R7 the bait; U15 the easy way is the trap). You start on the deck over the lane, the door is
        // below you on the lane, and the hole in the deck right next to the start is the shortcut: spikes grow where you land in it. The way is the
        // long one: along the deck (a cart rolls at you), off its end, and back along the lane (a cart rolls out of the mine from the left) over the gold
        // pile, which is the one place to stand while it passes. Rematch: the gold is the honeypot now and gives way under you
        Level(
            name = T("Gold Mine", "Goldgrube"),
            intro = T("Take the easy way. You've earned it.", "Nimm den leichten Weg. Du hast ihn dir verdient."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(Landed(6f, 8.9f), Show('A'), say("Honeypot triggered. Intruder detected: you.", "Honeypot ausgelöst. Eindringling erkannt: du.")),
                trap(PastX(11f), Play(Card.DEVIL_SAW), Saw(26f, 9.4f, -5.5f, 0f), say("Cart 1: ore on the way out.", "Lore 1: Erz auf dem Weg nach draußen.")),
                trap(Landed(27.5f, 31f), Saw(-1.5f, 14.4f, 7f, 0f), say("Cart 2: it knows the mine better than you.", "Lore 2: Sie kennt die Grube besser als du.")),
                trap(Zone(8.5f, 12f, 9.9f, 15.5f), Saw(32f, 14.4f, -9f, 0f), say("Cart 3: express to the exit.", "Lore 3: Express zum Ausgang.")),
            ),
            rematch = listOf(
                Round(
                    T("Honeypot refilled. Hole patched. Help yourself.", "Honeypot nachgefüllt. Loch geflickt. Bedien dich."),
                    traps = listOf(
                        trap(Touch('h'), say("Gold touched. Nobody minds. Yet.", "Gold berührt. Keinen stört's. Noch.")),
                        trap(PastX(11f), Saw(26f, 9.4f, -5.5f, 0f), say("The cart is early. Mind the gold.", "Die Lore ist früh dran. Achtung, das Gold.")),
                        trap(Landed(27.5f, 31f), Saw(-1.5f, 14.4f, 5f, 0f), say("Second cart. Same mine.", "Zweite Lore. Gleiche Grube.")),
                        trap(Touch('i'), Play(Card.COLLAPSE), Hide('i'), say("All that glitters is a trapdoor.", "Es ist nicht alles Gold, was glänzt, manches ist eine Falltür."), delay = 0.3f),
                        trap(Zone(8.5f, 12f, 9.9f, 15.5f), Saw(32f, 14.4f, -9f, 0f), say("Third cart. No parking.", "Dritte Lore. Parken verboten.")),
                    ),
                ) { fill(12..15, 13..13, 'i'); fill(13..14, 12..12, 'i'); fill(7..8, 10..10); fill(6..9, 14..14, '.') },
            ),
        ) {
            border(); floor()
            fill(1..6, 10..10); fill(9..27, 10..10)
            fill(11..16, 14..14); fill(12..15, 13..13)
            leds(6..9, c = 'A')
            put(3, 9, 'P'); door(2, 14); bits(42)
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
            // rematch: the wall stays, the block holds; right before the door Mephi hits Ctrl+Z and you do it all again
            rematch = listOf(
                Round(
                    T("Rematch. Force-pushed.", "Revanche. Force-Push."),
                    traps = listOf(
                        trap(PastX(25.5f), Play(Card.UNDO), Undo(1.6f), say("git reset --hard HEAD~1", "git reset --hard HEAD~1")),
                    ),
                ),
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
            // rematch: demoted, the belts run with you now and throw you at the next step's spikes
            rematch = listOf(
                Round(
                    T("Demoted. Climb again, intern.", "Zurückgestuft. Die Leiter läuft jetzt mit."),
                    start = listOf(Belt('a', 3f), Belt('b', 5f), Belt('c', 7f)),
                    traps = listOf(
                        trap(Touch('a'), Play(Card.TWISTED), Belt('a', 4f), say("intern: please hurry.", "Praktikant: Kaffee holen, aber zackig.")),
                        trap(Touch('b'), say("Fast track. Mind the gap.", "Überholspur. Lücke beachten, bitte.")),
                        trap(Touch('c'), say("root: express delivery.", "root: Same-Day-Delivery.")),
                    ),
                ),
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
