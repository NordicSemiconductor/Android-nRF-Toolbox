package no.nordicsemi.android.toolbox.scanner.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.WarningView

@Composable
internal fun ScanErrorView(
    error: Throwable? = null,
) {
    WarningView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
        title = "Scanning failed",
        hint = "Scanning failed with error: ${error?.message}.",
    )
}

@Preview(showBackground = true)
@Composable
private fun ErrorSectionPreview() {
    MaterialTheme {
        ScanErrorView(null)
    }
}