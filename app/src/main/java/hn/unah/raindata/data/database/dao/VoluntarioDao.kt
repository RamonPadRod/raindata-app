package hn.unah.raindata.data.database.dao

import android.content.ContentValues
import android.database.Cursor
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.Voluntario

class VoluntarioDao(private val dbHelper: AppDatabase) {

    fun insertar(voluntario: Voluntario): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", voluntario.id)
            put("nombre", voluntario.nombre)
            put("direccion", voluntario.direccion)
            put("departamento", voluntario.departamento)
            put("municipio", voluntario.municipio)
            put("aldea", voluntario.aldea)
            put("caserio_barrio_colonia", voluntario.caserio_barrio_colonia)
            put("telefono", voluntario.telefono)
            put("email", voluntario.email)
            put("cedula", voluntario.cedula)
            put("fecha_nacimiento", voluntario.fecha_nacimiento)
            put("genero", voluntario.genero)
            put("tipo_usuario", voluntario.tipo_usuario)
            put("experiencia_años", voluntario.experiencia_años)
            put("observaciones", voluntario.observaciones)
            put("activo", if (voluntario.activo) 1 else 0)
            put("fecha_creacion", voluntario.fecha_creacion)
            put("fecha_modificacion", voluntario.fecha_modificacion)
        }

        return db.insert("voluntarios", null, values)
    }

    fun obtenerActivos(): List<Voluntario> {
        val voluntarios = mutableListOf<Voluntario>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM voluntarios WHERE activo = 1 ORDER BY nombre",
            null
        )

        with(cursor) {
            while (moveToNext()) {
                voluntarios.add(cursorToVoluntario(this))
            }
        }
        cursor.close()
        return voluntarios
    }

    fun obtenerVoluntariosActivos(): List<Voluntario> {
        val voluntarios = mutableListOf<Voluntario>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM voluntarios WHERE activo = 1 AND tipo_usuario = 'Voluntario' ORDER BY nombre",
            null
        )

        with(cursor) {
            while (moveToNext()) {
                voluntarios.add(cursorToVoluntario(this))
            }
        }
        cursor.close()
        return voluntarios
    }

    fun obtenerTodos(): List<Voluntario> {
        val voluntarios = mutableListOf<Voluntario>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM voluntarios ORDER BY nombre",
            null
        )

        with(cursor) {
            while (moveToNext()) {
                voluntarios.add(cursorToVoluntario(this))
            }
        }
        cursor.close()
        return voluntarios
    }

    fun obtenerPorId(id: String): Voluntario? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM voluntarios WHERE id = ? LIMIT 1",
            arrayOf(id)
        )

        var voluntario: Voluntario? = null
        if (cursor.moveToFirst()) {
            voluntario = cursorToVoluntario(cursor)
        }
        cursor.close()
        return voluntario
    }

    // ✅ NUEVO: Verificar si un DNI ya existe
    fun existeDNI(dni: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM voluntarios WHERE cedula = ? AND activo = 1",
            arrayOf(dni)
        )

        var existe = false
        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0
        }
        cursor.close()
        return existe
    }

    // ✅ NUEVO: Obtener voluntario por DNI (para login)
    fun obtenerPorDNI(dni: String): Voluntario? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM voluntarios WHERE cedula = ? AND activo = 1 LIMIT 1",
            arrayOf(dni)
        )

        var voluntario: Voluntario? = null
        if (cursor.moveToFirst()) {
            voluntario = cursorToVoluntario(cursor)
        }
        cursor.close()
        return voluntario
    }

    fun buscar(termino: String): List<Voluntario> {
        val voluntarios = mutableListOf<Voluntario>()
        val db = dbHelper.readableDatabase
        val searchTerm = "%$termino%"
        val cursor: Cursor = db.rawQuery(
            """SELECT * FROM voluntarios 
               WHERE (nombre LIKE ? OR cedula LIKE ? OR municipio LIKE ?) 
               AND activo = 1 
               ORDER BY nombre""",
            arrayOf(searchTerm, searchTerm, searchTerm)
        )

        with(cursor) {
            while (moveToNext()) {
                voluntarios.add(cursorToVoluntario(this))
            }
        }
        cursor.close()
        return voluntarios
    }

    fun actualizar(voluntario: Voluntario): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", voluntario.nombre)
            put("direccion", voluntario.direccion)
            put("departamento", voluntario.departamento)
            put("municipio", voluntario.municipio)
            put("aldea", voluntario.aldea)
            put("caserio_barrio_colonia", voluntario.caserio_barrio_colonia)
            put("telefono", voluntario.telefono)
            put("email", voluntario.email)
            put("cedula", voluntario.cedula)
            put("fecha_nacimiento", voluntario.fecha_nacimiento)
            put("genero", voluntario.genero)
            put("tipo_usuario", voluntario.tipo_usuario)
            put("experiencia_años", voluntario.experiencia_años)
            put("observaciones", voluntario.observaciones)
            put("activo", if (voluntario.activo) 1 else 0)
            put("fecha_modificacion", System.currentTimeMillis())
        }

        return db.update("voluntarios", values, "id = ?", arrayOf(voluntario.id))
    }

    fun eliminar(id: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete("voluntarios", "id = ?", arrayOf(id))
    }

    fun contarActivos(): Int {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM voluntarios WHERE activo = 1",
            null
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    private fun cursorToVoluntario(cursor: Cursor): Voluntario {
        return Voluntario(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
            departamento = cursor.getString(cursor.getColumnIndexOrThrow("departamento")),
            municipio = cursor.getString(cursor.getColumnIndexOrThrow("municipio")),
            aldea = cursor.getString(cursor.getColumnIndexOrThrow("aldea")),
            caserio_barrio_colonia = cursor.getString(cursor.getColumnIndexOrThrow("caserio_barrio_colonia")) ?: "",
            telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
            cedula = cursor.getString(cursor.getColumnIndexOrThrow("cedula")),
            fecha_nacimiento = cursor.getString(cursor.getColumnIndexOrThrow("fecha_nacimiento")),
            genero = cursor.getString(cursor.getColumnIndexOrThrow("genero")),
            tipo_usuario = try {
                cursor.getString(cursor.getColumnIndexOrThrow("tipo_usuario"))
            } catch (e: Exception) {
                try {
                    cursor.getString(cursor.getColumnIndexOrThrow("ocupacion"))
                } catch (e2: Exception) {
                    null
                }
            },
            experiencia_años = if (cursor.isNull(cursor.getColumnIndexOrThrow("experiencia_años"))) null else cursor.getInt(cursor.getColumnIndexOrThrow("experiencia_años")),
            observaciones = cursor.getString(cursor.getColumnIndexOrThrow("observaciones")),
            activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1,
            fecha_creacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_creacion")),
            fecha_modificacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_modificacion"))
        )
    }
}