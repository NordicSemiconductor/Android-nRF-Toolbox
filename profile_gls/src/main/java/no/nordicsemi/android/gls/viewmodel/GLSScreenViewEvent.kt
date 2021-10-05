package no.nordicsemi.android.gls.viewmodel

import no.nordicsemi.android.gls.data.WorkingMode

internal sealed class GLSScreenViewEvent

internal data class OnWorkingModeSelected(val workingMode: WorkingMode) : GLSScreenViewEvent()

internal object DisconnectEvent : GLSScreenViewEvent()
