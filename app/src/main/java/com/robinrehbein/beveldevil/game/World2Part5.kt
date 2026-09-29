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

/** World 2, levels 65-80: "Runtime Errors". */
internal val world2Part5: List<Level> = listOf(
    // 65 — EASTER EGG: HTTP 500 Internal Devil Error
    Level(
        name = T("500 Internal Devil Error", "500 Interner Teufelsfehler"),
        intro = T("Something went wrong. On my side. On purpose.", "Etwas ist schiefgelaufen. Bei mir. Absichtlich."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(9.5f), Play(Card.COLLAPSE), Fall('a'), Show('A'), Saw(-1.5f, 14.4f, 6f, 0f), say("500: everything is fine. (Not.)", "500: Alles in Ordnung. (Nicht.)")),
            trap(PastX(21.4f), Fall('c'), say("Please contact the devil administrator.", "Bitte wende dich an den Teufels-Administrator.")),
        ),
    ) {
        border(); floor()
        rack(6, 1, 2)
        fill(13..15, 15..17, 'a')
        put(19, 14, 'A'); put(20, 14, 'A')
        fill(24..25, 3..4, 'c')
        spawn(); door(); bits(65)
    },

    // 66 — EASTER EGG: SQL injection  '; DROP TABLE floor;--
    Level(
        name = T("Bobby Tables", "Klein Bobby Tables"),
        intro = T("Name: Robert'); DROP TABLE floor;--", "Name: Robert'); DROP TABLE floor;--"),
        traps = listOf(
            trap(PastX(4f), Play(Card.UPSIDE_DOWN), Gravity(true), DoorTo(29, 1, speed = 20f, hanging = true), say("DROP TABLE floor; -- Did you sanitize your inputs?", "DROP TABLE floor; -- Hast du deine Eingaben bereinigt?")),
        ) + ('a'..'h').mapIndexed { i, g -> trap(PastX(4f), Fall(g), delay = 0.12f * i) },
    ) {
        border()
        for (i in 0..7) fill(1 + i * 4..minOf(4 + i * 4, 30), 15..17, 'h' - i)
        put(10, 1, 'v'); put(16, 1, 'v'); put(17, 1, 'v'); put(23, 1, 'v'); put(24, 1, 'v')
        spawn(); door()
    },

    // 67 — EASTER EGG: tabs vs spaces (two routes, both wrong)
    Level(
        name = T("Tabs vs Spaces", "Tabs gegen Leerzeichen"),
        intro = T("Two ways to indent. Both are wrong.", "Zwei Arten einzurücken. Beide falsch."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(10.5f), Play(Card.SPIKE_SEED), Show('A'), say("Tabs. Definitely tabs. (Or spaces.)", "Tabs. Ganz klar Tabs. (Oder Leerzeichen.)")),
            trap(Touch('x'), Fall('x'), delay = 0.3f),
            trap(Touch('y'), Fall('y'), delay = 0.3f),
        ),
    ) {
        border(); floor()
        put(12, 14, 'A'); put(16, 14, 'A'); put(20, 14, 'A')
        put(8, 14, '#'); fill(9..10, 13..13)
        fill(12..15, 11..11, 'x'); fill(18..21, 11..11, 'y')
        spawn(); door(); bits(67)
    },

    // 68 — EASTER EGG: spaghetti code (every platform goes somewhere else)
    Level(
        name = T("Spaghetti Code", "Spaghetti-Code"),
        intro = T("Nobody knows what moves what. Not even me.", "Keiner weiß, was was bewegt. Nicht mal ich."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, -4f, 3f), say("goto 10; goto 20; goto 10;", "goto 10; goto 20; goto 10;"), delay = 0.2f),
            trap(Touch('b'), Move('b', 3f, 0f, 3f), Show('A'), delay = 0.3f),
            trap(Touch('c'), Move('c', 0f, 4f, 3f), delay = 0.3f),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(26..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(13..15, 11..11, 'b'); fill(21..23, 11..11, 'c')
        put(19, 10, 'A')
        spawn(); door(); bits(68)
    },

    // 69 — EASTER EGG: greedy regex  ^.*$  (the wall eats everything)
    Level(
        name = T("Greedy Quantifier", "Gieriger Quantor"),
        intro = T("^.*$ matches everything. Including you.", "^.*$ passt auf alles. Auch auf dich."),
        traps = listOf(
            trap(BeforeX(25f), Play(Card.SINKING), Move('W', -28f, 0f, 6.5f), say(".* is greedy. It will consume the whole line.", ".* ist gierig. Es frisst die ganze Zeile.")),
        ),
    ) {
        border(); floor()
        fill(30..30, 2..14, 'W')
        rack(21, 1, 2); rack(15, 1, 2); pit(9..10)
        spawn(28); door(2)
    },

    // 70 — EASTER EGG: printf debugging (the log line comes before the spike)
    Level(
        name = T("printf(\"here\")", "printf(\"hier\")"),
        intro = T("Debugging with print statements. Yours are late.", "Debugging mit Ausgaben. Deine kommen zu spät."),
        legend = mapOf('A' to hidden, 'B' to hidden),
        traps = listOf(
            trap(PastX(5f), say("printf(\"here 1\");", "printf(\"hier 1\");")),
            trap(PastX(5f), Play(Card.SPIKE_SEED), Show('A'), delay = 0.7f),
            trap(PastX(12f), say("printf(\"here 2\");", "printf(\"hier 2\");")),
            trap(PastX(12f), Show('B'), delay = 0.7f),
            trap(PastX(19f), say("printf(\"here 3\");", "printf(\"hier 3\");")),
            trap(PastX(19f), Fall('c'), delay = 0.6f),
        ),
    ) {
        border(); floor()
        put(11, 14, 'A'); put(12, 14, 'A')
        put(18, 14, 'B'); put(19, 14, 'B'); put(20, 14, 'B')
        fill(24..25, 3..4, 'c')
        spawn(); door(); bits(70)
    },

    // 71 — EASTER EGG: technical debt (the interest compounds)
    Level(
        name = T("Technical Debt", "Technische Schulden"),
        intro = T("We'll fix it later. With interest.", "Das fixen wir später. Mit Zinsen."),
        traps = listOf(
            trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, 12f, 4f), say("Interest rate: 100% per platform.", "Zinssatz: 100 % pro Plattform."), delay = 0.5f),
            trap(Touch('b'), Move('b', 0f, 12f, 7f), delay = 0.3f),
            trap(Touch('c'), Move('c', 0f, 12f, 12f), delay = 0.15f),
            trap(Touch('d'), Move('d', 0f, 12f, 20f), delay = 0.05f),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(27..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(12..14, 15..15, 'b'); fill(17..19, 15..15, 'c'); fill(22..24, 15..15, 'd')
        spawn(); door(); bits(71)
    },

    // 72 — EASTER EGG: chaos monkey
    Level(
        name = T("Chaos Monkey", "Chaos-Affe"),
        intro = T("The monkey randomly kills things in production.", "Der Affe killt zufällig Dinge in Produktion."),
        legend = mapOf('A' to hidden, 'B' to hidden),
        traps = listOf(
            trap(PastX(5f), Play(Card.COLLAPSE), Show('A'), say("Monkey killed a spike... into existence.", "Der Affe hat einen Spike erschaffen.")),
            trap(PastX(9f), Fall('a')),
            trap(PastX(15.5f), Saw(33.5f, 14.4f, -5f, 0f), say("Monkey killed the network.", "Der Affe hat das Netzwerk gekillt.")),
            trap(PastX(17.5f), Swap(true)),
            trap(PastX(21.5f), Fall('c')),
            trap(PastX(24f), Swap(false), Show('B'), say("Monkey killed the monkey.", "Der Affe hat den Affen gekillt.")),
        ),
    ) {
        border(); floor()
        put(8, 14, 'A'); fill(13..14, 15..17, 'a'); fill(24..25, 3..4, 'c'); put(27, 14, 'B')
        spawn(); door(30); bits(72)
    },

    // 73 — EASTER EGG: canary release (the canary goes first)
    Level(
        name = T("Canary Release", "Canary-Release"),
        intro = T("Deploying to 1% of users first. That's you.", "Zuerst für 1 % der Nutzer. Das bist du."),
        legend = mapOf('A' to hidden, 'B' to hidden),
        traps = listOf(
            trap(PastX(5f), Play(Card.SPIKE_SEED), Show('A'), say("The canary is dead. Roll back!", "Der Kanarienvogel ist tot. Rollback!")),
            trap(PastX(13f), Fall('a'), Saw(-1.5f, 14.4f, 6f, 0f), Show('B'), say("Rolling out to 100%.", "Ausrollen auf 100 %.")),
            trap(PastX(23f), Fall('c')),
        ),
    ) {
        border(); floor()
        put(9, 14, 'A'); fill(16..18, 15..17, 'a'); put(21, 14, 'B'); put(22, 14, 'B'); fill(26..27, 3..4, 'c')
        spawn(); door(); bits(73)
    },

    // 74 — EASTER EGG: blue/green deployment
    Level(
        name = T("Blue/Green Deployment", "Blue/Green-Deployment"),
        intro = T("Two environments. Traffic switches every 1.8 seconds.", "Zwei Umgebungen. Der Verkehr wechselt alle 1,8 Sekunden."),
        legend = mapOf('G' to hiddenSolid),
        traps = blink('B', 1.8f, 1.8f, 3.6f, 3, first = listOf(Play(Card.GHOST_BLOCK), say("Switching traffic to green.", "Schalte Verkehr auf Grün."))) +
            blinkOn('G', 1.8f, 1.8f, 3.6f, 3),
    ) {
        border()
        fill(0..5, 15..17); fill(27..31, 15..17)
        fill(7..9, 15..15, 'B'); fill(13..15, 15..15, 'B'); fill(19..21, 15..15, 'B')
        fill(10..12, 15..15, 'G'); fill(16..18, 15..15, 'G'); fill(22..24, 15..15, 'G')
        spawn(); door(); bits(74)
    },

    // 75 — EASTER EGG: CrashLoopBackOff (1, 2, 4... seconds of downtime)
    Level(
        name = T("CrashLoopBackOff", "CrashLoopBackOff"),
        intro = T("Pod restarting. Back-off: 1s, 2s, 4s, 8s...", "Pod startet neu. Back-off: 1 s, 2 s, 4 s, 8 s..."),
        traps = listOf(
            trap(After(0.6f), Play(Card.GHOST_BLOCK), Hide('f'), say("Back-off restarting failed container.", "Back-off: Neustart des fehlgeschlagenen Containers.")),
            trap(After(1.6f), Show('f')),
            trap(After(3.0f), Hide('f')),
            trap(After(5.0f), Show('f')),
            trap(After(7.0f), Hide('f')),
            trap(After(11.0f), Show('f')),
        ),
    ) {
        border()
        fill(0..6, 15..17); fill(26..31, 15..17)
        fill(7..25, 15..17, 'f')
        rack(12, 2, 1); rack(19, 2, 1)
        spawn(); door(); bits(75)
    },

    // 76 — EASTER EGG: proof of work (the bridge is mined block by block)
    Level(
        name = T("Proof of Work", "Proof of Work"),
        intro = T("Mining the bridge. Block 1 of 4. Please hold.", "Die Brücke wird geschürft. Block 1 von 4. Bitte warten."),
        legend = mapOf('a' to hiddenSolid, 'b' to hiddenSolid, 'c' to hiddenSolid, 'd' to hiddenSolid),
        traps = listOf(
            trap(After(2.2f), Play(Card.GHOST_BLOCK), Show('a'), say("Block mined. Difficulty: rising.", "Block geschürft. Schwierigkeit: steigend.")),
            trap(After(3.6f), Show('b')),
            trap(After(5.0f), Show('c')),
            trap(After(6.4f), Show('d')),
            trap(After(2.0f), Saw(-1.5f, 14.4f, 4f, 0f), say("51% attack incoming.", "51-%-Angriff im Anflug.")),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(26..31, 15..17)
        fill(6..10, 15..15, 'a'); fill(11..15, 15..15, 'b'); fill(16..20, 15..15, 'c'); fill(21..25, 15..15, 'd')
        spawn(); door(); bits(76)
    },

    // 77 — EASTER EGG: thread pool (three lifts, worker threads on a schedule)
    Level(
        name = T("Thread Pool", "Thread-Pool"),
        intro = T("newFixedThreadPool(3). Each worker has a schedule.", "newFixedThreadPool(3). Jeder Worker hat einen Zeitplan."),
        traps = shuttle('a', 1.0f, 3.0f, 5f, 2.5f, 8) + shuttle('b', 2.0f, 3.0f, 5f, 2.5f, 8) + shuttle('c', 3.0f, 3.0f, 5f, 2.5f, 8) +
            trap(Touch('a'), Play(Card.SINKING), say("Task submitted to worker 1.", "Aufgabe an Worker 1 übergeben.")),
    ) {
        border()
        fill(0..5, 15..17); fill(23..31, 15..17)
        fill(7..9, 10..10, 'a'); fill(12..14, 10..10, 'b'); fill(17..19, 10..10, 'c')
        put(28, 14, '^')
        spawn(); door(); bits(77)
    },

    // 78 — EASTER EGG: Lisp parentheses ((( ))) and the missing one
    Level(
        name = T("(((Lisp)))", "(((Lisp)))"),
        intro = T("Count your parentheses. Somebody has to.", "Zähl deine Klammern. Irgendjemand muss es tun."),
        legend = mapOf('A' to Glyph(spike = true, hidden = true, dir = Dir.RIGHT), 'B' to Glyph(spike = true, hidden = true, dir = Dir.RIGHT)),
        traps = listOf(
            trap(PastX(15.5f), Play(Card.SPIKE_SEED), Show('A'), say("Unbalanced parentheses.", "Unausgeglichene Klammern.")),
            trap(PastX(21.5f), Show('B')),
        ),
    ) {
        border(); floor()
        for (x in listOf(7, 10, 13)) { put(x, 13, '<'); put(x, 14, '<') }
        for (x in listOf(17, 20)) { put(x, 13, '>'); put(x, 14, '>') }
        put(24, 13, 'A'); put(24, 14, 'A'); put(27, 13, 'B'); put(27, 14, 'B')
        spawn(); door(); bits(78)
    },

    // 79 — EASTER EGG: docker run --rm (the container removes itself, and leaves spikes)
    Level(
        name = T("docker run --rm", "docker run --rm"),
        intro = T("Containers are ephemeral. So is your life.", "Container sind flüchtig. Dein Leben auch."),
        legend = mapOf('A' to hidden, 'B' to hidden, 'C' to hidden),
        traps = listOf(
            trap(PastX(7.4f), Play(Card.HEADBUTT), Fall('c'), say("Container started.", "Container gestartet.")),
            trap(PastX(7.4f), Hide('c'), Show('A'), say("Container removed (--rm).", "Container entfernt (--rm)."), delay = 1.6f),
            trap(PastX(14.4f), Fall('d')),
            trap(PastX(14.4f), Hide('d'), Show('B'), delay = 1.6f),
            trap(PastX(21.4f), Fall('e')),
            trap(PastX(21.4f), Hide('e'), Show('C'), delay = 1.6f),
        ),
    ) {
        border(); floor()
        fill(9..10, 3..4, 'c'); fill(16..17, 3..4, 'd'); fill(23..24, 3..4, 'e')
        fill(9..10, 14..14, 'A'); fill(16..17, 14..14, 'B'); fill(23..24, 14..14, 'C')
        spawn(); door(); bits(79, x0 = 24, y = 1)
    },

    // 80 — EASTER EGG: uncaught exception (act finale)
    Level(
        name = T("Uncaught Exception", "Nicht abgefangene Ausnahme"),
        intro = T("Exception in thread \"main\": Everything.", "Exception in thread \"main\": Alles."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(3.5f), Play(Card.GRAND_FINALE), Saw(-1.5f, 14.4f, 6.2f, 0f), say("at Level.run(Level.java:80)", "at Level.run(Level.java:80)")),
            trap(PastX(8f), Swap(true), say("at Controls.swap(Controls.java:13)", "at Controls.swap(Controls.java:13)")),
            trap(PastX(11.5f), Fall('a')),
            trap(PastX(18f), Gravity(true), say("at Gravity.flip(Gravity.java:42)", "at Gravity.flip(Gravity.java:42)")),
            trap(PastX(24f), Swap(false)),
            trap(PastX(26.5f), Gravity(false), Show('A'), say("Caused by: you.", "Verursacht durch: dich.")),
        ),
    ) {
        border(); floor()
        fill(13..15, 15..17, 'a')
        leds(19..25); put(21, 1, 'v'); put(23, 1, 'v')
        put(28, 14, 'A')
        spawn(); door(30)
    },
)
