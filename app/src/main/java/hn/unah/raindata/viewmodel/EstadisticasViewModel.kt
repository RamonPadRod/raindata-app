package hn.unah.raindata.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.repository.EstadisticasCompletas
import hn.unah.raindata.data.repository.EstadisticasRepository
import hn.unah.raindata.data.repository.FiltroEstadisticas
import hn.unah.raindata.data.repository.PeriodoFiltro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhatim.fastexcel.Workbook
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EstadisticasViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EstadisticasRepository(
        datoDao = db.datoMeteorologicoDao(),
        pluvioDao = db.pluviometroDao(),
        voluntarioDao = db.voluntarioDao()
    )

    private val _filtro = MutableStateFlow(FiltroEstadisticas())
    val filtro: StateFlow<FiltroEstadisticas> = _filtro.asStateFlow()

    private val _estadisticas = MutableStateFlow(EstadisticasCompletas())
    val estadisticas: StateFlow<EstadisticasCompletas> = _estadisticas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _exportResult = MutableStateFlow<Uri?>(null)
    val exportResult: StateFlow<Uri?> = _exportResult.asStateFlow()

    private val _listaPluviometros = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val listaPluviometros: StateFlow<List<Pair<String, String>>> = _listaPluviometros.asStateFlow()

    private val _listaVoluntarios = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val listaVoluntarios: StateFlow<List<Pair<String, String>>> = _listaVoluntarios.asStateFlow()

    init {
        cargarFiltrosDisponibles()
        calcularEstadisticas()
    }

    private fun cargarFiltrosDisponibles() {
        viewModelScope.launch {
            _listaPluviometros.value = repository.obtenerListaPluviometros()
            _listaVoluntarios.value = repository.obtenerListaVoluntarios()
        }
    }

    fun aplicarFiltro(nuevo: FiltroEstadisticas) {
        _filtro.value = nuevo
        calcularEstadisticas()
    }

    private fun calcularEstadisticas() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // DEBUG TEMPORAL
                val debug = db.datoMeteorologicoDao().debugTodos()
                android.util.Log.d("DEBUG_DB", "Total en tabla: ${debug.size}")
                debug.forEach {
                    android.util.Log.d("DEBUG_DB", "id=${it.id} activo=${it.activo} fecha=${it.fecha_lectura} sync=${it.syncStatus}")
                }
                _estadisticas.value = repository.calcularEstadisticas(_filtro.value)
            } catch (e: Exception) {
                _errorMessage.value = "Error al calcular estadísticas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarExportResult() {
        _exportResult.value = null
    }

    // ===== EXPORTAR EXCEL =====
    fun exportarExcel(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val datos = repository.obtenerDatosParaExportar(_filtro.value)
                val uri = generarExcel(context, datos)
                _exportResult.value = uri
            } catch (e: Exception) {
                _errorMessage.value = "Error al generar Excel: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun generarExcel(context: Context, datos: List<DatoMeteorologico>): Uri =
        withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "raindata_reporte_$timestamp.xlsx"

            val outputStream: OutputStream
            val uri: Uri

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cv = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)!!
                outputStream = context.contentResolver.openOutputStream(uri)!!
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(dir, fileName)
                outputStream = java.io.FileOutputStream(file)
                uri = Uri.fromFile(file)
            }

            outputStream.use { os ->
                val wb = Workbook(os, "RainData", "1.0")
                val ws = wb.newWorksheet("Datos Meteorológicos")

                val headers = listOf(
                    "Pluviómetro", "Voluntario", "Fecha Lectura", "Hora Lectura",
                    "Precipitación (mm)", "Temp. Máx (°C)", "Temp. Mín (°C)",
                    "Condiciones", "Observaciones", "Fecha Registro"
                )
                headers.forEachIndexed { col, header ->
                    ws.value(0, col, header)
                    ws.style(0, col).bold().set()
                }

                datos.forEachIndexed { rowIdx, dato ->
                    val row = rowIdx + 1
                    ws.value(row, 0, dato.pluviometro_registro)
                    ws.value(row, 1, dato.voluntario_nombre)
                    ws.value(row, 2, dato.fecha_lectura)
                    ws.value(row, 3, dato.hora_lectura)
                    ws.value(row, 4, dato.precipitacion)
                    ws.value(row, 5, dato.temperatura_maxima?.toString() ?: "")
                    ws.value(row, 6, dato.temperatura_minima?.toString() ?: "")
                    ws.value(row, 7, dato.condiciones_dia.replace("|", ", "))
                    ws.value(row, 8, dato.observaciones ?: "")
                    ws.value(row, 9, dato.fecha_registro)
                }

                wb.finish()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cv = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                context.contentResolver.update(uri, cv, null, null)
            }

            uri
        }

    // ===== EXPORTAR PDF =====
    fun exportarPdf(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val datos = repository.obtenerDatosParaExportar(_filtro.value)
                val uri = generarPdf(context, datos)
                _exportResult.value = uri
            } catch (e: Exception) {
                _errorMessage.value = "Error al generar PDF: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun generarPdf(context: Context, datos: List<DatoMeteorologico>): Uri =
        withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "raindata_reporte_$timestamp.pdf"
            val baos = ByteArrayOutputStream()

            // Fuentes iText 5
            val boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, BaseColor.DARK_GRAY)
            val boldFontSmall = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, BaseColor.DARK_GRAY)
            val boldFontTiny = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, BaseColor.WHITE)
            val normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.BLACK)
            val normalFontSmall = FontFactory.getFont(FontFactory.HELVETICA, 9f, BaseColor.BLACK)
            val normalFontTiny = FontFactory.getFont(FontFactory.HELVETICA, 7f, BaseColor.BLACK)
            val grayFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.GRAY)

            val stats = _estadisticas.value
            val filtroActual = _filtro.value

            // Crear documento iText 5
            val document = Document()
            PdfWriter.getInstance(document, baos)
            document.open()

            // Título
            val titulo = Paragraph("Reporte de Datos Meteorológicos\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.DARK_GRAY))
            titulo.alignment = Element.ALIGN_CENTER
            document.add(titulo)

            val subtitulo = Paragraph("RainData — Sistema Pluviométrico\n", grayFont)
            subtitulo.alignment = Element.ALIGN_CENTER
            document.add(subtitulo)

            val periodo = Paragraph(
                "Período: ${filtroActual.periodo.label}  |  " +
                        "Pluviómetro: ${filtroActual.pluviometroLabel}  |  " +
                        "Voluntario: ${filtroActual.voluntarioLabel}\n\n",
                FontFactory.getFont(FontFactory.HELVETICA, 9f, BaseColor.GRAY)
            )
            periodo.alignment = Element.ALIGN_CENTER
            document.add(periodo)

            // Sección resumen
            val tituloResumen = Paragraph("Resumen General\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, BaseColor.DARK_GRAY))
            document.add(tituloResumen)

            val resumenTable = PdfPTable(2)
            resumenTable.widthPercentage = 100f
            resumenTable.setWidths(floatArrayOf(50f, 50f))

            fun addResumenRow(label: String, value: String) {
                resumenTable.addCell(PdfPCell(Phrase(label, boldFontSmall)))
                resumenTable.addCell(PdfPCell(Phrase(value, normalFontSmall)))
            }

            addResumenRow("Total de registros:", "${stats.resumen.totalRegistros}")
            addResumenRow("Precipitación total:", "${"%.2f".format(stats.resumen.precipitacionTotal)} mm")
            addResumenRow("Promedio precipitación/día:", "${"%.2f".format(stats.resumen.promedioPrecipitacionDia)} mm")
            addResumenRow("Día con mayor lluvia:", stats.resumen.diaMayorPrecipitacion ?: "—")
            addResumenRow("Temperatura máxima abs.:", stats.resumen.temperaturaMaximaAbsoluta?.let { "${"%.1f".format(it)}°C" } ?: "—")
            addResumenRow("Temperatura mínima abs.:", stats.resumen.temperaturaMinimaAbsoluta?.let { "${"%.1f".format(it)}°C" } ?: "—")
            addResumenRow("Pluviómetro más activo:", stats.pluvioMasActivo?.let { "${it.registro} (${it.totalRegistros} reg.)" } ?: "—")
            addResumenRow("Voluntario con más registros:", stats.voluntarioTop?.let { "${it.nombre} (${it.totalRegistros} reg.)" } ?: "—")

            document.add(resumenTable)
            document.add(Paragraph("\n"))

            // Tabla de datos
            val tituloDetalle = Paragraph("Detalle de registros (${datos.size})\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, BaseColor.DARK_GRAY))
            document.add(tituloDetalle)

            val dataTable = PdfPTable(8)
            dataTable.widthPercentage = 100f
            dataTable.setWidths(floatArrayOf(14f, 14f, 11f, 9f, 13f, 10f, 10f, 19f))

            val headers = listOf("Pluviómetro", "Voluntario", "Fecha", "Hora", "Precip. (mm)", "T.Máx", "T.Mín", "Condiciones")
            val headerColor = BaseColor(0, 105, 92) // teal oscuro
            headers.forEach { h ->
                val cell = PdfPCell(Phrase(h, boldFontTiny))
                cell.backgroundColor = headerColor
                cell.horizontalAlignment = Element.ALIGN_CENTER
                dataTable.addCell(cell)
            }

            datos.forEach { dato ->
                dataTable.addCell(PdfPCell(Phrase(dato.pluviometro_registro, normalFontTiny)))
                dataTable.addCell(PdfPCell(Phrase(dato.voluntario_nombre, normalFontTiny)))
                dataTable.addCell(PdfPCell(Phrase(dato.fecha_lectura, normalFontTiny)))
                dataTable.addCell(PdfPCell(Phrase(dato.hora_lectura, normalFontTiny)))
                dataTable.addCell(PdfPCell(Phrase("${"%.2f".format(dato.precipitacion)}", normalFontTiny)))
                dataTable.addCell(PdfPCell(Phrase(dato.temperatura_maxima?.let { "${"%.1f".format(it)}°" } ?: "—", normalFontTiny)))
                dataTable.addCell(PdfPCell(Phrase(dato.temperatura_minima?.let { "${"%.1f".format(it)}°" } ?: "—", normalFontTiny)))
                dataTable.addCell(PdfPCell(Phrase(dato.condiciones_dia.replace("|", ", "), normalFontTiny)))
            }

            document.add(dataTable)

            val pie = Paragraph(
                "Generado el: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor.GRAY)
            )
            pie.alignment = Element.ALIGN_RIGHT
            document.add(pie)

            document.close()

            // Guardar en Descargas
            val uri: Uri
            val outputStream: OutputStream

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cv = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)!!
                outputStream = context.contentResolver.openOutputStream(uri)!!
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(dir, fileName)
                outputStream = java.io.FileOutputStream(file)
                uri = Uri.fromFile(file)
            }

            outputStream.use { it.write(baos.toByteArray()) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cv = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                context.contentResolver.update(uri, cv, null, null)
            }

            uri
        }
}