package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val context = application.applicationContext

    // ===== ENCRYPTED PREFERENCES PARA RECUÉRDAME =====
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val db = hn.unah.raindata.data.database.AppDatabase.getDatabase(application)
    private val voluntarioRepository = hn.unah.raindata.data.repository.VoluntarioRepository(db.voluntarioDao())

    // Estados para UI
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Verificar si hay usuario logueado
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    // REGISTRO DE USUARIO
    fun registrarUsuario(
        email: String,
        password: String,
        onSuccess: (String) -> Unit, // Devuelve el UID de Firebase
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Validaciones básicas
                if (email.isBlank()) {
                    onError("El correo electrónico es obligatorio")
                    _isLoading.value = false
                    return@launch
                }

                if (!isValidEmail(email)) {
                    onError("Formato de correo electrónico inválido")
                    _isLoading.value = false
                    return@launch
                }

                if (password.length < 6) {
                    onError("La contraseña debe tener al menos 6 caracteres")
                    _isLoading.value = false
                    return@launch
                }

                // Crear usuario en Firebase
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: ""

                _authState.value = AuthState.Success("Usuario registrado exitosamente")
                _isLoading.value = false
                onSuccess(uid)

            } catch (e: FirebaseAuthException) {
                _isLoading.value = false
                val errorMessage = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo electrónico ya está registrado"
                    "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil"
                    "ERROR_INVALID_EMAIL" -> "Formato de correo electrónico inválido"
                    else -> "Error al registrar: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
                onError(errorMessage)
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMessage = "Error inesperado: ${e.message}"
                _authState.value = AuthState.Error(errorMessage)
                onError(errorMessage)
            }
        }
    }

    // LOGIN
    fun iniciarSesion(
        email: String,
        password: String,
        recordame: Boolean,
        onSuccess: (String) -> Unit, // Devuelve el UID de Firebase
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Validaciones
                if (email.isBlank() || password.isBlank()) {
                    onError("Por favor complete todos los campos")
                    _isLoading.value = false
                    return@launch
                }

                // Intentar Iniciar sesión en Firebase
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    val uid = result.user?.uid ?: ""

                    // GUARDAR CREDENCIALES SI MARCÓ RECUÉRDAME
                    if (recordame) {
                        guardarCredenciales(email, password)
                    } else {
                        borrarCredenciales()
                    }

                    _authState.value = AuthState.Success("Sesión iniciada correctamente")
                    _isLoading.value = false
                    onSuccess(uid)

                } catch (e: Exception) {
                    // SI FALLA POR RED, INTENTAR LOGIN OFFLINE
                    val isNetworkError = e.message?.contains("network", ignoreCase = true) == true || 
                                       e is com.google.firebase.FirebaseNetworkException ||
                                       e is com.google.firebase.firestore.FirebaseFirestoreException

                    if (isNetworkError) {
                        val (savedEmail, savedPass, isRemembered) = obtenerCredencialesGuardadas()
                        
                        if (isRemembered && email == savedEmail && password == savedPass) {
                            // Credenciales coinciden localmente, buscar perfil en Room
                            val voluntario = voluntarioRepository.obtenerPorEmail(email)
                            if (voluntario != null) {
                                _authState.value = AuthState.Success("Sesión iniciada (Modo Offline)")
                                _isLoading.value = false
                                onSuccess(voluntario.firebase_uid)
                                return@launch
                            }
                        }
                        
                        val offlineError = if (isRemembered) {
                            "Sin conexión. El perfil no está disponible offline aún."
                        } else {
                            "Sin conexión. Inicia sesión con internet al menos una vez para usar el modo offline."
                        }
                        _isLoading.value = false
                        _authState.value = AuthState.Error(offlineError)
                        onError(offlineError)
                    } else {
                        // Es un error real de credenciales o Firebase
                        throw e
                    }
                }

            } catch (e: FirebaseAuthException) {
                _isLoading.value = false
                val errorMessage = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Formato de correo electrónico inválido"
                    "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
                    "ERROR_USER_NOT_FOUND" -> "Este correo no está registrado en el sistema"
                    "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
                    "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos fallidos. Intente más tarde"
                    "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos"
                    else -> "Error al iniciar sesión. Verifica tus credenciales"
                }
                _authState.value = AuthState.Error(errorMessage)
                onError(errorMessage)
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMessage = "Error: ${e.message ?: "Verifique sus credenciales"}"
                _authState.value = AuthState.Error(errorMessage)
                onError(errorMessage)
            }
        }
    }

    // ===== MÉTODOS RECUÉRDAME =====
    private fun guardarCredenciales(email: String, pass: String) {
        sharedPreferences.edit().apply {
            putString("remember_email", email)
            putString("remember_pass", pass)
            putBoolean("remember_me", true)
            apply()
        }
    }

    fun borrarCredenciales() {
        sharedPreferences.edit().apply {
            remove("remember_email")
            remove("remember_pass")
            putBoolean("remember_me", false)
            apply()
        }
    }

    fun obtenerCredencialesGuardadas(): Triple<String, String, Boolean> {
        val email = sharedPreferences.getString("remember_email", "") ?: ""
        val pass = sharedPreferences.getString("remember_pass", "") ?: ""
        val rememberMe = sharedPreferences.getBoolean("remember_me", false)
        return Triple(email, pass, rememberMe)
    }

    // RECUPERAR CONTRASEÑA
    fun recuperarPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                if (email.isBlank()) {
                    onError("Por favor ingrese su correo electrónico")
                    _isLoading.value = false
                    return@launch
                }

                if (!isValidEmail(email)) {
                    onError("Formato de correo electrónico inválido")
                    _isLoading.value = false
                    return@launch
                }

                // Enviar email de recuperación
                auth.sendPasswordResetEmail(email).await()

                _authState.value = AuthState.Success("Email de recuperación enviado")
                _isLoading.value = false
                onSuccess()

            } catch (e: FirebaseAuthException) {
                _isLoading.value = false
                val errorMessage = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Formato de correo electrónico inválido"
                    "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo"
                    else -> "Error al enviar email: ${e.message}"
                }
                _authState.value = AuthState.Error(errorMessage)
                onError(errorMessage)
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMessage = "Error inesperado: ${e.message}"
                _authState.value = AuthState.Error(errorMessage)
                onError(errorMessage)
            }
        }
    }

    // CERRAR SESIÓN
    fun cerrarSesion() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    // ✅ ELIMINAR CUENTA ACTUAL
    fun eliminarCuentaActual(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser

        if (user != null) {
            user.delete()
                .addOnSuccessListener {
                    _authState.value = AuthState.Idle
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onError(exception.message ?: "Error al eliminar cuenta")
                }
        } else {
            onError("No hay usuario autenticado")
        }
    }

    // VALIDACIÓN DE EMAIL
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailPattern.toRegex())
    }

    // Resetear estado
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

// Estados de autenticación
sealed class AuthState {
    object Idle : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}