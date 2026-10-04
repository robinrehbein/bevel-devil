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
        // and the shelf's landing piece crumbles behind you. The pad at the far end of the shelf turns the bus around: drop
        // down and ride it to the door. On the way the floor in front of the door drops out: jump it
        Level(
            name = T("Data Bus", "Datenbus"),
            intro = T("Timetable: every ten seconds. Roughly.", "Fahrplan: alle zehn Sekunden. Ungefähr."),
            start = listOf(Belt('b', -9f), Pad('1', at = 4 to 7)),
            traps = listOf(
                trap(Zone(9.6f, 9f, 15f, 12.2f), Play(Card.HEADBUTT), Fall('c'), say("Packet from the upper bus. Mind your head.", "Paket vom oberen Bus. Kopf einziehen.")),
                trap(Zone(14f, 6.5f, 19f, 8.4f), Fall('d'), say("Shelf 2 is decoration. Do not stand on it.", "Regal 2 ist Deko. Nicht draufstellen."), delay = 0.55f),
                trap(Pressed('1'), Belt('b', 5f), say("Bus 1 reversed. Next stop: the door.", "Bus 1 fährt jetzt andersrum. Nächster Halt: die Tür.")),
                trap(Zone(10f, 13.5f, 21f, 15f), Fall('e'), say("Last stop. The station is closed. Please jump.", "Endstation. Der Bahnhof ist gesperrt. Bitte springen.")),
            ),
            hint = T("The bus turns around somewhere. Not down here.", "Irgendwo dreht der Bus um. Nicht hier unten."),
        ) {
            border(); floor()
            fill(4..5, 13..14)
            fill(7..20, 12..12); fill(8..14, 13..13, 'v'); fill(7..20, 15..15, 'b')
            fill(21..21, 1..12); fill(19..20, 10..11)
            fill(25..26, 15..17, 'e')
            fill(3..18, 8..8); fill(11..13, 8..8, 'c'); fill(14..18, 8..8, 'd')
            spawn(); door(); bits(17)
        },

        // 18 — the first laser, as a puzzle room (R5 floors and the way back, U13 firewall). The door hangs high on the left, and
        // the stairs to it are cut by a firewall beam; the pad that drops the beam lies on the rack on the far right. On the way
        // out the filter lights beams on the floor (a low one to hop, a gate to wait for); pressing the pad starts a port scan
        // (a gate on the way back), and the beam over the stairs comes back as you land on the first step: keep climbing
        Level(
            name = T("Firewall", "Firewall"),
            intro = T("I configured the firewall myself. Nice pattern, right?", "Die Firewall habe ich selbst eingestellt. Schönes Muster, oder?"),
            start = listOf(
                Laser('W', 1 to 10, 10 to 10),
                Pad('1', at = 29 to 12),
            ),
            traps = listOf(
                trap(PastX(8f), Play(Card.SPIKE_SEED), Laser('F', 18 to 14, 19 to 14, on = 1.2f, off = 40f, delay = 0.35f),
                    say("Packet filter: low packets only.", "Paketfilter: nur flache Pakete.")),
                trap(Landed(20f, 24.4f), Laser('G', 25 to 1, 25 to 14, on = 0.8f, off = 40f, delay = 0.3f),
                    say("Rule 2: no entry. I count to one.", "Regel 2: Zutritt verboten. Ich zähle bis eins.")),
                trap(Pressed('1'), Power('W', false), Laser('H', 20 to 14, 21 to 14, on = 1.5f, off = 40f, delay = 0.9f),
                    Laser('K', 28 to 12, 31 to 12, on = 1.0f, off = 40f, delay = 0.8f),
                    say("Rule 1 disabled. Port scan on your way back.", "Regel 1 deaktiviert. Portscan auf dem Rückweg.")),
                trap(Zone(7.3f, 9.9f, 11.6f, 11f), Laser('W', 1 to 10, 10 to 10, delay = 1.1f), say("Rule 1 restarted. Climb faster.", "Regel 1 neu gestartet. Kletter schneller.")),
            ),
            hint = T("The pad is on the rack. And don't dawdle on the stairs.", "Der Schalter liegt auf dem Rack. Und trödel nicht auf der Treppe."),
            // rematch: the door is down on the right behind a beam of its own, the pad is up where the door was; coming down, a saw inspects the floor
            rematch = listOf(
                Round(
                    T("Rules reloaded. Your move.", "Firewall-Regel aktualisiert. Rate mal, welche."),
                    start = listOf(
                        Laser('V', 28 to 1, 28 to 14),
                        Pad('2', at = 2 to 6),
                    ),
                    traps = listOf(
                        trap(Zone(7.3f, 9.9f, 11.6f, 11f), Laser('W', 1 to 10, 10 to 10, delay = 1.4f), say("Rule 1 started. Climb faster.", "Regel 1 gestartet. Kletter schneller.")),
                        trap(Pressed('2'), Play(Card.DEVIL_SAW), Power('V', false), Power('W', false), PathSaw(8f, 28f to 14.4f, 6f to 14.4f),
                            say("Port 29 open. Deep packet inspection on the way.", "Port 29 offen. Deep Packet Inspection unterwegs.")),
                        trap(Landed(17f, 24f), Laser('Z', 26 to 1, 26 to 14, on = 1.4f, off = 40f, delay = 0.3f), say("Rule 4: last gate. Count to one again.", "Regel 4: letztes Tor. Wieder bis eins zählen.")),
                    ),
                ) { put(2, 6, '.'); fill(28..30, 13..14, '.'); put(29, 14, 'D') },
            ),
        ) {
            border(); floor()
            fill(12..14, 13..13); fill(8..10, 11..11); fill(4..6, 9..9); fill(1..2, 7..7)
            fill(28..30, 13..14)
            spawn(2); put(2, 6, 'D'); bits(18)
        },

        // 19 — a trap room: the floor drops out ahead (a hop), the packets get reordered (left and right swap) while the stairs
        // go up to the right, and on the top floor, on the way back left to the door, the order is restored and a packet
        // falls from the ceiling on whoever keeps running
        Level(
            name = T("Delivery", "Zustellung"),
            intro = T("Packets arrive in order. Guaranteed.", "Pakete kommen der Reihe nach an. Garantiert."),
            traps = listOf(
                trap(PastX(4.5f), Fall('a'), say("Packet 1 got lost in transit.", "Paket 1 ging unterwegs verloren.")),
                trap(Landed(10.9f, 12.7f), Play(Card.TWISTED), Swap(true), say("Packet reordering! Left and right arrive swapped.", "Paket-Umsortierung! Links und rechts kommen vertauscht an.")),
                trap(Landed(25f, 28f), Swap(false), say("In-order delivery restored. You're welcome.", "Reihenfolge wiederhergestellt. Gern geschehen.")),
                trap(Zone(15.8f, 6.5f, 16.4f, 8.4f), Fall('c'), say("Packet from the upper bus. Mind your head.", "Paket vom oberen Bus. Kopf einziehen.")),
            ),
            hint = T("Left is right. Until the top floor.", "Links ist rechts. Bis zum Obergeschoss."),
        ) {
            border(); floor()
            fill(8..10, 15..17, 'a')
            fill(13..14, 13..14); fill(16..19, 13..14); fill(22..24, 11..14); fill(25..27, 9..14); fill(6..7, 6..7)
            fill(4..21, 8..8); fill(12..13, 1..2, 'c')
            spawn(); put(4, 7, 'D'); bits(19)
        },

        // 20 — a stateful firewall (R8 gates and a way, U13 firewall). Gate 2 and gate 3 are shut. The first pad (the ID scanner, on the
        // way) opens gate 3. The second pad, the cyan one, is the second check: it opens gate 2, but it shuts gate 3 again, for good,
        // until the ID scanner is stepped on a second time: back to the start. The scanner then starts a queue behind you and sends two
        // more beams down the lane, one low and one gate
        Level(
            name = T("Stateful Inspection", "Zustandsprüfung"),
            intro = T("Please have your ID ready.", "Bitte Ausweis bereithalten."),
            start = listOf(
                Laser('M', 21 to 1, 21 to 14),
                Laser('K', 26 to 1, 26 to 14),
                Circuit('w', on = false), Circuit('x', on = false),
                Pad('1', at = 5 to 14), Pad('2', at = 18 to 14, circuits = "x"),
            ),
            traps = listOf(
                trap(Pressed('1'), Power('K', false), say("ID scanned. Gate 3 open.", "Ausweis gescannt. Tor 3 offen.")),
                trap(Pressed('2'), Power('M', false), Power('K', true), say("Second check passed. Gate 2 open. Gate 3 closed. Naturally.", "Zweite Kontrolle bestanden. Tor 2 offen. Tor 3 zu. Natürlich.")),
                trap(Pressed('1', 2), Power('K', false), Laser('N', 3 to 1, 3 to 14, on = 0.8f, off = 40f, delay = 0.2f),
                    Laser('F', 15 to 14, 16 to 14, on = 1f, off = 40f, delay = 0.9f),
                    say("ID scanned again. The queue forms behind you.", "Ausweis erneut gescannt. Hinter dir bildet sich eine Schlange.")),
                trap(PastX(21.4f), Play(Card.SPIKE_SEED), Laser('Z', 24 to 1, 24 to 14, on = 0.8f, off = 40f, delay = 0.35f), say("Stateful firewall: I remember you.", "Zustandsbehaftete Firewall: Ich merk mir dich.")),
            ),
            hint = T("Gate 3 will not stay open. The scanner is back at the start.", "Tor 3 bleibt nicht offen. Der Scanner steht am Start."),
            // rematch: the room is turned around (spawn on the right, door on the left, the scanner far from the second check),
            // and the last gate comes with a bluff
            rematch = listOf(
                Round(
                    T("Stateless now. I forgot everything. Almost.", "Zustandslos jetzt. Ich merk mir nur noch dich."),
                    start = listOf(
                        Laser('M', 10 to 1, 10 to 14),
                        Laser('K', 5 to 1, 5 to 14),
                        Circuit('w', on = false), Circuit('x', on = false),
                        Pad('1', at = 26 to 14), Pad('2', at = 13 to 14, circuits = "x"),
                    ),
                    traps = listOf(
                        trap(Pressed('1'), Power('K', false), say("ID scanned. Gate 3 open.", "Ausweis gescannt. Tor 3 offen.")),
                        trap(Pressed('2'), Power('M', false), Power('K', true), say("Second check passed. Gate 2 open. Gate 3 closed. Again.", "Zweite Kontrolle bestanden. Tor 2 offen. Tor 3 zu. Wieder.")),
                        trap(Pressed('1', 2), Power('K', false), Laser('N', 28 to 1, 28 to 14, on = 0.8f, off = 40f, delay = 0.2f),
                            Laser('F', 15 to 14, 16 to 14, on = 1f, off = 40f, delay = 0.9f),
                            say("ID scanned again. The queue forms behind you. Again.", "Ausweis erneut gescannt. Hinter dir bildet sich wieder eine Schlange.")),
                        trap(BeforeX(10.6f), Bluff(Card.SPIKE_SEED), Laser('Z', 7 to 1, 7 to 14, on = 1f, off = 40f, delay = 0.35f), say("Spikes in gate 4. (Or are there?)", "Stacheln in Tor 4. (Oder doch nicht?)")),
                    ),
                ) { put(2, 14, 'D'); put(29, 14, 'P') },
            ),
        ) {
            border(); floor()
            put(1, 1, 'w'); put(2, 1, 'x')
            spawn(); door(); bits(20)
        },

        // 21 — EASTER EGG: the flat rate (R3 portal routing, U11 the route is manipulated). A wall blocks the lane and the portal
        // in front of it seems to lead past it, until you come close: then it is a captive portal that drops you on the top floor
        // at the far right. A queue gate forms behind you and the small print throttles the top floor: a beam just over your head,
        // so no jumping, not even over the hole. The way on is down, through the hole (you walk back left, and the hole is the
        // way). On the lane the portal in front of the door is a captive portal too: hop it
        Level(
            name = T("Flat Rate", "Flatrate"),
            intro = T("Unlimited flat rate. There is no small print.", "Unbegrenzte Flatrate. Ein Kleingedrucktes gibt es nicht."),
            start = listOf(
                Portal('p', 3 to 14, 8 to 14, twoWay = false),
                Portal('q', 22 to 14, 28 to 14, twoWay = false),
            ),
            traps = listOf(
                trap(Zone(1.9f, 13f, 2.9f, 15.5f), Play(Card.DECOY), Reroute('p', 28 to 7), say("Captive portal. Please accept the terms.", "Captive Portal. Bitte AGB akzeptieren.")),
                trap(Zone(24f, 6.5f, 31f, 8.4f), Laser('N', 28 to 1, 28 to 14, on = 0.6f, off = 40f, delay = 0.8f), Laser('U', 1 to 6, 27 to 6, delay = 1f),
                    say("Terms accepted. Throttled to 56k, as agreed: no jumping.", "AGB akzeptiert. Gedrosselt auf 56k, wie vereinbart: kein Springen.")),
                trap(Landed(6f, 10.5f), Laser('F', 13 to 14, 14 to 14, on = 1f, off = 40f, delay = 0.25f), say("Packet filter: low packets only.", "Paketfilter: nur flache Pakete.")),
                trap(Zone(17.6f, 13f, 21.6f, 15.5f), Reroute('q', 28 to 7), say("301 Moved Permanently. The portal too.", "301 Moved Permanently. Das Portal auch.")),
                trap(Landed(23f, 27f), Laser('G', 27 to 1, 27 to 14, on = 0.7f, off = 40f, delay = 0.3f), say("Rule 5: no entry. I count to one.", "Regel 5: Zutritt verboten. Ich zähle bis eins.")),
            ),
            hint = T("The small print says: no jumping. It says nothing about falling.", "Im Kleingedruckten steht: nicht springen. Von Fallen steht da nichts."),
        ) {
            border(); floor()
            fill(5..6, 9..14)
            fill(1..8, 8..8); fill(11..30, 8..8)
            spawn(1); door(); bits(21)
        },

        // 22 — the bouncer, a trap room (U2 the ceiling falls, U3 the wall moves): you start up on the top floor and run right; two
        // packets drop from the ceiling on the way. At the end you drop down to the lane and the door is on the far left, behind the
        // bouncer. He walks toward you to walk you out (hop him, or he walks you into the wall), and the floor in front of the door drops
        Level(
            name = T("Bouncer", "Türsteher"),
            intro = T("The bouncer won't let you in. I like him.", "Der Türsteher lässt dich nicht rein. Ich mag ihn."),
            traps = listOf(
                trap(Zone(7.8f, 5.5f, 8.7f, 7.4f), Play(Card.HEADBUTT), Fall('c'), say("Bouncer: you're not on the list.", "Türsteher: Du stehst nicht auf der Liste.")),
                trap(Zone(18.8f, 5.5f, 19.7f, 7.4f), Fall('d'), say("Packet from the upper bus. Mind your head.", "Paket vom oberen Bus. Kopf einziehen.")),
                trap(Landed(24f, 31f), Move('w', 22f, 0f, 3.5f), say("Bouncer: let me walk you out.", "Türsteher: Ich begleite dich raus.")),
                trap(Zone(15f, 13f, 16f, 15.5f), Fall('p'), say("The floor is for guests, too.", "Der Boden ist auch nur für Gäste.")),
            ),
            hint = T("The bouncer walks toward you. Jump him.", "Der Türsteher kommt auf dich zu. Spring über ihn."),
        ) {
            border(); floor()
            fill(1..28, 7..7)
            fill(10..11, 1..2, 'c'); fill(21..22, 1..2, 'd')
            fill(8..9, 13..14, 'w'); fill(4..5, 15..17, 'p')
            spawn(2, 6); put(2, 14, 'D'); bits(22)
        },

        // 23 — the information superhighway, a trap room (U3 the wall moves, R-free): two lanes, joined by a one-way on-ramp (a portal)
        // at the end of the first. Oncoming traffic on both: walls that roll toward you (hop them, or they walk you into the wall),
        // and a wrong-way driver on the second lane
        Level(
            name = T("Information Superhighway", "Datenautobahn"),
            intro = T("Have a safe trip! Buckle up.", "Gute Fahrt! Bitte anschnallen."),
            start = listOf(Portal('p', 27 to 14, 2 to 8, twoWay = false)),
            traps = listOf(
                trap(PastX(3f), Play(Card.SINKING), Move('a', -21f, 0f, 4f), say("Oncoming traffic. Slow vehicles keep right.", "Gegenverkehr. Langsame Fahrzeuge rechts halten.")),
                trap(Zone(1f, 7.2f, 6f, 9f), Move('b', -20f, 0f, 5f), say("Lane 2 is a lane, too.", "Spur 2 ist auch eine Spur.")),
                trap(Zone(12f, 5f, 17f, 9f), Move('c', -25f, 0f, 3.5f), say("Wrong-way driver. Not my fault.", "Geisterfahrer. Nicht meine Schuld.")),
            ),
            hint = T("Hop the traffic. All of it.", "Spring über den Verkehr. Über alles."),
        ) {
            border(); floor()
            fill(1..30, 9..9)
            fill(22..23, 13..14, 'a'); fill(21..22, 7..8, 'b'); fill(26..27, 7..8, 'c')
            spawn(); put(29, 8, 'D'); bits(23)
        },

        // 24 — the uplink, a trap room (U13 firewall, U1 the floor drops): the uplink is at the top, so you start at the top and go
        // down (the elevator is out of order). A packet drops from the ceiling on the first floor, a gate flashes where you land on the
        // second, a piece of its floor drops over the spikes on the ground, and a low beam waits where you land down there
        Level(
            name = T("Uplink", "Uplink"),
            intro = T("The uplink is at the top. I'm taking the elevator.", "Der Uplink ist ganz oben. Ich nehme den Aufzug."),
            traps = listOf(
                trap(Zone(7.6f, 5.5f, 9.2f, 7.1f), Fall('c'), say("Uplink full. Try the downlink.", "Uplink voll. Versuch's mit dem Downlink."), delay = 0.1f),
                trap(Zone(18.5f, 10f, 31f, 11.2f), Laser('G', 15 to 8, 15 to 10, on = 0.7f, off = 40f, delay = 0.3f), say("Floor 2: please wait for your number.", "Etage 2: Bitte Wartenummer ziehen.")),
                trap(Zone(17.2f, 10f, 20.5f, 11.2f), Play(Card.COLLAPSE), Fall('h'), say("Floor 2 is under maintenance. As of now.", "Etage 2 ist in Wartung. Ab jetzt.")),
                trap(Zone(15.2f, 14.3f, 17f, 15.2f), Laser('H', 19 to 14, 20 to 14, on = 1f, off = 40f, delay = 0.35f), say("Downlink: low packets only.", "Downlink: nur flache Pakete.")),
            ),
            hint = T("Down is the new up.", "Unten ist das neue Oben."),
        ) {
            border(); floor()
            fill(1..18, 7..7); fill(8..30, 11..11); fill(11..14, 11..11, 'h')
            fill(11..12, 1..1, 'c'); fill(11..14, 14..14, '^')
            spawn(2, 6); door(28); bits(24)
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
