import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.artifacts.ComponentSelection

plugins {
    id("com.github.ben-manes.versions")
}

private val prereleaseQualifiers = listOf("alpha", "beta", "rc", "cr", "m", "preview")
private val prereleaseRegex = Regex("(?i).*[.-](${prereleaseQualifiers.joinToString("|")})[.\\d-]*")

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    doFirst {
        gradle.startParameter.isParallelProjectExecutionEnabled = false
    }
    resolutionStrategy {
        componentSelection {
            all { selection: ComponentSelection ->
                if (selection.candidate.version.matches(prereleaseRegex)) {
                    selection.reject("Release candidate")
                }
            }
        }
    }
    outputFormatter = "plain"
    checkForGradleUpdate = true
    revision = "release"
    gradleReleaseChannel = "current"
    reportfileName = "dependecyUpdates"
}
