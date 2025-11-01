package hn.unah.raindata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.ui.ui.*
import hn.unah.raindata.ui.theme.RainDataTheme
<<<<<<< Updated upstream
=======
import hn.unah.raindata.viewmodel.AuthViewModel
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
>>>>>>> Stashed changes

// Enum para manejar las pantallas
enum class Pantalla {
    LOGIN,
    HOME,
    LISTA_VOLUNTARIOS,
    REGISTRO_VOLUNTARIO,
    LISTA_PLUVIOMETROS,
    REGISTRO_PLUVIOMETRO,
<<<<<<< Updated upstream
=======
    DETALLES_PLUVIOMETRO,
    EDITAR_PLUVIOMETRO,
>>>>>>> Stashed changes
    LISTA_DATOS_METEOROLOGICOS,
    REGISTRO_DATO_METEOROLOGICO,
    DETALLES_DATO_METEOROLOGICO,
    EDITAR_DATO_METEOROLOGICO
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RainDataTheme {
<<<<<<< Updated upstream
                var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }
=======
                val authViewModel: AuthViewModel = viewModel()
                val voluntarioViewModel: VoluntarioViewModel = viewModel()
                val pluviometroViewModel: PluviometroViewModel = viewModel()
                val datoMeteorologicoViewModel: DatoMeteorologicoViewModel = viewModel()

                var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }
                var emailRegistrado by remember { mutableStateOf("") }
                var firebaseUidRegistrado by remember { mutableStateOf("") }
                var esPrimerUsuario by remember { mutableStateOf(false) }
                var pluviometroSeleccionado by remember { mutableStateOf<Pluviometro?>(null) }
                var datoMeteorologicoIdSeleccionado by remember { mutableStateOf<String?>(null) }

                // Control para doble tap de salida
                var intentosSalir by remember { mutableStateOf(0) }
                val scope = rememberCoroutineScope()

                // Función para manejar doble tap de salida
                fun manejarSalida() {
                    if (intentosSalir == 0) {
                        Toast.makeText(
                            this@MainActivity,
                            "Presiona atrás nuevamente para salir",
                            Toast.LENGTH_SHORT
                        ).show()
                        intentosSalir = 1
                        scope.launch {
                            delay(2000)
                            intentosSalir = 0
                        }
                    } else {
                        finish()
                    }
                }

                // Manejo inteligente del botón Atrás
                BackHandler(enabled = true) {
                    when (pantallaActual) {
                        Pantalla.LOGIN -> {
                            manejarSalida()
                        }
                        Pantalla.REGISTRO -> {
                            pantallaActual = Pantalla.LOGIN
                        }
                        Pantalla.RECUPERAR_PASSWORD -> {
                            pantallaActual = Pantalla.LOGIN
                        }
                        Pantalla.HOME -> {
                            manejarSalida()
                        }
                        Pantalla.LISTA_VOLUNTARIOS,
                        Pantalla.LISTA_PLUVIOMETROS,
                        Pantalla.LISTA_DATOS_METEOROLOGICOS -> {
                            pantallaActual = Pantalla.HOME
                        }
                        Pantalla.REGISTRO_VOLUNTARIO -> {
                            if (UserSession.isLoggedIn()) {
                                pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                            } else {
                                authViewModel.cerrarSesion()
                                pantallaActual = Pantalla.LOGIN
                            }
                        }
                        Pantalla.REGISTRO_PLUVIOMETRO -> {
                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                        }
                        Pantalla.REGISTRO_DATO_METEOROLOGICO -> {
                            pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                        }
                        Pantalla.DETALLES_PLUVIOMETRO -> {
                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                            pluviometroSeleccionado = null
                        }
                        Pantalla.EDITAR_PLUVIOMETRO -> {
                            pantallaActual = Pantalla.DETALLES_PLUVIOMETRO
                        }
                        Pantalla.DETALLES_DATO_METEOROLOGICO -> {
                            pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                            datoMeteorologicoIdSeleccionado = null
                        }
                        Pantalla.EDITAR_DATO_METEOROLOGICO -> {
                            pantallaActual = Pantalla.DETALLES_DATO_METEOROLOGICO
                        }
                    }
                }

                // Verificar si hay usuarios al iniciar
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val voluntarioDao = database.getVoluntarioDao()
                        val totalUsuarios = voluntarioDao.contarTotalUsuarios()
                        esPrimerUsuario = totalUsuarios == 0
                    }
                }
>>>>>>> Stashed changes

                when (pantallaActual) {
                    Pantalla.LOGIN -> {
                        LoginScreen(
<<<<<<< Updated upstream
                            onLoginSuccess = {
                                pantallaActual = Pantalla.HOME
=======
                            authViewModel = authViewModel,
                            onLoginSuccess = { firebaseUid ->
                                val voluntarioDao = database.getVoluntarioDao()
                                val voluntario = voluntarioDao.obtenerPorFirebaseUid(firebaseUid)

                                if (voluntario != null) {
                                    when (voluntario.estado_aprobacion) {
                                        "Aprobado" -> {
                                            UserSession.login(voluntario)
                                            pantallaActual = Pantalla.HOME
                                        }
                                        "Pendiente" -> {
                                            authViewModel.cerrarSesion()
                                        }
                                        "Rechazado" -> {
                                            authViewModel.cerrarSesion()
                                        }
                                    }
                                } else {
                                    pantallaActual = Pantalla.REGISTRO
                                }
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
                                Pantalla.DETALLES_PLUVIOMETRO -> "PLUVIOMETROS"
                                Pantalla.EDITAR_PLUVIOMETRO -> "PLUVIOMETROS"
>>>>>>> Stashed changes
                                Pantalla.LISTA_DATOS_METEOROLOGICOS -> "DATOS_METEOROLOGICOS"
                                Pantalla.REGISTRO_DATO_METEOROLOGICO -> "REGISTRO_DATO_METEOROLOGICO"
                                Pantalla.DETALLES_DATO_METEOROLOGICO -> "DATOS_METEOROLOGICOS"
                                Pantalla.EDITAR_DATO_METEOROLOGICO -> "DATOS_METEOROLOGICOS"
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

                                // ✅ CORREGIDO: LISTA_VOLUNTARIOS con recarga explícita
                                Pantalla.LISTA_VOLUNTARIOS -> {
                                    // ✅ CRÍTICO: Recargar cada vez que se entra a la pantalla
                                    LaunchedEffect(pantallaActual) {
                                        voluntarioViewModel.cargarVoluntarios()
                                    }

                                    ListaVoluntariosScreen(
                                        viewModel = voluntarioViewModel,
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

                                // ✅ CORREGIDO: REGISTRO_VOLUNTARIO con recarga después de guardar
                                Pantalla.REGISTRO_VOLUNTARIO -> {
                                    RegistroVoluntarioScreen(
<<<<<<< Updated upstream
                                        onVoluntarioGuardado = {
                                            if (UserSession.isLoggedIn()) {
                                                pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                                            } else {
                                                pantallaActual = Pantalla.LOGIN
=======
                                        viewModel = voluntarioViewModel,
                                        emailPrecargado = emailRegistrado,
                                        firebaseUid = firebaseUidRegistrado,
                                        onVoluntarioGuardado = { tipoUsuario ->
                                            // ✅ SOLUCIÓN: Lanzar coroutine secuencial
                                            scope.launch {
                                                // 1. Esperar a que se complete el guardado en BD
                                                delay(500)

                                                // 2. Recargar explícitamente la lista de voluntarios
                                                voluntarioViewModel.cargarVoluntarios()

                                                // 3. Esperar a que se complete la recarga
                                                delay(500)

                                                // 4. Ahora sí, buscar el voluntario en BD
                                                withContext(Dispatchers.IO) {
                                                    try {
                                                        val voluntarioDao = database.getVoluntarioDao()
                                                        val voluntario = voluntarioDao.obtenerPorEmail(emailRegistrado)

                                                        // Actualizar contador
                                                        val totalUsuarios = voluntarioDao.contarTotalUsuarios()

                                                        withContext(Dispatchers.Main) {
                                                            esPrimerUsuario = totalUsuarios == 0

                                                            if (voluntario != null) {
                                                                // Login automático
                                                                UserSession.login(voluntario)

                                                                // Redirigir según rol
                                                                when (tipoUsuario) {
                                                                    "Administrador", "Voluntario", "Observador" -> {
                                                                        pantallaActual = Pantalla.HOME
                                                                    }
                                                                    else -> {
                                                                        pantallaActual = Pantalla.LOGIN
                                                                    }
                                                                }
                                                            } else {
                                                                Toast.makeText(
                                                                    this@MainActivity,
                                                                    "Error: No se pudo cargar el voluntario",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                pantallaActual = Pantalla.LOGIN
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "Error: ${e.message}",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            pantallaActual = Pantalla.LOGIN
                                                        }
                                                    }
                                                }
>>>>>>> Stashed changes
                                            }
                                        },
                                        soloAdministrador = !UserSession.isLoggedIn()
                                    )
                                }

<<<<<<< Updated upstream
=======
                                // LISTA DE PLUVIÓMETROS
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======
                                // REGISTRO DE PLUVIÓMETRO
>>>>>>> Stashed changes
                                Pantalla.REGISTRO_PLUVIOMETRO -> {
                                    RegistroPluviometroScreen(
                                        onPluviometroGuardado = {
                                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                        }
                                    )
                                }

<<<<<<< Updated upstream
=======
                                // DETALLES DE PLUVIÓMETRO
                                Pantalla.DETALLES_PLUVIOMETRO -> {
                                    pluviometroSeleccionado?.let { pluviometro ->
                                        DetallesPluviometroScreen(
                                            pluviometro = pluviometro,
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                            },
                                            onEditar = {
                                                pantallaActual = Pantalla.EDITAR_PLUVIOMETRO
                                            },
                                            onEliminar = {
                                                pluviometroViewModel.eliminarPluviometro(pluviometro.id)
                                                pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                            }
                                        )
                                    }
                                }

                                // EDITAR PLUVIÓMETRO
                                Pantalla.EDITAR_PLUVIOMETRO -> {
                                    pluviometroSeleccionado?.let { pluviometro ->
                                        EditarPluviometroScreen(
                                            pluviometro = pluviometro,
                                            pluviometroViewModel = pluviometroViewModel,
                                            voluntarioViewModel = voluntarioViewModel,
                                            onPluviometroActualizado = {
                                                pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                            },
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.DETALLES_PLUVIOMETRO
                                            }
                                        )
                                    }
                                }

                                // LISTA DE DATOS METEOROLÓGICOS
>>>>>>> Stashed changes
                                Pantalla.LISTA_DATOS_METEOROLOGICOS -> {
                                    ListaDatosMeteorologicosScreen(
                                        viewModel = datoMeteorologicoViewModel,
                                        onAgregarDato = {
                                            if (UserSession.canCreateDatosMeteorologicos()) {
                                                pantallaActual = Pantalla.REGISTRO_DATO_METEOROLOGICO
                                            }
                                        },
                                        onVerDetalles = { dato ->
                                            datoMeteorologicoIdSeleccionado = dato.id
                                            pantallaActual = Pantalla.DETALLES_DATO_METEOROLOGICO
                                        },
                                        onEditarDato = { dato ->
<<<<<<< Updated upstream
                                            // Aquí puedes implementar la edición más adelante
=======
                                            datoMeteorologicoIdSeleccionado = dato.id
                                            pantallaActual = Pantalla.EDITAR_DATO_METEOROLOGICO
>>>>>>> Stashed changes
                                        }
                                    )
                                }

                                // REGISTRO DE DATO METEOROLÓGICO
                                Pantalla.REGISTRO_DATO_METEOROLOGICO -> {
                                    RegistroDatoMeteorologicoScreen(
                                        datoMeteorologicoViewModel = datoMeteorologicoViewModel,
                                        voluntarioViewModel = voluntarioViewModel,
                                        pluviometroViewModel = pluviometroViewModel,
                                        onDatoGuardado = {
                                            pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                        },
                                        // AGREGADO: Navegación al registro de pluviómetros
                                        onNavegarARegistroPluviometro = {
                                            pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
                                        }
                                    )
                                }

<<<<<<< Updated upstream
                                Pantalla.LOGIN -> {
                                    // No debería llegar aquí, pero por seguridad
                                }
=======
                                // DETALLES DE DATO METEOROLÓGICO
                                Pantalla.DETALLES_DATO_METEOROLOGICO -> {
                                    datoMeteorologicoIdSeleccionado?.let { datoId ->
                                        DetallesDatoMeteorologicoScreen(
                                            datoId = datoId,
                                            viewModel = datoMeteorologicoViewModel,
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                            },
                                            onEditar = {
                                                pantallaActual = Pantalla.EDITAR_DATO_METEOROLOGICO
                                            },
                                            onEliminar = {
                                                datoMeteorologicoViewModel.eliminarDato(datoId)
                                                pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                            }
                                        )
                                    }
                                }

                                // EDITAR DATO METEOROLÓGICO
                                Pantalla.EDITAR_DATO_METEOROLOGICO -> {
                                    datoMeteorologicoIdSeleccionado?.let { datoId ->
                                        EditarDatoMeteorologicoScreen(
                                            datoId = datoId,
                                            datoMeteorologicoViewModel = datoMeteorologicoViewModel,
                                            voluntarioViewModel = voluntarioViewModel,
                                            pluviometroViewModel = pluviometroViewModel,
                                            onDatoActualizado = {
                                                pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                            },
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.DETALLES_DATO_METEOROLOGICO
                                            }
                                        )
                                    }
                                }

                                else -> {}
>>>>>>> Stashed changes
                            }
                        }
                    }
                }
            }
        }
    }
}