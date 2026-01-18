import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class DatastoreConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("brewerysearcher.kmp.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    androidMain.dependencies {
                        implementation(versionCatalog.findLibrary("koin-android").get())
                    }
                    commonMain.dependencies {
                        api(versionCatalog.findLibrary("datastore-core-okio").get())
                        api(versionCatalog.findLibrary("squareup-wire-runtime").get())
                        api(versionCatalog.findLibrary("koin-core").get())
                    }
                }
            }
        }
    }
}
