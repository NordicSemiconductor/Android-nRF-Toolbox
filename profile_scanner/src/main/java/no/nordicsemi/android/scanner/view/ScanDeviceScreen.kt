package no.nordicsemi.android.scanner.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.scanner.R
import no.nordicsemi.android.scanner.viewmodel.ScanDevicesViewModel
import no.nordicsemi.android.theme.view.dialog.FlowCanceled
import no.nordicsemi.android.theme.view.dialog.ItemSelectedResult
import no.nordicsemi.android.theme.view.dialog.StringListDialog
import no.nordicsemi.android.theme.view.dialog.StringListDialogConfig
import no.nordicsemi.android.theme.view.dialog.StringListDialogResult
import no.nordicsemi.android.theme.view.dialog.StringListView
import no.nordicsemi.android.utils.exhaustive

@Composable
fun ScanDeviceScreen(serviceId: String, finishAction: (ScanDeviceScreenResult) -> Unit) {
    val viewModel: ScanDevicesViewModel = hiltViewModel()
    val data = viewModel.data.collectAsState().value

    val isScreenActive = viewModel.isActive.collectAsState().value

    LaunchedEffect(isScreenActive) {
        if (!isScreenActive) {
            viewModel.stopScanner()
            finishAction(ScanDeviceScreenResult.OK)
        } else {
            viewModel.startScan(serviceId)
        }
    }

    val names = data.devices.map { it.displayName() }
    StringListDialog(createConfig(names) {
        when (it) {
            FlowCanceled -> finishAction(ScanDeviceScreenResult.CANCEL)
            is ItemSelectedResult -> viewModel.onEvent(OnDeviceSelected(data.devices[it.index]))
        }.exhaustive
    })
}

@Composable
private fun createConfig(devices: List<String>, onClick: (StringListDialogResult) -> Unit): StringListDialogConfig {
    val annotatedString = buildAnnotatedString {
        append(stringResource(id = R.string.connect_to))
        append(" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.W800)) {
            append(stringResource(id = R.string.app_name))
        }
    }
    return StringListDialogConfig(
        title = annotatedString,
        leftIcon = R.drawable.ic_bluetooth,
        items = devices.map { it }
    ) {
        onClick(it)
    }
}

@Preview
@Composable
fun ScanDeviceScreenPreview() {
    val items = listOf("Nordic_HRS", "iPods PRO")
    val config = createConfig(items) {}
    StringListView(config)
}
