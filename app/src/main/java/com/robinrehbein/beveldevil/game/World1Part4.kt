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

/** World 1, levels 49-64: two tricks at a time, and the first proper nerd jokes. */
object World1Part4 {

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
        // 49 — no door anywhere. Walk left; it turns up on the far side once you gave up.
        // EASTER EGG: HTTP 404 Not Found
        Level(
            name = T("404: Door Not Found", "404: Tür nicht gefunden"),
            intro = T("The door you are looking for does not exist.", "Die gesuchte Tür existiert nicht."),
            traps = listOf(
                trap(BeforeX(5.5f), Play(Card.DECOY), Say(T("404. Redirecting you to the door...", "404. Leite dich zur Tür weiter..."))),
                trap(BeforeX(5.5f), DoorTo(28, 14, speed = 12f), delay = 1.6f),
            ),
        ) {
            border(); floor(); pit(19..20); pit(11..12)
            put(26, 14, 'P'); put(30, 0, 'D')
        },

        // 50 — a spiked pit fills up with crates that keep coming: the stack overflows into a bump
        // EASTER EGG: Stack Overflow
        Level(
            name = T("Stack Overflow", "Stapelüberlauf"),
            intro = T("Q: How do I cross a pit? A: Wait.", "F: Wie überquere ich eine Grube? A: Warte."),
            traps = listOf(
                trap(After(2.5f), Play(Card.HEADBUTT), Fall('a'), Say(T("push(crate)", "push(Kiste)"))),
                trap(After(3.4f), Fall('b'), Say(T("push(crate)", "push(Kiste)"))),
                trap(After(4.3f), Fall('c'), Say(T("Stack overflow!", "Stapelüberlauf!"))),
            ),
        ) {
            border(); floor()
            fill(12..17, 15..16, '.'); fill(12..17, 16..16, '^')
            fill(12..17, 3..3, 'a'); fill(12..17, 2..2, 'b'); fill(12..17, 1..1, 'c')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 51 — one saw chases you while another rushes towards you. Who gets there first?
        // EASTER EGG: race condition (two threads)
        Level(
            name = T("Race Condition", "Wettlaufsituation"),
            intro = T("Two threads, one you. May the fastest win.", "Zwei Threads, ein Du. Der Schnellste gewinnt."),
            traps = listOf(
                trap(PastX(4f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 7f, 0f, 0.62f), Say(T("Thread 1: chase.", "Thread 1: Verfolgung."))),
                trap(After(2f), Saw(33f, 14.4f, -9f, 0f, 0.62f), Say(T("Thread 2: collide. Undefined behaviour!", "Thread 2: Kollision. Undefiniertes Verhalten!"))),
            ),
        ) {
            border(); floor()
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 52 — three crushers loop up and down. Time your run, or squish.
        // EASTER EGG: while (true) infinite loop
        Level(
            name = T("Infinite Loop", "Endlosschleife"),
            intro = T("while (true) { squish(); }", "while (true) { quetschen(); }"),
            traps = listOf(trap(After(0.3f), Play(Card.HEADBUTT), Say(T("Ctrl+C will not help you.", "Strg+C hilft dir hier nicht.")))) +
                slabLoop('a', 0.3f) + slabLoop('b', 0.8f) + slabLoop('c', 1.3f),
        ) {
            border(); floor()
            fill(9..11, 3..4, 'a'); fill(16..18, 3..4, 'b'); fill(23..25, 3..4, 'c')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 53 — the first jump swaps your controls. Mephi: works on my machine.
        // EASTER EGG: "It works on my machine"
        Level(
            name = T("Works On My Machine", "Bei mir läuft's"),
            intro = T("I tested this level. Twice. On my machine.", "Ich habe das Level getestet. Zweimal. Bei mir."),
            traps = listOf(
                trap(Zone(0f, 0f, 32f, 13.9f), Play(Card.TWISTED), Swap(true), Say(T("Works on my machine!", "Bei mir läuft's!"))),
            ),
        ) {
            border(); floor(); pit(20..21)
            put(14, 14, '#')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 54 — the floor is deleted when you are halfway; the shelf above it is not
        // EASTER EGG: sudo rm -rf /
        Level(
            name = T("sudo rm -rf /", "sudo rm -rf /"),
            intro = T("Are you sure? [y/N]", "Bist du sicher? [j/N]"),
            traps = listOf(
                trap(PastX(14f), Play(Card.COLLAPSE), Fall('f'), Shake(1.5f), Say(T("rm: removing '/' ... done. You wanted root.", "rm: entferne '/' ... erledigt. Du wolltest doch root."))),
            ),
        ) {
            border()
            fill(1..30, 15..17, 'f')
            fill(4..30, 13..13)
            put(20, 12, '^')
            put(2, 14, 'P'); put(29, 12, 'D')
        },

        // 55 — reach the door and I overwrite history: it moves back to the start and spikes appear
        // EASTER EGG: git push --force
        Level(
            name = T("git push --force", "git push --force"),
            intro = T("The door is at the remote. Or so you thought.", "Die Tür liegt im Remote. Dachtest du."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(23f), Play(Card.SHY_DOOR), DoorTo(29, 0, speed = 25f), Show('A'), Say(T("History rewritten. You're welcome.", "Historie umgeschrieben. Gern geschehen."))),
                trap(PastX(23f), DoorTo(2, 14, speed = 20f), delay = 0.8f),
            ),
        ) {
            border(); floor(); pit(11..12)
            put(18, 14, '#')
            put(5, 14, 'A'); put(6, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 56 — at the first step everything flips: gravity and controls. Walk the ceiling.
        // EASTER EGG: Kernel panic
        Level(
            name = T("Kernel Panic", "Kernel Panic"),
            intro = T("Everything is fine. Really.", "Alles in Ordnung. Wirklich."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Swap(true), Shake(1.5f), Say(T("KERNEL PANIC - not syncing", "KERNEL PANIC - nicht synchronisiert"))),
            ),
        ) {
            border(); floor()
            fill(6..27, 14..14, '^')
            put(18, 0, '.'); put(19, 0, '.')
            put(2, 14, 'P'); put(29, 1, 'D')
        },

        // 57 — controls swap right as a saw starts chasing you, and there is a pit
        Level(
            name = T("Twisted Saw", "Verdrehte Säge"),
            intro = T("A relaxed stroll. Left to right.", "Ein gemütlicher Spaziergang. Von links nach rechts."),
            traps = listOf(
                trap(PastX(6f), Play(Card.TWISTED), Swap(true), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f), Say(T("Left is right! Also: saw.", "Links ist rechts! Außerdem: Säge."))),
            ),
        ) {
            border(); floor(); pit(17..18)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 58 — the huge spike field is a paper tiger; the real spikes come later
        Level(
            name = T("Paper Tiger", "Papiertiger"),
            intro = T("Seven spikes. Pretty scary, right?", "Sieben Spikes. Ziemlich gruselig, oder?"),
            legend = mapOf('B' to hiddenSpike),
            traps = listOf(
                trap(PastX(6.5f), Hide('A'), Say(T("Just kidding. They're stickers.", "War nur Spaß. Sind Aufkleber."))),
                trap(PastX(15.5f), Play(Card.SPIKE_SEED), Show('B'), Say(T("These are real.", "Die hier sind echt."))),
            ),
        ) {
            border(); floor()
            fill(8..14, 14..14, 'A')
            put(19, 14, 'B'); put(20, 14, 'B')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 59 — the lift goes down. And keeps going. Get off in time.
        Level(
            name = T("Elevator Down", "Fahrstuhl abwärts"),
            intro = T("Going down. Next stop: the basement of the basement.", "Es geht abwärts. Nächster Halt: Keller vom Keller."),
            traps = listOf(
                trap(Touch('l'), Play(Card.SINKING), Move('l', 0f, 14f, 4f), Say(T("Ding! Basement. And further.", "Ding! Keller. Und weiter."))),
            ),
        ) {
            border(); floor(); pit(1..17)
            fill(1..10, 6..6)
            fill(11..15, 6..6, 'l')
            put(2, 5, 'P'); put(29, 14, 'D')
        },

        // 60 — sinking platforms and a saw at waist height
        Level(
            name = T("Sinking Saw", "Sinkende Säge"),
            intro = T("Solid platforms. Plus one small saw.", "Stabile Plattformen. Plus eine kleine Säge."),
            traps = listOf(
                trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, 12f, 8f), delay = 0.1f),
                trap(Touch('b'), Move('b', 0f, 12f, 8f), delay = 0.1f),
                trap(Touch('c'), Move('c', 0f, 12f, 8f), delay = 0.1f),
                trap(Touch('d'), Move('d', 0f, 12f, 8f), delay = 0.1f),
                trap(After(2.3f), Saw(33f, 13.7f, -7f, 0f, 0.6f), Say(T("And a saw. For the road.", "Und eine Säge. Für unterwegs."))),
            ),
        ) {
            border(); floor(); pit(6..27)
            fill(8..10, 14..14, 'a'); fill(13..15, 14..14, 'b'); fill(18..20, 14..14, 'c'); fill(23..25, 14..14, 'd')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 61 — gravity flips off, on, and off again. Mind where you land.
        // EASTER EGG: "Have you tried turning it off and on again?"
        Level(
            name = T("Off And On Again", "Aus- und wieder Einschalten"),
            intro = T("Have you tried turning it off and on again?", "Hast du schon versucht, es aus- und wieder einzuschalten?"),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Turning it off...", "Ausschalten..."))),
                trap(PastX(13f), Gravity(false), Say(T("...and on again.", "...und wieder ein."))),
                trap(PastX(20f), Gravity(true), Say(T("Did it fix the problem? No.", "Hat es geholfen? Nein."))),
            ),
        ) {
            border(); floor()
            fill(6..12, 14..14, '^'); fill(22..27, 14..14, '^')
            put(17, 14, '^')
            put(9, 1, 'v'); put(10, 1, 'v'); put(25, 1, 'v')
            put(2, 14, 'P'); put(29, 1, 'D')
        },

        // 62 — saws sweep the floor; ledges are the safe spots
        Level(
            name = T("Higher Ground", "Hochwasserschutz"),
            intro = T("It is a floor-only hazard. Somebody said so.", "Gefahr nur am Boden. Hat jemand gesagt."),
            traps = listOf(
                trap(After(2f), Play(Card.DEVIL_SAW), Saw(33f, 14.4f, -9f, 0f, 0.62f), Say(T("Sweeping the floor!", "Bodenreinigung!"))),
                trap(After(5.5f), Saw(33f, 14.4f, -9f, 0f, 0.62f), Say(T("Second pass.", "Zweiter Durchgang."))),
            ),
        ) {
            border(); floor()
            fill(10..12, 13..13); fill(20..22, 13..13)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 63 — the floor behind you is gone and so is the door: it went back to the start
        Level(
            name = T("Point of No Return", "Kein Zurück"),
            intro = T("It's only forwards from here.", "Ab hier geht es nur vorwärts."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(16f), Play(Card.COLLAPSE), Fall('a'), Say(T("No going back.", "Kein Zurück."))),
                trap(PastX(20f), DoorTo(29, 0, speed = 25f), Show('A'), Say(T("Going back. Surprise!", "Doch zurück. Überraschung!"))),
                trap(PastX(20f), DoorTo(2, 14, speed = 20f), delay = 0.8f),
            ),
        ) {
            border(); floor()
            fill(12..14, 15..17, 'a')
            put(5, 14, 'A'); put(6, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 64 — 1, 2, 4, 8 tiles wide; each platform crumbles twice as slowly as the one before
        // EASTER EGG: 2^6 = 64, powers of two
        Level(
            name = T("Power of Two", "Zweierpotenz"),
            intro = T("2^6 = 64. Platforms: 1, 2, 4, 8.", "2^6 = 64. Plattformen: 1, 2, 4, 8."),
            traps = listOf(
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), Say(T("Do the math.", "Rechne nach.")), delay = 0.15f),
                trap(Touch('b'), Fall('b'), delay = 0.3f),
                trap(Touch('c'), Fall('c'), delay = 0.6f),
                trap(Touch('d'), Fall('d'), delay = 1.2f),
            ),
        ) {
            border(); floor(); pit(6..30)
            fill(7..7, 14..14, 'a'); fill(11..12, 14..14, 'b'); fill(16..19, 14..14, 'c'); fill(23..30, 14..14, 'd')
            put(2, 14, 'P'); put(28, 13, 'D')
        },
    )
}
