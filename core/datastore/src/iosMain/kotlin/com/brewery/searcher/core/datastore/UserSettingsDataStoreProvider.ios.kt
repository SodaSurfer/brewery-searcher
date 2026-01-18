package com.brewery.searcher.core.datastore

import androidx.datastore.core.DataStore
import okio.Path
import com.brewery.searcher.core.datastore.UserSettings as ProtoUserSettings

actual fun createUserSettingsDataStore(producePath: () -> Path): DataStore<ProtoUserSettings> {
    TODO("iOS implementation not yet needed")
}
