package hn.unah.raindata.data.database.entities

import java.util.UUID

data class Pluviometro(
    val id: String = UUID.randomUUID().toString(),
    val numero_registro: String,
    val latitud: Double,
    val longitud: Double,
    val direccion: String,
    val departamento: String,
    val municipio: String,
    val aldea: String,
    val caserio_barrio_colonia: String? = null,
    val responsable_id: String, // ID del voluntario responsable
    val responsable_nombre: String, // Nombre del voluntario (para mostrar)
    val observaciones: String? = null,
    val activo: Boolean = true,
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis()
)