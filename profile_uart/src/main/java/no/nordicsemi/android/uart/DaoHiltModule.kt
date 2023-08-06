package no.nordicsemi.android.uart

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.uart.db.ConfigurationsDao
import no.nordicsemi.android.uart.db.ConfigurationsDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DaoHiltModule {

    @Provides
    @Singleton
    internal fun provideDao(db: ConfigurationsDatabase): ConfigurationsDao {
        return db.dao()
    }
}
