plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":encryptedstorage"))
    implementation(project(":accounts"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.core.inappreview"
}
