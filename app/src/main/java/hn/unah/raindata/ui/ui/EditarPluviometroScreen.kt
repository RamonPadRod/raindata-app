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
import androidx.compose.material.icons.filled.ArrowBack
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
fun EditarPluviometroScreen(
    pluviometro: Pluviometro,
    pluviometroViewModel: PluviometroViewModel,
    voluntarioViewModel: VoluntarioViewModel,
    onPluviometroActualizado: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onAccesoDenegado: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val puedeEditar = UserSession.canEditPluviometros()

    if (!puedeEditar) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("❌ Solo los administradores pueden editar pluviómetros")
            onAccesoDenegado()
        }

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ Acceso denegado",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Solo los administradores pueden editar pluviómetros")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAccesoDenegado) {
                        Text("Volver")
                    }
                }
            }
        }
        return
    }

    val todosLosVoluntarios by voluntarioViewModel.voluntarios.collectAsState()
    val isLoading by pluviometroViewModel.isLoading.collectAsState()

    val voluntariosElegibles = todosLosVoluntarios.filter {
        it.tipo_usuario == "Voluntario" && it.estado_aprobacion == "Aprobado"
    }

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

    // Detección de "no modificado" para preservar los Double originales del pluviómetro
    val latitudOriginal = pluviometro.latitud
    val longitudOriginal = pluviometro.longitud

    val latitudTextoInicial = remember {
        String.format(Locale.US, "%.6f", latitudOriginal)
    }
    val longitudTextoInicial = remember {
        String.format(Locale.US, "%.6f", longitudOriginal)
    }

    var latitudTexto by remember { mutableStateOf(latitudTextoInicial) }
    var longitudTexto by remember { mutableStateOf(longitudTextoInicial) }
    var ubicacionSeleccionada by remember {
        mutableStateOf<LatLng?>(LatLng(latitudOriginal, longitudOriginal))
    }

    var coordenadasFueronModificadas by remember { mutableStateOf(false) }

    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var errorLatitud by remember { mutableStateOf<String?>(null) }
    var errorLongitud by remember { mutableStateOf<String?>(null) }
    var errorCoordenadas by remember { mutableStateOf<String?>(null) }
    var errorAldea by remember { mutableStateOf<String?>(null) }
    var errorBarrio by remember { mutableStateOf<String?>(null) }
    var errorVoluntario by remember { mutableStateOf<String?>(null) }
    var errorDepartamento by remember { mutableStateOf<String?>(null) }
    var errorMunicipio by remember { mutableStateOf<String?>(null) }

    var mostrarMapa by remember { mutableStateOf(false) }
    var permisoUbicacionConcedido by remember { mutableStateOf(false) }

    val municipiosDisponibles = remember(departamento) {
        DepartamentosHonduras.obtenerMunicipios(departamento)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionSeleccionada ?: LatLng(latitudOriginal, longitudOriginal), 15f)
    }

    LaunchedEffect(Unit) {
        permisoUbicacionConcedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val responsable = voluntariosElegibles.find { it.firebase_uid == pluviometro.responsable_uid }
        voluntarioSeleccionado = responsable
    }

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
                            coordenadasFueronModificadas = true
                            val latRedondeada = PluviometroViewModel.redondearA6Decimales(it.latitude)
                            val lngRedondeada = PluviometroViewModel.redondearA6Decimales(it.longitude)
                            val latLng = LatLng(latRedondeada, lngRedondeada)
                            ubicacionSeleccionada = latLng
                            latitudTexto = String.format(Locale.US, "%.6f", latRedondeada)
                            longitudTexto = String.format(Locale.US, "%.6f", lngRedondeada)
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

    // Parseo numérico normalizado
    val latitudDouble = remember(latitudTexto) {
        latitudTexto.trim().replace(',', '.').toDoubleOrNull()
    }
    val longitudDouble = remember(longitudTexto) {
        longitudTexto.trim().replace(',', '.').toDoubleOrNull()
    }

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

    // Aviso opcional NO bloqueante
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
        errorDepartamento = if (departamento.isBlank()) "Debe seleccionar un departamento" else null
        if (municipio.isNotEmpty() && !municipiosDisponibles.contains(municipio)) {
            municipio = ""
        }
    }
    LaunchedEffect(municipio) {
        errorMunicipio = if (municipio.isBlank()) "Debe seleccionar un municipio" else null
    }

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
            ubicacionSeleccionada == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar pluviómetro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Código del pluviómetro",
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información del pluviómetro",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedVoluntario,
                        onExpandedChange = { expandedVoluntario = it }
                    ) {
                        OutlinedTextField(
                            value = voluntarioSeleccionado?.nombre ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Responsable del pluviómetro *") },
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium
                    )

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
                        label = { Text("Caserío/Barrio/Colonia (opcional)") },
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
                                    if (textoFiltrado != latitudTextoInicial) {
                                        coordenadasFueronModificadas = true
                                    }
                                    latitudTexto = textoFiltrado
                                    val lat = textoFiltrado.trim().replace(',', '.').toDoubleOrNull()
                                    val lng = longitudTexto.trim().replace(',', '.').toDoubleOrNull()
                                    if (lat != null && lat.isFinite() && lat in -90.0..90.0 &&
                                        lng != null && lng.isFinite() && lng in -180.0..180.0) {
                                        val nuevoLatLng = LatLng(lat, lng)
                                        ubicacionSeleccionada = nuevoLatLng
                                        scope.launch {
                                            try {
                                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(nuevoLatLng))
                                            } catch (e: Exception) {}
                                        }
                                    } else {
                                        ubicacionSeleccionada = null
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
                                    if (textoFiltrado != longitudTextoInicial) {
                                        coordenadasFueronModificadas = true
                                    }
                                    longitudTexto = textoFiltrado
                                    val lat = latitudTexto.trim().replace(',', '.').toDoubleOrNull()
                                    val lng = textoFiltrado.trim().replace(',', '.').toDoubleOrNull()
                                    if (lat != null && lat.isFinite() && lat in -90.0..90.0 &&
                                        lng != null && lng.isFinite() && lng in -180.0..180.0) {
                                        val nuevoLatLng = LatLng(lat, lng)
                                        ubicacionSeleccionada = nuevoLatLng
                                        scope.launch {
                                            try {
                                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(nuevoLatLng))
                                            } catch (e: Exception) {}
                                        }
                                    } else {
                                        ubicacionSeleccionada = null
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
                                                coordenadasFueronModificadas = true
                                                val latRedondeada = PluviometroViewModel.redondearA6Decimales(it.latitude)
                                                val lngRedondeada = PluviometroViewModel.redondearA6Decimales(it.longitude)
                                                val latLng = LatLng(latRedondeada, lngRedondeada)
                                                ubicacionSeleccionada = latLng
                                                latitudTexto = String.format(Locale.US, "%.6f", latRedondeada)
                                                longitudTexto = String.format(Locale.US, "%.6f", lngRedondeada)
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
                        Text(if (mostrarMapa) "Ocultar mapa" else "Ver/modificar ubicación en mapa")
                    }

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
                                    coordenadasFueronModificadas = true
                                    val latRedondeada = PluviometroViewModel.redondearA6Decimales(latLng.latitude)
                                    val lngRedondeada = PluviometroViewModel.redondearA6Decimales(latLng.longitude)
                                    val nuevoLatLng = LatLng(latRedondeada, lngRedondeada)
                                    ubicacionSeleccionada = nuevoLatLng
                                    latitudTexto = String.format(Locale.US, "%.6f", latRedondeada)
                                    longitudTexto = String.format(Locale.US, "%.6f", lngRedondeada)
                                }
                            ) {
                                ubicacionSeleccionada?.let { pos ->
                                    Marker(
                                        state = MarkerState(position = pos),
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

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val huboCambioCoordenadas = coordenadasFueronModificadas ||
                                    latitudTexto != latitudTextoInicial ||
                                    longitudTexto != longitudTextoInicial

                            val latFinal = if (huboCambioCoordenadas) ubicacionSeleccionada!!.latitude else latitudOriginal
                            val lngFinal = if (huboCambioCoordenadas) ubicacionSeleccionada!!.longitude else longitudOriginal

                            val pluviometroActualizado = pluviometro.copy(
                                latitud = latFinal,
                                longitud = lngFinal,
                                direccion = direccion,
                                departamento = departamento,
                                municipio = municipio,
                                aldea = aldea,
                                caserio_barrio_colonia = caserioBarrioColonia.ifBlank { null },
                                responsable_uid = voluntarioSeleccionado!!.firebase_uid,
                                responsable_nombre = voluntarioSeleccionado!!.nombre,
                                observaciones = observaciones.ifBlank { null }
                            )

                            pluviometroViewModel.actualizarPluviometro(
                                pluviometroActualizado,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Pluviómetro actualizado")
                                        onPluviometroActualizado()
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
                        Text("Guardar cambios")
                    }
                }

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
