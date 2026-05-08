package hn.unah.raindata.ui.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.utils.DepartamentosHonduras
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPluviometroScreen(
    pluviometroViewModel: PluviometroViewModel,
    voluntarioViewModel: VoluntarioViewModel,
    onPluviometroGuardado: () -> Unit = {},
    onAccesoDenegado: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ VERIFICAR PERMISOS DEL USUARIO ACTUAL
    val esAdministrador = UserSession.canCreatePluviometros()
    val usuarioActual = UserSession.getCurrentUser()

    // ✅ SI NO ES ADMIN, BLOQUEAR ACCESO
    if (!esAdministrador) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("❌ Solo los administradores pueden crear pluviómetros")
            onAccesoDenegado()
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚠️ Acceso denegado",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Solo los administradores pueden crear pluviómetros",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAccesoDenegado) {
                        Text("Volver")
                    }
                }
            }
        }
        return
    }

    // ===== SI ES ADMIN, MOSTRAR FORMULARIO =====

    val todosLosVoluntarios by voluntarioViewModel.voluntarios.collectAsState()
    val codigoGenerado by pluviometroViewModel.codigoGenerado.collectAsState()
    val isLoading by pluviometroViewModel.isLoading.collectAsState()

    // ✅ Filtrar voluntarios: solo tipo "Voluntario" y estado "Aprobado"
    val voluntariosElegibles = todosLosVoluntarios.filter {
        it.tipo_usuario == "Voluntario" && it.estado_aprobacion == "Aprobado"
    }



    // Estados del formulario
    var direccion by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var aldea by remember { mutableStateOf("") }
    var caserioBarrioColonia by remember { mutableStateOf("") }
    var voluntarioSeleccionado by remember { mutableStateOf<Voluntario?>(null) }
    var expandedVoluntario by remember { mutableStateOf(false) }
    var expandedDepartamento by remember { mutableStateOf(false) }
    var expandedMunicipio by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf("") }

    // Estados de error
    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var errorCoordenadas by remember { mutableStateOf<String?>(null) }
    var errorAldea by remember { mutableStateOf<String?>(null) }
    var errorBarrio by remember { mutableStateOf<String?>(null) }
    var errorVoluntario by remember { mutableStateOf<String?>(null) }
    var errorDepartamento by remember { mutableStateOf<String?>(null) }
    var errorMunicipio by remember { mutableStateOf<String?>(null) }

    // Estados del mapa
    var ubicacionSeleccionada by remember { mutableStateOf<LatLng?>(null) }
    var mostrarMapa by remember { mutableStateOf(false) }
    var permisoUbicacionConcedido by remember { mutableStateOf(false) }

    // Lista de municipios según departamento seleccionado
    val municipiosDisponibles = remember(departamento) {
        DepartamentosHonduras.obtenerMunicipios(departamento)
    }

    // Ubicación predeterminada (Tegucigalpa)
    val ubicacionPredeterminada = LatLng(14.0723, -87.1921)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionSeleccionada ?: ubicacionPredeterminada, 15f)
    }

    // ✅ GENERAR CÓDIGO AUTOMÁTICAMENTE
    LaunchedEffect(departamento, municipio) {
        if (departamento.isNotBlank() && municipio.isNotBlank()) {
            pluviometroViewModel.generarCodigoAutomatico(departamento, municipio)
        } else {
            pluviometroViewModel.limpiarCodigo()
        }
    }

    // Verificar permisos al inicio
    LaunchedEffect(Unit) {
        permisoUbicacionConcedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permisoUbicacionConcedido = isGranted
        if (isGranted) {
            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                try {
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (e: Exception) {}
            } else {
                mostrarMapa = true
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
                } catch (e: SecurityException) {}
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
        errorCoordenadas = validarCoordenadas(ubicacionSeleccionada?.latitude, ubicacionSeleccionada?.longitude)
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
        errorDepartamento = if (departamento.isBlank()) {
            "Debe seleccionar un departamento"
        } else null
        if (municipio.isNotEmpty() && !municipiosDisponibles.contains(municipio)) {
            municipio = ""
        }
    }
    LaunchedEffect(municipio) {
        errorMunicipio = if (municipio.isBlank()) {
            "Debe seleccionar un municipio"
        } else null
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
            voluntarioSeleccionado == null ||
            ubicacionSeleccionada == null ||
            codigoGenerado.isBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registro de pluviómetro",
                style = MaterialTheme.typography.headlineMedium
            )

            // ✅ INDICADOR: Solo administradores
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "🔒 Registrando como administrador: ${usuarioActual?.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        text = "Información del pluviómetro",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // CÓDIGO AUTOGENERADO
                    OutlinedTextField(
                        value = codigoGenerado,
                        onValueChange = { },
                        label = { Text("Código del pluviómetro") },
                        readOnly = true,
                        enabled = false,
                        supportingText = {
                            if (codigoGenerado.isBlank() && departamento.isNotBlank() && municipio.isNotBlank()) {
                                Text(
                                    text = "Generando código...",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (codigoGenerado.isNotBlank()) {
                                Text(
                                    text = "✓ Código generado automáticamente",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text("Seleccione departamento y municipio para generar el código")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ✅ SELECTOR DE VOLUNTARIO RESPONSABLE
                    ExposedDropdownMenuBox(
                        expanded = expandedVoluntario,
                        onExpandedChange = { expandedVoluntario = it }
                    ) {
                        OutlinedTextField(
                            value = voluntarioSeleccionado?.nombre ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Asignar a voluntario *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVoluntario) },
                            isError = errorVoluntario != null,
                            supportingText = {
                                if (errorVoluntario != null) {
                                    Text(
                                        text = errorVoluntario!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text("Seleccione el voluntario responsable de este pluviómetro")
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
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

            // Ubicación del Pluviómetro
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // DEPARTAMENTO
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
                                    Text(
                                        text = errorDepartamento!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (departamento.isNotBlank()) {
                                    Text(
                                        text = "Código: ${DepartamentosHonduras.obtenerCodigoDepartamento(departamento)}",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
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

                    // MUNICIPIO
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
                                    Text(
                                        text = errorMunicipio!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (departamento.isBlank()) {
                                    Text("Primero seleccione un departamento")
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMunicipio,
                            onDismissRequest = { expandedMunicipio = false }
                        ) {
                            if (municipiosDisponibles.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Seleccione primero un departamento") },
                                    onClick = { }
                                )
                            } else {
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
                                    Text(
                                        text = errorAldea!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
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
                        label = { Text("Caserío/Barrio/Colonia (opcional)") },
                        isError = errorBarrio != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorBarrio != null) {
                                    Text(
                                        text = errorBarrio!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
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
                                Text(
                                    text = errorDireccion!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botón para mapa
                    Button(
                        onClick = {
                            if (!mostrarMapa) {
                                val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                                if (!permisoUbicacionConcedido) {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                } else if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                                    try {
                                        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                    } catch (e: Exception) {}
                                } else {
                                    mostrarMapa = true
                                }
                            } else {
                                mostrarMapa = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (mostrarMapa) "Ocultar mapa" else "Seleccionar ubicación en mapa")
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
                                ubicacionSeleccionada?.let { ubicacion ->
                                    Marker(
                                        state = MarkerState(position = ubicacion),
                                        title = "Ubicación del pluviómetro"
                                    )
                                }
                            }
                        }

                        if (ubicacionSeleccionada != null) {
                            Text(
                                text = "Coordenadas: Lat ${String.format("%.6f", ubicacionSeleccionada!!.latitude)}, Lng ${String.format("%.6f", ubicacionSeleccionada!!.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (errorCoordenadas != null) {
                        Text(
                            text = errorCoordenadas!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val pluviometro = Pluviometro(
                                numero_registro = codigoGenerado,
                                latitud = ubicacionSeleccionada!!.latitude,
                                longitud = ubicacionSeleccionada!!.longitude,
                                direccion = direccion,
                                departamento = departamento,
                                municipio = municipio,
                                aldea = aldea,
                                caserio_barrio_colonia = caserioBarrioColonia.ifBlank { null },
                                responsable_uid = voluntarioSeleccionado!!.firebase_uid,
                                responsable_nombre = voluntarioSeleccionado!!.nombre,
                                observaciones = observaciones.ifBlank { null }
                            )

                            pluviometroViewModel.guardarPluviometro(
                                pluviometro,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Pluviómetro guardado exitosamente")
                                        onPluviometroGuardado()
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
                    modifier = Modifier.weight(1f),
                    enabled = !hayErrores && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar")
                    }
                }

                OutlinedButton(
                    onClick = onPluviometroGuardado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}