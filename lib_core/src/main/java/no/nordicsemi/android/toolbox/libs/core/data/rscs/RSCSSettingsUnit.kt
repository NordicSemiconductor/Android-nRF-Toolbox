package no.nordicsemi.android.toolbox.libs.core.data.rscs

enum class RSCSSettingsUnit {
    UNIT_CM,
    UNIT_M,
    UNIT_KM,
    UNIT_MPH, ;

    override fun toString(): String {
        return when (this) {
            UNIT_KM -> " kilometer [km/h]"
            UNIT_M -> " meter [m/s]"
            UNIT_MPH -> "miles [mph]"
            UNIT_CM -> "centimeter [cm/s]"
        }
    }

}
