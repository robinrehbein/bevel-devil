package com.robinrehbein.beveldevil.game

import kotlin.math.abs
import org.junit.Assert.assertTrue

/**
 * Guard rails of docs/LEVEL_DESIGN_V2.md §9: the quality rules of the V2 rebuild as reusable checks.
 *
 * Every rule is a function that returns its violations as readable lines (empty: the rule holds), so the same check
 * serves the design tests (assert empty) and the kit's own tests (assert that a bad room is caught).
 *
 * **How a rebuilt level gets checked** (the world's test class, e.g. `World2DesignTest`, extends [DesignTestBase]):
 *
 * 1. `design` already holds the level's row from §8 (block and twist codes). If you swap within a block as §8 allows,
 *    change the row here *and* in the doc; the table tests (H6, act structure) then tell you whether the swap is legal.
 * 2. Add the level's bot solution to `SOLUTIONS` (companion of the world's design test), one [Solution] per round:
 *    round 1 first, then each rematch round. Prefer `rightTo`/`hopR`/`waitFor` over raw hold times: the slop test
 *    perturbs every timed hold by ±0.15 s, every `rightTo`/`leftTo` target by ±1 tile and every condition wait
 *    (`waitFor`, `rightUntil`, …) by reacting 0.15 s late or early. Do not pad a solution with `wait`s: H3 measures
 *    the run again with every idle command skipped.
 * 3. Add the level number to `REBUILT`. From then on every rule below runs for it.
 *
 *     val SOLUTIONS: Map<Int, List<Solution>> = mapOf(
 *         12 to listOf(
 *             { hopR(4.2f).leftTo(2.6f).rightTo(20f).waitFor { !it.beams[0].lit }.hopR(24f).right(2f) },  // round 1
 *             { rightTo(9f).hopL(6f).rightTo(27f) },                                                   // rematch
 *         ),
 *     )
 *     val REBUILT: Set<Int> = setOf(11, 12)
 *
 * The world's level test can play the same script, so the solution lives in one place:
 * `@Test fun level12() = World2DesignTest.play(12)` (and `play(12, round = 2)` in the deck test).
 *
 * The rules (all for rebuilt levels only, except H6 and the table checks, which run on the whole §8 table):
 * - H3 [densityViolations]: never more than [GAP] s without a real trap ([Weight]) in the clean run, standing still at
 *   most [WAIT_SHARE] of it and at most [WAIT_STRETCH] s in one go, and no shorter than [minDuration], also with idle
 *   waits skipped,
 * - H2 [holdRightWithHopsViolations]: holding right, plain or hopping every 0.4 / 0.7 / 1.0 s, right away or after
 *   standing still [PROBE_WAIT] s, never wins,
 * - H7 [slopViolations]: the solution still wins with all its timings late *or* all early,
 * - H5 [spikePopupCount] ≤ 1 per round, and at most [ACT_QUOTA] rebuilt levels per act with any (W3: also [heatSpikeFinaleCount]),
 * - H6 [h6Violations] on the §8 table, [tableRotationViolations] and the puzzle share on the table,
 * - §7 [cardSpreadViolations]: no card more than 3 times in an act (GRAND_FINALE in a finale does not count),
 * - H8 [cardLintViolations]: the card fits the trap it sits on ([cardFits]),
 * - H9 [rematchViolations]: a rematch round's clean run is at least as long as round 1's, and round 1's solution loses it,
 * - H12 [rotationViolations]: per act at most [CAPS] levels with a door that flees, pads, timed lasers, blinking,
 *   gravity flip, swapped controls (counted from the actions in the level code),
 * - H13 [familyViolations]: at most [MAX_FAMILIES] effect families ([families]) per room,
 * - H15 [teethViolations]: no lethal trap of the clean run is beaten both by standing still and by running straight on,
 * - H17 [fillerDeathViolations]: holding right from the spawn does not die in the first [FILLER_WINDOW] s before a trap went off.
 */
typealias Solution = Bot.() -> Unit

/**
 * A level's row in §8: its puzzle blocks (R1–R12) and its surprise families (U1–U18), the main one first. A meta twist
 * keeps its variant (`U16:Ghost`): the meta tricks have nothing in common but the label, so H6 compares the variant.
 */
data class Design(val blocks: Set<String>, val twists: Set<String>, val breather: Boolean = false) {
    val mainBlock get() = blocks.firstOrNull()
    val mainTwist get() = twists.firstOrNull()
    val codes get() = blocks + twists

    override fun toString() = "${blocks.joinToString("+").ifEmpty { "–" }}/${twists.joinToString("+")}${if (breather) " ★" else ""}"
}

/** Builds a [Design] the way §8 writes it: `d("R1+R5", "U7+U4")`, `d("–", "U9", breather = true)`, `d("R5", "U16:Ghost")`. */
fun d(blocks: String, twists: String, breather: Boolean = false) = Design(
    blocks.split('+').map { it.trim() }.filter { it.isNotEmpty() && it != "–" && it != "-" }.toCollection(LinkedHashSet()),
    twists.split('+').map { it.trim() }.filter { it.isNotEmpty() }.toCollection(LinkedHashSet()),
    breather,
)

object DesignRules {
    const val SLOP_TIME = 0.15f
    const val SLOP_TILES = 1f
    /** The two sloppy players of [slopViolations]: late (holds longer, jumps further on) and early. */
    val SLOPS = listOf(Slop(SLOP_TIME, SLOP_TILES), Slop(-SLOP_TIME, -SLOP_TILES))
    /** Hop rhythms (seconds between jumps) of the naive player. */
    val HOP_RHYTHMS = listOf(0.4f, 0.7f, 1.0f)
    /** How long the naive player keeps at it. */
    const val NAIVE_SECONDS = 30f
    val FINALES = setOf(16, 32, 48)
    /** H5: rebuilt levels per act (of 16) with a spike popup, 25 %. */
    const val ACT_QUOTA = 4
    /** §7: plays of one card per act. */
    const val CARD_LIMIT = 3

    val BLOCKS = (1..12).map { "R$it" }.toSet()
    val TWISTS = (1..18).map { "U$it" }.toSet()
    /** The families W3 has to bring in at least every third level (§8, World 3). */
    val OTHER_FAMILIES = setOf("U1", "U2", "U3", "U4", "U6", "U7", "U8", "U12", "U14")

    fun act(n: Int) = (n - 1) / 16 + 1
    fun isFinale(n: Int) = n in FINALES
    /** The levels the V2 rules apply to: all from W1-7 on. */
    fun ruled(world: Int, n: Int) = world != 1 || n >= 7

    /**
     * §6 (recipe v2): the floor a clean run must not fall below, a sanity check only (density, not duration, is the
     * goal, see [densityViolations]). 6 s; W1 1–6 4 s; act finales 10 s; ★ breathers none.
     */
    fun minDuration(world: Int, n: Int, design: Design?): Float = when {
        design?.breather == true -> 0f
        isFinale(n) -> 10f
        world == 1 && n <= 6 -> 4f
        else -> 6f
    }

    // ---------- playing ----------

    /** Plays [solution] on [round] (0-based) of [level]. */
    fun play(level: Level, round: Int, solution: Solution, slop: Slop = Slop.NONE): Bot = Bot(level, round, slop).apply(solution)

    /** H3: plays the solution, asserts the win and returns [World.time] at the win. */
    fun cleanRunTime(level: Level, round: Int, solution: Solution): Float {
        val bot = play(level, round, solution)
        bot.expect(WorldState.WON)
        return bot.world.time
    }

    /**
     * H3 as violations: the solution does not win, or wins faster than [min] seconds. Standing still does not make a
     * room longer: the solution is played a second time with every idle command skipped ([Bot.skipIdle]); if it still
     * wins, that (shorter) run is the one measured. Waits the room really needs make the second run lose and count.
     */
    fun cleanRunViolations(level: Level, round: Int, solution: Solution, min: Float): List<String> {
        val bot = play(level, round, solution)
        val where = "${level.name.en} round ${round + 1}"
        if (bot.world.state != WorldState.WON) return listOf("$where: the solution ends ${bot.world.state} at t=%.2f".format(bot.world.time))
        if (bot.world.time < min) return listOf("$where: clean run %.2f s, needs %.0f s".format(bot.world.time, min))
        val brisk = Bot(level, round, skipIdle = true).apply(solution).world
        if (brisk.state == WorldState.WON && brisk.time < min) {
            return listOf("$where: clean run %.2f s, but %.2f s without its idle waits, needs %.0f s".format(bot.world.time, brisk.time, min))
        }
        return emptyList()
    }

    /**
     * The naive player: after standing still [wait] seconds, holding right, plainly or hopping every [rhythm] seconds,
     * for [NAIVE_SECONDS].
     */
    fun naive(level: Level, round: Int, rhythm: Float?, wait: Float = 0f): Bot {
        val bot = Bot(level, round)
        if (wait > 0f) bot.wait(wait)
        val end = wait + NAIVE_SECONDS
        if (rhythm == null) return bot.right(NAIVE_SECONDS)
        val jump = minOf(0.35f, rhythm / 2)
        while (bot.world.state == WorldState.PLAYING && bot.world.time < end) bot.rightJump(jump).right(rhythm - jump)
        return bot
    }

    /**
     * H2 (and the room-wide half of the teeth test H15): the naive strategies that win [round] of [level] (empty: none
     * does). Holding right, plain or hopping, right away and after standing still [PROBE_WAIT] s.
     */
    fun holdRightWithHopsViolations(level: Level, round: Int): List<String> =
        listOf(0f, PROBE_WAIT).flatMap { wait ->
            (listOf<Float?>(null) + HOP_RHYTHMS).mapNotNull { r ->
                val bot = naive(level, round, r, wait)
                if (bot.world.state != WorldState.WON) null
                else "${level.name.en} round ${round + 1}: ${if (wait > 0f) "standing still ${wait.toInt()} s, then " else ""}holding right${r?.let { " hopping every $it s" } ?: ""} wins at t=%.2f".format(bot.world.time)
            }
        }

    fun holdRightWithHopsNeverWins(level: Level, round: Int) {
        val v = holdRightWithHopsViolations(level, round)
        assertTrue(v.joinToString("\n"), v.isEmpty())
    }

    /** H7: the solution has to win with at least one of [SLOPS]. Empty: it does. */
    fun slopViolations(level: Level, round: Int, solution: Solution): List<String> {
        val results = SLOPS.map { s -> s to play(level, round, solution, s).world }
        if (results.any { it.second.state == WorldState.WON }) return emptyList()
        return listOf("${level.name.en} round ${round + 1}: needs pixel-perfect input, " +
            results.joinToString { (s, w) -> "${if (s.time > 0) "late" else "early"} ${w.state} at x=%.1f t=%.1f".format(w.player.box.cx, w.time) })
    }

    fun solutionToleratesSlop(level: Level, round: Int, solution: Solution): Boolean = slopViolations(level, round, solution).isEmpty()

    // ---------- reading a round ----------

    /** [actions] and everything nested in them ([Action.FakeWin.then]). */
    fun flatten(actions: List<Action>): List<Action> = actions.flatMap { a -> listOf(a) + if (a is Action.FakeWin) flatten(a.then) else emptyList() }

    private fun actions(round: Level) = flatten(round.start + round.traps.flatMap { it.actions })

    /** H5: [Action.Show]s in [round] (a level or one of its [Level.rounds]) that grow hidden spikes. */
    fun spikePopupCount(round: Level): Int = actions(round).count { a ->
        a is Action.Show && round.glyph(a.group).let { it != null && it.spike && it.hidden }
    }

    /**
     * H5 for W3: 1 if [round] ends on an overclocked floor ([Action.HeatSpike], also inside a fake win), else 0. The
     * final trap is the last one in the list, one that fires [Trigger.AtDoor], or one whose trigger lies within 3 tiles
     * of the door (where it starts or where a [Action.DoorTo] moves it), from either side.
     */
    fun heatSpikeFinaleCount(round: Level): Int {
        val heat = { t: Trap -> flatten(t.actions).any { it is Action.HeatSpike } }
        val start = round.map.grid.withIndex().firstNotNullOfOrNull { (y, row) -> row.indexOf('D').takeIf { it >= 0 }?.let { it to y } }
        val doors = listOfNotNull(start) + actions(round).filterIsInstance<Action.DoorTo>().map { it.col to it.row }
        val near = { x0: Float, x1: Float, y0: Float, y1: Float ->
            doors.any { (dx, dy) -> x0 <= dx + 1 + 3 && x1 >= dx - 3 && y0 <= dy + 1 + 3 && y1 >= dy - 3 }
        }
        val all = -100f to 100f
        val atDoor = round.traps.any { t ->
            heat(t) && when (val tr = t.trigger) {
                is Trigger.AtDoor -> true
                is Trigger.PastX -> near(tr.x, tr.x, all.first, all.second)
                is Trigger.BeforeX -> near(tr.x, tr.x, all.first, all.second)
                is Trigger.Landed -> near(tr.x0, tr.x1, all.first, all.second)
                is Trigger.Airborne -> near(tr.x0, tr.x1, all.first, all.second)
                is Trigger.Zone -> near(tr.x0, tr.x1, tr.y0, tr.y1)
                else -> false
            }
        }
        return if (atDoor || round.traps.lastOrNull()?.let(heat) == true) 1 else 0
    }

    /** Cards played ([Action.Play], also inside a fake win) in all rounds of [level]; bluffs do not count. */
    fun cards(level: Level): List<Card> = level.rounds.flatMap { r -> flatten(r.start + r.traps.flatMap { it.actions }).filterIsInstance<Action.Play>().map { it.card } }

    // ---------- rules over several levels ----------

    /** H5: per round at most one spike popup, per act at most [ACT_QUOTA] of [levels] (number → level) with any. */
    fun spikeQuotaViolations(levels: Map<Int, Level>, count: (Level) -> Int = ::spikePopupCount, what: String = "spike popups"): List<String> {
        val out = ArrayList<String>()
        for ((n, l) in levels) l.rounds.forEachIndexed { r, round ->
            val c = count(round)
            if (c > 1) out += "level $n round ${r + 1}: $c $what"
        }
        levels.keys.groupBy(::act).forEach { (a, ns) ->
            val with = ns.filter { n -> levels.getValue(n).rounds.any { count(it) > 0 } }.sorted()
            if (with.size > ACT_QUOTA) out += "act $a: ${with.size} levels with $what (max $ACT_QUOTA): $with"
        }
        return out
    }

    /** §7: no card more than [CARD_LIMIT] times per act among [levels]; GRAND_FINALE in a finale is free. */
    fun cardSpreadViolations(levels: Map<Int, Level>): List<String> = levels.keys.groupBy(::act).flatMap { (a, ns) ->
        ns.flatMap { n -> cards(levels.getValue(n)).filterNot { it == Card.GRAND_FINALE && isFinale(n) } }
            .groupingBy { it }.eachCount().filter { it.value > CARD_LIMIT }
            .map { (c, k) -> "act $a: $c played $k times (max $CARD_LIMIT)" }
    }

    /**
     * H6 on a §8 table: two neighbours (neither an act finale) share neither the main block nor the main twist; no code
     * shows up in 3 of 4 consecutive levels. Finales (they combine the act on purpose), the levels before [from] and
     * the [lead] mechanic of an act (act → code, exempt from "3 of 4" only) stay out. Missing rows are skipped.
     */
    fun h6Violations(table: Map<Int, Design>, from: Int = 1, lead: Map<Int, String> = emptyMap()): List<String> {
        val out = ArrayList<String>()
        val ns = table.keys.filter { it >= from && !isFinale(it) }.sorted()
        for (n in ns) {
            val a = table.getValue(n)
            val b = table[n + 1] ?: continue
            if (isFinale(n + 1)) continue
            if (a.mainBlock != null && a.mainBlock == b.mainBlock) out += "levels $n and ${n + 1} share the main block ${a.mainBlock}"
            if (a.mainTwist != null && a.mainTwist == b.mainTwist) out += "levels $n and ${n + 1} share the main twist ${a.mainTwist}"
        }
        for (n in table.keys.filter { it >= from }.sorted()) {
            val window = (n..n + 3).toList()
            if (window.any { it !in table }) continue
            val counted = window.filterNot(::isFinale)
            val codes = counted.flatMap { table.getValue(it).codes }.groupingBy { it }.eachCount()
            for ((code, k) in codes) if (k >= 3 && window.none { lead[act(it)] == code }) {
                out += "$code in ${counted.filter { code in table.getValue(it).codes }} (3 of levels $n–${n + 3})"
            }
        }
        return out.distinct()
    }

    /**
     * The table itself: valid codes, finales combine at least two blocks and two twists, at most two breathers per act,
     * a breather has at most one block, about half the rows of an act are puzzle rooms ([puzzleShareViolations]) and the
     * rotation caps hold ([tableRotationViolations]).
     */
    fun tableViolations(world: Int, table: Map<Int, Design>, expected: Set<Int>): List<String> {
        val out = ArrayList<String>()
        if (table.keys != expected) out += "rows ${table.keys.sorted()} instead of ${expected.sorted()}"
        for ((n, design) in table.toSortedMap()) {
            val bad = design.blocks.filterNot { it in BLOCKS } + design.twists.filterNot { it.substringBefore(':') in TWISTS }
            if (bad.isNotEmpty()) out += "level $n: unknown codes $bad"
            if (design.twists.isEmpty()) out += "level $n: no surprise"
            if (isFinale(n) && (design.breather || design.blocks.size < 2 || design.twists.size < 2)) out += "level $n: a finale combines 2+ blocks and 2+ twists ($design)"
            if (design.breather && design.blocks.size > 1) out += "level $n: a breather has at most one block"
        }
        table.filterValues { it.breather }.keys.groupBy(::act).forEach { (a, ns) -> if (ns.size > 2) out += "act $a: ${ns.size} breathers $ns (max 2)" }
        out += puzzleShareViolations(world, table)
        out += tableRotationViolations(world, table)
        return out
    }

    /** Levels of [table] whose blocks include one of [codes]. */
    fun withBlock(table: Map<Int, Design>, vararg codes: String) = table.filterValues { d -> d.blocks.any { it in codes } }.keys.sorted()

    /** W3: at most two levels in a row without a surprise of [OTHER_FAMILIES]. */
    fun otherFamilyViolations(table: Map<Int, Design>): List<String> = table.keys.sorted().mapNotNull { n ->
        val run = (n..n + 2).toList()
        if (run.any { it !in table }) null
        else if (run.none { m -> table.getValue(m).twists.any { it.substringBefore(':') in OTHER_FAMILIES } }) "levels $run: no surprise from another family"
        else null
    }

    // ---------- recipe v2 ----------

    /** H3: seconds the clean run may go without a real trap. */
    const val GAP = 3f
    /** H3: share of the clean run the player may stand still. */
    const val WAIT_SHARE = 0.4f
    /** H3: seconds of standing still in one go. */
    const val WAIT_STRETCH = 1.5f
    /** H2/H15: the patient player stands still this long. */
    const val PROBE_WAIT = 8f
    /** H15: the reckless player runs straight on this long after a trap went off. */
    const val PROBE_RUN = 2f
    /** H17: holding right must not die before a trap went off in this window. */
    const val FILLER_WINDOW = 2f
    /** H13: effect families per room (Level Devil's median); an act finale may combine one more. */
    const val MAX_FAMILIES = 2

    /**
     * What a trap that went off counts for (H3 density, H15 teeth, H17 filler deaths). The weight of a trap is the
     * heaviest of its actions ([FakeWin.then] included):
     *
     * - [LETHAL], it can kill the player who does not react: `Fall`, `Move`, `Chase`, `Saw`, `PathSaw`, `Blink` and
     *   `Clock` (a floor that starts to blink), `Laser` (a new beam, also a one-shot), `HeatSpike`, `Ghost`,
     *   `FrameCrack`; `Show` of a spike group; `Hide` of a solid group with a floor tile at or below the player's feet
     *   within 6 tiles (the floor vanishes; a cage or wall opening is no trap); `Power(on)` of a laser or live trace and
     *   `Power(off)` of a copper rail; `Circuit` turning a trace on or a rail off; `Belt` that pushes away from the door.
     * - [ROUTE], it changes the way: `DoorTo`, `Gravity`, `Swap` (also back), `Reroute`, `Power` of a portal or fan,
     *   `FanSet`, `Toggle`, `BitFlip`, `FakeWin`, `Extend`, `Undo`, `Tilt`, `Slope`.
     * - [NONE], a state change or talk: `Say`, `Play`, `Bluff`, `Shake`, `Pad`, `Portal`, `Heat`, `Heatsink`, `Fan`,
     *   `Flip`, `Roll`, `PauseTrap`; `Show` of a solid group (a bridge or block appears: the pad did its job); `Hide`
     *   of spikes; `Power(off)` of a laser or trace and `Power(on)` of a rail; `Power` of a belt; a `Belt` towards the door.
     */
    enum class Weight { NONE, ROUTE, LETHAL }

    /**
     * Traps that went off together, as the player sees them: traps with the same trigger that held at the same moment
     * are one moment (a door hopping along a [doorTrail], a page that drops and comes back). [time] is when its first
     * real action ran.
     */
    class Moment(val trigger: Trigger, val triggered: Float, var time: Float, var weight: Weight, val actions: MutableList<Action>) {
        val real get() = weight != Weight.NONE
        override fun toString() = "t=%.2f %s".format(time, actions.filterNot { it is Action.Say || it is Action.Play }.joinToString("+") { it::class.simpleName ?: "?" })
    }

    /** Adds the trap [s] that just sprang in [w] (an attempt at [round]) to [moments]. */
    fun addMoment(moments: MutableList<Moment>, s: World.Sprung, round: Level, w: World) {
        val acts = flatten(s.trap.actions)
        val weight = acts.maxOfOrNull { weigh(it, round, w) } ?: Weight.NONE
        val same = moments.lastOrNull { it.trigger == s.trap.trigger && abs(it.triggered - s.triggered) < 1e-4f }
        if (same == null) { moments += Moment(s.trap.trigger, s.triggered, s.time, weight, acts.toMutableList()); return }
        same.actions += acts
        if (!same.real && weight != Weight.NONE) same.time = s.time
        if (weight > same.weight) same.weight = weight
    }

    /** What [a] stands for: laser, portal, fan, belt, trace (live spike circuit) or rail (copper circuit). */
    private fun kind(round: Level, id: Char): String {
        val all = flatten(round.start + round.traps.flatMap { it.actions })
        return when {
            all.any { it is Action.Laser && it.id == id } -> "laser"
            all.any { it is Action.Portal && it.id == id } -> "portal"
            all.any { it is Action.Fan && it.id == id } -> "fan"
            all.any { it is Action.Belt && it.group == id } -> "belt"
            id.isUpperCase() -> "trace"
            else -> "rail"
        }
    }

    /** Tiles of [group] in the map of [round]. */
    private fun tiles(round: Level, group: Char) = round.map.grid.withIndex().flatMap { (y, row) -> row.withIndex().filter { it.value == group }.map { it.index to y } }

    private fun solid(round: Level, c: Char) = round.glyph(c)?.let { !it.spike } ?: false

    /** [group] has a tile one can stand on at or below the player's feet, within 6 tiles of them. */
    private fun floorNear(round: Level, group: Char, w: World): Boolean {
        val p = w.player.box
        return tiles(round, group).any { (x, y) ->
            (y == 0 || !solid(round, round.map.grid[y - 1][x])) && abs(x + 0.5f - p.cx) <= 6f && y >= p.b - 0.5f
        }
    }

    /** The weight of one action, see [Weight]; [w] is the world the moment it ran. */
    fun weigh(a: Action, round: Level, w: World): Weight = when (a) {
        is Action.Fall, is Action.Move, is Action.Chase, is Action.Saw, is Action.PathSaw, is Action.Blink, is Action.Clock,
        is Action.Laser, is Action.HeatSpike, is Action.Ghost, is Action.FrameCrack -> Weight.LETHAL
        is Action.Show -> if (round.glyph(a.group)?.spike == true) Weight.LETHAL else Weight.NONE
        is Action.Hide -> if (round.glyph(a.group)?.spike != true && floorNear(round, a.group, w)) Weight.LETHAL else Weight.NONE
        is Action.DoorTo, is Action.Gravity, is Action.Swap, is Action.Reroute, is Action.FanSet, is Action.Toggle, is Action.BitFlip,
        is Action.FakeWin, is Action.Extend, is Action.Undo, is Action.Tilt, is Action.Slope -> Weight.ROUTE
        is Action.Belt -> {
            val xs = tiles(round, a.group).map { it.first + 0.5f }
            val toDoor = w.door.tx + 0.6f - (if (xs.isEmpty()) w.player.box.cx else xs.average().toFloat())
            if (toDoor * a.speed < 0f) Weight.LETHAL else Weight.NONE
        }
        is Action.Power -> when (kind(round, a.id)) {
            "laser", "trace" -> if (a.on) Weight.LETHAL else Weight.NONE
            "portal", "fan" -> Weight.ROUTE
            "belt" -> Weight.NONE
            else -> if (a.on) Weight.NONE else Weight.LETHAL
        }
        is Action.Circuit -> if (a.group.isUpperCase() == a.on) Weight.LETHAL else Weight.NONE
        else -> Weight.NONE
    }

    /** One clean run of [round] of [level], with its moments and idle time. */
    fun cleanRun(level: Level, round: Int, solution: Solution): Bot = play(level, round, solution)

    /**
     * H3 (recipe v2, density over duration): the clean run wins, lasts at least [min] (also with idle waits skipped,
     * see [cleanRunViolations]), never goes more than [GAP] s without a real trap (from the start to the first, between
     * two, from the last to the door), and the player stands still at most [WAIT_SHARE] of it, at most [WAIT_STRETCH] s
     * in one go.
     */
    fun densityViolations(level: Level, round: Int, solution: Solution, min: Float): List<String> {
        val clean = cleanRun(level, round, solution)
        val where = "${level.name.en} round ${round + 1}"
        if (clean.world.state != WorldState.WON) return listOf("$where: the solution ends ${clean.world.state} at t=%.2f".format(clean.world.time))
        val out = ArrayList(cleanRunViolations(level, round, solution, min))
        val bot = measured(level, round, solution)
        val w = bot.world
        val run = if (bot.skipIdle) " (without its idle waits, which the room does not need)" else ""
        val marks = (listOf(0f) + bot.moments.filter { it.real }.map { it.time } + w.time).sorted()
        for ((a, b) in marks.zipWithNext()) if (b - a > GAP + 1e-3f) {
            val what = if (a == 0f && b == w.time) "start to door" else if (a == 0f) "start to first trap" else if (b == w.time) "last trap to door" else "between traps"
            out += "$where: no real trap for %.2f s (t=%.2f–%.2f, %s; max %.0f s)%s".format(b - a, a, b, what, GAP, run)
        }
        if (bot.idleTime > WAIT_SHARE * w.time + 1e-3f) {
            out += "$where: stands still %.1f %% of the run (%.2f of %.2f s, max %.0f %%)".format(100 * bot.idleTime / w.time, bot.idleTime, w.time, 100 * WAIT_SHARE)
        }
        if (bot.longestIdle > WAIT_STRETCH + 1e-3f) out += "$where: stands still %.2f s in one go (max %.1f s)".format(bot.longestIdle, WAIT_STRETCH)
        return out
    }

    /**
     * The run H3 measures: the clean run, or, when the solution still wins with every idle command skipped, that brisk
     * run (waits the room does not need do not stretch the gaps between traps).
     */
    fun measured(level: Level, round: Int, solution: Solution): Bot {
        val clean = cleanRun(level, round, solution)
        if (clean.world.state != WorldState.WON) return clean
        val brisk = Bot(level, round, skipIdle = true).apply(solution)
        return if (brisk.world.state == WorldState.WON && brisk.world.time < clean.world.time - 1e-3f) brisk else clean
    }

    /** The real traps of the run H3 measures ([measured]), for reports. */
    fun timeline(level: Level, round: Int, solution: Solution): String {
        val bot = measured(level, round, solution)
        val brisk = if (bot.skipIdle) ", idle waits skipped (not needed)" else ""
        return "run %.2f s (%s%s), still %.2f s, real traps: %s".format(bot.world.time, bot.world.state, brisk, bot.idleTime,
            bot.moments.filter { it.real }.joinToString().ifEmpty { "none" })
    }

    /** H12: a rotating mechanic and how many levels of an act may use it. */
    class Cap(val what: String, val max: Int, val counts: (Action) -> Boolean)

    /** H12, counted from the actions in the level code (all rounds), per act of 16. */
    val CAPS = listOf(
        Cap("door flees (DoorTo)", 1) { it is Action.DoorTo },
        Cap("pads/switches (Pad)", 3) { it is Action.Pad },
        Cap("timed laser gates", 3) { it is Action.Laser && it.off > 0f && it.off < 10f },
        Cap("Blink-wait", 3) { it is Action.Blink },
        Cap("gravity flip", 1) { it is Action.Gravity && it.flipped },
        Cap("controls swap", 2) { it is Action.Swap && it.on },
    )

    /** Every action of every round of [level], [Action.FakeWin.then] included. */
    fun levelActions(level: Level): List<Action> = level.rounds.flatMap { actions(it) }

    /** H12: per act, no mechanic of [CAPS] in more of [levels] (number → level) than its cap. */
    fun rotationViolations(levels: Map<Int, Level>): List<String> = levels.keys.groupBy(::act).toSortedMap().flatMap { (a, ns) ->
        CAPS.mapNotNull { cap ->
            val with = ns.filter { n -> levelActions(levels.getValue(n)).any(cap.counts) }.sorted()
            if (with.size > cap.max) "act $a: ${cap.what} in ${with.size} levels $with (max ${cap.max})" else null
        }
    }

    /**
     * H13: the effect families of a round, from its actions (start included) and its map. Talk, cards and pads are no
     * effect (pads are how the player switches an effect). A copper rail declared at the start that only pads switch is a
     * lock (puzzle block R1), not an effect either.
     *
     * drop: Fall, Hide of a solid group · spikes: Show of spikes · move: Move, Chase, Tilt, Slope · door: DoorTo, FakeWin,
     * Extend · saw: Saw, PathSaw · controls: Swap · gravity: Gravity, Flip · portal: Portal, Reroute, Power of a portal ·
     * belt: Belt, Power of a belt · laser: Laser, Power of a laser · blink: Blink · heat: Heat, Heatsink, HeatSpike ·
     * power: Clock, Toggle, BitFlip, Power of a circuit, a live trace, a Circuit set by a trap · fan: Fan, FanSet, Power of
     * a fan · meta: Undo, Ghost, PauseTrap, FrameCrack, Roll · secret: a hidden solid or bonk block in the map, Show of a
     * solid group.
     */
    fun families(round: Level): Set<String> {
        val out = LinkedHashSet<String>()
        val trapActions = flatten(round.traps.flatMap { it.actions })
        for (a in flatten(round.start) + trapActions) {
            when (a) {
                is Action.Fall -> out += "drop"
                is Action.Hide -> if (round.glyph(a.group)?.spike != true) out += "drop"
                is Action.Show -> out += if (round.glyph(a.group)?.spike == true) "spikes" else "secret"
                is Action.Move, is Action.Chase, is Action.Tilt, is Action.Slope -> out += "move"
                is Action.DoorTo, is Action.FakeWin, is Action.Extend -> out += "door"
                is Action.Saw, is Action.PathSaw -> out += "saw"
                is Action.Swap -> out += "controls"
                is Action.Gravity, is Action.Flip -> out += "gravity"
                is Action.Portal, is Action.Reroute -> out += "portal"
                is Action.Belt -> out += "belt"
                is Action.Laser -> out += "laser"
                is Action.Blink -> out += "blink"
                is Action.Heat, is Action.Heatsink, is Action.HeatSpike -> out += "heat"
                is Action.Clock, is Action.Toggle, is Action.BitFlip -> out += "power"
                is Action.Circuit -> if (a.group.isUpperCase() || a in trapActions) out += "power"
                is Action.Power -> out += when (kind(round, a.id)) { "rail", "trace" -> "power"; else -> kind(round, a.id) }
                is Action.Fan, is Action.FanSet -> out += "fan"
                is Action.Undo, is Action.Ghost, is Action.PauseTrap, is Action.FrameCrack, is Action.Roll -> out += "meta"
                else -> {}
            }
        }
        val secret = round.map.grid.any { row -> row.any { c -> round.glyph(c)?.let { !it.spike && (it.hidden || it.bonk) } == true } }
        if (secret) out += "secret"
        return out
    }

    /** H13: rounds of level [n] with more than [MAX_FAMILIES] effect families per room (an act finale: one more). */
    fun familyViolations(n: Int, level: Level): List<String> = level.rounds.withIndex().mapNotNull { (r, round) ->
        val f = families(round)
        val max = MAX_FAMILIES * level.rooms + if (isFinale(n)) 1 else 0
        if (f.size > max) "${level.name.en} round ${r + 1}: ${f.size} effect families $f (max $max)" else null
    }

    /**
     * H15, teeth: for every lethal moment of the clean run ([Weight.LETHAL]), two probes start from the step its trigger
     * held (the moment the player can still decide; a delayed trap runs its actions later).
     * The patient one stands still [PROBE_WAIT] s and then plays the rest of the solution; the reckless one keeps holding
     * the key it held (right if none) for [PROBE_RUN] s without jumping. If the patient one still wins *and* the reckless
     * one is still alive, the trap neither punishes waiting nor running: decoration. Route changes (door, gravity,
     * controls, portals) have their teeth in the new way, which H2 checks for the whole room, and are not probed.
     */
    fun teethViolations(level: Level, round: Int, solution: Solution): List<String> {
        val clean = cleanRun(level, round, solution)
        if (clean.world.state != WorldState.WON) return emptyList()
        val lethal = clean.moments.filter { it.weight == Weight.LETHAL }
        return lethal.mapNotNull { m ->
            // the runs are identical up to the moment the trap's trigger held: that is where the player decides
            val at = { b: Bot -> b.world.time >= m.triggered - 1e-4f }
            val patient = Bot(level, round, probe = Bot.Probe(Bot.Probe.Kind.WAIT, PROBE_WAIT, at))
            try { patient.solution() } catch (_: Bot.ProbeDone) {}
            val reckless = Bot(level, round, probe = Bot.Probe(Bot.Probe.Kind.RUN, PROBE_RUN, at))
            try { reckless.solution() } catch (_: Bot.ProbeDone) {}
            if (patient.world.state == WorldState.WON && reckless.ranThrough) {
                "${level.name.en} round ${round + 1}: the trap at $m has no teeth (standing still ${PROBE_WAIT.toInt()} s wins, running straight on survives)"
            } else null
        }
    }

    /**
     * H9: every rematch round of [level] is at least as long as round 1 (clean runs of [solutions], round 1 first) and
     * round 1's solution does not win it.
     */
    fun rematchViolations(level: Level, solutions: List<Solution>): List<String> {
        if (level.rounds.size < 2 || solutions.isEmpty()) return emptyList()
        val first = cleanRun(level, 0, solutions[0]).world
        val out = ArrayList<String>()
        for (r in 1 until level.rounds.size) {
            if (play(level, r, solutions[0]).world.state == WorldState.WON) out += "${level.name.en} round ${r + 1}: round 1's solution wins it"
            val s = solutions.getOrNull(r) ?: continue
            val w = cleanRun(level, r, s).world
            if (first.state == WorldState.WON && w.state == WorldState.WON && w.time < first.time - 0.05f) {
                out += "${level.name.en} round ${r + 1}: clean run %.2f s, shorter than round 1 (%.2f s)".format(w.time, first.time)
            }
        }
        return out
    }

    /** H17: holding right from the spawn dies within [FILLER_WINDOW] s before any real trap went off. */
    fun fillerDeathViolations(level: Level, round: Int): List<String> {
        val bot = Bot(level, round).right(FILLER_WINDOW)
        val w = bot.world
        if (w.state != WorldState.DEAD || bot.moments.any { it.real && it.time <= w.time }) return emptyList()
        return listOf("${level.name.en} round ${round + 1}: holding right dies at t=%.2f (x=%.1f) before any trap went off (filler death)".format(w.time, w.player.box.cx))
    }

    private fun hiddenBlock(round: Level, c: Char) = round.glyph(c)?.let { !it.spike && (it.hidden || it.bonk) } == true

    /**
     * H8, the card lint: what a card may sit on, by the other actions of its trap ([Action.FakeWin.then] included) or,
     * for a ghost block, by its trigger.
     *
     * COLLAPSE, CRUMBLE: Fall, Hide of a solid group, Blink (CRUMBLE), Move (COLLAPSE) · SINKING: Fall, Hide, Move ·
     * SPIKE_SEED: Show of spikes, Laser · HEADBUTT: Fall, Move · SHY_DOOR: DoorTo · DECOY: FakeWin, DoorTo, Reroute ·
     * UPSIDE_DOWN: Gravity(true), Flip · TWISTED: Swap(true) · DEVIL_SAW: Saw, PathSaw · GHOST_BLOCK: Show, Hide, Fall
     * or Move of a hidden solid or bonk group, or a trap triggered by touching one · STALKER: Chase · UNDO: Undo ·
     * ANNEX: Extend · SHORT_CIRCUIT: Power, Circuit, Clock, Toggle, BitFlip · BIT_FLIP: BitFlip, Toggle, Swap ·
     * OVERCLOCKED: HeatSpike, Heat · THROTTLE: Heat, Fan, FanSet, Laser · BACKDRAFT: Fan, FanSet, Belt, Power of a fan ·
     * BIOS: BitFlip, Toggle, Power, FakeWin, PauseTrap · GRAND_FINALE: anything.
     */
    fun cardFits(card: Card, trap: Trap, round: Level): Boolean {
        val acts = flatten(trap.actions)
        fun any(p: (Action) -> Boolean) = acts.any(p)
        val solidHide = { a: Action -> a is Action.Hide && round.glyph(a.group)?.spike != true }
        return when (card) {
            Card.COLLAPSE -> any { it is Action.Fall || it is Action.Move || solidHide(it) }
            Card.CRUMBLE -> any { it is Action.Fall || it is Action.Blink || solidHide(it) }
            Card.SINKING -> any { it is Action.Fall || it is Action.Move || solidHide(it) }
            Card.SPIKE_SEED -> any { (it is Action.Show && round.glyph(it.group)?.spike == true) || it is Action.Laser }
            Card.HEADBUTT -> any { it is Action.Fall || it is Action.Move }
            Card.SHY_DOOR -> any { it is Action.DoorTo }
            Card.DECOY -> any { it is Action.FakeWin || it is Action.DoorTo || it is Action.Reroute }
            Card.UPSIDE_DOWN -> any { (it is Action.Gravity && it.flipped) || it is Action.Flip }
            Card.TWISTED -> any { it is Action.Swap && it.on }
            Card.DEVIL_SAW -> any { it is Action.Saw || it is Action.PathSaw }
            Card.GHOST_BLOCK -> (trap.trigger as? Trigger.Touch)?.let { hiddenBlock(round, it.group) } == true || any { a ->
                when (a) {
                    is Action.Show -> hiddenBlock(round, a.group)
                    is Action.Hide -> hiddenBlock(round, a.group)
                    is Action.Fall -> hiddenBlock(round, a.group)
                    is Action.Move -> hiddenBlock(round, a.group)
                    else -> false
                }
            }
            Card.STALKER -> any { it is Action.Chase }
            Card.UNDO -> any { it is Action.Undo }
            Card.ANNEX -> any { it is Action.Extend }
            Card.SHORT_CIRCUIT -> any { it is Action.Power || it is Action.Circuit || it is Action.Clock || it is Action.Toggle || it is Action.BitFlip }
            Card.BIT_FLIP -> any { it is Action.BitFlip || it is Action.Toggle || it is Action.Swap }
            Card.OVERCLOCKED -> any { it is Action.HeatSpike || it is Action.Heat }
            Card.THROTTLE -> any { it is Action.Heat || it is Action.Fan || it is Action.FanSet || it is Action.Laser }
            Card.BACKDRAFT -> any { it is Action.Fan || it is Action.FanSet || it is Action.Belt || (it is Action.Power && kind(round, it.id) == "fan") }
            Card.BIOS -> any { it is Action.BitFlip || it is Action.Toggle || it is Action.Power || it is Action.FakeWin || it is Action.PauseTrap }
            Card.GRAND_FINALE, Card.BLUFF -> true
        }
    }

    /** H8: cards ([Action.Play], bluffs aside) that do not fit the trap they sit on, per round of [level]. */
    fun cardLintViolations(level: Level): List<String> = level.rounds.withIndex().flatMap { (r, round) ->
        round.traps.flatMap { t ->
            flatten(t.actions).filterIsInstance<Action.Play>().filterNot { cardFits(it.card, t, round) }.map { p ->
                val on = flatten(t.actions).filterNot { it is Action.Say || it is Action.Play }.joinToString("+") { it::class.simpleName ?: "?" }.ifEmpty { "nothing" }
                "${level.name.en} round ${r + 1}: ${p.card} sits on $on"
            }
        }
    }

    // ---------- the §8 table, recipe v2 ----------

    /** H12 on the table: per act at most one row with a door that flees (R6/U4), three with a switch (R1/R2/R4), two with swapped controls (U9). */
    fun tableRotationViolations(world: Int, table: Map<Int, Design>): List<String> {
        val caps = listOf(
            Triple("a fleeing door (R6/U4)", 1) { d: Design -> "R6" in d.blocks || "U4" in d.twists },
            Triple("a switch (R1/R2/R4)", 3) { d: Design -> d.blocks.any { it in setOf("R1", "R2", "R4") } },
            Triple("swapped controls (U9)", 2) { d: Design -> "U9" in d.twists },
        )
        return table.keys.filter { ruled(world, it) }.groupBy(::act).toSortedMap().flatMap { (a, ns) ->
            caps.mapNotNull { (what, max, has) ->
                val with = ns.filter { has(table.getValue(it)) }.sorted()
                if (with.size > max) "act $a: ${with.size} rows with $what $with (max $max)" else null
            }
        }
    }

    /**
     * Recipe v2, puzzle in half the levels: per act, of the ruled rows that are neither ★ nor finale, between a third and
     * two thirds are puzzle rooms (with a block R1–R12); the others are trap rooms ("–").
     */
    fun puzzleShareViolations(world: Int, table: Map<Int, Design>): List<String> =
        table.keys.filter { ruled(world, it) && !isFinale(it) && !table.getValue(it).breather }.groupBy(::act).toSortedMap().mapNotNull { (a, ns) ->
            val puzzles = ns.filter { table.getValue(it).blocks.isNotEmpty() }
            if (3 * puzzles.size < ns.size || 3 * puzzles.size > 2 * ns.size) "act $a: ${puzzles.size} of ${ns.size} rows are puzzle rooms (about half)" else null
        }
}
