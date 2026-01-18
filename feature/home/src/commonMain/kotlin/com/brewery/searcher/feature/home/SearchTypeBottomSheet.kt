package com.brewery.searcher.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brewery.searcher.core.model.SearchType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTypeBottomSheet(
    currentType: SearchType,
    onTypeSelected: (SearchType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.Companion,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Search by",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            SearchType.entries.forEach { type ->
                ListItem(
                    headlineContent = { Text(type.displayName) },
                    leadingContent = {
                        RadioButton(
                            selected = type == currentType,
                            onClick = { onTypeSelected(type) },
                        )
                    },
                    modifier = Modifier.clickable { onTypeSelected(type) },
                )
            }
        }
    }
}