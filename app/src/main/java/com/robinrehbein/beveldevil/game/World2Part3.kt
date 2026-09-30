package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

private val hidden = Glyph(spike = true, hidden = true)
private val ghost = Glyph(spike = false, hidden = true, bonk = true)
private val hiddenSolid = Glyph(spike = false, hidden = true)

/** World 2, levels 33-48: "Network". */
internal val world2Part3: List<Level> = listOf(
    // 33 — EASTER EGG: ping 127.0.0.1 (ping, then pong)
    Level(
        name = T("Ping Pong", "Ping Pong"),
        intro = T("ping 127.0.0.1: 64 bytes, time<1ms. Pong comes later.", "ping 127.0.0.1: 64 Bytes, Zeit<1ms. Pong kommt später."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(6f), Play(Card.DEVIL_SAW), Saw(-1.5f, 14.4f, 5.5f, 0f), say("Ping!", "Ping!")),
            trap(PastX(6f), Saw(33.5f, 14.4f, -5.5f, 0f), say("Pong!", "Pong!"), delay = 1.6f),
            trap(Touch('r'), Show('A'), say("Packet dropped.", "Paket verworfen.")),
        ),
    ) {
        border(); floor()
        rack(14, 2, 2); fill(21..22, 13..14, 'r')
        put(21, 12, 'A'); put(22, 12, 'A')
        spawn(); door(); bits(33)
    },

    // 34 — EASTER EGG: "It's always DNS" (the door resolves to the wrong address, twice)
    Level(
        name = T("It's Always DNS", "Es ist immer DNS"),
        intro = T("nslookup door. Non-authoritative answer: somewhere.", "nslookup tuer. Nicht autorisierende Antwort: irgendwo."),
        traps = doorTrail(
            PastX(25f), 29, 14,
            listOf(DoorTo(29, 1, 26f, hanging = true), DoorTo(5, 1, 26f, hanging = true), DoorTo(5, 14, 26f)),
            first = listOf(Play(Card.SHY_DOOR), say("DNS: resolved to 5.0.0.0", "DNS: aufgelöst zu 5.0.0.0")),
        ) + doorTrail(
            BeforeX(8.5f), 5, 14,
            listOf(DoorTo(5, 1, 26f, hanging = true), DoorTo(20, 1, 26f, hanging = true), DoorTo(20, 14, 26f)),
            first = listOf(say("DNS cache poisoned. Now it's at 20.0.0.0", "DNS-Cache vergiftet. Jetzt bei 20.0.0.0")),
        ),
    ) {
        border(); floor()
        rack(16, 2, 2); rack(23, 2, 2); rack(8, 1, 2)
        spawn(12); door()
    },

    // 35 — EASTER EGG: HTTP 504 Gateway Timeout (the door loads for five seconds)
    Level(
        name = T("504 Gateway Timeout", "504 Gateway-Timeout"),
        intro = T("The door is loading. Please stand by.", "Die Tür lädt noch. Bitte warten."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(After(2.6f), Saw(-1.5f, 14.4f, 6f, 0f)),
            trap(After(3.6f), Saw(33.5f, 14.4f, -6f, 0f)),
            trap(After(4.6f), Saw(-1.5f, 14.4f, 6f, 0f)),
            trap(After(5.5f), Play(Card.SHY_DOOR), DoorTo(29, 14, speed = 18f), Show('A'), say("504: the upstream server timed out. Try again later.", "504: Der Upstream-Server hat nicht geantwortet. Später nochmal.")),
        ),
    ) {
        border(); floor()
        fill(23..26, 13..14)
        put(28, 14, 'A'); put(27, 14, '.')
        spawn(); put(29, 3, 'D'); fill(28..30, 4..4)
    },

    // 36 — EASTER EGG: packet loss (the platforms blink like a bad connection)
    Level(
        name = T("Packet Loss", "Paketverlust"),
        intro = T("64 packets transmitted, 22 received, 65% packet loss.", "64 Pakete gesendet, 22 empfangen, 65 % Paketverlust."),
        traps = blink('a', 0.6f, 0.9f, 1.8f, 6, first = listOf(Play(Card.GHOST_BLOCK), say("Request timed out.", "Zeitüberschreitung der Anforderung."))) +
            blink('b', 1.2f, 0.9f, 1.8f, 6) + blink('c', 1.8f, 0.9f, 1.8f, 6) + blink('d', 1.0f, 0.9f, 1.8f, 6),
    ) {
        border()
        fill(0..5, 15..17); fill(27..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(12..14, 15..15, 'b'); fill(17..19, 15..15, 'c'); fill(22..24, 15..15, 'd')
        spawn(); door(); bits(36)
    },

    // 37 — EASTER EGG: Ping of Death (the packet is way too large)
    Level(
        name = T("Ping of Death", "Ping des Todes"),
        intro = T("ping -s 65510. Your packet is very, very large.", "ping -s 65510. Dein Paket ist sehr, sehr groß."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(6f), Play(Card.DEVIL_SAW), Saw(-2f, 14f, 5f, 0f, 1.6f), say("Oversized packet incoming.", "Übergroßes Paket im Anflug.")),
            trap(PastX(6f), Saw(35f, 14f, -5f, 0f, 1.6f), delay = 2.4f, ),
            trap(PastX(21.5f), Show('A')),
        ),
    ) {
        border(); floor()
        fill(8..9, 13..14); fill(11..20, 12..12)
        put(24, 14, 'A'); put(25, 14, 'A')
        spawn(); door(); bits(37)
    },

    // 38 — EASTER EGG: traceroute (* * * Request timed out, the door hops)
    Level(
        name = T("Traceroute", "Traceroute"),
        intro = T("1  door (12.0.0.1)  0.4 ms", "1  tuer (12.0.0.1)  0,4 ms"),
        traps = listOf(
            trap(PastX(8f), Play(Card.SHY_DOOR), DoorTo(18, 14, speed = 30f), say("2  hop (18.0.0.1)  1.1 ms", "2  Hop (18.0.0.1)  1,1 ms")),
            trap(PastX(14.5f), DoorTo(24, 14, speed = 30f), say("3  hop (24.0.0.1)  2.3 ms", "3  Hop (24.0.0.1)  2,3 ms")),
            trap(PastX(20.5f), DoorTo(29, 14, speed = 30f), say("4  hop (29.0.0.1)  4.2 ms", "4  Hop (29.0.0.1)  4,2 ms")),
        ) + doorTrail(
            PastX(26f), 29, 14,
            listOf(DoorTo(29, 1, 26f, hanging = true), DoorTo(3, 1, 26f, hanging = true), DoorTo(3, 14, 26f)),
            first = listOf(say("5  * * *  Request timed out.", "5  * * *  Zeitüberschreitung.")),
        ),
    ) {
        border(); floor()
        rack(10, 1, 2); rack(16, 1, 2); rack(22, 1, 2)
        spawn(); door(12)
    },

    // 39 — EASTER EGG: firewall (iptables -A INPUT -j DROP)
    Level(
        name = T("Firewall", "Firewall"),
        intro = T("iptables -A INPUT -j DROP. Ports open in windows.", "iptables -A INPUT -j DROP. Ports öffnen sich zeitweise."),
        traps = blink('W', 1.6f, 0.9f, 99f, 1, first = listOf(Play(Card.SPIKE_SEED), say("Port 80 open. For a moment.", "Port 80 offen. Für einen Moment."))) +
            blink('X', 2.6f, 0.9f, 99f, 1) + blink('Y', 3.6f, 0.9f, 99f, 1),
    ) {
        border(); floor()
        fill(11..11, 8..14, 'W'); fill(18..18, 8..14, 'X'); fill(25..25, 8..14, 'Y')
        spawn(); door(); bits(39)
    },

    // 40 — EASTER EGG: DDoS (Distributed Denial of Stairs)
    Level(
        name = T("DDoS", "DDoS"),
        intro = T("Distributed Denial of Stairs.", "Distributed Denial of Stairs."),
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
        fill(8..9, 3..4, 'c'); fill(12..13, 3..4, 'd'); fill(15..16, 3..4, 'e')
        fill(19..20, 3..4, 'f'); fill(22..23, 3..4, 'g'); fill(26..27, 3..4, 'h')
        spawn(); door(); bits(40, x0 = 24, y = 1)
    },

    // 41 — EASTER EGG: man in the middle (you end up in a cell)
    Level(
        name = T("Man in the Middle", "Mann in der Mitte"),
        intro = T("Your connection is encrypted. Mostly by me.", "Deine Verbindung ist verschlüsselt. Größtenteils von mir."),
        legend = mapOf('b' to ghost),
        traps = listOf(
            trap(PastX(15.8f), Play(Card.HEADBUTT), Fall('l'), Fall('r'), say("Hello. I'm between you and the door.", "Hallo. Ich bin zwischen dir und der Tür.")),
            trap(Touch('b'), say("Certificate valid. (It isn't.)", "Zertifikat gültig. (Ist es nicht.)")),
        ),
    ) {
        border(); floor()
        fill(13..14, 3..5, 'l'); fill(21..22, 3..5, 'r')
        put(18, 13, 'b')
        spawn(); door(); bits(41)
    },

    // 42 — EASTER EGG: TCP three-way handshake (SYN, SYN-ACK, ACK)
    Level(
        name = T("Three-Way Handshake", "Dreifach-Handschlag"),
        intro = T("SYN. Now go back and say SYN-ACK.", "SYN. Jetzt geh zurück und sag SYN-ACK."),
        legend = mapOf('q' to hiddenSolid, 'B' to hiddenSolid, 'A' to hidden),
        traps = listOf(
            trap(Touch('p'), Play(Card.GHOST_BLOCK), Show('q'), say("SYN. (Go back.)", "SYN. (Geh zurück.)")),
            trap(Touch('q'), Show('B'), say("SYN-ACK. Now the bridge.", "SYN-ACK. Jetzt die Brücke.")),
            trap(Touch('B'), Show('A'), say("ACK. Connection established. And spiked.", "ACK. Verbindung steht. Und bespikt.")),
        ),
    ) {
        border()
        fill(0..11, 15..17); fill(19..31, 15..17)
        put(6, 14, 'p'); put(1, 14, 'q')
        fill(12..18, 15..15, 'B'); put(15, 14, 'A')
        spawn(3); door(); bits(42)
    },

    // 43 — EASTER EGG: reverse proxy (gravity, in reverse)
    Level(
        name = T("Reverse Proxy", "Reverse Proxy"),
        intro = T("All traffic goes the other way. Including you.", "Aller Verkehr läuft andersherum. Du auch."),
        traps = listOf(
            trap(PastX(7.5f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Proxying your gravity.", "Deine Schwerkraft wird weitergeleitet.")),
            trap(PastX(14.5f), Gravity(false)),
            trap(PastX(20.5f), Gravity(true), say("Reverse, reverse.", "Rückwärts, rückwärts.")),
            trap(PastX(27.4f), Gravity(false)),
        ),
    ) {
        border(); floor()
        leds(9..13); put(11, 1, 'v')
        leds(22..26); put(24, 1, 'v'); put(17, 1, 'v')
        spawn(); door()
    },

    // 44 — EASTER EGG: VPN tunnel ("nobody can see you die")
    Level(
        name = T("VPN Tunnel", "VPN-Tunnel"),
        intro = T("Your connection is secure. Nobody can see you die.", "Deine Verbindung ist sicher. Niemand sieht dich sterben."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(10f), Play(Card.SPIKE_SEED), Show('A'), say("Tunnel established.", "Tunnel aufgebaut.")),
            trap(PastX(18.5f), Fall('a'), say("Packet lost in the tunnel.", "Paket im Tunnel verloren.")),
        ),
    ) {
        border(); floor()
        fill(7..25, 1..12)
        put(13, 14, '^'); put(16, 14, 'A')
        fill(21..22, 15..17, 'a')
        spawn(); door(); bits(44, x0 = 24, y = 1)
    },

    // 45 — EASTER EGG: load balancer (the platforms take turns)
    Level(
        name = T("Load Balancer", "Lastverteiler"),
        intro = T("Traffic is distributed evenly. Down.", "Der Verkehr wird gleichmäßig verteilt. Nach unten."),
        traps = listOf(
            trap(Touch('a'), Play(Card.SINKING), Move('b', 0f, -2f, 3f), say("Node 2 is now online.", "Knoten 2 ist jetzt online.")),
            trap(Touch('a'), Move('a', 0f, 8f, 4f), delay = 0.9f),
            trap(Touch('b'), Move('c', 0f, -2f, 3f), say("Node 3 is now online.", "Knoten 3 ist jetzt online.")),
            trap(Touch('b'), Move('b', 0f, 8f, 4f), delay = 0.9f),
            trap(Touch('c'), Move('c', 0f, 8f, 4f), delay = 0.9f),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(25..31, 15..17)
        fill(7..9, 15..15, 'a'); fill(13..15, 17..17, 'b'); fill(19..21, 17..17, 'c')
        spawn(); door(); bits(45)
    },

    // 46 — EASTER EGG: cache invalidation (one of the two hard problems)
    Level(
        name = T("Cache Invalidation", "Cache-Invalidierung"),
        intro = T("There are only two hard problems in computer science.", "Es gibt nur zwei schwere Probleme in der Informatik."),
        legend = mapOf('H' to hiddenSolid),
        traps = listOf(
            trap(Touch('a'), Fall('a'), delay = 0.35f),
            trap(Touch('b'), Fall('b'), delay = 0.35f),
            trap(Touch('c'), Fall('c'), delay = 0.35f),
        ) + doorTrail(
            PastX(24.2f), 29, 14,
            listOf(DoorTo(29, 1, 26f, hanging = true), DoorTo(2, 1, 26f, hanging = true), DoorTo(2, 14, 26f)),
            first = listOf(Play(Card.SHY_DOOR), Show('H'), say("Cache invalidated. The door is stale.", "Cache invalidiert. Die Tür ist veraltet.")),
        ),
    ) {
        border()
        fill(0..7, 15..17); fill(25..31, 15..17)
        fill(9..10, 15..15, 'a'); fill(14..15, 15..15, 'b'); fill(19..21, 15..15, 'c')
        fill(19..21, 14..14, 'H'); fill(14..15, 14..14, 'H'); fill(9..10, 14..14, 'H')
        spawn(); door()
    },

    // 47 — EASTER EGG: zero-day (an exploit nobody has patched yet)
    Level(
        name = T("Zero-Day", "Zero-Day"),
        intro = T("No known vulnerabilities. Officially.", "Keine bekannten Schwachstellen. Offiziell."),
        legend = mapOf('A' to hidden, 'B' to hidden, 'C' to hidden),
        traps = listOf(
            trap(PastX(7f), Play(Card.SPIKE_SEED), Show('A'), say("CVE-2026-0000: unpatched.", "CVE-2026-0000: ungepatcht.")),
            trap(PastX(13f), Show('B')),
            trap(PastX(19f), Show('C')),
            trap(PastX(24.5f), Fall('a'), say("Patch Tuesday was yesterday.", "Patch-Dienstag war gestern.")),
        ),
    ) {
        border(); floor()
        put(9, 14, 'A'); put(15, 14, 'B'); put(16, 14, 'B'); put(21, 14, 'C'); put(22, 14, 'C'); put(23, 14, 'C')
        fill(26..28, 15..17, 'a')
        spawn(); door(30); bits(47, x0 = 24, y = 1)
    },

    // 48 — EASTER EGG: bandwidth (the tunnel is narrow)
    Level(
        name = T("Bandwidth Limit", "Bandbreiten-Limit"),
        intro = T("Your plan allows two tiles of height.", "Dein Tarif erlaubt zwei Kacheln Höhe."),
        traps = listOf(
            trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Throttled to 56k.", "Auf 56k gedrosselt."), delay = 0.2f),
            trap(Touch('b'), Fall('b'), delay = 0.2f),
            trap(Touch('c'), Fall('c'), delay = 0.2f),
        ),
    ) {
        border()
        fill(0..10, 15..17); fill(24..31, 15..17)
        fill(11..12, 15..15, 'a'); fill(14..15, 15..17); fill(16..17, 15..15, 'b'); fill(19..20, 15..17); fill(21..22, 15..15, 'c')
        fill(7..25, 1..12)
        spawn(); door()
    },
)
