package hn.unah.raindata.data.database.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

/**
 * ✅ ENTIDAD VOLUNTARIO - FIREBASE FIRESTORE & ROOM
 * Versión limpia sin campos innecesarios
 */
@Entity(tableName = "voluntarios")
data class Voluntario(
    @PrimaryKey
    @DocumentId
    var firebase_uid: String = "",

    // ===== INFORMACIÓN PERSONAL =====
    var nombre: String = "",
    var email: String = "",
    var telefono: String? = null,

    // ===== IDENTIFICACIÓN =====
    var tipo_documento: String = "DNI",
    var cedula: String? = null,
    var pasaporte: String? = null,
    var fecha_nacimiento: String? = null,
    var genero: String? = null,

    // ===== UBICACIÓN =====
    var direccion: String = "",
    var departamento: String = "",
    var municipio: String = "",
    var aldea: String = "",
    var caserio_barrio_colonia: String = "",

    // ===== ROL Y ESTADO =====
    var tipo_usuario: String = "Observador",
    var estado_aprobacion: String = "Aprobado",
    var activo: Boolean = true,

    // ===== NOTIFICACIONES =====
    var visto_por_admin: Boolean = false,

    // ===== APROBACIÓN =====
    var fecha_aprobacion: String? = null,
    var aprobado_por_uid: String? = null,
    var aprobado_por_nombre: String? = null,

// ===== RECHAZO =====
    var fecha_rechazo: String? = null,
    var rechazado_por_uid: String? = null,
    var rechazado_por_nombre: String? = null,

    // ===== OTROS =====
    var observaciones: String? = null,

    // ===== SINCRONIZACIÓN OFFLINE =====
    var syncStatus: SyncStatus = SyncStatus.ENVIADO,
    var fechaRegistroLocal: Long = System.currentTimeMillis(),

    // ===== TIMESTAMPS AUTOMÁTICOS =====
    @Ignore
    @ServerTimestamp
    var fecha_registro: Timestamp? = null,
    @Ignore
    @ServerTimestamp
    var fecha_creacion: Timestamp? = null,
    @Ignore
    @ServerTimestamp
    var fecha_modificacion: Timestamp? = null
)