package com.robinrehbein.beveldevil.game

/**
 * World 3, "Platine" (circuit board): 48 levels in three acts of 16, the layer of hardware.
 * Act 1 "Stromkreise" (1-16, green board): copper rails, pressure pads, clocks and live traces; Mephi cuts the power under you.
 * Act 2 "Überhitzung" (17-32, green board): hot plates, chips under load, heatsinks, the overclocked floor, melt plates.
 * Act 3 "Lüfter" (33-48, blue board): fans (updrafts, crosswinds, reversals) and everything combined; 46-48 is the BIOS finale.
 */
object World3 {
    val name = T("Circuit Board", "Platine")

    val levels: List<Level> = World3Part1.levels + World3Part2.levels + World3Part3.levels
}
