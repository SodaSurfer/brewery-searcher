package com.brewery.searcher.feature.activity.di

import com.brewery.searcher.feature.activity.ActivityViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val activityModule = module {
    viewModelOf(::ActivityViewModel)
}
