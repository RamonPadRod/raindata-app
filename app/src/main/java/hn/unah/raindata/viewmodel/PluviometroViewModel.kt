package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.utils.DepartamentosHonduras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ✅ PLUVIOMETRO VIEWMODEL - MIGRADO A FIREBASE FIRESTORE
 */
class PluviometroViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val pluviometrosCollection = firestore.collection("pluviometros")

    // ===== ESTADOS CON STATEFLOW =====
    private val _pluviometros = MutableStateFlow<List<Pluviometro>>(emptyList())
    val pluviometros: StateFlow<List<Pluviometro>> = _pluviometros.asStateFlow()

    private val _pluviometroSeleccionado = MutableStateFlow<Pluviometro?>(null)
    val pluviometroSeleccionado: StateFlow<Pluviometro?> = _pluviometroSeleccionado.asStateFlow()

    private val _codigoGenerado = MutableStateFlow<String>("")
    val codigoGenerado: StateFlow<String> = _codigoGenerado.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var pluviometrosListener: ListenerRegistration? = null

    init {
        cargarPluviometros()
    }

    // ===== ESTADO DEL FORMULARIO (PERSISTENCIA AL NAVEGAR) =====
    private val _direccionDraft = MutableStateFlow("")
    val direccionDraft = _direccionDraft.asStateFlow()
    fun setDireccionDraft(value: String) { _direccionDraft.value = value }

    private val _departamentoDraft = MutableStateFlow("")
    val departamentoDraft = _departamentoDraft.asStateFlow()
    fun setDepartamentoDraft(value: String) { _departamentoDraft.value = value }

    private val _municipioDraft = MutableStateFlow("")
    val municipioDraft = _municipioDraft.asStateFlow()
    fun setMunicipioDraft(value: String) { _municipioDraft.value = value }

    private val _aldeaDraft = MutableStateFlow("")
    val aldeaDraft = _aldeaDraft.asStateFlow()
    fun setAldeaDraft(value: String) { _aldeaDraft.value = value }

    private val _caserioDraft = MutableStateFlow("")
    val caserioDraft = _caserioDraft.asStateFlow()
    fun setCaserioDraft(value: String) { _caserioDraft.value = value }

    private val _voluntarioDraft = MutableStateFlow<hn.unah.raindata.data.database.entities.Voluntario?>(null)
    val voluntarioDraft = _voluntarioDraft.asStateFlow()
    fun setVoluntarioDraft(value: hn.unah.raindata.data.database.entities.Voluntario?) { _voluntarioDraft.value = value }

    private val _observacionesDraft = MutableStateFlow("")
    val observacionesDraft = _observacionesDraft.asStateFlow()
    fun setObservacionesDraft(value: String) { _observacionesDraft.value = value }

    private val _ubicacionDraft = MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)
    val ubicacionDraft = _ubicacionDraft.asStateFlow()
    fun setUbicacionDraft(value: com.google.android.gms.maps.model.LatLng?) { _ubicacionDraft.value = value }

    fun limpiarDraft() {
        _direccionDraft.value = ""
        _departamentoDraft.value = ""
        _municipioDraft.value = ""
        _aldeaDraft.value = ""
        _caserioDraft.value = ""
        _voluntarioDraft.value = null
        _observacionesDraft.value = ""
        _ubicacionDraft.value = null
        _codigoGenerado.value = ""
    }

    // ===== LISTENER EN TIEMPO REAL =====
    fun cargarPluviometros() {
        pluviometrosListener?.remove()

        _isLoading.value = true

        pluviometrosListener = pluviometrosCollection
            .whereEqualTo("activo", true)
            .orderBy("numero_registro", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    _errorMessage.value = "Error al cargar pluviómetros: ${error.message}"
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Pluviometro::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                _pluviometros.value = lista
            }
    }

    // ===== GENERAR CÓDIGO AUTOMÁTICO =====
    /**
     * Genera código automático basado en departamento y municipio
     * Formato: XX-YY-ZZZ
     */
    fun generarCodigoAutomatico(departamento: String, municipio: String) {
        viewModelScope.launch {
            if (departamento.isBlank() || municipio.isBlank()) {
                _codigoGenerado.value = ""
                return@launch
            }

            try {
                // Obtener códigos de departamento y municipio
                val codigoDepto = DepartamentosHonduras.obtenerCodigoDepartamento(departamento) ?: "00"
                val codigoMuni = DepartamentosHonduras.obtenerCodigoMunicipio(departamento, municipio) ?: "00"

                // Buscar el último número secuencial para esta combinación
                val prefijo = "$codigoDepto-$codigoMuni"

                val snapshot = pluviometrosCollection
                    .orderBy("numero_registro", Query.Direction.DESCENDING)
                    .get()
                    .await()

                var nuevoSecuencial = 1

                // Buscar el último código con este prefijo
                snapshot.documents.forEach { doc ->
                    val codigo = doc.getString("numero_registro") ?: ""
                    if (codigo.startsWith(prefijo)) {
                        val partes = codigo.split("-")
                        if (partes.size == 3) {
                            try {
                                val ultimoSecuencial = partes[2].toInt()
                                if (ultimoSecuencial >= nuevoSecuencial) {
                                    nuevoSecuencial = ultimoSecuencial + 1
                                }
                            } catch (e: NumberFormatException) {
                                // Ignorar si no es un número válido
                            }
                        }
                    }
                }

                // Generar código completo
                val codigo = DepartamentosHonduras.generarCodigoPluviometro(
                    departamento,
                    municipio,
                    nuevoSecuencial
                )

                _codigoGenerado.value = codigo

            } catch (e: Exception) {
                _errorMessage.value = "Error al generar código: ${e.message}"
            }
        }
    }

    fun limpiarCodigo() {
        _codigoGenerado.value = ""
    }

    // ===== GUARDAR PLUVIÓMETRO =====
    fun guardarPluviometro(pluviometro: Pluviometro, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                pluviometrosCollection
                    .document(pluviometro.id)
                    .set(pluviometro)
                    .await()

                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                onError("Error al guardar: ${e.message}")
            }
        }
    }

    // ===== ACTUALIZAR PLUVIÓMETRO =====
    fun actualizarPluviometro(pluviometro: Pluviometro, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                pluviometrosCollection
                    .document(pluviometro.id)
                    .set(pluviometro)
                    .await()

                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                onError("Error al actualizar: ${e.message}")
            }
        }
    }

    // ===== ELIMINAR PLUVIÓMETRO =====
    fun eliminarPluviometro(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                pluviometrosCollection
                    .document(id)
                    .delete()
                    .await()

                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                onError("Error al eliminar: ${e.message}")
            }
        }
    }

    // ===== BUSCAR PLUVIÓMETROS =====
    fun buscarPluviometros(termino: String) {
        if (termino.isBlank()) {
            cargarPluviometros()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val snapshot = pluviometrosCollection
                    .whereEqualTo("activo", true)
                    .get()
                    .await()

                val resultados = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Pluviometro::class.java)
                }.filter { pluviometro ->
                    pluviometro.numero_registro.contains(termino, ignoreCase = true) ||
                            pluviometro.municipio.contains(termino, ignoreCase = true) ||
                            pluviometro.responsable_nombre.contains(termino, ignoreCase = true)
                }

                _pluviometros.value = resultados
                _isLoading.value = false

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error en búsqueda: ${e.message}"
            }
        }
    }

    // ===== OBTENER PLUVIÓMETRO POR ID =====
    fun obtenerPluviometroPorId(id: String) {
        viewModelScope.launch {
            try {
                val snapshot = pluviometrosCollection
                    .document(id)
                    .get()
                    .await()

                _pluviometroSeleccionado.value = snapshot.toObject(Pluviometro::class.java)

            } catch (e: Exception) {
                _errorMessage.value = "Error al obtener pluviómetro: ${e.message}"
            }
        }
    }

    // ===== OBTENER PLUVIÓMETROS POR RESPONSABLE =====
    fun obtenerPluviometrosPorResponsable(responsableUid: String) {
        pluviometrosListener?.remove()

        _isLoading.value = true

        pluviometrosListener = pluviometrosCollection
            .whereEqualTo("responsable_uid", responsableUid)
            .whereEqualTo("activo", true)
            .orderBy("numero_registro", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    _errorMessage.value = "Error al cargar pluviómetros: ${error.message}"
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Pluviometro::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                _pluviometros.value = lista
            }
    }

    // ===== LIMPIAR ERRORES =====
    fun clearError() {
        _errorMessage.value = null
    }

    // ===== DETENER LISTENERS =====
    override fun onCleared() {
        super.onCleared()
        pluviometrosListener?.remove()
    }
}