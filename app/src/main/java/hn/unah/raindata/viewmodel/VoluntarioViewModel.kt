package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import hn.unah.raindata.data.database.entities.Voluntario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ VOLUNTARIO VIEWMODEL - MIGRADO A FIREBASE FIRESTORE
 * - Usa StateFlow en lugar de LiveData
 * - Listeners en tiempo real
 * - Sincronización en la nube
 */
class VoluntarioViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val voluntariosCollection = firestore.collection("voluntarios")

    // ===== ESTADOS CON STATEFLOW =====
    private val _voluntarios = MutableStateFlow<List<Voluntario>>(emptyList())
    val voluntarios: StateFlow<List<Voluntario>> = _voluntarios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var voluntariosListener: ListenerRegistration? = null

    init {
        cargarVoluntarios()
    }

    // ===== LISTENER EN TIEMPO REAL =====
    fun cargarVoluntarios() {
        // Cancelar listener anterior si existe
        voluntariosListener?.remove()

        _isLoading.value = true

        voluntariosListener = voluntariosCollection
            .orderBy("fecha_creacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    _errorMessage.value = "Error al cargar voluntarios: ${error.message}"
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Voluntario::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                _voluntarios.value = lista
            }
    }

    // ===== GUARDAR VOLUNTARIO =====
    fun guardarVoluntario(voluntario: Voluntario, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Verificar si es el primer usuario
                val esPrimerUsuario = verificarSiEsPrimerUsuario()

                // Determinar estado de aprobación
                val estadoAprobacion = when {
                    esPrimerUsuario -> "Aprobado" // ✅ Primer usuario = Admin automático
                    voluntario.tipo_usuario == "Administrador" -> "Pendiente" // ⏳ Admin requiere aprobación
                    else -> "Aprobado" // ✅ Voluntario/Observador aprobados automáticamente
                }

                // Determinar tipo de usuario si es el primero
                val tipoUsuario = if (esPrimerUsuario) "Administrador" else voluntario.tipo_usuario

                // Crear voluntario actualizado
                val voluntarioFinal = voluntario.copy(
                    firebase_uid = voluntario.firebase_uid.ifBlank { auth.currentUser?.uid ?: UUID.randomUUID().toString() },
                    tipo_usuario = tipoUsuario,
                    estado_aprobacion = estadoAprobacion,
                    visto_por_admin = estadoAprobacion == "Aprobado"
                )

                // Guardar en Firestore
                voluntariosCollection
                    .document(voluntarioFinal.firebase_uid)
                    .set(voluntarioFinal)
                    .await()

                // ✅ NUEVO: ENVIAR EMAIL SI ES VOLUNTARIO U OBSERVADOR APROBADO AUTOMÁTICAMENTE
                if (estadoAprobacion == "Aprobado" && tipoUsuario != "Administrador") {
                    // Enviar email de bienvenida en segundo plano
                    launch {
                        hn.unah.raindata.data.email.EmailService.enviarEmailConCallback(
                            tipo = when (tipoUsuario) {
                                "Voluntario" -> hn.unah.raindata.data.email.EmailService.TipoEmail.BIENVENIDA_VOLUNTARIO
                                "Observador" -> hn.unah.raindata.data.email.EmailService.TipoEmail.BIENVENIDA_OBSERVADOR
                                else -> hn.unah.raindata.data.email.EmailService.TipoEmail.BIENVENIDA_VOLUNTARIO
                            },
                            destinatario = voluntarioFinal.email,
                            nombreUsuario = voluntarioFinal.nombre,
                            onSuccess = {
                                android.util.Log.d("VoluntarioViewModel", "✅ Email de bienvenida enviado a ${voluntarioFinal.email}")
                            },
                            onError = { error ->
                                android.util.Log.e("VoluntarioViewModel", "❌ Error al enviar email: $error")
                                // No bloquea el registro si falla el email
                            }
                        )
                    }
                }

                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                onError("Error al guardar: ${e.message}")
            }
        }
    }

    // ===== VERIFICAR SI ES EL PRIMER USUARIO =====
    private suspend fun verificarSiEsPrimerUsuario(): Boolean {
        return try {
            val snapshot = voluntariosCollection
                .whereEqualTo("tipo_usuario", "Administrador")
                .whereEqualTo("estado_aprobacion", "Aprobado")
                .get()
                .await()

            snapshot.isEmpty // Si está vacío, es el primer usuario
        } catch (e: Exception) {
            false
        }
    }

    // ===== ACTUALIZAR VOLUNTARIO =====
    fun actualizarVoluntario(voluntario: Voluntario, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                voluntariosCollection
                    .document(voluntario.firebase_uid)
                    .set(voluntario)
                    .await()

                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                onError("Error al actualizar: ${e.message}")
            }
        }
    }

    // ===== ELIMINAR VOLUNTARIO =====
    fun eliminarVoluntario(firebaseUid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                voluntariosCollection
                    .document(firebaseUid)
                    .delete()
                    .await()

                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                onError("Error al eliminar: ${e.message}")
            }
        }
    }

    // ===== BUSCAR VOLUNTARIOS =====
    fun buscarVoluntarios(termino: String) {
        if (termino.isBlank()) {
            cargarVoluntarios()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val snapshot = voluntariosCollection.get().await()

                val resultados = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Voluntario::class.java)
                }.filter { voluntario ->
                    voluntario.nombre.contains(termino, ignoreCase = true) ||
                            voluntario.cedula?.contains(termino, ignoreCase = true) == true ||
                            voluntario.email.contains(termino, ignoreCase = true) ||
                            voluntario.departamento.contains(termino, ignoreCase = true) ||
                            voluntario.municipio.contains(termino, ignoreCase = true)
                }

                _voluntarios.value = resultados
                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error en búsqueda: ${e.message}"
            }
        }
    }

    // ===== VERIFICAR SI DNI EXISTE =====
    suspend fun existeDNI(dni: String): Boolean {
        return try {
            val snapshot = voluntariosCollection
                .whereEqualTo("cedula", dni)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // ===== VERIFICAR SI PASAPORTE EXISTE =====
    suspend fun existePasaporte(pasaporte: String): Boolean {
        return try {
            val snapshot = voluntariosCollection
                .whereEqualTo("pasaporte", pasaporte)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // ===== BUSCAR POR CÉDULA =====
    suspend fun buscarPorCedula(cedula: String): Voluntario? {
        return try {
            val snapshot = voluntariosCollection
                .whereEqualTo("cedula", cedula)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(Voluntario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ===== LIMPIAR ERRORES =====
    fun clearError() {
        _errorMessage.value = null
    }

    // ===== DETENER LISTENERS AL DESTRUIR =====
    override fun onCleared() {
        super.onCleared()
        voluntariosListener?.remove()
    }

    // ========== FUNCIONES DE VALIDACIÓN (SIN CAMBIOS) ==========

    fun validarNombre(nombre: String): String? {
        if (nombre.isBlank()) return "El nombre es obligatorio"
        if (nombre.length > 40) return "El nombre no puede exceder 40 caracteres"

        val regex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
        if (!regex.matches(nombre)) {
            return "El nombre solo puede contener letras y espacios"
        }

        return null
    }

    fun validarDNI(dni: String): String? {
        if (dni.isBlank()) return null

        val dniLimpio = dni.replace("-", "")

        if (dniLimpio.length != 13) return "DNI incompleto (formato: XXXX-XXXX-XXXXX)"

        if (!dniLimpio.all { it.isDigit() }) {
            return "El DNI solo puede contener números"
        }

        if (!dni.matches(Regex("^\\d{4}-\\d{4}-\\d{5}$"))) {
            return "Formato inválido (ejemplo: 0708-2005-00276)"
        }

        val departamento = dni.substring(0, 2).toIntOrNull() ?: return "Departamento inválido"
        val municipio = dni.substring(2, 4).toIntOrNull() ?: return "Municipio inválido"
        val anio = dni.substring(5, 9).toIntOrNull() ?: return "Año inválido"

        if (departamento !in 1..18) {
            return "Departamento inválido (01-18)"
        }

        val municipiosPorDepartamento = mapOf(
            1 to 8, 2 to 10, 3 to 21, 4 to 23, 5 to 12, 6 to 16,
            7 to 19, 8 to 28, 9 to 6, 10 to 17, 11 to 4, 12 to 19,
            13 to 28, 14 to 16, 15 to 23, 16 to 28, 17 to 9, 18 to 11
        )

        val limiteMunicipios = municipiosPorDepartamento[departamento] ?: return "Departamento inválido"

        if (municipio !in 1..limiteMunicipios) {
            return "Municipio inválido para este departamento (01-${String.format("%02d", limiteMunicipios)})"
        }

        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        if (anio !in 1900..anioActual) {
            return "Año inválido (1900-$anioActual)"
        }

        return null
    }

    fun validarPasaporte(pasaporte: String): String? {
        if (pasaporte.isBlank()) return null

        if (pasaporte.length < 6) {
            return "El pasaporte debe tener al menos 6 caracteres"
        }
        if (pasaporte.length > 20) {
            return "El pasaporte no puede exceder 20 caracteres"
        }

        val regex = Regex("^[A-Z0-9]+$")
        if (!regex.matches(pasaporte.uppercase())) {
            return "El pasaporte solo puede contener letras y números (sin espacios)"
        }

        if (!pasaporte.any { it.isLetter() }) {
            return "El pasaporte debe contener al menos una letra"
        }
        if (!pasaporte.any { it.isDigit() }) {
            return "El pasaporte debe contener al menos un número"
        }

        return null
    }

    fun formatearPasaporte(input: String): String {
        return input.uppercase().replace(" ", "").filter { it.isLetterOrDigit() }.take(20)
    }

    fun validarFechaNacimiento(fecha: String): String? {
        if (fecha.isBlank()) return null

        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        if (!regex.matches(fecha)) {
            return "Formato inválido (debe ser AAAA-MM-DD)"
        }

        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formato.isLenient = false
            val fechaNacimiento = formato.parse(fecha) ?: return "Fecha inválida"

            val calendar = Calendar.getInstance()
            calendar.time = fechaNacimiento

            val hoy = Calendar.getInstance()
            if (calendar.after(hoy)) {
                return "La fecha de nacimiento no puede ser futura"
            }

            val edadMinima = Calendar.getInstance()
            edadMinima.add(Calendar.YEAR, -18)
            if (calendar.after(edadMinima)) {
                return "Debe ser mayor de 18 años"
            }

            val edadMaxima = Calendar.getInstance()
            edadMaxima.add(Calendar.YEAR, -100)
            if (calendar.before(edadMaxima)) {
                return "Fecha de nacimiento muy antigua (máximo 100 años)"
            }

            null
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }

    fun calcularEdad(fecha: String): Int? {
        if (fecha.isBlank()) return null

        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaNacimiento = formato.parse(fecha) ?: return null

            val nacimiento = Calendar.getInstance()
            nacimiento.time = fechaNacimiento

            val hoy = Calendar.getInstance()

            var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)

            if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--
            }

            edad
        } catch (e: Exception) {
            null
        }
    }

    fun validarGenero(genero: String): String? {
        if (genero.isBlank()) return null

        val generosValidos = listOf("Masculino", "Femenino", "Otro")
        if (genero !in generosValidos) {
            return "Género no válido"
        }

        return null
    }

    fun formatearDNI(input: String): String {
        val digitos = input.filter { it.isDigit() }
        val limitado = digitos.take(13)

        return when (limitado.length) {
            in 0..4 -> limitado
            in 5..8 -> "${limitado.substring(0, 4)}-${limitado.substring(4)}"
            else -> "${limitado.substring(0, 4)}-${limitado.substring(4, 8)}-${limitado.substring(8)}"
        }
    }

    fun limpiarDNI(dni: String): String {
        return dni.replace("-", "")
    }

    fun validarTelefono(telefono: String): String? {
        if (telefono.isBlank()) return null

        val telefonoLimpio = telefono.replace("-", "")

        if (telefonoLimpio.length != 8) {
            return "El teléfono debe tener 8 dígitos"
        }

        if (!telefonoLimpio.all { it.isDigit() }) {
            return "El teléfono solo puede contener números"
        }

        val primerDigito = telefonoLimpio.first()
        if (primerDigito !in listOf('2', '3', '8', '9')) {
            return "El teléfono debe empezar con 2, 3, 8 o 9"
        }

        return null
    }

    fun formatearTelefono(input: String): String {
        val digitos = input.filter { it.isDigit() }
        val limitado = digitos.take(8)

        return when (limitado.length) {
            in 0..4 -> limitado
            else -> "${limitado.substring(0, 4)}-${limitado.substring(4)}"
        }
    }

    fun limpiarTelefono(telefono: String): String {
        return telefono.replace("-", "")
    }

    fun validarCorreo(correo: String): String? {
        if (correo.isBlank()) return null

        if (correo.length > 50) {
            return "El correo no puede exceder 50 caracteres"
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(correo)) {
            return "Formato de correo inválido"
        }

        return null
    }

    fun validarDireccion(direccion: String): String? {
        if (direccion.isBlank()) return "La dirección es obligatoria"
        if (direccion.length > 100) return "La dirección no puede exceder 100 caracteres"
        return null
    }

    fun validarDepartamento(departamento: String): String? {
        if (departamento.isBlank()) return "El departamento es obligatorio"
        if (departamento.length > 30) return "El departamento no puede exceder 30 caracteres"
        return null
    }

    fun validarMunicipio(municipio: String): String? {
        if (municipio.isBlank()) return "El municipio es obligatorio"
        if (municipio.length > 30) return "El municipio no puede exceder 30 caracteres"
        return null
    }

    fun validarAldea(aldea: String): String? {
        if (aldea.isBlank()) return "La aldea es obligatoria"
        if (aldea.length > 15) return "La aldea no puede exceder 15 caracteres"
        return null
    }

    fun validarCaserioBarrioColonia(valor: String): String? {
        if (valor.isBlank()) return null
        if (valor.length > 15) return "No puede exceder 15 caracteres"
        return null
    }

    fun validarObservaciones(observaciones: String): String? {
        if (observaciones.isBlank()) return null

        if (observaciones.length < 10) {
            return "Las observaciones deben tener al menos 10 caracteres"
        }

        return null
    }

    fun validarTipoUsuario(tipoUsuario: String, soloAdministrador: Boolean): String? {
        if (soloAdministrador) return null

        if (tipoUsuario.isBlank()) {
            return "Debe seleccionar un tipo de usuario"
        }

        return null
    }

    suspend fun buscarPorFirebaseUid(firebaseUid: String): Voluntario? {
        return try {
            val snapshot = voluntariosCollection
                .document(firebaseUid)
                .get()
                .await()

            snapshot.toObject(Voluntario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ===== VERIFICAR SI ES EL PRIMER USUARIO (SUSPENDIDA PARA USO DIRECTO) =====
    suspend fun esPrimerUsuario(): Boolean {
        return try {
            val snapshot = voluntariosCollection
                .whereEqualTo("tipo_usuario", "Administrador")
                .whereEqualTo("estado_aprobacion", "Aprobado")
                .get()
                .await()

            snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}