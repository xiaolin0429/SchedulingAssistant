plugins {
    id("com.android.application")
}

android {
    namespace = "com.schedule.assistant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.schedule.assistant"
        minSdk = 26
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
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // Calendar View
    implementation("com.github.kizitonwose.calendar:view:2.4.1")
    implementation("com.github.kizitonwose.calendar:compose:2.4.1")
    
    // MPAndroidChart for statistics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // ThreeTenABP for better date and time handling
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.0")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}