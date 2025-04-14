package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConfigurationEntity::class],
    version = 1,
    exportSchema = false
)
internal abstract class ConfigurationDatabase : RoomDatabase() {
    abstract fun configurationDao(): ConfigurationsDao
}