rootProject.name = "BrewerySearcher"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
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

include(":composeApp")
include(":core")
include(":core:common")
include(":core:navigation")
include(":core:designsystem")
include(":core:datastore")
include(":core:database")
include(":core:model")
include(":core:network")
include(":core:data")
include(":feature")
include(":feature:home")
include(":feature:explore")
include(":feature:settings")
include(":feature:activity")