package hn.unah.raindata.data.repository

import hn.unah.raindata.data.database.dao.VoluntarioDao
import hn.unah.raindata.data.database.entities.Voluntario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VoluntarioRepository(private val voluntarioDao: VoluntarioDao) {

    suspend fun guardarVoluntario(voluntario: Voluntario) = withContext(Dispatchers.IO) {
        voluntarioDao.insertar(voluntario)
    }

    suspend fun obtenerVoluntarios(): List<Voluntario> = withContext(Dispatchers.IO) {
        voluntarioDao.obtenerActivos()
    }

    suspend fun obtenerVoluntarioPorId(id: String): Voluntario? = withContext(Dispatchers.IO) {
        voluntarioDao.obtenerPorId(id)
    }

    suspend fun actualizarVoluntario(voluntario: Voluntario) = withContext(Dispatchers.IO) {
        val voluntarioActualizado = voluntario.copy(
            fecha_modificacion = System.currentTimeMillis()
        )
        voluntarioDao.actualizar(voluntarioActualizado)
    }

    suspend fun buscarVoluntarios(termino: String): List<Voluntario> = withContext(Dispatchers.IO) {
        voluntarioDao.buscar(termino)
    }

    suspend fun contarVoluntarios(): Int = withContext(Dispatchers.IO) {
        voluntarioDao.contarActivos()
    }

    suspend fun eliminarVoluntario(id: String) = withContext(Dispatchers.IO) {
        voluntarioDao.eliminar(id)
    }
}