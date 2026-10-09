package com.robinrehbein.beveldevil.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.robinrehbein.beveldevil.BuildConfig
import com.robinrehbein.beveldevil.game.Monetization
import com.robinrehbein.beveldevil.game.Progress

/**
 * AdMob (interstitial between levels, rewarded level skip) and the one-time Play Billing purchase that removes the ads.
 * The consent form (UMP) runs first; no ad is requested before the player's choice allows it. Everything here runs on
 * the main thread; the game thread only reads the volatile state and calls the `show` methods.
 */
class AdsBilling(private val activity: Activity, private val progress: Progress) : Monetization, PurchasesUpdatedListener {
    private val main = Handler(Looper.getMainLooper())
    private val consent: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)

    @Volatile private var removed = progress.adsRemoved
    @Volatile private var interstitial: InterstitialAd? = null
    @Volatile private var rewarded: RewardedAd? = null
    @Volatile private var product: ProductDetails? = null
    @Volatile private var adsStarted = false
    private var destroyed = false
    private var interstitialLoading = false
    private var rewardedLoading = false

    private val billing: BillingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    override val adsRemoved get() = removed
    override val canPurchase get() = product != null && !removed
    override val privacyOptionsRequired
        get() = !removed && consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    override fun rewardedReady() = rewarded != null

    /**
     * Call from `onCreate`, after `setContentView`: with consent remembered from an earlier run this starts MobileAds
     * right away on a background thread, and its WebView load must not overlap the window inflating its content view.
     */
    fun start() {
        connectBilling()
        if (!removed) askConsent()
    }

    fun destroy() {
        destroyed = true
        main.removeCallbacksAndMessages(null)
        interstitial = null
        rewarded = null
        if (billing.isReady) billing.endConnection()
    }

    // ---------- consent ----------

    private fun askConsent() {
        // a choice from an earlier run may already allow ads
        startAdsIfAllowed()
        consent.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(), {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { startAdsIfAllowed() }
        }, { startAdsIfAllowed() })
    }

    override fun showPrivacyOptions() {
        main.post {
            if (!destroyed) UserMessagingPlatform.showPrivacyOptionsForm(activity) { startAdsIfAllowed() }
        }
    }

    private fun startAdsIfAllowed() {
        if (adsStarted || removed || destroyed || !consent.canRequestAds()) return
        adsStarted = true
        Thread {
            MobileAds.initialize(activity) { main.post { loadInterstitial(); loadRewarded() } }
        }.start()
    }

    // ---------- ads ----------

    private fun loadInterstitial() {
        if (removed || destroyed || interstitialLoading || interstitial != null) return
        interstitialLoading = true
        InterstitialAd.load(activity, BuildConfig.ADMOB_INTERSTITIAL_ID, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitialLoading = false; interstitial = ad }
            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialLoading = false
                main.postDelayed(::loadInterstitial, RETRY_MS)
            }
        })
    }

    private fun loadRewarded() {
        if (removed || destroyed || rewardedLoading || rewarded != null) return
        rewardedLoading = true
        RewardedAd.load(activity, BuildConfig.ADMOB_REWARDED_ID, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { rewardedLoading = false; rewarded = ad }
            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedLoading = false
                main.postDelayed(::loadRewarded, RETRY_MS)
            }
        })
    }

    override fun showInterstitial(closed: () -> Unit): Boolean {
        if (removed || interstitial == null) return false
        main.post {
            val ad = interstitial
            interstitial = null
            if (ad == null || destroyed || activity.isFinishing) { closed(); return@post }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { loadInterstitial(); closed() }
                override fun onAdFailedToShowFullScreenContent(error: AdError) { loadInterstitial(); closed() }
            }
            ad.show(activity)
        }
        return true
    }

    override fun showRewarded(closed: (earned: Boolean) -> Unit): Boolean {
        if (removed || rewarded == null) return false
        main.post {
            val ad = rewarded
            rewarded = null
            if (ad == null || destroyed || activity.isFinishing) { closed(false); return@post }
            var earned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { loadRewarded(); closed(earned) }
                override fun onAdFailedToShowFullScreenContent(error: AdError) { loadRewarded(); closed(false) }
            }
            ad.show(activity) { earned = true }
        }
        return true
    }

    // ---------- purchase ----------

    private fun connectBilling() {
        billing.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) return
                queryProduct()
                queryPurchases()
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryProduct() {
        val item = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP).build()
        billing.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(item)).build()) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) product = details.productDetailsList.firstOrNull()
        }
    }

    /** The store is the authority: a purchase that is gone (refund) takes the cached flag away again. */
    private fun queryPurchases() {
        billing.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryPurchasesAsync
            val owned = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED && PRODUCT_ID in it.products }
            handle(purchases)
            if (!owned && removed) main.post { setRemoved(false) }
        }
    }

    override fun purchaseRemoveAds() {
        main.post {
            val details = product ?: return@post
            if (removed || destroyed || activity.isFinishing) return@post
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()))
                .build()
            billing.launchBillingFlow(activity, params)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) handle(purchases.orEmpty())
        // ITEM_ALREADY_OWNED: bought on another device or before a reinstall; the query brings it back
        else if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) queryPurchases()
    }

    private fun handle(purchases: List<Purchase>) {
        for (p in purchases) {
            if (PRODUCT_ID !in p.products || p.purchaseState != Purchase.PurchaseState.PURCHASED) continue
            main.post { setRemoved(true) }
            if (!p.isAcknowledged) {
                billing.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) {}
            }
        }
    }

    private fun setRemoved(value: Boolean) {
        removed = value
        progress.adsRemoved = value
        if (value) { interstitial = null; rewarded = null } else if (!destroyed) askConsent()
    }

    private companion object {
        /** Managed in-app product in the Play Console. */
        const val PRODUCT_ID = "remove_ads"
        const val RETRY_MS = 30_000L
    }
}
