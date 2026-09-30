package com.robinrehbein.beveldevil

import android.content.Context
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Progress
import com.robinrehbein.beveldevil.game.SAVE_VERSION

/** Progress stored in SharedPreferences. */
class PrefsProgress(context: Context) : Progress {
    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    override var unlocked: Int
        get() = prefs.getInt("unlocked", 1)
        set(v) = prefs.edit().putInt("unlocked", v).apply()

    // a save without a version that already has progress is from the 128-level World 1 (version 1)
    override var saveVersion: Int
        get() = prefs.getInt("save_version", if (prefs.contains("unlocked")) 1 else SAVE_VERSION)
        set(v) = prefs.edit().putInt("save_version", v).apply()

    override var sound: Boolean
        get() = prefs.getBoolean("sound", true)
        set(v) = prefs.edit().putBoolean("sound", v).apply()

    override var stickScheme: Boolean
        get() = prefs.getBoolean("stick", false)
        set(v) = prefs.edit().putBoolean("stick", v).apply()

    override var buttonSize: Int
        get() = prefs.getInt("button_size", 1).coerceIn(0, 2)
        set(v) = prefs.edit().putInt("button_size", v).apply()

    override var haptics: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(v) = prefs.edit().putBoolean("haptics", v).apply()

    override var leftHanded: Boolean
        get() = prefs.getBoolean("left_handed", false)
        set(v) = prefs.edit().putBoolean("left_handed", v).apply()

    override var introSeen: Boolean
        get() = prefs.getBoolean("intro_seen", false)
        set(v) = prefs.edit().putBoolean("intro_seen", v).apply()

    override var tiltSensor: Boolean
        get() = prefs.getBoolean("tilt_sensor", true)
        set(v) = prefs.edit().putBoolean("tilt_sensor", v).apply()

    override fun bestDeaths(level: Int): Int? = prefs.getInt("best_$level", -1).takeIf { it >= 0 }
    override fun saveBest(level: Int, deaths: Int) = prefs.edit().putInt("best_$level", deaths).apply()
    override fun cardFound(card: Card) = prefs.getBoolean("card_${card.name}", false)
    override fun findCard(card: Card) = prefs.edit().putBoolean("card_${card.name}", true).apply()
    override fun cardDeaths(card: Card) = prefs.getInt("card_deaths_${card.name}", 0)
    override fun addCardDeath(card: Card) = prefs.edit().putInt("card_deaths_${card.name}", cardDeaths(card) + 1).apply()
}
