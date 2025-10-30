package hn.unah.raindata.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 🎨 Paleta de colores RainData
 * Basada en el logo del proyecto agroclimático
 * Mesa Agroclimática Paraíso - El Paraíso, Honduras
 */
object RainDataColors {
    // Colores principales del logo
    val VerdePrincipal = Color(0xFF4A7C59)      // Verde oscuro del borde
    val VerdeSecundario = Color(0xFF6B9B4D)     // Verde de las plantas
    val VerdeAcento = Color(0xFF8BAF50)         // Verde claro
    val Amarillo = Color(0xFFF4D03F)            // Amarillo/dorado del sol
    val AzulCielo = Color(0xFF87CEEB)           // Azul cielo del fondo
    val Marron = Color(0xFFC19A6B)              // Marrón/tierra del sombrero
    val Blanco = Color(0xFFFFFFFF)              // Blanco

    // Colores de estados
    val VerdeValidacion = Color(0xFF4CAF50)     // Verde para validaciones exitosas
    val RojoError = Color(0xFFFF5252)           // Rojo para errores

    // Gradientes orgánicos
    val GradienteVerde = Brush.verticalGradient(
        colors = listOf(VerdeAcento, VerdePrincipal)
    )

    val GradienteAmarillo = Brush.horizontalGradient(
        colors = listOf(
            Amarillo.copy(alpha = 0.3f),
            VerdeAcento.copy(alpha = 0.3f)
        )
    )

    val GradienteFondo = Brush.verticalGradient(
        colors = listOf(
            AzulCielo.copy(alpha = 0.4f),
            VerdeAcento.copy(alpha = 0.6f),
            VerdePrincipal
        )
    )
}