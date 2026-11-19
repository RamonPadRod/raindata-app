package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.ui.ui.components.SyncStatusIcon
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import kotlinx.coroutines.launch
import hn.unah.raindata.data.session.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDatosMeteorologicosScreen(
    viewModel: DatoMeteorologicoViewModel,
    onAgregarDato: () -> Unit = {},
    onVerDetalles: (DatoMeteorologico) -> Unit = {},
    onEditarDato: (DatoMeteorologico) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val datos by viewModel.datosMeteorologicos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showNoPermissionDialog by remember { mutableStateOf(false) }
    var noPermissionMessage by remember { mutableStateOf("") }

    var textoBusqueda by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        if (UserSession.shouldFilterDatosMeteorologicosByUser()) {
            val uid = UserSession.getCurrentUserUid()
            uid?.let { viewModel.cargarDatosPorVoluntario(it) }
        } else {
            viewModel.cargarTodosDatos()
        }
    }

    val datosFiltrados = remember(datos, textoBusqueda) {
        if (textoBusqueda.isBlank()) {
            datos
        } else {
            val busqueda = textoBusqueda.trim().lowercase()
            datos.filter { dato ->
                dato.voluntario_nombre.lowercase().contains(busqueda) ||
                        dato.pluviometro_registro.lowercase().contains(busqueda) ||
                        dato.fecha_lectura.contains(busqueda) ||
                        dato.condiciones_dia.lowercase().contains(busqueda) ||
                        dato.observaciones?.lowercase()?.contains(busqueda) == true
            }
        }
    }

    var paginaActual by rememberSaveable { mutableStateOf(1) }
    val listState = rememberLazyListState()

    val pageSize = 10
    val totalItems = datosFiltrados.size
    val totalPaginas = maxOf(1, (totalItems + pageSize - 1) / pageSize)

    LaunchedEffect(textoBusqueda) {
        paginaActual = 1
    }

    LaunchedEffect(totalPaginas) {
        if (paginaActual > totalPaginas) {
            paginaActual = totalPaginas
        }
        if (paginaActual < 1) {
            paginaActual = 1
        }
    }

    val startIndex = (paginaActual - 1) * pageSize
    val endIndex = minOf(startIndex + pageSize, totalItems)
    val datosPaginados = if (startIndex < totalItems) {
        datosFiltrados.subList(startIndex, endIndex)
    } else {
        emptyList()
    }

    Scaffold(
        floatingActionButton = {
            if (UserSession.canCreateDatosMeteorologicos()) {
                FloatingActionButton(
                    onClick = onAgregarDato
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar dato meteorológico")
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (UserSession.shouldFilterDatosMeteorologicosByUser()) {
                                    "Mis datos meteorológicos"
                                } else {
                                    "Datos meteorológicos"
                                },
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = if (textoBusqueda.isBlank()) {
                                    "${datos.size} registros"
                                } else {
                                    "${datosFiltrados.size} de ${datos.size} registros"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

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

                    // Botón de sincronización manual
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.sincronizarManual(
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Datos sincronizados correctamente")
                                    }
                                },
                                onError = { err ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("❌ Error al sincronizar: $err")
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizando...")
                        } else {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subir datos pendientes")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Buscar por voluntario, pluviómetro, fecha...")
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
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                datos.isEmpty() -> {
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
                            text = if (UserSession.shouldFilterDatosMeteorologicosByUser()) {
                                "No tienes datos meteorológicos registrados"
                            } else {
                                "No hay datos meteorológicos registrados"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                datosFiltrados.isEmpty() -> {
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
                        Button(
                            onClick = {
                                textoBusqueda = ""
                                keyboardController?.hide()
                            }
                        ) {
                            Text("Limpiar búsqueda")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = datosPaginados,
                            key = { it.id }
                        ) { dato ->
                            DatoMeteorologicoCard(
                                dato = dato,
                                onVerDetalles = {
                                    keyboardController?.hide()
                                    onVerDetalles(dato)
                                },
                                onEdit = {
                                    keyboardController?.hide()
                                    onEditarDato(dato)
                                },
                                onDelete = {
                                    keyboardController?.hide()
                                    scope.launch {
                                        viewModel.eliminarDato(
                                            dato.id,
                                            onSuccess = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("✅ Dato eliminado")
                                                }
                                            },
                                            onError = { error ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("❌ Error: $error")
                                                }
                                            }
                                        )
                                    }
                                },
                                canEdit = UserSession.canEditDatosMeteorologicos(),
                                canDelete = UserSession.canDeleteDatosMeteorologicos()
                            )
                        }

                        if (totalItems > pageSize) {
                            item {
                                val desde = startIndex + 1
                                val hasta = endIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (paginaActual > 1) {
                                                paginaActual--
                                                scope.launch { listState.scrollToItem(0) }
                                            }
                                        },
                                        enabled = paginaActual > 1
                                    ) {
                                        Text("Anterior")
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Página $paginaActual de $totalPaginas",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mostrando $desde–$hasta de $totalItems",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (paginaActual < totalPaginas) {
                                                paginaActual++
                                                scope.launch { listState.scrollToItem(0) }
                                            }
                                        },
                                        enabled = paginaActual < totalPaginas
                                    ) {
                                        Text("Siguiente")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNoPermissionDialog = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            title = { Text("Permiso denegado") },
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
    onVerDetalles: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canEdit: Boolean,
    canDelete: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onVerDetalles,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // Ícono de Sincronización en la esquina superior izquierda
            SyncStatusIcon(
                status = dato.syncStatus,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lectura:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${dato.fecha_lectura} ${dato.hora_lectura}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Registro: ${dato.fecha_registro} ${dato.hora_registro}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pluviómetro: ${dato.pluviometro_registro}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Registrado por: ${dato.voluntario_nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fila 1: Precipitación y Temp Máxima
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Precipitación",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dato.precipitacion} mm",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (dato.temperatura_maxima != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    Icons.Default.ThermostatAuto,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Temp. máx",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${dato.temperatura_maxima}°C",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    // Fila 2: Temp Mínima y Condiciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (dato.temperatura_minima != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    Icons.Default.AcUnit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Temp. mín",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${dato.temperatura_minima}°C",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (dato.humedad_relativa != null) {
                        SuggestionChip(
                            onClick = onVerDetalles,
                            label = { Text("Hum. ${dato.humedad_relativa} %") }
                        )
                    }
                }
            }
        }

        // Diálogo de confirmación de eliminación
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                title = { Text("Eliminar dato meteorológico") },
                text = {
                    Column {
                        Text("¿Estás seguro de que deseas eliminar este registro?")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Fecha de lectura: ${dato.fecha_lectura}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Pluviómetro: ${dato.pluviometro_registro}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
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