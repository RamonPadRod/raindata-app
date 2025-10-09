package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroVoluntarioScreen(
    viewModel: VoluntarioViewModel = viewModel(),
    onVoluntarioGuardado: () -> Unit = {},
    soloAdministrador: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var aldea by remember { mutableStateOf("") }
    var caserioBarrioColonia by remember { mutableStateOf("") }
    var tipoUsuario by remember { mutableStateOf("") }
    var expandedTipoUsuario by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf("") }

    // Estados de error
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorDNI by remember { mutableStateOf<String?>(null) }
    var errorTelefono by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var errorDepartamento by remember { mutableStateOf<String?>(null) }
    var errorMunicipio by remember { mutableStateOf<String?>(null) }
    var errorAldea by remember { mutableStateOf<String?>(null) }
    var errorCaserio by remember { mutableStateOf<String?>(null) }
    var errorTipoUsuario by remember { mutableStateOf<String?>(null) }
    var errorObservaciones by remember { mutableStateOf<String?>(null) }

    val tiposUsuario = listOf("Observador", "Voluntario", "Administrador")

    // Si es solo administrador, forzar el valor
    LaunchedEffect(soloAdministrador) {
        if (soloAdministrador) {
            tipoUsuario = "Administrador"
        }
    }

    // VALIDACIONES EN TIEMPO REAL
    LaunchedEffect(nombre) {
        errorNombre = viewModel.validarNombre(nombre)
    }

    LaunchedEffect(dni) {
        errorDNI = viewModel.validarDNI(dni)
    }

    LaunchedEffect(telefono) {
        errorTelefono = viewModel.validarTelefono(telefono)
    }

    LaunchedEffect(correo) {
        errorCorreo = viewModel.validarCorreo(correo)
    }

    LaunchedEffect(direccion) {
        errorDireccion = viewModel.validarDireccion(direccion)
    }

    LaunchedEffect(departamento) {
        errorDepartamento = viewModel.validarDepartamento(departamento)
    }

    LaunchedEffect(municipio) {
        errorMunicipio = viewModel.validarMunicipio(municipio)
    }

    LaunchedEffect(aldea) {
        errorAldea = viewModel.validarAldea(aldea)
    }

    LaunchedEffect(caserioBarrioColonia) {
        errorCaserio = viewModel.validarCaserioBarrioColonia(caserioBarrioColonia)
    }

    LaunchedEffect(observaciones) {
        errorObservaciones = viewModel.validarObservaciones(observaciones)
    }

    LaunchedEffect(tipoUsuario, soloAdministrador) {
        errorTipoUsuario = viewModel.validarTipoUsuario(tipoUsuario, soloAdministrador)
    }

    // Verificar si hay errores
    val hayErrores = errorNombre != null ||
            errorDNI != null ||
            errorTelefono != null ||
            errorCorreo != null ||
            errorDireccion != null ||
            errorDepartamento != null ||
            errorMunicipio != null ||
            errorAldea != null ||
            errorCaserio != null ||
            errorTipoUsuario != null ||
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
                text = if (soloAdministrador) "Registro de Administrador" else "Registro de Voluntario",
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

                    // NOMBRE
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            if (it.length <= 40) {
                                nombre = it.filter { char -> char.isLetter() || char.isWhitespace() || char in "áéíóúÁÉÍÓÚñÑ" }
                            }
                        },
                        label = { Text("Nombre completo *") },
                        isError = errorNombre != null && nombre.isNotBlank(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorNombre != null && nombre.isNotBlank()) {
                                    Text(
                                        text = errorNombre!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorNombre == null && nombre.isNotBlank()) {
                                    Text(
                                        text = "✓ Nombre válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${nombre.length}/40",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorNombre != null && nombre.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorNombre == null && nombre.isNotBlank() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // DNI
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { input ->
                            // Limpiar TODO excepto dígitos (incluyendo guiones previos)
                            val digitsOnly = input.replace("-", "").filter { it.isDigit() }.take(13)
                            // Formatear con guiones
                            dni = when (digitsOnly.length) {
                                in 0..4 -> digitsOnly
                                in 5..8 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4)}"
                                else -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 8)}-${digitsOnly.substring(8)}"
                            }
                        },
                        label = { Text("DNI *") },
                        placeholder = { Text("0708-2005-00276") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorDNI != null && dni.isNotBlank(),
                        supportingText = {
                            if (errorDNI != null && dni.isNotBlank()) {
                                Text(
                                    text = errorDNI!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (errorDNI == null && dni.length == 15) {
                                Text(
                                    text = "✓ DNI válido",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text("Formato: XXXX-XXXX-XXXXX")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorDNI != null && dni.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorDNI == null && dni.length == 15 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // TELÉFONO
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { input ->
                            // Limpiar TODO excepto dígitos (incluyendo guiones previos)
                            val digitsOnly = input.replace("-", "").filter { it.isDigit() }.take(8)
                            // Formatear con guion
                            telefono = when (digitsOnly.length) {
                                in 0..4 -> digitsOnly
                                else -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4)}"
                            }
                        },
                        label = { Text("Teléfono") },
                        placeholder = { Text("2245-5566") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = errorTelefono != null && telefono.isNotBlank(),
                        supportingText = {
                            if (errorTelefono != null && telefono.isNotBlank()) {
                                Text(
                                    text = errorTelefono!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (errorTelefono == null && telefono.length == 9) {
                                Text(
                                    text = "✓ Teléfono válido",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text("8 dígitos, debe empezar con 2, 3, 8 o 9")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorTelefono != null && telefono.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorTelefono == null && telefono.length == 9 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CORREO
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            if (it.length <= 50) correo = it
                        },
                        label = { Text("Correo") },
                        placeholder = { Text("ejemplo@correo.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = errorCorreo != null && correo.isNotBlank(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorCorreo != null && correo.isNotBlank()) {
                                    Text(
                                        text = errorCorreo!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorCorreo == null && correo.isNotBlank()) {
                                    Text(
                                        text = "✓ Correo válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${correo.length}/50",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorCorreo != null && correo.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorCorreo == null && correo.isNotBlank() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // TIPO DE USUARIO
                    if (soloAdministrador) {
                        OutlinedTextField(
                            value = "Administrador",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Tipo de Usuario") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = expandedTipoUsuario,
                            onExpandedChange = { expandedTipoUsuario = it }
                        ) {
                            OutlinedTextField(
                                value = tipoUsuario,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Tipo de Usuario *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoUsuario) },
                                isError = errorTipoUsuario != null && !soloAdministrador,
                                supportingText = {
                                    if (errorTipoUsuario != null && !soloAdministrador) {
                                        Text(
                                            text = errorTipoUsuario!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
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

                    // DIRECCIÓN
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = {
                            if (it.length <= 100) direccion = it
                        },
                        label = { Text("Dirección *") },
                        isError = errorDireccion != null && direccion.isNotBlank(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorDireccion != null && direccion.isNotBlank()) {
                                    Text(
                                        text = errorDireccion!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorDireccion == null && direccion.isNotBlank()) {
                                    Text(
                                        text = "✓ Válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${direccion.length}/100",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorDireccion != null && direccion.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorDireccion == null && direccion.isNotBlank() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // DEPARTAMENTO
                    OutlinedTextField(
                        value = departamento,
                        onValueChange = {
                            if (it.length <= 30) departamento = it
                        },
                        label = { Text("Departamento *") },
                        isError = errorDepartamento != null && departamento.isNotBlank(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorDepartamento != null && departamento.isNotBlank()) {
                                    Text(
                                        text = errorDepartamento!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorDepartamento == null && departamento.isNotBlank()) {
                                    Text(
                                        text = "✓ Válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${departamento.length}/30",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorDepartamento != null && departamento.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorDepartamento == null && departamento.isNotBlank() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // MUNICIPIO
                    OutlinedTextField(
                        value = municipio,
                        onValueChange = {
                            if (it.length <= 30) municipio = it
                        },
                        label = { Text("Municipio *") },
                        isError = errorMunicipio != null && municipio.isNotBlank(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorMunicipio != null && municipio.isNotBlank()) {
                                    Text(
                                        text = errorMunicipio!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorMunicipio == null && municipio.isNotBlank()) {
                                    Text(
                                        text = "✓ Válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${municipio.length}/30",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorMunicipio != null && municipio.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorMunicipio == null && municipio.isNotBlank() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ALDEA O COLONIA
                    OutlinedTextField(
                        value = aldea,
                        onValueChange = {
                            if (it.length <= 15) aldea = it
                        },
                        label = { Text("Aldea o Colonia *") },
                        isError = errorAldea != null && aldea.isNotBlank(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorAldea != null && aldea.isNotBlank()) {
                                    Text(
                                        text = errorAldea!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorAldea == null && aldea.isNotBlank()) {
                                    Text(
                                        text = "✓ Válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${aldea.length}/15",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorAldea != null && aldea.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorAldea == null && aldea.isNotBlank() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CASERÍO/BARRIO/COLONIA
                    OutlinedTextField(
                        value = caserioBarrioColonia,
                        onValueChange = {
                            if (it.length <= 15) caserioBarrioColonia = it
                        },
                        label = { Text("Caserío/Barrio/Colonia") },
                        isError = errorCaserio != null && caserioBarrioColonia.isNotBlank(),
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorCaserio != null && caserioBarrioColonia.isNotBlank()) {
                                    Text(
                                        text = errorCaserio!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorCaserio == null && caserioBarrioColonia.isNotBlank()) {
                                    Text(
                                        text = "✓ Válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = "${caserioBarrioColonia.length}/15",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = when {
                                errorCaserio != null && caserioBarrioColonia.isNotBlank() -> MaterialTheme.colorScheme.error
                                errorCaserio == null && caserioBarrioColonia.isNotBlank() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
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
                        label = { Text("Observaciones adicionales (opcional)") },
                        isError = errorObservaciones != null && observaciones.isNotBlank(),
                        supportingText = {
                            if (errorObservaciones != null && observaciones.isNotBlank()) {
                                Text(
                                    text = errorObservaciones!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (observaciones.isNotBlank() && observaciones.length < 10) {
                                Text("Mínimo 10 caracteres si desea agregar observaciones")
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
                            val dniLimpio = viewModel.limpiarDNI(dni)
                            val existe = viewModel.existeDNI(dniLimpio)

                            if (existe) {
                                snackbarHostState.showSnackbar(
                                    message = "⚠️ Este DNI ya está registrado en el sistema",
                                    duration = SnackbarDuration.Long
                                )
                                return@launch
                            }

                            val voluntario = Voluntario(
                                nombre = nombre,
                                direccion = direccion,
                                departamento = departamento,
                                municipio = municipio,
                                aldea = aldea,
                                caserio_barrio_colonia = caserioBarrioColonia.ifBlank { "" },
                                telefono = if (telefono.isBlank()) null else viewModel.limpiarTelefono(telefono),
                                email = correo.ifBlank { null },
                                cedula = dniLimpio,
                                tipo_usuario = tipoUsuario,
                                observaciones = observaciones.ifBlank { null }
                            )

                            viewModel.guardarVoluntario(voluntario)
                            snackbarHostState.showSnackbar("✅ Voluntario guardado exitosamente")
                            onVoluntarioGuardado()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !hayErrores
                ) {
                    Text("Guardar")
                }

                OutlinedButton(
                    onClick = {
                        nombre = ""
                        dni = ""
                        telefono = ""
                        correo = ""
                        direccion = ""
                        departamento = ""
                        municipio = ""
                        aldea = ""
                        caserioBarrioColonia = ""
                        if (!soloAdministrador) {
                            tipoUsuario = ""
                        }
                        observaciones = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }

                OutlinedButton(
                    onClick = onVoluntarioGuardado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }

            // Resumen de errores (SOLO SI HAY ERRORES VISIBLES)
            val erroresVisibles = listOfNotNull(
                if (errorNombre != null && nombre.isNotBlank()) errorNombre else null,
                if (errorDNI != null && dni.isNotBlank()) errorDNI else null,
                if (errorTelefono != null && telefono.isNotBlank()) errorTelefono else null,
                if (errorCorreo != null && correo.isNotBlank()) errorCorreo else null,
                if (errorDireccion != null && direccion.isNotBlank()) errorDireccion else null,
                if (errorDepartamento != null && departamento.isNotBlank()) errorDepartamento else null,
                if (errorMunicipio != null && municipio.isNotBlank()) errorMunicipio else null,
                if (errorAldea != null && aldea.isNotBlank()) errorAldea else null,
                if (errorCaserio != null && caserioBarrioColonia.isNotBlank()) errorCaserio else null,
                if (errorTipoUsuario != null && !soloAdministrador) errorTipoUsuario else null,
                if (errorObservaciones != null && observaciones.isNotBlank()) errorObservaciones else null
            )

            if (erroresVisibles.isNotEmpty()) {
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
                        erroresVisibles.forEach { error ->
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