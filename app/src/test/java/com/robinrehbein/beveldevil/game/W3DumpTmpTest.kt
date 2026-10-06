package com.robinrehbein.beveldevil.game

import org.junit.Test
import java.io.File

fun Bot.at(tag: String): Bot {
    val b = world.player.box
    File("/tmp/claude-0/-home-user-bevel-devil/e7fb7e56-3e5d-5200-85e3-f028f08c7c64/scratchpad/at.txt").appendText("%s t=%.2f x=%.2f y=%.2f g=%s %s\n".format(tag, world.time, b.cx, b.b, world.player.grounded, world.state))
    return this
}

class W3DumpTmpTest {
    @Test
    fun dump() {
        val sb = StringBuilder()
        for (n in World3DesignTest.REBUILT.sorted()) {
            val lvl = World3.levels[n - 1]
            val sols = World3DesignTest.SOLUTIONS.getValue(n)
            for (r in lvl.rounds.indices) {
                sb.append("3-$n round ${r + 1}: ")
                try {
                    sb.append(DesignRules.timeline(lvl, r, sols[r])).append("\n    ")
                    sb.append(DesignRules.signature(lvl, sols[r], r)).append("\n")
                } catch (e: Throwable) { sb.append("ERROR $e\n") }
            }
        }
        File("/tmp/claude-0/-home-user-bevel-devil/e7fb7e56-3e5d-5200-85e3-f028f08c7c64/scratchpad/w3fix-dump.txt").writeText(sb.toString())
    }

    @Test
    fun slopTrace() {
        val sb = StringBuilder()
        val n = (System.getenv("SLOP_LEVEL") ?: File("/tmp/claude-0/-home-user-bevel-devil/e7fb7e56-3e5d-5200-85e3-f028f08c7c64/scratchpad/slop.cfg").readText().trim().split(",")[0]).toInt()
        val r = File("/tmp/claude-0/-home-user-bevel-devil/e7fb7e56-3e5d-5200-85e3-f028f08c7c64/scratchpad/slop.cfg").readText().trim().split(",")[1].toInt()
        for (sl in listOf(Slop.NONE) + DesignRules.SLOPS) {
            val b = DesignRules.play(World3.levels[n - 1], r, World3DesignTest.SOLUTIONS.getValue(n)[r], sl)
            try { b.expect(WorldState.DEAD); sb.append("SLOP $sl: DEAD?\n") } catch (e: AssertionError) { sb.append("SLOP $sl: " + (e.message ?: "").lines().takeLast(40).joinToString(" | ") + "\n") }
        }
        File("/tmp/claude-0/-home-user-bevel-devil/e7fb7e56-3e5d-5200-85e3-f028f08c7c64/scratchpad/slop.out").writeText(sb.toString())
    }
}
