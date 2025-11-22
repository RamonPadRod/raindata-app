package hn.unah.raindata.data.database.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DatoMeteorologico(
    @DocumentId
    val id: String = "",

    @PropertyName("voluntario_uid")
    val voluntario_uid: String = "",

    @PropertyName("voluntario_nombre")
    val voluntario_nombre: String = "",

    @PropertyName("pluviometro_id")
    val pluviometro_id: String = "",

    @PropertyName("pluviometro_registro")
    val pluviometro_registro: String = "",

    @PropertyName("pluviometro_responsable_uid")
    val pluviometro_responsable_uid: String = "",

    @PropertyName("fecha_lectura")
    val fecha_lectura: String = "",

    @PropertyName("hora_lectura")
    val hora_lectura: String = "",

    @PropertyName("fecha_registro")
    val fecha_registro: String = "",

    @PropertyName("hora_registro")
    val hora_registro: String = "",

    @PropertyName("precipitacion")
    val precipitacion: Double = 0.0,

    @PropertyName("temperatura_maxima")
    val temperatura_maxima: Double? = null,

    @PropertyName("temperatura_minima")
    val temperatura_minima: Double? = null,

    @PropertyName("condiciones_dia")
    val condiciones_dia: String = "",

    @PropertyName("observaciones")
    val observaciones: String? = null,

    @ServerTimestamp
    @PropertyName("fecha_creacion")
    val fecha_creacion: Date? = null,

    @ServerTimestamp
    @PropertyName("fecha_modificacion")
    val fecha_modificacion: Date? = null,

    @PropertyName("activo")
    val activo: Boolean = true
) {
    constructor() : this(
        id = "",
        voluntario_uid = "",
        voluntario_nombre = "",
        pluviometro_id = "",
        pluviometro_registro = "",
        pluviometro_responsable_uid = "",
        fecha_lectura = "",
        hora_lectura = "",
        fecha_registro = "",
        hora_registro = "",
        precipitacion = 0.0,
        temperatura_maxima = null,
        temperatura_minima = null,
        condiciones_dia = "",
        observaciones = null,
        fecha_creacion = null,
        fecha_modificacion = null,
        activo = true
    )
}