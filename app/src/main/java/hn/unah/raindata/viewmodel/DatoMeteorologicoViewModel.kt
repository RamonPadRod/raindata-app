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
import java.text.SimpleDateFormat
import java.util.*

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

    fun obtenerDatoPorId(id: String): LiveData<DatoMeteorologico?> {
        val dato = MutableLiveData<DatoMeteorologico?>()
        viewModelScope.launch(Dispatchers.IO) {
            val resultado = datoMeteorologicoDao.obtenerPorId(id)
            dato.postValue(resultado)
        }
        return dato
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

    // Validación de fecha de lectura (±7 días)
    fun validarFechaLectura(fecha: String): String? {
        if (fecha.isBlank()) return "La fecha de lectura es obligatoria"

        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formato.isLenient = false
            val fechaIngresada = formato.parse(fecha) ?: return "Formato de fecha inválido (yyyy-MM-dd)"

            val calendario = Calendar.getInstance()
            val hoy = calendario.time

            // Calcular 7 días antes
            calendario.add(Calendar.DAY_OF_YEAR, -7)
            val hace7Dias = calendario.time

            // Calcular 7 días después
            calendario.time = hoy
            calendario.add(Calendar.DAY_OF_YEAR, 7)
            val en7Dias = calendario.time

            when {
                fechaIngresada.before(hace7Dias) -> "La fecha de lectura no puede ser anterior a 7 días"
                fechaIngresada.after(en7Dias) -> "La fecha de lectura no puede ser posterior a 7 días"
                else -> null
            }
        } catch (e: Exception) {
            "Formato de fecha inválido (yyyy-MM-dd)"
        }
    }

    fun validarHora(hora: String): String? {
        if (hora.isBlank()) return "La hora es obligatoria"

        return try {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            formato.isLenient = false
            formato.parse(hora)
            null
        } catch (e: Exception) {
            "Formato de hora inválido (HH:mm)"
        }
    }

    fun validarPrecipitacion(valor: String): String? {
        if (valor.isBlank()) return "La precipitación es obligatoria"

        val precipitacion = valor.toDoubleOrNull()
            ?: return "Debe ingresar un número válido"

        return when {
            precipitacion < 0 -> "La precipitación no puede ser negativa"
            precipitacion > 500 -> "Precipitación fuera de rango (máx. 500mm)"
            else -> null
        }
    }

    fun validarTemperaturaMaxima(valor: String): String? {
        if (valor.isBlank()) return null // Las temperaturas son opcionales

        val temp = valor.toDoubleOrNull()
            ?: return "Debe ingresar un número válido"

        return when {
            temp < 10 -> "Temp. máxima muy baja (mín. 10°C)"
            temp > 50 -> "Temp. máxima fuera de rango (máx. 50°C)"
            else -> null
        }
    }

    fun validarTemperaturaMinima(valor: String): String? {
        if (valor.isBlank()) return null // Las temperaturas son opcionales

        val temp = valor.toDoubleOrNull()
            ?: return "Debe ingresar un número válido"

        return when {
            temp < -5 -> "Temp. mínima muy baja (mín. -5°C)"
            temp > 40 -> "Temp. mínima muy alta (máx. 40°C)"
            else -> null
        }
    }

    fun validarCoherenciaTemperaturas(tempMin: String, tempMax: String): String? {
        if (tempMin.isBlank() || tempMax.isBlank()) return null

        val min = tempMin.toDoubleOrNull()
        val max = tempMax.toDoubleOrNull()

        if (min != null && max != null && min >= max) {
            return "Temp. mínima debe ser menor que temp. máxima"
        }

        return null
    }

    fun validarObservaciones(valor: String): String? {
        return if (valor.length > 500) {
            "Las observaciones no pueden exceder 500 caracteres"
        } else null
    }

    fun validarCondicionesDia(condiciones: List<String>): String? {
        return when {
            condiciones.isEmpty() -> "Debe seleccionar al menos una condición"
            condiciones.size > 3 -> "Puede seleccionar máximo 3 condiciones"
            else -> null
        }
    }
}