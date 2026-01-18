package com.brewery.searcher.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import com.brewery.searcher.core.datastore.UserSettings as ProtoUserSettings

actual fun createUserSettingsDataStore(producePath: () -> Path): DataStore<ProtoUserSettings> {
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            producePath = producePath,
            serializer = UserSettingsSerializer,
        ),
    )
}

fun getAndroidDataStorePath(context: Context): Path {
    return context.filesDir.resolve(USER_SETTINGS_FILE_NAME).absolutePath.toPath()
}
