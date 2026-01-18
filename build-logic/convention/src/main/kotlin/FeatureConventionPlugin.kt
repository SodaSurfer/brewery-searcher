import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("brewerysearcher.kmp.library")
            }

            val compose = extensions.getByType(ComposeExtension::class.java).dependencies

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        implementation(compose.ui)
                        implementation(compose.components.resources)
                        implementation(project(":core:common"))
                        implementation(project(":core:navigation"))
                        implementation(versionCatalog.findLibrary("koin-compose-viewmodel").get())
                        implementation(versionCatalog.findLibrary("androidx-lifecycle-viewmodelCompose").get())
                    }
                }
            }
        }
    }
}
