package no.nordicsemi.android.toolbox.profile.repository

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
internal class UartConfigurationDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = FILE)

    val lastConfigurationName = context.dataStore.data.map {
        it[LAST_CONFIGURATION]
    }

    suspend fun saveConfigurationName(name: String) {
        context.dataStore.edit {
            it[LAST_CONFIGURATION] = name
        }
    }

    companion object {
        private val LAST_CONFIGURATION = stringPreferencesKey(LAST_CONFIGURATION_KEY)
    }
}