plugins {
    id("passbolt.android.library")
    id(libs.plugins.compose.compiler.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
}

dependencies {
    implementation(project(":authentication"))
    implementation(project(":fulldatarefresh"))
    implementation(project(":architecture"))
    implementation(project(":coreui"))
    implementation(project(":ui"))
    implementation(project(":localization"))
    implementation(project(":policies"))
    implementation(project(":passwordgenerator"))
    implementation(project(":common"))
    implementation(project(":accounts"))
    implementation(project(":mappers"))
    implementation(project(":navigation"))
    implementation(project(":metadata"))
    implementation(project(":supportedresourcetypes"))
    implementation(project(":secrets"))
    implementation(project(":jsonmodel"))
    implementation(project(":resourcetypes"))
    implementation(project(":resources"))
    implementation(project(":serializers"))
    implementation(project(":metadatakeytrust"))
    implementation(project(":idlingresource"))
    implementation(project(":testtags"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.koin.compose)
    implementation(libs.espresso.idling.resource)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.lifecycle.viewmodel)
    implementation(libs.compose.ui.tooling)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.kotlin.serializationjson)
    implementation(libs.androidx.navigation3.runtime)

    debugImplementation(libs.compose.ui.tooling.preview)

    testImplementation(project(":commontest"))
    testImplementation(libs.gson)
    testImplementation(libs.json.path)
    testImplementation(libs.json.assert)
}

android {
    namespace = "com.passbolt.mobile.android.feature.resourceform"
    buildFeatures {
        compose = true
    }
}
