package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Configuration::class], version = 3)
internal abstract class ConfigurationsDatabase : RoomDatabase() {
    abstract fun dao(): ConfigurationsDao
}