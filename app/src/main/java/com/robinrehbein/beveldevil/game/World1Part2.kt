package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Bluff
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

    val levels: List<Level> = World1Part2B.levels + listOf(
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
            // round 2: the saws take a rest day; the pit is still there and the door end grows a spike for walkers.
            // round 3: still no saws; the pit holds now, and jumping it anyway makes the spike after it slide under the landing
            rematch = listOf(
                Round(
                    T("Rematch. The saws called in sick.", "Revanche. Die Sägen haben sich krankgemeldet."),
                    traps = listOf(
                        trap(PastX(2.6f), Say(T("Rest day. For them, not you.", "Ruhetag. Für die, nicht für dich."))),
                        trap(Touch('f'), Fall('f'), delay = 0.08f),
                        trap(PastX(26.5f), Play(Card.SPIKE_SEED), Show('A')),
                    ),
                ),
                Round(
                    T("Round three. Still on sick leave.", "Dritte Runde. Immer noch krankgeschrieben."),
                    traps = listOf(
                        trap(PastX(2.6f), Say(T("Doctor's note. Forged.", "Attest. Gefälscht."))),
                        trap(Airborne(12.5f, 16.5f), Play(Card.SPIKE_SEED), Move('K', -2f, 0f, 14f)),
                        trap(PastX(26.5f), Show('A')),
                    ),
                ) { put(19, 14, 'K') },
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
