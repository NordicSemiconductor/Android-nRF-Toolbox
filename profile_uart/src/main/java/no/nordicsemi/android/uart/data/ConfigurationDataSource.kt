package no.nordicsemi.android.uart.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val FILE = "UART_CONFIGURATION"
private const val LAST_CONFIGURATION_KEY = "LAST_CONFIGURATION"

@Singleton
internal class ConfigurationDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = FILE)

    private val LAST_CONFIGURATION = stringPreferencesKey(LAST_CONFIGURATION_KEY)

    val lastConfigurationName = context.dataStore.data.map {
        it[LAST_CONFIGURATION]
    }

    suspend fun saveConfigurationName(name: String) {
        context.dataStore.edit {
            it[LAST_CONFIGURATION] = name
        }
    }
}
