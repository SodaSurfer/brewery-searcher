package com.brewery.searcher.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.brewery.searcher.core.designsystem.component.BreweryListItem
import com.brewery.searcher.core.designsystem.component.BreweryTopBar
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.SearchType
import com.brewery.searcher.core.network.api.ApiException
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            BreweryTopBar(
                title = "Search Breweries",
                onBackClick = onBackClick
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = modifier.fillMaxSize().imePadding(),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

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
        }

        if (showBottomSheet) {
            SearchTypeBottomSheet(
                currentType = searchType,
                onTypeSelected = viewModel::onSearchTypeSelected,
                onDismiss = viewModel::onDismissBottomSheet,
            )
        }
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
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search for breweries...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Searching by: ${searchType.displayName}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
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
                message = "Start typing to find breweries",
                modifier = modifier,
            )
        }

        searchQuery.trim().length < 3 -> {
            EmptySearchState(
                message = "Enter at least 3 characters",
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
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            count = searchResults.itemCount,
            key = { index -> searchResults[index]?.id ?: index },
        ) { index ->
            val brewery = searchResults[index]
            if (brewery != null) {
                BreweryListItem(brewery = brewery)
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

@Preview
@Composable
fun PreviewSearchBar() {
    MaterialTheme {
        SearchBar(
            query = "Brew",
            searchType = SearchType.BY_CITY,
            onQueryChange = {},
            onFilterClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun PreviewEmptyState() {
    MaterialTheme {
        Surface {
            EmptySearchState("Start typing...")
        }
    }
}


