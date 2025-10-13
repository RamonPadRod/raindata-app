package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch

// ✅ VISUAL TRANSFORMATION PARA DNI
class DniVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(13)
        var out = ""

        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3 || i == 7) out += "-"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 7 -> offset + 1
                    offset <= 13 -> offset + 2
                    else -> 15
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 4 -> offset
                    offset <= 9 -> offset - 1
                    offset <= 15 -> offset - 2
                    else -> 13
                }
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// ✅ VISUAL TRANSFORMATION PARA TELÉFONO
class TelefonoVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        var out = ""

        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3) out += "-"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= 3) offset else offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset <= 4) offset else offset - 1
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

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
    var expandedDepartamento by remember { mutableStateOf(false) }
    var municipio by remember { mutableStateOf("") }
    var expandedMunicipio by remember { mutableStateOf(false) }
    var aldea by remember { mutableStateOf("") }
    var caserioBarrioColonia by remember { mutableStateOf("") }
    var tipoUsuario by remember { mutableStateOf("") }
    var expandedTipoUsuario by remember { mutableStateOf(false) }
    var observaciones by remember { mutableStateOf("") }

    // Lista de municipios según departamento seleccionado
    val municipiosDisponibles = remember(departamento) {
        hn.unah.raindata.data.utils.DepartamentosHonduras.obtenerMunicipios(departamento)
    }

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
        // Validar usando el DNI formateado
        val dniFormateado = if (dni.length == 13) {
            "${dni.substring(0, 4)}-${dni.substring(4, 8)}-${dni.substring(8)}"
        } else {
            dni
        }
        errorDNI = viewModel.validarDNI(dniFormateado)
    }

    LaunchedEffect(telefono) {
        // Validar usando el teléfono formateado
        val telefonoFormateado = if (telefono.length == 8) {
            "${telefono.substring(0, 4)}-${telefono.substring(4)}"
        } else {
            telefono
        }
        errorTelefono = viewModel.validarTelefono(telefonoFormateado)
    }

    LaunchedEffect(correo) {
        errorCorreo = viewModel.validarCorreo(correo)
    }

    LaunchedEffect(direccion) {
        errorDireccion = viewModel.validarDireccion(direccion)
    }

    LaunchedEffect(departamento) {
        errorDepartamento = viewModel.validarDepartamento(departamento)
        // Resetear municipio si cambia departamento
        if (municipio.isNotEmpty() && !municipiosDisponibles.contains(municipio)) {
            municipio = ""
        }
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

                    // DNI (CON VISUALTRANSFORMATION - SOLUCIÓN DEFINITIVA)
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { input ->
                            // Solo permitir dígitos, máximo 13
                            dni = input.filter { it.isDigit() }.take(13)
                        },
                        label = { Text("DNI *") },
                        placeholder = { Text("0708-2005-00276") },
                        visualTransformation = DniVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorDNI != null && dni.isNotBlank(),
                        supportingText = {
                            if (errorDNI != null && dni.isNotBlank()) {
                                Text(
                                    text = errorDNI!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (errorDNI == null && dni.length == 13) {
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
                                errorDNI == null && dni.length == 13 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // TELÉFONO (CON VISUALTRANSFORMATION - SOLUCIÓN DEFINITIVA)
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { input ->
                            // Solo permitir dígitos, máximo 8
                            telefono = input.filter { it.isDigit() }.take(8)
                        },
                        label = { Text("Teléfono") },
                        placeholder = { Text("2245-5566") },
                        visualTransformation = TelefonoVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = errorTelefono != null && telefono.isNotBlank(),
                        supportingText = {
                            if (errorTelefono != null && telefono.isNotBlank()) {
                                Text(
                                    text = errorTelefono!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (errorTelefono == null && telefono.length == 8) {
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
                                errorTelefono == null && telefono.length == 8 -> MaterialTheme.colorScheme.primary
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

                    // DEPARTAMENTO (DROPDOWN)
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
                            isError = errorDepartamento != null && departamento.isBlank(),
                            supportingText = {
                                if (errorDepartamento != null && departamento.isBlank()) {
                                    Text(
                                        text = errorDepartamento!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorDepartamento == null && departamento.isNotBlank()) {
                                    Text(
                                        text = "✓ Válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = when {
                                    errorDepartamento != null && departamento.isBlank() -> MaterialTheme.colorScheme.error
                                    errorDepartamento == null && departamento.isNotBlank() -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDepartamento,
                            onDismissRequest = { expandedDepartamento = false }
                        ) {
                            hn.unah.raindata.data.utils.DepartamentosHonduras.departamentos.forEach { depto ->
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

                    // MUNICIPIO (DROPDOWN)
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
                            isError = errorMunicipio != null && municipio.isBlank(),
                            supportingText = {
                                if (errorMunicipio != null && municipio.isBlank()) {
                                    Text(
                                        text = errorMunicipio!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (errorMunicipio == null && municipio.isNotBlank()) {
                                    Text(
                                        text = "✓ Válido",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (departamento.isBlank()) {
                                    Text("Primero seleccione un departamento")
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = when {
                                    errorMunicipio != null && municipio.isBlank() -> MaterialTheme.colorScheme.error
                                    errorMunicipio == null && municipio.isNotBlank() -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
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
                                            municipio = muni
                                            expandedMunicipio = false
                                        }
                                    )
                                }
                            }
                        }
                    }

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