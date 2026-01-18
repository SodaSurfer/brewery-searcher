plugins {
    id("brewerysearcher.datastore")
    alias(libs.plugins.wire)
}

android {
    namespace = "com.brewery.searcher.core.datastore"
}

wire {
    kotlin {}
    sourcePath {
        srcDir("src/commonMain/proto")
    }
}
