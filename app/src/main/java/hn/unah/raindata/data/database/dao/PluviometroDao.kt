package hn.unah.raindata.data.database.dao

import android.content.ContentValues
import android.database.Cursor
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.Pluviometro

class PluviometroDao(private val dbHelper: AppDatabase) {

    fun generarCodigoAutomatico(): String {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT MAX(CAST(SUBSTR(numero_registro, 5) AS INTEGER)) as max_num FROM pluviometros",
            null
        )

        var nuevoNumero = 1
        if (cursor.moveToFirst()) {
            val maxNum = cursor.getInt(cursor.getColumnIndexOrThrow("max_num"))
            nuevoNumero = maxNum + 1
        }
        cursor.close()

        return String.format("PLU-%04d", nuevoNumero)
    }

    fun insertar(pluviometro: Pluviometro): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", pluviometro.id)
            put("numero_registro", pluviometro.numero_registro)
            put("latitud", pluviometro.latitud)
            put("longitud", pluviometro.longitud)
            put("direccion", pluviometro.direccion)
            put("departamento", pluviometro.departamento)
            put("municipio", pluviometro.municipio)
            put("aldea", pluviometro.aldea)
            put("caserio_barrio_colonia", pluviometro.caserio_barrio_colonia)
            put("responsable_id", pluviometro.responsable_id)
            put("responsable_nombre", pluviometro.responsable_nombre)
            put("observaciones", pluviometro.observaciones)
            put("activo", if (pluviometro.activo) 1 else 0)
            put("fecha_creacion", pluviometro.fecha_creacion)
            put("fecha_modificacion", pluviometro.fecha_modificacion)
        }

        return db.insert("pluviometros", null, values)
    }

    fun obtenerActivos(): List<Pluviometro> {
        val pluviometros = mutableListOf<Pluviometro>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM pluviometros WHERE activo = 1 ORDER BY numero_registro",
            null
        )

        with(cursor) {
            while (moveToNext()) {
                pluviometros.add(cursorToPluviometro(this))
            }
        }
        cursor.close()
        return pluviometros
    }

    fun obtenerTodos(): List<Pluviometro> {
        val pluviometros = mutableListOf<Pluviometro>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM pluviometros ORDER BY numero_registro",
            null
        )

        with(cursor) {
            while (moveToNext()) {
                pluviometros.add(cursorToPluviometro(this))
            }
        }
        cursor.close()
        return pluviometros
    }

    fun obtenerPorId(id: String): Pluviometro? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM pluviometros WHERE id = ? LIMIT 1",
            arrayOf(id)
        )

        var pluviometro: Pluviometro? = null
        if (cursor.moveToFirst()) {
            pluviometro = cursorToPluviometro(cursor)
        }
        cursor.close()
        return pluviometro
    }

    fun obtenerPorResponsable(responsableId: String): List<Pluviometro> {
        val pluviometros = mutableListOf<Pluviometro>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM pluviometros WHERE responsable_id = ? AND activo = 1 ORDER BY numero_registro",
            arrayOf(responsableId)
        )

        with(cursor) {
            while (moveToNext()) {
                pluviometros.add(cursorToPluviometro(this))
            }
        }
        cursor.close()
        return pluviometros
    }

    fun buscar(termino: String): List<Pluviometro> {
        val pluviometros = mutableListOf<Pluviometro>()
        val db = dbHelper.readableDatabase
        val searchTerm = "%$termino%"
        val cursor: Cursor = db.rawQuery(
            """SELECT * FROM pluviometros 
               WHERE (numero_registro LIKE ? OR municipio LIKE ? OR responsable_nombre LIKE ?) 
               AND activo = 1 
               ORDER BY numero_registro""",
            arrayOf(searchTerm, searchTerm, searchTerm)
        )

        with(cursor) {
            while (moveToNext()) {
                pluviometros.add(cursorToPluviometro(this))
            }
        }
        cursor.close()
        return pluviometros
    }

    fun actualizar(pluviometro: Pluviometro): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("numero_registro", pluviometro.numero_registro)
            put("latitud", pluviometro.latitud)
            put("longitud", pluviometro.longitud)
            put("direccion", pluviometro.direccion)
            put("departamento", pluviometro.departamento)
            put("municipio", pluviometro.municipio)
            put("aldea", pluviometro.aldea)
            put("caserio_barrio_colonia", pluviometro.caserio_barrio_colonia)
            put("responsable_id", pluviometro.responsable_id)
            put("responsable_nombre", pluviometro.responsable_nombre)
            put("observaciones", pluviometro.observaciones)
            put("activo", if (pluviometro.activo) 1 else 0)
            put("fecha_modificacion", System.currentTimeMillis())
        }

        return db.update("pluviometros", values, "id = ?", arrayOf(pluviometro.id))
    }

    fun eliminar(id: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete("pluviometros", "id = ?", arrayOf(id))
    }

    fun contarActivos(): Int {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM pluviometros WHERE activo = 1",
            null
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    private fun cursorToPluviometro(cursor: Cursor): Pluviometro {
        return Pluviometro(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            numero_registro = cursor.getString(cursor.getColumnIndexOrThrow("numero_registro")),
            latitud = cursor.getDouble(cursor.getColumnIndexOrThrow("latitud")),
            longitud = cursor.getDouble(cursor.getColumnIndexOrThrow("longitud")),
            direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
            departamento = cursor.getString(cursor.getColumnIndexOrThrow("departamento")),
            municipio = cursor.getString(cursor.getColumnIndexOrThrow("municipio")),
            aldea = cursor.getString(cursor.getColumnIndexOrThrow("aldea")),
            caserio_barrio_colonia = cursor.getString(cursor.getColumnIndexOrThrow("caserio_barrio_colonia")),
            responsable_id = cursor.getLong(cursor.getColumnIndexOrThrow("responsable_id")), // ← CAMBIAR getString a getLong
            responsable_nombre = cursor.getString(cursor.getColumnIndexOrThrow("responsable_nombre")),
            observaciones = cursor.getString(cursor.getColumnIndexOrThrow("observaciones")),
            activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1,
            fecha_creacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_creacion")),
            fecha_modificacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_modificacion"))
        )
    }
}