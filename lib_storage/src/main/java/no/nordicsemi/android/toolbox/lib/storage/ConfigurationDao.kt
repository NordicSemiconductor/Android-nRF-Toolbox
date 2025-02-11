package no.nordicsemi.android.toolbox.lib.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigurationsDao {

    // TODO: Modify this so first it checks if the device id and then returns the configurations
    @Query("SELECT * FROM configurations")
    fun load(): Flow<List<Configuration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(configuration: Configuration)

    @Query("DELETE FROM configurations WHERE name = :name")
    suspend fun delete(name: String)
}