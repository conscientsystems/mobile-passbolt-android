plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.plugin)
    implementation(libs.kotlin.parcelize.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.dependency.analysis.gradle.plugin)
    implementation(libs.gradle.versions.plugin)
    implementation(libs.easylauncher)
    implementation(libs.licensee)
    implementation(libs.app.distribution.gradle.plugin)
}
