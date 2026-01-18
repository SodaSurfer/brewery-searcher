package com.brewery.searcher.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brewery.searcher.core.database.dao.FavoriteBreweryDao
import com.brewery.searcher.core.database.dao.SearchHistoryDao
import com.brewery.searcher.core.database.entity.FavoriteBreweryEntity
import com.brewery.searcher.core.database.entity.SearchHistoryEntity

@Database(
    entities = [SearchHistoryEntity::class, FavoriteBreweryEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class BrewerySearcherDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun favoriteBreweryDao(): FavoriteBreweryDao
}
