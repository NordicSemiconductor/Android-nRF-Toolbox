package no.nordicsemi.android.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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
    var selectedIndex by rememberSaveable { mutableIntStateOf(items.indexOfFirst { it == selectedText }) }

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        OutlinedTextField(
            value = selectedText?.toString() ?: placeholder,
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth(),
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
        // Transparent clickable surface on top of OutlinedTextField
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable { expanded = true },
            color = Color.Transparent,
        ) { }
    }

    AnimatedVisibility(visible = expanded) {
        Dialog(onDismissRequest = { expanded = false }) {
            Surface(shape = RoundedCornerShape(12.dp)) {
                val listState = rememberLazyListState()
                if (selectedIndex > -1) {
                    LaunchedEffect("ScrollToSelected") {
                        listState.scrollToItem(index = selectedIndex)
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth(), state = listState) {
                    itemsIndexed(items) { index, item ->
                        val selectedItem = index == selectedIndex
                        val (backgroundColor, itemColor) = if (selectedItem) {
                            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                        }
                        DropdownMenuItem(
                            colors = MenuDefaults.itemColors(itemColor),
                            modifier = Modifier.background(backgroundColor),
                            text = { Text(item.toString()) },
                            onClick = {
                                selectedText = item
                                selectedIndex = index
                                expanded = false
                                onItemSelected(item)
                            }
                        )

                        if (index < items.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
