package no.nordicsemi.dfu.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.dfu.DfuBaseService
import no.nordicsemi.android.utils.EMPTY
import no.nordicsemi.dfu.R

@Composable
internal fun DFUSelectFileView() {

    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        result.value = it
    }

    Row {
        Button(onClick = { launcher.launch(DfuBaseService.MIME_TYPE_ZIP) }) {
            Text(text = stringResource(id = R.string.dfu_select_zip))
        }

        Button(onClick = { launcher.launch(DfuBaseService.MIME_TYPE_OCTET_STREAM) }) {
            Text(text = stringResource(id = R.string.dfu_select_hex))
        }
    }
}

@Composable
fun ChooseFileMangerDialog(onDismiss: () -> Unit) {
    val alias = remember { mutableStateOf(String.EMPTY) }
    val command = remember { mutableStateOf(String.EMPTY) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.dfu_macro_dialog_title))
        },
        text = {
            Column {
                Text(stringResource(id = R.string.dfu_macro_dialog_info))

                FileManagerOption.values().forEach {
                    FileManagerItem(item = it) {
                        openFileMangerPlayStore(context, it.url)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(id = R.string.dfu_macro_dialog_dismiss))
            }
        },
        dismissButton = {

        }
    )
}

private fun openFileMangerPlayStore(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

@Composable
private fun FileManagerItem(item: FileManagerOption, onItemSelected: (FileManagerOption) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemSelected(item) }
    ) {
        Text(text = item.title)
    }
}

enum class FileManagerOption(val title: String, val url: String) {
    DRIVE("Drive", "market://details?id=com.google.android.apps.docs"),
    FILE_MANAGER("File Manager", "market://details?id=com.rhmsoft.fm"),
    TOTAL_COMMANDER("Total Commander", "market://details?id=com.ghisler.android.TotalCommander"),
    OTHERS("Search for others", "market://search?q=file manager"),
}
