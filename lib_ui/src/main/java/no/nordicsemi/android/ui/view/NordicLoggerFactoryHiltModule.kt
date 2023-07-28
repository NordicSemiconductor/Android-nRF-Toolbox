package no.nordicsemi.android.ui.view

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.common.logger.BleLoggerAndLauncher
import no.nordicsemi.android.common.logger.DefaultBleLogger

@Module
@InstallIn(SingletonComponent::class)
class NordicLoggerFactoryHiltModule {

    @Provides
    fun createLogger(): NordicLoggerFactory {
        return object : NordicLoggerFactory {
            override fun createNordicLogger(
                context: Context,
                profile: String?,
                key: String,
                name: String?,
            ): BleLoggerAndLauncher {
                return DefaultBleLogger.create(context, profile, key, name)
            }
        }
    }
}
