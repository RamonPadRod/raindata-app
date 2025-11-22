package hn.unah.raindata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DatoMeteorologicoViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val datosCollection = firestore.collection("datos_meteorologicos")

    private val _datoMeteorologico = MutableStateFlow<DatoMeteorologico?>(null)
    val datoMeteorologico: StateFlow<DatoMeteorologico?> = _datoMeteorologico

    private val _datosMeteorologicos = MutableStateFlow<List<DatoMeteorologico>>(emptyList())
    val datosMeteorologicos: StateFlow<List<DatoMeteorologico>> = _datosMeteorologicos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var datosListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun guardarDato(
        dato: DatoMeteorologico,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val docRef = if (dato.id.isEmpty()) {
                    datosCollection.document()
                } else {
                    datosCollection.document(dato.id)
                }

                val datoConId = dato.copy(id = docRef.id)
                docRef.set(datoConId).await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al guardar dato")
            }
        }
    }

    fun actualizarDato(
        dato: DatoMeteorologico,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                datosCollection.document(dato.id)
                    .set(dato)
                    .await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al actualizar dato")
            }
        }
    }

    fun eliminarDato(
        datoId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                datosCollection.document(datoId)
                    .update("activo", false)  // ✅ SOFT DELETE - solo marca como inactivo
                    .await()

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al eliminar dato")
            }
        }
    }

    fun cargarDatoPorId(datoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val document = datosCollection.document(datoId).get().await()
                val dato = document.toObject(DatoMeteorologico::class.java)

                _datoMeteorologico.value = dato
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar dato: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun cargarTodosDatos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                datosCollection
                    .whereEqualTo("activo", true)  // ✅ IMPORTANTE
                    .orderBy("fecha_lectura", Query.Direction.DESCENDING)
                    .orderBy("hora_lectura", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .let { querySnapshot ->
                        val lista = querySnapshot.documents.mapNotNull { doc ->
                            doc.toObject(DatoMeteorologico::class.java)?.copy(id = doc.id)
                        }
                        _datosMeteorologicos.value = lista
                    }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun cargarDatosPorVoluntario(voluntarioUid: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                datosCollection
                    .whereEqualTo("pluviometro_responsable_uid", voluntarioUid)
                    .whereEqualTo("activo", true)  // ✅ IMPORTANTE
                    .orderBy("fecha_lectura", Query.Direction.DESCENDING)
                    .orderBy("hora_lectura", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .let { querySnapshot ->
                        val lista = querySnapshot.documents.mapNotNull { doc ->
                            doc.toObject(DatoMeteorologico::class.java)?.copy(id = doc.id)
                        }
                        _datosMeteorologicos.value = lista
                    }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun cargarDatosPorPluviometro(pluviometroId: String) {
        datosListener?.remove()

        _isLoading.value = true

        datosListener = datosCollection
            .whereEqualTo("pluviometro_id", pluviometroId)
            .whereEqualTo("activo", true)
            .orderBy("fecha_lectura", Query.Direction.DESCENDING)
            .orderBy("hora_lectura", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    _errorMessage.value = "Error al cargar datos: ${error.message}"
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(DatoMeteorologico::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                _datosMeteorologicos.value = lista
            }
    }

    fun validarFechaLectura(fecha: String): String? {
        if (fecha.isBlank()) return "La fecha de lectura es obligatoria"

        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formato.isLenient = false
            val fechaIngresada = formato.parse(fecha) ?: return "Formato de fecha inválido (yyyy-MM-dd)"

            val calendario = Calendar.getInstance()
            val hoy = calendario.time

            calendario.add(Calendar.DAY_OF_YEAR, -7)
            val hace7Dias = calendario.time

            calendario.time = hoy
            calendario.add(Calendar.DAY_OF_YEAR, 7)
            val en7Dias = calendario.time

            when {
                fechaIngresada.before(hace7Dias) -> "La fecha de lectura no puede ser anterior a 7 días"
                fechaIngresada.after(en7Dias) -> "La fecha de lectura no puede ser posterior a 7 días"
                else -> null
            }
        } catch (e: Exception) {
            "Formato de fecha inválido (yyyy-MM-dd)"
        }
    }

    fun validarHora(hora: String): String? {
        if (hora.isBlank()) return "La hora es obligatoria"

        return try {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            formato.isLenient = false
            formato.parse(hora)
            null
        } catch (e: Exception) {
            "Formato de hora inválido (HH:mm)"
        }
    }

    fun validarPrecipitacion(valor: String): String? {
        if (valor.isBlank()) return "La precipitación es obligatoria"

        val precipitacion = valor.toDoubleOrNull()
            ?: return "Debe ingresar un número válido"

        return when {
            precipitacion < 0 -> "La precipitación no puede ser negativa"
            precipitacion > 500 -> "Precipitación fuera de rango (máx. 500mm)"
            else -> null
        }
    }

    fun validarTemperaturaMaxima(valor: String): String? {
        if (valor.isBlank()) return null

        val temp = valor.toDoubleOrNull()
            ?: return "Debe ingresar un número válido"

        return when {
            temp < 10 -> "Temp. máxima muy baja (mín. 10°C)"
            temp > 50 -> "Temp. máxima fuera de rango (máx. 50°C)"
            else -> null
        }
    }

    fun validarTemperaturaMinima(valor: String): String? {
        if (valor.isBlank()) return null

        val temp = valor.toDoubleOrNull()
            ?: return "Debe ingresar un número válido"

        return when {
            temp < -5 -> "Temp. mínima muy baja (mín. -5°C)"
            temp > 40 -> "Temp. mínima muy alta (máx. 40°C)"
            else -> null
        }
    }

    fun validarCoherenciaTemperaturas(tempMin: String, tempMax: String): String? {
        if (tempMin.isBlank() || tempMax.isBlank()) return null

        val min = tempMin.toDoubleOrNull()
        val max = tempMax.toDoubleOrNull()

        if (min != null && max != null && min >= max) {
            return "Temp. mínima debe ser menor que temp. máxima"
        }

        return null
    }

    fun validarObservaciones(valor: String): String? {
        return if (valor.length > 500) {
            "Las observaciones no pueden exceder 500 caracteres"
        } else null
    }

    fun validarCondicionesDia(condiciones: List<String>): String? {
        return when {
            condiciones.isEmpty() -> "Debe seleccionar al menos una condición"
            condiciones.size > 3 -> "Puede seleccionar máximo 3 condiciones"
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        datosListener?.remove()
    }
}