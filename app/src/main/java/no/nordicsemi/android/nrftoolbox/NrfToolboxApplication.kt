package no.nordicsemi.android.nrftoolbox

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import no.nordicsemi.ui.scanner.scannerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

@HiltAndroidApp
class NrfToolboxApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NrfToolboxApplication)
            modules(scannerModule)
        }
    }
}
