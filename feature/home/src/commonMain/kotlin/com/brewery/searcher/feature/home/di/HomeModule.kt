package com.brewery.searcher.feature.home.di

import com.brewery.searcher.feature.home.ui.brewerydetail.BreweryDetailViewModel
import com.brewery.searcher.feature.home.ui.home.HomeViewModel
import com.brewery.searcher.feature.home.ui.search.SearchViewModel
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
