package hn.unah.raindata.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "voluntarios",
    indices = [Index(value = ["email"], unique = true), Index(value = ["firebase_uid"], unique = true)]
)
data class Voluntario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firebase_uid: String = "",
    val nombre: String,
    val direccion: String,
    val departamento: String,
    val municipio: String,
    val aldea: String,
    val caserio_barrio_colonia: String,
    val telefono: String? = null,
    val email: String,
    val cedula: String? = null,
    val fecha_nacimiento: String? = null,
    val genero: String? = null,
    val tipo_usuario: String? = null,
    val estado_aprobacion: String = "Aprobado",
    val experiencia_años: Int? = null,
    val observaciones: String? = null,
    val activo: Boolean = true,
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis()
)