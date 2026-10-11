package com.robinrehbein.beveldevil.render

import com.robinrehbein.beveldevil.game.Txt
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The death count at the foot of a card stays inside the card's border, like the flavor text above it. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class CardTextFitTest {
    @Test
    fun theDeathCountFitsTheCard() {
        val px = Pixels(RuntimeEnvironment.getApplication())
        // as drawCardFace draws it: the count put in for %d, at size 3.2; 36 is the width the flavor text wraps at
        for (s in listOf(Txt.caught.en, Txt.caught.de)) {
            val line = s.replace("%d", "999")
            assertTrue(line, line.contains("999"))
            assertTrue("$line is ${px.fineWidth(line, 3.2f)} wide", px.fineWidth(line, 3.2f) <= 36f)
        }
    }
}
