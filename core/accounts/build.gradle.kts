plugins {
    id("passbolt.android.library")
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":encryptedstorage"))
    implementation(project(":biometrickey-domain"))
    implementation(project(":privatekey-domain"))
    implementation(project(":gopenpgp"))
    implementation(project(":dto"))
    implementation(project(":entity"))
    implementation(project(":navigation"))
    implementation(project(":uimodel"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.kotlin.serializationjson)
    implementation(libs.security)
}

android {
    namespace = "com.passbolt.mobile.android.core.accounts"
}
