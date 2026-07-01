package hn.unah.raindata.data.database.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.Exclude
import java.util.Date

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(tableName = "datos_meteorologicos")
data class DatoMeteorologico(
    @PrimaryKey
    @DocumentId
    var id: String = java.util.UUID.randomUUID().toString(),

    @PropertyName("voluntario_uid")
    var voluntario_uid: String = "",

    @PropertyName("voluntario_nombre")
    var voluntario_nombre: String = "",

    @PropertyName("pluviometro_id")
    var pluviometro_id: String = "",

    @PropertyName("pluviometro_registro")
    var pluviometro_registro: String = "",

    @PropertyName("pluviometro_responsable_uid")
    var pluviometro_responsable_uid: String = "",

    @PropertyName("fecha_lectura")
    var fecha_lectura: String = "",

    @PropertyName("hora_lectura")
    var hora_lectura: String = "",

    @PropertyName("fecha_registro")
    var fecha_registro: String = "",

    @PropertyName("hora_registro")
    var hora_registro: String = "",

    @PropertyName("precipitacion")
    var precipitacion: Double = 0.0,

    @PropertyName("temperatura_maxima")
    var temperatura_maxima: Double? = null,

    @PropertyName("temperatura_minima")
    var temperatura_minima: Double? = null,

    @PropertyName("condiciones_dia")
    var condiciones_dia: String = "",

    @PropertyName("observaciones")
    var observaciones: String? = null,

    // ===== SINCRONIZACIÓN OFFLINE (excluidos de Firestore) =====
    @get:Exclude
    var syncStatus: SyncStatus = SyncStatus.ENVIADO,
    @get:Exclude
    var fechaRegistroLocal: Long = System.currentTimeMillis(),

    @Ignore
    @ServerTimestamp
    @PropertyName("fecha_creacion")
    var fecha_creacion: Date? = null,

    @Ignore
    @ServerTimestamp
    @PropertyName("fecha_modificacion")
    var fecha_modificacion: Date? = null,

    @PropertyName("activo")
    var activo: Boolean = true
)