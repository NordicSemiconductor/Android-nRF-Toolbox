package no.nordicsemi.android.scanner

import android.bluetooth.BluetoothAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object HiltModule {

    @Provides
    fun createNordicBleScanner(): BluetoothAdapter? {
        return BluetoothAdapter.getDefaultAdapter()
    }
}
