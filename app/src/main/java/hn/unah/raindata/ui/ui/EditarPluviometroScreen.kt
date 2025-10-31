package hn.unah.raindata.ui.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.utils.DepartamentosHonduras
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPluviometroScreen(
    pluviometro: Pluviometro,
    pluviometroViewModel: PluviometroViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    onPluviometroActualizado: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val todosLosVoluntarios by voluntarioViewModel.todosLosVoluntarios.observeAsState(emptyList())

    // Filtrar solo voluntarios con tipo_usuario = "Voluntario"
    val voluntariosElegibles = todosLosVoluntarios.filter {
        it.tipo_usuario?.equals("Voluntario", ignoreCase = true) == true
    }

    // Estados del formulario (precargados con los datos del pluviómetro)
    var direccion by remember { mutableStateOf(pluviometro.direccion) }
    var departamento by remember { mutableStateOf(pluviometro.departamento) }
    var municipio by remember { mutableStateOf(pluviometro.municipio) }
    var aldea by remember { mutableStateOf(pluviometro.aldea) }
    var caserioBarrioColonia by remember { mutableStateOf(pluviometro.caserio_barrio_colonia ?: "") }
    var voluntarioSeleccionado by remember { mutableStateOf<Voluntario?>(null) }
    var expandedVoluntario by remember { mutableStateOf(false) }
    var expandedDepartamento by remember { mutableStateOf(false) }
    var expandedMunicipio by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf(pluviometro.observaciones ?: "") }

    // Estados de error
    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var errorCoordenadas by remember { mutableStateOf<String?>(null) }
    var errorAldea by remember { mutableStateOf<String?>(null) }
    var errorBarrio by remember { mutableStateOf<String?>(null) }
    var errorVoluntario by remember { mutableStateOf<String?>(null) }
    var errorDepartamento by remember { mutableStateOf<String?>(null) }
    var errorMunicipio by remember { mutableStateOf<String?>(null) }

    // Estados del mapa (precargado con ubicación actual)
    var ubicacionSeleccionada by remember {
        mutableStateOf(LatLng(pluviometro.latitud, pluviometro.longitud))
    }
    var mostrarMapa by remember { mutableStateOf(false) }
    var permisoUbicacionConcedido by remember { mutableStateOf(false) }

    // Lista de municipios según departamento seleccionado
    val municipiosDisponibles = remember(departamento) {
        DepartamentosHonduras.obtenerMunicipios(departamento)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionSeleccionada, 15f)
    }

    // Cargar el voluntario actual
    LaunchedEffect(Unit) {
        permisoUbicacionConcedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Buscar y asignar el voluntario responsable actual
        val responsable = voluntariosElegibles.find { it.id == pluviometro.responsable_id }
        voluntarioSeleccionado = responsable
    }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permisoUbicacionConcedido = isGranted
        if (isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        ubicacionSeleccionada = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude), 15f
                        )
                    }
                }
            } catch (e: SecurityException) {
                // Manejar error
            }
        }
    }

    // FUNCIONES DE VALIDACIÓN
    fun validarDireccion(valor: String): String? {
        if (valor.isBlank()) return "La dirección es obligatoria"
        val regex = Regex("^[a-zA-Z0-9\\s.,#/-]+$")
        return if (!regex.matches(valor)) {
            "Solo se permiten letras, números, espacios y los caracteres: . , # / -"
        } else null
    }

    fun validarCoordenadas(lat: Double?, lng: Double?): String? {
        if (lat == null || lng == null) return "Debe seleccionar una ubicación en el mapa"
        if (lat !in 13.0..16.0) return "Latitud fuera del rango de Honduras (13° a 16°)"
        if (lng !in -89.0..-83.0) return "Longitud fuera del rango de Honduras (-89° a -83°)"
        return null
    }

    fun validarLongitudTexto(valor: String, campo: String, maxLongitud: Int = 50): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        if (valor.length > maxLongitud) return "$campo no puede exceder $maxLongitud caracteres"
        return null
    }

    // Validar en tiempo real
    LaunchedEffect(direccion) { errorDireccion = validarDireccion(direccion) }
    LaunchedEffect(ubicacionSeleccionada) {
        errorCoordenadas = validarCoordenadas(ubicacionSeleccionada.latitude, ubicacionSeleccionada.longitude)
    }
    LaunchedEffect(aldea) { errorAldea = validarLongitudTexto(aldea, "Aldea") }
    LaunchedEffect(caserioBarrioColonia) {
        if (caserioBarrioColonia.isNotBlank()) {
            errorBarrio = if (caserioBarrioColonia.length > 50) {
                "Caserío/Barrio/Colonia no puede exceder 50 caracteres"
            } else null
        } else {
            errorBarrio = null
        }
    }
    LaunchedEffect(voluntarioSeleccionado) {
        errorVoluntario = if (voluntarioSeleccionado == null) {
            "Debe seleccionar un voluntario responsable"
        } else null
    }
    LaunchedEffect(departamento) {
        errorDepartamento = if (departamento.isBlank()) "Debe seleccionar un departamento" else null
        if (municipio.isNotEmpty() && !municipiosDisponibles.contains(municipio)) {
            municipio = ""
        }
    }
    LaunchedEffect(municipio) {
        errorMunicipio = if (municipio.isBlank()) "Debe seleccionar un municipio" else null
    }

    // Verificar si hay errores
    val hayErrores = errorDireccion != null ||
            errorCoordenadas != null ||
            errorAldea != null ||
            errorBarrio != null ||
            errorVoluntario != null ||
            errorDepartamento != null ||
            errorMunicipio != null ||
            direccion.isBlank() ||
            departamento.isBlank() ||
            municipio.isBlank() ||
            aldea.isBlank() ||
            voluntarioSeleccionado == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Pluviómetro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Código del pluviómetro (NO EDITABLE)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Código del Pluviómetro",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = pluviometro.numero_registro,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Este código no se puede modificar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Información Básica
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información del Pluviómetro",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Selector de voluntario responsable
                    ExposedDropdownMenuBox(
                        expanded = expandedVoluntario,
                        onExpandedChange = { expandedVoluntario = it }
                    ) {
                        OutlinedTextField(
                            value = voluntarioSeleccionado?.nombre ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Responsable del Pluviómetro *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVoluntario) },
                            isError = errorVoluntario != null,
                            supportingText = {
                                if (errorVoluntario != null) {
                                    Text(text = errorVoluntario!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedVoluntario,
                            onDismissRequest = { expandedVoluntario = false }
                        ) {
                            if (voluntariosElegibles.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay voluntarios disponibles") },
                                    onClick = { }
                                )
                            } else {
                                voluntariosElegibles.forEach { voluntario ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(voluntario.nombre)
                                                Text(
                                                    "${voluntario.municipio}, ${voluntario.departamento}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            voluntarioSeleccionado = voluntario
                                            expandedVoluntario = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ubicación
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Departamento
                    ExposedDropdownMenuBox(
                        expanded = expandedDepartamento,
                        onExpandedChange = { expandedDepartamento = it }
                    ) {
                        OutlinedTextField(
                            value = departamento,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Departamento *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDepartamento) },
                            isError = errorDepartamento != null,
                            supportingText = {
                                if (errorDepartamento != null) {
                                    Text(text = errorDepartamento!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDepartamento,
                            onDismissRequest = { expandedDepartamento = false }
                        ) {
                            DepartamentosHonduras.departamentos.forEach { depto ->
                                DropdownMenuItem(
                                    text = { Text(depto) },
                                    onClick = {
                                        departamento = depto
                                        expandedDepartamento = false
                                    }
                                )
                            }
                        }
                    }

                    // Municipio
                    ExposedDropdownMenuBox(
                        expanded = expandedMunicipio,
                        onExpandedChange = { expandedMunicipio = it }
                    ) {
                        OutlinedTextField(
                            value = municipio,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Municipio *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMunicipio) },
                            enabled = departamento.isNotBlank(),
                            isError = errorMunicipio != null,
                            supportingText = {
                                if (errorMunicipio != null) {
                                    Text(text = errorMunicipio!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMunicipio,
                            onDismissRequest = { expandedMunicipio = false }
                        ) {
                            municipiosDisponibles.forEach { muni ->
                                DropdownMenuItem(
                                    text = { Text(muni) },
                                    onClick = {
                                        municipio = muni
                                        expandedMunicipio = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = aldea,
                        onValueChange = { if (it.length <= 50) aldea = it },
                        label = { Text("Aldea * (máx. 50 caracteres)") },
                        isError = errorAldea != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorAldea != null) {
                                    Text(text = errorAldea!!, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${aldea.length}/50",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = caserioBarrioColonia,
                        onValueChange = { if (it.length <= 50) caserioBarrioColonia = it },
                        label = { Text("Caserío/Barrio/Colonia (máx. 50 caracteres)") },
                        isError = errorBarrio != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorBarrio != null) {
                                    Text(text = errorBarrio!!, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${caserioBarrioColonia.length}/50",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección *") },
                        isError = errorDireccion != null,
                        supportingText = {
                            if (errorDireccion != null) {
                                Text(text = errorDireccion!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botón para mapa
                    Button(
                        onClick = {
                            if (!permisoUbicacionConcedido) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            mostrarMapa = !mostrarMapa
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (mostrarMapa) "Ocultar Mapa" else "Modificar Ubicación en Mapa")
                    }

                    // Mapa
                    if (mostrarMapa) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    isMyLocationEnabled = permisoUbicacionConcedido,
                                    mapType = MapType.SATELLITE
                                ),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = true,
                                    myLocationButtonEnabled = permisoUbicacionConcedido
                                ),
                                onMapClick = { latLng ->
                                    ubicacionSeleccionada = latLng
                                }
                            ) {
                                Marker(
                                    state = MarkerState(position = ubicacionSeleccionada),
                                    title = "Ubicación del Pluviómetro"
                                )
                            }
                        }

                        Text(
                            text = "Coordenadas: ${String.format("%.6f", ubicacionSeleccionada.latitude)}, ${String.format("%.6f", ubicacionSeleccionada.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Observaciones
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Observaciones",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones adicionales") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val pluviometroActualizado = pluviometro.copy(
                            latitud = ubicacionSeleccionada.latitude,
                            longitud = ubicacionSeleccionada.longitude,
                            direccion = direccion,
                            departamento = departamento,
                            municipio = municipio,
                            aldea = aldea,
                            caserio_barrio_colonia = caserioBarrioColonia.ifBlank { null },
                            responsable_id = voluntarioSeleccionado!!.id,
                            responsable_nombre = voluntarioSeleccionado!!.nombre,
                            observaciones = observaciones.ifBlank { null }
                        )

                        pluviometroViewModel.actualizarPluviometro(pluviometroActualizado)
                        onPluviometroActualizado()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !hayErrores
                ) {
                    Text("Guardar Cambios")
                }

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }

            // Resumen de errores
            if (hayErrores) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠️ Corrija los siguientes errores:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        listOfNotNull(
                            errorDireccion,
                            errorCoordenadas,
                            errorAldea,
                            errorBarrio,
                            errorVoluntario,
                            errorDepartamento,
                            errorMunicipio
                        ).forEach { error ->
                            Text(
                                text = "• $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}