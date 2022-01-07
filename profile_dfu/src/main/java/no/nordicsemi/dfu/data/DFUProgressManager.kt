package no.nordicsemi.dfu.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import javax.inject.Inject

@ViewModelScoped
internal class DFUProgressManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) : DfuProgressListenerAdapter() {

    val status = MutableStateFlow<DFUServiceStatus>(Idle)

    override fun onDeviceConnecting(deviceAddress: String) {
        status.value = Connecting
    }

    override fun onDeviceConnected(deviceAddress: String) {
        status.value = Connected
    }

    override fun onDfuProcessStarting(deviceAddress: String) {
        status.value = Starting
    }

    override fun onDfuProcessStarted(deviceAddress: String) {
        status.value = Started
    }

    override fun onEnablingDfuMode(deviceAddress: String) {
        status.value = EnablingDfu
    }

    override fun onProgressChanged(
        deviceAddress: String,
        percent: Int,
        speed: Float,
        avgSpeed: Float,
        currentPart: Int,
        partsTotal: Int
    ) {
        status.value = ProgressUpdate(percent)
    }

    override fun onFirmwareValidating(deviceAddress: String) {
        status.value = Validating
    }

    override fun onDeviceDisconnecting(deviceAddress: String?) {
        status.value = Disconnecting
    }

    override fun onDeviceDisconnected(deviceAddress: String) {
        status.value = Disconnected
    }

    override fun onDfuCompleted(deviceAddress: String) {
        status.value = Completed
    }

    override fun onDfuAborted(deviceAddress: String) {
        status.value = Aborted
    }

    override fun onError(
        deviceAddress: String,
        error: Int,
        errorType: Int,
        message: String?
    ) {
        status.value = Error(message)
    }

    fun registerListener() {
        DfuServiceListenerHelper.registerProgressListener(context, this)
    }

    fun unregisterListener() {
        DfuServiceListenerHelper.unregisterProgressListener(context, this)
    }
}
