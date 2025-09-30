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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
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
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPluviometroScreen(
    pluviometroViewModel: PluviometroViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    onPluviometroGuardado: () -> Unit = {}
) {
    val context = LocalContext.current
    val voluntarios by voluntarioViewModel.todosLosVoluntarios.observeAsState(emptyList())

    // Estados del formulario
    var numeroRegistro by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var aldea by remember { mutableStateOf("") }
    var caserioBarrioColonia by remember { mutableStateOf("") }
    var voluntarioSeleccionado by remember { mutableStateOf<Voluntario?>(null) }
    var expandedVoluntario by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf("") }

    // Estados del mapa
    var ubicacionSeleccionada by remember { mutableStateOf<LatLng?>(null) }
    var mostrarMapa by remember { mutableStateOf(false) }
    var permisoUbicacionConcedido by remember { mutableStateOf(false) }

    // Ubicación predeterminada (Tegucigalpa)
    val ubicacionPredeterminada = LatLng(14.0723, -87.1921)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionSeleccionada ?: ubicacionPredeterminada, 15f)
    }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permisoUbicacionConcedido = isGranted
        if (isGranted) {
            // Obtener ubicación actual
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

    // Verificar permisos al inicio
    LaunchedEffect(Unit) {
        permisoUbicacionConcedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Registro de Pluviómetro",
            style = MaterialTheme.typography.headlineMedium
        )

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

                OutlinedTextField(
                    value = numeroRegistro,
                    onValueChange = { numeroRegistro = it },
                    label = { Text("Número de Registro *") },
                    placeholder = { Text("Ej: PLU-001") },
                    modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedVoluntario,
                        onDismissRequest = { expandedVoluntario = false }
                    ) {
                        if (voluntarios.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay voluntarios registrados") },
                                onClick = { }
                            )
                        } else {
                            voluntarios.forEach { voluntario ->
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

                // Botón para seleccionar ubicación en mapa
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
                    Text(if (mostrarMapa) "Ocultar Mapa" else "Seleccionar Ubicación en Mapa")
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
                                isMyLocationEnabled = permisoUbicacionConcedido
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
                                    title = "Ubicación del Pluviómetro"
                                )
                            }
                        }
                    }

                    if (ubicacionSeleccionada != null) {
                        Text(
                            text = "Coordenadas seleccionadas:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Lat: ${String.format("%.6f", ubicacionSeleccionada!!.latitude)}, " +
                                    "Lng: ${String.format("%.6f", ubicacionSeleccionada!!.longitude)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = departamento,
                    onValueChange = { departamento = it },
                    label = { Text("Departamento *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = municipio,
                    onValueChange = { municipio = it },
                    label = { Text("Municipio *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aldea,
                    onValueChange = { aldea = it },
                    label = { Text("Aldea *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = caserioBarrioColonia,
                    onValueChange = { caserioBarrioColonia = it },
                    label = { Text("Caserío/Barrio/Colonia") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                    if (numeroRegistro.isNotBlank() &&
                        direccion.isNotBlank() &&
                        departamento.isNotBlank() &&
                        municipio.isNotBlank() &&
                        aldea.isNotBlank() &&
                        voluntarioSeleccionado != null &&
                        ubicacionSeleccionada != null) {

                        val pluviometro = Pluviometro(
                            numero_registro = numeroRegistro,
                            latitud = ubicacionSeleccionada!!.latitude,
                            longitud = ubicacionSeleccionada!!.longitude,
                            direccion = direccion,
                            departamento = departamento,
                            municipio = municipio,
                            aldea = aldea,
                            caserio_barrio_colonia = caserioBarrioColonia.ifBlank { null },
                            responsable_id = voluntarioSeleccionado!!.id,
                            responsable_nombre = voluntarioSeleccionado!!.nombre,
                            observaciones = observaciones.ifBlank { null }
                        )

                        pluviometroViewModel.guardarPluviometro(pluviometro)
                        onPluviometroGuardado()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = numeroRegistro.isNotBlank() &&
                        direccion.isNotBlank() &&
                        departamento.isNotBlank() &&
                        municipio.isNotBlank() &&
                        aldea.isNotBlank() &&
                        voluntarioSeleccionado != null &&
                        ubicacionSeleccionada != null
            ) {
                Text("Guardar")
            }

            OutlinedButton(
                onClick = {
                    numeroRegistro = ""
                    direccion = ""
                    departamento = ""
                    municipio = ""
                    aldea = ""
                    caserioBarrioColonia = ""
                    voluntarioSeleccionado = null
                    observaciones = ""
                    ubicacionSeleccionada = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }

            OutlinedButton(
                onClick = onPluviometroGuardado,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
        }

        if (ubicacionSeleccionada == null) {
            Text(
                text = "⚠️ Debes seleccionar una ubicación en el mapa",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}