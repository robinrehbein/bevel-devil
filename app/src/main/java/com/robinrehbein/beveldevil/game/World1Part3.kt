package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Flip
import com.robinrehbein.beveldevil.game.Action.FrameCrack
import com.robinrehbein.beveldevil.game.Action.Ghost
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
                trap(Landed(12.5f, 17f), Saw(31.5f, 14.4f, -6.5f, 0f, 0.62f), Say(T("Oncoming traffic. Not a trap. A vehicle.", "Gegenverkehr. Keine Falle. Ein Fahrzeug."))),
                trap(AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 28) to 14, line = T("Clear road, you said. The road disagrees.", "Freie Fahrt, sagtest du. Die Straße sieht das anders."))),
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
                        trap(PastX(7f), Play(Card.DEVIL_SAW), PathSaw(7f, 17f to 14.4f, 17f to 10.6f, delay = 1.1f), Say(T("Jump rope. You skip, the traffic does not.", "Seilspringen. Du hüpfst, der Verkehr nicht."))),
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
        // pause (back button) opens the wall, and the floor behind it takes its break too. Then a second pit opens as you land, the way up
        // leads back along the upper floor, and there a slab from the ceiling takes its break right where you hurry
        // TWIST: PauseTrap dodge + Resumed
        Level(
            name = T("Monday Morning", "Montagmorgen"),
            intro = T("Everything is a bit slow today. Coffee is brewing.", "Heute ist alles etwas langsam. Der Kaffee läuft."),
            hint = T("The wall is closed for a break. Pause the game: the back button works, the button on screen runs away.", "Die Wand hat Pause. Pausiere das Spiel: Die Zurück-Taste geht, der Knopf auf dem Bildschirm läuft weg."),
            traps = listOf(
                trap(After(0.2f), PauseTrap(PauseTrick.DODGE)),
                trap(Zone(5.6f, 10f, 8f, 15f), Say(T("Closed for a break. Pause the game to open it.", "Wegen Pause geschlossen. Pausier das Spiel, dann geht's auf."))),
                trap(Resumed(), Play(Card.CRUMBLE), Hide('w'), Hide('a'), Say(T("Refreshed? The floor took a break too.", "Erholt? Der Boden macht jetzt auch Pause."))),
                trap(Landed(12.5f, 15.5f), Fall('g'), Say(T("Second pit. Same excuse.", "Zweites Loch. Gleiche Ausrede.")), delay = 0.15f),
                trap(Zone(23.5f, 7f, 25.9f, 9.5f), FrameCrack(18, 0, 20, 0, warn = 0.3f), Say(T("The ceiling is on a break. In your direction.", "Die Decke macht Pause. In deine Richtung."))),
            ),
        ) {
            border(); floor()
            fill(8..8, 10..14, 'w'); fill(9..11, 15..17, 'a'); fill(16..18, 15..17, 'g')
            fill(22..25, 13..14); fill(26..30, 11..14)
            fill(2..25, 9..9)
            put(3, 14, 'P'); put(7, 8, 'D')
        },

        // 35 — breather: the door is far away and buffering. It jumps up onto the high ledge as you start to climb (23%), and when you land
        // up there it is gone again: back down on the ground, in the middle of the room, behind you. The way down is straight down
        // TWIST: DoorTo (the one fleeing door of the act)
        Level(
            name = T("Home Network", "Heimnetz"),
            intro = T("My internet is slow today. Don't mind me.", "Mein Internet ist heute lahm. Lass dich nicht stören."),
            hint = T("The door buffers: it goes where you are not. Climb up, and come straight back down.", "Die Tür lädt: Sie geht dorthin, wo du nicht bist. Klettere hoch und komm direkt wieder runter."),
            traps = listOf(
                trap(PastX(8f), Play(Card.SHY_DOOR), DoorTo(30, 8, speed = 14f), Say(T("Buffering... 23%", "Lädt... 23 %"))),
                trap(Landed(24f, 30.5f), DoorTo(30, 2, speed = 25f), Say(T("Connection lost. Retrying...", "Verbindung verloren. Neuer Versuch..."))),
                trap(Landed(24f, 30.5f), DoorTo(14, 2, speed = 25f), delay = 0.3f),
                trap(Landed(24f, 30.5f), DoorTo(14, 14, speed = 25f), delay = 1.0f),
                trap(Landed(24f, 30.5f), Say(T("100%. Was that so hard?", "100 %. War das so schwer?")), delay = 1.4f),
            ),
        ) {
            border(); floor()
            fill(17..20, 13..13); fill(21..23, 11..11); fill(25..30, 9..9)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 36 — gallery: a piece of the golden frame (a bit of the upper floor) breaks off over your head and lands on the path ahead; hopping
        // over it sprouts spikes behind it, so you have to stand on it first. Upstairs, on the way back along the upper floor, the next
        // piece of the frame comes down where you hurry, and the hole the first one left is waiting
        // TWIST: FrameCrack
        Level(
            name = T("Gallery", "Galerie"),
            intro = T("All real gold. Almost. Don't touch.", "Alles echtes Gold. Fast. Nicht anfassen."),
            hint = T("The frame drops pieces where you run. Let them land, then climb on. Spikes grow behind the first piece.", "Der Rahmen lässt Stücke fallen, wo du rennst. Lass sie landen, dann steig drauf. Hinter dem ersten wachsen Stacheln."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(3.1f), FrameCrack(9, 9, 11, 9, warn = 0.5f), Say(T("Crack.", "Knack."))),
                trap(Airborne(12.2f, 14.4f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Hand-painted. The spikes, too.", "Handgemalt. Die Stacheln auch."))),
                trap(Zone(21f, 7f, 23f, 9.5f), FrameCrack(16, 0, 18, 0, warn = 0.4f), Say(T("The ceiling is an exhibit. Do not touch.", "Die Decke ist ein Exponat. Nicht berühren."))),
            ),
        ) {
            border(); floor()
            fill(21..23, 13..14); fill(24..30, 11..14)
            fill(2..23, 9..9)
            put(12, 14, 'A'); put(13, 14, 'A')
            put(3, 14, 'P'); put(6, 8, 'D')
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
                trap(PastX(8f), Play(Card.COLLAPSE), Fall('a'), delay = 0.2f),
                trap(PastX(8f), Say(T("Force-pushed. Your commits are gone.", "Force-gepusht. Deine Commits sind weg."))),
                trap(Landed(21f, 24f), Hide('u'), Say(T("The merge base is deleted too.", "Die Merge-Basis ist auch gelöscht.")), delay = 0.7f),
                trap(Zone(21f, 7f, 23f, 9.5f), Say(T("Rewriting history upstairs, too.", "Oben wird die Historie auch umgeschrieben."))),
            ) + (1..8).map { k -> trap(PastX(8f), Fall('a' + k), delay = 0.2f + 0.33f * k) } +
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
            put(1, 14, 'P'); put(2, 8, 'D')
        },

        // 38 — clear view: a saw rolls in on the ground and must be hopped; as you land by the stairs the picture turns upside down and a
        // second saw rolls in from behind: left and right follow the screen now, so you flee to the right with the left key and climb the
        // stairs mirrored. Upstairs the picture is back, and a rope saw swings across the way home
        // TWIST: Flip
        Level(
            name = T("Clear View", "Durchblick"),
            intro = T("Nice picture today. Sharper than ever.", "Schönes Bild heute. Schärfer als je zuvor."),
            hint = T("When the picture turns over, left and right follow the screen: run from the saw with the key that points to the stairs on screen.", "Wenn das Bild kippt, folgen links und rechts dem Bildschirm: Lauf vor der Säge mit der Taste, die auf dem Bildschirm zur Treppe zeigt."),
            traps = listOf(
                trap(PastX(3.5f), Saw(31.5f, 14.4f, -6.5f, 0f, 0.62f), Say(T("Oncoming traffic. Sharper than ever.", "Gegenverkehr. Schärfer als je zuvor."))),
                trap(Landed(12f, 23.6f), Play(Card.UPSIDE_DOWN), Flip(1.2f), Say(T("Better view from here.", "Von hier hat man die bessere Aussicht."))),
                trap(Landed(12f, 23.6f), Saw(-1.5f, 14.4f, 6f, 0f, 0.62f), Say(T("The saw prefers the original orientation.", "Die Säge mag lieber die Originalausrichtung."))),
                trap(Zone(24.5f, 7f, 26f, 9.5f), PathSaw(7f, 14f to 8.4f, 14f to 4.6f, delay = 0.35f), Say(T("Skipping rope for adults.", "Seilspringen für Erwachsene."))),
            ),
        ) {
            border(); floor()
            fill(24..26, 13..14); fill(28..30, 11..14)
            fill(2..26, 9..9)
            put(2, 14, 'P'); put(6, 8, 'D')
        },

        // 39 — hardware store: a ceiling tile drops in aisle 6; the pit is too wide to jump, and the only way across is the shelf at its edge,
        // which slides with the tilt of your phone (a ferry): the far bank gives way as you pass, so jump off the ferry's end onto the
        // stairs. Upstairs, on the way back, the next ceiling tile comes down where you hurry
        // MOTION: Tilt (one of the two phone-motion levels of the act)
        Level(
            name = T("Hardware Store", "Baumarkt"),
            intro = T("I built something wide. No instructions.", "Ich habe was Breites gebaut. Ohne Anleitung."),
            hint = T("The shelf at the edge of the pit slides when you tilt the phone. Hold it tilted, and jump off the shelf's end.", "Das Regal am Rand des Lochs gleitet, wenn du das Handy kippst. Halte es schräg und spring am Ende vom Regal ab."),
            start = listOf(Tilt('a', left = 0f, right = 7f, speed = 8f)),
            traps = listOf(
                trap(PastX(3f), Play(Card.HEADBUTT), Move('s', 0f, 13f, 24f), Shake(0.4f), Say(T("Aisle 6: ceiling tiles. Falling prices.", "Gang 6: Deckenplatten. Fallende Preise."))),
                trap(PastX(17f), Fall('g'), Say(T("The far bank is a display model.", "Das andere Ufer ist ein Ausstellungsstück."))),
                trap(Zone(24.5f, 7f, 26f, 9.5f), Move('t', 0f, 7f, 18f), Shake(0.5f), Say(T("Ceiling tiles upstairs, too. Mind your head.", "Deckenplatten gibt's oben auch. Kopf einziehen.")), delay = 0.45f),
            ),
        ) {
            border(); floor(); pit(10..19)
            fill(10..12, 15..15, 'a'); fill(20..21, 15..17, 'g')
            fill(6..8, 1..1, 's'); fill(24..26, 1..1, 't')
            fill(22..26, 13..14); fill(28..30, 11..14)
            fill(10..26, 9..9)
            put(1, 14, 'P'); put(10, 8, 'D')
        },

        // 40 — boot sequence: at the first steps the keys swap (kernel panic: left is right), and the memory test sweeps a beam across the
        // corridor; up the stairs the keys come back ("restored. Probably."), and upstairs the second test beam waits at the same spot
        // as the keys do not matter any more
        // TWIST: Swap (the one swapped-controls level of the act)
        Level(
            name = T("Boot Sequence", "Systemstart"),
            intro = T("Everything is fine. Really.", "Alles in Ordnung. Wirklich."),
            hint = T("After the panic left is right. Wait for the beam to go dark, then cross. When the keys come back, they are back.", "Nach der Panik ist links rechts. Warte, bis der Strahl dunkel ist, dann geh durch. Wenn die Tasten zurückkommen, sind sie zurück."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.TWISTED), Swap(true), Shake(1.2f), Say(T("KERNEL PANIC - not syncing", "KERNEL PANIC - nicht synchronisiert"))),
                trap(PastX(9f), Laser('A', 16 to 10, 16 to 14, on = 0.9f, off = 1.6f, delay = 0.2f), Say(T("Memory test. Please do not cross the beam.", "Speichertest. Bitte den Strahl nicht kreuzen."))),
                trap(Landed(28f, 31f), Swap(false), Say(T("Controls restored. Probably.", "Steuerung wiederhergestellt. Vermutlich."))),
                trap(Zone(24.5f, 7f, 26f, 9.5f), Laser('B', 14 to 3, 14 to 8, on = 0.9f, off = 1.6f, delay = 0.8f), Say(T("Second test, upstairs. Same beam, less patience.", "Zweiter Test, oben. Gleicher Strahl, weniger Geduld."))),
            ),
        ) {
            border(); floor()
            fill(24..27, 13..14); fill(28..30, 11..14)
            fill(2..26, 9..9)
            put(2, 14, 'P'); put(6, 8, 'D')
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
            legend = mapOf('S' to ceilingSpike, 'T' to ceilingSpike, 'U' to ceilingSpike, 'b' to ghost),
            traps = listOf(
                trap(BeforeX(13f), Fall('S'), Say(T("Technical difficulties.", "Bildstörung."))),
                trap(Touch('b'), Play(Card.GHOST_BLOCK), Hide('w'), Say(T("Hey! That piece was off air.", "He! Das Stück war nicht auf Sendung."))),
                trap(Touch('b'), Fall('T'), delay = 0.2f),
                trap(PastX(17.5f), Fall('U'), Say(T("Next channel: ceiling.", "Nächster Sender: Decke.")), delay = 0.35f),
            ),
        ) {
            border(); floor()
            put(7, 1, 'S'); put(8, 1, 'S'); put(9, 1, 'S'); put(7, 2, 'S'); put(8, 2, 'S'); put(9, 2, 'S')
            put(10, 1, 'T'); put(11, 1, 'T'); put(10, 2, 'T'); put(11, 2, 'T')
            put(24, 1, 'U'); put(25, 1, 'U'); put(24, 2, 'U'); put(25, 2, 'U')
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
                trap(PastX(5.5f), Move('W', 20f, 0f, 6.8f), Say(T("Tailwind! Free of charge. So are the brakes.", "Rückenwind! Gratis. Bremsen auch."))),
                trap(Landed(22f, 30f), Move('Y', -20f, 0f, 5.5f), Say(T("Second floor, second draft.", "Zweites Stockwerk, zweiter Luftzug."))),
                trap(Zone(1f, 12f, 9.5f, 15.5f), Move('X', 27f, 0f, 5.5f), Say(T("Ground floor. The draft is stronger here.", "Erdgeschoss. Hier zieht es stärker."))),
                trap(Zone(15.6f, 13f, 18.5f, 15.5f), Play(Card.COLLAPSE), Fall('a'), Say(T("The floor is on a diet.", "Der Boden macht Diät."))),
            ),
            // rematch: dead calm on the ground floor. The walls blow upstairs as before, but down below the way is three stones
            // over the pit that blink in turns: run on as in round 1 and you jump into the gap; this time the floor has to be waited for
            rematch = listOf(
                Round(
                    T("Rematch. The wind dropped.", "Revanche. Der Wind hat sich gelegt."),
                    traps = listOf(
                        trap(PastX(5.5f), Move('W', 20f, 0f, 6.8f), Say(T("Tailwind again. Only upstairs.", "Wieder Rückenwind. Nur oben."))),
                        trap(Landed(22f, 30f), Move('Y', -20f, 0f, 5.5f), Say(T("Still blowing on this floor.", "Auf diesem Stockwerk weht es noch."))),
                        trap(Zone(1f, 12f, 9.5f, 15.5f), Play(Card.CRUMBLE), Blink('p', on = 3f, off = 1f), Blink('q', on = 1.8f, off = 1.2f, phase = 0.6f),
                            Say(T("Dead calm downstairs. The floor takes its time.", "Unten Flaute. Der Boden lässt sich Zeit."))),
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
        // Dash through the open wall between the two (it waits while you are inside), hop the saw that comes from the front on the
        // way to the stairs, and back along the upper floor with the last one at your heels. Your last attempt comes back as a
        // ghost on top of that (it needs a first attempt, so it is flavour for the first clean run).
        // R2: hold pad, U16: ghost; the one lethal family is the saw
        Level(
            name = T("git blame", "git blame"),
            intro = T("I keep a log. Of everything you do.", "Ich führe Buch. Über alles, was du tust."),
            hint = T("The wall is open only while you stand on the pad, and the pad is what sets the saws rolling. Look through the wall, then dash.", "Die Wand ist nur offen, solange du auf dem Schalter stehst, und der Schalter setzt die Sägen in Gang. Schau durch die Wand, dann renn."),
            start = listOf(Circuit('w'), Pad('1', at = 13 to 14, circuits = "w", mode = PadMode.HOLD)),
            traps = listOf(
                trap(After(0f), Ghost(1f)),
                trap(PastX(3.5f), Play(Card.DEVIL_SAW), PathSaw(7f, 8f to 14.4f, 8f to 10.6f, delay = 0.9f), Say(T("git blame: the rope was you.", "git blame: Das Seil warst du."))),
                trap(Pressed('1'), Saw(-1.5f, 14.4f, 5.5f, 0f, 0.62f), PathSaw(7f, 19f to 14.4f, 19f to 10.6f, delay = 0.2f),
                    Say(T("Commit accepted. So is the blame.", "Commit angenommen. Die Schuld auch."))),
                trap(PastX(17f), Saw(33.5f, 14.4f, -7f, 0f, 0.62f), Say(T("Blame from the front, too. I'm thorough.", "Schuld kommt auch von vorn. Ich bin gründlich."))),
                trap(Zone(24.5f, 7f, 27f, 9.5f), Saw(33.5f, 8.4f, -6f, 0f, 0.62f), Say(T("Upstairs, same history.", "Oben, gleiche Historie."))),
                trap(Zone(17f, 7f, 20f, 9.5f), Saw(-1.5f, 8.4f, 7f, 0f, 0.62f), Say(T("Squeezed. Version control is hard.", "Eingeklemmt. Versionskontrolle ist schwer."))),
            ),
        ) {
            border(); floor()
            fill(14..15, 10..14, 'w')
            fill(2..27, 9..9); fill(23..27, 13..14); fill(28..30, 11..14)
            put(2, 14, 'P'); put(3, 8, 'D')
        },

        // 44 — wallflower: the pit is floored with four shy planks that only dance when you step on the one before: the next one is
        // gone for a moment, so you have to wait on the plank you stand on. The wall to the ceiling behind them does not budge, it
        // only comes down when you shake the phone, and the bit of floor beyond it drops as you pass, in front of the stairs to the door.
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
                trap(Shaken, Play(Card.COLLAPSE), Hide('a'), Say(T("Hey! Stop that!", "He! Lass das!"))),
                trap(PastX(24.6f), Fall('f'), Say(T("Flowers wilt. So does floor.", "Blumen welken. Boden auch."))),
            ),
        ) {
            border(); floor(); pit(5..24)
            fill(5..9, 15..15, 'p'); fill(10..14, 15..15, 'q'); fill(15..19, 15..15, 'r'); fill(20..24, 15..15, 's')
            fill(25..25, 1..14, 'a'); fill(26..26, 15..17, 'f')
            fill(27..28, 13..14); fill(29..29, 11..14); fill(30..30, 9..14)
            put(1, 14, 'P'); put(30, 8, 'D')
        },

        // 45 — the floor is deleted when you are halfway; the shelf above it is not, but climbing onto it sprouts spikes
        // EASTER EGG: sudo rm -rf /
        Level(
            name = T("sudo rm -rf /", "sudo rm -rf /"),
            intro = T("Please log in as root. Password: hunter2.", "Bitte als root anmelden. Passwort: hunter2."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(PastX(14f), Play(Card.COLLAPSE), Fall('f'), Shake(1.5f), PauseTrap(PauseTrick.SWAP), Say(T("rm: removing '/' ... done. You wanted root.", "rm: entferne '/' ... erledigt. Du wolltest doch root."))),
                trap(Airborne(2.5f, 7f), Show('A')),
            ),
        ) {
            border()
            fill(1..30, 15..17, 'f')
            fill(4..30, 13..13)
            put(9, 12, 'A'); put(10, 12, 'A')
            put(12, 12, 'B'); put(13, 12, 'B')
            put(20, 12, '^')
            put(2, 14, 'P'); put(29, 12, 'D')
        },

        // 46 — the wall is unclimbable; the way out is behind you
        Level(
            name = T("Home Stretch", "Zielgerade"),
            intro = T("That's the goal ahead. You can do it.", "Da vorne ist das Ziel. Du schaffst das."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('k'), Play(Card.DECOY), Hide('w'), Say(T("Oh, you found the button. Boo.", "Oh, du hast den Knopf gefunden. Buh."))),
                trap(PastX(20.5f), Show('A')),
            ),
            // rematch: the old button is a bluff; the real one hides in the air above the start
            rematch = listOf(
                Round(
                    T("Rematch. I moved the button.", "Revanche. Ich hab den Knopf versetzt."),
                    legend = mapOf('g' to ghost),
                    traps = listOf(
                        trap(Touch('k'), Bluff(Card.DECOY), Say(T("That button retired. Think higher.", "Der Knopf ist in Rente. Denk höher."))),
                        trap(Touch('g'), Hide('w'), Say(T("Fine. Up there.", "Na gut. Da oben."))),
                        trap(PastX(20.5f), Show('A')),
                    ),
                ) { put(10, 12, 'g') },
            ),
        ) {
            border(); floor()
            fill(15..16, 3..14, 'w')
            put(2, 14, 'k')
            put(25, 14, 'A'); put(26, 14, 'A')
            put(10, 14, 'P'); put(29, 14, 'D')
        },

        // 47 — the wall only opens for the one who finds the root block
        // EASTER EGG: xkcd "sudo make me a sandwich"
        Level(
            name = T("sudo make me a sandwich", "sudo mach mir ein Sandwich"),
            intro = T("I'm hungry. Make me a sandwich.", "Ich habe Hunger. Mach mir ein Sandwich."),
            legend = mapOf('k' to ghost, 'A' to hiddenSpike, 'C' to hiddenSpike),
            traps = listOf(
                trap(Touch('k'), Play(Card.GHOST_BLOCK), Hide('w'), Say(T("Okay.", "Okay."))),
                trap(PastX(13.5f), Show('A')),
                trap(Landed(19f, 22f), Fall('f')),
            ),
            // rematch: Mephi learned: whoever stands under the sandwich to fetch it lands on spikes; take it on the run
            rematch = listOf(
                Round(
                    T("Rematch. I'm hungry again.", "Revanche. Ich hab schon wieder Hunger."),
                    traps = listOf(
                        trap(Touch('k'), Play(Card.GHOST_BLOCK), Hide('w'), Show('C'), Say(T("Sandwich comes with a side of spikes.", "Sandwich mit Beilage. Spitzer Beilage."))),
                        trap(PastX(13.5f), Show('A')),
                        trap(Landed(19f, 22f), Fall('f')),
                    ),
                ),
            ),
        ) {
            border(); floor()
            put(17, 14, 'A'); put(18, 14, 'A')
            put(10, 14, 'C')
            fill(23..23, 15..17, 'f')
            fill(20..21, 3..14, 'w')
            put(10, 12, 'k')
            // the sandwich, hovering above the root block
            art(8, 4, "#####", "#.#.#", ".###.", "#####")
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 48 — finale: a blinking bridge, then the door ends the game. The credits roll, become stairs, the door went home.
        // TWIST: FakeWin (credits) + Roll
        Level(
            name = T("Exit", "Ausgang"),
            intro = T("Last level. Promise.", "Letztes Level. Versprochen."),
            legend = mapOf('c' to hiddenSolid, 'A' to hiddenSpike),
            traps = listOf(
                trap(Airborne(15.2f, 18.6f), Play(Card.GRAND_FINALE), Show('A'), Say(T("Final level! Everything I've got.", "Letztes Level! Alles, was ich habe."))),
                trap(AtDoor, FakeWin(FakeEnd.CREDITS, 'c', DoorTo(1, 8), Roll(3f, 2))),
            ),
            start = listOf(Blink('a', on = 2.4f, off = 1f)),
            // round 2 (encore, a collapse): no fake, but the floor after the bridge drops: jump off its end this time.
            // round 3 (second encore, a shy door): the door goes home, back over the bridge
            rematch = listOf(
                Round(
                    T("Encore! Nobody leaves before the encore.", "Zugabe! Keiner geht vor der Zugabe."),
                    start = listOf(Blink('a', on = 2.4f, off = 1f, phase = 1.2f)),
                    traps = listOf(
                        trap(After(0.4f), Say(T("Same song. New ending.", "Gleiches Lied. Neues Ende."))),
                        trap(Touch('e'), Play(Card.COLLAPSE), Fall('e'), delay = 0.08f),
                    ),
                ) { fill(17..18, 15..17, 'e') },
                Round(
                    T("Second encore. The crowd insists. I'm the crowd.", "Zweite Zugabe. Das Publikum will es. Ich bin das Publikum."),
                    start = listOf(Blink('a', on = 2.4f, off = 1f, phase = 1.2f)),
                    traps = listOf(
                        trap(After(0.4f), Say(T("From the top!", "Da capo!"))),
                        // up and over your head first, then home to the start
                        trap(PastX(23f), Play(Card.SHY_DOOR), DoorTo(28, 4, speed = 20f), Say(T("Encore means from the top.", "Zugabe heißt: von vorn."))),
                        trap(PastX(23f), DoorTo(3, 14, speed = 14f), delay = 0.6f),
                    ),
                ),
            ),
        ) {
            border(); floor(); pit(10..16)
            put(19, 14, 'A'); put(20, 14, 'A')
            fill(10..16, 15..15, 'a')
            fill(1..13, 9..9, 'c'); fill(15..19, 11..11, 'c'); fill(22..26, 13..13, 'c')
            put(2, 14, 'P'); put(28, 14, 'D')
        },
    )
}
