package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 1, levels 1-16. Act 1, "Die Karten": the classic tricks, one card at a time. From level 3 on the traps come
 * in chains: each next one punishes the counter the last one taught. Triggers sit on the spot (under the feet,
 * mid-jump, right after landing), and the rooms look plain: trap blocks are ordinary floor and ceiling until they fire.
 */
object World1Part1 {
    /** A hidden spike group, shown by a trap. */
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    /** A hidden spike group hanging from the ceiling. */
    private val hiddenCeilingSpike = Glyph(spike = true, dir = Dir.DOWN, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    val levels: List<Level> = listOf(
        // 1 — the floor in front of you collapses; then the door takes two steps away from you
        Level(
            name = T("Warm-up", "Aufwärmen"),
            intro = T("Welcome to the basement. Please wipe your feet.", "Willkommen im Keller. Bitte Füße abtreten."),
            traps = listOf(
                trap(PastX(17.2f), Play(Card.COLLAPSE), Fall('a'), Say(T("Floor? More of a suggestion.", "Boden? Eher ein Vorschlag."))),
                trap(PastX(23.2f), DoorTo(29, 14, speed = 10f), Say(T("Two more steps. Promise.", "Noch zwei Schritte. Versprochen."))),
            ),
        ) {
            border(); floor()
            fill(19..21, 15..17, 'a')
            put(2, 14, 'P'); put(27, 14, 'D')
        },

        // 2 — jump the spike and spikes sprout just behind your landing; jump again and it happens again; the fourth hop lands before a pit
        Level(
            name = T("The Hallway", "Der Flur"),
            intro = T("The floor is freshly mopped. Try not to slip.", "Der Boden ist frisch gewischt. Nicht ausrutschen."),
            legend = mapOf('A' to hiddenSpike, 'B' to hiddenSpike),
            traps = listOf(
                trap(Airborne(9.3f, 10.9f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Oh look, they grew.", "Oh, die sind gewachsen."))),
                trap(Airborne(15f, 17f), Show('B')),
                trap(Airborne(20f, 22f), Fall('c')),
            ),
        ) {
            border(); floor()
            put(9, 14, '^')
            put(14, 14, 'A'); put(15, 14, 'A')
            put(19, 14, 'B'); put(20, 14, 'B')
            fill(25..26, 15..17, 'c')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 3 — the door hops up to a ledge as you near it; a step crumbles behind you; the door comes back down as you reach the top
        Level(
            name = T("Stairwell", "Treppenhaus"),
            intro = T("Take the stairs. The elevator is broken. Again.", "Nimm die Treppe. Der Aufzug ist kaputt. Schon wieder."),
            hint = T("Stairs go up. So did the door.", "Treppen führen nach oben. Die Tür auch."),
            traps = listOf(
                trap(PastX(26.4f), Play(Card.SHY_DOOR), DoorTo(29, 6, speed = 18f), Say(T("Whoops. Up there now.", "Hoppla. Jetzt ist sie da oben."))),
                trap(Touch('b'), Fall('b'), delay = 0.7f),
                // the step floats back after a while: whoever fell with it can climb again instead of being stuck below
                trap(Touch('b'), Move('b', 0f, -3f, 5f), delay = 3f),
                trap(Zone(27.2f, 5f, 28.3f, 7f), DoorTo(29, 14, speed = 20f), Say(T("Going down?", "Wieder runter?"))),
            ),
        ) {
            border(); floor()
            fill(24..27, 13..13); fill(18..21, 11..11, 'b'); fill(24..27, 9..9); fill(27..30, 7..7)
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 4 — a slab of the thick ceiling drops when you pass under it; wait for the next and it drops while you are in the air
        Level(
            name = T("House Rules", "Hausordnung"),
            intro = T("The floor here is a bit shaky. Watch your feet.", "Der Boden hier ist etwas wackelig. Schau auf deine Füße."),
            traps = listOf(
                trap(PastX(13.1f), Play(Card.HEADBUTT), Fall('c'), Say(T("Ceiling delivery!", "Deckenlieferung!"))),
                trap(Landed(17f, 20.6f), Fall('d'), Say(T("Again? Again.", "Nochmal? Nochmal."))),
                trap(Airborne(22f, 24f), Fall('e')),
            ),
            // rematch: the slab you learned to wait for only rattles, the one you run under next falls
            rematch = listOf(
                Round(
                    T("Rematch. Same ceiling, new grudge.", "Revanche. Gleiche Decke, frischer Groll."),
                    traps = listOf(
                        trap(PastX(13.1f), Shake(0.5f), Say(T("Flinched. Cute.", "Gezuckt. Niedlich."))),
                        trap(PastX(18.2f), Play(Card.HEADBUTT), Fall('d'), Say(T("This one means it.", "Die hier meint es ernst."))),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(1..30, 1..6)
            fill(14..16, 5..6, 'c'); fill(21..23, 5..6, 'd'); fill(26..28, 5..6, 'e')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 5 — an invisible block appears when you hit it from below; the wall top is a bad place to stand; the far side is a worse one
        Level(
            name = T("Obstacle Course", "Hindernislauf"),
            intro = T("That wall is too high. Giving up is the only option.", "Die Wand ist zu hoch. Da hilft nur Aufgeben."),
            hint = T("Ever headbutted thin air? Try it just before the wall.", "Schon mal mit dem Kopf gegen Luft gesprungen? Kurz vor der Wand."),
            legend = mapOf('b' to Glyph(spike = false, hidden = true, bonk = true), 'A' to hiddenSpike, 'f' to Glyph(spike = false)),
            traps = listOf(
                trap(Touch('b'), Play(Card.GHOST_BLOCK), Say(T("Hey! That one was secret.", "Hey! Der war geheim."))),
                trap(Zone(19f, 9f, 21f, 12.2f), Show('A')),
                trap(Landed(26.6f, 29.5f), Fall('f')),
            ),
        ) {
            border(); floor()
            fill(19..20, 12..14)
            put(18, 13, 'b')
            fill(23..25, 14..14, 'A')
            fill(28..29, 15..17, 'f')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 6 — a saw comes from behind; the floor drops under your feet in mid-run; once you have landed a second saw comes from the front
        Level(
            name = T("Cozy", "Gemütlich"),
            intro = T("Take your time.", "Lass dir ruhig Zeit."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(PastX(5f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f), Say(T("It only wants a hug!", "Sie will nur kuscheln!"))),
                trap(Touch('a'), Fall('a'), delay = 0.12f),
                trap(Landed(23.4f, 27f), Saw(33.5f, 14.4f, -4.5f, 0f, 0.62f), Say(T("And its brother wants one too.", "Und ihr Bruder will auch eine."))),
            ),
            // rematch: the floor holds now, and jumping the spot where it fell lands on spikes
            rematch = listOf(
                Round(
                    T("Rematch. Same saw, new manners.", "Revanche. Gleiche Säge, neue Manieren."),
                    traps = listOf(
                        trap(PastX(5f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f), Say(T("Back for seconds.", "Nachschlag gefällig?"))),
                        trap(Airborne(19.5f, 23f), Show('A'), Say(T("Jumped over nothing. Landed on something.", "Über nichts gesprungen. Auf was gelandet."))),
                    ),
                ),
            ),
        ) {
            border(); floor()
            put(14, 14, '#')
            put(23, 14, 'A'); put(24, 14, 'A')
            fill(21..22, 15..17, 'a')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 7 — two platforms sink as soon as you stand on them; the last one grows spikes as you fly towards it
        Level(
            name = T("Prefab", "Plattenbau"),
            intro = T("They still build things properly down here.", "Hier unten baut man noch mit Qualität."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('b'), Play(Card.SINKING), Move('b', 0f, 12f, 7f), Say(T("Solid ground, limited offer.", "Fester Boden, nur kurz gültig.")), delay = 0.12f),
                trap(Touch('c'), Move('c', 0f, 12f, 9f), delay = 0.08f),
                // hopping back off the first slab strands nobody: the start side gives way a moment after it sank
                trap(Touch('b'), Fall('s'), delay = 2.5f),
                trap(Airborne(22f, 23.6f), Show('A')),
            ),
        ) {
            border(); floor()
            fill(7..26, 15..17, '.')
            fill(1..6, 15..17, 's'); fill(9..11, 13..13, 's'); fill(14..16, 13..13, 'b'); fill(19..21, 13..13, 'c'); fill(23..26, 13..13)
            put(23, 12, 'A'); put(24, 12, 'A')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 8 — gravity flips over a spike floor as you reach it, flips back mid-way, then again as you land, then back for the door
        Level(
            name = T("Down to Earth", "Bodenständig"),
            intro = T("I picked the decor myself. Nice, right?", "Die Deko habe ich selbst ausgesucht. Schön, oder?"),
            traps = listOf(
                trap(PastX(9.9f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Up is the new down.", "Oben ist das neue Unten."))),
                trap(PastX(22.8f), Gravity(false)),
                trap(Landed(24.5f, 29f), Gravity(true)),
                trap(PastX(29.6f), Gravity(false)),
            ),
            // rematch: walking in no longer flips you, only a jump does
            rematch = listOf(
                Round(
                    T("Rematch. Gravity is opt-in now.", "Revanche. Schwerkraft nur noch auf Antrag."),
                    traps = listOf(
                        trap(Airborne(8f, 11.5f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Jumpers only.", "Nur für Springer."))),
                        trap(PastX(22.8f), Gravity(false)),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(11..24, 14..14, '^')
            put(17, 1, 'v'); put(22, 1, 'v')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 9 — controls swap while you are in the air over the first hole, and swap back in the air over the second
        Level(
            name = T("Potholes", "Schlaglöcher"),
            intro = T("Two little holes. Easy.", "Zwei kleine Löcher. Einfach."),
            traps = listOf(
                trap(Airborne(12.2f, 14f), Play(Card.TWISTED), Swap(true), Say(T("Left is the new right.", "Links ist das neue Rechts."))),
                trap(Airborne(21.4f, 23f), Swap(false), Say(T("Or is it?", "Oder doch nicht?"))),
            ),
        ) {
            border(); floor()
            fill(12..14, 15..17, '.'); fill(21..23, 15..17, '.')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 10 — the floor under the door gives way when you step on it, the real door waits downstairs; landing spikes on the way back; the door steps away
        Level(
            name = T("Homeward", "Heimweg"),
            intro = T("I aired the place out just for you.", "Ich habe extra für dich gelüftet."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('a'), Play(Card.DECOY), Fall('a'), DoorTo(3, 14, speed = 10f), Say(T("That door was decoration.", "Die Tür war nur Deko."))),
                trap(Airborne(14.6f, 17f), Show('A')),
                trap(Landed(7f, 10.5f), DoorTo(1, 14, speed = 8f), Say(T("Two more steps. Promise.", "Noch zwei Schritte. Versprochen."))),
            ),
        ) {
            border(); floor()
            fill(1..19, 9..9); fill(20..30, 9..9, 'a')
            put(17, 14, '^')
            put(11, 14, 'A'); put(12, 14, 'A')
            put(2, 8, 'P'); put(28, 8, 'D')
        },

        // 11 — stepping stones over a pit; the second one crumbles slowly, the third quickly, the last grows a spike as you fly at it
        Level(
            name = T("The Creek", "Bachlauf"),
            intro = T("The last jump is the hard one. Focus.", "Der letzte Sprung ist der schwerste. Konzentrier dich."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Touch('b'), Play(Card.CRUMBLE), Fall('b'), Say(T("Stone number two says bye.", "Stein Nummer zwei sagt tschüss.")), delay = 0.45f),
                trap(Touch('c'), Fall('c'), delay = 0.2f),
                trap(Airborne(21.3f, 22.6f), Show('A')),
                // the bank and the first stone give way behind you: hopping back after stone two leads nowhere, so it ends
                trap(Touch('b'), Fall('s'), delay = 2.2f),
            ),
        ) {
            border(); floor(); pit(6..27)
            fill(1..5, 15..17, 's'); fill(8..10, 14..14, 's'); fill(13..15, 14..14, 'b'); fill(18..20, 14..14, 'c'); fill(23..26, 13..13)
            put(25, 12, 'A')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 12 — spawn on the right, door on the left; jump the spike and spikes sprout behind the landing; the next hop lands before a pit
        Level(
            name = T("Return Trip", "Rückreise"),
            intro = T("The door is on the left. I know. Unusual.", "Die Tür ist links. Ich weiß. Ungewohnt."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Airborne(19f, 21.6f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Spikes read right to left too.", "Spikes lesen auch von rechts nach links."))),
                trap(Airborne(16.4f, 18f), Fall('c')),
            ),
            // rematch: no spikes this time; the first landing swaps the controls, so the hop-hop rhythm of round 1
            // jumps back the way you came. Walk on, pressing right
            rematch = listOf(
                Round(
                    T("Rematch. Same trip, other luggage.", "Revanche. Gleiche Reise, anderes Gepäck."),
                    traps = listOf(
                        trap(Landed(16.5f, 21.6f), Play(Card.TWISTED), Swap(true),
                            Say(T("Return trip. Your keys didn't get the memo.", "Rückreise. Deine Tasten haben's nicht mitbekommen."))),
                        trap(Airborne(16.4f, 18f), Fall('c')),
                    ),
                ),
            ),
        ) {
            border(); floor()
            put(22, 14, '^')
            put(15, 14, 'A'); put(16, 14, 'A')
            fill(11..12, 15..17, 'c')
            put(29, 14, 'P'); put(2, 14, 'D')
        },

        // 13 — the ceiling spikes are a bluff; the floor is not; landing after the pit sprouts spikes before the door
        Level(
            name = T("Wednesday", "Mittwoch"),
            intro = T("It's Wednesday. I'm grumpy. That's all.", "Es ist Mittwoch. Ich habe schlechte Laune. Mehr nicht."),
            legend = mapOf('S' to ceilingSpike, 'A' to hiddenSpike),
            traps = listOf(
                trap(PastX(11.5f), Shake(0.6f), Say(T("Incoming!", "Achtung, Einschlag!"))),
                trap(Touch('a'), Play(Card.COLLAPSE), Fall('a'), Say(T("Now THAT was the real one.", "DAS war jetzt die echte.")), delay = 0.1f),
                trap(Landed(24.5f, 27f), Show('A')),
            ),
        ) {
            border(); floor()
            put(12, 1, 'S'); put(13, 1, 'S')
            fill(22..24, 15..17, 'a')
            put(27, 14, 'A'); put(28, 14, 'A')
            put(2, 14, 'P'); put(30, 14, 'D')
        },

        // 14 — a lift takes you up; spikes appear on the ledge when you arrive; a piece of the ledge crumbles when you run over it
        Level(
            name = T("Performance Review", "Mitarbeitergespräch"),
            intro = T("Please hold. Your call is important to us.", "Bitte warten. Ihr Anliegen ist uns wichtig."),
            legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(
                // the lift only leaves with somebody above it, and the floor it leaves behind goes with it
                trap(Zone(11f, 9f, 16f, 14f), Play(Card.SINKING), Move('a', 0f, -7f, 4.5f), Say(T("Ding! Next floor: pointy.", "Ding! Nächste Etage: spitz."))),
                trap(Zone(11f, 9f, 16f, 14f), Fall('f'), delay = 0.6f),
                trap(Zone(11f, 5.5f, 16f, 7.6f), Show('A')),
                trap(Touch('b'), Fall('b'), delay = 0.2f),
            ),
        ) {
            border(); floor()
            // no floor under the ledge: whoever drops through the crumbling piece falls out instead of being stranded below
            pit(17..29)
            fill(1..16, 15..17, 'f')
            fill(11..15, 14..14, 'a')
            fill(18..30, 7..7); fill(23..24, 7..7, 'b')
            put(18, 6, 'A'); put(19, 6, 'A')
            put(2, 14, 'P'); put(29, 6, 'D')
        },

        // 15 — a pit in front of you falls out from under the feet; the door hovers one tile too far left of where it will end up
        // EASTER EGG: off-by-one error (i <= n)
        Level(
            name = T("Loop", "Schleife"),
            intro = T("for (i = 0; i < n; i++)  ... All correct. Guaranteed.", "for (i = 0; i < n; i++)  ... Alles korrekt. Garantiert."),
            traps = listOf(
                trap(Touch('a'), Fall('a'), delay = 0.06f),
                trap(Airborne(24.3f, 26.2f), Play(Card.DECOY), DoorTo(30, 13, speed = 20f), Say(T("Index out of bounds. One tile to the right.", "Index außerhalb. Ein Feld weiter rechts."))),
            ),
            // rematch: everything moved one tile to the right, the hole in the floor too: the old jump lands in it
            rematch = listOf(
                Round(
                    T("Rematch. I shifted everything by one.", "Revanche. Alles um eins verschoben."),
                    traps = listOf(
                        trap(Touch('g'), Fall('g'), delay = 0.06f),
                        trap(Landed(14.5f, 17.8f), Fall('g')),
                        trap(Airborne(24.3f, 26.2f), Play(Card.DECOY), DoorTo(30, 13, speed = 20f), Say(T("Consistently off by one. That's a feature.", "Konsequent um eins daneben. Ist ein Feature."))),
                    ),
                ) { fill(15..17, 15..17, 'g') },
            ),
        ) {
            border(); floor()
            fill(13..14, 15..17, 'a')
            fill(27..30, 14..14, '^')
            put(2, 14, 'P'); put(29, 13, 'D')
        },

        // 16 — everything at once: a saw behind you, a pit, spikes behind the landing, and the door takes the ceiling with it
        Level(
            name = T("Number 16", "Die Nummer 16"),
            intro = T("Last level of this floor. Be nice to me.", "Letztes Level dieses Stockwerks. Sei nett zu mir."),
            legend = mapOf('A' to Glyph(spike = true, hidden = true)),
            traps = listOf(
                trap(PastX(3f), Play(Card.GRAND_FINALE), Saw(-1.5f, 14.4f, 6f, 0f, 0.62f), Say(T("Grand finale! Everything at once!", "Finale! Alles gleichzeitig!"))),
                trap(Touch('a'), Fall('a'), delay = 0.1f),
                trap(Landed(11.6f, 13.6f), Show('A')),
                trap(PastX(24.5f), DoorTo(20, 1, speed = 14f, hanging = true), Gravity(true), Say(T("Come and get it.", "Hol sie dir doch."))),
            ),
        ) {
            border(); floor()
            fill(9..11, 15..17, 'a')
            put(14, 14, 'A'); put(15, 14, 'A')
            put(2, 14, 'P'); put(28, 14, 'D')
        },
    )
}
