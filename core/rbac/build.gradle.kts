plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":passboltapi"))
    implementation(project(":accounts"))
    implementation(project(":encryptedstorage"))
    implementation(project(":mappers"))
    implementation(project(":common"))
    implementation(project(":dto"))
    implementation(project(":uimodel"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.core.rbac"
}
