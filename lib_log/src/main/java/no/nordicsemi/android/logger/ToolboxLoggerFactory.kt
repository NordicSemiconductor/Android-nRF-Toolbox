package no.nordicsemi.android.logger

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

@AssistedFactory
interface ToolboxLoggerFactory {

    fun create(@Assisted("profile") profile: String, @Assisted("key") key: String): ToolboxLogger
}
