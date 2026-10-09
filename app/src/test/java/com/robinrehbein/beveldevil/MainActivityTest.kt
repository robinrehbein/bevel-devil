package com.robinrehbein.beveldevil

import android.view.ViewGroup
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The activity comes up with the game as its content. A smoke test only: it cannot reproduce the start-up race
 * between the window's decor and the background ad init, it just keeps the creation path working.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class MainActivityTest {
    @Test
    fun startsWithTheGameAsContent() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val content = controller.get().findViewById<ViewGroup>(android.R.id.content)
        assertTrue(content.getChildAt(0) is GameView)
        controller.pause().stop().destroy()
    }
}
