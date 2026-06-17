plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":passwordpolicies-domain"))
    implementation(project(":architecture"))
    implementation(project(":networking"))
    implementation(project(":dto"))
    implementation(project(":accounts"))
    implementation(project(":common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.data.passwordpolicies"
}
