package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.repository.VoluntarioRepository
import kotlinx.coroutines.launch

class VoluntarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VoluntarioRepository
    private val _voluntarios = MutableLiveData<List<Voluntario>>()
    val todosLosVoluntarios: LiveData<List<Voluntario>> = _voluntarios

    init {
        val database = AppDatabase.getDatabase(application)
        val voluntarioDao = database.getVoluntarioDao()
        repository = VoluntarioRepository(voluntarioDao)
        cargarVoluntarios()
    }

    fun guardarVoluntario(voluntario: Voluntario) = viewModelScope.launch {
        repository.guardarVoluntario(voluntario)
        cargarVoluntarios() // Recargar la lista
    }

    fun actualizarVoluntario(voluntario: Voluntario) = viewModelScope.launch {
        repository.actualizarVoluntario(voluntario)
        cargarVoluntarios()
    }

    fun buscarVoluntarios(termino: String) = viewModelScope.launch {
        if (termino.isBlank()) {
            cargarVoluntarios()
        } else {
            val resultados = repository.buscarVoluntarios(termino)
            _voluntarios.value = resultados
        }
    }

    private fun cargarVoluntarios() = viewModelScope.launch {
        val voluntarios = repository.obtenerVoluntarios()
        _voluntarios.value = voluntarios
    }
    fun eliminarVoluntario(id: String) = viewModelScope.launch {
        repository.eliminarVoluntario(id)
        cargarVoluntarios()
    }
}