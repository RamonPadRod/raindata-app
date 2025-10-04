package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDatoMeteorologicoScreen(
    datoMeteorologicoViewModel: DatoMeteorologicoViewModel = viewModel(),
    voluntarioViewModel: VoluntarioViewModel = viewModel(),
    pluviometroViewModel: PluviometroViewModel = viewModel(),
    onDatoGuardado: () -> Unit = {}
) {
    val voluntarios by voluntarioViewModel.todosLosVoluntarios.observeAsState(emptyList())
    val pluviometros by pluviometroViewModel.todosLosPluviometros.observeAsState(emptyList())

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

    Column(
        modifier = Modifier
            .fillMaxSize()
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

                // Selector de pluviómetro
                ExposedDropdownMenuBox(
                    expanded = expandedPluviometro,
                    onExpandedChange = { expandedPluviometro = it }
                ) {
                    OutlinedTextField(
                        value = pluviometroSeleccionado?.let { "${it.numero_registro} - ${it.municipio}" } ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Pluviómetro *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPluviometro) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPluviometro,
                        onDismissRequest = { expandedPluviometro = false }
                    ) {
                        if (pluviometros.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay pluviómetros registrados") },
                                onClick = { }
                            )
                        } else {
                            pluviometros.forEach { pluviometro ->
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
                    placeholder = { Text("2025-10-03") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora (HH:mm) *") },
                    placeholder = { Text("14:30") },
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
                    onValueChange = { precipitacion = it },
                    label = { Text("Precipitación (mm) *") },
                    placeholder = { Text("0.0") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = temperaturaMaxima,
                    onValueChange = { temperaturaMaxima = it },
                    label = { Text("Temperatura Máxima (°C)") },
                    placeholder = { Text("30.5") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = temperaturaMinima,
                    onValueChange = { temperaturaMinima = it },
                    label = { Text("Temperatura Mínima (°C)") },
                    placeholder = { Text("18.0") },
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
                    if (voluntarioSeleccionado != null &&
                        pluviometroSeleccionado != null &&
                        fecha.isNotBlank() &&
                        hora.isNotBlank() &&
                        precipitacion.isNotBlank() &&
                        condicionDia.isNotBlank()) {

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
                        onDatoGuardado()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = voluntarioSeleccionado != null &&
                        pluviometroSeleccionado != null &&
                        fecha.isNotBlank() &&
                        hora.isNotBlank() &&
                        precipitacion.isNotBlank() &&
                        condicionDia.isNotBlank()
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
    }
}