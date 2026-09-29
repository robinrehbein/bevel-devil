package com.robinrehbein.beveldevil

import android.content.Context
import com.robinrehbein.beveldevil.game.Card
import com.robinrehbein.beveldevil.game.Progress

/** Progress stored in SharedPreferences. */
class PrefsProgress(context: Context) : Progress {
    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    override var unlocked: Int
        get() = prefs.getInt("unlocked", 1)
        set(v) = prefs.edit().putInt("unlocked", v).apply()

    override var sound: Boolean
        get() = prefs.getBoolean("sound", true)
        set(v) = prefs.edit().putBoolean("sound", v).apply()

    override fun bestDeaths(level: Int): Int? = prefs.getInt("best_$level", -1).takeIf { it >= 0 }
    override fun saveBest(level: Int, deaths: Int) = prefs.edit().putInt("best_$level", deaths).apply()
    override fun cardFound(card: Card) = prefs.getBoolean("card_${card.name}", false)
    override fun findCard(card: Card) = prefs.edit().putBoolean("card_${card.name}", true).apply()
    override fun cardDeaths(card: Card) = prefs.getInt("card_deaths_${card.name}", 0)
    override fun addCardDeath(card: Card) = prefs.edit().putInt("card_deaths_${card.name}", cardDeaths(card) + 1).apply()
}
