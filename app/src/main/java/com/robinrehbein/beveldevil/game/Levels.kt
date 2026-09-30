package com.robinrehbein.beveldevil.game

/** Level registry: the worlds' levels back to back, so world 1 holds indices 0..47 (saved progress uses them). */
object Levels {
    val all: List<Level> = Worlds.all.flatMap { it.levels }
}
