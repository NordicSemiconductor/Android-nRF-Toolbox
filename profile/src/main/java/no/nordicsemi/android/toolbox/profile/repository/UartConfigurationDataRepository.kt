package no.nordicsemi.android.toolbox.profile.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UartConfigurationDataRepository @Inject constructor(
    private val configurationDataSource: UartConfigurationDataSource
) {
    val lastConfigurationName= configurationDataSource.lastConfigurationName
    suspend fun saveConfigurationName(name: String) {
        configurationDataSource.saveConfigurationName(name)
    }

}