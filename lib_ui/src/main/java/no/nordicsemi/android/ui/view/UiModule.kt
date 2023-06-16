package no.nordicsemi.android.ui.view

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.common.logger.NordicBlekLogger
import no.nordicsemi.android.common.logger.BlekLogger

@Module
@InstallIn(SingletonComponent::class)
class UiModule {

    @Provides
    fun createLogger(): NordicLoggerFactory {
        return object : NordicLoggerFactory {
            override fun createNordicLogger(
                context: Context,
                profile: String?,
                key: String,
                name: String?,
            ): BlekLogger {
                return NordicBlekLogger.create(context, profile, key, name)
            }
        }
    }
}
