package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch

/** World 1, levels 17-32. Act 2, "Neue Regeln": blinking platforms, path saws and the Idle trigger, alone first, then with the classics. */
object World1Part2 {
    private val hiddenSolid = Glyph(spike = false, hidden = true)

    val levels: List<Level> = listOf(
        // 17 — blinking bridge over a pit: cross when it comes back
        // MECHANIC: Blink (alone)
        Level(
            name = T("Blinkenlights", "Sparmodus"),
            intro = T("New house rule: platforms have a power-saving mode.", "Neue Hausordnung: Plattformen haben jetzt einen Energiesparmodus."),
            start = listOf(Blink('a', on = 1.8f, off = 1f)),
        ) {
            border(); floor(); pit(11..20)
            fill(11..20, 15..15, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 18 — three saws bob up and down across the path; slip under each while it is up
        // MECHANIC: PathSaw (alone)
        Level(
            name = T("Pendulum", "Pendeluhr"),
            intro = T("Clockwork. Mostly teeth.", "Uhrwerk. Überwiegend Zähne."),
            start = listOf(
                PathSaw(6.5f, 9f to 14.4f, 9f to 7f, delay = 1f),
                PathSaw(7.5f, 15f to 7f, 15f to 14.4f),
                PathSaw(5f, 21.5f to 14.4f, 21.5f to 7f, delay = 0.5f),
            ),
        ) {
            border(); floor()
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 19 — the door comes if you wait, but standing still collapses the start platform
        // MECHANIC: Idle (punishes standing still)
        Level(
            name = T("Heisenbug", "Heisenbug"),
            intro = T("Don't just stand there. Do something.", "Steh nicht nur rum. Mach was."),
            traps = listOf(
                trap(After(3.5f), Play(Card.SHY_DOOR), DoorTo(4, 14, speed = 12f), Say(T("Told you.", "Hab's dir gesagt."))),
                trap(Idle(1f), Fall('a'), Say(T("Observed. Collapsed.", "Beobachtet. Kollabiert."))),
            ),
        ) {
            border(); floor(); pit(7..24)
            fill(1..6, 15..17, 'a')
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 20 — four stones blink one after another, like a running light: keep running
        // MECHANIC: Blink (rhythm)
        Level(
            name = T("Running Lights", "Lauflicht"),
            intro = T("Keep up with the lights. Don't stop.", "Lauf mit dem Licht. Nicht stehen bleiben."),
            start = listOf(
                Blink('a', on = 1.6f, off = 1.4f, phase = -0.3f),
                Blink('b', on = 1.6f, off = 1.4f, phase = -0.9f),
                Blink('c', on = 1.6f, off = 1.4f, phase = -1.5f),
                Blink('d', on = 1.6f, off = 1.4f, phase = -2.1f),
            ),
        ) {
            border(); floor(); pit(6..27)
            fill(8..10, 14..14, 'a'); fill(13..15, 14..14, 'b'); fill(18..20, 14..14, 'c'); fill(23..25, 14..14, 'd')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 21 — each floor segment you touch deletes the one two segments ahead
        // EASTER EGG: Segfault (floor segments fault away)
        Level(
            name = T("Segfault", "Segmentierungsfehler"),
            intro = T("Core dumped. Floor segments, that is.", "Core Dump. Bodensegmente, genauer gesagt."),
            traps = listOf(
                trap(Touch('a'), Play(Card.COLLAPSE), Fall('c'), Say(T("Segmentation fault (core dumped)", "Speicherzugriffsfehler (Speicherabbild erstellt)")), delay = 0.3f),
                trap(Touch('b'), Fall('d'), delay = 0.3f),
                trap(Touch('e'), Fall('g'), delay = 0.3f),
                trap(Touch('f'), Fall('h'), delay = 0.3f),
            ),
        ) {
            border(); floor(); pit(4..19)
            ('a'..'h').forEachIndexed { i, g -> fill(4 + i * 2..5 + i * 2, 15..17, g) }
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 22 — three walls open and close in turns; pass each one while it is open
        // MECHANIC: Blink (walls)
        Level(
            name = T("Airlock", "Schleuse"),
            intro = T("Please stand clear of the closing doors.", "Zurückbleiben bitte, die Türen schließen selbsttätig."),
            start = listOf(
                Blink('a', on = 1.6f, off = 1.6f, phase = 0f),
                Blink('b', on = 1.6f, off = 1.6f, phase = 1.6f),
                Blink('c', on = 1.6f, off = 1.6f, phase = 0f),
            ),
        ) {
            border(); floor()
            fill(9..9, 1..14, 'a'); fill(15..15, 1..14, 'b'); fill(21..21, 1..14, 'c')
            fill(10..11, 14..14, '^'); fill(16..17, 14..14, '^'); fill(22..23, 14..14, '^')   // right behind each door
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 23 — a long blinking bridge with a saw bobbing through the middle of it
        // MECHANIC: Blink + PathSaw
        Level(
            name = T("Sawmill", "Sägewerk"),
            intro = T("The bridge blinks. The saw works shifts.", "Die Brücke blinkt. Die Säge macht Schichtdienst."),
            start = listOf(
                Blink('a', on = 4.4f, off = 1f),
                PathSaw(6f, 15.5f to 14.4f, 15.5f to 7.5f, delay = 1.6f),
            ),
        ) {
            border(); floor(); pit(9..22)
            fill(9..22, 15..15, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 24 — two walls of spikes converge; jump the one that comes at you
        // EASTER EGG: git merge conflict markers
        Level(
            name = T("Merge Conflict", "Merge-Konflikt"),
            intro = T("<<<<<<< HEAD  Two branches want your spot.", "<<<<<<< HEAD  Zwei Branches wollen deinen Platz."),
            legend = mapOf('L' to Glyph(spike = true, dir = Dir.RIGHT), 'R' to Glyph(spike = true, dir = Dir.LEFT)),
            traps = listOf(
                trap(After(2.4f), Play(Card.DEVIL_SAW), Move('R', -20f, 0f, 4.2f), Move('L', 25f, 0f, 3f),
                    Say(T(">>>>>>> feature/squash-bevel", ">>>>>>> feature/bevel-plattmachen"))),
            ),
        ) {
            border(); floor()
            put(8, 13, 'L'); put(8, 14, 'L')
            put(22, 13, 'R'); put(22, 14, 'R')
            put(15, 14, 'P'); put(29, 14, 'D')
        },

        // 25 — hands off: standing perfectly still brings the door to you
        // MECHANIC: Idle (rewards standing still)
        Level(
            name = T("Patience", "Geduld"),
            intro = T("Good things come to those who wait. Hands off.", "Gut Ding will Weile haben. Finger weg."),
            traps = listOf(
                trap(Idle(2f), Play(Card.SHY_DOOR), DoorTo(4, 14, speed = 12f), Say(T("Good boy. Sit. Stay.", "Brav. Sitz. Platz."))),
            ),
        ) {
            border(); floor()
            fill(7..26, 14..14, '^')
            fill(26..30, 7..7)
            put(2, 14, 'P'); put(29, 6, 'D')
        },

        // 26 — a staircase whose steps blink in turns; wait on each one for the next
        // MECHANIC: Blink (climb)
        Level(
            name = T("Stairway to Heaven", "Himmelsleiter"),
            intro = T("Stairs on a timetable. Delays are likely.", "Treppe mit Fahrplan. Verspätungen sind möglich."),
            start = listOf(
                Blink('a', on = 3f, off = 1.2f, phase = -0.5f),
                Blink('b', on = 3f, off = 1.2f, phase = -2f),
                Blink('c', on = 3f, off = 1.2f, phase = -3.5f),
                Blink('d', on = 3f, off = 1.2f, phase = -5f),
            ),
        ) {
            border(); floor()
            fill(4..27, 14..14, '^')
            fill(6..8, 13..13, 'a'); fill(11..13, 11..11, 'b'); fill(16..18, 9..9, 'c'); fill(21..23, 7..7, 'd')
            fill(25..30, 5..5)
            put(2, 14, 'P'); put(29, 4, 'D')
        },

        // 27 — the answer: 0b101010. A cosmic ray flips bits in the floor.
        // EASTER EGG: 42 / Hitchhiker's Guide / binary 101010 / bit flip (cosmic ray)
        Level(
            name = T("42", "42"),
            intro = T("The answer to everything: 0b101010. Mostly.", "Die Antwort auf alles: 0b101010. Meistens."),
            legend = mapOf('p' to hiddenSolid, 'r' to hiddenSolid, 's' to hiddenSolid),
            traps = listOf(
                trap(PastX(5.4f), Play(Card.COLLAPSE), Show('p'), Show('r'), Fall('q'), Say(T("Bit flip! Cosmic ray. Not my fault.", "Bit gekippt! Kosmische Strahlung. Nicht meine Schuld.")), delay = 0.15f),
                trap(PastX(12f), Show('s'), Fall('t'), Say(T("Six times nine, in base 13.", "Sechs mal neun, zur Basis 13.")), delay = 0.15f),
            ),
        ) {
            border(); floor(); pit(8..10); pit(14..16); pit(20..22)
            fill(8..10, 15..17, 'p'); fill(11..13, 15..17, 'q'); fill(14..16, 15..17, 'r')
            fill(17..19, 15..17, 't'); fill(20..22, 15..17, 's')
            art(14, 3, "#.#", "#.#", "###", "..#", "..#")   // 4
            art(18, 3, "###", "..#", "###", "#..", "###")   // 2
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 28 — two saws patrol the floor; jump each as it comes
        // MECHANIC: PathSaw (patrol)
        Level(
            name = T("Jump Rope", "Seilspringen"),
            intro = T("Sports day. Everybody jumps.", "Sporttag. Seilspringen für alle."),
            start = listOf(
                PathSaw(6f, 14f to 14.4f, 6f to 14.4f),
                PathSaw(9f, 26f to 14.4f, 18f to 14.4f),
            ),
        ) {
            border(); floor()
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 29 — climb: the floor is spikes and it is rising
        Level(
            name = T("Rising Tide", "Steigende Flut"),
            intro = T("Nice view. Enjoy it while it lasts.", "Schöne Aussicht. Genieß sie, solange sie dauert."),
            traps = listOf(
                trap(After(2.3f), Play(Card.SPIKE_SEED), Move('L', 0f, -16f, 1.6f), Say(T("The floor is lava. Well. Spikes.", "Der Boden ist Lava. Na gut. Spikes."))),
            ),
        ) {
            border()
            fill(1..5, 13..13); fill(8..11, 11..11); fill(14..17, 9..9); fill(20..23, 7..7); fill(26..30, 5..5)
            fill(1..30, 17..17, 'L')
            put(3, 12, 'P'); put(29, 4, 'D')
        },

        // 30 — tetrominoes drop from the ceiling and build the stairs. You only have to wait.
        // EASTER EGG: Tetris (O-pieces stack up, "Line clear!")
        Level(
            name = T("Tetris", "Tetris"),
            intro = T("Here, I built you some stairs. Almost.", "Hier, ich habe dir eine Treppe gebaut. Fast."),
            traps = listOf(
                trap(After(2.4f), Play(Card.HEADBUTT), Fall('p'), Say(T("Next piece: staircase.", "Nächster Stein: Treppe."))),
                trap(After(3.4f), Fall('q')),
                trap(After(4.4f), Fall('r')),
                trap(After(5.4f), Fall('s')),
                trap(After(6.4f), Fall('t')),
                trap(After(7.4f), Fall('u'), Say(T("Line clear! You're welcome.", "Reihe voll! Gern geschehen."))),
            ),
        ) {
            border(); floor()
            fill(14..15, 1..2, 'p')
            fill(16..17, 3..4, 'q'); fill(16..17, 1..2, 'r')
            fill(18..19, 5..6, 's'); fill(18..19, 3..4, 't'); fill(18..19, 1..2, 'u')
            fill(22..30, 8..8)
            put(2, 14, 'P'); put(28, 7, 'D')
        },

        // 31 — a saw swings up and down in each pit; jump when it is up
        // MECHANIC: PathSaw (started by the Devil Saw card)
        Level(
            name = T("Sawfly", "Flugsäge"),
            intro = T("Watch your step. And the thing above it.", "Achte auf den Sprung. Und auf das Ding darüber."),
            traps = listOf(
                trap(PastX(3f), Play(Card.DEVIL_SAW), PathSaw(6f, 12f to 14.4f, 12f to 6f), PathSaw(6f, 19f to 6f, 19f to 14.4f),
                    Say(T("It only wants a hug! Two, actually.", "Sie will nur kuscheln! Genauer: zwei."))),
            ),
        ) {
            border(); floor(); pit(10..20)
            fill(14..16, 15..17)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 32 — a bridge, an island where you must keep moving, a second bridge and a patrolling saw
        // MECHANIC: Blink + Idle + PathSaw
        Level(
            name = T("Beta Test", "Betaversion"),
            intro = T("New rules: all of them. Tested: none.", "Neue Regeln: alle. Getestet: keine."),
            traps = listOf(
                trap(Idle(1.2f), Play(Card.COLLAPSE), Fall('f'), Say(T("You stood still. That's a regression.", "Du standest still. Das ist ein Rückschritt."))),
            ),
            start = listOf(
                Blink('a', on = 2.4f, off = 1f),
                Blink('b', on = 2f, off = 1.6f, phase = -2f),
                PathSaw(6f, 29f to 14.4f, 24f to 14.4f),
            ),
        ) {
            border(); floor(); pit(7..12); pit(17..21)
            fill(7..12, 15..15, 'a'); fill(13..16, 15..17, 'f'); fill(17..21, 15..15, 'b')
            put(2, 14, 'P'); put(30, 14, 'D')
        },
    )
}
