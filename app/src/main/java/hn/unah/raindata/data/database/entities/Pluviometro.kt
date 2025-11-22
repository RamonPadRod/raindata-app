package hn.unah.raindata.data.database.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

/**
 * ✅ ENTIDAD PLUVIÓMETRO - FIREBASE FIRESTORE
 * Sistema de código automático: XX-YY-ZZZ
 * XX = Código departamento (01-18)
 * YY = Código municipio (01-28)
 * ZZZ = Número secuencial (001, 002, 003...)
 */
data class Pluviometro(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),

    // ===== CÓDIGO Y UBICACIÓN =====
    val numero_registro: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val direccion: String = "",
    val departamento: String = "",
    val municipio: String = "",
    val aldea: String = "",
    val caserio_barrio_colonia: String? = null,

    // ===== RESPONSABLE =====
    val responsable_uid: String = "", // ← CAMBIO: Ahora usa firebase_uid en lugar de Long
    val responsable_nombre: String = "",

    // ===== OTROS =====
    val observaciones: String? = null,
    val activo: Boolean = true,

    // ===== TIMESTAMPS AUTOMÁTICOS =====
    @ServerTimestamp
    val fecha_creacion: Timestamp? = null,
    @ServerTimestamp
    val fecha_modificacion: Timestamp? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(
        id = UUID.randomUUID().toString(),
        numero_registro = "",
        latitud = 0.0,
        longitud = 0.0,
        direccion = "",
        departamento = "",
        municipio = "",
        aldea = "",
        responsable_uid = "",
        responsable_nombre = ""
    )
}