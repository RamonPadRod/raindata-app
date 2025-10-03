package hn.unah.raindata.data.database.entities

import java.util.UUID

data class DatoMeteorologico(
    val id: String = UUID.randomUUID().toString(),
    val voluntario_id: String,
    val voluntario_nombre: String,
    val pluviometro_id: String,
    val pluviometro_registro: String,
    val fecha: String,
    val hora: String,
    val precipitacion: Double,
    val temperatura_maxima: Double?,
    val temperatura_minima: Double?,
    val condicion_dia: String,
    val observaciones: String?,
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis()
)