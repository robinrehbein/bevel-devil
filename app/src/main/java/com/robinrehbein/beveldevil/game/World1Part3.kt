package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.FakeWin
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Flip
import com.robinrehbein.beveldevil.game.Action.FrameCrack
import com.robinrehbein.beveldevil.game.Action.Ghost
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.PauseTrap
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Roll
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Action.Tilt
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Resumed
import com.robinrehbein.beveldevil.game.Trigger.Shaken
import com.robinrehbein.beveldevil.game.Trigger.Touch

/** World 1, levels 33-48. Act 3, "Mephi schummelt": the meta twists and phone motion, sparingly, as surprises. */
object World1Part3 {
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    val levels: List<Level> = listOf(
        // 33 — the door is a fake: clear screen, then the floor is gone and the door went home
        // TWIST: FakeWin (clear)
        Level(
            name = T("Too Easy", "Zu einfach"),
            intro = T("Just walk. Honestly.", "Einfach laufen. Ehrlich."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(trap(AtDoor, Play(Card.DECOY), FakeWin(FakeEnd.CLEAR, null, Hide('f'), Show('A'), DoorTo(3, 14)))),
        ) {
            border(); floor()
            fill(13..16, 15..17, 'f')
            put(6, 14, 'A'); put(7, 14, 'A')
            put(8, 14, 'P'); put(28, 14, 'D')
        },

        // 34 — the pause button dodges; only a real pause (back button) opens the wall
        // TWIST: PauseTrap dodge + Resumed
        Level(
            name = T("Coffee Break", "Kaffeepause"),
            intro = T("You look tired. Take a break.", "Du wirkst müde. Mach mal Pause."),
            traps = listOf(
                trap(After(0.3f), Play(Card.SHY_DOOR), PauseTrap(PauseTrick.DODGE)),
                trap(Resumed(), Hide('w'), Hide('a'), Say(T("Refreshed? The floor took a break too.", "Erholt? Der Boden macht jetzt auch Pause."))),
            ),
        ) {
            border(); floor()
            fill(22..22, 1..14, 'w')
            fill(12..14, 15..17, 'a')
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 35 — the door approaches in stages, stalls at 99% and then jumps back. Just wait.
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

        // 36 — a piece of the golden frame breaks off the ceiling and lands on the path
        // TWIST: FrameCrack
        Level(
            name = T("Load-Bearing Frame", "Tragender Rahmen"),
            intro = T("Nice frame, isn't it?", "Schöner Rahmen, oder?"),
            traps = listOf(trap(PastX(4f), Play(Card.HEADBUTT), FrameCrack(16, 0, 19, 0, warn = 0.9f), Say(T("Crack.", "Knack.")))),
        ) {
            border(); floor()
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 37 — reach the door and I overwrite history: it moves back to the start and spikes appear
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

        // 38 — the picture turns upside down over a spike pit; left and right follow the screen
        // TWIST: Flip
        Level(
            name = T("Headstand", "Kopfstand"),
            intro = T("Hold your phone properly.", "Halt dein Handy mal richtig."),
            traps = listOf(
                trap(PastX(6f), Play(Card.UPSIDE_DOWN), Flip(3f), Say(T("Better view from here.", "Von hier hat man die bessere Aussicht."))),
            ),
        ) {
            border(); floor()
            fill(13..14, 14..14, '^')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 39 — a pit too wide to jump; the platform at its edge slides with the phone's tilt
        // MOTION: Tilt
        Level(
            name = T("Spirit Level", "Wasserwaage"),
            intro = T("Hold it level, please.", "Bitte gerade halten."),
            start = listOf(Tilt('a', left = 0f, right = 13f, speed = 6f)),
        ) {
            border(); floor(); pit(8..23)
            fill(8..10, 15..15, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 40 — at the first step everything flips: gravity and controls. Walk the ceiling.
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

        // 41 — stalactites fall in two waves while the picture rolls; you cannot see them land
        // TWIST: Roll
        Level(
            name = T("V-Sync", "Bildfang"),
            intro = T("Enjoy the show. Keep your eyes on the level.", "Viel Spaß beim Zuschauen. Bleib mit den Augen im Level."),
            legend = mapOf('S' to ceilingSpike, 'T' to ceilingSpike),
            traps = listOf(
                trap(PastX(10f), Play(Card.GHOST_BLOCK), Fall('S'), Roll(2.4f, 2), Say(T("Technical difficulties.", "Bildstörung."))),
                trap(PastX(20.5f), Fall('T'), Roll(2f, 2)),
            ),
        ) {
            border(); floor()
            put(14, 1, 'S'); put(15, 1, 'S')
            put(24, 1, 'T'); put(25, 1, 'T')
            ceilingSpikes(7..8); ceilingSpikes(29..30)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 42 — the old wall of teeth chases you and Mephi offers a pause; the pause button is a spike
        // TWIST: PauseTrap spike
        Level(
            name = T("Panic Button", "Notbremse"),
            intro = T("Don't look back. Seriously.", "Schau nicht zurück. Ernsthaft."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT)),
            traps = listOf(
                trap(PastX(7f), Play(Card.SPIKE_SEED), Move('W', 30f, 0f, 7.4f), PauseTrap(PauseTrick.SPIKE),
                    Say(T("Hungry wall! Need a break? Tap pause.", "Hungrige Wand! Pause? Tipp auf Pause."))),
            ),
        ) {
            border(); floor()
            fill(1..1, 1..14, 'W')
            pit(11..12); pit(21..22)
            put(17, 14, '#'); put(26, 14, '#')
            put(5, 14, 'P'); put(29, 14, 'D')
        },

        // 43 — your last attempt comes back as a deadly ghost one second behind you
        // TWIST: Ghost
        Level(
            name = T("git blame", "git blame"),
            intro = T("Replaying your last attempt.", "Ich spiele deinen letzten Versuch ab."),
            traps = listOf(trap(After(0f), Play(Card.DEVIL_SAW), Ghost(1f))),
        ) {
            border(); floor(); pit(22..24)
            fill(15..16, 14..14, '^')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 44 — a wall to the ceiling that only comes down when you shake the phone
        // MOTION: Shaken
        Level(
            name = T("Shake It", "Schüttel dich"),
            intro = T("Have you tried turning it off and on again?", "Schon mal aus- und wieder eingeschaltet?"),
            traps = listOf(trap(Shaken, Play(Card.COLLAPSE), Hide('a'), Say(T("Hey! Stop that!", "He! Lass das!")))),
        ) {
            border(); floor()
            fill(20..20, 1..14, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 45 — the floor is deleted when you are halfway; the shelf above it is not
        // EASTER EGG: sudo rm -rf /
        Level(
            name = T("sudo rm -rf /", "sudo rm -rf /"),
            intro = T("Are you sure? [y/N]", "Bist du sicher? [j/N]"),
            traps = listOf(
                trap(PastX(14f), Play(Card.COLLAPSE), Fall('f'), Shake(1.5f), PauseTrap(PauseTrick.SWAP), Say(T("rm: removing '/' ... done. You wanted root.", "rm: entferne '/' ... erledigt. Du wolltest doch root."))),
            ),
        ) {
            border()
            fill(1..30, 15..17, 'f')
            fill(4..30, 13..13)
            put(20, 12, '^')
            put(2, 14, 'P'); put(29, 12, 'D')
        },

        // 46 — the wall is unclimbable; the way out is behind you
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

        // 48 — finale: a blinking bridge, then the door ends the game. The credits roll, become stairs, the door went home.
        // TWIST: FakeWin (credits) + Roll
        Level(
            name = T("Roll Credits", "Abspann"),
            intro = T("Last level. Promise.", "Letztes Level. Versprochen."),
            legend = mapOf('c' to hiddenSolid),
            traps = listOf(
                trap(PastX(5f), Play(Card.GRAND_FINALE), Say(T("Final level! Everything I've got.", "Letztes Level! Alles, was ich habe."))),
                trap(AtDoor, FakeWin(FakeEnd.CREDITS, 'c', DoorTo(1, 8), Roll(3f, 2))),
            ),
            start = listOf(Blink('a', on = 2.4f, off = 1f)),
        ) {
            border(); floor(); pit(10..16)
            fill(10..16, 15..15, 'a')
            fill(1..13, 9..9, 'c'); fill(15..19, 11..11, 'c'); fill(22..26, 13..13, 'c')
            put(2, 14, 'P'); put(28, 14, 'D')
        },
    )
}
