package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.VoluntarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVoluntariosScreen(
    viewModel: VoluntarioViewModel = viewModel(),
    onAgregarVoluntario: () -> Unit = {},
    onEditarVoluntario: (Voluntario) -> Unit = {}
) {
    // ✅ CAMBIO: collectAsState en lugar de observeAsState
    val voluntarios by viewModel.voluntarios.collectAsState()
    var showNoPermissionDialog by remember { mutableStateOf(false) }
    var noPermissionMessage by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            if (UserSession.canCreateVoluntarios()) {
                FloatingActionButton(
                    onClick = onAgregarVoluntario
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Voluntario")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voluntarios Registrados",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${voluntarios.size} voluntarios activos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Badge de permisos
                    if (!UserSession.canCreateVoluntarios()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "Solo lectura",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Lista de voluntarios
            if (voluntarios.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay voluntarios registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (UserSession.canCreateVoluntarios()) {
                        Text(
                            text = "Presiona el botón + para agregar el primer voluntario",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(voluntarios) { voluntario ->
                        VoluntarioCard(
                            voluntario = voluntario,
                            onEdit = {
                                if (UserSession.canEditVoluntarios()) {
                                    onEditarVoluntario(voluntario)
                                } else {
                                    noPermissionMessage = "No tienes permisos para editar voluntarios. Solo los Administradores pueden realizar esta acción."
                                    showNoPermissionDialog = true
                                }
                            },
                            onDelete = {
                                if (UserSession.canDeleteVoluntarios()) {
                                    // ✅ CAMBIO: Agregar callbacks
                                    viewModel.eliminarVoluntario(
                                        firebaseUid = voluntario.firebase_uid,
                                        onSuccess = {
                                            // Eliminado exitosamente
                                        },
                                        onError = { error ->
                                            noPermissionMessage = "Error al eliminar: $error"
                                            showNoPermissionDialog = true
                                        }
                                    )
                                } else {
                                    noPermissionMessage = "No tienes permisos para eliminar voluntarios. Solo los Administradores pueden realizar esta acción."
                                    showNoPermissionDialog = true
                                }
                            },
                            canEdit = UserSession.canEditVoluntarios(),
                            canDelete = UserSession.canDeleteVoluntarios()
                        )
                    }
                }
            }
        }
    }

    // Diálogo de sin permisos
    if (showNoPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNoPermissionDialog = false },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            title = { Text("Permiso Denegado") },
            text = { Text(noPermissionMessage) },
            confirmButton = {
                Button(onClick = { showNoPermissionDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoluntarioCard(
    voluntario: Voluntario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canEdit: Boolean,
    canDelete: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voluntario.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (!voluntario.cedula.isNullOrBlank()) {
                        Text(
                            text = "Cédula: ${voluntario.cedula}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${voluntario.municipio}, ${voluntario.departamento}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!voluntario.telefono.isNullOrBlank()) {
                        Text(
                            text = "Tel: ${voluntario.telefono}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (voluntario.tipo_usuario.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = voluntario.tipo_usuario,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Botones de acción
                Row {
                    if (canEdit) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                    if (canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Eliminar Voluntario") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${voluntario.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}