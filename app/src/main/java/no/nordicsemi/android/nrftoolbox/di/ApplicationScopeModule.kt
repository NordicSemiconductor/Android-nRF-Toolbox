package no.nordicsemi.android.nrftoolbox.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
class ApplicationScopeModule {

    @Provides
    fun applicationScope() = CoroutineScope(SupervisorJob())
}
