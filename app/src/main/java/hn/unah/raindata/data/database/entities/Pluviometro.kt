package hn.unah.raindata.data.database.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

/**
 * ✅ ENTIDAD PLUVIÓMETRO - FIREBASE FIRESTORE & ROOM
 * Sistema de código automático: XX-YY-ZZZ
 * XX = Código departamento (01-18)
 * YY = Código municipio (01-28)
 * ZZZ = Número secuencial (001, 002, 003...)
 */
@Entity(tableName = "pluviometros")
data class Pluviometro(
    @PrimaryKey
    @DocumentId
    var id: String = UUID.randomUUID().toString(),

    // ===== CÓDIGO Y UBICACIÓN =====
    var numero_registro: String = "",
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var direccion: String = "",
    var departamento: String = "",
    var municipio: String = "",
    var aldea: String = "",
    var caserio_barrio_colonia: String? = null,

    // ===== RESPONSABLE =====
    var responsable_uid: String = "", // ← CAMBIO: Ahora usa firebase_uid en lugar de Long
    var responsable_nombre: String = "",

    // ===== OTROS =====
    var observaciones: String? = null,
    var activo: Boolean = true,

    // ===== SINCRONIZACIÓN OFFLINE =====
    var syncStatus: SyncStatus = SyncStatus.ENVIADO,
    var fechaRegistroLocal: Long = System.currentTimeMillis(),

    // ===== TIMESTAMPS AUTOMÁTICOS =====
    @Ignore
    @ServerTimestamp
    var fecha_creacion: Timestamp? = null,
    @Ignore
    @ServerTimestamp
    var fecha_modificacion: Timestamp? = null
)