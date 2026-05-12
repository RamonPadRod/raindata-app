package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.repository.DatoMeteorologicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ VIEWMODEL DE DATOS METEOROLÓGICOS - MODO OFFLINE REPOSITORY
 */
class DatoMeteorologicoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = DatoMeteorologicoRepository(db.datoMeteorologicoDao())

    private val _datoMeteorologico = MutableStateFlow<DatoMeteorologico?>(null)
    val datoMeteorologico: StateFlow<DatoMeteorologico?> = _datoMeteorologico.asStateFlow()

    private val _datosMeteorologicos = MutableStateFlow<List<DatoMeteorologico>>(emptyList())
    val datosMeteorologicos: StateFlow<List<DatoMeteorologico>> = _datosMeteorologicos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ===== ESTADO DE NAVEGACIÓN =====
    private val _subPantalla = MutableStateFlow("LISTA")
    val subPantalla: StateFlow<String> = _subPantalla.asStateFlow()

    fun setSubPantalla(valor: String) { _subPantalla.value = valor }
    fun resetSubPantalla() { _subPantalla.value = "LISTA" }

    init {
        cargarTodosDatos()
    }

    fun cargarTodosDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            // Sync de fondo
            launch { repository.sincronizarDesdeNube() }

            repository.obtenerTodos().collect { lista ->
                _datosMeteorologicos.value = lista
                _isLoading.value = false
            }
        }
    }

    fun cargarDatosPorVoluntario(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Sync de fondo
            launch { repository.sincronizarDesdeNube() }

            repository.obtenerTodos().collect { lista ->
                _datosMeteorologicos.value = lista.filter { it.voluntario_uid == uid }
                _isLoading.value = false
            }
        }
    }

    // ALIAS para compatibilidad con pantallas existentes
    fun cargarDatoPorId(id: String) {
        obtenerPorId(id)
    }

    fun obtenerPorId(id: String) {
        viewModelScope.launch {
            _datoMeteorologico.value = repository.obtenerPorId(id)
        }
    }

    fun guardarDato(
        dato: DatoMeteorologico,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.guardarDato(dato)
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
                repository.guardarDato(dato) // Repository maneja insert/update
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
                repository.eliminarDato(datoId)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al eliminar dato")
            }
        }
    }

    // ===== MÉTODOS DE VALIDACIÓN (RESTORED) =====
    fun validarFechaLectura(fecha: String): String? {
        if (fecha.isBlank()) return "La fecha es obligatoria"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(fecha)
            val cal = Calendar.getInstance()
            val hoy = cal.time
            
            cal.add(Calendar.DAY_OF_YEAR, -7)
            val haceSieteDias = cal.time
            
            if (date.after(hoy)) return "La fecha no puede ser futura"
            if (date.before(haceSieteDias)) return "No puede registrar datos de hace más de 7 días"
            
            null
        } catch (e: Exception) {
            "Formato inválido (yyyy-MM-dd)"
        }
    }

    fun validarHora(hora: String): String? {
        if (hora.isBlank()) return "La hora es obligatoria"
        return try {
            val parts = hora.split(":")
            if (parts.size != 2) return "Formato inválido (HH:mm)"
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            if (h !in 0..23 || m !in 0..59) return "Hora inválida"
            null
        } catch (e: Exception) {
            "Formato inválido"
        }
    }

    fun validarPrecipitacion(valor: String): String? {
        if (valor.isBlank()) return "La precipitación es obligatoria"
        val p = valor.toDoubleOrNull() ?: return "Debe ser un número"
        if (p < 0 || p > 500) return "Rango válido: 0 - 500 mm"
        return null
    }

    fun validarTemperaturaMaxima(valor: String): String? {
        if (valor.isBlank()) return null // Opcional
        val t = valor.toDoubleOrNull() ?: return "Debe ser un número"
        if (t < 10 || t > 50) return "Rango válido: 10°C - 50°C"
        return null
    }

    fun validarTemperaturaMinima(valor: String): String? {
        if (valor.isBlank()) return null // Opcional
        val t = valor.toDoubleOrNull() ?: return "Debe ser un número"
        if (t < -5 || t > 40) return "Rango válido: -5°C - 40°C"
        return null
    }

    fun validarCoherenciaTemperaturas(min: String, max: String): String? {
        val tMin = min.toDoubleOrNull() ?: return null
        val tMax = max.toDoubleOrNull() ?: return null
        if (tMin >= tMax) return "La temperatura mínima debe ser menor a la máxima"
        return null
    }

    fun validarCondicionesDia(condiciones: List<String>): String? {
        if (condiciones.isEmpty()) return "Seleccione al menos una condición"
        if (condiciones.size > 3) return "Máximo 3 condiciones"
        return null
    }

    fun validarObservaciones(obs: String): String? {
        if (obs.length > 500) return "Máximo 500 caracteres"
        return null
    }

    fun limpiarDatoSeleccionado() {
        _datoMeteorologico.value = null
    }
}