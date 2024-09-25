package no.nordicsemi.android.toolbox.libs.profile.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.toolbox.libs.profile.repository.ServiceManager
import no.nordicsemi.android.toolbox.libs.profile.repository.ServiceManagerImp

@Module
@InstallIn(SingletonComponent::class)
object ServiceManagerImpModule {

    @Provides
    fun provideServiceManager(
        @ApplicationContext context: Context
    ): ServiceManager = ServiceManagerImp(context)

}
