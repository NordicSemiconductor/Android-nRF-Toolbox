package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "device_id") val id: Int?,
    @ColumnInfo(name = "address") val address: String,
)