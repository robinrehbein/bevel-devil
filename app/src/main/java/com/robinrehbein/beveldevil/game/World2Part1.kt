package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Undo
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 2, levels 1-16. Act 1, "Handshake": the best jokes of the data center, the first portals and the DNS trap. */
object World2Part1 {
    private val hidden = Glyph(spike = true, hidden = true)
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)
    /** The tiles of the bridge in 2-15 (one group per tile, so the bridge can crumble one tile at a time). */
    private val bridgeTiles = ('m'..'r').toList()

    val levels: List<Level> = listOf(

        // 1 — EASTER EGG: Hello, World! A breather with one gag: the floor driver segfaults twice, the first time right under your
        // feet (hop before it), the second time the next stretch as you land. Round 2 plays against round 1's reflex: the first pit is a
        // bluff, the hop over nothing lands in spikes, and the second stretch goes the moment you set foot on it (hop it from the edge)
        Level(
            name = T("Hello, World!", "Hallo, Welt!"),
            intro = T("Print it, then walk to the door.", "Gib es aus, dann lauf zur Tür."),
            legend = mapOf('C' to hidden),
            traps = listOf(
                trap(PastX(9f), Play(Card.COLLAPSE), Fall('a'), say("Hello, World! Segfault in the floor driver.", "Hallo, Welt! Segfault im Boden-Treiber.")),
                trap(Landed(14.4f, 17f), Fall('b'), say("Hello again, World.", "Hallo nochmal, Welt."), delay = 0.1f),
                trap(Landed(14.4f, 17f), Fall('c'), delay = 1.0f),
            ),
            rematch = listOf(
                Round(
                    T("Retransmit. Same text, new bugs.", "Nochmal gesendet. Diesmal mit Bonus-Bugs."),
                    traps = listOf(
                        trap(PastX(9f), Bluff(Card.COLLAPSE)),
                        trap(Airborne(2f, 16.5f), Show('C'), say("Jumped to conclusions.", "Voreilig gesprungen.")),
                        trap(Zone(19.7f, 12f, 23.2f, 15.5f), Fall('b'), say("Goodbye, World.", "Tschüss, Welt.")),
                    ),
                ) { fill(11..19, 14..14, 'C') },
            ),
        ) {
            border(); floor()
            fill(11..13, 15..17, 'a'); fill(14..19, 15..17, 'c'); fill(20..22, 15..17, 'b')
            spawn(); door(); bits(1)
        },

        // 2 — first routing puzzle: the door hangs on a stair high up on the left and nothing on this side climbs up there. The portal at the
        // far end of the floor takes you up to a long shelf. On the way, the floor portal is the loopback (it sends you home, and the
        // second time somewhere much worse), and where the hop over it lands a port opens a spike under you. The shelf has a gap that only
        // the portal for port 22 crosses (it wakes up when you come near)
        Level(
            name = T("Open Port", "Offener Port"),
            intro = T("The firewall is in a bad mood today. Like me.", "Die Firewall hat heute schlechte Laune. Wie ich."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Portal('a', 28 to 12, 28 to 10, twoWay = false),
                Portal('h', 17 to 14, 3 to 14, twoWay = false),
                Portal('g', 18 to 10, 2 to 10, twoWay = false), Power('g', false),
            ),
            traps = listOf(
                trap(Zone(16.5f, 13.9f, 18f, 15.5f), Reroute('h', 20 to 2), say("Loopback again? Rerouted to the server room ceiling.", "Schon wieder Loopback? Umgeleitet an die Serverraumdecke."), delay = 0.1f),
                trap(Landed(18.5f, 24.5f), Play(Card.SPIKE_SEED), Show('A'), say("Port 80 open. Port 9: a spike.", "Port 80 offen. Port 9: ein Spike."), delay = 0.45f),
                trap(Zone(20f, 8f, 21.5f, 11f), Power('g', true), say("Port 22 opens. For a moment.", "Port 22 geht auf. Für einen Moment."), delay = 0.45f),
            ),
            hint = T("The door is upstairs. The way up is at the far end.", "Die Tür ist oben. Der Weg nach oben liegt ganz hinten."),
        ) {
            border(); floor()
            fill(1..10, 11..11); fill(17..30, 11..11)
            fill(3..4, 9..10); fill(5..6, 7..10); fill(7..8, 5..10); fill(9..10, 3..10)
            fill(26..30, 13..14)
            fill(20..23, 14..14, 'A'); put(20, 1, 'v'); fill(19..21, 3..3, '^')
            spawn(); door(10, 2); bits(2)
        },

        // 3 — EASTER EGG: HTTP 404, the door is gone. A two-storey room: you start on the upper floor and the door stands at its far end, in plain
        // sight. The moment you come near it is "transferred": over the ceiling to the lower floor, below your start. The way down is the hole in
        // the upper floor you were about to hop (don't follow the old address: a saw comes along the upper floor), and the lower floor is the way
        // back: a new lane, with a saw that rolls at you (hold music) and, once you are past it, the next one in the queue behind you
        Level(
            name = T("Reception", "Empfang"),
            intro = T("One moment please. Connecting you.", "Einen Moment bitte. Wir verbinden."),
            traps = doorTrail(
                PastX(22f), 30, 10,
                listOf(DoorTo(30, 1, 24f, hanging = true), DoorTo(3, 1, 24f, hanging = true), DoorTo(3, 14, 24f)),
                first = listOf(Play(Card.SHY_DOOR), say("404: Door not found. Try /dev/down.", "404: Tür nicht gefunden. Versuch /dev/unten.")),
            ) + listOf(
                trap(PastX(22.5f), Saw(33f, 10.4f, -10f, 0f), say("Please hold. Or get held.", "Bitte warten. Oder abgeholt werden."), delay = 0.3f),
                trap(PastX(24.5f), Saw(30.3f, -1f, 0f, 9f), say("Old address demolished. Mind the wrecking saw.", "Alte Adresse wird abgerissen. Vorsicht, Abrisssäge."), delay = 0.1f),
                trap(Zone(25f, 12f, 29.5f, 15.5f), Saw(3f, 14.4f, 7f, 0f), say("Your call is important to us. So is this saw.", "Ihr Anruf ist uns wichtig. Diese Säge auch."), delay = 0.5f),
                trap(Zone(10f, 12f, 20f, 15.5f), Saw(30.5f, 14.4f, -8f, 0f), say("Transferring you to the next saw.", "Ich verbinde Sie mit der nächsten Säge.")),
            ),
            hint = T("The door moved. Downstairs, left. The hole you jumped is the stairs.", "Die Tür ist umgezogen. Unten links. Das Loch ist die Treppe."),
        ) {
            border(); floor()
            fill(1..25, 11..11); fill(29..30, 11..11)
            fill(9..10, 10..10); fill(15..16, 10..10)
            spawn(13, 10); door(30, 10); bits(3)
        },

        // 4 — EASTER EGG: off-by-one. The door is in plain sight behind a wall; the way is a stair up, the upper lane under the string of lights
        // (ceiling bulbs that drop when you come near and go back up on their own), a drop at its end and the lower lane back, where an LED strip
        // slides at you. Round 2 counts again: the two bulbs over the upper lane drop just as in round 1, but a third bulb now hangs under the lane,
        // over the lower lane back, and it drops twice, so "wait until it is up, then run" from round 1 runs into its second drop
        Level(
            name = T("String Lights", "Lichterkette"),
            intro = T("Nice lighting. I laid the cables myself.", "Schönes Licht hier. Ich habe die Kabel selbst verlegt."),
            legend = mapOf('A' to Glyph(spike = true, dir = Dir.DOWN), 'B' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = listOf(
                trap(PastX(11.5f), Play(Card.HEADBUTT), Move('A', 0f, 7f, 30f), say("Lights on. Mind your head.", "Licht an. Kopf einziehen."), delay = 0.1f),
                trap(PastX(11.5f), Move('A', 0f, -7f, 14f), delay = 0.8f),
                trap(PastX(17.5f), Move('B', 0f, 7f, 30f), say("Bulb two. I counted from zero.", "Birne zwei. Ich habe bei null angefangen."), delay = 0.1f),
                trap(PastX(17.5f), Move('B', 0f, -7f, 14f), delay = 1.0f),
                trap(PastX(25.3f), Move('G', 12f, 0f, 6f), say("The cable runs the other way, too.", "Das Kabel läuft auch andersrum.")),
            ),
            hint = T("Wait until the bulb is back up, then run.", "Warte, bis die Birne wieder oben ist, dann lauf."),
            rematch = listOf(
                Round(
                    T("Off by one. I count twice now.", "Um eins daneben. Ich zähle jetzt doppelt."),
                    hint = T("A bulb hangs under the lane, too. It flickers twice.", "Unter der Etage hängt auch eine Birne. Sie flackert zweimal."),
                    legend = mapOf('C' to Glyph(spike = true, dir = Dir.DOWN)),
                    traps = listOf(
                        trap(PastX(11.5f), Play(Card.HEADBUTT), Move('A', 0f, 7f, 30f), say("Same bulb. Different cable.", "Gleiche Birne. Anderes Kabel."), delay = 0.1f),
                        trap(PastX(11.5f), Move('A', 0f, -7f, 14f), delay = 0.8f),
                        trap(PastX(17.5f), Move('B', 0f, 7f, 30f), delay = 0.1f),
                        trap(PastX(17.5f), Move('B', 0f, -7f, 14f), delay = 1.0f),
                        trap(Zone(25.4f, 12f, 28.5f, 15.5f), Move('C', 0f, 4f, 30f), say("Flicker. Flicker.", "Flacker. Flacker."), delay = 0.02f),
                        trap(Zone(25.4f, 12f, 28.5f, 15.5f), Move('C', 0f, -4f, 40f), delay = 0.42f),
                        trap(Zone(25.4f, 12f, 28.5f, 15.5f), Move('C', 0f, 4f, 30f), delay = 0.67f),
                        trap(Zone(25.4f, 12f, 28.5f, 15.5f), Move('C', 0f, -4f, 40f), delay = 1.07f),
                        trap(Zone(19f, 12f, 20.5f, 15.5f), Move('G', 12f, 0f, 6f)),
                    ),
                ) { fill(22..23, 10..10, 'C') },
            ),
        ) {
            border(); floor()
            fill(4..5, 13..14); fill(6..7, 11..14); fill(8..9, 9..14)
            fill(10..26, 9..9)
            fill(15..16, 1..1, 'A'); fill(21..22, 1..1, 'B')
            fill(12..13, 14..14, 'G')
            spawn(1); door(11); bits(4)
        },

        // 5 — EASTER EGG: NullPointerException. A snake through three floors: along the top to the right, down to the middle floor and back to
        // the left, down to the ground floor and to the door on the right. Every floor has its own saw: the one that rolls at you, the one that
        // patrols the middle floor (and a fast one racing up behind you at its end: drop at once), and the one that "follows you home"
        Level(
            name = T("Null Pointer", "Nullzeiger"),
            intro = T("Nothing there. Literally.", "Da ist nichts. Wortwörtlich."),
            traps = listOf(
                trap(PastX(6.5f), Play(Card.DEVIL_SAW), Saw(33f, 4.4f, -7f, 0f), say("Not null. Pointing at you.", "Nicht null. Zeigt auf dich.")),
                trap(Zone(21f, 7.5f, 24f, 9.5f), PathSaw(4f, 8f to 8.4f, 18f to 8.4f), say("A pointer to a pointer. It patrols.", "Ein Zeiger auf einen Zeiger. Er patrouilliert.")),
                trap(Zone(5.5f, 6f, 7.8f, 9.5f), Saw(27f, 8.4f, -11f, 0f), say("Pointer arithmetic: plus one saw.", "Zeigerarithmetik: plus eine Säge.")),
                trap(Landed(0f, 6f), Saw(-1f, 14.4f, 7.5f, 0f), say("The segfault follows you home. Politely.", "Der Segfault folgt dir nach Hause. Höflich.")),
                trap(Zone(11f, 12f, 13f, 15.5f), Saw(29f, 14.4f, -7f, 0f), say("The door sends its regards.", "Die Tür lässt grüßen.")),
            ),
            hint = T("Three floors: right, left, right. Every floor has its own saw.", "Drei Etagen: rechts, links, rechts. Auf jeder läuft eine Säge."),
        ) {
            border(); floor()
            fill(1..20, 5..5); fill(5..26, 9..9)
            spawn(2, 4); door(28); bits(5)
        },

        // 6 — EASTER EGG: "It's always DNS". You start next to the portal, and it leads home (a loop). The right entry (a block, no pad) is on
        // top of the long stair, at the far end: walking into it re-points the portal (and restarts it: a wait). So: away from the portal, up, touch, back down and into
        // it. Following the stale entry home twice is fatal (the cache answers with the spikes in the attic), the old entry bites whoever
        // lingers after re-pointing it, and over there a stair to the door
        Level(
            name = T("Address Book", "Adressbuch"),
            intro = T("I looked up the door. It's in the phone book.", "Ich habe die Tür nachgeschlagen. Steht im Telefonbuch."),
            legend = mapOf('B' to hidden),
            start = listOf(Portal('1', 15 to 14, 12 to 11, twoWay = false)),
            traps = listOf(
                trap(Touch('k'), Play(Card.DECOY), Reroute('1', 18 to 14), Power('1', false), say("DNS updated. The portal restarts to apply it.", "DNS aktualisiert. Das Portal startet neu, um es zu übernehmen.")),
                trap(Zone(1f, 2.5f, 3.4f, 5.5f), Show('B'), say("Old entry deleted. With prejudice.", "Alter Eintrag gelöscht. Mit Nachdruck."), delay = 0.75f),
                trap(Zone(11.7f, 10.5f, 13.3f, 11.8f), Reroute('1', 13 to 2), say("Cached answer: home. Next lookup: the attic.", "Gecachte Antwort: daheim. Nächste Abfrage: der Dachboden.")),
                trap(Zone(1.2f, 3f, 3.2f, 5.5f), Power('1', true), say("Port is back up. Almost like new.", "Port wieder oben. Fast wie neu."), delay = 2.45f),
            ),
            hint = T("The right entry is at the far end of the stair. Then wait for the portal.", "Der richtige Eintrag liegt am Ende der Treppe. Dann warte aufs Portal."),
        ) {
            border(); floor()
            fill(9..10, 13..14); fill(7..8, 11..14); fill(5..6, 9..14); fill(3..4, 7..14); fill(1..2, 5..14)
            put(1, 4, 'k'); put(2, 4, 'B'); put(13, 1, 'v'); fill(12..14, 3..3, '^')
            fill(16..17, 1..14)
            fill(20..22, 13..14); fill(23..24, 11..14); fill(25..30, 11..14)
            spawn(13); door(30, 10); bits(6)
        },

        // 7 — EASTER EGG: Blue Screen of Death (stop code 0x7B: inaccessible boot device). The only level that runs right to left: the door is at
        // the far left, behind the hill. On the way up the hill a piece of ceiling crashes onto the step where you would catch your breath (keep
        // climbing); over the hill the next one lands on the plateau and becomes a step, and the last (in the low tunnel to the door) is a ghost
        // made of paper: it appears where you come down, falls, and is gone again a moment later. Dashing under the plateau piece or the paper is
        // the end, so wait for them. Round 2: the first piece stalks you and drops on whoever stops under it
        Level(
            name = T("Sky Blue", "Himmelblau"),
            intro = T("Nice ceiling. Very stable.", "Schöne Decke. Sehr stabil."),
            legend = mapOf('d' to hiddenSolid),
            traps = listOf(
                trap(Landed(24f, 26f), Fall('s'), say("Minor glitch. Do not stop.", "Kleiner Fehler. Nicht anhalten."), delay = 0.6f),
                trap(BeforeX(19.6f), Fall('a'), say("STOP: 0x0000007B. Your ceiling has crashed.", "STOP: 0x0000007B. Deine Decke ist abgestürzt."), delay = 0.28f),
                trap(Zone(6.5f, 13.5f, 9.8f, 15.5f), Play(Card.GHOST_BLOCK), Show('d'), Fall('d'), say("Collecting error info: 100%. Dying now.", "Fehlerinfo sammeln: 100 %. Sterbe jetzt."), delay = 0.25f),
                trap(Zone(6.5f, 13.5f, 9.8f, 15.5f), Hide('d'), delay = 1.55f),
            ),
            hint = T("Wait for each piece to land. The paper one goes away again.", "Warte, bis jedes Stück gelandet ist. Das aus Papier verschwindet wieder."),
            rematch = listOf(
                Round(
                    T("Reboot complete. Your ceiling logged in again.", "Neustart fertig. Die Decke hängt jetzt an dir."),
                    hint = T("The ceiling follows you now. Do not wait where you waited. Two paper pieces at the end.", "Die Decke folgt dir jetzt. Warte nicht, wo du gewartet hast. Zwei Papierstücke am Ende."),
                    traps = listOf(
                        trap(BeforeX(19.6f), Play(Card.STALKER), Chase('a', speed = 6f, left = 2f, right = 4f), say("Roaming profile: your ceiling travels with you.", "Roaming-Profil: Die Decke zieht mit dir um.")),
                        trap(Idle(0.6f), Fall('a'), say("Ceiling synced to your position. Eventually.", "Decke mit deiner Position synchronisiert. Irgendwann."), delay = 0.1f),
                        trap(Landed(24f, 26f), Fall('s'), say("Same glitch. Still do not stop.", "Gleicher Fehler. Immer noch nicht anhalten."), delay = 0.6f),
                        trap(Zone(6.5f, 13.5f, 9.8f, 15.5f), Fall('d'), say("Paper again. Two sheets this time.", "Wieder Papier. Diesmal zwei Blatt."), delay = 0.25f),
                        trap(Zone(6.5f, 13.5f, 9.8f, 15.5f), Fall('e'), delay = 0.15f),
                        trap(Zone(6.5f, 13.5f, 9.8f, 15.5f), Hide('e'), delay = 1.0f),
                        trap(Zone(6.5f, 13.5f, 9.8f, 15.5f), Hide('d'), delay = 1.9f),
                    ),
                    legend = mapOf('d' to Glyph(spike = false)),
                ) { fill(6..7, 9..10, 'e') },
            ),
        ) {
            border(); floor()
            fill(26..27, 13..14); fill(24..25, 11..14); fill(22..23, 9..14); fill(20..21, 7..14); fill(14..19, 5..14)
            fill(12..13, 9..14); fill(10..11, 13..14)
            fill(1..8, 1..8); fill(15..16, 1..2, 'a'); fill(3..4, 9..10, 'd'); fill(22..23, 1..2, 's')
            spawn(29); door(1); bits(7)
        },

        // 8 — EASTER EGG: RAM memory test (POST counts up, 640K). A breather with one punchline: the floor is memory, and the block you step
        // on fails the test: it goes dark under your feet and is plugged in again a moment later as if nothing had happened (hop the moment
        // you set foot on it). A few steps on, a parity error replays your last half second: whoever ran straight on is put back in the air over
        // the dead block (wait on the far side until it is plugged in again). The last address leaks, too: the two blocks before the door
        // go dark just as you run up to them, so whoever made it past the parity error runs into the leak (hop it from the edge)
        Level(
            name = T("Memory Test", "Speichertest"),
            intro = T("POST: 640K ought to be enough for anybody.", "POST: 640K sollten für jeden reichen."),
            start = listOf(Circuit('a'), Circuit('b')),
            traps = listOf(
                trap(PastX(16.4f), Power('a', false), say("RAM check: 3 of 4 blocks OK. Yours is number 4.", "RAM-Check: 3 von 4 Blöcken OK. Deiner ist Nummer 4."), delay = 0.12f),
                trap(PastX(16.4f), Power('a', true), say("Block 4 reseated. Memory test passed.", "Block 4 neu gesteckt. Speichertest bestanden."), delay = 1.6f),
                trap(PastX(24.6f), Power('b', false), say("Memory leak detected at the last address. Right in front of you.", "Speicherleck an der letzten Adresse. Direkt vor dir.")),
                trap(PastX(23.3f), Play(Card.UNDO), Undo(0.6f), say("Parity error. Replaying the last half second. Where were you, again?", "Paritätsfehler. Die letzte halbe Sekunde nochmal. Wo warst du gleich?")),
            ),
        ) {
            border()
            fill(0..15, 15..17); fill(21..25, 15..17); fill(28..31, 15..17); fill(16..20, 15..17, 'a'); fill(26..27, 15..17, 'b')
            spawn(); door(30); bits(8)
        },

        // 9 — EASTER EGG: hot swap. A shelf above the floor: the cables are crossed from the first second, so left is right up here. A saw
        // comes along the shelf, so wait for it and hop it. The shelf ends in the air; below, the kernel reloads the driver (left is left
        // again) and a second saw comes from the wall at the door end
        Level(
            name = T("Cable Mess", "Kabelsalat"),
            intro = T("Everything is plugged in tight. I checked.", "Alles steckt fest. Ich habe nachgesehen."),
            traps = listOf(
                trap(After(0.2f), Play(Card.TWISTED), Swap(true), say("Hot swap: left and right exchanged.", "Hot Swap: links und rechts getauscht.")),
                trap(PastX(3f), Saw(28f, 8.4f, -7f, 0f), say("Driver signed by nobody. Delivered anyway.", "Treiber von niemandem signiert. Trotzdem geliefert."), delay = 0.6f),
                trap(Landed(22f, 31f), Swap(false), say("Kernel reloaded the driver.", "Kernel hat den Treiber neu geladen.")),
                trap(Zone(20.5f, 11f, 23.5f, 15.5f), PathSaw(6f, 18.5f to 9.6f, 18.5f to 14.4f, 40f to 14.4f), say("Second delivery. Straight through the shelf.", "Zweite Lieferung. Direkt durchs Regal.")),
            ),
            hint = T("Up here left is right. Hop the saw, then go down. Below, the cable is plugged in properly again.", "Hier oben ist links rechts. Spring über die Säge und geh runter. Unten steckt das Kabel wieder richtig."),
        ) {
            border(); floor()
            fill(0..21, 9..10)
            spawn(2, 8); door(13); bits(9)
        },

        // 10 — a routing puzzle: a wall splits the room and the door is on the other side of it. Three tunnels wait in the room (the nearest hangs in mid-air above the floor, a hop away)
        // floor, the nearest one only goes home. The right one is closed until it has been knocked on (stand at its mouth for a moment) and
        // comes out on a long shelf above the room; the second tunnel up there is off as well and wakes up when you walk past. The floor
        // flickers on the way: the line drops packets, and three stretches of floor (two on the way to the tunnels, one behind the wall)
        // start to blink on and off as you come near, off first (hop them)
        Level(
            name = T("VPN Tunnel", "VPN-Tunnel"),
            intro = T("Your connection is secure. Really secure.", "Deine Verbindung ist sicher. Wirklich sicher."),
            start = listOf(
                Portal('a', 22 to 12, 30 to 14, twoWay = false),
                Portal('b', 13 to 14, 30 to 8, twoWay = false), Power('b', false),
                Portal('c', 13 to 8, 9 to 14, twoWay = false), Power('c', false),
            ),
            traps = listOf(
                trap(BeforeX(28.4f), Play(Card.CRUMBLE), Blink('a', 0.6f, 0.6f, phase = 0.6f), say("Tunnel established. One packet dropped.", "Tunnel steht. Ein Paket verloren.")),
                trap(BeforeX(18.4f), Blink('e', 0.6f, 0.6f, phase = 0.6f), say("Second hop: encrypted. So is the floor.", "Zweiter Hop: verschlüsselt. Der Boden auch.")),
                trap(Zone(14f, 10f, 17f, 15.5f), Power('b', true), say("Port knocking. Please stay on the line.", "Port-Knocking. Bitte bleiben Sie in der Leitung."), delay = 1.3f),
                trap(Zone(20f, 7f, 23f, 9.5f), Power('c', true), say("Keepalive received. Eventually.", "Keepalive empfangen. Irgendwann."), delay = 0.9f),
                trap(BeforeX(7.4f), Blink('f', 0.5f, 0.5f, phase = 0.5f), say("Third hop: the same, but worse.", "Dritter Hop: dasselbe, nur schlimmer.")),
            ),
            hint = T("The nearest tunnel only leads home. Wait at the mouth of the next one until it opens, then look up.", "Der nächste Tunnel führt nur nach Hause. Warte vor dem übernächsten, bis er aufgeht, und schau dann nach oben."),
        ) {
            border(); floor()
            fill(10..11, 0..14)
            fill(12..30, 9..10)
            fill(24..25, 15..17, 'a'); fill(17..18, 15..17, 'e'); fill(3..4, 15..17, 'f')
            fill(0..2, 14..14)
            spawn(29); door(1, 13); bits(10)
        },

        // 11 — EASTER EGG: fan #3 failed. A puzzle room: the door sits behind a rack gate (a slab that slides into the floor), the
        // switch is up on the mezzanine at the far end. Fan 1 circles the mezzanine (rushing at the switch runs into it); the
        // switch opens the gate for seven seconds and sets fan 2 loose in the aisle, and fan 3 comes rolling in right behind you as you set
        // off from the far wall (keep running); past the rack the raised floor is lifted
        Level(
            name = T("Server Room", "Serverraum"),
            intro = T("Nice and cool in here. Three fans, all fit.", "Schön kühl hier. Drei Lüfter, alle fit."),
            start = listOf(Pad('1', at = 2 to 10)),
            traps = listOf(
                trap(Zone(8f, 8.5f, 12.6f, 11f), PathSaw(5f, 5.5f to 10.3f, 8.5f to 10.3f, 8.5f to 6.5f, 5f to 6.5f, 5f to 10.3f, loop = true, delay = 0.4f),
                    say("Fan 1 of 3 spinning. It does laps.", "Lüfter 1 von 3 dreht. Er dreht Runden.")),
                trap(Zone(0.5f, 12.5f, 4f, 15.5f), Saw(-1.5f, 14.4f, 5f, 0f), say("Fan 3 reporting for duty. Right behind you.", "Lüfter 3 meldet sich zum Dienst. Direkt hinter dir."), delay = 0.3f),
                trap(Pressed('1'), Move('w', 0f, 4f, 14f), PathSaw(6f, 24f to 19.5f, 24f to 13.7f),
                    say("Rack unlocked. For 7 seconds. Fan 2 is on call.", "Rack offen. Für 7 Sekunden. Lüfter 2 hat Bereitschaft.")),
                trap(Pressed('1'), Move('w', 0f, -4f, 14f), say("Rack locked again. Session expired.", "Rack wieder zu. Sitzung abgelaufen."), delay = 7f),
                trap(PastX(14.7f), Play(Card.COLLAPSE), Move('p', 0f, 8f, 30f), say("Raised floor: tiles lifted for maintenance.", "Doppelboden: Platten zur Wartung entnommen.")),
            ),
            hint = T("The switch is upstairs. The fans keep a rhythm.", "Der Schalter ist oben. Die Lüfter halten einen Takt."),
            // rematch: the aisle left of the spawn is a hot aisle now (LEDs): dropping off the shelf where round 1 dropped is fatal.
            // The way out is back across the shelf, past fan 1 a second time, and down over the rack
            rematch = listOf(
                Round(
                    T("Failover test. The left aisle is a hot aisle now.", "Failover-Test. Der linke Gang ist jetzt ein Heißgang."),
                    hint = T("Don't drop where you dropped. Go back the way you came.", "Spring nicht dort runter, wo du es kennst. Geh zurück."),
                    traps = listOf(
                        trap(Landed(8f, 11.6f), PathSaw(5f, 5.5f to 10.3f, 8.5f to 10.3f, 8.5f to 6.5f, 5f to 6.5f, 5f to 10.3f, loop = true, delay = 0.4f),
                            say("Fan 1 again. Same laps. New schedule.", "Lüfter 1 wieder. Gleiche Runden. Neuer Plan.")),
                        trap(Pressed('1'), Move('w', 0f, 4f, 14f), PathSaw(6f, 24f to 19.5f, 24f to 13.7f),
                            say("Rack unlocked. Fan 2 is on call. The hot aisle is not.", "Rack offen. Lüfter 2 hat Bereitschaft. Der Heißgang nicht.")),
                        trap(Pressed('1'), Move('w', 0f, -4f, 14f), say("Rack locked again. Session expired.", "Rack wieder zu. Sitzung abgelaufen."), delay = 7f),
                        trap(After(5.9f), Saw(4.4f, 10.4f, 4f, 0f), say("Fan 3 takes the high road this time. Behind you.", "Lüfter 3 nimmt diesmal den oberen Weg. Hinter dir.")),
                        trap(PastX(14.7f), Play(Card.COLLAPSE), Move('p', 0f, 8f, 30f), say("Raised floor: lifted again. Maintenance is thorough.", "Doppelboden: wieder entnommen. Die Wartung ist gründlich.")),
                    ),
                ) { put(1, 14, '^'); put(2, 14, '^') },
            ),
        ) {
            border(); floor()
            fill(2..11, 11..11)
            rack(12, 4, 2)
            fill(27..27, 1..10); fill(27..27, 11..14, 'w')
            fill(16..18, 15..17, 'p')
            spawn(4); door(29); bits(11, x0 = 14)
        },

        // 12 — EASTER EGG: Segmentation fault (core dumped). A loop around the room: the door is in kernel space (upper left),
        // you start in user space (lower left). Every page you touch on the way right is freed behind you, faster each time; a
        // pointer at the far end takes you upstairs, where the page you land on is freed too. On the way back the next page is
        // realloc'ed away the moment you step up to it (standing there is enough), and the last page before the door is freed as you land
        Level(
            name = T("Address Space", "Adressraum"),
            intro = T("Everyone gets their own space. You too.", "Jeder bekommt seinen eigenen Platz. Du auch."),
            start = listOf(Portal('1', 30 to 14, 30 to 8, twoWay = false)),
            traps = listOf(
                trap(Touch('a'), Move('a', 0f, 12f, 20f), say("free(page). Use after free is your problem.", "free(page). Use after free ist dein Problem."), delay = 0.28f),
                trap(Touch('b'), Move('b', 0f, 12f, 20f), delay = 0.2f),
                trap(Touch('c'), Move('c', 0f, 12f, 20f), delay = 0.14f),
                trap(Touch('d'), Move('d', 0f, 12f, 20f), delay = 0.08f),
                trap(Touch('g'), Move('g', 0f, 12f, 20f), say("Dangling pointer: this page is freed too.", "Hängender Zeiger: Diese Seite wird auch freigegeben."), delay = 0.8f),
                trap(Zone(17.9f, 3f, 22f, 9.9f), Play(Card.SINKING), Move('f', 0f, 12f, 40f),
                    say("realloc(): page moved to a new address. Below you.", "realloc(): Seite an neue Adresse verschoben. Unter dir.")),
                trap(Zone(17.9f, 3f, 22f, 9.9f), Move('f', 0f, -12f, 30f), say("...and back. Address space is a loan.", "...und zurück. Adressraum ist geliehen."), delay = 0.35f),
                trap(Zone(1f, 5f, 10.4f, 9.9f), Move('l', 0f, 12f, 20f), say("Last page before the stairs: freed on arrival.", "Letzte Seite vor der Treppe: bei Ankunft freigegeben."), delay = 0.45f),
            ),
            hint = T("Whatever you touch gets freed. Keep moving. The pointer is on the right.", "Was du berührst, wird freigegeben. Bleib in Bewegung. Der Zeiger ist rechts."),
        ) {
            border()
            fill(0..6, 15..17); fill(26..30, 15..17)
            fill(7..9, 15..15, 'a'); fill(12..14, 15..15, 'b'); fill(17..19, 15..15, 'c'); fill(22..24, 15..15, 'd')
            fill(26..26, 9..9); fill(27..30, 9..9, 'g'); fill(20..23, 9..9); fill(14..17, 9..9, 'f'); fill(1..6, 9..9); fill(7..12, 9..9, 'l')
            fill(1..2, 7..7); fill(4..12, 5..5)
            spawn(); door(12, 4); bits(12, x0 = 12)
        },

        // 13 — EASTER EGG: "works on my machine". The door sits on a ledge nobody can jump to. The deploy switch flips you onto
        // the ceiling (production), where you walk over the LEDs; the rollback drops you on the far side, and the redeploy
        // switch flips you again, the whole way back upside down. This time the ceiling has a flaky test (it blinks), and
        // the second rollback drops you on the ledge, next to the door. The shortcut to the door is a corner of legacy code: walk
        // under the ledge and a hotfix flips you up into it
        Level(
            name = T("Works on My Machine", "Läuft bei mir"),
            intro = T("Tested locally. Green everywhere.", "Lokal getestet. Überall grün."),
            start = listOf(Pad('1', at = 8 to 14), Pad('2', at = 28 to 14)),
            legend = mapOf('K' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = listOf(
                trap(Zone(0f, 11f, 1.9f, 16f), Gravity(true), say("Hotfix straight to production. Legacy code included.", "Hotfix direkt in Produktion. Mit Legacy-Code."), delay = 0.1f),
                trap(Pressed('1'), Play(Card.UPSIDE_DOWN), Gravity(true), say("Deployed. Production has different gravity.", "Deployt. Produktion hat eine andere Schwerkraft.")),
                trap(Zone(23.2f, 0f, 31f, 3f), Gravity(false), say("Rollback! The door stayed in staging.", "Rollback! Die Tür ist im Staging geblieben.")),
                trap(Pressed('2'), Gravity(true), say("Redeploy. Same bugs, other direction.", "Neu deployt. Gleiche Bugs, andere Richtung.")),
                trap(Landed(15.2f, 17.4f), Blink('f', on = 1f, off = 1f, phase = 1f), say("Flaky test: red again. Nobody knows why.", "Wackeliger Test: wieder rot. Keiner weiß, warum.")),
                trap(Zone(0f, 0f, 6.4f, 3f), Gravity(false), say("Rollback complete. Works on my machine.", "Rollback fertig. Läuft bei mir.")),
            ),
            hint = T("The flaky test blinks. Count before you cross.", "Der wackelige Test blinkt. Erst zählen, dann rüber."),
        ) {
            border(); floor()
            leds(10..22)
            fill(1..7, 6..6); door(1, 5); put(1, 7, 'K')
            put(20, 1, '#'); put(9, 1, 'v'); put(29, 1, 'v')
            fill(12..14, 0..0, 'f')
            spawn(6)
        },

        // 14 — EASTER EGG: 127.0.0.1 and traceroute. The room is four subnets (a slab and a wall split it into quarters), and the
        // way to the door is a spiral of one-way links: bottom left, top left, top right, bottom right. The obvious floor link
        // is the loopback (it sends you home, where a shelf comes down on you); the way on is the one hanging in mid-air above
        // it. Every hop drops something on whoever lingers where it lands you: the router upstairs, the next link flaps (down
        // for a moment just as you arrive), in the top right the ceiling comes down on whoever runs under it, the last subnet
        // gets heavy mail on arrival, and its pit has one stone: step on it and the packet hanging above drops onto it
        Level(
            name = T("127.0.0.1", "127.0.0.1"),
            intro = T("Please take off your shoes. Somebody lives here.", "Bitte Schuhe ausziehen. Hier wohnt jemand."),
            start = listOf(
                Portal('1', 13 to 14, 3 to 14, twoWay = false), Portal('2', 13 to 12, 13 to 7, twoWay = false),
                Portal('3', 1 to 7, 30 to 7, twoWay = false), Portal('4', 17 to 7, 30 to 14, twoWay = false),
            ),
            traps = listOf(
                trap(Zone(12.3f, 14f, 13f, 15f), Fall('n'), say("ping 127.0.0.1: reply from 127.0.0.1. Welcome home. Mind the shelf.", "ping 127.0.0.1: Antwort von 127.0.0.1. Willkommen daheim. Vorsicht, Regal.")),
                trap(Zone(10.5f, 1f, 15f, 8f), Fall('m'), say("Hop 2: router reached. Router dropped. On you.", "Hop 2: Router erreicht. Router fällt. Auf dich."), delay = 0.5f),
                trap(Zone(1f, 1f, 4.5f, 8f), Power('3', false), say("Link down. Hop 3 is flapping.", "Link down. Hop 3 flattert.")),
                trap(Zone(1f, 1f, 4.5f, 8f), Power('3', true), delay = 1.2f),
                trap(Zone(27f, 3f, 31f, 8f), Power('4', false), say("Hop 4 is up for 6 seconds. TTL, you know.", "Hop 4 lebt 6 Sekunden. TTL, du weißt schon."), delay = 6f),
                trap(Zone(23.4f, 1f, 26.5f, 8f), Play(Card.HEADBUTT), Fall('h'), say("Hop 4: * * * Request timed out. The ceiling didn't.", "Hop 4: * * * Zeitüberschreitung. Die Decke nicht.")),
                trap(Zone(26.5f, 9f, 31f, 15f), Fall('i'), say("Hop 5: you've got mail. Heavy mail.", "Hop 5: Sie haben Post. Schwere Post.")),
                trap(Touch('k'), Fall('j'), say("Hop 6: packet dropped. From the floor above.", "Hop 6: Paket verworfen. Vom Stockwerk drüber."), delay = 0.4f),
            ),
            hint = T("The obvious link goes home. Try the one in the air.", "Der offensichtliche Link führt heim. Nimm den in der Luft."),
            // rematch: the two links at home swap (the one in the air is the loopback now), the router upstairs is a press, the
            // DNS entry of the way on is poisoned for a moment after you arrive (it drops you into the pit; stay out of the link
            // until it heals), the rack in the top right comes down too, and a firewall follows your packet into the last subnet
            rematch = listOf(
                Round(
                    T("Connection reset by peer. The peer is me.", "Verbindung zurückgesetzt. Von mir, natürlich."),
                    start = listOf(
                        Portal('1', 13 to 14, 13 to 7, twoWay = false), Portal('2', 13 to 12, 3 to 14, twoWay = false),
                        Portal('3', 1 to 7, 30 to 7, twoWay = false), Portal('4', 17 to 7, 29 to 14, twoWay = false),
                    ),
                    hint = T("Let the DNS settle before you take the link. Then don't stop.", "Lass das DNS sich beruhigen, bevor du den Link nimmst. Dann nicht stehen bleiben."),
                    traps = listOf(
                        trap(Zone(12.3f, 11f, 14f, 13f), say("Routing table updated. You're home again.", "Routing-Tabelle aktualisiert. Du bist wieder daheim.")),
                        trap(Zone(10.5f, 1f, 15f, 8f), Move('m', 0f, 5f, 30f), say("Hop 2 pings back. With the router.", "Hop 2 pingt zurück. Mit dem Router."), delay = 0.5f),
                        trap(Zone(1f, 1f, 6f, 8f), Play(Card.DECOY), Reroute('3', 25 to 16), say("DNS poisoned: hop 3 now exits over the pit.", "DNS vergiftet: Hop 3 endet jetzt über der Grube.")),
                        trap(Zone(1f, 1f, 6f, 8f), Reroute('3', 30 to 7), say("TTL expired. DNS healed.", "TTL abgelaufen. DNS geheilt."), delay = 1.2f),
                        trap(Zone(27f, 3f, 31f, 8f), Power('4', false), say("Hop 4 is up for 6 seconds. TTL, you know.", "Hop 4 lebt 6 Sekunden. TTL, du weißt schon."), delay = 6f),
                        trap(Zone(27f, 1f, 31f, 8f), Move('g', 0f, 5f, 30f), say("Hop 3 lands. So does its rack.", "Hop 3 landet. Sein Rack auch."), delay = 0.3f),
                        trap(Zone(23.4f, 1f, 26.5f, 8f), Move('h', 0f, 5f, 30f), say("Hop 4: still timing out.", "Hop 4: immer noch Zeitüberschreitung.")),
                        trap(Zone(26.5f, 9f, 31f, 15f), Move('i', 0f, 5f, 30f), say("Hop 5: registered mail. Signature required.", "Hop 5: Einschreiben. Unterschrift erforderlich."), delay = 0.15f),
                        trap(Touch('k'), Move('j', 0f, 5f, 10f), say("Hop 6: dropped again. Consistently.", "Hop 6: wieder verworfen. Immerhin konsistent."), delay = 0.45f),
                        trap(Zone(17f, 9f, 28.6f, 15f), Chase('F', speed = 4f, left = 13f, right = 0f),
                            say("Firewall rule added: follow that packet.", "Neue Firewall-Regel: Folge dem Paket.")),
                    ),
                ) { put(30, 13, 'F'); put(30, 14, 'F') },
            ),
        ) {
            border(); floor()
            fill(1..30, 8..8); fill(15..16, 1..14)
            fill(20..22, 1..2, 'h'); fill(13..14, 1..2, 'm'); fill(4..5, 9..9, 'n'); fill(28..30, 1..2, 'g'); fill(29..30, 9..9, 'i'); pit(20..25); fill(20..24, 15..15, 'k'); fill(21..25, 9..9, 'j')
            leds(6..7, 7)
            spawn(); door(18); bits(14, x0 = 3)
        },

        // 15 — EASTER EGG: TCP three-way handshake. The door floats at the top right, nobody can reach it; two switches are client
        // (far left) and server (the far side of a wide pit). SYN opens a port: a bridge appears over the pit, and the client's
        // island closes behind you. The bridge is a SYN cookie that crumbles under whoever stops on it. SYN-ACK (the server's
        // pad) builds a staircase up to the door. ACK is the door
        Level(
            name = T("Greeting", "Begrüßung"),
            intro = T("Politeness is free, they say.", "Höflichkeit kostet nichts, sagt man."),
            legend = (bridgeTiles + 'a' + 'b').associateWith { hiddenSolid },
            start = listOf(Pad('1', at = 4 to 14), Pad('2', at = 26 to 14), Pad('3', at = 15 to 14)),
            traps = listOf(
                trap(Pressed('1'), *(listOf<Action>(Play(Card.GHOST_BLOCK), say("SYN. A port opened over there. The cookie is good for 8 seconds.", "SYN. Drüben ging ein Port auf. Das Cookie hält 8 Sekunden.")) +
                    bridgeTiles.map { Show(it) }).toTypedArray()),
                trap(Pressed('1'), Move('i', 0f, 12f, 20f), say("The client socket closes behind you. Politely.", "Der Client-Socket schließt hinter dir. Höflich."), delay = 0.9f),
                trap(Pressed('1'), *bridgeTiles.map { Move(it, 0f, 12f, 30f) }.toTypedArray(), say("SYN cookie expired.", "SYN-Cookie abgelaufen."), delay = 8f),
                trap(Touch('k'), Move('h', 0f, 11f, 40f), say("RST. Connection reset. The cooling rack too.", "RST. Verbindung zurückgesetzt. Das Kühlrack auch."), delay = 0.05f),
                trap(Touch('k'), Move('h', 0f, -11f, 40f), delay = 0.75f),
                trap(Pressed('2'), Show('a'), say("SYN-ACK. The server is building you a staircase. ACK it.", "SYN-ACK. Der Server baut dir eine Treppe. Bestätige sie."), delay = 0.5f),
                trap(Pressed('2'), Show('b'), delay = 1f),
                trap(Zone(28.8f, 8f, 31f, 11f), say("ACK. Connection established. Hello!", "ACK. Verbindung steht. Hallo!")),
            ) + bridgeTiles.mapIndexed { n, c -> trap(Touch(bridgeTiles[0]), Move(c, 0f, 12f, 30f), delay = 0.7f + 0.14f * n) },
            hint = T("Client left, server right. Who greets first?", "Client links, Server rechts. Wer grüßt zuerst?"),
        ) {
            border(); floor()
            fill(0..6, 15..17, 'i'); pit(7..9); pit(20..25)
            for (n in 0 until 6) put(20 + n, 15, bridgeTiles[n])
            fill(28..29, 13..14, 'a'); fill(30..30, 11..14, 'b')
            fill(17..18, 1..3, 'h'); put(15, 15, 'k')
            spawn(13); door(30, 10); bits(15)
        },

        // 16 — EASTER EGG: man in the middle. Two pillars hang from the ceiling; step between them and they drop into a cell
        // (sprinting on gets you crushed by the far one). Inside, Mephi "encrypts" the connection: left and right swap. The
        // only way out is the cell's portal, which wakes up after a moment and forwards your packet to the top right, where
        // it is decrypted. The whole upper lane back to the left is a field of freed pages; at its end sits the server's
        // switch, and when its port opens it leads to the door
        Level(
            name = T("Through Traffic", "Durchgangsverkehr"),
            intro = T("Your connection is encrypted. Mostly by me.", "Deine Verbindung ist verschlüsselt. Größtenteils von mir."),
            start = listOf(
                Portal('1', 13 to 14, 28 to 7, twoWay = false), Portal('2', 3 to 7, 22 to 14, twoWay = false),
                Power('1', false), Power('2', false),
            ),
            traps = listOf(
                trap(PastX(12.7f), Play(Card.GRAND_FINALE), Move('l', 0f, 2f, 8f), say("Hello. I'm between you and the door.", "Hallo. Ich bin zwischen dir und der Tür."), delay = 0.3f),
                trap(PastX(12.7f), Move('r', 0f, 2f, 8f), delay = 0.5f),
                trap(PastX(12.7f), Swap(true), say("Connection encrypted. By me. Left is right now.", "Verbindung verschlüsselt. Von mir. Links ist jetzt rechts."), delay = 0.6f),
                trap(PastX(12.7f), Power('1', true), say("Packet forwarded. Eventually.", "Paket weitergeleitet. Irgendwann."), delay = 2.55f),
                trap(Zone(24f, 3f, 31f, 8.5f), Swap(false), say("Decrypted. You have the plaintext now. Don't lose it.", "Entschlüsselt. Du hast jetzt den Klartext. Verlier ihn nicht.")),
                trap(Touch('s'), Power('2', true), say("Port 443 opens in a moment. Courtesy of the server.", "Port 443 öffnet gleich. Mit freundlichen Grüßen vom Server."), delay = 1.45f),
            ) + "abcd".map { c -> trap(Touch(c), Move(c, 0f, 12f, 30f), delay = 0.3f) } + "efgj".map { c -> trap(Touch(c), Move(c, 0f, 12f, 30f), delay = 0.3f) } +
                "qtuw".map { c -> trap(Touch(c), Move(c, 0f, 12f, 30f), delay = 0.3f) },
            hint = T("The way out of the cell is the portal. It needs a moment. Everything upstairs is freed.", "Der Weg aus der Zelle ist das Portal. Es braucht einen Moment. Oben wird alles freigegeben."),
        ) {
            border(); floor()
            fill(11..12, 9..12, 'l'); fill(19..20, 9..12, 'r')
            fill(1..30, 8..8)
            for (n in 0 until 4) { put(23 + n, 8, "abcd"[n]); put(16 + n, 8, "efgj"[n]); put(4 + n, 8, "qtuw"[n]) }
            put(21, 7, '^'); put(9, 7, '^'); put(1, 7, 's'); put(14, 7, '^'); fill(7..8, 13..14)
            put(27, 14, '^')
            spawn(); door(30); bits(16, x0 = 23)
        },

    )
}
