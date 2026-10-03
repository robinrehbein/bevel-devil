package com.robinrehbein.beveldevil.game

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DevilQuipsTest {
    private class Prog : Progress {
        override var unlocked = 99
        override var saveVersion = SAVE_VERSION
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        override var tiltSensor = true
        override var totalDeaths = 0
        override fun bestDeaths(level: Int): Int? = null
        override fun saveBest(level: Int, deaths: Int) {}
        override fun cardFound(card: Card) = false
        override fun findCard(card: Card) {}
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private val quips get() = DevilQuips.pools.flatten().flatten().flatMap { listOf(it.en, it.de) }.toSet()

    @After fun reset() { Lang.german = true }

    private fun died(g: Game) {
        g.world!!.events += Event.Died(1f, 1f)
        g.update(0.016f)
    }

    private fun game(p: Progress = Prog(), level: Int = 0) = Game(p, silent).also { it.startLevel(level); it.update(0.05f); assertEquals(0, it.deaths) }

    @Test
    fun quipsAppearExactlyAtThreeSixAndTen() {
        for (german in listOf(true, false)) {
            Lang.german = german
            val g = game()
            val shown = mutableListOf<Int>()
            for (n in 1..12) {
                died(g)
                if (g.bubble in quips) shown += n
            }
            assertEquals(listOf(3, 6, 10), shown)
        }
    }

    @Test
    fun aQuipDoesNotBlockTheWorld() {
        val g = game()
        repeat(3) { died(g) }
        assertTrue(g.bubble in quips)
        assertNotNull(g.world)
    }

    @Test
    fun poolsAreFilledInBothLanguagesAndFit() {
        assertEquals(3, DevilQuips.pools.size)
        for (world in DevilQuips.pools) {
            assertEquals(3, world.size)
            for (tier in world) {
                assertTrue(tier.size >= 3)
                for (t in tier) {
                    assertTrue(t.en.isNotBlank() && t.de.isNotBlank())
                    assertTrue(t.en, t.en.length <= DevilQuips.MAX_LEN)
                    assertTrue(t.de, t.de.length <= DevilQuips.MAX_LEN)
                }
            }
        }
    }

    @Test
    fun deterministicAndNeverTheSameTwiceInARow() {
        for (lvl in listOf(0, 5, Worlds.all[1].firstLevel, Worlds.all[2].firstLevel)) {
            assertEquals(DevilQuips.pick(lvl, 3), DevilQuips.pick(lvl, 3))
            for (d in DevilQuips.milestones) {
                val a = DevilQuips.pick(lvl, d)!!
                assertNotEquals(a, DevilQuips.pick(lvl, d, a))
            }
        }
        assertNull(DevilQuips.pick(0, 4))
    }

    @Test
    fun quipsFollowTheWorld() {
        Lang.german = false
        val w3 = Worlds.all[2].firstLevel
        assertTrue(DevilQuips.pools[2].flatten().any { it.en == DevilQuips.pick(w3, 3) })
        assertTrue(DevilQuips.pools[0].flatten().any { it.en == DevilQuips.pick(0, 3) })
    }

    @Test
    fun noQuipWhileATrapLineShows() {
        val g = game()
        died(g); died(g)
        g.world!!.events += Event.Say(T("Trap line", "Fallenzeile"))
        g.update(0.016f)
        died(g)
        assertTrue(g.bubble !in quips)
    }

    @Test
    fun noQuipInTheFinale() {
        val g = game(level = Levels.all.lastIndex)
        repeat(10) { died(g); assertTrue(g.bubble !in quips) }
    }

    @Test
    fun totalCounterCountsEveryDeathAndRestartAndPersists() {
        val p = Prog()
        val g = game(p)
        died(g); died(g)
        assertEquals(2, p.totalDeaths)
        assertEquals(2, g.totalDeaths)
        g.back()
        assertEquals(Screen.PAUSE, g.screen)
        g.tap(Ui.pauseRestart.x + 1f, Ui.pauseRestart.y + 1f)
        assertEquals(3, p.totalDeaths)
        assertEquals(3, Game(p, silent).totalDeaths)
    }
}
