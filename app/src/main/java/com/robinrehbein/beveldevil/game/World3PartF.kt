package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.*
import com.robinrehbein.beveldevil.game.Trigger.*

/**
 * World 3, act 3, block F (levels 41-48: Air Bridge, Exhaust, Wiring Diagram, Display, Cold Air, POST, Boot Order, BIOS Setup), rebuilt
 * under the V2 level design (docs/LEVEL_DESIGN_V2.md, section 8). The bot solutions are in the test sources ([World3RoomsF]).
 */
object World3PartF {
    val levels: List<Level> = listOf(

        // 41 — Air Bridge
        Level(
            name = T("Air Bridge", "Luftbrücke"),
            intro = T("Two fans, one door. The door is very high.", "Zwei Lüfter, eine Tür. Die Tür ist sehr hoch."),
            start = listOf(
                Fan('f', at = 4 to 15, dir = Dir.UP, reach = 6, speed = 3.0f, width = 2),
                Fan('g', at = 21 to 10, dir = Dir.UP, reach = 6, speed = 3.0f, width = 3),
            ),
            traps = listOf(
                trap(Landed(10f, 12f), Play(Card.SINKING), Fall('b'), say("The bridge is built one plank ahead of you.", "Die Brücke wird eine Planke vor dir gebaut. Oder abgebaut.")),
                trap(Landed(14f, 16f), Hide('d'), say("Planning permission: revoked.", "Baugenehmigung: widerrufen.")),
                trap(Zone(21f, 5.8f, 24f, 8f), FanSet('g', 6f), say("The lift has a turbo. You're welcome.", "Der Aufzug hat einen Turbo. Gern geschehen.")),
                trap(Touch('e'), Fall('e'), say("The landing strip is on loan.", "Die Landebahn ist geliehen."), delay = 0.4f),
            ),
            hint = T("The planks ahead are the ones that go. Hop as soon as you land, and hold on to the wall in the lift.", "Die Planken vor dir gehen weg. Spring gleich nach der Landung und halt dich im Lift an der Wand."),
            // rematch: the bridge holds its planks but not its jumpers: any hop in the air drops the plank it would land on (the card is a bluff), and the gap is
            // closed so you can just walk. Keep your feet down, keep moving: every other plank is on loan
            rematch = listOf(
                Round(
                    T("Same bridge. Mephi read the manual.", "Gleiche Brücke. Mephi hat das Handbuch gelesen."),
                    traps = listOf(
                        trap(Landed(6f, 9f), Bluff(Card.SINKING), say("Planks ahead: sinking. Or not. Trust me.", "Planken voraus: sinken. Oder nicht. Vertrau mir.")),
                        trap(Touch('a'), Fall('a'), say("This one, though. This one is real.", "Aber die hier. Die hier ist echt."), delay = 0.3f),
                        trap(Airborne(10f, 13f), Hide('b')),
                        trap(Airborne(13f, 15f), Hide('c')),
                        trap(Airborne(15f, 18f), Hide('d'), say("No jumping on the bridge. Union rules.", "Springen auf der Brücke verboten. Betriebsrat.")),
                        trap(Zone(21f, 5.8f, 24f, 8f), FanSet('g', 6f), say("Turbo again. Nobody asked.", "Wieder Turbo. Keiner hat gefragt.")),
                        trap(Touch('e'), Fall('e'), say("Landing strip: still on loan.", "Landebahn: immer noch geliehen."), delay = 0.4f),
                    ),
                ) {
                    put(9, 10, 'a'); put(11, 10, '#')
                },
            ),
        ) {
            border(); floor()
            fill(9..29, 14..14, '^')
            fill(6..8, 10..14)
            fill(10..11, 10..10, 'a'); fill(12..13, 10..10, 'b'); fill(14..15, 10..10, 'c'); fill(16..17, 10..10, 'd')
            fill(18..25, 10..14)
            fill(19..20, 2..5)
            fill(24..25, 5..5); fill(26..27, 5..5, 'e'); fill(28..30, 5..5)
            fill(21..30, 1..2, 'v')
            spawn(1, 14); door(30, 4)
        },

        // 42 — Exhaust
        Level(
            name = T("Exhaust", "Abluft"),
            intro = T("It's a bit warm in here.", "Ist ein bisschen warm hier."),
            start = listOf(
                Fan('f', at = 14 to 15, dir = Dir.UP, reach = 9, speed = 6.5f, width = 3),
                Fan('D', at = 27 to 0, dir = Dir.DOWN, reach = 14, speed = 3f, width = 3),
            ),
            traps = listOf(
                trap(PastX(11f), Play(Card.STALKER), Chase('W', 4.4f, left = 0f, right = 30f), say("Someone is following you. Rude, but loyal.", "Jemand folgt dir. Unhöflich, aber treu.")),
                trap(Zone(21f, 5f, 26f, 7f), Move('h', 0f, 3.6f, 4.5f), say("Exhaust hood, closing time.", "Abzugshaube, Feierabend."), delay = 0.2f),
            ),
            hint = T("Stand in the vent and let it carry you. Up top, do not linger under the hood.", "Stell dich in den Schacht und lass dich tragen. Oben nicht unter der Haube trödeln."),
            // rematch: the vent is pressure-sensitive now: whoever stands still in it (as in round one) gets the ceiling slab down the shaft. Keep hopping
            rematch = listOf(
                Round(
                    T("Same vent. It listens now.", "Gleicher Schacht. Er hört jetzt zu."),
                    traps = listOf(
                        trap(PastX(11f), Chase('W', 4.4f, left = 0f, right = 30f), say("The follower is back. He brought a friend: your own habits.", "Der Verfolger ist zurück. Er hat deine Gewohnheiten mitgebracht.")),
                        trap(Idle(0.5f), Play(Card.HEADBUTT), Move('c', 0f, 12f, 16f), say("Standing still in my vent? That is loitering.", "In meinem Schacht rumstehen? Das ist Herumlungern.")),
                        trap(Zone(21f, 5f, 26f, 7f), Move('h', 0f, 3.6f, 4.5f), say("Hood: still closing at six.", "Haube: schließt weiter um sechs."), delay = 0.2f),
                    ),
                ) {
                    fill(14..16, 1..2, 'c')
                },
            ),
        ) {
            border(); floor()
            fill(1..2, 11..14, 'W')
            fill(17..26, 7..7)
            fill(19..20, 1..4); fill(19..20, 8..14)
            fill(21..25, 1..2, 'h')
            fill(30..30, 7..14)
            spawn(3, 14); door(29, 14)
        },

        // 43 — Wiring Diagram
        Level(
            name = T("Wiring Diagram", "Schaltplan"),
            intro = T("Page one: everything is connected.", "Seite eins: Alles hängt zusammen."),
            start = listOf(
                Circuit('a'), Circuit('p'), Circuit('l'), Circuit('w'),
                Fan('f', at = 17 to 15, dir = Dir.UP, reach = 8, speed = 5f, width = 2), Power('f', false),
                Pad('1', at = 5 to 14), Pad('2', at = 11 to 7, circuits = "w", mode = PadMode.OFF),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.SHORT_CIRCUIT), Clock('a', on = 1.8f, off = 60f), Power('f', true), say("Switch one powers the lift. And the bridge, for a moment.", "Schalter eins versorgt den Aufzug. Und kurz die Brücke.")),
                trap(Touch('p'), Power('p', false), say("The landing is on a different circuit.", "Die Landefläche hängt an einem anderen Stromkreis."), delay = 0.4f),
                trap(Touch('l'), Clock('l', on = 1.8f, off = 1.2f), say("The ledge is on a timer. Page two.", "Der Sims hat einen Timer. Seite zwei.")),
            ),
        ) {
            border(); floor()
            pit(8..16); put(8, 15, 'a')
            fill(8..14, 15..15, 'a'); fill(15..16, 15..15, 'p')
            fill(19..23, 8..14)
            fill(9..16, 8..8, 'l')
            fill(26..27, 1..14, 'w')
            spawn(2, 14); door(29, 14)
        },

        // 44 — Display (a breather: one punchline)
        Level(
            name = T("Display", "Anzeige"),
            intro = T("I mounted the monitor myself.", "Den Monitor habe ich selbst montiert."),
            start = listOf(
                Fan('f', at = 8 to 15, dir = Dir.UP, reach = 9, speed = 3f, width = 5),
            ),
            traps = listOf(
                trap(Zone(8f, 11f, 13f, 15.5f), FanSet('f', 7.5f), say("Warming up the lift. Mind the cable.", "Der Aufzug läuft warm. Achtung, Kabel.")),
                trap(Zone(8f, 5.5f, 13f, 7.5f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Mounted upside down. Obviously. The door, too.", "Kopfüber montiert. Natürlich. Die Tür auch.")),
            ),
        ) {
            border(); floor()
            put(19, 1, 'v')
            spawn(2, 14); door(29, 1)
        },
    )
}
