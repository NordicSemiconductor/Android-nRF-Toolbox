package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceWithConfigurations(
    @Embedded val device: DeviceEntity,
    @Relation(
        parentColumn = "device_id",
        entityColumn = "device_id",
    )
    val configurations: List<ConfigurationEntity>
)
