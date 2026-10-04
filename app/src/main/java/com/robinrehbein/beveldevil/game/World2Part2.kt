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

/** World 2, levels 17-32. Act 2, "Traffic": conveyor belts (the data bus) and lasers (the firewall), first alone, then with portals and the classics. */
object World2Part2 {
    private val hidden = Glyph(spike = true, hidden = true)
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)

    val levels: List<Level> = listOf(

        // 17 — the first conveyor belt: standing still rides you into the spikes; landing after the first hop turns the belt around, and again after the second
        Level(
            name = T("Data Bus", "Datenbus"),
            intro = T("Timetable: every ten seconds. Roughly.", "Fahrplan: alle zehn Sekunden. Ungefähr."),
            start = listOf(Belt('b', 3.5f)),
            traps = listOf(
                trap(Landed(14.2f, 18.6f), Play(Card.TWISTED), Belt('b', -6f), say("Packet reordering: the bus runs the other way.", "Paket-Umsortierung: Der Bus fährt andersrum.")),
                trap(Landed(21.4f, 25f), Belt('b', -10f), say("Congestion control: even faster.", "Staukontrolle: noch schneller.")),
            ),
        ) {
            border(); floor()
            fill(5..26, 15..15, 'b')
            put(13, 14, '^'); put(20, 14, '^'); put(21, 14, '^')
            spawn(); door(); bits(17)
        },

        // 18 — the first laser: a firewall gate that opens now and then; waiting in front of it is a bad idea, and so is running behind it
        Level(
            name = T("Firewall", "Firewall"),
            intro = T("I configured the firewall myself. Nice pattern, right?", "Die Firewall habe ich selbst eingestellt. Schönes Muster, oder?"),
            start = listOf(Laser('L', 15 to 1, 15 to 14, on = 1f, off = 1.4f, phase = 1.4f)),
            traps = listOf(
                trap(Landed(10.6f, 14.4f), Play(Card.GHOST_BLOCK), Laser('M', 14 to 1, 14 to 14, on = 0.6f, off = 40f, delay = 0.9f), say("Port scan detected.", "Portscan erkannt.")),
                trap(PastX(15.6f), Laser('N', 20 to 1, 20 to 14, on = 0.7f, off = 40f, delay = 0.35f), say("Rule 2 of 2: no running in the data center.", "Regel 2 von 2: Im Rechenzentrum wird nicht gerannt.")),
            ),
            // rematch: no scan in front of the gate; behind it, the beam waits for whoever waits
            rematch = listOf(
                Round(
                    T("Rules reloaded. Your move.", "Firewall-Regel aktualisiert. Rate mal, welche."),
                    start = listOf(Laser('L', 15 to 1, 15 to 14, on = 1f, off = 1.4f, phase = 1.4f)),
                    traps = listOf(
                        trap(Landed(10.6f, 14.4f), say("Port scan detected. Probably.", "Portscan erkannt. Vielleicht.")),
                        trap(PastX(15.6f), Play(Card.GHOST_BLOCK), Laser('N', 19 to 1, 19 to 14, on = 0.7f, off = 40f, delay = 0.8f), say("New rule: no standing in the data center.", "Neue Regel: Wer steht, wird gelöscht.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            rack(9, 1, 2)
            spawn(); door(); bits(18)
        },

        // 19 — the belt carries you along, then reverses faster than you run; the hops out of it land in spikes, then before a pit
        Level(
            name = T("Delivery", "Zustellung"),
            intro = T("Packets arrive in order. Guaranteed.", "Pakete kommen der Reihe nach an. Garantiert."),
            legend = mapOf('A' to hidden),
            start = listOf(Belt('b', 2f)),
            traps = listOf(
                trap(PastX(10f), Play(Card.TWISTED), Belt('b', -10f), say("Packet reordering! Everything arrives backwards.", "Paket-Umsortierung! Alles kommt rückwärts an.")),
                trap(Airborne(15f, 16.6f), Show('A'), say("Checksum mismatch.", "Prüfsumme stimmt nicht.")),
                trap(Landed(24f, 26.9f), Fall('f'), say("Connection dropped.", "Verbindung getrennt."), delay = 0.05f),
            ),
        ) {
            border(); floor()
            fill(1..25, 15..15, 'b')
            put(1, 13, '>'); put(1, 14, '>')
            put(20, 14, 'A'); put(21, 14, 'A')
            fill(27..28, 15..17, 'f')
            spawn(5); door()
        },

        // 20 — two gates that are never open together: wait on the island between them. A spike grows behind the hop, the landing resets the second gate, and a third gate warms up for runners
        Level(
            name = T("Stateful Inspection", "Zustandsprüfung"),
            intro = T("Please have your ID ready.", "Bitte Ausweis bereithalten."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Laser('L', 10 to 1, 10 to 14, on = 1f, off = 2f, phase = 2f),
                Laser('M', 20 to 1, 20 to 14, on = 1f, off = 2f, phase = 0.4f),
            ),
            traps = listOf(
                trap(PastX(15.6f), Play(Card.DECOY), Show('A'), say("Hidden rule: no landing after port 15.", "Versteckte Regel: Keine Landung nach Port 15.")),
                trap(Landed(16f, 19.8f), Laser('M', 20 to 1, 20 to 14, on = 1f, off = 2f, phase = 0f), say("Firewall rules reloaded.", "Firewall-Regeln neu geladen.")),
                trap(PastX(20.8f), Laser('K', 25 to 1, 25 to 14, on = 0.7f, off = 40f, delay = 0.45f), say("Session limit reached.", "Sitzungslimit erreicht.")),
            ),
            // rematch: the landing behind the spike is safe now (bluff), but the beam comes for whoever waits behind the gate
            rematch = listOf(
                Round(
                    T("Stateless now. I forgot everything. Almost.", "Zustandslos jetzt. Ich merk mir nur noch dich."),
                    start = listOf(
                        Laser('L', 10 to 1, 10 to 14, on = 1f, off = 2f, phase = 2f),
                        Laser('M', 20 to 1, 20 to 14, on = 1f, off = 2f, phase = 0.4f),
                    ),
                    traps = listOf(
                        trap(PastX(15.6f), Bluff(Card.DECOY)),
                        trap(Landed(16f, 19.8f), Laser('M', 20 to 1, 20 to 14, on = 1f, off = 2f, phase = 0f)),
                        trap(PastX(20.8f), Laser('K', 22 to 1, 22 to 14, on = 0.7f, off = 40f, delay = 0.9f), say("Loitering is logged.", "Wer rumsteht, landet im Log.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            put(15, 14, '^')
            put(18, 14, 'A'); put(19, 14, 'A')
            spawn(); door(); bits(20)
        },

        // 21 — EASTER EGG: bandwidth (a beam as a low ceiling: only short hops fit through); spikes before the door, and a portal in front of them
        Level(
            name = T("Flat Rate", "Flatrate"),
            intro = T("Unlimited flat rate. There is no small print.", "Unbegrenzte Flatrate. Ein Kleingedrucktes gibt es nicht."),
            legend = mapOf('A' to hidden),
            start = listOf(Laser('H', 5 to 12, 26 to 12)),
            traps = listOf(
                trap(PastX(22.4f), Play(Card.SPIKE_SEED), Show('A'), say("Throttled to 56k. Also: spikes.", "Auf 56k gedrosselt. Und: Spikes.")),
                trap(Airborne(23.5f, 25.8f), Portal('1', 27 to 14, 3 to 14, twoWay = false), say("301 Moved Permanently. The door, too.", "301 Moved Permanently. Die Tür auch.")),
            ),
        ) {
            border(); floor()
            put(9, 14, '^'); put(10, 14, '^'); put(14, 14, '^'); put(15, 14, '^'); put(19, 14, '^'); put(20, 14, '^')
            put(24, 14, 'A'); put(25, 14, 'A')
            spawn(); door(); bits(21)
        },

        // 22 — a hidden block is the switch for the firewall: knock by jumping. Then the firewall has a new rule for runners, and a last pair of spikes
        Level(
            name = T("Bouncer", "Türsteher"),
            intro = T("The bouncer won't let you in. I like him.", "Der Türsteher lässt dich nicht rein. Ich mag ihn."),
            legend = mapOf('k' to ghost, 'A' to hidden),
            start = listOf(
                Laser('L', 15 to 1, 15 to 14),
                Laser('M', 23 to 1, 23 to 14, on = 0.9f, off = 1.6f, phase = 0.5f),
            ),
            traps = listOf(
                trap(Touch('k'), Play(Card.GHOST_BLOCK), Power('L', false), say("Knock-knock-knock. Port 22 is open. (It was hidden.)", "Klopf-klopf-klopf. Port 22 ist offen. (Er war versteckt.)")),
                trap(PastX(15.6f), Laser('N', 19 to 1, 19 to 14, on = 0.7f, off = 40f, delay = 0.35f), say("Bouncer: new rule, same face.", "Türsteher: neue Regel, gleiches Gesicht.")),
                trap(PastX(24.2f), Show('A'), say("VIP list: spikes only.", "VIP-Liste: nur Spikes.")),
            ),
        ) {
            border(); floor()
            put(8, 11, 'k')
            put(27, 14, 'A'); put(28, 14, 'A')
            spawn(); door(); bits(22)
        },

        // 23 — a ride through three belts and two one-way portals: the express lane turns around under you, and so does the last one
        Level(
            name = T("Information Superhighway", "Datenautobahn"),
            intro = T("Have a safe trip! Buckle up.", "Gute Fahrt! Bitte anschnallen."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Belt('a', 4.5f), Belt('b', 4.5f), Belt('c', 4.5f),
                Portal('1', 15 to 14, 4 to 8, twoWay = false),
                Portal('2', 15 to 8, 20 to 14, twoWay = false),
            ),
            traps = listOf(
                trap(Touch('b'), Play(Card.SINKING), Belt('b', -6f), say("Traffic jam on the express lane.", "Stau auf der Überholspur.")),
                trap(Touch('c'), Belt('c', -6f), say("Load shedding: lane 3 runs backwards.", "Lastabwurf: Spur 3 läuft rückwärts.")),
                trap(Airborne(23f, 25.6f), Show('A'), say("Packet dropped.", "Paket verworfen.")),
            ),
        ) {
            border(); floor()
            fill(3..13, 15..15, 'a'); fill(19..27, 15..15, 'c')
            fill(16..17, 1..14)
            fill(3..14, 9..9, 'b'); put(15, 9, '#')
            put(8, 14, '^'); put(9, 8, '^'); put(12, 8, '^'); put(23, 14, '^'); put(24, 14, '^')
            put(27, 14, 'A'); put(28, 14, 'A')
            spawn(); door()
        },

        // 24 — climb the racks: every jump crosses a timed beam, a beam warms up where you wait on the second rack, the last rack crumbles and grows spikes
        Level(
            name = T("Uplink", "Uplink"),
            intro = T("The uplink is at the top. I'm taking the elevator.", "Der Uplink ist ganz oben. Ich nehme den Aufzug."),
            legend = mapOf('A' to hidden),
            start = listOf(
                Laser('1', 8 to 1, 8 to 12, on = 1f, off = 1.8f, phase = 1.4f),
                Laser('2', 14 to 1, 14 to 10, on = 1f, off = 1.8f, phase = 0.4f),
                Laser('3', 20 to 1, 20 to 8, on = 1f, off = 1.8f, phase = 2.2f),
            ),
            traps = listOf(
                trap(Landed(10f, 13.9f), Laser('W', 13 to 1, 13 to 10, on = 0.6f, off = 40f, delay = 0.9f), say("Rack 2: hot aisle. Do not linger.", "Rack 2: Heißgang. Nicht verweilen.")),
                trap(Touch('c'), Play(Card.CRUMBLE), Fall('c'), say("Thermal throttling: this rack is going down.", "Thermische Drosselung: Dieses Rack fährt herunter."), delay = 1.2f),
                trap(Landed(22f, 23.9f), Show('A'), say("Uplink established. Spikes included.", "Uplink steht. Spikes inklusive.")),
            ),
        ) {
            border(); floor()
            pit(16..19)
            fill(4..7, 13..14); fill(10..13, 11..14); fill(16..19, 9..13, 'c'); fill(22..29, 7..14)
            for (x in listOf(8, 9, 14, 15, 16, 17, 18, 19, 20, 21)) put(x, 14, '^')
            put(24, 6, 'A'); put(25, 6, 'A')
            spawn(); put(28, 6, 'D')
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
