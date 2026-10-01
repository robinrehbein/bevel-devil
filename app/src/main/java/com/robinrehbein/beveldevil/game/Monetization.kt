package com.robinrehbein.beveldevil.game

/**
 * Ads and the one-time "remove ads" purchase as the game logic sees them. The Android side lives in
 * `ads/AdsBilling.kt`; every call may come from the game thread and must not block.
 */
interface Monetization {
    /** The player bought "remove ads": no interstitials, and the level skip is free. */
    val adsRemoved: Boolean get() = false
    /** The store answered and the purchase can be started. */
    val canPurchase: Boolean get() = false
    /** The consent form must stay reachable from the settings (EU/UK users). */
    val privacyOptionsRequired: Boolean get() = false
    fun rewardedReady(): Boolean = false

    /** Shows a full-screen ad if one is loaded; [closed] runs when it goes away. False: nothing shown, [closed] never runs. */
    fun showInterstitial(closed: () -> Unit): Boolean = false
    /** Shows a rewarded ad if one is loaded; [closed] runs when it goes away, with whether the reward was earned. */
    fun showRewarded(closed: (earned: Boolean) -> Unit): Boolean = false
    fun purchaseRemoveAds() {}
    fun showPrivacyOptions() {}
}

/** Builds without ads (tests, screenshots). */
object NoAds : Monetization

/**
 * When the game may interrupt with an interstitial: only on the clear screen between two levels, never while
 * playing, after a death or on the pause menu.
 */
object AdRules {
    /** Levels (from the first) that never come with an ad, so nobody meets one before they are hooked. */
    const val FREE_LEVELS = 5
    /** A clear screen shows an ad every this many cleared levels... */
    const val EVERY = 3
    /** ...and at most once per this many seconds of play. */
    const val COOLDOWN = 120f
    /** The pause menu offers "skip level" after this many deaths on the level. */
    const val SKIP_AFTER_DEATHS = 6

    fun interstitialDue(levelIndex: Int, clearsSinceAd: Int, secondsSinceAd: Float) =
        levelIndex >= FREE_LEVELS && clearsSinceAd >= EVERY && secondsSinceAd >= COOLDOWN
}
