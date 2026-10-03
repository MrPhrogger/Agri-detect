plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.agri_detect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.agri_detect"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("com.google.android.material:material:1.11.0")

    // Firebase BoM (Manages all Firebase library versions)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Firebase Products
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth") // Added for Login/Signup

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Added for Google Login

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}