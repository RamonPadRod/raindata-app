package hn.unah.raindata.data.repository

// ===== FILTRO =====

enum class PeriodoFiltro(val label: String) {
    HOY("Hoy"),
    ULTIMO_MES("Último mes"),
    ULTIMO_ANIO("Último año"),
    TODO("Todo")
}

data class FiltroEstadisticas(
    val periodo: PeriodoFiltro = PeriodoFiltro.TODO,
    val desde: String = "",          // yyyy-MM-dd, vacío = sin límite inferior
    val hasta: String = "",          // yyyy-MM-dd, vacío = sin límite superior
    val pluviometroId: String = "",  // ID del pluviómetro, vacío = todos
    val pluviometroLabel: String = "Todos",
    val voluntarioUid: String = "", // UID del voluntario, vacío = todos
    val voluntarioLabel: String = "Todos"
)

// ===== RESULTADOS ESTADÍSTICOS =====

data class ResumenGeneral(
    val totalRegistros: Int = 0,
    val precipitacionTotal: Double = 0.0,
    val promedioPrecipitacionDia: Double = 0.0,
    val diaMayorPrecipitacion: String? = null,
    val temperaturaMaximaAbsoluta: Double? = null,
    val temperaturaMinimaAbsoluta: Double? = null,
    val promedioTemperaturaMaxima: Double? = null,
    val promedioTemperaturaMinima: Double? = null
)

data class PrecipPorPluviometro(
    val registro: String,
    val totalMm: Double
)

data class TopVoluntario(
    val nombre: String,
    val totalRegistros: Int
)

data class PluvioMasActivo(
    val registro: String,
    val totalRegistros: Int
)

data class EstadisticasCompletas(
    val resumen: ResumenGeneral = ResumenGeneral(),
    val precipPorPluviometro: List<PrecipPorPluviometro> = emptyList(),
    val voluntarioTop: TopVoluntario? = null,
    val pluvioMasActivo: PluvioMasActivo? = null
)
