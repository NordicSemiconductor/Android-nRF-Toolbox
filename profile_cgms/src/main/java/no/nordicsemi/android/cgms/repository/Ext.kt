package no.nordicsemi.android.cgms.repository

import android.util.SparseArray
import androidx.core.util.keyIterator
import no.nordicsemi.android.cgms.data.CGMRecord

internal fun SparseArray<CGMRecord>.toList(): List<CGMRecord> {
    val list = mutableListOf<CGMRecord>()
    this.keyIterator().forEach {
        list.add(get(it))
    }
    return list.sortedBy { it.sequenceNumber }.toList()
}
