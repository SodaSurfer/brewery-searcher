package com.brewery.searcher.core.datastore

import androidx.datastore.core.DataStore
import okio.Path
import com.brewery.searcher.core.datastore.UserSettings as ProtoUserSettings

internal const val USER_SETTINGS_FILE_NAME = "user_settings.pb"

expect fun createUserSettingsDataStore(producePath: () -> Path): DataStore<ProtoUserSettings>
