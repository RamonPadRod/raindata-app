package hn.unah.raindata.data.database.entities

import java.util.UUID

data class DatoMeteorologico(
    val id: String = UUID.randomUUID().toString(),
    val voluntario_id: Long,
    val voluntario_nombre: String,
    val pluviometro_id: String,
    val pluviometro_registro: String,
    val fecha_lectura: String,  // Fecha cuando se leyó el pluviómetro (editable ±7 días)
    val hora_lectura: String,   // Hora cuando se leyó el pluviómetro (editable)
    val fecha_registro: String, // Fecha cuando se registró en la app (no editable)
    val hora_registro: String,  // Hora cuando se registró en la app (no editable)
    val precipitacion: Double,
    val temperatura_maxima: Double?,
    val temperatura_minima: Double?,
    val condiciones_dia: String,  // Múltiples condiciones separadas por "|"
    val observaciones: String?,
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis()
)