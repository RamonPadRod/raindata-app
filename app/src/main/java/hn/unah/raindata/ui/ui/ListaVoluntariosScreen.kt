package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVoluntariosScreen(
    viewModel: VoluntarioViewModel = viewModel(),
    onAgregarVoluntario: () -> Unit = {},
    onEditarVoluntario: (Voluntario) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val voluntarios by viewModel.voluntarios.collectAsState()
    var showNoPermissionDialog by remember { mutableStateOf(false) }
    var noPermissionMessage by remember { mutableStateOf("") }

    // ✅ NUEVO: Contar solicitudes pendientes
    val solicitudesPendientes = voluntarios.count {
        it.tipo_usuario == "Administrador" && it.estado_aprobacion == "Pendiente"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (UserSession.canCreateVoluntarios()) {
                FloatingActionButton(
                    onClick = onAgregarVoluntario
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Voluntario")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voluntarios Registrados",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${voluntarios.size} voluntarios activos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // ✅ NUEVO: Mostrar solicitudes pendientes
                        if (solicitudesPendientes > 0 && UserSession.isAdmin()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "⚠️ $solicitudesPendientes solicitud(es) pendiente(s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Badge de permisos
                    if (!UserSession.canCreateVoluntarios()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "Solo lectura",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Lista de voluntarios
            if (voluntarios.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay voluntarios registrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (UserSession.canCreateVoluntarios()) {
                        Text(
                            text = "Presiona el botón + para agregar el primer voluntario",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(voluntarios) { voluntario ->
                        VoluntarioCard(
                            voluntario = voluntario,
                            viewModel = viewModel,
                            onEdit = {
                                if (UserSession.canEditVoluntarios()) {
                                    onEditarVoluntario(voluntario)
                                } else {
                                    noPermissionMessage = "No tienes permisos para editar voluntarios."
                                    showNoPermissionDialog = true
                                }
                            },
                            onDelete = {
                                if (UserSession.canDeleteVoluntarios()) {
                                    scope.launch {
                                        viewModel.eliminarVoluntario(
                                            firebaseUid = voluntario.firebase_uid,
                                            onSuccess = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("✅ Voluntario eliminado")
                                                }
                                            },
                                            onError = { error ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("❌ Error: $error")
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    noPermissionMessage = "No tienes permisos para eliminar voluntarios."
                                    showNoPermissionDialog = true
                                }
                            },
                            onAprobar = { voluntarioParaAprobar ->
                                scope.launch {
                                    // ✅ Obtener información del admin que aprueba
                                    val adminActual = UserSession.getCurrentUser()
                                    val fechaAprobacion = java.text.SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date())

                                    // Actualizar estado a "Aprobado"
                                    val voluntarioAprobado = voluntarioParaAprobar.copy(
                                        estado_aprobacion = "Aprobado",
                                        visto_por_admin = true,
                                        aprobado_por_nombre = adminActual?.nombre,
                                        aprobado_por_uid = adminActual?.firebase_uid,
                                        fecha_aprobacion = fechaAprobacion
                                    )

                                    viewModel.actualizarVoluntario(
                                        voluntario = voluntarioAprobado,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("✅ Administrador aprobado")

                                                // ✅ ENVIAR EMAIL DE APROBACIÓN
                                                launch {
                                                    hn.unah.raindata.data.email.EmailService.enviarEmailConCallback(
                                                        tipo = hn.unah.raindata.data.email.EmailService.TipoEmail.APROBACION_ADMIN,
                                                        destinatario = voluntarioAprobado.email,
                                                        nombreUsuario = voluntarioAprobado.nombre,
                                                        onSuccess = {
                                                            android.util.Log.d("ListaVoluntarios", "✅ Email de aprobación enviado")
                                                        },
                                                        onError = { error ->
                                                            android.util.Log.e("ListaVoluntarios", "❌ Error al enviar email: $error")
                                                        }
                                                    )
                                                }
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
                            onRechazar = { voluntarioParaRechazar ->
                                scope.launch {
                                    // ✅ Obtener información del admin que rechaza
                                    val adminActual = UserSession.getCurrentUser()
                                    val fechaRechazo = java.text.SimpleDateFormat(
                                        "yyyy-MM-dd HH:mm:ss",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date())

                                    // Actualizar estado a "Rechazado"
                                    val voluntarioRechazado = voluntarioParaRechazar.copy(
                                        estado_aprobacion = "Rechazado",
                                        visto_por_admin = true,
                                        rechazado_por_nombre = adminActual?.nombre,
                                        rechazado_por_uid = adminActual?.firebase_uid,
                                        fecha_rechazo = fechaRechazo
                                    )

                                    viewModel.actualizarVoluntario(
                                        voluntario = voluntarioRechazado,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("❌ Solicitud rechazada")
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
                            canEdit = UserSession.canEditVoluntarios(),
                            canDelete = UserSession.canDeleteVoluntarios(),
                            canApprove = UserSession.canApproveAdminRequests()
                        )
                    }
                }
            }
        }
    }

    // Diálogo de sin permisos
    if (showNoPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNoPermissionDialog = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            title = { Text("Permiso Denegado") },
            text = { Text(noPermissionMessage) },
            confirmButton = {
                Button(onClick = { showNoPermissionDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoluntarioCard(
    voluntario: Voluntario,
    viewModel: VoluntarioViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAprobar: (Voluntario) -> Unit,
    onRechazar: (Voluntario) -> Unit,
    canEdit: Boolean,
    canDelete: Boolean,
    canApprove: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAprobarDialog by remember { mutableStateOf(false) }
    var showRechazarDialog by remember { mutableStateOf(false) }

    // ✅ Determinar color de la card según estado
    val cardColor = when {
        voluntario.estado_aprobacion == "Pendiente" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        voluntario.estado_aprobacion == "Rechazado" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),  // ← AGREGAR ESTA LÍNEA
        colors = CardDefaults.cardColors(containerColor = cardColor),
        onClick = { }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voluntario.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (!voluntario.cedula.isNullOrBlank()) {
                        Text(
                            text = "Cédula: ${voluntario.cedula}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!voluntario.pasaporte.isNullOrBlank()) {
                        Text(
                            text = "Pasaporte: ${voluntario.pasaporte}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = voluntario.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${voluntario.municipio}, ${voluntario.departamento}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!voluntario.telefono.isNullOrBlank()) {
                        Text(
                            text = "Tel: ${voluntario.telefono}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Badge de tipo de usuario
                        if (voluntario.tipo_usuario.isNotBlank()) {
                            Surface(
                                color = when (voluntario.tipo_usuario) {
                                    "Administrador" -> MaterialTheme.colorScheme.primaryContainer
                                    "Voluntario" -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.tertiaryContainer
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = voluntario.tipo_usuario,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = when (voluntario.tipo_usuario) {
                                        "Administrador" -> MaterialTheme.colorScheme.onPrimaryContainer
                                        "Voluntario" -> MaterialTheme.colorScheme.onSecondaryContainer
                                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )
                            }
                        }

                        // Badge de estado de aprobación (solo para Administradores)
                        if (voluntario.tipo_usuario == "Administrador") {
                            Surface(
                                color = when (voluntario.estado_aprobacion) {
                                    "Aprobado" -> MaterialTheme.colorScheme.primaryContainer
                                    "Pendiente" -> MaterialTheme.colorScheme.errorContainer
                                    "Rechazado" -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = when (voluntario.estado_aprobacion) {
                                        "Aprobado" -> "✅ Aprobado"
                                        "Pendiente" -> "⏳ Pendiente"
                                        "Rechazado" -> "❌ Rechazado"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Botones de editar/eliminar
                Row {
                    if (canEdit && voluntario.estado_aprobacion != "Pendiente") {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                    if (canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // ✅ BOTONES DE APROBAR/RECHAZAR ABAJO
            if (canApprove &&
                voluntario.tipo_usuario == "Administrador" &&
                voluntario.estado_aprobacion == "Pendiente") {

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAprobarDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aprobar", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { showRechazarDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Rechazar",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

    // ✅ Diálogo de aprobación
    if (showAprobarDialog) {
        AlertDialog(
            onDismissRequest = { showAprobarDialog = false },
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            title = { Text("Aprobar Administrador") },
            text = {
                Column {
                    Text("¿Estás seguro de que deseas aprobar a ${voluntario.nombre} como Administrador?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Esta persona tendrá acceso completo al sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAprobar(voluntario)
                        showAprobarDialog = false
                    }
                ) {
                    Text("Aprobar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAprobarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ✅ Diálogo de rechazo
    if (showRechazarDialog) {
        AlertDialog(
            onDismissRequest = { showRechazarDialog = false },
            icon = { Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Rechazar Solicitud") },
            text = { Text("¿Estás seguro de que deseas rechazar la solicitud de ${voluntario.nombre}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onRechazar(voluntario)
                        showRechazarDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRechazarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Eliminar Voluntario") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${voluntario.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}}