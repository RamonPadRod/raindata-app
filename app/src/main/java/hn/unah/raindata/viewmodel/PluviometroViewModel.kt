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

    init {
        val database = AppDatabase.getDatabase(application)
        val pluviometroDao = database.getPluviometroDao()
        repository = PluviometroRepository(pluviometroDao)
        cargarPluviometros()
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

    private fun cargarPluviometros() = viewModelScope.launch {
        val pluviometros = repository.obtenerPluviometros()
        _pluviometros.value = pluviometros
    }
}