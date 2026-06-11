plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":architecture"))
    implementation(project(":uimodel"))
    // TODO MOB-4496: networking + dto are only required by the not-yet-migrated getUsers signature
    implementation(project(":networking"))
    implementation(project(":dto"))
}

android {
    namespace = "com.passbolt.mobile.android.domain.users"
}
