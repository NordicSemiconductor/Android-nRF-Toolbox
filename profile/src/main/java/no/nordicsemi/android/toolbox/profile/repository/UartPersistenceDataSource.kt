package no.nordicsemi.android.toolbox.profile.repository

import no.nordicsemi.android.toolbox.lib.storage.ConfigurationEntity
import no.nordicsemi.android.toolbox.lib.storage.ConfigurationsDao
import no.nordicsemi.android.toolbox.lib.storage.DeviceDao
import no.nordicsemi.android.toolbox.lib.storage.DeviceEntity
import javax.inject.Inject
import javax.inject.Singleton

data class UartConfigurationForDevice(
    val name: String,
)

@Singleton
internal class UARTPersistentDataSource @Inject constructor(
    private val deviceDao: DeviceDao,
    private val configurationDao: ConfigurationsDao,
) {
    suspend fun getDevice(deviceId: String): Int? {
        return deviceDao.getDeviceById(deviceId)?.id
    }

    suspend fun insertDevice(device: String): Long {
        return deviceDao.insertDevice(DeviceEntity(null, device))
    }

    suspend fun getAllConfigurations(address: String): List<UartConfigurationForDevice> {
        // Check if the device exists
        return configurationDao.getAllConfigurations(deviceDao.getDeviceById(address)?.id!!)
            .map { deviceWithConfigurations ->
                deviceWithConfigurations.configurations.map { configuration ->
                    UartConfigurationForDevice(
                        name = configuration.name
                    )
                }
            }.flatten()

    }

    suspend fun insertConfiguration(deviceId: String, configurationName: String): Long {
        val device = deviceDao.getDeviceById(deviceId)
        val deviceEntityId = if (device == null) {
            // Insert device if it doesn't exist
            deviceDao.insertDevice(DeviceEntity(null, deviceId)).toInt()
        } else {
            device.id
        }
        val configuration = ConfigurationEntity(
            id = null,
            deviceId = deviceEntityId!!,
            name = configurationName
        )
        return configurationDao.insertConfiguration(configuration)

    }
}