package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.repository.PluviometroRepository
import hn.unah.raindata.data.utils.DepartamentosHonduras
import hn.unah.raindata.data.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.Job

/**
 * ✅ VIEWMODEL DE PLUVIÓMETROS - MODO OFFLINE REPOSITORY
 */
class PluviometroViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PluviometroRepository(db.pluviometroDao())

    private val _pluviometros = MutableStateFlow<List<Pluviometro>>(emptyList())
    val pluviometros: StateFlow<List<Pluviometro>> = _pluviometros.asStateFlow()

    private val _pluviometroSeleccionado = MutableStateFlow<Pluviometro?>(null)
    val pluviometroSeleccionado: StateFlow<Pluviometro?> = _pluviometroSeleccionado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ===== ESTADO DEL FORMULARIO (PERSISTENCIA AL NAVEGAR) =====
    private val _direccionDraft = MutableStateFlow("")
    val direccionDraft = _direccionDraft.asStateFlow()
    fun setDireccionDraft(value: String) { _direccionDraft.value = value }

    private val _departamentoDraft = MutableStateFlow("")
    val departamentoDraft = _departamentoDraft.asStateFlow()
    fun setDepartamentoDraft(value: String) { _departamentoDraft.value = value }

    private val _municipioDraft = MutableStateFlow("")
    val municipioDraft = _municipioDraft.asStateFlow()
    fun setMunicipioDraft(value: String) { _municipioDraft.value = value }

    private val _aldeaDraft = MutableStateFlow("")
    val aldeaDraft = _aldeaDraft.asStateFlow()
    fun setAldeaDraft(value: String) { _aldeaDraft.value = value }

    private val _caserioDraft = MutableStateFlow("")
    val caserioDraft = _caserioDraft.asStateFlow()
    fun setCaserioDraft(value: String) { _caserioDraft.value = value }

    private val _voluntarioDraft = MutableStateFlow<hn.unah.raindata.data.database.entities.Voluntario?>(null)
    val voluntarioDraft = _voluntarioDraft.asStateFlow()
    fun setVoluntarioDraft(value: hn.unah.raindata.data.database.entities.Voluntario?) { _voluntarioDraft.value = value }

    private val _observacionesDraft = MutableStateFlow("")
    val observacionesDraft = _observacionesDraft.asStateFlow()
    fun setObservacionesDraft(value: String) { _observacionesDraft.value = value }

    private val _ubicacionDraft = MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)
    val ubicacionDraft = _ubicacionDraft.asStateFlow()
    fun setUbicacionDraft(value: com.google.android.gms.maps.model.LatLng?) { _ubicacionDraft.value = value }

    // ✅ ESTADO PARA CÓDIGO GENERADO
    private val _codigoGenerado = MutableStateFlow("")
    val codigoGenerado: StateFlow<String> = _codigoGenerado.asStateFlow()

    fun limpiarDraft() {
        _direccionDraft.value = ""
        _departamentoDraft.value = ""
        _municipioDraft.value = ""
        _aldeaDraft.value = ""
        _caserioDraft.value = ""
        _voluntarioDraft.value = null
        _observacionesDraft.value = ""
        _ubicacionDraft.value = null
        _codigoGenerado.value = ""
    }

    // ===== LÓGICA DE CÓDIGO =====
    fun generarCodigoAutomatico(departamento: String, municipio: String) {
        viewModelScope.launch {
            val deptoCode = DepartamentosHonduras.obtenerCodigoDepartamento(departamento)
            if (deptoCode == "00") {
                _codigoGenerado.value = ""
                return@launch
            }

            val lista = repository.obtenerPluviometros().first()
            val count = lista.count { it.departamento == departamento } + 1
            val correlativo = count.toString().padStart(3, '0')
            _codigoGenerado.value = "PL-$deptoCode-$correlativo"
        }
    }

    fun limpiarCodigo() {
        _codigoGenerado.value = ""
    }

    // ===== ESTADO DE NAVEGACIÓN =====
    private val _subPantalla = MutableStateFlow("LISTA")
    val subPantalla: StateFlow<String> = _subPantalla.asStateFlow()

    fun setSubPantalla(valor: String) { _subPantalla.value = valor }
    fun resetSubPantalla() { _subPantalla.value = "LISTA" }

    init {
        cargarPluviometros()
        observarConectividad()
    }

    // Job del collect activo. Se cancela antes de iniciar uno nuevo.
    private var collectJob: Job? = null

    /**
     * Al recuperar conexión a internet, sincroniza automáticamente los pluviómetros pendientes.
     * drop(1): ignora el estado inicial del StateFlow para solo reaccionar a CAMBIOS de red.
     */
    private fun observarConectividad() {
        viewModelScope.launch {
            NetworkMonitor.isOnline.drop(1).collect { online ->
                if (online) {
                    android.util.Log.d("PluviometroViewModel", "🌐 Online – sincronizando pluviometros")
                    launch { repository.sincronizarPendientesLocal() }
                }
            }
        }
    }

    fun cargarPluviometros() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _isLoading.value = true
            // Sync de fondo solo si estamos online
            if (NetworkMonitor.isOnline.value) {
                launch { repository.sincronizarDesdeNube() }
            }
            repository.obtenerPluviometros().collect { lista ->
                _pluviometros.value = lista
                _isLoading.value = false
            }
        }
    }

    fun cargarPluviometrosPorUsuario(uid: String) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _isLoading.value = true
            // Sync de fondo solo si estamos online
            if (NetworkMonitor.isOnline.value) {
                launch { repository.sincronizarDesdeNube() }
            }
            repository.obtenerPluviometros().collect { lista ->
                _pluviometros.value = lista.filter { it.responsable_uid == uid }
                _isLoading.value = false
            }
        }
    }

    fun obtenerPluviometrosPorResponsable(uid: String) {
        cargarPluviometrosPorUsuario(uid)
    }

    fun guardarPluviometro(
        pluviometro: Pluviometro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.guardarPluviometro(pluviometro, getApplication())
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al guardar pluviómetro")
            }
        }
    }

    fun actualizarPluviometro(
        pluviometro: Pluviometro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        guardarPluviometro(pluviometro, onSuccess, onError)
    }

    fun eliminarPluviometro(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.eliminarPluviometro(id)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al eliminar pluviómetro")
            }
        }
    }

    fun obtenerPorId(id: String) {
        viewModelScope.launch {
            _pluviometroSeleccionado.value = repository.obtenerPorId(id)
        }
    }

    fun limpiarSeleccion() {
        _pluviometroSeleccionado.value = null
    }
}