package no.nordicsemi.android.service.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.mapNotNull
import no.nordicsemi.android.toolbox.lib.utils.Profile
import no.nordicsemi.kotlin.ble.client.RemoteService
import timber.log.Timber
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

private val RAS_FEATURES = UUID.fromString("00002C14-0000-1000-8000-00805F9B34FB")
private val REALTIME_RANGING_DATA = UUID.fromString("00002C15-0000-1000-8000-00805F9B34FB")
private val RAS_ON_DEMAND_RD = UUID.fromString("00002C16-0000-1000-8000-00805F9B34FB")
private val RAS_CP = UUID.fromString("00002C17-0000-1000-8000-00805F9B34FB")
private val RAS_RD_READY = UUID.fromString("00002C18-0000-1000-8000-00805F9B34FB")
private val RAS_RD_OVERWRITTEN = UUID.fromString("00002C19-0000-1000-8000-00805F9B34FB")

internal class ChannelSoundingManager : ServiceManager {
    override val profile: Profile
        get() = Profile.CHANNEL_SOUNDING

    @OptIn(ExperimentalUuidApi::class, ExperimentalStdlibApi::class)
    override suspend fun observeServiceInteractions(
        deviceId: String,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        remoteService.characteristics.firstOrNull {
            it.uuid == RAS_FEATURES.toKotlinUuid()
        }
            ?.read()
            ?.let {
                val rasFeature = RasFeatureParser.parse(it)
                Timber.tag("AAA").d("Ranging Feature: $rasFeature")
            }

        remoteService.characteristics.firstOrNull {
            it.uuid == REALTIME_RANGING_DATA.toKotlinUuid()
        }
            ?.subscribe()
            ?.mapNotNull {
                Timber.tag("AAA").d("Ranging data: ${it.toHexString()}")
            }
        }

}

data class RasFeature(
    val realTimeRangingData: Boolean,
    val retrieveLostSegments: Boolean,
    val abortOperation: Boolean,
    val filterRangingData: Boolean,
)


object RasFeatureParser {

    fun parse(data: ByteArray): RasFeature {
        require(data.size >= 4) { "RAS Features characteristic must be at least 4 bytes." }

        val featureBits = (data[0].toInt() and 0xFF) or
                ((data[1].toInt() and 0xFF) shl 8) or
                ((data[2].toInt() and 0xFF) shl 16) or
                ((data[3].toInt() and 0xFF) shl 24)

        return RasFeature(
            realTimeRangingData = featureBits and (1 shl 0) != 0,
            retrieveLostSegments = featureBits and (1 shl 1) != 0,
            abortOperation = featureBits and (1 shl 2) != 0,
            filterRangingData = featureBits and (1 shl 3) != 0
        )
    }

}
