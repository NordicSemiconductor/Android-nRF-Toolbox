package no.nordicsemi.dfu.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.dfu.DfuBaseService
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.dfu.R

@Composable
internal fun DFUSelectFileView(onEvent: (DFUViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(icon = Icons.Default.Settings, title = stringResource(id = R.string.dfu_choose_file))

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = stringResource(id = R.string.dfu_choose_info),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.padding(8.dp))

        ButtonsRow(onEvent)
    }
}

@Composable
private fun ButtonsRow(onEvent: (DFUViewEvent) -> Unit) {

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        onEvent(OnFileSelected(it!!))
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = { launcher.launch(DfuBaseService.MIME_TYPE_ZIP) }) {
            Text(text = stringResource(id = R.string.dfu_select_zip))
        }

        Button(onClick = { launcher.launch(DfuBaseService.MIME_TYPE_OCTET_STREAM) }) {
            Text(text = stringResource(id = R.string.dfu_select_hex))
        }
    }
}
