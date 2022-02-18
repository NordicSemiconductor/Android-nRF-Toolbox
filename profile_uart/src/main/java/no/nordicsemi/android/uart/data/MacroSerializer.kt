package no.nordicsemi.android.uart.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import no.nordicsemi.android.MacroSettings
import java.io.InputStream
import java.io.OutputStream

object MacroSerializer : Serializer<MacroSettings> {
    override val defaultValue: MacroSettings = MacroSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): MacroSettings {
        try {
            return MacroSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: MacroSettings,
        output: OutputStream
    ) = t.writeTo(output)
}
