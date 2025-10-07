package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDatoMeteorologicoScreen(
    datoMeteorologicoViewModel: DatoMeteorologicoViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    pluviometroViewModel: PluviometroViewModel = viewModel(),
    onDatoGuardado: () -> Unit = {},
    onNavegarARegistroPluviometro: () -> Unit = {}
) {
    val todosLosVoluntarios by voluntarioViewModel.todosLosVoluntarios.observeAsState(emptyList())
    val todosLosPluviometros by pluviometroViewModel.todosLosPluviometros.observeAsState(emptyList())

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Filtrar solo voluntarios con tipo_usuario = "Voluntario" (sin modificar el DAO)
    val voluntariosElegibles = remember(todosLosVoluntarios) {
        todosLosVoluntarios.filter {
            it.tipo_usuario?.equals("Voluntario", ignoreCase = true) == true
        }
    }

    // Estados del formulario
    var voluntarioSeleccionado by remember { mutableStateOf<Voluntario?>(null) }
    var expandedVoluntario by remember { mutableStateOf(false) }

    var pluviometroSeleccionado by remember { mutableStateOf<Pluviometro?>(null) }
    var expandedPluviometro by remember { mutableStateOf(false) }

    var fecha by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var hora by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var precipitacion by remember { mutableStateOf("") }
    var temperaturaMaxima by remember { mutableStateOf("") }
    var temperaturaMinima by remember { mutableStateOf("") }
    var condicionDia by remember { mutableStateOf("") }
    var expandedCondicion by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf("") }

    // Estados de error
    var errorVoluntario by remember { mutableStateOf<String?>(null) }
    var errorPluviometro by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }
    var errorHora by remember { mutableStateOf<String?>(null) }
    var errorPrecipitacion by remember { mutableStateOf<String?>(null) }
    var errorTempMax by remember { mutableStateOf<String?>(null) }
    var errorTempMin by remember { mutableStateOf<String?>(null) }
    var errorCoherenciaTemp by remember { mutableStateOf<String?>(null) }
    var errorCondicion by remember { mutableStateOf<String?>(null) }
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

    // Validar voluntario
    LaunchedEffect(voluntarioSeleccionado) {
        errorVoluntario = if (voluntarioSeleccionado == null) {
            "Debe seleccionar un voluntario"
        } else null
    }

    // Validar pluviómetro
    LaunchedEffect(pluviometroSeleccionado) {
        errorPluviometro = if (pluviometroSeleccionado == null) {
            "Debe seleccionar un pluviómetro"
        } else null
    }

    // Validar fecha
    LaunchedEffect(fecha) {
        errorFecha = datoMeteorologicoViewModel.validarFecha(fecha)
    }

    // Validar hora
    LaunchedEffect(hora) {
        errorHora = datoMeteorologicoViewModel.validarHora(hora)
    }

    // Validar precipitación
    LaunchedEffect(precipitacion) {
        errorPrecipitacion = datoMeteorologicoViewModel.validarPrecipitacion(precipitacion)
    }

    // Validar temperatura máxima
    LaunchedEffect(temperaturaMaxima) {
        errorTempMax = datoMeteorologicoViewModel.validarTemperaturaMaxima(temperaturaMaxima)
    }

    // Validar temperatura mínima
    LaunchedEffect(temperaturaMinima) {
        errorTempMin = datoMeteorologicoViewModel.validarTemperaturaMinima(temperaturaMinima)
    }

    // Validar coherencia de temperaturas
    LaunchedEffect(temperaturaMinima, temperaturaMaxima) {
        errorCoherenciaTemp = datoMeteorologicoViewModel.validarCoherenciaTemperaturas(
            temperaturaMinima,
            temperaturaMaxima
        )
    }

    // Validar condición del día
    LaunchedEffect(condicionDia) {
        errorCondicion = if (condicionDia.isBlank()) {
            "Debe seleccionar una condición del día"
        } else null
    }

    // Validar observaciones
    LaunchedEffect(observaciones) {
        errorObservaciones = datoMeteorologicoViewModel.validarObservaciones(observaciones)
    }

    // Verificar si hay errores
    val hayErrores = errorVoluntario != null ||
            errorPluviometro != null ||
            errorFecha != null ||
            errorHora != null ||
            errorPrecipitacion != null ||
            errorTempMax != null ||
            errorTempMin != null ||
            errorCoherenciaTemp != null ||
            errorCondicion != null ||
            errorObservaciones != null

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
                text = "Registro de Dato Meteorológico",
                style = MaterialTheme.typography.headlineMedium
            )

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
                            if (voluntariosElegibles.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay voluntarios con rol 'Voluntario'") },
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
                                            pluviometroSeleccionado = null // Reset pluviómetro
                                            expandedVoluntario = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Selector de pluviómetro (filtrado por voluntario)
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
                            if (pluviometrosFiltrados.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("No hay pluviómetros registrados")
                                            Text(
                                                "Toque para registrar uno",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        expandedPluviometro = false
                                        onNavegarARegistroPluviometro()
                                    }
                                )
                            } else {
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
            }

            // Sección: Fecha y Hora
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Fecha y Hora",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha (yyyy-MM-dd) *") },
                        placeholder = { Text("2025-10-07") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        isError = errorFecha != null,
                        supportingText = {
                            if (errorFecha != null) {
                                Text(
                                    text = errorFecha!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora (HH:mm) *") },
                        placeholder = { Text("14:30") },
                        isError = errorHora != null,
                        supportingText = {
                            if (errorHora != null) {
                                Text(
                                    text = errorHora!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            if (it.length <= 6) { // Limitar entrada
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
                        label = { Text("Temperatura Máxima (°C) *") },
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
                        label = { Text("Temperatura Mínima (°C) *") },
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

                    // Selector de condición del día
                    ExposedDropdownMenuBox(
                        expanded = expandedCondicion,
                        onExpandedChange = { expandedCondicion = it }
                    ) {
                        OutlinedTextField(
                            value = condicionDia,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Condición del Día *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCondicion) },
                            isError = errorCondicion != null,
                            supportingText = {
                                if (errorCondicion != null) {
                                    Text(
                                        text = errorCondicion!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCondicion,
                            onDismissRequest = { expandedCondicion = false }
                        ) {
                            condicionesDia.forEach { condicion ->
                                DropdownMenuItem(
                                    text = { Text(condicion) },
                                    onClick = {
                                        condicionDia = condicion
                                        expandedCondicion = false
                                    }
                                )
                            }
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
                            // Validación final: verificar si ya existe registro
                            if (pluviometroSeleccionado != null) {
                                val existe = datoMeteorologicoViewModel.existeRegistroEnFecha(
                                    pluviometroSeleccionado!!.id,
                                    fecha
                                )

                                if (existe) {
                                    snackbarHostState.showSnackbar(
                                        message = "⚠️ Ya existe un registro para este pluviómetro en esta fecha",
                                        duration = SnackbarDuration.Long
                                    )
                                    return@launch
                                }
                            }

                            val precipitacionDouble = precipitacion.toDoubleOrNull() ?: 0.0
                            val tempMaxDouble = temperaturaMaxima.toDoubleOrNull()
                            val tempMinDouble = temperaturaMinima.toDoubleOrNull()

                            val dato = DatoMeteorologico(
                                voluntario_id = voluntarioSeleccionado!!.id,
                                voluntario_nombre = voluntarioSeleccionado!!.nombre,
                                pluviometro_id = pluviometroSeleccionado!!.id,
                                pluviometro_registro = pluviometroSeleccionado!!.numero_registro,
                                fecha = fecha,
                                hora = hora,
                                precipitacion = precipitacionDouble,
                                temperatura_maxima = tempMaxDouble,
                                temperatura_minima = tempMinDouble,
                                condicion_dia = condicionDia,
                                observaciones = observaciones.ifBlank { null }
                            )

                            datoMeteorologicoViewModel.guardarDato(dato)
                            snackbarHostState.showSnackbar("✅ Dato meteorológico guardado exitosamente")
                            onDatoGuardado()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !hayErrores
                ) {
                    Text("Guardar")
                }

                OutlinedButton(
                    onClick = {
                        voluntarioSeleccionado = null
                        pluviometroSeleccionado = null
                        fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        precipitacion = ""
                        temperaturaMaxima = ""
                        temperaturaMinima = ""
                        condicionDia = ""
                        observaciones = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }

                OutlinedButton(
                    onClick = onDatoGuardado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }

            // Resumen de errores (si existen)
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
                            errorFecha,
                            errorHora,
                            errorPrecipitacion,
                            errorTempMax,
                            errorTempMin,
                            errorCoherenciaTemp,
                            errorCondicion,
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