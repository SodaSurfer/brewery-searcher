package com.brewery.searcher.core.database

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<BrewerySearcherDatabase>
