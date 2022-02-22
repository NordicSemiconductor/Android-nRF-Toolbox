package no.nordicsemi.android.uart.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.uart.db.*
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.strategy.Strategy
import org.simpleframework.xml.strategy.VisitorStrategy
import org.simpleframework.xml.stream.Format
import org.simpleframework.xml.stream.HyphenStyle
import java.io.StringWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UARTPersistentDataSource @Inject constructor(
    private val configurationsDao: ConfigurationsDao,
) {

    fun getConfigurations(): Flow<List<UARTConfiguration>> = configurationsDao.load().map {
        it.map {
            val xml: String = it.xml
            val format = Format(HyphenStyle())
            val serializer: Serializer = Persister(format)
            val configuration = serializer.read(XmlConfiguration::class.java, xml)

            UARTConfiguration(configuration.name ?: "Unknown", createMacro(configuration.commands))
        }
    }

    private fun createMacro(macros: Array<XmlCommand?>): List<UARTMacro?> {
        return macros.map {
            if (it == null) {
                null
            } else {
                val icon = MacroIcon.create(it.iconIndex)
                it.command?.let { c -> UARTMacro(icon, c, it.eol) }
            }
        }
    }

    suspend fun saveConfiguration(configuration: UARTConfiguration) {
        val format = Format(HyphenStyle())
        val strategy: Strategy = VisitorStrategy(CommentVisitor())
        val serializer: Serializer = Persister(strategy, format)
        val writer = StringWriter()
        serializer.write(configuration, writer)
        val xml = writer.toString()

        configurationsDao.insert(Configuration(0, configuration.name, xml, 0))
    }

    suspend fun deleteConfiguration(configuration: UARTConfiguration) {
        configurationsDao.delete(configuration.name)
    }
}
