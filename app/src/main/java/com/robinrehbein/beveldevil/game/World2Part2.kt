package com.robinrehbein.beveldevil.game

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
                trap(Zone(9.6f, 9f, 15f, 12.2f), Play(Card.HEADBUTT), Fall('c'), say("Packet from the upper bus. Mind your head.", "Paket vom oberen Bus. Kopf einziehen.")),
                trap(Zone(14f, 6.5f, 19f, 8.4f), Fall('d'), say("Shelf 2 is decoration. Do not stand on it.", "Regal 2 ist Deko. Nicht draufstellen."), delay = 0.55f),
                trap(Touch('s'), Belt('b', 5f), say("Bus 1 reversed. Next stop: the door.", "Bus 1 fährt jetzt andersrum. Nächster Halt: die Tür.")),
                trap(Zone(10f, 13.5f, 21f, 15f), Fall('e'), say("Last stop. The station is closed. Please jump.", "Endstation. Der Bahnhof ist gesperrt. Bitte springen.")),
            ),
            hint = T("The bus turns around somewhere. Not down here.", "Irgendwo dreht der Bus um. Nicht hier unten."),
        ) {
            border(); floor()
            fill(4..5, 13..14)
            fill(7..20, 12..12); fill(8..14, 13..13, 'v'); fill(7..20, 15..15, 'b')
            fill(21..21, 1..12); fill(19..20, 10..11)
            fill(25..26, 15..17, 'e')
            fill(3..18, 8..8); fill(3..4, 8..8, 's'); fill(11..13, 8..8, 'c'); fill(14..18, 8..8, 'd')
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
                trap(PastX(10.5f), Move('a', 0f, 12f, 30f), say("Packet loss. The floor was never in the rule set.", "Paketverlust. Der Boden stand nie im Regelwerk.")),
                trap(PastX(20.6f), Play(Card.SPIKE_SEED), Laser('G', 23 to 1, 23 to 14, on = 0.4f, off = 40f, delay = 0.1f),
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
                        trap(PastX(14.5f), Play(Card.COLLAPSE), Move('a', 0f, 12f, 30f), say("Packet loss, second edition. A bit further on.", "Paketverlust, zweite Auflage. Etwas weiter hinten.")),
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
            fill(12..14, 13..13); fill(8..10, 11..11); fill(4..6, 9..9); fill(1..2, 7..7)
            fill(25..27, 13..14)
            fill(17..18, 15..17, 'a')
            spawn(2); put(2, 6, 'D'); bits(18)
        },

        // 19 — a trap room: the floor drops out ahead (a hop), the packets get reordered (left and right swap) while the stairs
        // go up to the right, and on the top floor, on the way back left to the door, the order is restored and a wall drives
        // toward you: hop it
        Level(
            name = T("Delivery", "Zustellung"),
            intro = T("Packets arrive in order. Guaranteed.", "Pakete kommen der Reihe nach an. Garantiert."),
            traps = listOf(
                trap(PastX(4.5f), Move('a', 0f, 12f, 30f), say("Packet 1 got lost in transit.", "Paket 1 ging unterwegs verloren.")),
                trap(Landed(10.9f, 12.7f), Play(Card.TWISTED), Swap(true), say("Packet reordering! Left and right arrive swapped.", "Paket-Umsortierung! Links und rechts kommen vertauscht an.")),
                trap(Landed(25f, 28f), Swap(false), say("In-order delivery restored. You're welcome.", "Reihenfolge wiederhergestellt. Gern geschehen.")),
                trap(Landed(25f, 28f), Move('w', 9f, 0f, 5f), say("Return to sender. Express.", "Rücksendung an den Absender. Per Express."), delay = 0.2f),
            ),
            hint = T("Left is right. Until the top floor.", "Links ist rechts. Bis zum Obergeschoss."),
        ) {
            border(); floor()
            fill(8..10, 15..17, 'a')
            fill(13..14, 13..14); fill(16..19, 13..14); fill(22..24, 11..14); fill(25..27, 9..14); fill(7..8, 6..7)
            fill(2..21, 8..8); fill(11..12, 6..7, 'w')
            spawn(1); put(2, 7, 'D'); bits(19)
        },

        // 20 — a stateful firewall (R8 gates and a way, U13 firewall). Gate 3 at the exit is shut. The second check (a pad right at the start)
        // powers the stairs up to the ledge, but running straight on gets you nowhere: the first step is yours to hop, and landing on it
        // starts gate 2 on a rhythm over the next gap (wait for it); a twin gate over the gap after the third step. The ID scanner (a pad)
        // is where the stairs end, up on the ledge: it reopens gate 3 for a moment's notice, and the queue at the exit has other plans:
        // wait up there until it has passed. The floor behind you and the stairs go dark as you climb (the ledge is the only way on)
        Level(
            name = T("Stateful Inspection", "Zustandsprüfung"),
            intro = T("Please have your ID ready.", "Bitte Ausweis bereithalten."),
            start = listOf(
                Laser('K', 25 to 1, 25 to 14),
                Circuit('x'), Circuit('w', on = false),
                Pad('2', at = 2 to 14, circuits = "w"),
                Pad('1', at = 21 to 2),
            ),
            traps = listOf(
                trap(Pressed('2'), say("Second check started. The stairs are powered.", "Zweite Kontrolle gestartet. Die Treppe steht unter Strom.")),
                trap(Landed(6f, 7.9f), Play(Card.SPIKE_SEED), Laser('M', 8 to 1, 8 to 14, on = 1.3f, off = 1.4f, phase = 0.4f),
                    say("Gate 2 now runs on a rhythm.", "Tor 2 läuft jetzt im Takt.")),
                trap(Landed(6f, 7.9f), Circuit('x', on = false), say("Second check expired. Nobody is covering the floor behind you.", "Zweite Kontrolle abgelaufen. Den Boden hinter dir deckt niemand mehr.")),
                trap(Landed(12f, 13.9f), Laser('N', 14 to 1, 14 to 14, on = 1.3f, off = 1.4f, phase = 0.4f), say("Gate 2 has a twin. Same rhythm, other hole.", "Tor 2 hat einen Zwilling. Gleicher Takt, anderes Loch.")),
                trap(Landed(21.2f, 24.6f), Circuit('w', on = false), say("Stairs closed. The ledge is one-way.", "Treppe gesperrt. Der Sims ist eine Einbahnstraße.")),
                trap(Pressed('1'), Power('K', false), Laser('K', 25 to 1, 25 to 14, on = 0.5f, off = 40f, delay = 0.1f),
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
                        trap(Landed(21.9f, 24.5f), Laser('H', 25 to 4, 26 to 4, on = 0.6f, off = 40f, delay = 0.2f), say("Tripwire. I hid it in plain sight.", "Stolperdraht. Ich habe ihn offen versteckt.")),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('y', on = false), say("Lane floor: expired as well. Right to left.", "Boden unten: auch abgelaufen. Von rechts nach links."), delay = 0.7f),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('z', on = false), delay = 1.05f),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('q', on = false), delay = 1.4f),
                        trap(Zone(26.5f, 13f, 31f, 15.5f), Circuit('r', on = false), delay = 1.75f),
                    ),
                ) {
                    fill(9..10, 11..11, '.'); fill(12..13, 9..9, '.'); fill(15..16, 7..7, '.'); fill(18..19, 5..5, '.'); fill(21..24, 3..3, '.')
                    fill(10..11, 11..11, 'w'); fill(14..15, 9..9, 'k'); fill(18..19, 7..7, 'u'); fill(22..28, 5..5)
                    put(29, 14, '.'); put(15, 14, 'D'); put(26, 1, '.'); put(28, 1, '.')
                    fill(26..28, 15..17, 'y'); fill(23..25, 15..17, 'z'); fill(20..22, 15..17, 'q'); fill(17..19, 15..17, 'r')
                },
            ),
        ) {
            border(); floor()
            fill(2..14, 15..17, 'x')
            fill(6..7, 13..13); fill(9..10, 11..11, 'w'); fill(12..13, 9..9, 'w'); fill(15..16, 7..7, 'w'); fill(18..19, 5..5, 'w'); fill(21..24, 3..3)
            spawn(1); door(); bits(20)
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
                Portal('a', 5 to 14, 15 to 14),
                Portal('b', 9 to 14, 4 to 7, twoWay = false),
                Power('b', false),
                Portal('r', 26 to 14, 28 to 14, twoWay = false),
            ),
            traps = listOf(
                trap(Zone(14f, 13f, 17f, 15.5f), Power('b', true), say("Walled garden. Unlimited flat rate, limited to this cell. And port 80.", "Walled Garden. Flatrate unbegrenzt, begrenzt auf diese Zelle. Und Port 80.")),
                trap(Zone(3.5f, 5.5f, 6f, 8f), Belt('c', -6.5f), say("Terms accepted. Throttled to 56k, as agreed.", "AGB akzeptiert. Gedrosselt auf 56k, wie vereinbart.")),
                trap(Zone(10.5f, 5.5f, 12f, 8f), Reroute('b', 26 to 7), Belt('d', -3f), say("Portal b has moved on. The rest of the shelf is throttled, too.", "Portal b ist weitergezogen. Der Rest vom Regal ist auch gedrosselt.")),
                trap(Zone(24.4f, 13f, 25.8f, 15.5f), Play(Card.DECOY), Reroute('r', 4 to 7), say("301 Moved Permanently. The portal too.", "301 Moved Permanently. Das Portal auch.")),
            ),
            hint = T("The far portal has no link. Ask the near one why.", "Das ferne Portal hat keine Verbindung. Frag das nahe, warum."),
        ) {
            border(); floor()
            fill(12..13, 9..14); fill(17..18, 9..14)
            fill(1..19, 8..8); fill(2..9, 8..8, 'c'); fill(10..19, 8..8, 'd'); fill(22..30, 8..8)
            put(2, 7, '^'); ceilingSpikes(1..21, 6)
            spawn(1); door(); bits(21)
        },

        // 22 — the bouncer, a trap room (U7 the saw, U1 the floor goes): you start up on the top floor and run right; the first bouncer rolls
        // out of the far wall and walks toward you (hop him), and the carpet in front of the end drops out over a row of LEDs (hop that, too).
        // At the end you drop down to the lane, and the door is on the far left behind the second bouncer, who rolls out of the back door to
        // walk you out (hop him, after the LEDs under the hole), and the floor in front of the door drops
        Level(
            name = T("Bouncer", "Türsteher"),
            intro = T("The bouncer won't let you in. I like him.", "Der Türsteher lässt dich nicht rein. Ich mag ihn."),
            traps = listOf(
                trap(PastX(8.5f), Play(Card.DEVIL_SAW), Saw(33.5f, 6.4f, -4.5f, 0f), say("Bouncer: you're not on the list.", "Türsteher: Du stehst nicht auf der Liste.")),
                trap(Zone(12.5f, 3f, 14.5f, 7.4f), Hide('c'), say("Guest list updated. The carpet was crossed off.", "Gästeliste aktualisiert. Der Teppich wurde gestrichen.")),
                trap(Zone(26f, 12.5f, 31f, 15.5f), Saw(-1.5f, 14.4f, 4.5f, 0f), say("Bouncer: let me walk you out.", "Türsteher: Ich begleite dich raus.")),
                trap(Zone(15f, 13f, 16f, 15.5f), Fall('p'), say("The floor is for guests, too.", "Der Boden ist auch nur für Gäste.")),
            ),
            hint = T("The bouncer rolls toward you. Jump him.", "Der Türsteher rollt auf dich zu. Spring über ihn."),
        ) {
            border(); floor()
            fill(1..28, 7..7); fill(16..17, 7..7, 'c')
            leds(20..20)
            fill(4..5, 15..17, 'p')
            spawn(2, 6); put(2, 14, 'D'); bits(22)
        },

        // 23 — the information superhighway, a trap room (U12 the transport goes wrong, floor-move and drop): lifts. A piece of the on-ramp is
        // missing (it drops as you come near: hop it), and the on-ramp itself is a lift flush with the road; it carries whoever steps on it up,
        // past the deck (walk off to the right) and on into a ceiling of spikes. Along the deck a piece of the road is closed for repairs (hop it),
        // and the last lift, to the exit deck, is out of service as soon as it has arrived: it rises four tiles, and a moment later it drops away
        // under whoever stays on it (walk off to the left onto the exit deck, where the door is)
        Level(
            name = T("Information Superhighway", "Datenautobahn"),
            intro = T("Have a safe trip! Buckle up.", "Gute Fahrt! Bitte anschnallen."),
            traps = listOf(
                trap(PastX(2.6f), Play(Card.SINKING), Fall('g'), say("Merge lane closed. Merge anyway.", "Einfädelspur gesperrt. Bitte trotzdem einfädeln."), delay = 0.1f),
                trap(Touch('a'), Move('a', 0f, -13f, 3f), say("On-ramp open. Next exit: the ceiling.", "Auffahrt frei. Nächste Ausfahrt: die Decke.")),
                trap(PastX(15f), Move('b', -3f, 0f, 14f), say("Roadworks ahead. Nobody told the road.", "Baustelle voraus. Der Straße hat's keiner gesagt.")),
                trap(Touch('c'), Move('c', 0f, -4f, 12f), say("Express lift to the exit. Doors closing.", "Expresslift zur Ausfahrt. Türen schließen.")),
                trap(Touch('c'), Fall('c'), say("Out of service. Effective immediately.", "Außer Betrieb. Ab sofort."), delay = 0.8f),
            ),
            hint = T("Do not ride to the top. Get off where the deck is.", "Fahr nicht bis nach oben. Steig aus, wo das Deck ist."),
        ) {
            border(); floor()
            fill(7..8, 15..17, 'g')
            fill(10..11, 15..15, 'a'); ceilingSpikes(10..11)
            pit(12..27); fill(12..27, 17..17, '^')
            fill(12..12, 10..14); fill(12..27, 9..9); fill(19..21, 9..9, 'b'); fill(26..27, 9..9, 'c')
            fill(28..30, 1..14)
            fill(13..25, 5..5)
            spawn(1); door(18, 4); bits(23)
        },

        // 24 — the uplink, a trap room (U2 the ceiling comes down, with a saw from the side as the last surprise): the elevator is out of order and the
        // uplink is in the basement, so you start up on the top deck and run right: stalactites hang over the deck and the ones ahead drop as you
        // come near (wait for them to fall, then run). At the end you drop down to the lane and run back left under the underside of the deck, which
        // comes down on whoever steps under it (keep running); on the last stretch before the door a saw rolls out of the back wall as you land (hop it, before the door)
        Level(
            name = T("Uplink", "Uplink"),
            intro = T("The uplink is in the basement. So is the elevator.", "Der Uplink ist im Keller. Der Aufzug auch."),
            legend = mapOf('V' to Glyph(spike = true, dir = Dir.DOWN)),
            traps = listOf(
                trap(PastX(16.8f), Play(Card.HEADBUTT), Move('V', 0f, 16f, 25f), say("Stalactites. This is a cave now.", "Stalaktiten. Das hier ist jetzt eine Höhle."), delay = 0.3f),
                trap(Zone(19f, 12.5f, 26.9f, 15.5f), Move('r', 0f, 6f, 3.6f), say("Uplink full. Try the downlink.", "Uplink voll. Versuch's mit dem Downlink.")),
                trap(Landed(26f, 31f), Saw(-1.5f, 14.4f, 12f, 0f), say("Downlink traffic. It has teeth.", "Downlink-Verkehr. Er hat Zähne.")),
            ),
            hint = T("Wait for the stalactites to fall. Then run, and do not stop under the deck.", "Warte, bis die Stalaktiten gefallen sind. Dann lauf, und bleib nicht unter dem Deck stehen."),
        ) {
            border(); floor()
            fill(0..26, 6..6)
            fill(20..21, 1..2, 'V')
            fill(19..26, 7..8, 'r')
            spawn(1, 5); door(12, 14); bits(24)
        },

        // 25 — the load balancer, a trap room (U1 the floor goes): over a spike pit three nodes, a conveyor that runs against you and crumbles once
        // you are on it, a stone that crumbles, and a conveyor that runs against you, faster than you run (hop off it as you land). Then you
        // climb the rack on the right (the first step crumbles too) and run back over the pit on the top floor, where the stone drops under
        // whoever stands on it; the door is on the platform at the end.
        // Rematch: rebalanced. The nodes sit two tiles further on (round 1's hops land in the pit), crumble faster, the first one is longer and
        // runs slower against you, and up on the top floor the stone is a belt that runs the way you came
        Level(
            name = T("Load Balancer", "Lastverteiler"),
            intro = T("I distribute the load evenly. Onto you.", "Ich verteile die Last gleichmäßig. Auf dich."),
            start = listOf(Belt('a', -6f), Belt('c', -6f)),
            traps = listOf(
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Node 1 is overloaded.", "Knoten 1 ist überlastet."), delay = 1.1f),
                trap(Touch('b'), Fall('b'), say("Node 2 passes the load on. Fast.", "Knoten 2 reicht die Last weiter. Schnell."), delay = 0.8f),
                trap(Touch('p'), Fall('p'), say("The rack is a node, too.", "Das Rack ist auch ein Knoten."), delay = 0.9f),
                trap(Touch('j'), Fall('j'), say("Rebalanced again. The load stays where it is. So do you.", "Schon wieder neu verteilt. Die Last bleibt, wo sie ist. Du auch."), delay = 0.5f),
            ),
            hint = T("Nodes crumble when you stand on them. Do not cross them at a walk.", "Knoten bröckeln, sobald du draufstehst. Geh nicht im Schritt drüber."),
            rematch = listOf(
                Round(
                    T("Rebalanced. Same nodes, other places.", "Neu verteilt. Gleiche Knoten, andere Plätze."),
                    hint = T("The nodes moved two tiles on. The first one is longer. The stone up top is a belt now.", "Die Knoten sind zwei Felder weitergerückt. Der erste ist länger. Der Stein oben ist jetzt ein Band."),
                    start = listOf(Belt('a', -5f), Belt('c', 3f), Belt('j', -4f)),
                    traps = listOf(
                        trap(Touch('a'), Play(Card.COLLAPSE), Fall('a'), say("Node 1: overloaded, but longer.", "Knoten 1: überlastet, aber länger."), delay = 1.5f),
                        trap(Touch('b'), Fall('b'), delay = 0.7f),
                        trap(Touch('p'), Fall('p'), delay = 0.7f),
                        trap(Touch('j'), Fall('j'), delay = 0.8f),
                    ),
                ) { pit(5..22); fill(5..22, 17..17, '^'); fill(5..10, 15..15, 'a'); fill(13..16, 15..15, 'b'); fill(19..22, 15..15, 'c') },
            ),
        ) {
            border(); floor()
            pit(5..20); fill(5..20, 17..17, '^')
            fill(5..8, 15..15, 'a'); fill(11..14, 15..15, 'b'); fill(17..20, 15..15, 'c')
            fill(27..29, 13..13, 'p'); fill(30..30, 11..11); fill(22..27, 9..9)
            fill(15..18, 9..9, 'j'); fill(9..12, 9..9)
            spawn(1); door(10, 8); bits(25)
        },

        // 26 — the queue, a trap room (U8 the stalker): you start on the top deck at the right and run left; the deck piece ahead drops as you
        // come near (hop it) and at the left end you fall onto the middle deck, where you run right. As you land the queue starts: a block of
        // spikes at the left end that follows you along the deck (keep moving, it is slower than you). At the right end you fall onto the lane
        // and run left to the door, and a second queue starts at the right wall behind you and follows you; a low block on the lane to hop.
        // Rematch: the queues are waiting in front of you. The first one hangs at the far right of the middle deck (its end is open now) and
        // the second one stands on the lane next to the door; both come toward you, slowly, and have to be hopped (the one at the deck
        // as you come up to it, then you fall to the lane; round 1's run walks into it)
        Level(
            name = T("Ticket Number", "Wartenummer"),
            intro = T("Your number is 41. Now serving: 3. Please walk.", "Ihre Nummer ist 41. Aufgerufen wird: 3. Bitte gehen Sie."),
            traps = listOf(
                trap(BeforeX(26f), Fall('f'), say("Number 40 was called. The floor was number 39.", "Nummer 40 wurde aufgerufen. Der Boden war Nummer 39."), delay = 0.15f),
                trap(Zone(0f, 8f, 24f, 10.6f), Play(Card.STALKER), Chase('S', speed = 5f, left = 0f, right = 24f), say("The queue moves up. Onto you.", "Die Schlange rückt auf. Auf dich.")),
                trap(Zone(23f, 12f, 31f, 15.6f), Chase('Q', speed = 5.5f, left = 28f, right = 0f), say("A second queue. Same service.", "Eine zweite Schlange. Gleicher Service.")),
            ),
            hint = T("The queue is slower than you. Do not stand in line.", "Die Schlange ist langsamer als du. Stell dich nicht an."),
            rematch = listOf(
                Round(
                    T("Number 42. Please come to counter 2. The queue is already there.", "Nummer 42. Bitte zu Schalter 2. Die Schlange ist schon da."),
                    hint = T("The queues stand in front of you now and come toward you. Hop them.", "Die Schlangen stehen jetzt vor dir und kommen dir entgegen. Spring drüber."),
                    traps = listOf(
                        trap(Zone(0f, 8f, 24f, 10.6f), Play(Card.STALKER), Chase('S', speed = 2.5f, left = 19f, right = 0f), say("The queue moves up. Toward you.", "Die Schlange rückt auf. Auf dich zu.")),
                        trap(Zone(22f, 9.9f, 31f, 15.6f), Chase('Q', speed = 3.5f, left = 0f, right = 17f), say("The second queue was already waiting.", "Die zweite Schlange wartete schon.")),
                    ),
                ) {
                    fill(0..1, 9..9, '.'); fill(28..30, 10..10, '.'); put(29, 9, 'S')
                    put(30, 14, '.'); put(14, 14, '.'); put(11, 14, 'Q'); door(7, 14)
                },
            ),
        ) {
            border(); floor()
            fill(14..30, 5..5); fill(20..22, 5..5, 'f')
            fill(0..24, 10..10); fill(28..30, 10..10)
            fill(0..1, 9..9, 'S'); put(30, 14, 'Q'); put(14, 14, '#')
            spawn(29, 4); door(4, 14); bits(26)
        },

        // 27 — DDoS, a trap room (U2 the ceiling falls, on a belt that runs against you): the stairs to the exit are built from packets that
        // drop from the ceiling: one, two and three tiles high. The first falls on the spot you reach if you keep running; the next one
        // comes down when you step onto the one before. The belt carries you back while you wait, and the walkway on top is a belt, too.
        Level(
            name = T("DDoS", "DDoS"),
            intro = T("Light traffic today. Just you and the stairs.", "Heute wenig Verkehr. Nur du und die Treppe."),
            start = listOf(Belt('b', -3f), Belt('l', -4f)),
            traps = listOf(
                trap(PastX(5.9f), Play(Card.HEADBUTT), Fall('c'), say("10,000 packets per second.", "10.000 Pakete pro Sekunde."), delay = 0.1f),
                trap(Touch('c'), Fall('d'), say("Request 10,001.", "Anfrage 10.001.")),
                trap(Touch('d'), Fall('e'), say("Request 10,002. The stairs are a rumour.", "Anfrage 10.002. Die Treppe ist ein Gerücht.")),
                trap(PastX(19.5f), Fall('f'), say("And one more for the road.", "Und noch eins für unterwegs."), delay = 0.4f),
            ),
        ) {
            border(); floor()
            fill(3..24, 15..15, 'b')
            fill(8..10, 1..1, 'c'); fill(11..13, 1..2, 'd'); fill(14..16, 1..3, 'e'); put(23, 1, 'f')
            fill(17..30, 12..12, 'l')
            spawn(1, 14); door(29, 11); bits(27)
        },

        // 28 — a VPN tunnel under the firewall, a trap room (U13 the laser, R3 the portal): the firewall is a wall of light you cannot cross, the
        // tunnel is the only way past it. Walking into the tunnel re-points it (DNS changed): you come out right in front of the IPS, which starts a
        // gate as you appear (run on and you are in it), and a second one as you pass the first. At the end of the lane a second tunnel leads up to the
        // walkway under the ceiling, where you run back to the door, and a third gate starts as you come out.
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
            ),
        ) {
            border(); floor()
            fill(2..30, 9..9)
            spawn(); door(8, 8); bits(28)
        },

        // 29 — the race condition, a trap room (U7 the saw): three pendulum saws hang over the lane, each one set off when you come within six tiles of it.
        // It stays down on the floor for a moment and then swings up and down: running on runs into it, the clever way is to stop in front of it and
        // slip under it when it is up.
        // Rematch: a thread hunts you from behind, so there is no time to wait it out. The pendulums start swinging at once; the way through is to
        // run on and stop only for the one that is down when you get there
        Level(
            name = T("Race Condition", "Wettlaufsituation"),
            intro = T("The winner gets a cookie. I have no cookies.", "Der Sieger bekommt einen Keks. Ich habe keine Kekse."),
            start = listOf(Belt('b', -5f)),
            traps = listOf(
                trap(PastX(4f), Play(Card.DEVIL_SAW), PathSaw(3f, 10f to 14f, 10f to 11.2f, delay = 0.6f, r = 1f), say("Thread 1 holds the lock. Thread 2 wants it.", "Thread 1 hält das Lock. Thread 2 will es.")),
                trap(PastX(10f), PathSaw(3f, 16f to 14f, 16f to 11.2f, delay = 0.6f, r = 1f), say("Thread 2 holds it now.", "Jetzt hält es Thread 2.")),
                trap(PastX(19f), PathSaw(3f, 22f to 14f, 22f to 11.2f, delay = 0.6f, r = 1f), say("Thread 3. Nobody told me about thread 3.", "Thread 3. Von Thread 3 hat mir keiner was gesagt.")),
            ),
            hint = T("Do not run into the saw. Wait until it is up.", "Lauf nicht in die Säge. Warte, bis sie oben ist."),
            rematch = listOf(
                Round(
                    T("Rerun. Same threads, new scheduler.", "Nochmal. Gleiche Threads, neuer Scheduler."),
                    hint = T("The second thread laps the lane now. Run when it is up in the corner.", "Der zweite Thread dreht jetzt Runden über die Bahn. Lauf, wenn er oben in der Ecke ist."),
                    traps = listOf(
                        trap(PastX(4f), Play(Card.DEVIL_SAW), PathSaw(3f, 10f to 14f, 10f to 11.2f, delay = 0.4f, r = 1f), say("Thread 1 is the same as before. I am proud of it.", "Thread 1 ist wie vorher. Ich bin stolz auf ihn.")),
                        trap(PastX(11.5f), PathSaw(10f, 13f to 14f, 22f to 14f, 22f to 6f, 13f to 6f, loop = true, r = 1f), say("Thread 2 has joined the race. It laps.", "Thread 2 ist ins Rennen eingestiegen. Er überrundet.")),
                        trap(PastX(19.5f), PathSaw(3f, 25f to 14f, 25f to 11.2f, delay = 0.6f, r = 1f), say("Thread 3 was always going to be here.", "Thread 3 war immer schon hier.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(26..29, 15..15, 'b')
            spawn(); door(30); bits(29)
        },

        // 30 — hop limit, a puzzle room of two rooms (R3 the portal, R5 the floors, U11 the route is manipulated, U18 the room goes on): the tunnel on the
        // lane leads up to the ledge, and the ledge piece in front of the door drops out over a row of LEDs as you come near (TTL expired: hop the
        // hole). At the door the wall breaks open and the door slips into the second room. There you run along the ledge and drop onto a lane
        // whose pit is crossed by a portal, which is re-pointed as you come near (the exit moves) and out of which you step into spikes that grow
        Level(
            name = T("Hop Limit", "Hop-Limit"),
            intro = T("TTL: 64. No need to rush.", "TTL: 64. Kein Grund zur Eile."),
            legend = mapOf('A' to hidden),
            rooms = 2,
            start = listOf(
                Portal('a', 9 to 14, 10 to 8, twoWay = false),
                Portal('b', roomX(1, 15f).toInt() to 14, roomX(1, 19f).toInt() to 14, twoWay = false),
            ),
            traps = listOf(
                trap(PastX(16f), Fall('f'), say("TTL exceeded in transit. The floor too.", "TTL in der Übertragung überschritten. Der Boden auch."), delay = 0.3f),
                trap(Trigger.AtDoor, Play(Card.ANNEX), Action.Extend(into = 1, top = 6, bottom = 8, door = roomX(1, 30f).toInt() to 14)),
                trap(Zone(roomX(1, 9f), 6f, roomX(1, 11.5f), 9f), Reroute('b', roomX(1, 26f).toInt() to 14), say("Hop 2 of 3: the exit has moved on.", "Hop 2 von 3: Der Ausgang ist weitergezogen.")),
                trap(Landed(roomX(1, 25.5f), roomX(1, 27.5f)), Show('A'), say("Hop 3 of 3: spikes.", "Hop 3 von 3: Spikes.")),
            ),
        ) {
            border(); floor()
            room(0) {
                put(1, 14, 'P')
                fill(10..30, 9..9); fill(19..21, 9..9, 'f')
                pit(17..23)
                put(28, 8, 'D')
            }
            room(1) {
                fill(0..11, 9..9)
                pit(17..23); fill(17..23, 17..17, '^')
                put(28, 14, 'A'); put(29, 14, 'A')
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
                trap(Zone(26f, 12f, 31f, 15.5f), Move('X', -25f, 0f, 3.4f), say("The second class has to go home, too.", "Die zweite Klasse muss auch nach Hause.")),
                trap(Zone(15f, 12f, 17f, 15.5f), Move('X', -10f, 0f, 5f), say("Oh, and they are in a hurry.", "Ach so, und sie haben es eilig.")),
            ),
            hint = T("The wall is slower than you. The stones are not.", "Die Wand ist langsamer als du. Die Steine nicht."),
        ) {
            border(); floor()
            fill(1..2, 1..8, 'W'); fill(30..30, 10..14, 'X')
            fill(1..26, 9..9)
            fill(10..25, 9..9, '^')
            fill(11..13, 8..8, 's'); fill(16..18, 8..8, 't'); fill(21..23, 8..8, 'u')
            spawn(4, 8); door(3, 14); bits(31)
        },

        // 32 — the act finale, a puzzle room (R4 the switch, R3 the portals, R8 the timing; U12 the transport goes wrong, with U13 given up because the one
        // laser level is 28): the lane is split by two firewalls and the door is behind the second one. The first firewall is crossed by a tunnel, and
        // the belt behind it turns around as you step on it (hop it, or you are carried into the spikes). The tunnel up to the deck is the only
        // way on: up there you walk back left against a belt that turns up as you come near, to the switch at the far end, which powers the tunnel
        // under the second firewall. Then back down to the lane, the same way again, and through the second tunnel; the spikes in front of the door
        // grow as you come out
        Level(
            name = T("Core Switch", "Core-Switch"),
            intro = T("Finally, the data-center basement. This is where I live.", "Endlich der Rechenzentrumskeller. Hier wohne ich."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Belt('a', -6.5f), Belt('b', 3f), Belt('c', 5f),
                Portal('1', 13 to 14, 17 to 14, twoWay = false),
                Portal('2', 21 to 14, 26 to 14, twoWay = false), Power('2', false),
                Portal('3', 23 to 14, 28 to 8, twoWay = false), Power('3', false),
                Pad('1', at = 4 to 8),
            ),
            traps = listOf(
                trap(Zone(4f, 12f, 6f, 15.5f), Power('3', true), say("Tunnel 3 is enabled for you. For the moment.", "Tunnel 3 ist für dich freigeschaltet. Vorerst.")),
                trap(PastX(18.5f), Play(Card.GRAND_FINALE), Belt('b', -9f), say("Spanning tree recalculated. Your belt now runs the other way.", "Spanning Tree neu berechnet. Dein Band läuft jetzt andersrum.")),
                trap(Zone(15f, 6f, 18f, 9f), Belt('c', 10f), say("Priority traffic. You are not priority.", "Priorisierter Verkehr. Du nicht.")),
                trap(Pressed('1'), Power('2', true), Belt('b', 3f), say("Switch thrown. Tunnel 2 is up and the belt is back. Probably.", "Schalter umgelegt. Tunnel 2 ist oben und das Band wieder da. Wahrscheinlich.")),
                trap(Zone(26.5f, 6f, 31f, 9.3f), Show('A'), say("Broadcast storm.", "Broadcast-Sturm.")),
                trap(Landed(0f, 2.9f), Reroute('1', 19 to 14), say("DNS changed. Tunnel 1 comes out further on now.", "DNS geändert. Tunnel 1 kommt jetzt weiter hinten raus.")),
            ),
        ) {
            border(); floor()
            fill(3..30, 9..9)
            fill(14..15, 10..14); fill(24..25, 10..14)
            fill(3..12, 15..15, 'a'); fill(17..20, 15..15, 'b'); fill(8..21, 9..9, 'c')
            put(8, 14, '^'); put(16, 14, '^')
            put(23, 8, 'A'); put(24, 8, 'A')
            spawn(1); door(29)
        },
    )
}
