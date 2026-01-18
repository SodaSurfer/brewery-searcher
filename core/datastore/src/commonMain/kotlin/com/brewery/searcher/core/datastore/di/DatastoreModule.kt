package com.brewery.searcher.core.datastore.di

import com.brewery.searcher.core.datastore.UserSettingsDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

expect val datastorePlatformModule: Module

val datastoreModule = module {
    single { UserSettingsDataSource(get()) }
}
