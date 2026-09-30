package com.robinrehbein.beveldevil.game

import com.robinrehbein.beveldevil.game.Action.DoorTo
import com.robinrehbein.beveldevil.game.Action.Fall
import com.robinrehbein.beveldevil.game.Action.Gravity
import com.robinrehbein.beveldevil.game.Action.Hide
import com.robinrehbein.beveldevil.game.Action.Move
import com.robinrehbein.beveldevil.game.Action.Play
import com.robinrehbein.beveldevil.game.Action.Saw
import com.robinrehbein.beveldevil.game.Action.Show
import com.robinrehbein.beveldevil.game.Action.Swap
import com.robinrehbein.beveldevil.game.Trigger.After
import com.robinrehbein.beveldevil.game.Trigger.BeforeX
import com.robinrehbein.beveldevil.game.Trigger.PastX
import com.robinrehbein.beveldevil.game.Trigger.Touch
import com.robinrehbein.beveldevil.game.Trigger.Zone

private val hidden = Glyph(spike = true, hidden = true)
private val hiddenSolid = Glyph(spike = false, hidden = true)
private val ghost = Glyph(spike = false, hidden = true, bonk = true)

/** World 2, levels 81-96: "HTTP Status Codes". */
internal val world2Part6: List<Level> = listOf(
    // 81 — EASTER EGG: HTTP 100 Continue
    Level(
        name = T("100 Continue", "100 Fortfahren"),
        intro = T("Expect: 100-continue. Keep climbing.", "Expect: 100-continue. Klettere weiter."),
        legend = mapOf('b' to hiddenSolid, 'c' to hiddenSolid, 'd' to hiddenSolid, 'e' to hiddenSolid),
        traps = listOf(
            trap(Touch('a'), Play(Card.GHOST_BLOCK), Show('b'), say("100 Continue. (I said continue, not survive.)", "100 Continue. (Ich sagte fortfahren, nicht überleben.)")),
            trap(Touch('b'), Show('c')),
            trap(Touch('c'), Show('d')),
            trap(Touch('d'), Show('e'), Fall('d'), say("100 Continue... continue... continue...", "100 Continue... weiter... weiter..."), delay = 0.6f),
        ),
    ) {
        border(); floor()
        fill(6..7, 13..13, 'a'); fill(10..11, 11..11, 'b'); fill(14..15, 9..9, 'c'); fill(18..19, 7..7, 'd'); fill(22..25, 5..5, 'e')
        spawn(); put(24, 4, 'D')
    },

    // 82 — EASTER EGG: HTTP 101 Switching Protocols
    Level(
        name = T("101 Switching Protocols", "101 Protokollwechsel"),
        intro = T("Upgrade: swap. Connection: upgrade.", "Upgrade: swap. Connection: upgrade."),
        traps = listOf(
            trap(Touch('a'), Play(Card.TWISTED), Swap(true), say("101: switching to the mirror protocol.", "101: Wechsel zum Spiegel-Protokoll.")),
            trap(Touch('b'), Swap(false), say("101: switching back. Or not.", "101: Wechsel zurück. Oder nicht.")),
            trap(Touch('c'), Swap(true)),
            trap(Touch('d'), Swap(false), say("Protocol switched. Enjoy HTTP/3.", "Protokoll gewechselt. Viel Spaß mit HTTP/3.")),
        ),
    ) {
        border(); floor()
        leds(6..24)
        fill(8..9, 14..14, 'a'); fill(13..14, 14..14, 'b'); fill(18..19, 14..14, 'c'); fill(23..24, 14..14, 'd')
        spawn(); door(); bits(82)
    },

    // 83 — EASTER EGG: HTTP 201 Created (a staircase is created, and the door moves in)
    Level(
        name = T("201 Created", "201 Erstellt"),
        intro = T("POST /door. A new door was created.", "POST /tuer. Eine neue Tür wurde erstellt."),
        legend = mapOf('S' to hiddenSolid),
        traps = listOf(
            trap(PastX(12.5f), Fall('a')),
            trap(PastX(20f), Play(Card.SHY_DOOR), Show('S'), DoorTo(29, 6, speed = 22f), say("201 Created. Location: /ledge/7", "201 Erstellt. Location: /ledge/7")),
        ),
    ) {
        border(); floor()
        fill(14..16, 15..17, 'a')
        fill(22..23, 13..13, 'S'); fill(24..25, 11..11, 'S'); fill(26..27, 9..9, 'S'); fill(28..30, 7..7, 'S')
        spawn(); door(); bits(83, x0 = 4)
    },

    // 84 — EASTER EGG: HTTP 204 No Content (every stair is invisible)
    Level(
        name = T("204 No Content", "204 Kein Inhalt"),
        intro = T("There is nothing here. Really. Nothing.", "Hier ist nichts. Wirklich. Nichts."),
        legend = mapOf('b' to ghost, 'c' to ghost, 'd' to ghost),
        traps = listOf(
            trap(Touch('b'), Play(Card.GHOST_BLOCK), say("204: No Content. Neither stairs.", "204: Kein Inhalt. Auch keine Treppe.")),
        ),
    ) {
        border(); floor()
        fill(8..9, 13..13, 'b'); fill(10..11, 11..11, 'c'); fill(12..13, 9..9, 'd')
        fill(14..30, 7..7)
        spawn(); put(28, 6, 'D')
    },

    // 85 — EASTER EGG: HTTP 301 Moved Permanently
    Level(
        name = T("301 Moved Permanently", "301 Dauerhaft verschoben"),
        intro = T("The door has moved. Update your bookmarks.", "Die Tür ist umgezogen. Aktualisiere deine Lesezeichen."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(PastX(20f), Fall('a'), say("The floor moved too.", "Der Boden ist auch umgezogen.")),
            trap(BeforeX(9f), Show('A')),
        ) + doorTrail(
            PastX(20f), 29, 14,
            listOf(DoorTo(29, 1, 26f, hanging = true), DoorTo(2, 1, 26f, hanging = true), DoorTo(2, 10, 26f)),
            first = listOf(Play(Card.SHY_DOOR), say("301 Moved Permanently. New address: the top left.", "301 Dauerhaft verschoben. Neue Adresse: oben links.")),
        ),
    ) {
        border(); floor()
        fill(14..16, 15..17, 'a')
        fill(4..5, 13..14); fill(1..3, 11..11)
        put(7, 14, 'A')
        spawn(12); door()
    },

    // 86 — EASTER EGG: HTTP 302 Found (a temporary redirect: the door comes back)
    Level(
        name = T("302 Found", "302 Gefunden"),
        intro = T("The door was found somewhere else. Temporarily.", "Die Tür wurde woanders gefunden. Vorübergehend."),
        traps = listOf(
            trap(After(3.2f), Saw(33.5f, 14.4f, -5.5f, 0f), say("Found. Redirecting. Please wait.", "Gefunden. Leite weiter. Bitte warten.")),
            trap(After(5.2f), Saw(-1.5f, 14.4f, 6f, 0f)),
        ) + doorTrail(
            PastX(22f), 29, 14,
            listOf(DoorTo(29, 1, 26f, hanging = true), DoorTo(4, 1, 26f, hanging = true), DoorTo(4, 14, 26f)),
            first = listOf(Play(Card.SHY_DOOR)),
        ) + doorTrail(
            PastX(22f), 4, 14,
            listOf(DoorTo(4, 1, 26f, hanging = true), DoorTo(29, 1, 26f, hanging = true), DoorTo(29, 14, 26f)),
            startDelay = 2.3f,
        ),
    ) {
        border(); floor()
        rack(9, 1, 2); rack(15, 1, 2)
        fill(24..25, 13..14)
        spawn(); door(); bits(86)
    },

    // 87 — EASTER EGG: HTTP 400 Bad Request (left and right are malformed)
    Level(
        name = T("400 Bad Request", "400 Fehlerhafte Anfrage"),
        intro = T("Your request could not be understood.", "Deine Anfrage konnte nicht verstanden werden."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(After(0.05f), Play(Card.TWISTED), Swap(true), say("400: malformed request. Direction: undefined.", "400: fehlerhafte Anfrage. Richtung: undefiniert.")),
            trap(PastX(12.5f), Show('A')),
            trap(PastX(6f), Saw(-1.5f, 14.4f, 5f, 0f)),
        ),
    ) {
        border(); floor()
        pit(9..10); put(15, 14, 'A'); put(16, 14, 'A'); rack(21, 2, 2)
        spawn(); door(); bits(87)
    },

    // 88 — EASTER EGG: HTTP 401 Unauthorized (the key is a stair)
    Level(
        name = T("401 Unauthorized", "401 Nicht autorisiert"),
        intro = T("Authentication required. Try sudo.", "Anmeldung erforderlich. Versuch sudo."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('k'), Play(Card.GHOST_BLOCK), Hide('w'), say("Authorized. Welcome, root. (You are not root.)", "Autorisiert. Willkommen, root. (Du bist nicht root.)")),
            trap(Touch('k'), Saw(-1.5f, 14.4f, 5.5f, 0f), delay = 0.4f),
            trap(PastX(22f), Show('A')),
        ),
    ) {
        border(); floor()
        fill(4..5, 13..14); fill(7..8, 11..12, 'k')
        fill(25..26, 12..14, 'w')
        put(28, 14, 'A')
        spawn(); door(30); bits(88)
    },

    // 89 — EASTER EGG: HTTP 403 Forbidden (walls fall in front of you)
    Level(
        name = T("403 Forbidden", "403 Verboten"),
        intro = T("You don't have permission to be here.", "Du hast keine Berechtigung, hier zu sein."),
        legend = mapOf('b' to ghost),
        traps = listOf(
            trap(PastX(12f), Play(Card.HEADBUTT), Fall('w'), say("403: Forbidden. Access denied by order of Mephi.", "403: Verboten. Zugriff verweigert auf Anweisung von Mephi.")),
            trap(PastX(19.5f), Fall('x'), say("Still forbidden.", "Immer noch verboten.")),
        ),
    ) {
        border(); floor()
        fill(20..21, 3..5, 'w'); fill(27..28, 3..5, 'x')
        put(18, 13, 'b'); put(25, 13, 'b')
        spawn(); door(30); bits(89)
    },

    // 90 — EASTER EGG: HTTP 408 Request Timeout (the gate closes at 4.6 s)
    Level(
        name = T("408 Request Timeout", "408 Zeitüberschreitung"),
        intro = T("The server waits 4.6 seconds. Not one more.", "Der Server wartet 4,6 Sekunden. Keine mehr."),
        legend = mapOf('b' to ghost),
        traps = listOf(
            trap(After(4.6f), Play(Card.SINKING), Move('g', 0f, 9f, 25f), say("408: you took too long.", "408: Du hast zu lange gebraucht.")),
        ),
    ) {
        border(); floor()
        fill(14..15, 12..14); put(12, 13, 'b')
        fill(27..27, 1..5, 'g')
        spawn(); door()
    },

    // 91 — EASTER EGG: HTTP 410 Gone (the stones are gone for good)
    Level(
        name = T("410 Gone", "410 Verschwunden"),
        intro = T("Not found. And not coming back.", "Nicht gefunden. Und kommt nicht wieder."),
        legend = mapOf('A' to hidden),
        traps = listOf(
            trap(Touch('a'), Play(Card.GHOST_BLOCK), Hide('a'), say("410: Gone. Permanently.", "410: Verschwunden. Dauerhaft."), delay = 0.5f),
            trap(Touch('b'), Hide('b'), delay = 0.5f),
            trap(Touch('c'), Hide('c'), delay = 0.5f),
            trap(Touch('d'), Hide('d'), delay = 0.5f),
            trap(BeforeX(9f), Show('A')),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(25..31, 15..17)
        fill(21..23, 15..15, 'a'); fill(17..19, 15..15, 'b'); fill(13..15, 15..15, 'c'); fill(9..11, 15..15, 'd')
        put(4, 14, 'A')
        spawn(28); door(2)
    },

    // 92 — EASTER EGG: HTTP 429 Too Many Requests (rate limit: the saws come faster)
    Level(
        name = T("429 Too Many Requests", "429 Zu viele Anfragen"),
        intro = T("Rate limit exceeded. Retry-After: never.", "Ratenlimit überschritten. Retry-After: nie."),
        traps = listOf(0f, 2.0f, 3.4f, 4.4f, 5.1f, 5.6f).mapIndexed { i, d ->
            trap(PastX(4f), *(if (i == 0) arrayOf(Play(Card.DEVIL_SAW), say("429: slow down. (You are going too fast.)", "429: Langsamer. (Du bist zu schnell.)")) else emptyArray()), Saw(33.5f, 14.4f, -7f, 0f), delay = d)
        },
    ) {
        border(); floor()
        rack(15, 2, 2); rack(22, 1, 2)
        spawn(); door(); bits(92)
    },

    // 93 — EASTER EGG: HTTP 451 Unavailable For Legal Reasons (redacted walls)
    Level(
        name = T("451 Redacted", "451 Geschwärzt"),
        intro = T("[REDACTED]. By court order.", "[GESCHWÄRZT]. Auf gerichtliche Anordnung."),
        legend = mapOf('A' to hidden, 'b' to ghost),
        traps = listOf(
            trap(Touch('r'), Play(Card.GHOST_BLOCK), Hide('r'), Show('A'), say("[REDACTED] (the spikes were also redacted)", "[GESCHWÄRZT] (die Spikes auch)"), delay = 0.25f),
            trap(Touch('s'), Hide('s'), delay = 0.25f),
        ),
    ) {
        border(); floor()
        fill(9..9, 12..14, 'r'); put(11, 14, 'A'); put(12, 14, 'A')
        fill(17..17, 12..14, 's')
        fill(24..25, 12..14); put(22, 13, 'b')
        spawn(); door(); bits(93)
    },

    // 94 — EASTER EGG: HTTP 502 Bad Gateway (the door is on the ceiling, gravity is unreliable)
    Level(
        name = T("502 Bad Gateway", "502 Fehlerhaftes Gateway"),
        intro = T("The upstream server is on the ceiling.", "Der Upstream-Server hängt an der Decke."),
        traps = listOf(
            trap(PastX(8f), Play(Card.UPSIDE_DOWN), Gravity(true), say("502: bad gateway. Gravity forwarded to the wrong host.", "502: fehlerhaftes Gateway. Schwerkraft an falschen Host weitergeleitet.")),
            trap(PastX(15f), Gravity(false)),
            trap(PastX(21f), Gravity(true), say("Retrying the gateway...", "Versuche das Gateway erneut...")),
        ),
    ) {
        border(); floor()
        leds(9..14); put(11, 1, 'v')
        leds(23..26); put(25, 1, 'v'); put(27, 1, 'v')
        spawn(); put(29, 1, 'D')
    },

    // 95 — EASTER EGG: HTTP 503 Service Unavailable / Retry-After (wait for the door)
    Level(
        name = T("503 Retry-After", "503 Retry-After"),
        intro = T("Service unavailable. Retry-After: 8 seconds.", "Dienst nicht verfügbar. Retry-After: 8 Sekunden."),
        traps = listOf(
            trap(After(3.0f), Play(Card.COLLAPSE), Fall('a'), say("503: the floor is undergoing maintenance.", "503: Der Boden wird gerade gewartet.")),
            trap(After(3.9f), Fall('b')),
            trap(After(4.8f), Fall('c')),
            trap(After(5.7f), Fall('d')),
            trap(After(6.6f), Fall('e')),
            trap(After(8.0f), DoorTo(28, 14, speed = 20f), say("Retrying... the door is available now.", "Neuer Versuch... die Tür ist jetzt verfügbar.")),
        ),
    ) {
        border()
        fill(0..5, 15..17, 'a'); fill(6..10, 15..17, 'b'); fill(11..15, 15..17, 'c'); fill(16..20, 15..17, 'd'); fill(21..25, 15..17, 'e')
        fill(26..31, 15..17)
        fill(26..30, 4..4)
        spawn(); put(28, 3, 'D')
    },

    // 96 — EASTER EGG: HTTP 511 Network Authentication Required (act finale: log in to the captive portal)
    Level(
        name = T("511 Captive Portal", "511 Captive Portal"),
        intro = T("Accept the terms of service to continue. (You can't read them.)", "Akzeptiere die Nutzungsbedingungen. (Du kannst sie nicht lesen.)"),
        legend = mapOf('B' to hiddenSolid, 'C' to hiddenSolid, 'E' to hiddenSolid),
        traps = listOf(
            trap(Touch('a'), Play(Card.GRAND_FINALE), Show('B'), Saw(-1.5f, 14.4f, 4.5f, 0f), say("511: log in first. Username: guest. Password: guest.", "511: Erst anmelden. Benutzer: gast. Passwort: gast.")),
            trap(Touch('B'), Show('C'), say("Please solve the CAPTCHA.", "Bitte löse das CAPTCHA.")),
            trap(Touch('C'), Show('E')),
            trap(Touch('E'), Gravity(true), DoorTo(29, 1, speed = 22f, hanging = true), say("Login successful. Redirecting to the ceiling.", "Anmeldung erfolgreich. Weiterleitung zur Decke.")),
        ),
    ) {
        border()
        fill(0..5, 15..17); fill(26..31, 15..17)
        fill(7..8, 15..15, 'a'); fill(12..13, 15..15, 'B'); fill(17..18, 15..15, 'C'); fill(22..23, 15..15, 'E')
        put(27, 1, 'v'); put(28, 1, 'v')
        spawn(); door()
    },
)
