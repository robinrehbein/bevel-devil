package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Undo
import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Trigger.Pressed
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
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Action.Extend
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
                        trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, 12f, 9f), say("Stone 1: revoked in advance.", "Stein 1: vorab widerrufen."), delay = 0.05f),
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

        // 43 — workshop (a trap room: U16 percussive maintenance, shake the phone). The lane ends in a wall, and the only way on is the cable port at
        // the foot of it, which comes out above a bed of LEDs. A beam flashes across the lane as you come near (wait until it is dark). Shake the phone and
        // the loose cable re-seats: the port now comes out on the deck over the start, and the deck is a corridor of flashing beams on the way to the door
        Level(
            name = T("Workshop", "Werkstatt"),
            intro = T("I tried talking to it.", "Ich habe es mit Zureden versucht."),
            start = listOf(Portal('1', 10 to 14, 17 to 13, twoWay = false)),
            traps = listOf(
                trap(PastX(4f), Play(Card.SPIKE_SEED), Laser('A', 8 to 1, 8 to 14, on = 1f, off = 60f, delay = 0.55f), say("Clause 1: the beam has right of way.", "Paragraf 1: Der Strahl hat Vorfahrt.")),
                trap(Shaken, Reroute('1', 3 to 9), Shake(1.2f), say("Works 90% of the time. Every time.", "Klappt in 90 % der Fälle. Jedes Mal.")),
                trap(Zone(6f, 7f, 9f, 10.6f), Laser('B', 14 to 1, 14 to 9, on = 0.9f, off = 60f, delay = 0.5f), say("Now it is screwed on properly. Mostly.", "Jetzt ist es ordentlich festgeschraubt. Größtenteils.")),
                trap(Zone(18f, 7f, 22f, 10.6f), Laser('C', 27 to 1, 27 to 14, on = 0.9f, off = 60f, delay = 0.5f), say("One more screw. Stand clear.", "Noch eine Schraube. Bitte zurücktreten.")),
            ),
            hint = T("It has a loose cable. Have you tried hitting it?", "Da sitzt ein Kabel locker. Schon mal draufgehauen?"),
        ) {
            border(); floor()
            fill(12..13, 11..14); fill(1..25, 10..10)
            leds(16..19)
            spawn(2, 14); door(29, 14); bits(43)
        },

        // 44 — rebase (a trap room: U16 Ctrl+Z, with falling slabs). A low wall (hop it), then three slabs hang over the lane. The first drops out of the ceiling as you come
        // near (wait until it lands, hop the block). When you are past it Mephi hits undo: you are put back where you waited, standing, under the slab that
        // hung over your waiting place, and it drops half a second later (run!). Then the third slab, and the door. Rematch: the undo comes right before the
        // door and sends you back three seconds, to where you waited for the first slab: under the slab above it, with both blocks to hop again. Round 1's waiting places are wrong now
        Level(
            name = T("Rebase", "Rebase"),
            intro = T("<<<<<<< HEAD  ...  =======  ...  >>>>>>> feature", "<<<<<<< HEAD  ...  =======  ...  >>>>>>> feature"),
            traps = listOf(
                trap(PastX(8.5f), Play(Card.HEADBUTT), Fall('c'), say("Automatic merge failed. Fix conflicts and try again.", "Automatischer Merge fehlgeschlagen. Konflikte lösen und nochmal versuchen."), delay = 0.35f),
                trap(PastX(19f), Undo(2f), Fall('e'), say("git reset --hard HEAD~1. Conflict resolved. Mostly.", "git reset --hard HEAD~1. Konflikt gelöst. Größtenteils."), delay = 0.1f),
                trap(PastX(21f), Fall('d'), say("Accept both changes? Bold.", "Beide Änderungen übernehmen? Mutig."), delay = 0.22f),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. Force-pushed.", "Revanche. Force-Push."),
                    traps = listOf(
                        trap(PastX(7.5f), Fall('c'), say("Your history is mine now.", "Deine Historie gehört jetzt mir."), delay = 0.35f),
                        trap(PastX(16.5f), Fall('d'), say("Both changes were mine anyway.", "Beide Änderungen waren sowieso von mir."), delay = 0.35f),
                        trap(PastX(27f), Play(Card.UNDO), Undo(3.4f), Fall('e'), say("git push --force. Oops. Again.", "git push --force. Hoppla. Nochmal."), delay = 0.1f),
                    ),
                ) {
                    fill(14..16, 1..2, '.'); fill(24..26, 1..2, '.'); fill(8..10, 1..2, '.')
                    fill(12..14, 1..2, 'c'); fill(21..23, 1..2, 'd'); fill(7..9, 1..2, 'e')
                },
            ),
        ) {
            border(); floor()
            fill(4..5, 13..14)
            fill(14..16, 1..2, 'c'); fill(8..10, 1..2, 'e'); fill(24..26, 1..2, 'd')
            spawn(); door(); bits(44)
        },

        // 45 — playground (a puzzle room: R2 the hold switch, U3 the walls close in). You start in a crawl space under a roof, the walls come: one from the
        // left as you crawl, and the far end of the roof is a hatch of copper that is only open while you stand on the pad beneath it. Stand on the
        // pad, jump up through the hatch (the copper cannot close on you in the gap), and the other wall closes in below you. On the roof it is
        // your way back to the door, with a wall of its own behind you, and two steps up to the deck
        Level(
            name = T("Playground", "Spielplatz"),
            intro = T("Nothing can happen to you in here, says the docs.", "Hier drin kann dir nichts passieren, sagt die Doku."),
            start = listOf(Circuit('g'), Pad('1', at = 26 to 14, circuits = "g", mode = PadMode.HOLD), Belt('b', -3f)),
            traps = listOf(
                trap(PastX(10f), Play(Card.COLLAPSE), Move('s', 25f, 0f, 3.6f), say("The sandbox is being garbage collected.", "Die Sandbox wird gerade aufgeräumt.")),
                trap(Pressed('1'), Move('r', -8f, 0f, 4f), say("Stand still. The walls are only playing.", "Stillstehen. Die Wände spielen nur."), delay = 0.2f),
                trap(Landed(19f, 26.5f), Move('y', -22f, 0f, 3.0f), say("Playtime is over. Up the steps.", "Die Pause ist vorbei. Die Stufen hoch.")),
            ),
            hint = T("The copper only lets you out while you stand on the pad. Jump from it.", "Das Kupfer lässt dich nur raus, solange du auf dem Knopf stehst. Spring von ihm."),
        ) {
            border(); floor()
            fill(3..22, 13..13); fill(23..26, 13..13, 'g')
            fill(7..18, 15..15, 'b'); fill(2..2, 13..14, 's'); fill(27..27, 13..14, 'r'); fill(27..27, 11..12, 'y')
            fill(3..9, 9..12); fill(10..12, 10..12); fill(13..15, 11..12); fill(16..17, 12..12)
            spawn(4, 14); door(4, 8); bits(45)
        },

        // 46 — privilege escalation (a puzzle room: R10 the transport, U12 the belt turns around). A staircase of three belts over beds of LEDs: user,
        // admin, root. Each belt is calm until you step on it, then it runs against you, a little faster every step, so there is no standing on any of
        // them, and the top is a plank bridge over the LEDs that drops out a while after you land on it. Rematch: demoted, the belts run harder
        // against you, and the bridge at the top is a belt too, far too fast to walk: it has to be hopped
        Level(
            name = T("Privilege Escalation", "Rechteausweitung"),
            intro = T("I'm promoting you. All the way to the top.", "Ich befördere dich. Ganz nach oben."),
            start = listOf(Belt('a', 0f), Belt('b', -3f), Belt('c', -3.5f), Belt('p', 0f)),
            traps = listOf(
                trap(Touch('a'), Belt('a', -5f), say("user: permission denied.", "user: Zugriff verweigert."), delay = 0.1f),
                trap(Touch('b'), Belt('b', -6.5f), say("admin: sudo required.", "admin: sudo nötig."), delay = 0.1f),
                trap(Touch('c'), Belt('c', -7f), say("root: you shall not pass. (Jump.)", "root: Du kommst hier nicht durch. (Spring.)"), delay = 0.1f),
                trap(Touch('p'), Belt('p', -6f), say("root: the last step is a belt, too.", "root: Die letzte Stufe ist auch ein Band."), delay = 0.1f),
                trap(Landed(27f, 30f), Play(Card.CRUMBLE), Fall('p'), say("sudo: the bridge is not in the sudoers file.", "sudo: Die Brücke steht nicht in der sudoers-Datei."), delay = 0.9f),
            ),
            rematch = listOf(
                Round(
                    T("Demoted. Climb again, intern.", "Zurückgestuft. Die Leiter läuft jetzt gegen dich."),
                    start = listOf(Belt('a', 0f), Belt('b', -3.5f), Belt('c', -4f), Belt('p', -3f)),
                    traps = listOf(
                        trap(Touch('a'), Belt('a', -5.2f), say("intern: please hurry.", "Praktikant: Kaffee holen, aber zackig."), delay = 0.1f),
                        trap(Touch('b'), Belt('b', -6.8f), say("Fast track. Mind the gap.", "Überholspur. Lücke beachten, bitte."), delay = 0.1f),
                        trap(Touch('c'), Belt('c', -7f), say("Please hold the handrail. There is none.", "Bitte am Geländer festhalten. Es gibt keins."), delay = 0.1f),
                        trap(Touch('p'), Belt('p', -9.5f), say("Last step: the handrail is the floor.", "Letzte Stufe: Das Geländer ist der Boden."), delay = 0.1f),
                        trap(Landed(27f, 30f), Play(Card.HEADBUTT), Fall('p'), say("Same-day delivery, by gravity.", "Zustellung am selben Tag, per Schwerkraft."), delay = 0.9f),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(4..8, 14..14, 'a')
            fill(11..15, 14..14); fill(11..15, 13..13, 'b')
            fill(19..23, 13..14); fill(19..23, 12..12, 'c')
            fill(27..30, 11..11, 'p')
            leds(9..10); leds(16..18); leds(24..30)
            spawn(); put(29, 10, 'D'); bits(46)
        },

        // 47 — math problem (a puzzle room: R5 two floors, U9 the controls swap). 127 + 1 wraps around: a cart rolls at you along the lane, a second,
        // faster one starts behind you, and the steps at the end lead up to a deck over the lane, (the top step: the integer overflows and left is right).
        // The way to the door is back along the deck with swapped hands, and a cart comes at you there as well
        Level(
            name = T("Math Problem", "Rechenaufgabe"),
            intro = T("What's 127 plus 1? Take your time.", "Was ist 127 plus 1? Lass dir Zeit."),
            traps = listOf(
                trap(PastX(6f), Saw(32f, 14.4f, -6f, 0f), say("127 carts in the queue. One is yours.", "127 Loren in der Schlange. Eine ist deine.")),
                trap(PastX(24f), Saw(-1.5f, 14.4f, 12f, 0f), say("Integer overflow: the next one comes from behind.", "Ganzzahlüberlauf: Die nächste kommt von hinten.")),
                trap(Landed(21f, 24.9f), Play(Card.TWISTED), Swap(true), say("127 + 1 = -128. Left is right now.", "127 + 1 = -128. Links ist jetzt rechts.")),
                trap(Zone(15f, 8f, 18f, 10.5f), Saw(-1.5f, 9.4f, 6f, 0f), say("Signed or unsigned? Neither, it is a saw.", "Mit oder ohne Vorzeichen? Weder noch, es ist eine Säge.")),
            ),
        ) {
            border(); floor()
            fill(25..26, 14..14); fill(27..30, 12..14); fill(1..26, 10..10)
            spawn(); door(3, 9); bits(47)
        },

        // 48 — shutdown, the act and world finale, two rooms (R4 the switch, R3 the portal routing; U11 the route is re-pointed, U9 the controls swap,
        // U18 the room goes on). The only portal on the lane leads home until a pad on the step behind the start re-points it onto the deck over the wall, where a
        // firewall flashes and a tripwire waits in front of the door. At the door the wall breaks open (and the controls swap) and the door slips into the second room: there
        // a cart comes at you, and the door sinks through the floor to layer 3
        Level(
            name = T("shutdown -h now", "shutdown -h now"),
            intro = T("Broadcast from mephi@hell: maintenance. Do not disturb.", "Rundruf von mephi@hoelle: Wartungsarbeiten. Bitte nicht stören."),
            rooms = 2,
            start = listOf(
                Portal('2', 9 to 14, 5 to 14, twoWay = false),
                Pad('1', at = 2 to 13, circuits = "", mode = PadMode.TOGGLE),
            ),
            traps = listOf(
                trap(Pressed('1'), Reroute('2', 16 to 9), say("DNS updated. Propagation: instant. Reality: pending.", "DNS aktualisiert. Weitergabe: sofort. Realität: ausstehend.")),
                trap(Zone(15f, 7f, 17.5f, 10.6f), Laser('G', 21 to 1, 21 to 9, on = 1.2f, off = 60f, delay = 0.5f), say("Firewall rule 1: nobody gets out.", "Firewall-Regel 1: Keiner kommt raus.")),
                trap(Zone(22f, 7f, 23.5f, 10.6f), Laser('H', 25 to 9, 26 to 9, on = 2.5f, off = 60f, delay = 0.4f), say("Tripwire: it only checks your feet.", "Stolperdraht: Er prüft nur deine Füße.")),
                trap(Landed(26.5f, 29.8f), Swap(true), say("chown -R mephi /controls", "chown -R mephi /steuerung")),
                trap(AtDoor, Play(Card.GRAND_FINALE),
                    Extend(into = 1, top = 7, bottom = 9, door = roomX(1, 28) to 14, line = T("shutdown -h now. Who said the room ends here?", "shutdown -h now. Wer sagt, dass der Raum hier endet?"))),
                trap(Landed(roomX(1, 9f), roomX(1, 16f)), Saw(roomX(1, 32f), 14.4f, -8f, 0f), say("Last cron job: unplug the customer.", "Letzter Cronjob: Den Kunden abstecken.")),
                trap(Landed(roomX(1, 9f), roomX(1, 16f)), Laser('K', roomX(1, 27) to 1, roomX(1, 27) to 14, on = 2.4f, off = 60f, delay = 0.1f), say("Firewall rule 2: and nobody gets in.", "Firewall-Regel 2: Und keiner kommt rein.")),
                trap(PastX(roomX(1, 25.5f)), DoorTo(roomX(1, 29), 16, speed = 20f), say("Layer 3: hardware. I'm moving out. Follow me if you dare.", "Schicht 3: Hardware. Ich ziehe aus. Komm nach, wenn du dich traust.")),
            ),
        ) {
            border(); floor()
            room(0) {
                put(8, 14, 'P'); fill(1..3, 14..14)
                fill(12..13, 1..14)
                fill(15..30, 10..10)
                leds(17..30)
                put(29, 9, 'D'); bits(48)
            }
            room(1) {
                fill(1..8, 10..10)
                pit(28..30, 15)
                fill(28..30, 17..17)
            }
        },
    )

}
