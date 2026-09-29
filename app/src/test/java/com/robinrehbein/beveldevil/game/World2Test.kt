package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * World 2 checks: every level parses, the start is safe, every trap level plays a card,
 * and one scripted Bot solution per level plays it through with the real physics.
 */
class World2Test {
    private fun bot(i: Int) = Bot(World2.levels[i - 1])

    @Test
    fun has128Levels() = assertEquals(128, World2.levels.size)

    @Test
    fun allLevelsParse() {
        World2.levels.forEach { World(it) }
    }

    @Test
    fun standingStillIsSafeAtTheStart() {
        World2.levels.forEach { l -> Bot(l).wait(2f).expect(WorldState.PLAYING) }
    }

    @Test
    fun everyTrapLevelPlaysACard() {
        World2.levels.forEach { l ->
            val cards = l.traps.flatMap { it.actions }.filterIsInstance<Action.Play>()
            assertTrue("${l.name.en} has traps but plays no card", l.traps.isEmpty() || cards.isNotEmpty())
            assertTrue("${l.name.en} plays more than one card", cards.size <= 1)
        }
    }

    @Test
    fun textsAreFilledInBothLanguages() {
        World2.levels.forEach { l ->
            assertTrue(l.name.en.isNotBlank() && l.name.de.isNotBlank())
            assertTrue(l.intro.en.isNotBlank() && l.intro.de.isNotBlank())
        }
    }

    @Test
    fun levelNamesAreUnique() {
        assertEquals(World2.levels.size, World2.levels.map { it.name.en }.toSet().size)
    }

    @Test
    fun level001() = bot(1)
        .right(1.20f).rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level002() = bot(2)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.25f).right(0.03f).rightJump(0.55f)
        .right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level003() = bot(3)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).left(0.60f).right(0.25f).left(0.03f)
        .left(0.25f).leftJump(0.55f).leftJump(0.55f).left(1.20f).leftJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level004() = bot(4)
        .right(1.20f).rightJump(0.40f).leftJump(0.12f).rightJump(0.55f).left(0.03f).left(0.03f)
        .rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level005() = bot(5)
        .right(1.20f).right(0.10f).right(0.10f).right(0.03f).left(0.03f).leftJump(0.12f)
        .left(0.03f).left(0.03f).right(0.03f).right(0.03f).rightJump(0.40f).rightJump(0.55f)
        .right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level006() = bot(6)
        .right(0.60f).rightJump(0.55f).rightJump(0.40f).leftJump(0.40f).leftJump(0.55f).leftJump(0.55f)
        .left(0.60f).left(0.25f).leftJump(0.55f).left(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level007() = bot(7)
        .right(0.60f).right(0.03f).right(0.03f).left(0.03f).left(0.03f).left(0.03f)
        .right(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).right(0.25f).right(0.10f)
        .left(0.03f).left(0.03f).right(0.03f).left(0.03f).left(0.03f).right(0.03f)
        .left(0.03f).left(0.03f).rightJump(0.55f).right(0.25f).right(0.03f).right(0.03f)
        .left(0.03f).left(0.03f).left(0.03f).right(0.03f).right(0.03f).left(0.10f)
        .rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level008() = bot(8)
        .right(0.60f).right(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).rightJump(0.55f)
        .leftJump(0.12f).right(0.03f).left(0.03f).left(0.03f).right(0.03f).left(0.03f)
        .right(0.03f).rightJump(0.12f).rightJump(0.12f).leftJump(0.12f).left(0.03f).right(0.03f)
        .jump(0.40f).rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.03f)
        .expect(WorldState.WON)

    @Test
    fun level009() = bot(9)
        .rightJump(0.55f).leftJump(0.55f).leftJump(0.55f).leftJump(0.55f).left(0.25f).rightJump(0.55f)
        .right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level010() = bot(10)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level011() = bot(11)
        .right(1.20f).right(0.60f).right(0.10f).right(0.10f).left(0.03f).left(0.03f)
        .left(0.03f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level012() = bot(12)
        .rightJump(0.40f).right(0.60f).rightJump(0.55f).rightJump(0.40f).wait(2.00f).right(0.60f)
        .right(0.25f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level013() = bot(13)
        .rightTo(9.50f).rightJump(0.40f).right(0.03f).rightJump(0.55f).rightJump(0.12f).left(0.03f)
        .left(0.03f).right(0.03f).left(0.03f).rightJump(0.12f).leftTo(14.00f).leftJump(0.40f)
        .expect(WorldState.WON)

    @Test
    fun level014() = bot(14)
        .right(0.60f).right(0.25f).rightJump(0.40f).leftJump(0.12f).rightJump(0.40f).leftJump(0.12f)
        .rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level015() = bot(15)
        .right(0.60f).right(0.10f).right(0.03f).rightJump(0.12f).jump(0.40f).rightJump(0.55f)
        .rightJump(0.55f).right(0.03f).right(0.10f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level016() = bot(16)
        .rightJump(0.55f).right(0.03f).leftJump(0.12f).right(0.10f).right(0.03f).right(0.03f)
        .rightJump(0.55f).right(0.03f).right(0.03f).right(0.03f).right(0.03f).right(0.03f)
        .right(0.03f).rightJump(0.55f).rightJump(0.40f).right(1.20f).left(0.10f)
        .expect(WorldState.WON)

    @Test
    fun level017() = bot(17)
        .right(0.60f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.25f).rightJump(0.55f)
        .right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level018() = bot(18)
        .right(1.20f).left(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).right(0.60f)
        .rightJump(0.55f).rightJump(0.55f).right(0.10f)
        .expect(WorldState.WON)

    @Test
    fun level019() = bot(19)
        .right(0.60f).rightJump(0.55f).left(0.10f).left(0.03f).right(0.03f).left(0.03f)
        .rightJump(0.40f).leftJump(0.12f).rightJump(0.40f).leftJump(0.12f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level020() = bot(20)
        .right(1.20f).left(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).rightJump(0.55f)
        .rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level021() = bot(21)
        .rightTo(9.50f).rightJump(0.40f).leftJump(0.12f).rightJump(0.12f).left(0.03f).left(0.03f)
        .right(0.03f).left(0.03f).jump(0.40f).right(0.03f).jump(0.40f).right(0.03f)
        .jump(0.25f).right(0.03f).left(0.03f).left(0.03f).jump(0.25f).rightJump(0.25f)
        .rightTo(23.75f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level022() = bot(22)
        .rightTo(9.50f).rightJump(0.25f).rightJump(0.40f).rightJump(0.55f).rightJump(0.12f).rightJump(0.40f)
        .rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level023() = bot(23)
        .right(1.20f).right(1.20f).rightJump(0.25f).right(0.03f).right(0.03f).right(0.03f)
        .rightJump(0.55f).leftJump(0.40f).right(0.03f).right(0.03f).jump(0.25f).leftJump(0.40f)
        .leftJump(0.55f).left(1.20f).leftJump(0.55f).leftJump(0.40f).right(0.03f).right(0.25f)
        .rightJump(0.55f).right(1.20f).right(0.60f).rightJump(0.55f).right(0.03f)
        .expect(WorldState.WON)

    @Test
    fun level024() = bot(24)
        .right(0.60f).rightJump(0.55f).left(0.03f).left(0.03f).right(0.03f).rightJump(0.55f)
        .rightJump(0.55f).right(0.03f).right(0.03f).leftJump(0.12f).rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level025() = bot(25)
        .right(1.20f).right(0.60f).rightJump(0.12f).right(0.10f).right(0.03f).right(0.03f)
        .leftJump(0.12f).left(0.03f).left(0.03f).left(0.10f).leftJump(0.12f).right(0.03f)
        .left(0.03f).left(0.03f).left(0.03f).left(0.03f).left(0.03f).left(0.60f)
        .right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level026() = bot(26)
        .right(0.60f).rightJump(0.55f).left(0.10f).rightJump(0.55f).rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level027() = bot(27)
        .right(0.25f).rightJump(0.55f).left(0.03f).left(0.03f).left(0.03f).rightJump(0.40f)
        .leftJump(0.12f).left(0.03f).rightJump(0.40f).leftJump(0.12f).rightJump(0.40f).right(0.10f)
        .left(0.03f).left(0.03f).left(0.03f).rightJump(0.40f).leftJump(0.12f).rightJump(0.55f)
        .right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level028() = bot(28)
        .left(0.25f).leftJump(0.40f).rightJump(0.12f).leftJump(0.40f).left(0.10f).rightJump(0.12f)
        .leftJump(0.55f).left(0.03f).leftJump(0.12f).left(0.10f).left(0.10f).rightJump(0.12f)
        .right(0.03f).leftJump(0.55f).leftJump(0.55f).left(0.10f).left(0.03f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level029() = bot(29)
        .right(0.60f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.10f).right(0.03f)
        .right(0.03f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level030() = bot(30)
        .right(1.20f).right(0.60f).right(0.25f).rightJump(0.55f).right(0.10f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level031() = bot(31)
        .right(0.60f).right(0.25f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f)
        .right(0.03f)
        .expect(WorldState.WON)

    @Test
    fun level032() = bot(32)
        .right(1.20f).rightJump(0.55f).rightJump(0.55f).left(0.10f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level033() = bot(33)
        .right(1.20f).rightJump(0.55f).right(0.25f).right(0.03f).rightJump(0.12f).right(0.03f)
        .leftJump(0.12f).rightJump(0.55f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level034() = bot(34)
        .rightJump(0.40f).right(0.03f).right(0.03f).right(0.03f).rightJump(0.55f).right(0.60f)
        .rightJump(0.40f).right(0.03f).right(0.03f).left(0.03f).left(0.03f).right(0.03f)
        .left(0.03f).left(0.10f).left(0.03f).left(0.25f).left(0.03f).leftJump(0.55f)
        .left(1.20f).leftJump(0.40f).rightJump(0.55f).right(0.60f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level035() = bot(35)
        .right(1.20f).right(1.20f).rightJump(0.55f).right(0.25f).jump(0.16f).right(0.03f)
        .left(0.03f).leftJump(0.12f).rightJump(0.40f).right(0.03f).left(0.03f).wait(2.00f)
        .expect(WorldState.WON)

    @Test
    fun level036() = bot(36)
        .rightJump(0.55f).right(0.03f).rightJump(0.55f).right(0.10f).right(0.03f).rightJump(0.12f)
        .right(0.03f).rightJump(0.40f).rightJump(0.55f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level037() = bot(37)
        .rightJump(0.55f).rightJump(0.55f).right(1.20f).rightJump(0.40f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level038() = bot(38)
        .rightTo(8.6f).rightJump(0.4f).rightTo(14.6f).rightJump(0.4f).rightTo(20.6f).rightJump(0.4f)
        .rightTo(27f).wait(2.5f).leftTo(23.5f).leftJump(0.4f).leftTo(17.5f).leftJump(0.4f)
        .leftTo(11.5f).leftJump(0.4f).left(2f)
        .expect(WorldState.WON)

    @Test
    fun level039() = bot(39)
        .right(0.60f).right(0.10f).right(0.10f).rightJump(0.12f).left(0.10f).left(0.03f)
        .right(0.03f).left(0.03f).leftJump(0.12f).left(0.03f).right(0.60f).right(0.25f)
        .right(0.25f).left(0.03f).left(0.03f).left(0.03f).left(0.03f).right(0.60f)
        .right(0.25f).left(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).right(0.03f)
        .expect(WorldState.WON)

    @Test
    fun level040() = bot(40)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.10f)
        .left(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).right(0.10f)
        .expect(WorldState.WON)

    @Test
    fun level041() = bot(41)
        .right(1.20f).rightJump(0.55f).leftJump(0.12f).left(0.03f).left(0.03f).left(0.03f)
        .left(0.03f).right(0.03f).right(0.03f).rightJump(0.40f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level042() = bot(42)
        .rightTo(5.6f).leftTo(2.4f).rightTo(4.7f).rightJump(0.4f).rightTo(13.2f).rightJump(0.5f)
        .right(2f)
        .expect(WorldState.WON)

    @Test
    fun level043() = bot(43)
        .right(0.60f).right(0.10f).right(0.03f).right(0.03f).right(1.20f).right(0.25f)
        .right(0.03f).right(0.03f).right(0.10f).right(1.20f).left(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level044() = bot(44)
        .right(1.20f).rightJump(0.25f).right(0.10f).rightJump(0.55f).right(0.10f).rightJump(0.55f)
        .right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level045() = bot(45)
        .rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f)
        .right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level046() = bot(46)
        .right(0.60f).rightJump(0.25f).rightJump(0.12f).left(0.10f).right(0.03f).left(0.03f)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).jump(0.40f).left(0.25f).leftJump(0.55f)
        .leftJump(0.55f).leftJump(0.55f).left(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level047() = bot(47)
        .right(0.60f).rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.10f).rightJump(0.55f)
        .rightJump(0.55f).right(0.10f)
        .expect(WorldState.WON)

    @Test
    fun level048() = bot(48)
        .right(1.20f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level049() = bot(49)
        .right(0.60f).rightJump(0.55f).rightJump(0.55f).right(0.10f).left(0.03f).left(0.03f)
        .right(0.03f).left(0.03f).right(0.10f).rightJump(0.40f).left(0.10f).rightJump(0.55f)
        .right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level050() = bot(50)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level051() = bot(51)
        .right(0.60f).rightJump(0.55f).rightJump(0.55f).leftJump(0.55f).leftJump(0.55f).leftJump(0.55f)
        .right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level052() = bot(52)
        .rightJump(0.55f).rightJump(0.55f).right(0.10f).rightJump(0.40f).wait(0.20f).right(0.03f)
        .rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level053() = bot(53)
        .rightTo(28.8f).wait(5.6f).jump(0.4f).wait(2f).wait(3f)
        .expect(WorldState.WON)

    @Test
    fun level054() = bot(54)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.12f).left(0.10f).left(0.03f)
        .left(0.03f).left(0.03f).rightJump(0.55f).rightJump(0.55f).right(0.10f)
        .expect(WorldState.WON)

    @Test
    fun level055() = bot(55)
        .right(0.60f).right(0.10f).wait(1.00f).left(0.25f).left(0.10f).leftJump(0.55f)
        .leftJump(0.55f).rightJump(0.55f).right(0.25f).right(0.60f).left(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level056() = bot(56)
        .rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f)
        .right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level057() = bot(57)
        .right(0.60f).right(0.10f).right(0.03f).rightJump(0.12f).jump(0.40f).rightJump(0.55f)
        .rightJump(0.55f).right(0.25f).right(0.10f).right(0.10f).rightJump(0.12f).wait(0.50f)
        .expect(WorldState.WON)

    @Test
    fun level058() = bot(58)
        .right(1.20f).left(0.03f).left(0.03f).right(0.03f).right(0.03f).right(0.03f)
        .right(0.03f).left(0.10f).right(0.03f).right(0.03f).right(1.20f).right(1.20f)
        .right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level059() = bot(59)
        .right(0.60f).rightJump(0.55f).rightJump(0.55f).right(1.20f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level060() = bot(60)
        .rightJump(0.40f).right(0.03f).right(0.03f).right(0.03f).rightJump(0.55f).rightJump(0.55f)
        .right(0.25f).rightJump(0.55f).right(0.60f).left(0.03f).left(0.03f).right(0.03f)
        .left(0.03f).right(0.03f).left(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level061() = bot(61)
        .right(0.60f).rightJump(0.12f).right(0.25f).rightJump(0.12f).leftJump(0.12f).left(0.03f)
        .left(0.03f).left(0.03f).right(0.03f).jump(0.16f).left(0.03f).rightJump(0.12f)
        .jump(0.25f).left(0.03f).left(0.03f).left(0.03f).jump(0.25f).right(0.03f)
        .right(0.03f).right(0.03f).jump(0.25f).left(0.03f).left(0.03f).left(0.03f)
        .jump(0.25f).right(0.03f).right(0.03f).right(0.03f).jump(0.25f).rightJump(0.25f)
        .rightJump(0.55f).right(0.03f).right(0.03f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level062() = bot(62)
        .rightJump(0.25f).rightJump(0.12f).rightJump(0.40f).leftJump(0.12f).left(0.03f).left(0.03f)
        .right(0.03f).left(0.03f).left(0.03f).left(0.03f).rightJump(0.40f).right(0.60f)
        .leftJump(0.12f).leftJump(0.12f).wait(2.00f).right(0.60f).rightJump(0.55f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level063() = bot(63)
        .right(0.25f).left(0.60f).left(0.25f).leftJump(0.55f).right(0.10f).right(0.25f)
        .left(0.03f).left(0.03f).leftJump(0.55f).right(0.03f).right(0.03f).right(0.03f)
        .leftJump(0.55f).rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level064() = bot(64)
        .right(0.60f).rightJump(0.55f).right(0.25f).right(0.25f).rightJump(0.55f).rightJump(0.55f)
        .wait(0.50f)
        .expect(WorldState.WON)

    @Test
    fun level065() = bot(65)
        .right(0.25f).rightJump(0.55f).right(0.60f).rightJump(0.55f).rightJump(0.55f).left(0.03f)
        .left(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level066() = bot(66)
        .rightJump(0.55f).leftJump(0.40f).right(0.60f).left(0.03f).left(0.03f).left(0.03f)
        .left(0.03f).left(0.03f).rightJump(0.55f).right(0.25f).rightJump(0.55f).right(0.10f)
        .right(0.25f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level067() = bot(67)
        .right(0.60f).rightJump(0.55f).right(0.25f).right(0.25f).left(0.03f).left(0.03f)
        .left(0.03f).left(0.03f).rightJump(0.55f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level068() = bot(68)
        .rightJump(0.55f).right(0.10f).right(0.03f).left(0.03f).left(0.03f).left(0.03f)
        .left(0.03f).right(0.03f).left(0.03f).right(0.25f).left(0.03f).left(0.03f)
        .right(0.10f).rightJump(0.55f).right(0.10f).rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level069() = bot(69)
        .left(0.60f).leftJump(0.55f).leftJump(0.40f).leftJump(0.55f).leftJump(0.40f).leftJump(0.55f)
        .left(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level070() = bot(70)
        .right(0.60f).right(0.25f).rightJump(0.55f).right(0.25f).right(0.10f).rightJump(0.55f)
        .right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level071() = bot(71)
        .rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f)
        .right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level072() = bot(72)
        .right(0.60f).rightJump(0.55f).rightJump(0.55f).right(0.10f).leftJump(0.55f).right(0.03f)
        .right(0.03f).right(0.03f).right(0.03f).leftJump(0.40f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level073() = bot(73)
        .right(0.60f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f).right(0.10f)
        .right(0.03f).right(0.03f).left(0.03f).left(0.03f).left(0.03f).left(0.03f)
        .rightJump(0.55f).right(0.10f)
        .expect(WorldState.WON)

    @Test
    fun level074() = bot(74)
        .right(0.10f).right(0.03f).rightJump(0.12f).left(0.03f).leftJump(0.12f).left(0.03f)
        .leftJump(0.12f).left(0.03f).rightJump(0.55f).rightJump(0.40f).right(0.60f).rightJump(0.55f)
        .rightJump(0.55f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level075() = bot(75)
        .right(0.60f).right(0.03f).rightJump(0.55f).right(0.60f).rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level076() = bot(76)
        .rightTo(5.6f).wait(2.0f).rightTo(9.6f).wait(0.8f).rightTo(14.6f).wait(0.8f)
        .rightTo(19.6f).wait(0.8f).rightTo(27f).right(1.5f)
        .expect(WorldState.WON)

    @Test
    fun level077() = bot(77)
        .rightTo(5.5f).wait(2.9f).rightJump(0.12f).wait(0.5f).rightTo(9.4f).rightJump(0.3f)
        .wait(0.3f).rightTo(13.9f).rightJump(0.3f).wait(0.3f).rightTo(18.9f).rightJump(0.3f)
        .rightTo(26.0f).rightJump(0.4f).right(1f)
        .expect(WorldState.WON)

    @Test
    fun level078() = bot(78)
        .right(0.25f).right(0.10f).rightJump(0.40f).left(0.10f).left(0.03f).rightJump(0.40f)
        .left(0.10f).left(0.10f).rightJump(0.55f).left(0.03f).left(0.03f).left(0.03f)
        .left(0.03f).left(0.03f).rightJump(0.55f).left(0.03f).left(0.03f).left(0.03f)
        .left(0.03f).rightJump(0.55f).left(0.03f).left(0.03f).left(0.03f).left(0.03f)
        .rightJump(0.40f).left(0.10f).right(0.03f).left(0.03f).left(0.03f).left(0.03f)
        .rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level079() = bot(79)
        .right(0.60f).right(0.10f).right(0.03f).left(0.03f).left(0.03f).left(0.03f)
        .left(0.03f).left(0.03f).right(0.03f).rightJump(0.55f).right(0.25f).right(0.03f)
        .right(0.03f).left(0.03f).left(0.03f).left(0.03f).right(0.03f).right(0.03f)
        .left(0.10f).rightJump(0.55f).right(0.25f).right(0.03f).left(0.03f).left(0.03f)
        .right(0.03f).right(0.03f).left(0.10f).right(0.03f).left(0.03f).left(0.03f)
        .rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level080() = bot(80)
        .rightTo(8.25f).leftJump(0.25f).leftJump(0.40f).leftJump(0.12f).left(0.03f).left(0.03f)
        .right(0.10f).left(0.03f).leftJump(0.25f).right(0.03f).left(0.03f).rightJump(0.40f)
        .leftJump(0.55f).leftJump(0.12f).left(0.10f).jump(0.16f).right(0.03f).right(0.03f)
        .leftJump(0.55f).rightTo(30.75f)
        .expect(WorldState.WON)

    @Test
    fun level081() = bot(81)
        .rightJump(0.25f).right(0.03f).right(0.03f).left(0.03f).left(0.03f).right(0.03f)
        .right(0.03f).jump(0.25f).right(0.25f).rightJump(0.40f).rightJump(0.40f).rightJump(0.12f)
        .right(0.03f).right(0.03f).left(0.03f).leftJump(0.12f).jump(0.25f).rightJump(0.40f)
        .rightJump(0.25f).wait(0.20f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level082() = bot(82)
        .right(0.25f).rightJump(0.55f).leftJump(0.55f).rightJump(0.55f).leftJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level083() = bot(83)
        .right(1.20f).rightJump(0.55f).rightJump(0.40f).right(0.03f).left(0.03f).jump(0.25f)
        .right(0.25f).rightJump(0.40f).rightJump(0.40f).rightJump(0.40f)
        .expect(WorldState.WON)

    @Test
    fun level084() = bot(84)
        .rightTo(9.0f).jump(0.3f).wait(0.3f).leftTo(7.0f).rightJump(0.4f).wait(0.3f)
        .rightTo(9.9f).wait(0.25f).jump(0.3f).wait(0.4f).leftTo(9.3f).wait(0.2f)
        .jump(0.2f).rightJump(0.3f).wait(0.3f).rightTo(11.9f).wait(0.25f).jump(0.3f)
        .wait(0.4f).leftTo(11.3f).wait(0.2f).jump(0.2f).rightJump(0.3f).wait(0.3f)
        .leftTo(12.6f).wait(0.2f).rightJump(0.4f).right(2f)
        .expect(WorldState.WON)

    @Test
    fun level085() = bot(85)
        .right(0.60f).right(1.20f).left(0.60f).leftJump(0.40f).leftJump(0.40f).leftJump(0.40f)
        .leftJump(0.55f).leftJump(0.40f).leftJump(0.40f)
        .expect(WorldState.WON)

    @Test
    fun level086() = bot(86)
        .right(0.60f).rightJump(0.55f).rightJump(0.40f).rightJump(0.55f).right(1.20f).left(0.25f)
        .left(0.60f).leftJump(0.55f).right(0.03f).rightJump(0.12f).right(0.03f).right(0.03f)
        .right(0.03f).right(0.03f).right(0.03f).left(0.03f).leftJump(0.12f).left(0.03f)
        .left(0.03f).right(0.03f).left(0.10f).left(0.03f).leftJump(0.55f).leftJump(0.40f)
        .right(0.60f).rightJump(0.55f).right(0.25f).rightJump(0.40f).rightJump(0.25f).right(0.10f)
        .left(0.03f).leftJump(0.12f).left(0.03f).rightJump(0.40f).left(0.03f).leftJump(0.12f)
        .rightJump(0.55f).rightJump(0.40f).left(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level087() = bot(87)
        .left(0.60f).leftJump(0.55f).left(0.25f).leftJump(0.55f).leftJump(0.55f).left(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level088() = bot(88)
        .rightJump(0.55f).rightJump(0.40f).right(1.20f).right(0.60f).rightJump(0.40f).right(0.03f)
        .leftJump(0.12f).left(0.03f).rightJump(0.40f)
        .expect(WorldState.WON)

    @Test
    fun level089() = bot(89)
        .right(1.20f).right(0.60f).rightJump(0.12f).right(0.03f).right(0.03f).left(0.03f)
        .left(0.03f).jump(0.16f).left(0.03f).left(0.03f).rightJump(0.55f).right(0.10f)
        .rightJump(0.12f).rightJump(0.40f).rightJump(0.55f).right(0.03f)
        .expect(WorldState.WON)

    @Test
    fun level090() = bot(90)
        .right(1.20f).rightJump(0.12f).jump(0.16f).left(0.03f).left(0.03f).left(0.03f)
        .rightJump(0.55f).right(1.20f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level091() = bot(91)
        .leftJump(0.55f).leftJump(0.55f).leftJump(0.55f).leftJump(0.55f).left(0.25f).leftJump(0.55f)
        .right(0.03f)
        .expect(WorldState.WON)

    @Test
    fun level092() = bot(92)
        .right(1.20f).rightJump(0.55f).rightJump(0.55f).right(0.60f).leftJump(0.12f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level093() = bot(93)
        .right(1.20f).rightJump(0.55f).right(0.25f).right(0.60f).right(0.03f).right(0.03f)
        .rightJump(0.12f).left(0.10f).rightJump(0.12f).right(0.03f).leftJump(0.12f).jump(0.16f)
        .left(0.03f).right(0.03f).left(0.03f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level094() = bot(94)
        .rightJump(0.40f).right(0.03f).right(0.03f).rightJump(0.55f).rightJump(0.55f).right(1.20f)
        .jump(0.16f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level095() = bot(95)
        .rightTo(26.5f).wait(3f).wait(3f).wait(1.5f).right(1.5f)
        .expect(WorldState.WON)

    @Test
    fun level096() = bot(96)
        .rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.55f).right(0.10f).rightJump(0.55f)
        .right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level097() = bot(97)
        .right(1.20f).right(0.60f).right(0.25f).right(0.10f).right(0.10f).rightJump(0.25f)
        .rightJump(0.40f)
        .expect(WorldState.WON)

    @Test
    fun level098() = bot(98)
        .rightJump(0.40f).rightJump(0.40f).rightJump(0.40f).right(1.20f).right(0.25f).rightJump(0.25f)
        .right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level099() = bot(99)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(1.20f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level100() = bot(100)
        .rightJump(0.55f).left(0.03f).left(0.03f).left(0.03f).left(0.03f).rightJump(0.55f)
        .wait(0.20f).left(0.03f).rightJump(0.40f).leftJump(0.55f).left(0.25f).left(0.03f)
        .left(0.03f).right(0.03f).right(0.03f).right(0.03f).right(0.03f).leftJump(0.55f)
        .rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level101() = bot(101)
        .rightJump(0.55f).rightJump(0.40f).leftJump(0.12f).left(0.03f).left(0.03f).left(0.03f)
        .rightJump(0.55f).rightJump(0.12f).right(0.10f).jump(0.16f).right(0.03f).rightJump(0.40f)
        .leftJump(0.12f).left(0.03f).left(0.03f).left(0.03f).rightJump(0.55f).rightJump(0.12f)
        .jump(0.40f).rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level102() = bot(102)
        .rightJump(0.55f).rightJump(0.55f).right(0.10f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f)
        .right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level103() = bot(103)
        .right(0.60f).rightJump(0.40f).left(0.03f).left(0.03f).rightJump(0.40f).rightJump(0.55f)
        .rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level104() = bot(104)
        .left(0.03f).left(0.03f).left(0.03f).left(0.03f).right(0.03f).leftJump(0.12f)
        .right(0.03f).leftJump(0.55f).leftJump(0.40f).leftJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level105() = bot(105)
        .right(0.10f).right(0.03f).right(0.03f).left(0.03f).right(0.03f).left(0.03f)
        .rightJump(0.12f).left(0.10f).rightJump(0.12f).right(0.03f).leftJump(0.12f).jump(0.16f)
        .left(0.03f).right(0.03f).left(0.03f).rightJump(0.55f).rightJump(0.55f).right(0.25f)
        .leftJump(0.55f).leftJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level106() = bot(106)
        .right(1.20f).rightJump(0.55f).right(0.25f).rightJump(0.55f).rightJump(0.12f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level107() = bot(107)
        .leftJump(0.55f).right(1.20f).right(0.60f).rightJump(0.55f).right(0.25f).rightJump(0.55f)
        .expect(WorldState.WON)

    @Test
    fun level108() = bot(108)
        .jump(0.16f).left(0.03f).left(0.03f).left(0.03f).right(0.03f).left(0.03f)
        .right(0.03f).left(0.03f).right(0.03f).rightJump(0.55f).rightJump(0.55f).rightJump(0.55f)
        .rightJump(0.12f).rightJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level109() = bot(109)
        .right(0.60f).right(0.25f).right(0.03f).right(0.03f).rightJump(0.12f).wait(2.00f)
        .right(0.10f).right(0.03f).rightJump(0.55f).right(0.60f)
        .expect(WorldState.WON)

    @Test
    fun level110() = bot(110)
        .rightJump(0.55f).rightJump(0.55f).rightJump(0.55f).right(0.25f).right(0.10f).rightJump(0.55f)
        .rightJump(0.55f).right(0.25f)
        .expect(WorldState.WON)

    @Test
    fun level111() = bot(111)
        .right(0.60f).rightJump(0.55f).right(0.03f).right(0.03f).rightJump(0.12f).rightJump(0.12f)
        .left(0.03f).rightJump(0.12f).leftJump(0.55f).left(0.03f).left(0.03f).right(0.03f)
        .left(0.03f).left(0.03f).leftJump(0.55f).right(1.20f)
        .expect(WorldState.WON)

    @Test
    fun level112() = bot(112)
        .rightTo(8.25f).leftJump(0.25f).leftJump(0.40f).leftJump(0.12f).left(0.03f).left(0.03f)
        .right(0.03f).right(0.03f).leftJump(0.25f).right(0.03f).right(0.03f).right(0.03f)
        .right(0.03f).right(0.03f).right(0.03f).right(0.03f).left(0.03f).right(0.03f)
        .left(0.03f).right(0.03f).jump(0.25f).right(0.03f).leftJump(0.55f).leftJump(0.12f)
        .rightTo(27.25f).rightJump(0.55f)
        .expect(WorldState.WON)

}
