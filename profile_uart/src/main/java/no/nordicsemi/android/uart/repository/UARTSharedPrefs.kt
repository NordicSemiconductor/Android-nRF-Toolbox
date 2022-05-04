package no.nordicsemi.android.uart.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val FILE_NAME = "uart-sp"
private const val SHOW_TUTORIAL_FIELD = "show-tutorial"

class UARTSharedPrefs @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var showTutorial: Boolean
        get() = sp.getBoolean(SHOW_TUTORIAL_FIELD, true)
        set(value) = sp.edit().putBoolean(SHOW_TUTORIAL_FIELD, value).apply()
}
