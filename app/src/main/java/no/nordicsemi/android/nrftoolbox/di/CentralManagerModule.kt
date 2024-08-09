package no.nordicsemi.android.nrftoolbox.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.native

@Module
@InstallIn(ViewModelComponent::class)
object CentralManagerModule {

    @Provides
    fun provideCentralManager(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): CentralManager {
        return CentralManager.Factory.native(context, scope)
    }
}