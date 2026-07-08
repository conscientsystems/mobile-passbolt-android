plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":accounts"))
    implementation(project(":architecture"))
    implementation(project(":common"))
    implementation(project(":entity"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.domain.featureflags"
}
