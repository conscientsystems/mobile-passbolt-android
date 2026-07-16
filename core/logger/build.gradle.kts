plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":localization"))
    implementation(project(":envinfo"))
    implementation(project(":common"))
    implementation(project(":preferences-domain"))
    implementation(project(":uimodel"))
    implementation(project(":accounts"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.core.logger"
}
