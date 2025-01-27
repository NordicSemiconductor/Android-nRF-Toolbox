package no.nordicsemi.android.lib.profile.prx

object AlertLevelInputParser {

    fun parse(alarmLevel: AlarmLevel): Byte {
        return alarmLevel.value
    }
}