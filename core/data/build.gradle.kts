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
            implementation(projects.core.database)
            api(libs.paging.common)
            implementation(libs.kotlinx.datetime)
        }
    }
}
