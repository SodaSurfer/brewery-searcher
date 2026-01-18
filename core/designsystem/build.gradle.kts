plugins {
    id("brewerysearcher.core")
}

android {
    namespace = "com.brewery.searcher.core.designsystem"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))

            implementation(compose.material3)
            implementation(libs.compose.material.icons.extended)
        }
    }
}
