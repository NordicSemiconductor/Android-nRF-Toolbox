package no.nordicsemi.android.uart.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.NewLineChar

@Composable
fun NewLineChar.toDisplayString(): String {
    return when (this) {
        NewLineChar.LF -> stringResource(id = R.string.uart_macro_dialog_lf)
        NewLineChar.CR_LF -> stringResource(id = R.string.uart_macro_dialog_cr_lf)
        NewLineChar.CR -> stringResource(id = R.string.uart_macro_dialog_cr)
    }
}
