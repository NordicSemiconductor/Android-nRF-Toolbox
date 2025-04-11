package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "configurations",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["device_id"],
            childColumns = ["device_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["device_id"], unique = true)]
)
data class ConfigurationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Int?,
    @ColumnInfo(name = "device_id") val deviceId: Int,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "name") val name: String,
)