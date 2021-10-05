package no.nordicsemi.android.csc.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.CSCData

@Composable
internal fun WheelSizeView(state: CSCData, onEvent: (CSCViewEvent) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.wheelSizeDisplay,
        onValueChange = { },
        enabled = false,
        label = { Text(text = stringResource(id = R.string.scs_field_wheel_size)) },
        trailingIcon = { EditIcon(onEvent = onEvent) }
    )
}

@Composable
private fun EditIcon(onEvent: (CSCViewEvent) -> Unit) {
    IconButton(onClick = { onEvent(OnShowEditWheelSizeDialogButtonClick) }) {
        Icon(Icons.Filled.Edit, "Edit wheel size.")
    }
}

@Preview
@Composable
private fun WheelSizeViewPreview() {
    WheelSizeView(CSCData()) { }
}
