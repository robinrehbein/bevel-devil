package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Toggle
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 3, levels 1-8 (block A of the V2 rebuild, docs/LEVEL_DESIGN_V2.md §8/§11): the first copper. Every room is a
 * different shape (a descent, a pillar, two lanes, a shaft ...), and the hardware always does something other than
 * what it just taught.
 */
object World3PartA {
    val levels: List<Level> = listOf(

        // 1 — start high, door low: a descent in two floors. The deck is warm in the middle (hop it), the pad at its far end cuts
        // the live wall on the floor below, but only on a timer, and the way down lands you on a plate that has just been
        // overclocked: hop that one too, leaving it with a jump.
        Level(
            name = T("First Copper", "Erstes Kupfer"),
            intro = T("A button, a wall of live copper, a timer. Even I can explain this.", "Ein Knopf, eine Wand unter Strom, ein Timer. Das kann sogar ich erklären."),
            start = listOf(Circuit('Z'), Pad('1', at = 5 to 5, circuits = "Z", mode = PadMode.OFF)),
            traps = listOf(
                trap(BeforeX(19.5f), HeatSpike('g', 0.95f), say("Complimentary preheating.", "Kostenlose Vorheizung.")),
                trap(Pressed('1'), say("Click. The breaker is open. Do hurry.", "Klick. Die Sicherung ist offen. Beeil dich.")),
                trap(Pressed('1'), Power('Z', true), say("Breaker closed. Hope you weren't attached.", "Sicherung zu. Hoffentlich warst du nicht dran."), delay = 3.4f),
                trap(Landed(2f, 8f), Play(Card.OVERCLOCKED), HeatSpike('h', 0.95f), say("Landing pad: warm, as promised.", "Landeplatz: warm, wie versprochen.")),
            ),
            hint = T("The deck bites the runner. The wire bites the dawdler.", "Das Deck beißt den Läufer. Der Draht beißt den Trödler."),
            // rematch: the wall is gone and the button rings nothing; the warm plates moved house (hop them where they are now),
            // and the landing wakes a stalker that follows along the floor: do not stop to look back
            rematch = listOf(
                Round(
                    T("Same copper, new manners.", "Gleiches Kupfer, neue Manieren."),
                    start = listOf(Pad('1', at = 5 to 5)),
                    legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT)),
                    hint = T("The wall is gone. Something else is coming for you now.", "Die Wand ist weg. Jetzt kommt etwas anderes hinter dir her."),
                    traps = listOf(
                        trap(BeforeX(14.5f), HeatSpike('g', 0.95f), say("The warm bit moved house.", "Das Warme ist umgezogen.")),
                        trap(Pressed('1'), say("Click. Nothing. I did say: decorative.", "Klick. Nichts. Ich sagte ja: Deko.")),
                        trap(Landed(2f, 8f), Play(Card.STALKER), Chase('W', speed = 4.2f, left = 0f, right = 28f), say("Someone followed you down. Politely.", "Jemand ist dir nach unten gefolgt. Höflich.")),
                        trap(Zone(9f, 12f, 10.5f, 15f), HeatSpike('h', 1f), say("And the landing pad moved with it.", "Und der Landeplatz ist mitgezogen.")),
                        trap(Zone(21.8f, 12f, 23f, 15f), HeatSpike('k', 1f), say("One more warm bit for the road.", "Noch ein warmes Stück für unterwegs.")),
                    ),
                ) {
                    fill(15..17, 6..6); fill(6..8, 15..15)
                    fill(15..15, 7..14, '.')
                    fill(9..11, 6..6, 'g'); fill(11..13, 15..15, 'h'); fill(24..26, 15..15, 'k')
                    fill(1..1, 12..14, 'W')
                },
            ),
        ) {
            border(); floor()
            fill(3..30, 6..6)
            fill(15..17, 6..6, 'g')
            fill(6..8, 15..15, 'h')
            wire(15, 'Z', top = 7)
            spawn(30, 5); door(30, 14)
        },

        // 2 — a pillar in the middle of the room: up the stairs, over the top, down the other side. The overhead line above the
        // top lights up for a moment (wait for it at the foot of the line), a pin on the roof of the pillar walks towards
        // you the while (jump it), and the doormat in front of the door has been plugged in.
        Level(
            name = T("Live Wire", "Unter Strom"),
            intro = T("Mind the wires. They mind you.", "Achte auf die Kabel. Die achten auf dich."),
            start = listOf(Circuit('Z', on = false), Circuit('Y', on = false)),
            traps = listOf(
                trap(PastX(1.6f), Move('K', -3.5f, 0f, 2.5f), say("The welcome pin. It comes to you.", "Der Begrüßungsstift. Er kommt zu dir.")),
                trap(Landed(14f, 19f), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Overhead line: live from the second you look up.", "Oberleitung: unter Strom, sobald du hochschaust."), delay = 0.2f),
                trap(Landed(14f, 19f), Power('Z', false), delay = 1.45f),
                trap(Zone(14f, 5.6f, 19f, 7f), Move('S', -7f, 0f, 3f), say("A visiting pin. It followed you home.", "Ein Besucher-Stift. Er ist dir nachgelaufen."), delay = 0.9f),
                trap(Landed(24f, 29f), Power('Y', true), say("The doormat has been plugged in.", "Die Fußmatte wurde eingesteckt."), delay = 0.3f),
            ),
            hint = T("The wire above the pillar glows only for a moment. The pin does not stop.", "Der Draht über dem Pfeiler glüht nur kurz. Der Stift hört nicht auf."),
        ) {
            border(); floor()
            fill(8..9, 13..14); fill(10..11, 11..14); fill(12..13, 9..14)
            fill(14..22, 7..14)
            fill(19..21, 5..6, 'Z')
            put(22, 6, 'S'); put(5, 14, 'K')
            fill(28..29, 14..14, 'Y')
            spawn(1, 14); door(30, 14)
        },

        // 3 — a pit with islands, and every floor in it is on loan. A hole opens right behind the spawn (hop it), a clocked rail
        // wants you to wait for its beat, a stepping stone gives way the moment you land, the island in the middle holds for a
        // moment (wait there for the second rail, but not too long) and the last tiles before the door follow you.
        Level(
            name = T("Clock Cycle", "Taktgeber"),
            intro = T("Punctuality is a feature. Mine, not yours.", "Pünktlichkeit ist ein Feature. Meins, nicht deins."),
            start = listOf(Clock('p', on = 2.2f, off = 2.1f, phase = 2.2f), Clock('m', on = 1.8f, off = 1.6f, phase = 2.0f)),
            traps = listOf(
                trap(PastX(3.9f), Fall('g'), say("A hole. The floor had a better offer.", "Ein Loch. Der Boden hatte ein besseres Angebot."), delay = 0.2f),
                trap(Touch('s'), Play(Card.CRUMBLE), Fall('s'), say("The stone was only on loan.", "Der Stein war nur geliehen."), delay = 0.4f),
                trap(Touch('i'), Fall('i'), say("Island time is limited. Check the clock.", "Inselzeit ist begrenzt. Schau auf die Uhr."), delay = 1.8f),
            ),
            hint = T("Wait on the island. Never on a stone.", "Warte auf der Insel. Nie auf einem Stein."),
        ) {
            border(); floor()
            pit(10..27)
            fill(5..6, 15..17, 'g')
            fill(10..12, 15..15, 'p')
            fill(15..17, 15..15, 's')
            fill(20..22, 15..15, 'i')
            fill(25..27, 15..15, 'm')
            fill(28..29, 13..14); fill(30..31, 11..14)
            spawn(1, 14); door(30, 10)
        },

        // 4 — start right on a shelf, door left on top of a staircase. A fan blade rolls in from behind the moment you land, a
        // pendulum blade hangs in the middle of the floor, and the copper bridge before the stairs is cut on a timer.
        Level(
            name = T("Solid Copper", "Massives Kupfer"),
            intro = T("Nothing can go wrong here. Nothing.", "Hier kann nichts schiefgehen. Gar nichts."),
            start = listOf(Circuit('a')),
            traps = listOf(
                trap(Landed(18f, 26f), Play(Card.DEVIL_SAW), Saw(32.6f, 14.4f, -3.4f, 0f), say("Stop staring. Fan blade, from behind.", "Nicht rumstehen. Lüfterblatt, von hinten.")),
                trap(BeforeX(21.5f), PathSaw(5.5f, 17.5f to 14.4f, 17.5f to 9.5f, delay = 0.9f), say("Pendulum blade. Bring your own rhythm.", "Pendelblatt. Bring deinen eigenen Rhythmus mit.")),
                trap(BeforeX(16.5f), PathSaw(5.5f, 12.5f to 14.4f, 12.5f to 9.5f, delay = 0.7f), say("A second pendulum. Over the bridge. Obviously.", "Ein zweites Pendel. Über der Brücke. Natürlich.")),
                trap(Touch('b'), Power('a', false), say("Solid copper. Was.", "Massives Kupfer. War."), delay = 0.2f),
            ),
            hint = T("The blade behind you is slower than you. The one in front is not slow, just late.", "Das Blatt hinter dir ist langsamer als du. Das vor dir ist nicht langsam, nur spät."),
        ) {
            border(); floor()
            fill(27..30, 9..9)
            pit(11..14); fill(11..12, 15..15, 'a'); fill(13..14, 15..15, 'b')
            fill(7..8, 13..14); fill(5..6, 11..14); fill(3..4, 9..14); fill(1..2, 7..14)
            spawn(29, 8); door(1, 6)
        },

        // 5 — a ceiling that is on its way down: along the floor, up the stairs and back along the copper shelf to the door above
        // the spawn. Under the shelf the first slab falls where you want to go and turns into a step (wait for it, then
        // climb); the second only falls on whoever dawdles. Upstairs the next one waits for you again (wait, climb),
        // and the last one comes down behind you: keep moving.
        Level(
            name = T("Turnstile", "Drehkreuz"),
            intro = T("Revolving door. Very modern.", "Drehtür. Sehr modern."),
            start = listOf(Circuit('r')),
            traps = listOf(
                trap(PastX(5.0f), Play(Card.HEADBUTT), Fall('c'), say("Revolving door: now with ceiling.", "Drehtür: jetzt mit Decke."), delay = 0.5f),
                trap(PastX(14.5f), Fall('g'), say("Second door, second ceiling.", "Zweite Tür, zweite Decke."), delay = 1f),
                trap(Zone(17f, 6f, 26f, 9.3f), Fall('d'), say("Upstairs the ceiling is already waiting.", "Oben wartet die Decke schon."), delay = 0.3f),
                trap(Zone(17f, 6f, 18.5f, 9.2f), Fall('e'), say("The last one is for the door itself.", "Die letzte gehört der Tür selbst."), delay = 0.8f),
            ),
            hint = T("The ceiling is on the floor now. Floors can be jumped.", "Die Decke liegt jetzt am Boden. Über Böden kann man springen."),
        ) {
            border(); floor()
            fill(1..25, 9..9, 'r')
            fill(25..26, 13..14); fill(27..28, 11..14); fill(29..30, 9..14)
            fill(9..12, 10..11, 'c'); fill(17..20, 10..11, 'g')
            fill(17..21, 1..2); fill(17..21, 3..4, 'd')
            fill(12..14, 1..2); fill(12..14, 3..4, 'e')
            spawn(2, 14); door(2, 8)
        },

        // 6 — the door is sealed by a wall of live copper, the button that cuts it sits on a lonely island across a pit (and the
        // island sinks while you press it), the second button, in the way back to the door, puts the wall back, and the last
        // bit of floor in front of the door is on loan. Two buttons: press the one you need, hop the other.
        Level(
            name = T("Two Buttons", "Zwei Knöpfe"),
            intro = T("Press whichever you like.", "Drück, welchen du willst."),
            start = listOf(
                Circuit('w'),
                Pad('1', at = 2 to 14, circuits = "w", mode = PadMode.OFF),
                Pad('2', at = 19 to 14, circuits = "w", mode = PadMode.ON),
            ),
            traps = listOf(
                trap(Touch('p'), Play(Card.SINKING), Fall('p'), say("Button 1 undoes the wall. The island undoes itself.", "Knopf 1 räumt die Wand weg. Die Insel räumt sich selbst weg."), delay = 0.5f),
                trap(Landed(6f, 10.5f), Fall('m'), say("A hole. Handmade.", "Ein Loch. Handarbeit."), delay = 0.2f),
                trap(Landed(19.5f, 25f), Fall('q'), say("The tiles behind that button are on loan.", "Die Kacheln hinter dem Knopf sind geliehen."), delay = 0.5f),
            ),
            hint = T("Button 2 puts the wall back. Hop it.", "Knopf 2 stellt die Wand wieder hin. Hüpf drüber."),
        ) {
            border(); floor()
            pit(4..5); pit(1..3)
            fill(1..3, 15..15, 'p')
            fill(11..12, 15..17, 'm')
            fill(21..23, 15..17, 'q')
            wire(25, 'w')
            fill(26..27, 13..14); fill(28..30, 11..14)
            spawn(22, 14); door(30, 10)
        },

        // 7 — a long hall with a ceiling that comes down in pieces and two walls of copper that flicker. The first slab falls
        // behind you (keep moving), the second lands in front of you (wait for it, climb it), the third comes down behind
        // you again, and at each wall you wait for the dark on the side where no slab can reach you. Two steps up to the door.
        Level(
            name = T("Loose Contact", "Wackelkontakt"),
            intro = T("It's not a bug, it's a flicker.", "Das ist kein Fehler, das ist ein Flimmern."),
            start = listOf(Clock('Z', on = 1.2f, off = 1.2f, phase = 0.5f), Clock('Y', on = 1.2f, off = 1.2f, phase = 0.9f)),
            traps = listOf(
                trap(PastX(2.4f), Fall('a'), say("Loose ceiling. Mind the first one.", "Lose Decke. Pass auf die erste auf."), delay = 0.5f),
                trap(PastX(5.6f), Play(Card.COLLAPSE), Fall('c'), say("Observed. Collapsed.", "Beobachtet. Kollabiert."), delay = 0.35f),
                trap(PastX(12.8f), Fall('b'), say("Another one, just behind your heels.", "Noch eine, dicht hinter deinen Fersen."), delay = 0.5f),
                trap(PastX(20.4f), Fall('d'), say("The wall flickers, the ceiling does not.", "Die Wand flackert, die Decke nicht."), delay = 0.9f),
            ),
            hint = T("Wait where nothing hangs above you.", "Warte dort, wo nichts über dir hängt."),
        ) {
            border(); floor()
            fill(3..5, 1..9); fill(3..5, 10..11, 'a')
            fill(9..12, 1..9); fill(9..12, 10..11, 'c')
            fill(14..15, 1..9); fill(14..15, 10..11, 'b')
            fill(21..22, 1..9); fill(21..22, 10..11, 'd')
            wire(19, 'Z'); wire(26, 'Y')
            fill(28..29, 13..14); fill(30..30, 11..14)
            spawn(2, 14); door(30, 10)
        },

        // 8 ★ — the bridge is half there: the first half is live, the second dark, and too wide to jump. A cosmic ray swaps the two
        // the moment something lands on the first half, so you have to be gone (in the air again) when it hits.
        Level(
            name = T("Memory", "Arbeitsspeicher"),
            intro = T("ECC memory. Error-free, allegedly.", "ECC-Speicher. Angeblich fehlerfrei."),
            start = listOf(Circuit('a'), Circuit('b', on = false)),
            traps = listOf(
                trap(Landed(18f, 22f), Play(Card.BIT_FLIP), BitFlip('a', 'b'), say("Bit flip! Purely cosmic. Nothing to do with me.", "Bitkipper! Rein kosmisch. Hat nichts mit mir zu tun."), delay = 0.12f),
            ),
            hint = T("The far half only lights up when you land on the near one. Hop on, hop off. And walk the last stretch.", "Die ferne Hälfte leuchtet erst, wenn du auf der nahen landest. Drauf, runter, und das letzte Stück gehen."),
        ) {
            border(); floor()
            pit(18..26)
            fill(18..21, 15..15, 'a'); fill(22..26, 15..15, 'b')
            ceilingSpikes(27..29, 13)
            spawn(2, 14); door(30, 14)
        },
    )
}
