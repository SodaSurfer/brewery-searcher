package com.brewery.searcher.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brewery.searcher.core.database.dao.SearchHistoryDao
import com.brewery.searcher.core.database.entity.SearchHistoryEntity

@Database(
    entities = [SearchHistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BrewerySearcherDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
}
