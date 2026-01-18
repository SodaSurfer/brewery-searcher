package com.brewery.searcher.core.database.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.brewery.searcher.core.database.BrewerySearcherDatabase
import com.brewery.searcher.core.database.getDatabaseBuilder
import org.koin.dsl.module

val databaseModule = module {
    single<BrewerySearcherDatabase> {
        getDatabaseBuilder()
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<BrewerySearcherDatabase>().searchHistoryDao() }
}
