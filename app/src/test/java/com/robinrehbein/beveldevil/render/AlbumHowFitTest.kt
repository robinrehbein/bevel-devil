package com.robinrehbein.beveldevil.render

import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Lang
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The album's "what it does" line is measured with the real font: every card, both languages, fits the panel. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AlbumHowFitTest {
    private val german = Lang.german

    @After fun restore() { Lang.german = german }

    @Test
    fun everyLineFitsThePanel() {
        val ui = UiPainter(Pixels(RuntimeEnvironment.getApplication()))
        for (de in listOf(false, true)) {
            Lang.german = de
            for (c in Card.entries) {
                val lines = ui.howLines(c)
                assertTrue("${c.name} (${if (de) "de" else "en"}): ${lines.size} lines, the panel holds 3", lines.size <= 3)
            }
        }
    }
}
