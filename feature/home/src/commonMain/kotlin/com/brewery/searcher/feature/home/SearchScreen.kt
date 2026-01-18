package com.brewery.searcher.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.SearchType
import com.brewery.searcher.core.network.api.ApiException
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        SearchBar(
            query = searchQuery,
            searchType = searchType,
            onQueryChange = viewModel::onQueryChange,
            onFilterClick = viewModel::onShowBottomSheet,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SearchResultsContent(
            searchResults = searchResults,
            searchQuery = searchQuery,
        )
    }

    if (showBottomSheet) {
        SearchTypeBottomSheet(
            currentType = searchType,
            onTypeSelected = viewModel::onSearchTypeSelected,
            onDismiss = viewModel::onDismissBottomSheet,
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    searchType: SearchType,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search breweries...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                )
            },
            singleLine = true,
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Search by: ${searchType.displayName}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SearchResultsContent(
    searchResults: LazyPagingItems<Brewery>,
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    when {
        searchQuery.isBlank() -> {
            EmptySearchState(
                message = "Enter a search query to find breweries",
                modifier = modifier,
            )
        }

        searchQuery.trim().length < 3 -> {
            EmptySearchState(
                message = "Enter at least 3 characters to search",
                modifier = modifier,
            )
        }

        searchResults.loadState.refresh is LoadState.Loading -> {
            LoadingState(modifier = modifier)
        }

        searchResults.loadState.refresh is LoadState.Error -> {
            val error = (searchResults.loadState.refresh as LoadState.Error).error
            val message = when (error) {
                is ApiException -> error.userMessage
                else -> error.message ?: "Unknown error"
            }
            ErrorState(
                title = "Search failed",
                message = message,
                onRetry = { searchResults.retry() },
                modifier = modifier,
            )
        }

        searchResults.itemCount == 0 && searchResults.loadState.refresh is LoadState.NotLoading -> {
            NoResultsState(query = searchQuery, modifier = modifier)
        }

        else -> {
            BreweryList(
                searchResults = searchResults,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun EmptySearchState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap to retry",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRetry() },
            )
        }
    }
}

@Composable
private fun NoResultsState(
    query: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No breweries found for \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BreweryList(
    searchResults: LazyPagingItems<Brewery>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = searchResults.itemCount,
            key = { index -> searchResults[index]?.id ?: index },
        ) { index ->
            val brewery = searchResults[index]
            if (brewery != null) {
                BreweryItem(brewery = brewery)
            }
        }

        if (searchResults.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        if (searchResults.loadState.append is LoadState.Error) {
            item {
                val error = (searchResults.loadState.append as LoadState.Error).error
                Text(
                    text = "Error loading more: ${error.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { searchResults.retry() },
                )
            }
        }
    }
}

