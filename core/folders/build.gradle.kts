plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":architecture"))
    implementation(project(":entity"))
    implementation(project(":mappers"))
    implementation(project(":uimodel"))
    implementation(project(":dto"))
    implementation(project(":networking"))
    implementation(project(":passboltapi"))
    implementation(project(":share-domain"))
    implementation(project(":database"))
    implementation(project(":featureflags"))
    implementation(project(":accounts"))
    implementation(project(":preferences"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.room.core)
    implementation(libs.room.paging)
    implementation(libs.sqlite.cipher) { artifact { type = "aar" } }
}

android {
    namespace = "com.passbolt.mobile.android.core.folders"
}
