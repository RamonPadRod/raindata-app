package hn.unah.raindata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.ui.ui.*
import hn.unah.raindata.ui.theme.RainDataTheme
import hn.unah.raindata.viewmodel.AuthViewModel
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Enum para manejar las pantallas
enum class Pantalla {
    LOGIN,
    REGISTRO,
    RECUPERAR_PASSWORD,
    HOME,
    LISTA_VOLUNTARIOS,
    REGISTRO_VOLUNTARIO,
    LISTA_PLUVIOMETROS,
    REGISTRO_PLUVIOMETRO,
    DETALLES_PLUVIOMETRO,      // ✅ NUEVO
    EDITAR_PLUVIOMETRO,         // ✅ NUEVO
    LISTA_DATOS_METEOROLOGICOS,
    REGISTRO_DATO_METEOROLOGICO
}

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar base de datos
        database = AppDatabase.getDatabase(this)

        setContent {
            RainDataTheme {
                val authViewModel: AuthViewModel = viewModel()
                val voluntarioViewModel: VoluntarioViewModel = viewModel()
                val pluviometroViewModel: PluviometroViewModel = viewModel()

                var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }
                var emailRegistrado by remember { mutableStateOf("") }
                var firebaseUidRegistrado by remember { mutableStateOf("") }
                var esPrimerUsuario by remember { mutableStateOf(false) }
                var pluviometroSeleccionado by remember { mutableStateOf<Pluviometro?>(null) } // ✅ NUEVO

                // Verificar si hay usuarios al iniciar
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val voluntarioDao = database.getVoluntarioDao()
                        val totalUsuarios = voluntarioDao.contarTotalUsuarios()
                        esPrimerUsuario = totalUsuarios == 0
                    }
                }

                when (pantallaActual) {
                    Pantalla.LOGIN -> {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = { firebaseUid ->
                                // Buscar usuario en Room por Firebase UID
                                val voluntarioDao = database.getVoluntarioDao()
                                val voluntario = voluntarioDao.obtenerPorFirebaseUid(firebaseUid)

                                if (voluntario != null) {
                                    // Verificar estado de aprobación
                                    when (voluntario.estado_aprobacion) {
                                        "Aprobado" -> {
                                            UserSession.login(voluntario)
                                            pantallaActual = Pantalla.HOME
                                        }

                                        "Pendiente" -> {
                                            // Mostrar mensaje de cuenta pendiente
                                            authViewModel.cerrarSesion()
                                            // TODO: Mostrar un diálogo informando que está pendiente
                                        }

                                        "Rechazado" -> {
                                            // Mostrar mensaje de cuenta rechazada
                                            authViewModel.cerrarSesion()
                                            // TODO: Mostrar un diálogo informando que fue rechazada
                                        }
                                    }
                                } else {
                                    // Usuario no existe en Room, ir a completar registro
                                    pantallaActual = Pantalla.REGISTRO
                                }
                            },
                            onNavigateToRegistro = {
                                pantallaActual = Pantalla.REGISTRO
                            },
                            onNavigateToRecuperarPassword = {
                                pantallaActual = Pantalla.RECUPERAR_PASSWORD
                            }
                        )
                    }

                    Pantalla.REGISTRO -> {
                        RegistroScreen(
                            authViewModel = authViewModel,
                            onRegistroExitoso = { firebaseUid, email ->
                                emailRegistrado = email
                                firebaseUidRegistrado = firebaseUid
                                pantallaActual = Pantalla.REGISTRO_VOLUNTARIO
                            },
                            onNavigateToLogin = {
                                pantallaActual = Pantalla.LOGIN
                            },
                            esPrimerUsuario = esPrimerUsuario
                        )
                    }

                    Pantalla.RECUPERAR_PASSWORD -> {
                        RecuperarPasswordScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = {
                                pantallaActual = Pantalla.LOGIN
                            }
                        )
                    }

                    else -> {
                        MainLayout(
                            currentScreen = when (pantallaActual) {
                                Pantalla.HOME -> "HOME"
                                Pantalla.LISTA_VOLUNTARIOS -> "VOLUNTARIOS"
                                Pantalla.REGISTRO_VOLUNTARIO -> "REGISTRO_VOLUNTARIO"
                                Pantalla.LISTA_PLUVIOMETROS -> "PLUVIOMETROS"
                                Pantalla.REGISTRO_PLUVIOMETRO -> "REGISTRO_PLUVIOMETRO"
                                Pantalla.DETALLES_PLUVIOMETRO -> "PLUVIOMETROS"      // ✅ NUEVO
                                Pantalla.EDITAR_PLUVIOMETRO -> "PLUVIOMETROS"         // ✅ NUEVO
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
                                            authViewModel.cerrarSesion()
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
                                            // TODO: Implementar edición de voluntarios
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_VOLUNTARIO -> {
                                    var intentarLogin by remember { mutableStateOf(false) }
                                    var tipoUsuarioGuardado by remember { mutableStateOf("") }

                                    // Login automático después de guardar
                                    LaunchedEffect(intentarLogin) {
                                        if (intentarLogin) {
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    val voluntarioDao = database.getVoluntarioDao()
                                                    val voluntario = voluntarioDao.obtenerPorEmail(emailRegistrado)

                                                    // Actualizar si ya no es el primer usuario
                                                    val totalUsuarios = voluntarioDao.contarTotalUsuarios()
                                                    esPrimerUsuario = totalUsuarios == 0

                                                    withContext(Dispatchers.Main) {
                                                        if (voluntario != null) {
                                                            // Login automático exitoso
                                                            UserSession.login(voluntario)

                                                            // Redirigir según rol
                                                            when (tipoUsuarioGuardado) {
                                                                "Administrador" -> {
                                                                    pantallaActual = Pantalla.HOME
                                                                }
                                                                "Voluntario" -> {
                                                                    pantallaActual = Pantalla.HOME
                                                                }
                                                                "Observador" -> {
                                                                    pantallaActual = Pantalla.HOME
                                                                }
                                                                else -> {
                                                                    pantallaActual = Pantalla.LOGIN
                                                                }
                                                            }
                                                        } else {
                                                            pantallaActual = Pantalla.LOGIN
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        pantallaActual = Pantalla.LOGIN
                                                    }
                                                }
                                            }
                                            intentarLogin = false
                                        }
                                    }

                                    RegistroVoluntarioScreen(
                                        emailPrecargado = emailRegistrado,
                                        firebaseUid = firebaseUidRegistrado,
                                        onVoluntarioGuardado = { tipoUsuario ->
                                            tipoUsuarioGuardado = tipoUsuario
                                            intentarLogin = true
                                        },
                                        soloAdministrador = esPrimerUsuario
                                    )
                                }

                                // ✅ LISTA DE PLUVIÓMETROS - ACTUALIZADO
                                Pantalla.LISTA_PLUVIOMETROS -> {
                                    ListaPluviometrosScreen(
                                        viewModel = pluviometroViewModel,
                                        onAgregarPluviometro = {
                                            if (UserSession.canCreatePluviometros()) {
                                                pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
                                            }
                                        },
                                        onVerDetalles = { pluviometro ->
                                            pluviometroSeleccionado = pluviometro
                                            pantallaActual = Pantalla.DETALLES_PLUVIOMETRO
                                        },
                                        onEditarPluviometro = { pluviometro ->
                                            pluviometroSeleccionado = pluviometro
                                            pantallaActual = Pantalla.EDITAR_PLUVIOMETRO
                                        }
                                    )
                                }

                                // ✅ REGISTRO DE PLUVIÓMETRO
                                Pantalla.REGISTRO_PLUVIOMETRO -> {
                                    RegistroPluviometroScreen(
                                        pluviometroViewModel = pluviometroViewModel,
                                        voluntarioViewModel = voluntarioViewModel,
                                        onPluviometroGuardado = {
                                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                        }
                                    )
                                }

                                // ✅ NUEVO: DETALLES DE PLUVIÓMETRO
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

                                // ✅ NUEVO: EDITAR PLUVIÓMETRO
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

                                Pantalla.LISTA_DATOS_METEOROLOGICOS -> {
                                    ListaDatosMeteorologicosScreen(
                                        onAgregarDato = {
                                            if (UserSession.canCreateDatosMeteorologicos()) {
                                                pantallaActual = Pantalla.REGISTRO_DATO_METEOROLOGICO
                                            }
                                        },
                                        onEditarDato = { dato ->
                                            // TODO: Implementar edición de datos meteorológicos
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_DATO_METEOROLOGICO -> {
                                    RegistroDatoMeteorologicoScreen(
                                        onDatoGuardado = {
                                            pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                        },
                                        onNavegarARegistroPluviometro = {
                                            pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
                                        }
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}