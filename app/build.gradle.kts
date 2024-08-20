plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.atvtres"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.atvtres"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.android.gms:play-services-maps:18.0.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("gov.nist.math:jama:1.0.3")
    implementation ("org.ejml:ejml-all:0.41")
}