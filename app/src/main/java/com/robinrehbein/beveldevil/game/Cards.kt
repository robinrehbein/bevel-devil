package com.robinrehbein.beveldevil.game

enum class Rarity(val label: T) {
    COMMON(T("Common", "Häufig")),
    RARE(T("Rare", "Selten")),
    LEGENDARY(T("Legendary", "Legendär")),
}

/** Trap cards Mephi deals. Getting killed by a trap puts its card into the album. */
enum class Card(val title: T, val flavor: T, val rarity: Rarity) {
    COLLAPSE(T("Collapse", "Einsturz"), T("The floor was only borrowed.", "Der Boden war nur geliehen."), Rarity.COMMON),
    SPIKE_SEED(T("Spike Seed", "Stachelsaat"), T("Spikes grow where you step.", "Spikes wachsen, wo du hintrittst."), Rarity.COMMON),
    SHY_DOOR(T("Shy Door", "Türflucht"), T("The door has better plans.", "Die Tür hat Besseres vor."), Rarity.RARE),
    HEADBUTT(T("Headbutt", "Kopfnuss"), T("What goes up was never up there.", "Die Decke kommt zu dir."), Rarity.COMMON),
    UPSIDE_DOWN(T("Upside Down", "Kopfüber"), T("Up is the new down.", "Oben ist das neue Unten."), Rarity.LEGENDARY),
    TWISTED(T("Twisted", "Verdreht"), T("Left is right. Probably.", "Links ist rechts. Vermutlich."), Rarity.RARE),
    DEVIL_SAW(T("Devil Saw", "Teufelssäge"), T("It only wants a hug.", "Sie will nur kuscheln."), Rarity.RARE),
    GHOST_BLOCK(T("Ghost Block", "Geisterblock"), T("It was always there. Sort of.", "Er war immer da. Irgendwie."), Rarity.COMMON),
    SINKING(T("Sinking", "Sinkflug"), T("Solid ground, limited offer.", "Fester Boden, nur kurz gültig."), Rarity.COMMON),
    DECOY(T("Decoy", "Attrappe"), T("That door was decoration.", "Die Tür war Deko."), Rarity.RARE),
    CRUMBLE(T("Crumble", "Wackelboden"), T("Keep moving. Seriously.", "Nicht stehen bleiben. Ernsthaft."), Rarity.COMMON),
    GRAND_FINALE(T("Grand Finale", "Großes Finale"), T("Everything. At once.", "Alles. Gleichzeitig."), Rarity.LEGENDARY),

    // World 3 (hardware); appended so that saved cards and album positions stay as they were
    SHORT_CIRCUIT(T("Short Circuit", "Kurzschluss"), T("The wire was on my side.", "Der Draht war auf meiner Seite."), Rarity.COMMON),
    OVERCLOCKED(T("Overclocked", "Übertaktet"), T("Warranty void. Floor too.", "Garantie erloschen. Boden auch."), Rarity.COMMON),
    BIT_FLIP(T("Bit Flip", "Bitkipper"), T("Cosmic rays. Allegedly.", "Kosmische Strahlung. Angeblich."), Rarity.RARE),
    BACKDRAFT(T("Backdraft", "Gegenwind"), T("Wind is free. Direction is not.", "Wind ist gratis. Die Richtung nicht."), Rarity.COMMON),
    THROTTLE(T("Throttle", "Drosselung"), T("Slow down. Or I will.", "Werd langsamer. Sonst mach ich es."), Rarity.RARE),
    BIOS(T("BIOS", "BIOS"), T("Press DEL to lose.", "ENTF drücken zum Verlieren."), Rarity.LEGENDARY),
}
