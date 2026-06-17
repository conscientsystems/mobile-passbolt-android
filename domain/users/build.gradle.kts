plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":architecture"))
    implementation(project(":uimodel"))
}

android {
    namespace = "com.passbolt.mobile.android.domain.users"
}
