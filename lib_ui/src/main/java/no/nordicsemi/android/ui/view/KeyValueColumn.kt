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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun KeyValueColumn(
    value: String = "String",
    key: String = "Key",
    icon: ImageVector? = null,
) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
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
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KeyValueColumnReverse(
    value: String = "String Reverse",
    key: String = "Key Reverse",
    icon: ImageVector? = null,
) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
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
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
