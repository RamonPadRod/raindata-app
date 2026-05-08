package hn.unah.raindata

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import hn.unah.raindata.data.session.UserSession
import hn.unah.raindata.ui.ui.*  // <-- ESTA LÍNEA ES CRÍTICA
import hn.unah.raindata.ui.theme.RainDataTheme
import hn.unah.raindata.viewmodel.AuthViewModel
import hn.unah.raindata.viewmodel.DatoMeteorologicoViewModel
import hn.unah.raindata.viewmodel.PluviometroViewModel
import hn.unah.raindata.viewmodel.VoluntarioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class Pantalla {
    LOGIN,
    REGISTRO,
    RECUPERAR_PASSWORD,

    SOLICITUD_PENDIENTE,
    HOME,
    LISTA_VOLUNTARIOS,
    REGISTRO_VOLUNTARIO,
    LISTA_PLUVIOMETROS,
    REGISTRO_PLUVIOMETRO,
    DETALLES_PLUVIOMETRO,
    EDITAR_PLUVIOMETRO,
    LISTA_DATOS_METEOROLOGICOS,
    REGISTRO_DATO_METEOROLOGICO,
    DETALLES_DATO_METEOROLOGICO,
    EDITAR_DATO_METEOROLOGICO
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locale = java.util.Locale("es", "HN")
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        setContent {
            RainDataTheme {
                val authViewModel: AuthViewModel = viewModel()
                val voluntarioViewModel: VoluntarioViewModel = viewModel()
                val pluviometroViewModel: PluviometroViewModel = viewModel()
                val datoMeteorologicoViewModel: DatoMeteorologicoViewModel = viewModel()

                // rememberSaveable: sobrevive a Configuration Changes (rotación, tema, bluetooth)
                var pantallaActual by rememberSaveable { mutableStateOf(Pantalla.LOGIN) }
                var emailRegistrado by rememberSaveable { mutableStateOf("") }
                var firebaseUidRegistrado by rememberSaveable { mutableStateOf("") }
                // Guardar solo IDs (String) en lugar de objetos completos (no serializables)
                var pluviometroSeleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }
                var datoMeteorologicoSeleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }

                // Derivar objetos completos desde los IDs usando los StateFlows de los ViewModels
                val pluviometrosState by pluviometroViewModel.pluviometros.collectAsState()
                val datosMeteorologicosState by datoMeteorologicoViewModel.datosMeteorologicos.collectAsState()
                val pluviometroSeleccionado = pluviometroSeleccionadoId?.let { id ->
                    pluviometrosState.find { it.id == id }
                }
                val datoMeteorologicoSeleccionado = datoMeteorologicoSeleccionadoId?.let { id ->
                    datosMeteorologicosState.find { it.id == id }
                }

                // Fallback: Si hay un ID guardado pero el ViewModel aún no tiene los datos cargados
                // (ocurre después de un Configuration Change), cargar los datos necesarios
                LaunchedEffect(datoMeteorologicoSeleccionadoId) {
                    val id = datoMeteorologicoSeleccionadoId
                    if (id != null && datosMeteorologicosState.none { it.id == id }) {
                        datoMeteorologicoViewModel.cargarTodosDatos()
                    }
                }

                var intentosSalir by remember { mutableStateOf(0) }
                val scope = rememberCoroutineScope()

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

                BackHandler(enabled = true) {
                    when (pantallaActual) {
                        Pantalla.LOGIN -> manejarSalida()
                        Pantalla.REGISTRO -> pantallaActual = Pantalla.LOGIN
                        Pantalla.RECUPERAR_PASSWORD -> pantallaActual = Pantalla.LOGIN
                        Pantalla.SOLICITUD_PENDIENTE -> manejarSalida()  // ← AGREGAR ESTA LÍNEA
                        Pantalla.HOME -> manejarSalida()
                        Pantalla.LISTA_VOLUNTARIOS,
                        Pantalla.LISTA_PLUVIOMETROS,
                        Pantalla.LISTA_DATOS_METEOROLOGICOS -> pantallaActual = Pantalla.HOME
                        Pantalla.REGISTRO_VOLUNTARIO -> {
                            if (UserSession.isLoggedIn()) {
                                pantallaActual = Pantalla.LISTA_VOLUNTARIOS
                            } else {
                                authViewModel.cerrarSesion()
                                pantallaActual = Pantalla.LOGIN
                            }
                        }
                        Pantalla.REGISTRO_PLUVIOMETRO -> pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                        Pantalla.REGISTRO_DATO_METEOROLOGICO -> pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                        Pantalla.DETALLES_PLUVIOMETRO -> {
                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                            pluviometroSeleccionadoId = null
                        }
                        Pantalla.EDITAR_PLUVIOMETRO -> pantallaActual = Pantalla.DETALLES_PLUVIOMETRO
                        Pantalla.DETALLES_DATO_METEOROLOGICO -> {
                            pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                            datoMeteorologicoSeleccionadoId = null
                        }
                        Pantalla.EDITAR_DATO_METEOROLOGICO -> pantallaActual = Pantalla.DETALLES_DATO_METEOROLOGICO
                    }
                }

                when (pantallaActual) {
                    Pantalla.LOGIN -> {
                        // Protección contra Configuration Changes (rotación, tema, bluetooth):
                        // Si el usuario ya tenía sesión activa, redirigir en lugar de destruirla
                        LaunchedEffect(Unit) {
                            if (UserSession.hasActiveSession()) {
                                // El usuario llegó aquí por un Configuration Change, redirigir
                                when (UserSession.getApprovalStatus()) {
                                    "Aprobado" -> pantallaActual = Pantalla.HOME
                                    "Pendiente" -> pantallaActual = Pantalla.SOLICITUD_PENDIENTE
                                    else -> {
                                        // Solo hacer logout si el estado es Rechazado o desconocido
                                        UserSession.logout()
                                    }
                                }
                            }
                            // Si NO hay sesión activa, no hacemos nada: el usuario ya está en LOGIN correctamente
                        }

                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = { firebaseUid ->
                                scope.launch {
                                    val voluntario = voluntarioViewModel.buscarPorFirebaseUid(firebaseUid)

                                    if (voluntario != null) {
                                        when (voluntario.estado_aprobacion) {
                                            "Aprobado" -> {
                                                UserSession.login(voluntario)
                                                pantallaActual = Pantalla.HOME
                                            }
                                            "Pendiente" -> {
                                                // ✅ IR A PANTALLA DE ESPERA (en lugar de cerrar sesión)
                                                UserSession.login(voluntario)  // Cargar sesión temporalmente
                                                pantallaActual = Pantalla.SOLICITUD_PENDIENTE
                                            }
                                            "Rechazado" -> {
                                                authViewModel.cerrarSesion()
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Tu solicitud fue rechazada. Contacta al administrador",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    } else {
                                        emailRegistrado = ""
                                        firebaseUidRegistrado = firebaseUid
                                        pantallaActual = Pantalla.REGISTRO_VOLUNTARIO
                                    }
                                }
                            },
                            onNavigateToRegistro = { pantallaActual = Pantalla.REGISTRO },
                            onNavigateToRecuperarPassword = { pantallaActual = Pantalla.RECUPERAR_PASSWORD }
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
                            onNavigateToLogin = { pantallaActual = Pantalla.LOGIN },
                            esPrimerUsuario = false
                        )
                    }

                    Pantalla.RECUPERAR_PASSWORD -> {
                        RecuperarPasswordScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = { pantallaActual = Pantalla.LOGIN }
                        )
                    }

                    Pantalla.SOLICITUD_PENDIENTE -> {
                        PantallaSolicitudPendiente(
                            nombreUsuario = UserSession.getCurrentUserName(),
                            emailUsuario = UserSession.getCurrentUser()?.email ?: "",
                            onCerrarSesion = {
                                authViewModel.cerrarSesion()
                                UserSession.logout()
                                pantallaActual = Pantalla.LOGIN
                            },
                            onCancelarSolicitud = {
                                scope.launch {
                                    val currentUser = UserSession.getCurrentUser()

                                    if (currentUser != null) {
                                        voluntarioViewModel.eliminarVoluntario(
                                            firebaseUid = currentUser.firebase_uid,
                                            onSuccess = {
                                                authViewModel.eliminarCuentaActual(
                                                    onSuccess = {
                                                        UserSession.logout()

                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "✅ Solicitud cancelada exitosamente",
                                                            Toast.LENGTH_LONG
                                                        ).show()

                                                        pantallaActual = Pantalla.LOGIN
                                                    },
                                                    onError = { error ->
                                                        authViewModel.cerrarSesion()
                                                        UserSession.logout()

                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "⚠️ Solicitud cancelada, pero hubo un problema. Por favor no vuelvas a usar este correo.",
                                                            Toast.LENGTH_LONG
                                                        ).show()

                                                        pantallaActual = Pantalla.LOGIN
                                                    }
                                                )
                                            },
                                            onError = { error ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "❌ No se pudo cancelar la solicitud. Por favor intenta de nuevo.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    }
                                }
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
                                Pantalla.DETALLES_PLUVIOMETRO -> "PLUVIOMETROS"
                                Pantalla.EDITAR_PLUVIOMETRO -> "PLUVIOMETROS"
                                Pantalla.LISTA_DATOS_METEOROLOGICOS -> "DATOS_METEOROLOGICOS"
                                Pantalla.REGISTRO_DATO_METEOROLOGICO -> "REGISTRO_DATO_METEOROLOGICO"
                                Pantalla.DETALLES_DATO_METEOROLOGICO -> "DATOS_METEOROLOGICOS"
                                Pantalla.EDITAR_DATO_METEOROLOGICO -> "DATOS_METEOROLOGICOS"
                                else -> "HOME"
                            },
                            onNavigateToHome = { pantallaActual = Pantalla.HOME },
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
                                            UserSession.logout()
                                            pantallaActual = Pantalla.LOGIN
                                        }
                                    )
                                }

                                Pantalla.LISTA_VOLUNTARIOS -> {
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
                                            // TODO: Implementar edición
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_VOLUNTARIO -> {
                                    RegistroVoluntarioScreen(
                                        viewModel = voluntarioViewModel,
                                        emailPrecargado = emailRegistrado,
                                        firebaseUid = firebaseUidRegistrado,
                                        onVoluntarioGuardado = { tipoUsuario ->
                                            scope.launch {
                                                delay(1500)
                                                val voluntario = voluntarioViewModel.buscarPorFirebaseUid(firebaseUidRegistrado)

                                                if (voluntario != null) {
                                                    when (voluntario.estado_aprobacion) {
                                                        "Aprobado" -> {
                                                            UserSession.login(voluntario)
                                                            pantallaActual = Pantalla.HOME
                                                        }
                                                        "Pendiente" -> {
                                                            UserSession.login(voluntario)
                                                            pantallaActual = Pantalla.SOLICITUD_PENDIENTE
                                                        }
                                                        "Rechazado" -> {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "Tu solicitud fue rechazada. Contacta al administrador.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            authViewModel.cerrarSesion()
                                                            pantallaActual = Pantalla.LOGIN
                                                        }
                                                        else -> {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "Error en el estado de aprobación. Intenta nuevamente.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            authViewModel.cerrarSesion()
                                                            pantallaActual = Pantalla.LOGIN
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Error al cargar usuario. Intenta iniciar sesión manualmente.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    authViewModel.cerrarSesion()
                                                    pantallaActual = Pantalla.LOGIN
                                                }
                                            }
                                        },
                                        soloAdministrador = false
                                    )
                                }

                                Pantalla.LISTA_PLUVIOMETROS -> {
                                    ListaPluviometrosScreen(
                                        viewModel = pluviometroViewModel,
                                        onAgregarPluviometro = {
                                            if (UserSession.canCreatePluviometros()) {
                                                pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
                                            }
                                        },
                                        onVerDetalles = { pluviometro ->
                                            pluviometroSeleccionadoId = pluviometro.id
                                            pantallaActual = Pantalla.DETALLES_PLUVIOMETRO
                                        },
                                        onEditarPluviometro = { pluviometro ->
                                            pluviometroSeleccionadoId = pluviometro.id
                                            pantallaActual = Pantalla.EDITAR_PLUVIOMETRO
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_PLUVIOMETRO -> {
                                    RegistroPluviometroScreen(
                                        pluviometroViewModel = pluviometroViewModel,
                                        voluntarioViewModel = voluntarioViewModel,
                                        onPluviometroGuardado = {
                                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                        },
                                        onAccesoDenegado = {
                                            pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                        }
                                    )
                                }

                                Pantalla.DETALLES_PLUVIOMETRO -> {
                                    pluviometroSeleccionado?.let { pluviometro ->
                                        DetallesPluviometroScreen(
                                            pluviometro = pluviometro,
                                            pluviometroViewModel = pluviometroViewModel,
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                                pluviometroSeleccionadoId = null
                                            },
                                            onEditar = {
                                                pantallaActual = Pantalla.EDITAR_PLUVIOMETRO
                                            }
                                        )
                                    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }

                                Pantalla.EDITAR_PLUVIOMETRO -> {
                                    pluviometroSeleccionado?.let { pluviometro ->
                                        EditarPluviometroScreen(
                                            pluviometro = pluviometro,
                                            pluviometroViewModel = pluviometroViewModel,
                                            voluntarioViewModel = voluntarioViewModel,
                                            onPluviometroActualizado = {
                                                pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                                pluviometroSeleccionadoId = null
                                            },
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.DETALLES_PLUVIOMETRO
                                            },
                                            onAccesoDenegado = {
                                                pantallaActual = Pantalla.LISTA_PLUVIOMETROS
                                                pluviometroSeleccionadoId = null
                                            }
                                        )
                                    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }

                                Pantalla.LISTA_DATOS_METEOROLOGICOS -> {
                                    ListaDatosMeteorologicosScreen(
                                        viewModel = datoMeteorologicoViewModel,
                                        onAgregarDato = {
                                            if (UserSession.canCreateDatosMeteorologicos()) {
                                                pantallaActual = Pantalla.REGISTRO_DATO_METEOROLOGICO
                                            }
                                        },
                                        onVerDetalles = { dato ->
                                            datoMeteorologicoSeleccionadoId = dato.id
                                            pantallaActual = Pantalla.DETALLES_DATO_METEOROLOGICO
                                        },
                                        onEditarDato = { dato ->
                                            datoMeteorologicoSeleccionadoId = dato.id
                                            pantallaActual = Pantalla.EDITAR_DATO_METEOROLOGICO
                                        }
                                    )
                                }

                                Pantalla.REGISTRO_DATO_METEOROLOGICO -> {
                                    RegistroDatoMeteorologicoScreen(
                                        datoMeteorologicoViewModel = datoMeteorologicoViewModel,
                                        voluntarioViewModel = voluntarioViewModel,
                                        pluviometroViewModel = pluviometroViewModel,
                                        onDatoGuardado = {
                                            pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                        },
                                        onNavegarARegistroPluviometro = {
                                            if (UserSession.canCreatePluviometros()) {
                                                pantallaActual = Pantalla.REGISTRO_PLUVIOMETRO
                                            }
                                        }
                                    )
                                }

                                Pantalla.DETALLES_DATO_METEOROLOGICO -> {
                                    datoMeteorologicoSeleccionado?.let { dato ->
                                        DetallesDatoMeteorologicoScreen(
                                            dato = dato,
                                            datoMeteorologicoViewModel = datoMeteorologicoViewModel,
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                                datoMeteorologicoSeleccionadoId = null
                                            },
                                            onEditar = {
                                                pantallaActual = Pantalla.EDITAR_DATO_METEOROLOGICO
                                            }
                                        )
                                    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }

                                Pantalla.EDITAR_DATO_METEOROLOGICO -> {
                                    datoMeteorologicoSeleccionado?.let { dato ->
                                        EditarDatoMeteorologicoScreen(
                                            datoId = dato.id,
                                            datoMeteorologicoViewModel = datoMeteorologicoViewModel,
                                            onDatoActualizado = {
                                                pantallaActual = Pantalla.LISTA_DATOS_METEOROLOGICOS
                                                datoMeteorologicoSeleccionadoId = null
                                            },
                                            onNavigateBack = {
                                                pantallaActual = Pantalla.DETALLES_DATO_METEOROLOGICO
                                            }
                                        )
                                    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
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