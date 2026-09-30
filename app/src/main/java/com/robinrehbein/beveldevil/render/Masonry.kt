package com.robinrehbein.beveldevil.render

import com.robinrehbein.beveldevil.game.Dir

/** Colors of one kind of masonry: stone faces, joints between stones and the bevel on exposed edges. */
class Stone(
    val face: Int, val faceLi: Int, val faceDk: Int, val speck: Int,
    val hi: Int, val mid: Int, val lo: Int, val lo2: Int,
    val jointHi: Int, val jointLo: Int, val spark: Int,
    val details: Boolean,
    /** Steel panels instead of stones: vent slots and rivets as the details. */
    val metal: Boolean = false,
)

val GOLD_STONE = Stone(
    GOLD, 0xFFEEB257.toInt(), 0xFFE09F45.toInt(), 0xFFD9963F.toInt(),
    GOLD_HI, GOLD_MID, GOLD_LO, GOLD_LO2,
    0xFFF3BC62.toInt(), 0xFFC7822F.toInt(), GOLD_SPARK, true,
)

val ROCK_STONE = Stone(
    ROCK, 0xFF331829.toInt(), 0xFF2A1123.toInt(), 0xFF26101F.toInt(),
    ROCK_HI, ROCK_MID, ROCK_LO, ROCK_LO2,
    0xFF3D1C31.toInt(), ROCK_LO2, ROCK_HI, false,
)

/** Colors of a spike; [tip] (0 for none) tints its top two rows, [tipLo] being the shaded side. */
class Spike(val lit: Int, val dark: Int, val left: Int, val right: Int, val baseL: Int, val baseR: Int, val tip: Int = 0, val tipLo: Int = 0)

val BONE_SPIKE = Spike(WHITE, 0xFF8C789E.toInt(), BONE, BONE_LO, BONE_LO, 0xFF9E8AB0.toInt())

/**
 * Autotiled masonry as pure pixel functions into ARGB arrays, used to bake the static layers.
 * Touching solid cells merge into one mass: the bevel sits only on exposed edges, the inside is
 * irregular stones seeded by world tile position, so the same cell always looks the same.
 */
object Masonry {
    private const val N = 1
    private const val S = 2
    private const val W = 4
    private const val E = 8
    private const val NW = 16
    private const val NE = 32
    private const val SW = 64
    private const val SE = 128

    fun hash(x: Int, y: Int): Int {
        var h = x * 374761393 + y * 668265263
        h = (h xor (h ushr 13)) * 1274126177
        return h xor (h ushr 16)
    }

    /** Is there a joint between tile columns [col] and [col] + 1 in tile row [row]? Stones are 1–3 tiles long. */
    fun joint(col: Int, row: Int) = (hash(col, row * 7 + 3) and 3) == 0 || Math.floorMod(col + row * 2, 3) == 0

    private fun stoneStart(col: Int, row: Int): Int {
        var s = col
        while (!joint(s - 1, row)) s--
        return s
    }

    /**
     * Paints the gw×gh grid [cells] (0 empty, 1 paint, 2 solid but not painted) into [out] ([stride] wide,
     * [outH] high) with cell (0,0) at pixel ([ox], [oy]). [col0]/[row0] is that cell's world tile position,
     * which seeds the stones. Cells outside the grid count as solid when [outside] is set.
     */
    fun bake(out: IntArray, stride: Int, outH: Int, ox: Int, oy: Int, cells: IntArray, gw: Int, gh: Int, col0: Int, row0: Int, outside: Boolean, s: Stone) {
        fun solid(c: Int, r: Int) = if (c < 0 || r < 0 || c >= gw || r >= gh) outside else cells[r * gw + c] != 0
        for (r in 0 until gh) for (c in 0 until gw) {
            if (cells[r * gw + c] != 1) continue
            var m = 0
            if (solid(c, r - 1)) m = m or N
            if (solid(c, r + 1)) m = m or S
            if (solid(c - 1, r)) m = m or W
            if (solid(c + 1, r)) m = m or E
            if (solid(c - 1, r - 1)) m = m or NW
            if (solid(c + 1, r - 1)) m = m or NE
            if (solid(c - 1, r + 1)) m = m or SW
            if (solid(c + 1, r + 1)) m = m or SE
            tile(out, stride, outH, ox + c * TS, oy + r * TS, col0 + c, row0 + r, m, s)
        }
    }

    private fun tile(out: IntArray, stride: Int, outH: Int, x0: Int, y0: Int, col: Int, row: Int, m: Int, s: Stone) {
        val n = m and N != 0
        val so = m and S != 0
        val w = m and W != 0
        val e = m and E != 0
        val jr = e && joint(col, row)
        val jl = w && joint(col - 1, row)
        val face = when (Math.floorMod(hash(stoneStart(col, row), row), 5)) { 0, 1 -> s.face; 2 -> s.faceLi; 3 -> s.faceDk; else -> s.face }
        val h = hash(col * 3 + 1, row * 5 + 2)
        val detail = if (s.details) Math.floorMod(h, if (s.metal) 12 else 19) else -1
        val gx = 2 + (h ushr 8 and 3)
        val gy = 2 + (h ushr 10 and 3)
        for (y in 0 until TS) {
            val py = y0 + y
            if (py < 0 || py >= outH) continue
            for (x in 0 until TS) {
                val px = x0 + x
                if (px < 0 || px >= stride) continue
                var c = face
                if (hash(col * TS + x, row * TS + y) and 31 == 0) c = s.speck
                if (s.metal) when (detail) {
                    0, 1 -> if (x in 2..5) { if (y == 3 || y == 5) c = s.lo2 else if (y == 4) c = s.jointHi }
                    2, 3 -> if (x == gx && y == gy) c = s.mid else if (x == gx + 1 && y == gy + 1) c = s.lo2
                } else when (detail) {
                    0 -> if ((x == 2 && y == 2) || (x == 3 && (y == 3 || y == 4)) || (x == 4 && y == 5) || (x == 5 && y == 5)) c = s.jointLo
                    1 -> if (x == 3 && y == 3) c = s.spark else if (x == 4 && y == 4) c = s.lo2 else if ((x == 4 && y == 3) || (x == 3 && y == 4)) c = s.mid
                    2 -> if (x == gx && y == gy) c = s.spark else if ((x - gx) * (x - gx) + (y - gy) * (y - gy) == 1) c = s.faceLi
                }
                // joints between stones inside the mass
                if (y == 0 && n) c = s.jointHi
                if (x == 0 && jl) c = s.jointHi
                if (y == TS - 1 && so) c = s.jointLo
                if (x == TS - 1 && jr) c = s.jointLo
                // bevel on exposed edges: light from the top left
                if (!n && y == 0) c = s.hi else if (!n && y == 1 && x > 0) c = s.mid
                if (!w && x == 0) c = s.hi else if (!w && x == 1 && y > 0) c = s.mid
                if (!so && y == TS - 1) c = s.lo else if (!so && y == TS - 2) c = s.lo2
                if (!e && x == TS - 1) c = s.lo else if (!e && x == TS - 2) c = s.lo2
                // inner corners continue the neighbours' bevels
                if (n && w && m and NW == 0 && x <= 1 && y <= 1) c = if (x == 0 && y == 0) s.hi else s.mid
                if (n && e && m and NE == 0 && x == TS - 1 && y <= 1) c = s.lo2
                if (so && w && m and SW == 0 && x == 0 && y >= TS - 2) c = s.lo2
                if (so && e && m and SE == 0 && x >= TS - 2 && y >= TS - 2) c = if (x == TS - 1 && y == TS - 1) s.lo else s.lo2
                // outer corners: chipped, with a glint on the lit one
                if (!n && !w && x == 1 && y == 1) c = s.spark
                val corner = (x == 0 || x == TS - 1) && (y == 0 || y == TS - 1)
                if (corner && ((y == 0 && !n) || (y != 0 && !so)) && ((x == 0 && !w) || (x != 0 && !e))) continue
                out[py * stride + px] = c
            }
        }
    }

    // ---------- spikes ----------

    /** Color of pixel ([x], [y]) of an upward spike, 0 for none. Rows 1..7, widening every other row. */
    fun spikeColor(x: Int, y: Int, s: Spike = BONE_SPIKE): Int {
        if (y < 1) return if (x == 3 || x == 4) INK else 0
        val half = (y + 1) / 2
        val l = 4 - half
        val r = 3 + half
        return when {
            x == l - 1 || x == r + 1 -> if (y < TS - 1) INK else 0
            x < l || x > r -> 0
            y == TS - 1 -> if (x < 4) s.baseL else s.baseR
            s.tip != 0 && y <= 2 -> if (x < 4) s.tip else s.tipLo
            x == l -> s.lit
            x == r -> s.dark
            x < 4 -> s.left
            else -> s.right
        }
    }

    /** Maps a pixel of an upward spike to the tile pixel for [dir]. */
    fun spikeX(x: Int, y: Int, dir: Dir) = when (dir) { Dir.UP, Dir.DOWN -> x; Dir.LEFT -> y; Dir.RIGHT -> TS - 1 - y }
    fun spikeY(x: Int, y: Int, dir: Dir) = when (dir) { Dir.UP -> y; Dir.DOWN -> TS - 1 - y; Dir.LEFT, Dir.RIGHT -> x }

    fun spike(out: IntArray, stride: Int, outH: Int, x0: Int, y0: Int, dir: Dir, st: Spike = BONE_SPIKE) {
        for (y in 0 until TS) for (x in 0 until TS) {
            val c = spikeColor(x, y, st)
            if (c == 0) continue
            val px = x0 + spikeX(x, y, dir)
            val py = y0 + spikeY(x, y, dir)
            if (px in 0 until stride && py in 0 until outH && out[py * stride + px] == 0) out[py * stride + px] = c
        }
    }
}
