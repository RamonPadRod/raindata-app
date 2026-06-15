package hn.unah.raindata.data.repository

import hn.unah.raindata.data.database.dao.DatoMeteorologicoDao
import hn.unah.raindata.data.database.dao.PluviometroDao
import hn.unah.raindata.data.database.dao.VoluntarioDao
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EstadisticasRepository(
    private val datoDao: DatoMeteorologicoDao,
    private val pluvioDao: PluviometroDao,
    private val voluntarioDao: VoluntarioDao
) {

    private val isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** Calcula el par (desde, hasta) según el período elegido */
    private fun calcularRango(filtro: FiltroEstadisticas): Pair<String, String> {
        val hoy = LocalDate.now()
        return when (filtro.periodo) {
            PeriodoFiltro.HOY -> {
                val s = hoy.format(isoFmt)
                Pair(s, s)
            }
            PeriodoFiltro.ULTIMO_MES -> {
                val desde = hoy.minusMonths(1).format(isoFmt)
                Pair(desde, hoy.format(isoFmt))
            }
            PeriodoFiltro.ULTIMO_ANIO -> {
                val desde = hoy.minusYears(1).format(isoFmt)
                Pair(desde, hoy.format(isoFmt))
            }
            PeriodoFiltro.TODO -> Pair("", "")
        }
    }

    suspend fun calcularEstadisticas(filtro: FiltroEstadisticas): EstadisticasCompletas =
        withContext(Dispatchers.IO) {
            val (desde, hasta) = calcularRango(filtro)
            val pid = filtro.pluviometroId
            val vid = filtro.voluntarioUid

            val resumen = ResumenGeneral(
                totalRegistros = datoDao.contarRegistros(desde, hasta, pid, vid),
                precipitacionTotal = datoDao.sumarPrecipitacion(desde, hasta, pid, vid),
                promedioPrecipitacionDia = datoDao.promedioPrecipitacion(desde, hasta, pid, vid),
                diaMayorPrecipitacion = datoDao.diaMayorPrecipitacion(desde, hasta, pid, vid),
                temperaturaMaximaAbsoluta = datoDao.temperaturaMaximaAbsoluta(desde, hasta, pid, vid),
                temperaturaMinimaAbsoluta = datoDao.temperaturaMinimaAbsoluta(desde, hasta, pid, vid),
                promedioTemperaturaMaxima = datoDao.promedioTemperaturaMaxima(desde, hasta, pid, vid),
                promedioTemperaturaMinima = datoDao.promedioTemperaturaMinima(desde, hasta, pid, vid)
            )

            val precipPorPluvio = datoDao
                .precipitacionPorPluviometro(desde, hasta, pid, vid)
                .map { PrecipPorPluviometro(registro = it.pluviometro_registro, totalMm = it.totalPrecipitacion) }

            val topVol = datoDao.voluntarioConMasRegistros(desde, hasta, pid)?.let {
                TopVoluntario(nombre = it.voluntario_nombre, totalRegistros = it.totalRegistros)
            }

            val topPluv = datoDao.pluviometroMasActivo(desde, hasta, pid, vid)?.let {
                PluvioMasActivo(registro = it.pluviometro_registro, totalRegistros = it.totalRegistros)
            }

            EstadisticasCompletas(
                resumen = resumen,
                precipPorPluviometro = precipPorPluvio,
                voluntarioTop = topVol,
                pluvioMasActivo = topPluv
            )
        }

    suspend fun obtenerDatosParaExportar(filtro: FiltroEstadisticas): List<DatoMeteorologico> =
        withContext(Dispatchers.IO) {
            val (desde, hasta) = calcularRango(filtro)
            datoDao.obtenerTodosFiltrados(desde, hasta, filtro.pluviometroId, filtro.voluntarioUid)
        }

    suspend fun obtenerListaPluviometros(): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            try {
                pluvioDao.obtenerActivos().first()
                    .map { Pair(it.id, it.numero_registro) }
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun obtenerListaVoluntarios(): List<Pair<String, String>> =
        withContext(Dispatchers.IO) {
            try {
                voluntarioDao.obtenerActivos().first()
                    .filter { it.estado_aprobacion == "Aprobado" }
                    .map { Pair(it.firebase_uid, it.nombre) }
            } catch (e: Exception) {
                emptyList()
            }
        }
}
