package hn.unah.raindata.ui.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.viewmodel.AuthViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    authViewModel: AuthViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    onRegistroExitoso: (String, String) -> Unit = { _, _ -> }, // ← CAMBIO: Ahora recibe firebaseUid Y email
    onNavigateToLogin: () -> Unit = {},
    esPrimerUsuario: Boolean = false
) {
    var nombreCompleto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading by authViewModel.isLoading.collectAsState()

    // Validaciones en tiempo real
    val passwordsCoinciden = password == confirmarPassword && confirmarPassword.isNotEmpty()
    val passwordEsValida = password.length >= 6
    val emailEsValido = email.contains("@") && email.contains(".")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00D4FF),
                            Color(0xFF5B5FFF)
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                                .height(120.dp)
                                .offset(y = (-32).dp)
                                .clip(
                                    RoundedCornerShape(
                                        bottomStart = 100.dp,
                                        bottomEnd = 100.dp
                                    )
                                )
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF5B5FFF),
                                            Color(0xFF8B8FFF)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hola,",
                                    fontSize = 18.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = if (esPrimerUsuario) "¡Crear Admin!" else "¡Regístrate!",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Logo
                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = "Logo",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF5B5FFF)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (esPrimerUsuario)
                                "Registrar Administrador"
                            else
                                "Es más fácil registrarse ahora",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )


                        Spacer(modifier = Modifier.height(24.dp))

                        // Campo Nombre
                        OutlinedTextField(
                            value = nombreCompleto,
                            onValueChange = {
                                nombreCompleto = it
                                showError = false
                            },
                            label = { Text("NOMBRE DE USUARIO") },
                            placeholder = { Text("Juan Pérez") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5B5FFF),
                                unfocusedBorderColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                showError = false
                            },
                            label = { Text("CORREO ELECTRÓNICO") },
                            placeholder = { Text("juan@gmail.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            trailingIcon = {
                                if (email.isNotEmpty()) {
                                    Icon(
                                        if (emailEsValido) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (emailEsValido) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5B5FFF),
                                unfocusedBorderColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Contraseña
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                showError = false
                            },
                            label = { Text("CONTRASEÑA") },
                            placeholder = { Text("Ingrese contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                Row {
                                    if (password.isNotEmpty()) {
                                        Icon(
                                            if (passwordEsValida) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (passwordEsValida) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                            contentDescription = null
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
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5B5FFF),
                                unfocusedBorderColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (password.isNotEmpty() && !passwordEsValida) {
                            Text(
                                text = "Mínimo 6 caracteres",
                                fontSize = 12.sp,
                                color = Color(0xFFFF5252),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Confirmar Contraseña
                        OutlinedTextField(
                            value = confirmarPassword,
                            onValueChange = {
                                confirmarPassword = it
                                showError = false
                            },
                            label = { Text("CONFIRMAR CONTRASEÑA") },
                            placeholder = { Text("Reingrese contraseña") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                Row {
                                    if (confirmarPassword.isNotEmpty()) {
                                        Icon(
                                            if (passwordsCoinciden) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (passwordsCoinciden) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    IconButton(onClick = { confirmarPasswordVisible = !confirmarPasswordVisible }) {
                                        Icon(
                                            if (confirmarPasswordVisible) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                            contentDescription = null
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
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5B5FFF),
                                unfocusedBorderColor = Color.LightGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (confirmarPassword.isNotEmpty() && !passwordsCoinciden) {
                            Text(
                                text = "Las contraseñas no coinciden",
                                fontSize = 12.sp,
                                color = Color(0xFFFF5252),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Checkbox de términos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = aceptaTerminos,
                                onCheckedChange = { aceptaTerminos = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF5B5FFF)
                                )
                            )
                            Text(
                                text = "Acepto las políticas y términos",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mensaje de error
                        if (showError) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Botón Sign Up
                        Button(
                            onClick = {
                                // Validaciones
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
                                    !aceptaTerminos -> {
                                        errorMessage = "Debe aceptar los términos y condiciones"
                                        showError = true
                                    }
                                    else -> {
                                        scope.launch {
                                            authViewModel.registrarUsuario(
                                                email = email.trim(),
                                                password = password,
                                                onSuccess = { uid ->
                                                    // ← CAMBIO: Ahora pasa el uid Y el email
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
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5B5FFF)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Registrarse",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider
                        if (!esPrimerUsuario) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f))
                                Text(
                                    text = "  O continuar con  ",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botones de redes sociales
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = "Google",
                                        tint = Color(0xFFDB4437),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                IconButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Twitter",
                                        tint = Color(0xFF1DA1F2),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                IconButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "LinkedIn",
                                        tint = Color(0xFF0077B5),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Texto de login
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "¿Ya tienes cuenta? ",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            TextButton(
                                onClick = onNavigateToLogin,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Iniciar Sesión",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5B5FFF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}