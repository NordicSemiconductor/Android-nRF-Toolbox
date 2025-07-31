package no.nordicsemi.android.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun KeyValueColumn(
    value: String,
    key: String,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 8.dp,
    keyStyle: TextStyle?= null
) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalAlignment = Alignment.Start,
            modifier = modifier
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = key,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = keyStyle ?: MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyValueColumnPreview() {
    KeyValueColumn(
        value = "Sample Value",
        key = "Sample Key",
//        keyStyle = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun KeyValueColumn(
    value: String,
    modifier: Modifier = Modifier,
    key: @Composable (() -> Unit)
) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            key()
        }
    }
}

@Composable
fun KeyValueColumnReverse(
    value: String,
    key: String,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 8.dp,
    keyStyle: TextStyle? = null,
) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalAlignment = Alignment.End,
            modifier = modifier
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = key,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = keyStyle ?: MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun KeyValueColumnReverse(
    value: String,
    modifier: Modifier = Modifier,
    key: @Composable (() -> Unit)
) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
            modifier = modifier
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            key()
        }
    }
}