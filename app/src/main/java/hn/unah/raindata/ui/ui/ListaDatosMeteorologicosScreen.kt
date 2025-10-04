package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDatosMeteorologicosScreen(
    viewModel: DatoMeteorologicoViewModel = viewModel(),
    onAgregarDato: () -> Unit = {},
    onEditarDato: (DatoMeteorologico) -> Unit = {}
) {
    val datos by viewModel.todosLosDatos.observeAsState(emptyList())
    var showNoPermissionDialog by remember { mutableStateOf(false) }
    var noPermissionMessage by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            if (UserSession.canCreateDatosMeteorologicos()) {
                FloatingActionButton(
                    onClick = onAgregarDato
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Dato Meteorológico")
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
                        Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Datos Meteorológicos",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${datos.size} registros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Badge de permisos
                    if (!UserSession.canCreateDatosMeteorologicos()) {
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

            // Lista de datos
            if (datos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay datos meteorológicos registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (UserSession.canCreateDatosMeteorologicos()) {
                        Text(
                            text = "Presiona el botón + para agregar el primer registro",
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
                    items(datos) { dato ->
                        DatoMeteorologicoCard(
                            dato = dato,
                            onEdit = {
                                if (UserSession.canEditDatosMeteorologicos()) {
                                    onEditarDato(dato)
                                } else {
                                    noPermissionMessage = "No tienes permisos para editar datos meteorológicos. Solo los Administradores pueden realizar esta acción."
                                    showNoPermissionDialog = true
                                }
                            },
                            onDelete = {
                                if (UserSession.canDeleteDatosMeteorologicos()) {
                                    viewModel.eliminarDato(dato.id)
                                } else {
                                    noPermissionMessage = "No tienes permisos para eliminar datos meteorológicos. Solo los Administradores pueden realizar esta acción."
                                    showNoPermissionDialog = true
                                }
                            },
                            canEdit = UserSession.canEditDatosMeteorologicos(),
                            canDelete = UserSession.canDeleteDatosMeteorologicos()
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
            icon = { Icon(Icons.Default.CloudQueue, contentDescription = null) },
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
fun DatoMeteorologicoCard(
    dato: DatoMeteorologico,
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
                    // Fecha y Hora
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${dato.fecha} - ${dato.hora}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pluviómetro y Voluntario
                    Text(
                        text = "Pluviómetro: ${dato.pluviometro_registro}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Registrado por: ${dato.voluntario_nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Datos meteorológicos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Precipitación",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${dato.precipitacion} mm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (dato.temperatura_maxima != null) {
                            Column {
                                Text(
                                    text = "Temp. Máx",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dato.temperatura_maxima}°C",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (dato.temperatura_minima != null) {
                            Column {
                                Text(
                                    text = "Temp. Mín",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dato.temperatura_minima}°C",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Condición del día
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = dato.condicion_dia,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Observaciones
                    if (!dato.observaciones.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Observaciones: ${dato.observaciones}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            title = { Text("Eliminar Dato Meteorológico") },
            text = { Text("¿Estás seguro de que deseas eliminar este registro del ${dato.fecha}? Esta acción no se puede deshacer.") },
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