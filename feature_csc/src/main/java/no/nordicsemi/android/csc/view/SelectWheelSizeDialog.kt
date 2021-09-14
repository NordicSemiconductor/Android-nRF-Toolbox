package no.nordicsemi.android.csc.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.theme.Background
import no.nordicsemi.android.theme.TestTheme

@Composable
internal fun SelectWheelSizeDialog(onEvent: (OnWheelSizeSelected) -> Unit) {
    Dialog(onDismissRequest = {}) {
        SelectWheelSizeView(onEvent)
    }
}

@Composable
private fun SelectWheelSizeView(onEvent: (OnWheelSizeSelected) -> Unit) {
    val wheelEntries = stringArrayResource(R.array.wheel_entries)
    val wheelValues = stringArrayResource(R.array.wheel_values)

    Box(Modifier.padding(16.dp)) {
        Column(modifier = Background.whiteRoundedCorners()) {
            Text(text = "Wheel size")
            wheelEntries.forEachIndexed { i, entry ->
                Text(text = entry, modifier = Modifier.clickable {
                    onEvent(OnWheelSizeSelected(wheelValues[i].toInt(), entry))
                })
            }
        }
    }
}

@Preview
@Composable
internal fun DefaultPreview() {
    TestTheme {
        SelectWheelSizeView { }
    }
}
