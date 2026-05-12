package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.repository.VoluntarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ VIEWMODEL DE VOLUNTARIOS - MODO OFFLINE REPOSITORY
 */
class VoluntarioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = VoluntarioRepository(db.voluntarioDao())

    private val _voluntarios = MutableStateFlow<List<Voluntario>>(emptyList())
    val voluntarios: StateFlow<List<Voluntario>> = _voluntarios.asStateFlow()

    private val _voluntarioSeleccionado = MutableStateFlow<Voluntario?>(null)
    val voluntarioSeleccionado: StateFlow<Voluntario?> = _voluntarioSeleccionado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ===== ESTADO DEL FORMULARIO (PERSISTENCIA AL NAVEGAR) =====
    private val _tipoDocumentoDraft = MutableStateFlow("DNI")
    val tipoDocumentoDraft = _tipoDocumentoDraft.asStateFlow()
    fun setTipoDocumentoDraft(value: String) { _tipoDocumentoDraft.value = value }

    private val _nombreDraft = MutableStateFlow("")
    val nombreDraft = _nombreDraft.asStateFlow()
    fun setNombreDraft(value: String) { _nombreDraft.value = value }

    private val _dniDraft = MutableStateFlow("")
    val dniDraft = _dniDraft.asStateFlow()
    fun setDniDraft(value: String) { _dniDraft.value = value }

    private val _pasaporteDraft = MutableStateFlow("")
    val pasaporteDraft = _pasaporteDraft.asStateFlow()
    fun setPasaporteDraft(value: String) { _pasaporteDraft.value = value }

    private val _telefonoDraft = MutableStateFlow("")
    val telefonoDraft = _telefonoDraft.asStateFlow()
    fun setTelefonoDraft(value: String) { _telefonoDraft.value = value }

    private val _correoDraft = MutableStateFlow("")
    val correoDraft = _correoDraft.asStateFlow()
    fun setCorreoDraft(value: String) { _correoDraft.value = value }

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

    private val _tipoUsuarioDraft = MutableStateFlow("")
    val tipoUsuarioDraft = _tipoUsuarioDraft.asStateFlow()
    fun setTipoUsuarioDraft(value: String) { _tipoUsuarioDraft.value = value }

    private val _fechaNacimientoDraft = MutableStateFlow("")
    val fechaNacimientoDraft = _fechaNacimientoDraft.asStateFlow()
    fun setFechaNacimientoDraft(value: String) { _fechaNacimientoDraft.value = value }

    private val _generoDraft = MutableStateFlow("")
    val generoDraft = _generoDraft.asStateFlow()
    fun setGeneroDraft(value: String) { _generoDraft.value = value }

    private val _observacionesDraft = MutableStateFlow("")
    val observacionesDraft = _observacionesDraft.asStateFlow()
    fun setObservacionesDraft(value: String) { _observacionesDraft.value = value }

    // ===== ESTADO DE NAVEGACIÓN =====
    private val _subPantalla = MutableStateFlow("LISTA")
    val subPantalla: StateFlow<String> = _subPantalla.asStateFlow()

    fun setSubPantalla(valor: String) { _subPantalla.value = valor }
    fun resetSubPantalla() { _subPantalla.value = "LISTA" }

    init {
        cargarVoluntarios()
    }

    fun cargarVoluntarios() {
        viewModelScope.launch {
            _isLoading.value = true
            // Sync de fondo
            launch { repository.sincronizarDesdeNube() }
            
            repository.obtenerVoluntarios().collect { lista ->
                _voluntarios.value = lista
                _isLoading.value = false
            }
        }
    }

    suspend fun esPrimerUsuario(): Boolean {
        return repository.obtenerVoluntarios().first().isEmpty()
    }

    fun guardarVoluntario(
        voluntario: Voluntario,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.guardarVoluntario(voluntario)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al guardar voluntario")
            }
        }
    }

    suspend fun buscarPorFirebaseUid(uid: String): Voluntario? {
        return repository.obtenerPorUid(uid)
    }

    fun obtenerPorUid(uid: String) {
        viewModelScope.launch {
            _voluntarioSeleccionado.value = repository.obtenerPorUid(uid)
        }
    }

    suspend fun buscarYDescargarUsuario(uid: String): Voluntario? {
        // 1. Intentar local
        val local = repository.obtenerPorUid(uid)
        if (local != null) return local

        // 2. Intentar nube
        return repository.buscarEnNube(uid)
    }

    suspend fun buscarPorEmail(email: String): Voluntario? {
        return repository.obtenerPorEmail(email)
    }

    fun eliminarVoluntario(
        uid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.eliminarVoluntario(uid)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.message ?: "Error al eliminar voluntario")
            }
        }
    }

    fun actualizarVoluntario(
        voluntario: Voluntario,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        guardarVoluntario(voluntario, onSuccess, onError)
    }

    // ===== VALIDACIONES (RESTORED) =====
    fun validarNombre(valor: String): String? {
        if (valor.isBlank()) return "El nombre es obligatorio"
        if (valor.length < 3) return "Mínimo 3 caracteres"
        if (!valor.all { it.isLetter() || it.isWhitespace() }) return "Solo letras y espacios"
        return null
    }

    fun validarDNI(valor: String): String? {
        if (valor.isBlank()) return "El DNI es obligatorio"
        val cleanDni = valor.replace("-", "")
        if (cleanDni.length != 13) return "Debe tener 13 dígitos"
        if (!cleanDni.all { it.isDigit() }) return "Solo números"
        return null
    }

    fun validarPasaporte(valor: String): String? {
        if (valor.isBlank()) return "El pasaporte es obligatorio"
        if (valor.length < 6) return "Mínimo 6 caracteres"
        return null
    }

    fun validarTelefono(valor: String): String? {
        if (valor.isBlank()) return "El teléfono es obligatorio"
        val clean = valor.replace("-", "")
        if (clean.length != 8) return "Debe tener 8 dígitos"
        return null
    }

    fun validarCorreo(valor: String): String? {
        if (valor.isBlank()) return "El correo es obligatorio"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(valor).matches()) return "Correo inválido"
        return null
    }

    fun validarDireccion(valor: String): String? {
        if (valor.isBlank()) return "La dirección es obligatoria"
        return null
    }

    fun validarDepartamento(valor: String): String? {
        if (valor.isBlank()) return "Seleccione un departamento"
        return null
    }

    fun validarMunicipio(valor: String): String? {
        if (valor.isBlank()) return "Seleccione un municipio"
        return null
    }

    fun validarAldea(valor: String): String? {
        if (valor.isBlank()) return "La aldea es obligatoria"
        return null
    }

    fun validarCaserioBarrioColonia(valor: String): String? {
        if (valor.length > 50) return "Máximo 50 caracteres"
        return null
    }

    fun validarObservaciones(valor: String): String? {
        if (valor.length > 200) return "Máximo 200 caracteres"
        return null
    }

    fun validarTipoUsuario(tipo: String, soloAdmin: Boolean): String? {
        if (tipo.isBlank()) return "Seleccione un tipo de usuario"
        if (soloAdmin && tipo != "Administrador") return "Debe ser Administrador"
        return null
    }

    fun validarFechaNacimiento(fecha: String): String? {
        if (fecha.isBlank()) return "La fecha es obligatoria"
        return try {
            val edad = calcularEdad(fecha)
            if (edad < 18) "Debe ser mayor de 18 años" else null
        } catch (e: Exception) {
            "Formato inválido"
        }
    }

    fun validarGenero(valor: String): String? {
        if (valor.isBlank()) return "Seleccione un género"
        return null
    }

    fun calcularEdad(fechaNacimiento: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val nacimiento = sdf.parse(fechaNacimiento) ?: return 0
        val hoy = Calendar.getInstance()
        val cumple = Calendar.getInstance().apply { time = nacimiento }
        
        var edad = hoy.get(Calendar.YEAR) - cumple.get(Calendar.YEAR)
        if (hoy.get(Calendar.DAY_OF_YEAR) < cumple.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad
    }

    fun limpiarDNI(dni: String): String {
        return dni.replace("-", "").trim()
    }

    fun limpiarTelefono(telefono: String): String {
        return telefono.replace("-", "").replace(" ", "").trim()
    }

    suspend fun existeDNI(dni: String): Boolean {
        return repository.obtenerVoluntarios().first().any { it.cedula == dni }
    }

    suspend fun existePasaporte(pasaporte: String): Boolean {
        return repository.obtenerVoluntarios().first().any { it.pasaporte == pasaporte }
    }

    fun formatearPasaporte(input: String): String {
        return input.uppercase().filter { it.isLetterOrDigit() }.take(20)
    }

    fun limpiarSeleccion() {
        _voluntarioSeleccionado.value = null
    }

    fun limpiarDraft() {
        _nombreDraft.value = ""
        _dniDraft.value = ""
        _pasaporteDraft.value = ""
        _telefonoDraft.value = ""
        _correoDraft.value = ""
        _direccionDraft.value = ""
        _departamentoDraft.value = ""
        _municipioDraft.value = ""
        _aldeaDraft.value = ""
        _caserioDraft.value = ""
        _tipoUsuarioDraft.value = ""
        _fechaNacimientoDraft.value = ""
        _generoDraft.value = ""
        _tipoDocumentoDraft.value = "DNI"
        _observacionesDraft.value = ""
    }
}