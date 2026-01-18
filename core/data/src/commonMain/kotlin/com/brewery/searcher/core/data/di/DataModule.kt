package com.brewery.searcher.core.data.di

import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.data.repository.BreweryRepositoryImpl
import com.brewery.searcher.core.data.repository.SearchHistoryRepository
import com.brewery.searcher.core.data.repository.SearchHistoryRepositoryImpl
import org.koin.dsl.module

val dataModule = module {
    single<BreweryRepository> { BreweryRepositoryImpl(get()) }
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get()) }
}
