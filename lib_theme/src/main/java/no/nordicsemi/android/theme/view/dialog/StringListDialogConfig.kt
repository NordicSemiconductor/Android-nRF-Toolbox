package no.nordicsemi.android.theme.view.dialog

import androidx.annotation.DrawableRes
import androidx.compose.ui.text.AnnotatedString

data class StringListDialogConfig(
    val title: AnnotatedString? = null,
    @DrawableRes
    val leftIcon: Int? = null,
    val items: List<String> = emptyList(),
    val onResult: (StringListDialogResult) -> Unit
)
