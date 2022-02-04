package no.nordicsemi.android.service

enum class BleManagerStatus {
    CONNECTING, OK, LINK_LOSS, DISCONNECTED, MISSING_SERVICE
}

sealed class BleManagerResult <T>

class ConnectingResult<T> : BleManagerResult<T>()
class ReadyResult<T> : BleManagerResult<T>()

data class SuccessResult<T>(val data: T) : BleManagerResult<T>()

class LinkLossResult<T> : BleManagerResult<T>()
class DisconnectedResult<T> : BleManagerResult<T>()
class MissingServiceResult<T> : BleManagerResult<T>()

