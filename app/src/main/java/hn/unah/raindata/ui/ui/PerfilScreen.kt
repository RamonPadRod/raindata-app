package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.ui.components.RevoclimapButton

@Composable
fun PerfilScreen(
    onLogout: () -> Unit
) {
    val currentUser = UserSession.getCurrentUser()
    val userRole = UserSession.getUserRole()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Perfil",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Auto-size: reduce la fuente hasta caber en 1 línea; si llega al mínimo, permite 2
        var fontSize by remember { mutableStateOf(28.sp) }
        var maxLinesActual by remember { mutableIntStateOf(1) }

        Text(
            text = currentUser?.nombre ?: "Usuario",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = maxLinesActual,
            overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { result ->
                if (result.didOverflowWidth || result.didOverflowHeight) {
                    if (fontSize > 16.sp) {
                        fontSize *= 0.88f
                    } else {
                        maxLinesActual = 2
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Rol: $userRole",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentUser?.email ?: "Sin correo",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        RevoclimapButton(
            text = "Cerrar sesión",
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
