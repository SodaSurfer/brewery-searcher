package com.brewery.searcher

import android.app.Application
import com.brewery.searcher.core.common.isDebug
import com.brewery.searcher.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

class BrewerySearcherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (isDebug()) {
            Napier.base(DebugAntilog())
        }
        initKoin {
            androidContext(this@BrewerySearcherApplication)
        }
    }
}
