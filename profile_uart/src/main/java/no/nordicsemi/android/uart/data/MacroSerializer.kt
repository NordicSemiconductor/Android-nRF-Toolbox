package no.nordicsemi.android.uart.data

import androidx.datastore.core.Serializer
import no.nordicsemi.android.Macro
import java.io.InputStream
import java.io.OutputStream

object MacroSerializer : Serializer<Macro> {
    override val defaultValue: UARTMacro = Macro.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UARTMacro {
        try {
            return Settings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: UARTMacro,
        output: OutputStream
    ) = t.writeTo(output)
}
