package no.nordicsemi.android.uart

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.uart.db.ConfigurationsDao
import no.nordicsemi.android.uart.db.ConfigurationsDatabase
import no.nordicsemi.android.uart.db.MIGRATION_1_2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HiltModule {

    @Provides
    @Singleton
    internal fun provideDB(@ApplicationContext context: Context): ConfigurationsDatabase {
        return Room.databaseBuilder(
            context,
            ConfigurationsDatabase::class.java, "toolbox_uart.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    internal fun provideDao(db: ConfigurationsDatabase): ConfigurationsDao {
        return db.dao()
    }

}
