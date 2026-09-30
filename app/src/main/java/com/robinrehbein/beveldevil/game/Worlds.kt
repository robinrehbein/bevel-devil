package com.robinrehbein.beveldevil.game

/** One world of the descent: shown on its transition screen and as a tab on the level select. */
class WorldInfo(val number: Int, val name: T, val layer: T, val line: T, val levels: List<Level>) {
    /** Index of the world's first level in [Levels.all]; the worlds are laid out back to back. */
    val firstLevel: Int get() = Worlds.all.takeWhile { it !== this }.sumOf { it.size }
    val size get() = levels.size
    operator fun contains(i: Int) = i - firstLevel in levels.indices
}

object Worlds {
    val all = listOf(
        WorldInfo(
            1, T("Hell's Cellar", "Höllenkeller"),
            T("Layer 1: Userspace", "Schicht 1: Userspace"),
            T("Userspace. Floors? 404, not found.", "Willkommen im Erdgeschoss. Böden gibt's nur auf Anfrage."),
            World1.levels,
        ),
        WorldInfo(
            2, World2.name,
            T("Layer 2: Network & Servers", "Schicht 2: Netzwerk & Server"),
            T("99.9% uptime. The 0.1% is you.", "99,9 % Verfügbarkeit. Die 0,1 % sind du."),
            World2.levels,
        ),
        WorldInfo(
            3, World3.name,
            T("Layer 3: Hardware", "Schicht 3: Hardware"),
            T("Hardware. You can't patch this one.", "Hardware. Das fixt du nicht per Update."),
            World3.levels,
        ),
    )

    /** World [n] (1-based); unknown numbers fall back to the first world. */
    fun get(n: Int): WorldInfo = all.firstOrNull { it.number == n } ?: all.first()

    /** The world holding global level [i]; out of range, the nearest world with levels. */
    fun of(i: Int): WorldInfo = all.firstOrNull { i in it } ?: if (i < 0) all.first() else all.last { it.size > 0 }

    /** Level number within its world, 1-based. */
    fun local(i: Int) = i - of(i).firstLevel + 1

    /** "2-17": world and level number within it. */
    fun label(i: Int) = "${of(i).number}-${local(i)}"
}
