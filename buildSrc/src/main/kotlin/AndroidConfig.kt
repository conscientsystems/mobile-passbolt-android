import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

val lintDisabledIssues = setOf(
    "GoogleAppIndexingWarning", "GradleDependency", "NewerVersionAvailable", "UnusedIds",
    "Autofill", "PermissionImpliesUnsupportedChromeOsHardware", "WrongConstant", "RequiredSize",
    "Instantiatable", "InvalidPackage", "MissingTranslation", "ExtraTranslation",
)

/**
 * Registers an aggregate `unitTest` task that runs Android unit tests.
 * AGP 9 renamed unit tests to host tests (`testDebugHostTest`); we depend on whichever exists.
 */
fun Project.registerUnitTestAggregate() {
    val unitTestTask = tasks.register("unitTest")
    afterEvaluate {
        unitTestTask.configure {
            listOf("testDebugHostTest", "testDebugUnitTest").forEach { name ->
                if (project.tasks.names.contains(name)) {
                    dependsOn(name)
                }
            }
        }
    }
}
