plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":accounts"))
    implementation(project(":architecture"))
    implementation(project(":common"))
    implementation(project(":uimodel"))
    implementation(project(":entity"))
    implementation(project(":mappers"))
    implementation(project(":share-domain"))
    implementation(project(":featureflags-domain"))
    implementation(project(":preferences"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.kotlin.coroutines)
    implementation(libs.paging.runtime)
    implementation(libs.timber)

    testImplementation(project(":commontest"))
}

android {
    namespace = "com.passbolt.mobile.android.domain.folders"
}
