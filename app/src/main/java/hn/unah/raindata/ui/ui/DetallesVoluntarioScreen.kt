package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.VoluntarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallesVoluntarioScreen(
    voluntario: Voluntario,
    voluntarioViewModel: VoluntarioViewModel,
    onNavigateBack: () -> Unit = {},
    onEditar: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del voluntario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (UserSession.canEditVoluntarios()) {
                        IconButton(onClick = onEditar) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = voluntario.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = voluntario.tipo_usuario,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Identificación
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Identificación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!voluntario.cedula.isNullOrBlank())
                        VoluntarioDetailRow(Icons.Default.Badge, "DNI", voluntario.cedula!!)
                    if (!voluntario.pasaporte.isNullOrBlank())
                        VoluntarioDetailRow(Icons.Default.Badge, "Pasaporte", voluntario.pasaporte!!)
                    if (!voluntario.fecha_nacimiento.isNullOrBlank())
                        VoluntarioDetailRow(Icons.Default.CalendarToday, "Fecha de nacimiento", voluntario.fecha_nacimiento!!)
                    if (!voluntario.genero.isNullOrBlank())
                        VoluntarioDetailRow(Icons.Default.Person, "Género", voluntario.genero!!)
                }
            }

            // Contacto
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Contacto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    VoluntarioDetailRow(Icons.Default.Email, "Correo", voluntario.email)
                    if (!voluntario.telefono.isNullOrBlank())
                        VoluntarioDetailRow(Icons.Default.Phone, "Teléfono", voluntario.telefono!!)
                }
            }

            // Ubicación
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ubicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    VoluntarioDetailRow(Icons.Default.Map, "Departamento", voluntario.departamento)
                    VoluntarioDetailRow(Icons.Default.LocationCity, "Municipio", voluntario.municipio)
                    VoluntarioDetailRow(Icons.Default.Home, "Aldea", voluntario.aldea)
                    if (voluntario.caserio_barrio_colonia.isNotBlank())
                        VoluntarioDetailRow(Icons.Default.LocationOn, "Caserío/Barrio/Colonia", voluntario.caserio_barrio_colonia)
                    VoluntarioDetailRow(Icons.Default.NearMe, "Dirección", voluntario.direccion)
                }
            }

            if (!voluntario.observaciones.isNullOrBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Observaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(voluntario.observaciones!!, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (UserSession.canEditVoluntarios()) {
                Button(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar información")
                }
            }
        }
    }
}

@Composable
private fun VoluntarioDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
}