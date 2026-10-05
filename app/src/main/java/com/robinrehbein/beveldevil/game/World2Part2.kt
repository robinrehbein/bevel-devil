package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Belt
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
            // rematch: a three-way handshake. Same room, same door, same rack, same pad, but one press is only a SYN: the floor stays gone and
            // the beam over the stairs stays on until the pad has been pressed a second time (step off, step on). Whoever does what round 1 taught,
            // press once and run for the stairs, falls into the hole that never closed
            rematch = listOf(
                Round(
                    T("Three-way handshake now. I insist.", "Jetzt mit Handshake. Ich bestehe darauf."),
                    hint = T("One press is only a SYN. The pad wants an ACK.", "Ein Druck ist nur ein SYN. Der Schalter will ein ACK."),
                    start = listOf(
                        Laser('W', 1 to 10, 10 to 10),
                        Pad('1', at = 26 to 12),
                    ),
                    traps = listOf(
                        trap(PastX(10.5f), Play(Card.COLLAPSE), Move('a', 0f, 12f, 30f), say("Packet loss, second edition.", "Paketverlust, zweite Auflage.")),
                        trap(PastX(20.6f), Laser('G', 23 to 1, 23 to 14, on = 0.4f, off = 40f, delay = 0.1f),
                            say("Rule 2: still no entry. Still counting to one.", "Regel 2: immer noch kein Zutritt. Ich zähle immer noch bis eins.")),
                        trap(Pressed('1'), say("SYN received. Floor and beam are waiting for the ACK.", "SYN erhalten. Boden und Strahl warten auf das ACK.")),
                        trap(Pressed('1', 2), Power('W', false), Move('a', 0f, -12f, 25f), Laser('K', 25 to 12, 29 to 12, on = 1.0f, off = 40f, delay = 0.8f),
                            say("ACK. Rule 1 disabled. The port scan wants the rack.", "ACK. Regel 1 deaktiviert. Der Portscan will das Rack.")),
                        trap(Zone(7.3f, 9.9f, 11.6f, 11f), Laser('W', 1 to 10, 10 to 10, delay = 1.1f), say("Rule 1 is back. It did not like being off.", "Regel 1 ist zurück. Ausgeschaltet sein hat ihr nicht gefallen.")),
                    ),
                ) { fill(25..29, 13..14) },
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
            // rematch: gate 3 is low and stays shut, the scanner on the ledge is a bluff, and the ledge goes on: over a gap and across a step
            // that loses its power when you step on it, above a floor of LEDs. The stairs are the same, so round 1's run gets as far as the
            // ledge: whoever waits up there for gate 3 as in round 1, or drops where round 1 dropped, lands in the LEDs
            rematch = listOf(
                Round(
                    T("Stateless now. I forgot everything. Almost.", "Zustandslos jetzt. Ich merk mir nur noch dich."),
                    hint = T("The scanner is a bluff. The ledge goes on.", "Der Scanner ist ein Bluff. Der Sims geht weiter."),
                    start = listOf(
                        Laser('K', 25 to 10, 25 to 14),
                        Circuit('x'), Circuit('w', on = false),
                        Pad('2', at = 2 to 14, circuits = "w"),
                        Pad('1', at = 21 to 2),
                        Circuit('y'),
                    ),
                    traps = listOf(
                        trap(Pressed('2'), say("Second check started. Same stairs.", "Zweite Kontrolle gestartet. Gleiche Treppe.")),
                        trap(Landed(6f, 7.9f), Laser('M', 8 to 1, 8 to 14, on = 1.8f, off = 1.4f, phase = 0.5f), say("Gate 2 on a rhythm. Again.", "Tor 2 im Takt. Schon wieder.")),
                        trap(Landed(6f, 7.9f), Circuit('x', on = false), say("Second check expired. Same floor, same nobody.", "Zweite Kontrolle abgelaufen. Gleicher Boden, gleiches Niemand.")),
                        trap(Landed(12f, 13.9f), Laser('N', 14 to 1, 14 to 14, on = 1.8f, off = 1.4f, phase = 0.5f), say("The twin again. It never learns.", "Der Zwilling schon wieder. Er lernt nie.")),
                        trap(Pressed('1'), Bluff(Card.SPIKE_SEED), say("ID scanned. Gate 3 open. (Is it?)", "Ausweis gescannt. Tor 3 offen. (Echt jetzt?)")),
                        trap(Landed(15f, 16.9f), Circuit('y', on = false), say("Lane floor: expired as well. Nobody renewed it.", "Boden unten: auch abgelaufen. Niemand hat verlängert.")),
                        trap(Touch('f'), Circuit('f', on = false), say("The ledge expires with your ID.", "Der Sims läuft mit deinem Ausweis ab."), delay = 0.55f),
                    ),
                ) { fill(21..24, 3..3, 'f'); fill(27..28, 3..3); fill(15..20, 15..17, 'y'); fill(21..26, 14..14, '^'); put(29, 14, '.'); put(27, 14, 'D') },
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
                trap(Touch('a'), Move('a', 0f, -13f, 3.5f), say("On-ramp open. Next exit: the ceiling.", "Auffahrt frei. Nächste Ausfahrt: die Decke.")),
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
            spawn(1); door(14, 4); bits(23)
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
                trap(PastX(16.8f), Move('V', 0f, 16f, 25f), say("Stalactites. This is a cave now.", "Stalaktiten. Das hier ist jetzt eine Höhle."), delay = 0.3f),
                trap(Zone(19f, 12.5f, 26.9f, 15.5f), Move('r', 0f, 6f, 2.8f), say("Uplink full. Try the downlink.", "Uplink voll. Versuch's mit dem Downlink.")),
                trap(Landed(26f, 31f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 8f, 0f), say("Downlink traffic. It has teeth.", "Downlink-Verkehr. Er hat Zähne.")),
            ),
            hint = T("Wait for the stalactites to fall. Then run, and do not stop under the deck.", "Warte, bis die Stalaktiten gefallen sind. Dann lauf, und bleib nicht unter dem Deck stehen."),
        ) {
            border(); floor()
            fill(0..26, 6..6)
            fill(20..21, 1..2, 'V')
            fill(19..26, 7..8, 'r')
            spawn(1, 5); door(9, 14); bits(24)
        },

        // 25 — four belts over a spike pit, each going the other way and crumbling when you step on it
        Level(
            name = T("Load Balancer", "Lastverteiler"),
            intro = T("I distribute the load evenly. Onto you.", "Ich verteile die Last gleichmäßig. Auf dich."),
            start = listOf(Belt('a', 4f), Belt('b', -4f), Belt('c', 4f), Belt('d', -4f)),
            traps = listOf(
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Node 1 is overloaded.", "Knoten 1 ist überlastet."), delay = 0.9f),
                trap(Touch('b'), Fall('b'), delay = 0.9f),
                trap(Touch('c'), Fall('c'), delay = 0.9f),
                trap(Touch('d'), Fall('d'), delay = 0.9f),
            ),
            // rematch: rebalanced, every belt runs the other way, so round 1's timing throws you off
            rematch = listOf(
                Round(
                    T("Rebalanced. Same nodes, other way round.", "Neu verteilt. Gleiche Knoten, andersrum."),
                    start = listOf(Belt('a', -4f), Belt('b', 4f), Belt('c', -4f), Belt('d', 4f)),
                    traps = listOf(
                        trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Node 1: overloaded backwards.", "Knoten 1: rückwärts überlastet."), delay = 0.9f),
                        trap(Touch('b'), Fall('b'), delay = 0.9f),
                        trap(Touch('c'), Fall('c'), delay = 0.9f),
                        trap(Touch('d'), Fall('d'), delay = 0.9f),
                    ),
                ),
            ),
        ) {
            border()
            fill(0..4, 15..17); fill(28..31, 15..17)
            fill(5..9, 15..15, 'a'); fill(11..15, 15..15, 'b'); fill(17..21, 15..15, 'c'); fill(23..27, 15..15, 'd')
            fill(5..27, 17..17, '^')
            spawn(); door(); bits(25)
        },

        // 26 — the gate stays shut for seconds, but idle connections are dropped: keep hopping while you wait. Behind the gate the floor drops, and the hop over it lands in spikes
        Level(
            name = T("Ticket Number", "Wartenummer"),
            intro = T("The gate opens shortly. Please linger.", "Das Tor öffnet gleich. Bitte kurz verweilen."),
            legend = mapOf('A' to hidden),
            start = listOf(Laser('L', 15 to 1, 15 to 14, on = 3.5f, off = 1.6f)),
            traps = listOf(
                trap(Idle(1.5f), Play(Card.CRUMBLE), Fall('a'), say("Connection closed: idle timeout.", "Verbindung beendet: Leerlauf-Timeout.")),
                trap(PastX(15.4f), Fall('b'), say("Session expired.", "Sitzung abgelaufen.")),
                trap(Airborne(18.2f, 21f), Show('A'), say("Re-login required.", "Erneute Anmeldung nötig.")),
            ),
            // rematch: idling is fine now, but the queue moves up: a spike pops up behind you and creeps after you, right up to
            // the gate. Hopping on the spot (round 1's keep-alive) lands on it; jump over it and back until the gate opens
            rematch = listOf(
                Round(
                    T("Your number was called. Take a new one.", "Nummer 2, bitte. Ach, du schon wieder."),
                    legend = mapOf('S' to hidden),
                    traps = listOf(
                        trap(PastX(10.5f), Play(Card.STALKER), Show('S'), Chase('S', speed = 3f, left = 4f, right = 4.5f),
                            say("The queue moves up. Onto you.", "Die Schlange rückt auf. Auf dich.")),
                        trap(PastX(15.4f), Fall('b')),
                        trap(Airborne(18.2f, 21f), Show('A'), say("Please wait quietly.", "Bitte leise warten.")),
                    ),
                ) { put(9, 14, 'S') },
            ),
        ) {
            border(); floor()
            fill(9..13, 15..17, 'a'); fill(18..20, 15..17, 'b')
            put(23, 14, 'A'); put(24, 14, 'A')
            spawn(); door(); bits(26)
        },

        // 27 — EASTER EGG: DDoS (Distributed Denial of Stairs, on a belt that runs against you); after the stairs the belt speeds up against you
        Level(
            name = T("DDoS", "DDoS"),
            intro = T("Light traffic today. Just you and the stairs.", "Heute wenig Verkehr. Nur du und die Treppe."),
            start = listOf(Belt('b', -3f)),
            traps = listOf(
                trap(PastX(4f), Play(Card.HEADBUTT), Fall('c'), say("10,000 blocks per second.", "10.000 Blöcke pro Sekunde.")),
            ) + "defgh".mapIndexed { i, g -> trap(PastX(4f), Fall(g), delay = 0.35f * (i + 1)) } + listOf(
                trap(Landed(14f, 22f), Belt('b', -7f), say("Rate limit: the belt says no.", "Ratenbegrenzung: Das Band sagt nein.")),
            ),
        ) {
            border(); floor()
            fill(5..26, 15..15, 'b')
            fill(8..9, 3..4, 'c'); fill(12..13, 3..4, 'd'); fill(15..16, 3..4, 'e')
            fill(19..20, 3..4, 'f'); fill(22..23, 3..4, 'g'); fill(26..27, 3..4, 'h')
            spawn(); door(); bits(27, x0 = 24, y = 1)
        },

        // 28 — a VPN tunnel goes under the firewall; walking into it re-points it to the edge of the IPS beam, which warms up on the far side; past the beam, two spikes
        Level(
            name = T("Split Tunnel", "Split Tunnel"),
            intro = T("The tunnel is the only shortcut. I swear.", "Der Tunnel ist die einzige Abkürzung. Ich schwöre."),
            legend = mapOf('A' to hidden),
            start = listOf(Laser('L', 15 to 1, 15 to 14), Portal('1', 11 to 14, 19 to 14)),
            traps = listOf(
                trap(Zone(9.6f, 12.5f, 10.8f, 15f), Reroute('1', 22 to 14), say("Split tunnel: your exit has moved.", "Split Tunnel: Dein Ausgang ist umgezogen.")),
                trap(PastX(19.2f), Play(Card.SPIKE_SEED), Laser('M', 23 to 14, 26 to 14, on = 0.8f, off = 1.8f, delay = 0.35f), say("IPS: tunnel detected. New rule installed.", "IPS: Tunnel erkannt. Neue Regel installiert.")),
                trap(PastX(24.6f), Show('A'), say("Intrusion logged.", "Eindringen protokolliert.")),
            ),
        ) {
            border(); floor()
            put(28, 14, 'A'); put(29, 14, 'A')
            spawn(); door(30); bits(28)
        },

        // 29 — a pendulum saw over a belt that hurries you, and another saw that is right behind you; after the first hop the belt turns against you, and the last hop lands in spikes
        Level(
            name = T("Race Condition", "Wettlaufsituation"),
            intro = T("The winner gets a cookie. I have no cookies.", "Der Sieger bekommt einen Keks. Ich habe keine Kekse."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Belt('b', 5f),
                PathSaw(5f, 16f to 14.4f, 16f to 11.6f),
                PathSaw(5f, 21f to 14.4f, 21f to 11.6f, delay = 0.56f),
            ),
            traps = listOf(
                trap(PastX(6f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6f, 0f), say("Thread 3 joins the race.", "Thread 3 steigt ins Rennen ein.")),
                trap(Landed(12f, 15f), Belt('b', -4f), say("Mutex acquired: by the belt.", "Mutex gesperrt: vom Band.")),
                trap(Airborne(23f, 25.6f), Show('A'), say("Deadlock.", "Deadlock.")),
            ),
            // rematch: no saw behind you; the belt still turns against you after the first hop, but landing behind the first
            // pendulum turns it forward again: whoever keeps holding right like in round 1 is rushed into the last spike.
            // Let it carry you, slip under the second pendulum and jump the spike (the floor behind it stays clean)
            rematch = listOf(
                Round(
                    T("Rerun. Same threads, new scheduler.", "Nochmal. Gleiche Threads, neuer Scheduler."),
                    traps = listOf(
                        trap(Landed(12f, 15f), Belt('b', -4f)),
                        trap(Landed(19f, 24.5f), Play(Card.TWISTED), Belt('b', 3f), say("Priority inversion. The belt goes first.", "Prioritätsumkehr. Das Band hat Vorfahrt.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(6..27, 15..15, 'b')
            put(11, 14, '^'); put(25, 14, '^')
            put(28, 14, 'A'); put(29, 14, 'A')
            spawn(); door(30)
        },

        // 30 — three one-way portals, each one lands higher up in the air; a spike grows in front of the first, and behind each landing
        Level(
            name = T("Hop Limit", "Hop-Limit"),
            intro = T("TTL: 64. No need to rush.", "TTL: 64. Kein Grund zur Eile."),
            legend = mapOf('A' to hidden, 'B' to hidden, 'Z' to hidden),
            start = listOf(
                Portal('1', 7 to 14, 12 to 8, twoWay = false),
                Portal('2', 18 to 9, 23 to 4, twoWay = false),
                Portal('3', 29 to 5, 25 to 12, twoWay = false),
            ),
            traps = listOf(
                trap(Trigger.PastX(3f), Play(Card.SPIKE_SEED), Show('Z'), say("TTL exceeded in transit.", "TTL in der Übertragung überschritten.")),
                trap(Landed(13.5f, 17f), Show('A'), say("Hop 1 of 3: spikes.", "Hop 1 von 3: Spikes.")),
                trap(Landed(24f, 27.5f), Show('B'), say("Hop 2 of 3: more spikes.", "Hop 2 von 3: mehr Spikes.")),
            ),
        ) {
            border(); floor()
            fill(19..20, 1..14)
            fill(11..18, 10..10); fill(22..29, 6..6)
            put(16, 9, '^'); put(27, 5, '^')
            put(6, 14, 'Z'); put(17, 9, 'A'); put(28, 5, 'B')
            spawn(); door()
        },

        // 31 — EASTER EGG: HTTP 408 Request Timeout (a treadmill against you and a gate that closes for good at 5.2 s); spikes grow under the hops, the belt speeds up
        Level(
            name = T("Detention", "Nachsitzen"),
            intro = T("No rush. I've got all day.", "Keine Hektik. Ich habe den ganzen Tag Zeit."),
            legend = mapOf('A' to hidden, 'C' to hidden),
            start = listOf(Belt('b', -5f), Laser('L', 27 to 1, 27 to 14, delay = 5.2f)),
            traps = listOf(
                trap(After(0.2f), Play(Card.SINKING), say("408: the server waited for you. Not anymore.", "408: Der Server hat auf dich gewartet. Nicht mehr.")),
                trap(Airborne(9f, 12f), Show('A'), say("Retry-After: never.", "Retry-After: nie.")),
                trap(Landed(16f, 22f), Belt('b', -9f), say("Keep-alive rejected.", "Keep-Alive abgelehnt.")),
                // the gate closes for good at 5.2 s: whoever still stands in front of it loses the connection
                trap(After(5.6f), Fall('b'), say("Timeout: connection closed.", "Zeitüberschreitung: Verbindung getrennt.")),
            ),
        ) {
            border(); floor()
            fill(3..26, 15..17, 'b')
            put(15, 14, 'A'); put(16, 14, 'A')
            put(20, 14, 'C'); put(21, 14, 'C')
            spawn(); door()
        },

        // 32 — act finale: belt, tunnel, timed gate and a belt that turns around in front of it; spikes grow behind the first hop
        Level(
            name = T("Core Switch", "Core-Switch"),
            intro = T("Finally, the data-center basement. This is where I live.", "Endlich der Rechenzentrumskeller. Hier wohne ich."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Belt('a', 4f), Belt('b', 3f),
                Portal('1', 13 to 14, 17 to 14, twoWay = false),
                Portal('2', 24 to 14, 28 to 14, twoWay = false),
                Laser('G', 21 to 1, 21 to 14, on = 0.9f, off = 1.8f, phase = 1.0f),
            ),
            traps = listOf(
                trap(Airborne(6f, 9.4f), Show('A'), say("Broadcast storm.", "Broadcast-Sturm.")),
                trap(PastX(18.5f), Play(Card.GRAND_FINALE), Belt('b', -9f), say("Spanning tree recalculated. Your belt now runs the other way.", "Spanning Tree neu berechnet. Dein Band läuft jetzt andersrum.")),
            ),
        ) {
            border(); floor()
            fill(3..12, 15..15, 'a'); fill(17..24, 15..15, 'b')
            fill(14..15, 1..14); fill(25..26, 1..14)
            put(8, 14, '^'); put(16, 14, '^')
            put(10, 14, 'A'); put(11, 14, 'A')
            spawn(); door()
        },
    )
}
