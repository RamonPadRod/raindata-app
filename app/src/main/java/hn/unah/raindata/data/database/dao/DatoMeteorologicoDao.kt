package hn.unah.raindata.data.database.dao

import androidx.room.*
import hn.unah.raindata.data.database.entities.SyncStatus
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import kotlinx.coroutines.flow.Flow

@Dao
interface DatoMeteorologicoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(dato: DatoMeteorologico)

    @Update
    suspend fun actualizar(dato: DatoMeteorologico)

    @Query("SELECT * FROM datos_meteorologicos WHERE activo = 1 ORDER BY fecha_registro DESC, hora_registro DESC")
    fun obtenerTodos(): Flow<List<DatoMeteorologico>>

    @Query("SELECT * FROM datos_meteorologicos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): DatoMeteorologico?

    @Query("SELECT * FROM datos_meteorologicos WHERE syncStatus = :status ORDER BY fechaRegistroLocal ASC")
    suspend fun obtenerPorSyncStatus(status: SyncStatus): List<DatoMeteorologico>

    @Query("UPDATE datos_meteorologicos SET syncStatus = :status WHERE id = :id")
    suspend fun actualizarSyncStatus(id: String, status: SyncStatus)

    @Delete
    suspend fun eliminar(dato: DatoMeteorologico)

    @Query("DELETE FROM datos_meteorologicos WHERE id = :id")
    suspend fun eliminarPorId(id: String)
}