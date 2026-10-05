package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.*
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Trigger.*

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

        // 18 — the hour strikes: two saws roll in along the upper floor, the second one makes you leap off its end, down on the ground
        // floor the next hour comes at you from the left, and the floor opens up twice on the way home. Rematch: the hours come from behind
        // MECHANIC: Saw (chimes)
        Level(
            name = T("On the Hour", "Stundenschlag"),
            intro = T("Be right back. Just getting coffee.", "Bin gleich zurück. Nur kurz Kaffee holen."),
            traps = listOf(
                trap(PastX(5f), Play(Card.DEVIL_SAW), Saw(33f, 7.4f, -7f, 0f, 0.62f), Say(T("Ding. One o'clock.", "Ding. Ein Uhr."))),
                trap(Airborne(19.9f, 24f), Saw(33f, 7.4f, -8f, 0f, 0.62f), Say(T("Ding. Two o'clock. Punctual people leave early.", "Ding. Zwei Uhr. Pünktliche gehen früher."))),
                trap(Landed(27.5f, 31f), Saw(-1.5f, 14.4f, 7.5f, 0f, 0.62f), Say(T("Three o'clock. Ground floor.", "Drei Uhr. Erdgeschoss.")), delay = 0.3f),
                trap(Zone(27.5f, 12f, 30f, 15.5f), Move('g', 0f, 5f, 12f), Say(T("Lunch break. For the floor.", "Mittagspause. Für den Boden."))),
                trap(Zone(10.5f, 12f, 13f, 15.5f), Move('k', 0f, 5f, 12f), Say(T("Closing time.", "Feierabend."))),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. The clock runs backwards now.", "Revanche. Die Uhr läuft jetzt rückwärts."),
                    traps = listOf(
                        trap(PastX(5f), Saw(33f, 7.4f, -10f, 0f, 0.62f), Say(T("Ding. Time zone: behind you.", "Ding. Zeitzone: hinter dir."))),
                        trap(Landed(14f, 25f), Saw(8f, 7.4f, 17f, 0f, 0.62f), Say(T("The cuckoo came out the back.", "Der Kuckuck kam hinten raus."))),
                        trap(Landed(27.5f, 31f), Play(Card.TWISTED), Swap(true), Say(T("Daylight saving: left is right.", "Zeitumstellung: links ist rechts."))),
                        trap(Landed(27.5f, 31f), Saw(33f, 14.4f, -17f, 0f, 0.62f), Say(T("Quarter past. From the other side.", "Viertel nach. Von der anderen Seite.")), delay = 0.5f),
                        trap(Zone(14.5f, 12f, 17f, 15.5f), Saw(-1.5f, 14.4f, 8f, 0f, 0.62f), Say(T("Half past. The clock is stuck.", "Halb. Die Uhr hängt."))),
                    ),
                ),
            ),
        ) {
            border(); floor(); fill(1..26, 8..8)
            fill(21..23, 15..17, 'g'); fill(6..7, 15..17, 'k')
            put(2, 7, 'P'); put(3, 14, 'D')
        },

        // 19 — a hold pad under a trapdoor: the hatch above the pad is open only while somebody stands on it, so you jump straight up
        // through it (it waits while you are inside); pressing the pad also sends a slab down from the ceiling of the room above, where
        // you land, and the lowering ceiling at the far end catches whoever stops
        // MECHANIC: hold pad (R2)
        Level(
            name = T("Waiting Room", "Wartezimmer"),
            intro = T("Have a seat for a second. I'll fetch the door.", "Setz dich kurz. Ich hole die Tür."),
            start = listOf(Circuit('w'), Pad('1', at = 26 to 12, circuits = "w", mode = PadMode.HOLD)),
            traps = listOf(
                trap(PastX(6.5f), Fall('a'), Say(T("Please mind the gap. I dug it myself.", "Bitte Abstand halten. Ich habe ihn selbst gegraben."))),
                trap(PastX(12.5f), Fall('b'), Say(T("Another gap. Waiting rooms have a lot of those.", "Noch eine Lücke. Wartezimmer haben viele davon."))),
                trap(Pressed('1'), Play(Card.HEADBUTT), Move('x', 0f, 10f, 18f), Say(T("Next, please! ... Not you.", "Der Nächste, bitte! ... Nicht du.")), delay = 0.35f),
                trap(Zone(21.6f, 1f, 23.4f, 11f), Move('y', 0f, 8f, 18f), Say(T("Please wait to be called.", "Bitte warten, bis Sie aufgerufen werden."))),
                trap(Zone(13f, 1f, 16f, 11f), Move('l', 0f, 7.5f, 2.2f), Say(T("The ceiling is fully booked. Come back lower.", "Die Decke ist ausgebucht. Komm tiefer wieder."))),
            ),
        ) {
            border(); floor()
            fill(1..30, 11..11); fill(23..26, 11..11, 'w')
            fill(9..10, 15..17, 'a'); fill(15..16, 15..17, 'b')
            fill(19..21, 14..14); fill(22..27, 13..14)
            fill(25..26, 1..2, 'x'); fill(18..19, 1..2, 'y'); fill(3..9, 1..2, 'l')
            put(27, 12, '#')
            put(2, 14, 'P'); put(2, 10, 'D')
        },

        // 20 — disco: stones over the pit light up in turns, the way up is a stair, and the way back along the top is three more
        // stones under a ceiling of spikes that comes down on whoever dances too long
        // MECHANIC: Blink (running light)
        Level(
            name = T("Disco Night", "Discoabend"),
            intro = T("Disco tonight. You're not invited.", "Heute Abend ist Disco. Du hast keine Einladung."),
            legend = mapOf('C' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = listOf(
                trap(PastX(3.5f), Blink('p', on = 0.05f, off = 60f), Blink('q', on = 0.05f, off = 60f), Blink('r', on = 0.05f, off = 60f), Blink('s', on = 0.05f, off = 60f),
                    Say(T("House lights off. Floor too.", "Saallicht aus. Der Boden auch."))),
                trap(Landed(9f, 12f), Blink('a', on = 1.8f, off = 0.9f), Say(T("Lights! ... Sometimes.", "Licht! ... Manchmal."))),
                trap(Landed(14f, 17f), Blink('b', on = 1.8f, off = 0.9f), Say(T("Next song. Same floor. Worse.", "Nächstes Lied. Gleicher Boden. Schlechter."))),
                trap(Landed(19f, 22f), Blink('c', on = 1.8f, off = 0.9f)),
                trap(Landed(28f, 31f), Play(Card.COLLAPSE), Move('C', 0f, 6.4f, 1.1f), Say(T("Last dance. The ceiling joins in.", "Letzter Tanz. Die Decke tanzt mit."))),
                trap(Zone(22.5f, 5f, 26f, 9.5f), Blink('f', on = 1.8f, off = 0.9f)),
                trap(Zone(17.5f, 5f, 21f, 9.5f), Blink('g', on = 1.8f, off = 0.9f)),
                trap(Zone(12.5f, 5f, 16f, 9.5f), Blink('h', on = 1.8f, off = 0.9f)),
                trap(Zone(9f, 5f, 11f, 9.5f), Blink('z', on = 1.8f, off = 0.9f), Say(T("The exit has stage fright.", "Der Ausgang hat Lampenfieber."))),
            ),
        ) {
            border(); floor(); pit(8..23)
            fill(8..8, 15..17, 'p'); fill(12..13, 15..17, 'q'); fill(17..18, 15..17, 'r'); fill(22..23, 15..17, 's')
            fill(9..11, 15..17, 'a'); fill(14..16, 15..17, 'b'); fill(19..21, 15..17, 'c')
            fill(26..27, 13..14); fill(28..30, 11..14)
            fill(23..25, 9..9, 'f'); fill(18..20, 9..9, 'g'); fill(13..15, 9..9, 'h')
            fill(4..8, 9..9); fill(9..10, 9..9, 'z')
            fill(4..27, 1..1, 'C')
            put(2, 14, 'P'); put(5, 8, 'D')
        },

        // 21 — the door is high up on the left and there is no way up. A strip of the floor sinks as you come, spikes grow after the hop;
        // on the far bank sits the one slab that is different: step on it and slabs of concrete come down from the ceiling and
        // build the stairs, while the slab you stand on sinks into the floor. Rematch: concrete only sets while you stand still
        // MECHANIC: Move (the room rebuilds itself)
        Level(
            name = T("Foundation", "Fundament"),
            intro = T("Concrete. Two inches. Fully inspected.", "Beton. Fünf Zentimeter. Alles geprüft."),
            legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(
                trap(PastX(5.5f), Move('f', 0f, 4f, 12f), Say(T("Floor strip 9 to 11: collected for recycling.", "Bodenstreifen 9 bis 11: wird recycelt."))),
                trap(Airborne(15f, 18.5f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Rebar. Included in the price.", "Bewehrung. Im Preis inbegriffen."))),
                trap(Zone(27.2f, 13.2f, 29.8f, 14.0f), Move('s', 0f, 12f, 22f), Move('t', 0f, 10f, 22f), Move('u', 0f, 8f, 22f), Move('k', 0f, 1.6f, 4f),
                    Say(T("Pour the stairs. Mind your head.", "Treppe gießen. Kopf einziehen."))),
                trap(Touch('s'), Move('s', 0f, 12f, 12f), Say(T("The first step has set. Elsewhere.", "Die erste Stufe ist abgebunden. Woanders.")), delay = 0.8f),
                trap(Touch('t'), Move('t', 0f, 12f, 12f), Say(T("Second step: also on its way out.", "Zweite Stufe: ebenfalls auf dem Weg nach draußen.")), delay = 0.8f),
                trap(Touch('u'), Move('u', 0f, 12f, 12f), delay = 0.8f),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. The concrete needs to cure.", "Revanche. Der Beton muss abbinden."),
                    traps = listOf(
                        trap(Airborne(6.8f, 12.4f), Move('g', 0f, 4f, 14f), Say(T("Landing gear: removed. Nobody hops on my site.", "Fahrwerk: ausgebaut. Auf meiner Baustelle wird nicht gehüpft."))),
                        trap(Touch('f'), Move('f', 0f, 4f, 12f), Say(T("Walk, don't hop. The strip is still on its break.", "Gehen, nicht hüpfen. Der Streifen hat noch Pause.")), delay = 0.5f),
                        trap(Touch('b'), Bluff(Card.SPIKE_SEED), Move('b', 0f, 4f, 12f), Say(T("Rebar? ... Never mind. The plank is the problem.", "Bewehrung? ... Egal. Das Brett ist das Problem.")), delay = 0.5f),
                        trap(Zone(27.2f, 13.2f, 29.8f, 14.0f), Move('s', 0f, 12f, 26f), Move('t', 0f, 10f, 26f), Move('u', 0f, 8f, 26f),
                            Say(T("Stand still. Let it cure. Then leave.", "Stillhalten. Abbinden lassen. Dann gehen.")), delay = 0.8f),
                        trap(Zone(27.2f, 13.2f, 29.8f, 14.0f), Move('k', 0f, 1.6f, 4f), delay = 2.1f),
                        trap(Touch('s'), Move('s', 0f, 12f, 12f), delay = 0.8f),
                        trap(Touch('t'), Move('t', 0f, 12f, 12f), delay = 0.8f),
                        trap(Touch('u'), Move('u', 0f, 12f, 12f), delay = 0.8f),
                    ),
                ) { fill(12..14, 15..17, 'g'); fill(15..16, 15..17, 'b') },
            ),
        ) {
            border(); floor(); pit(9..11)
            fill(9..11, 15..15, 'f')
            pit(15..16)
            put(22, 14, 'A'); put(23, 14, 'A')
            fill(27..29, 14..14, 'k')
            fill(18..23, 1..1, 's'); fill(14..17, 1..1, 't'); fill(10..13, 1..1, 'u')
            fill(1..9, 9..9)
            put(2, 14, 'P'); put(3, 8, 'D')
        },

        // 22 — the airlock: along the upper floor through a pulsing beam to the pad, which cuts the lock on the ground floor; the
        // cycle fails and Mephi rewinds you a moment, then you drop to the ground floor and the next beam pulses on the way to the door
        // MECHANIC: laser gate (R1 switch + door cage)
        Level(
            name = T("Airlock", "Schleuse"),
            intro = T("Cycle time: a few seconds. Survivors: variable.", "Zykluszeit: wenige Sekunden. Überlebende: variabel."),
            start = listOf(Circuit('w'), Pad('1', at = 25 to 7, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(PastX(6f), Laser('A', 12 to 1, 12 to 7, on = 0.9f, off = 1.5f, delay = 0.2f), Say(T("Outer door: pulsing. Please wait for the green.", "Außentür: pulsiert. Bitte auf Grün warten."))),
                trap(Pressed('1'), Play(Card.UNDO), Undo(1.1f), Say(T("Pressure test failed. Rewinding the last bit.", "Drucktest fehlgeschlagen. Das letzte Stück wird zurückgespult."))),
                trap(Zone(22f, 12f, 25f, 15.5f), Laser('B', 18 to 9, 18 to 14, on = 0.6f, off = 1.5f, delay = 0.1f), Say(T("Inner door: also pulsing. It is catching.", "Innentür: pulsiert auch. Das ist ansteckend."))),
            ),
        ) {
            border(); floor(); fill(1..26, 8..8)
            fill(5..6, 9..14, 'w')
            put(2, 7, 'P'); put(3, 14, 'D')
        },

        // 23 — a sawmill on three floors: a saw bobs through the top plank, the knot in the plank above the start is a hidden block
        // that sets a second saw on whoever jumps into it; the drop to the middle floor meets a saw head-on, and the ground floor
        // has a second bobbing saw and a roller from the far side
        // MECHANIC: PathSaw + Saw
        Level(
            name = T("Carpentry", "Zimmerei"),
            intro = T("Everything here is still handmade.", "Hier ist noch alles Handarbeit."),
            legend = mapOf('b' to Glyph(spike = false, hidden = true, bonk = true)),
            traps = listOf(
                trap(PastX(3.5f), PathSaw(6f, 8f to 4.4f, 8f to 0.7f, delay = 0.5f), Say(T("Measure twice. Cut once. Mind the first cut.", "Zweimal messen. Einmal sägen. Vorsicht beim ersten Schnitt."))),
                trap(Touch('b'), Play(Card.GHOST_BLOCK), Saw(-1.5f, 4.4f, 9f, 0f, 0.62f), Say(T("A knot! Nobody told you? Mind your head.", "Ein Ast! Hat dir keiner gesagt? Kopf einziehen."))),
                trap(Landed(14f, 31f), Saw(5.5f, 9.4f, 6f, 0f, 0.62f), Say(T("Sawdust everywhere. Mostly on you.", "Sägemehl überall. Vor allem auf dir."))),
                trap(Zone(5.2f, 12f, 7f, 15.5f), PathSaw(6f, 9f to 14.4f, 9f to 7.4f, delay = 0.3f), Say(T("The foreman cuts in at the end of the shift.", "Der Vorarbeiter sägt mit, kurz vor Feierabend."))),
                trap(Zone(12.5f, 12f, 14.5f, 15.5f), PathSaw(6f, 17f to 14.4f, 17f to 7.4f, delay = 0.45f), Say(T("And a piece for the road.", "Und ein Stück für den Heimweg."))),
            ),
        ) {
            border(); floor()
            fill(1..13, 5..5); fill(6..26, 10..10)
            put(6, 2, 'b')
            put(2, 4, 'P'); put(24, 14, 'D')
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
