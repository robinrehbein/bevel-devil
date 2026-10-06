package com.robinrehbein.beveldevil.game

/**
 * World 3, levels 1-16, act 1 "Stromkreise": copper rails, pressure pads, clocks and live traces, Mephi cutting the
 * power under you, a bit flip. Levels 1-8 are the rebuilt block A ([World3PartA]), levels 9-16 block B ([World3PartB]).
 */
object World3Part1 {
    val levels: List<Level> = World3PartA.levels + World3PartB.levels
}
