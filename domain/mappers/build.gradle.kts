plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":dto"))
    implementation(project(":entity"))
    implementation(project(":common"))
    implementation(project(":ui"))
    implementation(project(":navigation"))
    implementation(project(":jsonmodel"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.gson)
    implementation(project(":supportedresourcetypes"))
}

android {
    namespace = "com.passbolt.mobile.android.domain.mappers"
}
