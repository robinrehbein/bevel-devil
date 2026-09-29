package com.robinrehbein.beveldevil.game

/** One world of the descent: shown on its transition screen. [firstLevel] is an index into [Levels.all]. */
class WorldInfo(val number: Int, val name: T, val layer: T, val line: T, val firstLevel: Int)

object Worlds {
    val all = listOf(
        WorldInfo(
            1, T("Hell's Cellar", "Höllenkeller"),
            T("Layer 1: Userspace", "Schicht 1: Userspace"),
            T("Userspace. Floors? 404, not found.", "Willkommen im Erdgeschoss. Böden gibt's nur auf Anfrage."),
            firstLevel = 0,
        ),
        WorldInfo(
            2, T("Hell Data Center", "Höllen-Rechenzentrum"),
            T("Layer 2: Network & Servers", "Schicht 2: Netzwerk & Server"),
            T("99.9% uptime. The 0.1% is you.", "99,9 % Verfügbarkeit. Die 0,1 % sind du."),
            firstLevel = 12,
        ),
        WorldInfo(
            3, T("Circuit Board", "Platine"),
            T("Layer 3: Hardware", "Schicht 3: Hardware"),
            T("Hardware. You can't patch this one.", "Hardware. Das fixt du nicht per Update."),
            firstLevel = 24,
        ),
    )

    /** World [n] (1-based); unknown numbers fall back to the first world. */
    fun get(n: Int): WorldInfo = all.firstOrNull { it.number == n } ?: all.first()
}
