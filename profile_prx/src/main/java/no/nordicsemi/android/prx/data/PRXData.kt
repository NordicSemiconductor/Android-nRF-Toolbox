package no.nordicsemi.android.prx.data

internal data class PRXData(
    private val batteryLevel: Int = 0,
    private val localAlarmLevel: AlarmLevel = AlarmLevel.NONE,
    private val remoteAlarmLevel: Boolean = false
)

internal enum class AlarmLevel(val value: Int) {
    NONE(0x00),
    MEDIUM(0x01),
    HIGH(0x02);

    companion object {
        fun create(value: Int): AlarmLevel {
            return AlarmLevel.values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot find AlarmLevel for provided value: $value")
        }
    }
}
