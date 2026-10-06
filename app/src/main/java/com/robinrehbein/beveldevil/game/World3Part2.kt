package com.robinrehbein.beveldevil.game

/**
 * World 3, levels 17-32, act 2 "Überhitzung": hot plates, chips under load, heatsinks, the overclocked floor and melting plates,
 * then combined with the circuits of act 1. The green board. Levels 17-24 are the rebuilt block C ([World3PartC]), levels 25-32
 * block D ([World3PartD]).
 */
object World3Part2 {
    val levels: List<Level> = World3PartC.levels + World3PartD.levels
}
