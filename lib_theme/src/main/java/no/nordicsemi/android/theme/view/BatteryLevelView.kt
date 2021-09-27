package no.nordicsemi.android.theme.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.NordicColors
import no.nordicsemi.android.theme.R

@Composable
fun BatteryLevelView(batteryLevel: Int) {
    Card(
        backgroundColor = NordicColors.NordicGray4.value(),
        shape = RoundedCornerShape(10.dp),
        elevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            KeyValueField(
                stringResource(id = R.string.field_battery),
                "$batteryLevel%"
            )
        }
    }
}