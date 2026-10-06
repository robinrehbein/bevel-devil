package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldSelectTest {
    private class Prog(override var unlocked: Int = 1, override var saveVersion: Int = SAVE_VERSION) : Progress {
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        override var tiltSensor = true
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
    /** Puts Bevel into the door and waits for the clear/end screen (long enough to sit through a fake credits roll). */
    private fun Game.win() {
        // a door that travels (a room extension) is only entered once it stands still again; extensions and rematches take their time
        var lastDoor = Float.NaN
        repeat(20000) {
            val w = world!!
            val d = w.door.box
            if (w.state == WorldState.PLAYING && d.x == lastDoor) {
                w.player.box.x = d.x + d.w / 2 - w.player.box.w / 2
                w.player.box.y = d.y + d.h - w.player.box.h
            }
            lastDoor = d.x
            update(1f / 120f)
            if (screen != Screen.PLAY) return
        }
    }

    @Test fun worldsAreLaidOutBackToBack() {
        assertEquals(48, World1.levels.size)
        assertEquals(listOf(0, 48, 96), Worlds.all.map { it.firstLevel })
        assertEquals(144, Levels.all.size)
        assertSame(World1.levels[0], Levels.all[0])
        assertSame(World1.levels[47], Levels.all[47])
        assertSame(World2.levels[0], Levels.all[48])
        assertSame(World3.levels[0], Levels.all[96])
        assertEquals(48, Worlds.get(3).size)
    }

    @Test fun indexMapping() {
        assertEquals(1, Worlds.of(0).number)
        assertEquals(1, Worlds.of(47).number)
        assertEquals(2, Worlds.of(48).number)
        assertEquals(2, Worlds.of(95).number)
        assertEquals(3, Worlds.of(96).number)
        assertEquals(3, Worlds.of(143).number)
        assertEquals("1-1", Worlds.label(0))
        assertEquals("1-48", Worlds.label(47))
        assertEquals("2-1", Worlds.label(48))
        assertEquals("2-17", Worlds.label(64))
        assertEquals(17, Worlds.local(64))
        assertEquals("3-1", Worlds.label(96))
        assertEquals("3-48", Worlds.label(143))
        // out of range: the nearest world that has levels
        assertEquals(3, Worlds.of(144).number)
        assertEquals(1, Worlds.of(-1).number)
        for (i in Levels.all.indices) assertTrue(i in Worlds.of(i))
    }

    @Test fun clearingWorldOneRoutesThroughWorldTwoIntro() {
        val p = Prog(48)
        val g = Game(p, silent)
        g.startLevel(47)
        assertEquals("1-48", g.levelLabel)
        g.win()
        assertEquals(Screen.CLEAR, g.screen)
        assertEquals(49, p.unlocked)
        g.tapOn(Ui.clearNext)
        assertEquals(Screen.WORLD_INTRO, g.screen)
        assertEquals(2, g.worldInfo.number)
        g.update(0.5f)
        g.tap(100f, 100f)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(48, g.levelIndex)
        assertEquals("2-1", g.levelLabel)
    }

    @Test fun clearingWorldTwoRoutesThroughWorldThreeIntro() {
        val p = Prog(96)
        val g = Game(p, silent)
        g.startLevel(95)
        assertEquals("2-48", g.levelLabel)
        g.win()
        assertEquals(Screen.CLEAR, g.screen)
        assertEquals(97, p.unlocked)
        g.tapOn(Ui.clearNext)
        assertEquals(Screen.WORLD_INTRO, g.screen)
        assertEquals(3, g.worldInfo.number)
        g.update(0.5f)
        g.tap(100f, 100f)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(96, g.levelIndex)
        assertEquals("3-1", g.levelLabel)
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
        val n = Levels.all.size
        val p = Prog(n)
        val g = Game(p, silent)
        g.startLevel(n - 1)
        g.win()
        assertEquals(Screen.END, g.screen)
        assertEquals(n, p.unlocked)
    }

    @Test fun oldSaveThatFinishedWorldOneUnlocksWorldTwo() {
        // before world 2 existed, unlocking was capped at the end of world 1
        val p = Prog(48).apply { best[47] = 4 }
        Game(p, silent)
        assertEquals(49, p.unlocked)
        val q = Prog(7).apply { best[5] = 1 }
        Game(q, silent)
        assertEquals(7, q.unlocked)
    }

    @Test fun saveThatFinishedWorldTwoBeforeWorldThreeExistedUnlocksIt() {
        // version 3 saves end at 96 (World 2 was the last world, so "unlocked" was capped there)
        val p = Prog(96).apply { best[95] = 6 }
        Game(p, silent)
        assertEquals(97, p.unlocked)
        assertEquals(SAVE_VERSION, p.saveVersion)
        val g = select(p)
        assertEquals(3, g.selWorld.number)
        assertTrue(g.worldOpen(Worlds.get(3)))
        // a save that has not cleared 2-48 stays closed to World 3, even with the unlock count at the cap
        val q = Prog(96)
        Game(q, silent)
        assertEquals(96, q.unlocked)
        assertFalse(Game(q, silent).worldOpen(Worlds.get(3)))
    }

    @Test fun worldThreeTabOpensAndPlaysTheFirstLevel() {
        val n = Worlds.all.size
        val g = select(Prog(97))
        assertEquals(3, g.selWorld.number)
        g.tapOn(Ui.worldTab(1, n))
        assertEquals(2, g.selWorld.number)
        g.tapOn(Ui.worldTab(2, n))
        assertEquals(3, g.selWorld.number)
        assertEquals(96, g.selLevel(0))
        g.tapOn(Ui.levelTile(0))
        assertEquals(Screen.PLAY, g.screen)
        assertEquals("3-1", g.levelLabel)
    }

    @Test fun lastLevelOfWorldThreeEndsTheGame() {
        val p = Prog(144)
        val g = Game(p, silent)
        g.startLevel(143)
        assertEquals("3-48", g.levelLabel)
        g.win()
        assertEquals(Screen.END, g.screen)
        // the ending types its kill -9 into a terminal first: a tap skips ahead, a second one leaves
        assertTrue(g.endAge < Intro.endDuration)
        g.tapOn(Ui.endTitle)
        assertEquals(Screen.END, g.screen)
        assertTrue(g.endAge >= Intro.endDuration)
        g.tapOn(Ui.endTitle)
        assertEquals(Screen.TITLE, g.screen)
    }

    @Test fun endingTerminalRunsByItself() {
        val g = Game(Prog(144), silent)
        g.startLevel(143)
        g.win()
        repeat((Intro.endDuration * 120f).toInt() + 5) { g.update(1f / 120f) }
        assertTrue(g.endAge >= Intro.endDuration)
        assertEquals(Screen.END, g.screen)
        assertTrue(Intro.ending.any { it.text.en.contains("kill -9") })
        assertTrue(Intro.ending.all { it.text.en.length <= 40 && it.text.de.length <= 44 })
    }

    @Test fun oldSaveFromTheBigWorldOneIsClamped() {
        // version 1 saves counted 128 levels in world 1: unlocked=100 would now sit deep inside world 2
        val old = Prog(100, saveVersion = 1).apply { best[99] = 3 }
        Game(old, silent)
        assertEquals(49, old.unlocked)
        assertEquals(SAVE_VERSION, old.saveVersion)
        // an old save that had not got far keeps its place
        val early = Prog(30, saveVersion = 1)
        Game(early, silent)
        assertEquals(30, early.unlocked)
        // version 2 saves came from the 128-level World 2: clamped the same way, then marked current
        val big = Prog(150, saveVersion = 2)
        Game(big, silent)
        assertEquals(49, big.unlocked)
        assertEquals(SAVE_VERSION, big.saveVersion)
        val w1 = Prog(40, saveVersion = 2)
        Game(w1, silent)
        assertEquals(40, w1.unlocked)
        // current saves are left alone and never point past the last level
        val current = Prog(Levels.all.size)
        Game(current, silent)
        assertEquals(Levels.all.size, current.unlocked)
        assertEquals(144, Levels.all.size)
    }

    @Test fun selectOpensOnPageOfHighestUnlockedLevel() {
        val g = select(Prog(40)) // highest unlocked: 1-40, on page 3
        assertEquals(1, g.selWorld.number)
        assertEquals(2, g.selPage)
        val h = select(Prog(48 + 20))
        assertEquals(2, h.selWorld.number)
        assertEquals(1, h.selPage)
        assertEquals(3, g.pages())
        assertEquals(3, h.pages())
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
        assertEquals(2, g.selPage)
        g.tapOn(Ui.pagePrev)
        assertEquals(1, g.selPage)
        assertEquals(16, g.selLevel(0))
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

        val h = select(Prog(48 + 20))
        assertEquals(2, h.selWorld.number)
        h.tapOn(Ui.worldTab(0, n))
        assertEquals(1, h.selWorld.number)
        assertEquals(2, h.selPage) // world 1 fully unlocked: its last page
        h.tapOn(Ui.worldTab(1, n))
        assertEquals(1, h.selPage)
        h.tapOn(Ui.levelTile(0)) // 2-17
        assertEquals(64, h.levelIndex)
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
