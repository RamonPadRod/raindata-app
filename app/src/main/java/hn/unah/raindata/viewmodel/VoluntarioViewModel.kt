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
import java.text.SimpleDateFormat
import java.util.*

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

    suspend fun existePasaporte(pasaporte: String): Boolean {
        return withContext(Dispatchers.IO) {
            voluntarioDao.existePasaporte(pasaporte)
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

    // ✅ CORREGIDO: DNI ahora es opcional (solo si tipo_documento = "DNI")
    fun validarDNI(dni: String): String? {
        if (dni.isBlank()) return null // ← CAMBIO: Ya no es obligatorio siempre

        // Si el usuario empieza a escribir, validar
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
            1 to 8, 2 to 10, 3 to 21, 4 to 23, 5 to 12, 6 to 16,
            7 to 19, 8 to 28, 9 to 6, 10 to 17, 11 to 4, 12 to 19,
            13 to 28, 14 to 16, 15 to 23, 16 to 28, 17 to 9, 18 to 11
        )

        val limiteMunicipios = municipiosPorDepartamento[departamento] ?: return "Departamento inválido"

        if (municipio !in 1..limiteMunicipios) {
            return "Municipio inválido para este departamento (01-${String.format("%02d", limiteMunicipios)})"
        }

        // Validar año (1900 - año actual)
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        if (anio !in 1900..anioActual) {
            return "Año inválido (1900-$anioActual)"
        }

        return null
    }

    // ✅ CORREGIDO: Pasaporte ahora es opcional (solo si tipo_documento = "Pasaporte")
    fun validarPasaporte(pasaporte: String): String? {
        if (pasaporte.isBlank()) return null // ← CAMBIO: Ya no es obligatorio siempre

        // Si el usuario empieza a escribir, validar
        if (pasaporte.length < 6) {
            return "El pasaporte debe tener al menos 6 caracteres"
        }
        if (pasaporte.length > 20) {
            return "El pasaporte no puede exceder 20 caracteres"
        }

        // Solo letras y números (sin espacios ni caracteres especiales)
        val regex = Regex("^[A-Z0-9]+$")
        if (!regex.matches(pasaporte.uppercase())) {
            return "El pasaporte solo puede contener letras y números (sin espacios)"
        }

        // Validar que tenga al menos una letra y un número
        if (!pasaporte.any { it.isLetter() }) {
            return "El pasaporte debe contener al menos una letra"
        }
        if (!pasaporte.any { it.isDigit() }) {
            return "El pasaporte debe contener al menos un número"
        }

        return null
    }

    fun formatearPasaporte(input: String): String {
        // Convertir a mayúsculas y remover espacios
        return input.uppercase().replace(" ", "").filter { it.isLetterOrDigit() }.take(20)
    }

    // ✅ CORREGIDO: Fecha de Nacimiento ahora es OPCIONAL
    fun validarFechaNacimiento(fecha: String): String? {
        if (fecha.isBlank()) return null // ← CAMBIO: Ahora es opcional

        // Validar formato YYYY-MM-DD
        val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        if (!regex.matches(fecha)) {
            return "Formato inválido (debe ser AAAA-MM-DD)"
        }

        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formato.isLenient = false
            val fechaNacimiento = formato.parse(fecha) ?: return "Fecha inválida"

            val calendar = Calendar.getInstance()
            calendar.time = fechaNacimiento

            // Validar que no sea fecha futura
            val hoy = Calendar.getInstance()
            if (calendar.after(hoy)) {
                return "La fecha de nacimiento no puede ser futura"
            }

            // Validar edad mínima (debe tener al menos 18 años)
            val edadMinima = Calendar.getInstance()
            edadMinima.add(Calendar.YEAR, -18)
            if (calendar.after(edadMinima)) {
                return "Debe ser mayor de 18 años"
            }

            // Validar edad máxima (no más de 100 años)
            val edadMaxima = Calendar.getInstance()
            edadMaxima.add(Calendar.YEAR, -100)
            if (calendar.before(edadMaxima)) {
                return "Fecha de nacimiento muy antigua (máximo 100 años)"
            }

            null
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }

    fun calcularEdad(fecha: String): Int? {
        if (fecha.isBlank()) return null // ← AÑADIDO: Manejar cadena vacía

        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaNacimiento = formato.parse(fecha) ?: return null

            val nacimiento = Calendar.getInstance()
            nacimiento.time = fechaNacimiento

            val hoy = Calendar.getInstance()

            var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)

            if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--
            }

            edad
        } catch (e: Exception) {
            null
        }
    }

    // ✅ CORREGIDO: Género ahora es OPCIONAL
    fun validarGenero(genero: String): String? {
        if (genero.isBlank()) return null // ← CAMBIO: Ahora es opcional

        val generosValidos = listOf("Masculino", "Femenino", "Otro")
        if (genero !in generosValidos) {
            return "Género no válido"
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