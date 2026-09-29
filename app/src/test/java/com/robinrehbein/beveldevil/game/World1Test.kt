package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** One scripted solution per level of World 1, played with the real physics. */
class World1Test {
    private fun b(n: Int) = Bot(World1.levels[n - 1])

    @Test
    fun worldHas128LevelsThatParse() {
        assertEquals(128, World1.levels.size)
        World1.levels.forEach { World(it) }
    }

    @Test
    fun standingStillIsSafeForTwoSeconds() {
        World1.levels.forEach { l -> Bot(l).wait(2.2f).expect(WorldState.PLAYING) }
    }

    @Test
    fun namesAndIntrosAreFilledInBothLanguages() {
        World1.levels.forEach { l ->
            assertTrue(l.name.en.isNotBlank() && l.name.de.isNotBlank() && l.intro.en.isNotBlank() && l.intro.de.isNotBlank())
        }
    }

    @Test
    fun everyTrapLevelPlaysACard() {
        World1.levels.forEachIndexed { i, l ->
            if (l.traps.isNotEmpty()) assertTrue("level ${i + 1} plays no card", l.traps.any { t -> t.actions.any { it is Action.Play } })
        }
    }

    /** The obvious thing to do (run, or jump the way you would expect) must not win these levels. */
    @Test
    fun naiveRunsAreNotEnough() {
        val naive: Map<Int, (Bot) -> Bot> = mapOf(
            13 to { b -> b.left(8f) }, 14 to { b -> b.right(8f) }, 18 to { b -> b.right(8f) }, 19 to { b -> b.right(8f) },
            20 to { b -> b.right(8f) }, 21 to { b -> b.right(8f) }, 22 to { b -> b.hopR(11.9f).right(2f) },
            23 to { b -> b.right(8f) }, 24 to { b -> b.hopR(12.0f).right(2f) }, 28 to { b -> b.right(8f) },
            29 to { b -> b.right(8f) }, 30 to { b -> b.right(8f) }, 31 to { b -> b.rightTo(10.3f).rightJump(0.3f).landRight().wait(3f) },
            33 to { b -> b.right(8f) }, 34 to { b -> b.right(8f) }, 35 to { b -> b.wait(14f) }, 37 to { b -> b.wait(8f) },
            38 to { b -> b.right(8f) }, 39 to { b -> b.right(8f) }, 40 to { b -> b.right(8f) }, 42 to { b -> b.right(8f) },
            43 to { b -> b.hopR(11.0f).right(2f) }, 44 to { b -> b.right(8f) }, 45 to { b -> b.right(8f) },
            46 to { b -> b.right(8f) }, 48 to { b -> b.right(8f) },
            49 to { b -> b.left(8f) }, 50 to { b -> b.right(8f) }, 51 to { b -> b.right(8f) }, 52 to { b -> b.right(8f) },
            54 to { b -> b.right(8f) }, 57 to { b -> b.right(8f) }, 58 to { b -> b.right(8f) },
            59 to { b -> b.rightTo(12.5f).wait(8f) }, 60 to { b -> b.right(8f) }, 61 to { b -> b.right(8f) },
            62 to { b -> b.right(8f) }, 64 to { b -> b.right(8f) },
            65 to { b -> b.right(8f) }, 66 to { b -> b.right(8f) }, 67 to { b -> b.right(8f) }, 70 to { b -> b.hopR(11.7f).right(2f) },
            72 to { b -> b.right(8f) }, 73 to { b -> b.right(8f) }, 74 to { b -> b.right(8f) }, 76 to { b -> b.right(8f) },
            79 to { b -> b.right(8f) }, 80 to { b -> b.right(8f) },
        )
        val winners = naive.filter { (n, play) -> val bot = Bot(World1.levels[n - 1]); play(bot); bot.world.state != WorldState.DEAD }
        assertTrue("naive play survives or wins in levels ${winners.keys}", winners.isEmpty())
    }

    // ---- 1-12: the original levels ----
    @Test fun level01() = b(1).rightTo(17.4f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level02() = b(2).rightTo(8.6f).rightJump(0.3f).rightTo(22.4f).rightJump(0.3f).right(3f).expect(WorldState.WON)
    @Test fun level03() = b(3).rightTo(26f).wait(0.4f)
        .leftTo(24.9f).leftJump(0.35f).wait(0.3f)
        .leftTo(20.7f).leftJump(0.35f).wait(0.3f)
        .leftTo(14.7f).leftJump(0.35f).wait(0.3f)
        .leftTo(8.7f).leftJump(0.35f).left(1.5f)
        .expect(WorldState.WON)
    @Test fun level04() = b(4).rightTo(12.9f).wait(1.2f)
        .rightTo(13.2f).rightJump(0.35f).right(0.3f).wait(0.2f)
        .rightTo(21.4f).wait(1.2f).rightTo(21.9f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)
    @Test fun level05() = b(5).rightTo(10f).wait(1f)
        .rightTo(15.8f).rightJump(0.25f).rightTo(20.8f).rightJump(0.25f).right(3f)
        .expect(WorldState.WON)
    @Test fun level06() = b(6).rightTo(7.2f)
        .leftKeyRightTo(11.2f).leftJump(0.35f).left(0.3f).leftKeyRightTo(17.8f)
        .rightTo(20.3f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)
    @Test fun level07() = b(7).rightTo(12.8f).rightJump(0.3f).rightTo(20.8f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level08() = b(8).rightTo(18.5f).jump(0.3f).wait(0.5f)
        .leftTo(16.8f).wait(0.2f).rightJump(0.35f).right(0.2f).rightJump(0.35f).right(3f)
        .expect(WorldState.WON)
    @Test fun level09() = b(9).rightTo(6.6f).rightJump(0.3f).rightTo(11.6f).rightJump(0.3f)
        .rightTo(16.4f).rightJump(0.3f).rightTo(21.3f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level10() = b(10).rightTo(22.5f).wait(1.5f).leftTo(19.2f).leftJump(0.3f).leftTo(13.9f).leftJump(0.3f).left(3f)
        .expect(WorldState.WON)
    @Test fun level11() = b(11).right(4.5f).expect(WorldState.WON)
    @Test fun level12() = b(12).rightTo(7.3f).rightJump(0.35f).rightTo(15.4f).rightJump(0.3f).rightTo(24.7f).wait(1.2f).left(3f)
        .expect(WorldState.WON)

    // ---- 13-16 ----
    @Test fun level13() = b(13).hopL(22.3f).hopL(11.0f).left(3f).expect(WorldState.WON)
    @Test fun level14() = b(14).rightTo(10.2f).wait(1.2f).rightTo(20.7f).wait(1.2f).right(3f).expect(WorldState.WON)
    @Test fun level15() = b(15).hopR(5.7f).hopR(10.7f).hopR(15.7f).hopR(20.5f).hopR(25.7f).right(1f).expect(WorldState.WON)
    @Test fun level16() = b(16).rightTo(10.2f).rightJump(0.3f).landRight().wait(1.9f).hopR(15.7f).right(2f).expect(WorldState.WON)

    // ---- 17-32 ----
    @Test fun level17() = b(17).right(4f).expect(WorldState.WON)
    @Test fun level18() = b(18).hopR(10.2f).hopR(15.6f).hopR(20.5f).hopR(25.4f).right(1f).expect(WorldState.WON)
    @Test fun level19() = b(19).rightTo(10.2f).wait(2.5f).rightTo(20.7f).wait(2.5f).right(3f).expect(WorldState.WON)
    @Test fun level20() = b(20).hopR(5.7f).hopR(10.7f).hopR(18.7f).hopR(25.7f).right(1f).expect(WorldState.WON)
    @Test fun level21() = b(21).waitUntil(7f).right(2f).expect(WorldState.WON)
    @Test fun level22() = b(22).rightTo(10.8f).wait(2.6f).hopR(12.1f).right(2f).expect(WorldState.WON)
    @Test fun level23() = b(23).waitUntil(3f).untilSaw(4f).rightJump(0.35f).landRight().waitUntil(9f).right(3f).expect(WorldState.WON)
    @Test fun level24() = b(24).rightTo(12.9f).rightJump(0.02f).right(0.6f).right(3f).expect(WorldState.WON)
    @Test fun level25() = b(25).rightTo(16.8f).wait(0.1f).jump(0.3f).wait(0.4f)
        .rightTo(19.8f).wait(0.1f).jump(0.3f).wait(0.4f)
        .leftTo(14.4f).wait(0.3f).rightJump(0.3f).landRight().wait(0.2f)
        .leftTo(16.6f).wait(0.2f).rightJump(0.3f).landRight().wait(0.2f)
        .leftTo(19.5f).wait(0.2f).rightJump(0.3f).landRight().right(3f)
        .expect(WorldState.WON)
    @Test fun level26() = b(26).left(1.2f).hopR(23.6f).right(2f).expect(WorldState.WON)
    @Test fun level27() = b(27).rightTo(26.3f).wait(1.6f).leftTo(3.6f).wait(0.3f).rightJump(0.35f).landRight()
        .hopR(9.7f).hopR(16.7f).hopR(23.7f).right(2f).expect(WorldState.WON)
    @Test fun level28() = b(28).hopR(7.7f).hopR(15.7f).right(2f).expect(WorldState.WON)
    @Test fun level29() = b(29).rightTo(25.5f).rightJump(0.35f).right(1f).expect(WorldState.WON)
    @Test fun level30() = b(30).hopR(12.0f).hopR(17.6f).right(2f).expect(WorldState.WON)
    @Test fun level31() = b(31).rightTo(10.3f).rightJump(0.3f).landRight().wait(0.7f).hopR(17.4f).right(2f).expect(WorldState.WON)
    @Test fun level32() = b(32).rightTo(6.8f).wait(1.0f).hopR(7.0f).hopR(16.5f).hopR(23.2f).right(2f).expect(WorldState.WON)

    // ---- 33-48 ----
    @Test fun level33() = b(33).hopR(20.3f).right(2f).expect(WorldState.WON)
    @Test fun level34() = b(34).rightTo(12.4f).rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level35() = b(35).hopR(5.7f).hopR(11.7f).hopR(17.7f).hopR(23.7f).right(2f).expect(WorldState.WON)
    @Test fun level36() = b(36).waitUntil(8.3f).hopR(12.2f).hopR(15.7f).hopR(17.7f).hopR(19.7f).right(2f).expect(WorldState.WON)
    @Test fun level37() = b(37).waitUntil(3.3f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level38() = b(38).rightTo(7.1f).wait(0.9f).rightTo(13.1f).wait(0.9f).rightTo(19.1f).wait(0.9f).rightTo(24.1f).wait(0.9f).right(3f)
        .expect(WorldState.WON)
    @Test fun level39() = b(39).waitUntil(2.6f).hopR(5.7f).hopR(10.7f).hopR(15.7f).hopR(20.7f).hopR(25.7f).right(1f).expect(WorldState.WON)
    @Test fun level40() = b(40).rightTo(8.1f).wait(2.3f).rightJump(0.35f).landRight().hopR(14.6f).right(2f).expect(WorldState.WON)
    @Test fun level41() = b(41).hopS(8.6f).hopS(14.4f).hopS(19.6f).left(0.14f).rightTo(26.2f).rightJump(0.35f).right(2f).expect(WorldState.WON)
    @Test fun level42() = b(42).rightTo(10.7f).rightJump(0.35f).landRight().rightTo(16.7f).rightJump(0.35f).landRight().right(2f).expect(WorldState.WON)
    @Test fun level43() = b(43).rightTo(12.9f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level44() = b(44).hopR(9.4f).hopR(15.9f).hopR(22.9f).right(1f).expect(WorldState.WON)
    @Test fun level45() = b(45).waitUntil(11.5f).right(2f).expect(WorldState.WON)
    @Test fun level46() = b(46).rightTo(5f).jump(0.3f).wait(0.6f).hopR(13f).right(4f).expect(WorldState.WON)
    @Test fun level47() = b(47).rightTo(10.5f).jump(0.3f).wait(0.4f).right(4f).expect(WorldState.WON)
    @Test fun level48() = b(48).hopR(10.9f).rightTo(16.1f).wait(1.2f).hopR(23.5f).right(2f).expect(WorldState.WON)

    // ---- 49-64 ----
    @Test fun level49() = b(49).hopL(21.3f).hopL(13.3f).left(1f).wait(3.5f).hopR(10.7f).hopR(18.7f).right(3f).expect(WorldState.WON)
    @Test fun level50() = b(50).waitUntil(5.5f).hopR(10.4f).right(3f).expect(WorldState.WON)
    @Test fun level51() = b(51).rightUntilSaw(4.5f).rightJump(0.35f).landRight().right(3f).expect(WorldState.WON)
    @Test fun level52() = b(52).rightTo(6f).waitUntil(1.85f).right(3.5f).expect(WorldState.WON)
    @Test fun level53() = b(53).rightTo(12.0f).rightJump(0.06f).wait(0.6f).hopS(12.5f).hopS(19.6f).left(2f).expect(WorldState.WON)
    @Test fun level54() = b(54).rightJump(0.35f).landRight().hopR(18.2f).right(2f).expect(WorldState.WON)
    @Test fun level55() = b(55).hopR(10.7f).hopR(16.5f).rightTo(23.5f).wait(2.5f).hopL(20.5f).hopL(13.3f).hopL(8.5f).left(3f).expect(WorldState.WON)
    @Test fun level56() = b(56).rightTo(4.6f).wait(1.0f).hopS(17.7f).left(3f).expect(WorldState.WON)
    @Test fun level57() = b(57).rightTo(6.1f).hopS(16.7f).left(3f).expect(WorldState.WON)
    @Test fun level58() = b(58).rightTo(15.6f).hopR(17.5f).right(2f).expect(WorldState.WON)
    @Test fun level59() = b(59).rightTo(12.5f).wait(1.75f).hopR(15.7f).right(2f).expect(WorldState.WON)
    @Test fun level60() = b(60).untilSaw(2.0f).jump(0.35f).wait(0.4f).hopR(5.7f).hopR(10.7f).hopR(15.7f).hopR(20.7f).hopR(25.7f).right(1f).expect(WorldState.WON)
    @Test fun level61() = b(61).rightTo(4.6f).wait(1.0f).hopR(7.5f).rightTo(13.1f).wait(1.0f).hopR(14.4f).rightTo(20.1f).wait(1.0f).hopR(23.5f).right(2f).expect(WorldState.WON)
    @Test fun level62() = b(62).rightTo(8.3f).rightJump(0.35f).landRight().waitUntil(4.9f).hopR(17.6f).waitUntil(7.2f).right(3f).expect(WorldState.WON)
    @Test fun level63() = b(63).rightTo(20.5f).wait(2.6f).hopL(15.3f).hopL(8.5f).left(3f).expect(WorldState.WON)
    @Test fun level64() = b(64).hopR(3.0f).hopR(7.7f).hopR(12.7f).hopR(19.7f).right(2f).expect(WorldState.WON)

    // ---- 65-80 ----
    @Test fun level65() = b(65).waitUntil(5.2f).hopR(15.7f).hopR(23.7f).right(2f).expect(WorldState.WON)
    @Test fun level66() = b(66).rightTo(9.6f).wait(3.6f).hopR(21.5f).right(2f).expect(WorldState.WON)
    @Test fun level67() = b(67).rightTo(16.6f).landRight().rightJump(0.35f).right(3f).expect(WorldState.WON)
    @Test fun level68() = b(68).hopL(13.8f).leftTo(9.5f).leftJump(0.35f).landLeft().leftTo(6.5f).leftJump(0.35f).landLeft().left(2f).expect(WorldState.WON)
    @Test fun level69() = b(69).rightTo(8.5f).rightJump(0.35f).landRight().wait(1.9f).hopS(15.7f).left(3f).expect(WorldState.WON)
    @Test fun level70() = b(70).rightTo(9.1f).waitUntil(5.6f).hopR(11.7f).right(2f).expect(WorldState.WON)
    @Test fun level71() = b(71).hopR(2.4f).hopR(6.4f).hopR(10.4f).hopR(14.4f).hopR(18.4f).hopR(22.4f).right(2f).expect(WorldState.WON)
    @Test fun level72() = b(72).hopR(5.7f).hopR(10.7f).hopR(16.7f).hopR(21.7f).hopR(25.2f).right(1f).expect(WorldState.WON)
    @Test fun level73() = b(73).rightTo(12.5f).waitUntil(2.8f).rightTo(20.6f).waitUntil(5.2f).right(3f).expect(WorldState.WON)
    @Test fun level74() = b(74).rightTo(9.6f).rightJump(0.35f).landRight().hopR(14.5f).hopR(21.5f).right(2f).expect(WorldState.WON)
    @Test fun level75() = b(75).hopR(8.7f).hopR(13.5f).hopR(19.7f).right(3f).expect(WorldState.WON)
    @Test fun level76() = b(76).rightTo(5.6f).waitUntil(4.85f).rightJump(0.35f).landRight().waitUntil(7.7f).hopR(21.5f).right(3f).expect(WorldState.WON)
    @Test fun level77() = b(77).waitUntil(5.2f).hopS(15.7f).hopS(21.5f).left(3f).expect(WorldState.WON)
    @Test fun level78() = b(78).rightTo(20.2f).rightJump(0.35f).landRight().hopR(26.5f).right(2f).expect(WorldState.WON)
    @Test fun level79() = b(79).hopR(5.0f).hopR(9.7f).hopR(15.7f).hopR(21.7f).hopR(27.7f).right(1f).expect(WorldState.WON)
    @Test fun level80() = b(80).hopR(12.5f).hopR(18.5f).hopR(25.7f).right(2f).expect(WorldState.WON)
}
