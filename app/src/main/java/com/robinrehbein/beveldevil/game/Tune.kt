package com.robinrehbein.beveldevil.game

/** Background music selection; one synthesized loop per entry. */
enum class Tune {
    TITLE, WORLD1, WORLD2, WORLD3;

    companion object {
        fun ofWorld(n: Int) = when (n) { 2 -> WORLD2; 3 -> WORLD3; else -> WORLD1 }
    }
}
