package no.nordicsemi.android.cgms.data

internal sealed class CGMEvent

internal object Idle : CGMEvent()

internal data class OnCGMValueReceived(val record: CGMRecord) : CGMEvent()

internal object OnOperationStarted : CGMEvent()

internal object OnOperationCompleted : CGMEvent()

internal object OnOperationFailed : CGMEvent()

internal object OnOperationAborted : CGMEvent()

internal object OnOperationNotSupported : CGMEvent()

internal object OnDataSetCleared : CGMEvent()

internal data class OnNumberOfRecordsRequested(val value: Int) : CGMEvent()
