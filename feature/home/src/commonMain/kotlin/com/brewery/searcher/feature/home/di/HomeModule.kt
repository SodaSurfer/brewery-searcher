package com.brewery.searcher.feature.home.di

import com.brewery.searcher.feature.home.BreweryDetailViewModel
import com.brewery.searcher.feature.home.HomeViewModel
import com.brewery.searcher.feature.home.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
    viewModel { params ->
        BreweryDetailViewModel(
            breweryId = params.get(),
            breweryRepository = get(),
            favoriteBreweryRepository = get(),
        )
    }
}
