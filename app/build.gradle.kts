plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

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

    buildTypes {
        release {
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

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
