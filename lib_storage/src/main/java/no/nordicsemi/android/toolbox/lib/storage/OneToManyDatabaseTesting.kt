package no.nordicsemi.android.toolbox.lib.storage

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Int? = null,
    @ColumnInfo(name = "device_id") val deviceId: String
)

@Entity(
    tableName = "configurations_testing",
    foreignKeys = [ForeignKey(
        entity = Device::class,
        parentColumns = ["device_id"],
        childColumns = ["device_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("device_id")]
)
data class ConfigurationTest(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Int? = null,
    @ColumnInfo(name = "device_id") val deviceId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "xml") val xml: String,
    @ColumnInfo(name = "deleted", defaultValue = "0") val deleted: Int = 0
)

@Dao
interface DeviceDao {

    // Insert device
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device): Long

    // Insert configurations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigurations(configs: List<ConfigurationTest>)

    // Update configuration
    @Update
    suspend fun updateConfiguration(config: ConfigurationTest)

    // Soft delete (set deleted = 1)
    @Query("UPDATE configurations_testing SET deleted = 1 WHERE _id = :configId")
    suspend fun softDeleteConfiguration(configId: Int)

    // Get all devices with configurations
    @Transaction
    @Query("SELECT * FROM devices")
    suspend fun getAllDevicesWithConfigurations(): List<DeviceWithConfigurations>

    // Get configurations for a device
    @Query("SELECT * FROM configurations_testing WHERE device_id = :deviceId AND deleted = 0")
    suspend fun getConfigurationsByDeviceId(deviceId: String): List<ConfigurationTest>
}

// Helper class to load relationship
data class DeviceWithConfigurations(
    @Embedded val device: Device,
    @Relation(
        parentColumn = "device_id",
        entityColumn = "device_id"
    )
    val configurations: List<ConfigurationTest>
)

@Database(entities = [Device::class, ConfigurationTest::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DeviceRepository(private val dao: DeviceDao) {

    suspend fun addDeviceWithConfigurations(device: Device, configs: List<ConfigurationTest>) {
        val deviceId = dao.insertDevice(device)
        dao.insertConfigurations(configs)
    }

    suspend fun updateConfiguration(config: ConfigurationTest) {
        dao.updateConfiguration(config)
    }

    suspend fun softDeleteConfiguration(configId: Int) {
        dao.softDeleteConfiguration(configId)
    }

    suspend fun getDeviceWithConfigs(): List<DeviceWithConfigurations> {
        return dao.getAllDevicesWithConfigurations()
    }

    suspend fun getConfigsForDevice(deviceId: String): List<ConfigurationTest> {
        return dao.getConfigurationsByDeviceId(deviceId)
    }
}
