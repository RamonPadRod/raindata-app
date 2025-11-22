package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDatoMeteorologicoScreen(
    datoMeteorologicoViewModel: DatoMeteorologicoViewModel,
    voluntarioViewModel: VoluntarioViewModel,
    pluviometroViewModel: PluviometroViewModel,
    onDatoGuardado: () -> Unit = {},
    onNavegarARegistroPluviometro: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val todosLosVoluntarios by voluntarioViewModel.voluntarios.collectAsState()
    val todosLosPluviometros by pluviometroViewModel.pluviometros.collectAsState()
    val isLoading by datoMeteorologicoViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        voluntarioViewModel.cargarVoluntarios()

        if (UserSession.shouldFilterPluviometrosByUser()) {
            val uid = UserSession.getCurrentUserUid()
            uid?.let { pluviometroViewModel.obtenerPluviometrosPorResponsable(it) }
        } else {
            pluviometroViewModel.cargarPluviometros()
        }
    }

    val voluntariosElegibles = remember(todosLosVoluntarios) {
        todosLosVoluntarios.filter {
            it.tipo_usuario == "Voluntario" && it.estado_aprobacion == "Aprobado"
        }
    }

    var voluntarioSeleccionado by remember { mutableStateOf<Voluntario?>(null) }
    var expandedVoluntario by remember { mutableStateOf(false) }

    var pluviometroSeleccionado by remember { mutableStateOf<Pluviometro?>(null) }
    var expandedPluviometro by remember { mutableStateOf(false) }

    var fechaLectura by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var horaLectura by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }

    val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val horaRegistro = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    var precipitacion by remember { mutableStateOf("") }
    var temperaturaMaxima by remember { mutableStateOf("") }
    var temperaturaMinima by remember { mutableStateOf("") }

    var condicionesSeleccionadas by remember { mutableStateOf(setOf<String>()) }

    var observaciones by remember { mutableStateOf("") }

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

    val pluviometrosFiltrados = remember(voluntarioSeleccionado, todosLosPluviometros) {
        if (UserSession.isAdmin()) {
            voluntarioSeleccionado?.let { voluntario ->
                todosLosPluviometros.filter { it.responsable_uid == voluntario.firebase_uid }
            } ?: emptyList()
        } else {
            todosLosPluviometros
        }
    }

    LaunchedEffect(Unit) {
        if (UserSession.isVoluntario()) {
            val uid = UserSession.getCurrentUserUid()
            val voluntario = voluntariosElegibles.find { it.firebase_uid == uid }
            voluntarioSeleccionado = voluntario
        }
    }

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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Identificación",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (UserSession.isAdmin()) {
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
                                                pluviometroSeleccionado = null
                                                expandedVoluntario = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = voluntarioSeleccionado?.nombre ?: UserSession.getCurrentUserName(),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Voluntario") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedPluviometro,
                        onExpandedChange = {
                            if (voluntarioSeleccionado != null || UserSession.isVoluntario()) {
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
                            enabled = voluntarioSeleccionado != null || UserSession.isVoluntario(),
                            isError = errorPluviometro != null,
                            supportingText = {
                                when {
                                    errorPluviometro != null -> Text(
                                        text = errorPluviometro!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    voluntarioSeleccionado == null && UserSession.isAdmin() -> Text("Primero seleccione un voluntario")
                                    pluviometrosFiltrados.isEmpty() -> Text(
                                        "No hay pluviómetros disponibles",
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
                                            if (UserSession.isAdmin()) {
                                                Text(
                                                    "Toque para registrar uno",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        expandedPluviometro = false
                                        if (UserSession.isAdmin()) {
                                            onNavegarARegistroPluviometro()
                                        }
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
                        text = "Fecha y Hora de Registro",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Registro automático del sistema",
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

                            val dato = DatoMeteorologico(
                                voluntario_uid = voluntarioSeleccionado!!.firebase_uid,
                                voluntario_nombre = voluntarioSeleccionado!!.nombre,
                                pluviometro_id = pluviometroSeleccionado!!.id,
                                pluviometro_registro = pluviometroSeleccionado!!.numero_registro,
                                pluviometro_responsable_uid = pluviometroSeleccionado!!.responsable_uid,
                                fecha_lectura = fechaLectura,
                                hora_lectura = horaLectura,
                                fecha_registro = fechaRegistro,
                                hora_registro = horaRegistro,
                                precipitacion = precipitacionDouble,
                                temperatura_maxima = tempMaxDouble,
                                temperatura_minima = tempMinDouble,
                                condiciones_dia = condicionesSeleccionadas.joinToString("|"),
                                observaciones = observaciones.ifBlank { null }
                            )

                            datoMeteorologicoViewModel.guardarDato(
                                dato,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Dato meteorológico guardado")
                                        onDatoGuardado()
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
                        voluntarioSeleccionado = if (UserSession.isVoluntario()) {
                            voluntariosElegibles.find { it.firebase_uid == UserSession.getCurrentUserUid() }
                        } else null
                        pluviometroSeleccionado = null
                        fechaLectura = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        horaLectura = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        precipitacion = ""
                        temperaturaMaxima = ""
                        temperaturaMinima = ""
                        condicionesSeleccionadas = setOf()
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