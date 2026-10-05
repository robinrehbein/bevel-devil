package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 2, levels 33-40 (block C of the V2 rollout, docs/LEVEL_DESIGN_V2.md §8 and §11). Act 3, "Root": the traps repeat,
 * flip, queue and sudo. Cards in this block: CRUMBLE 33, UPSIDE_DOWN 34, DECOY 34r2, COLLAPSE 35, DEVIL_SAW 36, HEADBUTT 37,
 * SINKING 38, SPIKE_SEED 39, STALKER 40, a bluff of CRUMBLE in 33r2.
 */
object World2Part3C {
    private val hidden = Glyph(spike = true, hidden = true)

    val levels: List<Level> = listOf(

        // 33 — sudo !! (a trap room: U14 the repeat is a lie, U1 the floor). Along the lane a hole opens in the floor (command 1), and the same
        // hole opens again as you land (sudo !!: louder). Up the stairs and back left along the deck, the repeat comes from above: a block drops
        // out of the ceiling as you pass, and as you hop it the spikes grow where the hop lands
        Level(
            name = T("sudo !!", "sudo !!"),
            intro = T("The last command is still warm.", "Der letzte Befehl ist noch warm."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(4.5f), Play(Card.CRUMBLE), Fall('a'), say("Command: make hole. Done.", "Befehl: Loch machen. Erledigt.")),
                trap(PastX(11.0f), Fall('b'), say("sudo !!  Again. Louder.", "sudo !!  Nochmal. Lauter.")),
                trap(Zone(15.6f, 4f, 16.4f, 9f), Fall('c'), say("sudo !!  As root, this time.", "sudo !!  Diesmal als root.")),
                trap(Zone(11.5f, 3f, 14.5f, 7.2f), Show('A'), say("sudo sudo !!  Spikes are the new hole.", "sudo sudo !!  Spikes sind das neue Loch.")),
            ),
            // rematch: the first hole is a bluff, and the hop you learned lands in the LEDs. The echo is late now (the hole opens while you are over
            // the LEDs), and the block that dropped when you stopped now drops late: whoever stops to look is under it, the one who runs is not
            rematch = listOf(
                Round(
                    T("Same command. Check your privileges.", "Gleicher Befehl. Prüf deine Rechte."),
                    legend = mapOf('E' to hidden),
                    traps = listOf(
                        trap(PastX(4.5f), Bluff(Card.CRUMBLE), say("Command: make hole. (Not this time.)", "Befehl: Loch machen. (Diesmal nicht.)")),
                        trap(PastX(6.5f), Show('E'), say("Spikes grow where your hop lands.", "Spikes wachsen, wo dein Hüpfer landet.")),
                        trap(Landed(12.2f, 15f), Fall('b'), say("sudo !!  The echo is late.", "sudo !!  Das Echo kommt spät.")),
                        trap(Zone(22f, 4f, 22.8f, 9f), Fall('c'), say("Do not stop to look. I will not.", "Bleib nicht stehen. Ich tu es auch nicht."), delay = 0.35f),
                    ),
                ) {
                    put(11, 14, 'E')
                    fill(14..15, 15..17, '#'); fill(17..18, 15..17, 'b')
                    fill(12..13, 1..2, '#'); fill(21..22, 1..2, 'c')
                    fill(20..21, 13..14, '.'); fill(22..27, 11..14, '.')
                    fill(20..22, 9..9); fill(23..24, 13..14); fill(25..29, 11..14)
                    put(4, 8, '^'); put(5, 8, '^')
                },
            ),
        ) {
            border(); floor()
            fill(1..19, 9..9)
            fill(8..9, 15..17, 'a'); fill(14..15, 15..17, 'b')
            fill(12..13, 1..2, 'c')
            fill(20..21, 13..14); fill(22..27, 11..14)
            put(4, 8, 'A'); put(5, 8, 'A')
            spawn(2, 14); door(2, 8)
        },

        // 34 — reverse proxy (a puzzle room: R3 the portals, R5 the floors, U10 the world turns over). The lane runs right and the gravity turns over
        // before the middle of it: you fall up onto the ceiling, the only way on, and the obvious way along it is a wall of links at the right end,
        // which is the loopback and hands you back at the start of the ceiling. The way on is back to the left: a dark link in the top left comes
        // up as you pass the middle of the ceiling again, and leads to a ledge, where the gravity is yours again, and from there it is the lane
        // to the door
        Level(
            name = T("Reverse Proxy", "Reverse Proxy"),
            intro = T("Everything goes through me here. Everything.", "Hier läuft alles über mich. Alles."),
            start = listOf(
                Portal('2', 24 to 1, 14 to 1, twoWay = false), Portal('4', 24 to 2, 14 to 1, twoWay = false),
                Portal('5', 24 to 3, 14 to 1, twoWay = false), Portal('6', 24 to 4, 14 to 1, twoWay = false),
                Portal('3', 6 to 1, 3 to 10, twoWay = false), Power('3', false),
            ),
            traps = listOf(
                trap(Zone(12f, 10f, 13.5f, 15.5f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Proxying your gravity.", "Deine Schwerkraft wird weitergeleitet.")),
                trap(Zone(10.4f, 0.5f, 11.8f, 4f), Power('3', true), say("Link 3 is up. It always was. Mostly.", "Link 3 ist oben. War er immer. Meistens.")),
                trap(Zone(2.5f, 9f, 5f, 11f), Gravity(false), say("Reverse, reverse.", "Rückwärts, rückwärts.")),
            ),
            // rematch: the gravity turns over at the far end of the lane, behind the LEDs, and the ceiling run goes left; the wall of links is gone,
            // and the only link is stale (the loopback again) until its DNS entry is renewed, a moment after you pass it: hold the loop, wait
            rematch = listOf(
                Round(
                    T("Cache cleared. Please reload.", "Cache geleert. Bitte neu laden."),
                    start = listOf(Portal('3', 17 to 1, 22 to 1, twoWay = false)),
                    traps = listOf(
                        trap(Zone(24f, 10f, 25.4f, 15.5f), Gravity(true), say("Flipped at the other end now. Old habits, cached.", "Jetzt am anderen Ende gekippt. Alte Gewohnheiten, im Cache.")),
                        trap(Zone(19.5f, 0.5f, 20.9f, 4f), Play(Card.DECOY), Reroute('3', 10 to 10), say("Link 3 is renewed. Give it a second.", "Link 3 wird erneuert. Gib ihm eine Sekunde."), delay = 1f),
                        trap(Zone(9f, 9f, 11.5f, 11f), Gravity(false), say("Lane again. Same door, same rules.", "Wieder die Bahn. Gleiche Tür, gleiche Regeln.")),
                    ),
                ) {
                    put(17, 14, '.'); put(18, 14, '.'); put(20, 14, '^'); put(21, 14, '^')
                    put(27, 14, '.'); put(28, 14, 'D'); fill(7..12, 11..11)
                },
            ),
        ) {
            border(); floor()
            fill(1..6, 11..11)
            leds(17..18)
            spawn(2, 14); door(27, 14)
        },
    )
}
