package hn.unah.raindata.ui.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import hn.unah.raindata.data.session.UserSession
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

/**
 * Gestiona el flujo secuencial de permisos sin diálogos duplicados.
 * 1. Notificaciones (Android 13+)
 * 2. DONE (Finaliza y cierra la UI)
 */
@Composable
fun PermissionFlow(
    onFlowCompleted: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("RevoclimapPrefs", Context.MODE_PRIVATE) }
    
    // Obtener info del usuario actual para asegurar que el flujo es por usuario
    val currentUid = UserSession.getCurrentUserUid() ?: "unknown"
    val prefKey = "permissions_flow_completed_$currentUid"
    val hasCompletedFlow = remember(prefKey) { sharedPrefs.getBoolean(prefKey, false) }

    if (hasCompletedFlow) {
        LaunchedEffect(Unit) { onFlowCompleted() }
        return
    }

    var currentStep by remember { 
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) PermissionStep.NOTIFICATIONS else PermissionStep.DONE) 
    }
    
    fun advanceToNextStep(step: PermissionStep) {
        when (step) {
            PermissionStep.NOTIFICATIONS -> {
                currentStep = PermissionStep.DONE
            }
            PermissionStep.DONE -> {
                sharedPrefs.edit().putBoolean(prefKey, true).apply()
                onFlowCompleted()
            }
        }
    }

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        advanceToNextStep(PermissionStep.NOTIFICATIONS)
    }

    when (currentStep) {
        PermissionStep.NOTIFICATIONS -> {
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionDialog(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    description = "Para recibir alertas climáticas importantes y actualizaciones de la red de voluntarios, necesitamos tu permiso.",
                    confirmText = "Continuar",
                    onConfirm = {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onDismiss = { advanceToNextStep(PermissionStep.NOTIFICATIONS) }
                )
            } else {
                LaunchedEffect(Unit) { advanceToNextStep(PermissionStep.NOTIFICATIONS) }
            }
        }
        PermissionStep.DONE -> {
            LaunchedEffect(Unit) {
                advanceToNextStep(PermissionStep.DONE)
            }
        }
    }
}

enum class PermissionStep {
    NOTIFICATIONS, DONE
}

@Composable
fun PermissionDialog(
    icon: ImageVector,
    title: String,
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Omitir")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
