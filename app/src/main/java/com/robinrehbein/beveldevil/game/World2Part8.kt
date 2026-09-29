package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

private val hidden = Glyph(spike = true, hidden = true)
private val hiddenSolid = Glyph(spike = false, hidden = true)

/** World 2, levels 113-128: "Root" (hex, binary, leet, and the end of the data center). */
internal val world2Part8: List<Level> = listOf(
    // 113 — EASTER EGG: 0xDEADBEEF (eight stones, D E A D B E E F)
    Level(
        name = T("0xDEADBEEF", "0xDEADBEEF"),
        intro = T("Eight stones. D, E, A, D, B, E, E, F.", "Acht Steine. D, E, A, D, B, E, E, F."),
        traps = listOf(
            trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("0xDEADBEEF: uninitialized memory.", "0xDEADBEEF: nicht initialisierter Speicher."), delay = 0.28f),
        ) + ('b'..'h').map { g -> trap(Touch(g), Fall(g), delay = 0.28f) },
    ) {
        border()
        fill(0..5, 15..17); fill(30..31, 15..17)
        for (i in 0..7) fill(6 + 3 * i..7 + 3 * i, 15..15, 'a' + i)
        spawn(); door(30); bits(113)
    },

    // 114 — EASTER EGG: 0xCAFEBABE (the Java class file magic number: a cup of coffee)
    Level(
        name = T("0xCAFEBABE", "0xCAFEBABE"),
        intro = T("Every Java class starts with a cup of coffee. Hot.", "Jede Java-Klasse beginnt mit einer Tasse Kaffee. Heiß."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Zone(18f, 10f, 24f, 15f), Play(Card.SPIKE_SEED), Show('A'), say("Warning: the coffee is hot.", "Achtung: der Kaffee ist heiß.")),
        ),
    ) {
        border(); floor()
        fill(10..11, 13..14); fill(13..14, 11..12)
        fill(16..17, 10..14); fill(24..25, 10..14)
        put(19, 14, 'A'); put(23, 14, 'A')
        spawn(); put(21, 14, 'D'); bits(114, x0 = 26, y = 1)
    },

    // 115 — EASTER EGG: 1337 (leet: the traps fire at x = 3.37 and 13.37)
    Level(
        name = T("1337", "1337"),
        intro = T("Only elite hackers get past x = 13.37.", "Nur Elite-Hacker kommen an x = 13,37 vorbei."),
        traps = listOf(
            trap(PastX(3.37f), Play(Card.TWISTED), Swap(true), say("1337 h4x0r m0d3 3n4bl3d", "1337 h4x0r m0d3 4kt1v13rt")),
            trap(PastX(13.37f), Gravity(true), say("pwn3d.", "g3pwn7.")),
            trap(PastX(13.37f), Swap(false)),
            trap(PastX(26.5f), Gravity(false)),
        ),
    ) {
        border(); floor()
        pit(8..9)
        leds(15..25); put(18, 1, 'v'); put(22, 1, 'v')
        spawn(); door()
    },

    // 116 — EASTER EGG: 0b101010 = 42 (a binary counter decides which stones exist)
    Level(
        name = T("0b101010", "0b101010"),
        intro = T("The answer is 42. The stones follow a binary counter.", "Die Antwort ist 42. Die Steine folgen einem Binärzähler."),
        legend = mapOf('a' to hiddenSolid, 'b' to hiddenSolid, 'c' to hiddenSolid),
        traps = blinkOn('a', 1.0f, 1.0f, 2.0f, 8, first = listOf(Play(Card.GHOST_BLOCK), say("Counter: 001. Stone a exists.", "Zähler: 001. Stein a existiert."))) +
            blinkOn('b', 2.0f, 2.0f, 4.0f, 4) + blinkOn('c', 4.0f, 4.0f, 8.0f, 3),
    ) {
        border()
        fill(0..5, 15..17); fill(27..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(12..14, 15..15, 'b'); fill(17..19, 15..15, 'c'); fill(22..24, 15..15, 'a')
        spawn(); door(); bits(42)
    },

    // 117 — EASTER EGG: infinite loop (while(true) { saw(); })
    Level(
        name = T("Infinite Loop", "Endlosschleife"),
        intro = T("while (true) { spawnSaw(); }", "while (true) { saegeSpawnen(); }"),
        traps = (0 until 8).map { i ->
            val fromLeft = i % 2 == 1
            trap(After(1.0f + i * 1.25f), *(if (i == 0) arrayOf(Play(Card.DEVIL_SAW), say("for(;;) — no exit condition.", "for(;;) — keine Abbruchbedingung.")) else emptyArray()),
                if (fromLeft) Saw(-1.5f, 14.4f, 6f, 0f) else Saw(33.5f, 14.4f, -6f, 0f))
        },
    ) {
        border(); floor()
        rack(12, 2, 2); rack(19, 1, 2); rack(25, 2, 2)
        spawn(); door(); bits(117)
    },

    // 118 — EASTER EGG: sieve of Eratosthenes (only primes get spikes, composites get them anyway)
    Level(
        name = T("Sieve of Eratosthenes", "Sieb des Eratosthenes"),
        intro = T("Spikes stand on every prime. Composites are strictly forbidden.", "Auf jeder Primzahl steht ein Spike. Zusammengesetzte sind streng verboten."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(16f), Play(Card.SPIKE_SEED), Show('A'), say("Composites detected. Adding spikes anyway.", "Zusammengesetzte Zahlen entdeckt. Spikes gibt's trotzdem.")),
        ),
    ) {
        border(); floor()
        for (x in listOf(5, 7, 11, 13, 17, 19, 23, 29)) put(x, 14, '^')
        put(21, 14, 'A'); put(25, 14, 'A'); put(27, 14, 'A')
        spawn(); door(30)
    },

    // 119 — EASTER EGG: Fibonacci (the gaps are 1, 1, 2, 3, and 5, which needs help)
    Level(
        name = T("Fibonacci Gaps", "Fibonacci-Lücken"),
        intro = T("1, 1, 2, 3, 5... The next gap is the problem.", "1, 1, 2, 3, 5... Die nächste Lücke ist das Problem."),
        legend = mapOf('H' to hiddenSolid, 'A' to hidden),
        traps = listOf(
            trap(Touch('f'), Play(Card.GHOST_BLOCK), Show('H'), say("5 is the sum of 2 and 3. So is your bridge.", "5 ist die Summe aus 2 und 3. Deine Brücke auch.")),
            trap(PastX(6.5f), Show('A')),
            trap(Touch('g'), Fall('g'), delay = 0.3f),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(7..9, 15..17); fill(11..13, 15..17); fill(16..18, 15..17, 'g'); fill(22..23, 15..17, 'f'); fill(29..31, 15..17)
        fill(24..28, 15..15, 'H')
        put(8, 14, 'A')
        spawn(); door(30)
    },

    // 120 — EASTER EGG: Schrödinger's platform (solid and not solid, until you look)
    Level(
        name = T("Schrödinger's Platform", "Schrödingers Plattform"),
        intro = T("The platform is both solid and not. Until you stand on it.", "Die Plattform ist fest und nicht fest. Bis du draufstehst."),
        traps = blink('a', 0.5f, 0.35f, 0.75f, 12, first = listOf(Play(Card.GHOST_BLOCK), say("Observing the platform collapses the wave function.", "Beobachten lässt die Wellenfunktion kollabieren."))) +
            blink('b', 0.7f, 0.35f, 0.75f, 12) + blink('c', 0.9f, 0.35f, 0.75f, 12),
    ) {
        border()
        fill(0..5, 15..17); fill(25..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(13..15, 15..15, 'b'); fill(19..21, 15..15, 'c')
        spawn(); door(); bits(120)
    },

    // 121 — EASTER EGG: year 2038 problem (at t = 2.038 s gravity overflows)
    Level(
        name = T("Year 2038", "Jahr 2038"),
        intro = T("The 32-bit clock overflows at 2.038 seconds. Roughly.", "Die 32-Bit-Uhr läuft nach 2,038 Sekunden über. Ungefähr."),
        traps = listOf(
            trap(After(2.038f), Play(Card.UPSIDE_DOWN), Gravity(true), say("time_t overflow: gravity is now negative.", "time_t-Überlauf: Die Schwerkraft ist jetzt negativ.")),
            trap(After(5.5f), Gravity(false), say("Clock reset to 1901.", "Uhr auf 1901 zurückgesetzt.")),
        ),
    ) {
        border(); floor()
        leds(14..24)
        put(11, 1, 'v'); put(17, 1, 'v'); put(18, 1, 'v'); put(22, 1, 'v')
        spawn(); door(30)
    },

    // 122 — EASTER EGG: Ctrl+Alt+Del (three keys, three walls)
    Level(
        name = T("Ctrl+Alt+Del", "Strg+Alt+Entf"),
        intro = T("Press three keys at once. Or one after the other.", "Drei Tasten gleichzeitig. Oder nacheinander."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.GHOST_BLOCK), Hide('x'), Saw(-1.5f, 14.4f, 5f, 0f), say("Ctrl pressed.", "Strg gedrückt.")),
            trap(Touch('b'), Hide('y'), Fall('f'), say("Alt pressed.", "Alt gedrückt.")),
            trap(Touch('c'), Hide('z'), Show('A'), say("Del pressed. Reboot imminent.", "Entf gedrückt. Neustart steht bevor.")),
        ),
    ) {
        border(); floor()
        fill(9..10, 13..14, 'a'); fill(13..14, 11..14, 'x')
        fill(16..17, 13..14, 'b'); fill(19..20, 11..14, 'y'); fill(15..16, 15..17, 'f')
        fill(22..23, 13..14, 'c'); fill(25..26, 11..14, 'z')
        put(28, 14, 'A')
        spawn(); door(30)
    },

    // 123 — EASTER EGG: "Have you tried turning it off and on again?"
    Level(
        name = T("Off and On Again", "Aus- und wieder Einschalten"),
        intro = T("Have you tried turning it off and on again?", "Schon mal aus- und wieder eingeschaltet?"),
        traps = blink('w', 4.0f, 2.0f, 99f, 1, first = listOf(Play(Card.GHOST_BLOCK), say("Power off. Everything is temporarily gone.", "Strom aus. Alles ist vorübergehend weg."))) +
            blink('S', 4.0f, 2.0f, 99f, 1),
    ) {
        border(); floor()
        leds(9..13, c = 'S')
        fill(16..17, 5..14, 'w')
        spawn(6); door(30); bits(123)
    },

    // 124 — EASTER EGG: git push --force (the floor is overwritten)
    Level(
        name = T("git push --force", "git push --force"),
        intro = T("Your local floor differs from the remote. Force wins.", "Dein lokaler Boden weicht vom Remote ab. Force gewinnt."),
        legend = mapOf('n' to hiddenSolid, 'A' to hidden),
        traps = listOf(
            trap(PastX(14f), Play(Card.COLLAPSE), Hide('o'), Show('n'), Show('A'), say("+++ forced update +++ (12 commits lost)", "+++ Force-Update +++ (12 Commits verloren)")),
            trap(PastX(14f), Saw(-1.5f, 16.4f, 6f, 0f), delay = 0.7f),
        ),
    ) {
        border()
        fill(0..11, 15..17); fill(21..31, 15..17)
        fill(12..20, 15..16, 'o'); fill(12..20, 17..17, 'n')
        put(16, 16, 'A')
        spawn(); door(); bits(124)
    },

    // 125 — EASTER EGG: kill -9 (SIGKILL cannot be caught, but it can be climbed)
    Level(
        name = T("kill -9", "kill -9"),
        intro = T("SIGKILL. It can't be caught, ignored or jumped over.", "SIGKILL. Kann nicht abgefangen, ignoriert oder übersprungen werden."),
        traps = listOf(
            trap(PastX(4f), Play(Card.DEVIL_SAW), Saw(-4f, 14f, 9f, 0f, 2.6f), say("kill -9 1337", "kill -9 1337")),
            trap(PastX(4f), Saw(38f, 14f, -9f, 0f, 2.6f), delay = 3.1f),
        ),
    ) {
        border(); floor()
        fill(8..9, 13..14); fill(11..17, 11..11)
        fill(22..26, 11..11); fill(19..20, 13..14)
        spawn(); door(30)
    },

    // 126 — EASTER EGG: cd .. (go up one directory)
    Level(
        name = T("cd ..", "cd .."),
        intro = T("ls -la: five directories, each one level up.", "ls -la: fünf Verzeichnisse, jedes eine Ebene höher."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("rm -rf ./ (you were standing in it)", "rm -rf ./ (du hast darin gestanden)"), delay = 0.5f),
            trap(Touch('b'), Fall('b'), delay = 0.5f),
            trap(Touch('c'), Fall('c'), Show('A'), delay = 0.5f),
            trap(Touch('d'), Fall('d'), delay = 0.5f),
        ),
    ) {
        border(); floor()
        fill(5..7, 13..13, 'a'); fill(10..12, 11..11, 'b'); fill(15..17, 9..9, 'c'); fill(20..22, 7..7, 'd'); fill(25..28, 5..5)
        put(26, 4, 'A')
        spawn(); put(28, 4, 'D')
    },

    // 127 — EASTER EGG: 127.0.0.1 (there is no place like home, and the door was right there)
    Level(
        name = T("127.0.0.1", "127.0.0.1"),
        intro = T("There's no place like 127.0.0.1. Look, the door is right there.", "Es gibt keinen Ort wie 127.0.0.1. Schau, die Tür ist gleich da."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(10f), Fall('a')),
            trap(PastX(17f), Show('A'), say("Connection refused: localhost.", "Verbindung abgelehnt: localhost.")),
            trap(PastX(23f), Fall('c')),
            trap(PastX(3.2f), Saw(-1.5f, 14.4f, 6f, 0f), delay = 1.4f),
        ) + doorTrail(
            PastX(3.2f), 5, 14,
            listOf(DoorTo(5, 1, 26f, hanging = true), DoorTo(30, 1, 26f, hanging = true), DoorTo(30, 14, 26f)),
            first = listOf(Play(Card.SHY_DOOR), say("ping 127.0.0.1: destination unreachable.", "ping 127.0.0.1: Ziel nicht erreichbar.")),
        ),
    ) {
        border(); floor()
        fill(13..15, 15..17, 'a'); put(20, 14, 'A'); put(21, 14, 'A'); fill(26..27, 3..4, 'c')
        spawn(); door(5); bits(127)
    },

    // 128 — EASTER EGG: integer overflow 127 + 1 (the LEDs wrap to 0000000) and kernel panic
    Level(
        name = T("Integer Overflow: 127+1", "Integer-Überlauf: 127+1"),
        intro = T("KERNEL PANIC — not syncing: 127 + 1 = -128", "KERNEL PANIC — not syncing: 127 + 1 = -128"),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(3f), Play(Card.GRAND_FINALE), Saw(-1.5f, 14.4f, 6.2f, 0f), say("Kernel panic! Attempted to kill init!", "Kernel Panic! Versuch, init zu beenden!")),
            trap(PastX(7.5f), Swap(true), say("127 + 1 = -128", "127 + 1 = -128")),
            trap(PastX(11f), Fall('a'), say("BUG: unable to handle kernel paging request", "BUG: Kernel-Paging-Anfrage nicht behandelbar")),
            trap(PastX(16f), Gravity(true), say("Call Trace: mephi_devil_flip+0x2a/0x80", "Call Trace: mephi_devil_flip+0x2a/0x80")),
            trap(PastX(22.5f), Swap(false)),
            trap(PastX(25.5f), Gravity(false), Show('A'), say("---[ end Kernel panic ]---", "---[ Ende Kernel Panic ]---")),
        ),
    ) {
        border(); floor()
        fill(12..14, 15..17, 'a')
        leds(17..24); put(19, 1, 'v'); put(21, 1, 'v')
        put(28, 14, 'A')
        spawn(); door(30); bits(128)
    },
)
