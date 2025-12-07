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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
 * 🔓 PANTALLA DE RECUPERAR CONTRASEÑA MEJORADA - REVOCLIMAP
 * CAMBIOS APLICADOS:
 * ✅ LOGOS ELIMINADOS (no aparecen aquí)
 * ✅ "REVOCLIMAP" ELIMINADO (no aparece aquí)
 * ✅ Botón de regresar arriba ELIMINADO
 * ✅ Responsividad mejorada en textos
 * ✅ Labels no se montan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarPasswordScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // 🔧 Estados
    var email by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var emailEnviado by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by authViewModel.isLoading.collectAsState()

    // Validación
    val emailEsValido = email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))

    // 📱 Responsividad
    val configuration = LocalConfiguration.current
    val isSmallPhone = configuration.screenWidthDp < 360
    val isTablet = configuration.screenWidthDp >= 600

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
                .background(brush = RainDataColors.GradienteFondoRecuperar)
                .padding(padding)
        ) {
            // Fondo animado
            FloatingBackgroundCircles(
                colors = listOf(
                    RainDataColors.AzulCielo.copy(alpha = 0.25f),
                    RainDataColors.Blanco.copy(alpha = 0.15f),
                    RainDataColors.VerdeAcento.copy(alpha = 0.2f)
                )
            )

            // ✅ SIN BOTÓN DE REGRESAR ARRIBA

            // Contenido principal
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
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
                                    vertical = 32.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Transición animada entre estados
                            AnimatedContent(
                                targetState = emailEnviado,
                                transitionSpec = {
                                    slideInHorizontally { it } + fadeIn() togetherWith
                                            slideOutHorizontally { -it } + fadeOut()
                                },
                                label = "content_transition"
                            ) { enviado ->
                                if (!enviado) {
                                    // 📧 PANTALLA DE SOLICITUD
                                    RecuperarPasswordSolicitud(
                                        email = email,
                                        emailEsValido = emailEsValido,
                                        showError = showError,
                                        errorMessage = errorMessage,
                                        isLoading = isLoading,
                                        buttonScale = buttonScale,
                                        isSmallPhone = isSmallPhone,
                                        onEmailChange = { email = it; showError = false },
                                        onEnviar = {
                                            if (!emailEsValido) {
                                                errorMessage = "Por favor ingrese un correo válido"
                                                showError = true
                                            } else {
                                                scope.launch {
                                                    buttonScale.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy))
                                                    buttonScale.animateTo(1f)
                                                    authViewModel.recuperarPassword(
                                                        email = email.trim(),
                                                        onSuccess = { emailEnviado = true },
                                                        onError = { error ->
                                                            errorMessage = error
                                                            showError = true
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    // ✅ PANTALLA DE CONFIRMACIÓN
                                    RecuperarPasswordConfirmacion(
                                        email = email,
                                        isSmallPhone = isSmallPhone,
                                        onNavigateBack = onNavigateBack,
                                        onReintent = {
                                            emailEnviado = false
                                            email = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 📧 PANTALLA DE SOLICITUD DE RECUPERACIÓN (SIN LOGOS, SIN REVOCLIMAP)
 */
@Composable
fun RecuperarPasswordSolicitud(
    email: String,
    emailEsValido: Boolean,
    showError: Boolean,
    errorMessage: String,
    isLoading: Boolean,
    buttonScale: Animatable<Float, AnimationVector1D>,
    isSmallPhone: Boolean,
    onEmailChange: (String) -> Unit,
    onEnviar: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícono animado
        val scale by rememberInfiniteTransition(label = "icon").animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(RainDataColors.AzulCielo.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = RainDataColors.AzulProfundo
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ SIN LOGOS, SIN REVOCLIMAP - Solo título directo

        Text(
            text = "¿Olvidaste tu Contraseña?",
            fontSize = if (isSmallPhone) 24.sp else 26.sp,
            fontWeight = FontWeight.Bold,
            color = RainDataColors.VerdePrincipal,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No te preocupes, te enviaremos un enlace para restablecerla",
            fontSize = 14.sp,
            color = RainDataColors.TextoSecundario,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            maxLines = 3,
            modifier = Modifier.padding(horizontal = if (isSmallPhone) 8.dp else 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Email (RESPONSIVO)
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

        // Card de error
        ErrorCard(
            message = errorMessage,
            visible = showError
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón enviar
        RevoclimapButton(
            text = "Enviar Enlace",
            onClick = onEnviar,
            enabled = !isLoading && email.isNotBlank(),
            isLoading = isLoading,
            loadingText = "Enviando...",
            icon = Icons.Default.Send,
            buttonScale = buttonScale
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Footer de seguridad (RESPONSIVO)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = RainDataColors.TextoSecundario,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Tus datos están seguros",
                fontSize = 12.sp,
                color = RainDataColors.TextoSecundario,
                maxLines = 1
            )
        }
    }
}

/**
 * ✅ PANTALLA DE CONFIRMACIÓN DE ENVÍO
 */
@Composable
fun RecuperarPasswordConfirmacion(
    email: String,
    isSmallPhone: Boolean,
    onNavigateBack: () -> Unit,
    onReintent: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícono de éxito animado
        val scale by rememberInfiniteTransition(label = "success").animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(RainDataColors.VerdeValidacion.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = RainDataColors.VerdeValidacion
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Correo Enviado!",
            fontSize = if (isSmallPhone) 26.sp else 28.sp,
            fontWeight = FontWeight.Bold,
            color = RainDataColors.VerdePrincipal
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hemos enviado un enlace de recuperación a:",
            fontSize = 14.sp,
            color = RainDataColors.TextoSecundario,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = email,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RainDataColors.VerdePrincipal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Card de instrucciones
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = RainDataColors.AzulCielo.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = RainDataColors.Info,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Importante:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RainDataColors.Info
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Revisa tu carpeta de spam si no ves el correo\n• El enlace expira en 1 hora\n• Haz clic en el enlace para restablecer tu contraseña",
                            fontSize = 13.sp,
                            color = RainDataColors.TextoPrincipal,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Botón volver
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RainDataColors.VerdePrincipal
            )
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                tint = RainDataColors.Blanco
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Volver al Inicio",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = RainDataColors.Blanco
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón reenviar
        TextButton(onClick = onReintent) {
            Text(
                text = "¿No recibiste el correo? Intenta de nuevo",
                color = RainDataColors.VerdePrincipal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * 📱 PREVIEWS
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun RecuperarPasswordScreenPreview() {
    MaterialTheme {
        RecuperarPasswordScreen()
    }
}