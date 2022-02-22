package no.nordicsemi.android.uart.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ConfigurationsDao {

    @Query("SELECT * FROM configurations")
    fun load(): Flow<List<Configuration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(configuration: Configuration)

    @Query("DELETE FROM configurations WHERE name = :name")
    suspend fun delete(name: String)
}
