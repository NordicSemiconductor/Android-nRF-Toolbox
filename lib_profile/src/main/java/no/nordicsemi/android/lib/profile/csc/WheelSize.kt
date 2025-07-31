package no.nordicsemi.android.lib.profile.csc

data class WheelSize(
    val value: Int,
    val name: String
)

object WheelSizes {
    val data = listOf(
        WheelSize(2340, "60-622"),
        WheelSize(2284, "50-622"),
        WheelSize(2268, "47-622"),
        WheelSize(2224, "44-622"),
        WheelSize(2265, "40-635"),
        WheelSize(2224, "40-622"),
        WheelSize(2180, "38-622"),
        WheelSize(2205, "37-622"),
        WheelSize(2168, "35-622"),
        WheelSize(2199, "32-630"),
        WheelSize(2174, "32-622"),
        WheelSize(2155, "32-622"),
        WheelSize(2149, "28-622"),
        WheelSize(2146, "60-559"),
        WheelSize(2136, "28-622"),
        WheelSize(2146, "25-622"),
        WheelSize(2105, "25-622"),
        WheelSize(2133, "23-622"),
        WheelSize(2114, "20-622"),
        WheelSize(2102, "18-622"),
        WheelSize(2169, "35-630"),
        WheelSize(2161, "32-630"),
        WheelSize(2155, "28-630"),
        WheelSize(2133, "57-559"),
        WheelSize(2114, "54-559"),
        WheelSize(2105, "37-590"),
        WheelSize(2097, "23-622"),
        WheelSize(2089, "50-559"),
        WheelSize(2086, "20-622"),
        WheelSize(2114, "54-559"),
        WheelSize(2070, "47-559"),
        WheelSize(2068, "35-590"),
        WheelSize(2105, "37-590"),
        WheelSize(2055, "47-559"),
        WheelSize(2089, "50-559"),
        WheelSize(2051, "44-559"),
        WheelSize(2026, "40-559"),
        WheelSize(1973, "23-571"),
        WheelSize(1954, "20-571"),
        WheelSize(1953, "32-559"),
        WheelSize(1952, "25-571"),
        WheelSize(1948, "34-540"),
        WheelSize(1910, "50-507"),
        WheelSize(1907, "47-507"),
        WheelSize(1618, "28-451"),
        WheelSize(1593, "50-406"),
        WheelSize(1590, "47-406"),
        WheelSize(1325, "28-369"),
        WheelSize(1282, "35-349"),
        WheelSize(1272, "47-305")
    )
    val default = data.first()

    fun getWheelSizeByName(name: String) = data.find { it.name == name } ?: default
}
