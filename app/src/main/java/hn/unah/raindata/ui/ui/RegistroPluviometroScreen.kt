package hn.unah.raindata.ui.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
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
import java.util.Locale

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

    // Estados del formulario persistidos en ViewModel
    val direccion by pluviometroViewModel.direccionDraft.collectAsState()
    val departamento by pluviometroViewModel.departamentoDraft.collectAsState()
    val municipio by pluviometroViewModel.municipioDraft.collectAsState()
    val aldea by pluviometroViewModel.aldeaDraft.collectAsState()
    val caserioBarrioColonia by pluviometroViewModel.caserioDraft.collectAsState()
    val voluntarioSeleccionado by pluviometroViewModel.voluntarioDraft.collectAsState()
    val observaciones by pluviometroViewModel.observacionesDraft.collectAsState()
    val ubicacionSeleccionada by pluviometroViewModel.ubicacionDraft.collectAsState()
    val latitudTexto by pluviometroViewModel.latitudDraft.collectAsState()
    val longitudTexto by pluviometroViewModel.longitudDraft.collectAsState()

    var expandedVoluntario by remember { mutableStateOf(false) }
    var expandedDepartamento by remember { mutableStateOf(false) }
    var expandedMunicipio by remember { mutableStateOf(false) }

    // Estados de error
    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var errorLatitud by remember { mutableStateOf<String?>(null) }
    var errorLongitud by remember { mutableStateOf<String?>(null) }
    var errorCoordenadas by remember { mutableStateOf<String?>(null) }
    var errorAldea by remember { mutableStateOf<String?>(null) }
    var errorBarrio by remember { mutableStateOf<String?>(null) }
    var errorVoluntario by remember { mutableStateOf<String?>(null) }
    var errorDepartamento by remember { mutableStateOf<String?>(null) }
    var errorMunicipio by remember { mutableStateOf<String?>(null) }

    // Estados del mapa
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

    // Inicializar campos de texto de coordenadas si hay ubicación previa en borrador
    LaunchedEffect(Unit) {
        if (latitudTexto.isBlank() && ubicacionSeleccionada != null) {
            val latRedondeada = PluviometroViewModel.redondearA6Decimales(ubicacionSeleccionada!!.latitude)
            pluviometroViewModel.setLatitudDraft(String.format(Locale.US, "%.6f", latRedondeada))
        }
        if (longitudTexto.isBlank() && ubicacionSeleccionada != null) {
            val lngRedondeada = PluviometroViewModel.redondearA6Decimales(ubicacionSeleccionada!!.longitude)
            pluviometroViewModel.setLongitudDraft(String.format(Locale.US, "%.6f", lngRedondeada))
        }
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
                            val latRedondeada = PluviometroViewModel.redondearA6Decimales(it.latitude)
                            val lngRedondeada = PluviometroViewModel.redondearA6Decimales(it.longitude)
                            val latLng = LatLng(latRedondeada, lngRedondeada)
                            pluviometroViewModel.setUbicacionDraft(latLng)
                            pluviometroViewModel.setLatitudDraft(String.format(Locale.US, "%.6f", latRedondeada))
                            pluviometroViewModel.setLongitudDraft(String.format(Locale.US, "%.6f", lngRedondeada))
                            scope.launch {
                                try {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                } catch (e: Exception) {}
                            }
                        }
                    }
                } catch (e: SecurityException) {}
            }
        }
    }

    // Parseo numérico normalizado (coma a punto, evitando NumberFormatException)
    val latitudDouble = remember(latitudTexto) {
        latitudTexto.trim().replace(',', '.').toDoubleOrNull()
    }
    val longitudDouble = remember(longitudTexto) {
        longitudTexto.trim().replace(',', '.').toDoubleOrNull()
    }

    // FUNCIONES DE VALIDACIÓN
    fun validarDireccion(valor: String): String? {
        if (valor.isBlank()) return "La dirección es obligatoria"
        val regex = Regex("^[a-zA-Z0-9\\s.,#/-]+$")
        return if (!regex.matches(valor)) {
            "Solo se permiten letras, números, espacios y los caracteres: . , # / -"
        } else null
    }

    fun validarLongitudTexto(valor: String, campo: String, maxLongitud: Int = 50): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        if (valor.length > maxLongitud) return "$campo no puede exceder $maxLongitud caracteres"
        return null
    }

    // Validar en tiempo real
    LaunchedEffect(direccion) { errorDireccion = validarDireccion(direccion) }

    LaunchedEffect(latitudTexto) {
        errorLatitud = if (latitudTexto.isBlank()) {
            "La latitud es obligatoria"
        } else {
            pluviometroViewModel.validarLatitud(latitudDouble)
        }
    }

    LaunchedEffect(longitudTexto) {
        errorLongitud = if (longitudTexto.isBlank()) {
            "La longitud es obligatoria"
        } else {
            pluviometroViewModel.validarLongitud(longitudDouble)
        }
    }

    LaunchedEffect(latitudDouble, longitudDouble, errorLatitud, errorLongitud) {
        errorCoordenadas = when {
            latitudTexto.isBlank() || longitudTexto.isBlank() -> "Debe ingresar latitud y longitud"
            errorLatitud != null -> errorLatitud
            errorLongitud != null -> errorLongitud
            else -> pluviometroViewModel.validarCoordenadas(latitudDouble, longitudDouble)
        }
    }

    // Aviso opcional NO bloqueante si está fuera de Honduras
    val advertenciaHonduras = remember(latitudDouble, longitudDouble, errorLatitud, errorLongitud) {
        if (latitudDouble != null && longitudDouble != null && errorLatitud == null && errorLongitud == null) {
            if (pluviometroViewModel.estaFueraDeHonduras(latitudDouble, longitudDouble)) {
                "Estas coordenadas están fuera de Honduras. Verifique el signo de la longitud."
            } else null
        } else null
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
            pluviometroViewModel.setMunicipioDraft("")
        }
    }
    LaunchedEffect(municipio) {
        errorMunicipio = if (municipio.isBlank()) {
            "Debe seleccionar un municipio"
        } else null
    }

    // Verificar si hay errores bloqueantes
    val hayErrores = errorDireccion != null ||
            errorLatitud != null ||
            errorLongitud != null ||
            errorCoordenadas != null ||
            errorAldea != null ||
            errorBarrio != null ||
            errorVoluntario != null ||
            errorDepartamento != null ||
            errorMunicipio != null ||
            direccion.isBlank() ||
            latitudTexto.isBlank() ||
            longitudTexto.isBlank() ||
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
                        text = "🔒 Registrando como administrador: ${usuarioActual?.nombre ?: ""}",
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
                                            pluviometroViewModel.setVoluntarioDraft(voluntario)
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
                                        pluviometroViewModel.setDepartamentoDraft(depto)
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
                                            pluviometroViewModel.setMunicipioDraft(muni)
                                            expandedMunicipio = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = aldea,
                        onValueChange = { if (it.length <= 50) pluviometroViewModel.setAldeaDraft(it) },
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
                        onValueChange = { if (it.length <= 50) pluviometroViewModel.setCaserioDraft(it) },
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
                        onValueChange = { pluviometroViewModel.setDireccionDraft(it) },
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

                    // ===== CAMPOS DE COORDENADAS MANUALES (REQ. 4) =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latitudTexto,
                            onValueChange = { nuevoTexto ->
                                val textoFiltrado = pluviometroViewModel.filtrarEntradaCoordenada(nuevoTexto, esLatitud = true)
                                if (textoFiltrado != null) {
                                    pluviometroViewModel.setLatitudDraft(textoFiltrado)
                                    val lat = textoFiltrado.trim().replace(',', '.').toDoubleOrNull()
                                    val lng = longitudTexto.trim().replace(',', '.').toDoubleOrNull()
                                    if (lat != null && lat.isFinite() && lat in -90.0..90.0 &&
                                        lng != null && lng.isFinite() && lng in -180.0..180.0) {
                                        val nuevoLatLng = LatLng(lat, lng)
                                        pluviometroViewModel.setUbicacionDraft(nuevoLatLng)
                                        scope.launch {
                                            try {
                                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(nuevoLatLng))
                                            } catch (e: Exception) {}
                                        }
                                    } else {
                                        pluviometroViewModel.setUbicacionDraft(null)
                                    }
                                }
                            },
                            label = { Text("Latitud *") },
                            placeholder = { Text("Ej. 14.072300") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = errorLatitud != null,
                            supportingText = {
                                if (errorLatitud != null) {
                                    Text(
                                        text = errorLatitud!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text("Entre -90 y 90 (hasta 6 decimales)")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = longitudTexto,
                            onValueChange = { nuevoTexto ->
                                val textoFiltrado = pluviometroViewModel.filtrarEntradaCoordenada(nuevoTexto, esLatitud = false)
                                if (textoFiltrado != null) {
                                    pluviometroViewModel.setLongitudDraft(textoFiltrado)
                                    val lat = latitudTexto.trim().replace(',', '.').toDoubleOrNull()
                                    val lng = textoFiltrado.trim().replace(',', '.').toDoubleOrNull()
                                    if (lat != null && lat.isFinite() && lat in -90.0..90.0 &&
                                        lng != null && lng.isFinite() && lng in -180.0..180.0) {
                                        val nuevoLatLng = LatLng(lat, lng)
                                        pluviometroViewModel.setUbicacionDraft(nuevoLatLng)
                                        scope.launch {
                                            try {
                                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(nuevoLatLng))
                                            } catch (e: Exception) {}
                                        }
                                    } else {
                                        pluviometroViewModel.setUbicacionDraft(null)
                                    }
                                }
                            },
                            label = { Text("Longitud *") },
                            placeholder = { Text("Ej. -87.192100") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = errorLongitud != null,
                            supportingText = {
                                if (errorLongitud != null) {
                                    Text(
                                        text = errorLongitud!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text("Entre -180 y 180 (hasta 6 decimales)")
                                }
                            }
                        )
                    }

                    // Aviso opcional NO bloqueante (fuera de Honduras)
                    if (advertenciaHonduras != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ $advertenciaHonduras",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Botón para mapa / GPS
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
                                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                    try {
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            location?.let {
                                                val latRedondeada = PluviometroViewModel.redondearA6Decimales(it.latitude)
                                                val lngRedondeada = PluviometroViewModel.redondearA6Decimales(it.longitude)
                                                val latLng = LatLng(latRedondeada, lngRedondeada)
                                                pluviometroViewModel.setUbicacionDraft(latLng)
                                                pluviometroViewModel.setLatitudDraft(String.format(Locale.US, "%.6f", latRedondeada))
                                                pluviometroViewModel.setLongitudDraft(String.format(Locale.US, "%.6f", lngRedondeada))
                                                scope.launch {
                                                    try {
                                                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        }
                                    } catch (e: SecurityException) {}
                                }
                            } else {
                                mostrarMapa = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (mostrarMapa) "Ocultar mapa" else "Ver/seleccionar ubicación en mapa")
                    }

                    // Mapa interactivo
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
                                    val latRedondeada = PluviometroViewModel.redondearA6Decimales(latLng.latitude)
                                    val lngRedondeada = PluviometroViewModel.redondearA6Decimales(latLng.longitude)
                                    val nuevoLatLng = LatLng(latRedondeada, lngRedondeada)
                                    pluviometroViewModel.setUbicacionDraft(nuevoLatLng)
                                    pluviometroViewModel.setLatitudDraft(String.format(Locale.US, "%.6f", latRedondeada))
                                    pluviometroViewModel.setLongitudDraft(String.format(Locale.US, "%.6f", lngRedondeada))
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
                                text = "Coordenadas seleccionadas: Lat ${String.format(Locale.US, "%.6f", ubicacionSeleccionada!!.latitude)}, Lng ${String.format(Locale.US, "%.6f", ubicacionSeleccionada!!.longitude)}",
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
                onValueChange = { pluviometroViewModel.setObservacionesDraft(it) },
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
                                        pluviometroViewModel.limpiarDraft()
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
                    onClick = {
                        pluviometroViewModel.limpiarDraft()
                        onPluviometroGuardado()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
