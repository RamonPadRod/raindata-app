package hn.unah.raindata.ui.ui

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 🔐 PANTALLA DE LOGIN MEJORADA - REVOCLIMAP
 * CAMBIOS APLICADOS:
 * ✅ Logos UNAH + Mesa más grandes (80dp móvil, 100dp tablet)
 * ✅ "REVOCLIMAP" y subtítulo con mejor visibilidad (verde oscuro)
 * ✅ Botones sociales ELIMINADOS
 * ✅ Checkbox "Recuérdame" FUNCIONAL (guarda email)
 * ✅ Responsividad mejorada en textos
 * ✅ Logos SOLO en LoginScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit = {},
    onNavigateToRegistro: () -> Unit = {},
    onNavigateToRecuperarPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("revoclimap_prefs", Context.MODE_PRIVATE)

    // 🔧 Estados
    var email by remember { mutableStateOf(sharedPrefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(sharedPrefs.getBoolean("remember_me", false)) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by authViewModel.isLoading.collectAsState()

    // 📱 Responsividad
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600
    val isLandscape = screenWidthDp > screenHeightDp
    val isCompact = screenHeightDp < 600
    val isSmallPhone = screenWidthDp < 360

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
                .background(brush = RainDataColors.GradienteFondoLogin)
                .padding(padding)
        ) {
            // Fondo animado con círculos flotantes
            FloatingBackgroundCircles()

            // Layout adaptativo
            when {
                isTablet && isLandscape -> {
                    // MODO TABLET HORIZONTAL
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LoginBrandingPanel(
                            modifier = Modifier.weight(1f),
                            isTablet = isTablet
                        )

                        Spacer(modifier = Modifier.width(48.dp))

                        LoginFormPanel(
                            modifier = Modifier.weight(1f),
                            email = email,
                            password = password,
                            passwordVisible = passwordVisible,
                            rememberMe = rememberMe,
                            showError = showError,
                            errorMessage = errorMessage,
                            isLoading = isLoading,
                            visible = visible,
                            buttonScale = buttonScale,
                            isCompact = false,
                            isSmallPhone = false,
                            onEmailChange = { email = it; showError = false },
                            onPasswordChange = { password = it; showError = false },
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                            onRememberMeToggle = {
                                rememberMe = it
                                // Guardar preferencia
                                sharedPrefs.edit().putBoolean("remember_me", it).apply()
                            },
                            onForgotPassword = onNavigateToRecuperarPassword,
                            onLogin = {
                                scope.launch {
                                    buttonScale.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy))
                                    buttonScale.animateTo(1f)

                                    // Guardar email si "Recuérdame" está activado
                                    if (rememberMe) {
                                        sharedPrefs.edit().putString("saved_email", email.trim()).apply()
                                    } else {
                                        sharedPrefs.edit().remove("saved_email").apply()
                                    }

                                    authViewModel.iniciarSesion(
                                        email = email.trim(),
                                        password = password,
                                        onSuccess = { uid -> onLoginSuccess(uid) },
                                        onError = { error -> errorMessage = error; showError = true }
                                    )
                                }
                            },
                            onNavigateToRegistro = onNavigateToRegistro
                        )
                    }
                }
                else -> {
                    // MODO VERTICAL
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
                                vertical = if (isCompact) 16.dp else 24.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (!isCompact) {
                            Spacer(modifier = Modifier.height(40.dp))
                        }

                        LoginFormPanel(
                            modifier = Modifier.fillMaxWidth(),
                            email = email,
                            password = password,
                            passwordVisible = passwordVisible,
                            rememberMe = rememberMe,
                            showError = showError,
                            errorMessage = errorMessage,
                            isLoading = isLoading,
                            visible = visible,
                            buttonScale = buttonScale,
                            isCompact = isCompact,
                            isSmallPhone = isSmallPhone,
                            onEmailChange = { email = it; showError = false },
                            onPasswordChange = { password = it; showError = false },
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                            onRememberMeToggle = {
                                rememberMe = it
                                sharedPrefs.edit().putBoolean("remember_me", it).apply()
                            },
                            onForgotPassword = onNavigateToRecuperarPassword,
                            onLogin = {
                                scope.launch {
                                    buttonScale.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy))
                                    buttonScale.animateTo(1f)

                                    if (rememberMe) {
                                        sharedPrefs.edit().putString("saved_email", email.trim()).apply()
                                    } else {
                                        sharedPrefs.edit().remove("saved_email").apply()
                                    }

                                    authViewModel.iniciarSesion(
                                        email = email.trim(),
                                        password = password,
                                        onSuccess = { uid -> onLoginSuccess(uid) },
                                        onError = { error -> errorMessage = error; showError = true }
                                    )
                                }
                            },
                            onNavigateToRegistro = onNavigateToRegistro
                        )

                        if (!isCompact) {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎨 PANEL DE BRANDING (TABLETS HORIZONTAL)
 */
@Composable
fun LoginBrandingPanel(
    modifier: Modifier = Modifier,
    isTablet: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logos institucionales MÁS GRANDES
        InstitutionalHeaderLogin(
            isTablet = isTablet
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mensaje de bienvenida
        Text(
            text = "Monitoreo Climático\nComunitario",
            fontSize = 18.sp,
            color = RainDataColors.TextoSobreFondo.copy(alpha = 0.9f),
            lineHeight = 26.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Datos en tiempo real\npara proteger tu comunidad",
            fontSize = 15.sp,
            color = RainDataColors.TextoSobreFondo.copy(alpha = 0.75f),
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 🏛️ HEADER CON LOGOS GRANDES (SOLO LOGIN)
 */
@Composable
fun InstitutionalHeaderLogin(
    isTablet: Boolean
) {
    // Logos MÁS GRANDES: 80dp móvil, 100dp tablet
    val logoSize = if (isTablet) 100.dp else 80.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logos institucionales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoImage(
                drawableRes = hn.unah.raindata.R.drawable.logo_unah,
                contentDescription = "Universidad Nacional Autónoma de Honduras",
                size = logoSize
            )

            Spacer(modifier = Modifier.width(24.dp))

            LogoImage(
                drawableRes = hn.unah.raindata.R.drawable.logo_mesa_agroclimatica,
                contentDescription = "Mesa Agroclimática del Paraíso",
                size = logoSize
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nombre REVOCLIMAP con mejor visibilidad
        Text(
            text = "REVOCLIMAP",
            fontSize = if (isTablet) 36.sp else 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RainDataColors.VerdePrincipal, // Verde oscuro visible
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Red de Voluntarios Climáticos del Paraíso",
            fontSize = if (isTablet) 14.sp else 13.sp,
            color = RainDataColors.TextoPrincipal, // Negro visible
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * 📝 PANEL DE FORMULARIO DE LOGIN
 */
@Composable
fun LoginFormPanel(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    passwordVisible: Boolean,
    rememberMe: Boolean,
    showError: Boolean,
    errorMessage: String,
    isLoading: Boolean,
    visible: Boolean,
    buttonScale: Animatable<Float, AnimationVector1D>,
    isCompact: Boolean,
    isSmallPhone: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeToggle: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(
            initialOffsetY = { it / 3 },
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
                        horizontal = if (isSmallPhone) 20.dp else if (isCompact) 24.dp else 32.dp,
                        vertical = if (isCompact) 24.dp else 32.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con logos SOLO en modo vertical
                if (!isTablet || configuration.screenWidthDp <= configuration.screenHeightDp) {
                    InstitutionalHeaderLogin(isTablet = isTablet)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = "Iniciar Sesión",
                    fontSize = if (isCompact) 20.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = RainDataColors.VerdePrincipal
                )

                Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

                Text(
                    text = "Accede a la plataforma de monitoreo",
                    fontSize = 13.sp,
                    color = RainDataColors.TextoSecundario,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isCompact) 20.dp else 28.dp))

                // Campo Email
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = {
                        Text(
                            "Correo Electrónico",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    },
                    placeholder = {
                        Text(
                            "usuario@ejemplo.com",
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

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = {
                        Text(
                            "Contraseña",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    },
                    placeholder = {
                        Text(
                            "••••••••",
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
                        IconButton(onClick = onPasswordVisibilityToggle) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                tint = RainDataColors.VerdeSecundario
                            )
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

                Spacer(modifier = Modifier.height(14.dp))

                // Recuérdame y Olvidaste contraseña
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = onRememberMeToggle,
                            colors = CheckboxDefaults.colors(
                                checkedColor = RainDataColors.VerdePrincipal,
                                uncheckedColor = RainDataColors.GrisMedio
                            )
                        )
                        Text(
                            text = "Recuérdame",
                            fontSize = 13.sp,
                            color = RainDataColors.TextoSecundario,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onForgotPassword,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = RainDataColors.VerdePrincipal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                // Card de error
                ErrorCard(
                    message = errorMessage,
                    visible = showError
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón de login
                RevoclimapButton(
                    text = "Iniciar Sesión",
                    onClick = onLogin,
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    isLoading = isLoading,
                    loadingText = "Iniciando sesión...",
                    icon = Icons.Default.Login,
                    buttonScale = buttonScale
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Divider "o" - SIN botones sociales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RainDataColors.GrisMedio,
                        thickness = 1.dp
                    )
                    Text(
                        text = "  o  ",
                        fontSize = 12.sp,
                        color = RainDataColors.TextoSecundario,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RainDataColors.GrisMedio,
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Enlace a registro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes cuenta? ",
                        fontSize = 14.sp,
                        color = RainDataColors.TextoSecundario
                    )
                    TextButton(
                        onClick = onNavigateToRegistro,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            text = "Regístrate",
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
 * 📱 PREVIEWS
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}