plugins {
    id("passbolt.android.library")
}

dependencies {
    implementation(project(":architecture"))
}

android {
    namespace = "com.passbolt.mobile.android.domain.mfa"
}
