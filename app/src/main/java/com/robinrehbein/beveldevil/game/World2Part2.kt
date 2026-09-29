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
private val ghost = Glyph(spike = false, hidden = true, bonk = true)
private val hiddenSolid = Glyph(spike = false, hidden = true)

/** Shared map of levels 27 and 28 (Recursion: see Recursion). */
private fun MapBuilder.bigO() {
    border(); floor()
    put(6, 14, '^')
    leds(10..11); leds(13..14)
    leds(17..19); leds(21..23); leds(25..27)
}

/** World 2, levels 17-32: "Kernel Space". */
internal val world2Part2: List<Level> = listOf(
    // 17 — EASTER EGG: Segmentation fault (core dumped)
    Level(
        name = T("Segmentation Fault", "Speicherzugriffsfehler"),
        intro = T("Memory is divided into segments. Some are not yours.", "Der Speicher ist in Segmente geteilt. Manche gehören nicht dir."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Segmentation fault (core dumped)", "Speicherzugriffsfehler (Speicherabbild geschrieben)"), delay = 0.28f),
            trap(PastX(21f), Show('A')),
        ) + ('b'..'f').map { g -> trap(Touch(g), Fall(g), delay = 0.28f) },
    ) {
        border()
        fill(0..4, 15..17); fill(23..30, 15..17)
        for (i in 0..5) {
            val x = 5 + i * 3
            fill(x..x + 2, (if (i % 2 == 0) 15 else 14)..(if (i % 2 == 0) 15 else 14), 'a' + i)
        }
        put(25, 14, 'A'); put(26, 14, 'A')
        spawn(); door(); bits(17)
    },

    // 18 — EASTER EGG: Stack Overflow (the stack of blocks grows until nothing fits)
    Level(
        name = T("Stack Overflow", "Stapelüberlauf"),
        intro = T("Please wait while the stack grows.", "Bitte warten, der Stapel wächst."),
        traps = listOf(
            trap(PastX(9f), Play(Card.HEADBUTT), Fall('c'), say("Stack Overflow: the answer was already marked as duplicate.", "Stack Overflow: Die Antwort wurde als Duplikat markiert.")),
            trap(PastX(9f), Fall('d'), delay = 0.3f),
            trap(PastX(9f), Fall('e'), delay = 0.6f),
            trap(PastX(9f), Fall('f'), delay = 0.9f),
            trap(PastX(9f), Fall('g'), delay = 1.2f),
        ),
    ) {
        border(); floor()
        put(14, 7, 'c'); put(14, 6, 'd'); put(14, 5, 'e'); put(14, 4, 'f'); put(14, 3, 'g')
        rack(22, 2, 1); rack(26, 1, 2)
        spawn(); door(); bits(18)
    },

    // 19 — EASTER EGG: Buffer Overflow (the spikes write past the end of the array)
    Level(
        name = T("Buffer Overflow", "Pufferüberlauf"),
        intro = T("char buf[8]; strcpy(buf, \"AAAAAAAAAAAAAAAA\");", "char buf[8]; strcpy(buf, \"AAAAAAAAAAAAAAAA\");"),
        legend = mapOf('A' to hidden, 'B' to hidden, 'C' to hidden),
        traps = listOf(
            trap(PastX(9f), Play(Card.SPIKE_SEED), Show('A'), say("Writing 16 bytes into 8. What could go wrong?", "16 Bytes in 8 schreiben. Was soll schon passieren?")),
            trap(PastX(14.5f), Show('B')),
            trap(PastX(19f), Show('C'), say("AAAAAAAA... 0x41414141", "AAAAAAAA... 0x41414141")),
        ),
    ) {
        border(); floor()
        rack(10, 1, 2); rack(18, 1, 2)
        put(13, 14, 'A'); put(17, 14, 'B'); put(21, 14, 'C'); put(22, 14, 'C')
        spawn(); door(); bits(19)
    },

    // 20 — EASTER EGG: Deadlock (two threads, each waiting for the other)
    Level(
        name = T("Deadlock", "Deadlock"),
        intro = T("Thread A waits for B. Thread B waits for A. You wait for nobody.", "Thread A wartet auf B. Thread B wartet auf A. Du wartest auf niemanden."),
        traps = listOf(
            trap(PastX(8f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6f, 0f), say("Thread A acquired lock 1.", "Thread A hat Lock 1.")),
            trap(PastX(8f), Saw(33.5f, 14.4f, -6f, 0f), say("Thread B acquired lock 2.", "Thread B hat Lock 2."), delay = 0.4f),
            trap(Touch('a'), Fall('a'), delay = 0.2f),
        ),
    ) {
        border(); floor()
        rack(13, 2, 2); fill(18..19, 13..14, 'a'); rack(24, 2, 2)
        spawn(); door(); bits(20)
    },

    // 21 — EASTER EGG: Race condition (whoever gets to the top first)
    Level(
        name = T("Race Condition", "Wettlaufsituation"),
        intro = T("Two threads race to the door. Guess who's not winning.", "Zwei Threads rennen zur Tür. Rate, wer nicht gewinnt."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('m'), Play(Card.SINKING), Move('m', 0f, -8f, 4f), say("Race condition: the lift and the spikes both won.", "Race Condition: Aufzug und Spikes haben beide gewonnen."), delay = 0.2f),
            trap(Zone(11f, 4f, 15f, 9f), Show('A')),
            trap(Touch('m'), Fall('f'), delay = 0.3f),
        ),
    ) {
        border(); floor()
        fill(12..14, 14..14, 'm')
        fill(3..8, 15..15, 'f')
        fill(16..30, 6..7)
        put(21, 5, 'A')
        spawn(); put(28, 5, 'D')
    },

    // 22 — EASTER EGG: rubber duck debugging (explain your code to the duck)
    Level(
        name = T("Rubber Duck", "Quietscheente"),
        intro = T("Explain the level to the duck. Out loud.", "Erklär der Ente das Level. Laut."),
        legend = mapOf('b' to ghost, 'd' to hiddenSolid),
        traps = listOf(
            trap(Touch('b'), Play(Card.GHOST_BLOCK), Show('d'), say("Quack. Have you tried explaining it to the duck?", "Quak. Schon der Ente erklärt?")),
        ),
    ) {
        border(); floor()
        put(11, 12, 'b')
        put(14, 14, 'd'); fill(15..15, 13..14, 'd'); fill(16..24, 12..14, 'd')
        fill(21..22, 10..11, 'd'); fill(23..26, 8..9, 'd'); fill(27..28, 9..9, 'd')
        spawn(); put(25, 7, 'D')
    },

    // 23 — EASTER EGG: Konami code (up up down down left right left right B A)
    Level(
        name = T("Konami Code", "Konami-Code"),
        intro = T("↑ ↑ ↓ ↓ ← → ← → B A", "↑ ↑ ↓ ↓ ← → ← → B A"),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("+30 lives. Kidding. 0 lives.", "+30 Leben. Scherz. 0 Leben."), delay = 0.45f),
            trap(PastX(22f), Show('A')),
        ) + ('b'..'i').map { g -> trap(Touch(g), Fall(g), delay = 0.45f) },
    ) {
        border(); floor()
        fill(27..30, 13..14)
        for (i in 0..8) fill(1 + 3 * i..minOf(3 + 3 * i, 25), 11..11, 'a' + i)
        fill(1..3, 9..10)
        fill(6..30, 8..8)
        put(26, 7, 'A'); put(27, 7, 'A')
        spawn(); put(29, 7, 'D')
    },

    // 24 — EASTER EGG: merge conflict markers <<<<<<< ======= >>>>>>>
    Level(
        name = T("Merge Conflict", "Merge-Konflikt"),
        intro = T("<<<<<<< HEAD  ...  =======  ...  >>>>>>> feature", "<<<<<<< HEAD  ...  =======  ...  >>>>>>> feature"),
        traps = listOf(
            trap(PastX(16f), Play(Card.SINKING), Move('w', -7f, 0f, 6f), say("Automatic merge failed. Fix conflicts and try again.", "Automatischer Merge fehlgeschlagen. Konflikte lösen und nochmal versuchen.")),
            trap(PastX(24f), Fall('c'), say("Accept both changes? Bold.", "Beide Änderungen übernehmen? Mutig.")),
        ),
    ) {
        border(); floor()
        put(9, 14, '<'); put(9, 13, '<')
        rack(12, 2, 2)
        put(16, 14, '>'); put(16, 13, '>')
        fill(24..26, 13..14, 'w')
        fill(21..22, 4..5, 'c')
        spawn(); door(); bits(24)
    },

    // 25 — EASTER EGG: Y2K (at 19.99 turns 20.00, everything flips)
    Level(
        name = T("Y2K", "Jahr-2000-Problem"),
        intro = T("It is 19.99. Nothing bad will happen at 20.00.", "Es ist 19.99. Bei 20.00 passiert nichts Schlimmes."),
        traps = listOf(
            trap(PastX(19.99f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Happy New Year! 1900!", "Frohes neues Jahr! 1900!")),
            trap(PastX(20.0f), Swap(true), say("Two-digit years were a mistake.", "Zweistellige Jahreszahlen waren ein Fehler.")),
            trap(PastX(27f), Swap(false), Gravity(false)),
        ),
    ) {
        border(); floor()
        leds(21..25)
        put(23, 1, 'v'); put(25, 1, 'v')
        spawn(); door()
    },

    // 26 — EASTER EGG: Unix epoch (1970 -> After(1.97 s)) and the year-2038 overflow
    Level(
        name = T("Unix Epoch", "Unix-Epoche"),
        intro = T("00:00:00 UTC, January 1st, 1970. Time starts now.", "00:00:00 UTC, 1. Januar 1970. Die Zeit beginnt jetzt."),
        traps = listOf(
            trap(After(1.97f), Play(Card.COLLAPSE), Fall('a'), say("Epoch reached. Everything before it is deleted.", "Epoche erreicht. Alles davor wird gelöscht.")),
            trap(After(2.6f), Fall('b')),
            trap(After(3.1f), Fall('c')),
        ),
    ) {
        border()
        fill(0..5, 15..17, 'a'); fill(6..15, 15..17, 'b'); fill(16..21, 15..17, 'c'); fill(22..30, 15..17)
        pit(12..13); pit(19..20)
        rack(9, 1, 2)
        spawn(); door(); bits(26)
    },

    // 27 — EASTER EGG: Big O(n²): 1, 4, 9 spikes
    Level(
        name = T("Big O(n²)", "Groß-O von n²"),
        intro = T("Section n has n² spikes. Nested loops are fun.", "Abschnitt n hat n² Spikes. Verschachtelte Schleifen machen Spaß."),
    ) {
        bigO()
        spawn(2); door(30)
    },

    // 28 — EASTER EGG: recursion (looks like the previous level; to understand it, see level 28)
    Level(
        name = T("Recursion", "Rekursion"),
        intro = T("See: Recursion. (Wait, this looks familiar.)", "Siehe: Rekursion. (Moment, das kenne ich doch.)"),
        traps = listOf(
            trap(BeforeX(9f), Play(Card.HEADBUTT), Fall('c'), say("To understand recursion, you must first understand recursion.", "Um Rekursion zu verstehen, musst du zuerst Rekursion verstehen.")),
        ),
    ) {
        bigO()
        fill(3..4, 4..5, 'c')
        spawn(30); door(2)
    },

    // 29 — EASTER EGG: Turing test (prove you are human, one step at a time)
    Level(
        name = T("Turing Test", "Turing-Test"),
        intro = T("Are you a human? Then walk like one.", "Bist du ein Mensch? Dann lauf wie einer."),
        legend = mapOf('B' to hiddenSolid, 'C' to hiddenSolid, 'E' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.GHOST_BLOCK), Show('B'), say("Question 1: Are you human?", "Frage 1: Bist du ein Mensch?")),
            trap(Touch('B'), Show('C'), say("Question 2: Select all traffic lights.", "Frage 2: Wähle alle Ampeln aus.")),
            trap(Touch('C'), Show('E'), say("Wrong. Robots do not hesitate.", "Falsch. Roboter zögern nicht.")),
        ),
    ) {
        border()
        fill(0..8, 15..17); fill(24..31, 15..17)
        fill(10..11, 15..15, 'a'); fill(15..16, 15..15, 'B'); fill(20..21, 15..15, 'C')
        put(25, 14, 'E'); put(26, 14, 'E')
        spawn(); door(); bits(29)
    },

    // 30 — EASTER EGG: fork bomb  :(){ :|:& };:
    Level(
        name = T("Fork Bomb", "Fork-Bombe"),
        intro = T(":(){ :|:& };:", ":(){ :|:& };:"),
        traps = listOf(
            trap(PastX(5f), Play(Card.DEVIL_SAW), Saw(33f, 14.4f, -6f, 0f), say("One process. Then two. Then four...", "Ein Prozess. Dann zwei. Dann vier...")),
            trap(PastX(5f), Saw(17f, -0.5f, 0f, 9f), delay = 0.7f),
            trap(PastX(5f), Saw(33f, 14.4f, -6f, 0f), delay = 1.4f),
            trap(PastX(5f), Saw(23f, -0.5f, 0f, 9f), delay = 1.4f),
            trap(PastX(5f), Saw(33f, 14.4f, -6f, 0f), delay = 2.1f),
            trap(PastX(5f), Saw(28f, -0.5f, 0f, 9f), delay = 2.1f),
        ),
    ) {
        border(); floor()
        fill(14..19, 1..2); fill(21..25, 1..2); fill(26..29, 1..2)
        spawn(); door(); bits(30, x0 = 4)
    },

    // 31 — EASTER EGG: sudo !! (repeat the last command, but as root)
    Level(
        name = T("sudo !!", "sudo !!"),
        intro = T("Permission denied. Try: sudo !!", "Zugriff verweigert. Versuch: sudo !!"),
        legend = mapOf('A' to hidden, 'B' to hidden),
        traps = listOf(
            trap(PastX(9f), Play(Card.SPIKE_SEED), Show('A'), say("Command 1: one spike.", "Befehl 1: ein Spike.")),
            trap(PastX(17.5f), Show('B'), say("sudo !!  (three spikes, this time as root)", "sudo !!  (drei Spikes, diesmal als root)")),
            trap(PastX(24f), Fall('a'), say("sudo !!  (and the floor)", "sudo !!  (und der Boden)")),
        ),
    ) {
        border(); floor()
        put(12, 14, 'A')
        put(20, 14, 'B'); put(21, 14, 'B'); put(22, 14, 'B')
        fill(26..27, 15..17, 'a')
        spawn(); door(); bits(31)
    },

    // 32 — EASTER EGG: chmod 777 (read, write, execute for everyone)
    Level(
        name = T("chmod 777", "chmod 777"),
        intro = T("Everyone may read, write and execute. Especially me.", "Alle dürfen lesen, schreiben und ausführen. Vor allem ich."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(7.77f), Play(Card.COLLAPSE), Show('A'), Fall('a'), Saw(-1.5f, 14.4f, 6f, 0f, 0.62f), say("chmod 777: r, w and x. All at once.", "chmod 777: r, w und x. Alles gleichzeitig.")),
        ),
    ) {
        border(); floor()
        put(13, 14, 'A'); put(14, 14, 'A'); put(15, 14, 'A')
        fill(20..22, 15..17, 'a')
        rack(17, 1, 2)
        spawn(); door(); bits(32)
    },
)
