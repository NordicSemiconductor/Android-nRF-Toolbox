package no.nordicsemi.android.toolbox.profile.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.toolbox.lib.storage.Configuration
import no.nordicsemi.android.toolbox.lib.storage.ConfigurationsDao
import no.nordicsemi.android.toolbox.profile.data.uart.MacroIcon
import no.nordicsemi.android.toolbox.profile.data.uart.UARTConfiguration
import no.nordicsemi.android.toolbox.profile.data.uart.UARTMacro
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

    fun getConfigurations(deviceId: String): Flow<List<UARTConfiguration>> {
        // if deviceId is same as the one in the database, return the configurations
        return configurationsDao.load().map { configurations ->
            configurations.mapNotNull { it.toDomain() }
        }
    }
    private fun Configuration.toDomain(): UARTConfiguration? {
        return try {
            val xml: String = xml
            val format = Format(HyphenStyle())
            val serializer: Serializer = Persister(format)
            val configuration = serializer.read(XmlConfiguration::class.java, xml)

            UARTConfiguration(
                id,
                configuration.name ?: "Unknown",
                createMacro(configuration.commands)
            )
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
    private fun createMacro(macros: Array<XmlMacro?>): List<UARTMacro?> {
        return macros.map {
            if (it == null) {
                null
            } else {
                val icon = MacroIcon.create(it.iconIndex)
                UARTMacro(icon, it.command, it.eol)
            }
        }
    }

    suspend fun saveConfiguration(configuration: UARTConfiguration) {
        val format = Format(HyphenStyle())
        val strategy: Strategy = VisitorStrategy(CommentVisitor())
        val serializer: Serializer = Persister(strategy, format)
        val writer = StringWriter()
        serializer.write(configuration.toXmlConfiguration(), writer)
        val xml = writer.toString()

        configurationsDao.insert(Configuration(configuration.id, configuration.name, xml, 0))
    }

    suspend fun deleteConfiguration(configuration: UARTConfiguration) {
        configurationsDao.delete(configuration.name)
    }

    private fun UARTConfiguration.toXmlConfiguration(): XmlConfiguration {
        val xmlConfiguration = XmlConfiguration()
        xmlConfiguration.name = name
        val commands = macros.map { macro ->
            macro?.let {
                XmlMacro().apply {
                    setEol(it.newLineChar.ordinal)
                    command = it.command
                    iconIndex = it.icon.index
                }
            }
        }.toTypedArray()
        xmlConfiguration.commands = commands
        return xmlConfiguration
    }
}