package hn.unah.raindata.ui.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.ui.ui.components.SyncStatusIcon
import hn.unah.raindata.data.session.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPluviometrosScreen(
    viewModel: PluviometroViewModel = viewModel(),
    onAgregarPluviometro: () -> Unit,
    onVerDetalles: (String) -> Unit,
    onEditarPluviometro: (String) -> Unit
) {
    val pluviometros by viewModel.pluviometros.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (UserSession.shouldFilterPluviometrosByUser()) {
            val uid = UserSession.getCurrentUserUid()
            uid?.let { viewModel.cargarPluviometrosPorUsuario(it) }
        } else {
            viewModel.cargarPluviometros()
        }
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarFiltros by remember { mutableStateOf(false) }

    // Filtrar pluviómetros
    val pluviometrosFiltrados = remember(pluviometros, textoBusqueda) {
        if (textoBusqueda.isBlank()) {
            pluviometros
        } else {
            pluviometros.filter {
                it.numero_registro.contains(textoBusqueda, ignoreCase = true) ||
                        it.departamento.contains(textoBusqueda, ignoreCase = true) ||
                        it.municipio.contains(textoBusqueda, ignoreCase = true) ||
                        it.aldea.contains(textoBusqueda, ignoreCase = true) ||
                        it.responsable_nombre.contains(textoBusqueda, ignoreCase = true)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pluviometros") },
                actions = {
                    IconButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Icon(
                            if (mostrarFiltros) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filtros"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (UserSession.isAdmin()) {
                FloatingActionButton(onClick = onAgregarPluviometro) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Pluviómetro")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Buscador
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por código, ubicación o responsable...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (isLoading && pluviometros.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (pluviometrosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (textoBusqueda.isEmpty()) "No hay pluviómetros registrados" else "No se encontraron coincidencias",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pluviometrosFiltrados) { pluviometro ->
                        PluviometroCard(
                            pluviometro = pluviometro,
                            textoBusqueda = textoBusqueda,
                            onVerDetalles = { onVerDetalles(pluviometro.id) },
                            onEdit = { onEditarPluviometro(pluviometro.id) },
                            onDelete = {
                                viewModel.eliminarPluviometro(
                                    pluviometro.id,
                                    onSuccess = { },
                                    onError = { }
                                )
                            },
                            canEdit = UserSession.isAdmin(),
                            canDelete = UserSession.isAdmin()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluviometroCard(
    pluviometro: Pluviometro,
    textoBusqueda: String,
    onVerDetalles: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canEdit: Boolean,
    canDelete: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onVerDetalles
    ) {
        Box {
            // Ícono de Sincronización en la esquina superior izquierda
            SyncStatusIcon(
                status = pluviometro.syncStatus,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pluviometro.numero_registro,
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (textoBusqueda.isNotEmpty() &&
                                pluviometro.numero_registro.lowercase().contains(textoBusqueda.lowercase())) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Coincidencia",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${pluviometro.municipio}, ${pluviometro.departamento}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (textoBusqueda.isNotEmpty() && (
                                        pluviometro.departamento.lowercase().contains(textoBusqueda.lowercase()) ||
                                                pluviometro.municipio.lowercase().contains(textoBusqueda.lowercase()))) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Coincidencia",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Aldea: ${pluviometro.aldea}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (textoBusqueda.isNotEmpty() &&
                                pluviometro.aldea.lowercase().contains(textoBusqueda.lowercase())) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Coincidencia",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (!pluviometro.caserio_barrio_colonia.isNullOrBlank()) {
                            Text(
                                text = pluviometro.caserio_barrio_colonia ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Responsable: ${pluviometro.responsable_nombre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            if (textoBusqueda.isNotEmpty() &&
                                pluviometro.responsable_nombre.lowercase().contains(textoBusqueda.lowercase())) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Coincidencia",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "Coordenadas: ${String.format("%.6f", pluviometro.latitud)}, ${String.format("%.6f", pluviometro.longitud)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Botones de acción en la parte superior derecha
                    Row {
                        IconButton(onClick = onVerDetalles) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = "Ver detalles",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

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
                title = { Text("Eliminar pluviómetro") },
                text = { Text("¿Estás seguro de que deseas eliminar el pluviómetro ${pluviometro.numero_registro}? Esta acción no se puede deshacer.") },
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
}