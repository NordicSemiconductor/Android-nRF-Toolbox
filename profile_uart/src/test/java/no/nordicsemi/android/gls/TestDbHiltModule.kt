package no.nordicsemi.android.gls

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import no.nordicsemi.android.uart.DbHiltModule
import no.nordicsemi.android.uart.db.ConfigurationsDatabase
import no.nordicsemi.android.uart.db.MIGRATION_1_2
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DbHiltModule::class]
)
class TestDbHiltModule {
    @Provides
    @Singleton
    internal fun provideDB(@ApplicationContext context: Context): ConfigurationsDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            ConfigurationsDatabase::class.java
        ).addMigrations(MIGRATION_1_2).build()
    }
}