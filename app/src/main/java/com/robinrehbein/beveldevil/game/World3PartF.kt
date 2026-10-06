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
                trap(Zone(3f, 8.5f, 6.5f, 9.7f), Fall('k'), say("Top floor. The ceiling comes down to meet you.", "Oberste Etage. Die Decke kommt dir entgegen.")),
                trap(Landed(10f, 12f), Play(Card.CRUMBLE), Fall('b'), say("The bridge is built one plank ahead of you.", "Die Brücke wird eine Planke vor dir gebaut. Oder abgebaut.")),
                trap(Landed(14f, 16f), Hide('d'), say("Planning permission: revoked.", "Baugenehmigung: widerrufen.")),
                trap(Zone(21f, 5.8f, 24f, 8f), FanSet('g', 6f), say("The lift has a turbo. You're welcome.", "Der Aufzug hat einen Turbo. Gern geschehen.")),
                trap(Touch('e'), Fall('e'), say("The landing strip has a lease. It just ran out.", "Die Landebahn hat einen Mietvertrag. Er ist gerade abgelaufen."), delay = 0.4f),
            ),
            hint = T("Step off the first lift as soon as you are up: its ceiling drops. The planks ahead are the ones that go: hop as soon as you land, and hold on to the wall in the second lift.", "Tritt oben sofort aus dem ersten Lift: Seine Decke fällt. Die Planken vor dir gehen weg: Spring gleich nach der Landung und halt dich im zweiten Lift an der Wand."),
            // rematch: the bridge holds its planks but not its jumpers: any hop in the air drops the plank it would land on (the card is a bluff), and the gap is
            // closed so you can just walk. Keep your feet down, keep moving: every other plank is on loan
            rematch = listOf(
                Round(
                    T("Same bridge. Mephi read the manual.", "Gleiche Brücke. Mephi hat das Handbuch gelesen."),
                    traps = listOf(
                        trap(Zone(3f, 8.5f, 6.5f, 9.7f), Fall('k'), say("The lift ceiling, again. It missed you.", "Wieder die Liftdecke. Sie hat dich vermisst.")),
                        trap(Landed(6f, 9f), Bluff(Card.CRUMBLE), say("Planks ahead: crumbling. Or not. Trust me.", "Planken voraus: bröckeln. Oder nicht. Vertrau mir.")),
                        trap(Touch('a'), Fall('a'), say("This one, though. This one is real.", "Aber die hier. Die hier ist echt."), delay = 0.3f),
                        trap(Airborne(10f, 13f), Hide('b')),
                        trap(Airborne(13f, 15f), Hide('c')),
                        trap(Airborne(15f, 18f), Hide('d'), say("No jumping on the bridge. Union rules.", "Springen auf der Brücke verboten. Betriebsrat.")),
                        trap(Zone(21f, 5.8f, 24f, 8f), FanSet('g', 6f), say("Turbo again. Nobody asked.", "Wieder Turbo. Keiner hat gefragt.")),
                        trap(Touch('e'), Fall('e'), say("Landing strip: lease renewed. Not for you.", "Landebahn: Mietvertrag verlängert. Nicht für dich."), delay = 0.4f),
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
            fill(21..30, 1..2, 'v'); fill(3..5, 1..3, 'k')
            spawn(1, 14); door(30, 4)
        },

        // 42 — Exhaust (U8, a trap room, mirrored: you start at the right and the door is at the far left, down the exhaust shaft). A wall of
        // teeth sets off behind you as you head for the vent; ride it up to the shelf (it carries whoever stands in it). On the shelf the exhaust
        // hood slams down ahead of you, not on you: stop short of it and hop it. Beyond it the shaft blows you gently down, until the drain breathes
        // in and sucks everything up into the spikes in its housing: step out of the draft to the right and walk the last stretch to the door.
        // Rematch: the vent is pressure-sensitive now. Whoever stands still in it (as in round one) is dropped back to the teeth: keep hopping.
        Level(
            name = T("Exhaust", "Abluft"),
            intro = T("It's a bit warm in here.", "Ist ein bisschen warm hier."),
            start = listOf(
                Fan('f', at = 15 to 15, dir = Dir.UP, reach = 9, speed = 6.5f, width = 3),
                Fan('D', at = 2 to 0, dir = Dir.DOWN, reach = 14, speed = 3f, width = 3),
            ),
            traps = listOf(
                trap(BeforeX(21f), Play(Card.STALKER), Chase('W', 3.0f, left = 30f, right = 0f), say("Someone is following you. Rude, but loyal.", "Jemand folgt dir. Unhöflich, aber treu.")),
                trap(Zone(11.5f, 5f, 13.6f, 7f), Move('h', 0f, 4f, 6.5f), say("Exhaust hood, closing time. It closes in front of you. Out of courtesy.", "Abzugshaube, Feierabend. Sie schließt vor dir. Aus Höflichkeit.")),
                trap(Zone(2f, 7.3f, 5f, 12.5f), FanSet('D', -7f), say("The drain breathes in. So do the spikes up there.", "Der Abfluss atmet ein. Die Stacheln da oben auch.")),
                trap(Zone(2f, 7.3f, 5f, 12.5f), FanSet('D', 3f), delay = 2.2f),
            ),
            hint = T("Stand in the vent and let it carry you. Up top, the hood shuts ahead of you: wait for it, then hop it. Do not trust the slow way down: step out of the draft.", "Stell dich in den Schacht und lass dich tragen. Oben schließt die Haube vor dir: Warte, dann spring drüber. Trau dem langsamen Weg nach unten nicht: Tritt aus dem Luftstrom."),
            rematch = listOf(
                Round(
                    T("Same vent. It listens now.", "Gleicher Schacht. Er hört jetzt zu."),
                    hint = T("Do not stand still in the vent: it drops whoever loiters. Keep hopping on the way up.", "Steh im Schacht nicht still: Er lässt fallen, wer herumlungert. Hüpf auf dem Weg nach oben weiter."),
                    traps = listOf(
                        trap(BeforeX(21f), Play(Card.STALKER), Chase('W', 3.0f, left = 30f, right = 0f), say("The follower is back. He brought a friend: your own habits.", "Der Verfolger ist zurück. Er hat deine Gewohnheiten mitgebracht.")),
                        trap(Idle(0.5f), FanSet('f', -5f), say("Standing still in my vent? That is loitering. Back down you go.", "In meinem Schacht rumstehen? Das ist Herumlungern. Wieder runter mit dir.")),
                        trap(Zone(11.5f, 5f, 13.6f, 7f), Move('h', 0f, 4f, 6.5f), say("Hood: still closing at six.", "Haube: schließt weiter um sechs.")),
                        trap(Zone(2f, 7.3f, 5f, 12.5f), FanSet('D', -7f), say("Breathing in again. Some habits are my own.", "Wieder einatmen. Manche Angewohnheiten sind meine.")),
                        trap(Zone(2f, 7.3f, 5f, 12.5f), FanSet('D', 3f), delay = 2.2f),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(30..30, 10..14, 'W'); put(29, 14, '#')
            fill(5..14, 7..7)
            fill(11..12, 8..14)
            fill(6..10, 1..2, 'h')
            fill(1..1, 3..14)
            fill(2..4, 1..1, 'v')
            spawn(27, 14); door(2, 14)
        },

        // 43 — Wiring Diagram (R1+R10, U17), run from right to left: the first switch powers the lift and, for a moment, the bridge over the
        // pit (run), the landing past it is on another circuit and drops, the lift takes you up to a ledge on a timer, the second switch at its far
        // end flips one bit that the wall in front of the door shares with the floor under it.
        Level(
            name = T("Wiring Diagram", "Schaltplan"),
            intro = T("Page one: everything is connected.", "Seite eins: Alles hängt zusammen."),
            start = listOf(
                Circuit('a'), Circuit('p'), Circuit('l'), Circuit('w'), Circuit('x', on = false),
                Fan('f', at = 13 to 15, dir = Dir.UP, reach = 8, speed = 5f, width = 2), Power('f', false),
                Pad('1', at = 26 to 14), Pad('2', at = 20 to 7),
            ),
            traps = listOf(
                trap(Pressed('1'), Clock('a', on = 1.8f, off = 60f), Power('f', true), say("Switch one powers the lift. And the bridge, for a moment.", "Schalter eins versorgt den Aufzug. Und kurz die Brücke.")),
                trap(Pressed('1', 2), Clock('a', on = 1.8f, off = 60f), Power('f', true)),
                trap(Pressed('1', 3), Clock('a', on = 1.8f, off = 60f), Power('f', true)),
                trap(Touch('p'), Power('p', false), say("The landing is on a different circuit.", "Die Landefläche hängt an einem anderen Stromkreis."), delay = 0.4f),
                trap(Touch('l'), Clock('l', on = 1.8f, off = 1.2f), say("The ledge is on a timer. Page two.", "Der Sims hat einen Timer. Seite zwei.")),
                trap(Pressed('2'), Play(Card.BIT_FLIP), BitFlip('w', 'x'), say("Bit flip: the wall and the floor under it share one bit. They swap.", "Bitkipper: Die Wand und der Boden darunter teilen sich ein Bit. Sie tauschen.")),
            ),
            hint = T("Press the first switch, then run: the bridge only has power for a moment. The second switch hides at the far end of the ledge.", "Drück den ersten Schalter und lauf los: Die Brücke hat nur kurz Strom. Der zweite Schalter liegt am anderen Ende des Simses."),
        ) {
            border(); floor()
            pit(15..23); put(23, 15, 'a')
            fill(17..23, 15..15, 'a'); fill(15..16, 15..15, 'p')
            fill(8..12, 8..14)
            fill(15..22, 8..8, 'l')
            pit(4..5); fill(4..5, 15..15, 'x')
            fill(4..5, 1..14, 'w')
            spawn(29, 14); door(2, 14)
        },

        // 44 — Display (a breather, U10): you start on the shelf in the middle of the room, and the way right is walled off. The lift is far
        // over at the left: ride it, and at the top the screen is mounted upside down, you fall to the ceiling and walk it over the wall to the
        // door, which hangs from the ceiling too (hop the stud; past the wall the cooling fan behind the screen blows in your face). Auto-rotate
        // comes back after a few seconds, and below the door the floor is studded: do not dawdle up there.
        Level(
            name = T("Display", "Anzeige"),
            intro = T("I mounted the monitor myself.", "Den Monitor habe ich selbst montiert."),
            start = listOf(
                Fan('f', at = 2 to 15, dir = Dir.UP, reach = 9, speed = 3f, width = 5),
                Fan('g', at = 30 to 1, dir = Dir.LEFT, reach = 11, speed = 4.5f, width = 2), Power('g', false),
            ),
            traps = listOf(
                trap(Zone(2f, 11f, 7f, 15.5f), FanSet('f', 7.5f), say("Warming up the lift. Mind the cable.", "Der Aufzug läuft warm. Achtung, Kabel.")),
                trap(Zone(2f, 5.5f, 7f, 7.5f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Mounted upside down. Obviously. The door, too.", "Kopfüber montiert. Natürlich. Die Tür auch.")),
                trap(Zone(2f, 5.4f, 7f, 7.6f), Gravity(false), say("Auto-rotate is on. You had four seconds.", "Automatisch drehen ist an. Du hattest vier Sekunden."), delay = 4.0f),
                trap(Zone(19f, 1f, 21f, 3f), Power('g', true), say("The cooling fan behind the screen kicks in. Upside down, it blows in your face.", "Der Lüfter hinterm Bildschirm springt an. Kopfüber bläst er dir ins Gesicht.")),
            ),
            hint = T("The way over the wall is the ceiling. Ride the lift all the way up, and hurry along the ceiling: the screen rotates back soon.", "Der Weg über die Wand ist die Decke. Fahr mit dem Aufzug ganz nach oben, und beeil dich an der Decke: Der Bildschirm dreht sich bald zurück."),
        ) {
            border(); floor()
            fill(14..17, 8..8)
            fill(18..18, 2..14)
            fill(11..11, 1..1, 'v')
            fill(20..28, 14..14, '^')
            spawn(15, 7); door(26, 1)
        },

        // 45 — Cold Air (U3, a trap room): the cold aisle of a server room, top right to bottom left. You start up on the rack at the right
        // and step off into the cold-air shaft, which lowers you gently, then pushes hard; a spiked rack slides into the shaft from the right
        // (hug the left wall), and the raised floor at the bottom of the shaft sinks the moment anyone lands on it: steer left into the gap
        // under the racks before you touch down. In the aisle a spiked pin slides in from the far end at you: hop it, the door is behind it.
        Level(
            name = T("Cold Air", "Kaltluft"),
            intro = T("Plenty of hot air here. Mostly mine.", "Hier gibt es viel heiße Luft. Meist meine."),
            legend = mapOf('Q' to Glyph(spike = true, dir = Dir.LEFT), 'L' to Glyph(spike = true, dir = Dir.RIGHT)),
            start = listOf(
                Fan('d', at = 19 to 0, dir = Dir.DOWN, reach = 14, speed = 4.5f, width = 5),
            ),
            traps = listOf(
                trap(Zone(19f, 4f, 24f, 5.5f), Move('Q', -3f, 0f, 6f), say("Cold aisle, closing. Mind the rack you just stepped off.", "Kaltgang schließt. Vorsicht, das Rack, von dem du kommst.")),
                trap(Zone(19f, 8.5f, 24f, 10.5f), Play(Card.BACKDRAFT), FanSet('d', 8f), say("Cold air, extra strength. Mind where you touch down.", "Kaltluft, extra stark. Pass auf, wo du aufsetzt.")),
                trap(Landed(19f, 24f), Move('k', 0f, 6f, 6f), say("Raised floor. Lowered floor. Same floor.", "Doppelboden. Einfachboden. Gleicher Boden.")),
                trap(BeforeX(15f), Move('L', 10f, 0f, 6f), say("Hot-swap in progress. You are the old part.", "Hot-Swap läuft. Du bist das alte Teil.")),
            ),
            hint = T("Hug the left wall of the shaft on the way down, and do not land in it: slip into the gap under the wall. Hop the pin in the aisle.", "Halt dich im Schacht auf dem Weg nach unten links an der Wand, und lande nicht darin: Schlüpf in die Lücke unter der Wand. Spring im Gang über den Stift."),
        ) {
            border(); floor()
            fill(26..30, 4..14); fill(24..25, 4..7); fill(24..25, 10..14); fill(24..25, 8..9, 'Q')
            fill(16..18, 1..12)
            pit(19..23); fill(19..23, 15..15, 'k')
            put(3, 14, 'L')
            spawn(28, 3); door(2, 14)
        },

        // 46 — POST (R5, U16): a serpentine up through three floors. The first lift stops for a beep while you are in it and comes back
        // with a kick, the second one spins up hard under a cracked frame, and on the top shelf Mephi restores your memory from a backup:
        // once more from a second and a half ago, back up the second lift, and the frame over the spot you are put back to cracks as well
        // (do not stand there wondering). The door is in the middle of the top shelf.
        Level(
            name = T("POST", "Selbsttest"),
            intro = T("Power-on self-test. Three checks. One beep each.", "Einschalt-Selbsttest. Drei Prüfungen. Je ein Piep."),
            start = listOf(
                Fan('a', at = 12 to 15, dir = Dir.UP, reach = 6, speed = 5.5f, width = 3),
                Fan('b', at = 2 to 10, dir = Dir.UP, reach = 7, speed = 5.5f, width = 4),
            ),
            traps = listOf(
                trap(Zone(12f, 12f, 15f, 15.5f), Play(Card.BIOS), Power('a', false), say("Beep. Fan: 0 RPM. Press F1 to continue.", "Piep. Lüfter: 0 U/min. F1 zum Fortfahren."), delay = 0.2f),
                trap(Zone(12f, 12f, 15f, 15.5f), Power('a', true), delay = 1.3f),
                trap(Zone(12f, 10f, 15f, 11.8f), FanSet('a', 8f), say("Fan one: overspeed warning. Ignored.", "Lüfter eins: Überdrehzahl-Warnung. Ignoriert.")),
                trap(Zone(2f, 6f, 6f, 8f), FanSet('b', 7.5f), FrameCrack(5, 0, warn = 1.1f), say("Beep beep. Fan two: full speed. The frame did not pass the test.", "Piep piep. Lüfter zwei: Vollgas. Der Rahmen hat den Test nicht bestanden.")),
                trap(Zone(14f, 3f, 18f, 5f), Undo(1.5f), FrameCrack(3, 0, 4, 0, warn = 0.5f), say("Beep beep beep. Memory: restored from backup. The frame over the backup: not so much.", "Piep piep piep. Speicher: aus Sicherung wiederhergestellt. Der Rahmen über der Sicherung: eher nicht.")),
            ),
            hint = T("The serpentine goes up on the right, back left, up again, and right to the door. Hold on to the wall in the first lift, and do not dawdle at the second: the frame above it is cracked.", "Der Weg geht rechts hoch, links zurück, wieder hoch und rechts zur Tür. Halt dich im ersten Aufzug an der Wand, und trödle nicht am zweiten: Der Rahmen darüber hat einen Riss."),
        ) {
            border(); floor()
            fill(15..30, 10..14)
            fill(1..11, 10..10)
            fill(7..21, 5..5)
            put(6, 9, '^'); put(11, 4, '^')
            spawn(2, 14); door(20, 4)
        },

        // 47 — Boot Order (R7+R6, U14+U4): you start in the middle of the room, and the door is right there on the floor to your left: a bait, it
        // runs up the wall and along the ceiling, and drops onto the shelf at the right as you board the lift (ride it, and step off at the shelf: the lift runs all the way up into the spiked
        // ceiling). The door on the shelf is no end either: it runs to the far left wall, past where you started, and the lift now blows down. At
        // the bottom the boot loop turns the lift around once more, overclocked, and slams whoever is still in it into the ceiling: step out of the
        // draft and walk on, past where you started. (One room: the annex is the finale's gag, 48.)
        Level(
            name = T("Boot Order", "Boot-Reihenfolge"),
            intro = T("Just walk to the door. Really.", "Geh einfach zur Tür. Wirklich."),
            start = listOf(
                Fan('f', at = 18 to 15, dir = Dir.UP, reach = 13, speed = 9f, width = 4),
            ),
            traps = doorTrail(
                Zone(9.6f, 12.5f, 13.4f, 15.5f), 9, 14, listOf(DoorTo(9, 1, 24f, hanging = true), DoorTo(25, 1, 24f, hanging = true)),
                first = listOf(Play(Card.SHY_DOOR), say("Boot device 1: USB. No bootable medium. Try the next one.", "Bootgerät 1: USB. Kein bootfähiges Medium. Probier das nächste.")),
            ) + listOf(
                trap(Zone(18f, 10f, 22f, 15.5f), DoorTo(25, 5, 24f), say("It found a shelf. Shelves are comfortable.", "Sie hat ein Regal gefunden. Regale sind bequem.")),
            ) + doorTrail(
                Zone(21.8f, 3.5f, 25f, 6.5f), 25, 5, listOf(DoorTo(23, 14), DoorTo(2, 14)),
                first = listOf(FanSet('f', -7f), say("Boot device 2: disk. Sector 0 unreadable. Boot device 3: the floor. Far left, behind you.", "Bootgerät 2: Festplatte. Sektor 0 unlesbar. Bootgerät 3: der Boden. Ganz links, hinter dir.")),
            ) + listOf(
                trap(Zone(21.8f, 3.4f, 25f, 6.6f), FanSet('f', 16f), say("Boot loop. Overclocked, this time. Mind the ceiling.", "Bootschleife. Diesmal übertaktet. Vorsicht, Decke."), delay = 1.3f),
            ),
            hint = T("The door that runs away is not the way out. Take the lift up, and when the door runs off to the far left, let the lift take you down and step out of it at once: it turns around again, all the way to the ceiling.", "Die Tür, die wegläuft, ist nicht der Ausgang. Fahr mit dem Lift hoch, und wenn die Tür nach ganz links läuft, lass dich vom Lift hinunterbringen und tritt sofort heraus: Er dreht wieder um, bis an die Decke."),
        ) {
            border(); floor()
            fill(23..25, 6..6); fill(26..30, 6..14)
            fill(17..22, 1..1, 'v')
            spawn(15, 14); door(9, 14)
        },

        // 48 — BIOS Setup (the finale of the game)
        Level(
            name = T("BIOS Setup", "BIOS-Setup"),
            intro = T("Press DEL to enter setup. Everything else is my job.", "ENTF für das Setup. Alles andere ist mein Job."),
            rooms = 2,
            start = listOf(
                Fan('f', at = 9 to 15, dir = Dir.UP, reach = 9, speed = 7f, width = 3),
                Fan('g', at = 19 to 7, dir = Dir.UP, reach = 5, speed = -6f, width = 3),
                Pad('B', at = 14 to 6),
                Heat('n', rise = 0.9f), Heat('m', rise = 1.8f),
                Fan('D', at = roomX(1, 4) to 0, dir = Dir.DOWN, reach = 14, speed = 7f, width = 4),
                Heat('c', rise = 2.0f, load = true), Heatsink('k', cools = "c"), Heat('e', rise = 1.2f),
            ),
            traps = listOf(
                trap(Landed(12f, 14f), HeatSpike('n', 0.7f), say("CPU: overclocked on arrival.", "CPU: bei Ankunft übertaktet.")),
                trap(Pressed('B'), FanSet('g', 7f), say("Page two: fan mode. Reverse to forward.", "Seite zwei: Lüftermodus. Von rückwärts auf vorwärts.")),
                trap(AtDoor, Play(Card.GRAND_FINALE), Extend(into = 1, top = 1, bottom = 2, warn = 0.7f, door = roomX(1, 26) to 14,
                    line = T("Save and exit? The exit is on the next screen.", "Speichern und beenden? Der Ausgang ist auf dem nächsten Bildschirm."))),
                trap(Zone(roomX(1, 4f), 8f, roomX(1, 8f), 10f), FanSet('D', -7f), Flip(1.2f), say("Fan mode: REVERSE. Load defaults? No.", "Lüftermodus: UMGEKEHRT. Standardwerte laden? Nein.")),
                trap(Zone(roomX(1, 4f), 7.9f, roomX(1, 8f), 10.1f), FanSet('D', 7f), say("Fan mode: FORWARD again. Down you go.", "Lüftermodus: wieder VORWÄRTS. Abwärts mit dir."), delay = 0.7f),
                trap(Zone(roomX(1, 21f), 13f, roomX(1, 25f), 15.5f), HeatSpike('e', 0.45f), say("Thermal threshold: a matter of opinion.", "Temperaturgrenze: Ansichtssache.")),
            ),
            hint = T("The switch on the hot shelf turns the second fan around. Cool the chip on the heatsink before you run.", "Der Schalter auf dem heißen Regal dreht den zweiten Lüfter um. Kühl den Chip am Kühlkörper, bevor du läufst."),
        ) {
            border(); floor()
            room(0) {
                fill(12..13, 7..7, 'n'); fill(14..22, 7..7, 'm')
                fill(22..30, 3..3)
                spawn(3, 14); door(29, 2)
            }
            room(1) {
                fill(1..3, 3..3)
                fill(8..9, 4..11)
                fill(8..11, 15..15, 'k'); fill(12..18, 15..15, 'c')
                fill(21..24, 15..15, 'e')
            }
        },
    )
}
