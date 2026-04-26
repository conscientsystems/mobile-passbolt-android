plugins {
    id("passbolt.android.library")
}

dependencies {
    api(project(":networking"))
    implementation(project(":common"))

    implementation(platform(libs.koin.bom))
    implementation(libs.retrofit.gsonconverter)
    implementation(libs.koin)
}

android {
    namespace = "com.passbolt.mobile.android.services.linksapi"
}
