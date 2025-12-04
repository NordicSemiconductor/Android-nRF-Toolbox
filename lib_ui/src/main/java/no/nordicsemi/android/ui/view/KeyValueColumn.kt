package no.nordicsemi.android.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    key: String,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 4.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    value: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = horizontalAlignment,
        modifier = modifier,
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelMedium,
            overflow = TextOverflow.Ellipsis,
        )
        value()
    }
}

@Composable
fun KeyValueColumn(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 4.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    valueStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    KeyValueColumn(
        key = key,
        modifier = modifier,
        verticalSpacing = verticalSpacing,
        horizontalAlignment = horizontalAlignment,
        value = {
            Text(
                text = value,
                style = valueStyle,
                overflow = TextOverflow.Ellipsis,
            )
        }
    )
}

@Composable
fun KeyValueColumnReverse(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 4.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
    valueStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    KeyValueColumn(
        key = key,
        value = value,
        modifier = modifier,
        verticalSpacing = verticalSpacing,
        horizontalAlignment = horizontalAlignment,
        valueStyle = valueStyle,
    )
}

@Composable
fun KeyValueColumnReverse(
    key: String,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 4.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
    value: @Composable () -> Unit
) {
    KeyValueColumn(
        key = key,
        modifier = modifier,
        verticalSpacing = verticalSpacing,
        horizontalAlignment = horizontalAlignment,
        value = value
    )
}

@Preview(showBackground = true)
@Composable
private fun KeyValueColumnPreview() {
    KeyValueColumn(
        value = "Sample Value",
        key = "Sample Key",
    )
}