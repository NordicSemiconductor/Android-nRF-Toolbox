package no.nordicsemi.android.toolbox.libs.profile.data.prx

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

object AlertLevelInputParser {

    fun parse(alarmLevel: AlarmLevel): DataByteArray {
        return DataByteArray.from(alarmLevel.value)
    }
}