package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigurationsDao {
    @Query("SELECT * FROM configurations")
    fun getAllConfigurations(): Flow<List<Configuration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(configuration: Configuration): Long

    @Query("DELETE FROM configurations WHERE name = :configurationName")
    suspend fun deleteConfiguration(configurationName: String)

}
