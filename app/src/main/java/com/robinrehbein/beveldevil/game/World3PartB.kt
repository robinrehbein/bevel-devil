package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Extend
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
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Action.Toggle
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone


/**
 * World 3, levels 1-16. Act 1, "Stromkreise": copper rails, pressure pads, clocks and live traces, Mephi cutting the
 * power under you, a bit flip. The first three levels teach; from the fourth on the traps come in chains, each one
 * springing on the spot (mid-jump, at the landing, the moment you step on a rail) and each of a different kind.
 * The room looks calm: the danger is what the hardware does, not what is hidden in the floor.
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
                trap(After(0.05f), Saw(29.5f, 7.4f, -5f, 0f), say("Welcome. Mind the blade. It came with the room.", "Willkommen. Vorsicht, Messer. Gehört zum Zimmer.")),
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
                        trap(After(0.05f), Saw(29.5f, 7.4f, -5f, 0f), say("Welcome. Mind the blade. It came with the room.", "Willkommen. Vorsicht, Messer. Gehört zum Zimmer.")),
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

        // 13 — the fuse box is on top of the hill: climb it to switch the live wall off, but it only slows the current;
        // waiting for the dark beat on the rail in front of the wall cuts the rail (wait on the hill instead), and the floor behind the wall drops
        Level(
            name = T("Fuse Box", "Sicherungskasten"),
            intro = T("The fuse box is upstairs. Naturally.", "Der Sicherungskasten ist oben. Natürlich."),
            start = listOf(Circuit('Z'), Pad('1', at = 11 to 11, circuits = "Z", mode = PadMode.OFF), Circuit('a')),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Clock('Z', on = 0.8f, off = 2.8f, phase = 0.8f), say("Fuse box: the fuse is a clock now.", "Sicherungskasten: die Sicherung ist jetzt ein Taktgeber.")),
                trap(Touch('a'), Power('a', false), say("Waiting room closed. Should have waited upstairs.", "Wartezimmer geschlossen. Oben warten wäre klüger gewesen."), delay = 0.8f),
                trap(PastX(22.6f), Fall('x'), say("Load-bearing floor is optional.", "Tragender Boden ist optional."), delay = 0.15f),
            ),
        ) {
            border(); floor()
            chip(6, 2, 1); chip(8, 2, 2); chip(10, 4, 3); chip(14, 2, 2); chip(16, 2, 1)
            bridge(18..21, 'a')
            wire(22, 'Z')
            fill(24..26, 15..17, 'x')
            spawn(); door()
        },

        // 14 — the pad powers the bridge for a few seconds only
        Level(
            name = T("Power Supply", "Netzteil"),
            intro = T("Unlimited power. Terms and conditions apply.", "Unbegrenzter Strom. Es gelten die AGB."),
            legend = mapOf('A' to hidden),
            start = listOf(Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON), Circuit('Z')),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SINKING), Power('a', false), say("Timer's up. Did you think it was a gift?", "Die Zeit ist um. Dachtest du, das sei ein Geschenk?"), delay = 3.2f),
                trap(PastX(17f), Power('Z', false), say("The cable was never live. I just like the glow.", "Das Kabel war nie unter Strom. Ich mag nur das Glühen.")),
                trap(PastX(26.4f), Show('A'), say("Made it? Here, a spike. On the house.", "Geschafft? Hier, ein Spike. Aufs Haus.")),
            ),
        ) {
            border(); floor()
            put(8, 14, '^'); put(10, 14, '^')
            bridge(13..25, 'a')
            wire(19, 'Z')
            put(27, 14, 'A'); put(28, 14, 'A')
            spawn(); door()
        },

        // 15 — a pad swaps the controls until the second pad swaps them back
        Level(
            name = T("Connector", "Stecker"),
            intro = T("USB-C fits either way up. Everything does.", "USB-C passt andersrum. Alles passt andersrum."),
            start = listOf(Pad('1', at = 4 to 14), Pad('2', at = 16 to 14), Pad('3', at = 26 to 14)),
            traps = listOf(
                trap(Pressed('1'), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Left is right. Again.", "Polung vertauscht. Links ist rechts. Schon wieder.")),
                trap(Pressed('2'), Swap(false), say("Polarity restored. Probably.", "Polung wiederhergestellt. Vermutlich.")),
                trap(Pressed('3'), Swap(true), say("Polarity reversed again. Third time lucky.", "Polung schon wieder vertauscht. Dreimal ist Steckerrecht.")),
            ),
            // rematch: plug 2 only claims to fix the polarity, so the controls stay swapped all the way
            rematch = listOf(
                Round(
                    T("Unplugged and plugged back in. Classic.", "Aus- und wieder eingesteckt. Hilft immer. Mir."),
                    start = listOf(Pad('1', at = 4 to 14), Pad('2', at = 16 to 14), Pad('3', at = 26 to 14)),
                    traps = listOf(
                        trap(Pressed('1'), Play(Card.TWISTED), Swap(true), say("Polarity reversed. Old habit.", "Polung vertauscht. Alte Gewohnheit.")),
                        trap(Pressed('2'), say("Polarity restored. Probably not.", "Polung wiederhergestellt. Eher nicht.")),
                        trap(Pressed('3'), Swap(true), say("Plug 3: still reversed. Consistency.", "Stecker 3: immer noch verdreht. Konsequenz.")),
                    ),
                ),
            ),
        ) {
            border(); floor(); pit(8..11)
            put(22, 14, '^'); put(23, 14, '^')
            spawn(); door()
        },

        // 16 — act finale: a timed pad bridge, a flickering gate that changes rhythm, and a bit flip, in one go; the flip
        // comes back a moment after you land on the rail it saved you with, so do not stop on it
        Level(
            name = T("Motherboard", "Hauptplatine"),
            intro = T("Act one finale. All the parts, all the ways to die.", "Finale, Akt eins. Alle Teile, alle Wege zu sterben."),
            start = listOf(
                Circuit('a', on = false), Pad('1', at = 4 to 14, circuits = "a", mode = PadMode.ON),
                Clock('Z', on = 1.2f, off = 1.8f),
                Circuit('b'), Circuit('c', on = false),
            ),
            traps = listOf(
                trap(Pressed('1'), Power('a', false), say("The bridge is on a timer. Again.", "Die Brücke hat einen Timer. Schon wieder."), delay = 2.8f),
                trap(PastX(14.2f), Clock('Z', on = 0.9f, off = 1.6f), say("Gate firmware updated.", "Tor-Firmware aktualisiert.")),
                trap(PastX(21.5f), Play(Card.GRAND_FINALE), BitFlip('b', 'c'), say("Motherboard: 3 of 3 components hostile.", "Hauptplatine: 3 von 3 Bauteilen feindlich.")),
                trap(Landed(23.6f, 28f), BitFlip('b', 'c'), say("Component 4 of 3. I never could count.", "Bauteil 4 von 3. Zählen war nie meins."), delay = 0.5f),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            wire(17, 'Z')
            bridge(20..23, 'b'); bridge(24..27, 'c')
            spawn(); door()
        },
    )
}
