package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class ConfigurationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val _id: Int?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "xml") val xml: String,
    @ColumnInfo(name = "deleted", defaultValue = "0") val deleted: Int
)