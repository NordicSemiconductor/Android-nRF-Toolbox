package no.nordicsemi.android.service

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ServiceManagerHiltModule {

    @Provides
    fun createServiceManager(
        @ApplicationContext
        context: Context,
    ): ServiceManager {
        return ServiceManagerImpl(context)
    }
}
