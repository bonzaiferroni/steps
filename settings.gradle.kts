rootProject.name = "contemplate"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":pondui")
project(":pondui").projectDir = file("pondui/library")
include(":kabinet")
project(":kabinet").projectDir = file("kabinet/library")
include(":klutch")
project(":klutch").projectDir = file("klutch/library")

include(":app")
include(":model")
include(":server")