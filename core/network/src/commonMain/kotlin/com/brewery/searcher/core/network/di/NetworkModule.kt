package com.brewery.searcher.core.network.di

import com.brewery.searcher.core.network.api.BreweryApiService
import com.brewery.searcher.core.network.api.BreweryApiServiceImpl
import com.brewery.searcher.core.network.createHttpClient
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    single<BreweryApiService> { BreweryApiServiceImpl(get()) }
}
