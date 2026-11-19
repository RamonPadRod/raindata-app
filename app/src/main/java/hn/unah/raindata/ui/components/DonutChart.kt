package hn.unah.raindata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutSlice(
    val label: String,
    val value: Double,
    val color: Color
)

val DonutChartPalette = listOf(
    Color(0xFF2E7D32), // Verde
    Color(0xFF1976D2), // Azul
    Color(0xFFFFA000), // Ámbar
    Color(0xFF7B1FA2), // Morado
    Color(0xFF0097A7), // Cian
    Color(0xFFE65100)  // Naranja
)

val DonutOtrosColor = Color(0xFF757575) // Gris para "Otros"

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    centerSubLabel: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 22.dp
) {
    val validSlices = slices.filter { it.value > 0 }
    val total = validSlices.sumOf { it.value }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - strokePx
            if (diameter <= 0f) return@Canvas

            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            if (total <= 0.0 || validSlices.isEmpty()) {
                return@Canvas
            }

            if (validSlices.size == 1) {
                // Anillo completo (100 %)
                drawArc(
                    color = validSlices[0].color,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                )
            } else {
                val gapAngle = 3f // separación pequeña entre segmentos
                val totalGap = gapAngle * validSlices.size
                val availableSweep = 360f - totalGap

                var currentAngle = -90f
                for (slice in validSlices) {
                    val sweep = ((slice.value / total).toFloat() * availableSweep).coerceAtLeast(1f)
                    drawArc(
                        color = slice.color,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                    )
                    currentAngle += sweep + gapAngle
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (centerSubLabel.isNotEmpty()) {
                Text(
                    text = centerSubLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
