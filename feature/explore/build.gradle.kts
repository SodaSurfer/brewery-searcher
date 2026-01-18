plugins {
    id("brewerysearcher.feature")
}

android {
    namespace = "com.brewery.searcher.feature.explore"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(projects.core.designsystem)
            implementation(projects.feature.home)
            implementation(libs.compose.material.icons.extended)
        }
        androidMain.dependencies {
            implementation(libs.maps.compose)
            implementation(libs.play.services.maps)
        }
    }
}
