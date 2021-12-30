package no.nordicsemi.android.cgms.view

import no.nordicsemi.android.cgms.data.WorkingMode

internal sealed class CGMViewEvent

internal data class OnWorkingModeSelected(val workingMode: WorkingMode) : CGMViewEvent()

internal object DisconnectEvent : CGMViewEvent()
