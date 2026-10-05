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
                trap(Zone(23.5f, 7f, 25.9f, 9.5f), Fall('s'), Say(T("The ceiling is on a break. In your direction.", "Die Decke macht Pause. In deine Richtung."))),
            ),
        ) {
            border(); floor()
            fill(8..8, 10..14, 'w'); fill(9..11, 15..17, 'a'); fill(16..18, 15..17, 'g')
            fill(22..25, 13..14); fill(26..30, 11..14)
            fill(2..25, 9..9); fill(20..22, 1..1, 's')
            put(3, 14, 'P'); put(2, 8, 'D')
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

        // 37 — reach the door and I overwrite history: it moves back to the start and spikes appear
        // EASTER EGG: git push --force
        Level(
            name = T("git push --force", "git push --force"),
            intro = T("Mephi already pushed. You just need to pull.", "Mephi hat schon gepusht. Du musst nur noch pullen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(23f), Play(Card.SHY_DOOR), DoorTo(29, 0, speed = 25f), Show('A'), Say(T("History rewritten. You're welcome.", "Historie umgeschrieben. Gern geschehen."))),
                trap(PastX(23f), DoorTo(2, 14, speed = 20f), delay = 0.8f),
            ),
            // rematch: the door stays this time. Whoever stops where it fled in round 1 to wait for the push stands on a
            // floor that gives way under loiterers: run through to the door
            rematch = listOf(
                Round(
                    T("Rematch. I pushed first this time.", "Revanche. Diesmal hab ich zuerst gepusht."),
                    traps = listOf(
                        trap(Touch('w'), Play(Card.CRUMBLE), Say(T("Merge pending. Floor deprecated.", "Merge läuft. Boden veraltet."))),
                        trap(Touch('w'), Fall('w'), delay = 0.8f),
                    ),
                ) { fill(22..25, 15..17, 'w') },
            ),
        ) {
            border(); floor(); pit(11..12)
            put(18, 14, '#')
            put(5, 14, 'A'); put(6, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 38 — the picture turns upside down over a spike pit; left and right follow the screen; the jump over the spikes lands before more
        // TWIST: Flip
        Level(
            name = T("Clear View", "Durchblick"),
            intro = T("Nice picture today. Sharper than ever.", "Schönes Bild heute. Schärfer als je zuvor."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(6f), Play(Card.UPSIDE_DOWN), Flip(3f), Say(T("Better view from here.", "Von hier hat man die bessere Aussicht."))),
                trap(Airborne(10f, 16.5f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(13..14, 14..14, '^')
            put(18, 14, 'A'); put(19, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 39 — a pit too wide to jump; the platform at its edge slides with the phone's tilt; hopping onto it and hopping off it both land on spikes
        // MOTION: Tilt
        Level(
            name = T("Hardware Store", "Baumarkt"),
            intro = T("I built something wide. No instructions.", "Ich habe was Breites gebaut. Ohne Anleitung."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(Airborne(5.5f, 9.6f), Show('B')),
                trap(Airborne(22f, 28f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Nobody said you could jump.", "Springen hat niemand erlaubt."))),
            ),
            start = listOf(Tilt('a', left = 0f, right = 13f, speed = 6f)),
        ) {
            border(); floor(); pit(8..23)
            put(9, 14, 'B'); put(10, 14, 'B')
            put(27, 14, 'A'); put(28, 14, 'A')
            fill(8..10, 15..15, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 40 — at the first step everything flips: gravity and controls. Walk the ceiling.
        // EASTER EGG: Kernel panic
        Level(
            name = T("Boot Sequence", "Systemstart"),
            intro = T("Everything is fine. Really.", "Alles in Ordnung. Wirklich."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Swap(true), Shake(1.5f), Say(T("KERNEL PANIC - not syncing", "KERNEL PANIC - nicht synchronisiert"))),
                trap(Airborne(15f, 20.5f), Swap(false), Say(T("Controls restored. Probably.", "Steuerung wiederhergestellt. Vermutlich."))),
            ),
        ) {
            border(); floor()
            fill(6..27, 14..14, '^')
            put(18, 0, '.'); put(19, 0, '.')
            put(2, 14, 'P'); put(29, 1, 'D')
        },

        // 41 — stalactites fall in two waves while the picture rolls; you cannot see them land
        // TWIST: Roll
        Level(
            name = T("TV Night", "Fernsehabend"),
            intro = T("Reception is great today. No static.", "Der Empfang ist heute super. Kein Rauschen."),
            legend = mapOf('S' to ceilingSpike, 'T' to ceilingSpike),
            traps = listOf(
                trap(PastX(10f), Play(Card.GHOST_BLOCK), Fall('S'), Roll(2.4f, 2), Say(T("Technical difficulties.", "Bildstörung."))),
                trap(PastX(20.5f), Fall('T'), Roll(2f, 2)),
            ),
        ) {
            border(); floor()
            put(14, 1, 'S'); put(15, 1, 'S')
            put(24, 1, 'T'); put(25, 1, 'T')
            ceilingSpikes(7..8); ceilingSpikes(29..30)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 42 — the old wall of teeth chases you and Mephi offers a pause; the pause button is a spike
        // TWIST: PauseTrap spike
        Level(
            name = T("Tailwind", "Rückenwind"),
            intro = T("Look ahead. There's nothing behind you.", "Schau nach vorne. Hinter dir ist nichts."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT), 'A' to hiddenSpike),
            traps = listOf(
                trap(PastX(7f), Play(Card.SPIKE_SEED), Move('W', 30f, 0f, 7.4f), PauseTrap(PauseTrick.SPIKE),
                    Say(T("Hungry wall! Need a break? Tap pause.", "Hungrige Wand! Pause? Tipp auf Pause."))),
                trap(Airborne(9.5f, 12.6f), Show('A')),
            ),
            // rematch: the wall stays put, and a row of ceiling teeth waits over the first pit. Running on as in round 1
            // jumps right into it. Let it come to you at the start, then outrun it: it is slower than you
            rematch = listOf(
                Round(
                    T("Rematch. Someone up there likes you.", "Revanche. Da oben mag dich wer."),
                    legend = mapOf('S' to Glyph(spike = true, dir = Dir.DOWN, hidden = true)),
                    traps = listOf(
                        trap(After(0.5f), Play(Card.STALKER), Show('S'), Chase('S', speed = 4f, left = 10f, right = 16f),
                            Say(T("Personal space is a myth.", "Abstand halten? Nie gehört."))),
                    ),
                ) { put(12, 12, 'S'); put(13, 12, 'S') },
            ),
        ) {
            border(); floor()
            fill(1..1, 1..14, 'W')
            put(15, 14, 'A'); put(16, 14, 'A')
            pit(11..12); pit(21..22)
            put(17, 14, '#'); put(26, 14, '#')
            put(5, 14, 'P'); put(29, 14, 'D')
        },

        // 43 — your last attempt comes back as a deadly ghost one second behind you
        // TWIST: Ghost
        Level(
            name = T("git blame", "git blame"),
            intro = T("I keep a log. Of everything you do.", "Ich führe Buch. Über alles, was du tust."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(After(0f), Play(Card.DEVIL_SAW), Ghost(1f)),
                trap(Airborne(12.5f, 17.5f), Show('A')),
            ),
        ) {
            border(); floor(); pit(25..26)
            fill(15..16, 14..14, '^')
            put(20, 14, 'A'); put(21, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 44 — a wall to the ceiling that only comes down when you shake the phone
        // MOTION: Shaken
        Level(
            name = T("Wallflower", "Mauerblümchen"),
            intro = T("The wall won't budge. Neither will I.", "Die Wand bewegt sich nicht. Ich auch nicht."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Shaken, Play(Card.COLLAPSE), Hide('a'), Say(T("Hey! Stop that!", "He! Lass das!"))),
                trap(Airborne(17f, 19.7f), Show('A')),
                trap(Touch('f'), Fall('f'), delay = 0.06f),
            ),
        ) {
            border(); floor()
            fill(20..20, 1..14, 'a')
            put(18, 14, 'A'); put(19, 14, 'A')
            fill(24..25, 15..17, 'f')
            put(2, 14, 'P'); put(29, 14, 'D')
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
