package hn.unah.raindata.ui.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.ui.components.*
import hn.unah.raindata.ui.theme.RainDataColors
import hn.unah.raindata.viewmodel.AuthViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 📝 PANTALLA DE REGISTRO MEJORADA - REVOCLIMAP
 * CAMBIOS APLICADOS:
 * ✅ LOGOS ELIMINADOS (no aparecen aquí)
 * ✅ "REVOCLIMAP" ELIMINADO (no aparece aquí)
 * ✅ Botones sociales ELIMINADOS
 * ✅ Validación de nombre: NO acepta números
 * ✅ Responsividad mejorada en textos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    authViewModel: AuthViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    onRegistroExitoso: (String, String) -> Unit = { _, _ -> },
    onNavigateToLogin: () -> Unit = {},
    esPrimerUsuario: Boolean = false
) {
    // 🔧 Estados
    var nombreCompleto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by authViewModel.isLoading.collectAsState()

    // Validaciones en tiempo real
    val passwordsCoinciden = password == confirmarPassword && confirmarPassword.isNotEmpty()
    val passwordEsValida = password.length >= 6
    val emailEsValido = email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) && !email.endsWith(".")
    // ✅ NUEVO: Validar que el nombre NO tenga números
    val nombreEsValido = nombreCompleto.isNotBlank() && !nombreCompleto.any { it.isDigit() }

    // 📱 Responsividad
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val isSmallPhone = configuration.screenWidthDp < 360

    // ✨ Animaciones
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val buttonScale = remember { Animatable(1f) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = RainDataColors.GradienteFondoRegistro)
                .padding(padding)
        ) {
            // Fondo animado
            FloatingBackgroundCircles(
                colors = listOf(
                    RainDataColors.VerdeAcento.copy(alpha = 0.3f),
                    RainDataColors.Amarillo.copy(alpha = 0.25f),
                    RainDataColors.AzulCielo.copy(alpha = 0.2f)
                )
            )

            // Layout adaptativo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = when {
                            isTablet -> 80.dp
                            isSmallPhone -> 20.dp
                            else -> 24.dp
                        },
                        vertical = 24.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RegistroFormPanel(
                    modifier = Modifier.fillMaxWidth(),
                    nombreCompleto = nombreCompleto,
                    email = email,
                    password = password,
                    confirmarPassword = confirmarPassword,
                    passwordVisible = passwordVisible,
                    confirmarPasswordVisible = confirmarPasswordVisible,
                    showError = showError,
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    visible = visible,
                    buttonScale = buttonScale,
                    passwordsCoinciden = passwordsCoinciden,
                    passwordEsValida = passwordEsValida,
                    emailEsValido = emailEsValido,
                    nombreEsValido = nombreEsValido,
                    esPrimerUsuario = esPrimerUsuario,
                    isSmallPhone = isSmallPhone,
                    onNombreChange = {
                        // Filtrar números al escribir
                        val sinNumeros = it.filter { char -> !char.isDigit() }
                        nombreCompleto = sinNumeros
                        showError = false
                    },
                    onEmailChange = { email = it; showError = false },
                    onPasswordChange = { password = it; showError = false },
                    onConfirmarPasswordChange = { confirmarPassword = it; showError = false },
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    onConfirmarPasswordVisibilityToggle = { confirmarPasswordVisible = !confirmarPasswordVisible },
                    onRegistro = {
                        handleRegistro(
                            nombreCompleto = nombreCompleto,
                            email = email,
                            password = password,
                            emailEsValido = emailEsValido,
                            passwordEsValida = passwordEsValida,
                            passwordsCoinciden = passwordsCoinciden,
                            nombreEsValido = nombreEsValido,
                            buttonScale = buttonScale,
                            scope = scope,
                            authViewModel = authViewModel,
                            onRegistroExitoso = onRegistroExitoso,
                            onError = { error -> errorMessage = error; showError = true }
                        )
                    },
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}

/**
 * 📝 PANEL DE FORMULARIO DE REGISTRO (SIN LOGOS, SIN REVOCLIMAP, SIN BOTONES SOCIALES)
 */
@Composable
fun RegistroFormPanel(
    modifier: Modifier = Modifier,
    nombreCompleto: String,
    email: String,
    password: String,
    confirmarPassword: String,
    passwordVisible: Boolean,
    confirmarPasswordVisible: Boolean,
    showError: Boolean,
    errorMessage: String,
    isLoading: Boolean,
    visible: Boolean,
    buttonScale: Animatable<Float, AnimationVector1D>,
    passwordsCoinciden: Boolean,
    passwordEsValida: Boolean,
    emailEsValido: Boolean,
    nombreEsValido: Boolean,
    esPrimerUsuario: Boolean,
    isSmallPhone: Boolean,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmarPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmarPasswordVisibilityToggle: () -> Unit,
    onRegistro: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(800)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Card(
            modifier = modifier.wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = RainDataColors.FondoCard
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isSmallPhone) 20.dp else 32.dp,
                        vertical = 28.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ SIN LOGOS, SIN REVOCLIMAP - Solo título directo
                Text(
                    text = if (esPrimerUsuario) "Crear administrador" else "Crear cuenta",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = RainDataColors.VerdePrincipal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (esPrimerUsuario)
                        "Configuración inicial del sistema"
                    else
                        "Completa tus datos para registrarte",
                    fontSize = 13.sp,
                    color = RainDataColors.TextoSecundario,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Nombre Completo (SIN NÚMEROS)
                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = onNombreChange,
                    label = {
                        Text(
                            "Nombre completo",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    },
                    placeholder = {
                        Text(
                            "Juan Pérez González",
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        if (nombreCompleto.isNotEmpty()) {
                            Icon(
                                if (nombreEsValido) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (nombreEsValido) RainDataColors.VerdeValidacion else RainDataColors.RojoError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RainDataColors.TextoPrincipal,
                        unfocusedTextColor = RainDataColors.TextoPrincipal,
                        cursorColor = RainDataColors.VerdePrincipal,
                        focusedBorderColor = RainDataColors.VerdePrincipal,
                        unfocusedBorderColor = RainDataColors.GrisMedio,
                        focusedLabelColor = RainDataColors.VerdePrincipal,
                        unfocusedLabelColor = RainDataColors.TextoSecundario,
                        focusedContainerColor = RainDataColors.Blanco,
                        unfocusedContainerColor = RainDataColors.Blanco
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp,
                        color = RainDataColors.TextoPrincipal
                    )
                )

                // Mensaje de validación de nombre
                if (nombreCompleto.isNotEmpty() && !nombreEsValido) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = RainDataColors.RojoError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "El nombre no puede contener números",
                                fontSize = 12.sp,
                                color = RainDataColors.RojoError,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Email con validación visual
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = {
                        Text(
                            "Correo electrónico",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    },
                    placeholder = {
                        Text(
                            "correo@ejemplo.com",
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        if (email.isNotEmpty()) {
                            Icon(
                                if (emailEsValido) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (emailEsValido) RainDataColors.VerdeValidacion else RainDataColors.RojoError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RainDataColors.TextoPrincipal,
                        unfocusedTextColor = RainDataColors.TextoPrincipal,
                        cursorColor = RainDataColors.VerdePrincipal,
                        focusedBorderColor = RainDataColors.VerdePrincipal,
                        unfocusedBorderColor = RainDataColors.GrisMedio,
                        focusedLabelColor = RainDataColors.VerdePrincipal,
                        unfocusedLabelColor = RainDataColors.TextoSecundario,
                        focusedContainerColor = RainDataColors.Blanco,
                        unfocusedContainerColor = RainDataColors.Blanco
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp,
                        color = RainDataColors.TextoPrincipal
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Contraseña con validación
                PasswordFieldWithValidation(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña",
                    placeholder = "Mínimo 6 caracteres",
                    passwordVisible = passwordVisible,
                    onVisibilityToggle = onPasswordVisibilityToggle,
                    isValid = passwordEsValida,
                    showValidation = password.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Confirmar Contraseña
                PasswordFieldWithValidation(
                    value = confirmarPassword,
                    onValueChange = onConfirmarPasswordChange,
                    label = "Confirmar contraseña",
                    placeholder = "Reingrese contraseña",
                    passwordVisible = confirmarPasswordVisible,
                    onVisibilityToggle = onConfirmarPasswordVisibilityToggle,
                    isValid = passwordsCoinciden,
                    showValidation = confirmarPassword.isNotEmpty(),
                    validationMessage = if (!passwordsCoinciden && confirmarPassword.isNotEmpty())
                        "Las contraseñas no coinciden"
                    else null
                )

                // Card de error
                ErrorCard(
                    message = errorMessage,
                    visible = showError
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de registro
                RevoclimapButton(
                    text = if (esPrimerUsuario) "Crear admin" else "Registrarse",
                    onClick = onRegistro,
                    enabled = !isLoading,
                    isLoading = isLoading,
                    loadingText = "Registrando...",
                    icon = Icons.Default.PersonAdd,
                    buttonScale = buttonScale
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ SIN BOTONES SOCIALES - Divider eliminado

                // Enlace a login
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        fontSize = 14.sp,
                        color = RainDataColors.TextoSecundario
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RainDataColors.VerdePrincipal
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🔐 CAMPO DE CONTRASEÑA CON VALIDACIÓN
 */
@Composable
fun PasswordFieldWithValidation(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    passwordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    isValid: Boolean,
    showValidation: Boolean,
    validationMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    label,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            },
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = RainDataColors.VerdeSecundario,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showValidation) {
                        Icon(
                            if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isValid) RainDataColors.VerdeValidacion else RainDataColors.RojoError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(onClick = onVisibilityToggle) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario
                        )
                    }
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = RainDataColors.TextoPrincipal,
                unfocusedTextColor = RainDataColors.TextoPrincipal,
                cursorColor = RainDataColors.VerdePrincipal,
                focusedBorderColor = RainDataColors.VerdePrincipal,
                unfocusedBorderColor = RainDataColors.GrisMedio,
                focusedLabelColor = RainDataColors.VerdePrincipal,
                unfocusedLabelColor = RainDataColors.TextoSecundario,
                focusedContainerColor = RainDataColors.Blanco,
                unfocusedContainerColor = RainDataColors.Blanco
            ),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 15.sp,
                color = RainDataColors.TextoPrincipal
            )
        )

        // Mensaje de validación
        if (validationMessage != null) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = RainDataColors.RojoError,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = validationMessage,
                        fontSize = 12.sp,
                        color = RainDataColors.RojoError,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * 🎯 FUNCIÓN DE MANEJO DE REGISTRO (CON VALIDACIÓN DE NOMBRE)
 */
private fun handleRegistro(
    nombreCompleto: String,
    email: String,
    password: String,
    emailEsValido: Boolean,
    passwordEsValida: Boolean,
    passwordsCoinciden: Boolean,
    nombreEsValido: Boolean,
    buttonScale: Animatable<Float, AnimationVector1D>,
    scope: kotlinx.coroutines.CoroutineScope,
    authViewModel: AuthViewModel,
    onRegistroExitoso: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    when {
        nombreCompleto.isBlank() -> onError("Por favor ingrese su nombre completo")
        !nombreEsValido -> onError("El nombre no puede contener números")
        !emailEsValido -> onError("Por favor ingrese un email válido")
        !passwordEsValida -> onError("La contraseña debe tener al menos 6 caracteres")
        !passwordsCoinciden -> onError("Las contraseñas no coinciden")
        else -> {
            scope.launch {
                buttonScale.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy))
                buttonScale.animateTo(1f)

                authViewModel.registrarUsuario(
                    email = email.trim(),
                    password = password,
                    onSuccess = { uid -> onRegistroExitoso(uid, email.trim()) },
                    onError = { error -> onError(error) }
                )
            }
        }
    }
}

/**
 * 📱 PREVIEWS
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    MaterialTheme {
        RegistroScreen()
    }
}