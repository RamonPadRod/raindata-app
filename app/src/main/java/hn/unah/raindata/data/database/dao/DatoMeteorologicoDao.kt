package hn.unah.raindata.data.database.dao

import android.content.ContentValues
import android.database.Cursor
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.DatoMeteorologico

class DatoMeteorologicoDao(private val dbHelper: AppDatabase) {

    fun insertar(dato: DatoMeteorologico): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", dato.id)
            put("voluntario_id", dato.voluntario_id)
            put("voluntario_nombre", dato.voluntario_nombre)
            put("pluviometro_id", dato.pluviometro_id)
            put("pluviometro_registro", dato.pluviometro_registro)
            put("fecha", dato.fecha)
            put("hora", dato.hora)
            put("precipitacion", dato.precipitacion)
            put("temperatura_maxima", dato.temperatura_maxima)
            put("temperatura_minima", dato.temperatura_minima)
            put("condicion_dia", dato.condicion_dia)
            put("observaciones", dato.observaciones)
            put("fecha_creacion", dato.fecha_creacion)
            put("fecha_modificacion", dato.fecha_modificacion)
        }

        return db.insert("datos_meteorologicos", null, values)
    }

    fun obtenerTodos(): List<DatoMeteorologico> {
        val datos = mutableListOf<DatoMeteorologico>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM datos_meteorologicos ORDER BY fecha DESC, hora DESC",
            null
        )

        with(cursor) {
            while (moveToNext()) {
                datos.add(cursorToDatoMeteorologico(this))
            }
        }
        cursor.close()
        return datos
    }

    fun obtenerPorPluviometro(pluviometroId: String): List<DatoMeteorologico> {
        val datos = mutableListOf<DatoMeteorologico>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM datos_meteorologicos WHERE pluviometro_id = ? ORDER BY fecha DESC, hora DESC",
            arrayOf(pluviometroId)
        )

        with(cursor) {
            while (moveToNext()) {
                datos.add(cursorToDatoMeteorologico(this))
            }
        }
        cursor.close()
        return datos
    }

    fun obtenerPorVoluntario(voluntarioId: Long): List<DatoMeteorologico> {  // ← Cambiar String a Long
        val datos = mutableListOf<DatoMeteorologico>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM datos_meteorologicos WHERE voluntario_id = ? ORDER BY fecha DESC, hora DESC",
            arrayOf(voluntarioId.toString())  // ← Agregar .toString()
        )

        with(cursor) {
            while (moveToNext()) {
                datos.add(cursorToDatoMeteorologico(this))
            }
        }
        cursor.close()
        return datos
    }

    fun obtenerPorFecha(fecha: String): List<DatoMeteorologico> {
        val datos = mutableListOf<DatoMeteorologico>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM datos_meteorologicos WHERE fecha = ? ORDER BY hora DESC",
            arrayOf(fecha)
        )

        with(cursor) {
            while (moveToNext()) {
                datos.add(cursorToDatoMeteorologico(this))
            }
        }
        cursor.close()
        return datos
    }

    // ✅ NUEVO: Verificar si ya existe un registro para un pluviómetro en una fecha
    fun existeRegistroEnFecha(pluviometroId: String, fecha: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM datos_meteorologicos WHERE pluviometro_id = ? AND fecha = ?",
            arrayOf(pluviometroId, fecha)
        )

        var existe = false
        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0
        }
        cursor.close()
        return existe
    }

    fun actualizar(dato: DatoMeteorologico): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("voluntario_id", dato.voluntario_id)
            put("voluntario_nombre", dato.voluntario_nombre)
            put("pluviometro_id", dato.pluviometro_id)
            put("pluviometro_registro", dato.pluviometro_registro)
            put("fecha", dato.fecha)
            put("hora", dato.hora)
            put("precipitacion", dato.precipitacion)
            put("temperatura_maxima", dato.temperatura_maxima)
            put("temperatura_minima", dato.temperatura_minima)
            put("condicion_dia", dato.condicion_dia)
            put("observaciones", dato.observaciones)
            put("fecha_modificacion", System.currentTimeMillis())
        }

        return db.update("datos_meteorologicos", values, "id = ?", arrayOf(dato.id))
    }

    fun eliminar(id: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete("datos_meteorologicos", "id = ?", arrayOf(id))
    }

    fun contarRegistros(): Int {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM datos_meteorologicos",
            null
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    private fun cursorToDatoMeteorologico(cursor: Cursor): DatoMeteorologico {
        return DatoMeteorologico(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            voluntario_id = cursor.getLong(cursor.getColumnIndexOrThrow("voluntario_id")), // ← CAMBIAR getString a getLong
            voluntario_nombre = cursor.getString(cursor.getColumnIndexOrThrow("voluntario_nombre")),
            pluviometro_id = cursor.getString(cursor.getColumnIndexOrThrow("pluviometro_id")),
            pluviometro_registro = cursor.getString(cursor.getColumnIndexOrThrow("pluviometro_registro")),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
            hora = cursor.getString(cursor.getColumnIndexOrThrow("hora")),
            precipitacion = cursor.getDouble(cursor.getColumnIndexOrThrow("precipitacion")),
            temperatura_maxima = if (cursor.isNull(cursor.getColumnIndexOrThrow("temperatura_maxima"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("temperatura_maxima")),
            temperatura_minima = if (cursor.isNull(cursor.getColumnIndexOrThrow("temperatura_minima"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("temperatura_minima")),
            condicion_dia = cursor.getString(cursor.getColumnIndexOrThrow("condicion_dia")),
            observaciones = cursor.getString(cursor.getColumnIndexOrThrow("observaciones")),
            fecha_creacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_creacion")),
            fecha_modificacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_modificacion"))
        )
    }
}