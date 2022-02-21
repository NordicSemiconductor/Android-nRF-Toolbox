package no.nordicsemi.android.uart.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.uart.R
import no.nordicsemi.android.uart.data.MacroEol

@Composable
fun MacroEol.toDisplayString(): String {
    return when (this) {
        MacroEol.LF -> stringResource(id = R.string.uart_macro_dialog_lf)
        MacroEol.CR_LF -> stringResource(id = R.string.uart_macro_dialog_cr_lf)
        MacroEol.CR -> stringResource(id = R.string.uart_macro_dialog_cr)
    }
}
