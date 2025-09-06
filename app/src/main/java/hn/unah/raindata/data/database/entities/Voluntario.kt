package hn.unah.raindata.data.database.entities

import java.util.UUID

data class Voluntario(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val direccion: String,
    val departamento: String,
    val municipio: String,
    val aldea: String,
    val caserio_barrio_colonia: String,
    val telefono: String? = null,
    val email: String? = null,
    val cedula: String? = null,
    val fecha_nacimiento: String? = null,
    val genero: String? = null,
    val ocupacion: String? = null,
    val experiencia_años: Int? = null,
    val observaciones: String? = null,
    val activo: Boolean = true,
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis()
)