package no.nordicsemi.android.permission

import android.bluetooth.BluetoothAdapter
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.permission.tools.PermissionHelper
import no.nordicsemi.android.service.SelectedBluetoothDeviceHolder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object HiltModule {

    @Provides
    fun createNordicBleScanner(): BluetoothAdapter? {
        return BluetoothAdapter.getDefaultAdapter()
    }

    @Singleton
    @Provides
    fun createSelectedBluetoothDeviceHolder(): SelectedBluetoothDeviceHolder {
        return SelectedBluetoothDeviceHolder()
    }

    @Singleton
    @Provides
    fun createPermissionHelper(@ApplicationContext context: Context): PermissionHelper {
        return PermissionHelper(context)
    }
}
