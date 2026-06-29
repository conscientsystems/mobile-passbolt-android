plugins {
    id("passbolt.android.library")
}

android {
    namespace = "com.passbolt.mobile.android.core.fulldatarefresh"
}

dependencies {
    implementation(project(":groups-domain"))
    implementation(project(":users"))
    implementation(project(":architecture"))
    implementation(project(":resources-domain"))
    implementation(project(":folders-domain"))
    implementation(project(":idlingresource"))
    implementation(project(":authentication"))
    implementation(project(":resourcetypes"))
    implementation(project(":metadata"))
    implementation(project(":common"))
    implementation(project(":entity"))
    implementation(project(":featureflags-domain"))
    implementation(project(":accounts"))
    implementation(project(":database"))
    implementation(project(":coreui"))
    implementation(project(":notifications"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin)
    implementation(libs.room.core)
    implementation(libs.espresso.idling.resource)
    implementation(libs.androidx.lifecycle.service)

    testImplementation(project(":commontest"))
}
