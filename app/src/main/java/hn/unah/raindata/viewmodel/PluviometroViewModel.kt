package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.repository.PluviometroRepository
import kotlinx.coroutines.launch

class PluviometroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PluviometroRepository
    private val _pluviometros = MutableLiveData<List<Pluviometro>>()
    val todosLosPluviometros: LiveData<List<Pluviometro>> = _pluviometros

    private val _pluviometroSeleccionado = MutableLiveData<Pluviometro?>()
    val pluviometroSeleccionado: LiveData<Pluviometro?> = _pluviometroSeleccionado

    private val _codigoGenerado = MutableLiveData<String>()
    val codigoGenerado: LiveData<String> = _codigoGenerado

    init {
        val database = AppDatabase.getDatabase(application)
        val pluviometroDao = database.getPluviometroDao()
        repository = PluviometroRepository(pluviometroDao)
        cargarPluviometros()
    }

    /**
     * Genera código automático basado en departamento y municipio
     * Se llama automáticamente cuando se selecciona departamento Y municipio
     *
     * @param departamento Nombre del departamento seleccionado
     * @param municipio Nombre del municipio seleccionado
     */
    fun generarCodigoAutomatico(departamento: String, municipio: String) = viewModelScope.launch {
        // Solo generar si ambos están seleccionados
        if (departamento.isNotBlank() && municipio.isNotBlank()) {
            val codigo = repository.generarCodigoAutomatico(departamento, municipio)
            _codigoGenerado.value = codigo
        } else {
            _codigoGenerado.value = ""
        }
    }

    /**
     * Limpia el código generado (para cuando se cambia departamento/municipio)
     */
    fun limpiarCodigo() {
        _codigoGenerado.value = ""
    }

    fun guardarPluviometro(pluviometro: Pluviometro) = viewModelScope.launch {
        repository.guardarPluviometro(pluviometro)
        cargarPluviometros()
    }

    fun actualizarPluviometro(pluviometro: Pluviometro) = viewModelScope.launch {
        repository.actualizarPluviometro(pluviometro)
        cargarPluviometros()
    }

    fun buscarPluviometros(termino: String) = viewModelScope.launch {
        if (termino.isBlank()) {
            cargarPluviometros()
        } else {
            val resultados = repository.buscarPluviometros(termino)
            _pluviometros.value = resultados
        }
    }

    fun obtenerPluviometroPorId(id: String) = viewModelScope.launch {
        val pluviometro = repository.obtenerPluviometroPorId(id)
        _pluviometroSeleccionado.value = pluviometro
    }

    fun obtenerPluviometrosPorResponsable(responsableId: String) = viewModelScope.launch {
        val pluviometros = repository.obtenerPluviometrosPorResponsable(responsableId)
        _pluviometros.value = pluviometros
    }

    fun eliminarPluviometro(id: String) = viewModelScope.launch {
        repository.eliminarPluviometro(id)
        cargarPluviometros()
    }

    private fun cargarPluviometros() = viewModelScope.launch {
        val pluviometros = repository.obtenerPluviometros()
        _pluviometros.value = pluviometros
    }
}