package no.nordicsemi.android.toolbox.profile.view.hrs

import no.nordicsemi.android.toolbox.profile.data.HRSServiceData

fun HRSServiceData.displayHeartRate(): String {
    return "${this.heartRate} BPM"
}

fun HRSServiceData.displayBodySensorLocation(): String {
    return when (bodySensorLocation) {
        0 -> "Other"
        1 -> "Chest"
        2 -> "Wrist"
        3 -> "Finger"
        4 -> "Hand"
        5 -> "Ear Lobe"
        6 -> "Foot"
        else -> "Unknown"
    }
}