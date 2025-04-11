package no.nordicsemi.android.toolbox.lib.storage.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.toolbox.lib.storage.ConfigurationsDao
import no.nordicsemi.android.toolbox.lib.storage.DeviceDao
import no.nordicsemi.android.toolbox.lib.storage.DeviceDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DaoHiltModule {

    @Provides
    @Singleton
    internal fun provideDeviceDao(db: DeviceDatabase): DeviceDao {
        return db.deviceDao()
    }

    @Provides
    @Singleton
    internal fun provideConfigurationDao(db: DeviceDatabase): ConfigurationsDao {
        return db.configurationDao()
    }
}