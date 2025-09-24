package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.UARTRecord
import no.nordicsemi.android.toolbox.profile.data.UARTRecordType
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent
import no.nordicsemi.android.ui.view.SectionTitle
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun OutputSection(
    records: List<UARTRecord>,
    onEvent: (UARTEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Scrollable message area
        OutlinedCard(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(), // Set a fixed height for the message area
        ) {
            SectionTitle(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Messages",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                menu = { Menu(onEvent) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val listState = rememberLazyListState()
            LaunchedEffect(records.size) {
                listState.animateScrollToItem(records.lastIndex.coerceAtLeast(0))
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(16.dp)
                    .heightIn(max = 500.dp), // Set a fixed height for the message area
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (records.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.uart_output_placeholder),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(records) { record ->
                        when (record.type) {
                            UARTRecordType.INPUT -> MessageItemInput(record)
                            UARTRecordType.OUTPUT -> MessageItemOutput(record)
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()
                InputSection(
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OutputSectionPreview() {
    OutputSection(
        records = emptyList()
    ) { }
}

@Composable
private fun MessageItemInput(record: UARTRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = record.timeToString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = record.text.visualizeNewlines(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageItemInputPreview() {
    MessageItemInput(
        record = UARTRecord(
            text = "Hello, World!",
            type = UARTRecordType.INPUT
        )
    )
}

@Composable
private fun MessageItemOutput(record: UARTRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = record.timeToString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = record.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageItemOutputPreview() {
    MessageItemOutput(
        record = UARTRecord(
            text = "Hello, World!",
            type = UARTRecordType.OUTPUT
        )
    )
}

@Composable
private fun Menu(onEvent: (UARTEvent) -> Unit) {
    Icon(
        Icons.Default.Delete,
        contentDescription = stringResource(id = R.string.uart_clear_items),
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onEvent(UARTEvent.ClearOutputItems) }
            .padding(8.dp),
        tint = MaterialTheme.colorScheme.error,
    )
}

@Preview(showBackground = true)
@Composable
private fun MenuPreview() {
    Menu(onEvent = {})
}

private val datFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH)

private fun UARTRecord.timeToString(): String {
    return datFormatter.format(timestamp)
}

/**
 * Visualizes newlines in a string by replacing them with Unicode characters.
 * - `\n` is replaced with `␤` (U+240A)
 * - `\r` is replaced with `␍` (U+240D)
 * - `\r\n` is replaced with `␤␍` (U+240A U+240D)
 */
internal fun String.visualizeNewlines(): String {
    return this
        .replace("\r\n", "\u240A\u240D\r")
        .replace(Regex("(?<!\r)\n"), "\u240A")
        .replace(Regex("(?<!\u240D)\r(?!\n)"), "\u240D")
}

