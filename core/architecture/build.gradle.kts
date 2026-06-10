plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.core.architecture"
    buildFeatures {
        viewBinding = true
    }
}
