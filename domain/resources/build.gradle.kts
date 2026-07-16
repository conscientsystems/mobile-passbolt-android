plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":architecture"))
    implementation(project(":mappers"))
    implementation(project(":uimodel"))
    implementation(project(":dto"))
    implementation(project(":networking"))
    implementation(project(":share-domain"))
    implementation(project(":favourites-domain"))
    implementation(project(":gopenpgp"))
    implementation(project(":secrets-domain"))
    implementation(project(":resourcetypes"))
    implementation(project(":authentication"))
    implementation(project(":users"))
    implementation(project(":entity"))
    implementation(project(":tags"))
    implementation(project(":serializers"))
    implementation(project(":passwordexpiry-domain"))
    implementation(project(":supportedresourcetypes"))
    implementation(project(":folders-domain"))
    implementation(project(":accounts"))
    implementation(project(":preferences-domain"))
    implementation(project(":privatekey-domain"))
    implementation(project(":passphrasememorycache"))
    implementation(project(":metadata-domain"))
    implementation(project(":jsonmodel"))
    implementation(project(":coreui"))

    implementation(libs.gson)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.json.path)
    implementation(libs.paging.runtime)
    implementation(libs.kotlin.coroutines)

    testImplementation(project(":commontest"))
}

android {
    namespace = "com.passbolt.mobile.android.domain.resources"
}
