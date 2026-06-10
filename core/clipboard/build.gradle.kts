plugins {
    id("passbolt.android.library")
}

android {
    namespace = "com.passbolt.mobile.android.core.clipboard"
}

dependencies {
    implementation(project(":localization"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}
