package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldSelectTest {
    private class Prog(override var unlocked: Int = 1) : Progress {
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        val best = HashMap<Int, Int>()
        override fun bestDeaths(level: Int): Int? = best[level]
        override fun saveBest(level: Int, deaths: Int) { best[level] = deaths }
        override fun cardFound(card: Card) = false
        override fun findCard(card: Card) {}
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private fun Game.tapOn(h: Hit) = tap(h.x + 1f, h.y + 1f)
    private fun select(p: Prog) = Game(p, silent).also { it.tapOn(Ui.titlePlay) ; assertEquals(Screen.SELECT, it.screen) }
    /** Puts Bevel into the door and waits for the clear/end screen. */
    private fun Game.win() {
        repeat(400) {
            val w = world!!
            if (w.state == WorldState.PLAYING) {
                val d = w.door.box
                w.player.box.x = d.x + d.w / 2 - w.player.box.w / 2
                w.player.box.y = d.y + d.h - w.player.box.h
            }
            update(1f / 120f)
            if (screen != Screen.PLAY) return
        }
    }

    @Test fun worldsAreLaidOutBackToBack() {
        assertEquals(listOf(0, 128, 256), Worlds.all.map { it.firstLevel })
        assertEquals(256, Levels.all.size)
        assertSame(World1.levels[0], Levels.all[0])
        assertSame(World1.levels[127], Levels.all[127])
        assertSame(World2.levels[0], Levels.all[128])
        assertEquals(0, Worlds.get(3).size)
    }

    @Test fun indexMapping() {
        assertEquals(1, Worlds.of(0).number)
        assertEquals(1, Worlds.of(127).number)
        assertEquals(2, Worlds.of(128).number)
        assertEquals(2, Worlds.of(255).number)
        assertEquals("1-1", Worlds.label(0))
        assertEquals("1-128", Worlds.label(127))
        assertEquals("2-1", Worlds.label(128))
        assertEquals("2-17", Worlds.label(144))
        assertEquals(17, Worlds.local(144))
        // out of range: nearest world that has levels, never the empty one
        assertEquals(2, Worlds.of(256).number)
        assertEquals(1, Worlds.of(-1).number)
        for (i in Levels.all.indices) assertTrue(i in Worlds.of(i))
    }

    @Test fun clearingWorldOneRoutesThroughWorldTwoIntro() {
        val p = Prog(128)
        val g = Game(p, silent)
        g.startLevel(127)
        assertEquals("1-128", g.levelLabel)
        g.win()
        assertEquals(Screen.CLEAR, g.screen)
        assertEquals(129, p.unlocked)
        g.tapOn(Ui.clearNext)
        assertEquals(Screen.WORLD_INTRO, g.screen)
        assertEquals(2, g.worldInfo.number)
        g.update(0.5f)
        g.tap(100f, 100f)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(128, g.levelIndex)
        assertEquals("2-1", g.levelLabel)
    }

    @Test fun clearInsideAWorldGoesStraightOn() {
        val g = Game(Prog(5), silent)
        g.startLevel(4)
        g.win()
        g.tapOn(Ui.clearNext)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(5, g.levelIndex)
    }

    @Test fun endOnlyAfterTheLastLevel() {
        val p = Prog(256)
        val g = Game(p, silent)
        g.startLevel(255)
        g.win()
        assertEquals(Screen.END, g.screen)
        assertEquals(256, p.unlocked)
    }

    @Test fun oldSaveThatFinishedWorldOneUnlocksWorldTwo() {
        // before world 2 existed, unlocking was capped at 128
        val p = Prog(128).apply { best[127] = 4 }
        Game(p, silent)
        assertEquals(129, p.unlocked)
        val q = Prog(7).apply { best[5] = 1 }
        Game(q, silent)
        assertEquals(7, q.unlocked)
    }

    @Test fun selectOpensOnPageOfHighestUnlockedLevel() {
        val g = select(Prog(40)) // highest unlocked: 1-40, on page 3
        assertEquals(1, g.selWorld.number)
        assertEquals(2, g.selPage)
        val h = select(Prog(128 + 20))
        assertEquals(2, h.selWorld.number)
        assertEquals(1, h.selPage)
        assertEquals(8, g.pages())
    }

    @Test fun tileOnPageThreeStartsTheRightLevel() {
        val g = select(Prog(40))
        g.tapOn(Ui.levelTile(3))
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(35, g.levelIndex)
        assertEquals("1-36", g.levelLabel)
    }

    @Test fun lockedTilesDoNothing() {
        val g = select(Prog(40))
        g.tapOn(Ui.levelTile(8)) // 1-41
        assertEquals(Screen.SELECT, g.screen)
        g.tapOn(Ui.levelTile(7)) // 1-40, the next level
        assertEquals(39, g.levelIndex)
    }

    @Test fun arrowsTurnPagesAndClamp() {
        val g = select(Prog(1))
        assertEquals(0, g.selPage)
        g.tapOn(Ui.pagePrev)
        assertEquals(0, g.selPage)
        repeat(20) { g.tapOn(Ui.pageNext) }
        assertEquals(7, g.selPage)
        g.tapOn(Ui.pagePrev)
        assertEquals(6, g.selPage)
        assertEquals(96, g.selLevel(0))
        g.tapOn(Ui.levelTile(0))
        assertEquals(Screen.SELECT, g.screen)
        g.tapOn(Ui.pageDot(2, g.pages()))
        assertEquals(2, g.selPage)
        g.page(-5)
        assertEquals(0, g.selPage)
    }

    @Test fun worldTabsSwitchOnlyWhenUnlocked() {
        val n = Worlds.all.size
        val g = select(Prog(40))
        g.tapOn(Ui.worldTab(1, n))
        assertEquals(1, g.selWorld.number)
        g.tapOn(Ui.worldTab(2, n))
        assertEquals(1, g.selWorld.number)
        assertFalse(g.worldOpen(Worlds.get(3)))

        val h = select(Prog(128 + 20))
        assertEquals(2, h.selWorld.number)
        h.tapOn(Ui.worldTab(0, n))
        assertEquals(1, h.selWorld.number)
        assertEquals(7, h.selPage) // world 1 fully unlocked: its last page
        h.tapOn(Ui.worldTab(1, n))
        assertEquals(1, h.selPage)
        h.tapOn(Ui.levelTile(0)) // 2-17
        assertEquals(144, h.levelIndex)
        assertTrue(h.back()); assertEquals(Screen.PAUSE, h.screen)
        h.tapOn(Ui.pauseLevels)
        assertEquals(2, h.selWorld.number)
        assertTrue(h.back()); assertEquals(Screen.TITLE, h.screen)
    }

    @Test fun uiHitsDoNotOverlap() {
        val n = Worlds.all.size
        val hits = listOf(Ui.back, Ui.selectAlbum, Ui.pagePrev, Ui.pageNext) +
            (0 until n).map { Ui.worldTab(it, n) } + (0 until 8).map { Ui.pageDot(it, 8) } + (0 until Ui.PAGE).map { Ui.levelTile(it) }
        for (a in hits) {
            assertTrue("$a on stage", a.x >= 0 && a.y >= 0 && a.x + a.w <= Ui.W && a.y + a.h <= Ui.H)
            for (b in hits) if (a !== b) assertFalse("$a overlaps $b", a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h)
        }
    }
}
