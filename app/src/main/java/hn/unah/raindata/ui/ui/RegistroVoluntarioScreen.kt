package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.viewmodel.VoluntarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroVoluntarioScreen(
    viewModel: VoluntarioViewModel = viewModel(),
    onVoluntarioGuardado: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var aldea by remember { mutableStateOf("") }
    var caserioBarrioColonia by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var tipoUsuario by remember { mutableStateOf("") }
    var expandedTipoUsuario by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf("") }

    val tiposUsuario = listOf("Observador", "Voluntario", "Administrador")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Registro de Voluntario",
            style = MaterialTheme.typography.headlineMedium
        )

        // Información Personal
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Información Personal",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = { Text("Cédula") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo de Tipo de Usuario con dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedTipoUsuario,
                    onExpandedChange = { expandedTipoUsuario = it }
                ) {
                    OutlinedTextField(
                        value = tipoUsuario,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipo de Usuario") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoUsuario) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTipoUsuario,
                        onDismissRequest = { expandedTipoUsuario = false }
                    ) {
                        tiposUsuario.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    tipoUsuario = tipo
                                    expandedTipoUsuario = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Información de Ubicación
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium
                )

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

        // Botones - Cancelar, Limpiar, Guardar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    if (nombre.isNotBlank() && direccion.isNotBlank() &&
                        departamento.isNotBlank() && municipio.isNotBlank() && aldea.isNotBlank()) {

                        val voluntario = Voluntario(
                            nombre = nombre,
                            direccion = direccion,
                            departamento = departamento,
                            municipio = municipio,
                            aldea = aldea,
                            caserio_barrio_colonia = caserioBarrioColonia,
                            telefono = telefono.ifBlank { null },
                            email = email.ifBlank { null },
                            cedula = cedula.ifBlank { null },
                            tipo_usuario = tipoUsuario.ifBlank { null },
                            observaciones = observaciones.ifBlank { null }
                        )

                        viewModel.guardarVoluntario(voluntario)
                        onVoluntarioGuardado()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }

            OutlinedButton(
                onClick = {
                    // Limpiar formulario
                    nombre = ""
                    direccion = ""
                    departamento = ""
                    municipio = ""
                    aldea = ""
                    caserioBarrioColonia = ""
                    telefono = ""
                    email = ""
                    cedula = ""
                    tipoUsuario = ""
                    observaciones = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpiar")
            }

            OutlinedButton(
                onClick = onVoluntarioGuardado, // Regresa a la pantalla anterior
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
        }
    }
}