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
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
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

        // 9 — a trap room of walls, run from right to left: two wells in the floor, two chip stacks in the far left corner. The
        // first well is a drop you have to take; when you climb out of it the first stack sweeps the whole floor towards you (turn
        // back, the well behind you is the only roof). The second stack comes when you pass the middle: now the roof is the well
        // ahead of you, and it is close. Do not hide too long: a pin grows out of the first well's wall once the wall has passed.
        // The last tiles before the door go live. Holding left and hopping gets you pushed into a well and crushed.
        Level(
            name = T("Side Effect", "Nebenwirkung"),
            intro = T("Ask your sysadmin or your devil about side effects.", "Zu Risiken und Nebenwirkungen fragen Sie Ihren Teufel."),
            legend = mapOf('Y' to Glyph(spike = true, dir = Dir.LEFT)),
            start = listOf(Circuit('Z', on = false)),
            traps = listOf(
                trap(Landed(19.5f, 24f), Move('a', 28f, 0f, 15f), say("Side effects include: walls.", "Nebenwirkungen: Wände.")),
                trap(Zone(23.8f, 15.3f, 26.2f, 17f), Move('Y', -2f, 0f, 4f), say("Side effect of hiding: the well grows a pin.", "Nebenwirkung vom Verstecken: Dem Brunnen wächst ein Stift."), delay = 2.75f),
                trap(BeforeX(18.5f), Move('b', 26f, 0f, 14f), say("Common side effect: a second wall.", "Häufige Nebenwirkung: eine zweite Wand.")),
                trap(BeforeX(13.5f), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Rare: the floor goes live. Rare, I said.", "Selten: Der Boden steht unter Strom. Selten, sagte ich.")),
            ),
            hint = T("The first roof is behind you. The second one is in front.", "Das erste Dach liegt hinter dir. Das zweite vor dir."),
        ) {
            border(); floor()
            pit(24..25); fill(24..25, 17..17); put(27, 16, 'Y')
            pit(14..15); fill(14..15, 17..17)
            fill(1..2, 9..14, 'a'); fill(3..4, 9..14, 'b')
            fill(9..9, 14..14, 'Z')
            spawn(29, 14); door(6, 14)
        },

        // 10 — start on a deck high on the left, step off it onto the first island, then stepping stones in a pit, everything on a
        // beat: the first island is solid, the stone right next to it starts its beat when you land on the island (whoever walks
        // straight on meets its first dark half: wait on the island until it lights up again), the stone after it clocks in when you
        // step on the one before, late (wait at the edge for it), and it is on a short shift once you touch it (do not wait there
        // either). On the far side the door runs up the wall to a ledge, and the stairs to it keep time as well: hurry.
        Level(
            name = T("Metronome", "Metronom"),
            intro = T("Step to the beat. Mine.", "Tritt im Takt. In meinem."),
            traps = listOf(
                trap(Landed(7.5f, 12.6f), Clock('b', on = 2.2f, off = 1.0f, phase = 1.8f), say("The metronome starts when you step up. Not before.", "Das Metronom startet, wenn du aufsteigst. Nicht vorher.")),
                trap(Touch('b'), Clock('c', on = 1.4f, off = 60f, phase = 60.4f), say("Stone three clocks in. Late, as ever.", "Stein drei stempelt ein. Wie immer zu spät.")),
                trap(Touch('c'), Clock('c', on = 1.0f, off = 60f), say("The island has a shift too. A short one.", "Die Insel hat auch Schicht. Eine kurze.")),
                trap(Landed(15.5f, 19.5f), Play(Card.SHY_DOOR), DoorTo(30, 8), say("The door took the stairs. It does that.", "Die Tür hat die Treppe genommen. Macht sie öfter.")),
                trap(Landed(21.5f, 27.5f), Clock('p', on = 2.0f, off = 60f), say("The stairs keep time too. Badly.", "Auch die Treppe hält den Takt. Schlecht.")),
            ),
            hint = T("Wait on the island that does not blink, and at the edge of the next. Never on the last one.", "Warte auf der Insel, die nicht blinkt, und am Rand der nächsten. Nie auf der letzten."),
        ) {
            border(); floor()
            fill(1..5, 9..14)
            pit(6..21)
            fill(8..11, 15..15)
            fill(12..14, 15..15, 'b')
            fill(16..18, 15..15, 'c')
            fill(24..27, 13..14); fill(28..29, 11..14, 'p'); fill(30..30, 9..14)
            spawn(2, 8); door(23, 14)
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
                trap(Zone(16.6f, 8f, 18.0f, 10.5f), Power('Z', true), say("That cable was lying there. Quietly. Now loudly.", "Das Kabel lag nur so da. Leise. Jetzt laut.")),
                trap(Zone(1f, 11f, 9.5f, 15f), HeatSpike('c', 0.5f), say("Ground floor: also warm. Everything is warm. Hello.", "Erdgeschoss: auch warm. Alles ist warm. Hallo.")),
                trap(Zone(20.6f, 13f, 22f, 15f), Power('Y', true), say("And one more. For whoever walks. Jumpers are spared.", "Und noch eins. Für alle, die gehen. Springer werden verschont.")),
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

        // 12 — do not trust the button, trust the ceiling: start on the floor at the right, the door far left behind a wall of
        // three tiles. A big red button lies right in the way (a decoy: nothing happens). Under the low ledge a slab comes down on
        // whoever dawdles (run), then the slab above the run-up to the wall comes down on whoever keeps running (stop next to it,
        // let it land, it is the stair). On the wall a blade rolls in under your feet. The straight dash for the door is the bait.
        // Rematch: the card is the bluff and the button is real, lying right under the slab: press it, step back out from under,
        // and the slab comes down as the stair. Whoever waits next to the slab like the first time waits for ever.
        Level(
            name = T("The Button", "Der Knopf"),
            intro = T("A very ordinary button.", "Ein ganz gewöhnlicher Knopf."),
            start = listOf(Pad('1', at = 26 to 14)),
            traps = listOf(
                trap(Pressed('1'), say("Nothing happened. Suspicious, isn't it?", "Nichts passiert. Verdächtig, oder?")),
                trap(Zone(19.5f, 12f, 23.5f, 15f), Fall('d'), say("Hurry up. Nobody likes standing under a ceiling.", "Beeil dich. Niemand steht gern unter einer Decke."), delay = 0.4f),
                trap(BeforeX(19.5f), Fall('c'), say("The button was a decoy. The ceiling is real.", "Der Knopf war Attrappe. Die Decke ist echt.")),
                trap(Zone(8.9f, 10f, 14.1f, 12.5f), Play(Card.DEVIL_SAW), Saw(-1f, 14.4f, 8f, 0f), say("Delivery! One blade, rolling, no returns.", "Lieferung! Ein Blatt, rollend, Rückgabe ausgeschlossen.")),
            ),
            hint = T("Run under the low slab. Do not run under the high one: stand next to it and let it land.", "Unter der niedrigen Platte durchrennen. Unter die hohe nicht: daneben stehen und sie landen lassen."),
            rematch = listOf(
                Round(
                    T("Same button. Different mood.", "Gleicher Knopf. Er hat heute Montag."),
                    start = listOf(Pad('1', at = 15 to 14)),
                    hint = T("The blade card was a bluff. The button under the slab is not.", "Die Messerkarte war geblufft. Der Knopf unter der Platte nicht."),
                    traps = listOf(
                        trap(Zone(19.5f, 12f, 23.5f, 15f), Fall('d'), say("Hurry up. Nobody likes standing under a ceiling.", "Beeil dich. Niemand steht gern unter einer Decke."), delay = 0.4f),
                        trap(BeforeX(19.5f), Bluff(Card.DEVIL_SAW), say("A second blade. Cancelled: budget cuts.", "Ein zweites Messer. Gestrichen: Sparmaßnahmen.")),
                        trap(Pressed('1'), Fall('c'), say("Now it works. Mondays.", "Jetzt geht er. Montags eben."), delay = 0.9f),
                        trap(Zone(8.9f, 10f, 14.1f, 12.5f), Saw(-1f, 14.4f, 8f, 0f), say("Delivery! One blade, rolling, no returns.", "Lieferung! Ein Blatt, rollend, Rückgabe ausgeschlossen.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(19..28, 8..10)
            fill(20..22, 11..12, 'd')
            fill(13..17, 1..4); fill(14..16, 5..6, 'c')
            fill(9..13, 12..14)
            spawn(30, 14); door(5, 14)
        },

        // 13 — the fuse box is upstairs, and you start on top of the cabinet: hop its rim onto a step over a pit that drops away
        // once you touch it, hop to an island where the bridge resets the moment you jump for it (it goes dark: whoever runs on
        // falls, whoever waits sees it light up again, but the island is a fuse holder and trips under whoever waits too long), and
        // up to a ledge on the far side that drops away under you. On it a long fuse plate under a low ceiling (you cannot jump over it),
        // and the live wall at its end hears you coming: it surges when you step up to the sill in front of it, and the surge comes
        // back through the floor a moment later. Wait for it to die down, hop the sill at once, the floor behind the wall drops
        // away too, and down to the door.
        Level(
            name = T("Fuse Box", "Sicherungskasten"),
            intro = T("The fuse box is upstairs. Naturally.", "Der Sicherungskasten ist oben. Natürlich."),
            start = listOf(Circuit('Z', on = false), Circuit('r'), Circuit('I', on = false), Circuit('F', on = false)),
            traps = listOf(
                trap(Airborne(10.6f, 12.8f), Clock('r', on = 1.8f, off = 0.9f, phase = 1.8f), say("Fuse reset. The bridge reboots. Give it a second.", "Sicherung zurückgesetzt. Die Brücke startet neu. Gib ihr eine Sekunde.")),
                trap(Touch('p'), Play(Card.CRUMBLE), Fall('p'), say("The step has a shift. It ends in a moment.", "Die Stufe hat Schicht. Gleich ist Feierabend."), delay = 0.45f),
                trap(Touch('i'), Power('I', true), say("The island is a fuse holder. Under load it trips. Under you.", "Die Insel ist ein Sicherungshalter. Unter Last löst sie aus. Unter dir."), delay = 1.3f),
                trap(Touch('m'), Fall('m'), say("Same contract. Fewer minutes.", "Gleicher Vertrag. Weniger Minuten."), delay = 0.4f),
                trap(Zone(24.3f, 7f, 25.2f, 9.2f), Clock('Z', on = 1.1f, off = 60f), say("Fuse wall. It hears you coming. Everything you are flows into it.", "Sicherungswand. Sie hört dich kommen. Alles an dir fließt hinein.")),
                trap(Zone(24.3f, 7f, 25.2f, 9.2f), Power('F', true), delay = 1.8f),
                trap(Touch('g'), Fall('g'), say("The far side is rented out too. Sorry. All of it.", "Die andere Seite ist auch vermietet. Tut mir leid. Alles hier."), delay = 0.3f),
            ),
            hint = T("Wait on the island for the bridge, but not long: it trips. At the wall, wait for the surge to die down: it only goes off once.", "Warte auf der Insel auf die Brücke, aber nicht lange: Sie löst aus. An der Wand warte, bis der Stromstoß abklingt: Er kommt nur einmal."),
        ) {
            border(); floor()
            fill(1..4, 9..14)
            pit(5..29)
            fill(6..11, 11..11, 'p'); fill(13..16, 11..11, 'i'); fill(13..16, 10..10, 'I')
            fill(17..19, 11..11, 'r')
            fill(20..21, 10..11, 'm'); fill(22..24, 9..10, 'f'); fill(25..26, 9..10); fill(28..29, 9..10, 'g')
            fill(22..23, 6..7); put(25, 8, '#'); put(24, 8, 'F')
            fill(26..26, 1..8, 'Z')
            spawn(2, 8); door(30, 14)
        },

        // 14 — Power Supply, seen from the back: a lift in the floor takes you up to a long shelf, whether you are ready or not, and
        // it does not stop at the shelf (step off: it runs on into the ceiling). A pressure plate in the shelf is clause two: step on
        // it and the spiked fin ahead slams down (stop, let it go back up), and in the small print at the door a spiked pin slides out
        // of the wall as you come (hop it). The terms are the trap: the room only plays fair with whoever reads ahead.
        Level(
            name = T("Power Supply", "Netzteil"),
            intro = T("Unlimited power. Terms and conditions apply.", "Unbegrenzter Strom. Es gelten die AGB."),
            legend = mapOf('B' to Glyph(spike = true, dir = Dir.DOWN), 'W' to Glyph(spike = true, dir = Dir.RIGHT)),
            start = listOf(Circuit('p')),
            traps = listOf(
                trap(Zone(21.3f, 13f, 23.7f, 15.2f), Move('l', 0f, -6f, 4f), say("Going up. Terms and conditions apply.", "Wir fahren nach oben. Es gelten die AGB.")),
                trap(Zone(21.3f, 13f, 23.7f, 15.2f), Move('l', 0f, -6f, 12f), say("Clause one: the lift does not stop at your floor.", "Paragraf eins: Der Aufzug hält nicht in deinem Stockwerk."), delay = 1.6f),
                trap(Touch('p'), Play(Card.HEADBUTT), Move('B', 0f, 4f, 22f), say("Clause two takes effect immediately.", "Paragraf zwei gilt sofort.")),
                trap(Touch('p'), Move('B', 0f, -4f, 14f), delay = 1.3f),
                trap(BeforeX(10.5f), Move('W', 6f, 0f, 12f), say("Small print: the wall signs too.", "Kleingedrucktes: Die Wand unterschreibt mit.")),
            ),
            hint = T("Step off the lift as soon as you can. The plate in the shelf slams the fin ahead: stop in front of it. And read the wall at the door.", "Steig vom Aufzug, sobald es geht. Die Platte im Regal lässt die Finne vor dir runterkrachen: Bleib davor stehen. Und lies die Wand an der Tür."),
        ) {
            border(); floor()
            fill(21..23, 15..16, 'l')
            fill(1..20, 10..11); fill(19..20, 12..14)
            fill(13..14, 10..10, 'p')
            fill(3..23, 1..2)
            fill(11..11, 3..5, 'B')
            put(0, 8, '.'); put(0, 9, 'W')
            spawn(30, 14); door(4, 9)
        },

        // 15 — a plug on the shelf swaps the controls: start high in the middle of the shelf, a fan blade sets off behind you and chases you along it, halfway
        // the plug reverses left and right, so the rest of the shelf, the fall to the floor and the whole floor back to the door (a pit,
        // a roller that comes at you head on, a pendulum that wakes up as you land, a spike) are done on the other keys. The plug at the door claims to restore the polarity:
        // it does, and whoever keeps pressing the key that worked runs straight back into the spike behind it.
        // Rematch: the plug on the floor restores the polarity early, before the spike, and the one at the door reverses it again.
        Level(
            name = T("Connector", "Stecker"),
            intro = T("USB-C fits either way up. Everything does.", "USB-C passt andersrum. Alles passt andersrum."),
            start = emptyList(),
            traps = listOf(
                trap(PastX(13.5f), Saw(1f, 7.4f, 7f, 0f), say("A blade is following you. Politely. It has no key either.", "Ein Messer folgt dir. Höflich. Es hat auch keinen Schlüssel.")),
                trap(Zone(17f, 5f, 21f, 8.5f), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Left is right. Again.", "Polung vertauscht. Links ist rechts. Schon wieder.")),
                trap(Zone(25f, 13f, 30f, 15.2f), Saw(-3f, 14.4f, 9f, 0f), say("The ground floor sends a roller. Express.", "Das Erdgeschoss schickt einen Roller. Express.")),
                trap(Landed(12f, 15f), PathSaw(3f, 10.2f to 14.4f, 10.2f to 10.6f), say("A pendulum. It swings the right way round. You do not.", "Ein Pendel. Es schwingt richtig herum. Du nicht.")),
                trap(Zone(3.5f, 13f, 6f, 15.2f), Swap(false), say("Polarity restored. Probably.", "Polung wiederhergestellt. Vermutlich.")),
            ),
            hint = T("After the first plug the other key is the right one. After the second, think again.", "Nach dem ersten Stecker stimmt die andere Taste. Nach dem zweiten denk nochmal nach."),
            rematch = listOf(
                Round(
                    T("Unplugged and plugged back in. Classic.", "Aus- und wieder eingesteckt. Hilft immer. Mir."),
                    hint = T("The plug on the floor sits before the spike now. The one at the door turns everything around again.", "Der Stecker unten sitzt jetzt vor dem Stachel. Der an der Tür dreht alles nochmal um."),
                    traps = listOf(
                        trap(PastX(13.5f), Saw(1f, 7.4f, 7f, 0f), say("A blade is following you. Politely. It has no key either.", "Ein Messer folgt dir. Höflich. Es hat auch keinen Schlüssel.")),
                        trap(Zone(17f, 5f, 21f, 8.5f), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Old habit.", "Polung vertauscht. Alte Gewohnheit.")),
                        trap(Zone(25f, 13f, 30f, 15.2f), Saw(-3f, 14.4f, 9f, 0f), say("The ground floor sends a roller. Express.", "Das Erdgeschoss schickt einen Roller. Express.")),
                        trap(Zone(12f, 13f, 15f, 15.2f), Swap(false), say("Polarity restored. Early, this time.", "Polung wiederhergestellt. Diesmal früh.")),
                        trap(Zone(3.5f, 13f, 6f, 15.2f), Swap(true), say("And reversed again. For the door.", "Und wieder vertauscht. Für die Tür.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(1..23, 8..9)
            pit(22..23); put(6, 14, '^')
            spawn(12, 7); door(1, 14)
        },

        // 16 — act finale, and the room that does not end where it looks like it does. Room one: a pad at the start gives the bridge over the
        // pit a short window of power, the live gate in front of the island reboots as you step down to it (it flickers: wait for the dark), two steps up the wall over the pit
        // each give way under you, and at the top the door sits right there. As you land on the ledge the wall cracks: the end was never
        // here, and the door slips through the breach (high up in the wall) before you can touch it. Room two: a ledge, a hot landing,
        // a bridge that goes dark the moment you drop off the ledge (it comes back: wait for it), and the door at the far end, on a ledge.
        // The extension happens on the way, not at the door.
        Level(
            name = T("Motherboard", "Hauptplatine"),
            intro = T("Act one finale. All the parts, all the ways to die.", "Finale, Akt eins. Alle Teile, alle Wege zu sterben."),
            rooms = 2,
            start = listOf(Circuit('a', on = false), Circuit('Z', on = false), Pad('1', at = 5 to 13)),
            traps = listOf(
                trap(Pressed('1'), Clock('a', on = 3.2f, off = 7f), say("The bridge is on a timer. Again.", "Die Brücke hat einen Timer. Schon wieder.")),
                trap(Airborne(17.2f, 20.6f), Clock('Z', on = 1.8f, off = 2.4f, phase = 0.9f), say("Gate firmware updated. While you were standing in it.", "Tor-Firmware aktualisiert. Während du davorstandest.")),
                trap(Zone(21.3f, 13f, 22.2f, 15.2f), HeatSpike('i', 0.5f), say("The floor under the gate: a little warm. For you.", "Der Boden unter dem Tor: ein bisschen warm. Für dich.")),
                trap(Landed(22f, 25.2f), Move('p', 0f, 6f, 2.4f), say("Step one: sublet. Step two: also.", "Stufe eins: untervermietet. Stufe zwei: auch."), delay = 0.1f),
                trap(Landed(26f, 29f), Move('q', 0f, 8f, 2.4f), delay = 0.1f),
                trap(Zone(29f, 6.5f, 31f, 9.2f), Play(Card.ANNEX), Extend(into = 1, top = 7, bottom = 8, warn = 1.2f, door = roomX(1, 29) to 10, line = T("The motherboard has a second floor. Of course it does.", "Die Platine hat ein zweites Stockwerk. Natürlich.")), say("Almost there. Almost. Still almost.", "Gleich geschafft. Gleich. Immer noch gleich.")),
                trap(Zone(roomX(1, 4f), 7f, roomX(1, 8f), 9.3f), HeatSpike('l', 0.5f), say("The ledge is a heatsink. Reversed.", "Das Regal ist ein Kühlkörper. Verkehrt herum.")),
                trap(Zone(roomX(1, 12f), 13f, roomX(1, 17f), 15.2f), HeatSpike('h', 0.5f), say("Room two: preheated.", "Raum zwei: vorgeheizt.")),
                trap(Airborne(roomX(1, 10.2f), roomX(1, 13f)), Clock('b', on = 60f, off = 1.7f, phase = 60f), say("The last bridge is shy. It saw you jump.", "Die letzte Brücke ist scheu. Sie hat dich springen sehen.")),
                trap(Touch('b'), Clock('b', on = 1.1f, off = 60f), say("And it does not like being stood on.", "Und draufstehen mag sie auch nicht.")),
            ),
            hint = T("The pad powers the bridge for a moment. The door is not where it looks.", "Der Knopf gibt der Brücke kurz Strom. Die Tür ist nicht, wo sie scheint."),
        ) {
            border(); floor()
            room(0) {
                fill(1..6, 14..14)
                bridge(7..14, 'a', y = 14)
                fill(15..16, 14..17)
                fill(21..21, 1..14, 'Z')
                pit(22..28)
                fill(18..21, 15..15, 'i'); fill(22..24, 13..13, 'p'); fill(26..28, 11..11, 'q'); fill(29..30, 9..10)
                spawn(1, 13); door(30, 8)
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
