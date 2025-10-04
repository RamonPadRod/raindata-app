package hn.unah.raindata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.ui.ui.*
import hn.unah.raindata.ui.theme.RainDataTheme

// Enum para manejar las pantallas
enum class Pantalla {
    LOGIN,
    HOME,
    LISTA_VOLUNTARIOS,
    REGISTRO_VOLUNTARIO,
    LISTA_PLUVIOMETROS,
    REGISTRO_PLUVIOMETRO,
    LISTA_DATOS_METEOROLOGICOS,
    REGISTRO_DATO_METEOROLOGICO
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RainDataTheme {
                var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }

                when (pantallaActual) {
                    Pantalla.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = {
                                pantallaActual = Pantalla.HOME
                            }
                        )
                    }

                    else -> {
                        MainLayout(
                            currentScreen = when(pantallaActual) {
                                Pantalla.HOME -> "HOME"
                                Pantalla.LISTA_VOLUNTARIOS -> "VOLUNTARIOS"
                                Pantalla.REGISTRO_VOLUNTARIO -> "REGISTRO_VOLUNTARIO"
                                Pantalla.LISTA_PLUVIOMETROS -> "PLUVIOMETROS"
                                Pantalla.REGISTRO_PLUVIOMETRO -> "REGISTRO_PLUVIOMETRO"
                                Pantalla.LISTA_DATOS_METEOROLOGICOS -> "DATOS_METEOROLOGICOS"
                                Pantalla.REGISTRO_DATO_METEOROLOGICO -> "REGISTRO_DATO_METEOROLOGICO"
                                else -> "HOME"
                            },
                            onNavigateToHome = {
                                pantallaActual = Pantalla.HOME
                            },
                            onNavigateToVoluntarios = {
                                if (UserSession.canViewVoluntarios()) {
                                    pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                                }
                            },
                            onNavigateToPluviometros = {
                                if (UserSession.canViewPluviometros()) {
                                    pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                }
                            },
                            onNavigateToDatosMeteorologicos = {
                                if (UserSession.canViewDatosMeteorologicos()) {
                                    pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                }
                            }
                        ) {
                            when (pantallaActual) {
                                Pantalla.HOME -> {
                                    HomeScreen(
                                        onNavigateToVoluntarios = {
                                            if (UserSession.canViewVoluntarios()) {
                                                pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                                            }
                                        },
                                        onNavigateToPluviometros = {
                                            if (UserSession.canViewPluviometros()) {
                                                pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                            }
                                        },
                                        onNavigateToDatosMeteorologicos = {
                                            if (UserSession.canViewDatosMeteorologicos()) {
                                                pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                            }
                                        },
                                        onLogout = {
                                            pantallaActual = Pantalla.LOGIN
                                        }
                                    )
                                }

                                Pantalla.LISTA_VOLUNTARIOS -> {
                                    ListaVoluntariosScreen(
                                        onAgregarVoluntario = {
                                            if (UserSession.canCreateVoluntarios()) {
                                                pantallaActual = Pantalla.REGISTRO_VOLUNTARIO
                                            }
                                        },
                                        onEditarVoluntario = { voluntario ->
                                            // Aquí puedes implementar la edición más adelante
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_VOLUNTARIO -> {
                                    RegistroVoluntarioScreen(
                                        onVoluntarioGuardado = {
                                            pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                                        }
                                    )
                                }

                                Pantalla.LISTA_PLUVIOMETROS -> {
                                    ListaPluviometrosScreen(
                                        onAgregarPluviometro = {
                                            if (UserSession.canCreatePluviometros()) {
                                                pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
                                            }
                                        },
                                        onEditarPluviometro = { pluviometro ->
                                            // Aquí puedes implementar la edición más adelante
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_PLUVIOMETRO -> {
                                    RegistroPluviometroScreen(
                                        onPluviometroGuardado = {
                                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                        }
                                    )
                                }

                                Pantalla.LISTA_DATOS_METEOROLOGICOS -> {
                                    ListaDatosMeteorologicosScreen(
                                        onAgregarDato = {
                                            if (UserSession.canCreateDatosMeteorologicos()) {
                                                pantallaActual = Pantalla.REGISTRO_DATO_METEOROLOGICO
                                            }
                                        },
                                        onEditarDato = { dato ->
                                            // Aquí puedes implementar la edición más adelante
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_DATO_METEOROLOGICO -> {
                                    RegistroDatoMeteorologicoScreen(
                                        onDatoGuardado = {
                                            pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                        }
                                    )
                                }

                                Pantalla.LOGIN -> {
                                    // No debería llegar aquí, pero por seguridad
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}