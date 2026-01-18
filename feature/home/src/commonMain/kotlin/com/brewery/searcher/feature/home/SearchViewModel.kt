package com.brewery.searcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.data.repository.SearchHistoryRepository
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.SearchHistory
import com.brewery.searcher.core.model.SearchType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

sealed interface SearchHistoryState {
    data object Loading : SearchHistoryState
    data class Success(val history: List<SearchHistory>) : SearchHistoryState
    data class Error(val message: String) : SearchHistoryState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val breweryRepository: BreweryRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
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

    val searchHistoryState: StateFlow<SearchHistoryState> = searchHistoryRepository
        .getRecentSearches()
        .map<List<SearchHistory>, SearchHistoryState> { SearchHistoryState.Success(it) }
        .catch { e ->
            Napier.e(tag = TAG, throwable = e) { "Search history flow error" }
            emit(SearchHistoryState.Error("Failed to load search history"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchHistoryState.Loading,
        )

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

    fun onSearchSubmit() {
        val query = _searchQuery.value.trim()
        Napier.d(tag = TAG) { "onSearchSubmit(query=$query)" }
        if (query.length >= 3) {
            viewModelScope.launch {
                searchHistoryRepository.saveSearch(query, _searchType.value)
            }
        }
    }

    fun onHistoryItemClick(item: SearchHistory) {
        Napier.d(tag = TAG) { "onHistoryItemClick(item=$item)" }
        _searchQuery.value = item.query
        _searchType.value = item.searchType
    }

    fun onDeleteHistoryItem(id: Long) {
        Napier.d(tag = TAG) { "onDeleteHistoryItem(id=$id)" }
        viewModelScope.launch {
            searchHistoryRepository.deleteSearch(id)
        }
    }

    fun onClearHistory() {
        Napier.d(tag = TAG) { "onClearHistory()" }
        viewModelScope.launch {
            searchHistoryRepository.clearAll()
        }
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
