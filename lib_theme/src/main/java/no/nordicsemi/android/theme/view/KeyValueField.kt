package no.nordicsemi.android.theme.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.nordicsemi.android.theme.NordicColors

@Composable
fun KeyValueField(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = key)
        Text(
            color = NordicColors.NordicDarkGray.value(),
            text = value
        )
    }
}
