package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Extend
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone


/**
 * World 3, act 1, block B (levels 9-16: Side Effect, Metronome, Loose Cables, The Button, Fuse Box, Power Supply, Connector and
 * the finale Motherboard), rebuilt under the V2 level design (docs/LEVEL_DESIGN_V2.md, section 8). Each level is one idea in one
 * dominant family; the bot solutions are in the test sources ([World3RoomsB]).
 */
object World3PartB {
    private val hidden = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(

        // 9 — a trap room of walls: two wells in the floor, two chip stacks at the far end. The first well is a drop you have to
        // take; when you climb out of it the first stack sweeps the whole floor towards you (turn back, the well behind you is
        // the only roof). The second stack comes when you pass the middle: now the roof is the well ahead of you, and it is
        // close. The last tiles before the door go live. Holding right and hopping gets you pushed into a well and crushed.
        Level(
            name = T("Side Effect", "Nebenwirkung"),
            intro = T("Ask your sysadmin or your devil about side effects.", "Zu Risiken und Nebenwirkungen fragen Sie Ihren Teufel."),
            start = listOf(Circuit('Z', on = false)),
            traps = listOf(
                trap(Landed(8f, 12.5f), Move('a', -28f, 0f, 15f), say("Side effects include: walls.", "Nebenwirkungen: Wände.")),
                trap(PastX(13.5f), Move('b', -26f, 0f, 14f), say("Common side effect: a second wall.", "Häufige Nebenwirkung: eine zweite Wand.")),
                trap(PastX(18.5f), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Rare: the floor goes live. Rare, I said.", "Selten: Der Boden steht unter Strom. Selten, sagte ich.")),
            ),
            hint = T("The first roof is behind you. The second one is in front.", "Das erste Dach liegt hinter dir. Das zweite vor dir."),
        ) {
            border(); floor()
            pit(6..7); fill(6..7, 17..17)
            pit(16..17); fill(16..17, 17..17)
            fill(29..30, 9..14, 'a'); fill(27..28, 9..14, 'b')
            fill(22..22, 14..14, 'Z')
            spawn(2, 14); door(25, 14)
        },

        // 10 — a chip to hop, then stepping stones in a pit, everything on a beat: the first island is solid (wait there until the
        // clocked stone lights up), the last stone wakes up when you land on the one before and is on a short shift once you touch it
        // (do not wait there either). On the far side the door runs up the wall to a ledge, and the stairs to it keep time as well: hurry.
        Level(
            name = T("Metronome", "Metronom"),
            intro = T("Step to the beat. Mine.", "Tritt im Takt. In meinem."),
            start = listOf(Clock('b', on = 2.2f, off = 1.2f, phase = 1.1f)),
            traps = listOf(
                trap(Landed(12.5f, 16f), Clock('c', on = 1.4f, off = 60f, phase = 60.4f), say("Stone three clocks in. Late, as ever.", "Stein drei stempelt ein. Wie immer zu spät.")),
                trap(Touch('c'), Clock('c', on = 1.0f, off = 60f), say("The island has a shift too. A short one.", "Die Insel hat auch Schicht. Eine kurze.")),
                trap(Landed(17.5f, 21f), Play(Card.SHY_DOOR), DoorTo(30, 8), say("The door took the stairs. It does that.", "Die Tür hat die Treppe genommen. Macht sie öfter.")),
                trap(Landed(23.5f, 27.5f), Clock('p', on = 2.0f, off = 60f), say("The stairs keep time too. Badly.", "Auch die Treppe hält den Takt. Schlecht.")),
            ),
            hint = T("Wait on the island that does not blink. Never on the last one.", "Warte auf der Insel, die nicht blinkt. Nie auf der letzten."),
        ) {
            border(); floor()
            fill(5..6, 13..14)
            pit(7..22)
            fill(8..10, 15..15)
            fill(13..15, 15..15, 'b')
            fill(18..20, 15..15, 'c')
            fill(24..26, 13..14); fill(28..29, 11..14, 'p'); fill(30..30, 9..14)
            spawn(2, 14); door(23, 14)
        },

        // 11 — a tower of two shelves and the floor, a snake from top left to the door at the bottom right: along the top shelf (a chip
        // to climb that warms up under your feet), off its end onto the middle shelf (the landing is preheated: keep moving), back
        // along that one past a cable that comes alive, and off its other end onto the floor, where the landing plate warms up again
        // and one more cable wakes in front of the door. The way down is the way round.
        Level(
            name = T("Loose Cables", "Kabelsalat"),
            intro = T("Tidy cabling. I did it myself.", "Saubere Verkabelung. Hab ich selbst gemacht."),
            start = listOf(Circuit('Z', on = false), Circuit('Y', on = false)),
            traps = listOf(
                trap(Landed(11f, 15f), HeatSpike('a', 0.5f), say("The shelf is warm. So is the rest of the board.", "Das Regal ist warm. Der Rest der Platine auch.")),
                trap(Landed(21.5f, 27f), Play(Card.OVERCLOCKED), HeatSpike('b', 0.5f), say("Landing pad: preheated. You are welcome.", "Landeplatz: vorgeheizt. Gern geschehen.")),
                trap(Zone(16.5f, 8f, 21f, 10.5f), Power('Z', true), say("That cable was lying there. Quietly. Now loudly.", "Das Kabel lag nur so da. Leise. Jetzt laut.")),
                trap(Zone(1f, 13f, 7f, 15f), HeatSpike('c', 0.5f), say("Ground floor: also warm. Everything is warm. Hello.", "Erdgeschoss: auch warm. Alles ist warm. Hallo.")),
                trap(Zone(15f, 13f, 22f, 15f), Power('Y', true), say("And one more. For the road.", "Und noch eins. Für unterwegs.")),
            ),
            hint = T("Whatever you land on warms up. Keep moving.", "Was du auch betrittst, wird warm. Bleib in Bewegung."),
        ) {
            border(); floor()
            fill(1..21, 4..4); fill(11..14, 3..3, 'a')
            fill(9..30, 10..11); fill(22..25, 10..10, 'b')
            fill(16..16, 9..9, 'Z')
            fill(2..5, 15..15, 'c')
            fill(22..22, 14..14, 'Y')
            spawn(2, 3); door(28, 14)
        },

        // 12 — do not press: start at the left end of a ledge, a fan blade rolls along it towards you, off the right end onto the floor
        // and back under the ledge, over a big red button (a decoy), under a slab that comes down behind you if you hurry, to a wall of
        // three tiles and a second slab above the run-up: run under that one and it comes down on you, wait next to it and it comes
        // down as the stair over the wall. On the wall a third blade rolls in under your feet. Rematch: the card is the bluff, the
        // button is real: press it twice, and the blade comes from behind.
        Level(
            name = T("The Button", "Der Knopf"),
            intro = T("A very ordinary button.", "Ein ganz gewöhnlicher Knopf."),
            start = listOf(Pad('1', at = 28 to 14)),
            traps = listOf(
                trap(After(0.5f), Saw(29.5f, 7.4f, -5f, 0f), say("Welcome. Mind the blade. It came with the room.", "Willkommen. Vorsicht, Messer. Gehört zum Zimmer.")),
                trap(Zone(19.5f, 12f, 23.5f, 15f), Fall('d'), say("Hurry up. Nobody likes standing under a ceiling.", "Beeil dich. Niemand steht gern unter einer Decke."), delay = 0.4f),
                trap(Pressed('1'), say("Nothing happened. Suspicious, isn't it?", "Nichts passiert. Verdächtig, oder?")),
                trap(BeforeX(19.5f), Play(Card.HEADBUTT), Fall('c'), say("The button was a decoy. The ceiling is real.", "Der Knopf war Attrappe. Die Decke ist echt.")),
                trap(Zone(8.9f, 10f, 14.1f, 12.5f), Saw(-1f, 14.4f, 8f, 0f), say("Delivery! Fan blade, one piece, rolling.", "Lieferung! Lüfterblatt, ein Stück, rollend.")),
            ),
            hint = T("Do not stand under the slab. Stand next to it and let it land.", "Nicht unter die Platte stellen. Daneben stehen und sie landen lassen."),
            // rematch: the card is the bluff and the button is real, standing right under the slab: press it, step out from under,
            // and the slab comes down as the stair. Whoever waits next to the slab like the first time waits for ever.
            rematch = listOf(
                Round(
                    T("Same button. Different mood.", "Gleicher Knopf. Er hat heute Montag."),
                    start = listOf(Pad('1', at = 15 to 14)),
                    hint = T("The ceiling called in sick. The button did not.", "Die Decke ist krank. Der Knopf nicht."),
                    traps = listOf(
                        trap(After(0.5f), Saw(29.5f, 7.4f, -5f, 0f), say("Welcome. Mind the blade. It came with the room.", "Willkommen. Vorsicht, Messer. Gehört zum Zimmer.")),
                        trap(Zone(19.5f, 12f, 23.5f, 15f), Fall('d'), say("Hurry up. Nobody likes standing under a ceiling.", "Beeil dich. Niemand steht gern unter einer Decke."), delay = 0.4f),
                        trap(BeforeX(19.5f), Bluff(Card.HEADBUTT), say("Cancelled. The ceiling called in sick.", "Abgesagt. Die Decke hat sich krankgemeldet.")),
                        trap(Pressed('1'), Fall('c'), say("Now it works. Mondays.", "Jetzt geht er. Montags eben."), delay = 0.9f),
                        trap(Zone(8.9f, 10f, 14.1f, 12.5f), Saw(-1f, 14.4f, 8f, 0f), say("Delivery! Fan blade, one piece, rolling.", "Lieferung! Lüfterblatt, ein Stück, rollend.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(19..28, 8..10)
            fill(20..22, 11..12, 'd')
            fill(13..17, 1..4); fill(14..16, 5..6, 'c')
            fill(9..13, 12..14)
            spawn(19, 7); door(1, 14)
        },

        // 13 — the fuse box is upstairs: a chip to climb, a step over a pit that is on a short shift once you touch it, a stable island
        // where you wait for the rails of the bridge to light up, and a ledge on the far side that gives way as you land. On it a
        // long fuse plate under a low ceiling (you cannot jump over it): running over it sends a surge through a live wall that was
        // dark a moment ago. Wait for it to die down, run, the floor behind the wall gives way too, and down to the door.
        Level(
            name = T("Fuse Box", "Sicherungskasten"),
            intro = T("The fuse box is upstairs. Naturally.", "Der Sicherungskasten ist oben. Natürlich."),
            start = listOf(Circuit('Z', on = false), Clock('r', on = 1.8f, off = 1.2f, phase = 0.5f)),
            traps = listOf(
                trap(Touch('p'), Play(Card.CRUMBLE), Fall('p'), say("The step has a shift. It ends in half a second.", "Die Stufe hat Schicht. In einer halben Sekunde ist Feierabend."), delay = 0.45f),
                trap(Touch('m'), Fall('m'), say("Same contract. Fewer minutes.", "Gleicher Vertrag. Weniger Minuten."), delay = 0.4f),
                trap(Touch('f'), Clock('Z', on = 1.4f, off = 60f), say("Fuse plate. Everything you step on flows. Mostly into the wall.", "Sicherungsplatte. Alles, was du betrittst, fließt. Meistens in die Wand.")),
                trap(Touch('g'), Fall('g'), say("The far side is on loan too. Sorry. Everything is.", "Die andere Seite ist auch geliehen. Tut mir leid. Alles hier."), delay = 0.3f),
            ),
            hint = T("Whatever you run over on the long plate warms the wall. Wait for it to die down.", "Was du auf der langen Platte betrittst, setzt die Wand unter Strom. Warte, bis sie abklingt."),
        ) {
            border(); floor()
            fill(4..5, 13..14)
            pit(6..27)
            fill(6..8, 13..13, 'p'); fill(11..13, 11..11)
            fill(14..17, 11..11, 'r')
            fill(18..19, 9..10, 'm'); fill(20..22, 9..10, 'f'); fill(23..24, 9..10); fill(25..26, 9..10, 'g'); fill(27..27, 9..10)
            fill(20..22, 6..7)
            fill(24..24, 1..8, 'Z')
            spawn(1, 14); door(28, 14)
        },

        // 14 — a lift in the floor takes you up to a long shelf under a beam with spiked fins hanging from it: the first drops where you run
        // under it (do not stop), the second slams down in front of you the moment you pass (wait for it to go back up), and in front
        // of the door the shelf is live. The lift is part of the supply: step on it and it goes, whether you are ready or not.
        Level(
            name = T("Power Supply", "Netzteil"),
            intro = T("Unlimited power. Terms and conditions apply.", "Unbegrenzter Strom. Es gelten die AGB."),
            legend = mapOf('A' to Glyph(spike = true, dir = Dir.DOWN), 'B' to Glyph(spike = true, dir = Dir.DOWN), 'C' to Glyph(spike = true, dir = Dir.DOWN)),
            start = listOf(Circuit('Z', on = false)),
            traps = listOf(
                trap(Zone(8.3f, 13f, 10.7f, 15.2f), Move('l', 0f, -6f, 2.6f), say("Going up. Terms and conditions apply.", "Wir fahren nach oben. Es gelten die AGB.")),
                trap(Zone(8f, 7.5f, 11f, 9.6f), Move('C', 0f, 3.5f, 18f), say("Top floor. Please do not loiter. It is not a lounge.", "Oberstes Stockwerk. Bitte nicht herumstehen. Das ist keine Lounge."), delay = 0.4f),
                trap(Zone(13f, 6f, 16.5f, 9.5f), Move('A', 0f, 3.5f, 18f), say("Clause one: do not stand under clause one.", "Paragraf eins: Unter Paragraf eins nicht stehen bleiben."), delay = 0.35f),
                trap(PastX(17.2f), Play(Card.HEADBUTT), Move('B', 0f, 3.5f, 22f), say("Clause two takes effect immediately.", "Paragraf zwei gilt sofort.")),
                trap(PastX(17.2f), Move('B', 0f, -3.5f, 14f), delay = 1.3f),
                trap(PastX(22.5f), Power('Z', true), say("Final clause: the shelf is live. In writing.", "Letzte Klausel: Das Regal ist unter Strom. Schriftlich.")),
            ),
            hint = T("The second fin comes down right after you pass the first. Stop in front of it.", "Die zweite Finne fällt, sobald du die erste hinter dir hast. Bleib davor stehen."),
        ) {
            border(); floor()
            fill(8..10, 15..16, 'l')
            fill(11..30, 9..10); fill(11..12, 11..14)
            fill(8..28, 1..2)
            fill(8..10, 3..5, 'C'); fill(15..15, 3..5, 'A'); fill(20..20, 3..5, 'B')
            fill(26..26, 8..8, 'Z')
            spawn(1, 14); door(29, 8)
        },

        // 15 — a plug on the shelf swaps the controls: start high, a fan blade sets off behind you and chases you along the shelf, halfway
        // the plug reverses left and right, so the rest of the shelf, the fall to the floor and the whole floor back to the door (a pit,
        // a roller that comes at you head on, a spike) are done on the other keys. The plug at the door claims to restore the polarity:
        // it does, and whoever keeps pressing the key that worked runs straight back into the spike behind it.
        // Rematch: the plug on the floor restores the polarity early, before the spike, and the one at the door reverses it again.
        Level(
            name = T("Connector", "Stecker"),
            intro = T("USB-C fits either way up. Everything does.", "USB-C passt andersrum. Alles passt andersrum."),
            start = emptyList(),
            traps = listOf(
                trap(PastX(7f), Saw(-1f, 7.4f, 7f, 0f), say("A blade is following you. Politely. It has no key either.", "Ein Messer folgt dir. Höflich. Es hat auch keinen Schlüssel.")),
                trap(Zone(17f, 5f, 21f, 8.5f), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Left is right. Again.", "Polung vertauscht. Links ist rechts. Schon wieder.")),
                trap(Zone(25f, 13f, 30f, 15.2f), Saw(-1f, 14.4f, 9f, 0f), say("The ground floor sends a roller. Express.", "Das Erdgeschoss schickt einen Roller. Express.")),
                trap(Zone(3.5f, 13f, 6f, 15.2f), Swap(false), say("Polarity restored. Probably.", "Polung wiederhergestellt. Vermutlich.")),
            ),
            hint = T("After the first plug the other key is the right one. After the second, think again.", "Nach dem ersten Stecker stimmt die andere Taste. Nach dem zweiten denk nochmal nach."),
            rematch = listOf(
                Round(
                    T("Unplugged and plugged back in. Classic.", "Aus- und wieder eingesteckt. Hilft immer. Mir."),
                    hint = T("The plug on the floor sits before the spike now. The one at the door turns everything around again.", "Der Stecker unten sitzt jetzt vor dem Stachel. Der an der Tür dreht alles nochmal um."),
                    traps = listOf(
                        trap(PastX(7f), Saw(-1f, 7.4f, 7f, 0f), say("A blade is following you. Politely. It has no key either.", "Ein Messer folgt dir. Höflich. Es hat auch keinen Schlüssel.")),
                        trap(Zone(17f, 5f, 21f, 8.5f), Play(Card.BIT_FLIP), Swap(true), say("Polarity reversed. Old habit.", "Polung vertauscht. Alte Gewohnheit.")),
                        trap(Zone(25f, 13f, 30f, 15.2f), Saw(-1f, 14.4f, 9f, 0f), say("The ground floor sends a roller. Express.", "Das Erdgeschoss schickt einen Roller. Express.")),
                        trap(Zone(12f, 13f, 15f, 15.2f), Swap(false), say("Polarity restored. Early, this time.", "Polung wiederhergestellt. Diesmal früh.")),
                        trap(Zone(3.5f, 13f, 6f, 15.2f), Swap(true), say("And reversed again. For the door.", "Und wieder vertauscht. Für die Tür.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(1..23, 8..9)
            pit(22..23); put(8, 14, '^')
            spawn(2, 7); door(1, 14)
        },

        // 16 — act finale, and the room that does not end where it looks like it does. Room one: a pad at the start gives the bridge over the
        // pit a short window of power, a live gate in front of the island flickers (wait for the dark), two steps up the wall over the pit
        // each give way under you, and at the top the door sits right there. As you land on the ledge the wall cracks: the end was never
        // here, and the door slips through the breach (high up in the wall) before you can touch it. Room two: a ledge, a hot landing,
        // a bridge that leaves when you come near, and the door at the far end, on a ledge. The extension happens on the way, not at the door.
        Level(
            name = T("Motherboard", "Hauptplatine"),
            intro = T("Act one finale. All the parts, all the ways to die.", "Finale, Akt eins. Alle Teile, alle Wege zu sterben."),
            rooms = 2,
            start = listOf(Circuit('a', on = false), Circuit('Z', on = false), Pad('1', at = 3 to 14)),
            traps = listOf(
                trap(Pressed('1'), Clock('a', on = 3.2f, off = 7f), say("The bridge is on a timer. Again.", "Die Brücke hat einen Timer. Schon wieder.")),
                trap(Zone(15f, 13f, 19.5f, 15.2f), Clock('Z', on = 1.8f, off = 2.4f), say("Gate firmware updated. While you were standing in it.", "Tor-Firmware aktualisiert. Während du davorstandest.")),
                trap(Zone(19.4f, 13f, 21.8f, 15.2f), HeatSpike('i', 0.5f), say("The floor under the gate: a little warm. For you.", "Der Boden unter dem Tor: ein bisschen warm. Für dich.")),
                trap(Landed(22f, 25.2f), Fall('p'), say("Step one: on loan. Step two: also.", "Stufe eins: geliehen. Stufe zwei: auch."), delay = 0.45f),
                trap(Landed(26f, 29f), Fall('q'), delay = 0.4f),
                trap(Zone(29f, 6.5f, 31f, 9.2f), Play(Card.ANNEX), Extend(into = 1, top = 7, bottom = 8, warn = 1.2f, door = roomX(1, 29) to 10, line = T("The motherboard has a second floor. Of course it does.", "Die Platine hat ein zweites Stockwerk. Natürlich.")), say("Almost there. Almost. Still almost.", "Gleich geschafft. Gleich. Immer noch gleich.")),
                trap(Zone(roomX(1, 4f), 7f, roomX(1, 8f), 9.3f), HeatSpike('l', 0.5f), say("The ledge is a heatsink. Reversed.", "Das Regal ist ein Kühlkörper. Verkehrt herum.")),
                trap(Zone(roomX(1, 12f), 13f, roomX(1, 17f), 15.2f), HeatSpike('h', 0.5f), say("Room two: preheated.", "Raum zwei: vorgeheizt.")),
                trap(Zone(roomX(1, 17.2f), 13f, roomX(1, 20f), 15.2f), Clock('b', on = 1.6f, off = 60f), say("The last bridge is shy. It leaves when you come near.", "Die letzte Brücke ist scheu. Sie geht, wenn du näher kommst.")),
            ),
            hint = T("The pad powers the bridge for a moment. The door is not where it looks.", "Der Knopf gibt der Brücke kurz Strom. Die Tür ist nicht, wo sie scheint."),
        ) {
            border(); floor()
            room(0) {
                bridge(7..14, 'a')
                fill(20..20, 1..14, 'Z')
                pit(22..28)
                fill(18..21, 15..15, 'i'); fill(22..24, 13..13, 'p'); fill(26..28, 11..11, 'q'); fill(29..30, 9..10)
                spawn(1, 14); door(30, 8)
            }
            room(1) {
                fill(1..10, 9..10); fill(4..8, 9..10, 'l')
                fill(13..16, 15..15, 'h')
                bridge(20..25, 'b')
                fill(26..27, 13..14); fill(28..30, 11..14)
                door(29, 10)
            }
        },
    )
}
