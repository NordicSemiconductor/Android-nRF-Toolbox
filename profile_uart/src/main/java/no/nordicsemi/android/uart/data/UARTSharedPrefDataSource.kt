package no.nordicsemi.android.uart.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.prefs.Preferences
import javax.inject.Inject

private const val MACRO_FILE = "macro.proto"

internal class UARTSharedPrefDataSource @Inject constructor(
    @ApplicationContext
    context: Context
) {
//    val Context.dataStore: DataStore<Preferences> by dataStore(fileName = MACRO_FILE, MacroSerializer)
//
//    private val sp = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
//
////    val macros:
//
//    init {
//
//        sp.edit()
//    }
}
