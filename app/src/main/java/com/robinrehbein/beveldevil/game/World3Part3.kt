package com.robinrehbein.beveldevil.game

/**
 * World 3, levels 33-48. Act 3, "Lüfter": fans (updrafts, crosswinds, reversals, fan traps), everything combined,
 * and the BIOS finale in the last three levels. The blue board. Levels 33-40 are the rebuilt block E ([World3PartE]),
 * levels 41-48 the rebuilt block F ([World3PartF]): lifts and vents, a stalker, a monitor that turns, a self-test that
 * rewinds you, a boot order that runs out of doors, and the setup that ends the game.
 */
object World3Part3 {
    val levels: List<Level> = World3PartE.levels + World3PartF.levels
}
