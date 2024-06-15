plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.android.s22present"
    //noinspection GradleDependency
    compileSdk = 34
    lint {
        baseline = file("lint-baseline.xml")
    }
    defaultConfig {
        applicationId = "com.android.s22present"
        minSdk = 30
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 34
        versionCode = 3
        versionName = "0.2.1"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}
dependencies {
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.runtime.saved.instance.state)
    val libsuVersion = "5.2.2"
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("com.github.topjohnwu.libsu:core:${libsuVersion}")
    implementation ("com.github.topjohnwu.libsu:service:${libsuVersion}")
    implementation ("org.lsposed.hiddenapibypass:hiddenapibypass:2.0")
    implementation("io.github.gautamchibde:audiovisualizer:2.2.5")
}

