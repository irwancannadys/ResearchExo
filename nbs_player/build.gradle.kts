plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.researchexo.nbs_player"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Media3 dependencies
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.1.1")  // For DASH playback
    implementation("androidx.media3:media3-exoplayer-hls:1.1.1")   // For HLS playback
    implementation("androidx.media3:media3-ui:1.1.1")           // For UI components
    implementation("androidx.media3:media3-common:1.1.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.1.1")  // For OkHttp DataSource
    implementation("androidx.media3:media3-session:1.1.1")        // For MediaSession support

    // Optional: untuk database caching
    implementation("androidx.media3:media3-database:1.1.1")
}