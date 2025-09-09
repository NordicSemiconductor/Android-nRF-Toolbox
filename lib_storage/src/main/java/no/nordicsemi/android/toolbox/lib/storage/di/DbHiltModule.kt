package no.nordicsemi.android.toolbox.lib.storage.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.toolbox.lib.storage.ConfigurationDatabase
import no.nordicsemi.android.toolbox.lib.storage.MIGRATION_1_2
import no.nordicsemi.android.toolbox.lib.storage.MIGRATION_2_3
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DbHiltModule {

    @Provides
    @Singleton
    internal fun provideDeviceDB(@ApplicationContext context: Context): ConfigurationDatabase {
        return Room.databaseBuilder(
            context,
            ConfigurationDatabase::class.java,
            "toolbox_uart.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }
}