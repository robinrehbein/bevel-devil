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
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 1, levels 17-32. Act 2, "Neue Regeln": blinking platforms, path saws and the Idle trigger. Each level keeps its
 * mechanic, and around it sit two or three surprises that punish how the player handles that mechanic: the ledge you
 * wait on crumbles, the jump you learned lands on spikes. Every drop is a pit or spikes, never a floor to stand on.
 * Levels with saws in `start` would show them in the plain-room check, so their saws come with the first step.
 */
object World1Part2 {
    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val hiddenCeilingSpike = Glyph(spike = true, dir = Dir.DOWN, hidden = true)

    val levels: List<Level> = listOf(
        // 17 — blinking bridge: the ledge you wait on crumbles; jumping off the bridge's end lands on spikes; the last stretch is a hidden pit
        // MECHANIC: Blink
        Level(
            name = T("Night Shift", "Nachtschicht"),
            intro = T("Management is saving money now. On everything.", "Die Hausverwaltung spart neuerdings. An allem."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('e'), Play(Card.CRUMBLE), Fall('e'), Say(T("The waiting area is closing.", "Der Warteraum schließt.")), delay = 1.1f),
                trap(Airborne(19f, 21.4f), Show('A')),
                trap(Touch('c'), Fall('c'), delay = 0.1f),
            ),
            start = listOf(Blink('a', on = 2.2f, off = 1f)),
        ) {
            border(); floor(); pit(11..20)
            fill(11..20, 15..15, 'a')
            fill(9..10, 15..17, 'e')
            put(23, 14, 'A'); put(24, 14, 'A')
            fill(27..28, 15..17, 'c')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 18 — three saws bob across the path (they start with your first step); the floor where you would wait for the second one drops; a fourth saw comes from the front
        // MECHANIC: PathSaw
        Level(
            name = T("On the Hour", "Stundenschlag"),
            intro = T("Be right back. Just getting coffee.", "Bin gleich zurück. Nur kurz Kaffee holen."),
            traps = listOf(
                trap(PastX(2.6f), Play(Card.DEVIL_SAW),
                    PathSaw(6.5f, 9f to 14.4f, 9f to 7f, delay = 1f),
                    PathSaw(7.5f, 15f to 7f, 15f to 14.4f),
                    PathSaw(5f, 21.5f to 14.4f, 21.5f to 7f, delay = 0.5f)),
                trap(Touch('b'), Fall('b'), delay = 0.6f),
                trap(PastX(23.4f), Saw(33.5f, 14.4f, -5f, 0f, 0.62f), Say(T("Not everyone works the same shift.", "Nicht alle haben dieselbe Schicht."))),
            ),
        ) {
            border(); floor()
            fill(12..14, 15..17, 'b')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 19 — the door comes if you wait, but standing still collapses the start platform; hopping in place meets spikes; pacing to the edge drops it too
        // MECHANIC: Idle (punishes standing still)
        Level(
            name = T("Waiting Room", "Wartezimmer"),
            intro = T("Have a seat for a second. I'll fetch the door.", "Setz dich kurz. Ich hole die Tür."),
            legend = mapOf('S' to hiddenCeilingSpike),
            traps = listOf(
                trap(After(3.5f), Play(Card.SHY_DOOR), DoorTo(4, 14, speed = 12f), Say(T("Told you.", "Hab's dir gesagt."))),
                trap(Idle(1f), Fall('a'), Say(T("Observed. Collapsed.", "Beobachtet. Kollabiert."))),
                trap(Airborne(0f, 8f), Show('S')),
                trap(PastX(5.6f), Fall('a')),
            ),
        ) {
            border(); floor(); pit(7..24)
            fill(1..6, 15..17, 'a')
            fill(1..6, 12..12, 'S')
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 20 — four stones blink like a running light: jumping from the edge of a stone lands on spikes: on the first stone, on the third, and on the far bank
        // MECHANIC: Blink (rhythm)
        Level(
            name = T("Disco Night", "Discoabend"),
            intro = T("Disco tonight. You're not invited.", "Heute Abend ist Disco. Du hast keine Einladung."),
            legend = mapOf('A' to hiddenSpike, 'E' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(Airborne(2.5f, 8.5f), Play(Card.SPIKE_SEED), Show('E')),
                trap(Airborne(14f, 19.5f), Show('A')),
                trap(Landed(23f, 26.2f), Show('B')),
            ),
            start = listOf(
                Blink('a', on = 1.6f, off = 1.4f, phase = -0.3f),
                Blink('b', on = 1.6f, off = 1.4f, phase = -0.9f),
                Blink('c', on = 1.6f, off = 1.4f, phase = -1.5f),
                Blink('d', on = 1.6f, off = 1.4f, phase = -2.1f),
            ),
        ) {
            border(); floor(); pit(6..27)
            fill(8..10, 14..14, 'a'); fill(13..15, 14..14, 'b'); fill(18..20, 14..14, 'c'); fill(23..25, 14..14, 'd')
            put(10, 13, 'E'); put(20, 13, 'A')
            put(28, 14, 'B'); put(29, 14, 'B')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 21 — each floor segment you touch deletes the one two segments ahead
        // EASTER EGG: Segfault (floor segments fault away)
        Level(
            name = T("Foundation", "Fundament"),
            intro = T("Concrete. Two inches. Fully inspected.", "Beton. Fünf Zentimeter. Alles geprüft."),
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

        // 22 — three walls open and close in turns; the floor after each one drops when you land on it, and the last landing is a pit
        // MECHANIC: Blink (walls)
        Level(
            name = T("Airlock", "Schleuse"),
            intro = T("Tuesday is field-trip day. For the walls.", "Dienstag ist Wandertag. Für die Wände."),
            traps = listOf(
                trap(Touch('f'), Play(Card.COLLAPSE), Fall('f'), Say(T("Please wait here.", "Bitte hier warten.")), delay = 0.5f),
                trap(Touch('g'), Fall('g'), delay = 0.8f),
                trap(Landed(24f, 28f), Fall('h')),
            ),
            start = listOf(
                Blink('a', on = 1.6f, off = 1.6f, phase = 0f),
                Blink('b', on = 1.6f, off = 1.6f, phase = 1.6f),
                Blink('c', on = 1.6f, off = 1.6f, phase = 0f),
            ),
        ) {
            border(); floor()
            fill(9..9, 1..14, 'a'); fill(15..15, 1..14, 'b'); fill(21..21, 1..14, 'c')
            fill(10..11, 14..14, '^'); fill(16..17, 14..14, '^'); fill(22..23, 14..14, '^')   // right behind each door
            fill(12..14, 15..17, 'f'); fill(18..20, 15..17, 'g'); fill(27..28, 15..17, 'h')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 23 — a long blinking bridge with a saw bobbing through the middle; the ledge you wait on crumbles, the middle of the bridge is a hidden gap, jumping off its end lands on spikes
        // MECHANIC: Blink + PathSaw
        Level(
            name = T("Carpentry", "Zimmerei"),
            intro = T("Everything here is still handmade.", "Hier ist noch alles Handarbeit."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('e'), Play(Card.COLLAPSE), Fall('e'), delay = 0.6f),
                trap(Touch('m'), Fall('m'), delay = 0.25f),
                trap(Airborne(19.5f, 22.5f), Show('A')),
            ),
            start = listOf(
                Blink('a', on = 4.4f, off = 1f),
                PathSaw(6f, 15.5f to 14.4f, 15.5f to 7.5f, delay = 1.6f),
            ),
        ) {
            border(); floor(); pit(9..22)
            fill(9..13, 15..15, 'a'); fill(14..16, 15..15, 'm'); fill(17..22, 15..15, 'a')
            fill(7..8, 15..17, 'e')
            put(24, 14, 'A'); put(25, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 24 — two walls of spikes converge and you jump the one that comes at you; the landing is followed by a pit, and the jump over the pit by spikes
        // EASTER EGG: git merge conflict markers
        Level(
            name = T("Merge Conflict", "Merge-Konflikt"),
            intro = T("<<<<<<< HEAD  Commit message: 'minor changes'.", "<<<<<<< HEAD  Commit-Nachricht: 'kleine Änderungen'."),
            legend = mapOf('L' to Glyph(spike = true, dir = Dir.RIGHT), 'R' to Glyph(spike = true, dir = Dir.LEFT), 'A' to hiddenSpike),
            traps = listOf(
                trap(After(2.4f), Play(Card.DEVIL_SAW), Move('R', -20f, 0f, 4.2f), Move('L', 25f, 0f, 3f),
                    Say(T(">>>>>>> feature/squash-bevel", ">>>>>>> feature/bevel-plattmachen"))),
                trap(Landed(17.5f, 24f), Fall('c')),
                trap(Airborne(21f, 26f), Show('A')),
            ),
        ) {
            border(); floor()
            put(8, 13, 'L'); put(8, 14, 'L')
            put(22, 13, 'R'); put(22, 14, 'R')
            fill(22..24, 15..17, 'c')
            put(27, 14, 'A'); put(28, 14, 'A')
            put(15, 14, 'P'); put(29, 14, 'D')
        },

        // 25 — hands off: standing perfectly still brings the door to you, but sitting too long drops the platform, and so does stepping left or right too far
        // MECHANIC: Idle (rewards standing still)
        Level(
            name = T("Rush Hour", "Stoßzeit"),
            intro = T("Hurry! The door closes soon.", "Schnell, schnell! Die Tür schließt gleich."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Idle(2f), Play(Card.SHY_DOOR), DoorTo(4, 14, speed = 12f), Say(T("Good boy. Sit. Stay.", "Brav. Sitz. Platz."))),
                trap(Idle(3.4f), Fall('f'), Say(T("Stay? I said stay for a bit.", "Platz? Ich sagte: kurz."))),
                trap(BeforeX(1.7f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(7..26, 14..14, '^')
            fill(1..6, 15..17, 'f')
            fill(26..30, 7..7)
            put(1, 14, 'A')
            put(2, 14, 'P'); put(29, 6, 'D')
        },

        // 26 — a staircase whose steps blink in turns; run to the end of a step and the next one has spikes, jump from its middle and the ledge has spikes
        // MECHANIC: Blink (climb)
        Level(
            name = T("Skyscraper", "Hochhaus"),
            intro = T("Top floor. The air is better up there.", "Oberste Etage. Da oben ist die Luft besser."),
            legend = mapOf('C' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(Landed(16f, 19.2f), Play(Card.SPIKE_SEED), Show('C')),
                trap(Airborne(20.8f, 24.5f), Show('B')),
            ),
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
            put(23, 6, 'C'); put(26, 4, 'B')
            put(2, 14, 'P'); put(29, 4, 'D')
        },

        // 27 — the answer: 0b101010. A cosmic ray flips bits in the floor; the last stone crumbles under you
        // EASTER EGG: 42 / Hitchhiker's Guide / binary 101010 / bit flip (cosmic ray)
        Level(
            name = T("42", "42"),
            intro = T("The answer to everything. I forgot the question.", "Die Antwort auf alles. Die Frage habe ich vergessen."),
            legend = mapOf('p' to hiddenSolid, 'r' to hiddenSolid, 's' to hiddenSolid),
            traps = listOf(
                trap(PastX(5.4f), Play(Card.COLLAPSE), Show('p'), Show('r'), Fall('q'), Say(T("Bit flip! Cosmic ray. Not my fault.", "Bit gekippt! Kosmische Strahlung. Nicht meine Schuld.")), delay = 0.15f),
                trap(PastX(12f), Show('s'), Fall('t'), Say(T("Six times nine, in base 13.", "Sechs mal neun, zur Basis 13.")), delay = 0.15f),
                trap(Touch('s'), Fall('s'), delay = 0.08f),
            ),
        ) {
            border(); floor(); pit(8..10); pit(14..16); pit(20..22)
            fill(8..10, 15..17, 'p'); fill(11..13, 15..17, 'q'); fill(14..16, 15..17, 'r')
            fill(17..19, 15..17, 't'); fill(20..22, 15..17, 's')
            art(14, 3, "#.#", "#.#", "###", "..#", "..#")   // 4
            art(18, 3, "###", "..#", "###", "#..", "###")   // 2
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 28 — two saws patrol the floor (they start with your first step); a hidden pit right behind the first, spikes behind the landing after the second
        // MECHANIC: PathSaw (patrol)
        Level(
            name = T("Gym Class", "Turnstunde"),
            intro = T("Mephi is training for a marathon. Don't disturb him.", "Mephi trainiert für den Marathon. Stör ihn nicht."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(2.6f), Play(Card.DEVIL_SAW), PathSaw(6f, 12f to 14.4f, 6f to 14.4f), PathSaw(8f, 25f to 14.4f, 20f to 14.4f)),
                trap(Touch('f'), Fall('f'), delay = 0.08f),
                trap(Airborne(18.5f, 27.5f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(14..15, 15..17, 'f')
            put(29, 14, 'A')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 29 — climb: the floor is spikes and it is rising; the third platform crumbles, the fourth sprouts spikes where you would land running
        Level(
            name = T("Hike", "Bergtour"),
            intro = T("The journey is the destination. Allegedly.", "Der Weg ist das Ziel. Angeblich."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(After(2.3f), Play(Card.SPIKE_SEED), Move('L', 0f, -16f, 1.6f), Say(T("The floor is lava. Well. Spikes.", "Der Boden ist Lava. Na gut. Spikes."))),
                trap(Touch('b'), Fall('b'), delay = 0.4f),
                trap(Airborne(17f, 21f), Show('A')),
            ),
        ) {
            border()
            fill(1..5, 13..13); fill(8..11, 11..11); fill(14..17, 9..9, 'b'); fill(20..23, 7..7); fill(24..30, 5..5)
            fill(1..30, 17..17, 'L')
            put(23, 6, 'A')
            put(3, 12, 'P'); put(29, 4, 'D')
        },

        // 30 — tetrominoes drop from the ceiling and build the stairs; the top of the stairs is the trap
        // EASTER EGG: Tetris (O-pieces stack up, "Line clear!")
        Level(
            name = T("Arcade", "Spielhalle"),
            intro = T("I'm about to play something. Go on ahead.", "Ich spiele gleich was. Geh ruhig schon vor."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(After(2.4f), Play(Card.HEADBUTT), Fall('p'), Say(T("Next piece: staircase.", "Nächster Stein: Treppe."))),
                trap(After(3.4f), Fall('q')),
                trap(After(4.4f), Fall('r')),
                trap(After(5.4f), Fall('s')),
                trap(After(6.4f), Fall('t')),
                trap(After(7.4f), Fall('u'), Say(T("Line clear! You're welcome.", "Reihe voll! Gern geschehen."))),
                trap(Landed(21.5f, 25.9f), Show('A')),
                // ran past the pit before the stairs stood: the floor right of them is a dead end, so it kills instead of stranding
                trap(Zone(20.5f, 12f, 31f, 16f), Saw(32f, 14.4f, -8f, 0f), Say(T("Wrong side of the stairs. Tough luck.", "Falsche Seite der Treppe. Pech gehabt."))),
                trap(Zone(20.5f, 12f, 31f, 16f), Saw(32f, 14.4f, -8f, 0f), delay = 1.2f),
            ),
        ) {
            border(); floor()
            fill(14..15, 1..2, 'p')
            fill(16..17, 3..4, 'q'); fill(16..17, 1..2, 'r')
            fill(18..19, 5..6, 's'); fill(18..19, 3..4, 't'); fill(18..19, 1..2, 'u')
            fill(22..30, 8..8)
            put(26, 7, 'A'); put(27, 7, 'A')
            put(2, 14, 'P'); put(28, 7, 'D')
        },

        // 31 — a saw swings up and down in each pit; the island you wait on crumbles, and the far side has spikes behind the landing
        // MECHANIC: PathSaw (started by the Devil Saw card)
        Level(
            name = T("Meadow", "Wiesengrund"),
            intro = T("Sunny day. Birds singing. Nothing with teeth.", "Sonniger Tag. Vögel zwitschern. Nichts mit Zähnen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(3f), Play(Card.DEVIL_SAW), PathSaw(6f, 12f to 14.4f, 12f to 6f), PathSaw(6f, 19f to 6f, 19f to 14.4f),
                    Say(T("It only wants a hug! Two, actually.", "Sie will nur kuscheln! Genauer: zwei."))),
                trap(Touch('i'), Fall('i'), delay = 1.3f),
                trap(Airborne(16f, 20f), Show('A')),
            ),
        ) {
            border(); floor(); pit(10..20)
            fill(14..16, 15..17, 'i')
            put(22, 14, 'A'); put(23, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 32 — a bridge, an island where you must keep moving, a second bridge and a patrolling saw; the floor after the bridge crumbles under loiterers, and a short jump over the saw lands in a hidden pit
        // MECHANIC: Blink + Idle + PathSaw
        Level(
            name = T("Beta Test", "Betaversion"),
            intro = T("Please send feedback via the form. There is none.", "Bitte Feedback über das Formular senden. Es gibt keins."),
            traps = listOf(
                trap(Idle(1.2f), Play(Card.COLLAPSE), Fall('f'), Say(T("You stood still. That's a regression.", "Du standest still. Das ist ein Rückschritt."))),
                trap(Touch('s'), Fall('s'), delay = 0.35f),
                trap(Touch('t'), Fall('t'), delay = 0.08f),
            ),
            start = listOf(
                Blink('a', on = 2.4f, off = 1f),
                Blink('b', on = 2f, off = 1.6f, phase = -2f),
                PathSaw(6f, 29f to 14.4f, 24f to 14.4f),
            ),
        ) {
            border(); floor(); pit(7..12); pit(17..21)
            fill(7..12, 15..15, 'a'); fill(13..16, 15..17, 'f'); fill(17..21, 15..15, 'b')
            fill(22..23, 15..17, 's'); fill(27..28, 15..17, 't')
            put(2, 14, 'P'); put(30, 14, 'D')
        },
    )
}
