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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    onVerDetalles: (DatoMeteorologico) -> Unit = {},
    onEditarDato: (DatoMeteorologico) -> Unit = {}
) {
    val datos by viewModel.todosLosDatos.observeAsState(emptyList())
    var showNoPermissionDialog by remember { mutableStateOf(false) }
    var noPermissionMessage by remember { mutableStateOf("") }

    // Estado de búsqueda
    var textoBusqueda by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Filtrar datos en tiempo real
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
            // Header con búsqueda
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
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Datos Meteorológicos",
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo de búsqueda
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Buscar por voluntario, pluviómetro, fecha o condiciones...")
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
            } else if (datosFiltrados.isEmpty()) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(datosFiltrados) { dato ->
                        DatoMeteorologicoCard(
                            dato = dato,
                            onVerDetalles = {
                                keyboardController?.hide()
                                onVerDetalles(dato)
                            },
                            onEdit = {
                                keyboardController?.hide()
                                if (UserSession.canEditDatosMeteorologicos()) {
                                    onEditarDato(dato)
                                } else {
                                    noPermissionMessage = "No tienes permisos para editar datos meteorológicos. Solo los Administradores pueden realizar esta acción."
                                    showNoPermissionDialog = true
                                }
                            },
                            onDelete = {
                                keyboardController?.hide()
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✅ HEADER: Fechas organizadas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Fecha y hora de LECTURA (más prominente)
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



            // ✅ SECCIÓN: Pluviómetro y Voluntario
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

            // ✅ SECCIÓN: Datos meteorológicos simplificados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Precipitación
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                // Temperatura Máxima
                if (dato.temperatura_maxima != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ThermostatAuto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Temp. Máx",
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
                }

                // Temperatura Mínima
                if (dato.temperatura_minima != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AcUnit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Temp. Mín",
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
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ SECCIÓN: Condiciones del día (chips horizontales)
            val condiciones = dato.condiciones_dia.split("|").filter { it.isNotBlank() }
            if (condiciones.isNotEmpty()) {
                Column {
                    Text(
                        text = "Condiciones:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        condiciones.take(2).forEach { condicion ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = if (condicion.length > 18) condicion.take(18) + "..." else condicion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        if (condiciones.size > 2) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "+${condiciones.size - 2}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
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