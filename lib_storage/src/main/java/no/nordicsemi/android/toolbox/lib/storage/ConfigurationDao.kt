package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ConfigurationsDao {
    @Transaction
    @Query("SELECT * FROM configurations WHERE device_id = :deviceId")
    suspend fun getAllConfigurations(deviceId: Int): DeviceWithConfigurations

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(configuration: ConfigurationEntity): Long

}
