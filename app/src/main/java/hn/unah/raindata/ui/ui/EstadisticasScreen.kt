package hn.unah.raindata.ui.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hn.unah.raindata.data.repository.FiltroEstadisticas
import hn.unah.raindata.data.repository.PeriodoFiltro
import hn.unah.raindata.data.repository.PrecipPorPluviometro
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.viewmodel.EstadisticasViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(viewModel: EstadisticasViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filtro by viewModel.filtro.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val listaPluviometros by viewModel.listaPluviometros.collectAsState()
    val listaVoluntarios by viewModel.listaVoluntarios.collectAsState()

    // Mostrar snackbar cuando se genera un archivo
    LaunchedEffect(exportResult) {
        exportResult?.let { uri ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "✅ Archivo guardado en Descargas",
                    actionLabel = "Abrir",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                viewModel.limpiarExportResult()
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (isLoading && estadisticas.resumen.totalRegistros == 0) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Calculando estadísticas…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- FILTROS ----
                item {
                    FiltrosCard(
                        filtro = filtro,
                        listaPluviometros = listaPluviometros,
                        listaVoluntarios = listaVoluntarios,
                        onFiltroChange = { viewModel.aplicarFiltro(it) }
                    )
                }

                // ---- RESUMEN GENERAL ----
                item {
                    SectionCard(
                        title = "Resumen General",
                        icon = Icons.Default.Dashboard
                    ) {
                        val r = estadisticas.resumen
                        MetricGrid(
                            listOf(
                                Triple(Icons.Default.Dataset, "Total Registros", "${r.totalRegistros}"),
                                Triple(Icons.Default.WaterDrop, "Precip. Total", "${"%.1f".format(r.precipitacionTotal)} mm"),
                                Triple(Icons.Default.ShowChart, "Prom./Día", "${"%.1f".format(r.promedioPrecipitacionDia)} mm"),
                                Triple(Icons.Default.Event, "Día Mayor Lluvia", r.diaMayorPrecipitacion ?: "—")
                            )
                        )
                    }
                }

                // ---- PRECIPITACIÓN ----
                item {
                    SectionCard(title = "Precipitación", icon = Icons.Default.WaterDrop) {
                        val r = estadisticas.resumen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.WaterDrop,
                                label = "Total acumulada",
                                value = "${"%.2f".format(r.precipitacionTotal)} mm",
                                color = MaterialTheme.colorScheme.primary
                            )
                            MetricChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.BarChart,
                                label = "Promedio por día",
                                value = "${"%.2f".format(r.promedioPrecipitacionDia)} mm",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        if (estadisticas.precipPorPluviometro.isNotEmpty()) {
                            Text(
                                "Comparativa por pluviómetro",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            PrecipBarras(estadisticas.precipPorPluviometro)
                        }
                    }
                }

                // ---- TEMPERATURAS ----
                item {
                    SectionCard(title = "Temperaturas", icon = Icons.Default.Thermostat) {
                        val r = estadisticas.resumen
                        if (r.temperaturaMaximaAbsoluta == null && r.temperaturaMinimaAbsoluta == null) {
                            Text(
                                "No hay datos de temperatura en el período seleccionado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        } else {
                            MetricGrid(
                                listOf(
                                    Triple(Icons.Default.KeyboardArrowUp, "Temp. Máx. Abs.", r.temperaturaMaximaAbsoluta?.let { "${"%.1f".format(it)}°C" } ?: "—"),
                                    Triple(Icons.Default.KeyboardArrowDown, "Temp. Mín. Abs.", r.temperaturaMinimaAbsoluta?.let { "${"%.1f".format(it)}°C" } ?: "—"),
                                    Triple(Icons.Default.ThermostatAuto, "Prom. Máximas", r.promedioTemperaturaMaxima?.let { "${"%.1f".format(it)}°C" } ?: "—"),
                                    Triple(Icons.Default.AcUnit, "Prom. Mínimas", r.promedioTemperaturaMinima?.let { "${"%.1f".format(it)}°C" } ?: "—")
                                )
                            )
                        }
                    }
                }

                // ---- POR UBICACIÓN ----
                item {
                    SectionCard(title = "Actividad", icon = Icons.Default.LocationOn) {
                        val stats = estadisticas
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            InfoRow(
                                icon = Icons.Default.WaterDrop,
                                label = "Pluviómetro más activo",
                                value = stats.pluvioMasActivo?.let { "${it.registro} (${it.totalRegistros} reg.)" } ?: "—"
                            )
                            HorizontalDivider()
                            InfoRow(
                                icon = Icons.Default.Person,
                                label = "Voluntario con más registros",
                                value = stats.voluntarioTop?.let { "${it.nombre} (${it.totalRegistros} reg.)" } ?: "—"
                            )
                            if (stats.precipPorPluviometro.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    "Top pluviómetros por precipitación",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                stats.precipPorPluviometro.take(5).forEach { item ->
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.registro, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "${"%.1f".format(item.totalMm)} mm",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ---- EXPORTACIÓN (solo Admin) ----
                if (UserSession.isAdmin()) {
                    item {
                        SectionCard(title = "Exportar Reporte", icon = Icons.Default.FileDownload) {
                            if (isLoading) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Generando archivo…", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.exportarExcel(context) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1D6F42)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.TableChart,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Excel", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.exportarPdf(context) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFCC0000)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("PDF", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Los archivos se guardan en la carpeta Descargas del dispositivo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ===== COMPONENTE: Card de sección =====
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ===== COMPONENTE: Grilla de 4 métricas =====
@Composable
private fun MetricGrid(items: List<Triple<ImageVector, String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (icon, label, value) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                value,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                // Relleno si la fila tiene solo 1 elemento
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ===== COMPONENTE: Chip de métrica individual =====
@Composable
private fun MetricChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ===== COMPONENTE: Fila informativa =====
@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

// ===== COMPONENTE: Barras de precipitación =====
@Composable
private fun PrecipBarras(items: List<PrecipPorPluviometro>) {
    val max = items.maxOfOrNull { it.totalMm }?.takeIf { it > 0 } ?: 1.0
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.take(6).forEach { item ->
            val fraction = (item.totalMm / max).toFloat().coerceIn(0f, 1f)
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        item.registro,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${"%.1f".format(item.totalMm)} mm",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

// ===== COMPONENTE: Card de filtros =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosCard(
    filtro: FiltroEstadisticas,
    listaPluviometros: List<Pair<String, String>>,
    listaVoluntarios: List<Pair<String, String>>,
    onFiltroChange: (FiltroEstadisticas) -> Unit
) {
    var expandPeriodo by remember { mutableStateOf(false) }
    var expandPluvio by remember { mutableStateOf(false) }
    var expandVol by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Filtros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))

            // Período
            ExposedDropdownMenuBox(expanded = expandPeriodo, onExpandedChange = { expandPeriodo = it }) {
                OutlinedTextField(
                    value = filtro.periodo.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Período") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandPeriodo) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(expanded = expandPeriodo, onDismissRequest = { expandPeriodo = false }) {
                    PeriodoFiltro.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.label) },
                            onClick = {
                                onFiltroChange(filtro.copy(periodo = p))
                                expandPeriodo = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Pluviómetro
            ExposedDropdownMenuBox(expanded = expandPluvio, onExpandedChange = { expandPluvio = it }) {
                OutlinedTextField(
                    value = filtro.pluviometroLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pluviómetro") },
                    leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandPluvio) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(expanded = expandPluvio, onDismissRequest = { expandPluvio = false }) {
                    DropdownMenuItem(
                        text = { Text("Todos") },
                        onClick = {
                            onFiltroChange(filtro.copy(pluviometroId = "", pluviometroLabel = "Todos"))
                            expandPluvio = false
                        }
                    )
                    listaPluviometros.forEach { (id, reg) ->
                        DropdownMenuItem(
                            text = { Text(reg) },
                            onClick = {
                                onFiltroChange(filtro.copy(pluviometroId = id, pluviometroLabel = reg))
                                expandPluvio = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Voluntario (solo Admin)
            if (UserSession.isAdmin()) {
                ExposedDropdownMenuBox(expanded = expandVol, onExpandedChange = { expandVol = it }) {
                    OutlinedTextField(
                        value = filtro.voluntarioLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Voluntario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandVol) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = expandVol, onDismissRequest = { expandVol = false }) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                onFiltroChange(filtro.copy(voluntarioUid = "", voluntarioLabel = "Todos"))
                                expandVol = false
                            }
                        )
                        listaVoluntarios.forEach { (uid, nombre) ->
                            DropdownMenuItem(
                                text = { Text(nombre) },
                                onClick = {
                                    onFiltroChange(filtro.copy(voluntarioUid = uid, voluntarioLabel = nombre))
                                    expandVol = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
