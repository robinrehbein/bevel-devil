package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.*
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Trigger.*

/**
 * World 1, levels 17-32. Act 2, "Neue Regeln": blinking platforms, path saws and the Idle trigger. Levels 17-24 are block B of the
 * V2 rebuild (World1Part2B), levels 25-32 block C (here): every room chains two to four traps, each of which punishes the
 * habit the one before taught. Levels with saws in `start` would show them in the plain-room check, so saws come with a trap.
 */
object World1Part2 {
    private val hiddenSpike = Glyph(spike = true, hidden = true)
    private val ceilingSpike = Glyph(spike = true, dir = Dir.DOWN)

    /**
     * The door runs from you in several hops (up, across, down), so it never flies through the player. The hops are chained
     * with delays from their flight times; [first] runs with the first hop.
     */
    private fun flee(trigger: Trigger, fromCol: Int, fromRow: Int, first: List<Action>, vararg hops: DoorTo): List<Trap> {
        var x = fromCol - 0.1f
        var y = fromRow + 1f - 1.6f
        var t = 0f
        return hops.mapIndexed { i, h ->
            val trap = Trap(trigger, (if (i == 0) first else emptyList()) + h, delay = t)
            val nx = h.col - 0.1f
            val ny = h.row + 1f - 1.6f
            t += kotlin.math.hypot(nx - x, ny - y) / h.speed + 0.03f
            x = nx; y = ny
            trap
        }
    }

    val levels: List<Level> = World1Part2B.levels + listOf(
        // 25 — rush hour: the door waits on the far platform behind a bridge of stones over the void. Step on it and it starts to blink;
        // reach for the door and it leaves for the middle of the first platform; come back over the bridge and it leaves again, for the
        // far end behind a second bridge. The way back is the bridge, now with its rhythm
        // MECHANIC: Blink (two bridges) + the one fleeing door of the act
        Level(
            name = T("Rush Hour", "Stoßzeit"),
            intro = T("Hurry! The door closes soon.", "Schnell, schnell! Die Tür schließt gleich."),
            hint = T("The door is not where it was. The bridge is on a timetable.", "Die Tür ist nicht mehr da. Die Brücke hat einen Fahrplan."),
            traps = listOf(
                trap(Touch('a'), Blink('a', on = 1.2f, off = 1.0f), Blink('b', on = 1.2f, off = 1.0f), Say(T("The bridge runs on the hour. The hour is short.", "Die Brücke fährt zur vollen Stunde. Die Stunde ist kurz."))),
                trap(Touch('c'), Blink('c', on = 1.6f, off = 1.2f), Blink('d', on = 1.6f, off = 1.2f), Say(T("Second bridge, same timetable. Delays included.", "Zweite Brücke, gleicher Fahrplan. Verspätungen inklusive."))),
            ) + flee(PastX(26.2f), 29, 7, listOf(Play(Card.SHY_DOOR), Say(T("This door is out of service. Next stop: back there.", "Diese Tür ist außer Betrieb. Nächster Halt: da hinten."))),
                DoorTo(29, 3, speed = 14f), DoorTo(15, 3, speed = 14f), DoorTo(15, 7, speed = 14f)) +
                flee(Landed(25f, 27f), 15, 7, listOf(Say(T("Oh, you came back? I'll move on.", "Du kommst zurück? Dann ziehe ich weiter."))),
                    DoorTo(15, 3, speed = 14f), DoorTo(2, 3, speed = 14f), DoorTo(2, 7, speed = 14f)),
        ) {
            border()
            fill(1..4, 8..8); fill(9..22, 8..8); fill(27..30, 8..8)
            fill(7..8, 9..9, 'c'); fill(5..6, 9..9, 'd')
            fill(23..24, 9..9, 'a'); fill(25..26, 9..9, 'b')
            put(10, 7, 'P'); put(29, 7, 'D')
        },

        // 26 — skyscraper: two flights of stairs and a roof run back along the top, and the crane follows you up, hanging over your
        // head. It doesn't mind a runner, it minds a stand-still (it drops its load), and under it you cannot jump. Rivets pop up on
        // the second floor and a guard slides at you on the roof: you have to outrun the crane before you can jump him
        // MECHANIC: Chase (the crane overhead, the guards on the floors)
        Level(
            name = T("Skyscraper", "Hochhaus"),
            intro = T("Top floor. The air is better up there.", "Oberste Etage. Da oben ist die Luft besser."),
            hint = T("The crane hates standing still. Don't jump under it: outrun it first.", "Der Kran hasst Stillstand. Spring nicht unter ihm: lauf ihm erst davon."),
            legend = mapOf('S' to ceilingSpike, 'A' to hiddenSpike),
            traps = listOf(
                trap(PastX(4f), Play(Card.STALKER), Chase('S', 5.5f, left = 9f, right = 26f), Say(T("The crane operator likes you. He follows you home.", "Der Kranführer mag dich. Er begleitet dich bis nach oben."))),
                trap(Landed(16f, 19.5f), Show('A'), Say(T("Rivets grow on the second floor.", "Im zweiten Stock wachsen Nieten."))),
                trap(Zone(21f, 3f, 26.5f, 5.6f), Chase('T', 3.2f, left = 0f, right = 12f), Say(T("Night watch on the roof. Very thorough.", "Nachtwache auf dem Dach. Sehr gründlich."))),
                trap(Idle(0.6f), Move('S', 0f, 11f, 30f), Say(T("I said keep moving. It's a crane, not a bench.", "Ich sagte: in Bewegung bleiben. Das ist ein Kran, keine Bank."))),
            ),
        ) {
            border(); floor()
            fill(4..9, 13..14); fill(10..15, 11..14); fill(16..30, 9..14); fill(27..30, 7..8)
            fill(2..26, 5..5)
            fill(9..11, 1..2, 'S')
            put(20, 8, 'A')
            fill(14..15, 4..4, 'T')
            put(2, 14, 'P'); put(2, 4, 'D')
        },

        // 27 — the answer: 0b101010. A cosmic ray flips bits in the floor: the floor ahead goes 111111 -> 101010, and when you land on the
        // third cell it flips again, so the pits fill in and the cells you trusted are gone; the last cell goes with a final flip
        // EASTER EGG: 42 / Hitchhiker's Guide / binary 101010 / bit flip (cosmic ray)
        Level(
            name = T("42", "42"),
            intro = T("The answer to everything. I forgot the question.", "Die Antwort auf alles. Die Frage habe ich vergessen."),
            hint = T("Ones become zeroes and zeroes become ones. Keep walking.", "Einsen werden zu Nullen und Nullen zu Einsen. Geh einfach weiter."),
            traps = listOf(
                trap(PastX(4.5f), Shake(0.5f)),
                trap(PastX(4.5f), Play(Card.COLLAPSE), Hide('b'), Hide('d'), Hide('f'), Say(T("Bit flip! Cosmic ray. Not my fault.", "Bit gekippt! Kosmische Strahlung. Nicht meine Schuld.")), delay = 0.3f),
                trap(Landed(11f, 13.99f), Hide('e'), Show('b'), Show('d'), Show('f'), Shake(0.5f), Say(T("And back. Zero is one now.", "Und zurück. Null ist jetzt eins."))),
                trap(Landed(11f, 13.99f), Hide('c'), delay = 0.6f),
                trap(Landed(20f, 22.99f), Hide('f'), Say(T("Six times nine, in base 13.", "Sechs mal neun, zur Basis 13.")), delay = 0.45f),
            ),
        ) {
            border(); floor(); pit(5..22)
            fill(5..7, 15..17, 'a'); fill(8..10, 15..17, 'b'); fill(11..13, 15..17, 'c')
            fill(14..16, 15..17, 'd'); fill(17..19, 15..17, 'e'); fill(20..22, 15..17, 'f')
            art(14, 3, "#.#", "#.#", "###", "..#", "..#")   // 4
            art(18, 3, "###", "..#", "###", "#..", "###")   // 2
            put(2, 14, 'P'); put(29, 14, 'D')
        },

        // 28 — gym class: the pommel horse (a saw runs laps around it, the top of the horse is the only safe spot and the dismount is the
        // trap), the jump rope (a saw swings up and down across the lane), the wall bars (a saw rolls in from behind as you land on the
        // first step: don't linger), then the cool-down lap back along the balcony, where a saw patrols and turns round in front of you.
        // Rematch: the horse is hot (the saw passes at head height: wait for it, then mount), the balcony has a gap and two saws
        // MECHANIC: PathSaw (lap, rope, patrol) + Saw (wall bars)
        Level(
            name = T("Gym Class", "Turnstunde"),
            intro = T("Mephi is training for a marathon. Don't disturb him.", "Mephi trainiert für den Marathon. Stör ihn nicht."),
            hint = T("Stand on the horse. Wait for the rope to go up. Don't linger on the wall bars. The balcony saw turns round.", "Steig aufs Pferd. Warte, bis das Seil oben ist. Bleib nicht an der Sprossenwand stehen. Die Säge auf dem Balkon dreht um."),
            traps = listOf(
                trap(PastX(2f), Play(Card.DEVIL_SAW), PathSaw(5.5f, 11f to 14.4f, 5f to 14.4f, 5f to 10.8f, 11f to 10.8f, loop = true), Say(T("Pommel horse. The saw does the laps.", "Pauschenpferd. Die Säge dreht die Runden."))),
                trap(PastX(12f), PathSaw(7f, 17f to 14.4f, 17f to 10.6f, delay = 0.9f), Say(T("Jump rope. You skip, the saw does not.", "Seilspringen. Du hüpfst, die Säge nicht."))),
                trap(Landed(22f, 25.9f), Saw(-1.5f, 12.4f, 14f, 0f, 0.62f), Say(T("Wall bars. Somebody is right behind you.", "Sprossenwand. Hinter dir steht schon einer."))),
                trap(Zone(17f, 6f, 25.9f, 9f), PathSaw(8f, 16f to 8.4f, 6f to 8.4f), Say(T("Cool-down lap. The saw cools down too. Eventually.", "Auslaufen. Die Säge läuft sich auch aus. Irgendwann."))),
            ),
            rematch = listOf(
                Round(
                    T("Rematch. Evening class: the horse is hot now.", "Revanche. Abendkurs: Das Pferd ist jetzt heiß."),
                    hint = T("Let the saw pass before you mount. Mind the gap in the balcony.", "Lass die Säge vorbei, bevor du aufsteigst. Achtung, Lücke im Balkon."),
                    traps = listOf(
                        trap(PastX(3.2f), Play(Card.GRAND_FINALE), PathSaw(5.5f, 5f to 11.9f, 11f to 11.9f, 11f to 14.4f, 5f to 14.4f, loop = true), Say(T("Evening class. Same horse, sharper lap.", "Abendkurs. Gleiches Pferd, schärfere Runde."))),
                        trap(PastX(12f), PathSaw(7f, 17f to 14.4f, 17f to 10.6f, delay = 0.9f)),
                        trap(Landed(22f, 25.9f), Saw(-1.5f, 12.4f, 14f, 0f, 0.62f)),
                        trap(Zone(17f, 6f, 25.9f, 9f), PathSaw(8f, 16f to 8.4f, 22f to 8.4f), PathSaw(8f, 3f to 8.4f, 7f to 8.4f),
                            Say(T("Two saws on the balcony. And a gap. Enjoy.", "Zwei Sägen auf dem Balkon. Und eine Lücke. Viel Spaß."))),
                    ),
                ) { fill(12..13, 9..9, '.') },
            ),
        ) {
            border(); floor()
            fill(7..9, 13..14)
            fill(22..25, 13..14); fill(26..30, 11..14)
            fill(2..25, 9..9)
            put(1, 14, 'P'); put(2, 8, 'D')
        },

    ) + World1Part2Old.levels.drop(12)
}
