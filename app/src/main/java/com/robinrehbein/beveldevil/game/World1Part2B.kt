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
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 1, levels 17-24 (block B of the V2 rebuild). */
object World1Part2B {
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val hiddenCeilingSpike = Glyph(spike = true, dir = Dir.DOWN, hidden = true)

    val levels: List<Level> = listOf(
        // 17 — budget cuts: a gap opens in the floor ahead of you, the landing is cut under whoever lands on it, the way up is
        // platforms over the pit, the second one gives way, and so does the first piece of the upper floor on the way back. Rematch:
        // the floor holds, but whoever hops in the first stretch brings it down; the cuts have moved on to the walkers' road
        // MECHANIC: Fall (floor cuts)
        Level(
            name = T("Night Shift", "Nachtschicht"),
            intro = T("Management is saving money now. On everything.", "Die Hausverwaltung spart neuerdings. An allem."),
            traps = listOf(
                trap(PastX(4f), Play(Card.CRUMBLE), Fall('a'), Say(T("Budget cut: floor, tiles 7 to 9.", "Kürzung: Boden, Kacheln 7 bis 9."))),
                trap(Landed(10f, 13f), Fall('b'), Say(T("Landing fee: cancelled.", "Landegebühr: gestrichen.")), delay = 0.5f),
                trap(Touch('q'), Fall('q'), Say(T("Overtime is not paid. Neither is this step.", "Überstunden werden nicht bezahlt. Diese Stufe auch nicht.")), delay = 0.45f),
                trap(Touch('w'), Fall('w'), Say(T("Upper management cuts last.", "Die Chefetage kürzt zuletzt.")), delay = 0.4f),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. The cuts have been restructured.", "Revanche. Die Kürzungen wurden umstrukturiert."),
                    traps = listOf(
                        trap(Airborne(3f, 8.5f), Play(Card.SINKING), Fall('a'), Fall('b'), Say(T("Hopping is a cost centre. Cut.", "Hüpfen ist eine Kostenstelle. Gestrichen."))),
                        trap(PastX(7f), Fall('c'), Say(T("Cuts go where you are going.", "Gekürzt wird dort, wo du hinwillst."))),
                        trap(Touch('p'), Fall('p'), Say(T("This one is on probation.", "Diese hier ist in der Probezeit.")), delay = 0.45f),
                        trap(Touch('w'), Fall('w'), Say(T("Upper management cuts last.", "Die Chefetage kürzt zuletzt.")), delay = 0.4f),
                    ),
                ) { fill(11..12, 15..17, 'c') },
            ),
        ) {
            border(); floor(); pit(16..30)
            fill(7..9, 15..17, 'a'); fill(10..12, 15..17, 'b')
            fill(18..20, 13..13, 'p'); fill(23..24, 11..11, 'q'); fill(25..29, 9..14)
            fill(21..22, 7..7, 'w'); fill(2..20, 7..7)
            put(2, 14, 'P'); put(2, 6, 'D')
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
            // rematch: the same three saws, but no fourth one: whoever runs on to meet it and jump it runs onto the
            // floor before the door, and that floor collapses. Jump it from its edge
            rematch = listOf(
                Round(
                    T("Rematch. Shift change.", "Revanche. Schichtwechsel."),
                    traps = listOf(
                        trap(PastX(2.6f),
                            PathSaw(6.5f, 9f to 14.4f, 9f to 7f, delay = 1f),
                            PathSaw(7.5f, 15f to 7f, 15f to 14.4f),
                            PathSaw(5f, 21.5f to 14.4f, 21.5f to 7f, delay = 0.5f)),
                        trap(Touch('b'), Fall('b'), delay = 0.6f),
                        trap(Touch('w'), Play(Card.COLLAPSE), Fall('w'), delay = 0.12f),
                        trap(Touch('w'), Say(T("The fourth saw has the day off. So does the floor.", "Die vierte Säge hat frei. Der Boden auch."))),
                    ),
                ) { fill(24..26, 15..17, 'w') },
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
            // rematch: "patched": now each segment deletes the very next one
            rematch = listOf(
                Round(
                    T("Rematch. I patched the floor. Mostly.", "Revanche. Boden gepatcht. Größtenteils."),
                    traps = listOf(
                        trap(Touch('a'), Play(Card.COLLAPSE), Fall('b'), Say(T("Hotfix deployed. On a Friday.", "Hotfix eingespielt. Freitags.")), delay = 0.3f),
                        trap(Touch('c'), Fall('d'), delay = 0.3f),
                        trap(Touch('e'), Fall('f'), delay = 0.3f),
                        trap(Touch('g'), Fall('h'), delay = 0.3f),
                    ),
                ),
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
            legend = mapOf('A' to hiddenSpike, 'C' to hiddenSpike),
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
            put(17, 14, 'C'); put(18, 14, 'C')
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
            // rematch: the wall you learned to wait for and jump stays put; the one behind you comes, and faster
            rematch = listOf(
                Round(
                    T("Rematch. Rebased onto your mistakes.", "Revanche. Auf deine Fehler rebased."),
                    traps = listOf(
                        trap(After(2.4f), Play(Card.DEVIL_SAW), Move('L', 25f, 0f, 3.4f), Say(T("Fast-forward. From behind.", "Fast-Forward. Von hinten."))),
                        trap(Airborne(21f, 26f), Show('A')),
                    ),
                ),
            ),
        ) {
            border(); floor()
            put(8, 13, 'L'); put(8, 14, 'L')
            put(22, 13, 'R'); put(22, 14, 'R')
            fill(22..24, 15..17, 'c')
            put(27, 14, 'A'); put(28, 14, 'A')
            put(15, 14, 'P'); put(29, 14, 'D')
        },
    )
}
