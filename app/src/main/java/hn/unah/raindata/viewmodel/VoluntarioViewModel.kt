package hn.unah.raindata.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.dao.VoluntarioDao
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.repository.VoluntarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoluntarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VoluntarioRepository
    private val voluntarioDao: VoluntarioDao
    private val _voluntarios = MutableLiveData<List<Voluntario>>()
    val todosLosVoluntarios: LiveData<List<Voluntario>> = _voluntarios

    init {
        val database = AppDatabase.getDatabase(application)
        voluntarioDao = database.getVoluntarioDao()
        repository = VoluntarioRepository(voluntarioDao)
        cargarVoluntarios()
    }

    fun guardarVoluntario(voluntario: Voluntario) = viewModelScope.launch {
        repository.guardarVoluntario(voluntario)
        cargarVoluntarios()
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

    fun eliminarVoluntario(id: Long) {
        viewModelScope.launch {
            repository.eliminarVoluntario(id)
        }
    }

    suspend fun existeDNI(dni: String): Boolean {
        return withContext(Dispatchers.IO) {
            voluntarioDao.existeDNI(dni)
        }
    }

    fun buscarPorCedula(cedula: String): Voluntario? {
        return voluntarioDao.obtenerPorDNI(cedula)
    }

    // ========== FUNCIONES DE VALIDACIÓN ==========

    fun validarNombre(nombre: String): String? {
        if (nombre.isBlank()) return "El nombre es obligatorio"
        if (nombre.length > 40) return "El nombre no puede exceder 40 caracteres"

        // Solo letras, espacios y acentos
        val regex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
        if (!regex.matches(nombre)) {
            return "El nombre solo puede contener letras y espacios"
        }

        return null
    }

    fun validarDNI(dni: String): String? {
        if (dni.isBlank()) return "El DNI es obligatorio"

        // Remover guiones para validar
        val dniLimpio = dni.replace("-", "")

        if (dniLimpio.length != 13) return "DNI incompleto (formato: XXXX-XXXX-XXXXX)"

        if (!dniLimpio.all { it.isDigit() }) {
            return "El DNI solo puede contener números"
        }

        // Validar formato con guiones
        if (!dni.matches(Regex("^\\d{4}-\\d{4}-\\d{5}$"))) {
            return "Formato inválido (ejemplo: 0708-2005-00276)"
        }

        // Extraer partes del DNI
        val departamento = dni.substring(0, 2).toIntOrNull() ?: return "Departamento inválido"
        val municipio = dni.substring(2, 4).toIntOrNull() ?: return "Municipio inválido"
        val anio = dni.substring(5, 9).toIntOrNull() ?: return "Año inválido"

        // Validar departamento (01-18)
        if (departamento !in 1..18) {
            return "Departamento inválido (01-18)"
        }

        // Mapa de municipios por departamento
        val municipiosPorDepartamento = mapOf(
            1 to 8,   // Atlántida
            2 to 10,  // Colón
            3 to 21,  // Comayagua
            4 to 23,  // Copán
            5 to 12,  // Cortés
            6 to 16,  // Choluteca
            7 to 19,  // El Paraíso
            8 to 28,  // Francisco Morazán
            9 to 6,   // Gracias a Dios
            10 to 17, // Intibucá
            11 to 4,  // Islas de la Bahía
            12 to 19, // La Paz
            13 to 28, // Lempira
            14 to 16, // Ocotepeque
            15 to 23, // Olancho
            16 to 28, // Santa Bárbara
            17 to 9,  // Valle
            18 to 11  // Yoro
        )

        val limiteMunicipios = municipiosPorDepartamento[departamento] ?: return "Departamento inválido"

        if (municipio !in 1..limiteMunicipios) {
            return "Municipio inválido para este departamento (01-${String.format("%02d", limiteMunicipios)})"
        }

        // Validar año (1900 - año actual)
        val anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        if (anio !in 1900..anioActual) {
            return "Año inválido (1900-$anioActual)"
        }

        return null
    }

    fun formatearDNI(input: String): String {
        // Remover todo excepto dígitos
        val digitos = input.filter { it.isDigit() }

        // Limitar a 13 dígitos
        val limitado = digitos.take(13)

        // Agregar guiones automáticamente
        return when (limitado.length) {
            in 0..4 -> limitado
            in 5..8 -> "${limitado.substring(0, 4)}-${limitado.substring(4)}"
            else -> "${limitado.substring(0, 4)}-${limitado.substring(4, 8)}-${limitado.substring(8)}"
        }
    }

    fun limpiarDNI(dni: String): String {
        // Remover guiones para guardar en BD
        return dni.replace("-", "")
    }

    fun validarTelefono(telefono: String): String? {
        if (telefono.isBlank()) return null // Opcional

        // Remover guion
        val telefonoLimpio = telefono.replace("-", "")

        if (telefonoLimpio.length != 8) {
            return "El teléfono debe tener 8 dígitos"
        }

        if (!telefonoLimpio.all { it.isDigit() }) {
            return "El teléfono solo puede contener números"
        }

        // Validar que empiece con 2, 3, 8 o 9
        val primerDigito = telefonoLimpio.first()
        if (primerDigito !in listOf('2', '3', '8', '9')) {
            return "El teléfono debe empezar con 2, 3, 8 o 9"
        }

        return null
    }

    fun formatearTelefono(input: String): String {
        // Remover todo excepto dígitos
        val digitos = input.filter { it.isDigit() }

        // Limitar a 8 dígitos
        val limitado = digitos.take(8)

        // Agregar guion después del 4º dígito
        return when (limitado.length) {
            in 0..4 -> limitado
            else -> "${limitado.substring(0, 4)}-${limitado.substring(4)}"
        }
    }

    fun limpiarTelefono(telefono: String): String {
        // Remover guion para guardar en BD
        return telefono.replace("-", "")
    }

    fun validarCorreo(correo: String): String? {
        if (correo.isBlank()) return null // Opcional

        if (correo.length > 50) {
            return "El correo no puede exceder 50 caracteres"
        }

        // Validación de formato email
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(correo)) {
            return "Formato de correo inválido"
        }

        return null
    }

    fun validarDireccion(direccion: String): String? {
        if (direccion.isBlank()) return "La dirección es obligatoria"
        if (direccion.length > 100) return "La dirección no puede exceder 100 caracteres"
        return null
    }

    fun validarDepartamento(departamento: String): String? {
        if (departamento.isBlank()) return "El departamento es obligatorio"
        if (departamento.length > 30) return "El departamento no puede exceder 30 caracteres"
        return null
    }

    fun validarMunicipio(municipio: String): String? {
        if (municipio.isBlank()) return "El municipio es obligatorio"
        if (municipio.length > 30) return "El municipio no puede exceder 30 caracteres"
        return null
    }

    fun validarAldea(aldea: String): String? {
        if (aldea.isBlank()) return "La aldea es obligatoria"
        if (aldea.length > 15) return "La aldea no puede exceder 15 caracteres"
        return null
    }

    fun validarCaserioBarrioColonia(valor: String): String? {
        if (valor.isBlank()) return null // Opcional
        if (valor.length > 15) return "No puede exceder 15 caracteres"
        return null
    }

    fun validarObservaciones(observaciones: String): String? {
        if (observaciones.isBlank()) return null // Opcional

        if (observaciones.length < 10) {
            return "Las observaciones deben tener al menos 10 caracteres"
        }

        return null
    }

    fun validarTipoUsuario(tipoUsuario: String, soloAdministrador: Boolean): String? {
        if (soloAdministrador) return null // No validar si es modo administrador

        if (tipoUsuario.isBlank()) {
            return "Debe seleccionar un tipo de usuario"
        }

        return null
    }
}