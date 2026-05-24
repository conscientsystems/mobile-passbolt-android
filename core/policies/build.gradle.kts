plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":passboltapi"))
    implementation(project(":architecture"))
    implementation(project(":mappers"))
    implementation(project(":common"))
    implementation(project(":dto"))
    implementation(project(":ui"))
    implementation(project(":encryptedstorage"))
    implementation(project(":accounts"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.core.policies"
}
