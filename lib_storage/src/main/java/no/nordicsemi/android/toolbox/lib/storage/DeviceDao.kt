package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeviceDao {
    // Insert device
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    // Get device by ID
    @Query("SELECT * FROM devices WHERE address = :deviceId")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?
}