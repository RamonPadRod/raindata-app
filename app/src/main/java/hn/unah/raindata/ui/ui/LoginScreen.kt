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
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import hn.unah.raindata.ui.theme.RainDataColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit = {},
    onNavigateToRegistro: () -> Unit = {},
    onNavigateToRecuperarPassword: () -> Unit = {}
) {
    // 🔧 Estados (sin modificar funcionalidad)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by authViewModel.isLoading.collectAsState()

    // 📱 Detección de tamaño de pantalla para responsividad
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // ✨ Animaciones de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Animación de escala para botones (spring bounce)
    val buttonScale = remember { Animatable(1f) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    // Fondo con gradiente orgánico
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
            // Elementos decorativos de fondo (círculos flotantes)
            FloatingCircles()

            // Layout adaptativo según dispositivo
            if (isTablet && isLandscape) {
                // Layout horizontal para tablets en landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Panel izquierdo decorativo
                    BrandingPanel(modifier = Modifier.weight(1f))

                    Spacer(modifier = Modifier.width(48.dp))

                    // Panel derecho con formulario
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
                        onEmailChange = {
                            email = it
                            showError = false
                        },
                        onPasswordChange = {
                            password = it
                            showError = false
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onRememberMeToggle = { rememberMe = it },
                        onForgotPassword = onNavigateToRecuperarPassword,
                        onLogin = {
                            scope.launch {
                                // Animación de rebote en botón
                                buttonScale.animateTo(
                                    targetValue = 0.95f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                buttonScale.animateTo(1f)

                                authViewModel.iniciarSesion(
                                    email = email.trim(),
                                    password = password,
                                    onSuccess = { uid -> onLoginSuccess(uid) },
                                    onError = { error ->
                                        errorMessage = error
                                        showError = true
                                    }
                                )
                            }
                        },
                        onNavigateToRegistro = onNavigateToRegistro
                    )
                }
            } else {
                // Layout vertical para teléfonos y tablets en portrait
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
                        onEmailChange = {
                            email = it
                            showError = false
                        },
                        onPasswordChange = {
                            password = it
                            showError = false
                        },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onRememberMeToggle = { rememberMe = it },
                        onForgotPassword = onNavigateToRecuperarPassword,
                        onLogin = {
                            scope.launch {
                                buttonScale.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy))
                                buttonScale.animateTo(1f)

                                authViewModel.iniciarSesion(
                                    email = email.trim(),
                                    password = password,
                                    onSuccess = { uid -> onLoginSuccess(uid) },
                                    onError = { error ->
                                        errorMessage = error
                                        showError = true
                                    }
                                )
                            }
                        },
                        onNavigateToRegistro = onNavigateToRegistro
                    )
                }
            }
        }
    }
}

// 🎨 Panel decorativo de branding (para tablets landscape)
@Composable
fun BrandingPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo animado con escala
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
            Icons.Default.CloudQueue,
            contentDescription = "Logo",
            modifier = Modifier
                .size(180.dp)
                .scale(scale),
            tint = RainDataColors.Amarillo
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RainData",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = RainDataColors.Blanco,
            letterSpacing = 2.sp
        )

        Text(
            text = "Sistema Pluviométrico",
            fontSize = 18.sp,
            color = RainDataColors.Blanco.copy(alpha = 0.8f),
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Conectando el campo\ncon la ciencia del clima",
            fontSize = 16.sp,
            color = RainDataColors.Blanco.copy(alpha = 0.7f),
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// 📝 Panel del formulario de login
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
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeToggle: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    // Animación de entrada con fade + slide
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
                // Header con gradiente ondulado
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Animación del logo con rotación sutil
                        val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
                            initialValue = -5f,
                            targetValue = 5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = EaseInOutQuad),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "rotation"
                        )

                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(64.dp)
                                .scale(1.2f),
                            tint = RainDataColors.Amarillo
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "¡Bienvenido!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = RainDataColors.Blanco
                        )

                        Text(
                            text = "Inicia sesión para continuar",
                            fontSize = 14.sp,
                            color = RainDataColors.Blanco.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logo y título de la app
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RainData",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RainDataColors.VerdePrincipal,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Sistema Pluviométrico",
                        fontSize = 14.sp,
                        color = RainDataColors.VerdeSecundario,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Campo de Email con glassmorphism
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Correo Electrónico", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("tu_correo@example.com") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario
                        )
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

                Spacer(modifier = Modifier.height(20.dp))

                // Campo de Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("••••••••••") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = RainDataColors.VerdeSecundario
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onPasswordVisibilityToggle) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña"
                                else "Mostrar contraseña",
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
                        focusedBorderColor = RainDataColors.VerdePrincipal,
                        unfocusedBorderColor = RainDataColors.VerdeAcento.copy(alpha = 0.5f),
                        focusedLabelColor = RainDataColors.VerdePrincipal,
                        cursorColor = RainDataColors.VerdePrincipal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Remember me y Forgot password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = onRememberMeToggle,
                            colors = CheckboxDefaults.colors(
                                checkedColor = RainDataColors.VerdePrincipal,
                                uncheckedColor = RainDataColors.VerdeSecundario
                            )
                        )
                        Text(
                            text = "Recuérdame",
                            fontSize = 14.sp,
                            color = RainDataColors.VerdePrincipal,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(onClick = onForgotPassword) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = RainDataColors.VerdeSecundario,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Mensaje de error con animación
                AnimatedVisibility(
                    visible = showError,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Log In con animación de escala
                Button(
                    onClick = onLogin,
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
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
                        // Shimmer effect durante carga
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
                                text = "Iniciando...",
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
                                Icons.Default.Login,
                                contentDescription = null,
                                tint = RainDataColors.Blanco,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Iniciar Sesión",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Divider con texto
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

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de redes sociales con ripple effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialButton(
                        icon = Icons.Default.Email,
                        backgroundColor = Color(0xFFDB4437),
                        contentDescription = "Google"
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    SocialButton(
                        icon = Icons.Default.Info,
                        backgroundColor = Color(0xFF1DA1F2),
                        contentDescription = "Twitter"
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    SocialButton(
                        icon = Icons.Default.Person,
                        backgroundColor = Color(0xFF0077B5),
                        contentDescription = "LinkedIn"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Texto de registro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes cuenta? ",
                        fontSize = 14.sp,
                        color = RainDataColors.VerdeSecundario
                    )
                    TextButton(
                        onClick = onNavigateToRegistro,
                        contentPadding = PaddingValues(0.dp)
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

// 🔘 Botón de red social con animación
@Composable
fun SocialButton(
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

// 🎈 Círculos flotantes decorativos de fondo
@Composable
fun FloatingCircles() {
    // Animación infinita de flotación
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )

    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset3"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Círculo 1 - Amarillo
        Box(
            modifier = Modifier
                .offset(x = 50.dp, y = 100.dp + offset1.dp)
                .size(120.dp)
                .blur(40.dp)
                .background(
                    RainDataColors.Amarillo.copy(alpha = 0.3f),
                    CircleShape
                )
        )

        // Círculo 2 - Verde claro
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 200.dp + offset2.dp)
                .size(150.dp)
                .blur(50.dp)
                .background(
                    RainDataColors.VerdeAcento.copy(alpha = 0.25f),
                    CircleShape
                )
        )

        // Círculo 3 - Azul cielo
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = (-150).dp + offset3.dp)
                .size(180.dp)
                .blur(60.dp)
                .background(
                    RainDataColors.AzulCielo.copy(alpha = 0.2f),
                    CircleShape
                )
        )
    }
}

// 📱 Preview para múltiples dispositivos
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
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}