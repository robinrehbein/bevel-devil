package com.robinrehbein.beveldevil.game

/**
 * World 2: Mephi's data center in hell. 128 levels, eight acts of sixteen levels.
 * Easter eggs for techies are marked with EASTER EGG comments in the part files.
 */
object World2 {
    val name = T("Hell's Data Center", "Höllen-Rechenzentrum")

    val levels: List<Level> = world2Part1 + world2Part2 + world2Part3 + world2Part4 + world2Part5 + world2Part6 + world2Part7
}
