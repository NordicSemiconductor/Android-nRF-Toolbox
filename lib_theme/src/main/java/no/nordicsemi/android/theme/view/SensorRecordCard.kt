package no.nordicsemi.android.theme.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.NordicColors

@Composable
fun SensorRecordCard(content: @Composable () -> Unit) {
    Card(
        backgroundColor = NordicColors.NordicGray4.value(),
        shape = RoundedCornerShape(10.dp),
        elevation = 0.dp
    ) {
        content()
    }
}
