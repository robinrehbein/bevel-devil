package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Extend
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.PathSaw
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

        // 7 — a building site: a prefab piece of the upper floor drops in front of whoever runs at it (its hole waits on the way
        // back, right in front of the door), the first prefab panel sinks as you land, so does the bank behind it, and the upper
        // floor you climb to is prefab too: its panel over the pit goes the moment you stand on it
        Level(
            name = T("Prefab", "Plattenbau"),
            intro = T("They still build things properly down here.", "Hier unten baut man noch mit Qualität."),
            hint = T("Prefab panels sink the moment you land. Keep hopping. Upstairs, the piece that fell left a hole.", "Fertigteile sinken, sobald du landest. Hüpf weiter. Oben hat das Stück, das gefallen ist, ein Loch hinterlassen."),
            traps = listOf(
                trap(PastX(9.4f), Fall('c'), Say(T("Delivery! Ground floor, express.", "Lieferung! Erdgeschoss, per Express."))),
                trap(Touch('a'), Play(Card.SINKING), Move('a', 0f, 12f, 8f), Say(T("Rated for one person. Briefly.", "Zugelassen für eine Person. Kurz.")), delay = 0.45f),
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
            fill(1..28, 9..9); fill(18..20, 9..9, 'd'); fill(12..13, 9..9, 'c')
            put(1, 14, 'P'); put(11, 8, 'D')
        },

        // 8 — the door hangs from the ceiling a few steps right of the start. Hop the spike and the room turns over in mid-hop: whoever keeps flying
        // right lands in the spikes of the ceiling, so you steer back in the air; the way home is the ceiling, stalactites pop up on it,
        // and at the door Mephi turns the room around once more. Rematch: the room is upside down from the first steps and the door
        // hangs at the far end; halfway there it turns back under you
        Level(
            name = T("Down to Earth", "Bodenständig"),
            intro = T("I picked the decor myself. Nice, right?", "Die Deko habe ich selbst ausgesucht. Schön, oder?"),
            hint = T("The room turns over while you jump the spike. Steer back in the air: the far ceiling is spiked.", "Der Raum dreht sich, während du über den Stachel springst. Lenk in der Luft zurück: Die Decke dahinter ist gespickt."),
            legend = mapOf('S' to hiddenCeilingSpike),
            traps = listOf(
                trap(Airborne(19.5f, 23f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Up is the new down.", "Oben ist das neue Unten."))),
                trap(Zone(16.5f, 0.5f, 19.5f, 2.6f), Show('S'), Say(T("Mind the stalactites. They're new.", "Vorsicht, Stalaktiten. Ganz neu."))),
                trap(Zone(10f, 0.5f, 12f, 2.6f), Gravity(false), Say(T("Ah, the door. Let me get that for you.", "Ah, die Tür. Lass mich dir helfen."))),
                trap(Zone(0f, 4f, 10f, 11f), Gravity(true), Say(T("Kidding.", "War nur ein Scherz."))),
            ),
            // rematch: upside down from the first steps, the door at the far end of the ceiling; the ceiling's spikes are hopped head
            // down, and halfway the room turns back under you, over the floor spike: the way on is the floor, and at the end the room
            // turns over once more to hand you the door
            rematch = listOf(
                Round(
                    T("Rematch. I hung the room the other way round.", "Revanche. Ich habe den Raum andersrum aufgehängt."),
                    hint = T("The room turns over at once and turns back halfway. Land on the floor before the spike, not on it.", "Der Raum dreht sich sofort und dreht halbwegs zurück. Lande vor dem Stachel auf dem Boden, nicht darauf."),
                    traps = listOf(
                        trap(PastX(4f), Play(Card.UPSIDE_DOWN), Gravity(true), Say(T("Ceiling first. Floor later. Maybe.", "Erst die Decke. Der Boden später. Vielleicht."))),
                        trap(Zone(12.5f, 0.5f, 14.5f, 2.6f), Show('S'), Say(T("The stalactites moved in early.", "Die Stalaktiten sind früher eingezogen."))),
                        trap(Zone(17.5f, 0.5f, 19.5f, 2.6f), Gravity(false), Say(T("Floor's back. Mind the furniture.", "Der Boden ist zurück. Vorsicht, Möbel."))),
                        trap(Zone(26f, 9f, 29f, 15.5f), Gravity(true), Say(T("Door's upstairs. Elevator's me.", "Die Tür ist oben. Der Aufzug bin ich."))),
                    ),
                ) { put(6, 1, '.'); put(30, 1, 'D'); put(9, 1, 'v'); put(14, 1, '.'); put(16, 1, 'S') },
            ),
        ) {
            border(); floor()
            put(21, 14, '^')
            put(14, 1, 'S')
            fill(24..30, 1..1, 'v')
            put(2, 14, 'P'); put(6, 1, 'D')
        },

        // 9 — spawn on the upper deck, the door on the far right of the ground floor behind a copper wall to the ceiling, the switch
        // under the deck. A pothole opens in the deck ahead of you (the pit under it is real) and the ceiling over the spot drops a slab
        // on whoever stops to look; the keys swap as you land behind it. Down on the ground the way to the switch runs back left with
        // swapped hands over a pothole of its own and the pit; the switch swaps them back, and the way to the door is the same road with
        // the old hands
        Level(
            name = T("Potholes", "Schlaglöcher"),
            intro = T("Mind the potholes. The council is me.", "Vorsicht, Schlaglöcher. Die Stadtverwaltung bin ich."),
            hint = T("The switch is under the deck. Left and right will argue on the way there.", "Der Schalter ist unter dem Deck. Links und rechts streiten sich auf dem Weg dorthin."),
            start = listOf(Circuit('w'), Pad('1', at = 9 to 14, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(PastX(7f), Fall('a'), Fall('h'), Say(T("Pothole. Fresh from the pothole factory.", "Schlagloch. Frisch aus der Schlaglochfabrik."))),
                trap(Landed(14.5f, 18.5f), Play(Card.TWISTED), Swap(true), Say(T("Left is the new right.", "Links ist das neue Rechts."))),
                trap(Zone(18f, 12f, 24f, 15f), Fall('f'), Say(T("Another pothole. This one is a bonus.", "Noch ein Schlagloch. Das hier ist Bonus."))),
                trap(Pressed('1'), Swap(false), Say(T("Keys fixed. Door unlocked. You're welcome.", "Tasten repariert. Tür offen. Gern geschehen."))),
            ),
        ) {
            border(); floor(); pit(12..13)
            fill(1..24, 9..9); fill(12..13, 9..9, 'a')
            fill(5..7, 1..2, 'h')
            fill(19..20, 15..17, 'f')
            fill(26..27, 1..14, 'w')
            put(2, 8, 'P'); put(29, 14, 'D')
        },

        // 10 — the way home runs left along the ground floor with a concrete wall on your heels, up a stair, and back along the
        // upper floor to the door in its middle; the wall picks up speed at the stair, and a spike bed grows where the hop over the
        // step lands
        Level(
            name = T("Homeward", "Heimweg"),
            intro = T("I aired the place out just for you.", "Ich habe extra für dich gelüftet."),
            hint = T("The wall is slower than you. Don't stop to look at it.", "Die Wand ist langsamer als du. Bleib nicht stehen, um sie anzuschauen."),
            legend = mapOf('A' to hiddenSpike),
            traps = listOf(
                trap(BeforeX(24f), Play(Card.STALKER), Chase('a', speed = 3.5f, left = 28f, right = 0f), Say(T("Draft! Mind the wall.", "Zugluft! Vorsicht, die Wand."))),
                trap(Zone(1f, 9.5f, 5.2f, 13.2f), Chase('a', speed = 5f, left = 28f, right = 0f), Say(T("It's picking up speed.", "Sie nimmt Fahrt auf."))),
                trap(Airborne(8f, 11.5f), Show('A'), Say(T("Mind the carpet. It's new.", "Vorsicht, der Teppich. Ganz neu."))),
            ),
        ) {
            border(); floor()
            fill(29..30, 12..14, 'a')
            fill(3..4, 13..14); fill(1..2, 11..14)
            fill(3..30, 9..9); put(9, 8, '#'); fill(14..15, 8..8, 'A')
            put(26, 14, 'P'); put(16, 8, 'D')
        },

        // 11 — a deck over the creek: a pothole opens in it ahead of you, you drop off its end onto the left bank, and a fish leaps
        // out of the water right where you would hop: wait for it to dive. The way to the door is the creek itself: stones that
        // crumble, one of them in no time, up to the little bank in the middle of the water (the deck you came along has no bank under
        // its far end)
        Level(
            name = T("The Creek", "Bachlauf"),
            intro = T("Fresh water, fresh stones. Don't get attached.", "Frisches Wasser, frische Steine. Bind dich nicht."),
            hint = T("The fish bites whoever hops at once: let it dive. The stones don't like being stood on. Hop on, hop off.", "Der Fisch beißt alle, die sofort springen: Lass ihn abtauchen. Die Steine mögen es nicht, wenn man auf ihnen steht. Drauf, runter."),
            traps = listOf(
                trap(BeforeX(16f), Fall('d'), Say(T("Bridge works ahead. Surprise.", "Brückenarbeiten voraus. Überraschung."))),
                trap(Zone(2.2f, 13f, 4.5f, 15.5f), PathSaw(9f, 4.5f to 17.0f, 4.5f to 12.2f, 4.5f to 19.4f, loop = true), Say(T("Fresh fish. Very fresh. It bites.", "Frischer Fisch. Sehr frisch. Er beißt."))),
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), Fall('s'), Say(T("This stone is shy.", "Dieser Stein ist schüchtern.")), delay = 0.3f),
                trap(Touch('c'), Fall('c'), Say(T("Faster stones. Slower you.", "Schnellere Steine. Langsamerer du.")), delay = 0.2f),
            ),
        ) {
            border(); floor(); pit(4..18); pit(25..30)
            fill(1..3, 15..17, 's')
            fill(5..9, 15..15, 'a'); fill(12..14, 15..15); fill(17..18, 15..15, 'c')
            fill(4..30, 9..9); fill(10..11, 9..9, 'd')
            put(29, 8, 'P'); put(19, 14, 'D')
        },

        // 12 — spawn on the right, door on the left. Hop the spike and the keys swap as you land, and the spike you hopped comes
        // sliding after you: whoever stops to think is caught; the edge of the pit sinks as you come (COLLAPSE), and the last spike
        // shuffles at you as you land behind the pit: the next hops are done with swapped hands
        Level(
            name = T("Return Trip", "Rückreise"),
            intro = T("The door is on the left. I know. Unusual.", "Die Tür ist links. Ich weiß. Ungewohnt."),
            hint = T("After the first hop your keys change sides. Think fast, not long: the spike behind you is not done with you.", "Nach dem ersten Sprung wechseln die Tasten die Seite. Denk schnell, nicht lang: Der Stachel hinter dir ist noch nicht fertig mit dir."),
            legend = mapOf('M' to Glyph(spike = true), 'N' to Glyph(spike = true)),
            traps = listOf(
                trap(Landed(15f, 23.8f), Swap(true), Move('M', -10f, 0f, 4.5f), Say(T("Return trip. Your keys didn't get the memo.", "Rückreise. Deine Tasten haben's nicht mitbekommen."))),
                trap(BeforeX(15.2f), Play(Card.COLLAPSE), Move('e', 0f, 4f, 10f), Say(T("That pit is real. And it grows.", "Das Loch ist echt. Und es wächst."))),
                trap(Landed(8f, 10.9f), Move('N', 2f, 0f, 2.5f), Say(T("The last spike wants a word.", "Der letzte Stachel will dich sprechen."))),
                trap(Landed(2.5f, 7.5f), Swap(false), Say(T("Keys back. Old habits die hard.", "Tasten zur\u00fcck. Alte Gewohnheiten sterben langsam."))),
            ),
            // rematch: the keys are swapped from the first step, and a spike slides at you head-on: hop it with the swapped hands; behind
            // the pit they swap back, so the last spike is hopped with the hands round 1 had there no more
            rematch = listOf(
                Round(
                    T("Rematch. Same trip, other luggage.", "Revanche. Gleiche Reise, anderes Gepäck."),
                    hint = T("Swapped from the start, and back to normal behind the pit. The sliding spike wants a hop.", "Vertauscht ab dem Start, und hinter dem Loch wieder normal. Der rutschende Stachel will übersprungen werden."),
                    traps = listOf(
                        trap(BeforeX(28.2f), Swap(true), Move('M', 12f, 0f, 6.5f), Say(T("Keys packed already. Left is right. Again.", "Tasten schon gepackt. Links ist rechts. Wieder."))),
                        trap(BeforeX(15.2f), Play(Card.COLLAPSE), Move('e', 0f, 4f, 10f), Say(T("The pit had a growth spurt.", "Das Loch hatte einen Wachstumsschub."))),
                        trap(Landed(7f, 10.9f), Swap(false), Say(T("Unpacked early. Left is left. Surprise.", "Früh ausgepackt. Links ist links. Überraschung."))),
                        trap(Landed(7.5f, 10.9f), Move('N', 2f, 0f, 2.5f)),
                    ),
                ) { put(24, 14, '.'); put(18, 14, 'M'); put(29, 14, '.'); put(30, 14, 'P') },
            ),
        ) {
            border(); floor(); pit(11..13); put(13, 15, 'e')
            put(24, 14, 'M'); put(6, 14, 'N')
            put(29, 14, 'P'); put(2, 14, 'D')
        },

        // 13 — the room is upside down compared with the others: you start on the upper floor on the right and run left, and the ceiling
        // up there drops pieces: the first one in front of whoever runs (wait for it and hop it), the second one on whoever stops; off the
        // end of the upper floor you drop down to the ground floor and run back to the right, to the door in the middle of the room.
        // Down there spikes hang in the ceiling and the room shakes at them: a bluff. The plain bit of the ceiling is the real one, and
        // it drops in front of whoever runs
        Level(
            name = T("Wednesday", "Mittwoch"),
            intro = T("It's Wednesday. I'm grumpy. That's all.", "Es ist Mittwoch. Ich habe schlechte Laune. Mehr nicht."),
            hint = T("Don't look at the spikes. Look at the plain ceiling. Some pieces hate runners, one hates loiterers.", "Schau nicht auf die Stacheln. Schau auf die glatte Decke. Manche Stücke hassen Renner, eins hasst Herumsteher."),
            legend = mapOf('S' to ceilingSpike),
            traps = listOf(
                trap(BeforeX(26.6f), Fall('d'), Say(T("Wednesday is not over yet.", "Der Mittwoch ist noch nicht vorbei."))),
                trap(Zone(8.6f, 4f, 11f, 9.5f), Fall('e'), Say(T("Hard hat day. Sorry, no hats.", "Helmpflicht heute. Leider keine Helme."))),
                trap(Zone(5.5f, 12f, 8f, 15.5f), Shake(0.6f), Say(T("Ooh, spikes up there. Scary, huh?", "Ooh, Stacheln da oben. Gruselig, was?"))),
                trap(Zone(8.8f, 12f, 9.8f, 15.5f), Play(Card.HEADBUTT), Move('c', 0f, 4f, 6f), Say(T("Now THAT was the real one.", "DAS war jetzt die echte."))),
            ),
        ) {
            border(); floor()
            fill(5..30, 9..9)
            fill(7..8, 10..10, 'S'); fill(12..13, 10..10, 'c')
            fill(22..23, 1..2, 'd'); fill(9..10, 1..2, 'e')
            put(29, 8, 'P'); put(18, 14, 'D')
        },

        // 14 — the door is in the middle of the ground floor, right behind a wall, so take the lifts: the first one rises to the upper
        // floor and carries on into the ceiling spikes, a saw rolls at you along the floor, and the second lift goes down, and goes on
        // down into the ground; the door is back under the upper floor, and the ground floor right in front of it gives way
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
                trap(Zone(17.6f, 12f, 18.4f, 15.5f), Move('g', 0f, 12f, 14f), Say(T("Ground floor. Budget cuts.", "Erdgeschoss. Sparmaßnahmen."))),
            ),
        ) {
            border(); floor()
            fill(1..3, 15..17, 'f'); fill(4..5, 15..17, 'a'); fill(16..17, 15..17, 'g')
            fill(6..7, 10..14)
            fill(6..24, 9..9); fill(25..26, 9..9, 'b'); fill(27..30, 1..14)
            put(4, 3, 'v'); put(5, 3, 'v')
            put(3, 14, 'P'); put(15, 14, 'D')
        },

        // 15 — EASTER EGG: off-by-one error. The door stands in the middle of the floor; as you come close it goes one floor up, the
        // long way for you: over the stairs at the far right and back along the upper floor. A spiked block slams down on the floor in
        // front of whoever runs, and one on the upper floor comes down on whoever stops under it.
        // Rematch: off by one again: the lower block stays where it hangs, and the floor under it slides away as you come: whoever runs
        // on, or waits for the block like in round one, finds a hole; jump it, under the block. Upstairs the loop closes: as you
        // come close the door drops back to where it began, and the way down is a hole in the upper floor
        Level(
            name = T("Loop", "Schleife"),
            intro = T("for (i = 0; i < n; i++)  ... All correct. Guaranteed.", "for (i = 0; i < n; i++)  ... Alles korrekt. Garantiert."),
            hint = T("Follow the door. The long way round. Let the first block slam, don't stop under the second.", "Folge der Tür. Den langen Weg. Lass den ersten Block knallen, bleib nicht unter dem zweiten stehen."),
            legend = mapOf('C' to Glyph(spike = true, dir = Dir.DOWN), 'E' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = flee(PastX(7f), 14, 14, listOf(Play(Card.DECOY), Say(T("Index out of bounds. The door took the long way.", "Index außerhalb. Die Tür nimmt den langen Weg."))),
                DoorTo(14, 2, speed = 14f), DoorTo(20, 8, speed = 14f)) + listOf(
                trap(Zone(16.8f, 12f, 17.8f, 15f), Move('C', 0f, 4.2f, 6f), Say(T("Iteration one. Mind your head.", "Durchlauf eins. Kopf einziehen."))),
                trap(Landed(26.5f, 31f), DoorTo(13, 8, speed = 12f), Say(T("i++. The door counts too.", "i++. Die Tür zählt mit."))),
                trap(Zone(24f, 5f, 25f, 9.5f), Move('E', 0f, 6.2f, 9f), Say(T("Iteration two. Same head.", "Durchlauf zwei. Derselbe Kopf."))),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. I shifted everything by one.", "Revanche. Alles um eins verschoben."),
                    hint = T("The lower block stays up this time, the floor under it does not: jump the hole. The door goes up, and comes back down. The way down is the hole upstairs.", "Der untere Block bleibt diesmal oben, der Boden darunter nicht: Spring über das Loch. Die Tür geht hoch und kommt wieder runter. Der Weg nach unten ist das Loch oben."),
                    traps = flee(PastX(7f), 14, 14, listOf(Play(Card.SHY_DOOR), Say(T("Off by one. Again.", "Um eins daneben. Schon wieder."))),
                        DoorTo(14, 2, speed = 14f), DoorTo(20, 8, speed = 14f)) + listOf(
                        trap(Zone(17.2f, 12f, 18.2f, 15f), Move('f', 6f, 0f, 16f), Say(T("Off by one: the block stays. The floor under it shifts.", "Um eins verrutscht: Der Block bleibt. Der Boden darunter rutscht weg."))),
                        trap(Landed(26.5f, 31f), DoorTo(13, 8, speed = 12f)),
                        trap(Zone(24f, 5f, 25f, 9.5f), Move('E', 0f, 6.2f, 9f)),
                    ) + flee(Zone(15f, 5f, 19f, 9.5f), 13, 8, listOf(Say(T("Loop closed. The door is back at the start.", "Schleife geschlossen. Die Tür ist wieder am Anfang."))),
                        DoorTo(14, 14, speed = 14f)),
                ) { fill(9..10, 9..9, '.'); fill(21..23, 16..17, '.'); fill(21..23, 15..15, 'f') },
            ),
        ) {
            border(); floor()
            fill(27..28, 13..14); fill(29..30, 11..14)
            fill(1..28, 9..9)
            put(21, 10, 'C'); fill(24..25, 1..2, 'E')
            put(2, 14, 'P'); put(14, 14, 'D')
        },

        // 16 — the act finale, and the first room that does not end where it looks like it does. The door is locked; the switch is
        // upstairs on a ledge that gives way behind you, and a saw comes out of the wall behind you while you head for the stairs. The switch
        // opens the copper wall, and the wall behind the door with it: the door slips into a second room before you get there. On the
        // way a second saw and a slab of the ceiling; behind the breach a third saw, the stairs up (their ceiling comes down on whoever
        // stops on them), and back along a ledge whose ceiling
        // drops a piece on whoever stops
        Level(
            name = T("Number 16", "Die Nummer 16"),
            intro = T("Last level of this floor. Be nice to me.", "Letztes Level dieses Stockwerks. Sei nett zu mir."),
            hint = T("The switch is upstairs. It opens more than the copper wall: follow the door.", "Der Schalter ist oben. Er öffnet mehr als die Kupferwand: Folge der Tür."),
            rooms = 2,
            start = listOf(Circuit('w'), Pad('1', at = 3 to 8, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(BeforeX(20f), Saw(24.5f, 14.4f, -10f, 0f, 0.62f), Say(T("Housekeeping! Right behind you.", "Zimmerservice! Direkt hinter dir."))),
                trap(Touch('k'), Fall('k'), Say(T("Upstairs is rented out. The floor is not included.", "Oben ist vermietet. Der Boden ist nicht inklusive.")), delay = 0.45f),
                trap(Pressed('1'), Play(Card.ANNEX), Extend(into = 1, door = roomX(1, 10) to 8, line = T("Door's moving out. New address: next door.", "Die Tür zieht aus. Neue Adresse: nebenan."))),
                trap(Landed(0f, 3f), Saw(31.5f, 14.4f, -5.5f, 0f, 0.62f), Say(T("Second shift. Same saw.", "Zweite Schicht. Gleiche Säge.")), delay = 0.6f),
                trap(Zone(24f, 12f, 25f, 15.5f), Fall('q'), Say(T("The ceiling helps with the move.", "Die Decke hilft beim Umzug."))),
                trap(PastX(roomX(1, 5.5f)), Saw(roomX(1, 31.5f), 14.4f, -8f, 0f, 0.62f), Say(T("Second room, third saw. I like round numbers.", "Zweiter Raum, dritte Säge. Ich mag runde Zahlen."))),
                trap(Landed(roomX(1, 22.8f), roomX(1, 25.2f)), Fall('s'), Say(T("Stairs come with a lid. Keep climbing.", "Die Treppe hat einen Deckel. Weiterklettern.")), delay = 0.1f),
                trap(Zone(roomX(1, 17.5f), 5f, roomX(1, 18.5f), 9.5f), Fall('r'), Say(T("Mind the new ceiling. It's still settling.", "Vorsicht, neue Decke. Die setzt sich noch."))),
            ),
        ) {
            border(); floor()
            room(0) {
                fill(9..11, 13..14); fill(6..8, 11..11); put(3, 9, '#'); fill(4..5, 9..9, 'k')
                fill(24..25, 1..14, 'w')
                fill(28..29, 1..2, 'q')
                put(22, 14, 'P'); put(28, 14, 'D')
            }
            room(1) {
                fill(8..22, 9..9); fill(23..24, 13..14); fill(25..30, 11..14)
                fill(18..19, 1..2, 'r'); fill(23..24, 1..1, 's')
            }
        },
    )
}
