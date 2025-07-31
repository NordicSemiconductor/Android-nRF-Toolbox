package no.nordicsemi.android.toolbox.profile.view.directionFinder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.toolbox.profile.R
import no.nordicsemi.android.toolbox.profile.data.directionFinder.Range
import no.nordicsemi.android.toolbox.profile.viewmodel.DFSEvent
import no.nordicsemi.android.ui.view.ScreenSection
import no.nordicsemi.android.ui.view.SectionTitle

@Composable
internal fun DistanceSection(
    distanceValue: Int,
    range: Range,
    onClick: (DFSEvent) -> Unit,
) {
    ScreenSection {
        SectionTitle(
            R.drawable.ic_distance,
            stringResource(id = R.string.distance_section)
        )
        DistanceView(value = distanceValue, range = range)

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$distanceValue dm",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Column {
            Text(
                stringResource(R.string.distance_range),
                style = MaterialTheme.typography.titleSmall
            )
            RangeSlider(range) {
                onClick(DFSEvent.OnRangeChangedEvent(it))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DistanceSectionPreview() {
    DistanceSection(15, Range(0, 50)) {}
}