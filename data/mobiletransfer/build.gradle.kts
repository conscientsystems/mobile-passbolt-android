plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":mobiletransfer-domain"))
    implementation(project(":architecture"))
    implementation(project(":networking"))
    implementation(project(":dto"))
    implementation(project(":uimodel"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.data.mobiletransfer"
}
