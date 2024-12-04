package no.nordicsemi.android.toolbox.libs.profile.view.directionFinder

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.toolbox.lib.profile.R
import no.nordicsemi.android.toolbox.libs.core.data.SensorData
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.displayAzimuth
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.displayDistance
import no.nordicsemi.android.toolbox.libs.core.data.directionFinder.displayElevation

@Composable
internal fun MeasuresView(data: SensorData) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        data.displayAzimuth()?.let {
            MeasureItem(
                resId = R.drawable.ic_azimuth,
                value = data.displayAzimuth()!!,
                title = stringResource(R.string.azimuth_section)
            )

            Spacer(modifier = Modifier.padding(8.dp))
        }
        data.displayDistance()?.let {
            MeasureItem(
                resId = R.drawable.ic_distance,
                value = data.displayDistance()!!,
                title = stringResource(R.string.distance_section)
            )

            Spacer(modifier = Modifier.padding(8.dp))
        }
        data.displayElevation()?.let {
            MeasureItem(
                resId = R.drawable.ic_elevation,
                value = data.displayElevation()!!,
                title = stringResource(R.string.elevation_section)
            )
        }
    }
}

@Composable
private fun MeasureItem(
    @DrawableRes resId: Int,
    value: String,
    title: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value)

        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
                .padding(8.dp)
        )

        Text(text = title)
    }
}
