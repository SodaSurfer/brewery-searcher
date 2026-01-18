plugins {
    id("brewerysearcher.core")
}

android {
    namespace = "com.brewery.searcher.core.designsystem"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
            implementation(projects.core.model)
            implementation(libs.compose.material.icons.extended)
        }
    }
}
