package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Fall
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
    )
}
