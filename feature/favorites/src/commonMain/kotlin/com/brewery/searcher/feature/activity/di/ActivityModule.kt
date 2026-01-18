package com.brewery.searcher.feature.activity.di

import com.brewery.searcher.feature.activity.FavoritesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val activityModule = module {
    viewModelOf(::FavoritesViewModel)
}
