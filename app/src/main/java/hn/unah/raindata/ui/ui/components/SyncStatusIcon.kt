package hn.unah.raindata.ui.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hn.unah.raindata.data.database.entities.SyncStatus
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusIcon(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    when (status) {
        SyncStatus.PENDIENTE -> {
            val infiniteTransition = rememberInfiniteTransition(label = "sync_animation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Icon(
                imageVector = Icons.Filled.Sync,
                contentDescription = "Sincronizando...",
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
        }
        SyncStatus.ENVIADO -> {
            Icon(
                imageVector = Icons.Filled.CloudDone,
                contentDescription = "Sincronizado",
                tint = Color(0xFF4CAF50), // Verde esmeralda
                modifier = modifier.size(20.dp)
            )
        }
        SyncStatus.ERROR -> {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    Text(
                        text = "Error al sincronizar",
                        modifier = Modifier.padding(8.dp)
                    )
                },
                state = rememberTooltipState()
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = "Error de sincronización",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = modifier.size(20.dp)
                )
            }
        }
    }
}
