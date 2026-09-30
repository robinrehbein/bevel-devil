package com.robinrehbein.beveldevil.game

/** Small helpers for the tile art of world 3 (rails over pits, trace walls, chip stacks). */

/** A gap in the floor over [xs], bridged by the circuit or plate group [c] on the walking surface (row 15). */
internal fun MapBuilder.bridge(xs: IntRange, c: Char, y: Int = 15) {
    pit(xs, y)
    fill(xs, y..y, c)
}

/** A full-height wall of group [c] in column [x], from the ceiling to the floor. */
internal fun MapBuilder.wire(x: Int, c: Char, top: Int = 1, bottom: Int = 14) = fill(x..x, top..bottom, c)

/** A chip: a block [w] wide and [h] tall standing on row [base]. */
internal fun MapBuilder.chip(x: Int, w: Int, h: Int, base: Int = 14, c: Char = '#') = fill(x until x + w, base - h + 1..base, c)
