package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCData

@Composable
internal fun WheelSizeView(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    OutlinedButton(onClick = { onEvent(OnShowEditWheelSizeDialogButtonClick) }) {
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column {
                Text(
                    text = stringResource(id = R.string.csc_field_wheel_size),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(text = state.wheelSizeDisplay, style = MaterialTheme.typography.bodyMedium)
            }

            Icon(Icons.Default.ArrowDropDown, contentDescription = "")
        }
    }
}

@Preview
@Composable
private fun WheelSizeViewPreview() {
    WheelSizeView(CSCData()) { }
}
