package no.nordicsemi.android.csc.data

import no.nordicsemi.android.csc.view.CSCSettings

data class WheelSize(
    val value: Int = CSCSettings.DefaultWheelSize.VALUE,
    val name: String = CSCSettings.DefaultWheelSize.NAME
)
