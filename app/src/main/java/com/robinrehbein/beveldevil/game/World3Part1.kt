package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.BitFlip
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.Airborne
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
object World3Part1 {
    private val hidden = Glyph(spike = true, hidden = true)

    private val rest: List<Level> = listOf(

        // 9 — the pad powers the bridge and the trace wall behind it; a second pad on the bridge cuts the wall, for a while
        Level(
            name = T("Side Effect", "Nebenwirkung"),
            intro = T("Ask your sysadmin or your devil about side effects.", "Zu Risiken und Nebenwirkungen fragen Sie Ihren Teufel."),
            start = listOf(
                Circuit('a', on = false), Circuit('Z', on = false),
                Pad('1', at = 4 to 14, circuits = "aZ"),
                Pad('2', at = 10 to 14, circuits = "Z", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(Pressed('2'), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Side effects include: resets.", "Nebenwirkungen: Neustarts."), delay = 0.9f),
                trap(PastX(16.5f), HeatSpike('f', 0.8f), say("Side effects include: warm feet.", "Nebenwirkungen: warme Füße.")),
                trap(Airborne(16.3f, 20.5f), HeatSpike('g', 0.7f), say("Also: warm landings.", "Außerdem: warme Landungen.")),
            ),
        ) {
            border(); floor()
            bridge(8..13, 'a')
            wire(15, 'Z')
            fill(17..20, 15..15, 'f'); fill(21..22, 15..15, 'g')
            spawn(); door()
        },

        // 10 — stepping stones on clocks: hop over on the beat; the first stone goes on a short shift once you land on it, so
        // waiting there for the next beat is out (watch it from the floor); the last stone speeds up once you stand on the middle one
        Level(
            name = T("Metronome", "Metronom"),
            intro = T("Step to the beat. Mine.", "Tritt im Takt. In meinem."),
            start = listOf(Clock('a', on = 2.4f, off = 1f), Clock('b', on = 2.4f, off = 1f, phase = 1.8f), Clock('c', on = 2.4f, off = 1f)),
            traps = listOf(
                trap(Landed(7.6f, 13f), Clock('a', on = 1.2f, off = 1.0f), say("Stone one: clocked in, short shift.", "Stein eins: eingestempelt, Kurzschicht.")),
                trap(Landed(14f, 20.5f), Clock('c', on = 1.4f, off = 1.2f), say("Stone three went up-tempo.", "Stein drei ist auf Allegro umgestiegen.")),
                trap(Touch('e'), Play(Card.SINKING), Fall('e'), say("The last tile was on a timer too.", "Die letzte Kachel hatte auch einen Timer."), delay = 0.3f),
            ),
        ) {
            border(); floor()
            put(27, 15, 'e'); fill(27..27, 16..17, 'e')
            pit(7..26)
            fill(8..12, 15..15, 'a'); fill(15..19, 15..15, 'b'); fill(22..26, 15..15, 'c')
            spawn(); door()
        },

        // 11 — a dark cable in the path goes live when you come near; and the one behind the landing does too
        Level(
            name = T("Loose Cables", "Kabelsalat"),
            intro = T("Tidy cabling. I did it myself.", "Saubere Verkabelung. Hab ich selbst gemacht."),
            start = listOf(Circuit('Z', on = false), Circuit('Y', on = false)),
            traps = listOf(
                trap(PastX(12f), Play(Card.SHORT_CIRCUIT), Power('Z', true), say("Who said it was dead? Oh right, I did.", "Wer sagt, dass er tot ist? Ach ja, ich.")),
                trap(Landed(15.5f, 17.8f), Power('Y', true), say("And its sibling.", "Und sein Geschwisterkabel.")),
                trap(Airborne(18.5f, 23f), HeatSpike('g', 0.7f), say("The floor went and got warm.", "Der Boden ist auch warm geworden.")),
            ),
        ) {
            border(); floor()
            fill(13..14, 14..14, 'Z')
            fill(18..19, 14..14, 'Y'); fill(21..22, 15..15, 'g')
            spawn(); door()
        },

        // 12 — do not press: the button is a bluff; the ceiling drops when you run under it, and a fan blade rolls in behind the wall
        Level(
            name = T("The Button", "Der Knopf"),
            intro = T("A very ordinary button.", "Ein ganz gewöhnlicher Knopf."),
            start = listOf(Pad('1', at = 8 to 14)),
            traps = listOf(
                trap(Pressed('1'), say("Nothing happened. Suspicious, isn't it?", "Nichts passiert. Verdächtig, oder?")),
                trap(PastX(16.5f), Play(Card.HEADBUTT), Fall('c'), say("The button was a decoy. The ceiling is real.", "Der Knopf war Attrappe. Die Decke ist echt.")),
                trap(Airborne(19f, 23f), Saw(33f, 14.4f, -7f, 0f), say("Delivery! Fan blade, one piece, rolling.", "Lieferung! Lüfterblatt, ein Stück, rollend.")),
            ),
            // rematch: the ceiling card is a bluff, so there is no slab to stand on while the blade, now early, rolls through
            rematch = listOf(
                Round(
                    T("Same button. Different mood.", "Gleicher Knopf. Er hat heute Montag."),
                    start = listOf(Pad('1', at = 8 to 14)),
                    traps = listOf(
                        trap(PastX(16.5f), Bluff(Card.HEADBUTT)),
                        trap(PastX(21.5f), Saw(33f, 14.4f, -7f, 0f), say("Express delivery.", "Expresslieferung.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(20..22, 9..10, 'c')
            spawn(); door()
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

    /** Levels 1-8 are the rebuilt block ([World3PartA]); levels 9-16 are the old chain. */
    val levels: List<Level> = World3PartA.levels + rest
}
