package hn.unah.raindata.ui.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hn.unah.raindata.ui.theme.RainDataColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSolicitudPendiente(
    nombreUsuario: String = "Usuario",
    emailUsuario: String = "",
    onCerrarSesion: () -> Unit = {},
    onCancelarSolicitud: () -> Unit = {}
) {
    var showCancelarDialog by remember { mutableStateOf(false) }

    // Animación del ícono de reloj
    val infiniteTransition = rememberInfiniteTransition(label = "clock")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Animación de escala para el contenido
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RainDataColors.AzulCielo.copy(alpha = 0.3f),
                            RainDataColors.VerdeAcento.copy(alpha = 0.5f),
                            RainDataColors.VerdePrincipal.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Card principal
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = RainDataColors.Blanco.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Ícono de reloj animado
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(
                                            color = RainDataColors.Amarillo.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .scale(1f + (rotation / 360f) * 0.1f),
                                        tint = RainDataColors.Amarillo
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Título
                                Text(
                                    text = "⏳ Solicitud Pendiente",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RainDataColors.VerdePrincipal,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Saludo personalizado
                                Text(
                                    text = "Hola, $nombreUsuario",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = RainDataColors.VerdeSecundario,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Mensaje principal
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = RainDataColors.AzulCielo.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Tu solicitud de Administrador está siendo revisada por nuestro equipo.",
                                            fontSize = 15.sp,
                                            color = RainDataColors.VerdeSecundario,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 22.sp
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Te notificaremos cuando sea aprobada o rechazada.",
                                            fontSize = 14.sp,
                                            color = RainDataColors.VerdeSecundario,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Email del usuario
                                if (emailUsuario.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = RainDataColors.VerdeSecundario,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = emailUsuario,
                                            fontSize = 13.sp,
                                            color = RainDataColors.VerdeSecundario
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                HorizontalDivider(
                                    color = RainDataColors.VerdeAcento.copy(alpha = 0.3f)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Información adicional
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = RainDataColors.AzulCielo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "¿Qué puedo hacer mientras espero?",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RainDataColors.VerdePrincipal
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "• Puedes cerrar sesión y volver más tarde\n• Recibirás un email cuando haya novedades\n• Puedes cancelar tu solicitud si cambias de opinión",
                                            fontSize = 12.sp,
                                            color = RainDataColors.VerdeSecundario,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón Cerrar Sesión
                        Button(
                            onClick = onCerrarSesion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RainDataColors.VerdePrincipal
                            ),
                            shape = MaterialTheme.shapes.large,
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Cerrar Sesión",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botón Cancelar Solicitud
                        // Botón Cancelar Solicitud
                        Button(
                            onClick = { showCancelarDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = MaterialTheme.shapes.large,
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Cancelar Solicitud",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para cancelar solicitud
    if (showCancelarDialog) {
        AlertDialog(
            onDismissRequest = { showCancelarDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "⚠️ Cancelar Solicitud",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¿Estás seguro de que deseas cancelar tu solicitud de Administrador?",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "⚠️ Esta acción:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Eliminará tu cuenta de REVOCLIMAP\n• Eliminará tu usuario del sistema\n• No se puede deshacer",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Si deseas volver a registrarte, tendrás que crear una nueva cuenta.",
                        fontSize = 13.sp,
                        color = RainDataColors.VerdeSecundario,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelarDialog = false
                        onCancelarSolicitud()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sí, Cancelar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelarDialog = false }) {
                    Text("No, Volver")
                }
            }
        )
    }
}