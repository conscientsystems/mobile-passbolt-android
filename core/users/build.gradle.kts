plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":accounts-domain"))
    implementation(project(":dto"))
    implementation(project(":common"))
    implementation(project(":uimodel"))
    implementation(project(":users-domain"))
    implementation(project(":networking"))
    implementation(project(":architecture"))
    implementation(project(":mappers"))
    implementation(project(":database"))
    implementation(project(":entity"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.room.core)
    implementation(libs.sqlite.cipher) { artifact { type = "aar" } }
}

android {
    namespace = "com.passbolt.mobile.android.core.users"
}
