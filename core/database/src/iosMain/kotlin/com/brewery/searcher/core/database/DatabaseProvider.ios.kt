package com.brewery.searcher.core.database

import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<BrewerySearcherDatabase> {
    throw NotImplementedError("iOS database not implemented")
}
