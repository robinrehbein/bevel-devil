package com.robinrehbein.beveldevil.game

/** Small helpers for laying out World 1 levels. */

/** Draw tile art: each string is a row, '#' (or any non-'.') becomes [c]. Top-left at ([x], [y]). */
fun MapBuilder.art(x: Int, y: Int, vararg rows: String, c: Char = '#') {
    rows.forEachIndexed { dy, row -> row.forEachIndexed { dx, ch -> if (ch != '.' && ch != ' ') put(x + dx, y + dy, c) } }
}

/** Remove floor tiles: a pit over [xs]. */
fun MapBuilder.pit(xs: IntRange, top: Int = 15) = fill(xs, top until rows, '.')

/** Player start and door on the walking row (or another row). */
fun MapBuilder.spawn(x: Int = 2, y: Int = 14) = put(x, y, 'P')
fun MapBuilder.door(x: Int = 29, y: Int = 14) = put(x, y, 'D')

/** Ceiling spikes (static, decorative or deadly) over [xs] on row 1. */
fun MapBuilder.ceilingSpikes(xs: IntRange, y: Int = 1) { for (x in xs) put(x, y, 'v') }
