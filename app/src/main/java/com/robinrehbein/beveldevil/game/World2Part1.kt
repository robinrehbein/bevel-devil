package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.Bluff
import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Portal
import com.robinrehbein.beveldevil.game.Action.Reroute
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.Airborne
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.Landed
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch

/** World 2, levels 1-16. Act 1, "Handshake": the best jokes of the data center, the first portals and the DNS trap. */
object World2Part1 {
    private val hidden = Glyph(spike = true, hidden = true)
    private val ghost = Glyph(spike = false, hidden = true, bonk = true)
    private val hiddenSolid = Glyph(spike = false, hidden = true)
    /** Two-tile pieces of the raised floor that erodes behind you in level 11, round 2. */
    private const val ERODE = "fghijkmn"

    val levels: List<Level> = listOf(

        // 1 — EASTER EGG: Hello, World! (the floor driver segfaults; landing drops the next piece; the landing after that grows spikes)
        Level(
            name = T("Hello, World!", "Hallo, Welt!"),
            intro = T("Print it, then walk to the door.", "Gib es aus, dann lauf zur Tür."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(10.6f), Play(Card.COLLAPSE), Fall('a'), say("Hello, World! Segfault in the floor driver.", "Hallo, Welt! Segfault im Boden-Treiber.")),
                trap(Landed(14.6f, 18.5f), Fall('b'), say("Hello again, World.", "Hallo nochmal, Welt.")),
                trap(Airborne(20f, 23.5f), Show('A'), say("Warning: 2 new spikes installed.", "Warnung: 2 neue Spikes installiert.")),
            ),
            // rematch: the first pit is a bluff now; whoever hops it anyway lands in fresh spikes, the second pit drops early
            rematch = listOf(
                Round(
                    T("Retransmit. Same text, new bugs.", "Nochmal gesendet. Diesmal mit Bonus-Bugs."),
                    legend = mapOf('C' to hidden),
                    traps = listOf(
                        trap(PastX(10.6f), Bluff(Card.COLLAPSE)),
                        trap(Airborne(11.2f, 14.6f), Show('C'), say("Jumped to conclusions.", "Voreilig gesprungen.")),
                        trap(PastX(19.2f), Fall('b'), say("Goodbye, World.", "Tschüss, Welt.")),
                        trap(Airborne(20f, 23.5f), Show('A')),
                    ),
                ) { put(15, 14, 'C'); put(16, 14, 'C') },
            ),
        ) {
            border(); floor()
            fill(12..14, 15..17, 'a'); fill(20..22, 15..17, 'b')
            put(25, 14, 'A'); put(26, 14, 'A')
            spawn(); door(); bits(1)
        },

        // 2 — first portal: the wall is a firewall, port 80 is open. The hop over the first spike grows one more before the portal,
        // and the landing behind the second spike grows a pair in front of the door
        Level(
            name = T("Open Port", "Offener Port"),
            intro = T("The firewall is in a bad mood today. Like me.", "Die Firewall hat heute schlechte Laune. Wie ich."),
            legend = mapOf('A' to hidden, 'B' to hidden, 'C' to hidden),
            start = listOf(Portal('1', 10 to 14, 19 to 14)),
            traps = listOf(
                trap(Airborne(4.6f, 7.4f), Play(Card.SPIKE_SEED), Show('A'), say("Port 80 open. Port 9: a spike.", "Port 80 offen. Port 9: ein Spike.")),
                trap(Landed(23.5f, 27.6f), Show('B'), say("Packet loss: two spikes in transit.", "Paketverlust: zwei Spikes unterwegs.")),
            ),
            // rematch: the way to the portal stays clean; the spikes grow right behind the port, where round 1 landed safely
            rematch = listOf(
                Round(
                    T("Port closed. Knocking on another one.", "Port zu. Ich klopf an einem anderen."),
                    start = listOf(Portal('1', 10 to 14, 19 to 14)),
                    traps = listOf(
                        trap(Airborne(4.6f, 7.4f), say("Port 9? Closed for lunch.", "Port 9? Mittagspause.")),
                        trap(Airborne(20f, 23.4f), Play(Card.SPIKE_SEED), Show('C'), say("Port scan. Different port.", "Portscan. Falscher Port, richtige Spikes.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(14..15, 1..14)
            put(6, 14, '^'); put(23, 14, '^')
            put(9, 14, 'A')
            put(26, 14, 'C'); put(27, 14, 'C')
            put(28, 14, 'B'); put(29, 14, 'B')
            spawn(); door(30); bits(2)
        },

        // 3 — EASTER EGG: HTTP 404, the door is gone (it runs back to the start). The hop over the second rack lands in spikes,
        // and the way back has a spike behind the start that only shows when you come running
        Level(
            name = T("Reception", "Empfang"),
            intro = T("One moment please. Connecting you.", "Einen Moment bitte. Wir verbinden."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            traps = listOf(
                trap(Airborne(19f, 21.5f), Show('B'), say("Hold music: a spike.", "Warteschleifenmusik: ein Spike.")),
                trap(BeforeX(6.5f), Show('A'), say("Have you tried turning it off and on again?", "Schon mal aus- und wieder eingeschaltet?")),
            ) + doorTrail(
                PastX(22f), 29, 14,
                listOf(DoorTo(29, 1, 24f, hanging = true), DoorTo(1, 1, 24f, hanging = true), DoorTo(1, 14, 24f)),
                first = listOf(Play(Card.SHY_DOOR), say("404: Door not found. Try /dev/left.", "404: Tür nicht gefunden. Versuch /dev/left.")),
            ),
        ) {
            border(); floor()
            rack(15, 2, 2); rack(20, 2, 2)
            put(3, 14, 'A'); put(27, 14, 'B'); put(28, 14, 'B')
            spawn(10); door(); bits(3)
        },

        // 4 — EASTER EGG: off-by-one (the safe gap in the LED row is index 14, and it moves; the hop over the rack lands in spikes; the door steps aside)
        Level(
            name = T("String Lights", "Lichterkette"),
            intro = T("Nice lighting. I laid the cables myself.", "Schönes Licht hier. Ich habe die Kabel selbst verlegt."),
            legend = mapOf('A' to hidden, 'B' to hidden),
            traps = listOf(
                trap(PastX(11.6f), Play(Card.SPIKE_SEED), Show('A'), say("Off by one. Classic.", "Um eins daneben. Klassiker.")),
                trap(Airborne(20.4f, 22.6f), Show('B'), say("Buffer overflow: two more.", "Pufferüberlauf: noch zwei.")),
                trap(PastX(24.8f), DoorTo(28, 14, speed = 30f), say("You counted from zero, I counted from one.", "Du hast bei null angefangen, ich bei eins.")),
            ),
            // rematch: gap 14 stays open this time, the other gap gets plugged while you fly at it
            rematch = listOf(
                Round(
                    T("Packet lost. Sending it again.", "Paket verloren, nochmal. Wie bei der Post."),
                    legend = mapOf('C' to hidden),
                    traps = listOf(
                        trap(PastX(11.6f), say("Off by one. Again?", "Um eins daneben. Schon wieder?")),
                        trap(Airborne(12.6f, 15.4f), Play(Card.SPIKE_SEED), Show('C'), say("Now I count from two.", "Jetzt zähle ich ab zwei.")),
                        trap(Airborne(20.4f, 22.6f), Show('B')),
                        trap(PastX(24.8f), DoorTo(28, 14, speed = 30f)),
                    ),
                ) { put(16, 14, 'C') },
            ),
        ) {
            border(); floor()
            put(13, 14, '^'); put(15, 14, '^'); put(17, 14, '^'); put(14, 14, 'A')
            rack(21, 2, 2)
            put(25, 14, 'B'); put(26, 14, 'B')
            spawn(); door(27); bits(4)
        },

        // 5 — EASTER EGG: NullPointerException (the bonk block is real; landing behind the wall drops the floor; the hop over that lands in spikes)
        Level(
            name = T("Null Pointer", "Nullzeiger"),
            intro = T("Nothing there. Literally.", "Da ist nichts. Wortwörtlich."),
            legend = mapOf('b' to Glyph(spike = false, hidden = true, bonk = true), 'A' to hidden, 'B' to hidden),
            traps = listOf(
                trap(Touch('b'), Play(Card.GHOST_BLOCK), Show('A'), say("NullPointerException: block is null. It was there all along.", "NullPointerException: Block ist null. War aber die ganze Zeit da.")),
                trap(Landed(19f, 21.6f), Fall('f'), say("Garbage collected.", "Vom Garbage Collector abgeholt."), delay = 0.08f),
                trap(Airborne(23.2f, 25.6f), Show('B'), say("Use after free.", "Use after free.")),
            ),
        ) {
            border(); floor()
            rack(17, 2, 3)
            put(15, 13, 'b')
            put(17, 11, 'A'); put(18, 11, 'A')
            fill(22..24, 15..17, 'f')
            put(27, 14, 'B'); put(28, 14, 'B')
            spawn(); door(); bits(5)
        },

        // 6 — EASTER EGG: "It's always DNS" (Reroute: the portal's exit moves onto spikes, the floating block flushes the cache; behind the exit grows a last pair)
        Level(
            name = T("Address Book", "Adressbuch"),
            intro = T("I looked up the door. It's in the phone book.", "Ich habe die Tür nachgeschlagen. Steht im Telefonbuch."),
            legend = mapOf('A' to hidden),
            start = listOf(Portal('1', 8 to 14, 17 to 14)),
            traps = listOf(
                trap(PastX(6f), Play(Card.DECOY), Reroute('1', 5 to 3), say("DNS changed. The portal leads somewhere nicer now.", "DNS geändert. Das Portal führt jetzt an einen schöneren Ort.")),
                trap(Touch('s'), Reroute('1', 17 to 14), say("ipconfig /flushdns: cache cleared. Try again.", "ipconfig /flushdns: Cache geleert. Versuch's nochmal.")),
                trap(PastX(19.5f), Show('A'), say("Cache poisoned.", "Cache vergiftet.")),
            ),
            // rematch: the DNS stays put, and flushing the cache is what poisons it now
            rematch = listOf(
                Round(
                    T("New lease. Same address. Probably.", "DHCP sagt: gleiche Adresse. DHCP lügt gern."),
                    start = listOf(Portal('1', 8 to 14, 17 to 14)),
                    traps = listOf(
                        trap(PastX(6f), say("DNS changed. Or did it?", "DNS geändert. Oder doch nicht?")),
                        trap(Touch('s'), Play(Card.DECOY), Reroute('1', 5 to 3), say("Flushed. Into the spikes.", "Geleert. Direkt in die Spikes.")),
                        trap(PastX(19.5f), Show('A'), say("TTL expired: spikes.", "TTL abgelaufen: Spikes.")),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(12..12, 1..14)
            fill(3..8, 5..5); fill(3..8, 4..4, '^')
            fill(2..3, 12..12, 's')
            put(23, 14, 'A'); put(24, 14, 'A')
            spawn(); door(); bits(6)
        },

        // 7 — EASTER EGG: Blue Screen of Death (stop code 0x7B: inaccessible boot device)
        Level(
            name = T("Sky Blue", "Himmelblau"),
            intro = T("Nice ceiling. Very stable.", "Schöne Decke. Sehr stabil."),
            traps = listOf(
                trap(PastX(7.4f), Play(Card.HEADBUTT), Fall('c'), say("STOP: 0x0000007B. Your ceiling has crashed.", "STOP: 0x0000007B. Deine Decke ist abgestürzt.")),
                trap(PastX(14.3f), Fall('d')),
                trap(PastX(21.6f), Fall('e'), say("Collecting error info: 100%. Dying now.", "Fehlerinfo sammeln: 100 %. Sterbe jetzt.")),
            ),
            // rematch: the first ceiling comes down late, on whoever waits for it under it
            rematch = listOf(
                Round(
                    T("Rebooting. Your ceiling is up to date.", "Neustart. Deine Decke ist auf dem neuesten Stand."),
                    traps = listOf(
                        trap(PastX(9.3f), Play(Card.HEADBUTT), Fall('c'), say("Delayed write failed.", "Verzögertes Schreiben. Auf dich.")),
                        trap(PastX(14.3f), Fall('d')),
                        trap(PastX(21.6f), Fall('e')),
                    ),
                ),
            ),
        ) {
            border(); floor()
            fill(9..10, 4..5, 'c'); fill(16..17, 4..5, 'd'); fill(23..24, 4..5, 'e')
            spawn(); door(); bits(7)
        },

        // 8 — EASTER EGG: RAM memory test (POST counts up, never finishes)
        Level(
            name = T("Memory Test", "Speichertest"),
            intro = T("POST: 640K ought to be enough for anybody.", "POST: 640K sollten für jeden reichen."),
            traps = listOf(
                trap(Touch('b'), Play(Card.SINKING), Move('b', 0f, 12f, 7f), say("RAM check: 3 of 4 blocks OK.", "RAM-Check: 3 von 4 Blöcken OK."), delay = 0.15f),
                trap(Touch('a'), Fall('a'), delay = 0.3f),
                trap(Touch('c'), Fall('c'), delay = 0.2f),
            ),
        ) {
            border()
            fill(0..5, 15..17); fill(26..31, 15..17)
            fill(7..8, 15..15, 'a'); fill(11..13, 14..14, 'b'); fill(17..19, 15..15, 'c'); fill(23..24, 14..14)
            spawn(); door(); bits(8)
        },

        // 9 — EASTER EGG: hot swap (unplug the controls, plug them back in wrong; the hop off the last stone lands in spikes)
        Level(
            name = T("Cable Mess", "Kabelsalat"),
            intro = T("Everything is plugged in tight. I checked.", "Alles steckt fest. Ich habe nachgesehen."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(PastX(6.5f), Play(Card.TWISTED), Swap(true), say("Hot swap: left and right exchanged.", "Hot Swap: links und rechts getauscht.")),
                trap(PastX(23f), Swap(false), say("Kernel reloaded the driver.", "Kernel hat den Treiber neu geladen.")),
                trap(Airborne(21.8f, 24.4f), Show('A'), say("Driver signed by nobody.", "Treiber von niemandem signiert.")),
            ),
            // rematch: the cable stays put before the LEDs and comes loose while you land on the second stone
            rematch = listOf(
                Round(
                    T("Unplugged it. Plugged it back in. Mostly.", "Ausgesteckt, wieder eingesteckt. Größtenteils."),
                    traps = listOf(
                        trap(Airborne(13.8f, 15.4f), Play(Card.TWISTED), Swap(true), say("Plug and pray.", "Plug and Pray.")),
                        trap(PastX(23f), Swap(false)),
                        trap(Airborne(21.8f, 24.4f), Show('A')),
                    ),
                ),
            ),
        ) {
            border(); floor()
            leds(8..23)
            fill(10..11, 14..14); fill(15..16, 14..14); fill(20..21, 14..14)
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door(); bits(9)
        },

        // 10 — a portal hangs in mid-air above an LED field; hidden spikes wait where you land, and a saw waits at the door
        Level(
            name = T("VPN Tunnel", "VPN-Tunnel"),
            intro = T("Your connection is secure. Really secure.", "Deine Verbindung ist sicher. Wirklich sicher."),
            legend = mapOf('A' to hidden, 'C' to hidden),
            start = listOf(Portal('1', 10 to 12, 21 to 12)),
            traps = listOf(
                trap(Landed(21f, 23.8f), Play(Card.SPIKE_SEED), Show('A'), say("Tunnel established. Spikes included.", "Tunnel steht. Spikes inklusive.")),
                trap(Airborne(25.6f, 28.6f), Saw(33.5f, 14.4f, -5f, 0f), say("Packet loss. Incoming.", "Paketverlust. Kommt rein.")),
            ),
            // rematch: the spikes grow right where the tunnel drops you, while you are still falling
            rematch = listOf(
                Round(
                    T("Tunnel rebuilt. Shorter. Pointier.", "Tunnel neu gebaut. Kürzer. Spitzer."),
                    start = listOf(Portal('1', 10 to 12, 21 to 12)),
                    traps = listOf(
                        trap(Airborne(20.8f, 23.5f), Play(Card.SPIKE_SEED), Show('C'), say("Split tunnel. Split spikes.", "Geteilter Tunnel. Geteilte Spikes.")),
                        trap(Airborne(25.6f, 28.6f), Saw(33.5f, 14.4f, -5f, 0f)),
                    ),
                ),
            ),
        ) {
            border(); floor()
            leds(9..20)
            put(24, 14, 'C'); put(25, 14, 'C')
            put(26, 14, 'A'); put(27, 14, 'A')
            spawn(); door(); bits(10)
        },

        // 11 — EASTER EGG: fan #3 failed (cooling is a saw problem)
        Level(
            name = T("Server Room", "Serverraum"),
            intro = T("Nice and cool in here. Three fans, all fit.", "Schön kühl hier. Drei Lüfter, alle fit."),
            traps = listOf(
                trap(PastX(9.5f), Play(Card.DEVIL_SAW), Saw(15.5f, 2f, 0f, 9f), say("Fan 1 of 3 spinning. Rather fast.", "Lüfter 1 von 3 dreht. Ziemlich schnell.")),
                trap(PastX(15.2f), Saw(20.5f, 19f, 0f, -9f), say("Fan 2 spins from below. It's a feature.", "Lüfter 2 dreht von unten. Ist ein Feature.")),
                trap(PastX(21.6f), Saw(26.5f, 2f, 0f, 9f), say("Fan 3: REPLACE. (I meant it.)", "Lüfter 3: TAUSCHEN. (Ernst gemeint.)")),
            ),
            // rematch: same fans, but the raised floor erodes right behind you: backing off to let a saw pass is no longer an option
            rematch = listOf(
                Round(
                    T("Fans replaced. The floor was in the way.", "Lüfter getauscht. Der Boden war im Weg."),
                    traps = listOf(
                        trap(PastX(9.5f), Play(Card.DEVIL_SAW), Saw(15.5f, 2f, 0f, 9f), say("Raised floor, lowered expectations.", "Doppelboden, einfacher Abgang.")),
                        trap(PastX(15.2f), Saw(20.5f, 19f, 0f, -9f)),
                        trap(PastX(21.6f), Saw(26.5f, 2f, 0f, 9f), say("No way back. Only forward.", "Kein Zurück. Nur noch vorwärts.")),
                    ) + ERODE.mapIndexed { i, g -> trap(PastX(12.6f + 2 * i), Fall(g), delay = 0.1f) },
                ) { ERODE.forEachIndexed { i, g -> fill(10 + 2 * i..11 + 2 * i, 15..17, g) } },
            ),
        ) {
            border(); floor()
            fill(13..17, 1..2); fill(19..22, 1..2); fill(25..28, 1..2)
            spawn(); door(); bits(11, x0 = 4)
        },

        // 12 — EASTER EGG: Segmentation fault (core dumped)
        Level(
            name = T("Address Space", "Adressraum"),
            intro = T("Everyone gets their own space. You too.", "Jeder bekommt seinen eigenen Platz. Du auch."),
            legend = mapOf('A' to hidden),
            traps = listOf(
                trap(Touch('a'), Play(Card.CRUMBLE), Fall('a'), say("Segmentation fault (core dumped)", "Speicherzugriffsfehler (Speicherabbild geschrieben)"), delay = 0.28f),
                trap(PastX(21f), Show('A')),
            ) + ('b'..'f').map { g -> trap(Touch(g), Fall(g), delay = 0.28f) },
        ) {
            border()
            fill(0..4, 15..17); fill(23..30, 15..17)
            for (i in 0..5) {
                val x = 5 + i * 3
                fill(x..x + 2, (if (i % 2 == 0) 15 else 14)..(if (i % 2 == 0) 15 else 14), 'a' + i)
            }
            put(25, 14, 'A'); put(26, 14, 'A')
            spawn(); door(); bits(12)
        },

        // 13 — EASTER EGG: "works on my machine" (works until it's deployed to production; the ceiling grows a third spike where you land)
        Level(
            name = T("Works on My Machine", "Läuft bei mir"),
            intro = T("Tested locally. Green everywhere.", "Lokal getestet. Überall grün."),
            legend = mapOf('C' to Glyph(spike = true, dir = Dir.DOWN, hidden = true)),
            traps = listOf(
                trap(PastX(8.8f), Play(Card.UPSIDE_DOWN), Gravity(true), say("Production has different gravity.", "Produktion hat eine andere Schwerkraft.")),
                trap(Landed(12.5f, 16.5f), Show('C'), say("Hotfix deployed.", "Hotfix eingespielt.")),
                trap(PastX(25f), Gravity(false), say("Works on my machine!", "Läuft bei mir!")),
            ),
        ) {
            border(); floor()
            leds(11..24)
            put(19, 1, 'v'); put(22, 1, 'v')
            put(17, 1, 'C'); put(18, 1, 'C')
            leds(26..27)
            spawn(); door()
        },

        // 14 — EASTER EGG: 127.0.0.1 (loopback: the portal sends you home; jumping over it is the `break`; the landing behind it grows spikes)
        Level(
            name = T("127.0.0.1", "127.0.0.1"),
            intro = T("Please take off your shoes. Somebody lives here.", "Bitte Schuhe ausziehen. Hier wohnt jemand."),
            legend = mapOf('A' to hidden),
            start = listOf(Portal('1', 15 to 14, 4 to 14, twoWay = false)),
            traps = listOf(
                trap(Landed(12f, 15f), Show('A'), say("The loopback has a guest book.", "Das Loopback hat ein Gästebuch.")),
                trap(PastX(17.5f), Play(Card.DEVIL_SAW), Saw(33.5f, 14.4f, -6f, 0f), say("ping 127.0.0.1: reply from 127.0.0.1. That was me.", "ping 127.0.0.1: Antwort von 127.0.0.1. Das war ich.")),
            ),
            // rematch: the loopback now forwards to the door side, and jumping over it (last round's `break`) is what spikes you
            rematch = listOf(
                Round(
                    T("Connection reset by peer. The peer is me.", "Verbindung zurückgesetzt. Von mir, natürlich."),
                    start = listOf(Portal('1', 15 to 14, 22 to 14, twoWay = false)),
                    legend = mapOf('C' to hidden),
                    traps = listOf(
                        trap(Airborne(14.6f, 16.8f), Play(Card.SPIKE_SEED), Show('C'), say("break; is deprecated.", "break; ist veraltet.")),
                        trap(PastX(23f), say("Port forwarding. You're welcome.", "Portweiterleitung. Gern geschehen.")),
                    ),
                ) { put(18, 14, 'C'); put(19, 14, 'C') },
            ),
        ) {
            border(); floor()
            rack(10, 1, 2)
            put(16, 14, 'A'); put(17, 14, 'A')
            spawn(); door(); bits(14)
        },

        // 15 — EASTER EGG: TCP three-way handshake (SYN, SYN-ACK, ACK)
        Level(
            name = T("Greeting", "Begrüßung"),
            intro = T("Politeness is free, they say.", "Höflichkeit kostet nichts, sagt man."),
            legend = mapOf('q' to hiddenSolid, 'B' to hiddenSolid, 'A' to hidden),
            traps = listOf(
                trap(Touch('p'), Play(Card.GHOST_BLOCK), Show('q'), say("SYN. (Go back.)", "SYN. (Geh zurück.)")),
                trap(Touch('q'), Show('B'), say("SYN-ACK. Now the bridge.", "SYN-ACK. Jetzt die Brücke.")),
                trap(Touch('B'), Show('A'), say("ACK. Connection established. And spiked.", "ACK. Verbindung steht. Und bespikt.")),
            ),
            // rematch: TCP Fast Open, the bridge comes at once; no spike on it, but the old hop over the spike lands in new ones
            rematch = listOf(
                Round(
                    T("Handshake failed. Your palms were sweaty.", "Handshake fehlgeschlagen, neuer Versuch. Hände waschen."),
                    legend = mapOf('C' to hidden),
                    traps = listOf(
                        trap(Touch('p'), Play(Card.GHOST_BLOCK), Show('B'), say("Fast open. No time for manners.", "Fast Open. Keine Zeit für Manieren.")),
                        trap(Touch('B'), say("ACK. Spikes? Who said spikes?", "ACK. Spikes? Wer redet von Spikes?")),
                        trap(Airborne(13.4f, 16.6f), Show('C'), say("Duplicate ACK.", "Doppeltes ACK.")),
                    ),
                ) { put(17, 14, 'C'); put(18, 14, 'C') },
            ),
        ) {
            border()
            fill(0..11, 15..17); fill(19..31, 15..17)
            put(6, 14, 'p'); put(1, 14, 'q')
            fill(12..18, 15..15, 'B'); put(15, 14, 'A')
            spawn(3); door(); bits(15)
        },

        // 16 — EASTER EGG: man in the middle (you end up in a cell; the secret block is your way out, the landing behind the wall grows spikes)
        Level(
            name = T("Through Traffic", "Durchgangsverkehr"),
            intro = T("Your connection is encrypted. Mostly by me.", "Deine Verbindung ist verschlüsselt. Größtenteils von mir."),
            legend = mapOf('b' to ghost, 'A' to hidden),
            traps = listOf(
                trap(PastX(15.8f), Play(Card.HEADBUTT), Fall('l'), Fall('r'), say("Hello. I'm between you and the door.", "Hallo. Ich bin zwischen dir und der Tür.")),
                trap(Touch('b'), say("Certificate valid. (It isn't.)", "Zertifikat gültig. (Ist es nicht.)")),
                trap(Landed(23f, 26.4f), Show('A'), say("Man in the middle: spikes.", "Man in the Middle: Spikes.")),
            ),
            // rematch: no cell this time; the right wall comes down late, on whoever runs under it
            rematch = listOf(
                Round(
                    T("Session expired. The cookies were delicious.", "Sitzung abgelaufen. Bitte nochmal Ausweis zeigen."),
                    traps = listOf(
                        trap(PastX(15.8f), say("Man in the middle? Never met him.", "Man in the Middle? Kenn ich nicht.")),
                        trap(PastX(19.4f), Play(Card.HEADBUTT), Fall('r'), say("Late binding.", "Spätes Binden.")),
                        trap(Landed(23f, 26.4f), Show('A')),
                    ),
                ) { fill(21..22, 3..4, '.') },
            ),
        ) {
            border(); floor()
            fill(13..14, 3..5, 'l'); fill(21..22, 3..5, 'r')
            put(18, 13, 'b')
            put(27, 14, 'A'); put(28, 14, 'A')
            spawn(); door(); bits(16)
        },
    )
}
