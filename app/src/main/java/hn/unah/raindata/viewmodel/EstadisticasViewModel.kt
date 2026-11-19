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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import hn.unah.raindata.R
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


private class FooterPageEvent(private val fechaGeneracion: String) : PdfPageEventHelper() {
    private val footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor(0x75, 0x75, 0x75))

    override fun onEndPage(writer: PdfWriter, document: Document) {
        val cb = writer.directContent
        val leftText = "REVOCLIMAP · Mesa Agroclimática de El Paraíso — $fechaGeneracion"
        val rightText = "Página ${writer.pageNumber}"

        val y = document.bottom() - 15f
        ColumnText.showTextAligned(
            cb,
            Element.ALIGN_LEFT,
            Phrase(leftText, footerFont),
            document.left(),
            y,
            0f
        )
        ColumnText.showTextAligned(
            cb,
            Element.ALIGN_RIGHT,
            Phrase(rightText, footerFont),
            document.right(),
            y,
            0f
        )
    }
}

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
        refrescarEstadisticas()
    }

    fun refrescarEstadisticas() {
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
            val fechaGeneracion = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val fileName = "revoclimap_reporte_$timestamp.xlsx"
            val filtroActual = _filtro.value

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
                val wb = Workbook(os, "REVOCLIMAP", "1.0")
                val ws = wb.newWorksheet("Datos Meteorológicos")

                // Anchos de columna ajustados para no cortar encabezados ni fechas
                val colWidths = listOf(16.0, 24.0, 15.0, 14.0, 18.0, 16.0, 16.0, 14.0, 24.0, 32.0, 20.0)
                colWidths.forEachIndexed { col, w ->
                    ws.width(col, w)
                }

                // Fila 0: Nombre de la Mesa (celdas combinadas, fondo verde oscuro, texto blanco, negrita, tamaño grande)
                ws.value(0, 0, "MESA AGROCLIMÁTICA DE EL PARAÍSO")
                ws.range(0, 0, 0, 10).merge()
                ws.range(0, 0, 0, 10).style()
                    .fillColor("1B5E20")
                    .fontColor("FFFFFF")
                    .bold()
                    .fontSize(14)
                    .horizontalAlignment("center")
                    .verticalAlignment("center")
                    .set()

                // Fila 1: Subtítulo REVOCLIMAP
                ws.value(1, 0, "REVOCLIMAP — Red de Voluntarios Climáticos de El Paraíso")
                ws.range(1, 0, 1, 10).merge()
                ws.range(1, 0, 1, 10).style()
                    .fontColor("757575")
                    .fontSize(10)
                    .bold()
                    .horizontalAlignment("center")
                    .verticalAlignment("center")
                    .set()

                // Fila 2: Filtros y fecha de generación
                val textoFiltros = "Período: ${filtroActual.periodo.label}   |   Pluviómetro: ${filtroActual.pluviometroLabel}   |   Voluntario: ${filtroActual.voluntarioLabel}   |   Generado: $fechaGeneracion"
                ws.value(2, 0, textoFiltros)
                ws.range(2, 0, 2, 10).merge()
                ws.range(2, 0, 2, 10).style()
                    .fontColor("555555")
                    .fontSize(9)
                    .horizontalAlignment("center")
                    .verticalAlignment("center")
                    .set()

                // Fila 3: fila en blanco antes de la tabla

                // Fila 4: Encabezados de la tabla con fondo verde medio, texto blanco, negrita y centrados
                val headers = listOf(
                    "Pluviómetro", "Voluntario", "Fecha Lectura", "Hora Lectura",
                    "Precipitación (mm)", "Temp. Máx (°C)", "Temp. Mín (°C)",
                    "Humedad (%)", "Condiciones", "Observaciones", "Fecha Registro"
                )
                headers.forEachIndexed { col, header ->
                    ws.value(4, col, header)
                    ws.style(4, col)
                        .fillColor("2E7D32")
                        .fontColor("FFFFFF")
                        .bold()
                        .horizontalAlignment("center")
                        .verticalAlignment("center")
                        .set()
                }

                // Panel congelado debajo del encabezado (filas 0 a 4 congeladas)
                ws.freezePane(0, 5)

                // Filtro automático
                try {
                    ws.setAutoFilter(4, 0, headers.size - 1)
                } catch (e: Exception) {
                    // Ignorar si falla
                }

                // Filas de datos (a partir de la fila 5)
                val startRow = 5
                datos.forEachIndexed { rowIdx, dato ->
                    val row = startRow + rowIdx
                    ws.value(row, 0, dato.pluviometro_registro)
                    ws.value(row, 1, dato.voluntario_nombre)
                    ws.value(row, 2, dato.fecha_lectura)
                    ws.value(row, 3, dato.hora_lectura)
                    ws.value(row, 4, dato.precipitacion)
                    if (dato.temperatura_maxima != null) {
                        ws.value(row, 5, dato.temperatura_maxima!!)
                    } else {
                        ws.value(row, 5, "")
                    }
                    if (dato.temperatura_minima != null) {
                        ws.value(row, 6, dato.temperatura_minima!!)
                    } else {
                        ws.value(row, 6, "")
                    }
                    if (dato.humedad_relativa != null) {
                        ws.value(row, 7, dato.humedad_relativa!!)
                    } else {
                        ws.value(row, 7, "")
                    }
                    ws.value(row, 8, dato.condiciones_dia.replace("|", ", "))
                    ws.value(row, 9, dato.observaciones ?: "")
                    ws.value(row, 10, dato.fecha_registro)
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

    private fun obtenerLogoBytes(context: Context): ByteArray? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeResource(context.resources, R.drawable.logo_mesa_transparente, options)

            val targetWidth = 400
            var inSampleSize = 1
            if (options.outWidth > targetWidth) {
                val halfWidth = options.outWidth / 2
                while ((halfWidth / inSampleSize) >= targetWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val decoded = BitmapFactory.decodeResource(context.resources, R.drawable.logo_mesa_transparente, decodeOptions)
                ?: return null

            val finalBitmap = if (decoded.width > targetWidth) {
                val aspectRatio = decoded.height.toFloat() / decoded.width.toFloat()
                val targetHeight = (targetWidth * aspectRatio).toInt()
                val scaled = Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true)
                if (scaled != decoded) {
                    decoded.recycle()
                }
                scaled
            } else {
                decoded
            }

            val stream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            finalBitmap.recycle()
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun generarPdf(context: Context, datos: List<DatoMeteorologico>): Uri =
        withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fechaGeneracion = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val fileName = "revoclimap_reporte_$timestamp.pdf"
            val baos = ByteArrayOutputStream()

            // Paleta institucional
            val colorVerdeOscuro = BaseColor(0x1B, 0x5E, 0x20)
            val colorVerdeMedio = BaseColor(0x2E, 0x7D, 0x32)
            val colorVerdeMuyClaro = BaseColor(0xE8, 0xF5, 0xE9)
            val colorGrisClaro = BaseColor(0xF5, 0xF7, 0xF5)
            val colorGrisTexto = BaseColor(0x75, 0x75, 0x75)
            val colorGrisBorde = BaseColor(0xDD, 0xDD, 0xDD)

            // Fuentes iText 5
            val fontMesa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, colorVerdeOscuro)
            val fontRevoclimapSub = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, colorGrisTexto)
            val fontTituloReporte = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f, colorVerdeOscuro)
            val fontFiltros = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, BaseColor.DARK_GRAY)
            val fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f, colorVerdeOscuro)
            val fontResumenLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, colorVerdeOscuro)
            val fontResumenValue = FontFactory.getFont(FontFactory.HELVETICA, 8f, BaseColor.BLACK)
            val fontTablaHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, BaseColor.WHITE)
            val fontTablaDato = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, BaseColor.BLACK)

            val stats = _estadisticas.value
            val filtroActual = _filtro.value

            val document = Document()
            document.setMargins(36f, 36f, 36f, 50f)
            val writer = PdfWriter.getInstance(document, baos)
            writer.pageEvent = FooterPageEvent(fechaGeneracion)
            document.open()

            // 1. Membrete en tabla de 2 columnas
            val logoBytes = obtenerLogoBytes(context)
            val logoImage = logoBytes?.let {
                try {
                    com.itextpdf.text.Image.getInstance(it).apply {
                        scaleToFit(70f, 70f)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val headerTable = PdfPTable(2).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(75f, 445f))
                spacingAfter = 6f
            }

            val logoCell = if (logoImage != null) {
                PdfPCell(logoImage, true).apply {
                    border = PdfPCell.NO_BORDER
                    verticalAlignment = Element.ALIGN_MIDDLE
                    horizontalAlignment = Element.ALIGN_CENTER
                    paddingRight = 8f
                }
            } else {
                PdfPCell().apply {
                    border = PdfPCell.NO_BORDER
                }
            }
            headerTable.addCell(logoCell)

            val textHeaderCell = PdfPCell().apply {
                border = PdfPCell.NO_BORDER
                verticalAlignment = Element.ALIGN_MIDDLE
                paddingLeft = 6f
                addElement(Paragraph("MESA AGROCLIMÁTICA DE EL PARAÍSO", fontMesa))
                addElement(Paragraph("REVOCLIMAP — Red de Voluntarios Climáticos de El Paraíso", fontRevoclimapSub))
            }
            headerTable.addCell(textHeaderCell)
            document.add(headerTable)

            // 2. Línea gruesa verde medio con línea fina gris
            val lineaVerde = LineSeparator(2.5f, 100f, colorVerdeMedio, Element.ALIGN_CENTER, -1f)
            val lineaGris = LineSeparator(0.75f, 100f, colorGrisBorde, Element.ALIGN_CENTER, -2f)
            document.add(lineaVerde)
            document.add(lineaGris)

            // 3. Título y filtros en recuadro verde muy claro
            val bannerTable = PdfPTable(1).apply {
                widthPercentage = 100f
                spacingBefore = 8f
                spacingAfter = 12f
            }
            val bannerCell = PdfPCell().apply {
                backgroundColor = colorVerdeMuyClaro
                borderColor = colorVerdeMedio
                borderWidth = 0.5f
                setPadding(8f)

                val pTitulo = Paragraph("Reporte de Datos Meteorológicos", fontTituloReporte).apply {
                    alignment = Element.ALIGN_CENTER
                }
                val pFiltros = Paragraph(
                    "Período: ${filtroActual.periodo.label}   |   Pluviómetro: ${filtroActual.pluviometroLabel}   |   Voluntario: ${filtroActual.voluntarioLabel}",
                    fontFiltros
                ).apply {
                    alignment = Element.ALIGN_CENTER
                    spacingBefore = 3f
                }
                addElement(pTitulo)
                addElement(pFiltros)
            }
            bannerTable.addCell(bannerCell)
            document.add(bannerTable)

            // 4. Resumen General
            val tituloResumen = Paragraph("Resumen General", fontSeccion).apply {
                spacingAfter = 6f
            }
            document.add(tituloResumen)

            val resumenTable = PdfPTable(2).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(45f, 55f))
                spacingAfter = 14f
            }

            fun addResumenRow(label: String, value: String) {
                val cellLabel = PdfPCell(Phrase(label, fontResumenLabel)).apply {
                    backgroundColor = colorVerdeMuyClaro
                    borderColor = colorGrisBorde
                    borderWidth = 0.5f
                    setPadding(4.5f)
                    verticalAlignment = Element.ALIGN_MIDDLE
                }
                val cellVal = PdfPCell(Phrase(value, fontResumenValue)).apply {
                    backgroundColor = BaseColor.WHITE
                    borderColor = colorGrisBorde
                    borderWidth = 0.5f
                    setPadding(4.5f)
                    verticalAlignment = Element.ALIGN_MIDDLE
                }
                resumenTable.addCell(cellLabel)
                resumenTable.addCell(cellVal)
            }

            addResumenRow("Total de registros:", "${stats.resumen.totalRegistros}")
            addResumenRow("Precipitación total:", "${"%.2f".format(stats.resumen.precipitacionTotal)} mm")
            addResumenRow("Promedio precipitación/día:", "${"%.2f".format(stats.resumen.promedioPrecipitacionDia)} mm")
            addResumenRow("Día con mayor lluvia:", stats.resumen.diaMayorPrecipitacion ?: "—")
            addResumenRow("Temperatura máxima abs.:", stats.resumen.temperaturaMaximaAbsoluta?.let { "${"%.1f".format(it)}°C" } ?: "—")
            addResumenRow("Temperatura mínima abs.:", stats.resumen.temperaturaMinimaAbsoluta?.let { "${"%.1f".format(it)}°C" } ?: "—")
            addResumenRow("Humedad relativa promedio:", stats.resumen.humedadPromedio?.let { "${"%.1f".format(it)} %" } ?: "—")
            addResumenRow("Pluviómetro más activo:", stats.pluvioMasActivo?.let { "${it.registro} (${it.totalRegistros} reg.)" } ?: "—")
            addResumenRow("Voluntario con más registros:", stats.voluntarioTop?.let { "${it.nombre} (${it.totalRegistros} reg.)" } ?: "—")

            document.add(resumenTable)

            // 5. Tabla de detalle de registros
            val tituloDetalle = Paragraph("Detalle de registros (${datos.size})", fontSeccion).apply {
                spacingAfter = 6f
            }
            document.add(tituloDetalle)

            val dataTable = PdfPTable(9).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(12f, 13f, 11f, 9f, 12f, 9f, 9f, 10f, 15f))
                headerRows = 1
            }

            val headers = listOf("Pluviómetro", "Voluntario", "Fecha", "Hora", "Precip. (mm)", "T.Máx", "T.Mín", "Humedad (%)", "Condiciones")
            headers.forEach { h ->
                val cell = PdfPCell(Phrase(h, fontTablaHeader)).apply {
                    backgroundColor = colorVerdeOscuro
                    horizontalAlignment = Element.ALIGN_CENTER
                    verticalAlignment = Element.ALIGN_MIDDLE
                    borderColor = colorVerdeOscuro
                    setPadding(5f)
                }
                dataTable.addCell(cell)
            }

            datos.forEachIndexed { idx, dato ->
                val bg = if (idx % 2 == 1) colorGrisClaro else BaseColor.WHITE
                fun createDataCell(text: String, align: Int = Element.ALIGN_LEFT): PdfPCell {
                    return PdfPCell(Phrase(text, fontTablaDato)).apply {
                        backgroundColor = bg
                        horizontalAlignment = align
                        verticalAlignment = Element.ALIGN_MIDDLE
                        borderColor = colorGrisBorde
                        borderWidth = 0.5f
                        setPadding(4f)
                    }
                }

                dataTable.addCell(createDataCell(dato.pluviometro_registro))
                dataTable.addCell(createDataCell(dato.voluntario_nombre))
                dataTable.addCell(createDataCell(dato.fecha_lectura, Element.ALIGN_CENTER))
                dataTable.addCell(createDataCell(dato.hora_lectura, Element.ALIGN_CENTER))
                dataTable.addCell(createDataCell("${"%.2f".format(dato.precipitacion)}", Element.ALIGN_RIGHT))
                dataTable.addCell(createDataCell(dato.temperatura_maxima?.let { "${"%.1f".format(it)}°" } ?: "—", Element.ALIGN_RIGHT))
                dataTable.addCell(createDataCell(dato.temperatura_minima?.let { "${"%.1f".format(it)}°" } ?: "—", Element.ALIGN_RIGHT))
                dataTable.addCell(createDataCell(dato.humedad_relativa?.let { "${"%.1f".format(it)}%" } ?: "—", Element.ALIGN_RIGHT))
                dataTable.addCell(createDataCell(dato.condiciones_dia.replace("|", ", ")))
            }

            document.add(dataTable)
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