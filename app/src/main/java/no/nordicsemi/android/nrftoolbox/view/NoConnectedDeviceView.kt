package no.nordicsemi.android.nrftoolbox.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.nrftoolbox.R

@Composable
internal fun NoConnectedDeviceView() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_notification_icon),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )

        Text(text = "CAN'T SEE CONNECTED DEVICES?")
        Text(
            text = "1. Make sure the device is turned on and is connected to a power source.\n\n" +
                    "2. Make sure the device is in the range of your phone.\n\n",
            textAlign = TextAlign.Justify
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoConnectedDeviceViewPreview() {
    NordicTheme {
        NoConnectedDeviceView()
    }
}
