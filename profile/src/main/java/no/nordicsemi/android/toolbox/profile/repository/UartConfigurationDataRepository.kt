package no.nordicsemi.android.toolbox.profile.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UartConfigurationDataRepository @Inject constructor(
    private val configurationDataSource: UartConfigurationDataSource
) {
    val lastConfigurationName= configurationDataSource.lastConfigurationName
    suspend fun saveConfigurationName(deviceId: String, name: String) {
        configurationDataSource.saveConfigurationName(name)
    }

    suspend fun deleteConfiguration(deviceId: String, name: String) {
        configurationDataSource.deleteConfiguration(name)
    }

}