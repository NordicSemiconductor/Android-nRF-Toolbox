package no.nordicsemi.android.toolbox.scanner.viewmodel

import no.nordicsemi.kotlin.ble.client.android.Peripheral

sealed interface UiClickEvent

data class OnDeviceSelection(
    val peripheral: Peripheral
) : UiClickEvent

data object OnBackClick : UiClickEvent

data object OnRefreshScan : UiClickEvent