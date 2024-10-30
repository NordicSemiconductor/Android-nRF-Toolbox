package no.nordicsemi.android.toolbox.libs.core.data.prx

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

object AlertLevelInputParser {

    fun parse(alarmLevel: AlarmLevel): DataByteArray {
        return DataByteArray.from(alarmLevel.value)
    }
}