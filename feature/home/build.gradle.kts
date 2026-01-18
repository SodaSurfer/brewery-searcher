plugins {
    id("brewerysearcher.feature")
}

android {
    namespace = "com.brewery.searcher.feature.home"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.material.icons.extended)
        }
    }
}