package hn.unah.raindata.data.repository

import hn.unah.raindata.data.database.dao.VoluntarioDao
import hn.unah.raindata.data.database.entities.Voluntario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VoluntarioRepository(private val voluntarioDao: VoluntarioDao) {

    suspend fun guardarVoluntario(voluntario: Voluntario) = withContext(Dispatchers.IO) {
        voluntarioDao.insertar(voluntario)
    }

    suspend fun actualizarVoluntario(voluntario: Voluntario) = withContext(Dispatchers.IO) {
        voluntarioDao.actualizar(voluntario)
    }

    suspend fun eliminarVoluntario(id: String) = withContext(Dispatchers.IO) {
        voluntarioDao.eliminar(id)
    }

    suspend fun obtenerVoluntarios(): List<Voluntario> = withContext(Dispatchers.IO) {
        voluntarioDao.obtenerActivos()
    }

    suspend fun obtenerTodos(): List<Voluntario> = withContext(Dispatchers.IO) {
        voluntarioDao.obtenerTodos()
    }

    suspend fun buscarVoluntarios(termino: String): List<Voluntario> = withContext(Dispatchers.IO) {
        voluntarioDao.buscar(termino)
    }

    suspend fun obtenerPorId(id: String): Voluntario? = withContext(Dispatchers.IO) {
        voluntarioDao.obtenerPorId(id)
    }

    // ✅ NUEVOS MÉTODOS
    suspend fun existeDNI(dni: String): Boolean = withContext(Dispatchers.IO) {
        voluntarioDao.existeDNI(dni)
    }

    suspend fun obtenerPorDNI(dni: String): Voluntario? = withContext(Dispatchers.IO) {
        voluntarioDao.obtenerPorDNI(dni)
    }
}