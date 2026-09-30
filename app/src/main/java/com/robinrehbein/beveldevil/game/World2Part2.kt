package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Belt
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Laser
import com.robinrehbein.beveldevil.game.Action.PathSaw
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Power
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.Idle
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch

/** World 2, levels 17-32. Act 2, "Traffic": conveyor belts (the data bus) and lasers (the firewall), first alone, then with portals and the classics. */
object World2Part2 {
    private val hidden = Glyph(spike = true, hidden = true)
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)

    val levels: List<Level> = listOf(

        // 17 — the first conveyor belt: standing still rides you into the spikes
        Level(
            name = T("Data Bus", "Datenbus"),
            intro = T("Timetable: every ten seconds. Roughly.", "Fahrplan: alle zehn Sekunden. Ungefähr."),
            start = listOf(Belt('b', 3.5f)),
        ) {
            border(); floor()
            fill(5..26, 15..15, 'b')
            put(13, 14, '^'); put(20, 14, '^'); put(21, 14, '^')
            spawn(); door(); bits(17)
        },

        // 18 — the first laser: a firewall gate that opens now and then
        Level(
            name = T("Firewall", "Firewall"),
            intro = T("I configured the firewall myself. Nice pattern, right?", "Die Firewall habe ich selbst eingestellt. Schönes Muster, oder?"),
            start = listOf(Laser('L', 15 to 1, 15 to 14, on = 1f, off = 1.4f, phase = 1.4f)),
        ) {
            border(); floor()
            rack(9, 1, 2)
            spawn(); door(); bits(18)
        },

        // 19 — the belt carries you along, then reverses faster than you run
        Level(
            name = T("Delivery", "Zustellung"),
            intro = T("Packets arrive in order. Guaranteed.", "Pakete kommen der Reihe nach an. Garantiert."),
            start = listOf(Belt('b', 2f)),
            traps = listOf(
                trap(PastX(10f), Play(Card.TWISTED), Belt('b', -10f), say("Packet reordering! Everything arrives backwards.", "Paket-Umsortierung! Alles kommt rückwärts an.")),
            ),
        ) {
            border(); floor()
            fill(1..25, 15..15, 'b')
            put(1, 13, '>'); put(1, 14, '>')
            spawn(5); door()
        },

        // 20 — two gates that are never open together: wait on the island between them
        Level(
            name = T("Stateful Inspection", "Zustandsprüfung"),
            intro = T("Please have your ID ready.", "Bitte Ausweis bereithalten."),
            start = listOf(
                Laser('L', 10 to 1, 10 to 14, on = 1f, off = 2f, phase = 2f),
                Laser('M', 20 to 1, 20 to 14, on = 1f, off = 2f, phase = 0.4f),
            ),
        ) {
            border(); floor()
            put(15, 14, '^')
            spawn(); door(); bits(20)
        },

        // 21 — EASTER EGG: bandwidth (a beam as a low ceiling: only short hops fit through)
        Level(
            name = T("Flat Rate", "Flatrate"),
            intro = T("Unlimited flat rate. There is no small print.", "Unbegrenzte Flatrate. Ein Kleingedrucktes gibt es nicht."),
            legend = mapOf('A' to hidden),
            start = listOf(Laser('H', 5 to 12, 26 to 12)),
            traps = listOf(
                trap(PastX(22.4f), Play(Card.SPIKE_SEED), Show('A'), say("Throttled to 56k. Also: spikes.", "Auf 56k gedrosselt. Und: Spikes.")),
            ),
        ) {
            border(); floor()
            put(9, 14, '^'); put(10, 14, '^'); put(14, 14, '^'); put(15, 14, '^'); put(19, 14, '^'); put(20, 14, '^')
            put(24, 14, 'A'); put(25, 14, 'A')
            spawn(); door(); bits(21)
        },

        // 22 — a hidden block is the switch for the firewall: knock by jumping
        Level(
            name = T("Bouncer", "Türsteher"),
            intro = T("The bouncer won't let you in. I like him.", "Der Türsteher lässt dich nicht rein. Ich mag ihn."),
            legend = mapOf('k' to ghost),
            start = listOf(
                Laser('L', 15 to 1, 15 to 14),
                Laser('M', 23 to 1, 23 to 14, on = 0.9f, off = 1.6f, phase = 0.5f),
            ),
            traps = listOf(
                trap(Touch('k'), Play(Card.GHOST_BLOCK), Power('L', false), say("Knock-knock-knock. Port 22 is open. (It was hidden.)", "Klopf-klopf-klopf. Port 22 ist offen. (Er war versteckt.)")),
            ),
        ) {
            border(); floor()
            put(8, 11, 'k')
            spawn(); door(); bits(22)
        },

        // 23 — a ride through three belts and two one-way portals: only the spikes need you
        Level(
            name = T("Information Superhighway", "Datenautobahn"),
            intro = T("Have a safe trip! Buckle up.", "Gute Fahrt! Bitte anschnallen."),
            start = listOf(
                Belt('a', 4.5f), Belt('b', 4.5f), Belt('c', 4.5f),
                Portal('1', 15 to 14, 4 to 8, twoWay = false),
                Portal('2', 15 to 8, 20 to 14, twoWay = false),
            ),
        ) {
            border(); floor()
            fill(3..13, 15..15, 'a'); fill(19..27, 15..15, 'c')
            fill(16..17, 1..14)
            fill(3..14, 9..9, 'b'); put(15, 9, '#')
            put(8, 14, '^'); put(9, 8, '^'); put(12, 8, '^'); put(23, 14, '^'); put(24, 14, '^')
            spawn(); door()
        },

        // 24 — climb the racks: every jump crosses a timed beam, and the last rack crumbles
        Level(
            name = T("Uplink", "Uplink"),
            intro = T("The uplink is at the top. I'm taking the elevator.", "Der Uplink ist ganz oben. Ich nehme den Aufzug."),
            start = listOf(
                Laser('1', 8 to 1, 8 to 12, on = 1f, off = 1.8f, phase = 1.4f),
                Laser('2', 14 to 1, 14 to 10, on = 1f, off = 1.8f, phase = 0.4f),
                Laser('3', 20 to 1, 20 to 8, on = 1f, off = 1.8f, phase = 2.2f),
            ),
            traps = listOf(
                trap(Touch('c'), Play(Card.CRUMBLE), Fall('c'), say("Thermal throttling: this rack is going down.", "Thermische Drosselung: Dieses Rack fährt herunter."), delay = 1.2f),
            ),
        ) {
            border(); floor()
            pit(16..19)
            fill(4..7, 13..14); fill(10..13, 11..14); fill(16..19, 9..13, 'c'); fill(22..29, 7..14)
            for (x in listOf(8, 9, 14, 15, 16, 17, 18, 19, 20, 21)) put(x, 14, '^')
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
        ) {
            border()
            fill(0..4, 15..17); fill(28..31, 15..17)
            fill(5..9, 15..15, 'a'); fill(11..15, 15..15, 'b'); fill(17..21, 15..15, 'c'); fill(23..27, 15..15, 'd')
            fill(5..27, 17..17, '^')
            spawn(); door(); bits(25)
        },

        // 26 — the gate stays shut for seconds, but idle connections are dropped: keep hopping while you wait
        Level(
            name = T("Ticket Number", "Wartenummer"),
            intro = T("The gate opens shortly. Please linger.", "Das Tor öffnet gleich. Bitte kurz verweilen."),
            start = listOf(Laser('L', 15 to 1, 15 to 14, on = 3.5f, off = 1.6f)),
            traps = listOf(
                trap(Idle(1.5f), Play(Card.CRUMBLE), Fall('a'), say("Connection closed: idle timeout.", "Verbindung beendet: Leerlauf-Timeout.")),
            ),
        ) {
            border(); floor()
            fill(9..13, 15..17, 'a')
            spawn(); door(); bits(26)
        },

        // 27 — EASTER EGG: DDoS (Distributed Denial of Stairs, on a belt that runs against you)
        Level(
            name = T("DDoS", "DDoS"),
            intro = T("Light traffic today. Just you and the stairs.", "Heute wenig Verkehr. Nur du und die Treppe."),
            start = listOf(Belt('b', -3f)),
            traps = listOf(
                trap(PastX(4f), Play(Card.HEADBUTT), Fall('c'), say("10,000 blocks per second.", "10.000 Blöcke pro Sekunde.")),
                trap(PastX(4f), Fall('d'), delay = 0.35f),
                trap(PastX(4f), Fall('e'), delay = 0.7f),
                trap(PastX(4f), Fall('f'), delay = 1.05f),
                trap(PastX(4f), Fall('g'), delay = 1.4f),
                trap(PastX(4f), Fall('h'), delay = 1.75f),
            ),
        ) {
            border(); floor()
            fill(5..26, 15..15, 'b')
            fill(8..9, 3..4, 'c'); fill(12..13, 3..4, 'd'); fill(15..16, 3..4, 'e')
            fill(19..20, 3..4, 'f'); fill(22..23, 3..4, 'g'); fill(26..27, 3..4, 'h')
            spawn(); door(); bits(27, x0 = 24, y = 1)
        },

        // 28 — a VPN tunnel goes under the firewall; the intrusion prevention system lays a beam on the floor behind it
        Level(
            name = T("Split Tunnel", "Split Tunnel"),
            intro = T("The tunnel is the only shortcut. I swear.", "Der Tunnel ist die einzige Abkürzung. Ich schwöre."),
            start = listOf(Laser('L', 15 to 1, 15 to 14), Portal('1', 11 to 14, 19 to 14)),
            traps = listOf(
                trap(PastX(19.2f), Play(Card.SPIKE_SEED), Laser('M', 23 to 14, 26 to 14, on = 0.8f, off = 1.8f, delay = 0.35f), say("IPS: tunnel detected. New rule installed.", "IPS: Tunnel erkannt. Neue Regel installiert.")),
            ),
        ) {
            border(); floor()
            spawn(); door(); bits(28)
        },

        // 29 — a pendulum saw over a belt that hurries you, and another saw that is right behind you
        Level(
            name = T("Race Condition", "Wettlaufsituation"),
            intro = T("The winner gets a cookie. I have no cookies.", "Der Sieger bekommt einen Keks. Ich habe keine Kekse."),
            start = listOf(
                Belt('b', 5f),
                PathSaw(5f, 16f to 14.4f, 16f to 11.6f),
                PathSaw(5f, 21f to 14.4f, 21f to 11.6f, delay = 0.56f),
            ),
            traps = listOf(
                trap(PastX(6f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 6f, 0f), say("Thread 3 joins the race.", "Thread 3 steigt ins Rennen ein.")),
            ),
        ) {
            border(); floor()
            fill(6..27, 15..15, 'b')
            put(11, 14, '^'); put(25, 14, '^')
            spawn(); door()
        },

        // 30 — three one-way portals, each one lands higher up in the air
        Level(
            name = T("Hop Limit", "Hop-Limit"),
            intro = T("TTL: 64. No need to rush.", "TTL: 64. Kein Grund zur Eile."),
            start = listOf(
                Portal('1', 7 to 14, 12 to 8, twoWay = false),
                Portal('2', 18 to 9, 23 to 4, twoWay = false),
                Portal('3', 29 to 5, 25 to 12, twoWay = false),
            ),
        ) {
            border(); floor()
            fill(19..20, 1..14)
            fill(11..18, 10..10); fill(22..29, 6..6)
            put(16, 9, '^'); put(27, 5, '^')
            spawn(); door()
        },

        // 31 — EASTER EGG: HTTP 408 Request Timeout (a treadmill against you and a gate that closes for good at 5.2 s)
        Level(
            name = T("Detention", "Nachsitzen"),
            intro = T("No rush. I've got all day.", "Keine Hektik. Ich habe den ganzen Tag Zeit."),
            start = listOf(Belt('b', -5f), Laser('L', 27 to 1, 27 to 14, delay = 5.2f)),
            traps = listOf(
                trap(After(0.2f), Play(Card.SINKING), say("408: the server waited for you. Not anymore.", "408: Der Server hat auf dich gewartet. Nicht mehr.")),
            ),
        ) {
            border(); floor()
            fill(3..26, 15..15, 'b')
            spawn(); door()
        },

        // 32 — act finale: belt, tunnel, timed gate and a belt that turns around in front of it
        Level(
            name = T("Core Switch", "Core-Switch"),
            intro = T("Finally, the data-center basement. This is where I live.", "Endlich der Rechenzentrumskeller. Hier wohne ich."),
            start = listOf(
                Belt('a', 4f), Belt('b', 3f),
                Portal('1', 13 to 14, 17 to 14, twoWay = false),
                Portal('2', 24 to 14, 28 to 14, twoWay = false),
                Laser('G', 21 to 1, 21 to 14, on = 0.9f, off = 1.8f, phase = 1.0f),
            ),
            traps = listOf(
                trap(PastX(18.5f), Play(Card.GRAND_FINALE), Belt('b', -9f), say("Spanning tree recalculated. Your belt now runs the other way.", "Spanning Tree neu berechnet. Dein Band läuft jetzt andersrum.")),
            ),
        ) {
            border(); floor()
            fill(3..12, 15..15, 'a'); fill(17..24, 15..15, 'b')
            fill(14..15, 1..14); fill(25..26, 1..14)
            put(8, 14, '^'); put(16, 14, '^')
            spawn(); door()
        },
    )
}
