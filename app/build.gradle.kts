plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// AdMob ids come from Gradle properties (or the matching environment variables); without them, and always in debug
// builds, Google's sample ids are used, which only ever show test ads.
// Blank counts as missing: GitHub Actions passes an unset secret as an empty string, and an empty
// APPLICATION_ID makes the Mobile Ads SDK crash the app on launch.
fun admob(property: String, env: String, test: String) =
    providers.gradleProperty(property).orElse(providers.environmentVariable(env)).orNull?.takeIf { it.isNotBlank() } ?: test
val testAppId = "ca-app-pub-3940256099942544~3347511713"
val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val testRewardedId = "ca-app-pub-3940256099942544/5224354917"
val adAppId = admob("admobAppId", "ADMOB_APP_ID", testAppId)
val adInterstitialId = admob("admobInterstitialId", "ADMOB_INTERSTITIAL_ID", testInterstitialId)
val adRewardedId = admob("admobRewardedId", "ADMOB_REWARDED_ID", testRewardedId)

android {
    namespace = "com.robinrehbein.beveldevil"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.robinrehbein.beveldevil"
        minSdk = 26
        targetSdk = 36
        versionCode = providers.gradleProperty("releaseVersionCode").orNull?.toInt() ?: 6
        versionName = providers.gradleProperty("releaseVersionName").orNull ?: "0.6.0"
    }

    signingConfigs {
        create("upload") {
            val keyFile = System.getenv("ANDROID_UPLOAD_KEYSTORE")
            if (keyFile != null) {
                storeFile = file(keyFile)
                storePassword = System.getenv("ANDROID_UPLOAD_STORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_UPLOAD_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_UPLOAD_KEY_PASSWORD")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = testAppId
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$testInterstitialId\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"$testRewardedId\"")
        }
        release {
            manifestPlaceholders["admobAppId"] = adAppId
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$adInterstitialId\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"$adRewardedId\"")
            if (System.getenv("ANDROID_UPLOAD_KEYSTORE") != null) {
                signingConfig = signingConfigs.getByName("upload")
            }
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.configureEach {
    if (name == "bundleRelease" || name == "assembleRelease") {
        doFirst {
            if (adAppId == testAppId) logger.warn("WARNING: release build with AdMob TEST ids (set admobAppId, admobInterstitialId, admobRewardedId).")
        }
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-ads:24.5.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
    implementation("com.android.billingclient:billing:8.0.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
