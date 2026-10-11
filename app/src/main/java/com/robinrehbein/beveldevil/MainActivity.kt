package com.robinrehbein.beveldevil

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.window.OnBackInvokedDispatcher
import com.robinrehbein.beveldevil.ads.AdsBilling
import com.robinrehbein.beveldevil.audio.Music
import com.robinrehbein.beveldevil.audio.Sfx
import com.robinrehbein.beveldevil.game.Audio
import com.robinrehbein.beveldevil.game.Tune
import com.robinrehbein.beveldevil.game.Game

class MainActivity : Activity() {
    private lateinit var view: GameView
    private lateinit var sfx: Sfx
    private lateinit var music: Music
    private lateinit var ads: AdsBilling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val progress = PrefsProgress(this)
        sfx = Sfx { progress.sound }
        music = Music(this) { progress.music }
        val audio = object : Audio by sfx {
            override fun music(tune: Tune?, duck: Boolean) = music.set(tune, duck)
        }
        ads = AdsBilling(this, progress)
        view = GameView(this, Game(progress, audio, ads))
        setContentView(view)
        view.requestFocus()
        // only now: with remembered consent, start() boots MobileAds (and the WebView) on a background thread,
        // which must not race the window inflating its decor in setContentView. That race is the inferred cause of
        // the rare "couldn't find content container view" launch crash (not reproduced); the decor is inflated
        // synchronously in setContentView, so starting here leaves that inflation nothing to overlap with
        ads.start()
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                if (!view.back()) finish()
            }
        }
    }

    @Deprecated("Used below API 33")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!view.back()) super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    override fun onPause() {
        super.onPause()
        view.onPauseApp()
        music.pause()
    }

    override fun onResume() {
        super.onResume()
        music.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        sfx.release()
        music.release()
        ads.destroy()
    }
}
