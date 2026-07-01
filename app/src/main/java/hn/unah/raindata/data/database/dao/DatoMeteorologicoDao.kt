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

    // ===== ESTADÍSTICAS =====

    @Query("""
        SELECT COUNT(*) FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
    """)
    suspend fun contarRegistros(desde: String, hasta: String, pluvId: String, volId: String): Int

    @Query("""
        SELECT COALESCE(SUM(precipitacion), 0.0) FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
    """)
    suspend fun sumarPrecipitacion(desde: String, hasta: String, pluvId: String, volId: String): Double

    @Query("""
        SELECT COALESCE(SUM(precipitacion) / NULLIF(COUNT(DISTINCT fecha_lectura), 0), 0.0) FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
    """)
    suspend fun promedioPrecipitacion(desde: String, hasta: String, pluvId: String, volId: String): Double

    @Query("""
        SELECT fecha_lectura FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
        GROUP BY fecha_lectura
        ORDER BY SUM(precipitacion) DESC
        LIMIT 1
    """)
    suspend fun diaMayorPrecipitacion(desde: String, hasta: String, pluvId: String, volId: String): String?

    @Query("""
        SELECT MAX(temperatura_maxima) FROM datos_meteorologicos
        WHERE activo = 1 AND temperatura_maxima IS NOT NULL
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
    """)
    suspend fun temperaturaMaximaAbsoluta(desde: String, hasta: String, pluvId: String, volId: String): Double?

    @Query("""
        SELECT MIN(temperatura_minima) FROM datos_meteorologicos
        WHERE activo = 1 AND temperatura_minima IS NOT NULL
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
    """)
    suspend fun temperaturaMinimaAbsoluta(desde: String, hasta: String, pluvId: String, volId: String): Double?

    @Query("""
        SELECT AVG(temperatura_maxima) FROM datos_meteorologicos
        WHERE activo = 1 AND temperatura_maxima IS NOT NULL
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
    """)
    suspend fun promedioTemperaturaMaxima(desde: String, hasta: String, pluvId: String, volId: String): Double?

    @Query("""
        SELECT AVG(temperatura_minima) FROM datos_meteorologicos
        WHERE activo = 1 AND temperatura_minima IS NOT NULL
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
    """)
    suspend fun promedioTemperaturaMinima(desde: String, hasta: String, pluvId: String, volId: String): Double?

    @Query("""
        SELECT pluviometro_registro, COALESCE(SUM(precipitacion), 0.0) AS totalPrecipitacion
        FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
        GROUP BY pluviometro_id, pluviometro_registro
        ORDER BY totalPrecipitacion DESC
        LIMIT 10
    """)
    suspend fun precipitacionPorPluviometro(desde: String, hasta: String, pluvId: String, volId: String): List<PrecipPluvResult>

    @Query("""
        SELECT voluntario_nombre, COUNT(*) AS totalRegistros
        FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
        GROUP BY voluntario_uid, voluntario_nombre
        ORDER BY totalRegistros DESC
        LIMIT 1
    """)
    suspend fun voluntarioConMasRegistros(desde: String, hasta: String, pluvId: String): VoluntarioTopResult?

    @Query("""
        SELECT pluviometro_registro, COUNT(*) AS totalRegistros
        FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
        GROUP BY pluviometro_id, pluviometro_registro
        ORDER BY totalRegistros DESC
        LIMIT 1
    """)
    suspend fun pluviometroMasActivo(desde: String, hasta: String, pluvId: String, volId: String): PluvioActResult?

    @Query("SELECT id, activo, fecha_lectura, syncStatus FROM datos_meteorologicos")
    suspend fun debugTodos(): List<DebugDato>

    data class DebugDato(
        val id: String,
        val activo: Boolean,
        val fecha_lectura: String,
        val syncStatus: String
    )

    @Query("""
        SELECT * FROM datos_meteorologicos
        WHERE activo = 1
          AND (:desde = '' OR fecha_lectura >= :desde)
          AND (:hasta = '' OR fecha_lectura <= :hasta)
          AND (:pluvId = '' OR pluviometro_id = :pluvId)
          AND (:volId  = '' OR voluntario_uid = :volId)
        ORDER BY fecha_lectura DESC, hora_lectura DESC
    """)
    suspend fun obtenerTodosFiltrados(desde: String, hasta: String, pluvId: String, volId: String): List<DatoMeteorologico>

    @Query("SELECT * FROM datos_meteorologicos")
    suspend fun obtenerTodosDirecto(): List<DatoMeteorologico>
}

// Resultados de queries de agregación
data class PrecipPluvResult(
    val pluviometro_registro: String,
    val totalPrecipitacion: Double
)

data class VoluntarioTopResult(
    val voluntario_nombre: String,
    val totalRegistros: Int
)

data class PluvioActResult(
    val pluviometro_registro: String,
    val totalRegistros: Int
)
