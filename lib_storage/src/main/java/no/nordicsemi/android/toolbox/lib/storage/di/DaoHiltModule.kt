package no.nordicsemi.android.toolbox.lib.storage.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.toolbox.lib.storage.ConfigurationDatabase
import no.nordicsemi.android.toolbox.lib.storage.ConfigurationsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DaoHiltModule {

    @Provides
    @Singleton
    internal fun provideDeviceDao(db: ConfigurationDatabase): ConfigurationsDao {
        return db.configurationDao()
    }
}