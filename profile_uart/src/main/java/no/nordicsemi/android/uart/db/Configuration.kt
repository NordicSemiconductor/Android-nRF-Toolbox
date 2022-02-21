package no.nordicsemi.android.uart.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
internal data class Configuration(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "xml") val xml: String,
    @ColumnInfo(name = "deleted") val deleted: Int
)
