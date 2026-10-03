package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Bluff
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
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.Landed
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
        // 33 — a hidden pit on the way to the door; the door is a fake: clear screen, then the floor is gone and the door went home
        // TWIST: FakeWin (clear)
        Level(
            name = T("Clear Road", "Freie Fahrt"),
            intro = T("No traps today. I'm on vacation.", "Heute keine Fallen. Ich habe Urlaub."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('c'), Fall('c'), delay = 0.06f),
                trap(AtDoor, Play(Card.DECOY), FakeWin(FakeEnd.CLEAR, null, Hide('f'), Show('A'), DoorTo(3, 14))),
            ),
            // rematch: no fake this time, but the floor erodes behind the landing and drops right before the door
            rematch = listOf(
                Round(
                    T("Rematch. This time the door is real. Honest.", "Revanche. Diesmal ist die Tür echt. Ehrlich."),
                    traps = listOf(
                        trap(Touch('c'), Fall('c'), delay = 0.06f),
                        trap(PastX(23.5f), Say(T("Same trick twice? Please.", "Zweimal derselbe Trick? Ich bitte dich."))),
                        // the floor erodes behind you, tile by tile, toward the door: nobody waits for a fake here
                        trap(Landed(21.8f, 25f), Fall('h'), delay = 0.35f),
                        trap(Landed(21.8f, 25f), Fall('i'), delay = 0.6f),
                        trap(Touch('g'), Play(Card.COLLAPSE), Fall('g'), delay = 0.06f),
                    ),
                ) { fill(22..23, 15..17, 'h'); fill(24..24, 15..17, 'i'); fill(25..26, 15..17, 'g') },
            ),
        ) {
            border(); floor()
            fill(20..21, 15..17, 'c')
            fill(13..16, 15..17, 'f')
            put(6, 14, 'A'); put(7, 14, 'A')
            put(8, 14, 'P'); put(28, 14, 'D')
        },

        // 34 — the pause button dodges; only a real pause (back button) opens the wall
        // TWIST: PauseTrap dodge + Resumed
        Level(
            name = T("Monday Morning", "Montagmorgen"),
            intro = T("Everything is a bit slow today. Coffee is brewing.", "Heute ist alles etwas langsam. Der Kaffee läuft."),
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
            name = T("Home Network", "Heimnetz"),
            intro = T("My internet is slow today. Don't mind me.", "Mein Internet ist heute lahm. Lass dich nicht stören."),
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

        // 36 — a piece of the golden frame breaks off the ceiling and lands on the path; hopping onto it sprouts spikes behind it
        // TWIST: FrameCrack
        Level(
            name = T("Gallery", "Galerie"),
            intro = T("All real gold. Almost. Don't touch.", "Alles echtes Gold. Fast. Nicht anfassen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(4f), Play(Card.HEADBUTT), FrameCrack(16, 0, 19, 0, warn = 0.9f), Say(T("Crack.", "Knack."))),
                trap(Airborne(14f, 20f), Show('A')),
            ),
            // rematch: the frame holds, the spot you waited on drops, and the spikes come for walkers
            rematch = listOf(
                Round(
                    T("Rematch. New exhibition, same thief.", "Revanche. Neue Ausstellung, gleicher Dieb."),
                    traps = listOf(
                        trap(PastX(4f), Shake(0.3f), Say(T("Mind the frame.", "Vorsicht, Rahmen."))),
                        trap(Touch('w'), Play(Card.COLLAPSE), Fall('w'), delay = 1f),
                        trap(PastX(19.4f), Show('A'), Say(T("Looking up was the wrong idea.", "Nach oben schauen war falsch."))),
                    ),
                ) { fill(9..11, 15..17, 'w') },
            ),
        ) {
            border(); floor()
            put(21, 14, 'A'); put(22, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 37 — reach the door and I overwrite history: it moves back to the start and spikes appear
        // EASTER EGG: git push --force
        Level(
            name = T("git push --force", "git push --force"),
            intro = T("Mephi already pushed. You just need to pull.", "Mephi hat schon gepusht. Du musst nur noch pullen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(23f), Play(Card.SHY_DOOR), DoorTo(29, 0, speed = 25f), Show('A'), Say(T("History rewritten. You're welcome.", "Historie umgeschrieben. Gern geschehen."))),
                trap(PastX(23f), DoorTo(2, 14, speed = 20f), delay = 0.8f),
            ),
            // rematch: the force push comes early, halfway; whoever runs on to wait at the old spot meets spikes
            rematch = listOf(
                Round(
                    T("Rematch. I pushed first this time.", "Revanche. Diesmal hab ich zuerst gepusht."),
                    legend = mapOf('B' to hiddenSpike),
                    traps = listOf(
                        trap(PastX(14f), Play(Card.SHY_DOOR), DoorTo(2, 14, speed = 20f), Show('A'), Say(T("Early push. Pull faster.", "Früh gepusht. Zieh schneller."))),
                        trap(PastX(22f), Show('B')),
                    ),
                ) { put(24, 14, 'B'); put(25, 14, 'B') },
            ),
        ) {
            border(); floor(); pit(11..12)
            put(18, 14, '#')
            put(5, 14, 'A'); put(6, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 38 — the picture turns upside down over a spike pit; left and right follow the screen; the jump over the spikes lands before more
        // TWIST: Flip
        Level(
            name = T("Clear View", "Durchblick"),
            intro = T("Nice picture today. Sharper than ever.", "Schönes Bild heute. Schärfer als je zuvor."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(6f), Play(Card.UPSIDE_DOWN), Flip(3f), Say(T("Better view from here.", "Von hier hat man die bessere Aussicht."))),
                trap(Airborne(10f, 16.5f), Show('A')),
            ),
            // rematch: no flip where it came before; it comes in mid-jump over the spikes
            rematch = listOf(
                Round(
                    T("Rematch. I'll hold the picture still. Promise.", "Revanche. Ich halte das Bild still. Versprochen."),
                    traps = listOf(
                        trap(PastX(6f), Shake(0.3f), Say(T("See? Upright.", "Siehst du? Aufrecht."))),
                        trap(Airborne(11f, 13.5f), Play(Card.UPSIDE_DOWN), Flip(2.5f), Say(T("Promise expired.", "Versprechen abgelaufen."))),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(13..14, 14..14, '^')
            put(18, 14, 'A'); put(19, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 39 — a pit too wide to jump; the platform at its edge slides with the phone's tilt; hopping onto it and hopping off it both land on spikes
        // MOTION: Tilt
        Level(
            name = T("Hardware Store", "Baumarkt"),
            intro = T("I built something wide. No instructions.", "Ich habe was Breites gebaut. Ohne Anleitung."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(Airborne(5.5f, 9.6f), Show('B')),
                trap(Airborne(22f, 28f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Nobody said you could jump.", "Springen hat niemand erlaubt."))),
            ),
            start = listOf(Tilt('a', left = 0f, right = 13f, speed = 6f)),
        ) {
            border(); floor(); pit(8..23)
            put(9, 14, 'B'); put(10, 14, 'B')
            put(27, 14, 'A'); put(28, 14, 'A')
            fill(8..10, 15..15, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 40 — at the first step everything flips: gravity and controls. Walk the ceiling.
        // EASTER EGG: Kernel panic
        Level(
            name = T("Boot Sequence", "Systemstart"),
            intro = T("Everything is fine. Really.", "Alles in Ordnung. Wirklich."),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Swap(true), Shake(1.5f), Say(T("KERNEL PANIC - not syncing", "KERNEL PANIC - nicht synchronisiert"))),
                trap(Airborne(15f, 20.5f), Swap(false), Say(T("Controls restored. Probably.", "Steuerung wiederhergestellt. Vermutlich."))),
            ),
            // rematch: the keys stay sane on the ceiling and only swap over the hole
            rematch = listOf(
                Round(
                    T("Rematch. Rebooted in safe mode. Ha.", "Revanche. Im abgesicherten Modus. Haha."),
                    traps = listOf(
                        trap(PastX(4.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Shake(1.5f), Say(T("Panic, but gently.", "Panik, aber sanft."))),
                        trap(Airborne(15f, 20.5f), Swap(true), Say(T("Rebooted. Into a different panic.", "Neu gestartet. In eine andere Panik."))),
                    ),
                ),
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
            name = T("TV Night", "Fernsehabend"),
            intro = T("Reception is great today. No static.", "Der Empfang ist heute super. Kein Rauschen."),
            legend = mapOf('S' to ceilingSpike, 'T' to ceilingSpike),
            traps = listOf(
                trap(PastX(10f), Play(Card.GHOST_BLOCK), Fall('S'), Roll(2.4f, 2), Say(T("Technical difficulties.", "Bildstörung."))),
                trap(PastX(20.5f), Fall('T'), Roll(2f, 2)),
            ),
            // rematch: the second wave waits until you have walked on from where it fell before
            rematch = listOf(
                Round(
                    T("Rematch. Same channel, later broadcast.", "Revanche. Gleicher Sender, spätere Ausstrahlung."),
                    traps = listOf(
                        trap(PastX(10f), Play(Card.GHOST_BLOCK), Fall('S'), Roll(2.4f, 2), Say(T("Rerun.", "Wiederholung."))),
                        trap(PastX(21.1f), Fall('T'), Roll(2f, 2), Say(T("Now with a delay.", "Jetzt zeitversetzt."))),
                    ),
                ),
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
            name = T("Tailwind", "Rückenwind"),
            intro = T("Look ahead. There's nothing behind you.", "Schau nach vorne. Hinter dir ist nichts."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT), 'A' to hiddenSpike),
            traps = listOf(
                trap(PastX(7f), Play(Card.SPIKE_SEED), Move('W', 30f, 0f, 7.4f), PauseTrap(PauseTrick.SPIKE),
                    Say(T("Hungry wall! Need a break? Tap pause.", "Hungrige Wand! Pause? Tipp auf Pause."))),
                trap(Airborne(9.5f, 12.6f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(1..1, 1..14, 'W')
            put(15, 14, 'A'); put(16, 14, 'A')
            pit(11..12); pit(21..22)
            put(17, 14, '#'); put(26, 14, '#')
            put(5, 14, 'P'); put(29, 14, 'D')
        },

        // 43 — your last attempt comes back as a deadly ghost one second behind you
        // TWIST: Ghost
        Level(
            name = T("git blame", "git blame"),
            intro = T("I keep a log. Of everything you do.", "Ich führe Buch. Über alles, was du tust."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(After(0f), Play(Card.DEVIL_SAW), Ghost(1f)),
                trap(Airborne(12.5f, 17.5f), Show('A')),
            ),
        ) {
            border(); floor(); pit(25..26)
            fill(15..16, 14..14, '^')
            put(20, 14, 'A'); put(21, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 44 — a wall to the ceiling that only comes down when you shake the phone
        // MOTION: Shaken
        Level(
            name = T("Wallflower", "Mauerblümchen"),
            intro = T("The wall won't budge. Neither will I.", "Die Wand bewegt sich nicht. Ich auch nicht."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Shaken, Play(Card.COLLAPSE), Hide('a'), Say(T("Hey! Stop that!", "He! Lass das!"))),
                trap(Airborne(17f, 19.7f), Show('A')),
                trap(Touch('f'), Fall('f'), delay = 0.06f),
            ),
        ) {
            border(); floor()
            fill(20..20, 1..14, 'a')
            put(18, 14, 'A'); put(19, 14, 'A')
            fill(24..25, 15..17, 'f')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 45 — the floor is deleted when you are halfway; the shelf above it is not, but climbing onto it sprouts spikes
        // EASTER EGG: sudo rm -rf /
        Level(
            name = T("sudo rm -rf /", "sudo rm -rf /"),
            intro = T("Please log in as root. Password: hunter2.", "Bitte als root anmelden. Passwort: hunter2."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(PastX(14f), Play(Card.COLLAPSE), Fall('f'), Shake(1.5f), PauseTrap(PauseTrick.SWAP), Say(T("rm: removing '/' ... done. You wanted root.", "rm: entferne '/' ... erledigt. Du wolltest doch root."))),
                trap(Airborne(2.5f, 7f), Show('A')),
            ),
            // rematch: no spikes behind the climb; the hop you learned over them lands on new ones
            rematch = listOf(
                Round(
                    T("Rematch. Root again. Same password.", "Revanche. Wieder root. Gleiches Passwort."),
                    traps = listOf(
                        trap(PastX(14f), Play(Card.COLLAPSE), Fall('f'), Shake(1.5f), Say(T("rm -rf ./habits", "rm -rf ./gewohnheiten"))),
                        trap(Airborne(8.5f, 11.5f), Show('B')),
                    ),
                ),
            ),
        ) {
            border()
            fill(1..30, 15..17, 'f')
            fill(4..30, 13..13)
            put(9, 12, 'A'); put(10, 12, 'A')
            put(12, 12, 'B'); put(13, 12, 'B')
            put(20, 12, '^')
            put(2, 14, 'P'); put(29, 12, 'D')
        },

        // 46 — the wall is unclimbable; the way out is behind you
        Level(
            name = T("Home Stretch", "Zielgerade"),
            intro = T("That's the goal ahead. You can do it.", "Da vorne ist das Ziel. Du schaffst das."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('k'), Play(Card.DECOY), Hide('w'), Say(T("Oh, you found the button. Boo.", "Oh, du hast den Knopf gefunden. Buh."))),
                trap(PastX(20.5f), Show('A')),
            ),
            // rematch: the old button is a bluff; the real one hides in the air above the start
            rematch = listOf(
                Round(
                    T("Rematch. I moved the button.", "Revanche. Ich hab den Knopf versetzt."),
                    legend = mapOf('g' to ghost),
                    traps = listOf(
                        trap(Touch('k'), Bluff(Card.DECOY), Say(T("That button retired. Think higher.", "Der Knopf ist in Rente. Denk höher."))),
                        trap(Touch('g'), Hide('w'), Say(T("Fine. Up there.", "Na gut. Da oben."))),
                        trap(PastX(20.5f), Show('A')),
                    ),
                ) { put(10, 12, 'g') },
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
            intro = T("I'm hungry. Make me a sandwich.", "Ich habe Hunger. Mach mir ein Sandwich."),
            legend = mapOf('k' to ghost, 'A' to hiddenSpike, 'C' to hiddenSpike),
            traps = listOf(
                trap(Touch('k'), Play(Card.GHOST_BLOCK), Hide('w'), Say(T("Okay.", "Okay."))),
                trap(PastX(13.5f), Show('A')),
                trap(Landed(19f, 22f), Fall('f')),
            ),
            // rematch: Mephi learned: whoever stands under the sandwich to fetch it lands on spikes; take it on the run
            rematch = listOf(
                Round(
                    T("Rematch. I'm hungry again.", "Revanche. Ich hab schon wieder Hunger."),
                    traps = listOf(
                        trap(Touch('k'), Play(Card.GHOST_BLOCK), Hide('w'), Show('C'), Say(T("Sandwich comes with a side of spikes.", "Sandwich mit Beilage. Spitzer Beilage."))),
                        trap(PastX(13.5f), Show('A')),
                        trap(Landed(19f, 22f), Fall('f')),
                    ),
                ),
            ),
        ) {
            border(); floor()
            put(17, 14, 'A'); put(18, 14, 'A')
            put(10, 14, 'C')
            fill(23..23, 15..17, 'f')
            fill(20..21, 3..14, 'w')
            put(10, 12, 'k')
            // the sandwich, hovering above the root block
            art(8, 4, "#####", "#.#.#", ".###.", "#####")
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 48 — finale: a blinking bridge, then the door ends the game. The credits roll, become stairs, the door went home.
        // TWIST: FakeWin (credits) + Roll
        Level(
            name = T("Exit", "Ausgang"),
            intro = T("Last level. Promise.", "Letztes Level. Versprochen."),
            legend = mapOf('c' to hiddenSolid, 'A' to hiddenSpike),
            traps = listOf(
                trap(Airborne(15.2f, 18.6f), Play(Card.GRAND_FINALE), Show('A'), Say(T("Final level! Everything I've got.", "Letztes Level! Alles, was ich habe."))),
                trap(AtDoor, FakeWin(FakeEnd.CREDITS, 'c', DoorTo(1, 8), Roll(3f, 2))),
            ),
            start = listOf(Blink('a', on = 2.4f, off = 1f)),
            // round 2 (encore): no fake, but the floor after the bridge drops: jump off its end this time.
            // round 3 (second encore): the door goes home, back over the bridge
            rematch = listOf(
                Round(
                    T("Encore! Nobody leaves before the encore.", "Zugabe! Keiner geht vor der Zugabe."),
                    start = listOf(Blink('a', on = 2.4f, off = 1f, phase = 1.2f)),
                    traps = listOf(
                        trap(After(0.4f), Say(T("Same song. New ending.", "Gleiches Lied. Neues Ende."))),
                        trap(Touch('e'), Play(Card.GRAND_FINALE), Fall('e'), delay = 0.08f),
                    ),
                ) { fill(17..18, 15..17, 'e') },
                Round(
                    T("Second encore. The crowd insists. I'm the crowd.", "Zweite Zugabe. Das Publikum will es. Ich bin das Publikum."),
                    start = listOf(Blink('a', on = 2.4f, off = 1f, phase = 1.2f)),
                    traps = listOf(
                        trap(After(0.4f), Say(T("From the top!", "Da capo!"))),
                        // up and over your head first, then home to the start
                        trap(PastX(23f), Play(Card.GRAND_FINALE), DoorTo(28, 4, speed = 20f), Say(T("Encore means from the top.", "Zugabe heißt: von vorn."))),
                        trap(PastX(23f), DoorTo(3, 14, speed = 14f), delay = 0.6f),
                    ),
                ),
            ),
        ) {
            border(); floor(); pit(10..16)
            put(19, 14, 'A'); put(20, 14, 'A')
            fill(10..16, 15..15, 'a')
            fill(1..13, 9..9, 'c'); fill(15..19, 11..11, 'c'); fill(22..26, 13..13, 'c')
            put(2, 14, 'P'); put(28, 14, 'D')
        },
    )
}
