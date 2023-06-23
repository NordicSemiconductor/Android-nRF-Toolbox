package no.nordicsemi.android.gls

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import no.nordicsemi.android.common.logger.BlekLoggerAndLauncher
import no.nordicsemi.android.ui.view.NordicLoggerFactory
import no.nordicsemi.android.ui.view.NordicLoggerFactoryHiltModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NordicLoggerFactoryHiltModule::class]
)
class NordicLoggerFactoryTestModule {

    @Provides
    fun createLogger(): NordicLoggerFactory {
        return object : NordicLoggerFactory {
            override fun createNordicLogger(
                context: Context,
                profile: String?,
                key: String,
                name: String?,
            ): BlekLoggerAndLauncher {
                return object : BlekLoggerAndLauncher {
                    override fun launch() {

                    }

                    override fun log(priority: Int, log: String) {
                        println(log)
                    }
                }
            }
        }
    }
}
