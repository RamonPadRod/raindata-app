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

    // ===== CONSTANTES Y FILTRADO DE COORDENADAS =====
    companion object {
        const val MAX_DECIMALES_COORDENADA = 6
        const val MAX_ENTEROS_LATITUD = 2
        const val MAX_ENTEROS_LONGITUD = 3
        const val MAX_LONGITUD_TEXTO_LATITUD = 10 // signo(1) + 2 enteros + sep(1) + 6 decimales = 10
        const val MAX_LONGITUD_TEXTO_LONGITUD = 11 // signo(1) + 3 enteros + sep(1) + 6 decimales = 11

        val REGEX_LATITUD = Regex("^-?\\d{0,2}([.,]\\d{0,6})?$")
        val REGEX_LONGITUD = Regex("^-?\\d{0,3}([.,]\\d{0,6})?$")

        /**
         * Redondea un valor Double a 6 decimales usando Math.round(x * 1e6) / 1e6.
         */
        fun redondearA6Decimales(valor: Double): Double {
            return Math.round(valor * 1e6) / 1e6
        }

        /**
         * Filtra la entrada de texto para latitud o longitud.
         * - Acepta estados intermedios: "", "-", "14.", "-86.", ".5"
         * - Pegado: si trae más de 6 decimales, lo recorta al 6to decimal sin redondear.
         * - Si no cumple las reglas, devuelve null (rechazado).
         */
        fun filtrarEntradaCoordenada(texto: String, esLatitud: Boolean): String? {
            if (texto.isEmpty()) return ""

            // 1. Manejo de pegado con más de 6 decimales
            val separadores = texto.count { it == '.' || it == ',' }
            var textoProcesado = texto
            if (separadores == 1) {
                val sepIndex = textoProcesado.indexOfAny(charArrayOf('.', ','))
                val parteEnteraYSigno = textoProcesado.substring(0, sepIndex + 1)
                val parteDecimal = textoProcesado.substring(sepIndex + 1)
                if (parteDecimal.length > MAX_DECIMALES_COORDENADA) {
                    textoProcesado = parteEnteraYSigno + parteDecimal.take(MAX_DECIMALES_COORDENADA)
                }
            }

            // 2. Validar con la regex y longitud máxima correspondiente
            val regex = if (esLatitud) REGEX_LATITUD else REGEX_LONGITUD
            val maxLen = if (esLatitud) MAX_LONGITUD_TEXTO_LATITUD else MAX_LONGITUD_TEXTO_LONGITUD

            if (textoProcesado.length <= maxLen && regex.matches(textoProcesado)) {
                return textoProcesado
            }

            return null
        }
    }

    fun filtrarEntradaCoordenada(texto: String, esLatitud: Boolean): String? {
        return Companion.filtrarEntradaCoordenada(texto, esLatitud)
    }

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

    private val _latitudDraft = MutableStateFlow("")
    val latitudDraft = _latitudDraft.asStateFlow()
    fun setLatitudDraft(value: String) { _latitudDraft.value = value }

    private val _longitudDraft = MutableStateFlow("")
    val longitudDraft = _longitudDraft.asStateFlow()
    fun setLongitudDraft(value: String) { _longitudDraft.value = value }

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
        _latitudDraft.value = ""
        _longitudDraft.value = ""
    }

    // ===== VALIDACIÓN DE COORDENADAS =====
    fun validarLatitud(lat: Double?): String? {
        if (lat == null) return "Ingrese una latitud válida entre -90 y 90"
        if (!lat.isFinite()) return "La latitud debe ser un número válido"
        if (lat !in -90.0..90.0) return "La latitud debe estar entre -90 y 90"
        return null
    }

    fun validarLongitud(lng: Double?): String? {
        if (lng == null) return "Ingrese una longitud válida entre -180 y 180"
        if (!lng.isFinite()) return "La longitud debe ser un número válido"
        if (lng !in -180.0..180.0) return "La longitud debe estar entre -180 y 180"
        return null
    }

    fun validarCoordenadas(lat: Double?, lng: Double?): String? {
        if (lat == null || lng == null) return "Debe ingresar o seleccionar una ubicación válida"
        val errorLat = validarLatitud(lat)
        if (errorLat != null) return errorLat
        val errorLng = validarLongitud(lng)
        if (errorLng != null) return errorLng
        return null
    }

    fun estaFueraDeHonduras(lat: Double?, lng: Double?): Boolean {
        if (lat == null || lng == null) return false
        if (!lat.isFinite() || !lng.isFinite()) return false
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) return false
        return lat !in 13.0..16.0 || lng !in -89.0..-83.0
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
