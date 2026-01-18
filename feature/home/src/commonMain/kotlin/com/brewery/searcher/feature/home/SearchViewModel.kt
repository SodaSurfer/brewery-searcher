package com.brewery.searcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.SearchType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val breweryRepository: BreweryRepository,
) : ViewModel() {

    companion object {
        val TAG = SearchViewModel::class.simpleName
        private const val MAX_QUERY_LENGTH = 100
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Napier.e(tag = TAG, throwable = throwable) { "Uncaught exception in viewModelScope" }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.ALL_FIELDS)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    val searchResults: Flow<PagingData<Brewery>> = combine(
        _searchQuery.debounce(300),
        _searchType,
    ) { query, type ->
        query to type
    }.flatMapLatest { (query, type) ->
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 3) {
            flowOf(PagingData.empty())
        } else {
            breweryRepository.searchBreweries(trimmedQuery, type)
        }
    }.catch { e ->
        Napier.e(tag = TAG, throwable = e) { "Search flow error" }
        emit(PagingData.empty())
    }.cachedIn(viewModelScope + exceptionHandler)

    fun onQueryChange(query: String) {
        Napier.d(tag = TAG) { "onQueryChange(query=$query)" }
        _searchQuery.value = query.take(MAX_QUERY_LENGTH)
    }

    fun onSearchTypeSelected(type: SearchType) {
        Napier.d(tag = TAG) { "onSearchTypeSelected(type=$type)" }
        _searchType.value = type
        _showBottomSheet.value = false
    }

    fun onShowBottomSheet() {
        Napier.d(tag = TAG) { "onShowBottomSheet()" }
        _showBottomSheet.value = true
    }

    fun onDismissBottomSheet() {
        Napier.d(tag = TAG) { "onDismissBottomSheet()" }
        _showBottomSheet.value = false
    }
}
