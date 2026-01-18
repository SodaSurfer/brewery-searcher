plugins {
    id("brewerysearcher.core")
}

android {
    namespace = "com.brewery.searcher.core.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
            implementation(projects.core.network)
            api(libs.paging.common)
        }
    }
}
