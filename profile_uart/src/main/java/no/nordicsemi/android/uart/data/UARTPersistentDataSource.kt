package no.nordicsemi.android.uart.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.Macro
import no.nordicsemi.android.MacroSettings
import javax.inject.Inject
import javax.inject.Singleton

private const val MACRO_FILE = "macro.proto"

@Singleton
internal class UARTPersistentDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val Context.dataStore: DataStore<MacroSettings> by dataStore(fileName = MACRO_FILE, MacroSerializer)

    val macros = context.dataStore.data.map {
        it.macrosList.map {
            UARTMacro(it.name, it.newLineType.toNewLineChar())
        }
    }

    suspend fun saveMacros(uartMacros: List<UARTMacro>) {
        context.dataStore.updateData { settings ->
            val macros = uartMacros.map { it.toMacro() }
            settings.toBuilder()
                .clearMacros()
                .addAllMacros(macros)
                .build()
        }
    }

    suspend fun addNewMacro(uartMacro: UARTMacro) {
        context.dataStore.updateData { settings ->
            settings.toBuilder()
                .addMacros(uartMacro.toMacro())
                .build()
        }
    }

    suspend fun deleteMacro(uartMacro: UARTMacro) {
        context.dataStore.updateData { settings ->
            val i = settings.macrosList.map { it.name }.indexOf(uartMacro.command)
            settings.toBuilder()
                .removeMacros(i)
                .build()
        }
    }

    private fun UARTMacro.toMacro(): Macro {
        return Macro.newBuilder()
            .setName(command)
            .setNewLineType(newLineChar.toMacroNewLineType())
            .build()
    }

    private fun NewLineChar.toMacroNewLineType(): Macro.NewLineType {
        return when (this) {
            NewLineChar.LF -> Macro.NewLineType.LF
            NewLineChar.CR_LF -> Macro.NewLineType.LF_CR
            NewLineChar.CR -> Macro.NewLineType.CR
        }
    }

    private fun Macro.NewLineType.toNewLineChar(): NewLineChar {
        return when (this) {
            Macro.NewLineType.LF -> NewLineChar.LF
            Macro.NewLineType.LF_CR -> NewLineChar.CR_LF
            Macro.NewLineType.CR -> NewLineChar.CR
            Macro.NewLineType.UNRECOGNIZED -> throw IllegalArgumentException("Unrecognized NewLineChar.")
        }
    }
}
