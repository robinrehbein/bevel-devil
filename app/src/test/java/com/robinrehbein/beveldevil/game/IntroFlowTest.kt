package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntroFlowTest {
    private class Prog : Progress {
        override var unlocked = 1
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = false
        override var tiltSensor = true
        override fun bestDeaths(level: Int): Int? = null
        override fun saveBest(level: Int, deaths: Int) {}
        override fun cardFound(card: Card) = false
        override fun findCard(card: Card) {}
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }

    /** Taps twice per page: the first completes the typewriter, the second advances. */
    private fun advance(g: Game) { g.tap(100f, 100f); g.tap(100f, 100f) }

    @Test
    fun firstStartRunsIntroThenWorldThenLevel() {
        val p = Prog()
        val g = Game(p, silent)
        assertEquals(Screen.INTRO, g.screen)
        repeat(Intro.pages.size) {
            assertEquals(Screen.INTRO, g.screen)
            advance(g)
        }
        assertEquals(Screen.WORLD_INTRO, g.screen)
        assertEquals(1, g.worldInfo.number)
        assertTrue(p.introSeen)
        g.update(0.1f)
        g.tap(100f, 100f) // too early, ignored
        assertEquals(Screen.WORLD_INTRO, g.screen)
        g.update(0.5f)
        g.tap(100f, 100f)
        assertEquals(Screen.PLAY, g.screen)
        assertEquals(0, g.levelIndex)
    }

    @Test
    fun firstTapCompletesTextOnly() {
        val g = Game(Prog(), silent)
        g.tap(100f, 100f)
        assertEquals(0, g.introPage)
        g.tap(100f, 100f)
        assertEquals(1, g.introPage)
    }

    @Test
    fun skipGoesStraightToWorldIntro() {
        val g = Game(Prog(), silent)
        g.tap(Ui.introSkip.x + 2f, Ui.introSkip.y + 2f)
        assertEquals(Screen.WORLD_INTRO, g.screen)
    }

    @Test
    fun secondStartGoesToTitleAndStoryIsReplayable() {
        val p = Prog().apply { introSeen = true }
        val g = Game(p, silent)
        assertEquals(Screen.TITLE, g.screen)
        g.tap(Ui.titleStory.x + 2f, Ui.titleStory.y + 2f)
        assertEquals(Screen.INTRO, g.screen)
        g.tap(Ui.introSkip.x + 2f, Ui.introSkip.y + 2f)
        assertEquals(Screen.TITLE, g.screen)
        assertFalse(g.screen == Screen.WORLD_INTRO)
    }
}
