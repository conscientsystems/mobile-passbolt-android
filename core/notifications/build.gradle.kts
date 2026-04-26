plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":localization"))
    implementation(project(":coreui"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.core.notifications"
}
