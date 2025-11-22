package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDatoMeteorologicoScreen(
    datoId: String,
    datoMeteorologicoViewModel: DatoMeteorologicoViewModel,
    onDatoActualizado: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val dato by datoMeteorologicoViewModel.datoMeteorologico.collectAsState()
    val isLoading by datoMeteorologicoViewModel.isLoading.collectAsState()

    var fechaLectura by remember { mutableStateOf("") }
    var horaLectura by remember { mutableStateOf("") }
    var precipitacion by remember { mutableStateOf("") }
    var temperaturaMaxima by remember { mutableStateOf("") }
    var temperaturaMinima by remember { mutableStateOf("") }
    var condicionesSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var observaciones by remember { mutableStateOf("") }

    var errorFechaLectura by remember { mutableStateOf<String?>(null) }
    var errorHoraLectura by remember { mutableStateOf<String?>(null) }
    var errorPrecipitacion by remember { mutableStateOf<String?>(null) }
    var errorTempMax by remember { mutableStateOf<String?>(null) }
    var errorTempMin by remember { mutableStateOf<String?>(null) }
    var errorCoherenciaTemp by remember { mutableStateOf<String?>(null) }
    var errorCondiciones by remember { mutableStateOf<String?>(null) }
    var errorObservaciones by remember { mutableStateOf<String?>(null) }

    val condicionesDia = listOf(
        "Día despejado",
        "Día parcialmente nublado",
        "Día nublado",
        "Día con vientos calmados",
        "Fuertes vientos",
        "Fuertes vientos con lluvia",
        "Día con lluvias",
        "Lluvia con granizo",
        "Tormenta eléctrica",
        "Neblina",
        "Día con bruma (smog)",
        "Calor más de lo normal",
        "Sensación de frío más de lo normal"
    )

    LaunchedEffect(datoId) {
        datoMeteorologicoViewModel.cargarDatoPorId(datoId)
    }

    LaunchedEffect(dato) {
        dato?.let {
            fechaLectura = it.fecha_lectura
            horaLectura = it.hora_lectura
            precipitacion = it.precipitacion.toString()
            temperaturaMaxima = it.temperatura_maxima?.toString() ?: ""
            temperaturaMinima = it.temperatura_minima?.toString() ?: ""
            condicionesSeleccionadas = it.condiciones_dia.split("|").filter { cond -> cond.isNotBlank() }.toSet()
            observaciones = it.observaciones ?: ""
        }
    }

    LaunchedEffect(fechaLectura) {
        errorFechaLectura = datoMeteorologicoViewModel.validarFechaLectura(fechaLectura)
    }

    LaunchedEffect(horaLectura) {
        errorHoraLectura = datoMeteorologicoViewModel.validarHora(horaLectura)
    }

    LaunchedEffect(precipitacion) {
        errorPrecipitacion = datoMeteorologicoViewModel.validarPrecipitacion(precipitacion)
    }

    LaunchedEffect(temperaturaMaxima) {
        errorTempMax = datoMeteorologicoViewModel.validarTemperaturaMaxima(temperaturaMaxima)
    }

    LaunchedEffect(temperaturaMinima) {
        errorTempMin = datoMeteorologicoViewModel.validarTemperaturaMinima(temperaturaMinima)
    }

    LaunchedEffect(temperaturaMinima, temperaturaMaxima) {
        errorCoherenciaTemp = datoMeteorologicoViewModel.validarCoherenciaTemperaturas(
            temperaturaMinima,
            temperaturaMaxima
        )
    }

    LaunchedEffect(condicionesSeleccionadas) {
        errorCondiciones = datoMeteorologicoViewModel.validarCondicionesDia(condicionesSeleccionadas.toList())
    }

    LaunchedEffect(observaciones) {
        errorObservaciones = datoMeteorologicoViewModel.validarObservaciones(observaciones)
    }

    val hayErrores = errorFechaLectura != null ||
            errorHoraLectura != null ||
            errorPrecipitacion != null ||
            errorTempMax != null ||
            errorTempMin != null ||
            errorCoherenciaTemp != null ||
            errorCondiciones != null ||
            errorObservaciones != null

    if (isLoading || dato == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val datoActual = dato!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Dato Meteorológico") },
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
            Text(
                text = "Editar Dato Meteorológico",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información del Registro",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pluviómetro: ${datoActual.pluviometro_registro}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Voluntario: ${datoActual.voluntario_nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Fecha de registro: ${datoActual.fecha_registro} ${datoActual.hora_registro}",
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
                        text = "Fecha y Hora de Lectura",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Editable: ±7 días desde la fecha original",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = fechaLectura,
                        onValueChange = { fechaLectura = it },
                        label = { Text("Fecha de Lectura (yyyy-MM-dd) *") },
                        placeholder = { Text("2025-10-31") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        isError = errorFechaLectura != null,
                        supportingText = {
                            if (errorFechaLectura != null) {
                                Text(
                                    text = errorFechaLectura!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("Se permite ±7 días desde hoy")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = horaLectura,
                        onValueChange = { horaLectura = it },
                        label = { Text("Hora de Lectura (HH:mm) *") },
                        placeholder = { Text("14:30") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        isError = errorHoraLectura != null,
                        supportingText = {
                            if (errorHoraLectura != null) {
                                Text(
                                    text = errorHoraLectura!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Datos Meteorológicos",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = precipitacion,
                        onValueChange = {
                            if (it.length <= 6) {
                                precipitacion = it.filter { char -> char.isDigit() || char == '.' }
                            }
                        },
                        label = { Text("Precipitación (mm) *") },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorPrecipitacion != null,
                        supportingText = {
                            if (errorPrecipitacion != null) {
                                Text(
                                    text = errorPrecipitacion!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("Rango: 0 - 500 mm")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = temperaturaMaxima,
                        onValueChange = {
                            if (it.length <= 5) {
                                temperaturaMaxima = it.filter { char -> char.isDigit() || char == '.' || char == '-' }
                            }
                        },
                        label = { Text("Temperatura Máxima (°C) - Opcional") },
                        placeholder = { Text("30.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorTempMax != null,
                        supportingText = {
                            if (errorTempMax != null) {
                                Text(
                                    text = errorTempMax!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("Rango: 10°C - 50°C")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = temperaturaMinima,
                        onValueChange = {
                            if (it.length <= 5) {
                                temperaturaMinima = it.filter { char -> char.isDigit() || char == '.' || char == '-' }
                            }
                        },
                        label = { Text("Temperatura Mínima (°C) - Opcional") },
                        placeholder = { Text("18.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorTempMin != null || errorCoherenciaTemp != null,
                        supportingText = {
                            if (errorCoherenciaTemp != null) {
                                Text(
                                    text = errorCoherenciaTemp!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (errorTempMin != null) {
                                Text(
                                    text = errorTempMin!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("Rango: -5°C - 40°C")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Condiciones del Día",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Seleccione hasta 3 condiciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (errorCondiciones != null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (errorCondiciones != null) {
                        Text(
                            text = errorCondiciones!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (condicionesSeleccionadas.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Seleccionadas (${condicionesSeleccionadas.size}/3):",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                condicionesSeleccionadas.forEach { condicion ->
                                    Text(
                                        text = "• $condicion",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    condicionesDia.forEach { condicion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = condicionesSeleccionadas.contains(condicion),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        if (condicionesSeleccionadas.size < 3) {
                                            condicionesSeleccionadas = condicionesSeleccionadas + condicion
                                        }
                                    } else {
                                        condicionesSeleccionadas = condicionesSeleccionadas - condicion
                                    }
                                },
                                enabled = condicionesSeleccionadas.contains(condicion) || condicionesSeleccionadas.size < 3
                            )
                            Text(
                                text = condicion,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Observaciones",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = {
                            if (it.length <= 500) {
                                observaciones = it
                            }
                        },
                        label = { Text("Observaciones adicionales (opcional)") },
                        isError = errorObservaciones != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorObservaciones != null) {
                                    Text(
                                        text = errorObservaciones!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${observaciones.length}/500",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val precipitacionDouble = precipitacion.toDoubleOrNull() ?: 0.0
                            val tempMaxDouble = temperaturaMaxima.toDoubleOrNull()
                            val tempMinDouble = temperaturaMinima.toDoubleOrNull()

                            val datoActualizado = datoActual.copy(
                                fecha_lectura = fechaLectura,
                                hora_lectura = horaLectura,
                                precipitacion = precipitacionDouble,
                                temperatura_maxima = tempMaxDouble,
                                temperatura_minima = tempMinDouble,
                                condiciones_dia = condicionesSeleccionadas.joinToString("|"),
                                observaciones = observaciones.ifBlank { null }
                            )

                            datoMeteorologicoViewModel.actualizarDato(
                                datoActualizado,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Dato actualizado")
                                        onDatoActualizado()
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
                        Text("Guardar Cambios")
                    }
                }

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }

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
                            errorFechaLectura,
                            errorHoraLectura,
                            errorPrecipitacion,
                            errorTempMax,
                            errorTempMin,
                            errorCoherenciaTemp,
                            errorCondiciones,
                            errorObservaciones
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