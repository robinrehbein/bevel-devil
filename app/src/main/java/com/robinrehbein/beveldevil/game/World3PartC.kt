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
                trap(Zone(2.9f, 3f, 4f, 9f), Play(Card.THROTTLE), Heat('g', rise = 0.8f, cool = 5f), HeatSpike('g', 1f), say("Energy saving mode: off.", "Energiesparmodus: aus.")),
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
                        trap(Zone(2.9f, 3f, 4f, 9f), Play(Card.OVERCLOCKED), Heat('k', rise = 1f, cool = 6f), HeatSpike('k', 1f), say("The sink is on the menu now.", "Der Kühlkörper steht jetzt auf der Karte.")),
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

        // 18 — the door is right there, behind a copper wall, and the switch that opens it is on the far side of a chip under load. The chip
        // heats all the time and is the only way across the pit, so go over at once (a blade comes along the chip, hop it). The switch
        // opens the wall, turns the load up and sends a blade after you; so the way back is over the same chip, now burning: cool it on
        // the heatsink on the far side, but not for long.
        Level(
            name = T("Full Load", "Volllast"),
            intro = T("Chips get hot when they think. This one never stops.", "Chips werden heiß, wenn sie denken. Der hier hört nie auf."),
            start = listOf(Heat('c', rise = 2.1f, load = true), Heatsink('m', cools = "c"), Circuit('w'), Pad('1', at = 27 to 14, circuits = "w", mode = PadMode.OFF)),
            traps = listOf(
                trap(PastX(9f), Saw(33f, 14.4f, -7f, 0f), say("Fan blade, express. It comes with the chip.", "Lüfterblatt, Express. Gehört zum Chip."), delay = 0.25f),
                trap(Pressed('1'), Play(Card.DEVIL_SAW), Saw(35.5f, 14.4f, -6f, 0f), Heat('c', rise = 1.6f, load = true), say("Pressed. The lock is open, the load is up, the blade is loose.", "Gedrückt. Das Schloss ist offen, die Last oben, das Messer los."), delay = 0.6f),
            ),
            hint = T("Cross the chip at once, it only gets hotter. On the way back cool it on the sink first, but the blade is on your heels.", "Geh sofort über den Chip, er wird nur heißer. Zurück erst am Kühlkörper kühlen, aber das Messer ist dir auf den Fersen."),
        ) {
            border(); floor()
            fill(3..3, 1..14, 'w')
            fill(22..23, 15..15, 'm')
            pit(11..20); fill(11..20, 15..15, 'c')
            spawn(5, 14); door(2, 14)
        },

        // 19 ★ — a breather with one gag: the floor is a floor until you walk on it. As you set off the solder between three copper
        // stones lets go (the stones were the only part of the floor that was meant to stay), the first stone flares under whoever lands
        // on it and the second one gives way. Keep hopping; the third one is only slowly melting, as promised.
        Level(
            name = T("Melt Fuse", "Schmelzsicherung"),
            intro = T("Lead-free solder. Also free of mercy.", "Bleifreies Lot. Und gnadenfrei."),
            start = listOf(Heat('m', rise = 2.2f, melt = true), Heat('n', rise = 2.2f, melt = true), Heat('o', rise = 2.2f, melt = true)),
            traps = listOf(
                trap(PastX(4f), Fall('f'), say("Floor plan: three stones. The rest was a rumor.", "Grundriss: drei Steine. Der Rest war ein Gerücht.")),
                trap(Landed(10f, 12f), HeatSpike('m', 1f), say("Stone one: a little warm. Under you.", "Stein eins: ein bisschen warm. Unter dir."), delay = 0.3f),
                trap(Landed(15f, 17f), Play(Card.SINKING), Fall('n'), say("Stone two: solid ground, limited offer.", "Stein zwei: fester Boden, nur kurz gültig."), delay = 0.3f),
            ),
            hint = T("Do not stop on a stone. Any stone.", "Auf keinem Stein stehen bleiben. Auf keinem."),
        ) {
            border(); floor()
            fill(10..11, 15..17, 'm'); fill(15..16, 15..17, 'n'); fill(20..21, 15..17, 'o')
            fill(7..9, 15..17, 'f'); fill(12..14, 15..17, 'f'); fill(17..19, 15..17, 'f'); fill(22..23, 15..17, 'f')
            spawn(2, 14); door(29, 14)
        },

        // 20 — the glowing plates are cool and the plain floor between them is the part that burns: hop from plate to plate. When you are on
        // your way to the door it flies home to the start, and the plates have turned: they are the hot ones now, the plain floor is cool.
        // The way back is hopped the other way round, landing where you used to jump over.
        Level(
            name = T("Cold Start", "Kaltstart"),
            intro = T("Nice and cool here. Take your time.", "Schön kühl hier. Lass dir Zeit."),
            start = listOf(Heat('h', rise = 14f, cool = 3f)),
            traps = listOf(
                trap(PastX(5.8f), Heat('f', rise = 0.8f, cool = 0.8f), HeatSpike('f', 1f), say("Plain floor. That is the hot kind.", "Schlichter Boden. Das ist die heiße Sorte.")),
                trap(PastX(12.2f), Heat('g', rise = 0.8f, cool = 0.8f), HeatSpike('g', 1f), say("The glow is the cool part. Everyone gets that backwards.", "Das Glühen ist der kühle Teil. Das verwechselt jeder.")),
                trap(PastX(19.2f), Heat('i', rise = 0.8f, cool = 0.8f), HeatSpike('i', 1f), say("One more stretch of plain floor. Do not read the room, read the floor.", "Noch ein Stück schlichter Boden. Lies nicht den Raum, lies den Boden.")),
            ) + doorTrail(
                PastX(27.6f), 30, 14,
                listOf(DoorTo(30, 1, 24f, hanging = true), DoorTo(2, 1, 24f, hanging = true), DoorTo(2, 14, 24f)),
                first = listOf(Play(Card.SHY_DOOR), say("The door has plans. The plates have changed theirs.", "Die Tür hat Pläne. Die Platten haben ihre geändert.")),
            ) + listOf(
                trap(Landed(24.5f, 27.7f), HeatSpike('h', 1f), delay = 1.3f),
            ),
            hint = T("The glowing plates are the cool ones. When the door leaves, they are not any more.", "Die glühenden Platten sind kühl. Wenn die Tür geht, sind sie es nicht mehr."),
        ) {
            border(); floor()
            fill(4..6, 15..15, 'h'); fill(11..13, 15..15, 'h'); fill(18..20, 15..15, 'h'); fill(25..27, 15..15, 'h')
            fill(7..10, 15..15, 'f'); fill(14..17, 15..15, 'g'); fill(21..24, 15..15, 'i')
            spawn(1, 14); door(30, 14)
        },

        // 21 — two storeys: the stove under the shelf, the door on the shelf above where you start. A wall of fins that has been waiting behind
        // you sets off as you step onto the stove; stop on the heatsink for a breath (the far plate flares as you do) and it gains on you, so
        // do not stay. At the far end two steps lead up and back onto the shelf, and a fin that was only a bump on the shelf slides at whoever
        // lands there.
        // Rematch: the heatsink is the trap: it sends the wall at full speed, and the whole stove is on for good. Hop it on the blocks.
        Level(
            name = T("Relay Race", "Staffellauf"),
            intro = T("Three runners, one baton. Guess who is holding it.", "Drei Läufer, ein Staffelholz. Rate, wer es hält."),
            legend = mapOf('S' to Glyph(spike = true, dir = Dir.RIGHT)),
            start = listOf(Heat('a', rise = 1.0f), Heat('b', rise = 1.05f, cool = 1.0f), Heatsink('k', cools = "ab")),
            traps = listOf(
                trap(PastX(5.3f), Play(Card.STALKER), Chase('S', 4f, 0f, 12f), say("The next runner is right behind you. Do not wait for him.", "Der nächste Läufer ist direkt hinter dir. Warte nicht auf ihn.")),
                trap(Touch('k'), HeatSpike('b', 1f), say("Hand-over zone: the next plate is not ready.", "Wechselzone: Die nächste Platte ist noch nicht so weit.")),
                trap(PastX(19f), Chase('S', 9f, 0f, 20f), say("The runner behind you just found his second wind.", "Der Läufer hinter dir hat seinen zweiten Atem gefunden.")),
                trap(Zone(10f, 6f, 20.9f, 10.5f), Chase('U', 4.5f, 10f, 9f), say("Last leg. The baton runs towards you.", "Letzte Etappe. Das Staffelholz läuft dir entgegen.")),
            ),
            hint = T("Cool the far plate on the sink, but the wall behind you does not wait. Upstairs, hop what comes at you.", "Kühl die hintere Platte am Kühlkörper, aber die Wand hinter dir wartet nicht. Oben spring über das, was auf dich zuläuft."),
            rematch = listOf(
                Round(
                    T("Same baton. New hand-over.", "Gleiches Staffelholz. Neue Übergabe."),
                    hint = T("The sink hands the wall over at full speed now. Hop the stove on the blocks, and do not stand on the first one: it heats up.", "Der Kühlkörper übergibt die Wand jetzt mit Vollgas. Spring auf den Klötzen über den Herd, und bleib nicht auf dem ersten stehen: Er wird heiß."),
                    traps = listOf(
                        trap(Zone(4.4f, 13.5f, 5.2f, 15.2f), Heat('q', rise = 1.0f, cool = 3f), say("The blocks are hot-swappable. Please do not stand on them.", "Die Klötze sind hot-swap-fähig. Bitte nicht draufstellen.")),
                        trap(Landed(6f, 9.8f), HeatSpike('q', 1f), say("Block one: a little warm. Under you.", "Klotz eins: ein bisschen warm. Unter dir."), delay = 0.55f),
                        trap(PastX(5.3f), Chase('S', 4f, 0f, 12f), say("The next runner is behind you again.", "Der nächste Läufer ist wieder hinter dir.")),
                        trap(Touch('k'), Play(Card.COLLAPSE), Move('S', 11f, 0f, 16f), say("Hand-over zone, full speed.", "Wechselzone, volle Fahrt.")),
                        trap(PastX(19f), Chase('S', 9f, 0f, 20f), say("The runner behind you just found his second wind.", "Der Läufer hinter dir hat seinen zweiten Atem gefunden.")),
                        trap(Zone(10f, 6f, 20.9f, 10.5f), Chase('U', 4.5f, 10f, 9f), say("Last leg. The baton runs towards you.", "Letzte Etappe. Das Staffelholz läuft dir entgegen.")),
                    ),
                ) {
                    put(11, 15, 'b')
                    fill(6..9, 14..14, 'q'); fill(11..16, 14..14)
                    put(4, 14, '.'); spawn(3, 14); put(4, 9, '.'); door(2, 9)
                },
            ),
        ) {
            border(); floor()
            fill(1..20, 10..11)
            fill(6..9, 15..15, 'a'); fill(10..11, 15..15, 'k'); fill(12..18, 15..15, 'b')
            fill(1..2, 13..14, 'S')
            fill(22..23, 13..14); fill(24..29, 11..14)
            put(10, 9, 'U')
            spawn(4, 14); door(4, 9)
        },

        // 22 — a three-storey serpentine: along the top slab (the plates in front of you light up: hop them), down the right shaft, back
        // along the middle slab (the spiked strip that has been waiting at the far end slides at whoever lands), down the left shaft and along
        // the floor to the door: another hot patch right where you land, and a second strip that slides out from behind the door.
        Level(
            name = T("Warm-up", "Warmlaufen"),
            intro = T("A warm chip is a happy chip.", "Ein warmer Chip ist ein glücklicher Chip."),
            start = listOf(Heat('p', rise = 0.9f, cool = 1.2f), Heat('q', rise = 0.2f, cool = 0.4f)),
            traps = listOf(
                trap(PastX(7.5f), HeatSpike('p', 1f), say("Warm-up exercise one: do not touch the warm part.", "Aufwärmübung eins: Den warmen Teil nicht anfassen.")),
                trap(Zone(22f, 8f, 30f, 10f), Play(Card.HEADBUTT), Move('F', 17f, 0f, 5f), say("The strip has been waiting at the far end. It missed you.", "Der Streifen hat am anderen Ende gewartet. Du hast ihm gefehlt.")),
                trap(Zone(10.3f, 13.9f, 11.6f, 15.2f), HeatSpike('q', 1f), say("Exercise two. The same warm part, one floor down.", "Übung zwei. Der gleiche warme Teil, ein Stockwerk tiefer.")),
                trap(Zone(15.5f, 13f, 17f, 15.2f), Move('G', -16f, 0f, 5f), say("Cool-down: a strip from the other side of the door.", "Abkühlen: ein Streifen von der anderen Seite der Tür.")),
            ),
            hint = T("Three floors down, and back to the door. Hop the warm plates and whatever slides at you.", "Drei Stockwerke runter, und zurück zur Tür. Spring über die warmen Platten und alles, was auf dich zugleitet."),
        ) {
            border(); floor()
            fill(1..19, 4..5); fill(11..30, 9..10)
            fill(11..14, 4..4, 'p'); fill(12..13, 15..15, 'q')
            fill(11..11, 8..8, 'F'); fill(27..27, 14..14, 'G')
            spawn(2, 3); door(29, 14)
        },

        // 23 — you start on a little tower and step off it into a long ground floor under a ceiling of slabs. Three numbers are called, one
        // after the other: each time a whole slab lets go just ahead of you and comes down just as you would be underneath (stop, wait for
        // it to land, climb on, walk across). The last one is a staircase: the door sits on a ledge (a warm plate: do not dawdle) that is a hop too high without it.
        // Waiting is what the room asks for.
        Level(
            name = T("Waiting Room", "Wartezimmer"),
            intro = T("Please take a seat. You will be called.", "Bitte nehmen Sie Platz. Sie werden aufgerufen."),
            start = listOf(Heat('p', rise = 1.0f)),
            traps = listOf(
                trap(Landed(4.4f, 8f), Play(Card.CRUMBLE), Fall('a'), say("Number one, please.", "Nummer eins, bitte."), delay = 0.6f),
                trap(Landed(17.4f, 20.5f), Fall('m'), say("Number two. Wait, I said.", "Nummer zwei. Warten, hatte ich gesagt."), delay = 0.5f),
                trap(Touch('m'), Fall('e'), say("Number three. Take the stairs.", "Nummer drei. Nehmen Sie die Treppe."), delay = 0.35f),
            ),
            hint = T("Do not walk under what hangs over you. Wait for it to land, then climb on.", "Lauf nicht unter dem durch, was über dir hängt. Warte, bis es unten liegt, und klettere drauf."),
        ) {
            border(); floor()
            fill(1..3, 6..14)
            fill(10..26, 8..9)
            fill(10..16, 10..10, 'a'); fill(22..24, 10..10, 'm'); fill(26..26, 10..11, 'e')
            fill(27..30, 11..14); fill(27..30, 11..11, 'p')
            spawn(1, 5); door(29, 10)
        },

        // 24 — mirrored for once: you start on a tower at the right and the door is at the far left, on a ledge two hops up. Two sets of
        // cooling fins (hot plates) lie across the floor; each flares as you come down to it and takes about a second to cool. Wait on the
        // cold stone in front of it, then cross it quickly: it heats up again under your feet. The last set is not a plate at all: a live
        // trace that pulses on and off. Cross it while it is dark.
        Level(
            name = T("Cooling Fins", "Kühlrippen"),
            intro = T("Nice view from up there.", "Schöne Aussicht von da oben."),
            start = listOf(Heat('a', rise = 0.8f, cool = 1.3f), Heat('b', rise = 0.8f, cool = 1.3f), Circuit('Z', on = false)),
            traps = listOf(
                trap(Zone(20.5f, 13f, 28f, 15.2f), HeatSpike('a', 1f), say("Fins upgraded: now with extra heat.", "Rippen aufgerüstet: jetzt mit Extrawärme.")),
                trap(Zone(14.4f, 13f, 16.0f, 15.2f), HeatSpike('b', 1f), say("The second set was just plugged in.", "Der zweite Satz wurde gerade eingesteckt.")),
                trap(Zone(8f, 13f, 9.9f, 15.2f), Play(Card.SHORT_CIRCUIT), Clock('Z', on = 0.7f, off = 2.0f), say("The last set is wired differently. It blinks. Politely.", "Der letzte Satz ist anders verdrahtet. Er blinkt. Höflich.")),
            ),
            hint = T("Wait on the stone until the fins have cooled. The last set pulses: cross it while it is dark.", "Warte auf dem Stein, bis die Rippen abgekühlt sind. Der letzte Satz pulsiert: lauf drüber, solange er dunkel ist."),
        ) {
            border(); floor()
            fill(28..30, 6..14); fill(1..2, 11..14); fill(3..3, 13..14)
            fill(16..18, 15..15, 'a'); fill(10..12, 15..15, 'b'); fill(4..6, 14..14, 'Z')
            spawn(30, 5); door(1, 10)
        },

    )
}
