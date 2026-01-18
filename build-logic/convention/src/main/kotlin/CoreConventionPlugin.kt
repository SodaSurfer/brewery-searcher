import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CoreConventionPlugin : Plugin<Project> {
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
                        api(versionCatalog.findLibrary("koin-core").get())
                        api(versionCatalog.findLibrary("napier").get())
                    }
                }
            }
        }
    }
}
