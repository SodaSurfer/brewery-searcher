package com.brewery.searcher.feature.explore.di

import com.brewery.searcher.feature.explore.ui.explore.ExploreViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val exploreModule = module {
    viewModelOf(::ExploreViewModel)
}
