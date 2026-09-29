package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 1, levels 17-32: one new verb per level, new layouts. */
object World1Part2 {

    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(
        // 17 — the world collapses behind you, from left to right
        // EASTER EGG: "Hello, World!" spelled in tile art, printf/return 0 taunts
        Level(
            name = T("Hello, World!", "Hallo, Welt!"),
            intro = T("printf(\"Hello, World!\");  Classic.", "printf(\"Hallo, Welt!\");  Ein Klassiker."),
            traps = listOf(
                trap(PastX(5.5f), Play(Card.COLLAPSE), Fall('a'), Say(T("return 0; The world exits.", "return 0; Die Welt beendet sich."))),
            ) + ('b'..'j').mapIndexed { i, g -> trap(PastX(5.5f), Fall(g), delay = 0.45f * (i + 1)) },
        ) {
            border(); floor()
            ('a'..'j').forEachIndexed { i, g -> fill(1 + i * 3..3 + i * 3, 15..17, g) }
            // HELLO! in tile art, way up high
            art(6, 3, "#.#", "#.#", "###", "#.#", "#.#")
            art(10, 3, "###", "#..", "##.", "#..", "###")
            art(14, 3, "#..", "#..", "#..", "#..", "###")
            art(18, 3, "#..", "#..", "#..", "#..", "###")
            art(22, 3, "###", "#.#", "#.#", "#.#", "###")
            art(26, 3, "#", "#", "#", ".", "#")
            put(3, 14, 'P'); put(29, 14, 'D')
        },

        // 18 — a wall of teeth chases you; never stop
        Level(
            name = T("The Wall", "Die Wand"),
            intro = T("Don't look back. Seriously.", "Schau nicht zurück. Ernsthaft."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT)),
            traps = listOf(
                trap(PastX(7f), Play(Card.DEVIL_SAW), Move('W', 30f, 0f, 7.4f), Say(T("Hungry wall! Very hungry.", "Hungrige Wand! Sehr hungrig."))),
            ),
        ) {
            border(); floor()
            fill(1..1, 1..14, 'W')
            pit(11..12); pit(21..22)
            put(17, 14, '#'); put(26, 14, '#')
            put(5, 14, 'P'); put(29, 14, 'D')
        },

        // 19 — spiked slabs sink into a 1-tile tunnel; the spikes retract later, then you can crawl through
        Level(
            name = T("Low Bridge", "Niedrige Brücke"),
            intro = T("Mind the ceiling. It has plans.", "Achte auf die Decke. Sie hat Pläne."),
            legend = mapOf('V' to Glyph(spike = true, dir = Dir.DOWN), 'X' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = listOf(
                trap(PastX(10f), Play(Card.HEADBUTT), Move('s', 0f, 9f, 12f), Move('V', 0f, 9f, 12f), Say(T("Duck! Oh wait, you can't.", "Duck dich! Ach, du kannst ja nicht."))),
                trap(PastX(10f), Hide('V'), delay = 2.2f),
                trap(PastX(20.6f), Move('t', 0f, 9f, 12f), Move('X', 0f, 9f, 12f), Say(T("Second helping.", "Nachschlag."))),
                trap(PastX(20.6f), Hide('X'), delay = 2.2f),
            ),
        ) {
            border(); floor()
            fill(14..19, 3..4, 's'); fill(14..19, 5..5, 'V')
            fill(23..27, 3..4, 't'); fill(23..27, 5..5, 'X')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 20 — the stone in the middle vanishes when you touch the first one
        Level(
            name = T("Now You See Me", "Jetzt siehst du mich"),
            intro = T("Five honest stones. Honest.", "Fünf ehrliche Steine. Ehrlich."),
            traps = listOf(
                trap(Touch('a'), Play(Card.GHOST_BLOCK), Hide('b'), Say(T("Now you don't.", "Jetzt nicht mehr."))),
                trap(Touch('c'), Hide('d'), Say(T("Twice, even.", "Sogar zweimal."))),
            ),
        ) {
            border(); floor(); pit(6..27)
            fill(8..10, 14..14, 'a'); fill(12..13, 14..14, 'b'); fill(15..18, 14..14, 'c')
            fill(20..21, 14..14, 'd'); fill(23..26, 14..14)
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 21 — the trick is doing nothing: the door comes to you
        Level(
            name = T("Patience", "Geduld"),
            intro = T("Good things come to those who wait.", "Gut Ding will Weile haben."),
            traps = listOf(
                trap(After(3.5f), Play(Card.SHY_DOOR), DoorTo(4, 14, speed = 12f), Say(T("Told you.", "Hab's dir gesagt."))),
            ),
        ) {
            border(); floor()
            fill(7..26, 14..14, '^')
            fill(26..30, 7..7)
            put(2, 14, 'P'); put(29, 6, 'D')
        },

        // 22 — a saw shoots out of the pit while you jump over it; wait it out
        Level(
            name = T("Pit Stop", "Boxenstopp"),
            intro = T("Mind the gap. Only a tiny one.", "Achte auf die Lücke. Nur eine kleine."),
            traps = listOf(
                trap(PastX(10.5f), Play(Card.DEVIL_SAW), Saw(14.5f, 19f, 0f, -13f, 0.6f), Say(T("Surprise! From below.", "Überraschung! Von unten."))),
            ),
        ) {
            border(); floor(); pit(13..15)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 23 — the ferry carries you across; a saw comes the other way
        Level(
            name = T("Ferry Duel", "Fährduell"),
            intro = T("Enjoy the ride. Toll is collected on board.", "Gute Fahrt. Die Maut wird an Bord kassiert."),
            traps = listOf(
                trap(After(2.6f), Move('f', 20f, 0f, 5f)),
                trap(After(3.4f), Play(Card.DEVIL_SAW), Saw(33f, 14.4f, -9f, 0f, 0.62f), Say(T("Toll booth! Cash only.", "Mautstelle! Nur Bargeld."))),
            ),
        ) {
            border(); floor(); pit(1..25)
            fill(1..5, 15..15, 'f')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 24 — only a low hop is allowed; a full jump makes spikes drop from the ceiling
        Level(
            name = T("Low Ceiling", "Niedrige Decke"),
            intro = T("Jump as high as you like.", "Spring so hoch du willst."),
            legend = mapOf('A' to Glyph(spike = true, dir = Dir.DOWN, hidden = true)),
            traps = listOf(
                trap(Zone(11f, 0f, 18f, 12.9f), Play(Card.HEADBUTT), Show('A'), Say(T("I said as high as you LIKE.", "Ich sagte, so hoch du WILLST."))),
            ),
        ) {
            border(); floor(); pit(13..14)
            fill(11..17, 12..12, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 25 — two invisible steps lead over a wall that is far too high
        Level(
            name = T("Ghost Stairs", "Geistertreppe"),
            intro = T("That wall is much too high. Pity.", "Die Wand ist viel zu hoch. Schade."),
            legend = mapOf('b' to ghost, 'c' to ghost),
            traps = listOf(
                trap(Touch('b'), Play(Card.GHOST_BLOCK), Say(T("Hey! Stairs are a secret.", "Hey! Treppen sind geheim."))),
            ),
        ) {
            border(); floor()
            fill(22..24, 9..14)
            fill(16..17, 13..13, 'b'); fill(19..20, 11..11, 'c')
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 26 — the wall is unclimbable; the way out is behind you
        Level(
            name = T("Wrong Way", "Falsche Richtung"),
            intro = T("That wall is huge. Maybe stop staring at it.", "Die Wand ist riesig. Vielleicht guck woanders hin."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('k'), Play(Card.DECOY), Hide('w'), Say(T("Oh, you found the button. Boo.", "Oh, du hast den Knopf gefunden. Buh."))),
                trap(PastX(20.5f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(15..16, 3..14, 'w')
            put(2, 14, 'k')
            put(25, 14, 'A'); put(26, 14, 'A')
            put(10, 14, 'P'); put(29, 14, 'D')
        },

        // 27 — the door hops from platform to platform as you land on each
        Level(
            name = T("Hopscotch", "Himmel und Hölle"),
            intro = T("The door is right there. Really.", "Die Tür ist gleich da. Wirklich."),
            traps = listOf(
                trap(PastX(26f), Play(Card.SHY_DOOR), DoorTo(29, 0, speed = 20f), Say(T("Up, up and away!", "Auf und davon!"))),
                trap(PastX(26f), DoorTo(9, 12, speed = 30f), delay = 0.7f),
                trap(Touch('a'), DoorTo(16, 10, speed = 30f), Say(T("Next one, please.", "Der Nächste, bitte."))),
                trap(Touch('b'), DoorTo(23, 8, speed = 30f)),
                trap(Touch('c'), DoorTo(28, 6, speed = 30f), Say(T("Last hop. Probably.", "Letzter Hüpfer. Vermutlich."))),
            ),
        ) {
            border(); floor()
            fill(5..9, 13..13, 'a'); fill(12..16, 11..11, 'b'); fill(19..23, 9..9, 'c'); fill(26..30, 7..7)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 28 — each floor segment you touch deletes the one two segments ahead
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

        // 29 — the door hovers one tile too high over spikes and then moves one tile to the right
        // EASTER EGG: off-by-one error (i <= n)
        Level(
            name = T("Off-by-One", "Um eins daneben"),
            intro = T("for (i = 0; i <= n; i++)  ... what could go wrong?", "for (i = 0; i <= n; i++)  ... was soll schon schiefgehen?"),
            traps = listOf(
                trap(PastX(22f), Play(Card.DECOY), DoorTo(30, 13, speed = 20f), Say(T("Index out of bounds. One tile to the right.", "Index außerhalb. Ein Feld weiter rechts."))),
            ),
        ) {
            border(); floor()
            fill(27..30, 14..14, '^')
            put(2, 14, 'P'); put(29, 13, 'D')
        },

        // 30 — the second wave of spikes appears while you are still in the air
        Level(
            name = T("Encore", "Zugabe"),
            intro = T("One round of applause for the spikes.", "Ein Applaus für die Spikes."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(PastX(11f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Thank you, thank you!", "Danke, danke!"))),
                trap(PastX(15.4f), Show('B'), Say(T("Encore!", "Zugabe!"))),
            ),
        ) {
            border(); floor()
            put(14, 14, 'A'); put(15, 14, 'A')
            put(19, 14, 'B'); put(20, 14, 'B')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 31 — the lift is the only way up, and it does not stop at your floor
        Level(
            name = T("Lift Off", "Abheben"),
            intro = T("Top floor: door. Also top floor: ceiling.", "Oberste Etage: Tür. Auch oberste Etage: Decke."),
            traps = listOf(
                trap(Touch('l'), Play(Card.SINKING), Move('l', 0f, -10f, 5f), Say(T("Going up! Very up.", "Es geht nach oben! Sehr nach oben."))),
            ),
        ) {
            border(); floor()
            fill(13..17, 14..14, 'l')
            fill(12..18, 2..3)
            fill(21..30, 9..9)
            put(2, 14, 'P'); put(28, 8, 'D')
        },

        // 32 — pop quiz: a falling block, hidden spikes and a collapsing floor, one after the other
        Level(
            name = T("Pop Quiz", "Überraschungstest"),
            intro = T("Everything you've learned so far. Ready?", "Alles, was du bisher gelernt hast. Bereit?"),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(6.6f), Play(Card.HEADBUTT), Fall('c'), Say(T("Question one: ceilings.", "Frage eins: Decken."))),
                trap(PastX(15f), Show('A'), Say(T("Question two: spikes.", "Frage zwei: Spikes."))),
                trap(PastX(21.6f), Fall('a'), Say(T("Question three: floors. Bonus: all of them.", "Frage drei: Böden. Bonus: alle."))),
            ),
        ) {
            border(); floor()
            fill(9..11, 5..6, 'c')
            put(18, 14, 'A'); put(19, 14, 'A')
            fill(24..26, 15..17, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },
    )
}
