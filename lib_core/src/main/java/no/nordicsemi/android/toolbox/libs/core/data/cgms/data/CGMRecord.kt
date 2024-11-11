package no.nordicsemi.android.toolbox.libs.core.data.cgms.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CGMRecord(
    val glucoseConcentration: Float,
    val trend: Float?,
    val quality: Float?,
    val status: CGMStatus?,
    val timeOffset: Int,
    val crcPresent: Boolean
) : Parcelable
