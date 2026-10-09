package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The album holds more cards than one screen: twelve to a page, arrows to turn it, old saves untouched. */
class AlbumTest {
    private class Prog : Progress {
        override var unlocked = 1
        override var saveVersion = SAVE_VERSION
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        override var tiltSensor = true
        val found = HashSet<Card>()
        override fun bestDeaths(level: Int): Int? = null
        override fun saveBest(level: Int, deaths: Int) {}
        override fun cardFound(card: Card) = card in found
        override fun findCard(card: Card) { found += card }
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private fun Game.tapOn(h: Hit) = tap(h.x + 1f, h.y + 1f)

    private fun album(p: Prog): Game = Game(p, silent).also { it.tapOn(Ui.titleAlbum); assertEquals(Screen.ALBUM, it.screen) }

    @Test
    fun theAlbumHasTwoPages() {
        assertEquals(22, Card.entries.size)
        assertEquals(2, album(Prog()).albumPages())
    }

    @Test
    fun everyCardSlotFitsOnItsPage() {
        for (i in Card.entries.indices) {
            val h = Ui.albumCard(i)
            assertTrue("card $i outside the stage", h.x >= Ui.pagePrev.w && h.x + h.w <= Ui.pageNext.x && h.y >= 24 && h.y + h.h <= 132)
        }
        // the slots of a page never overlap
        for (page in 0 until 2) {
            val slots = (page * Ui.ALBUM_PAGE until minOf(Card.entries.size, (page + 1) * Ui.ALBUM_PAGE)).map { Ui.albumCard(it) }
            for (a in slots.indices) for (b in a + 1 until slots.size) {
                val x = slots[a]; val y = slots[b]
                assertTrue(x.x + x.w <= y.x || y.x + y.w <= x.x || x.y + x.h <= y.y || y.y + y.h <= x.y)
            }
        }
    }

    @Test
    fun arrowsTurnThePageAndPickTheRightCard() {
        val p = Prog().apply { found += Card.entries }
        val g = album(p)
        g.tapOn(Ui.albumCard(0))
        assertEquals(0, g.albumSelection)
        // the card of the next page is not on this one
        g.tapOn(Ui.pageNext)
        assertEquals(1, g.albumPage)
        assertEquals(-1, g.albumSelection)
        g.tapOn(Ui.albumCard(12))
        assertEquals(12, g.albumSelection)
        g.tapOn(Ui.albumCard(17))
        assertEquals(17, g.albumSelection)
        // no third page, and back goes to the first
        g.tapOn(Ui.pageNext)
        assertEquals(1, g.albumPage)
        g.tapOn(Ui.pagePrev)
        assertEquals(0, g.albumPage)
        g.tapOn(Ui.pagePrev)
        assertEquals(0, g.albumPage)
        // page one slot 0 is card 0, not card 12
        g.tapOn(Ui.albumCard(12 + 0))
        assertEquals(0, g.albumSelection)
    }

    @Test
    fun anUndiscoveredCardCannotBeInspected() {
        val g = album(Prog())
        g.tapOn(Ui.albumCard(3))
        assertEquals(-1, g.albumSelection)
    }

    @Test
    fun savesStayCompatible() {
        // the saved keys are built from the enum names, and the twelve old cards keep their names and places
        val old = listOf("COLLAPSE", "SPIKE_SEED", "SHY_DOOR", "HEADBUTT", "UPSIDE_DOWN", "TWISTED", "DEVIL_SAW", "GHOST_BLOCK", "SINKING", "DECOY", "CRUMBLE", "GRAND_FINALE")
        assertEquals(old, Card.entries.take(12).map { it.name })
        assertEquals(listOf("SHORT_CIRCUIT", "OVERCLOCKED", "BIT_FLIP", "BACKDRAFT", "THROTTLE", "BIOS"), Card.entries.drop(12).take(6).map { it.name })
        assertEquals(listOf("UNDO", "STALKER", "BLUFF", "ANNEX"), Card.entries.drop(18).map { it.name })
    }

    @Test
    fun everyCardSaysWhatItDoes() {
        // one short line in each language, and no two cards share one: look-alike families must read apart
        for (c in Card.entries) for (s in listOf(c.how.en, c.how.de)) {
            assertTrue("${c.name}: blank", s.isNotBlank())
            assertTrue("${c.name}: '$s' is too long for the album panel", s.length <= 55)
            assertTrue("${c.name}: '$s' repeats the flavor", s != c.flavor.en && s != c.flavor.de)
        }
        assertEquals(Card.entries.size, Card.entries.map { it.how.en }.toSet().size)
        assertEquals(Card.entries.size, Card.entries.map { it.how.de }.toSet().size)
    }

    @Test
    fun opensOnTheFirstPageEachTime() {
        val g = album(Prog().apply { found += Card.entries })
        g.tapOn(Ui.pageNext)
        g.back()
        g.tapOn(Ui.titleAlbum)
        assertEquals(0, g.albumPage)
    }
}
