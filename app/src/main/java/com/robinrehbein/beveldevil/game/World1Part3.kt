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

/** World 1, levels 33-48: twists. Fake traps, timing, waiting, and the answer. */
object World1Part3 {

    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    val levels: List<Level> = listOf(
        // 33 — the first trap is a bluff, the second one is real
        Level(
            name = T("Crying Wolf", "Der Hirte und der Wolf"),
            intro = T("Watch the ceiling. Or don't. I mean it this time.", "Achte auf die Decke. Oder nicht. Diesmal meine ich es ernst."),
            legend = mapOf('S' to ceilingSpike),
            traps = listOf(
                trap(PastX(2.6f), Fall('S'), Shake(0.6f), Say(T("Incoming!", "Achtung, Einschlag!"))),
                trap(PastX(18f), Play(Card.COLLAPSE), Fall('a'), Say(T("Now THAT was the real one.", "DAS war jetzt die echte."))),
            ),
        ) {
            border(); floor()
            put(12, 1, 'S'); put(13, 1, 'S')
            fill(22..24, 15..17, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 34 — an invisible bridge appears under you, but only if you jump
        Level(
            name = T("Leap of Faith", "Glaube versetzt Brücken"),
            intro = T("That gap is far too wide. Believe in yourself.", "Die Lücke ist viel zu breit. Glaub an dich."),
            legend = mapOf('b' to hiddenSolid),
            traps = listOf(
                trap(Zone(9.5f, 0f, 13.4f, 13.9f), Play(Card.GHOST_BLOCK), Show('b'), Say(T("See? It was always there.", "Siehst du? Sie war immer da.")), delay = 0.12f),
            ),
        ) {
            border(); floor(); pit(13..20)
            fill(13..20, 15..15, 'b')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 35 — climb: the floor is spikes and it is rising
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

        // 36 — tetrominoes drop from the ceiling and build the stairs. You only have to wait.
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

        // 37 — two walls of spikes converge; jump the one that comes at you
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

        // 38 — saws drop from the ceiling just as you pass under; stop and let each one go
        Level(
            name = T("Guillotine", "Fallbeil"),
            intro = T("Head up! No wait. Head down. No wait.", "Kopf hoch! Nein, Kopf runter. Nein, warte."),
            traps = listOf(
                trap(PastX(7f), Play(Card.DEVIL_SAW), Saw(9.5f, 9f, 0f, 22f, 0.6f), Say(T("Guillotine service!", "Fallbeil-Lieferservice!"))),
                trap(PastX(13f), Saw(15.5f, 9f, 0f, 22f, 0.6f)),
                trap(PastX(19f), Saw(21.5f, 9f, 0f, 22f, 0.6f)),
                trap(PastX(24f), Saw(26.5f, 9f, 0f, 22f, 0.6f), Say(T("Habit-forming, isn't it?", "Macht süchtig, oder?"))),
            ),
        ) {
            border(); floor()
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 39 — the bridge is built plank by plank, on a timer
        // NOD: 90s web "under construction"
        Level(
            name = T("Under Construction", "Baustelle"),
            intro = T("This page is under construction. Please hold.", "Diese Seite befindet sich im Aufbau. Bitte warten."),
            legend = mapOf('a' to hiddenSolid, 'b' to hiddenSolid, 'c' to hiddenSolid, 'd' to hiddenSolid),
            traps = listOf(
                trap(After(2.5f), Play(Card.GHOST_BLOCK), Show('a'), Say(T("Plank one. Hurry, it's temporary.", "Brett eins. Beeil dich, ist nur vorübergehend."))),
                trap(After(3.4f), Show('b')),
                trap(After(4.3f), Show('c')),
                trap(After(5.2f), Show('d')),
                trap(After(6.5f), Hide('a')),
                trap(After(7.4f), Hide('b')),
                trap(After(8.3f), Hide('c')),
                trap(After(9.2f), Hide('d')),
            ),
        ) {
            border(); floor(); pit(6..27)
            fill(8..10, 14..14, 'a'); fill(13..15, 14..14, 'b'); fill(18..20, 14..14, 'c'); fill(23..25, 14..14, 'd')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 40 — a bulldozer slides in from the right; hop it, then spikes appear where you land
        Level(
            name = T("Bulldozer", "Planierraupe"),
            intro = T("Beep beep. Beep beep.", "Piep piep. Piep piep."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(8f), Play(Card.DEVIL_SAW), Move('m', -30f, 0f, 5f), Say(T("Road works!", "Bauarbeiten!"))),
                trap(PastX(13.5f), Show('A'), Say(T("Also: spikes. Sorry.", "Außerdem: Spikes. Sorry."))),
            ),
        ) {
            border(); floor()
            fill(22..24, 14..14, 'm')
            put(16, 14, 'A'); put(17, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 41 — the whole level is mirrored, until it suddenly is not
        Level(
            name = T("Mirror Mirror", "Spieglein, Spieglein"),
            intro = T("Left is right and right is... wait, what?", "Links ist rechts und rechts ist... Moment, was?"),
            traps = listOf(
                trap(After(0.05f), Play(Card.TWISTED), Swap(true), Say(T("Mirror, mirror, on the wall.", "Spieglein, Spieglein an der Wand."))),
                trap(PastX(25.5f), Swap(false), Say(T("Habits die hard.", "Gewohnheiten sterben langsam."))),
            ),
        ) {
            border(); floor()
            pit(9..10); pit(21..22)
            put(16, 14, '^'); put(28, 14, '^')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 42 — the answer: 0b101010. A cosmic ray flips bits in the floor.
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

        // 43 — an invisible ceiling clips your jump
        Level(
            name = T("Glass Ceiling", "Gläserne Decke"),
            intro = T("The sky is the limit. Well, roughly.", "Der Himmel ist die Grenze. Ungefähr."),
            legend = mapOf('g' to ghost),
            traps = listOf(
                trap(Touch('g'), Play(Card.GHOST_BLOCK), Say(T("Invisible ceiling. Patent pending.", "Unsichtbare Decke. Patent angemeldet."))),
            ),
        ) {
            border(); floor(); pit(13..14)
            fill(8..18, 12..12, 'g')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 44 — the floor ahead of you vanishes, one trapdoor at a time
        Level(
            name = T("Trapdoor Alley", "Klappenallee"),
            intro = T("Solid ground. All the way. Promise.", "Fester Boden. Die ganze Strecke. Versprochen."),
            traps = listOf(
                trap(PastX(5.5f), Play(Card.COLLAPSE), Hide('a'), Say(T("Ground: 404 not found.", "Boden: 404 nicht gefunden."))),
                trap(PastX(12.5f), Hide('b')),
                trap(PastX(19.5f), Hide('c'), Say(T("Ta-da. Again.", "Tadaa. Schon wieder."))),
            ),
        ) {
            border(); floor()
            fill(10..11, 15..17, 'a'); fill(17..18, 15..17, 'b'); fill(24..25, 15..17, 'c')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 45 — the door approaches in stages, stalls at 99% and then jumps back. Just wait.
        // NOD: video buffering
        Level(
            name = T("Buffering...", "Lädt..."),
            intro = T("Your door is loading. Please stand by.", "Deine Tür wird geladen. Bitte warten."),
            traps = listOf(
                trap(After(2.6f), Play(Card.SHY_DOOR), DoorTo(23, 10, speed = 5f), Say(T("Buffering... 23%", "Lädt... 23 %"))),
                trap(After(4.4f), DoorTo(16, 11, speed = 5f), Say(T("Buffering... 67%", "Lädt... 67 %"))),
                trap(After(6.2f), DoorTo(9, 12, speed = 5f), Say(T("Buffering... 99%", "Lädt... 99 %"))),
                trap(After(8.3f), DoorTo(28, 2, speed = 30f), Say(T("Connection lost. Retrying...", "Verbindung verloren. Neuer Versuch..."))),
                trap(After(9.6f), DoorTo(4, 14, speed = 20f), Say(T("100%. Was that so hard?", "100 %. War das so schwer?"))),
            ),
        ) {
            border(); floor()
            fill(7..26, 14..14, '^')
            fill(26..30, 7..7)
            put(2, 14, 'P'); put(29, 6, 'D')
        },

        // 46 — any jump flips gravity: the spiked floor becomes a harmless ceiling walk
        // NOD: flip-flop (toggle)
        Level(
            name = T("Flip-Flop", "Kippschalter"),
            intro = T("Please don't jump. You will regret jumping.", "Bitte nicht springen. Du würdest es bereuen."),
            traps = listOf(
                trap(Zone(1f, 0f, 7f, 13.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Flip-flop. Not the sandals.", "Flip-Flop. Nicht die Badelatschen."))),
            ),
        ) {
            border(); floor()
            fill(6..25, 14..14, '^')
            put(15, 1, 'v'); put(16, 1, 'v')
            put(2, 14, 'P'); put(29, 1, 'D')
        },

        // 47 — the wall only opens for the one who finds the root block
        // EASTER EGG: xkcd "sudo make me a sandwich"
        Level(
            name = T("sudo make me a sandwich", "sudo mach mir ein Sandwich"),
            intro = T("\"Make me a sandwich.\" \"What? Make it yourself.\"", "\"Mach mir ein Sandwich.\" \"Was? Mach's dir doch selbst.\""),
            legend = mapOf('k' to ghost),
            traps = listOf(
                trap(Touch('k'), Play(Card.GHOST_BLOCK), Hide('w'), Say(T("Okay.", "Okay."))),
            ),
        ) {
            border(); floor()
            fill(20..21, 3..14, 'w')
            put(10, 12, 'k')
            // the sandwich, hovering above the root block
            art(8, 4, "#####", "#.#.#", ".###.", "#####")
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 48 — three doors, three traps, one real exit
        // NOD: Monty Hall / Let's Make a Deal
        Level(
            name = T("Behind Door Number Three", "Hinter Tür Nummer Drei"),
            intro = T("Pick a door. Any door. Would you like to switch?", "Such dir eine Tür aus. Irgendeine. Willst du wechseln?"),
            legend = mapOf('A' to hiddenSpike, 'S' to ceilingSpike),
            traps = listOf(
                trap(PastX(9.3f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Door one: spikes!", "Tür eins: Spikes!"))),
                trap(PastX(16f), Fall('S'), Say(T("Door two: ceiling!", "Tür zwei: Decke!"))),
                trap(PastX(22f), Fall('a'), Say(T("Door three: no floor!", "Tür drei: kein Boden!"))),
            ),
        ) {
            border(); floor()
            art(9, 3, ".#.", "##.", ".#.", ".#.", "###")      // 1
            art(15, 3, "###", "..#", "###", "#..", "###")     // 2
            art(21, 3, "###", "..#", "###", "..#", "###")     // 3
            put(13, 14, 'A'); put(14, 14, 'A')
            put(20, 1, 'S'); put(21, 1, 'S')
            fill(25..27, 15..17, 'a')
            put(2, 14, 'P'); put(30, 14, 'D')
        },
    )
}
