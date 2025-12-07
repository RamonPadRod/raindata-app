package hn.unah.raindata.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * REVOCLIMAP - Sistema de Colores Institucional
 * Paleta de colores para la Red de Voluntarios Climáticos del Paraíso
 * Basado en la identidad visual de UNAH y Mesa Agroclimática
 */
object RainDataColors {
    // 🌳 COLORES PRINCIPALES - Verde (Mesa Agroclimática)
    val VerdePrincipal = Color(0xFF2E7D32)      // Verde oscuro institucional
    val VerdeSecundario = Color(0xFF388E3C)     // Verde medio
    val VerdeAcento = Color(0xFF66BB6A)         // Verde claro
    val VerdeSuave = Color(0xFF81C784)          // Verde pastel

    // ☀️ COLORES SECUNDARIOS - Amarillo/Dorado (UNAH)
    val Amarillo = Color(0xFFFDD835)            // Amarillo brillante UNAH
    val AmarilloSuave = Color(0xFFFFF59D)       // Amarillo pastel
    val Dorado = Color(0xFFFFD700)              // Dorado institucional

    // 🌤️ COLORES TERCIARIOS - Azul (Clima)
    val AzulCielo = Color(0xFF81D4FA)           // Azul cielo
    val AzulClaro = Color(0xFFB3E5FC)           // Azul muy claro
    val AzulProfundo = Color(0xFF0288D1)        // Azul oscuro

    // ⚪ COLORES NEUTROS
    val Blanco = Color(0xFFFFFFFF)
    val BlancoHueso = Color(0xFFFAFAFA)
    val GrisClaro = Color(0xFFF5F5F5)
    val GrisMedio = Color(0xFFE0E0E0)
    val GrisOscuro = Color(0xFF757575)
    val Negro = Color(0xFF212121)

    // ✅ COLORES DE ESTADO
    val VerdeValidacion = Color(0xFF4CAF50)     // Verde check/success
    val RojoError = Color(0xFFD32F2F)           // Rojo error
    val Advertencia = Color(0xFFFF9800)         // Naranja advertencia
    val Info = Color(0xFF2196F3)                // Azul información

    // 🎨 GRADIENTES INSTITUCIONALES
    val GradienteVerde = Brush.verticalGradient(
        colors = listOf(VerdePrincipal, VerdeAcento)
    )

    val GradienteVerdeHorizontal = Brush.horizontalGradient(
        colors = listOf(VerdePrincipal, VerdeSecundario)
    )

    val GradienteVerdeCompleto = Brush.verticalGradient(
        colors = listOf(
            VerdePrincipal,
            VerdeSecundario,
            VerdeAcento
        )
    )

    val GradienteAzulVerde = Brush.verticalGradient(
        colors = listOf(
            AzulCielo.copy(alpha = 0.6f),
            VerdeAcento.copy(alpha = 0.8f),
            VerdePrincipal
        )
    )

    val GradienteFondoLogin = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1B5E20),    // Verde muy oscuro (top)
            VerdePrincipal,       // Verde principal (medio)
            VerdeAcento           // Verde claro (bottom)
        )
    )

    val GradienteFondoRegistro = Brush.verticalGradient(
        colors = listOf(
            AzulCielo.copy(alpha = 0.4f),
            VerdeAcento.copy(alpha = 0.6f),
            VerdePrincipal
        )
    )

    val GradienteFondoRecuperar = Brush.verticalGradient(
        colors = listOf(
            AzulProfundo,
            AzulCielo,
            VerdeAcento
        )
    )

    // 🎯 COLORES DE TEXTO
    val TextoPrincipal = Color(0xFF212121)      // Negro suave para texto principal
    val TextoSecundario = Color(0xFF757575)     // Gris para texto secundario
    val TextoDeshabilitado = Color(0xFFBDBDBD)  // Gris claro para deshabilitado
    val TextoSobreFondo = Blanco                // Blanco para texto sobre fondos oscuros

    // 📱 COLORES PARA COMPONENTES
    val BordereTextField = GrisMedio
    val BordereTextFieldFocus = VerdePrincipal
    val FondoTextField = Blanco
    val FondoCard = Blanco.copy(alpha = 0.95f)
    val FondoCardElevado = Blanco

    // 🌈 OPACIDADES ÚTILES
    object Opacity {
        const val TRANSLUCIDO = 0.1f
        const val SUAVE = 0.3f
        const val MEDIO = 0.5f
        const val ALTO = 0.7f
        const val CASI_OPACO = 0.9f
    }
}