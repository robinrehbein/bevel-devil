package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Chase
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

        // 17 — the first conveyor belt, as a puzzle room. The data bus runs in a cable duct, against you and faster than
        // you run, and the duct has spikes on its ceiling: no hopping through. Over the duct and up the stairs to the shelf;
        // a loose piece of the shelf drops on whoever runs under it (and leaves a hole up there). The pad at the far end of
        // the shelf turns the bus around: drop down and ride it to the door
        Level(
            name = T("Data Bus", "Datenbus"),
            intro = T("Timetable: every ten seconds. Roughly.", "Fahrplan: alle zehn Sekunden. Ungefähr."),
            start = listOf(Belt('b', -9f), Pad('1', at = 4 to 7)),
            traps = listOf(
                trap(Zone(10f, 9f, 17f, 12.2f), Play(Card.HEADBUTT), Fall('c'), say("Packet from the upper bus. Mind your head.", "Paket vom oberen Bus. Kopf einziehen.")),
                trap(Pressed('1'), Belt('b', 5f), say("Bus 1 reversed. Next stop: the door.", "Bus 1 fährt jetzt andersrum. Nächster Halt: die Tür.")),
            ),
            hint = T("The bus turns around somewhere. Not down here.", "Irgendwo dreht der Bus um. Nicht hier unten."),
        ) {
            border(); floor()
            fill(3..4, 14..14)
            fill(7..26, 12..12); fill(8..25, 13..13, 'v'); fill(7..26, 15..15, 'b')
            fill(27..27, 1..12)
            fill(24..26, 10..11)
            fill(3..22, 8..8); fill(12..14, 8..8, 'c')
            spawn(); door(); bits(17)
        },

        // 18 — the first laser, as a puzzle room. The door hangs high on the left, and the stairs to it are cut by a firewall
        // beam. The pad that drops the beam lies on the rack behind the blinking gate. Pressing it starts a port scan along
        // the floor (stay up on the rack until it has passed), and the beam over the stairs comes back once you stand on the
        // middle step: keep climbing
        Level(
            name = T("Firewall", "Firewall"),
            intro = T("I configured the firewall myself. Nice pattern, right?", "Die Firewall habe ich selbst eingestellt. Schönes Muster, oder?"),
            start = listOf(
                Laser('L', 16 to 1, 16 to 14, on = 1f, off = 1.6f, phase = 1.6f),
                Laser('W', 1 to 10, 10 to 10),
                Pad('1', at = 26 to 12),
            ),
            traps = listOf(
                trap(Pressed('1'), Play(Card.GHOST_BLOCK), Power('W', false), Laser('S', 17 to 14, 23 to 14, on = 0.8f, off = 40f, delay = 0.9f),
                    say("Rule 1 disabled. Running a port scan.", "Regel 1 deaktiviert. Starte Portscan.")),
                trap(Zone(7.3f, 9.9f, 10.6f, 11f), Laser('W', 1 to 10, 10 to 10, delay = 1f), say("Rule 1 restarted. Climb faster.", "Regel 1 neu gestartet. Kletter schneller.")),
            ),
            // rematch: the door moved down behind a beam of its own, the pad is up where the door was; coming down, a saw
            // inspects the floor
            rematch = listOf(
                Round(
                    T("Rules reloaded. Your move.", "Firewall-Regel aktualisiert. Rate mal, welche."),
                    start = listOf(
                        Laser('L', 16 to 1, 16 to 14, on = 1f, off = 1.6f, phase = 1.6f),
                        Laser('V', 28 to 1, 28 to 14),
                        Pad('2', at = 2 to 6),
                    ),
                    traps = listOf(
                        trap(Pressed('2'), Power('V', false), say("Port 29 open. Come on down.", "Port 29 offen. Komm runter.")),
                        trap(Pressed('2'), Play(Card.DEVIL_SAW), Saw(31f, 14.4f, -5f, 0f), say("Deep packet inspection.", "Deep Packet Inspection."), delay = 1f),
                    ),
                ) { put(2, 6, '.'); put(29, 14, 'D') },
            ),
        ) {
            border(); floor()
            fill(11..12, 13..14); fill(8..9, 11..11); fill(5..6, 9..9); fill(1..3, 7..7)
            fill(24..27, 13..14)
            spawn(3); put(2, 6, 'D'); bits(18)
        },

        // 19 — three floors, snake-shaped. The ground bus carries you to a pit, the stairs on the right lead to a shelf whose
        // bus runs back left. On the shelf the packets get reordered: left and right swap (jump the spikes, climb to the top
        // floor with swapped hands). On the top floor, right after the gap, the order is restored: whoever keeps pressing the
        // swapped key walks back into the gap
        Level(
            name = T("Delivery", "Zustellung"),
            intro = T("Packets arrive in order. Guaranteed.", "Pakete kommen der Reihe nach an. Garantiert."),
            start = listOf(Belt('a', 3f), Belt('u', -3f)),
            traps = listOf(
                trap(Zone(9f, 8.4f, 26f, 8.75f), Play(Card.TWISTED), Swap(true), say("Packet reordering! Left and right arrive swapped.", "Paket-Umsortierung! Links und rechts kommen vertauscht an.")),
                trap(Zone(18.6f, 3.5f, 30f, 5f), Swap(false), say("In-order delivery restored. You're welcome.", "Reihenfolge wiederhergestellt. Gern geschehen.")),
            ),
        ) {
            border(); floor()
            fill(7..8, 14..14, '^')
            fill(10..14, 15..15, 'a')
            pit(16..18); fill(16..18, 17..17, '^')
            fill(26..30, 13..14); fill(28..30, 11..12)
            fill(9..25, 9..9, 'u'); put(16, 8, '^')
            fill(6..8, 7..7)
            fill(10..30, 5..5); put(16, 5, '.')
            spawn(); put(29, 4, 'D')
        },

        // 20 — two firewall gates that are never open together, an island between them, a third gate in front of the door.
        // The ID scanner (a pad on the island's rack) opens gate 3, and passing gate 1 makes gate 2 remember you: it
        // reloads its rhythm. The second check behind gate 2 is the trap: stepping on it closes gate 3 again
        Level(
            name = T("Stateful Inspection", "Zustandsprüfung"),
            intro = T("Please have your ID ready.", "Bitte Ausweis bereithalten."),
            start = listOf(
                Laser('L', 8 to 1, 8 to 14, on = 2.4f, off = 1.2f),
                Laser('M', 21 to 1, 21 to 14, on = 2.2f, off = 1.2f, phase = 0.4f),
                Laser('K', 26 to 1, 26 to 14),
                Pad('1', at = 15 to 10), Pad('2', at = 24 to 14),
            ),
            traps = listOf(
                trap(PastX(8.8f), Laser('M', 21 to 1, 21 to 14, on = 3.2f, off = 1.2f, delay = 0.7f), say("Stateful firewall: I remember you.", "Zustandsbehaftete Firewall: Ich merk mir dich.")),
                trap(Pressed('1'), Power('K', false), say("ID scanned. Gate 3 open.", "Ausweis gescannt. Tor 3 offen.")),
                trap(Pressed('2'), Play(Card.GHOST_BLOCK), Laser('K', 26 to 1, 26 to 14, on = 3f, off = 60f, delay = 0.05f), say("Second check: your ID just expired.", "Zweite Kontrolle: Dein Ausweis ist gerade abgelaufen.")),
            ),
            // rematch: the second check is all talk now (bluff), but whoever jumps the queue crosses a fresh beam
            rematch = listOf(
                Round(
                    T("Stateless now. I forgot everything. Almost.", "Zustandslos jetzt. Ich merk mir nur noch dich."),
                    traps = listOf(
                        trap(PastX(8.8f), Laser('M', 21 to 1, 21 to 14, on = 3.2f, off = 1.2f, delay = 0.7f)),
                        trap(Pressed('1'), Power('K', false), say("ID scanned. Same procedure.", "Ausweis gescannt. Wie gehabt.")),
                        trap(Pressed('2'), Bluff(Card.GHOST_BLOCK), say("Second check. Your ID is... fine.", "Zweite Kontrolle. Dein Ausweis ist ... gültig.")),
                        trap(Airborne(22.2f, 25.6f), Laser('J', 22 to 13, 25 to 13, on = 0.8f, off = 60f, delay = 0.05f), say("Queue jumpers get logged.", "Vordrängler werden protokolliert.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(11..12, 13..14); fill(14..16, 11..14)
            spawn(); door(); bits(20)
        },

        // 21 — EASTER EGG: the flat rate. The portal at the end of the lower floor comes out right next to the door, until you
        // come close: then it is a captive portal, a cage up on the left. Accepting the terms (the pad) opens the cage after a
        // while, and the small print throttles the upper floor: a beam just over your head, so no jumping. Not even over the
        // hole. Just before it the door moves down below (301), and the portal is in the way down there: hop it
        Level(
            name = T("Flat Rate", "Flatrate"),
            intro = T("Unlimited flat rate. There is no small print.", "Unbegrenzte Flatrate. Ein Kleingedrucktes gibt es nicht."),
            start = listOf(
                Portal('p', 27 to 14, 28 to 7, twoWay = false),
                Pad('1', at = 2 to 7),
            ),
            traps = listOf(
                trap(Zone(22.6f, 13f, 26.6f, 15f), Play(Card.DECOY), Reroute('p', 4 to 7), say("Captive portal. Please accept the terms.", "Captive Portal. Bitte AGB akzeptieren.")),
                trap(Pressed('1'), Laser('U', 1 to 6, 27 to 6, delay = 1.6f), say("Loading terms and conditions...", "Lade AGB ...")),
                trap(Pressed('1'), Hide('w'), say("Accepted. Throttled to 56k, as agreed: no jumping.", "Akzeptiert. Gedrosselt auf 56k, wie vereinbart: kein Springen."), delay = 1.6f),
                trap(Zone(18.5f, 5.5f, 23.5f, 7.6f), DoorTo(29, 14), say("301 Moved Permanently. The door, too.", "301 Moved Permanently. Die Tür auch.")),
            ),
            hint = T("The small print says: no jumping. It says nothing about falling.", "Im Kleingedruckten steht: nicht springen. Von Fallen steht da nichts."),
        ) {
            border(); floor()
            fill(1..30, 8..8); fill(24..25, 8..8, '.')
            put(8, 14, '^'); put(9, 14, '^'); put(14, 14, '^'); put(15, 14, '^'); put(20, 14, '^'); put(21, 14, '^')
            fill(6..6, 1..7, 'w')
            spawn(); put(29, 7, 'D'); bits(21)
        },

        // 22 — the bouncer: a wall between you and the door that steps in front of you wherever you go. Go back left, knock on
        // the hidden step (port knocking) to climb to the shelf, drop onto the bouncer's head and keep walking: whoever
        // stands still up there is walked out, into the edge of the shelf
        Level(
            name = T("Bouncer", "Türsteher"),
            intro = T("The bouncer won't let you in. I like him.", "Der Türsteher lässt dich nicht rein. Ich mag ihn."),
            legend = mapOf('k' to ghost),
            traps = listOf(
                trap(BeforeX(24.4f), Play(Card.STALKER), Chase('w', speed = 6f, left = 5f, right = 0.5f), say("Bouncer: you're not on the list.", "Türsteher: Du stehst nicht auf der Liste.")),
                trap(Touch('k'), say("Knock-knock-knock. Port 22 is open. (It was hidden.)", "Klopf-klopf-klopf. Port 22 ist offen. (Er war versteckt.)")),
                trap(Zone(21.6f, 7.2f, 30f, 8.3f), Move('w', -4f, 0f, 2.5f), say("Bouncer: let me walk you out.", "Türsteher: Ich begleite dich raus."), delay = 0.6f),
            ),
            hint = T("Bouncers hate knocking. Knock anyway. From below.", "Türsteher hassen Klopfen. Klopf trotzdem. Von unten."),
        ) {
            border(); floor()
            fill(27..28, 8..14, 'w')
            fill(2..3, 13..14); fill(5..6, 11..11); fill(6..7, 9..9, 'k'); fill(8..21, 7..7)
            fill(22..30, 4..4); fill(22..30, 5..5, 'v')
            spawn(25); door(); bits(22)
        },

        // 23 — three express lanes, each running right, joined by one-way on-ramps (portals). Lane 2 jams and runs backwards
        // into the spikes you hopped coming in; lane 3 is closed for works ahead of you, its hole drops onto lane 2's spikes
        Level(
            name = T("Information Superhighway", "Datenautobahn"),
            intro = T("Have a safe trip! Buckle up.", "Gute Fahrt! Bitte anschnallen."),
            start = listOf(
                Belt('a', 4f), Belt('b', 4f), Belt('c', 4f), Belt('d', 4f),
                Portal('1', 30 to 14, 1 to 9, twoWay = false),
                Portal('2', 30 to 9, 1 to 4, twoWay = false),
            ),
            traps = listOf(
                trap(Zone(10f, 7.5f, 14f, 10f), Belt('b', -6f), say("Traffic jam. Lane 2 now runs backwards.", "Stau. Spur 2 läuft jetzt rückwärts.")),
                trap(Zone(9f, 2.5f, 12f, 5f), Play(Card.SINKING), Hide('d'), say("Lane closed for maintenance.", "Spur wegen Wartung gesperrt.")),
            ),
        ) {
            border(); floor()
            fill(2..29, 15..15, 'a'); put(12, 14, '^'); put(13, 14, '^')
            fill(1..30, 10..10); fill(2..29, 10..10, 'b'); put(6, 9, '^'); put(7, 9, '^'); fill(15..16, 9..9, '^')
            fill(1..30, 5..5); fill(2..27, 5..5, 'c'); fill(15..16, 5..5, 'd')
            spawn(); put(29, 4, 'D')
        },

        // 24 — climb the racks to the uplink; every rack has a timed beam across it, and the second rack is a hot aisle (a beam
        // warms up where you land and would wait). At the top the door sinks through the racks to the bottom (downlink): climb back
        // down, each beam in its turn
        Level(
            name = T("Uplink", "Uplink"),
            intro = T("The uplink is at the top. I'm taking the elevator.", "Der Uplink ist ganz oben. Ich nehme den Aufzug."),
            start = listOf(
                Laser('1', 7 to 1, 7 to 12, on = 1f, off = 1.8f, phase = 1.4f),
                Laser('2', 12 to 1, 12 to 10, on = 1f, off = 1.8f, phase = 0.4f),
                Laser('3', 17 to 1, 17 to 8, on = 1f, off = 1.8f, phase = 2.2f),
                Laser('4', 22 to 1, 22 to 6, on = 1f, off = 1.8f, phase = 1.0f),
            ),
            traps = listOf(
                trap(Landed(9f, 12f), Laser('W', 10 to 1, 10 to 10, on = 0.6f, off = 60f, delay = 0.9f), say("Rack 2: hot aisle. Do not linger.", "Rack 2: Heißgang. Nicht verweilen.")),
            ) + doorTrail(
                Zone(25.5f, 1f, 31f, 5f), 28, 4,
                listOf(DoorTo(28, 13, speed = 16f), DoorTo(2, 14, speed = 14f)),
                first = listOf(Play(Card.SHY_DOOR), say("Uplink full. Try the downlink.", "Uplink voll. Versuch's mit dem Downlink.")),
            ),
        ) {
            border(); floor()
            fill(4..8, 13..14); fill(9..13, 11..14); fill(14..18, 9..14); fill(19..23, 7..14); fill(24..30, 5..14)
            spawn(); put(28, 4, 'D')
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
