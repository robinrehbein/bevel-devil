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

/** World 1, levels 97-112: hard mode. Loops, timing, and things that come back. */
object World1Part7 {

    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    /** A platform on a rectangular loop: up 6, right 11, down 6, left 11. Three laps. */
    private fun ferris(g: Char, start: Float): List<Trap> {
        val up = 2f
        val across = 11f / 3f
        val cycle = up + across + up + across
        return (0..2).flatMap { k ->
            val b = start + cycle * k
            listOf(
                trap(After(b), Move(g, 0f, -6f, 3f)),
                trap(After(b + up + 0.05f), Move(g, 11f, 0f, 3f)),
                trap(After(b + up + across + 0.1f), Move(g, 0f, 6f, 3f)),
                trap(After(b + 2 * up + across + 0.15f), Move(g, -11f, 0f, 3f)),
            )
        }
    }

    /** A wall with a one-tile tunnel below that closes and opens every 2.6 s. */
    private fun chomp(g: Char, phase: Float): List<Trap> = (0..3).flatMap { k ->
        listOf(
            trap(After(phase + 2.6f * k), Move(g, 0f, 1f, 6f)),
            trap(After(phase + 2.6f * k + 1.0f), Move(g, 0f, -1f, 6f)),
        )
    }

    val levels: List<Level> = listOf(
        // 97 — a platform rides a rectangular loop; it leaves 0.3 s after you arrive, board it on the next lap
        Level(
            name = T("Ferris Wheel", "Riesenrad"),
            intro = T("The next ride starts in a moment. The moment was 0.3 seconds ago.", "Die nächste Fahrt beginnt gleich. Das \"gleich\" war vor 0,3 Sekunden."),
            traps = listOf(trap(After(0.3f), Play(Card.SINKING), Say(T("Please stay seated. Or wait for the next lap.", "Bitte sitzen bleiben. Oder auf die nächste Runde warten.")))) +
                ferris('w', 0.3f),
        ) {
            border(); floor(); pit(6..23)
            fill(9..11, 14..14, 'w')
            fill(24..30, 8..8)
            put(2, 14, 'P'); put(29, 7, 'D')
        },

        // 98 — a plateau over a chasm: spikes, a missing plank and a falling rock, in that order
        Level(
            name = T("Cliffhanger", "Nervenkitzel"),
            intro = T("Step up to the edge. Then keep going.", "Tritt an die Kante. Dann geh weiter."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(12.5f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Watch the spikes. And the edge.", "Achte auf die Spikes. Und auf die Kante."))),
                trap(PastX(19f), Fall('c'), Say(T("Plank 3 of 30 is missing.", "Brett 3 von 30 fehlt."))),
                trap(PastX(23.5f), Fall('S'), Say(T("Falling rocks. Very original.", "Steinschlag. Sehr originell."))),
            ),
        ) {
            border(); floor(); pit(10..30)
            fill(10..30, 13..13); fill(22..24, 13..13, 'c')
            put(16, 12, 'A'); put(17, 12, 'A')
            put(27, 1, 'S'); put(28, 1, 'S')
            put(2, 14, 'P'); put(30, 12, 'D')
        },

        // 99 — every hopped spike patch is replaced by a bigger one
        // EASTER EGG: "99 little bugs in the code"
        Level(
            name = T("99 Little Bugs", "99 kleine Bugs"),
            intro = T("99 little bugs in the code. Take one down, patch it around...", "99 kleine Bugs im Code. Einen gefixt, drumherum gepatcht..."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike, 'C' to hiddenSpike),
            traps = listOf(
                trap(PastX(7f), Play(Card.SPIKE_SEED), Show('A'), Say(T("99 little bugs in the code.", "99 kleine Bugs im Code."))),
                trap(PastX(13.5f), Show('B'), Say(T("Take one down, patch it around.", "Einen gefixt, drumherum gepatcht."))),
                trap(PastX(20.5f), Show('C'), Say(T("127 little bugs in the code.", "127 kleine Bugs im Code."))),
            ),
        ) {
            border(); floor()
            put(11, 14, 'A'); put(12, 14, 'A'); put(18, 14, 'B'); put(19, 14, 'B'); put(25, 14, 'C'); put(26, 14, 'C')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 100 — a zig-zag tower; the platforms wobble and the spiky floor rises
        // EASTER EGG: 100 = 0b1100100
        Level(
            name = T("Tower of Babel", "Turmbau zu Babel"),
            intro = T("Level 100 = 0b1100100. Seven floors, one language: spikes.", "Level 100 = 0b1100100. Sieben Etagen, eine Sprache: Spikes."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(After(2.5f), Play(Card.SPIKE_SEED), Move('L', 0f, -17f, 1.5f), Say(T("The floor is rising. Babel-fish not included.", "Der Boden steigt. Babelfisch nicht inklusive."))),
                trap(Touch('b'), Fall('b'), delay = 0.4f),
                trap(Touch('d'), Fall('d'), delay = 0.4f),
                trap(PastX(15f), Show('A'), Say(T("One last surprise at the top.", "Eine letzte Überraschung ganz oben."))),
            ),
        ) {
            border()
            fill(1..6, 14..14); fill(9..13, 12..12); fill(2..6, 10..10, 'b'); fill(9..13, 8..8); fill(2..6, 6..6, 'd'); fill(9..13, 4..4)
            fill(14..30, 4..4)
            fill(1..30, 17..17, 'L')
            put(20, 3, 'A')
            put(2, 13, 'P'); put(29, 3, 'D')
        },

        // 101 — controls swap on, off, on again, with a pit after each swap
        // NOD: hall of mirrors
        Level(
            name = T("Hall of Mirrors", "Spiegelkabinett"),
            intro = T("Which one is the real you? The one falling into the pit.", "Welcher ist der echte du? Der, der in die Grube fällt."),
            traps = listOf(
                trap(PastX(7f), Play(Card.TWISTED), Swap(true), Say(T("Mirror on.", "Spiegel an."))),
                trap(PastX(16f), Swap(false), Say(T("Mirror off. Trust your feet.", "Spiegel aus. Vertrau deinen Füßen."))),
                trap(PastX(26f), Swap(true), Say(T("Mirror on. Again. Sorry.", "Spiegel an. Schon wieder. Sorry."))),
            ),
        ) {
            border(); floor(); pit(10..11); pit(20..21); pit(28..29)
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 102 — three gaps of four tiles between two-tile islands; the middle island is not made for standing
        // NOD: Coyote Ugly / coyote time
        Level(
            name = T("Coyote Ugly", "Coyote Ugly"),
            intro = T("Four tiles of nothing. Coyote time is your friend.", "Vier Felder Nichts. Koyotenzeit ist dein Freund."),
            traps = listOf(
                trap(Touch('b'), Play(Card.CRUMBLE), Fall('b'), Say(T("Ugly, but honest work.", "Hässlich, aber ehrliche Arbeit.")), delay = 0.3f),
            ),
        ) {
            border(); floor(); pit(4..27)
            fill(8..9, 15..17); fill(14..15, 15..17, 'b'); fill(20..21, 15..17); fill(26..27, 15..17)
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 103 — touch the switch on the far left; the gate opens for four seconds
        // NOD: expired session token
        Level(
            name = T("Session Expired", "Sitzung abgelaufen"),
            intro = T("Please log in. Your token is valid for four seconds.", "Bitte einloggen. Dein Token ist vier Sekunden gültig."),
            traps = listOf(
                trap(Touch('k'), Play(Card.DECOY), Hide('w'), Say(T("Login successful. Token: 4 s.", "Login erfolgreich. Token: 4 s."))),
                trap(Touch('k'), Show('w'), Say(T("401: Session expired.", "401: Sitzung abgelaufen.")), delay = 4f),
            ),
        ) {
            border(); floor(); pit(12..13)
            fill(22..22, 5..14, 'w')
            put(18, 14, '^'); put(19, 14, '^')
            put(2, 14, 'k')
            put(6, 14, 'P'); put(29, 14, 'D')
        },

        // 104 — gravity is flipped from the start; everything happens on the ceiling
        Level(
            name = T("Ceiling Commuter", "Deckenpendler"),
            intro = T("Business as usual. Please mind the gap. Above you.", "Alles wie immer. Bitte beachten Sie die Lücke. Über Ihnen."),
            legend = mapOf('A' to Glyph(spike = true, dir = Dir.DOWN, hidden = true)),
            traps = listOf(
                trap(After(0.05f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Next stop: the ceiling.", "Nächster Halt: die Decke."))),
                trap(PastX(18f), Show('A'), Say(T("Delays due to spikes.", "Verspätung wegen Spikes."))),
            ),
        ) {
            border(); floor()
            put(9, 1, 'v'); put(10, 1, 'v')
            put(16, 0, '.'); put(17, 0, '.')
            put(22, 1, 'A'); put(23, 1, 'A')
            put(2, 14, 'P'); put(29, 1, 'D')
        },

        // 105 — a long fall from the ledge; a platform appears at the last moment and does not stay
        Level(
            name = T("Trust Fall", "Vertrauensfall"),
            intro = T("Just step forward. I will catch you. (No.)", "Tritt einfach vor. Ich fange dich auf. (Nein.)"),
            legend = mapOf('p' to hiddenSolid),
            traps = listOf(
                trap(Zone(8f, 5f, 16f, 9.5f), Play(Card.GHOST_BLOCK), Show('p'), Say(T("Caught you! ...for half a second.", "Aufgefangen! ...für eine halbe Sekunde.")), delay = 0.05f),
                trap(Touch('p'), Fall('p'), delay = 0.5f),
            ),
        ) {
            border(); floor()
            fill(1..18, 14..14, '^')
            fill(1..7, 3..3)
            fill(10..14, 12..12, 'p')
            put(3, 2, 'P'); put(29, 14, 'D')
        },

        // 106 — three floor segments are deleted and restored one after another; the trick is patience
        // NOD: undo history
        Level(
            name = T("Undo History", "Änderungsverlauf"),
            intro = T("Oops. I deleted the floor. Three times. Ctrl+Z, Ctrl+Z, Ctrl+Z.", "Ups. Ich habe den Boden gelöscht. Dreimal. Strg+Z, Strg+Z, Strg+Z."),
            traps = listOf(
                trap(PastX(9f), Play(Card.COLLAPSE), Hide('a'), Say(T("Delete.", "Löschen."))),
                trap(PastX(9f), Show('a'), Say(T("Undo.", "Rückgängig.")), delay = 2.6f),
                trap(PastX(9f), Hide('b'), delay = 0.9f),
                trap(PastX(9f), Show('b'), Say(T("Undo.", "Rückgängig.")), delay = 3.5f),
                trap(PastX(9f), Hide('c'), delay = 1.8f),
                trap(PastX(9f), Show('c'), Say(T("Undo. Finally.", "Rückgängig. Endlich.")), delay = 4.4f),
            ),
        ) {
            border(); floor()
            fill(12..15, 15..17, 'a'); fill(16..19, 15..17, 'b'); fill(20..23, 15..17, 'c')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 107 — three branches enter, one player leaves: walls from both sides
        // NOD: git rebase
        Level(
            name = T("Rebase", "Rebase"),
            intro = T("Three branches, one you. Let's rebase.", "Drei Branches, ein Du. Wir rebasen das."),
            legend = mapOf(
                'L' to Glyph(spike = true, dir = Dir.RIGHT),
                'R' to Glyph(spike = true, dir = Dir.LEFT),
                'Q' to Glyph(spike = true, dir = Dir.LEFT),
            ),
            traps = listOf(
                trap(After(2.4f), Play(Card.DEVIL_SAW), Move('R', -20f, 0f, 4.2f), Move('L', 25f, 0f, 3f), Say(T(">>>>>>> feature/lasers", ">>>>>>> feature/laser"))),
                trap(After(3.6f), Move('Q', -26f, 0f, 4.2f), Say(T("<<<<<<< HEAD  Also me.", "<<<<<<< HEAD  Ich auch."))),
            ),
        ) {
            border(); floor()
            put(4, 13, 'L'); put(4, 14, 'L')
            put(20, 13, 'R'); put(20, 14, 'R')
            put(28, 13, 'Q'); put(28, 14, 'Q')
            put(12, 14, 'P'); put(30, 14, 'D')
        },

        // 108 — three heads for every guillotine: saws fall in bunches
        Level(
            name = T("Hydra", "Hydra"),
            intro = T("Cut off one saw, two more grow back.", "Schneidest du eine Säge ab, wachsen zwei nach."),
            traps = listOf(
                trap(PastX(7f), Play(Card.DEVIL_SAW), Saw(9.5f, 9f, 0f, 22f, 0.6f), Saw(10.7f, 9f, 0f, 22f, 0.6f), Say(T("Heads up. Or down.", "Kopf hoch. Oder runter."))),
                trap(PastX(7f), Saw(10.1f, 9f, 0f, 22f, 0.6f), delay = 0.7f),
                trap(PastX(14f), Saw(16.5f, 9f, 0f, 22f, 0.6f), Saw(17.7f, 9f, 0f, 22f, 0.6f)),
                trap(PastX(14f), Saw(17.1f, 9f, 0f, 22f, 0.6f), delay = 0.7f),
                trap(PastX(21f), Saw(23.5f, 9f, 0f, 22f, 0.6f), Saw(24.7f, 9f, 0f, 22f, 0.6f), Say(T("Regrowth: guaranteed.", "Nachwuchs: garantiert."))),
                trap(PastX(21f), Saw(24.1f, 9f, 0f, 22f, 0.6f), delay = 0.7f),
            ),
        ) {
            border(); floor()
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 109 — three chomping walls with a tunnel under each; each one has its own rhythm
        Level(
            name = T("Chompers", "Beißer"),
            intro = T("Three tunnels. Three appetites.", "Drei Tunnel. Drei Appetite."),
            traps = listOf(trap(After(1.4f), Play(Card.HEADBUTT), Say(T("Nom nom nom.", "Nom nom nom.")))) +
                chomp('w', 1.4f) + chomp('x', 3.4f) + chomp('y', 5.3f),
        ) {
            border(); floor()
            fill(13..15, 8..13, 'w'); fill(19..21, 8..13, 'x'); fill(25..27, 8..13, 'y')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 110 — a bulldozer from the right, spikes, and a second bulldozer from behind
        Level(
            name = T("Steamroller", "Dampfwalze"),
            intro = T("Road works. Sorry about the traffic. Both directions.", "Bauarbeiten. Sorry wegen des Verkehrs. In beide Richtungen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(8f), Play(Card.DEVIL_SAW), Move('m', -30f, 0f, 5f), Say(T("Beep beep. From the front.", "Piep piep. Von vorne."))),
                trap(PastX(13.5f), Show('A')),
                trap(PastX(14f), Move('n', 33f, 0f, 6f), Say(T("Beep beep. From behind.", "Piep piep. Von hinten."))),
            ),
        ) {
            border(); floor()
            fill(22..24, 14..14, 'm'); fill(1..3, 14..14, 'n')
            put(16, 14, 'A'); put(17, 14, 'A')
            put(6, 14, 'P'); put(29, 14, 'D')
        },

        // 111 — collapse, spikes, falling rocks, and then the ceiling becomes the floor
        Level(
            name = T("Final Exam", "Abschlussprüfung"),
            intro = T("Open book. Closed door.", "Open Book. Geschlossene Tür."),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(5f), Play(Card.COLLAPSE), Fall('a'), Say(T("Question 1: floors.", "Frage 1: Böden."))),
                trap(PastX(11.5f), Show('A'), Say(T("Question 2: spikes.", "Frage 2: Spikes."))),
                trap(PastX(16f), Fall('S'), Say(T("Question 3: ceilings.", "Frage 3: Decken."))),
                trap(PastX(23f), Gravity(true), Say(T("Bonus question: physics.", "Bonusfrage: Physik."))),
            ),
        ) {
            border(); floor()
            fill(8..10, 15..17, 'a')
            put(15, 14, 'A'); put(16, 14, 'A')
            put(21, 1, 'S'); put(22, 1, 'S')
            put(27, 0, '.'); put(28, 0, '.')
            put(2, 14, 'P'); put(30, 1, 'D')
        },

        // 112 — a saw chases you while pit, spikes and swapped controls are all still unpatched
        // NOD: zero-day exploit
        Level(
            name = T("Zero Day", "Zero Day"),
            intro = T("No patch available. No plan either.", "Kein Patch verfügbar. Kein Plan auch nicht."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(4f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f), Say(T("CVE-2025-0042. Unpatched.", "CVE-2025-0042. Ungepatcht."))),
                trap(PastX(5.5f), Fall('a')),
                trap(PastX(12.5f), Show('A')),
                trap(PastX(21f), Swap(true), Say(T("Left is right. That one is a feature.", "Links ist rechts. Das ist ein Feature."))),
            ),
        ) {
            border(); floor(); pit(25..26)
            fill(9..11, 15..17, 'a')
            put(16, 14, 'A'); put(17, 14, 'A')
            put(2, 14, 'P'); put(30, 14, 'D')
        },
    )
}
