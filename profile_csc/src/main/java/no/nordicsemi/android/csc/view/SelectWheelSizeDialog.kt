package no.nordicsemi.android.csc.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.csc.R
import no.nordicsemi.android.csc.data.WheelSize
import no.nordicsemi.android.material.you.NordicTheme
import no.nordicsemi.android.theme.view.dialog.FlowCanceled
import no.nordicsemi.android.theme.view.dialog.ItemSelectedResult
import no.nordicsemi.android.theme.view.dialog.StringListDialog
import no.nordicsemi.android.theme.view.dialog.StringListDialogConfig
import no.nordicsemi.android.theme.view.dialog.StringListDialogResult
import no.nordicsemi.android.theme.view.dialog.toAnnotatedString
import no.nordicsemi.android.utils.exhaustive

@Composable
internal fun SelectWheelSizeDialog(onEvent: (StringListDialogResult) -> Unit) {
    val wheelEntries = stringArrayResource(R.array.wheel_entries)
    val wheelValues = stringArrayResource(R.array.wheel_values)

    StringListDialog(createConfig(wheelEntries) {
        onEvent(it)
    })
}

@Composable
private fun createConfig(entries: Array<String>, onResult: (StringListDialogResult) -> Unit): StringListDialogConfig {
    return StringListDialogConfig(
        title = stringResource(id = R.string.csc_dialog_title).toAnnotatedString(),
        items = entries.toList(),
        onResult = onResult
    )
}

@Preview
@Composable
internal fun DefaultPreview() {
    NordicTheme {
        val wheelEntries = stringArrayResource(R.array.wheel_entries)
        StringListDialog(createConfig(wheelEntries) {})
    }
}
