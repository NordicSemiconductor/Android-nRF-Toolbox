package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConfigurationEntity::class],
    version = 3
)
internal abstract class ConfigurationDatabase : RoomDatabase() {
    abstract fun configurationDao(): ConfigurationsDao
}