package no.nordicsemi.dfu.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import no.nordicsemi.dfu.data.HexFileLoadedState

@Composable
internal fun DFUSelectDatFileView(state: HexFileLoadedState, onEvent: (DFUViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(
            icon = Icons.Default.Settings,
            title = stringResource(id = R.string.dfu_choose_file)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = stringResource(id = R.string.dfu_choose_dat_info),
            style = MaterialTheme.typography.bodyMedium
        )

        if (state.isDatFileError) {
            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = stringResource(id = R.string.dfu_load_file_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { onEvent(OnDatFileSelected(it)) }
        }

        Button(onClick = { launcher.launch(DfuBaseService.MIME_TYPE_OCTET_STREAM) }) {
            Text(text = stringResource(id = R.string.dfu_select_dat))
        }
    }
}
