plugins {
    id("brewerysearcher.core")
}

android {
    namespace = "com.brewery.searcher.core.common"
    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.navigationevent)
            implementation(libs.navigationevent.compose)
        }
    }
}
