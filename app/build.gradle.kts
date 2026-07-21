plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.zagirlek.ya_money"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zagirlek.ya_money"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.systemdesign)
    implementation(projects.core.ui)
    implementation(projects.feature.transactions)
    implementation(projects.feature.accounts)
    implementation(projects.feature.analytics)
    implementation(projects.finance.api)
    implementation(projects.finance.impl)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.decompose)
    implementation(libs.decompose.compose)
}
