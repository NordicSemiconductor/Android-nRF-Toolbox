package no.nordicsemi.android.cgms.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CGMRecord(
    var sequenceNumber: Int,
    var glucoseConcentration: Float,
    var timestamp: Long
) : Parcelable
