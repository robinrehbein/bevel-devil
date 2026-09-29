package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/** World 1, levels 1-16. */
object World1Part1 {

    val levels: List<Level> = listOf(
        // 1 — the floor in front of you collapses
        Level(
            name = T("Warm-up", "Aufwärmen"),
            intro = T("Go on, walk to the door. I won't do a thing.", "Geh ruhig zur Tür. Ich tu nichts."),
            traps = listOf(
                trap(PastX(16.5f), Play(Card.COLLAPSE), Fall('a'), Say(T("Floor? More of a suggestion.", "Boden? Eher ein Vorschlag."))),
            ),
        ) {
            border(); floor()
            fill(19..21, 15..17, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 2 — spikes sprout right before the door
        Level(
            name = T("Spike Seed", "Spitzfindig"),
            intro = T("Spikes hurt. Just so you know.", "Spikes tun weh. Nur zur Info."),
            legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(
                trap(PastX(21.5f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Oh look, they grew.", "Oh, die sind gewachsen."))),
            ),
        ) {
            border(); floor()
            put(10, 14, '^')
            put(24, 14, 'A'); put(25, 14, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 3 — the door flees up the stairs
        Level(
            name = T("The Shy Door", "Die Tür ist schüchtern"),
            intro = T("The door is a bit shy.", "Die Tür ist etwas schüchtern."),
            traps = listOf(
                trap(PastX(25.5f), Play(Card.SHY_DOOR), DoorTo(2, 6, speed = 18f), Say(T("Whoops. Up there now.", "Hoppla. Jetzt ist sie da oben."))),
            ),
        ) {
            border(); floor()
            fill(20..23, 13..13); fill(14..17, 11..11); fill(8..11, 9..9); fill(1..5, 7..7)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 4 — blocks drop from above
        Level(
            name = T("Headbutt", "Kopfnuss"),
            intro = T("Mind your head.", "Pass auf deinen Kopf auf."),
            traps = listOf(
                trap(PastX(12.8f), Play(Card.HEADBUTT), Fall('c'), Say(T("Ceiling delivery!", "Deckenlieferung!"))),
                trap(PastX(21.3f), Fall('d'), Say(T("Again? Again.", "Nochmal? Nochmal."))),
            ),
        ) {
            border(); floor()
            fill(14..16, 5..6, 'c'); fill(23..25, 5..6, 'd')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 5 — gravity flips over a spike floor
        Level(
            name = T("Upside Down", "Kopfüber"),
            intro = T("Nice floor, right? Very pointy.", "Schöner Boden, oder? Sehr spitz."),
            traps = listOf(
                trap(PastX(9.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Up is the new down.", "Oben ist das neue Unten."))),
                trap(PastX(24.2f), Gravity(false)),
            ),
        ) {
            border(); floor()
            fill(11..24, 14..14, '^')
            put(17, 1, 'v'); put(22, 1, 'v')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 6 — controls swap, then swap back right before the second pit
        Level(
            name = T("Twisted", "Verdreht"),
            intro = T("Two little holes. Easy.", "Zwei kleine Löcher. Einfach."),
            traps = listOf(
                trap(PastX(7f), Play(Card.TWISTED), Swap(true), Say(T("Left is the new right.", "Links ist das neue Rechts."))),
                trap(PastX(17.5f), Swap(false), Say(T("Or is it?", "Oder doch nicht?"))),
            ),
        ) {
            border(); floor()
            fill(12..14, 15..17, '.'); fill(21..23, 15..17, '.')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 7 — a saw chases you while the floor crumbles
        Level(
            name = T("Devil Saw", "Teufelssäge"),
            intro = T("Take your time.", "Lass dir ruhig Zeit."),
            traps = listOf(
                trap(PastX(5f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f), Say(T("It only wants a hug!", "Sie will nur kuscheln!"))),
                trap(PastX(20f), Fall('a')),
            ),
        ) {
            border(); floor()
            put(14, 14, '#')
            fill(22..23, 15..17, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 8 — an invisible block appears when you hit it from below
        Level(
            name = T("Head First", "Mit dem Kopf durch"),
            intro = T("That wall is way too high. Sad.", "Die Wand ist viel zu hoch. Schade."),
            legend = mapOf('b' to Glyph(spike = false, hidden = true, bonk = true)),
            traps = listOf(
                trap(Touch('b'), Play(Card.GHOST_BLOCK), Say(T("Hey! That one was secret.", "Hey! Der war geheim."))),
            ),
        ) {
            border(); floor()
            fill(19..20, 12..14)
            put(18, 13, 'b')
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 9 — platforms sink as soon as you stand on them
        Level(
            name = T("Sinking", "Sinkflug"),
            intro = T("Solid platforms. Promise.", "Stabile Plattformen. Versprochen."),
            traps = listOf(
                trap(Touch('b'), Play(Card.SINKING), Move('b', 0f, 12f, 7f), Say(T("Solid ground, limited offer.", "Fester Boden, nur kurz gültig.")), delay = 0.12f),
                trap(Touch('c'), Move('c', 0f, 12f, 9f), delay = 0.08f),
            ),
        ) {
            border(); floor()
            fill(7..24, 15..17, '.')
            fill(9..11, 13..13, 'a'); fill(14..16, 13..13, 'b'); fill(19..21, 13..13, 'c')
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 10 — the floor under the door gives way, the real door waits downstairs
        Level(
            name = T("Decoy", "Attrappe"),
            intro = T("Look, the door is right there.", "Schau, die Tür ist gleich da."),
            traps = listOf(
                trap(PastX(22f), Play(Card.DECOY), Fall('a'), DoorTo(3, 14, speed = 10f), Say(T("That door was decoration.", "Die Tür war nur Deko."))),
            ),
        ) {
            border(); floor()
            fill(1..19, 9..9); fill(20..30, 9..9, 'a')
            put(12, 14, '^'); put(17, 14, '^')
            put(2, 8, 'P'); put(28, 8, 'D')
        },

        // 11 — every floor tile drops shortly after you touch it
        Level(
            name = T("Crumble", "Wackelboden"),
            intro = T("Don't stop. Just a tip.", "Nicht stehen bleiben. Nur ein Tipp."),
            traps = listOf(trap(Touch('a'), Play(Card.CRUMBLE), Say(T("Keep moving!", "Weiterlaufen!")))) +
                ('a'..'k').map { g -> trap(Touch(g), Fall(g), delay = 0.28f) },
        ) {
            border()
            fill(0..4, 15..17); fill(27..31, 15..17)
            ('a'..'k').forEachIndexed { i, g -> fill(5 + i * 2..6 + i * 2, 15..15, g) }
            fill(8..24, 12..12, 'v')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 12 — everything at once
        Level(
            name = T("Grand Finale", "Großes Finale"),
            intro = T("Last level. I saved the best for you.", "Letztes Level. Das Beste kommt zum Schluss."),
            legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(
                trap(PastX(3f), Play(Card.GRAND_FINALE), Saw(-1.5f, 14.4f, 6f, 0f, 0.62f), Say(T("Grand finale! Everything at once!", "Finale! Alles gleichzeitig!"))),
                trap(PastX(7.2f), Fall('a')),
                trap(PastX(14f), Show('A')),
                trap(PastX(24.5f), DoorTo(20, 1, speed = 14f, hanging = true), Gravity(true), Say(T("Come and get it.", "Hol sie dir doch."))),
            ),
        ) {
            border(); floor()
            fill(9..11, 15..17, 'a')
            put(17, 14, 'A')
            put(2, 14, 'P'); put(28, 14, 'D')
        },

        // 13 — spawn on the right, door on the left; spikes sprout on the way back
        Level(
            name = T("Lefty", "Linkshänder"),
            intro = T("The door is on the left. I know. Unusual.", "Die Tür ist links. Ich weiß. Ungewohnt."),
            legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(
                trap(BeforeX(12.5f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Spikes read right to left too.", "Spikes lesen auch von rechts nach links."))),
            ),
        ) {
            border(); floor()
            put(20, 14, '^')
            put(7, 14, 'A'); put(8, 14, 'A')
            put(29, 14, 'P'); put(2, 14, 'D')
        },

        // 14 — stalactites fall in two waves; wait for each wave to land
        Level(
            name = T("Stalactites", "Tropfsteinhöhle"),
            intro = T("Lovely cave. The ceiling is purely decorative.", "Schöne Höhle. Die Decke ist reine Deko."),
            legend = mapOf('S' to Glyph(spike = true, dir = Dir.DOWN), 'T' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = listOf(
                trap(PastX(10f), Play(Card.HEADBUTT), Fall('S'), Say(T("Drip. Drop. Dead.", "Tropf. Tropf. Tot."))),
                trap(PastX(20.5f), Fall('T'), Say(T("There is always a second wave.", "Eine zweite Welle gibt's immer."))),
            ),
        ) {
            border(); floor()
            put(14, 1, 'S'); put(15, 1, 'S')
            put(24, 1, 'T'); put(25, 1, 'T')
            ceilingSpikes(7..8); ceilingSpikes(29..30)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 15 — stepping stones over a pit; the third one is a crumbler
        Level(
            name = T("Stepping Stones", "Trittsteine"),
            intro = T("Five stones. Four of them are honest.", "Fünf Steine. Vier davon sind ehrlich."),
            traps = listOf(
                trap(Touch('c'), Play(Card.CRUMBLE), Fall('c'), Say(T("Stone number three says bye.", "Stein Nummer drei sagt tschüss.")), delay = 0.22f),
            ),
        ) {
            border(); floor(); pit(6..27)
            fill(8..10, 14..14); fill(13..15, 14..14); fill(18..20, 14..14, 'c'); fill(23..25, 13..13)
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 16 — a lift takes you up; spikes appear on the ledge when you arrive
        Level(
            name = T("Elevator Pitch", "Fahrstuhlmusik"),
            intro = T("Going up. Please enjoy the silence.", "Es geht aufwärts. Bitte genieße die Stille."),
            legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(
                trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, -7f, 4.5f), Say(T("Ding! Next floor: pointy.", "Ding! Nächste Etage: spitz."))),
                trap(Zone(11f, 5.5f, 16f, 7.6f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(11..15, 14..14, 'a')
            fill(18..30, 7..7)
            put(18, 6, 'A'); put(19, 6, 'A')
            put(2, 14, 'P'); put(29, 6, 'D')
        },
    )
}
