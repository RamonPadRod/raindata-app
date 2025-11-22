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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.PluviometroViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPluviometrosScreen(
    viewModel: PluviometroViewModel,
    onAgregarPluviometro: () -> Unit = {},
    onVerDetalles: (Pluviometro) -> Unit = {},
    onEditarPluviometro: (Pluviometro) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ OBTENER DATOS CON FILTRO POR ROL
    val todosLosPluviometros by viewModel.pluviometros.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showNoPermissionDialog by remember { mutableStateOf(false) }
    var noPermissionMessage by remember { mutableStateOf("") }

    // Estado de búsqueda
    var textoBusqueda by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        if (UserSession.shouldFilterPluviometrosByUser()) {
            val uid = UserSession.getCurrentUserUid()
            uid?.let { viewModel.obtenerPluviometrosPorResponsable(it) }
        } else {
            viewModel.cargarPluviometros()
        }
    }
    // Filtrar pluviómetros en tiempo real
    val pluviometrosFiltrados = remember(todosLosPluviometros, textoBusqueda) {
        if (textoBusqueda.isBlank()) {
            todosLosPluviometros
        } else {
            val busqueda = textoBusqueda.trim().lowercase()
            todosLosPluviometros.filter { pluviometro ->
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (UserSession.shouldFilterPluviometrosByUser()) {
                                    "Mis Pluviómetros"
                                } else {
                                    "Pluviómetros Registrados"
                                },
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
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    UserSession.isAdmin() -> MaterialTheme.colorScheme.primaryContainer
                                    UserSession.isVoluntario() -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.tertiaryContainer
                                }
                            )
                        ) {
                            Text(
                                text = when {
                                    UserSession.isAdmin() -> "Admin"
                                    UserSession.isVoluntario() -> "Voluntario"
                                    else -> "Solo lectura"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo de búsqueda
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Buscar por código, responsable, departamento...")
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
                                        keyboardController?.hide()
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
                                keyboardController?.hide()
                            }
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

            // Contenido
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                todosLosPluviometros.isEmpty() -> {
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
                            text = if (UserSession.shouldFilterPluviometrosByUser()) {
                                "No tienes pluviómetros asignados"
                            } else {
                                "No hay pluviómetros registrados"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (UserSession.canCreatePluviometros()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Presiona el botón + para agregar el primer pluviómetro",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                pluviometrosFiltrados.isEmpty() -> {
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
                }

                else -> {
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
                                    keyboardController?.hide()
                                    onVerDetalles(pluviometro)
                                },
                                onEdit = {
                                    keyboardController?.hide()
                                    if (UserSession.canEditPluviometros()) {
                                        onEditarPluviometro(pluviometro)
                                    } else {
                                        noPermissionMessage = "No tienes permisos para editar pluviómetros."
                                        showNoPermissionDialog = true
                                    }
                                },
                                onDelete = {
                                    keyboardController?.hide()
                                    if (UserSession.canDeletePluviometros()) {
                                        scope.launch {
                                            viewModel.eliminarPluviometro(
                                                pluviometro.id,
                                                onSuccess = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("✅ Pluviómetro eliminado")
                                                    }
                                                },
                                                onError = { error ->
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("❌ Error: $error")
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        noPermissionMessage = "No tienes permisos para eliminar pluviómetros."
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
    }

    // Diálogo de sin permisos
    if (showNoPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNoPermissionDialog = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
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