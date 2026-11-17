plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.currency_counter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.currency_counter"
        minSdk = 21
        targetSdk = 34
        versionCode = 3
        versionName = "2.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
}

dependencies {

    // Application dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.compose.runtime:runtime:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2") // JUnit for unit tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // AndroidX JUnit
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Espresso for UI tests
    androidTestImplementation("androidx.test:runner:1.5.2") // Test runner
    androidTestImplementation("androidx.test:rules:1.5.0") // Test rules
}
