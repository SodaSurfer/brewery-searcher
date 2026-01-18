package com.brewery.searcher.di

import com.brewery.searcher.core.data.di.dataModule
import com.brewery.searcher.core.datastore.di.datastoreModule
import com.brewery.searcher.core.datastore.di.datastorePlatformModule
import com.brewery.searcher.core.network.di.networkModule
import com.brewery.searcher.feature.activity.di.activityModule
import com.brewery.searcher.feature.explore.di.exploreModule
import com.brewery.searcher.feature.home.di.homeModule
import com.brewery.searcher.feature.settings.di.settingsModule
import com.brewery.searcher.ui.MainViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules())
    }
}

val mainModule = module {
    viewModelOf(::MainViewModel)
}

fun appModules(): List<Module> = listOf(
    datastorePlatformModule,
    datastoreModule,
    networkModule,
    dataModule,
    mainModule,
    homeModule,
    exploreModule,
    activityModule,
    settingsModule,
)
