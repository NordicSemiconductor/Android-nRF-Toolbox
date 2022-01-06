package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.dfu.R

@Composable
internal fun DFUSuccessView(onEvent: (DFUViewEvent) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Icon(
            painter = painterResource(id = R.drawable.ic_success_circle),
            contentDescription = stringResource(id = R.string.dfu_success_icon_description)
        )

        Spacer(modifier = Modifier.padding(16.dp))

        Button(onClick = { onEvent(OnPauseButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_done))
        }
    }
}
