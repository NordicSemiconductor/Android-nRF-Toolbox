package no.nordicsemi.dfu.data

internal sealed class DFUServiceStatus

internal object Idle : DFUServiceStatus()
internal object Connecting : DFUServiceStatus()
internal object Connected : DFUServiceStatus()
internal object Starting : DFUServiceStatus()
internal object Started : DFUServiceStatus()
internal object EnablingDfu : DFUServiceStatus()
internal data class ProgressUpdate(val progress: Int): DFUServiceStatus()
internal object Validating : DFUServiceStatus()
internal object Disconnecting : DFUServiceStatus()
internal object Disconnected : DFUServiceStatus()
internal object Completed : DFUServiceStatus()
internal object Aborted : DFUServiceStatus()
internal data class Error(val message: String?): DFUServiceStatus()
