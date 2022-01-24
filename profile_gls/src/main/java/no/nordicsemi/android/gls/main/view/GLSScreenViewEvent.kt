package no.nordicsemi.android.gls.main.view

import no.nordicsemi.android.gls.data.GLSRecord
import no.nordicsemi.android.gls.data.WorkingMode

internal sealed class GLSScreenViewEvent

internal data class OnWorkingModeSelected(val workingMode: WorkingMode) : GLSScreenViewEvent()

internal data class OnGLSRecordClick(val record: GLSRecord) : GLSScreenViewEvent()

internal object DisconnectEvent : GLSScreenViewEvent()
