pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        flatDir { dirs("${rootProject.projectDir}/core/gopenpgp/libs") }
    }
}

rootProject.name = "Passbolt"

fun projectModule(
    name: String,
    directory: String = "",
) {
    include(":$name")
    project(":$name").projectDir = file(directory + name)
}

fun coreModule(name: String) = projectModule(name, "core/")

fun featureModule(name: String) = projectModule(name, "features/")

fun serviceModule(name: String) = projectModule(name, "services/")

fun domainModule(name: String) = projectModule(name, "domain/")

projectModule("app")

coreModule("architecture")
coreModule("accounts")
coreModule("networking")
coreModule("gopenpgp")
coreModule("coreui")
coreModule("common")
coreModule("navigation")
coreModule("qrscan")
coreModule("featureflags")
coreModule("database")
coreModule("resources")
coreModule("security")
coreModule("users")
coreModule("logger")
coreModule("localization")
coreModule("folders")
coreModule("groups")
coreModule("commontest")
coreModule("secrets")
coreModule("passwordgenerator")
coreModule("fulldatarefresh")
coreModule("resourcetypes")
coreModule("notifications")
coreModule("autofill")
coreModule("inappreview")
coreModule("envinfo")
coreModule("idlingresource")
coreModule("otpcore")
coreModule("tags")
coreModule("rbac")
coreModule("policies")
coreModule("metadata")
coreModule("encryptedstorage")
coreModule("authenticationcore")
coreModule("preferences")
coreModule("passphrasememorycache")
coreModule("clipboard")
coreModule("testtags")

featureModule("startup")
featureModule("setup")
featureModule("autofillresources")
featureModule("authentication")
featureModule("main")
featureModule("home")
featureModule("settings")
featureModule("resourcedetails")
featureModule("accountdetails")
featureModule("permissions")
featureModule("locationdetails")
featureModule("createfolder")
featureModule("folderdetails")
featureModule("groupdetails")
featureModule("tagsdetails")
featureModule("featureflagserror")
featureModule("helpmenu")
featureModule("logs")
featureModule("resourcemoremenu")
featureModule("transferaccounttoanotherdevice")
featureModule("otp")
featureModule("resourcepicker")
featureModule("scanotp")
featureModule("otpmoremenu")
featureModule("createresourcemenu")
featureModule("resourceform")
featureModule("metadatakeytrust")

serviceModule("passboltapi")
serviceModule("linksapi")
serviceModule("pwnedpasswordsapi")

domainModule("dto")
domainModule("ui")
domainModule("mappers")
domainModule("entity")
domainModule("serializers")
domainModule("supportedresourcetypes")
domainModule("jsonmodel")
