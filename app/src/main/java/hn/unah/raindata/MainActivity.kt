package hn.unah.raindata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import hn.unah.raindata.ui.theme.RainDataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RainDataTheme {
<<<<<<< Updated upstream
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
=======
                var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }

                when (pantallaActual) {
                    Pantalla.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = {
                                pantallaActual = Pantalla.HOME
                            },
                            onNavigateToRegistroAdmin = {
                                pantallaActual = Pantalla.REGISTRO_VOLUNTARIO
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
                                            if (UserSession.isLoggedIn()) {
                                                pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                                            } else {
                                                pantallaActual = Pantalla.LOGIN
                                            }
                                        },
                                        soloAdministrador = !UserSession.isLoggedIn()
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
                                        },
                                        // AGREGADO: Navegación al registro de pluviómetros
                                        onNavegarARegistroPluviometro = {
                                            pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
                                        }
                                    )
                                }

                                Pantalla.LOGIN -> {
                                    // No debería llegar aquí, pero por seguridad
                                }
                            }
                        }
                    }
>>>>>>> Stashed changes
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RainDataTheme {
        Greeting("Android")
    }
}