package no.nordicsemi.android.toolbox.libs.core.data.prx

object AlertLevelInputParser {

    fun parse(alarmLevel: AlarmLevel): Byte {
        return alarmLevel.value
    }
}