package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Flip
import com.robinrehbein.beveldevil.game.Action.FrameCrack
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.PauseTrap
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.Action.Extend
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Action.Tilt
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.Undo
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Resumed
import com.robinrehbein.beveldevil.game.Trigger.Zone
import com.robinrehbein.beveldevil.game.Trigger.Shaken
import com.robinrehbein.beveldevil.game.Trigger.Touch

/** World 1, levels 33-48. Act 3, "Mephi schummelt": the meta twists and phone motion, sparingly, as surprises. */
object World1Part3 {
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    val levels: List<Level> = listOf(
        // 33 — clear road: the strip of the road is pulled away from under the runner, oncoming traffic, and at the door the end of
        // the road turns out to be a lie: the wall breaks open and the road goes on, with a ferry that leaves as you board it.
        // Rematch: Mephi built ahead (the road is longer from the first second), the hop that saved you lands on spikes now
        // TWIST: Extend (the end was a lie), no FakeWin: a fake screen (3.4 s) cannot meet the density rule
        Level(
            name = T("Clear Road", "Freie Fahrt"),
            intro = T("No traps today. I'm on vacation.", "Heute keine Fallen. Ich habe Urlaub."),
            hint = T("The road is pulled away as you come. At the door the road is not over: the ferry leaves at once.", "Die Straße wird dir weggezogen. An der Tür ist sie nicht zu Ende: Die Fähre legt sofort ab."),
            rooms = 2,
            traps = listOf(
                trap(PastX(5.2f), Move('a', -3f, 0f, 9f), Say(T("Roadworks. Unannounced.", "Baustelle. Unangekündigt."))),
                trap(Landed(12.5f, 17f), Saw(35.5f, 14.4f, -6.5f, 0f, 0.62f), Say(T("Oncoming traffic. Not a trap. A vehicle.", "Gegenverkehr. Keine Falle. Ein Fahrzeug."))),
                trap(AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 28) to 14, line = T("Clear road, you said. The road disagrees.", "Freie Fahrt, sagtest du. Die Straße sieht das anders."))),
                trap(PastX(roomX(1, 2.5f)), Saw(roomX(1, 9.5f), 14.4f, -6f, 0f, 0.62f), Say(T("Road continues. So does the traffic.", "Die Straße geht weiter. Der Verkehr auch."))),
                trap(Landed(roomX(1, 12.5f), roomX(1, 16f)), Move('b', -3.5f, 0f, 6f), Say(T("The ferry leaves now. With or without you.", "Die Fähre legt jetzt ab. Mit oder ohne dich."))),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. This time I built ahead.", "Revanche. Diesmal habe ich vorgebaut."),
                    legend = mapOf('A' to hiddenSpike),
                    hint = T("The road is long from the start. Do not hop where you hopped before.", "Die Straße ist von Anfang an lang. Spring nicht, wo du vorhin gesprungen bist."),
                    traps = listOf(
                        trap(After(0.3f), Extend(into = 1, door = roomX(1, 28) to 14, line = T("Prebuilt. Delivered ahead of schedule.", "Vorgefertigt. Vorzeitig geliefert."))),
                        trap(Airborne(8.5f, 14f), Show('A'), Say(T("Old habits. Mine too.", "Alte Gewohnheiten. Meine auch."))),
                        trap(PastX(7f), Play(Card.DEVIL_SAW), PathSaw(7f, 19.5f to 14.4f, 19.5f to 10.6f, delay = 1.4f), Say(T("Jump rope. You skip, the traffic does not.", "Seilspringen. Du hüpfst, der Verkehr nicht."))),
                        trap(PastX(20.5f), Move('d', -3f, 0f, 9f), Say(T("The road is a subscription. Cancelled.", "Die Straße ist ein Abo. Gekündigt."))),
                        trap(PastX(roomX(1, 1.5f)), Move('c', -3f, 0f, 9f), Say(T("Delivered. The road is still on the truck.", "Geliefert. Die Straße liegt noch auf dem Laster."))),
                        trap(Landed(roomX(1, 12.5f), roomX(1, 16f)), Saw(roomX(1, 31.5f), 14.4f, -6.5f, 0f, 0.62f), Say(T("The ferry has a cargo. It rolls.", "Die Fähre hat Ladung. Sie rollt."))),
                    ),
                ) { fill(10..12, 15..17, '#'); put(14, 14, 'A'); put(15, 14, 'A'); pit(25..27); fill(25..27, 15..15, 'd'); room(1) { pit(5..7); fill(5..7, 15..15, 'c') } },
            ),
        ) {
            border(); floor()
            room(0) { pit(10..12); fill(10..12, 15..15, 'a'); put(2, 14, 'P'); put(29, 14, 'D') }
            room(1) { pit(11..16); fill(13..15, 15..15, 'b') }
        },

        // 34 — monday morning: the way is closed for a break right at the start. The HUD pause button dodges your finger; only a real
        // pause (back button) opens the wall, and the floor behind it takes its break too, and so does the floor you paused on. Then the
        // floor you land on behind the pit opens under you, the way up leads back along the upper floor, and there a slab from the ceiling takes its break
        // right where you hurry, in front of the door in the middle of the upper floor
        // TWIST: PauseTrap dodge + Resumed
        Level(
            name = T("Monday Morning", "Montagmorgen"),
            intro = T("Everything is a bit slow today. Coffee is brewing.", "Heute ist alles etwas langsam. Der Kaffee läuft."),
            hint = T("The wall is closed for a break. Pause the game: the back button works, the button on screen runs away.", "Die Wand hat Pause. Pausiere das Spiel: Die Zurück-Taste geht, der Knopf auf dem Bildschirm läuft weg."),
            traps = listOf(
                trap(After(0.2f), PauseTrap(PauseTrick.DODGE)),
                trap(Zone(5.6f, 10f, 8f, 15f), Say(T("Closed for a break. Pause the game to open it.", "Wegen Pause geschlossen. Pausier das Spiel, dann geht's auf."))),
                trap(Resumed(), Play(Card.CRUMBLE), Hide('w'), Hide('a'), Say(T("Refreshed? The floor took a break too.", "Erholt? Der Boden macht jetzt auch Pause."))),
                trap(Resumed(), Hide('s'), delay = 0.7f),
                trap(Landed(12.5f, 15.5f), Fall('g'), Say(T("Second pit. Same excuse.", "Zweites Loch. Gleiche Ausrede.")), delay = 0.3f),
                trap(Zone(23.5f, 7f, 25.9f, 9.5f), FrameCrack(18, 0, 24, 0, warn = 0.3f), Say(T("The ceiling is on a break. In your direction.", "Die Decke macht Pause. In deine Richtung."))),
            ),
        ) {
            border(); floor()
            fill(8..8, 10..14, 'w'); fill(9..11, 15..17, 'a'); fill(13..15, 15..17, 'g'); fill(5..7, 15..17, 's')
            fill(22..25, 13..14); fill(26..30, 11..14)
            fill(2..25, 9..9)
            put(2, 14, 'P'); put(12, 8, 'D')
        },

        // 35 — breather: the door is far away and buffering. It jumps up onto the high ledge as you start to climb (23%), and when you land
        // up there it is gone again: back down on the ground, in the middle of the room, behind you. The way down is straight down,
        // and at once: the router over the ledge reboots onto whoever waits for the door up there, and a moment later the platform you
        // climbed by uploads itself into the ceiling: whoever is still on it on the way down goes up with it
        // TWIST: DoorTo (the one fleeing door of the act)
        Level(
            name = T("Home Network", "Heimnetz"),
            intro = T("My internet is slow today. Don't mind me.", "Mein Internet ist heute lahm. Lass dich nicht stören."),
            hint = T("The door buffers: it goes where you are not. Climb up, and come straight back down, before the platform uploads you.", "Die Tür lädt: Sie geht dorthin, wo du nicht bist. Klettere hoch und komm direkt wieder runter, bevor die Plattform dich hochlädt."),
            traps = listOf(
                trap(PastX(8f), Play(Card.SHY_DOOR), DoorTo(30, 8, speed = 14f), Say(T("Buffering... 23%", "Lädt... 23 %"))),
                trap(Landed(24f, 30.5f), DoorTo(30, 2, speed = 25f), Say(T("Connection lost. Retrying...", "Verbindung verloren. Neuer Versuch..."))),
                trap(Landed(24f, 30.5f), DoorTo(14, 2, speed = 25f), delay = 0.3f),
                trap(Landed(24f, 30.5f), DoorTo(14, 14, speed = 25f), delay = 1.0f),
                trap(Landed(24f, 30.5f), Say(T("100%. Was that so hard?", "100 %. War das so schwer?")), delay = 1.4f),
                trap(Landed(24f, 30.5f), Move('q', 0f, 6f, 14f), Say(T("Router reboot. Mind your head.", "Router-Neustart. Kopf einziehen.")), delay = 0.5f),
                trap(Landed(24f, 30.5f), Move('u', 0f, -10f, 8f), Say(T("Uploading the platform. And whoever is on it.", "Lade die Plattform hoch. Und alle, die drauf sind.")), delay = 0.9f),
            ),
        ) {
            border(); floor()
            fill(17..20, 13..13); fill(21..23, 11..11, 'u'); fill(25..30, 9..9); fill(25..30, 1..2, 'q')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 36 — gallery: a jump rope swings across the hall, and then the golden frame comes apart: a piece of the ceiling cracks off as you
        // near the high ledge where the door is, and whoever runs on is under it when it lands. The piece is the step you need up to the
        // ledge (wait for it to land, then climb on). Up on the ledge the next piece comes down in front of the door, and the curator
        // hits Ctrl+Z on whoever has just crossed it
        // TWIST: FrameCrack (meta); the rope is the second family, the undo is meta too
        Level(
            name = T("Gallery", "Galerie"),
            intro = T("All real gold. Almost. Don't touch.", "Alles echtes Gold. Fast. Nicht anfassen."),
            hint = T("The frame drops pieces where you run. Let them land, then climb on. Mind the rope, and the curator.", "Der Rahmen lässt Stücke fallen, wo du rennst. Lass sie landen, dann steig drauf. Achte auf das Seil und auf die Kuratorin."),
            traps = listOf(
                trap(PastX(3.2f), PathSaw(7f, 7f to 10.6f, 7f to 14.4f), Say(T("Jump rope. The exhibit is interactive.", "Seilspringen. Das Exponat ist interaktiv."))),
                trap(PastX(8.5f), FrameCrack(14, 0, 16, 0, warn = 0.5f), Say(T("Crack.", "Knack."))),
                trap(Landed(17f, 23f), FrameCrack(26, 0, 28, 0, warn = 0.55f), Say(T("The ceiling is an exhibit. Do not touch.", "Die Decke ist ein Exponat. Nicht berühren."))),
                trap(PastX(28.4f), Play(Card.UNDO), Undo(1.0f), FrameCrack(19, 0, 25, 0, warn = 0.7f), Say(T("The curator pressed Ctrl+Z. On you.", "Die Kuratorin hat Strg+Z gedrückt. Bei dir."))),
            ),
        ) {
            border(); floor()
            fill(17..30, 12..14)
            put(2, 14, 'P'); put(29, 11, 'D')
        },

        // 37 — force push: as you pass the first marks the floor behind you is deleted, tile pair by tile pair, chasing you to the stairs;
        // the first step is gone as soon as you have left it, and upstairs history is rewritten behind you again, all the way to the door.
        // Rematch: Mephi pushed first: the floor ahead is already gone, the stones that are left give way under whoever lands on them,
        // and upstairs the rewrite is faster
        // TWIST: Fall / Hide (the floor is not there)
        Level(
            name = T("git push --force", "git push --force"),
            intro = T("Mephi already pushed. You just need to pull.", "Mephi hat schon gepusht. Du musst nur noch pullen."),
            hint = T("History is rewritten behind you: do not stop. The first step is deleted once you have left it.", "Die Historie wird hinter dir umgeschrieben: Bleib nicht stehen. Die erste Stufe ist gelöscht, sobald du sie verlassen hast."),
            traps = listOf(
                trap(PastX(8.8f), Play(Card.COLLAPSE), Fall('a'), delay = 0.2f),
                trap(PastX(8.8f), Say(T("Force-pushed. Your commits are gone.", "Force-gepusht. Deine Commits sind weg."))),
                trap(Landed(21f, 24f), Hide('u'), Say(T("The merge base is deleted too.", "Die Merge-Basis ist auch gelöscht.")), delay = 0.7f),
                trap(Zone(21f, 7f, 23f, 9.5f), Say(T("Rewriting history upstairs, too.", "Oben wird die Historie auch umgeschrieben."))),
            ) + (1..8).map { k -> trap(PastX(8.8f), Fall('a' + k), delay = 0.2f + 0.33f * k) } +
                (0..9).map { k -> trap(Zone(21f, 7f, 23f, 9.5f), Hide('j' + k), delay = 0.4f + 0.33f * k) },
            rematch = listOf(
                Round(
                    T("Rematch. I pushed first this time.", "Revanche. Ich war schneller beim Pushen."),
                    hint = T("The floor ahead is already deleted. The stones that are left give way under you: hop on, do not stay.", "Der Boden vor dir ist schon gelöscht. Die Steine, die bleiben, geben nach: Hüpf weiter, bleib nicht stehen."),
                    traps = listOf(
                        trap(PastX(2.7f), Play(Card.SINKING), Fall('b'), Fall('d'), Fall('f'), Say(T("Merge conflict. I resolved it for you.", "Merge-Konflikt. Ich habe ihn für dich gelöst."))),
                        trap(Touch('a'), Fall('a'), delay = 0.35f),
                        trap(Touch('c'), Fall('c'), delay = 0.35f),
                        trap(Touch('e'), Fall('e'), delay = 0.35f),
                        trap(Touch('g'), Fall('g'), Say(T("Stale branch. Deleted.", "Veralteter Branch. Gelöscht.")), delay = 0.35f),
                        trap(Landed(21f, 24f), Hide('u'), delay = 0.7f),
                    ) + (0..9).map { k -> trap(Zone(21f, 7f, 23f, 9.5f), Hide('j' + k), delay = 0.3f + 0.3f * k) },
                ) {
                    fill(3..20, 15..17, '#')
                    fill(3..5, 15..17, 'a'); fill(6..7, 15..17, 'b'); fill(8..10, 15..17, 'c'); fill(11..12, 15..17, 'd')
                    fill(13..15, 15..17, 'e'); fill(16..17, 15..17, 'f'); fill(18..20, 15..17, 'g')
                },
            ),
        ) {
            border(); floor()
            for (k in 0..8) fill((3 + 2 * k)..(4 + 2 * k), 15..17, 'a' + k)
            fill(21..23, 13..14, 'u'); fill(24..30, 11..14)
            fill(2..23, 9..9)
            for (k in 0..9) fill((22 - 2 * k)..(23 - 2 * k), 9..9, 'j' + k)
            put(1, 14, 'P'); put(12, 8, 'D')
        },

        // 38 — clear view: you start on the right and the way home runs left, so the stairs are on the far left. A saw rolls in on the ground
        // from the left and must be hopped; as you land by the stairs the picture turns upside down and a second saw rolls in from behind:
        // left and right follow the screen now, so you flee to the left with the right key and climb the stairs mirrored. Upstairs the
        // picture is back, and a rope saw swings across the way to the door on the right
        // TWIST: Flip
        Level(
            name = T("Clear View", "Durchblick"),
            intro = T("Nice picture today. Sharper than ever.", "Sch\u00f6nes Bild heute. Sch\u00e4rfer als je zuvor."),
            hint = T("When the picture turns over, left and right follow the screen: run from the saw with the key that points to the stairs on screen.", "Wenn das Bild kippt, folgen links und rechts dem Bildschirm: Lauf vor der S\u00e4ge mit der Taste, die auf dem Bildschirm zur Treppe zeigt."),
            traps = listOf(
                trap(BeforeX(27.5f), Saw(0.5f, 14.4f, 6.5f, 0f, 0.62f), Say(T("Oncoming traffic. Sharper than ever.", "Gegenverkehr. Sch\u00e4rfer als je zuvor."))),
                trap(Landed(8.4f, 20f), Play(Card.UPSIDE_DOWN), Flip(1.2f), Say(T("Better view from here.", "Von hier hat man die bessere Aussicht."))),
                trap(Landed(8.4f, 20f), Saw(33.5f, 14.4f, -6f, 0f, 0.62f), Say(T("The saw prefers the original orientation.", "Die S\u00e4ge mag lieber die Originalausrichtung."))),
                trap(Zone(6f, 7f, 7.5f, 9.5f), PathSaw(7f, 18f to 8.4f, 18f to 4.6f, delay = 0.35f), Say(T("Skipping rope for adults.", "Seilspringen f\u00fcr Erwachsene."))),
            ),
        ) {
            border(); floor()
            fill(5..7, 13..14); fill(1..3, 11..14)
            fill(5..29, 9..9)
            put(29, 14, 'P'); put(19, 8, 'D')
        },

        // 39 — hardware store: a ceiling tile drops in aisle 6 and is hopped, then the stairs go up three floors (the middle step is
        // self-assembly and comes apart under whoever stands on it) to the platform, where
        // the floor sample at its edge is gone for a moment (wait for it); beyond it the pit is too wide to jump, and the only way across
        // is the shelf at its edge, which slides with the tilt of your phone (a ferry): the far bank gives way as you ride in, so jump
        // off the ferry's end onto the high ledge with the door, up on the right
        // MOTION: Tilt (one of the two phone-motion levels of the act)
        Level(
            name = T("Hardware Store", "Baumarkt"),
            intro = T("I built something wide. No instructions.", "Ich habe was Breites gebaut. Ohne Anleitung."),
            hint = T("The shelf at the edge of the pit slides when you tilt the phone. Hold it tilted, and jump off the shelf's end.", "Das Regal am Rand des Lochs gleitet, wenn du das Handy kippst. Halte es schr\u00e4g und spring am Ende vom Regal ab."),
            start = listOf(Tilt('a', left = 0f, right = 3f, speed = 3.4f)),
            traps = listOf(
                trap(PastX(3f), Play(Card.HEADBUTT), Move('s', 0f, 13f, 24f), Shake(0.4f), Say(T("Aisle 6: ceiling tiles. Falling prices.", "Gang 6: Deckenplatten. Fallende Preise."))),
                trap(Landed(12f, 14f), Move('c', 0f, 8f, 8f), Say(T("Aisle 7: self-assembly steps. Assembly not included.", "Gang 7: Stufen zum Selbstaufbau. Aufbau nicht inklusive.")), delay = 0.3f),
                trap(Landed(16f, 20f), Blink('h', on = 1.2f, off = 1.1f, phase = 1.2f), Say(T("Floor sample. Not for sale. Back in a minute.", "Bodenmuster. Unverk\u00e4uflich. Gleich wieder da."))),
                trap(PastX(25f), Move('g', 0f, 14f, 40f), Say(T("The far bank is a display model.", "Das andere Ufer ist ein Ausstellungsst\u00fcck."))),
            ),
        ) {
            border(); floor(); pit(16..30)
            fill(6..8, 1..1, 's')
            fill(10..11, 13..14); pit(12..13); fill(12..13, 11..17, 'c'); fill(14..15, 9..14)
            fill(16..19, 7..8); fill(20..21, 7..7, 'h'); fill(22..24, 7..7, 'a'); fill(28..28, 7..7, 'g'); fill(29..30, 5..14)
            put(2, 14, 'P'); put(30, 4, 'D')
        },

        // 40 — boot sequence: spawn on a shelf over the void, the door far below on the ground at the other end. At the first steps the
        // keys swap (kernel panic: left is right) and the memory test sweeps a beam across the shelf; run when it goes dark and do not
        // dawdle, a second beam near the end of the shelf keeps almost the same rhythm. At the end of the shelf the keys come back
        // ("restored. Probably."), you drop down the shaft (where you land is a bad sector a moment later) and run back along the ground
        // floor to the door in the middle of it (not under the start), where the keys swap once more halfway, with a pit in the way that has
        // to be hopped with the new hands
        // TWIST: Swap (the one swapped-controls level of the act), Laser (the one beam)
        Level(
            name = T("Boot Sequence", "Systemstart"),
            intro = T("Everything is fine. Really.", "Alles in Ordnung. Wirklich."),
            hint = T("After the panic left is right. Wait for the beam to go dark. The way home is down the shaft and back: the keys change twice more.", "Nach der Panik ist links rechts. Warte, bis der Strahl dunkel ist. Der Heimweg führt den Schacht hinunter und zurück: Die Tasten wechseln noch zweimal."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.TWISTED), Swap(true), Shake(1.2f), Say(T("KERNEL PANIC - not syncing", "KERNEL PANIC - nicht synchronisiert"))),
                trap(PastX(8f), Laser('A', 14 to 3, 14 to 8, on = 1.4f, off = 1.6f, delay = 0.2f), Say(T("Memory test. Please do not cross the beam.", "Speichertest. Bitte den Strahl nicht kreuzen."))),
                trap(PastX(14.5f), Laser('B', 22 to 3, 22 to 8, on = 0.8f, off = 1.6f, phase = 1.3f), Say(T("Memory test, part two. Same rhythm. Almost.", "Speichertest, Teil zwei. Gleicher Takt. Fast."))),
                trap(Zone(23f, 7f, 26.5f, 9.5f), Swap(false), Say(T("Controls restored. Probably.", "Steuerung wiederhergestellt. Vermutlich."))),
                trap(Landed(26f, 31f), Laser('C', 30 to 10, 30 to 15, on = 0.5f, off = 60f, delay = 0.6f), Say(T("Disk check. Sector 30 is bad. Move.", "Datenträgerprüfung. Sektor 30 ist defekt. Weiter."))),
                trap(Zone(21.5f, 12f, 23.5f, 15.5f), Swap(true), Say(T("Rebooting. Left is right again. Obviously.", "Neustart. Links ist wieder rechts. Offensichtlich."))),
            ),
        ) {
            border(); floor()
            fill(1..29, 9..9)
            pit(16..17)
            put(2, 8, 'P'); put(12, 14, 'D')
        },

        // 41 — TV night: the door is straight ahead behind a wall that is too high, so the way is the other way round. Left of the
        // start the shelf is missing a piece you cannot see (a bracket hangs next to the gap): the stalactites over the sofa come down
        // as you walk under them, butt the air under the bracket and the wall far away goes off air, with a bang, and the next stalactite
        // is on its way to the lane you have to run back along. Where the wall stood the last one waits.
        // R12: the hidden shelf piece, U2: the ceiling falls
        Level(
            name = T("TV Night", "Fernsehabend"),
            intro = T("Reception is great today. No static.", "Der Empfang ist heute super. Kein Rauschen."),
            hint = T("The wall is too high, the way is behind you: a bracket on the left shows where the shelf is missing a piece. Butt the air.", "Die Wand ist zu hoch, der Weg liegt hinter dir: Eine Halterung links zeigt, wo dem Regal ein Stück fehlt. Spring gegen die Luft."),
            legend = mapOf('S' to ceilingSpike, 'T' to ceilingSpike, 'U' to ceilingSpike, 'V' to ceilingSpike, 'b' to ghost),
            traps = listOf(
                trap(BeforeX(13f), Fall('S'), Say(T("Technical difficulties.", "Bildstörung."))),
                trap(Touch('b'), Play(Card.GHOST_BLOCK), Hide('w'), Say(T("Hey! That piece was off air.", "He! Das Stück war nicht auf Sendung."))),
                trap(Touch('b'), Fall('T'), delay = 0.2f),
                trap(PastX(17.5f), Fall('U'), Say(T("Next channel: ceiling.", "Nächster Sender: Decke.")), delay = 0.35f),
                trap(Zone(18.6f, 12f, 19.9f, 15.5f), Fall('V'), Say(T("Pushing the TV doesn't help. Neither does this.", "Am Fernseher drücken hilft nicht. Das hier auch nicht."))),
            ),
        ) {
            border(); floor()
            put(7, 1, 'S'); put(8, 1, 'S'); put(9, 1, 'S'); put(7, 2, 'S'); put(8, 2, 'S'); put(9, 2, 'S')
            put(10, 1, 'T'); put(11, 1, 'T'); put(10, 2, 'T'); put(11, 2, 'T')
            put(24, 1, 'U'); put(25, 1, 'U'); put(24, 2, 'U'); put(25, 2, 'U'); put(18, 1, 'V'); put(19, 1, 'V')
            put(4, 10, '#'); put(5, 10, '#')
            put(4, 12, 'b'); put(5, 12, 'b')
            fill(20..21, 3..14, 'w')
            pit(27..28)
            put(17, 14, 'P'); put(30, 14, 'D')
        },

        // 42 — tailwind: the door stands far away on the ground, the way there leads down three floors, and on every floor a wall of
        // teeth is at your back: along the top floor (a block and a curb in the way) and off its end, along the middle floor back
        // to the left and off that end, and along the ground to the door, where a piece of the floor drops on the way
        // U8: the wall of teeth (wall-move), a floor piece as the second family
        Level(
            name = T("Tailwind", "Rückenwind"),
            intro = T("Look ahead. There's nothing behind you.", "Schau nach vorne. Hinter dir ist nichts."),
            hint = T("Every floor has its own wall, and every wall is slower than you. Keep moving and drop at the end of each floor.", "Jedes Stockwerk hat seine eigene Wand, und jede ist langsamer als du. Bleib in Bewegung und spring am Ende jedes Stockwerks runter."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT), 'Y' to Glyph(spike = true, dir = Dir.LEFT), 'X' to Glyph(spike = true, dir = Dir.RIGHT)),
            traps = listOf(
                trap(PastX(5.5f), Play(Card.STALKER), Chase('W', 6.8f, left = 0f, right = 20f), Say(T("Tailwind! Free of charge. So are the brakes.", "Rückenwind! Gratis. Bremsen auch."))),
                trap(Landed(22f, 30f), Move('Y', -20f, 0f, 5.5f), Say(T("Second floor, second draft.", "Zweites Stockwerk, zweiter Luftzug."))),
                trap(Zone(1f, 12f, 9.5f, 15.5f), Move('X', 27f, 0f, 5.5f), Say(T("Ground floor. The draft is stronger here.", "Erdgeschoss. Hier zieht es stärker."))),
                trap(Zone(15.6f, 13f, 18.5f, 15.5f), Fall('a'), Say(T("The floor is on a diet.", "Der Boden macht Diät."))),
            ),
            // rematch: dead calm on the ground floor. The walls blow upstairs as before, but down below the way is three stones
            // over the pit that blink in turns: run on as in round 1 and you jump into the gap; this time the floor has to be waited for
            rematch = listOf(
                Round(
                    T("Rematch. The wind dropped.", "Revanche. Der Wind hat sich gelegt."),
                    traps = listOf(
                        trap(PastX(5.5f), Play(Card.STALKER), Chase('W', 6.8f, left = 0f, right = 20f), Say(T("Tailwind again. Only upstairs.", "Wieder Rückenwind. Nur oben."))),
                        trap(Landed(22f, 30f), Move('Y', -20f, 0f, 5.5f), Say(T("Still blowing on this floor.", "Auf diesem Stockwerk weht es noch."))),
                        trap(Zone(1f, 12f, 9.5f, 15.5f), Blink('p', on = 2.7f, off = 0.8f, phase = 2.2f),
                            Say(T("Dead calm downstairs. The floor takes its time.", "Unten Flaute. Der Boden lässt sich Zeit."))),
                        trap(Touch('p'), Blink('q', on = 1.8f, off = 1.2f, phase = 2.5f)),
                        trap(Touch('q'), Blink('r', on = 2f, off = 1f, phase = 2f), Say(T("Next stone. Same time zone.", "Nächster Stein. Gleiche Zeitzone."))),
                    ),
                ) {
                    pit(12..27); fill(12..15, 15..15, 'p'); fill(18..21, 15..15, 'q'); fill(24..27, 15..15, 'r')
                    fill(1..1, 11..14, '.'); fill(24..25, 14..14, '.')
                },
            ),
        ) {
            border(); floor()
            fill(1..22, 5..5); fill(10..11, 3..4); fill(16..17, 4..4)
            fill(9..30, 10..10); fill(19..20, 9..9)
            fill(1..1, 1..4, 'W'); fill(30..30, 6..9, 'Y'); fill(1..1, 11..14, 'X')
            pit(13..14); fill(19..20, 15..17, 'a'); fill(24..25, 14..14)
            put(4, 4, 'P'); put(29, 14, 'D')
        },

        // 43 — git blame: a rope saw on the way, then the wall: it is open only while somebody stands on the pad next to it, and the
        // pad is where blame comes from: a saw rolls in from behind the moment you step on it, and another swings behind the wall.
        // Dash through the open wall (it waits while you are inside), and where the ground ends a lift takes you up to the ledge with the
        // door: it starts as you step on it, and a saw from the front rolls in under it at the same moment, so whoever waits at the foot
        // of the lift is run over; and the lift goes on too far, into the spikes in the ceiling, for whoever stays on it.
        // R2: hold pad, U7: the saws; the lift is the second family (the ghost of the old level is gone: it has nothing to replay in a
        // clean run, so it was decoration)
        Level(
            name = T("git blame", "git blame"),
            intro = T("I keep a log. Of everything you do.", "Ich f\u00fchre Buch. \u00dcber alles, was du tust."),
            hint = T("The wall is open only while you stand on the pad, and the pad is what sets the saws rolling. Look through the wall, then dash. Take the lift before the saw takes you.", "Die Wand ist nur offen, solange du auf dem Schalter stehst, und der Schalter setzt die S\u00e4gen in Gang. Schau durch die Wand, dann renn. Nimm den Aufzug, bevor dich die S\u00e4ge nimmt."),
            start = listOf(Circuit('w'), Pad('1', at = 10 to 14, circuits = "w", mode = PadMode.HOLD)),
            traps = listOf(
                trap(PastX(3f), Play(Card.DEVIL_SAW), PathSaw(7f, 7f to 14.4f, 7f to 10.6f, delay = 0.6f), Say(T("git blame: the rope was you.", "git blame: Das Seil warst du."))),
                trap(Pressed('1'), Saw(-1.5f, 14.4f, 5.5f, 0f, 0.62f), PathSaw(7f, 18f to 10.6f, 18f to 14.4f, delay = 0.42f),
                    Say(T("Commit accepted. So is the blame.", "Commit angenommen. Die Schuld auch."))),
                trap(Zone(19.5f, 12f, 21.5f, 15.5f), Saw(36f, 14.4f, -9f, 0f, 0.62f), Say(T("Blame from the front, too. I'm thorough.", "Schuld kommt auch von vorn. Ich bin gr\u00fcndlich."))),
                trap(Zone(24f, 12f, 26.9f, 15.5f), Move('a', 0f, -8f, 3f), Say(T("Going up. The history goes with you.", "Es geht nach oben. Die Historie f\u00e4hrt mit."))),
            ),
        ) {
            border(); floor()
            fill(11..12, 1..14, 'w')
            fill(24..26, 15..17, 'a'); fill(27..30, 9..14)
            for (x in 24..26) put(x, 6, 'v')
            put(2, 14, 'P'); put(29, 8, 'D')
        },

        // 44 — wallflower: the pit is floored with four shy planks that only dance when you step on the one before: the next one is
        // gone for a moment, so you have to wait on the plank you stand on. The wall to the ceiling behind them does not budge, it
        // only comes down when you shake the phone, and the shaking brings down the bit of floor beyond it too, in front of the stairs.
        // MOTION: Shaken (one of the two phone-motion levels of the act). U16: shake. Blink planks (the one lethal family), a floor piece
        Level(
            name = T("Wallflower", "Mauerblümchen"),
            intro = T("The wall won't budge. Neither will I.", "Die Wand bewegt sich nicht. Ich auch nicht."),
            hint = T("Each plank is shy: it is gone until you stand on the one before it. Wait for it. The wall comes down if you shake the phone.", "Jede Diele ist schüchtern: Sie fehlt, bis du auf der davor stehst. Warte auf sie. Die Wand fällt, wenn du das Handy schüttelst."),
            traps = listOf(
                trap(PastX(6.5f), Blink('p', on = 3.4f, off = 1f), Say(T("Planks! Shy ones.", "Dielen! Schüchterne."))),
                trap(Touch('p'), Blink('q', on = 2.6f, off = 1.15f, phase = 2.6f), Say(T("Wallflowers only dance when nobody looks.", "Mauerblümchen tanzen nur, wenn keiner hinsieht."))),
                trap(Touch('q'), Blink('r', on = 2.6f, off = 1.15f, phase = 2.6f)),
                trap(Touch('r'), Blink('s', on = 2.6f, off = 1.15f, phase = 2.6f)),
                trap(Shaken, Play(Card.CRUMBLE), Hide('a'), Say(T("Hey! Stop that!", "He! Lass das!"))),
                trap(Shaken, Fall('f'), Say(T("Flowers wilt. So does floor.", "Blumen welken. Boden auch.")), delay = 0.3f),
            ),
        ) {
            border(); floor(); pit(5..24)
            fill(5..9, 15..15, 'p'); fill(10..14, 15..15, 'q'); fill(15..19, 15..15, 'r'); fill(20..24, 15..15, 's')
            fill(25..25, 1..14, 'a'); fill(26..26, 15..17, 'f')
            fill(27..28, 13..14); fill(29..29, 11..14); fill(30..30, 9..14)
            put(1, 14, 'P'); put(30, 8, 'D')
        },

        // 45 — sudo rm -rf /: you wanted root, so the root of the floor goes. A descent: you start on the top floor on the right and run left;
        // the floor you stand on is deleted behind you, two pieces ahead of your feet (the second one late, when you are almost on it),
        // and under the top floor lies a shelf of spikes that catches whoever falls through. The top floor ends over a shaft: drop down
        // it to the ground floor and run back to the right, where one more piece of the ground is deleted ahead of you, to the door in the
        // middle of the ground floor
        // EASTER EGG: sudo rm -rf /. U1: the floor goes (drop), top to bottom
        Level(
            name = T("sudo rm -rf /", "sudo rm -rf /"),
            intro = T("Please log in as root. Password: hunter2.", "Bitte als root anmelden. Passwort: hunter2."),
            hint = T("The floor is deleted just ahead of you, top floor first: hop early, before the hole shows, drop down the shaft, and hop again on the ground. The shelf in between is all spikes.", "Der Boden wird direkt vor dir gelöscht, zuerst oben: Spring früh, bevor das Loch da ist, lass dich durch den Schacht fallen und spring unten wieder. Das Regal dazwischen besteht nur aus Stacheln."),
            traps = listOf(
                trap(BeforeX(25.6f), Play(Card.SINKING), Hide('a'), Say(T("rm: removing '/usr' ... done. You wanted root.", "rm: entferne '/usr' ... erledigt. Du wolltest doch root."))),
                trap(BeforeX(28.5f), Hide('s'), Say(T("rm: removing '~' ... you were standing on it.", "rm: entferne '~' ... du standest drauf.")), delay = 0.6f),
                trap(BeforeX(17.6f), Hide('b'), Say(T("rm: removing '/lib' ... done.", "rm: entferne '/lib' ... erledigt."))),
                trap(BeforeX(10.4f), Hide('c'), Say(T("rm: removing '/etc' ... done. Config is overrated.", "rm: entferne '/etc' ... erledigt. Konfiguration wird überschätzt."))),
                trap(Zone(9.5f, 12f, 10.5f, 15.5f), Hide('d'), Say(T("rm: removing '/bin' ... done. Who needs those.", "rm: entferne '/bin' ... erledigt. Wer braucht die schon."))),
            ),
        ) {
            border(); floor()
            fill(7..21, 5..5); fill(22..24, 5..5, 'a'); fill(25..26, 5..5); fill(27..30, 5..5, 's')
            fill(14..16, 5..5, 'b'); fill(8..9, 5..5, 'c')
            fill(7..30, 10..10)
            for (x in 7..30) put(x, 9, '^')
            fill(11..13, 15..17, 'd')
            put(29, 4, 'P'); put(18, 14, 'D')
        },

        // 46 — home stretch: the goal is on the left, behind a wall nobody climbs, so the way out is the switch behind you, at the far
        // end of the room on the right. It cuts the wall's power, and it is the one that sets everything off: a laser fence on the way
        // back from the switch, spikes that grow out of the floor right in front of it a moment later (hop them), and a beam that
        // scans the switch itself for whoever stays on it.
        // Behind them two more fences wait, and whoever leans on the goal wall is scanned.
        // Rematch: the old switch is a bluff (Mephi's card flies in and turns over); the real one has moved up onto a ledge above it
        // R1: switch opens the door, U15: the help is the trap; the lasers are the lethal family, the spikes the second
        Level(
            name = T("Home Stretch", "Zielgerade"),
            intro = T("That's the goal ahead. You can do it.", "Da vorne ist das Ziel. Du schaffst das."),
            hint = T("The goal is a trick: the way out is the switch behind you. And whatever the switch does, it does to you.", "Das Ziel ist ein Trick: Der Ausweg ist der Schalter hinter dir. Und was der Schalter tut, tut er dir."),
            legend = mapOf('X' to hiddenSpike),
            start = listOf(Circuit('w'), Pad('1', at = 30 to 14, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SPIKE_SEED), Laser('A', 25 to 1, 25 to 14, on = 0.6f, off = 1.8f, delay = 0.3f),
                    Say(T("Oh, you found the button. Boo.", "Oh, du hast den Knopf gefunden. Buh."))),
                trap(Pressed('1'), Show('X'), Say(T("Spikes grow where you step. You stepped.", "Stacheln wachsen, wo du hintrittst. Du bist getreten.")), delay = 0.4f),
                trap(Pressed('1'), Laser('E', 30 to 1, 30 to 15, on = 0.5f, off = 60f, delay = 1.1f)),
                trap(BeforeX(18.8f), Laser('C', 11 to 1, 11 to 14, on = 1.4f, off = 1.8f, delay = 0.1f), Say(T("Customer service. Please hold the line.", "Kundendienst. Bitte bleiben Sie in der Leitung."))),
                trap(BeforeX(9f), Laser('B', 4 to 1, 4 to 14, on = 1f, off = 1.6f, delay = 0.3f)),
                trap(Zone(12f, 12f, 12.8f, 15.5f), Laser('D', 12 to 1, 12 to 15, on = 0.5f, off = 60f, delay = 0.5f), Say(T("Leaning on the goal? Security scan.", "Am Ziel lehnen? Sicherheitsscan."))),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. I moved the button.", "Revanche. Ich hab den Knopf versetzt."),
                    start = listOf(Circuit('w'), Pad('1', at = 29 to 10, circuits = "w", mode = PadMode.OFF)),
                    traps = listOf(
                        trap(Zone(21.5f, 12f, 22.9f, 15.5f), Bluff(Card.SPIKE_SEED), Say(T("That button retired. Think higher.", "Der Knopf ist in Rente. Denk höher."))),
                        trap(Pressed('1'), Laser('A', 21 to 1, 21 to 14, on = 0.6f, off = 1.8f, delay = 0.3f), Say(T("Fine. Up there.", "Na gut. Da oben."))),
                        trap(Pressed('1'), Fall('l'), Say(T("The ledge was only a loan.", "Das Regal war nur geliehen.")), delay = 1.9f),
                        trap(BeforeX(18.8f), Laser('C', 11 to 1, 11 to 14, on = 1.4f, off = 1.8f, delay = 0.1f)),
                        trap(BeforeX(9f), Laser('B', 4 to 1, 4 to 14, on = 1f, off = 1.6f, delay = 0.3f)),
                        trap(Zone(12f, 12f, 12.8f, 15.5f), Laser('D', 12 to 1, 12 to 15, on = 0.5f, off = 60f, delay = 0.5f), Say(T("Still leaning? Still scanning.", "Lehnst du immer noch? Ich scanne immer noch."))),
                    ),
                ) { put(27, 14, '.'); pit(23..30); fill(25..30, 11..11, 'l'); fill(23..24, 13..14) },
            ),
        ) {
            border(); floor()
            fill(10..11, 1..14, 'w')
            put(27, 14, 'X')
            put(20, 14, 'P'); put(2, 14, 'D')
        },

        // 47 — sudo make me a sandwich: the goal is on the right behind two walls that only open for the one who finds the root blocks,
        // and those are on the left and on a ledge: a block under the bread floating over the left ledge and one under the pickle over
        // the second ledge. Each block makes a wall rise slowly, and each time Mephi sends a wall to keep you honest: one from behind
        // at the first, one from the front at the second, so whoever dawdles on the ledge or at the gate is squashed. Whoever leans on
        // the first wall gets the cheese from above, and the first step of the right ledges has a lid of its own.
        // Rematch: Mephi learned: whoever stands under the sandwich to fetch a block gets the bread on top of him; take it on the run
        // EASTER EGG: xkcd "sudo make me a sandwich". R12: the root blocks; U3: the walls that slide (wall-move) are the lethal family
        Level(
            name = T("sudo make me a sandwich", "sudo mach mir ein Sandwich"),
            intro = T("I'm hungry. Make me a sandwich.", "Ich habe Hunger. Mach mir ein Sandwich."),
            hint = T("Look up: the bread floats over a block you cannot see, and so does the pickle. Butt them from below, and then hurry.", "Schau nach oben: Das Brot schwebt über einem Block, den du nicht siehst, die Gurke auch. Spring von unten dagegen, und dann beeil dich."),
            legend = mapOf('k' to ghost, 'm' to ghost, 'Z' to Glyph(spike = true, dir = Dir.RIGHT), 'q' to Glyph(spike = true, dir = Dir.LEFT)),
            traps = listOf(
                trap(Touch('k'), Move('g', 0f, -13f, 1.0f), Move('Z', 12f, 0f, 3.5f), Say(T("Okay.", "Okay."))),
                trap(Zone(14.5f, 12f, 15f, 15.5f), Play(Card.HEADBUTT), Move('o', 0f, 12f, 14f), Say(T("Cheese. Sliced. From above.", "Käse. In Scheiben. Von oben."))),
                trap(Landed(17.5f, 19.9f), Move('n', 0f, 10f, 14f), Say(T("A lid for the first step. Keep climbing.", "Ein Deckel für die erste Stufe. Weiterklettern.")), delay = 0.2f),
                trap(Touch('m'), Move('h', 0f, -13f, 1.0f), Move('q', -8f, 0f, 2.0f), Say(T("Sandwich comes with a side of wall.", "Sandwich mit Beilage. Wand."))),
            ),
            // rematch: the same kitchen, but the bread and the pickle drop on whoever stands under them to butt the block: butt them on the run
            rematch = listOf(
                Round(
                    T("Rematch. Hungry again. This time the bread is in a hurry.", "Revanche. Schon wieder Hunger. Diesmal hat es das Brot eilig."),
                    hint = T("Butt the blocks on the run: whoever stands under the sandwich gets it on his head.", "Spring im Laufen gegen die Blöcke: Wer unter dem Sandwich stehen bleibt, kriegt es auf den Kopf."),
                    traps = listOf(
                        trap(Touch('k'), Move('g', 0f, -13f, 1.0f), Move('Z', 12f, 0f, 3.5f), Say(T("Same bread. Same hurry.", "Gleiches Brot. Gleiche Eile."))),
                        trap(Touch('k'), Move('z', 0f, 4f, 10f), delay = 0.6f),
                        trap(Zone(14.5f, 12f, 15f, 15.5f), Play(Card.HEADBUTT), Move('o', 0f, 12f, 14f)),
                        trap(Landed(17.5f, 19.9f), Move('n', 0f, 10f, 14f), delay = 0.2f),
                        trap(Touch('m'), Move('h', 0f, -13f, 0.9f), Move('q', -8f, 0f, 2.0f), Say(T("Sandwich comes with the pickle on top.", "Sandwich mit Gurke obendrauf."))),
                        trap(Touch('m'), Move('y', 0f, 5f, 14f)),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(10..11, 13..14); fill(8..9, 11..14); fill(3..7, 9..14)
            art(3, 2, "zzzzz", "z.z.z", "zzzzz", c = 'z')
            put(5, 6, 'k'); put(6, 6, 'k')
            fill(1..1, 1..14, 'Z')
            fill(15..15, 2..14, 'g')
            fill(14..14, 1..2, 'o')
            fill(18..19, 13..14); fill(20..22, 11..14)
            fill(18..19, 1..2, 'n')
            put(22, 8, 'm'); put(23, 8, 'm')
            art(20, 4, "yyyy", "yyyy", c = 'y')
            fill(24..24, 2..14, 'h')
            fill(30..30, 1..14, 'q')
            put(12, 14, 'P'); put(26, 14, 'D')
        },

        // 48 — exit (finale): the door is right there, behind a wall that only the switch opens. The switch is on the roof: up the stairs, under a
        // jump rope that comes down from above, press it (the roof's ceiling comes down on whoever stays on it), and the switch opens
        // the wall, and the room with it: the outer wall breaks open (U18) and the door slips away into a second room. Off the end of the
        // roof, a spike comes down on the way back, through the wall and after the door, which walks off to its new place: a spike, a
        // jump rope, a fence and a spike more.
        // Rematch: a second switch lies on the way back, wired the other way: whoever steps on it closes the wall again, so
        // you hop it; the door runs further this time, past two fences
        // R1: switch opens the door, R5: up and over, R7: the door at hand is the bait; U14+U18: the end that was a lie
        Level(
            name = T("Exit", "Ausgang"),
            intro = T("Last level. Promise.", "Letztes Level. Versprochen."),
            hint = T("A wall locks the door. The switch is on the roof: up the stairs, under the jump rope, and off the end. Then follow the door.", "Eine Wand versperrt die Tür. Der Schalter liegt auf dem Dach: Treppe hoch, unterm Seil durch, und am Ende runter. Dann lauf der Tür nach."),
            rooms = 2,
            legend = mapOf('S' to ceilingSpike, 'V' to ceilingSpike, 'U' to ceilingSpike, 'W' to ceilingSpike),
            start = listOf(Circuit('w'), Pad('1', at = 17 to 10, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(PastX(6.6f), Fall('S'), Say(T("Final level! Everything I've got.", "Letztes Level! Alles, was ich habe."))),
                trap(PastX(10.2f), PathSaw(7f, 14f to 6.6f, 14f to 10.4f), Say(T("Skip along. It is a roof party.", "Hüpf mit. Es ist eine Dachparty."))),
                trap(Pressed('1'), Move('r', 0f, 8f, 10f), Say(T("The switch! And what a view.", "Der Schalter! Und was für eine Aussicht."))),
                trap(Pressed('1'), Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 20) to 14, doorSpeed = 3.4f, line = T("Exit? There is a second floor plan.", "Ausgang? Es gibt noch einen zweiten Grundriss."))),
                trap(Landed(19f, 25f), Fall('V'), Say(T("Almost home. Almost.", "Fast zu Hause. Fast."))),
                trap(PastX(roomX(1, 0.3f)), Fall('U'), Say(T("Mind the step. The ceiling one.", "Vorsicht, Stufe. Die an der Decke."))),
                trap(PastX(roomX(1, 5.2f)), PathSaw(7f, roomX(1, 9f) to 10.6f, roomX(1, 9f) to 14.4f), Say(T("Extension. Rope included.", "Anbau. Seil inklusive."))),
                trap(PastX(roomX(1, 11f)), Laser('B', roomX(1, 15) to 1, roomX(1, 15) to 14, on = 1f, off = 1.6f, delay = 0.3f), Say(T("The extension has a fence. Of course.", "Der Anbau hat einen Zaun. Natürlich."))),
                trap(PastX(roomX(1, 13.5f)), Fall('W'), Say(T("Credits roll next. Eventually.", "Als Nächstes der Abspann. Irgendwann."))),
            ),
            rematch = listOf(
                Round(
                    T("Encore! Nobody leaves before the encore.", "Zugabe! Keiner geht vor der Zugabe."),
                    hint = T("A second switch lies on the way back. It is wired the other way round: hop it.", "Auf dem Rückweg liegt ein zweiter Schalter. Er ist andersherum verdrahtet: Spring drüber."),
                    start = listOf(
                        Circuit('w'), Pad('1', at = 17 to 10, circuits = "w", mode = PadMode.OFF),
                        Pad('2', at = 24 to 14, circuits = "w", mode = PadMode.TOGGLE),
                    ),
                    traps = listOf(
                        trap(PastX(6.6f), Fall('S'), Play(Card.GRAND_FINALE), Say(T("Same song. New ending.", "Gleiches Lied. Neues Ende."))),
                        trap(PastX(10.2f), PathSaw(7f, 14f to 6.6f, 14f to 10.4f), Say(T("From the top!", "Da capo!"))),
                        trap(Pressed('1'), Move('r', 0f, 8f, 10f), Say(T("Encore view. Same roof.", "Zugabe mit Aussicht. Gleiches Dach."))),
                        trap(Pressed('1'), Extend(into = 1, door = roomX(1, 28) to 14, line = T("The encore is in the next room.", "Die Zugabe ist im nächsten Raum."))),
                        trap(Landed(19f, 25f), Fall('V'), Say(T("Ah, you remembered the switch. I added another.", "Ah, du weißt noch, wo der Schalter ist. Ich habe noch einen dazugestellt."))),
                        trap(PastX(roomX(1, 0.3f)), Fall('U')),
                        trap(PastX(roomX(1, 4.2f)), PathSaw(7f, roomX(1, 8f) to 10.6f, roomX(1, 8f) to 14.4f)),
                        trap(PastX(roomX(1, 10.5f)), Laser('B', roomX(1, 14) to 1, roomX(1, 14) to 14, on = 1f, off = 1.6f, delay = 0.3f), Say(T("Two fences. The crowd insists.", "Zwei Zäune. Das Publikum besteht darauf."))),
                        trap(PastX(roomX(1, 15.6f)), Fall('W'), Say(T("Curtain.", "Vorhang."))),
                        trap(PastX(roomX(1, 21.5f)), Laser('C', roomX(1, 25) to 1, roomX(1, 25) to 14, on = 1f, off = 1.6f, delay = 0.3f)),
                    ),
                ) { room(1) { fill(18..19, 1..2, '.'); fill(20..21, 1..2, 'W') } },
            ),
        ) {
            border(); floor()
            room(0) {
                fill(4..5, 13..14); fill(6..7, 11..14); fill(6..18, 11..11)
                fill(26..27, 8..14, 'w')
                fill(10..11, 1..2, 'S'); fill(25..26, 1..2, 'V'); fill(16..18, 1..2, 'r')
                put(2, 14, 'P'); put(29, 14, 'D')
            }
            room(1) { fill(5..6, 1..2, 'U'); fill(18..19, 1..2, 'W') }
        },
    )
}
