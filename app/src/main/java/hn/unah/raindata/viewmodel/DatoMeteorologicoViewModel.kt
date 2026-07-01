package hn.unah.raindata.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.repository.DatoMeteorologicoRepository
import hn.unah.raindata.data.sync.SyncWorker
import hn.unah.raindata.data.utils.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.Job
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
        observarConectividad()
    }

    /**
     * Al recuperar conexión:
     * 1. Sincroniza inmediatamente en el scope del ViewModel (funciona si la app está en primer plano).
     * 2. Encola un OneTimeWorkRequest con restricción de red para garantizar la sincronización
     *    incluso si la app va al fondo justo después de reconectarse (proceso podría morir).
     */
    private fun observarConectividad() {
        viewModelScope.launch {
            // drop(1): ignoramos el estado inicial para solo reaccionar
            // a CAMBIOS reales de red (offline → online)
            NetworkMonitor.isOnline.drop(1).collect { online ->
                if (online) {
                    android.util.Log.d("DatoMeteorologicoVM", "🌐 Online – verificando sesión Firebase Auth...")

                    // Garantizar que Firebase Auth tenga sesión activa antes de sincronizar.
                    // Puede ser null si el usuario inició sesión en modo offline y la
                    // re-autenticación en background de AuthViewModel aún no completó.
                    val authOk = garantizarSesionFirebase()
                    if (!authOk) {
                        android.util.Log.e("DatoMeteorologicoVM", "❌ No se pudo restablecer sesión Firebase. Sincronización cancelada.")
                        return@collect
                    }

                    android.util.Log.d("DatoMeteorologicoVM", "📋 Sesión OK. Iniciando sincronización inmediata...")

                    // --- Vía 1: sincronización directa en este scope (primer plano) ---
                    launch { repository.sincronizarPendientesLocal() }

                    // --- Vía 2: OneTimeWorkRequest de respaldo (segundo plano) ---
                    // Garantiza la sincronización aunque la app muera antes de que
                    // el scope anterior termine. WorkManager persiste la tarea.
                    val context = getApplication<Application>()
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val oneShotSync = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "ImmediateSyncOnReconnect",
                        ExistingWorkPolicy.REPLACE,
                        oneShotSync
                    )
                    android.util.Log.d("DatoMeteorologicoVM", "📋 OneTimeWorkRequest encolado (ImmediateSyncOnReconnect)")
                }
            }
        }
    }

    /**
     * Verifica que FirebaseAuth.currentUser no sea null.
     * Si es null (sesión offline sin Firebase Auth activo), intenta re-autenticar
     * usando las credenciales guardadas en EncryptedSharedPreferences.
     *
     * @return true si hay sesión válida (ya exístía o se restauró exitosamente).
     *         false si currentUser es null Y la re-autenticación falló.
     */
    private suspend fun garantizarSesionFirebase(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            android.util.Log.d("DatoMeteorologicoVM", "🔒 Firebase Auth activo: ${currentUser.uid} (${currentUser.email})")
            return true
        }

        android.util.Log.w("DatoMeteorologicoVM", "⚠️ currentUser es null. Intentando re-autenticación con credenciales guardadas...")

        return try {
            val context = getApplication<Application>()

            // Leer credenciales del mismo EncryptedSharedPreferences que usa AuthViewModel
            val prefs = try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    "auth_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Mismo fallback que AuthViewModel
                context.getSharedPreferences("auth_prefs_fallback", Context.MODE_PRIVATE)
            }

            val rememberMe = prefs.getBoolean("remember_me", false)
            val email = prefs.getString("remember_email", "") ?: ""
            val pass  = prefs.getString("remember_pass",  "") ?: ""

            if (!rememberMe || email.isBlank() || pass.isBlank()) {
                android.util.Log.w("DatoMeteorologicoVM", "⚠️ No hay credenciales guardadas para re-autenticación. " +
                    "El usuario deberá iniciar sesión manualmente.")
                return false
            }

            val result = withContext(Dispatchers.IO) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass).await()
            }
            val uid = result.user?.uid
            android.util.Log.d("DatoMeteorologicoVM", "✅ Re-autenticación exitosa. UID: $uid")
            uid != null

        } catch (e: Exception) {
            android.util.Log.e("DatoMeteorologicoVM", "❌ Error en re-autenticación: ${e.message}", e)
            false
        }
    }

    // Job del collect activo. Se cancela antes de iniciar uno nuevo.
    private var collectJob: Job? = null

    fun cargarTodosDatos() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _isLoading.value = true
            // Sync de fondo solo si estamos online
            if (NetworkMonitor.isOnline.value) {
                launch { repository.sincronizarDesdeNube() }
            }
            repository.obtenerTodos().collect { lista ->
                _datosMeteorologicos.value = lista
                _isLoading.value = false
            }
        }
    }

    fun cargarDatosPorVoluntario(uid: String) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            _isLoading.value = true
            // Sync de fondo solo si estamos online
            if (NetworkMonitor.isOnline.value) {
                launch { repository.sincronizarDesdeNube() }
            }
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

    /**
     * Sincronización manual disparada por el botón de la UI.
     * Sube todos los datos pendientes y luego descarga los remotos.
     */
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun sincronizarManual(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.sincronizarPendientesLocal()
                repository.sincronizarDesdeNube()
                android.util.Log.d("DatoMeteorologicoVM", "✅ Sincronización manual completada")
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("DatoMeteorologicoVM", "❌ Error en sincronización manual", e)
                onError(e.message ?: "Error al sincronizar")
            } finally {
                _isSyncing.value = false
            }
        }
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
                repository.guardarDato(dato, getApplication())
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
                repository.guardarDato(dato, getApplication())
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