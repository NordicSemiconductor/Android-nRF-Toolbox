package no.nordicsemi.android.theme.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString

@Composable
fun String.toAnnotatedString() = buildAnnotatedString {
    append(this@toAnnotatedString)
}
