package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Extend
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Say
import com.robinrehbein.beveldevil.game.Action.Shake
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 1, levels 1-16. Act 1, "Die Karten": the classic tricks, one card at a time. From level 3 on the traps come
 * in chains: each next one punishes the counter the last one taught. Triggers sit on the spot (under the feet,
 * mid-jump, right after landing), and the rooms look plain: trap blocks are ordinary floor and ceiling until they fire.
 *
 * Levels 1-6 are the short tutorial. Levels 7-16 (rollout block W1-A, docs/LEVEL_DESIGN_V2.md) are rooms of 6-10 s with a
 * route of two lanes (out, up or over, and back), two to four real traps in a chain, and a different family each: panels
 * that sink, a room that turns over, a switch and swapped keys, a wall on your heels, stones that crumble, a short
 * mirrored trip, a ceiling bluff, lifts that go too far, a door that takes the long way; and level 16, the act's finale,
 * the first room that does not end where it looks like it does (U18).
 */
object World1Part1 {
    /** A hidden spike group, shown by a trap. */
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    /** A hidden spike group hanging from the ceiling. */
    private val hiddenCeilingSpike = Glyph(spike = true, dir = Dir.DOWN, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    /**
     * The door runs from you in several hops (up, across, down), so it never flies through the player. The hops are chained
     * with delays from their flight times; [first] runs with the first hop.
     */
    private fun flee(trigger: Trigger, fromCol: Int, fromRow: Int, first: List<Action>, vararg hops: DoorTo): List<Trap> {
        var x = fromCol - 0.1f
        var y = fromRow + 1f - 1.6f
        var t = 0f
        return hops.mapIndexed { i, h ->
            val trap = Trap(trigger, (if (i == 0) first else emptyList()) + h, delay = t)
            val nx = h.col - 0.1f
            val ny = h.row + 1f - 1.6f
            t += kotlin.math.hypot(nx - x, ny - y) / h.speed + 0.03f
            x = nx; y = ny
            trap
        }
    }

    val levels: List<Level> = listOf(
        // 1 — the floor in front of you collapses, and the hole grows back toward whoever stops at its edge; then the door takes two
        // steps away from you
        Level(
            name = T("Warm-up", "Aufwärmen"),
            intro = T("Welcome to the basement. Please wipe your feet.", "Willkommen im Keller. Bitte Füße abtreten."),
            traps = listOf(
                trap(PastX(17.2f), Play(Card.COLLAPSE), Fall('a'), Say(T("Floor? More of a suggestion.", "Boden? Eher ein Vorschlag."))),
                trap(PastX(17.2f), Fall('b'), Say(T("It spreads. Like gossip.", "Es breitet sich aus. Wie Tratsch.")), delay = 0.55f),
                trap(PastX(23.2f), DoorTo(29, 14, speed = 10f), Say(T("Two more steps. Promise.", "Noch zwei Schritte. Versprochen."))),
            ),
        ) {
            border(); floor()
            fill(19..21, 15..17, 'a'); fill(17..18, 15..17, 'b')
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

        // 3 — the door hops up to a ledge as you near it; a step crumbles behind you; the door comes back down as you reach the top,
        // and the broken lift comes down the shaft onto whoever stays up there
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
                trap(Zone(27.2f, 5f, 28.3f, 7f), Fall('e'), Say(T("The lift works again. Downward only.", "Der Aufzug geht wieder. Nur abwärts.")), delay = 0.5f),
            ),
        ) {
            border(); floor()
            fill(24..27, 13..13); fill(18..21, 11..11, 'b'); fill(24..27, 9..9); fill(27..30, 7..7); fill(28..30, 1..2, 'e')
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 4 — a slab of the thick ceiling drops when you pass under it, and the one above the spot where you stopped follows; wait for
        // the next and it drops while you are in the air
        Level(
            name = T("House Rules", "Hausordnung"),
            intro = T("The floor here is a bit shaky. Watch your feet.", "Der Boden hier ist etwas wackelig. Schau auf deine Füße."),
            traps = listOf(
                trap(PastX(13.1f), Play(Card.HEADBUTT), Fall('c'), Say(T("Ceiling delivery!", "Deckenlieferung!"))),
                trap(PastX(13.1f), Fall('b'), Say(T("And one for whoever waits.", "Und eins für alle, die warten.")), delay = 0.9f),
                trap(Landed(17f, 20.6f), Fall('d'), Say(T("Again? Again.", "Nochmal? Nochmal."))),
                trap(Airborne(22f, 24f), Fall('e')),
            ),
            // rematch: the slab you learned to wait for only rattles, and the one above the spot where you waited falls; the one you
            // run under next falls for real, and the one above you after it, a moment later
            rematch = listOf(
                Round(
                    T("Rematch. Same ceiling, new grudge.", "Revanche. Gleiche Decke, frischer Groll."),
                    traps = listOf(
                        trap(PastX(13.1f), Shake(0.5f), Say(T("Flinched. Cute.", "Gezuckt. Niedlich."))),
                        trap(PastX(13.1f), Fall('b'), delay = 0.45f),
                        trap(PastX(18.2f), Play(Card.HEADBUTT), Fall('d'), Say(T("This one means it.", "Die hier meint es ernst."))),
                        trap(PastX(18.2f), Fall('g'), Say(T("Don't admire it. Jump it.", "Nicht bewundern. Drüber.")), delay = 1.5f),
                    ),
                ) { fill(18..20, 5..6, 'g') },
            ),
        ) {
            border(); floor()
            fill(1..30, 1..6)
            fill(12..13, 5..6, 'b'); fill(14..16, 5..6, 'c'); fill(21..23, 5..6, 'd'); fill(26..28, 5..6, 'e')
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
            // rematch: the floor holds now, but the brother saw comes from the front at once: the saws close in from both sides, so
            // you wait for the front one and jump it on the spot. Whoever jumps it running lands where spikes grow
            rematch = listOf(
                Round(
                    T("Rematch. Same saw, new manners.", "Revanche. Gleiche Säge, neue Manieren."),
                    hint = T("Let the front saw come to you and jump it on the spot. Forward is pointy.", "Lass die vordere Säge zu dir kommen und spring auf der Stelle. Vorne ist es spitz."),
                    traps = listOf(
                        trap(PastX(5f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6.5f, 0f, 0.62f), Saw(33.5f, 14.4f, -9f, 0f, 0.62f),
                            Say(T("Back for seconds. Both of them.", "Nachschlag gefällig? Beide."))),
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

        // 7 — a building site: the first prefab panel sinks as you land, so does the bank behind it, and the upper floor you
        // climb to is prefab too: its panel over the pit goes the moment you stand on it
        Level(
            name = T("Prefab", "Plattenbau"),
            intro = T("They still build things properly down here.", "Hier unten baut man noch mit Qualität."),
            hint = T("Prefab panels sink the moment you land. Keep hopping.", "Fertigteile sinken, sobald du landest. Hüpf weiter."),
            traps = listOf(
                trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, 12f, 8f), Say(T("Rated for one person. Briefly.", "Zugelassen für eine Person. Kurz.")), delay = 0.5f),
                trap(Touch('k'), Move('k', 0f, 12f, 8f), Say(T("The ground floor is optional too.", "Das Erdgeschoss ist auch optional.")), delay = 0.5f),
                // hopping back off the first panel strands nobody: the start floor gives way a moment after it sank
                trap(Touch('a'), Fall('s'), delay = 2.2f),
                trap(Touch('d'), Move('d', 0f, 12f, 9f), Say(T("Upper floor. Same panels, better view.", "Obergeschoss. Gleiche Platten, bessere Aussicht.")), delay = 0.5f),
            ),
        ) {
            border(); floor(); pit(17..22)
            fill(1..16, 15..17, 's')
            fill(18..21, 15..15, 'a'); fill(23..30, 15..15, 'k')
            fill(27..28, 13..14); fill(29..30, 11..14)
            fill(1..28, 9..9); fill(18..20, 9..9, 'd')
            put(2, 14, 'P'); put(2, 8, 'D')
        },

        // 8 — the door hangs from the ceiling above the start. Jump the spikes, and the room turns over: the way home is the
        // ceiling, all the way back; stalactites pop up on it; at the door Mephi turns the room around once more
        Level(
            name = T("Down to Earth", "Bodenständig"),
            intro = T("I picked the decor myself. Nice, right?", "Die Deko habe ich selbst ausgesucht. Schön, oder?"),
            hint = T("The door is on the ceiling. Somebody has to turn you over first.", "Die Tür hängt an der Decke. Erst muss dich jemand umdrehen."),
            legend = mapOf('S' to hiddenCeilingSpike),
            traps = listOf(
                trap(PastX(25.2f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Up is the new down.", "Oben ist das neue Unten."))),
                trap(Zone(16.5f, 0.5f, 19.5f, 2.6f), Show('S'), Say(T("Mind the stalactites. They're new.", "Vorsicht, Stalaktiten. Ganz neu."))),
                trap(Zone(6f, 0.5f, 8f, 2.6f), Gravity(false), Say(T("Ah, the door. Let me get that for you.", "Ah, die Tür. Lass mich dir helfen."))),
                trap(Zone(0f, 4f, 6f, 11f), Gravity(true), Say(T("Kidding.", "War nur ein Scherz."))),
            ),
            // rematch: Mephi mirrored the room: you start on the right and the door hangs over the start; walking in turns nothing over
            // any more, only a jump near the left end does, and the way back along the ceiling leads to the right
            rematch = listOf(
                Round(
                    T("Rematch. I mirrored the room. Gravity is opt-in now.", "Revanche. Ich habe den Raum gespiegelt. Schwerkraft nur noch auf Antrag."),
                    hint = T("The room is mirrored: you start on the right, and the door hangs over the start. Gravity only turns when you jump at the left end.", "Der Raum ist gespiegelt: Du startest rechts, und die T\u00fcr h\u00e4ngt \u00fcber dem Start. Die Schwerkraft dreht sich nur, wenn du am linken Ende springst."),
                    traps = listOf(
                        trap(Airborne(1f, 5f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Jumpers only.", "Nur f\u00fcr Springer."))),
                        trap(Zone(15.5f, 0.5f, 18.5f, 2.6f), Show('S')),
                        trap(Zone(23f, 0.5f, 25f, 2.6f), Gravity(false), Say(T("Here you go. The door. Again.", "Bitte sehr. Die T\u00fcr. Schon wieder."))),
                        trap(Zone(25f, 4f, 31f, 11f), Gravity(true), Say(T("Still kidding.", "Immer noch ein Scherz."))),
                    ),
                ) { put(2, 14, '.'); put(2, 1, '.'); put(29, 14, 'P'); put(29, 1, 'D'); put(14, 1, '.'); put(21, 1, 'S') },
            ),
        ) {
            border(); floor()
            put(21, 14, '^')
            put(14, 1, 'S')
            put(2, 14, 'P'); put(2, 1, 'D')
        },

        // 9 — spawn on the upper floor, the door downstairs behind a copper wall, the switch down on the right. A pothole
        // opens in the deck ahead of you (the pit under it is real), the keys swap as you land behind it, and they swap
        // back on the switch; the way home along the ground floor has a pothole of its own
        Level(
            name = T("Potholes", "Schlaglöcher"),
            intro = T("Mind the potholes. The council is me.", "Vorsicht, Schlaglöcher. Die Stadtverwaltung bin ich."),
            hint = T("The switch is down on the right. Left and right will argue on the way.", "Der Schalter ist unten rechts. Links und rechts streiten sich unterwegs."),
            start = listOf(Circuit('w'), Pad('1', at = 29 to 14, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(PastX(7f), Fall('a'), Say(T("Pothole. Fresh from the pothole factory.", "Schlagloch. Frisch aus der Schlaglochfabrik."))),
                trap(Landed(14.5f, 18.5f), Play(Card.TWISTED), Swap(true), Say(T("Left is the new right.", "Links ist das neue Rechts."))),
                trap(Pressed('1'), Swap(false), Say(T("Keys fixed. Door unlocked. You're welcome.", "Tasten repariert. Tür offen. Gern geschehen."))),
                trap(Zone(18f, 12f, 24f, 15f), Fall('f'), Say(T("Another pothole. This one is a bonus.", "Noch ein Schlagloch. Das hier ist Bonus."))),
            ),
        ) {
            border(); floor(); pit(12..13)
            fill(1..24, 9..9); fill(12..13, 9..9, 'a')
            fill(19..20, 15..17, 'f')
            fill(4..5, 11..14, 'w')
            put(2, 8, 'P'); put(2, 14, 'D')
        },

        // 10 — the way home runs left along the ground floor with a concrete wall on your heels, up a stair, and back along the
        // upper floor; the wall picks up speed at the stair, and a spike bed grows where the hop over the step lands
        Level(
            name = T("Homeward", "Heimweg"),
            intro = T("I aired the place out just for you.", "Ich habe extra für dich gelüftet."),
            hint = T("The wall is slower than you. Don't stop to look at it.", "Die Wand ist langsamer als du. Bleib nicht stehen, um sie anzuschauen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(BeforeX(24f), Play(Card.STALKER), Chase('a', speed = 3.5f, left = 28f, right = 0f), Say(T("Draft! Mind the wall.", "Zugluft! Vorsicht, die Wand."))),
                trap(Zone(1f, 11f, 8f, 15f), Chase('a', speed = 5f, left = 28f, right = 0f), Say(T("It's picking up speed.", "Sie nimmt Fahrt auf."))),
                trap(Airborne(8f, 11.5f), Show('A'), Say(T("Mind the carpet. It's new.", "Vorsicht, der Teppich. Ganz neu."))),
            ),
        ) {
            border(); floor()
            fill(29..30, 12..14, 'a')
            fill(3..4, 13..14); fill(1..2, 11..14)
            fill(3..30, 9..9); put(9, 8, '#'); fill(14..15, 8..8, 'A')
            put(26, 14, 'P'); put(29, 8, 'D')
        },

        // 11 — a deck over the creek: a pothole opens in it ahead of you, you drop off its end onto the left bank where the
        // ceiling drops a delivery, and the way home is the creek itself: stones that crumble, one of them in no time
        Level(
            name = T("The Creek", "Bachlauf"),
            intro = T("Fresh water, fresh stones. Don't get attached.", "Frisches Wasser, frische Steine. Bind dich nicht."),
            hint = T("The stones don't like being stood on. Hop on, hop off.", "Die Steine mögen es nicht, wenn man auf ihnen steht. Drauf, runter."),
            traps = listOf(
                trap(BeforeX(16f), Fall('d'), Say(T("Bridge works ahead. Surprise.", "Brückenarbeiten voraus. Überraschung."))),
                trap(Landed(0f, 4.5f), Fall('h'), Say(T("Special delivery from upstairs.", "Spezialzustellung von oben.")), delay = 0.25f),
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), Fall('s'), Say(T("This stone is shy.", "Dieser Stein ist schüchtern.")), delay = 0.3f),
                trap(Touch('c'), Fall('c'), Say(T("Faster stones. Slower you.", "Schnellere Steine. Langsamerer du.")), delay = 0.12f),
            ),
        ) {
            border(); floor(); pit(4..21)
            fill(1..3, 15..17, 's')
            fill(5..9, 15..15, 'a'); fill(12..14, 15..15); fill(17..19, 15..15, 'c')
            fill(4..30, 9..9); fill(10..11, 9..9, 'd'); fill(1..3, 1..3, 'h')
            put(28, 8, 'P'); put(28, 14, 'D')
        },

        // 12 — spawn on the right, door on the left. Hop the spike and the keys swap as you land; the next hop and the next spike
        // are done with swapped hands
        Level(
            name = T("Return Trip", "Rückreise"),
            intro = T("The door is on the left. I know. Unusual.", "Die Tür ist links. Ich weiß. Ungewohnt."),
            hint = T("After the first hop your keys change sides. Think before you press.", "Nach dem ersten Sprung wechseln die Tasten die Seite. Erst denken, dann drücken."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(Landed(15f, 23.8f), Play(Card.TWISTED), Swap(true), Say(T("Return trip. Your keys didn't get the memo.", "Rückreise. Deine Tasten haben's nicht mitbekommen."))),
                trap(BeforeX(13.5f), Say(T("That pit is real. Unlike my promises.", "Das Loch ist echt. Anders als meine Versprechen."))),
                trap(Landed(2.5f, 7.5f), Swap(false), Say(T("Keys back. Old habits die hard.", "Tasten zur\u00fcck. Alte Gewohnheiten sterben langsam."))),
            ),
            // rematch: the keys are swapped from the first steps, and the spike you hop with them grows where you land
            rematch = listOf(
                Round(
                    T("Rematch. Same trip, other luggage.", "Revanche. Gleiche Reise, anderes Gepäck."),
                    traps = listOf(
                        trap(BeforeX(28.2f), Swap(true), Say(T("Keys packed already. Left is right. Again.", "Tasten schon gepackt. Links ist rechts. Wieder."))),
                        trap(Landed(4f, 10.6f), Play(Card.SPIKE_SEED), Show('A'), Say(T("Souvenirs grow where you land.", "Souvenirs wachsen, wo du landest."))),
                    ),
                ) { put(24, 14, '.'); put(6, 14, 'A'); put(29, 14, '.'); put(30, 14, 'P') },
            ),
        ) {
            border(); floor(); pit(11..12)
            put(24, 14, '^'); put(6, 14, '^')
            put(29, 14, 'P'); put(2, 14, 'D')
        },

        // 13 — the room is upside down compared with the others: you start on the upper floor on the right and run left, and the ceiling
        // up there drops pieces as you pass under them (one on whoever stops, one more further on); off the end of the upper floor you drop
        // down to the ground floor and run back to the right, to the door in the bottom right corner. Down there spikes hang in the
        // ceiling and the room shakes at them: a bluff. The plain bits of the ceiling are the real ones: one drops as you pass
        Level(
            name = T("Wednesday", "Mittwoch"),
            intro = T("It's Wednesday. I'm grumpy. That's all.", "Es ist Mittwoch. Ich habe schlechte Laune. Mehr nicht."),
            hint = T("Don't look at the spikes. Look at the plain ceiling.", "Schau nicht auf die Stacheln. Schau auf die glatte Decke."),
            legend = mapOf('S' to ceilingSpike),
            traps = listOf(
                trap(Zone(21.6f, 4f, 24f, 9.5f), Fall('d'), Say(T("Wednesday is not over yet.", "Der Mittwoch ist noch nicht vorbei."))),
                trap(Zone(8.6f, 4f, 11f, 9.5f), Fall('e'), Say(T("Hard hat day. Sorry, no hats.", "Helmpflicht heute. Leider keine Helme."))),
                trap(Zone(5.5f, 12f, 8f, 15.5f), Shake(0.6f), Say(T("Ooh, spikes up there. Scary, huh?", "Ooh, Stacheln da oben. Gruselig, was?"))),
                trap(Zone(13f, 12f, 14f, 15f), Play(Card.HEADBUTT), Fall('c'), Say(T("Now THAT was the real one.", "DAS war jetzt die echte."))),
            ),
        ) {
            border(); floor()
            fill(5..30, 9..9)
            fill(7..8, 10..10, 'S'); fill(12..13, 10..10, 'c')
            fill(22..23, 1..2, 'd'); fill(9..10, 1..2, 'e')
            put(29, 8, 'P'); put(29, 14, 'D')
        },

        // 14 — the door is right behind a wall, so take the lifts: the first one rises to the upper floor and carries on into the
        // ceiling spikes, a saw rolls at you along the floor, and the second lift goes down, and goes on down into the ground
        Level(
            name = T("Performance Review", "Mitarbeitergespräch"),
            intro = T("Please hold. Your call is important to us.", "Bitte warten. Ihr Anliegen ist uns wichtig."),
            hint = T("Lifts go too far. Step off when the floor lines up.", "Aufzüge fahren zu weit. Steig aus, wenn der Boden passt."),
            traps = listOf(
                // the lift only leaves with somebody above it, and the floor it leaves behind goes with it
                trap(Zone(4f, 12f, 6f, 15f), Move('a', 0f, -11f, 4.2f), Say(T("Ding! Next floor: pointy.", "Ding! Nächste Etage: spitz."))),
                trap(Zone(4f, 12f, 6f, 15f), Move('f', 0f, 12f, 8f), delay = 0.5f),
                trap(PastX(10f), Play(Card.DEVIL_SAW), Saw(26.5f, 8.4f, -5.5f, 0f, 0.62f), Say(T("Feedback is a gift. This one rolls.", "Feedback ist ein Geschenk. Dieses rollt."))),
                trap(Zone(25f, 7f, 27f, 9.5f), Move('b', 0f, 6.5f, 4f), Say(T("Going down. Further than you asked.", "Abwärts. Weiter, als du wolltest."))),
            ),
        ) {
            border(); floor()
            fill(1..3, 15..17, 'f'); fill(4..5, 15..17, 'a')
            fill(6..7, 10..14)
            fill(6..24, 9..9); fill(25..26, 9..9, 'b'); fill(27..30, 1..14)
            put(4, 3, 'v'); put(5, 3, 'v')
            put(3, 14, 'P'); put(9, 14, 'D')
        },

        // 15 — EASTER EGG: off-by-one error. The door stands in the middle of the floor; as you come close it takes the long way
        // to the top left, over the stairs at the far right. Spiked blocks come down on the floor and on the upper floor.
        // Rematch: the upper floor ends early and the door comes back to where it began (the loop closes)
        Level(
            name = T("Loop", "Schleife"),
            intro = T("for (i = 0; i < n; i++)  ... All correct. Guaranteed.", "for (i = 0; i < n; i++)  ... Alles korrekt. Garantiert."),
            hint = T("Follow the door. The long way round.", "Folge der Tür. Den langen Weg."),
            legend = mapOf('C' to Glyph(spike = true, dir = Dir.DOWN), 'E' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = flee(PastX(7f), 14, 14, listOf(Play(Card.DECOY), Say(T("Index out of bounds. The door took the long way.", "Index außerhalb. Die Tür nimmt den langen Weg."))),
                DoorTo(14, 2, speed = 14f), DoorTo(2, 8, speed = 14f)) + listOf(
                trap(Zone(22f, 12f, 23f, 15f), Move('C', 0f, 4.2f, 9f), Say(T("Iteration one. Mind your head.", "Durchlauf eins. Kopf einziehen."))),
                trap(Zone(22f, 5f, 23f, 9.5f), Move('E', 0f, 6.2f, 9f), Say(T("Iteration two. Same head.", "Durchlauf zwei. Derselbe Kopf."))),
            ),
            // rematch: the door runs to the top left again, and when you get close it runs all the way back; the upper floor
            // has a hole at its end now, so the way back down is a fall
            rematch = listOf(
                Round(
                    T("Rematch. I shifted everything by one.", "Revanche. Alles um eins verschoben."),
                    traps = flee(PastX(7f), 14, 14, listOf(Play(Card.SHY_DOOR), Say(T("Off by one. Again.", "Um eins daneben. Schon wieder."))),
                        DoorTo(14, 2, speed = 14f), DoorTo(2, 8, speed = 14f)) + listOf(
                        trap(Zone(22f, 12f, 23f, 15f), Move('C', 0f, 4.2f, 9f)),
                        trap(Zone(22f, 5f, 23f, 9.5f), Move('E', 0f, 6.2f, 9f)),
                    ) + flee(Zone(1f, 5f, 9f, 9.5f), 2, 8, listOf(Say(T("Loop closed. The door is back at the start.", "Schleife geschlossen. Die Tür ist wieder am Anfang."))),
                        DoorTo(2, 2, speed = 14f), DoorTo(12, 2, speed = 14f), DoorTo(12, 14, speed = 14f)),
                ) { fill(1..4, 9..9, '.') },
            ),
        ) {
            border(); floor()
            fill(27..28, 13..14); fill(29..30, 11..14)
            fill(1..28, 9..9)
            fill(21..22, 10..10, 'C'); fill(22..23, 1..2, 'E')
            put(2, 14, 'P'); put(14, 14, 'D')
        },

        // 16 — the act finale, and the first room that does not end where it looks like it does. The door is locked; the switch is
        // upstairs on a ledge that gives way behind you; a saw rolls in on the way back down. At the door the wall breaks open,
        // the door slips into a second room, and there is another saw
        Level(
            name = T("Number 16", "Die Nummer 16"),
            intro = T("Last level of this floor. Be nice to me.", "Letztes Level dieses Stockwerks. Sei nett zu mir."),
            hint = T("The switch is upstairs. The room is bigger than it looks.", "Der Schalter ist oben. Der Raum ist größer, als er aussieht."),
            rooms = 2,
            start = listOf(Circuit('w'), Pad('1', at = 3 to 8, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(Touch('k'), Fall('k'), Say(T("Upstairs is rented out. The floor is not included.", "Oben ist vermietet. Der Boden ist nicht inklusive.")), delay = 0.45f),
                trap(Landed(0f, 3f), Saw(31.5f, 14.4f, -5.5f, 0f, 0.62f), Say(T("Housekeeping! Mind the saw.", "Zimmerservice! Vorsicht, Säge.")), delay = 0.6f),
                trap(AtDoor, Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 28) to 14)),
                trap(PastX(roomX(1, 4f)), Saw(roomX(1, 31.5f), 14.4f, -6f, 0f, 0.62f), Say(T("Second room, second saw. I like symmetry.", "Zweiter Raum, zweite Säge. Ich mag Symmetrie."))),
            ),
        ) {
            border(); floor()
            room(0) {
                fill(9..11, 13..14); fill(6..8, 11..11); put(3, 9, '#'); fill(4..5, 9..9, 'k')
                fill(24..25, 1..14, 'w')
                put(22, 14, 'P'); put(28, 14, 'D')
            }
            room(1) { pit(12..13) }
        },
    )
}
