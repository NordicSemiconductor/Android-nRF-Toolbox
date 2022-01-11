package no.nordicsemi.dfu.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.data.UploadFailureState

@Composable
internal fun DFUErrorView(state: UploadFailureState, onEvent: (DFUViewEvent) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ScreenSection {
            val errorColor = MaterialTheme.colorScheme.error

            Icon(
                painter = painterResource(id = R.drawable.ic_fail_circle),
                contentDescription = stringResource(id = R.string.dfu_failure_icon_description),
                tint = errorColor
            )


            Spacer(modifier = Modifier.size(8.dp))

            val error = state.message ?: stringResource(id = R.string.dfu_unknown_error)
            Text(
                text = error,
                color = errorColor,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.size(16.dp))

        Button(onClick = { onEvent(OnPauseButtonClick) }) {
            Text(text = stringResource(id = R.string.dfu_close))
        }
    }
}
