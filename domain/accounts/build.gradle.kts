plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":entity"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.domain.accounts"
}
