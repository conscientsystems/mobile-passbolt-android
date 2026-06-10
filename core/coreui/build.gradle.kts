plugins {
    id("passbolt.android.library")
    id(libs.plugins.compose.compiler.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

dependencies {
    implementation(project(":localization"))
    implementation(project(":ui"))
    implementation(project(":common"))
    implementation(project(":testtags"))

    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.coil.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.windowsizeclass)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.icons)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    debugImplementation(libs.compose.ui.tooling.preview)
}

android {
    namespace = "com.passbolt.mobile.android.core.ui"
    buildFeatures {
        compose = true
    }
}
