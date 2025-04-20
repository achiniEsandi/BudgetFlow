plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.budgetflow"
    compileSdk = 35
    // Changed to match targetSdk (34)

    defaultConfig {
        applicationId = "com.example.budgetflow"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Add these if missing
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Add this buildFeatures section
    buildFeatures {
        viewBinding = true  // or dataBinding if you use it
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Update these versions to be consistent
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)  // Ensure this is 1.6.1+
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Remove duplicate appcompat dependency
    // implementation("androidx.appcompat:appcompat:1.4.0")  // Duplicate

    implementation ("com.google.code.gson:gson:2.8.8")


    // Add these if missing
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.firebase.crashlytics.buildtools)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}