package no.nordicsemi.android.lib.profile.rscs

enum class RSCSSettingsUnit {
    UNIT_CM,
    UNIT_M,
    UNIT_KM,
    UNIT_MPH, ;

    override fun toString(): String {
        return when (this) {
            UNIT_KM -> "Kilometer [km/h]"
            UNIT_M -> "Meter [m/s]"
            UNIT_MPH -> "Miles [mph]"
            UNIT_CM -> "Centimeter [cm/s]"
        }
    }

}
