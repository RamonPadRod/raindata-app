package hn.unah.raindata.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hn.unah.raindata.ui.theme.RainDataColors

/**
 * 🏛️ HEADER INSTITUCIONAL CON LOGOS
 * Componente que muestra los logos de UNAH y Mesa Agroclimática
 */
@Composable
fun InstitutionalHeader(
    modifier: Modifier = Modifier,
    showAppName: Boolean = true,
    appNameSize: Float = 1f
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val logoSize = if (isTablet) 80.dp else 60.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logos institucionales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo UNAH
            LogoImage(
                drawableRes = hn.unah.raindata.R.drawable.logo_unah,
                contentDescription = "Universidad Nacional Autónoma de Honduras",
                size = logoSize
            )

            Spacer(modifier = Modifier.width(24.dp))

            // Logo Mesa Agroclimática
            LogoImage(
                drawableRes = hn.unah.raindata.R.drawable.logo_mesa_agroclimatica,
                contentDescription = "Mesa Agroclimática del Paraíso",
                size = logoSize
            )
        }

        if (showAppName) {
            Spacer(modifier = Modifier.height(20.dp))

            // Nombre de la aplicación
            Text(
                text = "REVOCLIMAP",
                fontSize = (32 * appNameSize).sp,
                fontWeight = FontWeight.ExtraBold,
                color = RainDataColors.TextoSobreFondo,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Red de Voluntarios Climáticos del Paraíso",
                fontSize = (13 * appNameSize).sp,
                color = RainDataColors.TextoSobreFondo.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * 🖼️ COMPONENTE DE LOGO CON ANIMACIÓN
 */
@Composable
fun LogoImage(
    drawableRes: Int,
    contentDescription: String,
    size: Dp = 60.dp,
    animated: Boolean = true
) {
    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoaded = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = tween(600),
        label = "logo_alpha"
    )

    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(size)
            .scale(if (animated) scale else 1f)
            .alpha(if (animated) alpha else 1f),
        contentScale = ContentScale.Fit
    )
}

/**
 * 📝 CAMPO DE TEXTO MEJORADO CON VISIBILIDAD PERFECTA
 * Resuelve el problema de texto no visible al escribir
 */
@Composable
fun RevoclimapTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = RainDataColors.TextoSecundario.copy(alpha = 0.6f)
                )
            },
            leadingIcon = leadingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isError) RainDataColors.RojoError else RainDataColors.VerdeSecundario,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            isError = isError,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // CRÍTICO: Colores de texto con máximo contraste
                focusedTextColor = RainDataColors.TextoPrincipal,           // Negro para texto activo
                unfocusedTextColor = RainDataColors.TextoPrincipal,         // Negro para texto inactivo
                disabledTextColor = RainDataColors.TextoDeshabilitado,      // Gris para deshabilitado

                // Cursor
                cursorColor = RainDataColors.VerdePrincipal,

                // Bordes
                focusedBorderColor = if (isError) RainDataColors.RojoError else RainDataColors.VerdePrincipal,
                unfocusedBorderColor = if (isError) RainDataColors.RojoError.copy(alpha = 0.5f)
                else RainDataColors.GrisMedio,
                errorBorderColor = RainDataColors.RojoError,

                // Labels
                focusedLabelColor = if (isError) RainDataColors.RojoError else RainDataColors.VerdePrincipal,
                unfocusedLabelColor = RainDataColors.TextoSecundario,
                errorLabelColor = RainDataColors.RojoError,

                // Fondo - blanco puro para máximo contraste
                focusedContainerColor = RainDataColors.Blanco,
                unfocusedContainerColor = RainDataColors.Blanco,
                disabledContainerColor = RainDataColors.GrisClaro,
                errorContainerColor = RainDataColors.Blanco
            ),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = RainDataColors.TextoPrincipal  // Garantizar texto negro
            )
        )

        // Mensaje de error animado
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = RainDataColors.RojoError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorMessage ?: "",
                    fontSize = 12.sp,
                    color = RainDataColors.RojoError,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 🎯 BOTÓN PRINCIPAL ANIMADO
 */
@Composable
fun RevoclimapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String = "Procesando...",
    icon: ImageVector? = null,
    buttonScale: Animatable<Float, AnimationVector1D>? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(buttonScale?.value ?: 1f),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RainDataColors.VerdePrincipal,
            disabledContainerColor = RainDataColors.GrisMedio
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = RainDataColors.Blanco,
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = loadingText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RainDataColors.Blanco
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = RainDataColors.Blanco,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RainDataColors.Blanco
                )
            }
        }
    }
}

/**
 * ⚠️ TARJETA DE ERROR ANIMADA
 */
@Composable
fun ErrorCard(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically() + slideInVertically(),
        exit = fadeOut() + shrinkVertically() + slideOutVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = RainDataColors.RojoError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = RainDataColors.RojoError,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * ✅ TARJETA DE ÉXITO ANIMADA
 */
@Composable
fun SuccessCard(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically() + slideInVertically(),
        exit = fadeOut() + shrinkVertically() + slideOutVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RainDataColors.VerdeValidacion,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 🎈 CÍRCULOS FLOTANTES DECORATIVOS (FONDO ANIMADO)
 */
@Composable
fun FloatingBackgroundCircles(
    colors: List<Color> = listOf(
        RainDataColors.Amarillo.copy(alpha = 0.2f),
        RainDataColors.Blanco.copy(alpha = 0.1f),
        RainDataColors.VerdeAcento.copy(alpha = 0.15f)
    ),
    sizes: List<Dp> = listOf(120.dp, 150.dp, 180.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float_bg")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )

    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset3"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Círculo 1
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 100.dp + offset1.dp)
                .size(sizes.getOrElse(0) { 120.dp })
                .blur(50.dp)
                .background(
                    colors.getOrElse(0) { RainDataColors.Amarillo.copy(alpha = 0.2f) },
                    CircleShape
                )
        )

        // Círculo 2
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = 180.dp + offset2.dp)
                .size(sizes.getOrElse(1) { 150.dp })
                .blur(55.dp)
                .background(
                    colors.getOrElse(1) { RainDataColors.Blanco.copy(alpha = 0.1f) },
                    CircleShape
                )
        )

        // Círculo 3
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-140).dp + offset3.dp)
                .size(sizes.getOrElse(2) { 180.dp })
                .blur(60.dp)
                .background(
                    colors.getOrElse(2) { RainDataColors.VerdeAcento.copy(alpha = 0.15f) },
                    CircleShape
                )
        )
    }
}

/**
 * 🔘 BOTÓN DE RED SOCIAL
 */
@Composable
fun SocialLoginButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "social_scale"
    )

    Surface(
        modifier = modifier
            .size(60.dp)
            .scale(scale),
        shape = CircleShape,
        color = backgroundColor.copy(alpha = 0.1f),
        border = BorderStroke(2.dp, backgroundColor.copy(alpha = 0.3f)),
        shadowElevation = 4.dp
    ) {
        IconButton(
            onClick = {
                pressed = true
                onClick()
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = backgroundColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}