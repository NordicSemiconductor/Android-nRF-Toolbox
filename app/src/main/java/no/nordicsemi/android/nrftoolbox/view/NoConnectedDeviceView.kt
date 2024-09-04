package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.core.parseBold
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.ui.view.WarningView
import no.nordicsemi.android.nrftoolbox.R

@Composable
internal fun NoConnectedDeviceView() {
    WarningView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        painterResource = painterResource(id = R.drawable.ic_launcher_foreground),
        title = "CAN'T SEE CONNECTED DEVICES?",
        hint = "1. Make sure the device is turned on and is connected to a power source.\n\n" +
                "2. Make sure the device is in the range of your phone.\n\n".parseBold(),
        hintTextAlign = TextAlign.Justify,
    )
}

@Preview(showBackground = true)
@Composable
private fun NoConnectedDeviceViewPreview() {
    NordicTheme {
        NoConnectedDeviceView()
    }
}
