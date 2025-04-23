plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.thebetterinternet.aria"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.thebetterinternet.aria"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            if (project.hasProperty("MY_RELEASE_KEYSTORE")) {
                storeFile = file(project.property("MY_RELEASE_KEYSTORE") as String)
                storePassword = project.property("MY_RELEASE_KEYSTORE_PASSWORD") as String
                keyAlias = project.property("MY_KEY_ALIAS") as String
                keyPassword = project.property("MY_KEY_PASSWORD") as String
            } else {
                storeFile = file("../aria-release.keystore")
                storePassword = "ariabrowser"
                keyAlias = "ariabrowse"
                keyPassword = "ariabrowser"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // whyyyyyy 😭
            isShrinkResources = false // whyyyyyyyyyyy 😭
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("org.mozilla.geckoview:geckoview:137.0.20250325171416")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}