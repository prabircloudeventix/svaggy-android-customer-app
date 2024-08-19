
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    id ("androidx.navigation.safeargs.kotlin")
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")

}

android {
    namespace = "com.svaggy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.svaggy"
        minSdk = 24
        targetSdk = 34
        versionCode = 28
        versionName = "1.0.27"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = "svaggyuser"
            keyPassword = "Svaggy360#"
            storeFile = file("/Users/johndoe/Documents/Preet/svaggy/svaggy2.0/svaggy-android/keystore/svaggyuser.jks")
            storePassword = "Svaggy360#"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
         //   buildConfigField("String", "BASE_URL", "\"http://15.207.194.62:3010/\"")
           buildConfigField("String", "BASE_URL", "\"https://svaggy.com:3010/\"")
            buildConfigField("String","PUSHER_KEY","\"26f52ec946b00ad2d9a8\"")
            buildConfigField("String","PUSHER_CHANNEL","\"USER_COMMON_CHANNEL\"")
            signingConfig = signingConfigs.getByName("release")


        }
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BASE_URL", "\"http://15.207.194.62:3010/\"")
    //    buildConfigField("String", "BASE_URL", "\" https://svaggy.com:3010/\"")
           buildConfigField("String","PUSHER_KEY","\"26f52ec946b00ad2d9a8\"")
            buildConfigField("String","PUSHER_CHANNEL","\"USER_COMMON_CHANNEL\"")

        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildToolsVersion = "33.0.1"
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Layout and Text Size
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")

    // Navigation Graph
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.7.7")

    // country picker
    implementation("io.michaelrocks:libphonenumber-android:8.13.13")

    //google
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
    implementation ("com.google.maps.android:android-maps-utils:2.2.1")

    //shimmer
    //glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.2coffees1team:GlideToVectorYou:v2.0.0")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.7.2")
    // GSON
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // Digger Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")
    // dependency for slider view
  //  implementation("com.github.smarteist:autoimageslider:1.3.9")
    // Location
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-places:17.0.0")
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation ("androidx.paging:paging-runtime-ktx:3.2.1")
    // Stripe Android SDK
   // implementation ("com.stripe:stripe-android:20.36.1")
    implementation ("com.stripe:stripe-android:20.47.4")
    implementation ("com.google.android.gms:play-services-wallet:19.4.0")

    //google map
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.maps:google-maps-services:0.18.0")
    //Pusher
    implementation("com.pusher:pusher-java-client:2.2.1")
    //Lotte
    implementation ("com.airbnb.android:lottie:6.3.0")
    //memory leak
  //  debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.7")
    // Testing Navigation
  //  androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")
    //Facebook Shimmer Affect
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    // Jackson Module @Json
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")
    //chat spot
    implementation ("com.github.livechat:chat-window-android:v2.2.1")
    // For Kotlin users also import the Kotlin extensions library for Play In-App Update:
    implementation ("com.google.android.play:app-update-ktx:2.1.0")
  //  implementation("com.google.android.play:app-update:2.1.0")
   // double click
  //  implementation ("com.gitlab.developerdeveloperdeveloper:androidutilslibrary:1.0.0")
    // Add or update Play Feature Delivery library
    implementation("com.google.android.play:feature-delivery:2.1.0") // Replace with the latest version



    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}