plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
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
        
        // 启用MultiDex支持
        multiDexEnabled = true
        
        // 启用R8优化
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // 使用新的编译优化选项
        isCoreLibraryDesugaringEnabled = true
    }
    
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    
    // 启用并行编译
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            // 启用Kotlin编译优化
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all"
            )
        }
    }
    
    // 配置Lint选项
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        ignoreWarnings = false
        warningsAsErrors = false
    }
}

dependencies {
    // Core dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    
    // Desugaring support
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Room dependencies
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")
    
    // Calendar View
    implementation("com.github.kizitonwose:CalendarView:1.0.4")
    
    // MPAndroidChart for statistics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // ThreeTenABP for better date and time handling
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.0")
    
    // Color Picker
    implementation("com.github.dhaval2404:colorpicker:2.3")
    
    // Compose dependencies
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // AndroidX Fragment dependency
    implementation("androidx.fragment:fragment-ktx:1.6.1")
}