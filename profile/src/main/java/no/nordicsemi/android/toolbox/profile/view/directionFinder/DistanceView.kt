package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.Range
import java.util.Locale

@Composable
internal fun DistanceView(value: Int, range: Range) {
    Column {
        DistanceChartView(value = value, range = range)

        Spacer(modifier = Modifier.padding(4.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = String.format(Locale.US, "%ddm", range.from))

            val diff = range.to - range.from
            val part = (diff / 4)
            if (part > 0) {
                Text(text = String.format(Locale.US, "%ddm", range.from + part))
                Text(text = String.format(Locale.US, "%ddm", range.from + 2 * part))
            }

            Text(text = String.format(Locale.US, "%ddm", range.to))
        }
    }
}

@Preview
@Composable
private fun DistanceViewPreview() {
    DistanceView(20, Range(0, 50))
}
