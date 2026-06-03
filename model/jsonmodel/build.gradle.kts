plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(libs.json.path)
    implementation(libs.gson)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.domain.jsonmodel"
}
