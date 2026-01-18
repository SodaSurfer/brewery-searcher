package com.brewery.searcher.core.data.di

import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.data.repository.BreweryRepositoryImpl
import org.koin.dsl.module

val dataModule = module {
    single<BreweryRepository> { BreweryRepositoryImpl(get()) }
}
