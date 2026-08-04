plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":inappreview-domain"))
    implementation(project(":encryptedstorage"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.data.inappreview"
}
