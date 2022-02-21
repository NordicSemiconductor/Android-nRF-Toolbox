package no.nordicsemi.android.uart.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Configuration::class], version = 2)
internal abstract class ConfigurationsDatabase : RoomDatabase() {
    abstract fun dao(): ConfigurationsDao
}
