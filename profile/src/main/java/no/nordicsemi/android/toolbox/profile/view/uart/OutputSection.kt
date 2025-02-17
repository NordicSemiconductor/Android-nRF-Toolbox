package no.nordicsemi.android.toolbox.profile.view.uart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.UARTRecord
import no.nordicsemi.android.toolbox.profile.data.UARTRecordType
import no.nordicsemi.android.toolbox.profile.viewmodel.DeviceConnectionViewEvent
import no.nordicsemi.android.toolbox.profile.viewmodel.UARTEvent
import no.nordicsemi.android.ui.view.SectionTitle
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun OutputSection(
    records: List<UARTRecord>,
    onEvent: (DeviceConnectionViewEvent) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(
                resId = R.drawable.ic_output,
                title = "Messages",
                modifier = Modifier.fillMaxSize(),
                menu = { Menu(onEvent) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (records.isEmpty()) {
                    Text(text = stringResource(id = R.string.uart_output_placeholder))
                } else {
                    // Display the latest messages at the start
                    records.reversed().forEach {
                        when (it.type) {
                            UARTRecordType.INPUT -> MessageItemInput(record = it)
                            UARTRecordType.OUTPUT -> MessageItemOutput(record = it)
                        }
                    }
                }
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
        modifier = Modifier.fillMaxWidth(),
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
                .background(MaterialTheme.colorScheme.secondary)
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = record.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondary
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
        modifier = Modifier.fillMaxWidth(),
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
                text = record.text.trimEnd(),
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
private fun Menu(onEvent: (DeviceConnectionViewEvent) -> Unit) {
    Row {
        IconButton(onClick = { onEvent(UARTEvent.ClearOutputItems) }) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.uart_clear_items),
            )
        }
    }
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