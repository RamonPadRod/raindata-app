package hn.unah.raindata.ui.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.viewmodel.AuthViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import hn.unah.raindata.ui.theme.RainDataColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    authViewModel: AuthViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    onRegistroExitoso: (String, String) -> Unit = { _, _ -> },
    onNavigateToLogin: () -> Unit = {},
    esPrimerUsuario: Boolean = false
) {
    // 🔧 Estados (sin modificar funcionalidad)
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

    // Validaciones en tiempo real (sin modificar lógica)
    val passwordsCoinciden = password == confirmarPassword && confirmarPassword.isNotEmpty()
    val passwordEsValida = password.length >= 6
    val emailEsValido = email.contains("@") && email.contains(".")

    // 📱 Detección de tamaño de pantalla
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // ✨ Animaciones de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Animación de escala para botón
    val buttonScale = remember { Animatable(1f) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RainDataColors.AzulCielo.copy(alpha = 0.4f),
                            RainDataColors.VerdeAcento.copy(alpha = 0.6f),
                            RainDataColors.VerdePrincipal
                        )
                    )
                )
                .padding(padding)
        ) {
            // Elementos decorativos flotantes
            FloatingCirclesRegistro()

            // Layout adaptativo
            if (isTablet && isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RegistroBrandingPanel(
                        modifier = Modifier.weight(1f),
                        esPrimerUsuario = esPrimerUsuario
                    )

                    Spacer(modifier = Modifier.width(48.dp))

                    RegistroFormPanel(
                        modifier = Modifier.weight(1f),
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
                        esPrimerUsuario = esPrimerUsuario,
                        onNombreChange = {
                            nombreCompleto = it
                            showError = false
                        },
                        onEmailChange = {
                            email = it
                            showError = false
                        },
                        onPasswordChange = {
                            password = it
                            showError = false
                        },
                        onConfirmarPasswordChange = {
                            confirmarPassword = it
                            showError = false
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onConfirmarPasswordVisibilityToggle = {
                            confirmarPasswordVisible = !confirmarPasswordVisible
                        },
                        onRegistro = {
                            when {
                                nombreCompleto.isBlank() -> {
                                    errorMessage = "Por favor ingrese su nombre completo"
                                    showError = true
                                }
                                !emailEsValido -> {
                                    errorMessage = "Por favor ingrese un email válido"
                                    showError = true
                                }
                                !passwordEsValida -> {
                                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                    showError = true
                                }
                                !passwordsCoinciden -> {
                                    errorMessage = "Las contraseñas no coinciden"
                                    showError = true
                                }
                                else -> {
                                    scope.launch {
                                        buttonScale.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy))
                                        buttonScale.animateTo(1f)

                                        authViewModel.registrarUsuario(
                                            email = email.trim(),
                                            password = password,
                                            onSuccess = { uid ->
                                                onRegistroExitoso(uid, email.trim())
                                            },
                                            onError = { error ->
                                                errorMessage = error
                                                showError = true
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = if (isTablet) 80.dp else 24.dp,
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
                        esPrimerUsuario = esPrimerUsuario,
                        onNombreChange = {
                            nombreCompleto = it
                            showError = false
                        },
                        onEmailChange = {
                            email = it
                            showError = false
                        },
                        onPasswordChange = {
                            password = it
                            showError = false
                        },
                        onConfirmarPasswordChange = {
                            confirmarPassword = it
                            showError = false
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onConfirmarPasswordVisibilityToggle = {
                            confirmarPasswordVisible = !confirmarPasswordVisible
                        },
                        onRegistro = {
                            when {
                                nombreCompleto.isBlank() -> {
                                    errorMessage = "Por favor ingrese su nombre completo"
                                    showError = true
                                }
                                !emailEsValido -> {
                                    errorMessage = "Por favor ingrese un email válido"
                                    showError = true
                                }
                                !passwordEsValida -> {
                                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                    showError = true
                                }
                                !passwordsCoinciden -> {
                                    errorMessage = "Las contraseñas no coinciden"
                                    showError = true
                                }
                                else -> {
                                    scope.launch {
                                        buttonScale.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy))
                                        buttonScale.animateTo(1f)

                                        authViewModel.registrarUsuario(
                                            email = email.trim(),
                                            password = password,
                                            onSuccess = { uid ->
                                                onRegistroExitoso(uid, email.trim())
                                            },
                                            onError = { error ->
                                                errorMessage = error
                                                showError = true
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
            }
        }
    }
}

// 🎨 Panel de branding para registro (tablets landscape)
@Composable
fun RegistroBrandingPanel(
    modifier: Modifier = Modifier,
    esPrimerUsuario: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo animado
        val scale by rememberInfiniteTransition(label = "logo").animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Icon(
            Icons.Default.PersonAdd,
            contentDescription = "Registro",
            modifier = Modifier
                .size(180.dp)
                .scale(scale),
            tint = RainDataColors.Amarillo
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (esPrimerUsuario) "Admin" else "Únete",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RainDataColors.Blanco,
            letterSpacing = 2.sp
        )

        Text(
            text = if (esPrimerUsuario)
                "Crear cuenta de Administrador"
            else
                "Comienza tu experiencia",
            fontSize = 18.sp,
            color = RainDataColors.Blanco.copy(alpha = 0.8f),
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Monitoreo climático\npara tu comunidad",
            fontSize = 16.sp,
            color = RainDataColors.Blanco.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )
    }
}

// 📝 Panel del formulario de registro
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
    esPrimerUsuario: Boolean,
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
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = RainDataColors.Blanco.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header decorativo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .offset(y = (-32).dp)
                        .clip(RoundedCornerShape(bottomStart = 120.dp, bottomEnd = 120.dp))
                        .background(RainDataColors.GradienteVerde),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Registro",
                            modifier = Modifier.size(64.dp),
                            tint = RainDataColors.Amarillo
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (esPrimerUsuario) "¡Crear Admin!" else "¡Regístrate!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = RainDataColors.Blanco
                        )

                        Text(
                            text = if (esPrimerUsuario)
                                "Configuración inicial del sistema"
                            else
                                "Únete a la comunidad agroclimática",
                            fontSize = 13.sp,
                            color = RainDataColors.Blanco.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logo y título
                Text(
                    text = "RainData",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RainDataColors.VerdePrincipal,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (esPrimerUsuario)
                        "Registrar Administrador"
                    else
                        "Es más fácil registrarse ahora",
                    fontSize = 14.sp,
                    color = RainDataColors.VerdeSecundario,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Campo Nombre con animación
                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre Completo", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Juan Pérez González") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RainDataColors.VerdePrincipal,
                        unfocusedBorderColor = RainDataColors.VerdeAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RainDataColors.VerdePrincipal,
                        cursorColor = RainDataColors.VerdePrincipal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Campo Email con validación visual
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Correo Electrónico", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("juan@example.com") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario
                        )
                    },
                    trailingIcon = {
                        if (email.isNotEmpty()) {
                            Icon(
                                if (emailEsValido) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (emailEsValido)
                                    RainDataColors.VerdeValidacion
                                else
                                    RainDataColors.RojoError,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RainDataColors.VerdePrincipal,
                        unfocusedBorderColor = RainDataColors.VerdeAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RainDataColors.VerdePrincipal,
                        cursorColor = RainDataColors.VerdePrincipal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Campo Contraseña con validación
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Mínimo 6 caracteres") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (password.isNotEmpty()) {
                                Icon(
                                    if (passwordEsValida) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (passwordEsValida)
                                        RainDataColors.VerdeValidacion
                                    else
                                        RainDataColors.RojoError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(onClick = onPasswordVisibilityToggle) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = RainDataColors.VerdeSecundario
                                )
                            }
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RainDataColors.VerdePrincipal,
                        unfocusedBorderColor = RainDataColors.VerdeAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RainDataColors.VerdePrincipal,
                        cursorColor = RainDataColors.VerdePrincipal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Mensaje de ayuda para contraseña
                AnimatedVisibility(
                    visible = password.isNotEmpty() && !passwordEsValida,
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
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = RainDataColors.RojoError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mínimo 6 caracteres requeridos",
                            fontSize = 12.sp,
                            color = RainDataColors.RojoError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Campo Confirmar Contraseña
                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = onConfirmarPasswordChange,
                    label = { Text("Confirmar Contraseña", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Reingrese su contraseña") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (confirmarPassword.isNotEmpty()) {
                                Icon(
                                    if (passwordsCoinciden) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (passwordsCoinciden)
                                        RainDataColors.VerdeValidacion
                                    else
                                        RainDataColors.RojoError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(onClick = onConfirmarPasswordVisibilityToggle) {
                                Icon(
                                    if (confirmarPasswordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = RainDataColors.VerdeSecundario
                                )
                            }
                        }
                    },
                    visualTransformation = if (confirmarPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RainDataColors.VerdePrincipal,
                        unfocusedBorderColor = RainDataColors.VerdeAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RainDataColors.VerdePrincipal,
                        cursorColor = RainDataColors.VerdePrincipal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Mensaje de contraseñas no coinciden
                AnimatedVisibility(
                    visible = confirmarPassword.isNotEmpty() && !passwordsCoinciden,
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
                            text = "Las contraseñas no coinciden",
                            fontSize = 12.sp,
                            color = RainDataColors.RojoError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mensaje de error general
                AnimatedVisibility(
                    visible = showError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Registrarse con animación
                Button(
                    onClick = onRegistro,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .scale(buttonScale.value),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RainDataColors.VerdePrincipal,
                        disabledContainerColor = RainDataColors.VerdeAcento.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = RainDataColors.Amarillo,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Registrando...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RainDataColors.Blanco
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = RainDataColors.Blanco,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Registrarse",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Divider y botones sociales (solo si no es primer usuario)
                if (!esPrimerUsuario) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = RainDataColors.VerdeAcento.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "  O continúa con  ",
                            fontSize = 12.sp,
                            color = RainDataColors.VerdeSecundario,
                            fontWeight = FontWeight.Medium
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = RainDataColors.VerdeAcento.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botones de redes sociales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialButtonRegistro(
                            icon = Icons.Default.Email,
                            backgroundColor = Color(0xFFDB4437),
                            contentDescription = "Google"
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        SocialButtonRegistro(
                            icon = Icons.Default.Info,
                            backgroundColor = Color(0xFF1DA1F2),
                            contentDescription = "Twitter"
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        SocialButtonRegistro(
                            icon = Icons.Default.Person,
                            backgroundColor = Color(0xFF0077B5),
                            contentDescription = "LinkedIn"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Texto de navegación a login
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        fontSize = 14.sp,
                        color = RainDataColors.VerdeSecundario
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Iniciar Sesión",
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

// 🔘 Botón de red social para registro
@Composable
fun SocialButtonRegistro(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentDescription: String
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .size(56.dp)
            .scale(scale),
        shape = CircleShape,
        color = backgroundColor.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            backgroundColor.copy(alpha = 0.3f)
        ),
        shadowElevation = 4.dp
    ) {
        IconButton(
            onClick = {
                pressed = true
                /* TODO: Social Sign In */
            }
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = backgroundColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// 🎈 Círculos flotantes decorativos para registro
@Composable
fun FloatingCirclesRegistro() {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )

    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset3"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Círculo 1 - Verde claro
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = 120.dp + offset1.dp)
                .size(140.dp)
                .blur(45.dp)
                .background(
                    RainDataColors.VerdeAcento.copy(alpha = 0.3f),
                    CircleShape
                )
        )

        // Círculo 2 - Amarillo
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = 180.dp + offset2.dp)
                .size(160.dp)
                .blur(55.dp)
                .background(
                    RainDataColors.Amarillo.copy(alpha = 0.25f),
                    CircleShape
                )
        )

        // Círculo 3 - Verde principal
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-180).dp + offset3.dp)
                .size(190.dp)
                .blur(65.dp)
                .background(
                    RainDataColors.VerdePrincipal.copy(alpha = 0.2f),
                    CircleShape
                )
        )

        // Círculo 4 - Azul cielo (adicional para más profundidad)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-50).dp, y = (-100).dp + offset1.dp)
                .size(130.dp)
                .blur(50.dp)
                .background(
                    RainDataColors.AzulCielo.copy(alpha = 0.2f),
                    CircleShape
                )
        )
    }
}

// 📱 Previews para múltiples dispositivos
@androidx.compose.ui.tooling.preview.Preview(
    name = "Phone Portrait",
    showBackground = true,
    device = "spec:width=360dp,height=640dp,dpi=480"
)
@androidx.compose.ui.tooling.preview.Preview(
    name = "Phone Landscape",
    showBackground = true,
    device = "spec:width=640dp,height=360dp,dpi=480"
)
@androidx.compose.ui.tooling.preview.Preview(
    name = "Tablet Portrait",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=320"
)
@androidx.compose.ui.tooling.preview.Preview(
    name = "Tablet Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=320"
)
@androidx.compose.ui.tooling.preview.Preview(
    name = "Large Phone",
    showBackground = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@androidx.compose.ui.tooling.preview.Preview(
    name = "Primer Usuario - Admin",
    showBackground = true,
    device = "spec:width=360dp,height=640dp,dpi=480"
)
@Composable
fun RegistroScreenPreview() {
    MaterialTheme {
        RegistroScreen(esPrimerUsuario = false)
    }
}

@Composable
fun RegistroScreenAdminPreview() {
    MaterialTheme {
        RegistroScreen(esPrimerUsuario = true)
    }
}