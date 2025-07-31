package no.nordicsemi.android.lib.profile.cgms.data

data class CGMRecord(
    val glucoseConcentration: Float,
    val trend: Float?,
    val quality: Float?,
    val status: CGMStatus?,
    val timeOffset: Int,
    val crcPresent: Boolean
)
