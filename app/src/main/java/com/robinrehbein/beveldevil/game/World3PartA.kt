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

        // 1 — the first copper, and everything is wired: start high on a deck, the door on a shelf low in the middle. A copper inlay
        // in the deck goes live under whoever stops on it (keep moving), the button at the far end cuts the live wall below, and the
        // rail under your feet with it (breaker open: for everything), the strip on the floor where you land is switched on (hop
        // it), and the wall closes again on a timer: run through, up the step at the far end, back left along the shelf.
        Level(
            name = T("First Copper", "Erstes Kupfer"),
            intro = T("A button, a live wall, a timer. Even I can explain this.", "Ein Knopf, eine Stromwand, ein Timer. Sogar ich erkläre das."),
            start = listOf(Circuit('Z'), Circuit('Q', on = false), Circuit('Y', on = false), Circuit('d'), Pad('1', at = 12 to 5, circuits = "Z", mode = PadMode.OFF)),
            traps = listOf(
                trap(BeforeX(27.6f), Play(Card.SHORT_CIRCUIT), Power('Q', true), say("Copper inlay. Live, for the connoisseur.", "Kupfereinlage. Unter Strom, für Kenner."), delay = 0.2f),
                trap(Pressed('1'), say("Click. The wall is dark. For now.", "Klick. Die Wand ist aus. Vorerst.")),
                trap(Zone(5f, 3f, 10.6f, 6f), Power('d', false), say("Breaker open. For everything. Sorry.", "Sicherung raus. Für alles. Sorry."), delay = 0.25f),
                trap(Landed(1f, 11.5f), Power('Y', true), say("Welcome downstairs. The floor is on, too.", "Willkommen unten. Der Boden ist auch an."), delay = 0.85f),
                trap(Zone(9f, 12f, 12f, 15f), Power('Z', true), say("Breaker closed. Hope you weren't attached.", "Sicherung zu. Hoffentlich warst du nicht dran."), delay = 1.5f),
            ),
            hint = T("Press, fall, hop, run. The wall does not wait, the shelf is behind you.", "Drücken, fallen, hüpfen, rennen. Die Wand wartet nicht, das Regal liegt hinter dir."),
            // rematch: the wall is dark now and the button switches it ON: hop the button. The inlay moved, a stalker follows you
            // down, the wall comes on by itself if you dawdle at its foot, and the shelf rail is cut under your feet
            rematch = listOf(
                Round(
                    T("Same copper, new manners.", "Gleiches Kupfer, neue Manieren."),
                    start = listOf(Circuit('Z', on = false), Circuit('Q', on = false), Circuit('d'), Circuit('k'), Pad('1', at = 12 to 5, circuits = "Z", mode = PadMode.ON)),
                    legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT)),
                    hint = T("Do not touch the button this time. And something follows you down.", "Diesmal den Knopf nicht anfassen. Und dir folgt etwas nach unten."),
                    traps = listOf(
                        trap(Zone(16f, 3f, 19f, 6f), Power('Q', true), say("The inlay moved. Copper gets around.", "Die Einlage ist umgezogen. Kupfer kommt rum."), delay = 0.45f),
                        trap(Pressed('1'), say("Click. You switched the wall ON. Thank you.", "Klick. Du hast die Wand EINgeschaltet. Danke.")),
                        trap(Zone(5.5f, 3f, 10.5f, 6f), Power('d', false), say("No button needed. The rail drops on its own now.", "Ohne Knopf. Die Schiene fällt jetzt von allein."), delay = 0.2f),
                        trap(Zone(1f, 12f, 11.5f, 15f), Play(Card.STALKER), Chase('W', speed = 4.6f, left = 0f, right = 6f), say("Someone followed you down. Politely.", "Jemand ist dir nach unten gefolgt. Höflich.")),
                        trap(Zone(9.5f, 12f, 15f, 15f), Power('Z', true), say("The wall noticed you. Late, but it did.", "Die Wand hat dich bemerkt. Spät, aber doch."), delay = 1.4f),
                        trap(Zone(22f, 9f, 26.5f, 11f), Power('k', false), say("The shelf was on loan from the same breaker.", "Das Regal hing an derselben Sicherung."), delay = 0.6f),
                    ),
                ) {
                    fill(21..21, 5..5, '.'); fill(16..18, 5..5, 'Q'); fill(4..7, 14..14, '.')
                    fill(22..24, 11..11, 'k')
                    fill(1..1, 12..14, 'W')
                },
            ),
        ) {
            border(); floor()
            fill(3..30, 6..6)
            fill(5..10, 6..6, 'd')
            fill(21..21, 5..5, 'Q')
            fill(15..17, 7..14, 'Z')
            fill(4..7, 14..14, 'Y')
            fill(27..30, 13..14)
            fill(18..25, 11..11)
            spawn(28, 5); door(19, 10)
        },

        // 2 — a pillar in the middle of the room: up the stairs, over the top, down the other side, and every pin in the room moves.
        // One walks to meet you at the start (jump it), the overhead line above the top lights up as you arrive (wait for it at
        // its foot), a pin on the roof of the pillar walks towards you the while (jump it), and one slides out of the pillar's
        // foot behind you on the far side while the doormat is plugged in (do not stop, hop the mat).
        Level(
            name = T("Live Wire", "Unter Strom"),
            intro = T("Mind the wires. They mind you.", "Achte auf die Kabel. Die achten auf dich."),
            start = listOf(Circuit('Z', on = false), Circuit('Y', on = false)),
            traps = listOf(
                trap(PastX(2.6f), Move('K', -3.5f, 0f, 2.5f), say("The welcome pin. It comes to you.", "Der Begrüßungsstift. Er kommt zu dir.")),
                trap(Landed(11.6f, 19f), Play(Card.BIOS), Power('Z', true), say("Overhead line: live from the second you look up.", "Oberleitung: unter Strom, sobald du hochschaust."), delay = 0.1f),
                trap(Landed(11.6f, 19f), Power('Z', false), delay = 1.3f),
                trap(Zone(12f, 7.6f, 19f, 9f), Move('S', -10f, 0f, 3f), say("A visiting pin. It followed you home.", "Ein Besucher-Stift. Er ist dir nachgelaufen."), delay = 0.9f),
                trap(Landed(23.5f, 29f), Move('R', 5f, 0f, 5.5f), say("A pin from the wall. It wants the door too.", "Ein Stift aus der Wand. Er will auch zur Tür."), delay = 0.15f),
                trap(Landed(23.5f, 29f), Power('Y', true), say("And the doormat has been plugged in.", "Und die Fußmatte wurde eingesteckt."), delay = 0.3f),
            ),
            hint = T("The wire above the pillar glows only for a moment. The pins never stop.", "Der Draht über dem Pfeiler glüht nur kurz. Die Stifte hören nie auf."),
        ) {
            border(); floor()
            fill(8..9, 13..14); fill(10..11, 11..14)
            fill(12..22, 9..14)
            fill(19..21, 7..8, 'Z')
            put(22, 8, 'S'); put(5, 14, 'K'); put(23, 14, 'R')
            fill(28..29, 14..14, 'Y')
            spawn(1, 14); door(30, 14)
        },

        // 3 — a pit with islands, and every floor in it is on loan. A hole opens right behind the spawn (hop it), a clocked rail
        // wants you to wait for its beat at the edge, and the edge only waits for one beat, a stepping stone gives way the moment
        // you land, the island in the middle holds for a moment (wait there for the second rail, but not too long).
        Level(
            name = T("Clock Cycle", "Taktgeber"),
            intro = T("Punctuality is a feature. Mine, not yours.", "Pünktlichkeit ist ein Feature. Meins, nicht deins."),
            start = listOf(Clock('p', on = 2.2f, off = 2.1f, phase = 2.2f), Clock('m', on = 1.8f, off = 1.6f, phase = 2.0f)),
            traps = listOf(
                trap(PastX(3.9f), Fall('g'), say("A hole. The floor had a better offer.", "Ein Loch. Der Boden hatte ein besseres Angebot."), delay = 0.2f),
                trap(PastX(8.0f), Fall('w'), say("The edge waits one beat. Not two.", "Die Kante wartet einen Takt. Nicht zwei."), delay = 1.75f),
                trap(Touch('s'), Play(Card.CRUMBLE), Fall('s'), say("The stone was only on loan.", "Der Stein war nur geliehen."), delay = 0.4f),
                trap(Touch('i'), Fall('i'), say("Island time is limited. Check the clock.", "Inselzeit ist begrenzt. Schau auf die Uhr."), delay = 1.8f),
            ),
            hint = T("Wait on the island. Never on a stone.", "Warte auf der Insel. Nie auf einem Stein."),
        ) {
            border(); floor()
            pit(10..27)
            fill(5..6, 15..17, 'g')
            fill(7..9, 15..17, 'w')
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
            fill(7..8, 13..14); fill(5..6, 11..14); fill(1..4, 9..14)
            spawn(29, 8); door(1, 8)
        },

        // 5 — a ceiling that is on its way down: along the floor, up the stairs and back along the copper shelf to the door above
        // the spawn. Under the shelf the first slab falls where you want to go and turns into a step (wait for it, then
        // climb); the second only falls on whoever dawdles. The middle stair is copper and goes dark under you (keep climbing).
        // Upstairs the next slab waits for you again (wait, climb), and the last one comes down behind you: keep moving.
        Level(
            name = T("Turnstile", "Drehkreuz"),
            intro = T("Revolving door. Very modern.", "Drehtür. Sehr modern."),
            start = listOf(Circuit('r'), Circuit('q')),
            traps = listOf(
                trap(PastX(5.0f), Play(Card.HEADBUTT), Fall('c'), say("Revolving door: now with ceiling.", "Drehtür: jetzt mit Decke."), delay = 0.4f),
                trap(PastX(14.5f), Fall('g'), say("Second door, second ceiling.", "Zweite Tür, zweite Decke."), delay = 1f),
                trap(Landed(27.1f, 28.9f), Power('q', false), say("The middle step is copper. Was.", "Die mittlere Stufe ist aus Kupfer. War."), delay = 0.55f),
                trap(Landed(27.1f, 28.9f), Power('q', true), delay = 2.6f),
                trap(Zone(17f, 6f, 26f, 9.3f), Fall('d'), say("Upstairs the ceiling is already waiting.", "Oben wartet die Decke schon."), delay = 0.3f),
                trap(Zone(11.5f, 6f, 13.3f, 9.2f), Fall('e'), say("The last one is for the door itself. And for you, if you stop.", "Die letzte gehört der Tür. Und dir, wenn du anhältst.")),
            ),
            hint = T("The ceiling is on the floor now. Floors can be jumped.", "Die Decke liegt jetzt am Boden. Über Böden kann man springen."),
            // rematch: the habits are reversed. What waited for you now drops after you passed, what fell on whoever dawdled is
            // now the one to wait for: wait where you used to run, run where you used to wait
            rematch = listOf(
                Round(
                    T("Same ceiling, different habits.", "Gleiche Decke, andere Gewohnheiten."),
                    hint = T("Wait where you ran last time. Run where you waited.", "Warte, wo du zuletzt gerannt bist. Renn, wo du gewartet hast."),
                    traps = listOf(
                        trap(PastX(5.0f), Fall('c'), say("This one takes its time. Don't wait for it.", "Die hier lässt sich Zeit. Warte nicht auf sie."), delay = 3f),
                        trap(PastX(17.0f), Play(Card.HEADBUTT), Fall('g'), say("Headbutt, the sequel. Starring this door.", "Kopfnuss, die Fortsetzung. Mit dieser Tür in der Hauptrolle."), delay = 0.3f),
                        trap(Landed(27.1f, 28.9f), Power('q', false), say("The copper step clocked out early today.", "Die Kupferstufe hat heute früher Feierabend."), delay = 0.4f),
                        trap(Landed(27.1f, 28.9f), Power('q', true), delay = 2.4f),
                        trap(Landed(29f, 31f), Fall('k'), say("Even the top step has a ceiling now.", "Selbst die oberste Stufe hat jetzt eine Decke."), delay = 1.7f),
                        trap(Landed(25f, 27f), Fall('d'), say("Upstairs they close behind you. Slowly.", "Oben schließen sie sich hinter dir. Langsam."), delay = 2.8f),
                        trap(Zone(17f, 6f, 18.5f, 9.2f), Fall('e'), say("The last one wants you to look at it.", "Die letzte will, dass du sie ansiehst."), delay = 0.35f),
                    ),
                ) {
                    fill(17..20, 10..11, '.'); fill(20..24, 10..11, 'g')
                    fill(29..30, 1..2); fill(29..30, 3..4, 'k')
                    fill(17..21, 3..4, '.'); fill(17..22, 1..2); fill(19..22, 3..4, 'd')
                },
            ),
        ) {
            border(); floor()
            fill(1..25, 9..9, 'r')
            fill(25..26, 13..14); fill(27..28, 11..14, 'q'); fill(29..30, 9..14)
            fill(9..12, 10..11, 'c'); fill(17..20, 10..11, 'g')
            fill(17..21, 1..2); fill(17..21, 3..4, 'd')
            fill(12..13, 1..2); fill(12..13, 3..4, 'e')
            spawn(2, 14); door(2, 8)
        },

        // 6 — the door is sealed by a wall of copper, and the straight way to it is a bait: the floor in front of the wall drops
        // under whoever walks up to it (the locked wall is a dead end, the open one a jump). The button that cuts the wall sits
        // on a lonely island across a pit (and the island sinks while you press it), the second button, in the way back to the
        // door, puts the wall back. Two buttons: press the one you need, hop the other.
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
                trap(PastX(23.3f), Fall('q'), say("The tiles in front of the wall have been recalled.", "Die Kacheln vor der Wand wurden zurückgerufen."), delay = 0.35f),
            ),
            hint = T("Button 2 puts the wall back. Hop it.", "Knopf 2 stellt die Wand wieder hin. Hüpf drüber."),
        ) {
            border(); floor()
            pit(4..5); pit(1..3)
            fill(1..3, 15..15, 'p')
            fill(11..12, 15..17, 'm')
            fill(21..24, 15..17, 'q')
            wire(25, 'w')
            fill(26..27, 13..14); fill(28..30, 11..14)
            spawn(17, 14); door(30, 10)
        },

        // 7 — a long hall with a ceiling that comes down in pieces and two walls of copper that flicker. The first slab falls
        // behind you (keep moving), the second lands in front of you (wait for it, climb it), the third comes down behind
        // you again, and at each wall you wait for the dark on the side where no slab can reach you. Two steps up to the door.
        Level(
            name = T("Loose Contact", "Wackelkontakt"),
            intro = T("It's not a bug, it's a flicker.", "Das ist kein Fehler, das ist ein Flimmern."),
            start = listOf(Clock('Z', on = 1.2f, off = 1.2f, phase = 0.5f), Clock('Y', on = 1.2f, off = 1.2f, phase = 0.9f)),
            traps = listOf(
                trap(PastX(4.0f), Fall('a'), say("Loose ceiling. Mind the first one: it does not wait.", "Lose Decke. Pass auf die erste auf: Sie wartet nicht.")),
                trap(PastX(5.6f), Play(Card.COLLAPSE), Fall('c'), say("Observed. Collapsed.", "Beobachtet. Kollabiert."), delay = 0.35f),
                trap(PastX(12.8f), Fall('b'), say("Another one, just behind your heels.", "Noch eine, dicht hinter deinen Fersen."), delay = 0.5f),
                trap(PastX(20.4f), Fall('d'), say("The wall flickers, the ceiling does not.", "Die Wand flackert, die Decke nicht."), delay = 0.9f),
            ),
            hint = T("Wait where nothing hangs above you.", "Warte dort, wo nichts über dir hängt."),
            // rematch: the slabs swap their jobs. The one that used to land in front now drops behind you, the one that used to
            // drop behind now lands in front, and a beam seeded on the step makes you wait a second time
            rematch = listOf(
                Round(
                    T("Same hall. The ceiling has been rearranged.", "Gleiche Halle. Die Decke wurde umgeräumt."),
                    hint = T("The first slab lands in front of you now: wait for it, and climb. The one you waited for later is the one you walk away from, and the beam wants a minute.", "Die erste Platte fällt jetzt vor dich: Warte und klettere. Die, auf die du später gewartet hast, lässt du hinter dir, und der Strahl will eine Minute."),
                    start = emptyList(),
                    traps = listOf(
                        trap(PastX(1.8f), Fall('a'), say("First slab: in front of you this time. Take a seat.", "Erste Platte: diesmal vor dir. Nehmen Sie Platz.")),
                        trap(PastX(8.2f), Fall('b'), say("Reserved seating: wait here, it's coming.", "Reservierter Platz: Warte hier, sie kommt gleich."), delay = 0.5f),
                        trap(PastX(13.5f), Fall('c'), say("This one was always going to be behind you.", "Die hier war schon immer hinter dir."), delay = 0.3f),
                        trap(Landed(14f, 17.5f), Play(Card.SPIKE_SEED), Laser('A', 18 to 1, 18 to 14, on = 0.95f, off = 60f, delay = 0.1f), say("A beam, grown on the step. Fresh.", "Ein Strahl, frisch auf der Stufe gewachsen.")),
                        trap(PastX(20.4f), Fall('d'), say("Mind the last roof. The walls stopped flickering.", "Achtung, letztes Dach. Die Wände flackern nicht mehr."), delay = 0.9f),
                        trap(Zone(21.8f, 12f, 23f, 15f), Laser('B', 25 to 1, 25 to 14, on = 0.95f, off = 60f, delay = 0.15f), say("Second beam. The first one told you what to do.", "Zweiter Strahl. Der erste hat dir gesagt, was zu tun ist.")),
                    ),
                ) { wire(19, '.'); wire(26, '.'); fill(16..16, 1..9); fill(16..16, 10..11, 'b') },
            ),
        ) {
            border(); floor()
            fill(3..4, 1..9); fill(3..4, 10..11, 'a')
            fill(9..12, 1..9); fill(9..12, 10..11, 'c')
            fill(14..15, 1..9); fill(14..15, 10..11, 'b')
            fill(21..22, 1..9); fill(21..22, 10..11, 'd')
            wire(19, 'Z'); wire(26, 'Y')
            fill(28..29, 13..14); fill(30..30, 11..14)
            spawn(2, 14); door(30, 10)
        },

        // 8 ★ — the bridge is half there: the first half is live, the second dark, and too wide to jump. A cosmic ray swaps the two
        // the moment something lands on the first half, so you have to be gone (in the air again) when it hits. Then the punchline:
        // rays come in pairs. The second one flips them back once you stand on the far half, and the reflex you just learned (jump
        // at once) meets the pins in the low ceiling over the far end: walk off it, quickly.
        Level(
            name = T("Memory", "Arbeitsspeicher"),
            intro = T("ECC memory. Error-free, allegedly.", "ECC-Speicher. Angeblich fehlerfrei."),
            start = listOf(Circuit('a'), Circuit('b', on = false)),
            traps = listOf(
                trap(Landed(16f, 20f), Play(Card.BIT_FLIP), BitFlip('a', 'b'), say("Bit flip! Purely cosmic. Nothing to do with me.", "Bitkipper! Rein kosmisch. Hat nichts mit mir zu tun."), delay = 0.12f),
                trap(Zone(20f, 14f, 26f, 15f), BitFlip('a', 'b'), say("Cosmic rays come in pairs. Ask any astronomer.", "Kosmische Strahlen kommen paarweise. Frag einen Astronomen."), delay = 0.45f),
            ),
            hint = T("Hop on, hop off. And the far half flips back: walk, do not jump, the ceiling is low.", "Drauf, wieder runter. Und die ferne Hälfte kippt zurück: geh, spring nicht, die Decke ist niedrig."),
        ) {
            border(); floor()
            pit(16..25)
            fill(16..19, 15..15, 'a'); fill(20..25, 15..15, 'b')
            ceilingSpikes(25..28, 13)
            spawn(3, 14); door(30, 14)
        },
    )
}
