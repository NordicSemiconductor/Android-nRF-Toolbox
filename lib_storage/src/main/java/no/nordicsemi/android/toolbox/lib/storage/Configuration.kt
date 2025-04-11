package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class ConfigurationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Int?,
    @ColumnInfo(name = "device_id") val deviceId: Int,
    @ColumnInfo(name = "name") val name: String,
)