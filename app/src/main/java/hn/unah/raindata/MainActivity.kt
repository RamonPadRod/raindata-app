package hn.unah.raindata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import hn.unah.raindata.ui.ui.*
import hn.unah.raindata.ui.theme.RainDataTheme

// Enum para manejar las pantallas
enum class Pantalla {
    HOME,
    LISTA_VOLUNTARIOS,
    REGISTRO_VOLUNTARIO,
    LISTA_PLUVIOMETROS,
    REGISTRO_PLUVIOMETRO
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RainDataTheme {
                var pantallaActual by remember { mutableStateOf(Pantalla.HOME) }

                MainLayout(
                    currentScreen = when(pantallaActual) {
                        Pantalla.HOME -> "HOME"
                        Pantalla.LISTA_VOLUNTARIOS -> "VOLUNTARIOS"
                        Pantalla.REGISTRO_VOLUNTARIO -> "REGISTRO_VOLUNTARIO"
                        Pantalla.LISTA_PLUVIOMETROS -> "PLUVIOMETROS"
                        Pantalla.REGISTRO_PLUVIOMETRO -> "REGISTRO_PLUVIOMETRO"
                    },
                    onNavigateToHome = {
                        pantallaActual = Pantalla.HOME
                    },
                    onNavigateToVoluntarios = {
                        pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                    },
                    onNavigateToPluviometros = {
                        pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                    }
                ) {
                    when (pantallaActual) {
                        Pantalla.HOME -> {
                            HomeScreen(
                                onNavigateToVoluntarios = {
                                    pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                                },
                                onNavigateToPluviometros = {
                                    pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                }
                            )
                        }

                        Pantalla.LISTA_VOLUNTARIOS -> {
                            ListaVoluntariosScreen(
                                onAgregarVoluntario = {
                                    pantallaActual = Pantalla.REGISTRO_VOLUNTARIO
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
                                    pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
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
                    }
                }
            }
        }
    }
}