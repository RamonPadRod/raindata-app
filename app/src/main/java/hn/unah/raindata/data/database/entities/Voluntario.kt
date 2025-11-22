package hn.unah.raindata.data.database.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * ✅ ENTIDAD VOLUNTARIO - FIREBASE FIRESTORE
 * Versión limpia sin campos innecesarios
 */
data class Voluntario(
    @DocumentId
    val firebase_uid: String = "",

    // ===== INFORMACIÓN PERSONAL =====
    val nombre: String = "",
    val email: String = "",
    val telefono: String? = null,

    // ===== IDENTIFICACIÓN =====
    val tipo_documento: String = "DNI",
    val cedula: String? = null,
    val pasaporte: String? = null,
    val fecha_nacimiento: String? = null,
    val genero: String? = null,

    // ===== UBICACIÓN =====
    val direccion: String = "",
    val departamento: String = "",
    val municipio: String = "",
    val aldea: String = "",
    val caserio_barrio_colonia: String = "",

    // ===== ROL Y ESTADO =====
    val tipo_usuario: String = "Observador",
    val estado_aprobacion: String = "Aprobado",
    val activo: Boolean = true,

    // ===== NOTIFICACIONES =====
    val visto_por_admin: Boolean = false,

    // ===== APROBACIÓN =====
    val fecha_aprobacion: Timestamp? = null,
    val aprobado_por_uid: String? = null,
    val aprobado_por_nombre: String? = null,

    // ===== RECHAZO =====
    val fecha_rechazo: Timestamp? = null,
    val rechazado_por_uid: String? = null,
    val rechazado_por_nombre: String? = null,

    // ===== OTROS =====
    val observaciones: String? = null,

    // ===== TIMESTAMPS AUTOMÁTICOS =====
    @ServerTimestamp
    val fecha_registro: Timestamp? = null,
    @ServerTimestamp
    val fecha_creacion: Timestamp? = null,
    @ServerTimestamp
    val fecha_modificacion: Timestamp? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(
        firebase_uid = "",
        nombre = "",
        email = "",
        direccion = "",
        departamento = "",
        municipio = "",
        aldea = ""
    )
}