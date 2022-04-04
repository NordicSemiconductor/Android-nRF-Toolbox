package no.nordicsemi.android.cgms.view

import no.nordicsemi.android.cgms.data.CGMServiceCommand

internal sealed class CGMViewEvent

internal data class OnWorkingModeSelected(val workingMode: CGMServiceCommand) : CGMViewEvent()

internal object NavigateUp : CGMViewEvent()

internal object DisconnectEvent : CGMViewEvent()

internal object OpenLoggerEvent : CGMViewEvent()
