package com.brewery.searcher.feature.home.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brewery.searcher.core.model.SearchType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTypeBottomSheet(
    currentType: SearchType,
    onTypeSelected: (SearchType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = modifier,
    ) {
        SearchTypeContent(
            currentType = currentType,
            onTypeSelected = onTypeSelected
        )
    }
}

@Composable
private fun SearchTypeContent(
    currentType: SearchType,
    onTypeSelected: (SearchType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Search Filter",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )

        SearchType.entries.forEach { type ->
            val icon = when (type) {
                SearchType.ALL_FIELDS -> Icons.Default.Search
                SearchType.BY_CITY -> Icons.Default.LocationCity
                SearchType.BY_COUNTRY -> Icons.Default.Public
                SearchType.BY_STATE -> Icons.Default.Map
            }

            ListItem(
                headlineContent = {
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (type == currentType) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (type == currentType) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = type == currentType,
                        onClick = { onTypeSelected(type) },
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun PreviewSearchTypeBottomSheet() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            SearchTypeContent(
                currentType = SearchType.BY_CITY,
                onTypeSelected = {}
            )
        }
    }
}
