package no.nordicsemi.android.csc.view

import no.nordicsemi.android.events.EMPTY

internal sealed class CSCViewState {

    fun ensureConnectedState(): CSCViewConnectedState {
        return (this as? CSCViewConnectedState)
            ?: throw IllegalStateException("Wrong state. Device not connected.")
    }

    fun ensureDisconnectedState(): CSCViewNotConnectedState {
        return (this as? CSCViewNotConnectedState)
            ?: throw IllegalStateException("Wrong state. Device should be connected.")
    }
}

//TODO("Change to navigation")
internal data class CSCViewNotConnectedState(
    val showScannerDialog: Boolean = false
) : CSCViewState()

internal data class CSCViewConnectedState(
    val showDialog: Boolean = false,
    val scanDevices: Boolean = false,
    val selectedSpeedUnit: SpeedUnit = SpeedUnit.M_S,
    val speed: Float = 0f,
    val cadence: Int = 0,
    val distance: Float = 0f,
    val totalDistance: Float = 0f,
    val gearRatio: Float = 0f,
    val batteryLevel: Int = 0,
    val wheelSize: String = String.EMPTY
) : CSCViewState() {

    fun displaySpeed(): String {
        return speed.toString()
    }

    fun displayCadence(): String {
        return cadence.toString()
    }

    fun displayDistance(): String {
        return distance.toString()
    }

    fun displayTotalDistance(): String {
        return totalDistance.toString()
    }

    fun displayBatteryLever(): String {
        return batteryLevel.toString()
    }
}
