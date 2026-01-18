package com.brewery.searcher.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

lateinit var applicationContext: Context
    private set

fun initializeDatabase(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<BrewerySearcherDatabase> {
    val dbFile = applicationContext.getDatabasePath("brewery_searcher.db")
    return Room.databaseBuilder<BrewerySearcherDatabase>(
        context = applicationContext,
        name = dbFile.absolutePath,
    )
}
