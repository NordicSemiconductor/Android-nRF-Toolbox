package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DistanceChartView(value: Int, range: IntRange) {
    LinearProgressIndicator(
        progress = { value.toFloat() / range.last },
        modifier = Modifier.height(12.dp).fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun DistanceChartViewPreview() {
    DistanceChartView(20, 0..50)
}