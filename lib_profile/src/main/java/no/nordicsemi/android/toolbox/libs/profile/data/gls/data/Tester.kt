package no.nordicsemi.android.toolbox.libs.profile.data.gls.data

enum class Tester(internal val value: Int) {
    RESERVED(0),
    SELF(1),
    HEALTH_CARE_PROFESSIONAL(2),
    LAB_TEST(3),
    NOT_AVAILABLE(15);

    companion object {
        fun create(value: Int): Tester {
            return entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Cannot create Tester for value $value")
        }
    }
}