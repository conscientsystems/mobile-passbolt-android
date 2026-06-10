plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":accounts"))
    implementation(project(":encryptedstorage"))
    implementation(project(":common"))
    implementation(project(":featureflags"))
    implementation(project(":ui"))
    implementation(project(":entity"))
    implementation(project(":rbac"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.core.preferences"
}
