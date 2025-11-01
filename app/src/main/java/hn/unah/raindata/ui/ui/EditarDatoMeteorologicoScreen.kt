package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDatoMeteorologicoScreen(
    datoId: String,
    datoMeteorologicoViewModel: DatoMeteorologicoViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    pluviometroViewModel: PluviometroViewModel = viewModel(),
    onDatoActualizado: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val datoOriginal by datoMeteorologicoViewModel.obtenerDatoPorId(datoId).observeAsState()
    val todosLosVoluntarios by voluntarioViewModel.todosLosVoluntarios.observeAsState(emptyList())
    val todosLosPluviometros by pluviometroViewModel.todosLosPluviometros.observeAsState(emptyList())

    if (datoOriginal == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val dato = datoOriginal!!
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Filtrar solo voluntarios con tipo_usuario = "Voluntario"
    val voluntariosElegibles = remember(todosLosVoluntarios) {
        todosLosVoluntarios.filter {
            it.tipo_usuario?.equals("Voluntario", ignoreCase = true) == true
        }
    }

    // Estados del formulario (pre-cargados con datos existentes)
    var voluntarioSeleccionado by remember {
        mutableStateOf(voluntariosElegibles.find { it.id == dato.voluntario_id })
    }
    var expandedVoluntario by remember { mutableStateOf(false) }

    var pluviometroSeleccionado by remember {
        mutableStateOf(todosLosPluviometros.find { it.id == dato.pluviometro_id })
    }
    var expandedPluviometro by remember { mutableStateOf(false) }

    // Fecha y hora de lectura (editable)
    var fechaLectura by remember { mutableStateOf(dato.fecha_lectura) }
    var horaLectura by remember { mutableStateOf(dato.hora_lectura) }

    // Fecha y hora de registro (NO EDITABLE - mantener originales)
    val fechaRegistro = dato.fecha_registro
    val horaRegistro = dato.hora_registro

    var precipitacion by remember { mutableStateOf(dato.precipitacion.toString()) }
    var temperaturaMaxima by remember {
        mutableStateOf(dato.temperatura_maxima?.toString() ?: "")
    }
    var temperaturaMinima by remember {
        mutableStateOf(dato.temperatura_minima?.toString() ?: "")
    }

    // Condiciones del día
    var condicionesSeleccionadas by remember {
        mutableStateOf(
            dato.condiciones_dia.split("|").filter { it.isNotBlank() }.toSet()
        )
    }

    var observaciones by remember { mutableStateOf(dato.observaciones ?: "") }

    // Estados de error
    var errorVoluntario by remember { mutableStateOf<String?>(null) }
    var errorPluviometro by remember { mutableStateOf<String?>(null) }
    var errorFechaLectura by remember { mutableStateOf<String?>(null) }
    var errorHoraLectura by remember { mutableStateOf<String?>(null) }
    var errorPrecipitacion by remember { mutableStateOf<String?>(null) }
    var errorTempMax by remember { mutableStateOf<String?>(null) }
    var errorTempMin by remember { mutableStateOf<String?>(null) }
    var errorCoherenciaTemp by remember { mutableStateOf<String?>(null) }
    var errorCondiciones by remember { mutableStateOf<String?>(null) }
    var errorObservaciones by remember { mutableStateOf<String?>(null) }

    // Lista de condiciones del día
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

    // Filtrar pluviómetros por voluntario seleccionado
    val pluviometrosFiltrados = remember(voluntarioSeleccionado, todosLosPluviometros) {
        voluntarioSeleccionado?.let { voluntario ->
            todosLosPluviometros.filter { it.responsable_id == voluntario.id }
        } ?: emptyList()
    }

    // VALIDACIONES EN TIEMPO REAL
    LaunchedEffect(voluntarioSeleccionado) {
        errorVoluntario = if (voluntarioSeleccionado == null) {
            "Debe seleccionar un voluntario"
        } else null
    }

    LaunchedEffect(pluviometroSeleccionado) {
        errorPluviometro = if (pluviometroSeleccionado == null) {
            "Debe seleccionar un pluviómetro"
        } else null
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

    // Verificar si hay errores
    val hayErrores = errorVoluntario != null ||
            errorPluviometro != null ||
            errorFechaLectura != null ||
            errorHoraLectura != null ||
            errorPrecipitacion != null ||
            errorTempMax != null ||
            errorTempMin != null ||
            errorCoherenciaTemp != null ||
            errorCondiciones != null ||
            errorObservaciones != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Registro Meteorológico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
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
            // ID del registro (NO EDITABLE)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ID del Registro",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = dato.id,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Este ID no se puede modificar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Sección: Identificación
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Identificación",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Selector de voluntario
                    ExposedDropdownMenuBox(
                        expanded = expandedVoluntario,
                        onExpandedChange = { expandedVoluntario = it }
                    ) {
                        OutlinedTextField(
                            value = voluntarioSeleccionado?.nombre ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Voluntario *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVoluntario) },
                            isError = errorVoluntario != null,
                            supportingText = {
                                if (errorVoluntario != null) {
                                    Text(
                                        text = errorVoluntario!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
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
                                        pluviometroSeleccionado = null
                                        expandedVoluntario = false
                                    }
                                )
                            }
                        }
                    }

                    // Selector de pluviómetro
                    ExposedDropdownMenuBox(
                        expanded = expandedPluviometro,
                        onExpandedChange = {
                            if (voluntarioSeleccionado != null) {
                                expandedPluviometro = it
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = pluviometroSeleccionado?.let { "${it.numero_registro} - ${it.municipio}" } ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Pluviómetro *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPluviometro) },
                            enabled = voluntarioSeleccionado != null,
                            isError = errorPluviometro != null,
                            supportingText = {
                                when {
                                    errorPluviometro != null -> Text(
                                        text = errorPluviometro!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    voluntarioSeleccionado == null -> Text("Primero seleccione un voluntario")
                                    pluviometrosFiltrados.isEmpty() -> Text(
                                        "No hay pluviómetros para este voluntario",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPluviometro,
                            onDismissRequest = { expandedPluviometro = false }
                        ) {
                            pluviometrosFiltrados.forEach { pluviometro ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(pluviometro.numero_registro)
                                            Text(
                                                "${pluviometro.municipio}, ${pluviometro.departamento}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        pluviometroSeleccionado = pluviometro
                                        expandedPluviometro = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sección: Fecha y Hora de LECTURA (Editable)
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
                        text = "Momento cuando se realizó la lectura del pluviómetro",
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

            // Sección: Fecha y Hora de REGISTRO (No Editable)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Fecha y Hora de Registro Original",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "No se puede modificar",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = fechaRegistro,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hora:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = horaRegistro,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Sección: Datos Meteorológicos
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

            // Sección: Condiciones del Día (Checklist)
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
                        text = "Seleccione hasta 3 condiciones que mejor describan el día",
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

                    // Mostrar condiciones seleccionadas
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

                    // Lista de checkboxes
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

            // Sección: Observaciones
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

            // Botones
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

                            val datoActualizado = dato.copy(
                                voluntario_id = voluntarioSeleccionado!!.id,
                                voluntario_nombre = voluntarioSeleccionado!!.nombre,
                                pluviometro_id = pluviometroSeleccionado!!.id,
                                pluviometro_registro = pluviometroSeleccionado!!.numero_registro,
                                fecha_lectura = fechaLectura,
                                hora_lectura = horaLectura,
                                // Mantener fecha y hora de registro originales
                                precipitacion = precipitacionDouble,
                                temperatura_maxima = tempMaxDouble,
                                temperatura_minima = tempMinDouble,
                                condiciones_dia = condicionesSeleccionadas.joinToString("|"),
                                observaciones = observaciones.ifBlank { null }
                            )

                            datoMeteorologicoViewModel.actualizarDato(datoActualizado)
                            snackbarHostState.showSnackbar("✅ Dato meteorológico actualizado exitosamente")
                            onDatoActualizado()
                        }
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
                            errorVoluntario,
                            errorPluviometro,
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