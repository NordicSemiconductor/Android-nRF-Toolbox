package no.nordicsemi.android.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T> DropdownView(
    items: List<T>,
    label: String,
    placeholder: String,
    defaultSelectedItem: T? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    crossinline onItemSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by rememberSaveable { mutableStateOf(defaultSelectedItem) }

    Box {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedText?.toString() ?: placeholder,
                onValueChange = { }, // No need to handle since it's readOnly
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                placeholder = { Text(text = placeholder) },
                label = { Text(text = label) },
                isError = isError,
                supportingText = {
                    if (isError) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
            )
            // Animated dropdown menu
            AnimatedVisibility(visible = expanded) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.exposedDropdownSize(),
                ) {
                    items.forEach {
                        DropdownMenuItem(
                            text = { Text(it.toString()) },
                            onClick = {
                                selectedText = it
                                expanded = false
                                onItemSelected(it)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DropdownViewPreview() {
    val items = listOf("Item 1", "Item 2", "Item 3")
    DropdownView(
        items = items,
        label = "Label",
        placeholder = "Placeholder",
        defaultSelectedItem = items[0],
        onItemSelected = {}
    )
}
