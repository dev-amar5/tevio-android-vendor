import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.tevioapp.vendor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tevioapp.vendor"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    // Split APK per ABI instead of shipping fat APK
    splits {
        abi {
            isEnable = true
            reset()
            isUniversalApk = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        dataBinding = true
    }

    flavorDimensions += listOf("environment")
    productFlavors {                                                                                                                                                                                                                                                                                                                                                                                                                                      
        create("dev") {
            dimension = "environment"
            versionCode = 11
            versionName = "1.9"
            versionNameSuffix = "-dev"
            buildConfigField("String", "URL_AUTH", "\"https://dev-auth.tevioapp.com/\"")
            buildConfigField("String", "URL_RESTAURANT", "\"https://dev-restaurant.tevioapp.com/\"")
            buildConfigField("String", "URL_ORDERS", "\"https://dev-order.tevioapp.com/\"")
            buildConfigField(
                "String", "URL_NOTIFICATION", "\"https://dev-notification.tevioapp.com/\""
            )
        }
        create("qa") {
            dimension = "environment"
            versionCode = 6
            versionName = "1.4"
            versionNameSuffix = "-qa"
            buildConfigField("String", "URL_AUTH", "\"https://qa-auth.tevioapp.com/\"")
            buildConfigField("String", "URL_RESTAURANT", "\"https://qa-restaurant.tevioapp.com/\"")
            buildConfigField("String", "URL_ORDERS", "\"https://qa-order.tevioapp.com/\"")
            buildConfigField(
                "String", "URL_NOTIFICATION", "\"https://qa-notification.tevioapp.com/\""
            )
        }
        create("prod") {
            dimension = "environment"
            versionCode = 1
            versionName = "1.0"
            buildConfigField("String", "URL_AUTH", "\"https://prod-auth.tevioapp.com/\"")
            buildConfigField(
                "String", "URL_RESTAURANT", "\"https://prod-restaurant.tevioapp.com/\""
            )
            buildConfigField("String", "URL_ORDERS", "\"https://prod-order.tevioapp.com/\"")
            buildConfigField(
                "String", "URL_NOTIFICATION", "\"https://prod-notification.tevioapp.com/\""
            )
        }
    }


    applicationVariants.configureEach {
        outputs.configureEach {
            if (this is ApkVariantOutputImpl) {
                val buildTypeName = buildType.name
                val versionNameSafe = versionName
                outputFileName = "TevioDelivery-${buildTypeName}-${versionNameSafe}.apk"
            }
        }
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.intellij" && requested.name == "annotations") {
                useTarget("org.jetbrains:annotations:23.0.0")
            }
        }
    }

    lint {
        lintConfig = file("lint.xml")
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.adapter.rxjava2)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.glide)
    implementation(libs.lottie)
    implementation(libs.shimmer)
    implementation(libs.blurry)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    // ML Kit + CameraX
    implementation(libs.gmsPlayFaceDetection)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava2)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.rxjava2)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    implementation(libs.gmsPlayMaps)
    implementation(libs.gmsPlayLocation)
    implementation (libs.gmsPlayAuth)

    implementation(libs.places)
    implementation(libs.android.maps.utils)
    implementation(libs.googleid)
    implementation(libs.ucrop)
    implementation(libs.androidx.biometric)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    // Agora Voice SDK
    implementation(libs.agoraVoice)

    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    testImplementation(libs.androidx.espresso.accessibility)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.truth)
    kapt(libs.glide.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
