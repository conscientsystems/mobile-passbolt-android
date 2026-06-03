plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":database"))
    implementation(project(":common"))
    implementation(project(":mappers"))
    implementation(project(":entity"))
    implementation(project(":uimodel"))
    implementation(project(":jsonmodel"))
    implementation(project(":accounts"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.room.core)
    implementation(libs.room.paging)
    implementation(libs.sqlite.cipher) { artifact { type = "aar" } }
}

android {
    namespace = "com.passbolt.mobile.android.core.tags"
}
