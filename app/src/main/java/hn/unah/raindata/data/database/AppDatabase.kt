package hn.unah.raindata.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hn.unah.raindata.data.database.dao.VoluntarioDao

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "raindata_database"
        private const val DATABASE_VERSION = 2

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createVoluntariosTable = """
            CREATE TABLE voluntarios (
                id TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                direccion TEXT NOT NULL,
                departamento TEXT NOT NULL,
                municipio TEXT NOT NULL,
                aldea TEXT NOT NULL,
                caserio_barrio_colonia TEXT,
                telefono TEXT,
                email TEXT,
                cedula TEXT,
                fecha_nacimiento TEXT,
                genero TEXT,
                tipo_usuario TEXT,
                experiencia_años INTEGER,
                observaciones TEXT,
                activo INTEGER DEFAULT 1,
                fecha_creacion INTEGER,
                fecha_modificacion INTEGER
            )
        """.trimIndent()

        db.execSQL(createVoluntariosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Migrar de ocupacion a tipo_usuario
            try {
                db.execSQL("ALTER TABLE voluntarios ADD COLUMN tipo_usuario TEXT")
                db.execSQL("UPDATE voluntarios SET tipo_usuario = ocupacion WHERE ocupacion IS NOT NULL")
            } catch (e: Exception) {
                // Si falla la migración, recrear tabla
                db.execSQL("DROP TABLE IF EXISTS voluntarios")
                onCreate(db)
            }
        }
    }

    fun getVoluntarioDao(): VoluntarioDao {
        return VoluntarioDao(this)
    }
}