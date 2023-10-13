package no.nordicsemi.android.nrftoolbox

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.common.logger.BleLogger
import no.nordicsemi.android.common.logger.DefaultConsoleLogger

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal class AppModule {

    @Provides
    fun provideNordicLogger(
        @ApplicationContext context: Context
    ): BleLogger = DefaultConsoleLogger(context)
}
