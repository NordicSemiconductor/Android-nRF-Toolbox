package no.nordicsemi.android.service

enum class BleManagerStatus {
    CONNECTING, OK, LINK_LOSS, DISCONNECTED, MISSING_SERVICE
}

sealed class BleManagerResult <T> {

    fun isRunning(): Boolean {
        return this is SuccessResult
    }

    fun hasBeenDisconnected(): Boolean {
        return this is LinkLossResult || this is DisconnectedResult || this is MissingServiceResult
    }

    fun hasBeenDisconnectedWithoutLinkLoss(): Boolean {
        return this is DisconnectedResult || this is MissingServiceResult
    }
}

class ConnectingResult<T> : BleManagerResult<T>()
data class SuccessResult<T>(val data: T) : BleManagerResult<T>()

class LinkLossResult<T>(val data: T) : BleManagerResult<T>()
class DisconnectedResult<T> : BleManagerResult<T>()
class UnknownErrorResult<T> : BleManagerResult<T>()
class MissingServiceResult<T> : BleManagerResult<T>()
