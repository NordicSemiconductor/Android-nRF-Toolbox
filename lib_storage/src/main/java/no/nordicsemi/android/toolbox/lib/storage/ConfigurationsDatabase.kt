package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Configuration::class],
    version = 2
)
abstract class ConfigurationsDatabase : RoomDatabase() {
    abstract fun configurationDao(): ConfigurationsDao
}