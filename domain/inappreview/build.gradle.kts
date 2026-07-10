plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":accounts"))
    implementation(project(":common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.timber)
}

android {
    namespace = "com.passbolt.mobile.android.domain.inappreview"
}
