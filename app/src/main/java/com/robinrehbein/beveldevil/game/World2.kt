package com.robinrehbein.beveldevil.game

/**
 * World 2, "Höllen-Rechenzentrum": 48 levels in three acts of 16, the layer of networks and servers.
 * Act 1 "Handshake" (1-16): the best jokes of the old data center, the first portals and the DNS trap.
 * Act 2 "Traffic" (17-32): conveyor belts (data bus) and lasers (firewall), first alone, then with portals and the classics.
 * Act 3 "Root" (33-48): combinations, three meta twists in network disguise, one phone shake, and the finale.
 * Easter eggs for techies are marked with EASTER EGG comments in the part files.
 */
object World2 {
    val name = T("Hell's Data Center", "Höllen-Rechenzentrum")

    val levels: List<Level> = World2Part1.levels + World2Part2.levels + World2Part3.levels
}
