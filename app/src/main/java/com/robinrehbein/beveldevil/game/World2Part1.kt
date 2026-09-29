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

private val hiddenSpike = Glyph(spike = true, hidden = true)

/** World 2, levels 1-16: "POST" (power-on self test). */
internal val world2Part1: List<Level> = listOf(
    // 1 — EASTER EGG: Hello, World!
    Level(
        name = T("Hello, World!", "Hallo, Welt!"),
        intro = T("Print it, then walk to the door. What could go wrong?", "Gib es aus und lauf zur Tür. Was soll schon schiefgehen?"),
        legend = mapOf('A' to hiddenSpike),
        traps = listOf(
            trap(PastX(3.5f), say("Hello, World!", "Hallo, Welt!")),
            trap(PastX(10.4f), Play(Card.COLLAPSE), Fall('a'), say("Segfault in the floor driver.", "Segfault im Boden-Treiber.")),
            trap(PastX(17.6f), Show('A'), say("Warning: 2 new spikes installed.", "Warnung: 2 neue Spikes installiert.")),
        ),
    ) {
        border(); floor()
        fill(12..14, 15..17, 'a')
        put(21, 14, 'A'); put(22, 14, 'A')
        spawn(); door(); bits(1)
    },

    // 2 — EASTER EGG: RAM memory test (POST counts up, never finishes)
    Level(
        name = T("Memory Test", "Speichertest"),
        intro = T("POST: 640K ought to be enough for anybody.", "POST: 640K sollten für jeden reichen."),
        traps = listOf(
            trap(Touch('b'), Play(Card.SINKING), Move('b', 0f, 12f, 7f), say("RAM check: 3 of 4 blocks OK.", "RAM-Check: 3 von 4 Blöcken OK."), delay = 0.15f),
            trap(Touch('a'), Fall('a'), delay = 0.3f),
            trap(Touch('c'), Fall('c'), delay = 0.2f),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(26..31, 15..17)
        fill(7..8, 15..15, 'a'); fill(11..13, 14..14, 'b'); fill(17..19, 15..15, 'c'); fill(23..24, 14..14)
        spawn(); door(); bits(2)
    },

    // 3 — EASTER EGG: HTTP 404, the door is gone (it runs back to the start)
    Level(
        name = T("404 Door Not Found", "404 Tür nicht gefunden"),
        intro = T("The door is right there. Probably.", "Die Tür ist gleich da. Wahrscheinlich."),
        legend = mapOf('A' to hiddenSpike),
        traps = listOf(
            trap(BeforeX(4.6f), Show('A'), say("Have you tried turning it off and on again?", "Schon mal aus- und wieder eingeschaltet?")),
        ) + doorTrail(
            PastX(22f), 29, 14,
            listOf(DoorTo(29, 1, 24f, hanging = true), DoorTo(1, 1, 24f, hanging = true), DoorTo(1, 14, 24f)),
            first = listOf(Play(Card.SHY_DOOR), say("404: Door not found. Try /dev/left.", "404: Tür nicht gefunden. Versuch /dev/left.")),
        ),
    ) {
        border(); floor()
        rack(15, 2, 2); rack(20, 2, 2)
        put(3, 14, 'A')
        spawn(10); door(); bits(3)
    },

    // 4 — EASTER EGG: off-by-one (the safe gap in the LED row is index 14, and it moves)
    Level(
        name = T("Off-by-one", "Um-eins-daneben"),
        intro = T("See the gap? Index 14. Trust me.", "Siehst du die Lücke? Index 14. Vertrau mir."),
        legend = mapOf('A' to hiddenSpike),
        traps = listOf(
            trap(PastX(11.6f), Play(Card.SPIKE_SEED), Show('A'), say("Off by one. Classic.", "Um eins daneben. Klassiker.")),
            trap(PastX(24.8f), DoorTo(28, 14, speed = 30f), say("You counted from zero, I counted from one.", "Du hast bei null angefangen, ich bei eins.")),
        ),
    ) {
        border(); floor()
        put(13, 14, '^'); put(15, 14, '^'); put(17, 14, '^'); put(14, 14, 'A')
        rack(21, 2, 2)
        spawn(); door(27); bits(4)
    },

    // 5 — EASTER EGG: NullPointerException
    Level(
        name = T("Null Pointer", "Nullzeiger"),
        intro = T("Nothing there. Literally.", "Da ist nichts. Wortwörtlich."),
        legend = mapOf('b' to Glyph(spike = false, hidden = true, bonk = true), 'A' to hiddenSpike),
        traps = listOf(
            trap(Touch('b'), Play(Card.GHOST_BLOCK), Show('A'), say("NullPointerException: block is null. It was there all along.", "NullPointerException: Block ist null. War aber die ganze Zeit da.")),
        ),
    ) {
        border(); floor()
        rack(17, 2, 3)
        put(15, 13, 'b')
        put(17, 11, 'A'); put(18, 11, 'A')
        spawn(); door(); bits(5)
    },

    // 6 — EASTER EGG: Ctrl+Z, the pit appears behind you and the door undoes its position
    Level(
        name = T("Ctrl+Z", "Strg+Z"),
        intro = T("Whatever happens: no going back.", "Egal was passiert: Zurück geht's nicht."),
        traps = listOf(
            trap(PastX(15f), Play(Card.COLLAPSE), Fall('a'), say("Undo floor.", "Boden rückgängig gemacht.")),
        ) + doorTrail(
            PastX(22f), 29, 14,
            listOf(DoorTo(29, 1, 24f, hanging = true), DoorTo(2, 1, 24f, hanging = true), DoorTo(2, 14, 24f)),
            first = listOf(say("Ctrl+Z: the door is back where it started.", "Strg+Z: Die Tür ist wieder am Anfang.")),
        ),
    ) {
        border(); floor()
        fill(6..8, 15..17, 'a')
        rack(18, 2, 2); rack(22, 2, 2)
        spawn(11); door(); bits(6)
    },

    // 7 — EASTER EGG: Blue Screen of Death (stop code 0x7B: inaccessible boot device)
    Level(
        name = T("Blue Screen", "Blauer Bildschirm"),
        intro = T("Nice ceiling. Very stable.", "Schöne Decke. Sehr stabil."),
        traps = listOf(
            trap(PastX(7.4f), Play(Card.HEADBUTT), Fall('c'), say("STOP: 0x0000007B. Your ceiling has crashed.", "STOP: 0x0000007B. Deine Decke ist abgestürzt.")),
            trap(PastX(14.3f), Fall('d')),
            trap(PastX(21.6f), Fall('e'), say("Collecting error info: 100%. Dying now.", "Fehlerinfo sammeln: 100 %. Sterbe jetzt.")),
        ),
    ) {
        border(); floor()
        fill(9..10, 4..5, 'c'); fill(16..17, 4..5, 'd'); fill(23..24, 4..5, 'e')
        spawn(); door(); bits(7)
    },

    // 8 — EASTER EGG: never deploy on a Friday (the deploy takes 4 seconds, then the hotfix bites)
    Level(
        name = T("Friday Deploy", "Freitags-Deploy"),
        intro = T("Deploying... please wait. Do not close the game.", "Deploy läuft... bitte warten. Spiel nicht schließen."),
        legend = mapOf('B' to hiddenSpike),
        traps = listOf(
            trap(After(3.5f), Hide('A'), say("Deploy finished. 0 errors. (Lie.)", "Deploy fertig. 0 Fehler. (Gelogen.)")),
            trap(PastX(23.2f), Play(Card.SPIKE_SEED), Show('B'), say("Friday 16:59: one tiny hotfix.", "Freitag, 16:59: nur ein winziger Hotfix.")),
        ),
    ) {
        border(); floor()
        rack(8, 2, 2); rack(13, 2, 2)
        leds(19..25, c = 'A')
        put(27, 14, 'B'); put(28, 14, 'B')
        spawn(); door(30); bits(8)
    },

    // 9 — EASTER EGG: hot swap (unplug the controls, plug them back in wrong)
    Level(
        name = T("Hot Swap", "Hot Swap"),
        intro = T("Please don't unplug the controller.", "Bitte den Controller nicht abziehen."),
        traps = listOf(
            trap(PastX(6.5f), Play(Card.TWISTED), Swap(true), say("Hot swap: left and right exchanged.", "Hot Swap: links und rechts getauscht.")),
            trap(PastX(23f), Swap(false), say("Kernel reloaded the driver.", "Kernel hat den Treiber neu geladen.")),
        ),
    ) {
        border(); floor()
        leds(8..23)
        fill(10..11, 14..14); fill(15..16, 14..14); fill(20..21, 14..14)
        spawn(); door(); bits(9)
    },

    // 10 — EASTER EGG: rm -rf /
    Level(
        name = T("rm -rf /", "rm -rf /"),
        intro = T("Your files are safe. Sort of.", "Deine Dateien sind sicher. Irgendwie."),
        traps = listOf(
            trap(PastX(6.4f), Play(Card.COLLAPSE), Fall('a'), say("rm: it is dangerous to operate recursively on '/'. Just kidding.", "rm: rekursives Löschen von '/' ist gefährlich. War ein Scherz.")),
        ) + ('b'..'i').mapIndexed { i, g -> trap(PastX(6.4f), Fall(g), delay = 0.11f * (i + 1)) },
    ) {
        border()
        fill(0..7, 15..17); fill(26..31, 15..17)
        ('a'..'i').forEachIndexed { i, g -> fill(8 + 2 * i..9 + 2 * i, 15..17, g) }
        fill(8..9, 13..13); fill(13..14, 12..12); fill(18..19, 12..12); fill(23..24, 13..13)
        spawn(); door(); bits(10)
    },

    // 11 — EASTER EGG: fan #3 failed (cooling is a saw problem)
    Level(
        name = T("Fan Failure", "Lüfterausfall"),
        intro = T("Cooling nominal. Fans: three.", "Kühlung normal. Lüfter: drei."),
        traps = listOf(
            trap(PastX(9.5f), Play(Card.DEVIL_SAW), Saw(15.5f, 2f, 0f, 9f), say("Fan 1 of 3 spinning. Rather fast.", "Lüfter 1 von 3 dreht. Ziemlich schnell.")),
            trap(PastX(15.2f), Saw(20.5f, 19f, 0f, -9f), say("Fan 2 spins from below. It's a feature.", "Lüfter 2 dreht von unten. Ist ein Feature.")),
            trap(PastX(21.6f), Saw(26.5f, 2f, 0f, 9f), say("Fan 3: REPLACE. (I meant it.)", "Lüfter 3: TAUSCHEN. (Ernst gemeint.)")),
        ),
    ) {
        border(); floor()
        fill(13..17, 1..2); fill(19..22, 1..2); fill(25..28, 1..2)
        spawn(); door(); bits(11, x0 = 4)
    },

    // 12 — EASTER EGG: Heisenbug (it only appears when you go and look)
    Level(
        name = T("Heisenbug", "Heisenbug"),
        intro = T("Works perfectly. Until you look.", "Läuft perfekt. Bis du hinschaust."),
        legend = mapOf('A' to hiddenSpike),
        traps = listOf(
            trap(After(3.6f), DoorTo(29, 14, speed = 22f), say("The door is ready. Or is it?", "Die Tür ist fertig. Oder doch nicht?")),
            trap(Zone(22.5f, 9f, 29f, 15f), Play(Card.SPIKE_SEED), Show('A'), say("Heisenbug: it appears when you observe it.", "Heisenbug: Er erscheint, sobald du hinsiehst."), delay = 0.35f),
        ),
    ) {
        border(); floor()
        fill(27..30, 4..5)
        put(25, 14, 'A'); put(26, 14, 'A')
        rack(11, 2, 2)
        spawn(); put(29, 2, 'D'); bits(12)
    },

    // 13 — EASTER EGG: HTTP 418 I'm a teapot (the level is a teapot)
    Level(
        name = T("418 I'm a Teapot", "418 Ich bin eine Teekanne"),
        intro = T("The door is inside. Climb the spout.", "Die Tür ist drin. Klettere die Tülle hoch."),
        legend = mapOf('A' to hiddenSpike),
        traps = doorTrail(
            PastX(20f), 24, 10,
            listOf(DoorTo(24, 1, 24f, hanging = true), DoorTo(11, 1, 24f, hanging = true), DoorTo(11, 14, 24f)),
            first = listOf(Play(Card.SHY_DOOR), Show('A'), say("418: I'm a teapot. The door refuses to brew coffee.", "418: Ich bin eine Teekanne. Die Tür braut keinen Kaffee.")),
        ),
    ) {
        border(); floor()
        fill(15..16, 14..14); fill(17..18, 13..14)
        fill(19..27, 12..14)
        fill(28..29, 12..13); fill(29..29, 11..11)
        fill(21..25, 11..11, 'l'); put(23, 10, '#')
        put(13, 14, 'A'); put(14, 14, 'A')
        spawn(); put(24, 10, 'D')
    },

    // 14 — EASTER EGG: CAPTCHA "select all squares with spikes"
    Level(
        name = T("CAPTCHA", "CAPTCHA"),
        intro = T("Select all squares with spikes. Or rather: avoid them.", "Wähle alle Kacheln mit Spikes. Oder besser: meide sie."),
        legend = mapOf('A' to hiddenSpike),
        traps = listOf(
            trap(PastX(12.8f), Play(Card.SPIKE_SEED), Show('A'), say("Wrong. Are you a robot?", "Falsch. Bist du ein Roboter?")),
        ),
    ) {
        border(); floor()
        for (x in listOf(10, 12, 14, 16, 18, 20)) put(x, 14, '^')
        put(15, 14, 'A')
        spawn(); door(); bits(14)
    },

    // 15 — EASTER EGG: git blame
    Level(
        name = T("git blame", "git blame"),
        intro = T("Someone broke this. Not naming names.", "Jemand hat das kaputtgemacht. Namen nenne ich nicht."),
        traps = listOf(
            trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, 12f, 8f), say("git blame: 100% you, 3 seconds ago.", "git blame: zu 100 % du, vor 3 Sekunden."), delay = 0.12f),
            trap(Touch('b'), Move('b', 0f, 12f, 8f), delay = 0.1f),
            trap(Touch('c'), Move('c', 0f, 12f, 8f), delay = 0.1f),
            trap(PastX(6f), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f)),
        ),
    ) {
        border()
        fill(0..7, 15..17); fill(25..31, 15..17)
        fill(9..10, 15..15, 'a'); fill(14..15, 14..14, 'b'); fill(19..21, 15..15, 'c')
        spawn(); door(); bits(15)
    },

    // 16 — EASTER EGG: "works on my machine" (works until it's deployed to production)
    Level(
        name = T("Works on My Machine", "Läuft bei mir"),
        intro = T("Tested locally. Green everywhere.", "Lokal getestet. Überall grün."),
        traps = listOf(
            trap(PastX(8.8f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Production has different gravity.", "Produktion hat eine andere Schwerkraft.")),
            trap(PastX(25f), Gravity(false), say("Works on my machine!", "Läuft bei mir!")),
        ),
    ) {
        border(); floor()
        leds(11..24)
        put(19, 1, 'v'); put(22, 1, 'v')
        leds(26..27)
        spawn(); door()
    },
)
