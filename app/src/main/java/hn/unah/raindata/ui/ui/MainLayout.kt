package hn.unah.raindata.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    currentScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToVoluntarios: () -> Unit,
    onNavigateToPluviometros: () -> Unit,
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
                                "VOLUNTARIOS" -> "Gestión de Voluntarios"
                                "REGISTRO_VOLUNTARIO" -> "Registro de Voluntario"
                                "PLUVIOMETROS" -> "Gestión de Pluviómetros"
                                "REGISTRO_PLUVIOMETRO" -> "Registro de Pluviómetro"
                                "DATOS_PLUVIOMETRICOS" -> "Datos Pluviométricos"
                                "DATOS_CLIMATICOS" -> "Datos Climáticos"
                                else -> "DatosLluvia"
                            }
                        )
                    },
                    navigationIcon = {
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
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
                        text = "Sistema Pluviométrico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Items del menú
            DrawerMenuItem(
                icon = Icons.Default.Home,
                title = "Inicio",
                isSelected = currentScreen == "HOME",
                onClick = onNavigateToHome
            )

            DrawerMenuItem(
                icon = Icons.Default.Person,
                title = "Gestión de Voluntarios",
                isSelected = currentScreen == "VOLUNTARIOS" || currentScreen == "REGISTRO_VOLUNTARIO",
                isEnabled = true,
                onClick = onNavigateToVoluntarios
            )

            DrawerMenuItem(
                icon = Icons.Default.LocationOn,
                title = "Gestión de Pluviómetros",
                isSelected = currentScreen == "PLUVIOMETROS" || currentScreen == "REGISTRO_PLUVIOMETRO",
                isEnabled = true,
                onClick = onNavigateToPluviometros
            )

            DrawerMenuItem(
                icon = Icons.Default.WaterDrop,
                title = "Datos Pluviométricos",
                isSelected = currentScreen == "DATOS_PLUVIOMETRICOS",
                isEnabled = false,
                onClick = { /* Sin función por ahora */ }
            )

            DrawerMenuItem(
                icon = Icons.Default.Thermostat,
                title = "Datos Climáticos",
                isSelected = currentScreen == "DATOS_CLIMATICOS",
                isEnabled = false,
                onClick = { /* Sin función por ahora */ }
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