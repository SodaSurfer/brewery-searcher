package com.brewery.searcher.core.datastore.di

import androidx.datastore.core.DataStore
import com.brewery.searcher.core.datastore.createUserSettingsDataStore
import com.brewery.searcher.core.datastore.getAndroidDataStorePath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import com.brewery.searcher.core.datastore.UserSettings as ProtoUserSettings

actual val datastorePlatformModule: Module = module {
    single<DataStore<ProtoUserSettings>> {
        createUserSettingsDataStore { getAndroidDataStorePath(androidContext()) }
    }
}
