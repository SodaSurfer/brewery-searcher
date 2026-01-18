package com.brewery.searcher.feature.explore.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: (doNotAskAgain: Boolean) -> Unit,
) {
    var doNotAskAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss(doNotAskAgain) },
        title = { Text("Location Access") },
        text = {
            Column {
                Text("BrewerySearcher needs your location to show breweries near you.")
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = doNotAskAgain,
                        onCheckedChange = { doNotAskAgain = it }
                    )
                    Text("Do not ask me again")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Allow") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(doNotAskAgain) }) { Text("Cancel") }
        }
    )
}
