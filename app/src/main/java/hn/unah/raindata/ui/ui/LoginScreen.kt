package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.VoluntarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    val voluntarios by voluntarioViewModel.todosLosVoluntarios.observeAsState(emptyList())

    var usuarioSeleccionado by remember { mutableStateOf<String?>(null) }
    var expandedUsuario by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CloudQueue,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DatosLluvia",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Sistema de Monitoreo Pluviométrico",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Formulario de login
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Selecciona tu usuario para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selector de usuario
                ExposedDropdownMenuBox(
                    expanded = expandedUsuario,
                    onExpandedChange = { expandedUsuario = it }
                ) {
                    OutlinedTextField(
                        value = usuarioSeleccionado ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Seleccionar Usuario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUsuario)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUsuario,
                        onDismissRequest = { expandedUsuario = false }
                    ) {
                        if (voluntarios.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay usuarios registrados") },
                                onClick = { }
                            )
                        } else {
                            voluntarios.forEach { voluntario ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                voluntario.nombre,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                voluntario.tipo_usuario ?: "Sin rol",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "${voluntario.municipio}, ${voluntario.departamento}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        usuarioSeleccionado = voluntario.nombre
                                        showError = false

                                        // Iniciar sesión
                                        if (voluntario.tipo_usuario.isNullOrBlank()) {
                                            showError = true
                                            errorMessage = "Este usuario no tiene un rol asignado. Contacta al administrador."
                                        } else {
                                            UserSession.login(voluntario)
                                            expandedUsuario = false
                                            onLoginSuccess()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Mensaje de error
                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Información sobre roles
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Roles disponibles:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Administrador: Acceso completo", style = MaterialTheme.typography.bodySmall)
                        Text("• Voluntario: Gestión de pluviómetros", style = MaterialTheme.typography.bodySmall)
                        Text("• Observador: Solo visualización", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}