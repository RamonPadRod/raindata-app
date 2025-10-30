package hn.unah.raindata.data.database.entities

// ✅ ENTIDAD VOLUNTARIO COMPATIBLE CON LA MIGRACIÓN
// Todos los campos nuevos son opcionales (nullable) para evitar crashes

data class Voluntario(
    val id: Long = 0,
    val firebase_uid: String = "",
    val nombre: String,
    val direccion: String,
    val departamento: String,
    val municipio: String,
    val aldea: String,
    val caserio_barrio_colonia: String = "",
    val telefono: String? = null,
    val email: String,

    // ✅ Campos nuevos - todos opcionales
    val tipo_documento: String = "DNI", // Valores: "DNI" o "Pasaporte"
    val cedula: String? = null,
    val pasaporte: String? = null,
    val fecha_nacimiento: String? = null, // Formato: "YYYY-MM-DD"
    val genero: String? = null, // Valores: "Masculino", "Femenino", "Otro"

    val tipo_usuario: String? = null,
    val estado_aprobacion: String = "Aprobado",
    val experiencia_años: Int? = null,
    val observaciones: String? = null,
    val fotografia: String? = null,

    val activo: Boolean = true,
    val fecha_creacion: Long = System.currentTimeMillis(),
    val fecha_modificacion: Long = System.currentTimeMillis()
)