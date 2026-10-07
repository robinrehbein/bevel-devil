package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Undo
import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Blink
import com.robinrehbein.beveldevil.game.Action.Chase
import com.robinrehbein.beveldevil.game.Action.Circuit
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Pad
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Pressed

/** World 2, levels 17-32. Act 2, "Traffic": conveyor belts (the data bus) and lasers (the firewall), first alone, then with portals and the classics. */
object World2Part2 {
    private val hidden = Glyph(spike = true, hidden = true)
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)

    val levels: List<Level> = listOf(

        // 17 — the first conveyor belt, as a puzzle room (R10 transport, U12 the bus turns around). The data bus runs in a
        // cable duct, against you and faster than you run, and the duct has spikes on its ceiling at the start: no hopping
        // through. Over the duct and up the stairs to the shelf; a loose piece of the shelf drops on whoever runs under it,
        // and the shelf's landing piece crumbles behind you. The touch plate at the far end of the shelf turns the bus around:
        // drop down and ride it to the door. On the way the floor in front of the door drops out: jump it
        Level(
            name = T("Data Bus", "Datenbus"),
            intro = T("Timetable: every ten seconds. Roughly.", "Fahrplan: alle zehn Sekunden. Ungefähr."),
            start = listOf(Belt('b', -9f)),
            traps = listOf(
                trap(Zone(3.1f, 14.2f, 3.7f, 15.2f), Fall('y'), say("Nobody queues at my bus stop. Packet dropped.", "An meiner Haltestelle wird nicht angestanden. Paket verworfen."), delay = 0.25f),
                trap(Zone(9.6f, 9f, 15f, 12.2f), Fall('c'), say("Packet from the upper bus. Mind your head.", "Paket vom oberen Bus. Kopf einziehen.")),
                trap(Zone(14f, 6.5f, 19f, 8.4f), Fall('d'), say("Shelf 2 is decoration. Do not stand on it.", "Regal 2 ist Deko. Nicht draufstellen."), delay = 0.4f),
                trap(Zone(7f, 5f, 8.5f, 8.2f), Play(Card.HEADBUTT), Fall('k'), say("Express packet. It does not stop for pedestrians.", "Expresspaket. Hält nicht für Fußgänger."), delay = 0.1f),
                trap(Touch('s'), Belt('b', 5f), say("Bus 1 reversed. Next stop: the door.", "Bus 1 fährt jetzt andersrum. Nächster Halt: die Tür.")),
                trap(Zone(6.5f, 13.5f, 21f, 15f), Fall('e'), say("Last stop. The station is closed. Please jump.", "Endstation. Der Bahnhof ist gesperrt. Bitte springen.")),
            ),
            hint = T("The bus turns around somewhere. Not down here.", "Irgendwo dreht der Bus um. Nicht hier unten."),
        ) {
            border(); floor()
            fill(4..5, 13..14); fill(2..3, 15..17, 'y')
            fill(7..20, 12..12); fill(8..14, 13..13, 'v'); fill(7..20, 15..15, 'b')
            fill(21..21, 1..12); fill(19..20, 10..11)
            fill(25..26, 15..17, 'e')
            fill(3..18, 8..8); fill(3..4, 8..8, 's'); fill(11..13, 8..8, 'c'); fill(14..18, 8..8, 'd'); fill(5..6, 1..1, 'k')
            spawn(); door(); bits(17)
        },

        // 18 — the first laser, as a puzzle room (R5 floors and the way back, U13 firewall). The door hangs high on the left, and
        // the stairs to it are cut by a firewall beam; the pad that drops the beam lies on the rack on the right. On the way
        // out the floor drops out (a hop) and one gate counts to one; pressing the pad starts a port scan on the rack (leave it),
        // puts the floor back, and the beam over the stairs comes back as you land on the second step: keep climbing
        Level(
            name = T("Firewall", "Firewall"),
            intro = T("I configured the firewall myself. Nice pattern, right?", "Die Firewall habe ich selbst eingestellt. Schönes Muster, oder?"),
            start = listOf(
                Laser('W', 1 to 10, 10 to 10),
                Pad('1', at = 26 to 12),
            ),
            traps = listOf(
                trap(After(1.2f), Play(Card.COLLAPSE), Move('a', 0f, 12f, 30f), say("Packet loss. The floor was never in the rule set.", "Paketverlust. Der Boden stand nie im Regelwerk.")),
                trap(PastX(20.6f), Laser('G', 23 to 1, 23 to 14, on = 0.4f, off = 40f, delay = 0.1f),
                    say("Rule 2: no entry. I count to one.", "Regel 2: Zutritt verboten. Ich zähle bis eins.")),
                trap(Pressed('1'), Power('W', false), Move('a', 0f, -12f, 25f), Laser('K', 25 to 12, 28 to 12, on = 1.0f, off = 40f, delay = 0.8f),
                    say("Rule 1 disabled. Port scan on the rack. Don't linger.", "Regel 1 deaktiviert. Portscan auf dem Rack. Nicht trödeln.")),
                trap(Zone(7.3f, 9.9f, 11.6f, 11f), Laser('W', 1 to 10, 10 to 10, delay = 1.1f), say("Rule 1 restarted. Climb faster.", "Regel 1 neu gestartet. Kletter schneller.")),
            ),
            hint = T("The pad is on the rack. And don't dawdle on the stairs.", "Der Schalter liegt auf dem Rack. Und trödel nicht auf der Treppe."),
            // rematch: the firewall learned from round 1. The beam over the stairs stays on for good, and the pad on the rack does something
            // else now: it is the call button of the rack, which rises like a lift to a walkway under the ceiling that runs all the way to the
            // ledge. The floor still drops, but two tiles further on than in round 1 (whoever hops where round 1 hopped lands in it) and only
            // when you are close; the gate at the rack counts to one again. At the top a port scan sweeps the lift (walk on), and on the walkway
            // a tripwire flashes before the ledge (hop it). Round 1's run presses the pad, walks off the rising rack and climbs the stairs into
            // the beam that never goes out
            rematch = listOf(
                Round(
                    T("The firewall learned. The stairs stay closed.", "Die Firewall hat gelernt. Die Treppe bleibt zu."),
                    hint = T("The pad is a lift now. Go over the top, not up the stairs.", "Der Schalter ist jetzt ein Aufzug. Geh obenrum, nicht die Treppe hoch."),
                    start = listOf(
                        Laser('W', 1 to 10, 10 to 10),
                        Pad('1', at = 26 to 12),
                    ),
                    traps = listOf(
                        trap(After(1.6f), Play(Card.COLLAPSE), Move('a', 0f, 12f, 30f), say("Packet loss, second edition. A bit later.", "Paketverlust, zweite Auflage. Etwas später.")),
                        trap(PastX(19.2f), Laser('G', 24 to 1, 24 to 14, on = 0.4f, off = 40f, delay = 0.35f),
                            say("Rule 2: still no entry. Still counting to one.", "Regel 2: immer noch kein Zutritt. Ich zähle immer noch bis eins.")),
                        trap(Pressed('1'), Move('k', 0f, -8f, 8f), say("Going up. Rule 1 stays on. It likes the stairs.", "Fahrstuhl nach oben. Regel 1 bleibt an. Sie mag die Treppe.")),
                        trap(Pressed('1'), Laser('K', 25 to 4, 27 to 4, on = 0.7f, off = 40f, delay = 2.0f),
                            say("Port scan on the lift. Please do not stand there.", "Portscan im Aufzug. Bitte nicht stehen bleiben.")),
                        trap(Zone(14f, 3f, 17f, 5.2f), Laser('H', 10 to 4, 11 to 4, on = 0.5f, off = 40f, delay = 0.3f),
                            say("Tripwire. Mind the cable.", "Stolperdraht. Vorsicht, Kabel.")),
                    ),
                ) { fill(17..18, 15..17, '#'); fill(19..21, 15..17, 'a'); fill(25..27, 13..14, 'k'); fill(28..30, 5..14); fill(5..24, 5..5); fill(3..4, 7..7) },
            ),
        ) {
            border(); floor()
            fill(12..14, 13..13); fill(8..10, 11..11); fill(4..6, 9..9); fill(1..2, 7..7); fill(4..6, 5..5)
            fill(25..27, 13..14)
            fill(17..18, 15..17, 'a')
            spawn(2); put(6, 4, 'D'); bits(18)
        },

        // 19 — a trap room: the floor drops out ahead (a hop), the packets get reordered (left and right swap) while the stairs
        // go up to the right, and on the top floor, on the way back left to the door, the order is restored and a wall drives
        // toward you: hop it. Behind it the last parcel in front of the door is returned to sender, too: it slides at you (hop it late)
        Level(
            name = T("Delivery", "Zustellung"),
            intro = T("Packets arrive in order. Guaranteed.", "Pakete kommen der Reihe nach an. Garantiert."),
            traps = listOf(
                trap(PastX(6.6f), Move('a', 0f, 12f, 30f), say("Packet 1 got lost in transit. Under you.", "Paket 1 ging unterwegs verloren. Unter dir.")),
                trap(Landed(10.9f, 12.7f), Play(Card.TWISTED), Swap(true), say("Packet reordering! Left and right arrive swapped.", "Paket-Umsortierung! Links und rechts kommen vertauscht an.")),
                trap(Landed(25f, 28f), Swap(false), say("In-order delivery restored. You're welcome.", "Reihenfolge wiederhergestellt. Gern geschehen.")),
                trap(Landed(25f, 28f), Move('w', 9f, 0f, 5f), say("Return to sender. Express.", "Rücksendung an den Absender. Per Express."), delay = 0.2f),
                trap(Zone(9.5f, 4f, 16.5f, 7.9f), Move('K', 2f, 0f, 5f), say("Parcel two: also returned. Sign here.", "Paket zwei: auch zurück. Hier unterschreiben.")),
            ),
            hint = T("Left is right. Until the top floor.", "Links ist rechts. Bis zum Obergeschoss."),
        ) {
            border(); floor()
            fill(8..10, 15..17, 'a')
            fill(13..14, 13..14); fill(16..19, 13..14); fill(22..24, 11..14); fill(25..27, 9..14); fill(7..8, 7..7, 'K')
            fill(2..21, 8..8); fill(11..12, 6..7, 'w'); fill(4..6, 6..7)
            spawn(1); put(5, 5, 'D'); bits(19)
        },

        // 20 — a stateful firewall (R8 gates and a way, U13 firewall). Gate 3 at the exit is shut. The second check (a pad right at the start)
        // powers the stairs up to the ledge, but running straight on gets you nowhere: the first step is yours to hop, and landing on it
        // starts gate 2 on a rhythm over the next gap (wait for it); a twin gate over the gap after the third step. The ID scanner (a pad)
        // is where the stairs end, up on the ledge: it reopens gate 3 for a moment's notice, and the queue at the exit has other plans:
        // wait up there until it has passed. The floor behind you and the stairs go dark as you climb (the ledge is the only way on), and the
        // first step times out too if you camp on it
        Level(
            name = T("Stateful Inspection", "Zustandsprüfung"),
            intro = T("Please have your ID ready.", "Bitte Ausweis bereithalten."),
            start = listOf(
                Laser('K', 25 to 1, 25 to 14),
                Circuit('x'), Circuit('w', on = false), Circuit('s'),
                Pad('2', at = 1 to 14, circuits = "w"),
                Pad('1', at = 21 to 2),
            ),
            traps = listOf(
                trap(Pressed('2'), say("Second check started. The stairs are powered.", "Zweite Kontrolle gestartet. Die Treppe steht unter Strom.")),
                trap(Landed(6f, 7.9f), Play(Card.SPIKE_SEED), Laser('M', 8 to 1, 8 to 14, on = 1.3f, off = 1.4f, phase = 0.4f),
                    say("Gate 2 now runs on a rhythm.", "Tor 2 läuft jetzt im Takt.")),
                trap(Landed(6f, 7.9f), Circuit('x', on = false), say("Second check expired. Nobody is covering the floor behind you.", "Zweite Kontrolle abgelaufen. Den Boden hinter dir deckt niemand mehr.")),
                trap(Touch('s'), Circuit('s', on = false), say("This step times out, too. Do not camp.", "Diese Stufe läuft auch ab. Nicht campen."), delay = 1.7f),
                trap(Landed(12f, 13.9f), Laser('N', 14 to 1, 14 to 14, on = 1.3f, off = 1.4f, phase = 0.4f), say("Gate 2 has a twin. Same rhythm, other hole.", "Tor 2 hat einen Zwilling. Gleicher Takt, anderes Loch.")),
                trap(Landed(21.2f, 24.6f), Circuit('w', on = false), say("Stairs closed. The ledge is one-way.", "Treppe gesperrt. Der Sims ist eine Einbahnstraße.")),
                trap(Pressed('1'), Power('K', false), Laser('K', 25 to 1, 25 to 12, on = 0.5f, off = 40f, delay = 0.1f),
                    say("ID scanned. Gate 3 open. The queue at the exit has other plans.", "Ausweis gescannt. Tor 3 offen. Die Schlange am Ausgang hat andere Pläne.")),
            ),
            hint = T("The scanner is up on the ledge. Wait up there.", "Der Scanner steht oben auf dem Sims. Warte dort oben."),
            // rematch: stateless inspection, round 2 has no gates and nothing to wait for: the whole connection expires behind you. The floor
            // under the start is only good for a second (the door is at the far end of it: no running there), the stairs go dark one step
            // after the other (once, on your heels: not a rhythm), a tripwire flashes over the ledge (hop it), and the ledge drops you down
            // to the lane on the right, where the floor goes dark from the right to the left on your heels and the door waits at its end.
            // Round 1's run (up the stairs, wait up on the ledge for the queue) never gets that far: nobody opens a gate, and the ledge is
            // not where it was
            rematch = listOf(
                Round(
                    T("Stateless now. I forgot everything. Almost.", "Zustandslos jetzt. Ich merk mir nur noch dich."),
                    hint = T("Nothing expires while you are moving. Everything does when you stop.", "Nichts läuft ab, solange du dich bewegst. Alles, sobald du stehst."),
                    start = listOf(
                        Circuit('x'), Circuit('w', on = false), Circuit('k'), Circuit('u'),
                        Circuit('y'), Circuit('z'), Circuit('q'), Circuit('r'),
                    ),
                    traps = listOf(
                        trap(After(1.15f), Play(Card.SHORT_CIRCUIT), Circuit('x', on = false), say("Session expired. You got a second. Nobody renewed it.", "Sitzung abgelaufen. Du hattest eine Sekunde. Niemand hat verlängert.")),
                        trap(Landed(6f, 7.9f), Circuit('w', on = true), say("Handshake. One moment, please.", "Handschlag. Einen Moment, bitte."), delay = 0.9f),
                        trap(Touch('w'), Circuit('w', on = false), say("Idle connection dropped. Step by step.", "Verbindung im Leerlauf getrennt. Stufe für Stufe."), delay = 0.9f),
                        trap(Touch('w'), Circuit('k', on = false), delay = 1.5f),
                        trap(Touch('w'), Circuit('u', on = false), delay = 2.1f),
                        trap(Landed(21.9f, 24.5f), Laser('H', 25 to 4, 26 to 4, on = 1.8f, off = 40f, delay = 0.2f), say("Tripwire. I hid it in plain sight.", "Stolperdraht. Ich habe ihn offen versteckt.")),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('y', on = false), say("Lane floor: expired as well. Right to left.", "Boden unten: auch abgelaufen. Von rechts nach links."), delay = 0.7f),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('z', on = false), delay = 1.05f),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('q', on = false), delay = 1.4f),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('r', on = false), delay = 1.75f),
                    ),
                ) {
                    fill(9..10, 11..11, '.'); fill(12..13, 9..9, '.'); fill(15..16, 7..7, '.'); fill(18..19, 5..5, '.'); fill(21..24, 3..3, '.')
                    fill(10..11, 11..11, 'w'); fill(14..15, 9..9, 'k'); fill(18..19, 7..7, 'u'); fill(22..28, 5..5)
                    put(29, 14, '.'); put(15, 14, 'D'); put(26, 1, '.'); put(28, 1, '.')
                    fill(26..30, 15..17, 'y'); fill(23..25, 15..17, 'z'); fill(20..22, 15..17, 'q'); fill(17..19, 15..17, 'r')
                    fill(2..4, 9..9, '.'); put(3, 8, '.'); put(1, 14, 'P'); fill(13..14, 11..14)
                },
            ),
        ) {
            border(); floor()
            fill(2..14, 15..17, 'x')
            fill(6..7, 13..13, 's'); fill(9..10, 11..11, 'w'); fill(12..13, 9..9, 'w'); fill(15..16, 7..7, 'w'); fill(18..19, 5..5, 'w'); fill(21..24, 3..3)
            fill(2..4, 9..9)
            spawn(3, 8); door(); bits(20)
        },

        // 21 — EASTER EGG: the flat rate (R3 portal routing, U11 the route is manipulated). Two walls close off the lane, and in front
        // of them are two portals: the near one comes out in a closet between the walls, the far one leads up to the shelf, but its link
        // is down (it is dead until somebody opens the port), so the way in is the closet: the control room of the walled garden. Step
        // in, the port opens, step out again (back through the near portal), and the far portal works. Up on the shelf you are throttled to
        // 56k (a treadmill against you, with an LED at its far end: keep walking, and the ceiling is spiked: no jumping; past the treadmill the
        // rest of the shelf is throttled too, a slower belt that hands whoever stops back to the treadmill), the way on is down
        // through the hole, and on the lane the portal in front of the door is a captive portal: hop it
        Level(
            name = T("Flat Rate", "Flatrate"),
            intro = T("Unlimited flat rate. There is no small print.", "Unbegrenzte Flatrate. Ein Kleingedrucktes gibt es nicht."),
            start = listOf(
                Portal('a', 9 to 13, 16 to 14),
                Portal('b', 3 to 14, 6 to 7, twoWay = false),
                Power('b', false),
                Portal('r', 26 to 14, 28 to 14, twoWay = false),
            ),
            traps = listOf(
                trap(Zone(14f, 13f, 15f, 15.5f), Power('b', true), say("Walled garden. Unlimited flat rate, limited to this cell. And port 80.", "Walled Garden. Flatrate unbegrenzt, begrenzt auf diese Zelle. Und Port 80.")),
                trap(Zone(3.5f, 5.5f, 7.2f, 8f), Belt('c', -6.5f), say("Terms accepted. Throttled to 56k, as agreed.", "AGB akzeptiert. Gedrosselt auf 56k, wie vereinbart.")),
                trap(Zone(10.5f, 5.5f, 12f, 8f), Reroute('b', 26 to 7), Belt('d', -3f), say("Portal b has moved on. The rest of the shelf is throttled, too.", "Portal b ist weitergezogen. Der Rest vom Regal ist auch gedrosselt.")),
                trap(Zone(24.4f, 13f, 25.8f, 15.5f), Play(Card.DECOY), Reroute('r', 4 to 7), say("301 Moved Permanently. The portal too.", "301 Moved Permanently. Das Portal auch.")),
            ),
            hint = T("The far portal has no link. Ask the near one why.", "Das ferne Portal hat keine Verbindung. Frag das nahe, warum."),
        ) {
            border(); floor()
            fill(12..13, 9..14); fill(17..18, 9..14)
            fill(1..19, 8..8); fill(2..6, 8..8, 'c'); fill(7..19, 8..8, 'd'); fill(22..30, 8..8)
            put(2, 7, '^'); ceilingSpikes(1..21, 6)
            spawn(11); door(); bits(21)
        },

        // 22 — the bouncer, a trap room (U7 the saw, with a replay as the punchline): you start down on the lane at the right and run left; the
        // first bouncer rolls out of the far wall and comes at you, fast (hop him). Up the two steps at the left end and onto the club floor, where
        // the door waits in the middle; as you climb, the second bouncer rolls out of the right wall and patrols the club floor, down to the top
        // step and back (hop him). As you land behind him the guest list is reloaded: you are put back where you jumped, and the bouncer is on his
        // way back from the step: run for the door, or hop him once more
        Level(
            name = T("Bouncer", "Türsteher"),
            intro = T("The bouncer won't let you in. I like him.", "Der Türsteher lässt dich nicht rein. Ich mag ihn."),
            traps = listOf(
                trap(BeforeX(18f), Saw(-1.5f, 14.4f, 9f, 0f), say("Bouncer: you're not on the list.", "Türsteher: Du stehst nicht auf der Liste.")),
                trap(Zone(0.5f, 9f, 2.9f, 11.1f), PathSaw(12f, 31.5f to 9.4f, 4f to 9.4f, 1.5f to 10.3f), say("Second bouncer. He patrols the floor.", "Zweiter Türsteher. Er geht Streife.")),
                trap(Zone(15.5f, 9.3f, 17.5f, 10.1f), Play(Card.UNDO), Undo(0.6f), say("Guest list reloaded. Back in the queue, and he is coming back too.", "Gästeliste neu geladen. Zurück in die Schlange, und er kommt auch zurück.")),
            ),
            hint = T("Hop each bouncer as he comes. When the list is reloaded, run for the door before the second one is back.", "Spring über jeden Türsteher, wenn er kommt. Wird die Liste neu geladen, renn zur Tür, bevor der zweite zurück ist."),
        ) {
            border(); floor()
            fill(4..26, 10..10)
            fill(3..3, 13..14); fill(1..2, 11..14)
            spawn(29, 14); door(19, 9); bits(22)
        },

        // 23 — the information superhighway, a trap room (U12 the transport goes wrong, floor-move and belts): lifts. The merge lane of the on-ramp
        // is a conveyor that speeds you up toward the on-ramp, and the on-ramp itself is a lift flush with the road; it carries whoever steps on it up
        // to the deck (walk off to the right at once), and the moment it has passed the deck it goes full throttle into a ceiling of spikes. Along the deck a piece of the road is closed for repairs (hop it),
        // and the last lift, to the exit deck, is out of service as soon as it has arrived: it rises four tiles, and a moment later it drops away
        // under whoever stays on it (walk off to the left onto the exit deck, where the door is)
        Level(
            name = T("Information Superhighway", "Datenautobahn"),
            intro = T("Have a safe trip! Buckle up.", "Gute Fahrt! Bitte anschnallen."),
            traps = listOf(
                trap(PastX(2.6f), Belt('g', 10f), say("Merge lane: accelerate to highway speed.", "Einfädelspur: auf Autobahntempo beschleunigen."), delay = 0.1f),
                trap(Touch('a'), Move('a', 0f, -6f, 3f), say("On-ramp open. Next exit: the ceiling.", "Auffahrt frei. Nächste Ausfahrt: die Decke.")),
                trap(Touch('a'), Move('a', 0f, -13f, 16f), say("Exit missed. Full throttle.", "Ausfahrt verpasst. Vollgas."), delay = 2.1f),
                trap(PastX(15f), Move('b', -3f, 0f, 14f), say("Roadworks ahead. Nobody told the road.", "Baustelle voraus. Der Straße hat's keiner gesagt.")),
                trap(Touch('c'), Move('c', 0f, -4f, 12f), say("Express lift to the exit. Doors closing.", "Expresslift zur Ausfahrt. Türen schließen.")),
                trap(Touch('c'), Play(Card.SINKING), Move('c', 0f, 14f, 9f), say("Out of service. Effective immediately.", "Außer Betrieb. Ab sofort."), delay = 0.8f),
            ),
            hint = T("Do not ride to the top. Get off where the deck is.", "Fahr nicht bis nach oben. Steig aus, wo das Deck ist."),
        ) {
            border(); floor()
            fill(7..8, 15..15, 'g')
            fill(10..11, 15..15, 'a'); ceilingSpikes(10..11)
            pit(12..27); fill(12..27, 17..17, '^')
            fill(12..12, 10..14); fill(12..27, 9..9); fill(19..21, 9..9, 'b'); fill(26..27, 9..9, 'c')
            fill(28..30, 1..14)
            fill(13..25, 5..5)
            spawn(1); door(18, 4); bits(23)
        },

        // 24 — the uplink, a trap room (U2 the floor slides and the ceiling comes down, with a saw as the last surprise): the elevator is out of order
        // and the uplink is in the basement, so you start up on the top deck and run right. There is a gap in the deck ahead, over the door; as you
        // set off, the deck piece in front of it slides over into it, and the gap is where you are about to run, over a row of LEDs (stop, then jump
        // it). Along the deck a rack is delivered from the ceiling onto whoever runs under it (stop, then hop it). At the end you drop down to the lane and run back left under the underside of the deck, which comes down on whoever steps under it
        // (keep running); and as you land on the lane, the uplink sends its traffic down through the hole you jumped: a saw drops through it and
        // rolls along the lane at you (hop it)
        Level(
            name = T("Uplink", "Uplink"),
            intro = T("The uplink is in the basement. So is the elevator.", "Der Uplink ist im Keller. Der Aufzug auch."),
            traps = listOf(
                trap(PastX(4.5f), Move('a', 3f, 0f, 12f), say("Packet reordered. Your floor arrives out of sequence.", "Paket umsortiert. Dein Boden kommt in falscher Reihenfolge an."), delay = 0.1f),
                trap(PastX(16.6f), Move('c', 0f, 4f, 14f), say("A rack for you. Delivered from above.", "Ein Rack für dich. Von oben geliefert."), delay = 0.15f),
                trap(Zone(19f, 12.5f, 26.9f, 15.5f), Move('r', 0f, 6f, 3.6f), say("Uplink full. Try the downlink.", "Uplink voll. Versuch's mit dem Downlink.")),
                trap(Landed(26f, 31f), Play(Card.DEVIL_SAW), PathSaw(12f, 10f to 6.5f, 10f to 14.4f, 30f to 14.4f), say("Downlink traffic. It came through your hole.", "Downlink-Verkehr. Er kam durch dein Loch.")),
            ),
            hint = T("The gap in the deck moves: stop when it does, then jump it. Under the deck, keep running, and hop what comes down the hole.", "Die Lücke im Deck wandert: Bleib stehen, wenn sie es tut, dann spring. Unter dem Deck: weiterlaufen, und spring über das, was durchs Loch kommt."),
        ) {
            border(); floor()
            fill(0..26, 6..6); fill(9..11, 6..6, 'a'); fill(12..14, 6..6, '.')
            leds(8..14); fill(19..20, 1..1, 'c')
            fill(19..26, 7..8, 'r')
            spawn(1, 5); door(16, 14); bits(24)
        },

        // 25 — the load balancer, a trap room (U1 the floor goes): one way over a spike pit, from node to node, and every node balances the
        // load differently. The first sinks under you (do not stop on it), the second passes the load back (a belt that starts against you
        // as you land: hop straight on), and the third is steady, but landing on it sends the load to the floor in front of the door, which
        // sinks into the spikes and only comes back up a moment later (wait on the third node).
        Level(
            name = T("Load Balancer", "Lastverteiler"),
            intro = T("I distribute the load evenly. Onto you.", "Ich verteile die Last gleichmäßig. Auf dich."),
            traps = listOf(
                trap(Touch('a'), Play(Card.HEADBUTT), Move('a', 0f, 3f, 2.4f), say("Node 1 is overloaded.", "Knoten 1 ist überlastet.")),
                trap(Touch('b'), Belt('b', -6f), say("Node 2 passes the load back.", "Knoten 2 reicht die Last zurück.")),
                trap(Landed(14.5f, 18f), Move('e', 0f, 2.5f, 10f), say("Node 4 is busy. Please hold.", "Knoten 4 ist besetzt. Bitte warten.")),
                trap(Landed(14.5f, 18f), Move('e', 0f, -2.5f, 5f), delay = 0.9f),
                trap(Touch('e'), Move('e', 0f, 3f, 6f), say("Node 4: maintenance window. Starts now.", "Knoten 4: Wartungsfenster. Beginnt jetzt."), delay = 1.05f),
            ),
            hint = T("Do not stop on the first two nodes. On the third, wait for the floor to come back.", "Bleib auf den ersten zwei Knoten nicht stehen. Auf dem dritten: warte, bis der Boden zurück ist."),
            // rematch: rebalanced against what worked: the first node holds now, the second sinks under you (hop straight on), and the third,
            // where you waited, is a belt that runs back toward the pit: you cannot stand and wait on it. Walk against it, let it carry you back,
            // walk again, until the floor in front of the door is back, and then over it at once: its maintenance window is shorter (hop off its end)
            rematch = listOf(
                Round(
                    T("Rebalanced. Your favourite node is busy.", "Neu verteilt. Dein Lieblingsknoten ist beschäftigt."),
                    hint = T("The second node sinks, and the third runs back to the pit: keep walking against it until the floor ahead is back, then run and hop off its end.", "Der zweite Knoten sinkt, und der dritte läuft zur Grube: Lauf gegen ihn an, bis der Boden vorn zurück ist, dann renn und spring von seinem Ende."),
                    traps = listOf(
                        trap(Touch('b'), Move('b', 0f, 3f, 2.4f), say("Node 2 is overloaded today.", "Knoten 2 ist heute überlastet.")),
                        trap(Touch('c'), Play(Card.BACKDRAFT), Belt('c', -4.5f), say("Node 3: backpressure. Do not get comfortable.", "Knoten 3: Gegendruck. Mach es dir nicht bequem.")),
                        trap(Landed(14.5f, 18f), Move('e', 0f, 2.5f, 10f), say("Node 4 is busy. Please keep moving.", "Knoten 4 ist besetzt. Bitte in Bewegung bleiben."), delay = 0.35f),
                        trap(Landed(14.5f, 18f), Move('e', 0f, -2.5f, 8f), delay = 0.95f),
                        trap(Zone(18.2f, 13f, 25f, 15.5f), Move('e', 0f, 3f, 4f), say("Node 4: maintenance window. A short one.", "Knoten 4: Wartungsfenster. Ein kurzes."), delay = 0.5f),
                    ),
                ),
            ),
        ) {
            border(); floor()
            pit(5..24); fill(5..24, 17..17, '^')
            fill(5..8, 15..15, 'a'); fill(11..13, 15..15, 'b'); fill(15..17, 15..15, 'c')
            fill(18..24, 15..15, 'e')
            spawn(1); door(29); bits(25)
        },

        // 26 — the queue, a trap room (U8 the stalker): you start on the top deck at the right and run left; the deck piece ahead drops as you
        // come near (hop it: under the hole the middle deck has LEDs, no shortcut) and at the left end you fall onto the middle deck, where you
        // run right. As you land the queue starts: a block of spikes at the left end that follows you along the deck, nearly as fast as you
        // run (do not stop, hop the LEDs under the hole on the run). At the right end you fall onto the lane, and the second queue is already
        // there and comes at you head-on: hop it, then run left to the door.
        // Rematch: number 42. The first queue waits at the counter in the middle of the middle deck and comes at you head-on (hop it, then the
        // LEDs), and the second one starts behind you at the right wall of the lane and follows you to the door (hop the block on the run)
        Level(
            name = T("Ticket Number", "Wartenummer"),
            intro = T("Your number is 41. Now serving: 3. Please walk.", "Ihre Nummer ist 41. Aufgerufen wird: 3. Bitte gehen Sie."),
            traps = listOf(
                trap(BeforeX(26f), Hide('f'), say("Number 40 was called. The floor was number 39.", "Nummer 40 wurde aufgerufen. Der Boden war Nummer 39."), delay = 0.15f),
                trap(Zone(0f, 8f, 24f, 10.6f), Play(Card.STALKER), Chase('S', speed = 7.5f, left = 0f, right = 23f), say("The queue moves up. Onto you.", "Die Schlange rückt auf. Auf dich.")),
                trap(Zone(23f, 12f, 31f, 15.6f), Chase('Q', speed = 5.5f, left = 0f, right = 20f), say("Queue two. It skipped the line.", "Schlange zwei. Sie hat sich vorgedrängelt.")),
            ),
            hint = T("Hop the hole on the top deck. Below, keep running: the queue is nearly as fast as you. The last one comes at you: hop it.", "Spring über das Loch oben. Unten: weiterlaufen, die Schlange ist fast so schnell wie du. Die letzte kommt dir entgegen: drüber."),
            rematch = listOf(
                Round(
                    T("Number 42. Please come to counter 2. The queue is already there.", "Nummer 42. Bitte zu Schalter 2. Die Schlange ist schon da."),
                    hint = T("The first queue comes at you now: hop it, then the LEDs. The second one comes from behind.", "Die erste Schlange kommt dir jetzt entgegen: drüber, dann über die LEDs. Die zweite kommt von hinten."),
                    traps = listOf(
                        trap(BeforeX(26f), Hide('f'), say("Number 41 expired. So did the floor.", "Nummer 41 ist abgelaufen. Der Boden auch."), delay = 0.15f),
                        trap(Zone(0f, 8f, 16f, 10.6f), Play(Card.STALKER), Chase('S', speed = 3.5f, left = 7f, right = 0f), say("Counter 2 opened. The queue saw you first.", "Schalter 2 ist offen. Die Schlange hat dich zuerst gesehen.")),
                        trap(Zone(23f, 12f, 31f, 15.6f), Chase('Q', speed = 7.5f, left = 28f, right = 0f), say("The second queue was already waiting.", "Die zweite Schlange wartete schon.")),
                    ),
                ) {
                    fill(0..1, 9..9, '.'); put(17, 9, 'S')
                    put(8, 14, '.'); put(30, 14, 'Q'); put(15, 14, '#')
                },
            ),
        ) {
            border(); floor()
            fill(14..30, 5..5); put(20, 5, 'f'); put(20, 9, '^')
            fill(0..24, 10..10); fill(28..30, 10..10)
            fill(0..1, 9..9, 'S'); put(8, 14, 'Q')
            spawn(29, 4); door(4, 14); bits(26)
        },

        // 27 — DDoS, a trap room (U2 the ceiling comes down, on a belt that runs against you): the stairs to the exit are built from packets. The
        // first two drop from the ceiling, one and two tiles high: the first comes down on the spot you reach if you keep running, the second when
        // you step onto the first. The belt carries you back while you wait. The third request comes the other way: a piece of the floor past the
        // second packet is a lift, and it rises as you step on it, up past the walkway and on into the ceiling (get off at the walkway). The walkway
        // is a belt, too, and one more packet comes down just where a runner would be (let the belt hold you back, then hop it).
        Level(
            name = T("DDoS", "DDoS"),
            intro = T("Light traffic today. Just you and the stairs.", "Heute wenig Verkehr. Nur du und die Treppe."),
            start = listOf(Belt('b', -3f), Belt('l', -4f)),
            traps = listOf(
                trap(PastX(5.9f), Play(Card.COLLAPSE), Move('c', 0f, 13f, 24f), say("10,000 packets per second.", "10.000 Pakete pro Sekunde."), delay = 0.1f),
                trap(Touch('c'), Move('d', 0f, 12f, 30f), say("Request 10,001.", "Anfrage 10.001.")),
                trap(Touch('e'), Move('e', 0f, -14f, 6f), say("Request 10,002. It comes from below, and it does not stop.", "Anfrage 10.002. Die kommt von unten, und sie hält nicht an.")),
                trap(PastX(19.5f), Move('f', 0f, 8f, 16f), say("And one more for the road.", "Und noch eins für unterwegs."), delay = 0.4f),
            ),
        ) {
            border(); floor()
            fill(3..24, 15..15, 'b')
            fill(8..10, 1..1, 'c'); fill(11..13, 1..2, 'd'); fill(14..16, 15..15, 'e'); put(23, 1, 'f')
            fill(17..30, 10..10, 'l')
            spawn(1, 14); door(29, 9); bits(27)
        },

        // 28 — a VPN tunnel under the firewall, a trap room (U13 the laser, R3 the portal): the firewall is a wall of light you cannot cross, the
        // tunnel is the only way past it. Walking into the tunnel re-points it (DNS changed): you come out right in front of the IPS, which starts a
        // gate as you appear (run on and you are in it), and a second one as you pass the first. At the end of the lane a second tunnel leads up to the
        // walkway under the ceiling, where you run back to the door, and a third gate starts as you come out. Right in front of the door a tripwire
        // lights up at ankle height as you get there: hop it into the door.
        Level(
            name = T("Split Tunnel", "Split Tunnel"),
            intro = T("The tunnel is the only shortcut. I swear.", "Der Tunnel ist die einzige Abkürzung. Ich schwöre."),
            start = listOf(Laser('F', 8 to 10, 8 to 14), Portal('1', 5 to 14, 11 to 14, twoWay = false), Portal('2', 28 to 14, 30 to 8, twoWay = false)),
            traps = listOf(
                trap(Zone(3.4f, 12f, 4.9f, 15f), Reroute('1', 13 to 14), say("Split tunnel: your exit has moved.", "Split Tunnel: Dein Ausgang ist umgezogen.")),
                trap(Zone(12.4f, 12f, 13.8f, 15f), Play(Card.SPIKE_SEED), Laser('M', 17 to 10, 17 to 14, on = 0.4f, off = 2.0f, delay = 0.2f), say("IPS: tunnel detected. New rule installed.", "IPS: Tunnel erkannt. Neue Regel installiert.")),
                trap(PastX(17.6f), Laser('N', 22 to 10, 22 to 14, on = 0.4f, off = 2.0f, delay = 0.3f), say("Rule 2. Same rhythm, other hole.", "Regel 2. Gleicher Takt, anderes Loch.")),
                trap(PastX(22.6f), Laser('P', 26 to 10, 26 to 14, on = 0.4f, off = 2.0f, delay = 0.2f), say("Rule 3. I have a lot of rules.", "Regel 3. Ich habe viele Regeln.")),
                trap(Zone(18f, 6f, 24f, 9f), Laser('O', 14 to 1, 14 to 8, on = 0.4f, off = 2.0f, delay = 0.9f), say("Rule 4. This one is a rule I made up just now.", "Regel 4. Diese Regel habe ich mir gerade ausgedacht.")),
                trap(Zone(9.5f, 6f, 13.2f, 9.2f), Laser('T', 9 to 8, 13 to 8, delay = 0.3f), say("Rule 5: no loitering in front of the door.", "Regel 5: Kein Herumlungern vor der Tür.")),
            ),
        ) {
            border(); floor()
            fill(2..30, 9..9)
            spawn(); door(8, 8); bits(28)
        },

        // 29 — the race condition, a trap room (U7 the saw, U1 the floor): the lane ends in three steps up to the door, and the threads fight for
        // it. The first is a pendulum that is set off when you come near: it sits on the floor for a moment and then swings up and down (stop
        // in front of it and slip under it when it is up). The second is forked from below: a saw shoots up out of the slot in the lane as you
        // run at it, in front of you (stop, let it pass). The third pendulum hangs in front of the stairs, and the floor where everybody stops to
        // wait for it is freed while you stand on it (wait further back, then go in one run).
        // Rematch: a new scheduler. The first pendulum swings already, the slot forks twice (once right where round 1 waited), and the third
        // floor behind the third pendulum, in front of the stairs, is freed the moment you step on it: jump from under the pendulum straight onto the stairs
        Level(
            name = T("Race Condition", "Wettlaufsituation"),
            intro = T("The winner gets a cookie. I have no cookies.", "Der Sieger bekommt einen Keks. Ich habe keine Kekse."),
            traps = listOf(
                trap(PastX(4f), PathSaw(3f, 9f to 14f, 9f to 11.2f, delay = 0.8f, r = 1f), say("Thread 1 holds the lock. Thread 2 wants it.", "Thread 1 hält das Lock. Thread 2 will es.")),
                trap(PastX(10.4f), Saw(13.4f, 19.5f, 0f, -16f, r = 0.8f), say("Thread 2 was forked from below. Right in front of you.", "Thread 2 wurde von unten geforkt. Direkt vor dir.")),
                trap(PastX(17f), PathSaw(3f, 23.5f to 14f, 23.5f to 11.2f, delay = 0.6f, r = 1f), say("Thread 3. Nobody told me about thread 3.", "Thread 3. Von Thread 3 hat mir keiner was gesagt.")),
                trap(Zone(19.3f, 12f, 21.6f, 15.5f), Play(Card.CRUMBLE), Fall('w'), say("Memory freed. You were standing on it.", "Speicher freigegeben. Du standest drauf."), delay = 0.4f),
            ),
            hint = T("Stop for the saw out of the floor. Wait for the last saw, but not right in front of it: the floor there is freed.", "Bleib stehen für die Säge aus dem Boden. Warte auf die letzte Säge, aber nicht direkt davor: Der Boden dort wird freigegeben."),
            rematch = listOf(
                Round(
                    T("Rerun. Same threads, new scheduler.", "Nochmal. Gleiche Threads, neuer Scheduler."),
                    start = emptyList(),
                    hint = T("Where you waited for the last saw, a fork comes up now. Wait further back, go in one run, and behind the last saw jump straight onto the stairs.", "Wo du auf die letzte Säge gewartet hast, kommt jetzt ein Fork hoch. Warte weiter hinten, lauf in einem Zug, und hinter der letzten Säge spring direkt auf die Treppe."),
                    traps = listOf(
                        trap(PastX(4f), PathSaw(3f, 9f to 12.2f, 9f to 14f, 9f to 6f, r = 1f), say("Thread 1 swings already. I am proud of it.", "Thread 1 schwingt schon. Ich bin stolz auf ihn.")),
                        trap(PastX(10.4f), Saw(13.4f, 19.5f, 0f, -16f, r = 0.8f), say("Thread 2 forks early.", "Thread 2 forkt früh.")),
                        trap(PastX(14f), PathSaw(3f, 23.5f to 14f, 23.5f to 7f, delay = 0.9f, r = 1f), say("Thread 3 was always going to be here.", "Thread 3 war immer schon hier.")),
                        trap(PastX(17.5f), Saw(18.8f, 19.5f, 0f, -11f, r = 0.8f), say("Fork bomb. The second child likes your waiting spot.", "Fork-Bombe. Das zweite Kind mag deinen Warteplatz."), delay = 0.3f),
                        trap(Zone(23.7f, 12f, 26f, 15.5f), Play(Card.CRUMBLE), Fall('q'), say("Freed before you even asked.", "Freigegeben, bevor du gefragt hast."), delay = 0.05f),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(19..21, 15..17, 'w'); fill(24..25, 15..17, 'q')
            fill(26..27, 13..14); fill(28..29, 11..14); fill(30..30, 9..14)
            spawn(); door(30, 8); bits(29)
        },

        // 30 — hop limit, a puzzle room of two rooms (R3 the portal, R5 the floors, U11 the route is manipulated, U18 the room goes on): the tunnel on the
        // lane leads up to the ledge, and the ledge piece in front of the door drops out over a row of LEDs as you come near (TTL expired: hop the
        // hole). At the door the wall breaks open and the door slips into the second room. There you run along the ledge and drop onto a lane
        // whose pit is crossed by two portals: the obvious one, in front of you, is re-pointed as you walk the ledge (and now sends you home, TTL), and the
        // other one is behind you, under the ledge, which you have to walk back to; you step out of it into spikes that grow
        Level(
            name = T("Hop Limit", "Hop-Limit"),
            intro = T("TTL: 64. No need to rush.", "TTL: 64. Kein Grund zur Eile."),
            legend = mapOf('A' to hidden),
            rooms = 2,
            start = listOf(
                Portal('a', 9 to 14, 10 to 8, twoWay = false),
                Portal('b', roomX(1, 15f).toInt() to 14, roomX(1, 28f).toInt() to 14, twoWay = false),
                Portal('c', roomX(1, 5) to 14, roomX(1, 26) to 13, twoWay = false),
            ),
            traps = listOf(
                trap(PastX(14.5f), Fall('f'), say("TTL exceeded in transit. The floor too.", "TTL in der Übertragung überschritten. Der Boden auch."), delay = 0.3f),
                trap(Trigger.AtDoor, Play(Card.ANNEX), Action.Extend(into = 1, top = 6, bottom = 8, door = roomX(1, 30f).toInt() to 14)),
                trap(Zone(roomX(1, 6f), 1f, roomX(1, 9.5f), 9f), Reroute('b', roomX(1, 20) to 16), say("Hop 2 of 3: the obvious route was deprecated. It goes to /dev/null now.", "Hop 2 von 3: Die offensichtliche Route wurde abgekündigt. Sie führt jetzt nach /dev/null.")),
                trap(Zone(roomX(1, 6f), 1f, roomX(1, 9.5f), 9f), Reroute('c', roomX(1, 20) to 16), say("TTL 0. The backup route is gone, too.", "TTL 0. Die Ersatzroute ist auch weg."), delay = 2.5f),
                trap(Landed(roomX(1, 24.5f), roomX(1, 27.5f)), Show('A'), say("Hop 3 of 3: spikes. Right where the link drops you.", "Hop 3 von 3: Spikes. Genau da, wo der Link dich absetzt."), delay = 0.4f),
            ),
        ) {
            border(); floor()
            room(0) {
                fill(1..3, 9..9); put(2, 8, 'P')
                fill(10..30, 9..9); fill(19..21, 9..9, 'f')
                pit(17..23)
                put(28, 8, 'D')
            }
            room(1) {
                fill(0..9, 9..9)
                pit(17..23); fill(17..23, 17..17, '^')
                fill(24..26, 14..14, 'A')
            }
        },

        // 31 — detention, a puzzle room (R8 the timing plus the way, U3 the wall closes in): you start up on the top deck and the class wall, set with
        // spikes, slides in behind you from the left, so you cannot wait where you like. Over a trench of spikes three stones blink one after the
        // other like a wave; you hop from stone to stone as each one comes up, and when you reach the middle one the wall speeds up (detention is
        // over, go home). At the end of the deck you drop down onto the lane, where the second wall stands against the right wall, and run back
        // left to the door with it behind you
        Level(
            name = T("Detention", "Nachsitzen"),
            intro = T("No rush. I've got all day.", "Keine Hektik. Ich habe den ganzen Tag Zeit."),
            legend = mapOf('W' to Glyph(spike = true, dir = Dir.RIGHT), 'X' to Glyph(spike = true, dir = Dir.LEFT)),
            traps = listOf(
                trap(PastX(5.5f), Play(Card.SINKING), Move('W', 13f, 0f, 3.2f), say("Detention. Sit down. Stay seated.", "Nachsitzen. Setzen. Sitzen bleiben.")),
                trap(PastX(5f), Blink('s', 1.6f, 1.2f, phase = -1.2f), say("Please take a seat. The seat is leaving.", "Bitte nehmen Sie Platz. Der Platz geht gerade.")),
                trap(Zone(11f, 6f, 14f, 8.5f), Blink('t', 1.6f, 1.2f, phase = -0.9f)),
                trap(Zone(16f, 6f, 19f, 8.5f), Blink('u', 1.6f, 1.2f, phase = -0.9f)),
                trap(Zone(26f, 12f, 31f, 15.5f), Move('X', -25f, 0f, 7f), say("The second class has to go home, too.", "Die zweite Klasse muss auch nach Hause.")),
                trap(Zone(19f, 12f, 21f, 15.5f), Move('X', -25f, 0f, 9.5f), say("Oh, and they are in a hurry.", "Ach so, und sie haben es eilig.")),
            ),
            hint = T("The wall is slower than you. The stones are not.", "Die Wand ist langsamer als du. Die Steine nicht."),
        ) {
            border(); floor()
            fill(1..2, 1..8, 'W'); fill(30..30, 10..14, 'X')
            fill(1..26, 9..9)
            fill(10..25, 9..9, '^')
            fill(11..13, 8..8, 's'); fill(16..18, 8..8, 't'); fill(21..23, 8..8, 'u')
            spawn(4, 8); door(14, 14); bits(31)
        },

        // 32 — the act finale, a puzzle room (R4 the switch, R3 the portals, R8 the timing; U12 the transport goes wrong, with U13 given up because the one
        // laser level is 28): the lane is split by two firewalls and the door is behind the second one. The first firewall is crossed by a tunnel, and
        // the tunnel goes down for maintenance as you hop the LED in front of it (keep pushing against the belt until it is back), and the belt
        // behind it turns around as you step on it (hop it, or you are carried into the spikes). The tunnel up to the deck is the only way on:
        // up there you walk back left, and the deck
        // belt turns into an express lane as you come near the switch (jump the LED it throws you at), onto the switch at the far end, which
        // powers the tunnel under the second firewall. Then back down to the lane, the same way again, and through the second tunnel, which
        // reboots as you come out of the first one (wait for it)
        Level(
            name = T("Core Switch", "Core-Switch"),
            intro = T("Finally, the data-center basement. This is where I live.", "Endlich der Rechenzentrumskeller. Hier wohne ich."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Belt('a', -6.5f), Belt('b', 3f),
                Portal('1', 13 to 14, 17 to 14, twoWay = false),
                Portal('2', 22 to 14, 26 to 14, twoWay = false), Power('2', false),
                Portal('3', 23 to 14, 28 to 8, twoWay = false), Power('3', false),
                Pad('1', at = 4 to 8),
            ),
            traps = listOf(
                trap(Zone(5.5f, 12f, 7f, 15.5f), Power('3', true), say("Tunnel 3 is enabled for you. For the moment.", "Tunnel 3 ist für dich freigeschaltet. Vorerst.")),
                trap(Airborne(7.6f, 10.5f), Power('1', false), say("Tunnel 1: scheduled maintenance. Right now.", "Tunnel 1: geplante Wartung. Genau jetzt.")),
                trap(Airborne(7.6f, 10.5f), Power('1', true), delay = 1.2f),
                trap(PastX(18.5f), Play(Card.GRAND_FINALE), Belt('b', -9f), say("Spanning tree recalculated. Your belt now runs the other way.", "Spanning Tree neu berechnet. Dein Band läuft jetzt andersrum.")),
                trap(Zone(19.15f, 14.45f, 19.95f, 15.5f), Power('2', false), say("Tunnel 2: rebooting. Hold the line.", "Tunnel 2: Neustart. Bitte warten.")),
                trap(Zone(19.15f, 14.45f, 19.95f, 15.5f), Power('2', true), delay = 0.7f),
                trap(Zone(11f, 6f, 13.5f, 9f), Belt('c', -3f), say("Priority traffic. Express lane to the switch, no stopping.", "Priorisierter Verkehr. Expressspur zum Schalter, kein Halt.")),
                trap(Pressed('1'), Power('2', true), Belt('b', 3f), say("Switch thrown. Tunnel 2 is up and the belt is back. Probably.", "Schalter umgelegt. Tunnel 2 ist oben und das Band wieder da. Wahrscheinlich.")),
                trap(Zone(26.5f, 6f, 31f, 9.3f), Show('A'), say("Broadcast storm.", "Broadcast-Sturm.")),
                trap(Zone(0f, 9.6f, 3f, 13.5f), Reroute('1', 19 to 14), say("DNS changed. Tunnel 1 comes out further on now.", "DNS geändert. Tunnel 1 kommt jetzt weiter hinten raus."), delay = 1.2f),
            ),
        ) {
            border(); floor()
            fill(3..30, 9..9)
            fill(14..15, 10..14); fill(24..25, 10..14)
            fill(3..12, 15..15, 'a'); fill(17..20, 15..15, 'b'); fill(8..21, 9..9, 'c')
            put(9, 14, '^'); put(16, 14, '^'); put(7, 8, '^')
            put(23, 8, 'A'); put(24, 8, 'A')
            spawn(1); door(29)
        },
    )
}
