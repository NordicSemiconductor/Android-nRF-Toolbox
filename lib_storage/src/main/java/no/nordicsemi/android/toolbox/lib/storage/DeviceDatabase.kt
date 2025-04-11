package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DeviceEntity::class, ConfigurationEntity::class],
    version = 1,
    exportSchema = false
)
internal abstract class DeviceDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun configurationDao(): ConfigurationsDao
}