package hn.unah.raindata.data.database.dao

import androidx.room.*
import hn.unah.raindata.data.database.entities.SyncStatus
import hn.unah.raindata.data.database.entities.Pluviometro
import kotlinx.coroutines.flow.Flow

@Dao
interface PluviometroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pluviometro: Pluviometro)

    @Update
    suspend fun actualizar(pluviometro: Pluviometro)

    @Query("SELECT * FROM pluviometros WHERE activo = 1 ORDER BY numero_registro")
    fun obtenerActivos(): Flow<List<Pluviometro>>

    @Query("SELECT * FROM pluviometros WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): Pluviometro?

    @Query("SELECT * FROM pluviometros WHERE syncStatus = :status ORDER BY fechaRegistroLocal ASC")
    suspend fun obtenerPorSyncStatus(status: SyncStatus): List<Pluviometro>

    @Query("UPDATE pluviometros SET syncStatus = :status WHERE id = :id")
    suspend fun actualizarSyncStatus(id: String, status: SyncStatus)

    @Delete
    suspend fun eliminar(pluviometro: Pluviometro)

    @Query("DELETE FROM pluviometros WHERE id = :id")
    suspend fun eliminarPorId(id: String)
}