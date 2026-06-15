package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import hn.unah.raindata.data.session.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    currentScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToVoluntarios: () -> Unit,
    onNavigateToPluviometros: () -> Unit,
    onNavigateToDatosMeteorologicos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToEstadisticas: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentScreen = currentScreen,
                onNavigateToHome = {
                    onNavigateToHome()
                    scope.launch { drawerState.close() }
                },
                onNavigateToVoluntarios = {
                    onNavigateToVoluntarios()
                    scope.launch { drawerState.close() }
                },
                onNavigateToPluviometros = {
                    onNavigateToPluviometros()
                    scope.launch { drawerState.close() }
                },
                onNavigateToDatosMeteorologicos = {
                    onNavigateToDatosMeteorologicos()
                    scope.launch { drawerState.close() }
                },
                onNavigateToEstadisticas = {
                    onNavigateToEstadisticas()
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when(currentScreen) {
                                "HOME" -> "DatosLluvia"
                                "VOLUNTARIOS" -> "Gestión de voluntarios"
                                "REGISTRO_VOLUNTARIO" -> "Registro de voluntario"
                                "PLUVIOMETROS" -> "Gestión de pluviómetros"
                                "REGISTRO_PLUVIOMETRO" -> "Registro de pluviómetro"
                                "DATOS_METEOROLOGICOS" -> "Datos meteorológicos"
                                "REGISTRO_DATO_METEOROLOGICO" -> "Registro de dato meteorológico"
                                "PERFIL" -> "Perfil de usuario"
                                "ESTADISTICAS" -> "Estadísticas y Reportes"
                                else -> "DatosLluvia"
                            }
                        )
                    },
                    navigationIcon = {
                        if (UserSession.isLoggedIn()) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) {
                                            drawerState.open()
                                        } else {
                                            drawerState.close()
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                if (UserSession.isLoggedIn()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                                .height(80.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomNavigationItem(
                                icon = Icons.Default.Home,
                                label = "Inicio",
                                selected = currentScreen == "HOME",
                                onClick = onNavigateToHome,
                                modifier = Modifier.weight(1f)
                            )
                            
                            CustomNavigationItem(
                                icon = Icons.Default.WaterDrop,
                                label = "Pluvió.",
                                selected = currentScreen == "PLUVIOMETROS" || currentScreen == "REGISTRO_PLUVIOMETRO",
                                onClick = onNavigateToPluviometros,
                                modifier = Modifier.weight(1f)
                            )
                            
                            CustomNavigationItem(
                                icon = Icons.Default.Cloud,
                                label = "Clima",
                                selected = currentScreen == "DATOS_METEOROLOGICOS" || currentScreen == "REGISTRO_DATO_METEOROLOGICO",
                                onClick = onNavigateToDatosMeteorologicos,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (UserSession.canViewVoluntarios()) {
                                CustomNavigationItem(
                                    icon = Icons.Default.People,
                                    label = "Volunt.",
                                    selected = currentScreen == "VOLUNTARIOS" || currentScreen == "REGISTRO_VOLUNTARIO",
                                    onClick = onNavigateToVoluntarios,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            CustomNavigationItem(
                                icon = Icons.Default.Assessment,
                                label = "Reportes",
                                selected = currentScreen == "ESTADISTICAS",
                                enabled = true,
                                onClick = onNavigateToEstadisticas,
                                modifier = Modifier.weight(1f)
                            )
                            
                            CustomNavigationItem(
                                icon = Icons.Default.Person,
                                label = "Perfil",
                                selected = currentScreen == "PERFIL",
                                onClick = onNavigateToPerfil,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content()
            }
        }
    }
}

@Composable
fun DrawerContent(
    currentScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToVoluntarios: () -> Unit,
    onNavigateToPluviometros: () -> Unit,
    onNavigateToDatosMeteorologicos: () -> Unit,
    onNavigateToEstadisticas: () -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header del drawer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DatosLluvia",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sistema pluviométrico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Items del menú (Filtro por roles incluido)
            DrawerMenuItem(
                icon = Icons.Default.Home,
                title = "Inicio",
                isSelected = currentScreen == "HOME",
                onClick = onNavigateToHome
            )

            if (UserSession.canViewVoluntarios()) {
                DrawerMenuItem(
                    icon = Icons.Default.People,
                    title = "Gestión de voluntarios",
                    isSelected = currentScreen == "VOLUNTARIOS" || currentScreen == "REGISTRO_VOLUNTARIO",
                    isEnabled = true,
                    onClick = onNavigateToVoluntarios
                )
            }

            DrawerMenuItem(
                icon = Icons.Default.LocationOn,
                title = "Gestión de pluviómetros",
                isSelected = currentScreen == "PLUVIOMETROS" || currentScreen == "REGISTRO_PLUVIOMETRO",
                isEnabled = true,
                onClick = onNavigateToPluviometros
            )

            DrawerMenuItem(
                icon = Icons.Default.CloudQueue,
                title = "Datos meteorológicos",
                isSelected = currentScreen == "DATOS_METEOROLOGICOS" || currentScreen == "REGISTRO_DATO_METEOROLOGICO",
                isEnabled = true,
                onClick = onNavigateToDatosMeteorologicos
            )

            DrawerMenuItem(
                icon = Icons.Default.Assessment,
                title = "Estadísticas y Reportes",
                isSelected = currentScreen == "ESTADISTICAS",
                isEnabled = true,
                onClick = onNavigateToEstadisticas
            )

            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Cerrar sesión",
                isSelected = false,
                isEnabled = true,
                onClick = onLogout
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            HorizontalDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Versión 1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (isEnabled) {
                        if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                if (!isEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "Próximamente",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        icon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isEnabled) {
                    if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }
            )
        },
        selected = isSelected && isEnabled,
        onClick = if (isEnabled) onClick else { {} },
        modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
    )
}

@Composable
private fun CustomNavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (selected && enabled) {
                            Modifier.background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (!enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (selected && enabled) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}