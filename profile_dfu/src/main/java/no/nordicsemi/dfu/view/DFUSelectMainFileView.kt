package no.nordicsemi.dfu.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.dfu.DfuBaseService
import no.nordicsemi.android.theme.view.ScreenSection
import no.nordicsemi.android.theme.view.SectionTitle
import no.nordicsemi.dfu.R
import no.nordicsemi.dfu.data.NoFileSelectedState

@Composable
internal fun DFUSelectMainFileView(state: NoFileSelectedState, onEvent: (DFUViewEvent) -> Unit) {
    ScreenSection {
        SectionTitle(
            icon = Icons.Default.Settings,
            title = stringResource(id = R.string.dfu_choose_file)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = stringResource(id = R.string.dfu_choose_info),
            style = MaterialTheme.typography.bodyMedium
        )

        if (state.isError) {
            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = stringResource(id = R.string.dfu_load_file_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        ButtonsRow(onEvent)
    }
}

@Composable
private fun ButtonsRow(onEvent: (DFUViewEvent) -> Unit) {

    val fileType = rememberSaveable { mutableStateOf(DfuBaseService.MIME_TYPE_ZIP) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onEvent(OnZipFileSelected(it)) }
    }

    Button(onClick = {
        fileType.value = DfuBaseService.MIME_TYPE_ZIP
        launcher.launch(fileType.value)
    }) {
        Text(text = stringResource(id = R.string.dfu_select_zip))
    }
}
