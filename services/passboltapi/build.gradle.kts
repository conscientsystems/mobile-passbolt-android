plugins {
    id("passbolt.android.library")
}

dependencies {
    api(project(":networking"))
    implementation(project(":dto"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.services.passboltapi"
}
