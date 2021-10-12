package no.nordicsemi.android.theme.view.dialog

sealed class StringListDialogResult

data class ItemSelectedResult(val index: Int): StringListDialogResult()

object FlowCanceled : StringListDialogResult()
