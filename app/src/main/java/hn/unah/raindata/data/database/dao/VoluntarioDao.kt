package hn.unah.raindata.data.database.dao

import androidx.room.*
import hn.unah.raindata.data.database.entities.SyncStatus
import hn.unah.raindata.data.database.entities.Voluntario
import kotlinx.coroutines.flow.Flow

@Dao
interface VoluntarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(voluntario: Voluntario)

    @Update
    suspend fun actualizar(voluntario: Voluntario)

    @Query("SELECT * FROM voluntarios WHERE activo = 1 ORDER BY nombre")
    fun obtenerActivos(): Flow<List<Voluntario>>

    @Query("SELECT * FROM voluntarios ORDER BY nombre")
    fun obtenerTodos(): Flow<List<Voluntario>>

    @Query("SELECT * FROM voluntarios WHERE firebase_uid = :uid LIMIT 1")
    suspend fun obtenerPorUid(uid: String): Voluntario?

    @Query("SELECT * FROM voluntarios WHERE email = :email LIMIT 1")
    suspend fun obtenerPorEmail(email: String): Voluntario?

    @Query("SELECT * FROM voluntarios WHERE syncStatus = :status ORDER BY fechaRegistroLocal ASC")
    suspend fun obtenerPorSyncStatus(status: SyncStatus): List<Voluntario>

    @Query("UPDATE voluntarios SET syncStatus = :status WHERE firebase_uid = :uid")
    suspend fun actualizarSyncStatus(uid: String, status: SyncStatus)

    @Delete
    suspend fun eliminar(voluntario: Voluntario)

    @Query("DELETE FROM voluntarios WHERE firebase_uid = :uid")
    suspend fun eliminarPorUid(uid: String)
}