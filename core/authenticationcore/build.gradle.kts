plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":accounts"))
    implementation(project(":encryptedstorage"))
    implementation(project(":passphrasememorycache"))
    implementation(project(":entity"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.fusionauth.jwt)
}

android {
    namespace = "com.passbolt.mobile.android.core.authentication"
}
