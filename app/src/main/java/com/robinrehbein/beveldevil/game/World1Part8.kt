package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 1, levels 113-128: the finale. Everything you learned, then 127 + 1. */
object World1Part8 {

    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    /** A slab that goes down and up forever (well, four times). */
    private fun slabLoop(g: Char, phase: Float): List<Trap> = (0..3).flatMap { k ->
        listOf(
            trap(After(phase + 3.8f * k), Move(g, 0f, 10f, 14f)),
            trap(After(phase + 3.8f * k + 1.4f), Move(g, 0f, -10f, 14f)),
        )
    }

    val levels: List<Level> = listOf(
        // 113 — two bluffs first, then the real thing (twice)
        Level(
            name = T("The Wolf Strikes Back", "Der Wolf schlägt zurück"),
            intro = T("I cried wolf twice. Now I'm bored of crying.", "Ich habe zweimal Wolf gerufen. Jetzt habe ich keine Lust mehr zu rufen."),
            legend = mapOf('S' to ceilingSpike, 'A' to hiddenSpike),
            traps = listOf(
                trap(PastX(2.6f), Fall('S'), Shake(0.6f), Say(T("Wolf!", "Wolf!"))),
                trap(PastX(12f), Shake(1.2f), Say(T("WOLF!! ...just kidding.", "WOLF!! ...War nur Spaß."))),
                trap(PastX(16f), Play(Card.COLLAPSE), Fall('a'), Say(T("This one bites.", "Der hier beißt."))),
                trap(PastX(21.5f), Show('A'), Say(T("And so does this one.", "Und der hier auch."))),
            ),
        ) {
            border(); floor()
            put(11, 1, 'S'); put(12, 1, 'S')
            fill(19..21, 15..17, 'a')
            put(25, 14, 'A'); put(26, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 114 — three pits, each with a saw that jumps out of it; wait each one out
        Level(
            name = T("Pit Crew", "Boxencrew"),
            intro = T("Three pit stops. Full service. Tires included.", "Drei Boxenstopps. Vollservice. Reifen inklusive."),
            traps = listOf(
                trap(PastX(5f), Play(Card.DEVIL_SAW), Saw(9.5f, 19f, 0f, -13f, 0.6f), Say(T("Stop one: blades.", "Stopp eins: Klingen."))),
                trap(PastX(13f), Saw(17.5f, 19f, 0f, -13f, 0.6f), Say(T("Stop two: more blades.", "Stopp zwei: mehr Klingen."))),
                trap(PastX(21f), Saw(25.5f, 19f, 0f, -13f, 0.6f), Say(T("Stop three: you get the idea.", "Stopp drei: du verstehst schon."))),
            ),
        ) {
            border(); floor(); pit(8..10); pit(16..18); pit(24..26)
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 115 — you sink, the other platform rises; jump across when you are level
        Level(
            name = T("Seesaw", "Wippe"),
            intro = T("Nice and balanced. You go down, I go up.", "Schön ausbalanciert. Du gehst runter, ich geh hoch."),
            traps = listOf(
                trap(After(1f), Play(Card.SINKING), Move('a', 0f, 11f, 1.5f), Move('b', 0f, -5f, 1.5f), Say(T("Seesaw! Meet in the middle.", "Wippe! Treffen wir uns in der Mitte."))),
            ),
        ) {
            border()
            fill(4..8, 10..10, 'a'); fill(10..14, 15..15, 'b')
            fill(17..30, 10..10)
            put(5, 9, 'P'); put(29, 9, 'D')
        },

        // 116 — the lift swaps your controls on the way up; the ceiling is closing in
        Level(
            name = T("Lift Off III", "Abheben III"),
            intro = T("Third time is the charm. Also the crush.", "Aller guten Dinge sind drei. Auch der Quetschungen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('l'), Play(Card.SINKING), Swap(true), Move('l', 0f, -10f, 5f), Show('A'), Say(T("Going up. Left is right. Ceiling is close.", "Es geht hoch. Links ist rechts. Die Decke ist nah."))),
            ),
        ) {
            border(); floor()
            fill(13..17, 14..14, 'l')
            fill(12..18, 2..3)
            fill(20..30, 9..9)
            put(20, 8, 'A')
            put(2, 14, 'P'); put(29, 8, 'D')
        },

        // 117 — the door keeps half the remaining distance to itself
        // EASTER EGG: Zeno's paradox
        Level(
            name = T("Zeno's Door", "Zenons Tür"),
            intro = T("Reach the door by covering half the distance. Repeatedly.", "Erreiche die Tür, indem du die halbe Strecke zurücklegst. Immer wieder."),
            traps = listOf(
                trap(PastX(9f), Play(Card.SHY_DOOR), DoorTo(22, 14, speed = 8f), Say(T("Half of what's left.", "Die Hälfte vom Rest."))),
                trap(PastX(15.5f), DoorTo(25, 14, speed = 8f), Say(T("Half of what's left.", "Die Hälfte vom Rest."))),
                trap(PastX(20.25f), DoorTo(27, 14, speed = 8f), Say(T("Half of what's left.", "Die Hälfte vom Rest."))),
                trap(PastX(23.6f), DoorTo(28, 14, speed = 8f), Say(T("Achilles would be proud.", "Achilles wäre stolz."))),
                trap(PastX(25.8f), DoorTo(29, 14, speed = 8f), Say(T("Fine. You may have it. Almost.", "Na gut. Du darfst sie haben. Fast."))),
            ),
        ) {
            border(); floor(); pit(12..13)
            put(19, 14, '^'); put(20, 14, '^')
            put(2, 14, 'P'); put(16, 14, 'D')
        },

        // 118 — four crushers in a wave: run with it, not against it
        // EASTER EGG: deadlock
        Level(
            name = T("Deadlock", "Deadlock"),
            intro = T("Each thread waits for the next one. Nobody moves. Except the ceiling.", "Jeder Thread wartet auf den nächsten. Keiner bewegt sich. Außer der Decke."),
            traps = listOf(trap(After(0.3f), Play(Card.HEADBUTT), Say(T("Mutex acquired. Squish.", "Mutex gesperrt. Quetsch.")))) +
                slabLoop('a', 0.3f) + slabLoop('b', 0.8f) + slabLoop('c', 1.3f) + slabLoop('d', 1.8f),
        ) {
            border(); floor()
            fill(8..10, 3..4, 'a'); fill(13..15, 3..4, 'b'); fill(18..20, 3..4, 'c'); fill(23..25, 3..4, 'd')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 119 — the floor sinks as you cross it; jump up at the far end
        Level(
            name = T("Sinkhole", "Erdloch"),
            intro = T("Solid ground for eight tiles. Then less solid.", "Achte Felder fester Boden. Dann weniger fest."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, 10f, 1.5f), Say(T("Ground floor going down.", "Erdgeschoss fährt runter."))),
                trap(PastX(12f), Show('A')),
            ),
        ) {
            border(); floor(); pit(8..15)
            fill(8..15, 15..17, 'a')
            put(22, 14, 'A'); put(23, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 120 — five planks are built just ahead of you and removed just behind you
        Level(
            name = T("Bridge Builder II", "Brückenbauer II"),
            intro = T("Under construction (again). Hard hats not provided.", "Baustelle (schon wieder). Helme nicht im Preis inbegriffen."),
            legend = mapOf('a' to hiddenSolid, 'b' to hiddenSolid, 'c' to hiddenSolid, 'd' to hiddenSolid, 'e' to hiddenSolid),
            traps = listOf(
                trap(After(2.5f), Play(Card.GHOST_BLOCK), Show('a'), Say(T("Plank one. Plank two follows. Hurry.", "Brett eins. Brett zwei folgt. Beeil dich."))),
                trap(After(3.25f), Show('b')), trap(After(4f), Show('c')), trap(After(4.75f), Show('d')), trap(After(5.5f), Show('e')),
                trap(After(4.5f), Hide('a')), trap(After(5.25f), Hide('b')), trap(After(6f), Hide('c')),
                trap(After(6.75f), Hide('d')), trap(After(7.5f), Hide('e')),
            ),
        ) {
            border(); floor(); pit(6..30)
            fill(8..10, 14..14, 'a'); fill(13..15, 14..14, 'b'); fill(18..20, 14..14, 'c'); fill(23..25, 14..14, 'd'); fill(28..30, 14..14, 'e')
            put(2, 14, 'P'); put(30, 13, 'D')
        },

        // 121 — the deleted floor comes back; the next pit gets its bridge only after a while
        // EASTER EGG: Ctrl+Shift+Z (redo)
        Level(
            name = T("Redo", "Wiederholen"),
            intro = T("Ctrl+Z gave you the floor back. Ctrl+Shift+Z gives you the bridge.", "Strg+Z gab dir den Boden zurück. Strg+Umschalt+Z gibt dir die Brücke."),
            legend = mapOf('r' to hiddenSolid),
            traps = listOf(
                trap(PastX(9f), Play(Card.COLLAPSE), Hide('a'), Say(T("Delete.", "Löschen."))),
                trap(PastX(9f), Show('a'), Say(T("Undo.", "Rückgängig.")), delay = 3f),
                trap(PastX(19.5f), Show('r'), Say(T("Redo. Almost there.", "Wiederholen. Fast geschafft.")), delay = 2.5f),
            ),
        ) {
            border(); floor(); pit(22..27)
            fill(12..17, 15..17, 'a'); fill(22..27, 15..15, 'r')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 122 — everything goes wrong at once: wait for the rocks, then hop with swapped controls
        Level(
            name = T("Panic Room", "Panikraum"),
            intro = T("Don't panic. Also: panic.", "Keine Panik. Außerdem: Panik."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(8f), Play(Card.COLLAPSE), Shake(2f), Fall('a'), Show('A'), Fall('S'), Swap(true), Say(T("PANIC! Everything, all at once!", "PANIK! Alles, alles auf einmal!"))),
            ),
        ) {
            border(); floor()
            fill(12..14, 15..17, 'a')
            put(18, 14, 'A'); put(19, 14, 'A')
            put(23, 1, 'S'); put(24, 1, 'S')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 123 — floor, spikes, rocks and finally a saw coming right at you
        Level(
            name = T("Boss Rush II", "Bossrush II"),
            intro = T("The bosses are back. All of them. On a budget.", "Die Bosse sind zurück. Alle. Mit kleinem Budget."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(5f), Play(Card.COLLAPSE), Fall('a'), Say(T("Boss one: the floor.", "Boss eins: der Boden."))),
                trap(PastX(11.5f), Show('A'), Say(T("Boss two: the spikes.", "Boss zwei: die Spikes."))),
                trap(PastX(16f), Fall('S'), Say(T("Boss three: the ceiling.", "Boss drei: die Decke."))),
                trap(PastX(21f), Saw(33f, 14.4f, -9f, 0f, 0.62f), Say(T("Final boss: an intern with a saw.", "Endgegner: ein Praktikant mit Säge."))),
            ),
        ) {
            border(); floor()
            fill(8..10, 15..17, 'a')
            put(15, 14, 'A'); put(16, 14, 'A')
            put(21, 1, 'S'); put(22, 1, 'S')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 124 — three floors: right along the top, left along the middle, right along the bottom, dropping through holes
        Level(
            name = T("Down the Drain", "Den Abfluss runter"),
            intro = T("Follow the holes. Every floor has one.", "Folge den Löchern. Jede Etage hat eins."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike, 'C' to hiddenSpike),
            traps = listOf(
                trap(PastX(13.5f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Floor one: spikes.", "Etage eins: Spikes."))),
                trap(Zone(9f, 7f, 22f, 9.5f), Show('B'), Say(T("Floor two: more spikes.", "Etage zwei: mehr Spikes."))),
                trap(Zone(6f, 11f, 12f, 13.5f), Show('C'), Say(T("Floor three: you know the drill.", "Etage drei: du kennst das."))),
            ),
        ) {
            border(); floor()
            fill(1..25, 5..5); fill(29..30, 5..5)
            fill(5..30, 9..9)
            fill(1..25, 13..13); fill(29..30, 13..13)
            put(17, 4, 'A'); put(18, 4, 'A'); put(11, 8, 'B'); put(12, 8, 'B'); put(12, 12, 'C')
            put(2, 4, 'P'); put(29, 14, 'D')
        },

        // 125 — every island vanishes on a timer, whether you are on it or not
        Level(
            name = T("Hot Potato", "Heiße Kartoffel"),
            intro = T("Nobody wants to be the last one standing. Literally.", "Keiner will der Letzte sein, der steht. Wortwörtlich."),
            traps = listOf(
                trap(After(2.6f), Play(Card.CRUMBLE), Fall('g'), Say(T("Hot potato!", "Heiße Kartoffel!"))),
                trap(After(3.2f), Fall('a')), trap(After(3.8f), Fall('b')), trap(After(4.4f), Fall('c')), trap(After(5f), Fall('d')),
            ),
        ) {
            border(); floor(); pit(1..28)
            fill(1..5, 15..17, 'g'); fill(8..10, 15..17, 'a'); fill(14..16, 15..17, 'b'); fill(20..22, 15..17, 'c'); fill(26..28, 15..17, 'd')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 126 — four trapdoors open one after another, right under your nose
        Level(
            name = T("Trapdoor Tour", "Klappentour"),
            intro = T("Please keep your feet inside the level at all times.", "Bitte halten Sie Ihre Füße jederzeit innerhalb des Levels."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.COLLAPSE), Hide('a'), Say(T("Trapdoor one.", "Klappe eins."))),
                trap(PastX(10.5f), Hide('b'), Say(T("Trapdoor two.", "Klappe zwei."))),
                trap(PastX(16.5f), Hide('c'), Say(T("Trapdoor three.", "Klappe drei."))),
                trap(PastX(22.5f), Hide('d'), Say(T("Trapdoor four. Thanks for flying.", "Klappe vier. Danke fürs Fliegen."))),
            ),
        ) {
            border(); floor()
            fill(9..10, 15..17, 'a'); fill(15..16, 15..17, 'b'); fill(21..22, 15..17, 'c'); fill(27..28, 15..17, 'd')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 127 — the door runs away and comes back home, to the spot next to where you started
        // EASTER EGG: "There's no place like 127.0.0.1" (localhost)
        Level(
            name = T("There's No Place Like 127.0.0.1", "Es gibt keinen Ort wie 127.0.0.1"),
            intro = T("Ping the door. Round trip time: your whole life.", "Ping die Tür. Round-Trip-Zeit: dein ganzes Leben."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(12.5f), Play(Card.SHY_DOOR), DoorTo(16, 0, speed = 30f), Say(T("Request timed out.", "Zeitüberschreitung der Anforderung."))),
                trap(PastX(12.5f), DoorTo(29, 14, speed = 20f), delay = 0.5f),
                trap(PastX(27.5f), DoorTo(29, 0, speed = 30f), Show('A'), Say(T("Connection refused. Redirecting to localhost.", "Verbindung abgelehnt. Umleitung auf localhost."))),
                trap(PastX(27.5f), DoorTo(11, 14, speed = 20f), Say(T("There's no place like home.", "Es gibt keinen Ort wie Zuhause.")), delay = 0.5f),
            ),
        ) {
            border(); floor(); pit(20..21)
            put(26, 14, '^'); put(27, 14, '^')
            put(14, 14, 'A'); put(15, 14, 'A')
            put(10, 14, 'P'); put(16, 14, 'D')
        },

        // 128 — 127 + 1: the whole world turns negative: gravity, controls, and the way home
        // EASTER EGG: integer overflow, 127 + 1 = -128 (byte finale, 2^7)
        Level(
            name = T("Integer Overflow", "Ganzzahl-Überlauf"),
            intro = T("127 + 1 = -128. Welcome to the last byte.", "127 + 1 = -128. Willkommen im letzten Byte."),
            legend = mapOf('A' to hiddenSpike, 'B' to Glyph(spike = true, dir = Dir.DOWN, hidden = true), 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(5f), Play(Card.GRAND_FINALE), Fall('a'), Say(T("Bit 1: the floor.", "Bit 1: der Boden."))),
                trap(PastX(12.5f), Show('A'), Say(T("Bit 2: the spikes.", "Bit 2: die Spikes."))),
                trap(PastX(17.5f), Fall('S'), Say(T("Bit 3: the ceiling.", "Bit 3: die Decke."))),
                trap(PastX(26.5f), Gravity(true), Swap(true), Shake(2f), DoorTo(29, 0, speed = 30f), Say(T("127 + 1 = -128!  OVERFLOW!", "127 + 1 = -128!  ÜBERLAUF!"))),
                trap(PastX(26.5f), DoorTo(2, 1, speed = 18f, hanging = true), Say(T("Everything is negative now. Go home.", "Alles ist jetzt negativ. Geh nach Hause.")), delay = 0.5f),
                trap(Zone(9f, 0f, 20f, 3f), Show('B'), Say(T("Sign bit set. Spikes appear.", "Vorzeichenbit gesetzt. Spikes erscheinen."))),
            ),
        ) {
            border(); floor()
            fill(9..11, 15..17, 'a')
            put(16, 14, 'A'); put(17, 14, 'A')
            put(21, 1, 'S'); put(22, 1, 'S')
            put(23, 1, 'v'); put(24, 1, 'v')
            put(14, 0, '.'); put(15, 0, '.')
            put(8, 1, 'B'); put(9, 1, 'B')
            put(2, 14, 'P'); put(29, 14, 'D')
        },
    )
}
