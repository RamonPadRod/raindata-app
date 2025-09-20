package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.viewmodel.VoluntarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVoluntariosScreen(
    viewModel: VoluntarioViewModel = viewModel(),
    onAgregarVoluntario: () -> Unit = {}
) {
    val voluntarios by viewModel.todosLosVoluntarios.observeAsState(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarVoluntario
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Voluntario")
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
                    Column {
                        Text(
                            text = "Voluntarios Registrados",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${voluntarios.size} voluntarios activos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Lista de voluntarios
            if (voluntarios.isEmpty()) {
                // Estado vacío
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
                    Text(
                        text = "Presiona el botón + para agregar el primer voluntario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(voluntarios) { voluntario ->
                        VoluntarioCard(voluntario = voluntario)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoluntarioCard(voluntario: Voluntario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Navegar a detalles si necesario */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

            if (!voluntario.tipo_usuario.isNullOrBlank()) {
                Text(
                    text = "Tipo: ${voluntario.tipo_usuario}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}