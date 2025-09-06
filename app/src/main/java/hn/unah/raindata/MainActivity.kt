package hn.unah.raindata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import hn.unah.raindata.ui.ui.ListaVoluntariosScreen
import hn.unah.raindata.ui.ui.RegistroVoluntarioScreen
import hn.unah.raindata.ui.theme.RainDataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RainDataTheme {
                var mostrarRegistro by remember { mutableStateOf(false) }

                if (mostrarRegistro) {
                    RegistroVoluntarioScreen(
                        onVoluntarioGuardado = {
                            mostrarRegistro = false
                        }
                    )
                } else {
                    ListaVoluntariosScreen(
                        onAgregarVoluntario = {
                            mostrarRegistro = true
                        }
                    )
                }
            }
        }
    }
}