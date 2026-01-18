plugins {
    id("brewerysearcher.feature")
}

android {
    namespace = "com.brewery.searcher.feature.home"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:designsystem"))
            implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.10.0")
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.paging.common)
            implementation(libs.paging.compose)
        }
    }
}