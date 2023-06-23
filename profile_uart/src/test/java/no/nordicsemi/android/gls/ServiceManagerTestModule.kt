package no.nordicsemi.android.gls

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.service.DEVICE_DATA
import no.nordicsemi.android.service.ServiceManager
import no.nordicsemi.android.service.ServiceManagerHiltModule
import no.nordicsemi.android.uart.repository.UARTService
import org.robolectric.Robolectric
import org.robolectric.android.controller.ServiceController
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ServiceManagerHiltModule::class]
)
class ServiceManagerTestModule {

    private val componentName = ComponentName("org.robolectric", UARTService::class.java.name)

    @Provides
    internal fun provideDevice(): MockServerDevice {
        return MockServerDevice(
            name = "GLS Server",
            address = "55:44:33:22:11"
        )
    }

    @Provides
    internal fun provideServiceController(
        @ApplicationContext context: Context,
        device: MockServerDevice
    ): ServiceController<UARTService> {
        return Robolectric.buildService(UARTService::class.java, Intent(context, UARTService::class.java).apply {
            putExtra(DEVICE_DATA, device)
        })
    }

    @Provides
    @Singleton
    internal fun provideServiceManager(controller: ServiceController<UARTService>): ServiceManager {
        return object : ServiceManager {
            override fun <T> startService(service: Class<T>, device: ServerDevice) {
                controller.create().startCommand(3, 4).get()
            }
        }
    }
}
