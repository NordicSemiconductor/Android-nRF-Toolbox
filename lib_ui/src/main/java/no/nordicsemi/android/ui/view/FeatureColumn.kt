package no.nordicsemi.android.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FeaturesColumn(
    content: @Composable FeatureColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FeatureColumnScopeInstance.content()
    }
}

private object FeatureColumnScopeInstance : FeatureColumnScope

interface FeatureColumnScope {

    @Composable
    fun FeatureRow(
        text: String,
        supported: Boolean,
    ) {
        if (supported) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun FeatureRowPreview() {
    FeaturesColumn {
        FeatureRow(
            text = "Instantaneous stride length measurement supported",
            supported = true
        )
    }
}
