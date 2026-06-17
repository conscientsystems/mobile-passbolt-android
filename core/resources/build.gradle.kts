plugins {
    id("passbolt.android.library")
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":architecture"))
    implementation(project(":mappers"))
    implementation(project(":uimodel"))
    implementation(project(":dto"))
    implementation(project(":networking"))
    implementation(project(":passboltapi"))
    implementation(project(":favourites-domain"))
    implementation(project(":database"))
    implementation(project(":gopenpgp"))
    implementation(project(":secrets"))
    implementation(project(":resourcetypes"))
    implementation(project(":authentication"))
    implementation(project(":users"))
    implementation(project(":entity"))
    implementation(project(":tags"))
    implementation(project(":serializers"))
    implementation(project(":passwordexpiry-domain"))
    implementation(project(":supportedresourcetypes"))
    implementation(project(":folders"))
    implementation(project(":accounts"))
    implementation(project(":preferences"))
    implementation(project(":passphrasememorycache"))
    implementation(project(":metadata"))
    implementation(project(":jsonmodel"))
    implementation(project(":coreui"))

    implementation(libs.gson)
    implementation(libs.room.core)
    implementation(libs.sqlite.cipher) { artifact { type = "aar" } }
    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.json.path)
    implementation(libs.paging.runtime)
}

android {
    namespace = "com.passbolt.mobile.android.core.resources"
}
