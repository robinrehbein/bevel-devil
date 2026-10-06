package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Clock
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Heat
import com.robinrehbein.beveldevil.game.Action.HeatSpike
import com.robinrehbein.beveldevil.game.Action.Heatsink
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.AtDoor
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Heated
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Pressed
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

/**
 * World 3, act 2, block C (levels 17-24: Hot Plate, Full Load, Melt Fuse, Cold Start, Relay Race, Warm-up, Waiting Room, Cooling
 * Fins), rebuilt under the V2 level design (docs/LEVEL_DESIGN_V2.md, section 8). Each level is one idea in one dominant family;
 * the bot solutions are in the test sources ([World3RoomsC]).
 */
object World3PartC {
    val levels: List<Level> = listOf(

        // 17 — a stove on the top shelf and the way home underneath it. Start on the shelf: as you set off the far plate flares white (it
        // glows for a long time), so you stop on the heatsink for a breath; but the lid hanging over the sink comes down if you stay (do
        // not sit under it). Over the end of the shelf and down to the floor: a second lid hangs over where you land and lets go while you
        // run back under the shelf, and the floor in front of the door gives way as you pass.
        // Rematch: the sink is the one that flares, so stopping on it as before is the end. The plate is quick, too: hop across it. A lid
        // over the shelf and a lid under it, each to be waited out and hopped.
        Level(
            name = T("Hot Plate", "Herdplatte"),
            intro = T("It's just a stove. A very long one.", "Ist nur ein Herd. Ein sehr langer."),
            start = listOf(Heat('g', rise = 0.8f, cool = 5f), Heatsink('k', cools = "g")),
            traps = listOf(
                trap(PastX(2.9f), Play(Card.THROTTLE), Heat('g', rise = 0.8f, cool = 5f), HeatSpike('g', 1f), say("Energy saving mode: off.", "Energiesparmodus: aus.")),
                trap(Touch('k'), Fall('a'), say("Lid's on. Dinner is ready.", "Deckel drauf. Das Essen ist fertig."), delay = 0.8f),
                trap(Landed(24.5f, 31f), Fall('b'), say("A second lid. Pots come in pairs.", "Ein zweiter Deckel. Töpfe gibt es nur im Doppelpack."), delay = 0.25f),
                trap(Zone(12f, 12f, 14f, 15.2f), Fall('p'), say("The last tiles are a rental.", "Die letzten Kacheln sind gemietet."), delay = 0.2f),
            ),
            hint = T("The far plate is still glowing. Cool it on the sink, but do not sit under the lid.", "Die hintere Platte glüht noch. Kühl sie am Kühlkörper, aber setz dich nicht unter den Deckel."),
            rematch = listOf(
                Round(
                    T("Same stove. New chef.", "Gleicher Herd. Neue Köchin."),
                    start = listOf(Heat('g', rise = 0.5f, cool = 2f), Heatsink('k', cools = "g")),
                    hint = T("The sink is the hot one now. Hop it, and hop the plate.", "Jetzt ist der Kühlkörper der heiße. Spring drüber, und über die Platte auch."),
                    traps = listOf(
                        trap(PastX(2.9f), Play(Card.OVERCLOCKED), Heat('k', rise = 1f, cool = 6f), HeatSpike('k', 1f), say("The sink is on the menu now.", "Der Kühlkörper steht jetzt auf der Karte.")),
                        trap(PastX(16.4f), Fall('d'), say("Lids are seasonal.", "Deckel haben Saison."), delay = 0.3f),
                        trap(Landed(24.5f, 31f), Fall('b'), say("The lid over the landing. Again.", "Der Deckel über der Landung. Schon wieder."), delay = 0.25f),
                        trap(Zone(23f, 12f, 25f, 15.2f), Fall('c'), say("Third lid. I have a drawer full.", "Dritter Deckel. Ich hab eine Schublade voll."), delay = 0.35f),
                    ),
                ) {
                    fill(4..7, 1..2, '.')
                    fill(19..22, 1..2, 'd')
                    fill(17..20, 10..11, 'c')
                    fill(8..10, 15..17, '#')
                },
            ),
        ) {
            border(); floor()
            fill(1..23, 8..9)
            fill(6..7, 8..8, 'k'); fill(10..15, 8..8, 'g')
            fill(5..7, 1..2, 'a')
            fill(25..29, 1..2, 'b')
            pit(8..10); fill(8..10, 15..15, 'p')
            spawn(2, 7); door(3, 14)
        },

        // 18 — a chip under load heats all the time: cool it on the heatsink first, but the sink warms up under you; once you
        // are down on the chip it runs hotter, so the last tiles are only safe in the air, and the landing after them is hot
        Level(
            name = T("Full Load", "Volllast"),
            intro = T("Chips get hot when they think. This one never stops.", "Chips werden heiß, wenn sie denken. Der hier hört nie auf."),
            start = listOf(Heat('c', rise = 2.2f, load = true), Heatsink('k', cools = "c")),
            traps = listOf(
                trap(Landed(4.8f, 7.2f), Play(Card.OVERCLOCKED), HeatSpike('k', 0.55f), say("The heatsink has a fever.", "Der Kühlkörper hat Fieber.")),
                trap(Landed(7f, 16f), Heat('c', rise = 1.6f, load = true), say("Turbo boost. For the chip, not for you.", "Turbo-Boost. Für den Chip, nicht für dich.")),
                trap(PastX(24.3f), HeatSpike('f', 1f), say("Bonus round: the landing is lava. Mildly.", "Bonusrunde: Die Landung ist Lava. Mild.")),
            ),
        ) {
            border(); floor()
            fill(5..6, 13..14, 'k')
            bridge(9..22, 'c')
            fill(26..28, 15..15, 'f')
            spawn(); door()
        },


        // 19 — melting stones: each one is gone for good if you stand on it too long; the next ones are already warm
        Level(
            name = T("Melt Fuse", "Schmelzsicherung"),
            intro = T("Lead-free solder. Also free of mercy.", "Bleifreies Lot. Und gnadenfrei."),
            start = listOf(Heat('m', rise = 0.9f, melt = true), Heat('n', rise = 0.9f, melt = true), Heat('o', rise = 0.9f, melt = true)),
            traps = listOf(
                trap(Touch('n'), Play(Card.GHOST_BLOCK), HeatSpike('o', 0.85f), say("The last stone was already warm. Sorry.", "Der letzte Stein war schon warm. Sorry.")),
                trap(Landed(14f, 18f), Heat('o', rise = 0.5f, melt = true), say("Solder with a lower melting point.", "Lot mit niedrigerem Schmelzpunkt.")),
                trap(Airborne(21.5f, 26.5f), HeatSpike('g', 0.7f), say("Dry land. Dry and warm.", "Festland. Trocken und warm.")),
            ),
        ) {
            border(); floor(); pit(7..25)
            fill(8..11, 15..15, 'm'); fill(14..17, 15..15, 'n'); fill(20..23, 15..15, 'o'); fill(26..27, 15..15, 'g')
            spawn(); door()
        },


        // 20 — the plate that glows is cool; the plain floor is what burns; as you land behind the last plate the door flies home
        // to the cool plate, and the floor on the way back is overclocked again: wait for it to cool
        Level(
            name = T("Cold Start", "Kaltstart"),
            intro = T("Nice and cool here. Take your time.", "Schön kühl hier. Lass dir Zeit."),
            start = listOf(Heat('h', rise = 14f, cool = 1f)),
            traps = listOf(
                trap(PastX(13.2f), Play(Card.OVERCLOCKED), HeatSpike('f', 0.7f), say("Overclocked! Factory settings: mine.", "Übertaktet! Werkseinstellung: meine.")),
                trap(Airborne(23.4f, 26f), HeatSpike('g', 0.75f), DoorTo(29, 12, speed = 20f), say("Twice! It's a feature.", "Nochmal! Ist ein Feature.")),
                // the door hovers within jumping reach until you land, then flies over your head to the glowing plate
                trap(
                    Landed(25.5f, 28.6f), DoorTo(7, 14, speed = 30f), HeatSpike('f', 1f), Heat('f', rise = 1.5f, cool = 3f),
                    say("The door prefers the cool plate. The way back is overclocked.", "Die Tür mag die kühle Platte. Der Rückweg ist übertaktet."),
                ),
            ),
        ) {
            border(); floor()
            fill(5..9, 15..15, 'h')
            fill(13..19, 15..15, 'f'); fill(23..25, 15..15, 'g')
            spawn(); door()
        },


        // 21 — three plates that share their heat and two sinks between them; the sinks make the plates nervous, the last leg
        // turns up as you leave the second sink (leap from the sink), and the floor after it is overclocked
        Level(
            name = T("Relay Race", "Staffellauf"),
            intro = T("Three plates, two heatsinks. Do the math.", "Drei Platten, zwei Kühlkörper. Rechne nach."),
            start = listOf(Heat('h', rise = 1f), Heatsink('k', cools = "h")),
            traps = listOf(
                trap(Touch('k'), Play(Card.THROTTLE), Heat('h', rise = 0.85f, cool = 1.4f), say("Plates are warmed up. So to speak.", "Platten sind warmgelaufen. Sozusagen.")),
                trap(PastX(19.2f), Heat('h', rise = 0.55f, cool = 1.4f), say("Last leg: sprint mode.", "Letzte Etappe: Sprintmodus.")),
                trap(PastX(26.2f), HeatSpike('f', 0.9f), say("Plate 4 is a plain floor. I counted.", "Platte 4 ist ein schlichter Boden. Ich hab mitgezählt.")),
            ),
            // rematch: the throttle is old news; the card is the landing: the leap off the second sink lands on an
            // overclocked plate. The last leg is walkable now
            rematch = listOf(
                Round(
                    T("Second lap. The baton is hot.", "Zweite Runde. Staffelübergabe mit Brandblase."),
                    start = listOf(Heat('h', rise = 1f), Heatsink('k', cools = "h")),
                    traps = listOf(
                        trap(Touch('k'), Heat('h', rise = 0.85f, cool = 1.4f), say("Plates warmed up. Again.", "Platten warmgelaufen. Schon wieder.")),
                        trap(Landed(19f, 25f), Play(Card.OVERCLOCKED), HeatSpike('h', 0.95f), say("Long jump? Hot landing.", "Weitsprung? Heiße Landung.")),
                        trap(PastX(26.2f), HeatSpike('f', 0.9f), say("Plate 4: still counted.", "Platte 4: immer noch mitgezählt.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(5..10, 15..15, 'h'); put(11, 15, 'k'); fill(12..17, 15..15, 'h'); put(18, 15, 'k'); fill(19..24, 15..15, 'h')
            fill(27..28, 15..15, 'f')
            spawn(); door()
        },


        // 22 — the chip warms up; linger on it and it goes to full load; a fan blade rolls in, so hop it without slowing down;
        // run on and the floor behind it is the problem
        Level(
            name = T("Warm-up", "Warmlaufen"),
            intro = T("A warm chip is a happy chip.", "Ein warmer Chip ist ein glücklicher Chip."),
            start = listOf(Heat('c', rise = 4f, load = true)),
            traps = listOf(
                trap(Heated('c', 0.7f), HeatSpike('c', 1f), say("Thermal throttling: the chip throttles YOU.", "Thermische Drosselung: der Chip drosselt DICH.")),
                trap(PastX(6f), Saw(33f, 14.4f, -9f, 0f), say("Fan blade. Stopping is not an option.", "Lüfterblatt. Anhalten ist keine Option.")),
                trap(PastX(23.4f), Play(Card.OVERCLOCKED), HeatSpike('f', 0.8f), say("Overclocked on the far side, too.", "Auch auf der anderen Seite übertaktet.")),
            ),
        ) {
            border(); floor()
            bridge(9..20, 'c')
            fill(24..27, 15..15, 'f')
            spawn(); door()
        },


        // 23 — the plate is lukewarm until you step on it; then Mephi turns it up, so waiting for the rail is not an option there
        Level(
            name = T("Waiting Room", "Wartezimmer"),
            intro = T("Take a seat. The plate is lukewarm.", "Setz dich ruhig. Die Platte ist lauwarm."),
            start = listOf(Heat('h', rise = 3f), Clock('a', on = 2.4f, off = 2f, phase = 2f)),
            traps = listOf(
                trap(PastX(8.3f), Play(Card.SINKING), Heat('h', rise = 0.8f), say("Set to Sauna. Sitting not recommended.", "Stufe Sauna. Sitzen nicht empfohlen.")),
                trap(Touch('a'), Clock('a', on = 2.2f, off = 2f), say("The rail got a new appointment schedule.", "Die Schiene hat einen neuen Terminplan.")),
                trap(Airborne(24.3f, 28.5f), HeatSpike('f', 0.7f), say("Next appointment: preheated.", "Nächster Termin: vorgeheizt.")),
            ),
        ) {
            border(); floor()
            fill(8..11, 15..15, 'h')
            bridge(12..22, 'a')
            fill(26..28, 15..15, 'f')
            spawn(); door()
        },


        // 24 — cooling fins: hot plates as a staircase up to the door; they heat faster after the first landing, the top ledge is overclocked
        Level(
            name = T("Cooling Fins", "Kühlrippen"),
            intro = T("Nice view from up there.", "Schöne Aussicht von da oben."),
            start = listOf(Heat('h', rise = 0.8f)),
            traps = listOf(
                trap(Landed(5f, 9f), Play(Card.THROTTLE), Heat('h', rise = 0.6f), say("Fins upgraded: now with extra heat.", "Rippen aufgerüstet: jetzt mit Extrawärme.")),
                trap(Airborne(24f, 28f), HeatSpike('g', 0.7f), say("The summit is warm.", "Der Gipfel ist warm.")),
                trap(Idle(1.3f), HeatSpike('h', 1f), say("Sitting on a plate. Bold.", "Auf einer Platte sitzen. Mutig.")),
            ),
        ) {
            border(); floor()
            fill(5..8, 13..13, 'h'); fill(11..14, 11..11, 'h'); fill(17..20, 9..9, 'h'); fill(23..26, 7..7, 'h')
            fill(28..30, 5..5, 'g')
            spawn(); door(29, 4)
        },

    )
}
