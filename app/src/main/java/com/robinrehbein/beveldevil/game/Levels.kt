package com.robinrehbein.beveldevil.game

/** Level registry: the worlds' levels back to back, so world 1 keeps indices 0..127 (saved progress uses them). */
object Levels {
    val all: List<Level> = Worlds.all.flatMap { it.levels }
}
