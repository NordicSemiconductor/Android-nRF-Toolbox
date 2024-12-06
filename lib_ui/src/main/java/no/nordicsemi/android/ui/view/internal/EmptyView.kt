package no.nordicsemi.android.ui.view.internal

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.WarningView
import no.nordicsemi.android.ui.R

@Composable
fun EmptyView(
    @StringRes title: Int,
    @StringRes hint: Int,
) {
    WarningView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
        title = stringResource(title).uppercase(),
        hint = stringResource(hint).uppercase(),
        hintTextAlign = TextAlign.Justify,
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    EmptyView(
        R.string.app_name,
        R.string.app_name,
    )
}
