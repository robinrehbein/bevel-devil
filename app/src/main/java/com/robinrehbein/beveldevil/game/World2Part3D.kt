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
import com.robinrehbein.beveldevil.game.Action.PathSaw
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
        // land behind the hole the next one is revoked. Rematch: every lie is shuffled (the first stone drops at once, the second sinks slowly
        // under whoever stands on it, step 4 vanishes at once)
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
            // pit, the second one sinks slowly (cross it, don't stop), and step 4 vanishes the moment you land on it (jump on at once)
            rematch = listOf(
                Round(
                    T("Audit failed. Re-audit. Same stones.", "Audit durchgefallen. Nachprüfung. Gleiche Steine."),
                    traps = listOf(
                        trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Stone 1: revoked in advance.", "Stein 1: vorab widerrufen."), delay = 0.05f),
                        trap(Touch('b'), Move('b', 0f, 12f, 2.5f), say("Stone 2: trusted. Slowly less so.", "Stein 2: vertrauenswürdig. Langsam immer weniger."), delay = 0.4f),
                        trap(Touch('c'), Hide('c'), say("Stone 3: still not trusted. Just later.", "Stein 3: weiterhin nicht vertrauenswürdig. Nur später."), delay = 0.37f),
                        trap(Landed(25f, 28f), Hide('s'), say("Step 4: audit trail missing. So is the step.", "Stufe 4: Prüfpfad fehlt. Die Stufe auch."), delay = 0.3f),
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
        // on the lane a little further on, and the hole in the deck right next to the start is the shortcut: the copper where you land in it is a
        // dead trace that goes live under you. The way is the long one: along the deck (an ore bucket swings over it on a rope: stop, slip under
        // it; a rock drops behind you further on), off its end, and back along the lane (a cart rolls out of the mine from the left) over the gold
        // pile, which is the one place to stand while it passes, and to the door before the express cart. Rematch: the gold is the honeypot now:
        // it is a copper rail, and its power is cut under you
        Level(
            name = T("Gold Mine", "Goldgrube"),
            intro = T("Take the easy way. You've earned it.", "Nimm den leichten Weg. Du hast ihn dir verdient."),
            start = listOf(Circuit('A', on = false)),
            traps = listOf(
                trap(Zone(5.5f, 12.5f, 12.9f, 15.5f), Power('A', true), say("Honeypot triggered. Intruder detected: you.", "Honeypot ausgelöst. Eindringling erkannt: du.")),
                trap(PastX(15.5f), PathSaw(4f, 20f to 6.4f, 20f to 9f, 20f to -2f, r = 1f), say("Pulley 1: ore on the way out. Mind the rope.", "Flaschenzug 1: Erz auf dem Weg nach draußen. Achtung, das Seil.")),
                trap(PastX(22.5f), Saw(22.9f, -1f, 0f, 10f), say("Loose rock. Mines have those.", "Lockerer Fels. Gibt's in Minen.")),
                trap(Landed(27.5f, 31f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 12f, 0f), Power('A', true), say("Cart 2: it knows the mine better than you. The honeypot is live now, too.", "Lore 2: Sie kennt die Grube besser als du. Der Honeypot steht jetzt auch unter Strom.")),
                trap(Zone(16.5f, 12f, 19.9f, 15.5f), Saw(-1.5f, 14.4f, 9f, 0f), say("Cart 3: express to the exit. Racing you.", "Lore 3: Express zum Ausgang. Mit dir um die Wette.")),
            ),
            rematch = listOf(
                Round(
                    T("Honeypot refilled. Hole patched. Help yourself.", "Honeypot nachgefüllt. Loch geflickt. Bedien dich."),
                    start = listOf(Circuit('i')),
                    traps = listOf(
                        trap(PastX(15.5f), PathSaw(4f, 20f to 6.4f, 20f to 9f, 20f to -2f, r = 1f), say("The bucket is early. Mind the gold.", "Der Eimer ist früh dran. Achtung, das Gold.")),
                        trap(PastX(21.5f), Saw(21.9f, -1f, 0f, 10f), say("Loose rock. A bit earlier this time.", "Lockerer Fels. Diesmal etwas früher.")),
                        trap(Landed(27.5f, 31f), Saw(-1.5f, 14.4f, 5f, 0f), say("Second cart. Same mine.", "Zweite Lore. Gleiche Grube.")),
                        trap(Touch('i'), Play(Card.SHORT_CIRCUIT), Power('i', false), say("All that glitters is on a power strip. Click.", "Es ist nicht alles Gold, was glänzt. Manches hängt an der Steckdosenleiste. Klick."), delay = 0.3f),
                        trap(Zone(16.5f, 12f, 18.4f, 15.5f), Saw(32f, 14.4f, -12f, 0f), say("Third cart. From behind. No parking.", "Dritte Lore. Von hinten. Parken verboten.")),
                    ),
                ) { fill(20..23, 13..13, 'i'); fill(21..22, 12..12, 'i'); fill(7..8, 10..10); fill(6..12, 14..14, '.') },
            ),
        ) {
            border(); floor()
            fill(1..6, 10..10); fill(9..27, 10..10)
            fill(19..24, 14..14); fill(20..23, 13..13)
            fill(6..12, 14..14, 'A')
            put(3, 9, 'P'); door(13, 14); bits(42)
        },

        // 43 — workshop (a trap room: U16 percussive maintenance, shake the phone). The lane ends in a wall, and the only way on is the cable port at
        // the foot of it, which comes out above a bed of LEDs. A beam flashes across the lane as you come near (wait until it is dark). Shake the phone and
        // the loose cable re-seats: the port now comes out on the deck over the start (a test beam flashes there a moment later: move on), and the
        // deck is a corridor of flashing beams on the way to the door
        Level(
            name = T("Workshop", "Werkstatt"),
            intro = T("I tried talking to it.", "Ich habe es mit Zureden versucht."),
            start = listOf(Portal('1', 10 to 14, 17 to 13, twoWay = false)),
            traps = listOf(
                trap(PastX(4f), Laser('A', 8 to 1, 8 to 14, on = 1f, off = 60f, delay = 0.55f), say("Clause 1: the beam has right of way.", "Paragraf 1: Der Strahl hat Vorfahrt.")),
                trap(Shaken, Play(Card.DECOY), Reroute('1', 3 to 9), Shake(1.2f), say("Works 90% of the time. Every time.", "Klappt in 90 % der Fälle. Jedes Mal.")),
                trap(Zone(6f, 7f, 9f, 10.6f), Laser('B', 14 to 1, 14 to 9, on = 0.9f, off = 60f, delay = 0.5f), say("Now it is screwed on properly. Mostly.", "Jetzt ist es ordentlich festgeschraubt. Größtenteils.")),
                trap(Zone(6f, 7f, 9f, 10.6f), Laser('D', 6 to 1, 6 to 9, on = 1f, off = 60f, delay = 1.2f), say("And a test beam where the cable came out. Safety first.", "Und ein Prüfstrahl, wo das Kabel rauskam. Sicherheit geht vor.")),
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
                trap(PastX(8.5f), Fall('c'), say("Automatic merge failed. Fix conflicts and try again.", "Automatischer Merge fehlgeschlagen. Konflikte lösen und nochmal versuchen."), delay = 0.35f),
                trap(PastX(19f), Play(Card.UNDO), Undo(2f), Fall('e'), say("git reset --hard HEAD~1. Conflict resolved. Mostly.", "git reset --hard HEAD~1. Konflikt gelöst. Größtenteils."), delay = 0.1f),
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
        // left as you crawl (over a floor belt that drags you back toward it), and the far end of the roof is a hatch of copper that is only open
        // while you stand on the pad beneath it. Stand on the pad, jump up through the hatch (the copper cannot close on you in the gap), and the
        // other wall closes in below you. On the roof it is back left and up the steps to the door, with a wall of its own behind you
        Level(
            name = T("Playground", "Spielplatz"),
            intro = T("Nothing can happen to you in here, says the docs.", "Hier drin kann dir nichts passieren, sagt die Doku."),
            start = listOf(Circuit('g'), Pad('1', at = 26 to 14, circuits = "g", mode = PadMode.HOLD), Belt('b', -1.5f)),
            traps = listOf(
                trap(PastX(10f), Play(Card.COLLAPSE), Move('s', 25f, 0f, 3.6f), say("The sandbox is being garbage collected.", "Die Sandbox wird gerade aufgeräumt.")),
                trap(Pressed('1'), Move('r', -8f, 0f, 4f), say("Stand still. The walls are only playing.", "Stillstehen. Die Wände spielen nur."), delay = 0.2f),
                trap(Landed(19f, 27.5f), Move('y', -22f, 0f, 3.0f), say("Playtime is over. Up the steps.", "Die Pause ist vorbei. Die Stufen hoch.")),
                trap(Zone(28f, 9f, 31f, 15.5f), Move('z', 0f, 4f, 12f), say("Out of the sandbox? Into the bin.", "Raus aus der Sandbox? Rein in den Papierkorb."), delay = 0.2f),
            ),
            hint = T("The copper only lets you out while you stand on the pad. Jump from it.", "Das Kupfer lässt dich nur raus, solange du auf dem Knopf stehst. Spring von ihm."),
        ) {
            border(); floor()
            fill(3..22, 13..13); fill(23..26, 13..13, 'g')
            fill(7..18, 15..15, 'b'); fill(2..2, 13..14, 's'); fill(27..27, 13..14, 'r'); fill(27..27, 11..12, 'y'); fill(28..30, 9..10, 'z')
            fill(3..9, 9..12); fill(10..12, 10..12); fill(13..15, 11..12); fill(16..17, 12..12)
            spawn(4, 14); door(14, 10); bits(45)
        },

        // 46 — privilege escalation (a puzzle room: R10 the transport, U12 the belt turns around). A staircase of three belts over beds of LEDs: user,
        // admin, root. User is calm until you step on it, then it runs against you, and admin follows a moment later. Root runs against you harder than you can walk,
        // and a gate stands on it, except for one short maintenance window a moment after you step onto the pillar between admin and root (the
        // gate lifts, the belt stops): wait there for the window (the admin session times out from above meanwhile, so not too long), then cross
        // root before the gate comes down again. The top is a plank bridge over
        // the LEDs that runs against you too and sinks a while after you land on it. Rematch: demoted. The bridge is gone; the belts have all turned around: they carry you up, to the right, and off their
        // ends into the LEDs (ride, hop at the end), and from the root belt it is back left onto a carpet that runs the other way (hold left
        // against it) to the door in the middle of it. Round 1's door on the bridge does not exist
        Level(
            name = T("Privilege Escalation", "Rechteausweitung"),
            intro = T("I'm promoting you. All the way to the top.", "Ich befördere dich. Ganz nach oben."),
            start = listOf(Belt('a', 0f), Belt('b', -3f), Belt('c', -14f), Belt('p', 0f)),
            traps = listOf(
                trap(Touch('a'), Belt('a', -5f), say("user: permission denied.", "user: Zugriff verweigert."), delay = 0.1f),
                trap(Touch('a'), Belt('b', -5f), say("admin: sudo required. Starting now.", "admin: sudo nötig. Ab sofort."), delay = 1.5f),
                trap(Zone(16.4f, 10f, 18f, 13.2f), Belt('c', 0f), say("root: maintenance window. One second.", "root: Wartungsfenster. Eine Sekunde."), delay = 1.0f),
                trap(Zone(16.4f, 10f, 18f, 13.2f), Belt('c', -14f), say("root: window closed. You were not invited.", "root: Fenster zu. Du warst nicht eingeladen."), delay = 2.0f),
                trap(Zone(16.4f, 10f, 18f, 13.2f), Play(Card.HEADBUTT), Move('k', 0f, 8f, 3.2f), say("admin: session timing out. Slowly. From above.", "admin: Sitzung läuft ab. Langsam. Von oben.")),
                trap(Touch('p'), Belt('p', -6f), say("root: the last step is a belt, too.", "root: Die letzte Stufe ist auch ein Band."), delay = 0.1f),
                trap(Touch('p'), Move('p', 0f, 4f, 8f), say("sudo: the bridge is not in the sudoers file.", "sudo: Die Brücke steht nicht in der sudoers-Datei."), delay = 0.9f),
            ),
            hint = T("Root never stops. Almost never. Wait on the pillar.", "Root läuft immer. Fast immer. Warte auf der Säule."),
            rematch = listOf(
                Round(
                    T("Demoted. Climb again, intern.", "Zurückgestuft. Die Leiter läuft jetzt gegen dich."),
                    start = listOf(Belt('a', 0f), Belt('b', 0f), Belt('c', 0f), Belt('q', 0f)),
                    traps = listOf(
                        trap(Touch('a'), Belt('a', 2.4f), say("intern: please hurry.", "Praktikant: Kaffee holen, aber zackig."), delay = 0.1f),
                        trap(Touch('b'), Belt('b', 2.8f), say("Fast track. Mind the gap.", "Überholspur. Lücke beachten, bitte."), delay = 0.1f),
                        trap(Touch('c'), Belt('c', 6f), say("Please hold the handrail. There is none.", "Bitte am Geländer festhalten. Es gibt keins."), delay = 0.1f),
                        trap(Zone(9f, 8.5f, 13.6f, 9.6f), Play(Card.BACKDRAFT), Belt('q', 5.5f), Saw(1.5f, 9.4f, 6f, 0f), say("Last step: the way out is back down, and the carpet runs the other way. A guest is leaving, too.", "Letzte Stufe: Der Ausgang liegt wieder unten, und der Teppich läuft andersrum. Ein Gast geht auch gerade."), delay = 0.1f),
                    ),
                ) {
                    put(29, 10, '.'); fill(27..30, 11..11, '.')
                    fill(9..14, 10..10, 'q'); fill(2..8, 10..10); fill(17..18, 12..12, 'c')
                    fill(16..17, 13..13, '.'); put(16, 14, '^'); put(17, 14, '^')
                    put(11, 9, 'D')
                },
            ),
        ) {
            border(); floor()
            fill(4..8, 14..14, 'a')
            fill(11..15, 14..14); fill(11..15, 13..13, 'b')
            fill(19..23, 13..14); fill(19..23, 12..12, 'c'); fill(16..17, 1..2, 'k')
            fill(27..30, 11..11, 'p')
            fill(16..17, 13..14); leds(9..10); put(18, 14, '^'); leds(24..30)
            spawn(); put(29, 10, 'D'); bits(46)
        },

        // 47 — math problem (a puzzle room: R5 two floors, U9 the controls swap). 127 + 1 wraps around: a cart rolls out of the foot of the stairs at you, and the
        // moment you leave the ground to hop it the integer overflows: left is right, in mid-air. A pendulum hangs over the lane in front of the stairs (stop,
        // slip under it when it is up) and the stairs are narrow, five steps up to the deck at the top right. On the first step the counter
        // wraps around once more: left is left again. No way back: the door is at the far end
        Level(
            name = T("Math Problem", "Rechenaufgabe"),
            intro = T("What's 127 plus 1? Take your time.", "Was ist 127 plus 1? Lass dir Zeit."),
            traps = listOf(
                trap(PastX(3f), Saw(22.5f, 14.4f, -7f, 0f), say("127 carts in the queue. One is yours.", "127 Loren in der Schlange. Eine ist deine.")),
                trap(Airborne(7.5f, 16f), Play(Card.TWISTED), Swap(true), say("127 + 1 = -128. Left is right now.", "127 + 1 = -128. Links ist jetzt rechts.")),
                trap(PastX(13.7f), PathSaw(5.5f, 18.5f to 11f, 18.5f to 14f, 18.5f to 5f, r = 1f), say("Signed or unsigned? Neither, it is a saw.", "Mit oder ohne Vorzeichen? Weder noch, es ist eine Säge.")),
                trap(Landed(21.9f, 23.1f), Swap(false), say("-128 + 1 = -127. Left is left again. I am as surprised as you.", "-128 + 1 = -127. Links ist wieder links. Ich bin genauso überrascht wie du.")),
            ),
        ) {
            border(); floor()
            fill(22..22, 13..14); fill(23..23, 11..14); fill(24..24, 9..14); fill(25..25, 7..14); fill(26..30, 5..14)
            spawn(); door(30, 4); bits(47)
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
                trap(Landed(roomX(1, 9f), roomX(1, 16f)), Saw(roomX(1, 32f), 14.4f, -12f, 0f), say("Last cron job: unplug the customer.", "Letzter Cronjob: Den Kunden abstecken.")),
                trap(Landed(roomX(1, 9f), roomX(1, 16f)), Laser('K', roomX(1, 27) to 1, roomX(1, 27) to 14, on = 2.15f, off = 60f, delay = 0.1f), say("Firewall rule 2: and nobody gets in.", "Firewall-Regel 2: Und keiner kommt rein.")),
                trap(PastX(roomX(1, 24.5f)), DoorTo(roomX(1, 29), 16, speed = 20f), say("Layer 3: hardware. I'm moving out. Follow me if you dare.", "Schicht 3: Hardware. Ich ziehe aus. Komm nach, wenn du dich traust.")),
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
