package com.robinrehbein.beveldevil.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonetizationTest {
    private class Prog : Progress {
        override var unlocked = 1
        override var sound = true
        override var stickScheme = false
        override var buttonSize = 1
        override var haptics = true
        override var leftHanded = false
        override var introSeen = true
        override var tiltSensor = true
        override fun bestDeaths(level: Int): Int? = null
        override fun saveBest(level: Int, deaths: Int) {}
        override fun cardFound(card: Card) = false
        override fun findCard(card: Card) {}
        override fun cardDeaths(card: Card) = 0
        override fun addCardDeath(card: Card) {}
    }

    /** Ads that are always loaded; [close] ends the ad that is showing. */
    private class FakeAds : Monetization {
        override var adsRemoved = false
        override var canPurchase = true
        override var privacyOptionsRequired = false
        var rewardedLoaded = true
        var interstitialLoaded = true
        var interstitials = 0
        var rewardeds = 0
        var purchases = 0
        var options = 0
        private var closeInterstitial: (() -> Unit)? = null
        private var closeRewarded: ((Boolean) -> Unit)? = null
        override fun rewardedReady() = rewardedLoaded
        override fun showInterstitial(closed: () -> Unit): Boolean {
            if (!interstitialLoaded) return false
            interstitials++; closeInterstitial = closed; return true
        }
        override fun showRewarded(closed: (Boolean) -> Unit): Boolean {
            if (!rewardedLoaded) return false
            rewardeds++; closeRewarded = closed; return true
        }
        override fun purchaseRemoveAds() { purchases++ }
        override fun showPrivacyOptions() { options++ }
        fun closeInterstitial() = closeInterstitial!!.invoke()
        fun closeRewarded(earned: Boolean) = closeRewarded!!.invoke(earned)
    }

    private val silent = object : Audio { override fun play(sound: Sound) {} }
    private val dt = 1f / 120f

    private fun game(ads: FakeAds, prog: Progress = Prog()) = Game(prog, silent, ads)

    private fun Game.run(seconds: Float) { var t = 0f; while (t < seconds) { update(dt); t += dt } }

    /** Plays the always-winnable demo level as global level [i] and lands on the clear screen. */
    private fun Game.clear(i: Int) {
        sandbox = Demo.idle
        startLevel(i)
        input.right = true
        run(8f)
        input.right = false
        assertEquals(Screen.CLEAR, screen)
    }

    private fun Game.next() { tap(Ui.clearNext.x + 1f, Ui.clearNext.y + 1f); update(dt) }

    private fun Game.openPause() { tap(Ui.hudPause.x + 1f, Ui.hudPause.y + 1f); assertEquals(Screen.PAUSE, screen) }
    private fun Game.pauseRestart() { tap(Ui.pauseRestart.x + 1f, Ui.pauseRestart.y + 1f) }
    private fun Game.pauseSkip() = tap(Ui.pauseSkip.x + 1f, Ui.pauseSkip.y + 1f)

    @Test
    fun noInterstitialInTheFreeLevels() {
        val ads = FakeAds()
        val g = game(ads)
        for (i in 0 until AdRules.FREE_LEVELS) { g.clear(i); g.next(); g.run(AdRules.COOLDOWN) }
        assertEquals(0, ads.interstitials)
    }

    @Test
    fun interstitialEveryThirdClearAfterTheFreeLevels() {
        val ads = FakeAds()
        val g = game(ads)
        val first = AdRules.FREE_LEVELS
        repeat(AdRules.EVERY - 1) { g.clear(first + it); g.next(); g.run(AdRules.COOLDOWN) }
        assertEquals(0, ads.interstitials)
        g.clear(first + AdRules.EVERY - 1)
        g.next()
        assertEquals(1, ads.interstitials)
        assertEquals("the ad is in front of the next level", Screen.CLEAR, g.screen)
        g.next()
        assertEquals("a second tap does not start another ad", 1, ads.interstitials)
        ads.closeInterstitial(); g.update(dt)
        assertFalse(g.screen == Screen.CLEAR)
    }

    @Test
    fun cooldownBetweenInterstitials() {
        val ads = FakeAds()
        val g = game(ads)
        val first = AdRules.FREE_LEVELS
        repeat(AdRules.EVERY) { g.clear(first + it); g.next() }
        assertEquals(1, ads.interstitials)
        ads.closeInterstitial(); g.update(dt)
        // three more clears right away: due by count, but the cooldown has not run out (clearing takes ~8 s each)
        repeat(AdRules.EVERY) { g.clear(first + 3 + it); g.next() }
        assertEquals(1, ads.interstitials)
    }

    @Test
    fun noAdsMeansNoInterstitial() {
        val ads = FakeAds().apply { adsRemoved = true }
        val g = game(ads)
        repeat(AdRules.EVERY + 1) { g.clear(AdRules.FREE_LEVELS + it); g.next(); g.run(AdRules.COOLDOWN) }
        assertEquals(0, ads.interstitials)
    }

    @Test
    fun unloadedInterstitialNeverBlocksTheGame() {
        val ads = FakeAds().apply { interstitialLoaded = false }
        val g = game(ads)
        val first = AdRules.FREE_LEVELS
        repeat(AdRules.EVERY) { g.clear(first + it); g.next(); g.run(AdRules.COOLDOWN) }
        assertEquals(0, ads.interstitials)
        assertFalse(g.screen == Screen.CLEAR)
    }

    @Test
    fun skipIsOfferedOnlyAfterEnoughDeaths() {
        val g = game(FakeAds())
        g.sandbox = Demo.idle
        g.startLevel(2)
        g.openPause()
        assertFalse(g.skipOffered)
        g.tap(Ui.pauseResume.x + 1f, Ui.pauseResume.y + 1f)
        repeat(AdRules.SKIP_AFTER_DEATHS) { g.openPause(); g.pauseRestart() }
        g.openPause()
        assertTrue(g.skipOffered)
    }

    @Test
    fun skipNeedsTheRewardAndUnlocksTheNextLevel() {
        val ads = FakeAds()
        val p = Prog()
        val g = game(ads, p)
        g.sandbox = Demo.idle
        g.startLevel(2)
        repeat(AdRules.SKIP_AFTER_DEATHS) { g.openPause(); g.pauseRestart() }
        g.openPause()
        g.pauseSkip()
        assertEquals(1, ads.rewardeds)
        assertEquals("waiting for the ad", Screen.PAUSE, g.screen)
        ads.closeRewarded(false); g.update(dt)
        assertEquals("closed without reward: nothing happens", Screen.PAUSE, g.screen)
        assertEquals(1, p.unlocked)
        g.pauseSkip()
        assertEquals(2, ads.rewardeds)
        ads.closeRewarded(true); g.update(dt)
        assertEquals(Screen.CLEAR, g.screen)
        assertEquals(4, p.unlocked)
    }

    @Test
    fun skipIsFreeWithoutAds() {
        val ads = FakeAds().apply { adsRemoved = true }
        val g = game(ads)
        g.sandbox = Demo.idle
        g.startLevel(2)
        repeat(AdRules.SKIP_AFTER_DEATHS) { g.openPause(); g.pauseRestart() }
        g.openPause()
        g.pauseSkip()
        assertEquals(0, ads.rewardeds)
        assertEquals(Screen.CLEAR, g.screen)
    }

    @Test
    fun skipWaitsForALoadedAd() {
        val ads = FakeAds().apply { rewardedLoaded = false }
        val g = game(ads)
        g.sandbox = Demo.idle
        g.startLevel(2)
        repeat(AdRules.SKIP_AFTER_DEATHS) { g.openPause(); g.pauseRestart() }
        g.openPause()
        assertFalse(g.skipReady)
        g.pauseSkip()
        assertEquals(Screen.PAUSE, g.screen)
        assertEquals(0, ads.rewardeds)
    }

    @Test
    fun theFinaleCannotBeSkipped() {
        val g = game(FakeAds().apply { adsRemoved = true })
        g.sandbox = Demo.idle
        g.startLevel(Levels.all.lastIndex)
        repeat(AdRules.SKIP_AFTER_DEATHS) { g.openPause(); g.pauseRestart() }
        g.openPause()
        assertFalse(g.skipOffered)
    }

    @Test
    fun purchaseButtonOnTitleAndSettings() {
        val ads = FakeAds()
        val g = game(ads)
        assertTrue(g.noAdsOffered)
        g.tap(Ui.noAdsTitle.x + 1f, Ui.noAdsTitle.y + 1f)
        assertEquals(1, ads.purchases)
        g.tap(Ui.gear.x + 1f, Ui.gear.y + 1f)
        assertEquals(Screen.SETTINGS, g.screen)
        g.tap(Ui.noAdsSettings.x + 1f, Ui.noAdsSettings.y + 1f)
        assertEquals(2, ads.purchases)
        ads.adsRemoved = true
        g.tap(Ui.noAdsSettings.x + 1f, Ui.noAdsSettings.y + 1f)
        assertEquals("bought: the button is gone", 2, ads.purchases)
    }

    @Test
    fun adChoicesOnlyWhenRequired() {
        val ads = FakeAds()
        val g = game(ads)
        g.tap(Ui.gear.x + 1f, Ui.gear.y + 1f)
        g.tap(Ui.adChoices.x + 1f, Ui.adChoices.y + 1f)
        assertEquals(0, ads.options)
        ads.privacyOptionsRequired = true
        g.tap(Ui.adChoices.x + 1f, Ui.adChoices.y + 1f)
        assertEquals(1, ads.options)
    }

    @Test
    fun withoutAnAdsBackendNothingChanges() {
        val g = Game(Prog(), silent)
        assertFalse(g.noAdsOffered)
        assertFalse(g.adChoicesOffered)
        g.clear(AdRules.FREE_LEVELS + 5)
        g.next()
        assertFalse(g.screen == Screen.CLEAR)
    }
}
