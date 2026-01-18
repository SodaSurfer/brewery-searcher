import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.brewery.searcher.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeMultiplatform.gradlePlugin)
    compileOnly(libs.wire.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "brewerysearcher.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("featureLibrary") {
            id = "brewerysearcher.feature"
            implementationClass = "FeatureConventionPlugin"
        }
        register("coreLibrary") {
            id = "brewerysearcher.core"
            implementationClass = "CoreConventionPlugin"
        }
        register("navigationLibrary") {
            id = "brewerysearcher.navigation"
            implementationClass = "NavigationConventionPlugin"
        }
        register("datastoreLibrary") {
            id = "brewerysearcher.datastore"
            implementationClass = "DatastoreConventionPlugin"
        }
    }
}
