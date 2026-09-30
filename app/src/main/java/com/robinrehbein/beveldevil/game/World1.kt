package com.robinrehbein.beveldevil.game

/**
 * World 1, "Höllenkeller": 48 levels in three acts of 16.
 * Act 1 "Die Karten" (1-16): the classic tricks. Act 2 "Neue Regeln" (17-32): blinking platforms, path saws, Idle.
 * Act 3 "Mephi schummelt" (33-48): meta twists and, in exactly two levels, phone motion.
 */
object World1 {
    val levels: List<Level> = World1Part1.levels + World1Part2.levels + World1Part3.levels
}
