package hn.unah.raindata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

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

                // Iniciar sesión en Firebase
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: ""

                _authState.value = AuthState.Success("Sesión iniciada correctamente")
                _isLoading.value = false
                onSuccess(uid)

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
                val errorMessage = if (e.message?.contains("network", ignoreCase = true) == true) {
                    "Sin conexión a internet. Verifica tu conexión"
                } else {
                    "Correo o contraseña incorrectos"
                }
                _authState.value = AuthState.Error(errorMessage)
                onError(errorMessage)
            }
        }
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