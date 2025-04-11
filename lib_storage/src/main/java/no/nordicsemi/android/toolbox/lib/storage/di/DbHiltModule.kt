package no.nordicsemi.android.toolbox.lib.storage.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.toolbox.lib.storage.DeviceDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DbHiltModule {

    @Provides
    @Singleton
    internal fun provideDeviceDB(@ApplicationContext context: Context): DeviceDatabase {
        return Room.databaseBuilder(
            context,
            DeviceDatabase::class.java,
            "toolbox_uart_device.db"
        ).build()
    }
}