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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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

fun modelModule(name: String) = projectModule(name, "model/")

projectModule("app")

coreModule("architecture")
coreModule("accounts")
coreModule("networking")
coreModule("gopenpgp")
coreModule("coreui")
coreModule("common")
coreModule("navigation")
coreModule("qrscan")
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
featureModule("accessibilitypolicies")

serviceModule("passboltapi")
serviceModule("linksapi")
serviceModule("pwnedpasswordsapi")

modelModule("dto")
modelModule("uimodel")
modelModule("mappers")
modelModule("entity")
modelModule("serializers")
modelModule("supportedresourcetypes")
modelModule("jsonmodel")

include(":passwordpolicies-domain")
project(":passwordpolicies-domain").projectDir = file("domain/passwordpolicies")
include(":passwordpolicies-data")
project(":passwordpolicies-data").projectDir = file("data/passwordpolicies")
include(":rbac-domain")
project(":rbac-domain").projectDir = file("domain/rbac")
include(":rbac-data")
project(":rbac-data").projectDir = file("data/rbac")
include(":mobiletransfer-domain")
project(":mobiletransfer-domain").projectDir = file("domain/mobiletransfer")
include(":mobiletransfer-data")
project(":mobiletransfer-data").projectDir = file("data/mobiletransfer")
include(":favourites-domain")
project(":favourites-domain").projectDir = file("domain/favourites")
include(":favourites-data")
project(":favourites-data").projectDir = file("data/favourites")
include(":passwordexpiry-domain")
project(":passwordexpiry-domain").projectDir = file("domain/passwordexpiry")
include(":passwordexpiry-data")
project(":passwordexpiry-data").projectDir = file("data/passwordexpiry")
include(":users-domain")
project(":users-domain").projectDir = file("domain/users")
include(":users-data")
project(":users-data").projectDir = file("data/users")
include(":share-domain")
project(":share-domain").projectDir = file("domain/share")
include(":share-data")
project(":share-data").projectDir = file("data/share")
include(":mfa-domain")
project(":mfa-domain").projectDir = file("domain/mfa")
include(":mfa-data")
project(":mfa-data").projectDir = file("data/mfa")
include(":resourcetypes-domain")
project(":resourcetypes-domain").projectDir = file("domain/resourcetypes")
include(":resourcetypes-data")
project(":resourcetypes-data").projectDir = file("data/resourcetypes")
include(":featureflags-domain")
project(":featureflags-domain").projectDir = file("domain/featureflags")
include(":featureflags-data")
project(":featureflags-data").projectDir = file("data/featureflags")
