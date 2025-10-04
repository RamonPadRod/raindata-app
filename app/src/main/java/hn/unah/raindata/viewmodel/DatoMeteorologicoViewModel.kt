package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.dao.DatoMeteorologicoDao
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatoMeteorologicoViewModel(application: Application) : AndroidViewModel(application) {

    private val datoMeteorologicoDao: DatoMeteorologicoDao
    private val _todosLosDatos = MutableLiveData<List<DatoMeteorologico>>()
    val todosLosDatos: LiveData<List<DatoMeteorologico>> = _todosLosDatos

    init {
        val database = AppDatabase(application)
        datoMeteorologicoDao = DatoMeteorologicoDao(database)
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch(Dispatchers.IO) {
            val datos = datoMeteorologicoDao.obtenerTodos()
            _todosLosDatos.postValue(datos)
        }
    }

    fun guardarDato(dato: DatoMeteorologico) {
        viewModelScope.launch(Dispatchers.IO) {
            datoMeteorologicoDao.insertar(dato)
            cargarDatos()
        }
    }

    fun actualizarDato(dato: DatoMeteorologico) {
        viewModelScope.launch(Dispatchers.IO) {
            datoMeteorologicoDao.actualizar(dato)
            cargarDatos()
        }
    }

    fun eliminarDato(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            datoMeteorologicoDao.eliminar(id)
            cargarDatos()
        }
    }

    fun obtenerPorPluviometro(pluviometroId: String): LiveData<List<DatoMeteorologico>> {
        val datos = MutableLiveData<List<DatoMeteorologico>>()
        viewModelScope.launch(Dispatchers.IO) {
            val resultado = datoMeteorologicoDao.obtenerPorPluviometro(pluviometroId)
            datos.postValue(resultado)
        }
        return datos
    }

    fun obtenerPorVoluntario(voluntarioId: String): LiveData<List<DatoMeteorologico>> {
        val datos = MutableLiveData<List<DatoMeteorologico>>()
        viewModelScope.launch(Dispatchers.IO) {
            val resultado = datoMeteorologicoDao.obtenerPorVoluntario(voluntarioId)
            datos.postValue(resultado)
        }
        return datos
    }
}