package com.robinrehbein.beveldevil.game

enum class Rarity(val label: T) {
    COMMON(T("Common", "Häufig")),
    RARE(T("Rare", "Selten")),
    LEGENDARY(T("Legendary", "Legendär")),
}

/**
 * Trap cards Mephi deals. Getting killed by a trap puts its card into the album.
 * [how] is the plain line the album shows next to a found card: what the trap does, so that look-alike
 * families (Collapse, Crumble, Sinking; Shy Door, Decoy) read apart. Never where or when it strikes.
 */
enum class Card(val title: T, val flavor: T, val rarity: Rarity, val how: T) {
    COLLAPSE(T("Collapse", "Einsturz"), T("The floor was only borrowed.", "Der Boden war nur geliehen."), Rarity.COMMON,
        T("The ground drops away at once. No warning.", "Der Boden fällt sofort weg. Ohne Vorwarnung.")),
    SPIKE_SEED(T("Spike Seed", "Stachelsaat"), T("Spikes grow where you step.", "Spikes wachsen, wo du hintrittst."), Rarity.COMMON,
        T("Hidden spikes shoot up where you land.", "Versteckte Stacheln schießen hoch, wo du landest.")),
    SHY_DOOR(T("Shy Door", "Türflucht"), T("The door has better plans.", "Die Tür hat Besseres vor."), Rarity.RARE,
        T("The real door flees to a new spot.", "Die echte Tür flieht an eine neue Stelle.")),
    HEADBUTT(T("Headbutt", "Kopfnuss"), T("What goes up was never up there.", "Die Decke kommt zu dir."), Rarity.COMMON,
        T("Something drops on you from above.", "Von oben fällt etwas auf dich.")),
    UPSIDE_DOWN(T("Upside Down", "Kopfüber"), T("Up is the new down.", "Oben ist das neue Unten."), Rarity.LEGENDARY,
        T("Gravity or the picture turns upside down.", "Schwerkraft oder Bild stehen Kopf.")),
    TWISTED(T("Twisted", "Verdreht"), T("Left is right. Probably.", "Links ist rechts. Vermutlich."), Rarity.RARE,
        T("Left and right swap on your controls.", "Links und rechts sind auf der Steuerung vertauscht.")),
    DEVIL_SAW(T("Devil Saw", "Teufelssäge"), T("It only wants a hug.", "Sie will nur kuscheln."), Rarity.RARE,
        T("A saw runs along a fixed track.", "Eine Säge fährt eine feste Bahn ab.")),
    GHOST_BLOCK(T("Ghost Block", "Geisterblock"), T("It was always there. Sort of.", "Er war immer da. Irgendwie."), Rarity.COMMON,
        T("A block that was not there suddenly is.", "Ein Block, der nicht da war, ist plötzlich da.")),
    SINKING(T("Sinking", "Sinkflug"), T("Solid ground, limited offer.", "Fester Boden, nur kurz gültig."), Rarity.COMMON,
        T("The platform sinks away under your weight.", "Die Plattform sinkt unter deinem Gewicht weg.")),
    DECOY(T("Decoy", "Attrappe"), T("That door was decoration.", "Die Tür war Deko."), Rarity.RARE,
        T("The way out leads somewhere else.", "Der Ausgang führt woanders hin.")),
    CRUMBLE(T("Crumble", "Wackelboden"), T("Keep moving. Seriously.", "Nicht stehen bleiben. Ernsthaft."), Rarity.COMMON,
        T("Holds for a moment, then breaks under you.", "Hält kurz, dann bricht er unter dir weg.")),
    GRAND_FINALE(T("Grand Finale", "Großes Finale"), T("Everything. At once.", "Alles. Gleichzeitig."), Rarity.LEGENDARY,
        T("Mephi's big number. Expect anything.", "Mephis große Nummer. Rechne mit allem.")),

    // World 3 (hardware); appended so that saved cards and album positions stay as they were
    SHORT_CIRCUIT(T("Short Circuit", "Kurzschluss"), T("The wire was on my side.", "Der Draht war auf meiner Seite."), Rarity.COMMON,
        T("A circuit switches on or off under you.", "Ein Stromkreis schaltet unter dir an oder aus.")),
    OVERCLOCKED(T("Overclocked", "Übertaktet"), T("Warranty void. Floor too.", "Garantie erloschen. Boden auch."), Rarity.COMMON,
        T("Metal turns red-hot in a flash.", "Metall wird schlagartig glühend heiß.")),
    BIT_FLIP(T("Bit Flip", "Bitkipper"), T("Cosmic rays. Allegedly.", "Kosmische Strahlung. Angeblich."), Rarity.RARE,
        T("Two circuits swap their power.", "Zwei Stromkreise tauschen ihren Strom.")),
    BACKDRAFT(T("Backdraft", "Gegenwind"), T("Wind is free. Direction is not.", "Wind ist gratis. Die Richtung nicht."), Rarity.COMMON,
        T("A belt or fan suddenly works against you.", "Ein Band oder Lüfter arbeitet plötzlich gegen dich.")),
    THROTTLE(T("Throttle", "Drosselung"), T("Slow down. Or I will.", "Werd langsamer. Sonst mach ich es."), Rarity.RARE,
        T("Heat creeps up, or a fan gives way.", "Hitze kriecht hoch, oder ein Lüfter gibt nach.")),
    BIOS(T("BIOS", "BIOS"), T("Press DEL to lose.", "ENTF drücken zum Verlieren."), Rarity.LEGENDARY,
        T("Reboot: somewhere near you, power goes on or off.", "Neustart: Irgendwo bei dir geht Strom an oder aus.")),

    // V2; appended for the same reason
    UNDO(T("Ctrl+Z", "Strg+Z"), T("Progress was just a draft.", "Fortschritt war nur ein Entwurf."), Rarity.RARE,
        T("Rewinds you a few seconds.", "Spult dich ein paar Sekunden zurück.")),
    STALKER(T("Stalker", "Verfolger"), T("It only wants to be close.", "Er will nur in deiner Nähe sein."), Rarity.COMMON,
        T("Something chases you down the lane.", "Etwas jagt dir hinterher.")),
    /** Found when a bluff ([Action.Bluff]) first turns over; counts the deaths after one. */
    BLUFF(T("Bluff", "Bluff"), T("I never had that card. You believed me.", "Die Karte hatte ich nie. Du glaubtest mir."), Rarity.LEGENDARY,
        T("Mephi shows a card he does not hold.", "Mephi zeigt eine Karte, die er nicht hat.")),
    /** "Who says the room ends here?" ([Action.Extend]): the wall breaks open, the level goes on. */
    ANNEX(T("Annex", "Anbau"), T("The end was a load-bearing lie.", "Das Ende war eine tragende Lüge."), Rarity.RARE,
        T("The wall breaks open. The level goes on.", "Die Wand bricht auf. Das Level geht weiter.")),
}
