package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.PluviometroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPluviometrosScreen(
    viewModel: PluviometroViewModel = viewModel(),
    onAgregarPluviometro: () -> Unit = {},
    onVerDetalles: (Pluviometro) -> Unit = {},
    onEditarPluviometro: (Pluviometro) -> Unit = {}
) {
    val todosLosPluviometros by viewModel.todosLosPluviometros.observeAsState(emptyList())
    var showNoPermissionDialog by remember { mutableStateOf(false) }
    var noPermissionMessage by remember { mutableStateOf("") }

    // ✅ ESTADO DE BÚSQUEDA
    var textoBusqueda by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // ✅ FILTRAR PLUVIÓMETROS EN TIEMPO REAL
    val pluviometrosFiltrados = remember(todosLosPluviometros, textoBusqueda) {
        if (textoBusqueda.isBlank()) {
            todosLosPluviometros
        } else {
            val busqueda = textoBusqueda.trim().lowercase()
            todosLosPluviometros.filter { pluviometro ->
                // Buscar en: código, responsable, departamento, municipio, aldea
                pluviometro.numero_registro.lowercase().contains(busqueda) ||
                        pluviometro.responsable_nombre.lowercase().contains(busqueda) ||
                        pluviometro.departamento.lowercase().contains(busqueda) ||
                        pluviometro.municipio.lowercase().contains(busqueda) ||
                        pluviometro.aldea.lowercase().contains(busqueda) ||
                        pluviometro.caserio_barrio_colonia?.lowercase()?.contains(busqueda) == true
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (UserSession.canCreatePluviometros()) {
                FloatingActionButton(
                    onClick = onAgregarPluviometro
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Pluviómetro")
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pluviómetros Registrados",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = if (textoBusqueda.isBlank()) {
                                    "${todosLosPluviometros.size} pluviómetros activos"
                                } else {
                                    "${pluviometrosFiltrados.size} de ${todosLosPluviometros.size} pluviómetros"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Badge de permisos
                        if (!UserSession.canCreatePluviometros()) {
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ CAMPO DE BÚSQUEDA EN TIEMPO REAL
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = {
                            textoBusqueda = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Buscar por código, responsable, departamento, municipio o aldea...")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (textoBusqueda.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        textoBusqueda = ""
                                        keyboardController?.hide() // ✅ Cerrar teclado al limpiar
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Limpiar búsqueda"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide() // ✅ Cerrar teclado al presionar Search
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Indicador de búsqueda activa
                    if (textoBusqueda.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Búsqueda activa: \"$textoBusqueda\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Lista de pluviómetros
            if (todosLosPluviometros.isEmpty()) {
                // No hay pluviómetros en total
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay pluviómetros registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (UserSession.canCreatePluviometros()) {
                        Text(
                            text = "Presiona el botón + para agregar el primer pluviómetro",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (pluviometrosFiltrados.isEmpty()) {
                // Hay pluviómetros pero ninguno coincide con la búsqueda
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No se encontraron resultados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Intenta con otra búsqueda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            textoBusqueda = ""
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar búsqueda")
                    }
                }
            } else {
                // Mostrar resultados
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = pluviometrosFiltrados,
                        key = { it.id }
                    ) { pluviometro ->
                        PluviometroCard(
                            pluviometro = pluviometro,
                            textoBusqueda = textoBusqueda,
                            onVerDetalles = {
                                keyboardController?.hide() // ✅ Cerrar teclado al ver detalles
                                onVerDetalles(pluviometro)
                            },
                            onEdit = {
                                keyboardController?.hide() // ✅ Cerrar teclado al editar
                                if (UserSession.canEditPluviometros()) {
                                    onEditarPluviometro(pluviometro)
                                } else {
                                    noPermissionMessage = "No tienes permisos para editar pluviómetros. Solo los Administradores y Voluntarios pueden realizar esta acción."
                                    showNoPermissionDialog = true
                                }
                            },
                            onDelete = {
                                keyboardController?.hide() // ✅ Cerrar teclado al eliminar
                                if (UserSession.canDeletePluviometros()) {
                                    viewModel.eliminarPluviometro(pluviometro.id)
                                } else {
                                    noPermissionMessage = "No tienes permisos para eliminar pluviómetros. Solo los Administradores pueden realizar esta acción."
                                    showNoPermissionDialog = true
                                }
                            },
                            canEdit = UserSession.canEditPluviometros(),
                            canDelete = UserSession.canDeletePluviometros()
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
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
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
fun PluviometroCard(
    pluviometro: Pluviometro,
    textoBusqueda: String = "",
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

                        // ✅ Indicador de coincidencia en código
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

                        // ✅ Indicador de coincidencia en ubicación
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

                        // ✅ Indicador de coincidencia en aldea
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
                            text = pluviometro.caserio_barrio_colonia,
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

                        // ✅ Indicador de coincidencia en responsable
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

                // Botones de acción
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
            title = { Text("Eliminar Pluviómetro") },
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