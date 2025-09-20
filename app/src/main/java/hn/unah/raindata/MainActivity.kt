package hn.unah.raindata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import hn.unah.raindata.ui.ui.HomeScreen
import hn.unah.raindata.ui.ui.ListaVoluntariosScreen
import hn.unah.raindata.ui.ui.RegistroVoluntarioScreen
import hn.unah.raindata.ui.theme.RainDataTheme

// Enum para manejar las pantallas
enum class Pantalla {
    HOME,
    LISTA_VOLUNTARIOS,
    REGISTRO_VOLUNTARIO
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RainDataTheme {
                var pantallaActual by remember { mutableStateOf(Pantalla.HOME) }

                when (pantallaActual) {
                    Pantalla.HOME -> {
                        HomeScreen(
                            onNavigateToVoluntarios = {
                                pantallaActual = Pantalla.LISTA_VOLUNTARIOS
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
                }
            }
        }
    }
}