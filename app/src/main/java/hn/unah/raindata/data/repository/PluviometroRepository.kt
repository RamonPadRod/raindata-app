//package hn.unah.raindata.data.repository
//
//import hn.unah.raindata.data.database.dao.PluviometroDao
//import hn.unah.raindata.data.database.entities.Pluviometro
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class PluviometroRepository(private val pluviometroDao: PluviometroDao) {
//
//    /**
//     * Genera código automático basado en departamento y municipio seleccionados
//     * @param departamento Nombre del departamento seleccionado
//     * @param municipio Nombre del municipio seleccionado
//     * @return Código en formato XX-YY-ZZZ (ej: 07-03-001)
//     */
//    suspend fun generarCodigoAutomatico(departamento: String, municipio: String): String =
//        withContext(Dispatchers.IO) {
//            pluviometroDao.generarCodigoAutomatico(departamento, municipio)
//        }
//
//    suspend fun guardarPluviometro(pluviometro: Pluviometro) = withContext(Dispatchers.IO) {
//        pluviometroDao.insertar(pluviometro)
//    }
//
//    suspend fun obtenerPluviometros(): List<Pluviometro> = withContext(Dispatchers.IO) {
//        pluviometroDao.obtenerActivos()
//    }
//
//    suspend fun obtenerPluviometroPorId(id: String): Pluviometro? = withContext(Dispatchers.IO) {
//        pluviometroDao.obtenerPorId(id)
//    }
//
//    suspend fun obtenerPluviometrosPorResponsable(responsableId: String): List<Pluviometro> =
//        withContext(Dispatchers.IO) {
//            pluviometroDao.obtenerPorResponsable(responsableId)
//        }
//
//    suspend fun actualizarPluviometro(pluviometro: Pluviometro) = withContext(Dispatchers.IO) {
//        val pluviometroActualizado = pluviometro.copy(
//            fecha_modificacion = System.currentTimeMillis()
//        )
//        pluviometroDao.actualizar(pluviometroActualizado)
//    }
//
//    suspend fun buscarPluviometros(termino: String): List<Pluviometro> = withContext(Dispatchers.IO) {
//        pluviometroDao.buscar(termino)
//    }
//
//    suspend fun contarPluviometros(): Int = withContext(Dispatchers.IO) {
//        pluviometroDao.contarActivos()
//    }
//
//    suspend fun eliminarPluviometro(id: String) = withContext(Dispatchers.IO) {
//        pluviometroDao.eliminar(id)
//    }
//}